package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 联合用药推荐实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drug_combination")
public class DrugCombination extends BaseEntity {
    
    /**
     * 主药品ID
     */
    private Long primaryDrugId;
    
    /**
     * 主商品名称（冗余）
     */
    private String primaryDrugName;
    
    /**
     * 推荐药品ID
     */
    private Long recommendDrugId;
    
    /**
     * 推荐商品名称（冗余）
     */
    private String recommendDrugName;
    
    /**
     * 推荐类型：enhance-增效, symptom-对症, prevent-预防副作用
     */
    private String recommendType;
    
    /**
     * 适应症/场景
     */
    private String indication;
    
    /**
     * 推荐理由
     */
    private String reason;
    
    /**
     * 推荐权重（用于排序，越高越优先推荐）
     */
    private Integer weight;
    
    /**
     * 状态：active-启用, inactive-停用
     */
    private String status;
}
