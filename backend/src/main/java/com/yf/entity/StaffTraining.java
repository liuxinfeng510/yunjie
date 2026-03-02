package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工培训记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("staff_training")
public class StaffTraining extends BaseEntity {
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 培训主题
     */
    private String title;
    
    /**
     * 培训类型：gsp/drug_knowledge/service/safety/other
     */
    private String trainingType;
    
    /**
     * 培训内容
     */
    private String content;
    
    /**
     * 培训日期
     */
    private LocalDate trainingDate;
    
    /**
     * 培训时长（小时）
     */
    private Integer duration;
    
    /**
     * 讲师
     */
    private String trainer;
    
    /**
     * 培训地点
     */
    private String location;
    
    /**
     * 参训人员ID列表（JSON）
     */
    private String attendeeIds;
    
    /**
     * 参训人员姓名列表（JSON）
     */
    private String attendeeNames;
    
    /**
     * 参训人数
     */
    private Integer attendeeCount;
    
    /**
     * 培训资料URL
     */
    private String materials;
    
    /**
     * 考核方式：exam/practice/none
     */
    private String assessmentType;
    
    /**
     * 考核结果JSON
     */
    private String assessmentResults;
    
    /**
     * 签到表图片
     */
    private String signInImage;
    
    /**
     * 培训照片
     */
    private String images;
    
    /**
     * 状态：planned/completed/cancelled
     */
    private String status;
    
    /**
     * 备注
     */
    private String remark;
}
