package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DrugMaintenance;
import com.yf.entity.Store;
import com.yf.entity.SysUser;
import com.yf.mapper.DrugMaintenanceMapper;
import com.yf.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 药品养护服务
 */
@Service
@RequiredArgsConstructor
public class DrugMaintenanceService {
    
    private final DrugMaintenanceMapper drugMaintenanceMapper;
    private final StoreService storeService;
    private final SysUserMapper sysUserMapper;
    
    /**
     * 分页查询养护记录
     */
    public Page<DrugMaintenance> page(Long storeId, String maintenanceType, 
                                       LocalDateTime startTime, LocalDateTime endTime,
                                       int pageNum, int pageSize) {
        Page<DrugMaintenance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DrugMaintenance> wrapper = new LambdaQueryWrapper<>();
        
        if (storeId != null) {
            wrapper.eq(DrugMaintenance::getStoreId, storeId);
        }
        if (maintenanceType != null) {
            wrapper.eq(DrugMaintenance::getMaintenanceType, maintenanceType);
        }
        if (startTime != null) {
            wrapper.ge(DrugMaintenance::getMaintenanceTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(DrugMaintenance::getMaintenanceTime, endTime);
        }
        
        wrapper.orderByDesc(DrugMaintenance::getMaintenanceTime);
        return drugMaintenanceMapper.selectPage(page, wrapper);
    }
    
    /**
     * 创建养护记录
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
                java.util.List<Store> stores = storeService.listAll();
                if (!stores.isEmpty()) {
                    maintenance.setStoreId(stores.get(0).getId());
                }
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
}
