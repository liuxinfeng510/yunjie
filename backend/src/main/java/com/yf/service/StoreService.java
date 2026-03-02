package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Store;
import com.yf.exception.BusinessException;
import com.yf.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 门店服务
 */
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreMapper storeMapper;

    /**
     * 分页查询门店
     */
    public Page<Store> page(Page<Store> page, String name, String type, String status) {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Store::getName, name).or().like(Store::getCode, name);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Store::getType, type);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Store::getStatus, status);
        }
        wrapper.orderByAsc(Store::getType).orderByAsc(Store::getCreatedAt);
        return storeMapper.selectPage(page, wrapper);
    }

    /**
     * 获取所有门店列表（不分页）
     */
    public List<Store> listAll() {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getStatus, "active")
               .orderByAsc(Store::getType)
               .orderByAsc(Store::getName);
        return storeMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取门店
     */
    public Store getById(Long id) {
        return storeMapper.selectById(id);
    }

    /**
     * 创建门店
     */
    public int create(Store store) {
        // 检查门店编码唯一性
        if (StringUtils.hasText(store.getCode())) {
            LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Store::getCode, store.getCode());
            if (storeMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("门店编码已存在: " + store.getCode());
            }
        }
        if (store.getStatus() == null) {
            store.setStatus("active");
        }
        return storeMapper.insert(store);
    }

    /**
     * 更新门店
     */
    public int update(Store store) {
        return storeMapper.updateById(store);
    }

    /**
     * 删除门店（总部不可删除）
     */
    public int delete(Long id) {
        Store store = storeMapper.selectById(id);
        if (store != null && "headquarter".equals(store.getType())) {
            throw new BusinessException("总部/主店不可删除");
        }
        return storeMapper.deleteById(id);
    }

    /**
     * 获取总部/主店
     */
    public Store getHeadquarter() {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getType, "headquarter").last("LIMIT 1");
        return storeMapper.selectOne(wrapper);
    }

    /**
     * 统计门店数量
     */
    public long countActive() {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getStatus, "active");
        return storeMapper.selectCount(wrapper);
    }
}
