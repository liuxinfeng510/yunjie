package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DrugBatch;
import com.yf.mapper.DrugBatchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 药品批次服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugBatchService {

    private final DrugBatchMapper batchMapper;

    /**
     * 分页查询批次
     */
    public Page<DrugBatch> page(Long drugId, Long supplierId, String status,
                                 int pageNum, int pageSize) {
        Page<DrugBatch> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DrugBatch> wrapper = new LambdaQueryWrapper<>();

        if (drugId != null) {
            wrapper.eq(DrugBatch::getDrugId, drugId);
        }
        if (supplierId != null) {
            wrapper.eq(DrugBatch::getSupplierId, supplierId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(DrugBatch::getStatus, status);
        }

        wrapper.orderByDesc(DrugBatch::getCreatedAt);
        return batchMapper.selectPage(page, wrapper);
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
               .eq(DrugBatch::getBatchNo, batchNo);
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
               .orderByAsc(DrugBatch::getExpireDate); // 先进先出
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
