package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.entity.*;
import com.yf.mapper.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会员健康画像服务
 * 基于购买历史和健康信息构建会员画像
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberHealthProfileService {

    private final MemberHealthProfileMapper profileMapper;
    private final MemberMapper memberMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderDetailMapper orderDetailMapper;
    private final DrugMapper drugMapper;
    private final ChronicDiseaseRecordMapper diseaseMapper;

    /**
     * 获取或创建会员健康画像
     */
    public MemberHealthProfile getOrCreateProfile(Long memberId) {
        LambdaQueryWrapper<MemberHealthProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberHealthProfile::getMemberId, memberId)
               .last("LIMIT 1");
        MemberHealthProfile profile = profileMapper.selectOne(wrapper);
        
        if (profile == null) {
            profile = new MemberHealthProfile();
            profile.setMemberId(memberId);
            profile.setLastUpdateDate(LocalDate.now());
            profileMapper.insert(profile);
        }
        return profile;
    }

    /**
     * 更新健康画像
     */
    @Transactional
    public MemberHealthProfile updateProfile(MemberHealthProfile profile) {
        // 计算BMI
        if (profile.getHeight() != null && profile.getWeight() != null 
                && profile.getHeight().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightM = profile.getHeight().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal bmi = profile.getWeight().divide(heightM.multiply(heightM), 1, RoundingMode.HALF_UP);
            profile.setBmi(bmi);
        }
        profile.setLastUpdateDate(LocalDate.now());
        profileMapper.updateById(profile);
        return profile;
    }

    /**
     * 分析会员购买行为，生成健康标签
     */
    public MemberHealthAnalysis analyzeHealthProfile(Long memberId) {
        MemberHealthAnalysis analysis = new MemberHealthAnalysis();
        analysis.setMemberId(memberId);

        // 获取会员基本信息
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            return analysis;
        }
        analysis.setMemberName(member.getName());
        
        // 计算年龄
        if (member.getBirthday() != null) {
            int age = Period.between(member.getBirthday(), LocalDate.now()).getYears();
            analysis.setAge(age);
            if (age >= 60) {
                analysis.getTags().add("老年人");
            }
        }

        // 获取健康画像
        MemberHealthProfile profile = getOrCreateProfile(memberId);
        if (profile.getAllergyHistory() != null) {
            analysis.getTags().add("有过敏史");
        }

        // 分析购买历史
        analyzePurchaseHistory(memberId, analysis);

        // 获取慢病记录
        LambdaQueryWrapper<ChronicDiseaseRecord> diseaseWrapper = new LambdaQueryWrapper<>();
        diseaseWrapper.eq(ChronicDiseaseRecord::getMemberId, memberId)
                     .eq(ChronicDiseaseRecord::getManagementStatus, "active");
        List<ChronicDiseaseRecord> diseases = diseaseMapper.selectList(diseaseWrapper);
        
        for (ChronicDiseaseRecord disease : diseases) {
            analysis.getChronicDiseases().add(disease.getDiseaseName());
            analysis.getTags().add(getTagByDiseaseType(disease.getDiseaseType()));
        }

        return analysis;
    }

    /**
     * 分析购买历史
     */
    private void analyzePurchaseHistory(Long memberId, MemberHealthAnalysis analysis) {
        // 获取最近6个月的订单
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        LambdaQueryWrapper<SaleOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(SaleOrder::getMemberId, memberId)
                   .eq(SaleOrder::getStatus, "completed")
                   .ge(SaleOrder::getCreatedAt, sixMonthsAgo);
        List<SaleOrder> orders = saleOrderMapper.selectList(orderWrapper);

        if (orders.isEmpty()) {
            return;
        }

        // 统计药品购买频次
        Map<Long, Integer> drugFrequency = new HashMap<>();
        Map<String, Integer> categoryFrequency = new HashMap<>();
        
        for (SaleOrder order : orders) {
            LambdaQueryWrapper<SaleOrderDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(SaleOrderDetail::getSaleOrderId, order.getId());
            List<SaleOrderDetail> details = orderDetailMapper.selectList(detailWrapper);
            
            for (SaleOrderDetail detail : details) {
                int qty = detail.getQuantity() != null ? detail.getQuantity().intValue() : 1;
                drugFrequency.merge(detail.getDrugId(), qty, Integer::sum);
                
                // 获取药品分类
                Drug drug = drugMapper.selectById(detail.getDrugId());
                if (drug != null && drug.getCategoryId() != null) {
                    categoryFrequency.merge(drug.getCategoryId().toString(), 1, Integer::sum);
                    
                    // 根据商品名称推断健康标签
                    inferHealthTags(drug.getGenericName(), analysis);
                }
            }
        }

        // 找出购买频次最高的药品
        analysis.setFrequentDrugs(
            drugFrequency.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Drug drug = drugMapper.selectById(e.getKey());
                    DrugPurchaseInfo info = new DrugPurchaseInfo();
                    info.setDrugId(e.getKey());
                    info.setDrugName(drug != null ? drug.getGenericName() : "未知");
                    info.setPurchaseCount(e.getValue());
                    return info;
                })
                .collect(Collectors.toList())
        );

        analysis.setPurchaseCount(orders.size());
    }

    /**
     * 根据商品名称推断健康标签
     */
    private void inferHealthTags(String drugName, MemberHealthAnalysis analysis) {
        if (drugName == null) return;
        
        // 降压药
        if (drugName.contains("氨氯地平") || drugName.contains("缬沙坦") || 
            drugName.contains("硝苯地平") || drugName.contains("厄贝沙坦")) {
            analysis.getTags().add("高血压");
        }
        // 降糖药
        if (drugName.contains("二甲双胍") || drugName.contains("格列") || 
            drugName.contains("胰岛素") || drugName.contains("阿卡波糖")) {
            analysis.getTags().add("糖尿病");
        }
        // 降脂药
        if (drugName.contains("阿托伐他汀") || drugName.contains("瑞舒伐他汀") || 
            drugName.contains("辛伐他汀")) {
            analysis.getTags().add("高血脂");
        }
        // 心血管药
        if (drugName.contains("阿司匹林") || drugName.contains("氯吡格雷") || 
            drugName.contains("硝酸甘油")) {
            analysis.getTags().add("心血管疾病");
        }
        // 呼吸系统
        if (drugName.contains("沙丁胺醇") || drugName.contains("布地奈德") || 
            drugName.contains("孟鲁司特")) {
            analysis.getTags().add("呼吸系统疾病");
        }
    }

    /**
     * 获取疾病类型对应的标签
     */
    private String getTagByDiseaseType(String diseaseType) {
        return switch (diseaseType) {
            case "diabetes" -> "糖尿病";
            case "hypertension" -> "高血压";
            case "hyperlipidemia" -> "高血脂";
            case "coronary_heart" -> "冠心病";
            case "asthma" -> "哮喘";
            default -> "慢病患者";
        };
    }

    /**
     * 获取用药建议
     */
    public List<String> getMedicationAdvice(Long memberId) {
        List<String> advice = new ArrayList<>();
        MemberHealthAnalysis analysis = analyzeHealthProfile(memberId);
        
        // 基于健康标签生成建议
        Set<String> tags = analysis.getTags();
        
        if (tags.contains("高血压")) {
            advice.add("建议定期监测血压，每日早晚各测量一次");
            advice.add("低盐饮食，每日盐摄入不超过6克");
            advice.add("降压药请按时服用，不要自行停药");
        }
        if (tags.contains("糖尿病")) {
            advice.add("建议定期监测血糖，空腹及餐后2小时");
            advice.add("控制碳水化合物摄入，注意饮食结构");
            advice.add("定期检查糖化血红蛋白，每3个月一次");
        }
        if (tags.contains("老年人")) {
            advice.add("注意用药安全，避免跌倒");
            advice.add("可考虑使用分药盒，避免漏服或重复服药");
            advice.add("定期进行肝肾功能检查");
        }
        if (tags.contains("有过敏史")) {
            advice.add("购药时请告知药师您的过敏史");
            advice.add("新药使用前请仔细阅读说明书");
        }
        
        if (advice.isEmpty()) {
            advice.add("保持良好的生活习惯，定期体检");
        }
        
        return advice;
    }

    @Data
    public static class MemberHealthAnalysis {
        private Long memberId;
        private String memberName;
        private Integer age;
        private Set<String> tags = new HashSet<>();
        private List<String> chronicDiseases = new ArrayList<>();
        private List<DrugPurchaseInfo> frequentDrugs = new ArrayList<>();
        private Integer purchaseCount = 0;
    }

    @Data
    public static class DrugPurchaseInfo {
        private Long drugId;
        private String drugName;
        private Integer purchaseCount;
    }
}
