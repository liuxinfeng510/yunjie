package com.yf.service;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DrugManufacturer;
import com.yf.mapper.DrugManufacturerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 生产企业服务
 */
@Service
@RequiredArgsConstructor
public class DrugManufacturerService {

    private final DrugManufacturerMapper manufacturerMapper;

    /**
     * 搜索生产企业（名称/拼音模糊匹配）
     */
    public List<DrugManufacturer> search(String keyword) {
        LambdaQueryWrapper<DrugManufacturer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugManufacturer::getStatus, "active");
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(DrugManufacturer::getName, keyword)
                .or().like(DrugManufacturer::getShortName, keyword)
                .or().like(DrugManufacturer::getPinyin, keyword)
                .or().like(DrugManufacturer::getPinyinShort, keyword)
            );
        }
        wrapper.orderByDesc(DrugManufacturer::getCreatedAt)
               .last("LIMIT 20");
        return manufacturerMapper.selectList(wrapper);
    }

    /**
     * 按名称查找或创建（用于导入/自动保存）
     */
    public DrugManufacturer getOrCreate(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        // 先查找
        LambdaQueryWrapper<DrugManufacturer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugManufacturer::getName, name.trim());
        DrugManufacturer existing = manufacturerMapper.selectOne(wrapper);
        if (existing != null) {
            return existing;
        }
        // 创建新记录
        DrugManufacturer manufacturer = new DrugManufacturer();
        manufacturer.setName(name.trim());
        manufacturer.setPinyin(PinyinUtil.getPinyin(name, ""));
        manufacturer.setPinyinShort(PinyinUtil.getFirstLetter(name, ""));
        manufacturer.setStatus("active");
        manufacturerMapper.insert(manufacturer);
        return manufacturer;
    }

    /**
     * 分页查询
     */
    public Page<DrugManufacturer> page(String keyword, int pageNum, int pageSize) {
        Page<DrugManufacturer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DrugManufacturer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(DrugManufacturer::getName, keyword)
                .or().like(DrugManufacturer::getShortName, keyword)
            );
        }
        wrapper.orderByDesc(DrugManufacturer::getCreatedAt);
        return manufacturerMapper.selectPage(page, wrapper);
    }

    /**
     * 新增
     */
    public DrugManufacturer create(DrugManufacturer manufacturer) {
        if (StringUtils.hasText(manufacturer.getName())) {
            manufacturer.setPinyin(PinyinUtil.getPinyin(manufacturer.getName(), ""));
            manufacturer.setPinyinShort(PinyinUtil.getFirstLetter(manufacturer.getName(), ""));
        }
        manufacturer.setStatus("active");
        manufacturerMapper.insert(manufacturer);
        return manufacturer;
    }

    /**
     * 修改
     */
    public void update(DrugManufacturer manufacturer) {
        if (StringUtils.hasText(manufacturer.getName())) {
            manufacturer.setPinyin(PinyinUtil.getPinyin(manufacturer.getName(), ""));
            manufacturer.setPinyinShort(PinyinUtil.getFirstLetter(manufacturer.getName(), ""));
        }
        manufacturerMapper.updateById(manufacturer);
    }

    /**
     * 删除
     */
    public void delete(Long id) {
        manufacturerMapper.deleteById(id);
    }

    /**
     * 根据ID查询
     */
    public DrugManufacturer getById(Long id) {
        return manufacturerMapper.selectById(id);
    }
}
