package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户表 - SaaS多租户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant")
public class Tenant extends BaseEntity {
    /** 租户编码 */
    private String code;
    /** 租户名称（企业/药店名） */
    private String name;
    /** 统一社会信用代码 */
    private String creditCode;
    /** 联系人 */
    private String contactName;
    /** 联系电话 */
    private String contactPhone;
    /** 经营模式: single_store / chain_store */
    private String businessMode;
    /** 状态: active / disabled */
    private String status;
}
