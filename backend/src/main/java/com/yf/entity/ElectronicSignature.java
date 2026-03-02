package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 电子签名记录实体
 * 用于GSP合规操作的电子签名
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("electronic_signature")
public class ElectronicSignature extends BaseEntity {
    
    /**
     * 业务类型：acceptance/maintenance/check/approval
     */
    private String businessType;
    
    /**
     * 业务ID（关联的业务记录ID）
     */
    private Long businessId;
    
    /**
     * 签名人ID
     */
    private Long signerId;
    
    /**
     * 签名人姓名
     */
    private String signerName;
    
    /**
     * 签名人岗位
     */
    private String signerPosition;
    
    /**
     * 签名时间
     */
    private LocalDateTime signTime;
    
    /**
     * 签名数据（Base64加密的签名图片或数字证书）
     */
    private String signatureData;
    
    /**
     * 验证码（用于验证签名真实性）
     */
    private String verifyCode;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 操作说明
     */
    private String operationDesc;
    
    /**
     * 状态：valid/revoked
     */
    private String status;
}
