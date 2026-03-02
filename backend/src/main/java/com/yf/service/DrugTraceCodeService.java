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
import java.util.List;

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
}
