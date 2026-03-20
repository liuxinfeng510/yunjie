package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 药品分类实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drug_category")
public class DrugCategory extends BaseEntity {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父级分类ID
     */
    private Long parentId;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否系统预置分类（全局共享，不可修改删除）
     */
    private Boolean isSystem;

    /**
     * 子分类（非数据库字段，用于树形结构）
     */
    @TableField(exist = false)
    private List<DrugCategory> children;
}
