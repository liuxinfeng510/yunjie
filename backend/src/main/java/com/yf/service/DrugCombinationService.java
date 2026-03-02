package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DrugCombination;
import com.yf.mapper.DrugCombinationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 联合用药推荐服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugCombinationService {

    private final DrugCombinationMapper combinationMapper;

    /**
     * 创建联合用药推荐
     */
    @Transactional
    public DrugCombination create(DrugCombination combination) {
        combination.setStatus("active");
        if (combination.getWeight() == null) {
            combination.setWeight(0);
        }
        combinationMapper.insert(combination);
        log.info("创建联合用药推荐: primary={}, recommend={}", 
                combination.getPrimaryDrugName(), combination.getRecommendDrugName());
        return combination;
    }

    /**
     * 更新联合用药推荐
     */
    @Transactional
    public DrugCombination update(DrugCombination combination) {
        combinationMapper.updateById(combination);
        return combination;
    }

    /**
     * 删除联合用药推荐
     */
    @Transactional
    public void delete(Long id) {
        combinationMapper.deleteById(id);
    }

    /**
     * 启用/停用
     */
    @Transactional
    public void toggleStatus(Long id) {
        DrugCombination combination = combinationMapper.selectById(id);
        if (combination != null) {
            combination.setStatus("active".equals(combination.getStatus()) ? "inactive" : "active");
            combinationMapper.updateById(combination);
        }
    }

    /**
     * 根据ID查询
     */
    public DrugCombination getById(Long id) {
        return combinationMapper.selectById(id);
    }

    /**
     * 获取药品的推荐联合用药列表
     */
    public List<DrugCombination> getRecommendations(Long drugId) {
        LambdaQueryWrapper<DrugCombination> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugCombination::getPrimaryDrugId, drugId);
        wrapper.eq(DrugCombination::getStatus, "active");
        wrapper.orderByDesc(DrugCombination::getWeight);
        return combinationMapper.selectList(wrapper);
    }

    /**
     * 根据推荐类型获取推荐
     */
    public List<DrugCombination> getRecommendationsByType(Long drugId, String recommendType) {
        LambdaQueryWrapper<DrugCombination> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugCombination::getPrimaryDrugId, drugId);
        wrapper.eq(DrugCombination::getRecommendType, recommendType);
        wrapper.eq(DrugCombination::getStatus, "active");
        wrapper.orderByDesc(DrugCombination::getWeight);
        return combinationMapper.selectList(wrapper);
    }

    /**
     * 分页查询
     */
    public Page<DrugCombination> page(Long primaryDrugId, String recommendType, String status, 
                                       int pageNum, int pageSize) {
        LambdaQueryWrapper<DrugCombination> wrapper = new LambdaQueryWrapper<>();
        if (primaryDrugId != null) wrapper.eq(DrugCombination::getPrimaryDrugId, primaryDrugId);
        if (recommendType != null && !recommendType.isEmpty()) {
            wrapper.eq(DrugCombination::getRecommendType, recommendType);
        }
        if (status != null && !status.isEmpty()) wrapper.eq(DrugCombination::getStatus, status);
        wrapper.orderByDesc(DrugCombination::getWeight);
        return combinationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 批量创建联合用药推荐
     */
    @Transactional
    public void batchCreate(List<DrugCombination> combinations) {
        for (DrugCombination combination : combinations) {
            combination.setStatus("active");
            if (combination.getWeight() == null) {
                combination.setWeight(0);
            }
            combinationMapper.insert(combination);
        }
        log.info("批量创建联合用药推荐: count={}", combinations.size());
    }
}
