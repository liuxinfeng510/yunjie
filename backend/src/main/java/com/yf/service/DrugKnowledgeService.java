package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DrugKnowledge;
import com.yf.mapper.DrugKnowledgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DrugKnowledgeService {

    private final DrugKnowledgeMapper drugKnowledgeMapper;

    public Page<DrugKnowledge> page(int pageNum, int pageSize, String name, String category) {
        Page<DrugKnowledge> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DrugKnowledge> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.and(w -> w.like(DrugKnowledge::getTradeName, name)
                    .or()
                    .like(DrugKnowledge::getGenericName, name));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(DrugKnowledge::getCategory, category);
        }
        wrapper.orderByDesc(DrugKnowledge::getId);
        return drugKnowledgeMapper.selectPage(page, wrapper);
    }

    public DrugKnowledge getById(Long id) {
        return drugKnowledgeMapper.selectById(id);
    }

    public void add(DrugKnowledge drug) {
        drug.setCreatedAt(java.time.LocalDateTime.now());
        drug.setUpdatedAt(java.time.LocalDateTime.now());
        drug.setDeleted(0);
        drugKnowledgeMapper.insert(drug);
    }

    public void update(DrugKnowledge drug) {
        drug.setUpdatedAt(java.time.LocalDateTime.now());
        drugKnowledgeMapper.updateById(drug);
    }

    public void delete(Long id) {
        drugKnowledgeMapper.deleteById(id);
    }
}
