package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 微信用户绑定实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wechat_user")
public class WechatUser extends BaseEntity {
    
    /**
     * 微信openId
     */
    private String openId;
    
    /**
     * 微信unionId（可选）
     */
    private String unionId;
    
    /**
     * 会员ID（绑定后填写）
     */
    private Long memberId;
    
    /**
     * 员工ID（员工绑定时填写）
     */
    private Long staffId;
    
    /**
     * 用户类型：member-会员, staff-员工
     */
    private String userType;
    
    /**
     * 微信昵称
     */
    private String nickname;
    
    /**
     * 微信头像URL
     */
    private String avatarUrl;
    
    /**
     * 手机号（微信获取）
     */
    private String phone;
    
    /**
     * 绑定状态：bound-已绑定, unbound-未绑定
     */
    private String bindStatus;
    
    /**
     * 绑定时间
     */
    private LocalDateTime bindTime;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 小程序session_key（加密存储）
     */
    private String sessionKey;
}
