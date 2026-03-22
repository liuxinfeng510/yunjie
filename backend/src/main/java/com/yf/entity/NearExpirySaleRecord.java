package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("near_expiry_sale_record")
public class NearExpirySaleRecord extends BaseEntity {
    private Long storeId;
    private String reportMonth;
    private Long drugId;
    private String drugName;
    private String specification;
    private String manufacturer;
    private Long batchId;
    private String batchNo;
    private LocalDate expireDate;
    private Integer remainingDays;
    private BigDecimal stockQuantity;
    private String unit;
    private String saleMeasure;
    private String measureDetail;
    private String status;
    private String handlerName;
    private LocalDateTime completeTime;
    private String resultRemark;
}
