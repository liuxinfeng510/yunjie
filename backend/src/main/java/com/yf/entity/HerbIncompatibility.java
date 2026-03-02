package com.yf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 中药配伍禁忌实体
 * 全局共享表，不需要租户隔离
 */
@Data
@TableName("herb_incompatibility")
public class HerbIncompatibility {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    /** 禁忌类型: 18_oppose(十八反)/19_fear(十九畏)/pregnancy(妊娠禁忌) */
    private String type;

    /** 药材A */
    private String herbA;

    /** 药材B */
    private String herbB;

    /** 禁忌说明 */
    private String description;

    /** 严重程度: forbidden(禁用)/caution(慎用) */
    private String severity;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    private Integer deleted;
}
