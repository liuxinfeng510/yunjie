package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Herb;
import com.yf.mapper.HerbMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 中药饮片服务类
 */
@Service
@RequiredArgsConstructor
public class HerbService {
    
    private final HerbMapper herbMapper;
    
    /**
     * 分页查询
     */
    public Page<Herb> page(Page<Herb> page, String name, String pinyin, String category, 
                          String nature, Boolean isToxic, String status) {
        LambdaQueryWrapper<Herb> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Herb::getName, name)
               .like(StringUtils.hasText(pinyin), Herb::getPinyin, pinyin)
               .eq(StringUtils.hasText(category), Herb::getCategory, category)
               .eq(StringUtils.hasText(nature), Herb::getNature, nature)
               .eq(isToxic != null, Herb::getIsToxic, isToxic)
               .eq(StringUtils.hasText(status), Herb::getStatus, status)
               .orderByDesc(Herb::getCreatedAt);
        return herbMapper.selectPage(page, wrapper);
    }
    
    /**
     * 根据ID查询
     */
    public Herb getById(Long id) {
        return herbMapper.selectById(id);
    }
    
    /**
     * 新增
     */
    public boolean save(Herb herb) {
        return herbMapper.insert(herb) > 0;
    }
    
    /**
     * 更新
     */
    public boolean updateById(Herb herb) {
        return herbMapper.updateById(herb) > 0;
    }
    
    /**
     * 删除
     */
    public boolean deleteById(Long id) {
        return herbMapper.deleteById(id) > 0;
    }
}
