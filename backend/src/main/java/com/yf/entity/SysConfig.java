package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置表 - 存储租户级别的配置项
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {
    /** 配置分组 */
    private String configGroup;
    /** 配置键 */
    private String configKey;
    /** 配置值 */
    private String configValue;
    /** 值类型: string / number / boolean / json */
    private String valueType;
    /** 配置说明 */
    private String description;
    /** 排序号 */
    private Integer sortOrder;
}
