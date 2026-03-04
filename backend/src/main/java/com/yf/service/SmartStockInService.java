package com.yf.service;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yf.ai.BaseAiService;
import com.yf.dto.SmartStockInResult;
import com.yf.dto.SmartStockInResult.*;
import com.yf.entity.Drug;
import com.yf.entity.StockIn;
import com.yf.entity.StockInDetail;
import com.yf.entity.Supplier;
import com.yf.mapper.DrugMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能入库解析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartStockInService {

    private final BaseAiService baseAiService;
    private final SupplierService supplierService;
    private final DrugService drugService;
    private final DrugMapper drugMapper;
    private final StockInService stockInService;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            【重要】你必须且只能返回JSON格式，禁止返回任何其他文字、解释或说明。
            【重要】所有JSON字段名必须使用英文，禁止使用中文字段名。

            你是药品入库单/送货单/发票识别专家。注意：图片可能是旋转的（横向拍摄），请先识别正确的阅读方向。

            从图片提取信息后，必须严格按以下JSON结构返回（字段名不可更改）：
            {
              "supplier": {
                "name": "供货单位/开票单位名称",
                "creditCode": "信用代码或null",
                "contactPhone": "电话或null"
              },
              "invoiceDate": "单据日期yyyy-MM-dd",
              "drugs": [
                {
                  "genericName": "通用名/品名",
                  "specification": "规格",
                  "manufacturer": "生产厂家",
                  "approvalNo": "批准文号/国药准字",
                  "batchNo": "批号/生产批号",
                  "produceDate": "生产日期yyyy-MM-dd或null",
                  "expireDate": "有效期至yyyy-MM-dd",
                  "quantity": 数量,
                  "unit": "单位",
                  "purchasePrice": 单价,
                  "amount": 金额
                }
              ]
            }

            规则：
            1. 【强制】响应必须是纯JSON，以{开头以}结尾
            2. 【强制】字段名必须用英文，与上述示例完全一致
            3. 日期格式yyyy-MM-dd，价格为数字（保留原始精度），无法识别填null
            4. supplier.name取"开票单位"/"销售单位"/"供货单位"
            5. 如果图片不是单据，返回：{"error":"非入库单文档"}
            """;

    private static final String USER_PROMPT = "识别图片中的入库单信息，直接输出JSON。";

    // ==================== 第一阶段：解析（只读，不创建任何数据） ====================

    /**
     * 解析多张单据（只解析和匹配，不创建供应商/药品）
     */
    public SmartStockInResult parseMultiple(List<FileItem> files) {
        if (files == null || files.isEmpty()) {
            return SmartStockInResult.fail("请上传至少一张单据图片");
        }

        try {
            // 步骤1: 逐张AI解析
            List<ParsedInvoice> parsedList = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                FileItem file = files.get(i);
                log.info("解析第 {}/{} 张单据", i + 1, files.size());
                ParsedInvoice parsed = parseOneFile(file);
                if (parsed != null) {
                    parsedList.add(parsed);
                }
            }

            if (parsedList.isEmpty()) {
                return SmartStockInResult.fail("所有单据均未能识别，请检查图片是否为入库单/发票/送货单");
            }

            // 步骤2: 按 (供应商名, 日期) 分组
            Map<String, List<ParsedInvoice>> groups = parsedList.stream()
                    .collect(Collectors.groupingBy(p -> normalizeSupplierName(p.supplierName) + "_" + (p.invoiceDate != null ? p.invoiceDate : "unknown")));

            // 步骤3: 每组处理（只匹配，不创建）
            SmartStockInResult result = new SmartStockInResult();
            result.setSuccess(true);
            int newDrugsCount = 0;
            int newSuppliersCount = 0;

            for (Map.Entry<String, List<ParsedInvoice>> entry : groups.entrySet()) {
                SmartStockInOrder order = buildOrderPreview(entry.getKey(), entry.getValue());
                result.getOrders().add(order);
                newDrugsCount += order.getDrugsCreated();
                if (order.isSupplierNewCreated()) {
                    newSuppliersCount++;
                }
            }

            result.setTotalDrugsCreated(newDrugsCount);
            result.setTotalSuppliersCreated(newSuppliersCount);
            return result;

        } catch (Exception e) {
            log.error("智能入库解析失败", e);
            return SmartStockInResult.fail("解析失败: " + e.getMessage());
        }
    }

    /**
     * 构建分组预览（只匹配已有数据，标记哪些需要新建）
     */
    private SmartStockInOrder buildOrderPreview(String groupKey, List<ParsedInvoice> invoices) {
        SmartStockInOrder order = new SmartStockInOrder();
        order.setGroupKey(groupKey);

        ParsedInvoice firstInvoice = invoices.get(0);
        order.setInvoiceDate(firstInvoice.invoiceDate);
        order.setSupplierName(firstInvoice.supplierName);
        order.setSupplierCreditCode(firstInvoice.supplierCreditCode);
        order.setSupplierPhone(firstInvoice.supplierPhone);

        // 只匹配供应商，不创建
        SupplierMatchResult supplierResult = matchSupplier(firstInvoice);
        order.setSupplierId(supplierResult.supplierId);
        order.setSupplierNewCreated(supplierResult.isNewCreated);

        // 合并所有药品明细
        List<ParsedDrug> allDrugs = invoices.stream()
                .flatMap(inv -> inv.drugs.stream())
                .collect(Collectors.toList());

        int newDrugsCount = 0;
        int rowIndex = 0;
        for (ParsedDrug parsedDrug : allDrugs) {
            SmartDetailItem item = buildDrugPreview(parsedDrug, ++rowIndex);
            order.getDetails().add(item);
            if (item.isDrugNewCreated()) {
                newDrugsCount++;
            }
        }
        order.setDrugsCreated(newDrugsCount);

        // 字段完整性检测
        detectIncompleteFields(order, firstInvoice);

        return order;
    }

    /**
     * 只匹配供应商（不创建）
     */
    private SupplierMatchResult matchSupplier(ParsedInvoice invoice) {
        SupplierMatchResult result = new SupplierMatchResult();
        result.supplierName = invoice.supplierName;

        if (!StringUtils.hasText(invoice.supplierName)) {
            result.isNewCreated = true;
            return result;
        }

        // 搜索现有供应商
        List<Supplier> existing = supplierService.search(invoice.supplierName);
        for (Supplier s : existing) {
            if (normalizeSupplierName(s.getName()).equals(normalizeSupplierName(invoice.supplierName))) {
                result.supplierId = s.getId();
                result.supplierName = s.getName();
                result.isNewCreated = false;
                return result;
            }
        }

        // 未找到，标记为需要新建
        result.isNewCreated = true;
        return result;
    }

    /**
     * 构建药品预览（只匹配，不创建）
     */
    private SmartDetailItem buildDrugPreview(ParsedDrug parsed, int rowIndex) {
        SmartDetailItem item = new SmartDetailItem();
        item.setRowIndex(rowIndex);
        item.setGenericName(parsed.genericName);
        item.setTradeName(parsed.tradeName);
        item.setSpecification(parsed.specification);
        item.setManufacturer(parsed.manufacturer);
        item.setApprovalNo(parsed.approvalNo);
        item.setDosageForm(parsed.dosageForm);
        item.setBarcode(parsed.barcode);
        item.setMarketingAuthHolder(parsed.marketingAuthHolder);
        item.setBatchNo(parsed.batchNo);
        item.setProduceDate(parsed.produceDate);
        item.setExpireDate(parsed.expireDate);
        item.setQuantity(parsed.quantity);
        item.setUnit(parsed.unit);
        item.setPurchasePrice(parsed.purchasePrice != null ? parsed.purchasePrice.setScale(4, RoundingMode.HALF_UP) : null);
        item.setAmount(parsed.amount != null ? parsed.amount.setScale(2, RoundingMode.HALF_UP) : null);

        // 只匹配已有药品
        Drug existingDrug = findDrug(parsed);
        if (existingDrug != null) {
            item.setDrugId(existingDrug.getId());
            item.setDrugNewCreated(false);
            // 补充缺失信息
            if (!StringUtils.hasText(item.getSpecification())) {
                item.setSpecification(existingDrug.getSpecification());
            }
            if (!StringUtils.hasText(item.getManufacturer())) {
                item.setManufacturer(existingDrug.getManufacturer());
            }
            if (!StringUtils.hasText(item.getUnit())) {
                item.setUnit(existingDrug.getUnit());
            }
        } else {
            // 标记为需要新建（drugId为null）
            item.setDrugNewCreated(true);
        }

        // 检测缺失字段
        List<String> missing = new ArrayList<>();
        if (!StringUtils.hasText(parsed.batchNo)) missing.add("batchNo");
        if (!StringUtils.hasText(parsed.expireDate)) missing.add("expireDate");
        if (parsed.purchasePrice == null) missing.add("purchasePrice");
        if (parsed.quantity == null || parsed.quantity <= 0) missing.add("quantity");
        if (!StringUtils.hasText(parsed.approvalNo)) missing.add("approvalNo");
        if (!StringUtils.hasText(parsed.specification)) missing.add("specification");
        if (!StringUtils.hasText(parsed.manufacturer)) missing.add("manufacturer");
        item.setMissingFields(missing);

        return item;
    }

    // ==================== 第二阶段：批量创建（用户确认后调用） ====================

    /**
     * 批量创建入库单（创建供应商 → 创建药品 → 创建入库单+明细）
     */
    @Transactional(rollbackFor = Exception.class)
    public SmartStockInResult batchCreateOrders(SmartStockInResult parseResult) {
        if (parseResult == null || parseResult.getOrders() == null || parseResult.getOrders().isEmpty()) {
            return SmartStockInResult.fail("无可创建的入库单");
        }

        int totalDrugsCreated = 0;
        int totalSuppliersCreated = 0;

        for (SmartStockInOrder order : parseResult.getOrders()) {
            // 1. 创建供应商（如果需要）
            if (order.isSupplierNewCreated() || order.getSupplierId() == null) {
                Supplier newSupplier = new Supplier();
                newSupplier.setName(StringUtils.hasText(order.getSupplierName()) ? order.getSupplierName() : "未知供应商_" + System.currentTimeMillis());
                newSupplier.setCreditCode(order.getSupplierCreditCode());
                newSupplier.setContactPhone(order.getSupplierPhone());
                newSupplier.setStatus("active");
                supplierService.create(newSupplier);
                order.setSupplierId(newSupplier.getId());
                order.setSupplierName(newSupplier.getName());
                totalSuppliersCreated++;
                log.info("创建供应商: {} -> id={}", newSupplier.getName(), newSupplier.getId());
            }

            // 2. 创建药品（如果需要）并构建入库明细
            List<StockInDetail> stockInDetails = new ArrayList<>();
            for (SmartDetailItem item : order.getDetails()) {
                // 创建新药品
                if (item.isDrugNewCreated() || item.getDrugId() == null) {
                    Drug newDrug = new Drug();
                    newDrug.setGenericName(item.getGenericName());
                    newDrug.setTradeName(item.getTradeName());
                    newDrug.setSpecification(item.getSpecification());
                    newDrug.setManufacturer(item.getManufacturer());
                    newDrug.setApprovalNo(item.getApprovalNo());
                    newDrug.setDosageForm(item.getDosageForm());
                    newDrug.setMarketingAuthHolder(item.getMarketingAuthHolder());
                    newDrug.setBarcode(item.getBarcode());
                    newDrug.setUnit(item.getUnit());
                    newDrug.setPurchasePrice(item.getPurchasePrice());
                    newDrug.setStatus("正常");
                    if (StringUtils.hasText(item.getGenericName())) {
                        newDrug.setPinyin(PinyinUtil.getPinyin(item.getGenericName(), ""));
                        newDrug.setPinyinShort(PinyinUtil.getFirstLetter(item.getGenericName(), ""));
                    }
                    drugService.create(newDrug);
                    item.setDrugId(newDrug.getId());
                    totalDrugsCreated++;
                    log.info("创建药品: {} -> id={}", newDrug.getGenericName(), newDrug.getId());
                }

                // 构建入库明细
                StockInDetail detail = new StockInDetail();
                detail.setDrugId(item.getDrugId());
                detail.setBatchNo(StringUtils.hasText(item.getBatchNo()) ? item.getBatchNo() : "");
                detail.setProduceDate(parseDateSafe(item.getProduceDate()));
                detail.setExpireDate(parseDateSafe(item.getExpireDate()));
                detail.setQuantity(item.getQuantity() != null ? new BigDecimal(item.getQuantity()) : BigDecimal.ONE);
                detail.setUnit(StringUtils.hasText(item.getUnit()) ? item.getUnit() : "");
                detail.setPurchasePrice(item.getPurchasePrice() != null ? item.getPurchasePrice() : BigDecimal.ZERO);
                detail.setAmount(item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO);
                stockInDetails.add(detail);
            }

            // 3. 创建入库单
            StockIn stockIn = new StockIn();
            stockIn.setType("采购入库");
            stockIn.setSupplierId(order.getSupplierId());
            stockIn.setRemark("智能入库 - " + (order.getInvoiceDate() != null ? order.getInvoiceDate() : ""));
            stockInService.create(stockIn, stockInDetails);
            log.info("创建入库单: 供应商={}, 明细数={}", order.getSupplierName(), stockInDetails.size());
        }

        parseResult.setTotalDrugsCreated(totalDrugsCreated);
        parseResult.setTotalSuppliersCreated(totalSuppliersCreated);
        return parseResult;
    }

    private LocalDate parseDateSafe(String dateStr) {
        if (!StringUtils.hasText(dateStr)) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            log.debug("日期解析失败: {}", dateStr);
            return null;
        }
    }

    // ==================== AI解析逻辑 ====================

    private ParsedInvoice parseOneFile(FileItem file) {
        try {
            String base64 = file.getFileBase64();

            log.info("开始调用AI识别...");
            String response = baseAiService.visionBase64(SYSTEM_PROMPT, USER_PROMPT, base64);
            log.info("AI原始响应: {}", response);
            try {
                java.nio.file.Files.writeString(java.nio.file.Path.of("F:/yf/ai_response_debug.txt"), response,
                    java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception writeEx) {
                log.debug("写入调试文件失败", writeEx);
            }

            String jsonStr = extractJson(response);
            if (jsonStr == null) {
                log.error("无法从AI响应中提取JSON");
                return null;
            }

            JsonNode root = objectMapper.readTree(jsonStr);

            List<String> rootFields = new ArrayList<>();
            root.fieldNames().forEachRemaining(rootFields::add);
            log.info("JSON根级字段: {}", rootFields);

            if (root.has("error")) {
                log.warn("AI识别失败: {}", root.get("error").asText());
                return null;
            }

            ParsedInvoice parsed = new ParsedInvoice();

            // ---- 供应商信息 ----
            JsonNode supplierNode = root.get("supplier");
            JsonNode headerNode = root.get("header");
            if (supplierNode != null) {
                parsed.supplierName = getTextOrNull(supplierNode, "name");
                parsed.supplierCreditCode = getTextOrNull(supplierNode, "creditCode");
                parsed.supplierPhone = getTextOrNull(supplierNode, "contactPhone");
            } else if (headerNode != null) {
                parsed.supplierName = getTextOrNull(headerNode, "company");
                parsed.supplierCreditCode = getTextOrNull(headerNode, "credit_code");
                parsed.supplierPhone = getTextOrNull(headerNode, "contact_phone");
            } else {
                parsed.supplierName = getFirstNonNull(root, "公司名称", "开票单位", "销售单位", "供货单位", "供应商");
                parsed.supplierPhone = getFirstNonNull(root, "电话", "联系电话");

                if (parsed.supplierName == null) {
                    Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        String key = entry.getKey();
                        if ((key.contains("公司") || key.contains("单位") || key.contains("供")) &&
                            entry.getValue().isTextual()) {
                            parsed.supplierName = entry.getValue().asText();
                            log.info("通过遍历找到供应商名称，字段名: {}, 值: {}", key, parsed.supplierName);
                            break;
                        }
                    }
                }
            }

            // ---- 单据日期 ----
            parsed.invoiceDate = getTextOrNull(root, "invoiceDate");
            if (parsed.invoiceDate == null && headerNode != null) {
                parsed.invoiceDate = getTextOrNull(headerNode, "delivery_date");
                if (parsed.invoiceDate == null) parsed.invoiceDate = getTextOrNull(headerNode, "issue_date");
            }
            if (parsed.invoiceDate == null) {
                parsed.invoiceDate = getFirstNonNull(root, "单据日期", "开票日期", "出库日期", "日期",
                        "开票日期/出库日期", "发货日期", "销售日期", "发货日期/销售日期", "送货日期");
            }
            if (parsed.invoiceDate == null) {
                Iterator<Map.Entry<String, JsonNode>> dateFields = root.fields();
                while (dateFields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = dateFields.next();
                    String key = entry.getKey();
                    if (key.contains("日期") && entry.getValue().isTextual()) {
                        String val = entry.getValue().asText();
                        if (val.matches("\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}.*")) {
                            parsed.invoiceDate = val.substring(0, Math.min(val.length(), 10));
                            log.info("通过遍历找到发票日期，字段名: {}, 值: {}", key, parsed.invoiceDate);
                            break;
                        }
                    }
                }
            }
            parsed.invoiceDate = normalizeDateStr(parsed.invoiceDate);

            // ---- 药品明细 ----
            JsonNode drugsNode = root.get("drugs");
            if (drugsNode == null) drugsNode = root.get("items");
            if (drugsNode == null) drugsNode = root.get("药品信息");
            if (drugsNode == null) drugsNode = root.get("商品信息");
            if (drugsNode == null) drugsNode = root.get("商品明细");
            if (drugsNode == null) drugsNode = root.get("药品明细");
            if (drugsNode == null) drugsNode = root.get("明细");

            if (drugsNode == null) {
                Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    if (entry.getValue().isArray() && entry.getValue().size() > 0) {
                        JsonNode firstItem = entry.getValue().get(0);
                        if (firstItem.isObject()) {
                            List<String> itemFields = new ArrayList<>();
                            firstItem.fieldNames().forEachRemaining(itemFields::add);
                            if (itemFields.size() >= 3) {
                                drugsNode = entry.getValue();
                                log.info("通过遍历找到药品数组，字段名: {}", entry.getKey());
                                break;
                            }
                        }
                    }
                }
            }

            if (drugsNode != null && drugsNode.isArray()) {
                for (JsonNode drugNode : drugsNode) {
                    ParsedDrug drug = new ParsedDrug();
                    List<String> fieldNames = new ArrayList<>();
                    drugNode.fieldNames().forEachRemaining(fieldNames::add);

                    drug.genericName = getFirstNonNull(drugNode, "genericName", "generic_name",
                            "通用名称", "通用名", "品名", "药品名称", "名称", "商品名称", "商品名", "药品名", "药名",
                            "product_name", "drug_name", "name");

                    if (drug.genericName == null) {
                        for (String fn : fieldNames) {
                            if (fn.contains("名") && !fn.contains("厂")) {
                                drug.genericName = getTextOrNull(drugNode, fn);
                                if (drug.genericName != null) break;
                            }
                        }
                    }
                    if (drug.genericName == null && fieldNames.size() > 1) {
                        for (String fn : fieldNames) {
                            if (!fn.equals("序号") && !fn.contains("数量") && !fn.contains("单价") &&
                                !fn.contains("金额") && !fn.contains("规格") && !fn.contains("批号") &&
                                !fn.contains("厂") && !fn.contains("单位") && !fn.contains("有效") &&
                                !fn.contains("日期") && !fn.contains("price") && !fn.contains("amount") &&
                                drugNode.get(fn).isTextual()) {
                                drug.genericName = drugNode.get(fn).asText();
                                break;
                            }
                        }
                    }

                    drug.tradeName = getFirstNonNull(drugNode, "tradeName", "trade_name", "商品名", "商品名称");
                    drug.specification = getFirstNonNull(drugNode, "specification", "spec", "规格", "规格/型号", "规格型号");
                    drug.manufacturer = getFirstNonNull(drugNode, "manufacturer", "mfr", "生产企业", "生产厂家", "厂家", "生产商", "生产企业/生产厂商", "厂商");
                    drug.approvalNo = getFirstNonNull(drugNode, "approvalNo", "approval_number", "approval_no", "批准文号", "国药准字", "批准文号/注册证号/备案凭证", "文号");
                    drug.barcode = getFirstNonNull(drugNode, "barcode", "bar_code", "条码", "条形码");
                    drug.dosageForm = getFirstNonNull(drugNode, "dosageForm", "dosage_form", "剂型");
                    drug.marketingAuthHolder = getFirstNonNull(drugNode, "marketingAuthHolder", "marketing_auth_holder", "上市许可持有人", "持有人");
                    drug.batchNo = getFirstNonNull(drugNode, "batchNo", "batch_number", "batch_no", "批号", "生产批号", "批号/生产批号");
                    drug.produceDate = normalizeDateStr(getFirstNonNull(drugNode, "produceDate", "production_date", "produce_date", "生产日期"));
                    drug.expireDate = normalizeDateStr(getFirstNonNull(drugNode, "expireDate", "expiry_date", "expire_date", "有效期至", "有效期", "有效日期", "失效日期"));
                    drug.quantity = parseQuantityMulti(drugNode, "quantity", "数量", "qty", "入库数量", "发货数量", "实收数量");
                    drug.unit = getFirstNonNull(drugNode, "unit", "单位", "计量单位");
                    drug.purchasePrice = parsePriceMulti(drugNode, "purchasePrice", "purchase_price", "unit_price", "price", "单价", "进价", "进货价");
                    drug.amount = parsePriceMulti(drugNode, "amount", "total_amount", "subtotal", "金额", "total", "合计", "小计");

                    if (StringUtils.hasText(drug.genericName)) {
                        parsed.drugs.add(drug);
                    } else {
                        log.warn("跳过无品名的药品记录, 该节点全部字段值: {}", drugNode.toString());
                    }
                }
            }

            return parsed;
        } catch (Exception e) {
            log.error("解析单张单据失败", e);
            return null;
        }
    }

    // ==================== 药品匹配 ====================

    private Drug findDrug(ParsedDrug parsed) {
        if (StringUtils.hasText(parsed.barcode)) {
            LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Drug::getBarcode, parsed.barcode).last("LIMIT 1");
            Drug drug = drugMapper.selectOne(wrapper);
            if (drug != null) return drug;
        }
        if (StringUtils.hasText(parsed.approvalNo)) {
            LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Drug::getApprovalNo, parsed.approvalNo).last("LIMIT 1");
            Drug drug = drugMapper.selectOne(wrapper);
            if (drug != null) return drug;
        }
        if (StringUtils.hasText(parsed.genericName)) {
            LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Drug::getGenericName, parsed.genericName);
            if (StringUtils.hasText(parsed.specification)) wrapper.eq(Drug::getSpecification, parsed.specification);
            if (StringUtils.hasText(parsed.manufacturer)) wrapper.eq(Drug::getManufacturer, parsed.manufacturer);
            wrapper.last("LIMIT 1");
            Drug drug = drugMapper.selectOne(wrapper);
            if (drug != null) return drug;
        }
        if (StringUtils.hasText(parsed.genericName)) {
            LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(Drug::getGenericName, parsed.genericName).last("LIMIT 1");
            return drugMapper.selectOne(wrapper);
        }
        return null;
    }

    // ==================== 字段检测 ====================

    private void detectIncompleteFields(SmartStockInOrder order, ParsedInvoice invoice) {
        Map<String, String> fieldLabels = Map.of(
                "batchNo", "批号", "expireDate", "有效期", "purchasePrice", "进货价",
                "quantity", "数量", "approvalNo", "批准文号", "specification", "规格",
                "manufacturer", "生产厂家", "produceDate", "生产日期",
                "creditCode", "信用代码", "contactPhone", "联系电话"
        );

        if (!StringUtils.hasText(invoice.supplierCreditCode)) {
            addIncompleteField(order, "SUPPLIER", -1, "creditCode", fieldLabels.get("creditCode"), "WARNING");
        }
        if (!StringUtils.hasText(invoice.supplierPhone)) {
            addIncompleteField(order, "SUPPLIER", -1, "contactPhone", fieldLabels.get("contactPhone"), "WARNING");
        }
        for (SmartDetailItem item : order.getDetails()) {
            for (String field : item.getMissingFields()) {
                String severity = List.of("batchNo", "expireDate", "purchasePrice", "quantity").contains(field) ? "ERROR" : "WARNING";
                addIncompleteField(order, "DRUG", item.getRowIndex(), field, fieldLabels.getOrDefault(field, field), severity);
            }
        }
    }

    private void addIncompleteField(SmartStockInOrder order, String scope, int rowIndex, String fieldName, String fieldLabel, String severity) {
        IncompleteField f = new IncompleteField();
        f.setScope(scope);
        f.setRowIndex(rowIndex);
        f.setFieldName(fieldName);
        f.setFieldLabel(fieldLabel);
        f.setSeverity(severity);
        order.getIncompleteFields().add(f);
    }

    // ==================== 工具方法 ====================

    private String normalizeSupplierName(String name) {
        if (name == null) return "";
        return name.trim().replaceAll("\\s+", "").replaceAll("（", "(").replaceAll("）", ")");
    }

    private String normalizeDateStr(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        String normalized = dateStr.trim().replace("/", "-");
        if (normalized.matches("\\d{4}-\\d{1,2}")) normalized = normalized + "-01";
        if (normalized.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
            String[] parts = normalized.split("-");
            normalized = parts[0] + "-"
                    + (parts[1].length() == 1 ? "0" + parts[1] : parts[1]) + "-"
                    + (parts[2].length() == 1 ? "0" + parts[2] : parts[2]);
        }
        return normalized;
    }

    private String getTextOrNull(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) return null;
        String text = node.get(field).asText();
        return StringUtils.hasText(text) && !"null".equalsIgnoreCase(text) ? text : null;
    }

    private String getFirstNonNull(JsonNode node, String... fieldNames) {
        for (String field : fieldNames) {
            String value = getTextOrNull(node, field);
            if (value != null) return value;
        }
        return null;
    }

    private Integer parseQuantityMulti(JsonNode node, String... fieldNames) {
        for (String field : fieldNames) {
            if (node.has(field) && !node.get(field).isNull()) {
                try {
                    JsonNode valueNode = node.get(field);
                    if (valueNode.isNumber()) return valueNode.asInt();
                    String text = valueNode.asText().trim();
                    if (StringUtils.hasText(text) && !"null".equalsIgnoreCase(text)) {
                        return (int) Double.parseDouble(text);
                    }
                } catch (Exception e) {
                    log.debug("解析数量失败: field={}", field);
                }
            }
        }
        return null;
    }

    private BigDecimal parsePriceMulti(JsonNode node, String... fieldNames) {
        for (String field : fieldNames) {
            if (node.has(field) && !node.get(field).isNull()) {
                try {
                    String text = node.get(field).asText().trim();
                    if (StringUtils.hasText(text) && !"null".equalsIgnoreCase(text)) {
                        text = text.replaceAll("[¥￥$,，]", "");
                        return new BigDecimal(text);
                    }
                } catch (Exception e) {
                    log.debug("解析价格失败: field={}", field);
                }
            }
        }
        return null;
    }

    private String extractJson(String response) {
        if (response == null || response.isBlank()) return null;
        String trimmed = response.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) return trimmed;

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```");
        java.util.regex.Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String extracted = matcher.group(1).trim();
            if (extracted.startsWith("{") && extracted.endsWith("}")) return extracted;
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            String extracted = trimmed.substring(start, end + 1);
            try {
                objectMapper.readTree(extracted);
                return extracted;
            } catch (Exception e) {
                log.debug("提取的JSON无效: {}", extracted);
            }
        }
        return null;
    }

    // ==================== 内部数据类 ====================

    @Data
    public static class FileItem {
        private String fileBase64;
        private String fileType;
    }

    @Data
    private static class ParsedInvoice {
        String supplierName;
        String supplierCreditCode;
        String supplierPhone;
        String invoiceDate;
        List<ParsedDrug> drugs = new ArrayList<>();
    }

    @Data
    private static class ParsedDrug {
        String genericName;
        String tradeName;
        String specification;
        String manufacturer;
        String approvalNo;
        String dosageForm;
        String barcode;
        String marketingAuthHolder;
        String batchNo;
        String produceDate;
        String expireDate;
        Integer quantity;
        String unit;
        BigDecimal purchasePrice;
        BigDecimal amount;
    }

    @Data
    private static class SupplierMatchResult {
        Long supplierId;
        String supplierName;
        boolean isNewCreated;
    }
}
