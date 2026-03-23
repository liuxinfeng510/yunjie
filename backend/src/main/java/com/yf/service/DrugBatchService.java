package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Drug;
import com.yf.entity.DrugBatch;
import com.yf.entity.Inventory;
import com.yf.entity.Supplier;
import com.yf.mapper.DrugBatchMapper;
import com.yf.mapper.DrugMapper;
import com.yf.mapper.InventoryMapper;
import com.yf.mapper.SupplierMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 药品批次服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugBatchService {

    private final DrugBatchMapper batchMapper;
    private final DrugMapper drugMapper;
    private final SupplierMapper supplierMapper;
    private final InventoryMapper inventoryMapper;

    /**
     * 带药品和供应商详情的分页查询
     */
    public Page<Map<String, Object>> pageWithDetail(Long drugId, Long supplierId, String status,
                                                      String drugName, String batchNo,
                                                      int current, int size) {
        Page<DrugBatch> page = new Page<>(current, size);
        LambdaQueryWrapper<DrugBatch> wrapper = new LambdaQueryWrapper<>();

        if (drugId != null) {
            wrapper.eq(DrugBatch::getDrugId, drugId);
        }
        if (supplierId != null) {
            wrapper.eq(DrugBatch::getSupplierId, supplierId);
        }
        if (StringUtils.hasText(batchNo)) {
            wrapper.like(DrugBatch::getBatchNo, batchNo);
        }

        LocalDate today = LocalDate.now();

        // 前端状态筛选：normal/near_expiry/expired/locked
        if (StringUtils.hasText(status)) {
            if ("normal".equals(status)) {
                wrapper.eq(DrugBatch::getStatus, "active");
                wrapper.gt(DrugBatch::getExpireDate, today.plusDays(90));
            } else if ("near_expiry".equals(status)) {
                wrapper.eq(DrugBatch::getStatus, "active");
                wrapper.le(DrugBatch::getExpireDate, today.plusDays(90));
                wrapper.gt(DrugBatch::getExpireDate, today);
            } else if ("expired".equals(status)) {
                wrapper.eq(DrugBatch::getStatus, "active");
                wrapper.le(DrugBatch::getExpireDate, today);
            } else if ("locked".equals(status)) {
                wrapper.eq(DrugBatch::getStatus, "locked");
            } else {
                wrapper.eq(DrugBatch::getStatus, status);
            }
        }

        // 按药品名称搜索
        if (StringUtils.hasText(drugName)) {
            LambdaQueryWrapper<Drug> drugWrapper = new LambdaQueryWrapper<>();
            drugWrapper.and(w -> w.like(Drug::getGenericName, drugName)
                    .or().like(Drug::getTradeName, drugName));
            drugWrapper.select(Drug::getId);
            List<Drug> drugs = drugMapper.selectList(drugWrapper);
            if (drugs.isEmpty()) {
                Page<Map<String, Object>> emptyPage = new Page<>(current, size);
                emptyPage.setTotal(0);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
            List<Long> drugIds = drugs.stream().map(Drug::getId).collect(Collectors.toList());
            wrapper.in(DrugBatch::getDrugId, drugIds);
        }

        // 近效期/已过期按有效期升序排列（最紧急的在前），其他按创建时间降序
        if ("near_expiry".equals(status) || "expired".equals(status)) {
            wrapper.orderByAsc(DrugBatch::getExpireDate);
        } else {
            wrapper.orderByDesc(DrugBatch::getCreatedAt);
        }
        Page<DrugBatch> batchPage = batchMapper.selectPage(page, wrapper);

        // 批量查询关联的药品和供应商
        Set<Long> drugIds = new HashSet<>();
        Set<Long> supplierIds = new HashSet<>();
        Set<Long> batchIds = new HashSet<>();
        for (DrugBatch b : batchPage.getRecords()) {
            if (b.getDrugId() != null) drugIds.add(b.getDrugId());
            if (b.getSupplierId() != null) supplierIds.add(b.getSupplierId());
            batchIds.add(b.getId());
        }

        Map<Long, Drug> drugMap = new HashMap<>();
        if (!drugIds.isEmpty()) {
            List<Drug> drugList = drugMapper.selectBatchIds(drugIds);
            for (Drug d : drugList) {
                drugMap.put(d.getId(), d);
            }
        }

        Map<Long, Supplier> supplierMap = new HashMap<>();
        if (!supplierIds.isEmpty()) {
            List<Supplier> supplierList = supplierMapper.selectBatchIds(supplierIds);
            for (Supplier s : supplierList) {
                supplierMap.put(s.getId(), s);
            }
        }

        // 批量查询每个批次的库存数量
        Map<Long, BigDecimal> stockMap = new HashMap<>();
        if (!batchIds.isEmpty()) {
            List<Inventory> invList = inventoryMapper.selectList(
                    new LambdaQueryWrapper<Inventory>().in(Inventory::getBatchId, batchIds));
            invList.stream()
                    .filter(inv -> inv.getBatchId() != null)
                    .collect(Collectors.groupingBy(Inventory::getBatchId))
                    .forEach((bid, list) -> {
                        BigDecimal sum = list.stream()
                                .map(Inventory::getQuantity)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        stockMap.put(bid, sum);
                    });
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (DrugBatch b : batchPage.getRecords()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", b.getId());
            map.put("drugId", b.getDrugId());
            map.put("batchNo", b.getBatchNo());
            map.put("produceDate", b.getProduceDate());
            map.put("expireDate", b.getExpireDate());
            map.put("purchasePrice", b.getPurchasePrice());
            map.put("supplierId", b.getSupplierId());
            map.put("createdAt", b.getCreatedAt());

            // 计算剩余天数
            long remainingDays = 0;
            if (b.getExpireDate() != null) {
                remainingDays = ChronoUnit.DAYS.between(today, b.getExpireDate());
            }
            map.put("remainingDays", remainingDays);

            // 计算显示状态
            String displayStatus;
            if ("locked".equals(b.getStatus())) {
                displayStatus = "locked";
            } else if (b.getExpireDate() != null && b.getExpireDate().isBefore(today)) {
                displayStatus = "expired";
            } else if (b.getExpireDate() != null && b.getExpireDate().isBefore(today.plusDays(91))) {
                displayStatus = "near_expiry";
            } else {
                displayStatus = "normal";
            }
            map.put("status", displayStatus);

            // 关联药品信息
            Drug drug = drugMap.get(b.getDrugId());
            if (drug != null) {
                String drugDisplayName = drug.getTradeName() != null ? drug.getTradeName() : drug.getGenericName();
                map.put("drugName", drugDisplayName != null ? drugDisplayName : "");
                map.put("specification", drug.getSpecification());
                map.put("manufacturer", drug.getManufacturer());
            } else {
                map.put("drugName", "");
                map.put("specification", "");
                map.put("manufacturer", "");
            }

            // 关联供应商信息
            Supplier supplier = supplierMap.get(b.getSupplierId());
            map.put("supplier", supplier != null ? supplier.getName() : "");

            // 关联库存数量
            map.put("stockQuantity", stockMap.getOrDefault(b.getId(), BigDecimal.ZERO));

            records.add(map);
        }

        Page<Map<String, Object>> resultPage = new Page<>(current, size);
        resultPage.setTotal(batchPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 批次统计信息
     */
    public Map<String, Object> getStatistics() {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<DrugBatch> allWrapper = new LambdaQueryWrapper<>();
        allWrapper.ne(DrugBatch::getStatus, "locked");
        long total = batchMapper.selectCount(new LambdaQueryWrapper<>());

        // 已过期
        LambdaQueryWrapper<DrugBatch> expiredWrapper = new LambdaQueryWrapper<>();
        expiredWrapper.eq(DrugBatch::getStatus, "active")
                .le(DrugBatch::getExpireDate, today);
        long expired = batchMapper.selectCount(expiredWrapper);

        // 近效期（90天内到期，未过期）
        LambdaQueryWrapper<DrugBatch> nearWrapper = new LambdaQueryWrapper<>();
        nearWrapper.eq(DrugBatch::getStatus, "active")
                .gt(DrugBatch::getExpireDate, today)
                .le(DrugBatch::getExpireDate, today.plusDays(90));
        long nearExpiry = batchMapper.selectCount(nearWrapper);

        // 正常 = 总数 - 过期 - 近效期 - 锁定
        LambdaQueryWrapper<DrugBatch> lockedWrapper = new LambdaQueryWrapper<>();
        lockedWrapper.eq(DrugBatch::getStatus, "locked");
        long locked = batchMapper.selectCount(lockedWrapper);
        long normal = total - expired - nearExpiry - locked;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("normal", Math.max(0, normal));
        stats.put("nearExpiry", nearExpiry);
        stats.put("expired", expired);
        return stats;
    }

    /**
     * 根据ID查询
     */
    public DrugBatch getById(Long id) {
        return batchMapper.selectById(id);
    }

    /**
     * 根据批次号查询
     */
    public DrugBatch getByBatchNo(Long drugId, String batchNo) {
        LambdaQueryWrapper<DrugBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugBatch::getDrugId, drugId)
               .eq(DrugBatch::getBatchNo, batchNo)
               .last("LIMIT 1");
        return batchMapper.selectOne(wrapper);
    }

    /**
     * 创建批次
     */
    public DrugBatch create(DrugBatch batch) {
        batch.setStatus("active");
        batchMapper.insert(batch);
        return batch;
    }

    /**
     * 更新批次
     */
    public void update(DrugBatch batch) {
        batchMapper.updateById(batch);
    }

    /**
     * 更新状态
     */
    public void updateStatus(Long id, String status) {
        DrugBatch batch = new DrugBatch();
        batch.setId(id);
        batch.setStatus(status);
        batchMapper.updateById(batch);
    }

    /**
     * 获取药品的所有批次
     */
    public List<DrugBatch> getByDrugId(Long drugId) {
        LambdaQueryWrapper<DrugBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugBatch::getDrugId, drugId)
               .eq(DrugBatch::getStatus, "active")
               .orderByAsc(DrugBatch::getExpireDate);
        return batchMapper.selectList(wrapper);
    }

    /**
     * 获取近效期批次（默认90天内）
     */
    public List<DrugBatch> getNearExpiry(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        LambdaQueryWrapper<DrugBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugBatch::getStatus, "active")
               .le(DrugBatch::getExpireDate, deadline)
               .gt(DrugBatch::getExpireDate, LocalDate.now())
               .orderByAsc(DrugBatch::getExpireDate);
        return batchMapper.selectList(wrapper);
    }

    /**
     * 获取已过期批次
     */
    public List<DrugBatch> getExpired() {
        LambdaQueryWrapper<DrugBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugBatch::getStatus, "active")
               .lt(DrugBatch::getExpireDate, LocalDate.now())
               .orderByAsc(DrugBatch::getExpireDate);
        return batchMapper.selectList(wrapper);
    }

    /**
     * 获取有效批次（未过期）
     */
    public List<DrugBatch> getValidBatches(Long drugId) {
        LambdaQueryWrapper<DrugBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugBatch::getDrugId, drugId)
               .eq(DrugBatch::getStatus, "active")
               .gt(DrugBatch::getExpireDate, LocalDate.now())
               .orderByAsc(DrugBatch::getExpireDate);
        return batchMapper.selectList(wrapper);
    }

    /**
     * 统计药品有效批次数
     */
    public long countValidBatches(Long drugId) {
        LambdaQueryWrapper<DrugBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugBatch::getDrugId, drugId)
               .eq(DrugBatch::getStatus, "active")
               .gt(DrugBatch::getExpireDate, LocalDate.now());
        return batchMapper.selectCount(wrapper);
    }
}
