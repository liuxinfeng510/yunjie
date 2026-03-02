package com.yf.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.*;
import com.yf.exception.BusinessException;
import com.yf.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据迁移服务 - 从旧系统导入数据
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

    // ==================== 各模块数据导入处理 ====================

    private void processDrugImport(DataMigrationTask task) {
        log.info("开始处理药品数据导入，任务ID={}", task.getId());
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger skip = new AtomicInteger(0);
        StringBuilder errorLog = new StringBuilder();

        EasyExcel.read(task.getFilePath(), new ReadListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                total.incrementAndGet();
                try {
                    String genericName = row.get(0);  // 通用名
                    String tradeName = row.get(1);     // 商品名
                    String specification = row.get(2); // 规格
                    String dosageForm = row.get(3);    // 剂型
                    String manufacturer = row.get(4);  // 厂家
                    String approvalNo = row.get(5);    // 批准文号
                    String barcode = row.get(6);       // 条形码
                    String unit = row.get(7);          // 单位
                    String retailPriceStr = row.get(8);// 零售价
                    String purchasePriceStr = row.get(9); // 采购价

                    if (genericName == null || genericName.trim().isEmpty()) {
                        skip.incrementAndGet();
                        return;
                    }

                    // 检查重复
                    if (barcode != null && !barcode.trim().isEmpty()) {
                        LambdaQueryWrapper<Drug> w = new LambdaQueryWrapper<>();
                        w.eq(Drug::getBarcode, barcode.trim());
                        if (drugMapper.selectCount(w) > 0) {
                            skip.incrementAndGet();
                            return;
                        }
                    }

                    Drug drug = new Drug();
                    drug.setGenericName(genericName.trim());
                    drug.setTradeName(tradeName != null ? tradeName.trim() : null);
                    drug.setSpecification(specification != null ? specification.trim() : null);
                    drug.setDosageForm(dosageForm != null ? dosageForm.trim() : null);
                    drug.setManufacturer(manufacturer != null ? manufacturer.trim() : null);
                    drug.setApprovalNo(approvalNo != null ? approvalNo.trim() : null);
                    drug.setBarcode(barcode != null ? barcode.trim() : null);
                    drug.setUnit(unit != null ? unit.trim() : "盒");
                    drug.setRetailPrice(parseBigDecimal(retailPriceStr));
                    drug.setPurchasePrice(parseBigDecimal(purchasePriceStr));
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
    }

    private void processHerbImport(DataMigrationTask task) {
        log.info("开始处理中药数据导入，任务ID={}", task.getId());
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger skip = new AtomicInteger(0);
        StringBuilder errorLog = new StringBuilder();

        EasyExcel.read(task.getFilePath(), new ReadListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                total.incrementAndGet();
                try {
                    String name = row.get(0);       // 名称
                    String pinyin = row.get(1);      // 拼音
                    String nature = row.get(2);      // 性味
                    String meridian = row.get(3);    // 归经
                    String efficacy = row.get(4);    // 功效
                    String origin = row.get(5);      // 产地
                    String retailPriceStr = row.get(6); // 零售价
                    String purchasePriceStr = row.get(7); // 进货价

                    if (name == null || name.trim().isEmpty()) {
                        skip.incrementAndGet();
                        return;
                    }

                    // 检查重复
                    LambdaQueryWrapper<Herb> w = new LambdaQueryWrapper<>();
                    w.eq(Herb::getName, name.trim());
                    if (herbMapper.selectCount(w) > 0) {
                        skip.incrementAndGet();
                        return;
                    }

                    Herb herb = new Herb();
                    herb.setName(name.trim());
                    herb.setPinyin(pinyin != null ? pinyin.trim() : null);
                    herb.setNature(nature != null ? nature.trim() : null);
                    herb.setMeridian(meridian != null ? meridian.trim() : null);
                    herb.setEfficacy(efficacy != null ? efficacy.trim() : null);
                    herb.setOrigin(origin != null ? origin.trim() : null);
                    herb.setRetailPrice(parseBigDecimal(retailPriceStr));
                    herb.setPurchasePrice(parseBigDecimal(purchasePriceStr));
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
    }

    private void processMemberImport(DataMigrationTask task) {
        log.info("开始处理会员数据导入，任务ID={}", task.getId());
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger skip = new AtomicInteger(0);
        StringBuilder errorLog = new StringBuilder();

        EasyExcel.read(task.getFilePath(), new ReadListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                total.incrementAndGet();
                try {
                    String name = row.get(0);    // 姓名
                    String phone = row.get(1);   // 手机号
                    String gender = row.get(2);  // 性别
                    String birthday = row.get(3);// 生日
                    String idCard = row.get(4);  // 身份证

                    if (phone == null || phone.trim().isEmpty()) {
                        skip.incrementAndGet();
                        return;
                    }

                    // 检查手机号重复
                    LambdaQueryWrapper<Member> w = new LambdaQueryWrapper<>();
                    w.eq(Member::getPhone, phone.trim());
                    if (memberMapper.selectCount(w) > 0) {
                        skip.incrementAndGet();
                        return;
                    }

                    Member member = new Member();
                    member.setMemberNo("M" + System.currentTimeMillis() + total.get());
                    member.setName(name != null ? name.trim() : "");
                    member.setPhone(phone.trim());
                    member.setGender(gender != null ? gender.trim() : null);
                    if (birthday != null && !birthday.trim().isEmpty()) {
                        try {
                            member.setBirthday(java.time.LocalDate.parse(birthday.trim()));
                        } catch (Exception ignored) {}
                    }
                    member.setIdCard(idCard != null ? idCard.trim() : null);
                    member.setPoints(0);
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
    }

    private void processSupplierImport(DataMigrationTask task) {
        log.info("开始处理供应商数据导入，任务ID={}", task.getId());
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger skip = new AtomicInteger(0);
        StringBuilder errorLog = new StringBuilder();

        EasyExcel.read(task.getFilePath(), new ReadListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                total.incrementAndGet();
                try {
                    String name = row.get(0);         // 供应商名称
                    String contactName = row.get(1);  // 联系人
                    String contactPhone = row.get(2); // 联系电话
                    String address = row.get(3);      // 地址
                    String creditCode = row.get(4);   // 统一社会信用代码

                    if (name == null || name.trim().isEmpty()) {
                        skip.incrementAndGet();
                        return;
                    }

                    // 检查名称重复
                    LambdaQueryWrapper<Supplier> w = new LambdaQueryWrapper<>();
                    w.eq(Supplier::getName, name.trim());
                    if (supplierMapper.selectCount(w) > 0) {
                        skip.incrementAndGet();
                        return;
                    }

                    Supplier supplier = new Supplier();
                    supplier.setName(name.trim());
                    supplier.setContactName(contactName != null ? contactName.trim() : null);
                    supplier.setContactPhone(contactPhone != null ? contactPhone.trim() : null);
                    supplier.setAddress(address != null ? address.trim() : null);
                    supplier.setCreditCode(creditCode != null ? creditCode.trim() : null);
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
    }

    private void processInventoryImport(DataMigrationTask task) {
        log.info("开始处理库存数据导入，任务ID={}", task.getId());
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger skip = new AtomicInteger(0);
        StringBuilder errorLog = new StringBuilder();

        EasyExcel.read(task.getFilePath(), new ReadListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                total.incrementAndGet();
                try {
                    String drugCode = row.get(0);     // 药品编码
                    String batchNo = row.get(1);      // 批号
                    String quantityStr = row.get(2);  // 数量
                    String costPriceStr = row.get(3); // 进价
                    String unit = row.get(4);         // 单位
                    String location = row.get(5);     // 货位

                    if (drugCode == null || drugCode.trim().isEmpty()) {
                        skip.incrementAndGet();
                        return;
                    }

                    // 查找药品
                    LambdaQueryWrapper<Drug> dw = new LambdaQueryWrapper<>();
                    dw.eq(Drug::getDrugCode, drugCode.trim());
                    Drug drug = drugMapper.selectOne(dw);
                    if (drug == null) {
                        // 尝试用条形码查找
                        LambdaQueryWrapper<Drug> bw = new LambdaQueryWrapper<>();
                        bw.eq(Drug::getBarcode, drugCode.trim());
                        drug = drugMapper.selectOne(bw);
                    }
                    if (drug == null) {
                        fail.incrementAndGet();
                        errorLog.append("第").append(total.get()).append("行: 未找到药品 ").append(drugCode).append("\n");
                        return;
                    }

                    Inventory inventory = new Inventory();
                    inventory.setDrugId(drug.getId());
                    inventory.setBatchNo(batchNo != null ? batchNo.trim() : null);
                    inventory.setQuantity(parseBigDecimal(quantityStr));
                    inventory.setCostPrice(parseBigDecimal(costPriceStr));
                    inventory.setUnit(unit != null ? unit.trim() : drug.getUnit());
                    inventory.setLocation(location != null ? location.trim() : null);

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
}
