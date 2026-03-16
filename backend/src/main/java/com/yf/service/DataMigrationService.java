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
    private final HerbMapper herbMapper;
    private final MemberMapper memberMapper;
    private final SupplierMapper supplierMapper;
    private final InventoryMapper inventoryMapper;

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
        fieldAliases.put("tradeName", Arrays.asList("商品名", "商品名称", "tradename", "商品"));
        fieldAliases.put("specification", Arrays.asList("规格", "spec", "specification"));
        fieldAliases.put("dosageForm", Arrays.asList("剂型", "dosageform", "剂形"));
        fieldAliases.put("manufacturer", Arrays.asList("厂家", "生产厂家", "生产厂商", "生产企业", "manufacturer", "厂商"));
        fieldAliases.put("approvalNo", Arrays.asList("批准文号", "国药准字", "approvalno", "文号"));
        fieldAliases.put("barcode", Arrays.asList("条形码", "条码", "barcode", "商品条码", "ean"));
        fieldAliases.put("originalCode", Arrays.asList("原编号", "原系统编号", "商品编号", "编号", "旧编号"));
        fieldAliases.put("unit", Arrays.asList("单位", "unit", "包装单位", "计量单位"));
        fieldAliases.put("retailPrice", Arrays.asList("零售价", "售价", "商品零售价", "retail", "retailprice", "销售价"));
        fieldAliases.put("purchasePrice", Arrays.asList("采购价", "进价", "进货价", "purchase", "purchaseprice", "成本价"));
        fieldAliases.put("memberPrice", Arrays.asList("会员价", "memberprice", "vip价"));
        fieldAliases.put("storageCondition", Arrays.asList("储存条件", "存储条件", "storage", "贮藏"));
        fieldAliases.put("otcType", Arrays.asList("otc类型", "otc", "处方类型"));
        fieldAliases.put("medicalInsurance", Arrays.asList("医保类型", "医保", "医保编码"));

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

                    if (genericName == null || genericName.isEmpty()) {
                        skip.incrementAndGet();
                        skipTracker.record(total.get(), "药品名称为空", null);
                        return;
                    }

                    // 检查重复
                    if (barcode != null && !barcode.isEmpty()) {
                        LambdaQueryWrapper<Drug> w = new LambdaQueryWrapper<>();
                        w.eq(Drug::getBarcode, barcode);
                        if (drugMapper.selectCount(w) > 0) {
                            skip.incrementAndGet();
                            skipTracker.record(total.get(), "条形码重复", barcode);
                            return;
                        }
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
                    drug.setUnit(unit != null ? unit : "盒");
                    drug.setRetailPrice(parseBigDecimal(retailPriceStr));
                    drug.setPurchasePrice(parseBigDecimal(purchasePriceStr));
                    drug.setMemberPrice(parseBigDecimal(memberPriceStr));
                    drug.setStorageCondition(storageCondition);
                    drug.setOtcType(otcType);
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

        Map<String, List<String>> fieldAliases = new LinkedHashMap<>();
        fieldAliases.put("drugCode", Arrays.asList("药品编码", "编码", "drugcode", "条码", "barcode", "条形码", "药品编号", "商品编号"));
        fieldAliases.put("drugName", Arrays.asList("药品名称", "商品名称", "品名", "通用名", "名称", "药品名"));
        fieldAliases.put("batchNo", Arrays.asList("批号", "批次号", "batchno", "生产批号"));
        fieldAliases.put("quantity", Arrays.asList("数量", "库存数量", "quantity", "库存量", "在库数量"));
        fieldAliases.put("costPrice", Arrays.asList("进价", "成本价", "costprice", "采购价", "进货价"));
        fieldAliases.put("unit", Arrays.asList("单位", "unit", "计量单位"));
        fieldAliases.put("location", Arrays.asList("货位", "库位", "location", "存放位置", "储位"));

        Map<Integer, String> columnFieldMap = new HashMap<>();

        EasyExcel.read(task.getFilePath(), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                matchColumns(headMap, fieldAliases, columnFieldMap);
                log.info("库存导入字段匹配结果: {}", columnFieldMap);
            }

            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                total.incrementAndGet();
                try {
                    String drugCode = getFieldValue(row, columnFieldMap, "drugCode");
                    String drugName = getFieldValue(row, columnFieldMap, "drugName");
                    String batchNo = getFieldValue(row, columnFieldMap, "batchNo");
                    String quantityStr = getFieldValue(row, columnFieldMap, "quantity");
                    String costPriceStr = getFieldValue(row, columnFieldMap, "costPrice");
                    String unit = getFieldValue(row, columnFieldMap, "unit");
                    String location = getFieldValue(row, columnFieldMap, "location");

                    if ((drugCode == null || drugCode.isEmpty()) && (drugName == null || drugName.isEmpty())) {
                        skip.incrementAndGet();
                        skipTracker.record(total.get(), "药品编码和名称均为空", null);
                        return;
                    }

                    // 查找药品：依次尝试 drugCode -> barcode -> originalCode -> drugName -> genericName(drugCode)
                    Drug drug = null;
                    String searchKey = drugCode != null && !drugCode.isEmpty() ? drugCode : drugName;
                    if (drugCode != null && !drugCode.isEmpty()) {
                        // 1. 尝试用药品编码查找
                        LambdaQueryWrapper<Drug> dw = new LambdaQueryWrapper<>();
                        dw.eq(Drug::getDrugCode, drugCode);
                        drug = drugMapper.selectOne(dw);
                        if (drug == null) {
                            // 2. 尝试用条形码查找
                            LambdaQueryWrapper<Drug> bw = new LambdaQueryWrapper<>();
                            bw.eq(Drug::getBarcode, drugCode);
                            drug = drugMapper.selectOne(bw);
                        }
                        if (drug == null) {
                            // 3. 尝试用原系统编号查找
                            LambdaQueryWrapper<Drug> ow = new LambdaQueryWrapper<>();
                            ow.eq(Drug::getOriginalCode, drugCode);
                            drug = drugMapper.selectOne(ow);
                        }
                    }
                    if (drug == null && drugName != null && !drugName.isEmpty()) {
                        // 4. 尝试用通用名查找
                        LambdaQueryWrapper<Drug> nw = new LambdaQueryWrapper<>();
                        nw.eq(Drug::getGenericName, drugName);
                        drug = drugMapper.selectOne(nw);
                        searchKey = drugName;
                    }
                    if (drug == null && drugCode != null && !drugCode.isEmpty()) {
                        // 5. 最后用drugCode尝试名称匹配
                        LambdaQueryWrapper<Drug> nw = new LambdaQueryWrapper<>();
                        nw.eq(Drug::getGenericName, drugCode);
                        drug = drugMapper.selectOne(nw);
                    }
                    if (drug == null) {
                        fail.incrementAndGet();
                        errorLog.append("第").append(total.get()).append("行: 未找到药品 ").append(searchKey).append("\n");
                        return;
                    }

                    Inventory inventory = new Inventory();
                    inventory.setDrugId(drug.getId());
                    inventory.setBatchNo(batchNo);
                    inventory.setQuantity(parseBigDecimal(quantityStr));
                    inventory.setCostPrice(parseBigDecimal(costPriceStr));
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
}
