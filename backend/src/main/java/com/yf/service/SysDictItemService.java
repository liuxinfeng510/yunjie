package com.yf.service;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.config.tenant.TenantContext;
import com.yf.entity.SysDictItem;
import com.yf.mapper.SysDictItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 系统字典服务
 */
@Service
@RequiredArgsConstructor
public class SysDictItemService {

    private final SysDictItemMapper sysDictItemMapper;

    /**
     * 按类型查询字典项（包含系统预置和当前租户自定义）
     */
    public List<SysDictItem> listByType(String dictType) {
        Long tenantId = TenantContext.getTenantId();
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictItem::getDictType, dictType)
               .eq(SysDictItem::getStatus, "active");
        
        // 查询系统预置(tenant_id=0)和当前租户的数据
        if (tenantId != null && tenantId > 0) {
            wrapper.in(SysDictItem::getTenantId, Arrays.asList(0L, tenantId));
        } else {
            wrapper.eq(SysDictItem::getTenantId, 0L);
        }
        
        wrapper.orderByAsc(SysDictItem::getSortOrder)
               .orderByAsc(SysDictItem::getId);
        return sysDictItemMapper.selectList(wrapper);
    }

    /**
     * 新增字典项
     */
    public SysDictItem create(SysDictItem item) {
        // 自动填充拼音
        if (StringUtils.hasText(item.getItemValue())) {
            item.setPinyin(PinyinUtil.getPinyin(item.getItemValue(), ""));
            item.setPinyinShort(PinyinUtil.getFirstLetter(item.getItemValue(), ""));
        }
        item.setIsPreset(false);
        item.setStatus("active");
        sysDictItemMapper.insert(item);
        return item;
    }

    /**
     * 修改字典项
     */
    public void update(SysDictItem item) {
        SysDictItem existing = sysDictItemMapper.selectById(item.getId());
        if (existing == null) {
            throw new RuntimeException("字典项不存在");
        }
        // 预置数据只允许修改排序
        if (Boolean.TRUE.equals(existing.getIsPreset())) {
            existing.setSortOrder(item.getSortOrder());
            sysDictItemMapper.updateById(existing);
            return;
        }
        // 更新拼音
        if (StringUtils.hasText(item.getItemValue())) {
            item.setPinyin(PinyinUtil.getPinyin(item.getItemValue(), ""));
            item.setPinyinShort(PinyinUtil.getFirstLetter(item.getItemValue(), ""));
        }
        sysDictItemMapper.updateById(item);
    }

    /**
     * 删除字典项
     */
    public void delete(Long id) {
        SysDictItem existing = sysDictItemMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("字典项不存在");
        }
        if (Boolean.TRUE.equals(existing.getIsPreset())) {
            throw new RuntimeException("预置数据不允许删除");
        }
        sysDictItemMapper.deleteById(id);
    }

    /**
     * 根据ID查询
     */
    public SysDictItem getById(Long id) {
        return sysDictItemMapper.selectById(id);
    }
}
