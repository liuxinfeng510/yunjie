package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yf.entity.Promotion;
import com.yf.mapper.PromotionMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 促销引擎服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionMapper promotionMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建促销活动
     */
    @Transactional
    public Promotion create(Promotion promotion) {
        promotion.setStatus("draft");
        if (promotion.getPriority() == null) {
            promotion.setPriority(0);
        }
        promotionMapper.insert(promotion);
        log.info("创建促销活动: name={}, type={}", promotion.getName(), promotion.getType());
        return promotion;
    }

    /**
     * 更新促销活动
     */
    @Transactional
    public Promotion update(Promotion promotion) {
        promotionMapper.updateById(promotion);
        return promotion;
    }

    /**
     * 启用促销活动
     */
    @Transactional
    public void activate(Long id) {
        Promotion promotion = promotionMapper.selectById(id);
        if (promotion == null) {
            throw new RuntimeException("促销活动不存在");
        }
        promotion.setStatus("active");
        promotionMapper.updateById(promotion);
        log.info("启用促销活动: id={}", id);
    }

    /**
     * 暂停促销活动
     */
    @Transactional
    public void pause(Long id) {
        Promotion promotion = promotionMapper.selectById(id);
        if (promotion != null) {
            promotion.setStatus("paused");
            promotionMapper.updateById(promotion);
        }
    }

    /**
     * 结束促销活动
     */
    @Transactional
    public void end(Long id) {
        Promotion promotion = promotionMapper.selectById(id);
        if (promotion != null) {
            promotion.setStatus("ended");
            promotionMapper.updateById(promotion);
        }
    }

    /**
     * 根据ID查询
     */
    public Promotion getById(Long id) {
        return promotionMapper.selectById(id);
    }

    /**
     * 分页查询
     */
    public Page<Promotion> page(Long storeId, String status, String type, int pageNum, int pageSize) {
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.and(w -> w.isNull(Promotion::getStoreId).or().eq(Promotion::getStoreId, storeId));
        }
        if (status != null && !status.isEmpty()) wrapper.eq(Promotion::getStatus, status);
        if (type != null && !type.isEmpty()) wrapper.eq(Promotion::getType, type);
        wrapper.orderByDesc(Promotion::getPriority).orderByDesc(Promotion::getCreatedAt);
        return promotionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 获取当前有效的促销活动
     */
    public List<Promotion> getActivePromotions(Long storeId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Promotion::getStatus, "active");
        wrapper.le(Promotion::getStartTime, now);
        wrapper.ge(Promotion::getEndTime, now);
        if (storeId != null) {
            wrapper.and(w -> w.isNull(Promotion::getStoreId).or().eq(Promotion::getStoreId, storeId));
        }
        wrapper.orderByDesc(Promotion::getPriority);
        return promotionMapper.selectList(wrapper);
    }

    /**
     * 计算药品适用的促销优惠
     */
    public PromotionResult calculatePromotion(Long storeId, Long drugId, Long categoryId, 
                                               BigDecimal originalPrice, Integer quantity, Long memberLevelId) {
        List<Promotion> promotions = getActivePromotions(storeId);
        PromotionResult result = new PromotionResult();
        result.setOriginalAmount(originalPrice.multiply(BigDecimal.valueOf(quantity)));
        result.setFinalAmount(result.getOriginalAmount());
        result.setAppliedPromotions(new ArrayList<>());

        for (Promotion promotion : promotions) {
            if (!isApplicable(promotion, drugId, categoryId, memberLevelId)) {
                continue;
            }

            BigDecimal discount = calculateDiscount(promotion, result.getFinalAmount(), quantity);
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                result.setFinalAmount(result.getFinalAmount().subtract(discount));
                result.getAppliedPromotions().add(new AppliedPromotion(
                        promotion.getId(), promotion.getName(), promotion.getType(), discount));
                
                // 默认只应用一个促销（可根据业务调整）
                break;
            }
        }

        result.setDiscountAmount(result.getOriginalAmount().subtract(result.getFinalAmount()));
        return result;
    }

    private boolean isApplicable(Promotion promotion, Long drugId, Long categoryId, Long memberLevelId) {
        // 检查适用范围
        if ("product".equals(promotion.getScope()) && promotion.getTargetIds() != null) {
            try {
                List<Long> targetIds = objectMapper.readValue(promotion.getTargetIds(), 
                        new TypeReference<List<Long>>() {});
                if (!targetIds.contains(drugId)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } else if ("category".equals(promotion.getScope()) && promotion.getTargetIds() != null) {
            try {
                List<Long> targetIds = objectMapper.readValue(promotion.getTargetIds(), 
                        new TypeReference<List<Long>>() {});
                if (categoryId == null || !targetIds.contains(categoryId)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        // 检查会员等级限制
        if (promotion.getMemberLevelIds() != null && !promotion.getMemberLevelIds().isEmpty()) {
            try {
                List<Long> levelIds = objectMapper.readValue(promotion.getMemberLevelIds(), 
                        new TypeReference<List<Long>>() {});
                if (memberLevelId == null || !levelIds.contains(memberLevelId)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal amount, Integer quantity) {
        return switch (promotion.getType()) {
            case "discount" -> {
                if (promotion.getDiscountRate() != null) {
                    yield amount.multiply(BigDecimal.ONE.subtract(promotion.getDiscountRate()));
                }
                yield BigDecimal.ZERO;
            }
            case "reduction" -> {
                if (promotion.getThresholdAmount() != null && promotion.getReductionAmount() != null
                        && amount.compareTo(promotion.getThresholdAmount()) >= 0) {
                    yield promotion.getReductionAmount();
                }
                yield BigDecimal.ZERO;
            }
            default -> BigDecimal.ZERO;
        };
    }

    @Data
    public static class PromotionResult {
        private BigDecimal originalAmount;
        private BigDecimal discountAmount;
        private BigDecimal finalAmount;
        private List<AppliedPromotion> appliedPromotions;
    }

    @Data
    public static class AppliedPromotion {
        private Long promotionId;
        private String promotionName;
        private String promotionType;
        private BigDecimal discountAmount;

        public AppliedPromotion(Long id, String name, String type, BigDecimal discount) {
            this.promotionId = id;
            this.promotionName = name;
            this.promotionType = type;
            this.discountAmount = discount;
        }
    }
}
