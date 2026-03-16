package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.HerbIncompatibility;
import com.yf.service.HerbIncompatibilityService;
import com.yf.service.HerbIncompatibilityService.IncompatibilityCheckResult;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 中药配伍禁忌控制器
 */
@RestController
@RequestMapping("/herb-incompatibility")
@RequiredArgsConstructor
public class HerbIncompatibilityController {

    private final HerbIncompatibilityService incompatibilityService;

    /**
     * 分页查询配伍禁忌
     */
    @GetMapping("/page")
    public ApiResponse<Page<HerbIncompatibility>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String herbName,
            @RequestParam(required = false) String type) {
        return ApiResponse.success(incompatibilityService.page(pageNum, pageSize, herbName, type));
    }

    /**
     * 检查一组中药配伍
     */
    @PostMapping("/check")
    public ApiResponse<IncompatibilityCheckResult> check(@RequestBody Map<String, List<String>> request) {
        List<String> herbs = request.get("herbs");
        return ApiResponse.success(incompatibilityService.check(herbs));
    }

    /**
     * 检查新药材与已有药材的配伍
     */
    @PostMapping("/check-new")
    public ApiResponse<IncompatibilityCheckResult> checkNew(@RequestBody Map<String, Object> request) {
        String newHerb = (String) request.get("newHerb");
        @SuppressWarnings("unchecked")
        List<String> existingHerbs = (List<String>) request.get("existingHerbs");
        return ApiResponse.success(incompatibilityService.checkNewHerb(newHerb, existingHerbs));
    }

    /**
     * 获取某味药材的所有禁忌
     */
    @GetMapping("/herb/{herbName}")
    public ApiResponse<List<HerbIncompatibility>> getIncompatibleHerbs(@PathVariable String herbName) {
        return ApiResponse.success(incompatibilityService.getIncompatibleHerbs(herbName));
    }

    /**
     * 按类型获取禁忌规则
     */
    @GetMapping("/type/{type}")
    public ApiResponse<List<HerbIncompatibility>> getByType(@PathVariable String type) {
        return ApiResponse.success(incompatibilityService.getByType(type));
    }

    /**
     * 获取十八反规则
     */
    @GetMapping("/18-oppose")
    public ApiResponse<List<HerbIncompatibility>> get18Oppose() {
        return ApiResponse.success(incompatibilityService.getByType("18_oppose"));
    }

    /**
     * 获取十九畏规则
     */
    @GetMapping("/19-fear")
    public ApiResponse<List<HerbIncompatibility>> get19Fear() {
        return ApiResponse.success(incompatibilityService.getByType("19_fear"));
    }

    /**
     * 获取妊娠禁忌规则
     */
    @GetMapping("/pregnancy")
    public ApiResponse<List<HerbIncompatibility>> getPregnancy() {
        return ApiResponse.success(incompatibilityService.getByType("pregnancy"));
    }

    @GetMapping("/{id}")
    public ApiResponse<HerbIncompatibility> getById(@PathVariable Long id) {
        return ApiResponse.success(incompatibilityService.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> add(@RequestBody HerbIncompatibility item) {
        incompatibilityService.add(item);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody HerbIncompatibility item) {
        item.setId(id);
        incompatibilityService.update(item);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        incompatibilityService.delete(id);
        return ApiResponse.success(null);
    }
}
