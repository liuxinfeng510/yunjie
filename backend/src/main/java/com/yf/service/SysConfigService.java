package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.entity.SysConfig;
import com.yf.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统配置服务
 */
@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    /**
     * 根据配置键获取配置值
     */
    public String getValue(String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        SysConfig config = sysConfigMapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 根据配置键获取配置值，带默认值
     */
    public String getValue(String configKey, String defaultValue) {
        String value = getValue(configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取布尔类型配置
     */
    public boolean getBooleanValue(String configKey, boolean defaultValue) {
        String value = getValue(configKey);
        if (value == null) return defaultValue;
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * 按分组获取所有配置
     */
    public Map<String, String> getByGroup(String configGroup) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigGroup, configGroup)
               .orderByAsc(SysConfig::getSortOrder);
        List<SysConfig> configs = sysConfigMapper.selectList(wrapper);
        return configs.stream()
                .collect(Collectors.toMap(SysConfig::getConfigKey, SysConfig::getConfigValue,
                        (v1, v2) -> v2));
    }

    /**
     * 获取所有配置，按分组组织
     */
    public Map<String, List<SysConfig>> getAllGrouped() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysConfig::getConfigGroup)
               .orderByAsc(SysConfig::getSortOrder);
        List<SysConfig> configs = sysConfigMapper.selectList(wrapper);
        return configs.stream().collect(Collectors.groupingBy(SysConfig::getConfigGroup));
    }

    /**
     * 设置单个配置项（存在则更新，不存在则新增）
     */
    @Transactional
    public void setValue(String configGroup, String configKey, String configValue,
                         String valueType, String description) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        SysConfig existing = sysConfigMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setConfigValue(configValue);
            if (StringUtils.hasText(description)) {
                existing.setDescription(description);
            }
            sysConfigMapper.updateById(existing);
        } else {
            SysConfig config = new SysConfig();
            config.setConfigGroup(configGroup);
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setValueType(valueType != null ? valueType : "string");
            config.setDescription(description);
            config.setSortOrder(0);
            sysConfigMapper.insert(config);
        }
    }

    /**
     * 批量更新配置
     */
    @Transactional
    public void batchUpdate(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysConfig::getConfigKey, entry.getKey());
            SysConfig existing = sysConfigMapper.selectOne(wrapper);
            if (existing != null) {
                existing.setConfigValue(entry.getValue());
                sysConfigMapper.updateById(existing);
            }
        }
    }

    /**
     * 初始化默认功能开关配置
     */
    @Transactional
    public void initFeatureDefaults(boolean isChainStore) {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("feature.herb", "true");
        defaults.put("feature.member", "true");
        defaults.put("feature.gsp", "true");
        defaults.put("feature.prescription", "true");
        defaults.put("feature.scale", "false");
        defaults.put("feature.temp_humidity", "true");
        defaults.put("feature.analytics", "true");
        defaults.put("feature.central_purchase", isChainStore ? "true" : "false");
        defaults.put("feature.cross_transfer", isChainStore ? "true" : "false");

        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            setValue("feature", entry.getKey(), entry.getValue(), "boolean",
                    "功能开关: " + entry.getKey());
        }
    }
}
