package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.HerbKnowledge;
import com.yf.mapper.HerbKnowledgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class HerbKnowledgeService {

    private final HerbKnowledgeMapper herbKnowledgeMapper;

    public Page<HerbKnowledge> page(int pageNum, int pageSize, String name, String nature) {
        Page<HerbKnowledge> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HerbKnowledge> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(HerbKnowledge::getHerbName, name);
        }
        if (StringUtils.hasText(nature)) {
            wrapper.eq(HerbKnowledge::getNature, nature);
        }
        wrapper.orderByDesc(HerbKnowledge::getId);
        return herbKnowledgeMapper.selectPage(page, wrapper);
    }

    public HerbKnowledge getById(Long id) {
        return herbKnowledgeMapper.selectById(id);
    }

    public void add(HerbKnowledge herb) {
        herb.setCreatedAt(java.time.LocalDateTime.now());
        herb.setUpdatedAt(java.time.LocalDateTime.now());
        herb.setDeleted(0);
        herbKnowledgeMapper.insert(herb);
    }

    public void update(HerbKnowledge herb) {
        herb.setUpdatedAt(java.time.LocalDateTime.now());
        herbKnowledgeMapper.updateById(herb);
    }

    public void delete(Long id) {
        herbKnowledgeMapper.deleteById(id);
    }
}
