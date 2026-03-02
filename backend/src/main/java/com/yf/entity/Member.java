package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 会员实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member")
public class Member extends BaseEntity {
    
    /**
     * 会员编号
     */
    private String memberNo;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 性别
     */
    private String gender;
    
    /**
     * 生日
     */
    private LocalDate birthday;
    
    /**
     * 身份证号
     */
    private String idCard;
    
    /**
     * 微信OpenID
     */
    private String wechatOpenid;
    
    /**
     * 会员等级ID
     */
    private Long levelId;
    
    /**
     * 积分余额
     */
    private Integer points;
    
    /**
     * 累计消费金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 过敏信息
     */
    private String allergyInfo;
    
    /**
     * 慢性病信息
     */
    private String chronicDisease;
    
    /**
     * 状态（正常、冻结）
     */
    private String status;
}
