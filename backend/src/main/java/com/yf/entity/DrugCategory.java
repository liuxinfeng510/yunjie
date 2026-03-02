package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
}
