package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 会员健康画像实体
 * 存储会员的健康相关信息，用于精准营销和用药建议
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_health_profile")
public class MemberHealthProfile extends BaseEntity {
    
    /**
     * 会员ID
     */
    private Long memberId;
    
    /**
     * 身高(cm)
     */
    private BigDecimal height;
    
    /**
     * 体重(kg)
     */
    private BigDecimal weight;
    
    /**
     * BMI指数
     */
    private BigDecimal bmi;
    
    /**
     * 血型
     */
    private String bloodType;
    
    /**
     * 过敏史（JSON格式：药物/食物/其他）
     */
    private String allergyHistory;
    
    /**
     * 家族病史
     */
    private String familyHistory;
    
    /**
     * 手术史
     */
    private String surgeryHistory;
    
    /**
     * 当前用药情况（长期用药JSON）
     */
    private String currentMedications;
    
    /**
     * 饮酒情况：不饮酒/偶尔/经常
     */
    private String drinkingStatus;
    
    /**
     * 吸烟情况：不吸烟/已戒烟/吸烟
     */
    private String smokingStatus;
    
    /**
     * 运动习惯：不运动/偶尔/经常
     */
    private String exerciseHabit;
    
    /**
     * 睡眠质量：好/一般/差
     */
    private String sleepQuality;
    
    /**
     * 健康标签（JSON数组：高血压、糖尿病、老年人等）
     */
    private String healthTags;
    
    /**
     * 最近更新日期
     */
    private LocalDate lastUpdateDate;
    
    /**
     * 备注
     */
    private String remark;
}
