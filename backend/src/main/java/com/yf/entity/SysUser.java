package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    /** 所属门店ID */
    private Long storeId;
    /** 角色: admin / manager / pharmacist / clerk */
    private String role;
    /** 状态: active / disabled */
    private String status;
}
