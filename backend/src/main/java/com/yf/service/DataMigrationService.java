package com.yf.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.*;
import com.yf.exception.BusinessException;
import com.yf.mapper.*;
import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据迁移服务 - 从旧系统导入数据（自动匹配表头字段）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataMigrationService {

    private final DataMigrationTaskMapper taskMapper;
    private final DrugMapper drugMapper;
    private final DrugBatchMapper drugBatchMapper;
    private final HerbMapper herbMapper;
    private final MemberMapper memberMapper;
    private final SupplierMapper supplierMapper;
    private final InventoryMapper inventoryMapper;
    private final DrugCategoryMapper drugCategoryMapper;
    private final StoreMapper storeMapper;
    private final DrugService drugService;

    private static final int MAX_SKIP_EXAMPLES = 50;

    /**
     * 分页查询迁移任务
     */
    public Page<DataMigrationTask> page(Page<DataMigrationTask> page, String targetModule, String status) {
        LambdaQueryWrapper<DataMigrationTask> wrapper = new LambdaQueryWrapper<>();
        if (targetModule != null) {
            wrapper.eq(DataMigrationTask::getTargetModule, targetModule);
        }
        if (status != null) {
            wrapper.eq(DataMigrationTask::getStatus, status);
        }
        wrapper.orderByDesc(DataMigrationTask::getCreatedAt);
        return taskMapper.selectPage(page, wrapper);
    }

    /**
     * 获取迁移任务详情
     */
    public DataMigrationTask getById(Long id) {
        return taskMapper.selectById(id);
    }

    /**
     * 上传文件并创建迁移任务
     */
    @Transactional
    public DataMigrationTask uploadAndCreate(MultipartFile file, String targetModule,
                                              String sourceType, boolean skipDuplicate) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        // 验证文件类型
        if (!".xlsx".equalsIgnoreCase(extension) && !".xls".equalsIgnoreCase(extension)
                && !".csv".equalsIgnoreCase(extension)) {
            throw new BusinessException("仅支持 Excel (.xlsx/.xls) 和 CSV (.csv) 格式文件");
        }

        // 保存文件
        String fileName = UUID.randomUUID() + extension;
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads"
                + File.separator + "migration";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = uploadDir + File.separator + fileName;
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }

        // 创建任务记录
        DataMigrationTask task = new DataMigrationTask();
        task.setTaskName(getModuleName(targetModule) + "数据导入 - " + originalName);
        task.setSourceType(sourceType != null ? sourceType : "excel");
        task.setTargetModule(targetModule);
        task.setFilePath(filePath);
        task.setOriginalFileName(originalName);
        task.setStatus("pending");
        task.setTotalCount(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setSkipCount(0);
        taskMapper.insert(task);

        return task;
    }

    /**
     * 执行迁移任务
     */
    @Transactional
    public DataMigrationTask execute(Long taskId) {
        DataMigrationTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("迁移任务不存在");
        }
        if (!"pending".equals(task.getStatus()) && !"failed".equals(task.getStatus())) {
            throw new BusinessException("任务状态不允许执行，当前状态: " + task.getStatus());
        }

        task.setStatus("processing");
        task.setStartTime(LocalDateTime.now());
        taskMapper.updateById(task);

        try {
            switch (task.getTargetModule()) {
                case "drug":
                    processDrugImport(task);
                    break;
                case "herb":
                    processHerbImport(task);
                    break;
                case "member":
                    processMemberImport(task);
                    break;
                case "supplier":
                    processSupplierImport(task);
                    break;
                case "inventory":
                    processInventoryImport(task);
                    break;
                default:
                    throw new BusinessException("不支持的目标模块: " + task.getTargetModule());
            }

            task.setStatus("completed");
        } catch (Exception e) {
            log.error("数据迁移失败，任务ID={}", taskId, e);
            task.setStatus("failed");
            task.setErrorLog(e.getMessage());
        }

        task.setFinishTime(LocalDateTime.now());
        taskMapper.updateById(task);
        return task;
    }

    /**
     * 删除迁移任务
     */
    public int delete(Long id) {
        DataMigrationTask task = taskMapper.selectById(id);
        if (task != null && "processing".equals(task.getStatus())) {
            throw new BusinessException("正在执行的任务不能删除");
        }
        return taskMapper.deleteById(id);
    }

    /**
     * 下载导入模板
     */
    public String getTemplatePath(String targetModule) {
        return "templates/migration/" + targetModule + "_template.xlsx";
    }

    // ==================== 跳过原因追踪器 ====================

    private static class SkipTracker {
        private final Map<String, AtomicInteger> reasonCounts = new LinkedHashMap<>();
        private final Map<String, List<String>> reasonExamples = new LinkedHashMap<>();

        void record(int rowNum, String reason, String detail) {
            reasonCounts.computeIfAbsent(reason, k -> new AtomicInteger(0)).incrementAndGet();
            List<String> examples = reasonExamples.computeIfAbsent(reason, k -> new ArrayList<>());
            if (examples.size() < MAX_SKIP_EXAMPLES) {
                examples.add("第" + rowNum + "行" + (detail != null ? ": " + detail : ""));
            }
        }

        String buildLog() {
            if (reasonCounts.isEmpty()) return null;
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, AtomicInteger> entry : reasonCounts.entrySet()) {
                String reason = entry.getKey();
                int count = entry.getValue().get();
                sb.append("【").append(reason).append("】共 ").append(count).append(" 条\n");
                List<String> examples = reasonExamples.get(reason);
                if (examples != null) {
                    for (String ex : examples) {
                        sb.append("  ").append(ex).append("\n");
                    }
                    if (count > examples.size()) {
                        sb.append("  ...等共").append(count).append("条\n");
                    }
                }
            }
            return sb.toString();
        }
    }

    // ==================== 自动表头匹配工具 ====================

    /**
     * 根据表头和别名配置，自动匹配列索引到字段名
     */
    private void matchColumns(Map<Integer, String> headMap,
                              Map<String, List<String>> fieldAliases,
                              Map<Integer, String> columnFieldMap) {
        for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
            String header = entry.getValue() != null ? entry.getValue().trim().toLowerCase() : "";
            if (header.isEmpty()) continue;
            for (Map.Entry<String, List<String>> aliasEntry : fieldAliases.entrySet()) {
                if (columnFieldMap.containsValue(aliasEntry.getKey())) continue;
                for (String alias : aliasEntry.getValue()) {
                    if (alias.toLowerCase().equals(header)) {
                        columnFieldMap.put(entry.getKey(), aliasEntry.getKey());
                        break;
                    }
                }
                if (columnFieldMap.containsKey(entry.getKey())) break;
            }
        }
    }

    /**
     * 从行数据中根据字段映射获取值
     */
    private String getFieldValue(Map<Integer, String> row, Map<Integer, String> columnFieldMap, String fieldName) {
        for (Map.Entry<Integer, String> entry : columnFieldMap.entrySet()) {
            if (fieldName.equals(entry.getValue())) {
                String val = row.get(entry.getKey());
                return val != null ? val.trim() : null;
            }
        }
        return null;
    }

    private String convertGender(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        String v = val.trim();
        if ("男".equals(v) || "male".equalsIgnoreCase(v) || "M".equalsIgnoreCase(v)) return "男";
        if ("女".equals(v) || "female".equalsIgnoreCase(v) || "F".equalsIgnoreCase(v)) return "女";
        return v;
    }

    private LocalDate parseDate(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try {
            Date d = DateUtil.parse(val.trim());
            return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (Exception e) {
            try {
                return LocalDate.parse(val.trim());
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private Integer parseInteger(String val, int defaultVal) {
        if (val == null || val.trim().isEmpty()) return defaultVal;
        try {
            return (int) Double.parseDouble(val.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private BigDecimal parseBigDecimal(String str) {
        if (str == null || str.trim().isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(str.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String getModuleName(String module) {
        switch (module) {
            case "drug": return "药品";
            case "herb": return "中药";
            case "member": return "会员";
            case "supplier": return "供应商";
            case "inventory": return "库存";
            default: return module;
        }
    }

    // ==================== 各模块数据导入处理（自动匹配表头） ====================

    private void processDrugImport(DataMigrationTask task) {
        log.info("开始处理药品数据导入，任务ID={}", task.getId());
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger skip = new AtomicInteger(0);
        StringBuilder errorLog = new StringBuilder();
        SkipTracker skipTracker = new SkipTracker();

        Map<String, List<String>> fieldAliases = new LinkedHashMap<>();
        fieldAliases.put("genericName", Arrays.asList("通用名", "药品名称", "名称", "药名", "品名", "通用名称", "genericname", "name"));
        fieldAliases.put("tradeName", Arrays.asList("商品名", "商品名称", "tradename", "商品", "本企业自定名称", "自定名称", "自定义名称"));
        fieldAliases.put("specification", Arrays.asList("规格", "spec", "specification"));
        fieldAliases.put("dosageForm", Arrays.asList("剂型", "dosageform", "剂形"));
        fieldAliases.put("manufacturer", Arrays.asList("厂家", "生产厂家", "生产厂商", "生产企业", "manufacturer", "厂商", "制造商", "生产商"));
        fieldAliases.put("approvalNo", Arrays.asList("批准文号", "国药准字", "approvalno", "文号", "标准号", "注册证号", "注册号"));
        fieldAliases.put("barcode", Arrays.asList("条形码", "条码", "barcode", "商品条码", "ean"));
        fieldAliases.put("originalCode", Arrays.asList("原编号", "原系统编号", "商品编号", "编号", "旧编号", "商品编码"));
        fieldAliases.put("unit", Arrays.asList("单位", "unit", "包装单位", "计量单位"));
        fieldAliases.put("retailPrice", Arrays.asList("零售价", "售价", "商品零售价", "retail", "retailprice", "销售价", "商品销售价"));
        fieldAliases.put("purchasePrice", Arrays.asList("采购价", "进价", "进货价", "purchase", "purchaseprice", "成本价", "实际购价", "购价"));
        fieldAliases.put("memberPrice", Arrays.asList("会员价", "memberprice", "vip价"));
        fieldAliases.put("storageCondition", Arrays.asList("储存条件", "存储条件", "storage", "贮藏"));
        fieldAliases.put("otcType", Arrays.asList("otc类型", "otc", "处方类型"));
        fieldAliases.put("medicalInsurance", Arrays.asList("医保类型", "医保", "医保编码"));
        fieldAliases.put("validPeriod", Arrays.asList("有效期", "有效期(月)", "有效月数"));
        fieldAliases.put("origin", Arrays.asList("产地", "产地/来源", "来源"));
        fieldAliases.put("marketingAuthHolder", Arrays.asList("上市许可持有人", "持有人", "MAH"));
        fieldAliases.put("stockQuantity", Arrays.asList("库存数量", "库存", "现有库存"));
        fieldAliases.put("categoryName", Arrays.asList("商品分类", "药品分类", "分类", "类别", "category"));
        fieldAliases.put("requireRealName", Arrays.asList("实名登记", "实名", "需实名"));
        fieldAliases.put("stockUpperLimit", Arrays.asList("库存上限", "最大库存", "上限"));
        fieldAliases.put("stockLowerLimit", Arrays.asList("库存下限", "最小库存", "下限", "安全库存"));
        fieldAliases.put("allowPriceAdjust", Arrays.asList("销售可调价", "可调价", "允许调价"));
        fieldAliases.put("maintenanceMethod", Arrays.asList("养护方式", "养护", "养护方法"));

        // 预加载药品分类缓存（名称 -> ID）
        Map<String, Long> categoryCache = new HashMap<>();
        List<DrugCategory> categories = drugCategoryMapper.selectList(
                new LambdaQueryWrapper<DrugCategory>().eq(DrugCategory::getDeleted, 0));
        for (DrugCategory cat : categories) {
            categoryCache.put(cat.getName().trim(), cat.getId());
        }

        Map<Integer, String> columnFieldMap = new HashMap<>();

        EasyExcel.read(task.getFilePath(), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                matchColumns(headMap, fieldAliases, columnFieldMap);
                log.info("药品导入字段匹配结果: {}", columnFieldMap);
            }

            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                total.incrementAndGet();
                try {
                    String genericName = getFieldValue(row, columnFieldMap, "genericName");
                    String tradeName = getFieldValue(row, columnFieldMap, "tradeName");
                    String specification = getFieldValue(row, columnFieldMap, "specification");
                    String dosageForm = getFieldValue(row, columnFieldMap, "dosageForm");
                    String manufacturer = getFieldValue(row, columnFieldMap, "manufacturer");
                    String approvalNo = getFieldValue(row, columnFieldMap, "approvalNo");
                    String barcode = getFieldValue(row, columnFieldMap, "barcode");
                    String originalCode = getFieldValue(row, columnFieldMap, "originalCode");
                    String unit = getFieldValue(row, columnFieldMap, "unit");
                    String retailPriceStr = getFieldValue(row, columnFieldMap, "retailPrice");
                    String purchasePriceStr = getFieldValue(row, columnFieldMap, "purchasePrice");
                    String memberPriceStr = getFieldValue(row, columnFieldMap, "memberPrice");
                    String storageCondition = getFieldValue(row, columnFieldMap, "storageCondition");
                    String otcType = getFieldValue(row, columnFieldMap, "otcType");
                    String medicalInsurance = getFieldValue(row, columnFieldMap, "medicalInsurance");
                    String validPeriodStr = getFieldValue(row, columnFieldMap, "validPeriod");
                    String originVal = getFieldValue(row, columnFieldMap, "origin");
                    String marketingAuthHolder = getFieldValue(row, columnFieldMap, "marketingAuthHolder");
                    String stockQuantityStr = getFieldValue(row, columnFieldMap, "stockQuantity");
                    String categoryName = getFieldValue(row, columnFieldMap, "categoryName");
                    String requireRealNameStr = getFieldValue(row, columnFieldMap, "requireRealName");
                    String stockUpperLimitStr = getFieldValue(row, columnFieldMap, "stockUpperLimit");
                    String stockLowerLimitStr = getFieldValue(row, columnFieldMap, "stockLowerLimit");
                    String allowPriceAdjustStr = getFieldValue(row, columnFieldMap, "allowPriceAdjust");
                    String maintenanceMethodVal = getFieldValue(row, columnFieldMap, "maintenanceMethod");

                    if (genericName == null || genericName.isEmpty()) {
                        skip.incrementAndGet();
                        skipTracker.record(total.get(), "药品名称为空", null);
                        return;
                    }

                    Drug drug = new Drug();
                    drug.setGenericName(genericName);
                    drug.setTradeName(tradeName);
                    drug.setSpecification(specification);
                    drug.setDosageForm(dosageForm);
                    drug.setManufacturer(manufacturer);
                    drug.setApprovalNo(approvalNo);
                    drug.setBarcode(barcode);
                    drug.setOriginalCode(originalCode);
                    drug.setUnit(drugService.normalizeUnit(unit != null ? unit : "盒"));
                    drug.setRetailPrice(parseBigDecimal(retailPriceStr));
                    drug.setPurchasePrice(parseBigDecimal(purchasePriceStr));
                    drug.setMemberPrice(parseBigDecimal(memberPriceStr));
                    drug.setStorageCondition(drugService.convertStorageCondition(storageCondition));
                    drug.setOtcType(otcType);
                    if (StringUtils.hasText(medicalInsurance)) {
                        drug.setMedicalInsurance(medicalInsurance);
                    }
                    if (StringUtils.hasText(validPeriodStr)) {
                        try {
                            drug.setValidPeriod(Integer.parseInt(validPeriodStr.replaceAll("[^0-9]", "")));
                        } catch (NumberFormatException ignored) {}
                    }
                    if (StringUtils.hasText(originVal)) {
                        drug.setOrigin(originVal);
                    }
                    if (StringUtils.hasText(marketingAuthHolder)) {
                        drug.setMarketingAuthHolder(marketingAuthHolder);
                    }
                    if (StringUtils.hasText(stockQuantityStr)) {
                        drug.setStockQuantity(parseBigDecimal(stockQuantityStr));
                    }
                    if (StringUtils.hasText(requireRealNameStr)) {
                        String v = requireRealNameStr.trim();
                        drug.setRequireRealName("是".equals(v) || "true".equalsIgnoreCase(v) || "1".equals(v));
                    }
                    if (StringUtils.hasText(stockUpperLimitStr)) {
                        try { drug.setStockUpperLimit((int) Double.parseDouble(stockUpperLimitStr.trim())); } catch (NumberFormatException ignored) {}
                    }
                    if (StringUtils.hasText(stockLowerLimitStr)) {
                        try { drug.setStockLowerLimit((int) Double.parseDouble(stockLowerLimitStr.trim())); } catch (NumberFormatException ignored) {}
                    }
                    if (StringUtils.hasText(allowPriceAdjustStr)) {
                        String v = allowPriceAdjustStr.trim();
                        drug.setAllowPriceAdjust(!("否".equals(v) || "false".equalsIgnoreCase(v) || "0".equals(v)));
                    }
                    if (StringUtils.hasText(maintenanceMethodVal)) {
                        drug.setMaintenanceMethod(maintenanceMethodVal);
                    }
                    // 药品分类 -> categoryId（自动创建不存在的分类）
                    if (StringUtils.hasText(categoryName)) {
                        Long catId = drugService.getOrCreateCategoryId(categoryName.trim(), categoryCache);
                        if (catId != null) {
                            drug.setCategoryId(catId);
                        }
                    }
                    drug.setStatus("启用");

                    drugMapper.insert(drug);
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                    errorLog.append("第").append(total.get()).append("行: ").append(e.getMessage()).append("\n");
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).headRowNumber(1).sheet().doRead();

        task.setTotalCount(total.get());
        task.setSuccessCount(success.get());
        task.setFailCount(fail.get());
        task.setSkipCount(skip.get());
        if (errorLog.length() > 0) {
            task.setErrorLog(errorLog.toString());
        }
        task.setSkipLog(skipTracker.buildLog());
    }

    private void processHerbImport(DataMigrationTask task) {
        log.info("开始处理中药数据导入，任务ID={}", task.getId());
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger skip = new AtomicInteger(0);
        StringBuilder errorLog = new StringBuilder();
        SkipTracker skipTracker = new SkipTracker();

        Map<String, List<String>> fieldAliases = new LinkedHashMap<>();
        fieldAliases.put("name", Arrays.asList("名称", "中药名", "药名", "饮片名", "name", "品名"));
        fieldAliases.put("pinyin", Arrays.asList("拼音", "pinyin", "拼音码"));
        fieldAliases.put("nature", Arrays.asList("性味", "药性", "nature", "性"));
        fieldAliases.put("flavor", Arrays.asList("味", "药味", "flavor"));
        fieldAliases.put("meridian", Arrays.asList("归经", "meridian", "经络"));
        fieldAliases.put("efficacy", Arrays.asList("功效", "功能主治", "efficacy", "主治"));
        fieldAliases.put("origin", Arrays.asList("产地", "origin", "产区", "来源"));
        fieldAliases.put("retailPrice", Arrays.asList("零售价", "售价", "retail", "销售价"));
        fieldAliases.put("purchasePrice", Arrays.asList("进货价", "采购价", "进价", "purchase", "成本价"));
        fieldAliases.put("processingMethod", Arrays.asList("炮制方法", "加工方法", "炮制"));
        fieldAliases.put("category", Arrays.asList("分类", "类别", "category"));

        Map<Integer, String> columnFieldMap = new HashMap<>();

        EasyExcel.read(task.getFilePath(), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                matchColumns(headMap, fieldAliases, columnFieldMap);
                log.info("中药导入字段匹配结果: {}", columnFieldMap);
            }

            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                total.incrementAndGet();
                try {
                    String name = getFieldValue(row, columnFieldMap, "name");
                    String pinyin = getFieldValue(row, columnFieldMap, "pinyin");
                    String nature = getFieldValue(row, columnFieldMap, "nature");
                    String flavor = getFieldValue(row, columnFieldMap, "flavor");
                    String meridian = getFieldValue(row, columnFieldMap, "meridian");
                    String efficacy = getFieldValue(row, columnFieldMap, "efficacy");
                    String origin = getFieldValue(row, columnFieldMap, "origin");
                    String retailPriceStr = getFieldValue(row, columnFieldMap, "retailPrice");
                    String purchasePriceStr = getFieldValue(row, columnFieldMap, "purchasePrice");
                    String processingMethod = getFieldValue(row, columnFieldMap, "processingMethod");
                    String category = getFieldValue(row, columnFieldMap, "category");

                    if (name == null || name.isEmpty()) {
                        skip.incrementAndGet();
                        skipTracker.record(total.get(), "中药名称为空", null);
                        return;
                    }

                    // 检查重复
                    LambdaQueryWrapper<Herb> w = new LambdaQueryWrapper<>();
                    w.eq(Herb::getName, name);
                    if (herbMapper.selectCount(w) > 0) {
                        skip.incrementAndGet();
                        skipTracker.record(total.get(), "名称重复", name);
                        return;
                    }

                    Herb herb = new Herb();
                    herb.setName(name);
                    herb.setPinyin(pinyin);
                    herb.setNature(nature);
                    herb.setFlavor(flavor);
                    herb.setMeridian(meridian);
                    herb.setEfficacy(efficacy);
                    herb.setOrigin(origin);
                    herb.setRetailPrice(parseBigDecimal(retailPriceStr));
                    herb.setPurchasePrice(parseBigDecimal(purchasePriceStr));
                    herb.setProcessingMethod(processingMethod);
                    herb.setCategory(category);
                    herb.setStatus("启用");

                    herbMapper.insert(herb);
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                    errorLog.append("第").append(total.get()).append("行: ").append(e.getMessage()).append("\n");
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).headRowNumber(1).sheet().doRead();

        task.setTotalCount(total.get());
        task.setSuccessCount(success.get());
        task.setFailCount(fail.get());
        task.setSkipCount(skip.get());
        if (errorLog.length() > 0) {
            task.setErrorLog(errorLog.toString());
        }
        task.setSkipLog(skipTracker.buildLog());
    }

    private void processMemberImport(DataMigrationTask task) {
        log.info("开始处理会员数据导入，任务ID={}", task.getId());
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger skip = new AtomicInteger(0);
        StringBuilder errorLog = new StringBuilder();
        SkipTracker skipTracker = new SkipTracker();

        Map<String, List<String>> fieldAliases = new LinkedHashMap<>();
        fieldAliases.put("name", Arrays.asList("姓名", "名称", "name", "会员姓名", "会员名称", "客户名称", "客户姓名", "会员"));
        fieldAliases.put("phone", Arrays.asList("手机号", "电话", "手机", "phone", "tel", "联系电话", "手机号码", "联系方式"));
        fieldAliases.put("gender", Arrays.asList("性别", "gender", "sex"));
        fieldAliases.put("birthday", Arrays.asList("生日", "出生日期", "birthday", "出生年月", "生日日期"));
        fieldAliases.put("idCard", Arrays.asList("身份证", "身份证号", "idcard", "证件号", "身份证号码"));
        fieldAliases.put("points", Arrays.asList("积分", "points", "剩余积分", "可用积分", "当前积分", "会员积分"));
        fieldAliases.put("allergyInfo", Arrays.asList("过敏信息", "过敏史", "allergy", "过敏"));
        fieldAliases.put("chronicDisease", Arrays.asList("慢性病", "慢病", "chronic", "慢性病史", "疾病"));
        fieldAliases.put("memberNo", Arrays.asList("会员号", "会员编号", "卡号", "会员卡号", "编号", "实体卡号"));
        fieldAliases.put("totalAmount", Arrays.asList("累计消费", "消费总额", "总消费"));

        Map<Integer, String> columnFieldMap = new HashMap<>();

        EasyExcel.read(task.getFilePath(), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                matchColumns(headMap, fieldAliases, columnFieldMap);
                log.info("会员导入字段匹配结果: {}", columnFieldMap);
            }

            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                total.incrementAndGet();
                try {
                    String name = getFieldValue(row, columnFieldMap, "name");
                    String phone = getFieldValue(row, columnFieldMap, "phone");
                    String gender = getFieldValue(row, columnFieldMap, "gender");
                    String birthday = getFieldValue(row, columnFieldMap, "birthday");
                    String idCard = getFieldValue(row, columnFieldMap, "idCard");
                    String pointsStr = getFieldValue(row, columnFieldMap, "points");
                    String allergyInfo = getFieldValue(row, columnFieldMap, "allergyInfo");
                    String chronicDisease = getFieldValue(row, columnFieldMap, "chronicDisease");
                    String memberNo = getFieldValue(row, columnFieldMap, "memberNo");
                    String totalAmountStr = getFieldValue(row, columnFieldMap, "totalAmount");

                    // 姓名中可能包含性别，如 "杜学秀 (男)"、"杜学秀（女）"
                    if (name != null) {
                        java.util.regex.Matcher gm = java.util.regex.Pattern
                                .compile("^(.+?)\\s*[（(](男|女)[）)]\\s*$").matcher(name);
                        if (gm.matches()) {
                            name = gm.group(1).trim();
                            if (gender == null || gender.isEmpty()) {
                                gender = gm.group(2);
                            }
                        }
                    }

                    if (phone == null || phone.isEmpty()) {
                        skip.incrementAndGet();
                        skipTracker.record(total.get(), "手机号为空", name);
                        return;
                    }

                    // 检查手机号重复
                    LambdaQueryWrapper<Member> w = new LambdaQueryWrapper<>();
                    w.eq(Member::getPhone, phone);
                    if (memberMapper.selectCount(w) > 0) {
                        skip.incrementAndGet();
                        skipTracker.record(total.get(), "手机号重复", phone + (name != null ? " (" + name + ")" : ""));
                        return;
                    }

                    Member member = new Member();
                    member.setMemberNo(memberNo != null && !memberNo.isEmpty()
                            ? memberNo : "M" + System.currentTimeMillis() + total.get());
                    member.setName(name != null ? name : "");
                    member.setPhone(phone);
                    member.setGender(convertGender(gender));
                    member.setBirthday(parseDate(birthday));
                    member.setIdCard(idCard);
                    member.setPoints(parseInteger(pointsStr, 0));
                    member.setTotalAmount(parseBigDecimal(totalAmountStr));
                    member.setAllergyInfo(allergyInfo);
                    member.setChronicDisease(chronicDisease);
                    member.setStatus("正常");

                    memberMapper.insert(member);
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                    errorLog.append("第").append(total.get()).append("行: ").append(e.getMessage()).append("\n");
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).headRowNumber(1).sheet().doRead();

        task.setTotalCount(total.get());
        task.setSuccessCount(success.get());
        task.setFailCount(fail.get());
        task.setSkipCount(skip.get());
        if (errorLog.length() > 0) {
            task.setErrorLog(errorLog.toString());
        }
        task.setSkipLog(skipTracker.buildLog());
    }

    private void processSupplierImport(DataMigrationTask task) {
        log.info("开始处理供应商数据导入，任务ID={}", task.getId());
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger skip = new AtomicInteger(0);
        StringBuilder errorLog = new StringBuilder();
        SkipTracker skipTracker = new SkipTracker();

        Map<String, List<String>> fieldAliases = new LinkedHashMap<>();
        fieldAliases.put("name", Arrays.asList("供应商名称", "名称", "公司名称", "企业名称", "name", "供应商"));
        fieldAliases.put("shortName", Arrays.asList("简称", "短名", "shortname"));
        fieldAliases.put("contactName", Arrays.asList("联系人", "负责人", "contactname", "对接人"));
        fieldAliases.put("contactPhone", Arrays.asList("联系电话", "电话", "手机", "contactphone", "联系方式"));
        fieldAliases.put("address", Arrays.asList("地址", "address", "详细地址", "公司地址"));
        fieldAliases.put("creditCode", Arrays.asList("统一社会信用代码", "社会信用代码", "信用代码", "creditcode", "税号"));
        fieldAliases.put("bankName", Arrays.asList("开户银行", "银行", "bankname"));
        fieldAliases.put("bankAccount", Arrays.asList("银行账号", "账号", "bankaccount"));

        Map<Integer, String> columnFieldMap = new HashMap<>();

        EasyExcel.read(task.getFilePath(), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                matchColumns(headMap, fieldAliases, columnFieldMap);
                log.info("供应商导入字段匹配结果: {}", columnFieldMap);
            }

            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                total.incrementAndGet();
                try {
                    String name = getFieldValue(row, columnFieldMap, "name");
                    String shortName = getFieldValue(row, columnFieldMap, "shortName");
                    String contactName = getFieldValue(row, columnFieldMap, "contactName");
                    String contactPhone = getFieldValue(row, columnFieldMap, "contactPhone");
                    String address = getFieldValue(row, columnFieldMap, "address");
                    String creditCode = getFieldValue(row, columnFieldMap, "creditCode");
                    String bankName = getFieldValue(row, columnFieldMap, "bankName");
                    String bankAccount = getFieldValue(row, columnFieldMap, "bankAccount");

                    if (name == null || name.isEmpty()) {
                        skip.incrementAndGet();
                        skipTracker.record(total.get(), "供应商名称为空", null);
                        return;
                    }

                    // 检查名称重复
                    LambdaQueryWrapper<Supplier> w = new LambdaQueryWrapper<>();
                    w.eq(Supplier::getName, name);
                    if (supplierMapper.selectCount(w) > 0) {
                        skip.incrementAndGet();
                        skipTracker.record(total.get(), "名称重复", name);
                        return;
                    }

                    Supplier supplier = new Supplier();
                    supplier.setName(name);
                    supplier.setShortName(shortName);
                    supplier.setContactName(contactName);
                    supplier.setContactPhone(contactPhone);
                    supplier.setAddress(address);
                    supplier.setCreditCode(creditCode);
                    supplier.setBankName(bankName);
                    supplier.setBankAccount(bankAccount);
                    supplier.setStatus("启用");

                    supplierMapper.insert(supplier);
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                    errorLog.append("第").append(total.get()).append("行: ").append(e.getMessage()).append("\n");
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).headRowNumber(1).sheet().doRead();

        task.setTotalCount(total.get());
        task.setSuccessCount(success.get());
        task.setFailCount(fail.get());
        task.setSkipCount(skip.get());
        if (errorLog.length() > 0) {
            task.setErrorLog(errorLog.toString());
        }
        task.setSkipLog(skipTracker.buildLog());
    }

    private void processInventoryImport(DataMigrationTask task) {
        log.info("开始处理库存数据导入，任务ID={}", task.getId());
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger skip = new AtomicInteger(0);
        StringBuilder errorLog = new StringBuilder();
        SkipTracker skipTracker = new SkipTracker();

        // ===== 扩展字段别名映射，覆盖Excel全部20列 =====
        Map<String, List<String>> fieldAliases = new LinkedHashMap<>();
        fieldAliases.put("drugCode", Arrays.asList("药品编码", "编码", "drugcode", "药品编号", "商品编号", "商品编码"));
        fieldAliases.put("genericName", Arrays.asList("通用名", "药品名称", "品名", "名称", "药品名", "通用名称"));
        fieldAliases.put("tradeName", Arrays.asList("商品名", "商品名称", "商品"));
        fieldAliases.put("specification", Arrays.asList("规格", "spec", "specification"));
        fieldAliases.put("manufacturer", Arrays.asList("生产企业", "生产厂家", "生产厂商", "厂家", "厂商", "制造商", "生产商"));
        fieldAliases.put("unit", Arrays.asList("单位", "unit", "计量单位", "包装单位"));
        fieldAliases.put("saleStatus", Arrays.asList("销售状态", "状态", "批次状态"));
        fieldAliases.put("expiryStatus", Arrays.asList("效期状态"));
        fieldAliases.put("batchNo", Arrays.asList("批号", "批次号", "batchno", "生产批号"));
        fieldAliases.put("produceDate", Arrays.asList("生产日期", "producedate", "生产时间"));
        fieldAliases.put("expireDate", Arrays.asList("有效期", "有效期至", "失效日期", "expiredate", "效期"));
        fieldAliases.put("packQuantity", Arrays.asList("装量数", "装量", "包装数量"));
        fieldAliases.put("retailPrice", Arrays.asList("销售价格", "零售价", "售价", "销售价"));
        fieldAliases.put("retailPriceTotal", Arrays.asList("销售价格总值", "销售总值", "销售总额", "零售价金额"));
        fieldAliases.put("purchaseDate", Arrays.asList("进价日期", "采购日期", "进货日期", "入库日期"));
        fieldAliases.put("remainQuantity", Arrays.asList("剩余库存数", "剩余库存", "剩余数量"));
        fieldAliases.put("quantity", Arrays.asList("在库数量", "库存数量", "数量", "quantity", "库存量", "现有库存"));
        fieldAliases.put("factoryName", Arrays.asList("生产厂商2", "厂商2"));
        fieldAliases.put("belongTo", Arrays.asList("所属", "所属门店", "归属"));
        fieldAliases.put("costPrice", Arrays.asList("进价", "成本价", "costprice", "采购价", "进货价"));
        fieldAliases.put("barcode", Arrays.asList("条形码", "条码", "barcode", "商品条码"));
        fieldAliases.put("location", Arrays.asList("货位", "库位", "location", "存放位置", "储位"));
        fieldAliases.put("supplierName", Arrays.asList("供应商", "供应商名称", "供应单位", "供货商"));

        // ===== 预加载所有药品到内存Map，提升匹配性能 =====
        List<Drug> allDrugs = drugMapper.selectList(new LambdaQueryWrapper<Drug>().eq(Drug::getDeleted, 0));
        Map<String, Drug> originalCodeMap = new HashMap<>();
        Map<String, Drug> drugCodeMap = new HashMap<>();
        Map<String, Drug> barcodeMap = new HashMap<>();
        Map<String, Drug> genericNameMap = new HashMap<>();
        Map<String, Drug> tradeNameMap = new HashMap<>();
        // 以 genericName+specification 组合作为精确匹配key
        Map<String, Drug> nameSpecMap = new HashMap<>();

        for (Drug d : allDrugs) {
            if (d.getOriginalCode() != null && !d.getOriginalCode().isEmpty()) {
                originalCodeMap.put(d.getOriginalCode().trim(), d);
            }
            if (d.getDrugCode() != null && !d.getDrugCode().isEmpty()) {
                drugCodeMap.put(d.getDrugCode().trim(), d);
            }
            if (d.getBarcode() != null && !d.getBarcode().isEmpty()) {
                barcodeMap.put(d.getBarcode().trim(), d);
            }
            if (d.getGenericName() != null && !d.getGenericName().isEmpty()) {
                genericNameMap.putIfAbsent(d.getGenericName().trim(), d);
                if (d.getSpecification() != null && !d.getSpecification().isEmpty()) {
                    String key = d.getGenericName().trim() + "||" + d.getSpecification().trim();
                    nameSpecMap.putIfAbsent(key, d);
                }
            }
            if (d.getTradeName() != null && !d.getTradeName().isEmpty()) {
                tradeNameMap.putIfAbsent(d.getTradeName().trim(), d);
            }
        }
        log.info("预加载药品数量: {}, originalCodeMap={}, drugCodeMap={}, barcodeMap={}, genericNameMap={}, tradeNameMap={}",
                allDrugs.size(), originalCodeMap.size(), drugCodeMap.size(),
                barcodeMap.size(), genericNameMap.size(), tradeNameMap.size());

        // ===== 获取默认门店ID =====
        Store defaultStore = storeMapper.selectOne(new LambdaQueryWrapper<Store>()
                .eq(Store::getDeleted, 0).orderByAsc(Store::getId).last("LIMIT 1"));
        Long defaultStoreId = defaultStore != null ? defaultStore.getId() : null;
        log.info("默认门店: id={}, name={}", defaultStoreId, defaultStore != null ? defaultStore.getName() : "无");

        // ===== 预加载供应商缓存（名称 -> ID） =====
        List<Supplier> allSuppliers = supplierMapper.selectList(new LambdaQueryWrapper<Supplier>().eq(Supplier::getDeleted, 0));
        Map<String, Long> supplierCache = new HashMap<>();
        for (Supplier s : allSuppliers) {
            if (s.getName() != null && !s.getName().isEmpty()) {
                supplierCache.put(s.getName().trim(), s.getId());
            }
        }
        log.info("预加载供应商数量: {}", supplierCache.size());

        // ===== 预加载已有批次，避免重复创建 =====
        List<DrugBatch> existingBatches = drugBatchMapper.selectList(new LambdaQueryWrapper<DrugBatch>().eq(DrugBatch::getDeleted, 0));
        // key: drugId + "||" + batchNo
        Map<String, DrugBatch> batchCache = new HashMap<>();
        for (DrugBatch b : existingBatches) {
            if (b.getBatchNo() != null && !b.getBatchNo().isEmpty()) {
                batchCache.put(b.getDrugId() + "||" + b.getBatchNo().trim(), b);
            }
        }
        log.info("预加载已有批次数量: {}", batchCache.size());

        // 统计计数器
        AtomicInteger batchCreated = new AtomicInteger(0);
        AtomicInteger priceUpdated = new AtomicInteger(0);

        Map<Integer, String> columnFieldMap = new HashMap<>();

        EasyExcel.read(task.getFilePath(), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                matchColumns(headMap, fieldAliases, columnFieldMap);
                log.info("库存导入字段匹配结果: {}", columnFieldMap);

                // 检查必要字段是否匹配成功
                boolean hasDrugCode = columnFieldMap.containsValue("drugCode");
                boolean hasGenericName = columnFieldMap.containsValue("genericName");
                if (!hasDrugCode && !hasGenericName) {
                    log.warn("未匹配到药品编码或通用名列，导入可能无法正确关联药品");
                }
            }

            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                total.incrementAndGet();
                try {
                    String drugCode = getFieldValue(row, columnFieldMap, "drugCode");
                    String genericName = getFieldValue(row, columnFieldMap, "genericName");
                    String tradeName = getFieldValue(row, columnFieldMap, "tradeName");
                    String specification = getFieldValue(row, columnFieldMap, "specification");
                    String batchNo = getFieldValue(row, columnFieldMap, "batchNo");
                    String produceDateStr = getFieldValue(row, columnFieldMap, "produceDate");
                    String expireDateStr = getFieldValue(row, columnFieldMap, "expireDate");
                    String quantityStr = getFieldValue(row, columnFieldMap, "quantity");
                    String remainQuantityStr = getFieldValue(row, columnFieldMap, "remainQuantity");
                    String retailPriceStr = getFieldValue(row, columnFieldMap, "retailPrice");
                    String costPriceStr = getFieldValue(row, columnFieldMap, "costPrice");
                    String unit = getFieldValue(row, columnFieldMap, "unit");
                    String location = getFieldValue(row, columnFieldMap, "location");
                    String barcodeVal = getFieldValue(row, columnFieldMap, "barcode");
                    String supplierNameVal = getFieldValue(row, columnFieldMap, "supplierName");

                    // 如果 quantity 为空，尝试用 remainQuantity
                    if ((quantityStr == null || quantityStr.isEmpty()) && remainQuantityStr != null && !remainQuantityStr.isEmpty()) {
                        quantityStr = remainQuantityStr;
                    }

                    if ((drugCode == null || drugCode.isEmpty()) && (genericName == null || genericName.isEmpty())) {
                        skip.incrementAndGet();
                        skipTracker.record(total.get(), "药品编码和通用名均为空", null);
                        return;
                    }

                    // ===== 5级药品匹配策略（优先originalCode） =====
                    Drug drug = null;
                    String searchKey = drugCode != null && !drugCode.isEmpty() ? drugCode : genericName;

                    if (drugCode != null && !drugCode.isEmpty()) {
                        String code = drugCode.trim();
                        // 1. 优先匹配 originalCode（旧系统编号，最可靠）
                        drug = originalCodeMap.get(code);
                        if (drug == null) {
                            // 2. 匹配 drugCode（新系统编号）
                            drug = drugCodeMap.get(code);
                        }
                        if (drug == null) {
                            // 3. 匹配 barcode（条形码）
                            drug = barcodeMap.get(code);
                        }
                    }

                    // 4. 通用名+规格精确匹配
                    if (drug == null && genericName != null && !genericName.isEmpty()) {
                        if (specification != null && !specification.isEmpty()) {
                            String key = genericName.trim() + "||" + specification.trim();
                            drug = nameSpecMap.get(key);
                        }
                        if (drug == null) {
                            // 5. 仅通用名匹配
                            drug = genericNameMap.get(genericName.trim());
                        }
                        searchKey = genericName;
                    }

                    // 6. 商品名匹配（作为最后手段）
                    if (drug == null && tradeName != null && !tradeName.isEmpty()) {
                        drug = tradeNameMap.get(tradeName.trim());
                        searchKey = tradeName;
                    }

                    // 7. 条形码字段单独匹配
                    if (drug == null && barcodeVal != null && !barcodeVal.isEmpty()) {
                        drug = barcodeMap.get(barcodeVal.trim());
                        searchKey = barcodeVal;
                    }

                    if (drug == null) {
                        skip.incrementAndGet();
                        skipTracker.record(total.get(), "未找到匹配药品", searchKey);
                        return;
                    }

                    // ===== 可选：更新药品零售价 =====
                    if (retailPriceStr != null && !retailPriceStr.isEmpty()) {
                        BigDecimal newRetailPrice = parseBigDecimal(retailPriceStr);
                        if (newRetailPrice.compareTo(BigDecimal.ZERO) > 0
                                && (drug.getRetailPrice() == null || drug.getRetailPrice().compareTo(BigDecimal.ZERO) == 0)) {
                            drug.setRetailPrice(newRetailPrice);
                            drugMapper.updateById(drug);
                            priceUpdated.incrementAndGet();
                        }
                    }

                    // ===== 解析供应商 =====
                    Long supplierId = null;
                    if (supplierNameVal != null && !supplierNameVal.isEmpty()) {
                        String sName = supplierNameVal.trim();
                        supplierId = supplierCache.get(sName);
                        if (supplierId == null) {
                            // 自动创建供应商
                            Supplier newSupplier = new Supplier();
                            newSupplier.setName(sName);
                            newSupplier.setStatus("启用");
                            supplierMapper.insert(newSupplier);
                            supplierId = newSupplier.getId();
                            supplierCache.put(sName, supplierId);
                        }
                    }

                    // ===== 创建或查找 DrugBatch =====
                    Long batchId = null;
                    if (batchNo != null && !batchNo.isEmpty()) {
                        String batchKey = drug.getId() + "||" + batchNo.trim();
                        DrugBatch existBatch = batchCache.get(batchKey);
                        if (existBatch != null) {
                            batchId = existBatch.getId();
                            // 如果已有批次缺少供应商，补充设置
                            if (supplierId != null && existBatch.getSupplierId() == null) {
                                existBatch.setSupplierId(supplierId);
                                drugBatchMapper.updateById(existBatch);
                            }
                        } else {
                            // 创建新批次
                            DrugBatch newBatch = new DrugBatch();
                            newBatch.setDrugId(drug.getId());
                            newBatch.setBatchNo(batchNo.trim());
                            newBatch.setProduceDate(parseDate(produceDateStr));
                            newBatch.setExpireDate(parseDate(expireDateStr));
                            if (costPriceStr != null && !costPriceStr.isEmpty()) {
                                newBatch.setPurchasePrice(parseBigDecimal(costPriceStr));
                            }
                            newBatch.setSupplierId(supplierId);
                            newBatch.setStatus("active");
                            drugBatchMapper.insert(newBatch);
                            batchId = newBatch.getId();
                            // 放入缓存避免重复创建
                            batchCache.put(batchKey, newBatch);
                            batchCreated.incrementAndGet();
                        }
                    }

                    // ===== 创建 Inventory 记录 =====
                    Inventory inventory = new Inventory();
                    inventory.setStoreId(defaultStoreId);
                    inventory.setDrugId(drug.getId());
                    inventory.setBatchId(batchId);
                    inventory.setBatchNo(batchNo != null ? batchNo.trim() : null);
                    inventory.setQuantity(parseBigDecimal(quantityStr));
                    if (costPriceStr != null && !costPriceStr.isEmpty()) {
                        inventory.setCostPrice(parseBigDecimal(costPriceStr));
                    }
                    inventory.setUnit(unit != null ? unit : drug.getUnit());
                    inventory.setLocation(location);

                    inventoryMapper.insert(inventory);
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                    errorLog.append("第").append(total.get()).append("行: ").append(e.getMessage()).append("\n");
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                log.info("库存导入完成: 总计={}, 成功={}, 跳过={}, 失败={}, 新建批次={}, 更新零售价={}",
                        total.get(), success.get(), skip.get(), fail.get(),
                        batchCreated.get(), priceUpdated.get());
            }
        }).headRowNumber(1).sheet().doRead();

        task.setTotalCount(total.get());
        task.setSuccessCount(success.get());
        task.setFailCount(fail.get());
        task.setSkipCount(skip.get());
        String extraInfo = "新建批次: " + batchCreated.get() + " 条，更新零售价: " + priceUpdated.get() + " 条";
        if (errorLog.length() > 0) {
            task.setErrorLog(extraInfo + "\n" + errorLog.toString());
        } else {
            task.setErrorLog(extraInfo);
        }
        task.setSkipLog(skipTracker.buildLog());
    }
}
