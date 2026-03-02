package com.yf.controller;

import com.yf.entity.MemberHealthProfile;
import com.yf.service.MemberHealthProfileService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员健康画像控制器
 */
@RestController
@RequestMapping("/member-health")
@RequiredArgsConstructor
public class MemberHealthProfileController {

    private final MemberHealthProfileService healthProfileService;

    /**
     * 获取会员健康画像
     */
    @GetMapping("/profile/{memberId}")
    public ApiResponse<MemberHealthProfile> getProfile(@PathVariable Long memberId) {
        MemberHealthProfile profile = healthProfileService.getOrCreateProfile(memberId);
        return ApiResponse.success(profile);
    }

    /**
     * 更新健康画像
     */
    @PutMapping("/profile")
    public ApiResponse<MemberHealthProfile> updateProfile(@RequestBody MemberHealthProfile profile) {
        MemberHealthProfile updated = healthProfileService.updateProfile(profile);
        return ApiResponse.success(updated);
    }

    /**
     * 获取健康分析
     */
    @GetMapping("/analysis/{memberId}")
    public ApiResponse<MemberHealthProfileService.MemberHealthAnalysis> getAnalysis(
            @PathVariable Long memberId) {
        MemberHealthProfileService.MemberHealthAnalysis analysis = 
            healthProfileService.analyzeHealthProfile(memberId);
        return ApiResponse.success(analysis);
    }

    /**
     * 获取用药建议
     */
    @GetMapping("/advice/{memberId}")
    public ApiResponse<List<String>> getMedicationAdvice(@PathVariable Long memberId) {
        List<String> advice = healthProfileService.getMedicationAdvice(memberId);
        return ApiResponse.success(advice);
    }
}
