package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.entity.ElectronicSignature;
import com.yf.entity.SysUser;
import com.yf.mapper.ElectronicSignatureMapper;
import com.yf.mapper.SysUserMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * 电子签名服务
 * 为GSP合规操作提供电子签名功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElectronicSignatureService {

    private final ElectronicSignatureMapper signatureMapper;
    private final SysUserMapper userMapper;

    /**
     * 创建电子签名
     */
    @Transactional
    public ElectronicSignature createSignature(SignatureRequest request) {
        // 验证用户密码
        SysUser user = userMapper.selectById(request.getSignerId());
        if (user == null) {
            throw new RuntimeException("签名用户不存在");
        }
        
        // 实际应用中需要验证密码
        // if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        //     throw new RuntimeException("密码验证失败");
        // }

        ElectronicSignature signature = new ElectronicSignature();
        signature.setBusinessType(request.getBusinessType());
        signature.setBusinessId(request.getBusinessId());
        signature.setSignerId(request.getSignerId());
        signature.setSignerName(user.getRealName());
        signature.setSignerPosition(user.getRole());
        signature.setSignTime(LocalDateTime.now());
        signature.setSignatureData(request.getSignatureData());
        signature.setVerifyCode(generateVerifyCode(signature));
        signature.setIpAddress(request.getIpAddress());
        signature.setOperationDesc(request.getOperationDesc());
        signature.setStatus("valid");

        signatureMapper.insert(signature);
        log.info("创建电子签名，业务类型：{}，业务ID：{}，签名人：{}", 
                request.getBusinessType(), request.getBusinessId(), user.getRealName());
        
        return signature;
    }

    /**
     * 验证签名
     */
    public boolean verifySignature(Long signatureId) {
        ElectronicSignature signature = signatureMapper.selectById(signatureId);
        if (signature == null || !"valid".equals(signature.getStatus())) {
            return false;
        }
        
        String expectedCode = generateVerifyCode(signature);
        return expectedCode.equals(signature.getVerifyCode());
    }

    /**
     * 撤销签名
     */
    @Transactional
    public void revokeSignature(Long signatureId, String reason) {
        ElectronicSignature signature = signatureMapper.selectById(signatureId);
        if (signature != null) {
            signature.setStatus("revoked");
            signature.setOperationDesc(signature.getOperationDesc() + " [撤销原因：" + reason + "]");
            signatureMapper.updateById(signature);
            log.info("撤销电子签名：{}", signatureId);
        }
    }

    /**
     * 获取业务相关的所有签名
     */
    public List<ElectronicSignature> getBusinessSignatures(String businessType, Long businessId) {
        LambdaQueryWrapper<ElectronicSignature> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ElectronicSignature::getBusinessType, businessType)
               .eq(ElectronicSignature::getBusinessId, businessId)
               .eq(ElectronicSignature::getStatus, "valid")
               .orderByAsc(ElectronicSignature::getSignTime);
        return signatureMapper.selectList(wrapper);
    }

    /**
     * 检查业务是否已签名
     */
    public boolean hasSignature(String businessType, Long businessId, Long signerId) {
        LambdaQueryWrapper<ElectronicSignature> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ElectronicSignature::getBusinessType, businessType)
               .eq(ElectronicSignature::getBusinessId, businessId)
               .eq(ElectronicSignature::getSignerId, signerId)
               .eq(ElectronicSignature::getStatus, "valid");
        return signatureMapper.selectCount(wrapper) > 0;
    }

    /**
     * 生成验证码
     */
    private String generateVerifyCode(ElectronicSignature signature) {
        try {
            String data = signature.getBusinessType() + 
                         signature.getBusinessId() + 
                         signature.getSignerId() + 
                         signature.getSignTime().toString();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes());
            return Base64.getEncoder().encodeToString(hash).substring(0, 16);
        } catch (Exception e) {
            return UUID.randomUUID().toString().substring(0, 16);
        }
    }

    @Data
    public static class SignatureRequest {
        private String businessType;
        private Long businessId;
        private Long signerId;
        private String password;
        private String signatureData;
        private String ipAddress;
        private String operationDesc;
    }
}
