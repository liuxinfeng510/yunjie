package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("maintenance_equipment")
public class MaintenanceEquipment extends BaseEntity {
    private String equipmentName;
    private String equipmentType;
    private String model;
    private String manufacturer;
    private String location;
    private LocalDate purchaseDate;
    private String inspectionCycle;
    private LocalDate lastInspectionDate;
    private LocalDate nextInspectionDate;
    private String status;
    private String remark;
}
