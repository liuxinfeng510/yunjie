package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.HerbIncompatibility;
import com.yf.mapper.HerbIncompatibilityMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 中药配伍禁忌检查服务
 * 基于十八反、十九畏、妊娠禁忌等规则检查处方配伍安全性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HerbIncompatibilityService {

    private final HerbIncompatibilityMapper incompatibilityMapper;

    /**
     * 分页查询配伍禁忌
     */
    public Page<HerbIncompatibility> page(int pageNum, int pageSize, String herbName, String type) {
        Page<HerbIncompatibility> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HerbIncompatibility> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(herbName)) {
            wrapper.and(w -> w.like(HerbIncompatibility::getHerbA, herbName)
                    .or()
                    .like(HerbIncompatibility::getHerbB, herbName));
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(HerbIncompatibility::getType, type);
        }
        wrapper.orderByDesc(HerbIncompatibility::getId);
        return incompatibilityMapper.selectPage(page, wrapper);
    }

    /**
     * 检查一组中药是否存在配伍禁忌
     * @param herbNames 中药名称列表
     * @return 检查结果
     */
    public IncompatibilityCheckResult check(List<String> herbNames) {
        if (herbNames == null || herbNames.size() < 2) {
            return IncompatibilityCheckResult.safe();
        }

        // 加载所有配伍禁忌规则
        List<HerbIncompatibility> allRules = loadAllRules();

        List<IncompatibilityWarning> warnings = new ArrayList<>();
        Set<String> herbSet = herbNames.stream()
                .map(String::trim)
                .collect(Collectors.toSet());

        // 检查每对药材
        for (HerbIncompatibility rule : allRules) {
            boolean hasA = containsHerb(herbSet, rule.getHerbA());
            boolean hasB = containsHerb(herbSet, rule.getHerbB());

            if (hasA && hasB) {
                IncompatibilityWarning warning = new IncompatibilityWarning();
                warning.setHerbA(rule.getHerbA());
                warning.setHerbB(rule.getHerbB());
                warning.setType(rule.getType());
                warning.setTypeName(getTypeName(rule.getType()));
                warning.setSeverity(rule.getSeverity());
                warning.setDescription(rule.getDescription());
                warning.setForbidden("forbidden".equals(rule.getSeverity()));
                warnings.add(warning);
            }
        }

        IncompatibilityCheckResult result = new IncompatibilityCheckResult();
        result.setSafe(warnings.isEmpty());
        result.setWarnings(warnings);
        result.setForbiddenCount((int) warnings.stream().filter(IncompatibilityWarning::isForbidden).count());
        result.setCautionCount((int) warnings.stream().filter(w -> !w.isForbidden()).count());

        return result;
    }

    /**
     * 检查新增药材是否与已有药材冲突
     */
    public IncompatibilityCheckResult checkNewHerb(String newHerb, List<String> existingHerbs) {
        if (existingHerbs == null || existingHerbs.isEmpty()) {
            return IncompatibilityCheckResult.safe();
        }

        List<String> allHerbs = new ArrayList<>(existingHerbs);
        allHerbs.add(newHerb);
        return check(allHerbs);
    }

    /**
     * 获取某味药材的所有禁忌药材
     */
    public List<HerbIncompatibility> getIncompatibleHerbs(String herbName) {
        LambdaQueryWrapper<HerbIncompatibility> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(HerbIncompatibility::getHerbA, herbName)
                        .or()
                        .eq(HerbIncompatibility::getHerbB, herbName));
        return incompatibilityMapper.selectList(wrapper);
    }

    public HerbIncompatibility getById(Long id) {
        return incompatibilityMapper.selectById(id);
    }

    public void add(HerbIncompatibility item) {
        item.setCreatedAt(java.time.LocalDateTime.now());
        item.setUpdatedAt(java.time.LocalDateTime.now());
        item.setDeleted(0);
        incompatibilityMapper.insert(item);
    }

    public void update(HerbIncompatibility item) {
        item.setUpdatedAt(java.time.LocalDateTime.now());
        incompatibilityMapper.updateById(item);
    }

    public void delete(Long id) {
        incompatibilityMapper.deleteById(id);
    }

    /**
     * 按类型获取禁忌规则
     */
    public List<HerbIncompatibility> getByType(String type) {
        LambdaQueryWrapper<HerbIncompatibility> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HerbIncompatibility::getType, type);
        return incompatibilityMapper.selectList(wrapper);
    }

    private List<HerbIncompatibility> loadAllRules() {
        return incompatibilityMapper.selectList(null);
    }

    private boolean containsHerb(Set<String> herbSet, String herbName) {
        if (herbSet.contains(herbName)) {
            return true;
        }
        // 模糊匹配：处理别名或简称
        for (String herb : herbSet) {
            if (herb.contains(herbName) || herbName.contains(herb)) {
                return true;
            }
        }
        return false;
    }

    private String getTypeName(String type) {
        return switch (type) {
            case "18_oppose" -> "十八反";
            case "19_fear" -> "十九畏";
            case "pregnancy" -> "妊娠禁忌";
            default -> type;
        };
    }

    @Data
    public static class IncompatibilityCheckResult {
        private boolean safe;
        private List<IncompatibilityWarning> warnings = new ArrayList<>();
        private int forbiddenCount;
        private int cautionCount;

        public static IncompatibilityCheckResult safe() {
            IncompatibilityCheckResult r = new IncompatibilityCheckResult();
            r.setSafe(true);
            return r;
        }
    }

    @Data
    public static class IncompatibilityWarning {
        private String herbA;
        private String herbB;
        private String type;
        private String typeName;
        private String severity;
        private String description;
        private boolean forbidden;
    }
}
