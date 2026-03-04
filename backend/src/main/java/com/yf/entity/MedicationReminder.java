package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 用药提醒实体
 * 管理会员的用药提醒计划
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("medication_reminder")
public class MedicationReminder extends BaseEntity {
    
    /**
     * 会员ID
     */
    private Long memberId;
    
    /**
     * 关联慢病记录ID
     */
    private Long diseaseRecordId;
    
    /**
     * 药品ID
     */
    private Long drugId;
    
    /**
     * 商品名称（冗余）
     */
    private String drugName;
    
    /**
     * 规格（冗余）
     */
    private String specification;
    
    /**
     * 单次用量
     */
    private String dosage;
    
    /**
     * 用法：oral/injection/external/other
     */
    private String usageMethod;
    
    /**
     * 提醒频率：daily/twice_daily/three_times/weekly/custom
     */
    private String frequency;
    
    /**
     * 提醒时间（JSON数组：["08:00","12:00","18:00"]）
     */
    private String reminderTimes;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期（长期用药可为空）
     */
    private LocalDate endDate;
    
    /**
     * 提醒方式：sms/wechat/app/all
     */
    private String notifyMethod;
    
    /**
     * 状态：active/paused/completed
     */
    private String status;
    
    /**
     * 用药注意事项
     */
    private String precautions;
    
    /**
     * 最后提醒时间
     */
    private LocalDate lastNotifyDate;
    
    /**
     * 备注
     */
    private String remark;
}
