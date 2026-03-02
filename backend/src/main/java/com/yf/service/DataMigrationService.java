package com.yf.service;

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
import java.time.LocalDateTime;
import java.util.UUID;

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
        // 读取Excel文件，解析药品数据并批量插入
        // 字段映射: 通用名、商品名、规格、剂型、厂家、批准文号、条形码、分类、零售价、批发价
        log.info("开始处理药品数据导入，任务ID={}", task.getId());
        // TODO: 使用EasyExcel读取文件并逐行解析插入
        task.setTotalCount(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setSkipCount(0);
    }

    private void processHerbImport(DataMigrationTask task) {
        // 读取Excel文件，解析中药数据并批量插入
        // 字段映射: 名称、拼音、性味、归经、功效、适应症、用量、产地、等级
        log.info("开始处理中药数据导入，任务ID={}", task.getId());
        task.setTotalCount(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setSkipCount(0);
    }

    private void processMemberImport(DataMigrationTask task) {
        // 读取Excel文件，解析会员数据并批量插入
        // 字段映射: 姓名、手机号、性别、生日、身份证、地址、会员等级、积分余额
        log.info("开始处理会员数据导入，任务ID={}", task.getId());
        task.setTotalCount(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setSkipCount(0);
    }

    private void processSupplierImport(DataMigrationTask task) {
        // 读取Excel文件，解析供应商数据并批量插入
        // 字段映射: 供应商名称、联系人、电话、地址、营业执照号、GSP证书、经营范围
        log.info("开始处理供应商数据导入，任务ID={}", task.getId());
        task.setTotalCount(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setSkipCount(0);
    }

    private void processInventoryImport(DataMigrationTask task) {
        // 读取Excel文件，解析库存数据并批量插入
        // 字段映射: 药品名称/编码、批号、数量、进价、有效期、存储位置
        log.info("开始处理库存数据导入，任务ID={}", task.getId());
        task.setTotalCount(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setSkipCount(0);
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
