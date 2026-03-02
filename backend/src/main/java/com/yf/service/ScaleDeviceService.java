package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.ScaleDevice;
import com.yf.mapper.ScaleDeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 电子秤设备服务
 */
@Service
@RequiredArgsConstructor
public class ScaleDeviceService {

    private final ScaleDeviceMapper scaleDeviceMapper;

    public Page<ScaleDevice> page(Long storeId, String status, int pageNum, int pageSize) {
        Page<ScaleDevice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ScaleDevice> wrapper = new LambdaQueryWrapper<>();

        if (storeId != null) {
            wrapper.eq(ScaleDevice::getStoreId, storeId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ScaleDevice::getStatus, status);
        }

        wrapper.orderByDesc(ScaleDevice::getCreatedAt);
        return scaleDeviceMapper.selectPage(page, wrapper);
    }

    public List<ScaleDevice> listByStore(Long storeId) {
        LambdaQueryWrapper<ScaleDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScaleDevice::getStoreId, storeId);
        return scaleDeviceMapper.selectList(wrapper);
    }

    public ScaleDevice getById(Long id) {
        return scaleDeviceMapper.selectById(id);
    }

    public ScaleDevice create(ScaleDevice device) {
        device.setStatus("offline");
        scaleDeviceMapper.insert(device);
        return device;
    }

    public void update(ScaleDevice device) {
        scaleDeviceMapper.updateById(device);
    }

    public void delete(Long id) {
        scaleDeviceMapper.deleteById(id);
    }

    public void updateStatus(Long id, String status) {
        ScaleDevice device = new ScaleDevice();
        device.setId(id);
        device.setStatus(status);
        device.setLastHeartbeat(LocalDateTime.now());
        scaleDeviceMapper.updateById(device);
    }

    public void heartbeat(Long id) {
        ScaleDevice device = new ScaleDevice();
        device.setId(id);
        device.setLastHeartbeat(LocalDateTime.now());
        device.setStatus("online");
        scaleDeviceMapper.updateById(device);
    }

    public List<ScaleDevice> getOnlineDevices(Long storeId) {
        LambdaQueryWrapper<ScaleDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScaleDevice::getStoreId, storeId)
               .eq(ScaleDevice::getStatus, "online");
        return scaleDeviceMapper.selectList(wrapper);
    }
}
