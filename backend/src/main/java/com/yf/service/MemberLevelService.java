package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.MemberLevel;
import com.yf.exception.BusinessException;
import com.yf.mapper.MemberLevelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberLevelService {

    private final MemberLevelMapper memberLevelMapper;

    public Page<MemberLevel> page(int pageNum, int pageSize) {
        Page<MemberLevel> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(MemberLevel::getSortOrder);
        return memberLevelMapper.selectPage(page, wrapper);
    }

    public List<MemberLevel> list() {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(MemberLevel::getSortOrder);
        return memberLevelMapper.selectList(wrapper);
    }

    public MemberLevel getById(Long id) {
        return memberLevelMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberLevel create(MemberLevel level) {
        // 检查名称是否重复
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberLevel::getName, level.getName());
        if (memberLevelMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("等级名称已存在");
        }
        memberLevelMapper.insert(level);
        return level;
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, MemberLevel level) {
        MemberLevel existing = memberLevelMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("会员等级不存在");
        }
        // 检查名称是否重复（排除自身）
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberLevel::getName, level.getName())
               .ne(MemberLevel::getId, id);
        if (memberLevelMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("等级名称已存在");
        }
        level.setId(id);
        memberLevelMapper.updateById(level);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        memberLevelMapper.deleteById(id);
    }
}
