package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DrugTraceCode;
import com.yf.mapper.DrugTraceCodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 药品追溯码服务
 * 支持国家药品追溯系统对接
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugTraceCodeService {

    private final DrugTraceCodeMapper traceCodeMapper;

    /**
     * 分页查询追溯码
     */
    public Page<DrugTraceCode> page(Long drugId, String batchNo, String status,
                                     int pageNum, int pageSize) {
        Page<DrugTraceCode> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DrugTraceCode> wrapper = new LambdaQueryWrapper<>();

        if (drugId != null) {
            wrapper.eq(DrugTraceCode::getDrugId, drugId);
        }
        if (batchNo != null && !batchNo.isEmpty()) {
            wrapper.like(DrugTraceCode::getBatchNo, batchNo);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(DrugTraceCode::getStatus, status);
        }

        wrapper.orderByDesc(DrugTraceCode::getCreatedAt);
        return traceCodeMapper.selectPage(page, wrapper);
    }

    /**
     * 根据追溯码查询
     */
    public DrugTraceCode getByTraceCode(String traceCode) {
        LambdaQueryWrapper<DrugTraceCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugTraceCode::getTraceCode, traceCode);
        return traceCodeMapper.selectOne(wrapper);
    }

    /**
     * 创建追溯码记录
     */
    public DrugTraceCode create(DrugTraceCode traceCode) {
        traceCode.setStatus("active");
        traceCode.setTraceTime(LocalDateTime.now());
        traceCodeMapper.insert(traceCode);
        return traceCode;
    }

    /**
     * 批量创建追溯码（入库时）
     */
    public void batchCreate(List<DrugTraceCode> traceCodes) {
        for (DrugTraceCode tc : traceCodes) {
            tc.setStatus("active");
            tc.setTraceTime(LocalDateTime.now());
            traceCodeMapper.insert(tc);
        }
    }

    /**
     * 更新追溯码状态
     */
    public void updateStatus(Long id, String status) {
        DrugTraceCode tc = new DrugTraceCode();
        tc.setId(id);
        tc.setStatus(status);
        traceCodeMapper.updateById(tc);
    }

    /**
     * 关联销售订单（出库时）
     */
    public void bindSaleOrder(String traceCode, Long saleOrderId) {
        DrugTraceCode tc = getByTraceCode(traceCode);
        if (tc != null) {
            tc.setSaleOrderId(saleOrderId);
            tc.setStatus("sold");
            traceCodeMapper.updateById(tc);
        }
    }

    /**
     * 追溯查询 - 根据追溯码获取完整信息
     */
    public TraceInfo trace(String traceCode) {
        DrugTraceCode tc = getByTraceCode(traceCode);
        if (tc == null) {
            return null;
        }

        TraceInfo info = new TraceInfo();
        info.setTraceCode(tc);

        // 可以在这里关联更多信息，如药品详情、供应商详情等
        return info;
    }

    /**
     * 获取药品的所有追溯码
     */
    public List<DrugTraceCode> getByDrugId(Long drugId) {
        LambdaQueryWrapper<DrugTraceCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugTraceCode::getDrugId, drugId)
               .orderByDesc(DrugTraceCode::getCreatedAt);
        return traceCodeMapper.selectList(wrapper);
    }

    /**
     * 获取批次的所有追溯码
     */
    public List<DrugTraceCode> getByBatchId(Long batchId) {
        LambdaQueryWrapper<DrugTraceCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugTraceCode::getBatchId, batchId)
               .orderByDesc(DrugTraceCode::getCreatedAt);
        return traceCodeMapper.selectList(wrapper);
    }

    /**
     * 统计活跃追溯码数量
     */
    public long countActive(Long drugId) {
        LambdaQueryWrapper<DrugTraceCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugTraceCode::getDrugId, drugId)
               .eq(DrugTraceCode::getStatus, "active");
        return traceCodeMapper.selectCount(wrapper);
    }

    /**
     * 追溯信息封装类
     */
    @lombok.Data
    public static class TraceInfo {
        private DrugTraceCode traceCode;
        // 可扩展更多关联信息
    }

    // ========== 入库单追溯码方法 ==========

    /**
     * 为入库明细行批量创建追溯码
     */
    public void batchCreateForDetail(Long stockInId, Long stockInDetailId, Long drugId,
                                      String batchNo, LocalDate produceDate, LocalDate expireDate,
                                      Long supplierId, List<String> codes) {
        if (codes == null || codes.isEmpty()) return;
        for (String code : codes) {
            DrugTraceCode tc = new DrugTraceCode();
            tc.setTraceCode(code.trim());
            tc.setDrugId(drugId);
            tc.setBatchNo(batchNo);
            tc.setProduceDate(produceDate);
            tc.setExpireDate(expireDate);
            tc.setSupplierId(supplierId);
            tc.setPurchaseOrderId(stockInId);
            tc.setStockInDetailId(stockInDetailId);
            tc.setStatus("pending");
            tc.setTraceTime(LocalDateTime.now());
            traceCodeMapper.insert(tc);
        }
    }

    /**
     * 删除入库单关联的所有追溯码
     */
    public void deleteByStockInId(Long stockInId) {
        LambdaQueryWrapper<DrugTraceCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugTraceCode::getPurchaseOrderId, stockInId);
        traceCodeMapper.delete(wrapper);
    }

    /**
     * 入库完成时激活追溯码 pending -> in_stock
     */
    public void activateByStockInId(Long stockInId) {
        LambdaQueryWrapper<DrugTraceCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugTraceCode::getPurchaseOrderId, stockInId)
               .eq(DrugTraceCode::getStatus, "pending");
        DrugTraceCode update = new DrugTraceCode();
        update.setStatus("in_stock");
        traceCodeMapper.update(update, wrapper);
    }

    /**
     * 批量查询明细行的追溯码列表（按 stockInDetailId 分组）
     */
    public Map<Long, List<String>> getCodesByDetailIds(List<Long> detailIds) {
        if (detailIds == null || detailIds.isEmpty()) {
            return new HashMap<>();
        }
        LambdaQueryWrapper<DrugTraceCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DrugTraceCode::getStockInDetailId, detailIds)
               .orderByAsc(DrugTraceCode::getId);
        List<DrugTraceCode> list = traceCodeMapper.selectList(wrapper);
        return list.stream().collect(java.util.stream.Collectors.groupingBy(
                DrugTraceCode::getStockInDetailId,
                java.util.stream.Collectors.mapping(DrugTraceCode::getTraceCode, java.util.stream.Collectors.toList())
        ));
    }
}
