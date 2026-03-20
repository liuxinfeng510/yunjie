package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.dto.ReconciliationSubmitDTO;
import com.yf.entity.*;
import com.yf.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对账单服务
 */
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final ReconciliationMapper reconciliationMapper;
    private final ReconciliationDetailMapper reconciliationDetailMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final SysUserMapper sysUserMapper;
    private final StoreMapper storeMapper;

    /**
     * 预览对账数据（按支付方式汇总当日销售）
     * @param cashierId 收银员ID，null表示查询全部收银员
     */
    public Map<String, Object> preview(Long storeId, String dateStr, Long cashierId) {
        LocalDate date = LocalDate.parse(dateStr);

        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.plusDays(1).atStartOfDay();

        // 查询当日已完成订单（租户隔离由MyBatis-Plus自动处理）
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(SaleOrder::getStoreId, storeId);
        }
        wrapper.eq(SaleOrder::getStatus, "已完成")
               .ge(SaleOrder::getCreatedAt, startTime)
               .lt(SaleOrder::getCreatedAt, endTime);
        if (cashierId != null) {
            // 包含该收银员的订单 + 未分配的历史订单
            wrapper.and(w -> w.eq(SaleOrder::getCashierId, cashierId)
                              .or().isNull(SaleOrder::getCashierId));
        }

        List<SaleOrder> orders = saleOrderMapper.selectList(wrapper);

        // 按支付方式分组统计
        Map<String, List<SaleOrder>> grouped = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getPayMethod() != null ? o.getPayMethod() : "未知"));

        List<Map<String, Object>> details = new ArrayList<>();
        BigDecimal systemTotal = BigDecimal.ZERO;
        int totalOrderCount = 0;

        for (Map.Entry<String, List<SaleOrder>> entry : grouped.entrySet()) {
            String payMethod = entry.getKey();
            List<SaleOrder> groupOrders = entry.getValue();

            int count = groupOrders.size();
            BigDecimal amount = groupOrders.stream()
                    .map(SaleOrder::getPayAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("payMethod", payMethod);
            detail.put("orderCount", count);
            detail.put("systemAmount", amount);
            details.add(detail);

            systemTotal = systemTotal.add(amount);
            totalOrderCount += count;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reconcileDate", dateStr);
        result.put("storeId", storeId);
        result.put("cashierId", cashierId);
        result.put("orderCount", totalOrderCount);
        result.put("systemTotal", systemTotal);
        result.put("details", details);
        return result;
    }

    /**
     * 提交对账单
     */
    @Transactional(rollbackFor = Exception.class)
    public Reconciliation submit(ReconciliationSubmitDTO dto) {
        Long operatorId = getCurrentUserId();
        Long cashierId = dto.getCashierId(); // 对账的收银员（null表示全部）
        LocalDate reconcileDate = LocalDate.parse(dto.getReconcileDate());

        // 先获取系统数据（预览数据）
        Map<String, Object> previewData = preview(dto.getStoreId(), dto.getReconcileDate(), cashierId);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> systemDetails = (List<Map<String, Object>>) previewData.get("details");
        Map<String, Map<String, Object>> systemMap = systemDetails.stream()
                .collect(Collectors.toMap(
                        d -> (String) d.get("payMethod"),
                        d -> d
                ));

        // 计算总额
        BigDecimal systemTotal = (BigDecimal) previewData.get("systemTotal");
        BigDecimal actualTotal = BigDecimal.ZERO;
        int orderCount = (int) previewData.get("orderCount");

        // 计算实际总额
        if (dto.getDetails() != null) {
            for (ReconciliationSubmitDTO.DetailItem item : dto.getDetails()) {
                if (item.getActualAmount() != null) {
                    actualTotal = actualTotal.add(item.getActualAmount());
                }
            }
        }

        BigDecimal difference = actualTotal.subtract(systemTotal);

        // 确定状态
        String status;
        int cmp = difference.compareTo(BigDecimal.ZERO);
        if (cmp == 0) {
            status = "balanced";
        } else if (cmp > 0) {
            status = "surplus";
        } else {
            status = "shortage";
        }

        // 生成对账单号
        String no = "REC" + reconcileDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%04d", System.currentTimeMillis() % 10000);

        // 保存主表
        Reconciliation rec = new Reconciliation();
        rec.setReconciliationNo(no);
        rec.setStoreId(dto.getStoreId());
        rec.setCashierId(cashierId);
        rec.setReconcileDate(reconcileDate);
        rec.setOrderCount(orderCount);
        rec.setSystemTotal(systemTotal);
        rec.setActualTotal(actualTotal);
        rec.setDifference(difference);
        rec.setStatus(status);
        rec.setRemark(dto.getRemark());
        reconciliationMapper.insert(rec);

        // 保存明细
        if (dto.getDetails() != null) {
            for (ReconciliationSubmitDTO.DetailItem item : dto.getDetails()) {
                Map<String, Object> sysDtl = systemMap.getOrDefault(item.getPayMethod(), Map.of());
                BigDecimal sysAmt = sysDtl.get("systemAmount") != null
                        ? (BigDecimal) sysDtl.get("systemAmount") : BigDecimal.ZERO;
                int dtlOrderCount = sysDtl.get("orderCount") != null
                        ? (int) sysDtl.get("orderCount") : 0;
                BigDecimal actualAmt = item.getActualAmount() != null ? item.getActualAmount() : BigDecimal.ZERO;

                ReconciliationDetail detail = new ReconciliationDetail();
                detail.setReconciliationId(rec.getId());
                detail.setPayMethod(item.getPayMethod());
                detail.setOrderCount(dtlOrderCount);
                detail.setSystemAmount(sysAmt);
                detail.setActualAmount(actualAmt);
                detail.setDifference(actualAmt.subtract(sysAmt));
                detail.setRemark(item.getRemark());
                reconciliationDetailMapper.insert(detail);
            }
        }

        return rec;
    }

    /**
     * 分页查询对账记录
     */
    public Page<Reconciliation> pageList(Long storeId, Long cashierId, String status,
                                          String startDate, String endDate,
                                          int pageNum, int pageSize) {
        Page<Reconciliation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Reconciliation> wrapper = new LambdaQueryWrapper<>();

        if (storeId != null) {
            wrapper.eq(Reconciliation::getStoreId, storeId);
        }
        if (cashierId != null) {
            wrapper.eq(Reconciliation::getCashierId, cashierId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Reconciliation::getStatus, status);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Reconciliation::getReconcileDate, LocalDate.parse(startDate));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(Reconciliation::getReconcileDate, LocalDate.parse(endDate));
        }

        wrapper.orderByDesc(Reconciliation::getReconcileDate);
        Page<Reconciliation> result = reconciliationMapper.selectPage(page, wrapper);

        // 填充关联信息
        fillReconciliationDetails(result.getRecords());

        return result;
    }

    /**
     * 获取对账单详情
     */
    public Map<String, Object> getDetail(Long id) {
        Reconciliation rec = reconciliationMapper.selectById(id);
        if (rec == null) return null;

        fillReconciliationDetails(List.of(rec));

        LambdaQueryWrapper<ReconciliationDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReconciliationDetail::getReconciliationId, id);
        List<ReconciliationDetail> details = reconciliationDetailMapper.selectList(wrapper);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reconciliation", rec);
        result.put("details", details);
        return result;
    }

    /**
     * 填充对账单关联信息（收银员名、门店名）
     */
    private void fillReconciliationDetails(List<Reconciliation> records) {
        if (records == null || records.isEmpty()) return;

        Set<Long> userIds = records.stream()
                .map(Reconciliation::getCashierId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> storeIds = records.stream()
                .map(Reconciliation::getStoreId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            users.forEach(u -> userNameMap.put(u.getId(),
                    u.getRealName() != null ? u.getRealName() : u.getUsername()));
        }

        Map<Long, String> storeNameMap = new HashMap<>();
        if (!storeIds.isEmpty()) {
            List<Store> stores = storeMapper.selectBatchIds(storeIds);
            stores.forEach(s -> storeNameMap.put(s.getId(), s.getName()));
        }

        for (Reconciliation rec : records) {
            if (rec.getCashierId() != null) {
                rec.setCashierName(userNameMap.get(rec.getCashierId()));
            }
            if (rec.getStoreId() != null) {
                rec.setStoreName(storeNameMap.get(rec.getStoreId()));
            }
        }
    }

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long userId) {
                return userId;
            }
        } catch (Exception ignored) {}
        return null;
    }
}
