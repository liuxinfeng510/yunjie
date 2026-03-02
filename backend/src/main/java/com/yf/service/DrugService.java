package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Drug;
import com.yf.mapper.DrugMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 药品服务类
 */
@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugMapper drugMapper;

    /**
     * 分页查询药品
     *
     * @param page       分页对象
     * @param name       药品名称（支持通用名/商品名/拼音/拼音简码/条形码）
     * @param categoryId 分类ID
     * @param otcType    OTC类型
     * @param status     状态
     * @return 分页结果
     */
    public Page<Drug> page(Page<Drug> page, String name, Long categoryId, String otcType, String status) {
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();

        // 多字段模糊查询
        if (StringUtils.hasText(name)) {
            wrapper.and(w -> w
                    .like(Drug::getGenericName, name)
                    .or().like(Drug::getTradeName, name)
                    .or().like(Drug::getPinyin, name)
                    .or().like(Drug::getPinyinShort, name)
                    .or().like(Drug::getBarcode, name)
            );
        }

        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Drug::getCategoryId, categoryId);
        }

        // OTC类型筛选
        if (StringUtils.hasText(otcType)) {
            wrapper.eq(Drug::getOtcType, otcType);
        }

        // 状态筛选
        if (StringUtils.hasText(status)) {
            wrapper.eq(Drug::getStatus, status);
        }

        // 按创建时间降序
        wrapper.orderByDesc(Drug::getCreatedAt);

        return drugMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询药品
     *
     * @param id 药品ID
     * @return 药品信息
     */
    public Drug getById(Long id) {
        return drugMapper.selectById(id);
    }

    /**
     * 创建药品
     *
     * @param drug 药品信息
     * @return 创建成功的记录数
     */
    public int create(Drug drug) {
        return drugMapper.insert(drug);
    }

    /**
     * 更新药品
     *
     * @param drug 药品信息
     * @return 更新成功的记录数
     */
    public int update(Drug drug) {
        return drugMapper.updateById(drug);
    }

    /**
     * 删除药品
     *
     * @param id 药品ID
     * @return 删除成功的记录数
     */
    public int delete(Long id) {
        return drugMapper.deleteById(id);
    }
}
