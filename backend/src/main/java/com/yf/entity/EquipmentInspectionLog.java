package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("equipment_inspection_log")
public class EquipmentInspectionLog extends BaseEntity {
    private Long equipmentId;
    private String equipmentName;
    private LocalDate inspectionDate;
    private String appearanceCheck;
    private String functionCheck;
    private String inspectionResult;
    private String abnormalDesc;
    private String treatment;
    private String inspectorName;
    private LocalDate nextInspectionDate;
    private String remark;
}
