package com.yf.controller.mobile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.entity.Drug;
import com.yf.entity.SaleOrder;
import com.yf.entity.SaleOrderDetail;
import com.yf.mapper.DrugMapper;
import com.yf.mapper.SaleOrderDetailMapper;
import com.yf.mapper.SaleOrderMapper;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 移动端药品控制器
 * 为UniApp/微信小程序提供药品查询和订单相关接口
 */
@RestController
@RequestMapping("/mobile/drug")
@RequiredArgsConstructor
public class MobileDrugController {

    private final DrugMapper drugMapper;
    private final SaleOrderMapper orderMapper;
    private final SaleOrderDetailMapper orderDetailMapper;

    /**
     * 搜索药品
     */
    @GetMapping("/search")
    public ApiResponse<List<DrugSimpleInfo>> searchDrugs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Drug::getGenericName, keyword)
               .or().like(Drug::getTradeName, keyword)
               .or().like(Drug::getBarcode, keyword)
               .eq(Drug::getStatus, "启用")
               .last("LIMIT " + (page - 1) * size + "," + size);
        
        List<Drug> drugs = drugMapper.selectList(wrapper);
        List<DrugSimpleInfo> result = new ArrayList<>();
        
        for (Drug drug : drugs) {
            DrugSimpleInfo info = new DrugSimpleInfo();
            info.setDrugId(drug.getId());
            info.setGenericName(drug.getGenericName());
            info.setTradeName(drug.getTradeName());
            info.setSpecification(drug.getSpecification());
            info.setRetailPrice(drug.getRetailPrice());
            info.setUnit(drug.getUnit());
            info.setOtcType(drug.getOtcType());
            result.add(info);
        }
        
        return ApiResponse.success(result);
    }

    /**
     * 获取药品详情
     */
    @GetMapping("/{drugId}")
    public ApiResponse<DrugDetailInfo> getDrugDetail(@PathVariable Long drugId) {
        Drug drug = drugMapper.selectById(drugId);
        if (drug == null) {
            return ApiResponse.error("药品不存在");
        }
        
        DrugDetailInfo info = new DrugDetailInfo();
        info.setDrugId(drug.getId());
        info.setGenericName(drug.getGenericName());
        info.setTradeName(drug.getTradeName());
        info.setSpecification(drug.getSpecification());
        info.setManufacturer(drug.getManufacturer());
        info.setRetailPrice(drug.getRetailPrice());
        info.setUnit(drug.getUnit());
        info.setOtcType(drug.getOtcType());
        info.setDosageForm(drug.getDosageForm());
        info.setApprovalNo(drug.getApprovalNo());
        info.setStorageCondition(drug.getStorageCondition());
        
        return ApiResponse.success(info);
    }

    /**
     * 获取我的购药记录
     */
    @GetMapping("/orders")
    public ApiResponse<List<OrderSimpleInfo>> getMyOrders(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleOrder::getMemberId, memberId)
               .eq(SaleOrder::getStatus, "completed")
               .orderByDesc(SaleOrder::getCreatedAt)
               .last("LIMIT " + (page - 1) * size + "," + size);
        
        List<SaleOrder> orders = orderMapper.selectList(wrapper);
        List<OrderSimpleInfo> result = new ArrayList<>();
        
        for (SaleOrder order : orders) {
            OrderSimpleInfo info = new OrderSimpleInfo();
            info.setOrderId(order.getId());
            info.setOrderNo(order.getOrderNo());
            info.setOrderTime(order.getCreatedAt());
            info.setTotalAmount(order.getTotalAmount());
            info.setPayAmount(order.getPayAmount());
            
            // 获取订单药品
            LambdaQueryWrapper<SaleOrderDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(SaleOrderDetail::getSaleOrderId, order.getId());
            List<SaleOrderDetail> details = orderDetailMapper.selectList(detailWrapper);
            
            List<String> drugNames = new ArrayList<>();
            for (SaleOrderDetail detail : details) {
                drugNames.add(detail.getDrugName());
            }
            info.setDrugNames(drugNames);
            info.setDrugCount(details.size());
            
            result.add(info);
        }
        
        return ApiResponse.success(result);
    }

    @Data
    public static class DrugSimpleInfo {
        private Long drugId;
        private String genericName;
        private String tradeName;
        private String specification;
        private java.math.BigDecimal retailPrice;
        private String unit;
        private String otcType;
    }

    @Data
    public static class DrugDetailInfo extends DrugSimpleInfo {
        private String manufacturer;
        private String dosageForm;
        private String approvalNo;
        private String storageCondition;
    }

    @Data
    public static class OrderSimpleInfo {
        private Long orderId;
        private String orderNo;
        private java.time.LocalDateTime orderTime;
        private java.math.BigDecimal totalAmount;
        private java.math.BigDecimal payAmount;
        private List<String> drugNames;
        private Integer drugCount;
    }
}
