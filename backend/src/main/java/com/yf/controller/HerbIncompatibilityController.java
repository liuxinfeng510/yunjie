package com.yf.controller;

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
}
