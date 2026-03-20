package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.MemberLevel;
import com.yf.exception.BusinessException;
import com.yf.mapper.MemberLevelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberLevelService {

    private final MemberLevelMapper memberLevelMapper;

    public Page<MemberLevel> page(int pageNum, int pageSize) {
        ensureDefaultLevels();
        Page<MemberLevel> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(MemberLevel::getSortOrder);
        return memberLevelMapper.selectPage(page, wrapper);
    }

    public List<MemberLevel> list() {
        ensureDefaultLevels();
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(MemberLevel::getSortOrder);
        return memberLevelMapper.selectList(wrapper);
    }

    /**
     * 获取当前租户的默认等级（sort_order最小的）
     */
    public MemberLevel getDefaultLevel() {
        ensureDefaultLevels();
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(MemberLevel::getSortOrder).last("LIMIT 1");
        return memberLevelMapper.selectOne(wrapper);
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

    /**
     * 当前租户无会员等级时，自动创建默认等级
     */
    @Transactional(rollbackFor = Exception.class)
    public void ensureDefaultLevels() {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        if (memberLevelMapper.selectCount(wrapper) > 0) {
            return;
        }
        // 自动初始化4个默认等级
        Object[][] defaults = {
            {"普通会员", BigDecimal.ZERO,          new BigDecimal("100.00"), new BigDecimal("1.00"), 1},
            {"银卡会员", new BigDecimal("1000.00"), new BigDecimal("95.00"),  new BigDecimal("1.50"), 2},
            {"金卡会员", new BigDecimal("5000.00"), new BigDecimal("90.00"),  new BigDecimal("2.00"), 3},
            {"钻石会员", new BigDecimal("20000.00"),new BigDecimal("85.00"),  new BigDecimal("3.00"), 4},
        };
        for (Object[] d : defaults) {
            MemberLevel level = new MemberLevel();
            level.setName((String) d[0]);
            level.setMinAmount((BigDecimal) d[1]);
            level.setDiscount((BigDecimal) d[2]);
            level.setPointsRate((BigDecimal) d[3]);
            level.setSortOrder((Integer) d[4]);
            memberLevelMapper.insert(level);
        }
    }
}
