package com.yf.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.dto.*;
import com.yf.entity.Drug;
import com.yf.entity.DrugCategory;
import com.yf.entity.Member;
import com.yf.exception.BusinessException;
import com.yf.mapper.DrugCategoryMapper;
import com.yf.mapper.DrugMapper;
import com.yf.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchImportService {

    private final MemberMapper memberMapper;
    private final DrugMapper drugMapper;
    private final DrugCategoryMapper drugCategoryMapper;
    private final DrugService drugService;

    // fileToken -> filePath 映射
    private final ConcurrentHashMap<String, String> fileTokenMap = new ConcurrentHashMap<>();

    // ========== 字段别名配置 ==========

    private static final LinkedHashMap<String, FieldDef> MEMBER_FIELDS = new LinkedHashMap<>();
    private static final LinkedHashMap<String, FieldDef> DRUG_FIELDS = new LinkedHashMap<>();

    static {
        // 会员字段别名
        MEMBER_FIELDS.put("name", new FieldDef("姓名", true, "姓名", "名字", "会员姓名", "名称", "name"));
        MEMBER_FIELDS.put("phone", new FieldDef("手机号", true, "手机号", "电话", "手机", "联系电话", "联系方式", "手机号码", "phone", "tel"));
        MEMBER_FIELDS.put("gender", new FieldDef("性别", false, "性别", "gender", "sex"));
        MEMBER_FIELDS.put("birthday", new FieldDef("生日", false, "生日", "出生日期", "出生", "生日日期", "birthday"));
        MEMBER_FIELDS.put("idCard", new FieldDef("身份证号", false, "身份证号", "身份证", "证件号", "身份证号码", "idCard"));
        MEMBER_FIELDS.put("points", new FieldDef("积分", false, "积分", "积分余额", "会员积分", "points"));
        MEMBER_FIELDS.put("allergyInfo", new FieldDef("过敏信息", false, "过敏信息", "过敏", "过敏史"));
        MEMBER_FIELDS.put("chronicDisease", new FieldDef("慢性病史", false, "慢性病史", "慢性病", "慢病", "病史"));

        // 药品字段别名
        DRUG_FIELDS.put("genericName", new FieldDef("通用名", true, "通用名", "通用名称", "药品名", "药品名称", "名称", "genericName"));
        DRUG_FIELDS.put("tradeName", new FieldDef("商品名", false, "商品名", "商品名称", "品名", "tradeName", "本企业自定名称", "自定名称", "自定义名称"));
        DRUG_FIELDS.put("categoryName", new FieldDef("药品分类", false, "药品分类", "分类", "类别", "药品类别", "category", "商品分类"));
        DRUG_FIELDS.put("specification", new FieldDef("规格", false, "规格", "spec", "specification"));
        DRUG_FIELDS.put("dosageForm", new FieldDef("剂型", false, "剂型", "剂形", "dosageForm"));
        DRUG_FIELDS.put("manufacturer", new FieldDef("生产企业", false, "生产企业", "生产厂家", "生产厂商", "厂家", "厂商", "生产商", "manufacturer", "制造商"));
        DRUG_FIELDS.put("approvalNo", new FieldDef("批准文号", false, "批准文号", "国药准字", "文号", "approvalNo", "标准号", "注册证号", "注册号"));
        DRUG_FIELDS.put("barcode", new FieldDef("条形码", false, "条形码", "条码", "商品条码", "barcode", "ean"));
        DRUG_FIELDS.put("originalCode", new FieldDef("原编号", false, "原编号", "原系统编号", "商品编号", "编号", "旧编号", "商品编码"));
        DRUG_FIELDS.put("unit", new FieldDef("单位", false, "单位", "包装单位", "unit"));
        DRUG_FIELDS.put("retailPrice", new FieldDef("零售价", false, "零售价", "售价", "商品零售价", "零售", "retail", "retailPrice", "商品销售价", "销售价"));
        DRUG_FIELDS.put("purchasePrice", new FieldDef("采购价", false, "采购价", "进价", "进货价", "成本价", "purchasePrice", "实际购价", "购价"));
        DRUG_FIELDS.put("memberPrice", new FieldDef("会员价", false, "会员价", "会员售价", "vip价", "memberPrice"));
        DRUG_FIELDS.put("otcType", new FieldDef("OTC类型", false, "OTC类型", "OTC", "处方类型", "药品类型"));
        DRUG_FIELDS.put("storageCondition", new FieldDef("储存条件", false, "储存条件", "储存", "贮藏", "存储条件"));
        DRUG_FIELDS.put("medicalInsurance", new FieldDef("医保类型", false, "医保类型", "医保", "医保编码"));
        DRUG_FIELDS.put("validPeriod", new FieldDef("有效期", false, "有效期", "有效期(月)", "有效期月", "有效月数"));
        DRUG_FIELDS.put("origin", new FieldDef("产地", false, "产地", "产地/来源", "来源"));
        DRUG_FIELDS.put("marketingAuthHolder", new FieldDef("上市许可持有人", false, "上市许可持有人", "持有人", "MAH"));
        DRUG_FIELDS.put("stockQuantity", new FieldDef("库存数量", false, "库存数量", "库存", "现有库存"));
        DRUG_FIELDS.put("requireRealName", new FieldDef("实名登记", false, "实名登记", "实名", "需实名"));
        DRUG_FIELDS.put("stockUpperLimit", new FieldDef("库存上限", false, "库存上限", "最大库存", "上限"));
        DRUG_FIELDS.put("stockLowerLimit", new FieldDef("库存下限", false, "库存下限", "最小库存", "下限", "安全库存"));
        DRUG_FIELDS.put("allowPriceAdjust", new FieldDef("销售可调价", false, "销售可调价", "可调价", "允许调价"));
        DRUG_FIELDS.put("maintenanceMethod", new FieldDef("养护方式", false, "养护方式", "养护", "养护方法"));
    }

    // ========== 解析 Excel ==========

    public ImportParseResponse parseExcel(MultipartFile file, String module) {
        // 校验文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        if (!".xlsx".equalsIgnoreCase(extension) && !".xls".equalsIgnoreCase(extension)) {
            throw new BusinessException("仅支持 Excel (.xlsx/.xls) 格式文件");
        }

        // 保存临时文件
        String fileToken = UUID.randomUUID().toString().replace("-", "");
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "import";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filePath = uploadDir + File.separator + fileToken + extension;
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }
        fileTokenMap.put(fileToken, filePath);

        // 读取表头和预览数据
        List<String> headers = new ArrayList<>();
        List<Map<Integer, String>> previewRows = new ArrayList<>();
        AtomicInteger totalRows = new AtomicInteger(0);

        EasyExcel.read(filePath, new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                totalRows.incrementAndGet();
                if (previewRows.size() < 5) {
                    previewRows.add(row);
                }
            }

            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                // 按索引排序收集表头
                headMap.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            while (headers.size() < entry.getKey()) {
                                headers.add("");
                            }
                            headers.add(entry.getValue() != null ? entry.getValue().trim() : "");
                        });
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        }).headRowNumber(1).sheet().doRead();

        // 获取字段定义
        LinkedHashMap<String, FieldDef> fieldDefs = "member".equals(module) ? MEMBER_FIELDS : DRUG_FIELDS;

        // 自动映射
        List<FieldMapping> fieldMappings = autoMapFields(headers, fieldDefs);

        // 构建可用字段列表
        List<FieldMapping> availableFields = new ArrayList<>();
        fieldDefs.forEach((field, def) -> {
            FieldMapping fm = new FieldMapping();
            fm.setEntityField(field);
            fm.setEntityFieldLabel(def.label);
            fm.setRequired(def.required);
            availableFields.add(fm);
        });

        // 构建预览数据（使用映射后的字段名）
        List<Map<String, String>> previewData = new ArrayList<>();
        for (Map<Integer, String> row : previewRows) {
            Map<String, String> mappedRow = new LinkedHashMap<>();
            for (FieldMapping fm : fieldMappings) {
                if (StringUtils.hasText(fm.getEntityField())) {
                    String val = row.get(fm.getExcelIndex());
                    mappedRow.put(fm.getEntityField(), val != null ? val.trim() : "");
                }
            }
            previewData.add(mappedRow);
        }

        // 构建响应
        ImportParseResponse response = new ImportParseResponse();
        response.setFileToken(fileToken);
        response.setHeaders(headers);
        response.setFieldMappings(fieldMappings);
        response.setAvailableFields(availableFields);
        response.setPreviewData(previewData);
        response.setTotalRows(totalRows.get());
        return response;
    }

    // ========== 自动映射算法 ==========

    private List<FieldMapping> autoMapFields(List<String> excelHeaders, LinkedHashMap<String, FieldDef> fieldDefs) {
        List<FieldMapping> result = new ArrayList<>();
        Set<String> usedFields = new HashSet<>();

        for (int i = 0; i < excelHeaders.size(); i++) {
            String header = excelHeaders.get(i).trim().toLowerCase();
            FieldMapping fm = new FieldMapping();
            fm.setExcelIndex(i);
            fm.setExcelHeader(excelHeaders.get(i));

            // 尝试匹配
            String matchedField = null;
            for (Map.Entry<String, FieldDef> entry : fieldDefs.entrySet()) {
                if (usedFields.contains(entry.getKey())) {
                    continue;
                }
                for (String alias : entry.getValue().aliases) {
                    if (alias.toLowerCase().equals(header)) {
                        matchedField = entry.getKey();
                        break;
                    }
                }
                if (matchedField != null) {
                    break;
                }
            }

            if (matchedField != null) {
                FieldDef def = fieldDefs.get(matchedField);
                fm.setEntityField(matchedField);
                fm.setEntityFieldLabel(def.label);
                fm.setRequired(def.required);
                usedFields.add(matchedField);
            } else {
                fm.setEntityField("");
                fm.setEntityFieldLabel("");
                fm.setRequired(false);
            }
            result.add(fm);
        }
        return result;
    }

    // ========== 执行会员导入 ==========

    public ImportExecuteResponse executeMemberImport(ImportExecuteRequest request) {
        String filePath = fileTokenMap.get(request.getFileToken());
        if (filePath == null) {
            throw new BusinessException("文件已过期或不存在，请重新上传");
        }

        // 构建 excelIndex -> entityField 映射
        Map<Integer, String> indexFieldMap = new HashMap<>();
        for (FieldMapping fm : request.getFieldMappings()) {
            if (StringUtils.hasText(fm.getEntityField())) {
                indexFieldMap.put(fm.getExcelIndex(), fm.getEntityField());
            }
        }

        // 预加载已有手机号用于去重
        Set<String> existingPhones = new HashSet<>();
        if (request.isSkipDuplicate()) {
            LambdaQueryWrapper<Member> phoneQuery = new LambdaQueryWrapper<>();
            phoneQuery.select(Member::getPhone).isNotNull(Member::getPhone);
            List<Member> existList = memberMapper.selectList(phoneQuery);
            for (Member m : existList) {
                if (StringUtils.hasText(m.getPhone())) {
                    existingPhones.add(m.getPhone());
                }
            }
        }

        ImportExecuteResponse response = new ImportExecuteResponse();
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger skip = new AtomicInteger(0);
        List<String> errors = Collections.synchronizedList(new ArrayList<>());
        List<Member> batch = new ArrayList<>(500);

        EasyExcel.read(filePath, new ReadListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                int rowNum = total.incrementAndGet() + 1;
                try {
                    Map<String, String> fieldValues = new HashMap<>();
                    for (Map.Entry<Integer, String> entry : indexFieldMap.entrySet()) {
                        String val = row.get(entry.getKey());
                        fieldValues.put(entry.getValue(), val != null ? val.trim() : "");
                    }

                    String name = fieldValues.getOrDefault("name", "");
                    String phone = fieldValues.getOrDefault("phone", "");
                    if (!StringUtils.hasText(name) && !StringUtils.hasText(phone)) {
                        skip.incrementAndGet();
                        return;
                    }

                    // 内存去重
                    if (request.isSkipDuplicate() && StringUtils.hasText(phone)) {
                        if (existingPhones.contains(phone)) {
                            skip.incrementAndGet();
                            return;
                        }
                        existingPhones.add(phone);
                    }

                    Member member = new Member();
                    member.setMemberNo("M" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") + total.get());
                    member.setName(name);
                    member.setPhone(phone);

                    String gender = fieldValues.getOrDefault("gender", "");
                    if (StringUtils.hasText(gender)) {
                        if ("男".equals(gender)) member.setGender("MALE");
                        else if ("女".equals(gender)) member.setGender("FEMALE");
                        else member.setGender(gender);
                    }

                    String birthday = fieldValues.getOrDefault("birthday", "");
                    if (StringUtils.hasText(birthday)) {
                        member.setBirthday(parseDate(birthday));
                    }

                    String idCard = fieldValues.getOrDefault("idCard", "");
                    if (StringUtils.hasText(idCard)) {
                        member.setIdCard(idCard);
                    }

                    String pointsStr = fieldValues.getOrDefault("points", "");
                    if (StringUtils.hasText(pointsStr)) {
                        try {
                            member.setPoints(Integer.parseInt(pointsStr.replace(",", "").trim()));
                        } catch (NumberFormatException e) {
                            member.setPoints(0);
                        }
                    } else {
                        member.setPoints(0);
                    }

                    String allergyInfo = fieldValues.getOrDefault("allergyInfo", "");
                    if (StringUtils.hasText(allergyInfo)) member.setAllergyInfo(allergyInfo);

                    String chronicDisease = fieldValues.getOrDefault("chronicDisease", "");
                    if (StringUtils.hasText(chronicDisease)) member.setChronicDisease(chronicDisease);

                    member.setStatus("正常");
                    batch.add(member);
                    success.incrementAndGet();

                    // 每500条批量插入一次
                    if (batch.size() >= 500) {
                        flushMemberBatch(batch, fail, success, errors);
                    }

                } catch (Exception e) {
                    fail.incrementAndGet();
                    errors.add("第" + rowNum + "行: " + e.getMessage());
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                if (!batch.isEmpty()) {
                    flushMemberBatch(batch, fail, success, errors);
                }
            }
        }).headRowNumber(1).sheet().doRead();

        response.setTotal(total.get());
        response.setSuccess(success.get());
        response.setFail(fail.get());
        response.setSkip(skip.get());
        response.setErrors(errors);

        cleanupFile(request.getFileToken());
        return response;
    }

    // ========== 执行药品导入 ==========

    public ImportExecuteResponse executeDrugImport(ImportExecuteRequest request) {
        String filePath = fileTokenMap.get(request.getFileToken());
        if (filePath == null) {
            throw new BusinessException("文件已过期或不存在，请重新上传");
        }

        Map<Integer, String> indexFieldMap = new HashMap<>();
        for (FieldMapping fm : request.getFieldMappings()) {
            if (StringUtils.hasText(fm.getEntityField())) {
                indexFieldMap.put(fm.getExcelIndex(), fm.getEntityField());
            }
        }

        ImportExecuteResponse response = new ImportExecuteResponse();
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger skip = new AtomicInteger(0);
        List<String> errors = Collections.synchronizedList(new ArrayList<>());

        // 预加载药品分类缓存（名称 -> ID）
        Map<String, Long> categoryCache = new HashMap<>();
        List<DrugCategory> categories = drugCategoryMapper.selectList(
                new LambdaQueryWrapper<DrugCategory>().eq(DrugCategory::getDeleted, 0));
        for (DrugCategory cat : categories) {
            categoryCache.put(cat.getName().trim(), cat.getId());
        }

        // 预加载已有原编号用于去重
        Set<String> existingOriginalCodes = new HashSet<>();
        if (request.isSkipDuplicate()) {
            List<Drug> existingDrugs = drugMapper.selectList(
                    new LambdaQueryWrapper<Drug>()
                            .select(Drug::getOriginalCode));
            for (Drug d : existingDrugs) {
                if (StringUtils.hasText(d.getOriginalCode())) {
                    existingOriginalCodes.add(d.getOriginalCode());
                }
            }
        }

        // 预生成药品编码前缀和起始序号
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String codePrefix = "YP" + dateStr;
        LambdaQueryWrapper<Drug> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.likeRight(Drug::getDrugCode, codePrefix).orderByDesc(Drug::getDrugCode).last("LIMIT 1");
        Drug lastDrug = drugMapper.selectOne(codeWrapper);
        AtomicInteger codeSeq = new AtomicInteger(1);
        if (lastDrug != null && lastDrug.getDrugCode() != null && lastDrug.getDrugCode().length() >= 14) {
            try {
                codeSeq.set(Integer.parseInt(lastDrug.getDrugCode().substring(10)) + 1);
            } catch (NumberFormatException ignored) {}
        }

        // 批量收集待插入的药品
        List<Drug> batchList = new ArrayList<>();
        int BATCH_SIZE = 500;

        EasyExcel.read(filePath, new ReadListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                int rowNum = total.incrementAndGet() + 1;
                try {
                    Map<String, String> fieldValues = new HashMap<>();
                    for (Map.Entry<Integer, String> entry : indexFieldMap.entrySet()) {
                        String val = row.get(entry.getKey());
                        fieldValues.put(entry.getValue(), val != null ? val.trim() : "");
                    }

                    // 必填校验
                    String genericName = fieldValues.getOrDefault("genericName", "");
                    if (!StringUtils.hasText(genericName)) {
                        skip.incrementAndGet();
                        errors.add("第" + rowNum + "行: 通用名为空，已跳过");
                        return;
                    }

                    // 重复检查 - 原编号去重
                    String originalCode = fieldValues.getOrDefault("originalCode", "");
                    if (request.isSkipDuplicate() && StringUtils.hasText(originalCode)) {
                        if (existingOriginalCodes.contains(originalCode)) {
                            skip.incrementAndGet();
                            errors.add("第" + rowNum + "行: 原编号[" + originalCode + "]重复，已跳过(" + genericName + ")");
                            return;
                        }
                        existingOriginalCodes.add(originalCode);
                    }

                    // 构建 Drug 对象
                    Drug drug = new Drug();
                    drug.setDrugCode(codePrefix + String.format("%04d", codeSeq.getAndIncrement()));
                    drug.setGenericName(genericName);

                    String tradeName = fieldValues.getOrDefault("tradeName", "");
                    drug.setTradeName(StringUtils.hasText(tradeName) ? tradeName : genericName);

                    // 药品分类 -> categoryId（自动创建不存在的分类）
                    String categoryName = fieldValues.getOrDefault("categoryName", "");
                    if (StringUtils.hasText(categoryName)) {
                        Long catId = drugService.getOrCreateCategoryId(categoryName.trim(), categoryCache);
                        if (catId != null) {
                            drug.setCategoryId(catId);
                        }
                    }

                    String barcode = fieldValues.getOrDefault("barcode", "");
                    if (StringUtils.hasText(barcode)) {
                        drug.setBarcode(barcode);
                    }

                    if (StringUtils.hasText(originalCode)) {
                        drug.setOriginalCode(originalCode);
                    }

                    String specification = fieldValues.getOrDefault("specification", "");
                    if (StringUtils.hasText(specification)) {
                        drug.setSpecification(specification);
                    }

                    String dosageForm = fieldValues.getOrDefault("dosageForm", "");
                    if (StringUtils.hasText(dosageForm)) {
                        drug.setDosageForm(dosageForm);
                    }

                    String manufacturer = fieldValues.getOrDefault("manufacturer", "");
                    if (StringUtils.hasText(manufacturer)) {
                        drug.setManufacturer(manufacturer);
                    }

                    String approvalNo = fieldValues.getOrDefault("approvalNo", "");
                    if (StringUtils.hasText(approvalNo)) {
                        drug.setApprovalNo(approvalNo);
                    }

                    String unit = fieldValues.getOrDefault("unit", "");
                    drug.setUnit(StringUtils.hasText(unit) ? drugService.normalizeUnit(unit) : "盒");

                    drug.setRetailPrice(parseBigDecimal(fieldValues.getOrDefault("retailPrice", "")));
                    drug.setPurchasePrice(parseBigDecimal(fieldValues.getOrDefault("purchasePrice", "")));
                    drug.setMemberPrice(parseBigDecimal(fieldValues.getOrDefault("memberPrice", "")));

                    String otcType = fieldValues.getOrDefault("otcType", "");
                    if (StringUtils.hasText(otcType)) {
                        drug.setOtcType(convertOtcType(otcType));
                    }

                    String storageCondition = fieldValues.getOrDefault("storageCondition", "");
                    drug.setStorageCondition(drugService.convertStorageCondition(storageCondition));

                    String medicalInsurance = fieldValues.getOrDefault("medicalInsurance", "");
                    if (StringUtils.hasText(medicalInsurance)) {
                        drug.setMedicalInsurance(medicalInsurance);
                    }

                    String validPeriodStr = fieldValues.getOrDefault("validPeriod", "");
                    if (StringUtils.hasText(validPeriodStr)) {
                        try {
                            drug.setValidPeriod(Integer.parseInt(validPeriodStr.replaceAll("[^0-9]", "")));
                        } catch (NumberFormatException ignored) {}
                    }

                    String originVal = fieldValues.getOrDefault("origin", "");
                    if (StringUtils.hasText(originVal)) {
                        drug.setOrigin(originVal);
                    }

                    String mah = fieldValues.getOrDefault("marketingAuthHolder", "");
                    if (StringUtils.hasText(mah)) {
                        drug.setMarketingAuthHolder(mah);
                    }

                    String sqStr = fieldValues.getOrDefault("stockQuantity", "");
                    if (StringUtils.hasText(sqStr)) {
                        drug.setStockQuantity(parseBigDecimal(sqStr));
                    }

                    String requireRealNameStr = fieldValues.getOrDefault("requireRealName", "");
                    if (StringUtils.hasText(requireRealNameStr)) {
                        String v = requireRealNameStr.trim();
                        drug.setRequireRealName("是".equals(v) || "true".equalsIgnoreCase(v) || "1".equals(v));
                    }

                    String stockUpperStr = fieldValues.getOrDefault("stockUpperLimit", "");
                    if (StringUtils.hasText(stockUpperStr)) {
                        try { drug.setStockUpperLimit((int) Double.parseDouble(stockUpperStr.trim())); } catch (NumberFormatException ignored) {}
                    }

                    String stockLowerStr = fieldValues.getOrDefault("stockLowerLimit", "");
                    if (StringUtils.hasText(stockLowerStr)) {
                        try { drug.setStockLowerLimit((int) Double.parseDouble(stockLowerStr.trim())); } catch (NumberFormatException ignored) {}
                    }

                    String allowPriceStr = fieldValues.getOrDefault("allowPriceAdjust", "");
                    if (StringUtils.hasText(allowPriceStr)) {
                        String v = allowPriceStr.trim();
                        drug.setAllowPriceAdjust(!("否".equals(v) || "false".equalsIgnoreCase(v) || "0".equals(v)));
                    }

                    String maintenanceMethodStr = fieldValues.getOrDefault("maintenanceMethod", "");
                    if (StringUtils.hasText(maintenanceMethodStr)) {
                        drug.setMaintenanceMethod(maintenanceMethodStr);
                    }

                    drug.setStatus("启用");
                    batchList.add(drug);
                    success.incrementAndGet();

                    // 每 BATCH_SIZE 条批量插入一次
                    if (batchList.size() >= BATCH_SIZE) {
                        flushDrugBatch(batchList, fail, success, errors);
                    }

                } catch (Exception e) {
                    fail.incrementAndGet();
                    errors.add("第" + rowNum + "行: " + e.getMessage());
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // 插入剩余数据
                if (!batchList.isEmpty()) {
                    flushDrugBatch(batchList, fail, success, errors);
                }
            }
        }).headRowNumber(1).sheet().doRead();

        response.setTotal(total.get());
        response.setSuccess(success.get());
        response.setFail(fail.get());
        response.setSkip(skip.get());
        response.setErrors(errors);

        cleanupFile(request.getFileToken());
        return response;
    }

    private void flushMemberBatch(List<Member> batch, AtomicInteger fail, AtomicInteger success, List<String> errors) {
        for (Member member : batch) {
            try {
                memberMapper.insert(member);
            } catch (Exception e) {
                fail.incrementAndGet();
                success.decrementAndGet();
                String name = member.getName() != null ? member.getName() : "未知";
                String phone = member.getPhone() != null ? member.getPhone() : "";
                log.error("插入会员失败 [{}|{}]: {}", name, phone, e.getMessage());
                errors.add("插入失败[" + name + "|" + phone + "]: " + e.getMessage());
            }
        }
        batch.clear();
    }

    private void flushDrugBatch(List<Drug> batchList, AtomicInteger fail, AtomicInteger success, List<String> errors) {
        for (Drug drug : batchList) {
            try {
                drugMapper.insert(drug);
            } catch (Exception e) {
                fail.incrementAndGet();
                success.decrementAndGet();
                String name = drug.getGenericName() != null ? drug.getGenericName() : "未知";
                String code = drug.getOriginalCode() != null ? drug.getOriginalCode() : (drug.getBarcode() != null ? drug.getBarcode() : "");
                log.error("插入药品失败 [{}|{}]: {}", name, code, e.getMessage());
                errors.add("插入失败[" + name + "|" + code + "]: " + e.getMessage());
            }
        }
        batchList.clear();
    }

    // ========== 工具方法 ==========

    private void cleanupFile(String fileToken) {
        String filePath = fileTokenMap.remove(fileToken);
        if (filePath != null) {
            try {
                new File(filePath).delete();
            } catch (Exception ignored) {
            }
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf(".");
        return idx >= 0 ? filename.substring(idx) : "";
    }

    private LocalDate parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) return null;
        dateStr = dateStr.trim();

        // 尝试多种日期格式
        String[] patterns = {"yyyy-MM-dd", "yyyy/MM/dd", "yyyyMMdd", "yyyy.MM.dd", "yyyy年MM月dd日"};
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {
            }
        }

        // 尝试 Hutool 智能解析
        try {
            return DateUtil.parse(dateStr).toLocalDateTime().toLocalDate();
        } catch (Exception ignored) {
        }
        return null;
    }

    private BigDecimal parseBigDecimal(String str) {
        if (!StringUtils.hasText(str)) return BigDecimal.ZERO;
        try {
            return new BigDecimal(str.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String convertOtcType(String val) {
        if (val == null) return null;
        val = val.trim();
        if (val.contains("甲") || "OTC_A".equalsIgnoreCase(val)) return "OTC_A";
        if (val.contains("乙") || "OTC_B".equalsIgnoreCase(val)) return "OTC_B";
        if (val.contains("处方") || "RX".equalsIgnoreCase(val)) return "RX";
        return val;
    }

    // ========== 内部字段定义 ==========

    private static class FieldDef {
        String label;
        boolean required;
        List<String> aliases;

        FieldDef(String label, boolean required, String... aliases) {
            this.label = label;
            this.required = required;
            this.aliases = Arrays.asList(aliases);
        }
    }
}
