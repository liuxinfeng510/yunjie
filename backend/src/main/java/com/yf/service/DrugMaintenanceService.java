package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Drug;
import com.yf.entity.DrugMaintenance;
import com.yf.entity.Store;
import com.yf.entity.SysUser;
import com.yf.mapper.DrugMaintenanceMapper;
import com.yf.mapper.DrugMapper;
import com.yf.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 药品养护服务
 */
@Service
@RequiredArgsConstructor
public class DrugMaintenanceService {
    
    private final DrugMaintenanceMapper drugMaintenanceMapper;
    private final StoreService storeService;
    private final SysUserMapper sysUserMapper;
    private final DrugMapper drugMapper;
    
    /**
     * 分页查询养护记录（支持药品名称、是否重点养护筛选）
     */
    public Page<DrugMaintenance> page(Long storeId, String maintenanceType, 
                                       LocalDateTime startTime, LocalDateTime endTime,
                                       String drugName, Boolean isKeyDrug,
                                       int pageNum, int pageSize) {
        Page<DrugMaintenance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DrugMaintenance> wrapper = new LambdaQueryWrapper<>();
        
        if (storeId != null) {
            wrapper.eq(DrugMaintenance::getStoreId, storeId);
        }
        if (maintenanceType != null && !maintenanceType.isEmpty()) {
            wrapper.eq(DrugMaintenance::getMaintenanceType, maintenanceType);
        }
        if (startTime != null) {
            wrapper.ge(DrugMaintenance::getMaintenanceTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(DrugMaintenance::getMaintenanceTime, endTime);
        }
        if (drugName != null && !drugName.isEmpty()) {
            wrapper.like(DrugMaintenance::getDrugName, drugName);
        }
        if (isKeyDrug != null) {
            wrapper.eq(DrugMaintenance::getIsKeyDrug, isKeyDrug);
        }
        
        wrapper.orderByDesc(DrugMaintenance::getMaintenanceTime);
        return drugMaintenanceMapper.selectPage(page, wrapper);
    }
    
    /**
     * 创建养护记录（自动填充药品快照）
     */
    @Transactional(rollbackFor = Exception.class)
    public DrugMaintenance create(DrugMaintenance maintenance) {
        maintenance.setMaintenanceTime(LocalDateTime.now());
        // 自动填充门店ID
        if (maintenance.getStoreId() == null) {
            Store hq = storeService.getHeadquarter();
            if (hq != null) {
                maintenance.setStoreId(hq.getId());
            } else {
                List<Store> stores = storeService.listAll();
                if (!stores.isEmpty()) {
                    maintenance.setStoreId(stores.get(0).getId());
                }
            }
        }
        // 自动填充药品快照
        if (maintenance.getDrugId() != null) {
            Drug drug = drugMapper.selectById(maintenance.getDrugId());
            if (drug != null) {
                if (maintenance.getDrugName() == null) maintenance.setDrugName(drug.getGenericName());
                if (maintenance.getSpecification() == null) maintenance.setSpecification(drug.getSpecification());
                if (maintenance.getManufacturer() == null) maintenance.setManufacturer(drug.getManufacturer());
            }
        }
        // 快照：填充养护人姓名
        if (maintenance.getOperatorId() != null) {
            SysUser user = sysUserMapper.selectById(maintenance.getOperatorId());
            if (user != null) {
                maintenance.setOperatorName(user.getRealName());
            }
        }
        drugMaintenanceMapper.insert(maintenance);
        return maintenance;
    }

    /**
     * 查询某药品的养护历史（重点养护档案）
     */
    public List<DrugMaintenance> listByDrug(Long drugId) {
        LambdaQueryWrapper<DrugMaintenance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugMaintenance::getDrugId, drugId);
        wrapper.orderByDesc(DrugMaintenance::getMaintenanceTime);
        return drugMaintenanceMapper.selectList(wrapper);
    }

    /**
     * 按日期范围查询（非分页，用于打印养护记录表）
     */
    public List<DrugMaintenance> listByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<DrugMaintenance> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            wrapper.ge(DrugMaintenance::getMaintenanceTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(DrugMaintenance::getMaintenanceTime, endTime);
        }
        wrapper.orderByDesc(DrugMaintenance::getMaintenanceTime);
        return drugMaintenanceMapper.selectList(wrapper);
    }
}
