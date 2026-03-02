package com.yf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.entity.*;
import com.yf.mapper.*;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SaleOrderMapper saleOrderMapper;
    private final MemberMapper memberMapper;
    private final InventoryMapper inventoryMapper;
    private final DrugBatchMapper drugBatchMapper;
    private final DrugMapper drugMapper;
    private final StockInMapper stockInMapper;
    private final DrugAcceptanceMapper drugAcceptanceMapper;
    private final DrugMaintenanceMapper drugMaintenanceMapper;
    private final SaleOrderDetailMapper saleOrderDetailMapper;

    /**
     * 首页概览统计
     */
    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview(@RequestParam(required = false) Long storeId) {
        Map<String, Object> data = new HashMap<>();

        // 今日销售统计
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        LambdaQueryWrapper<SaleOrder> salesWrapper = new LambdaQueryWrapper<>();
        salesWrapper.ge(SaleOrder::getCreatedAt, todayStart)
                .lt(SaleOrder::getCreatedAt, todayEnd)
                .eq(SaleOrder::getStatus, "已完成");
        if (storeId != null) {
            salesWrapper.eq(SaleOrder::getStoreId, storeId);
        }

        List<SaleOrder> todayOrders = saleOrderMapper.selectList(salesWrapper);
        BigDecimal todaySales = todayOrders.stream()
                .map(SaleOrder::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        data.put("todaySales", todaySales);
        data.put("todayOrders", todayOrders.size());

        // 会员总数
        Long memberCount = memberMapper.selectCount(new LambdaQueryWrapper<>());
        data.put("totalMembers", memberCount);

        // 库存预警数量（库存量 <= 安全库存）
        LambdaQueryWrapper<Inventory> warnWrapper = new LambdaQueryWrapper<>();
        warnWrapper.apply("quantity <= safe_stock");
        if (storeId != null) {
            warnWrapper.eq(Inventory::getStoreId, storeId);
        }
        Long stockWarnings = inventoryMapper.selectCount(warnWrapper);
        data.put("stockWarnings", stockWarnings);

        return ApiResponse.success(data);
    }

    /**
     * 近7天销售趋势
     */
    @GetMapping("/sales-trend")
    public ApiResponse<List<Map<String, Object>>> salesTrend(@RequestParam(required = false) Long storeId) {
        List<Map<String, Object>> trend = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);

            LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(SaleOrder::getCreatedAt, start)
                    .lt(SaleOrder::getCreatedAt, end)
                    .eq(SaleOrder::getStatus, "已完成");
            if (storeId != null) {
                wrapper.eq(SaleOrder::getStoreId, storeId);
            }

            List<SaleOrder> orders = saleOrderMapper.selectList(wrapper);
            BigDecimal amount = orders.stream()
                    .map(SaleOrder::getPayAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("amount", amount);
            item.put("orderCount", orders.size());
            trend.add(item);
        }

        return ApiResponse.success(trend);
    }

    /**
     * 待办事项统计
     */
    @GetMapping("/todos")
    public ApiResponse<Map<String, Object>> todos(@RequestParam(required = false) Long storeId) {
        Map<String, Object> data = new HashMap<>();

        // 近效期药品（90天内过期）
        LocalDate expireLimit = LocalDate.now().plusDays(90);
        LambdaQueryWrapper<DrugBatch> batchWrapper = new LambdaQueryWrapper<>();
        batchWrapper.le(DrugBatch::getExpireDate, expireLimit)
                .gt(DrugBatch::getExpireDate, LocalDate.now());
        data.put("expiringCount", drugBatchMapper.selectCount(batchWrapper));

        // 低库存预警
        LambdaQueryWrapper<Inventory> lowWrapper = new LambdaQueryWrapper<>();
        lowWrapper.apply("quantity <= safe_stock");
        if (storeId != null) {
            lowWrapper.eq(Inventory::getStoreId, storeId);
        }
        data.put("lowStockCount", inventoryMapper.selectCount(lowWrapper));

        // 待审核入库单
        LambdaQueryWrapper<StockIn> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(StockIn::getStatus, "待审核");
        if (storeId != null) {
            pendingWrapper.eq(StockIn::getStoreId, storeId);
        }
        data.put("pendingStockIn", stockInMapper.selectCount(pendingWrapper));

        // 待养护任务
        LambdaQueryWrapper<DrugMaintenance> maintWrapper = new LambdaQueryWrapper<>();
        maintWrapper.eq(DrugMaintenance::getOverallResult, "待养护");
        data.put("maintenanceCount", drugMaintenanceMapper.selectCount(maintWrapper));

        return ApiResponse.success(data);
    }

    /**
     * 近效期药品TOP列表
     */
    @GetMapping("/expiring-drugs")
    public ApiResponse<List<Map<String, Object>>> expiringDrugs(
            @RequestParam(defaultValue = "5") int limit) {

        LocalDate expireLimit = LocalDate.now().plusDays(180);
        LambdaQueryWrapper<DrugBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(DrugBatch::getExpireDate, expireLimit)
                .gt(DrugBatch::getExpireDate, LocalDate.now())
                .orderByAsc(DrugBatch::getExpireDate)
                .last("LIMIT " + limit);

        List<DrugBatch> batches = drugBatchMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (DrugBatch batch : batches) {
            Drug drug = drugMapper.selectById(batch.getDrugId());
            Map<String, Object> item = new HashMap<>();
            item.put("name", drug != null ? drug.getGenericName() : "未知");
            item.put("batchNo", batch.getBatchNo());
            item.put("expireDate", batch.getExpireDate().toString());
            item.put("daysLeft", java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpireDate()));
            result.add(item);
        }

        return ApiResponse.success(result);
    }

    /**
     * 畅销药品TOP列表（近30天）
     */
    @GetMapping("/top-drugs")
    public ApiResponse<List<Map<String, Object>>> topDrugs(
            @RequestParam(defaultValue = "5") int limit) {

        LocalDateTime start = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        // 查询近30天已完成的订单
        LambdaQueryWrapper<SaleOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.ge(SaleOrder::getCreatedAt, start)
                .lt(SaleOrder::getCreatedAt, end)
                .eq(SaleOrder::getStatus, "已完成");
        List<SaleOrder> orders = saleOrderMapper.selectList(orderWrapper);

        if (orders.isEmpty()) {
            return ApiResponse.success(new ArrayList<>());
        }

        // 查询所有明细
        List<Long> orderIds = new ArrayList<>();
        for (SaleOrder order : orders) {
            orderIds.add(order.getId());
        }

        LambdaQueryWrapper<SaleOrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.in(SaleOrderDetail::getSaleOrderId, orderIds);
        List<SaleOrderDetail> details = saleOrderDetailMapper.selectList(detailWrapper);

        // 按药品汇总
        Map<Long, BigDecimal> quantityMap = new HashMap<>();
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        for (SaleOrderDetail detail : details) {
            quantityMap.merge(detail.getDrugId(), detail.getQuantity(), BigDecimal::add);
            amountMap.merge(detail.getDrugId(), detail.getAmount(), BigDecimal::add);
        }

        // 按销量排序取TOP
        List<Map.Entry<Long, BigDecimal>> sorted = new ArrayList<>(quantityMap.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Map<String, Object>> result = new ArrayList<>();
        int count = 0;
        for (Map.Entry<Long, BigDecimal> entry : sorted) {
            if (count >= limit) break;
            Drug drug = drugMapper.selectById(entry.getKey());
            Map<String, Object> item = new HashMap<>();
            item.put("name", drug != null ? drug.getGenericName() : "未知");
            item.put("quantity", entry.getValue());
            item.put("amount", amountMap.getOrDefault(entry.getKey(), BigDecimal.ZERO));
            result.add(item);
            count++;
        }

        return ApiResponse.success(result);
    }
}
