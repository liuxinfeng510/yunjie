package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.FirstMarketingDrug;
import com.yf.mapper.FirstMarketingDrugMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstMarketingDrugService {

    private final FirstMarketingDrugMapper mapper;
    private final SysConfigService sysConfigService;

    @Transactional
    public FirstMarketingDrug create(FirstMarketingDrug entity) {
        entity.setApplyNo("SP" + System.currentTimeMillis());
        entity.setStatus("draft");
        mapper.insert(entity);
        log.info("创建首营品种申请：{}", entity.getApplyNo());
        return entity;
    }

    @Transactional
    public void update(Long id, FirstMarketingDrug entity) {
        FirstMarketingDrug existing = mapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!"draft".equals(existing.getStatus()) && !"rejected".equals(existing.getStatus())) {
            throw new RuntimeException("当前状态不允许编辑");
        }
        entity.setId(id);
        entity.setApplyNo(existing.getApplyNo());
        entity.setStatus(existing.getStatus());
        mapper.updateById(entity);
    }

    @Transactional
    public void delete(Long id) {
        FirstMarketingDrug existing = mapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!"draft".equals(existing.getStatus())) {
            throw new RuntimeException("仅草稿状态可删除");
        }
        mapper.deleteById(id);
    }

    @Transactional
    public void submit(Long id) {
        FirstMarketingDrug entity = mapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!"draft".equals(entity.getStatus()) && !"rejected".equals(entity.getStatus())) {
            throw new RuntimeException("当前状态不允许提交");
        }
        entity.setStatus("pending_first");
        entity.setApplyTime(LocalDateTime.now());
        entity.setFirstApproverId(null);
        entity.setFirstApproverName(null);
        entity.setFirstApproveTime(null);
        entity.setFirstApproveOpinion(null);
        entity.setFirstApproveResult(null);
        entity.setSecondApproverId(null);
        entity.setSecondApproverName(null);
        entity.setSecondApproveTime(null);
        entity.setSecondApproveOpinion(null);
        entity.setSecondApproveResult(null);
        mapper.updateById(entity);
        log.info("提交首营品种申请：{}", entity.getApplyNo());
    }

    @Transactional
    public void approveFirst(Long id, Long approverId, String approverName, boolean approved, String opinion) {
        FirstMarketingDrug entity = mapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!"pending_first".equals(entity.getStatus())) {
            throw new RuntimeException("当前状态不允许一审");
        }
        entity.setFirstApproverId(approverId);
        entity.setFirstApproverName(approverName);
        entity.setFirstApproveTime(LocalDateTime.now());
        entity.setFirstApproveOpinion(opinion);
        entity.setFirstApproveResult(approved ? "approved" : "rejected");

        if (!approved) {
            entity.setStatus("rejected");
        } else {
            String level = sysConfigService.getValue("gsp.first_marketing_approval_level", "1");
            entity.setStatus("2".equals(level) ? "pending_second" : "approved");
        }
        mapper.updateById(entity);
        log.info("一审首营品种：{}，结果：{}", id, approved ? "通过" : "驳回");
    }

    @Transactional
    public void approveSecond(Long id, Long approverId, String approverName, boolean approved, String opinion) {
        FirstMarketingDrug entity = mapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("记录不存在");
        }
        if (!"pending_second".equals(entity.getStatus())) {
            throw new RuntimeException("当前状态不允许二审");
        }
        entity.setSecondApproverId(approverId);
        entity.setSecondApproverName(approverName);
        entity.setSecondApproveTime(LocalDateTime.now());
        entity.setSecondApproveOpinion(opinion);
        entity.setSecondApproveResult(approved ? "approved" : "rejected");
        entity.setStatus(approved ? "approved" : "rejected");
        mapper.updateById(entity);
        log.info("二审首营品种：{}，结果：{}", id, approved ? "通过" : "驳回");
    }

    public Page<FirstMarketingDrug> page(int current, int size, String genericName, String supplierName, String status) {
        Page<FirstMarketingDrug> page = new Page<>(current, size);
        LambdaQueryWrapper<FirstMarketingDrug> wrapper = new LambdaQueryWrapper<>();
        if (genericName != null && !genericName.isEmpty()) {
            wrapper.like(FirstMarketingDrug::getGenericName, genericName);
        }
        if (supplierName != null && !supplierName.isEmpty()) {
            wrapper.like(FirstMarketingDrug::getSupplierName, supplierName);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(FirstMarketingDrug::getStatus, status);
        }
        wrapper.orderByDesc(FirstMarketingDrug::getCreatedAt);
        return mapper.selectPage(page, wrapper);
    }

    public FirstMarketingDrug getById(Long id) {
        return mapper.selectById(id);
    }
}
