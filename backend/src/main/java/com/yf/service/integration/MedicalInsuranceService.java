package com.yf.service.integration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 医保接口服务（预留）
 * 对接国家医保信息平台
 */
@Slf4j
@Service
public class MedicalInsuranceService {

    /**
     * 医保资格验证
     */
    public InsuranceVerifyResult verifyInsurance(String idCard, String insuranceNo) {
        log.info("医保资格验证 - 身份证：{}，医保号：{}", maskIdCard(idCard), insuranceNo);
        
        // TODO: 对接医保平台API
        InsuranceVerifyResult result = new InsuranceVerifyResult();
        result.setValid(true);
        result.setMessage("接口预留，待对接医保平台");
        result.setInsuranceType("城镇职工");
        result.setBalance(0.0);
        
        return result;
    }

    /**
     * 医保结算
     */
    public InsuranceSettlementResult settleInsurance(InsuranceSettlementRequest request) {
        log.info("医保结算 - 订单号：{}，金额：{}", request.getOrderNo(), request.getTotalAmount());
        
        // TODO: 对接医保平台API
        InsuranceSettlementResult result = new InsuranceSettlementResult();
        result.setSuccess(false);
        result.setMessage("接口预留，待对接医保平台");
        
        return result;
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) return "****";
        return idCard.substring(0, 4) + "****" + idCard.substring(idCard.length() - 4);
    }

    @Data
    public static class InsuranceVerifyResult {
        private Boolean valid;
        private String message;
        private String insuranceType;
        private Double balance;
    }

    @Data
    public static class InsuranceSettlementRequest {
        private String orderNo;
        private String idCard;
        private String insuranceNo;
        private Double totalAmount;
        private Double insuranceAmount;
    }

    @Data
    public static class InsuranceSettlementResult {
        private Boolean success;
        private String message;
        private String settlementNo;
        private Double paidAmount;
    }
}
