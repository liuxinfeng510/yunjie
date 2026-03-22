package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.EquipmentInspectionLog;
import com.yf.entity.MaintenanceEquipment;
import com.yf.mapper.EquipmentInspectionLogMapper;
import com.yf.mapper.MaintenanceEquipmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceEquipmentService {

    private final MaintenanceEquipmentMapper equipmentMapper;
    private final EquipmentInspectionLogMapper inspectionMapper;

    public MaintenanceEquipment createEquipment(MaintenanceEquipment eq) {
        if (eq.getStatus() == null) {
            eq.setStatus("active");
        }
        if (eq.getNextInspectionDate() == null && eq.getPurchaseDate() != null) {
            eq.setNextInspectionDate(calcNextDate(eq.getPurchaseDate(), eq.getInspectionCycle()));
        }
        equipmentMapper.insert(eq);
        return eq;
    }

    public void updateEquipment(Long id, MaintenanceEquipment eq) {
        MaintenanceEquipment existing = equipmentMapper.selectById(id);
        if (existing == null) return;
        eq.setId(id);
        equipmentMapper.updateById(eq);
    }

    public void retireEquipment(Long id) {
        MaintenanceEquipment eq = equipmentMapper.selectById(id);
        if (eq != null) {
            eq.setStatus("retired");
            equipmentMapper.updateById(eq);
        }
    }

    public Page<MaintenanceEquipment> pageEquipments(String type, String status, int pageNum, int pageSize) {
        Page<MaintenanceEquipment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MaintenanceEquipment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq(MaintenanceEquipment::getEquipmentType, type);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MaintenanceEquipment::getStatus, status);
        }
        wrapper.orderByDesc(MaintenanceEquipment::getCreatedAt);
        return equipmentMapper.selectPage(page, wrapper);
    }

    public List<MaintenanceEquipment> listEquipments() {
        LambdaQueryWrapper<MaintenanceEquipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaintenanceEquipment::getStatus, "active");
        wrapper.orderByAsc(MaintenanceEquipment::getEquipmentName);
        return equipmentMapper.selectList(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public EquipmentInspectionLog createInspection(EquipmentInspectionLog log) {
        // 填充设备名称快照
        MaintenanceEquipment eq = equipmentMapper.selectById(log.getEquipmentId());
        if (eq != null) {
            log.setEquipmentName(eq.getEquipmentName());
        }
        if (log.getInspectionDate() == null) {
            log.setInspectionDate(LocalDate.now());
        }
        // 计算下次检查日期
        if (log.getNextInspectionDate() == null && eq != null) {
            log.setNextInspectionDate(calcNextDate(log.getInspectionDate(), eq.getInspectionCycle()));
        }
        inspectionMapper.insert(log);

        // 更新设备的检查日期
        if (eq != null) {
            eq.setLastInspectionDate(log.getInspectionDate());
            eq.setNextInspectionDate(log.getNextInspectionDate());
            equipmentMapper.updateById(eq);
        }
        return log;
    }

    public Page<EquipmentInspectionLog> pageInspections(Long equipmentId, LocalDate startDate,
                                                         LocalDate endDate, int pageNum, int pageSize) {
        Page<EquipmentInspectionLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<EquipmentInspectionLog> wrapper = new LambdaQueryWrapper<>();
        if (equipmentId != null) {
            wrapper.eq(EquipmentInspectionLog::getEquipmentId, equipmentId);
        }
        if (startDate != null) {
            wrapper.ge(EquipmentInspectionLog::getInspectionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(EquipmentInspectionLog::getInspectionDate, endDate);
        }
        wrapper.orderByDesc(EquipmentInspectionLog::getInspectionDate);
        return inspectionMapper.selectPage(page, wrapper);
    }

    public List<EquipmentInspectionLog> listInspections(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<EquipmentInspectionLog> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(EquipmentInspectionLog::getInspectionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(EquipmentInspectionLog::getInspectionDate, endDate);
        }
        wrapper.orderByDesc(EquipmentInspectionLog::getInspectionDate);
        return inspectionMapper.selectList(wrapper);
    }

    private LocalDate calcNextDate(LocalDate from, String cycle) {
        if (from == null) return null;
        return switch (cycle != null ? cycle : "annual") {
            case "monthly" -> from.plusMonths(1);
            case "quarterly" -> from.plusMonths(3);
            case "semi_annual" -> from.plusMonths(6);
            default -> from.plusYears(1);
        };
    }
}
