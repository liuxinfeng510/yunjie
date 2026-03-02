package com.yf.controller.mobile;

import com.yf.entity.Member;
import com.yf.service.MemberService;
import com.yf.service.MemberHealthProfileService;
import com.yf.service.ChronicDiseaseService;
import com.yf.service.MedicationReminderService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 移动端会员控制器
 * 为UniApp/微信小程序提供会员相关接口
 */
@RestController
@RequestMapping("/mobile/member")
@RequiredArgsConstructor
public class MobileMemberController {

    private final MemberService memberService;
    private final MemberHealthProfileService healthProfileService;
    private final ChronicDiseaseService chronicDiseaseService;
    private final MedicationReminderService reminderService;

    /**
     * 微信登录/绑定
     */
    @PostMapping("/wx-login")
    public ApiResponse<MemberLoginResult> wxLogin(@RequestBody WxLoginRequest request) {
        // 实际实现需对接微信API获取openid
        // String openid = wxService.getOpenid(request.getCode());
        
        MemberLoginResult result = new MemberLoginResult();
        
        // 查找或创建会员
        Member member = memberService.findByWechatOpenid(request.getOpenid());
        if (member == null && request.getPhone() != null) {
            // 新会员注册
            member = new Member();
            member.setWechatOpenid(request.getOpenid());
            member.setPhone(request.getPhone());
            member.setName(request.getNickname());
            member.setStatus("active");
            memberService.createMember(member);
        }
        
        if (member != null) {
            result.setMemberId(member.getId());
            result.setMemberName(member.getName());
            result.setPhone(member.getPhone());
            result.setBound(true);
            // 生成token
            result.setToken(generateMobileToken(member.getId()));
        } else {
            result.setBound(false);
        }
        
        return ApiResponse.success(result);
    }

    /**
     * 获取会员信息
     */
    @GetMapping("/info")
    public ApiResponse<MemberInfo> getMemberInfo(@RequestHeader("X-Member-Id") Long memberId) {
        Member member = memberService.getById(memberId);
        if (member == null) {
            return ApiResponse.error("会员不存在");
        }
        
        MemberInfo info = new MemberInfo();
        info.setMemberId(member.getId());
        info.setName(member.getName());
        info.setPhone(member.getPhone());
        info.setPoints(member.getPoints());
        info.setTotalAmount(member.getTotalAmount());
        
        // 获取健康分析
        MemberHealthProfileService.MemberHealthAnalysis analysis = 
            healthProfileService.analyzeHealthProfile(memberId);
        info.setHealthTags(analysis.getTags().toArray(new String[0]));
        
        // 获取活跃用药提醒数
        info.setActiveReminders(
            reminderService.getMemberReminders(memberId, "active").size());
        
        return ApiResponse.success(info);
    }

    /**
     * 获取我的用药提醒
     */
    @GetMapping("/reminders")
    public ApiResponse<List<?>> getMyReminders(@RequestHeader("X-Member-Id") Long memberId) {
        return ApiResponse.success(reminderService.getMemberReminders(memberId, "active"));
    }

    /**
     * 获取我的慢病记录
     */
    @GetMapping("/diseases")
    public ApiResponse<List<?>> getMyDiseases(@RequestHeader("X-Member-Id") Long memberId) {
        return ApiResponse.success(chronicDiseaseService.getMemberDiseases(memberId));
    }

    /**
     * 获取用药建议
     */
    @GetMapping("/advice")
    public ApiResponse<List<String>> getMedicationAdvice(@RequestHeader("X-Member-Id") Long memberId) {
        return ApiResponse.success(healthProfileService.getMedicationAdvice(memberId));
    }

    private String generateMobileToken(Long memberId) {
        // 简化实现，实际应使用JWT
        return "MOBILE_" + memberId + "_" + System.currentTimeMillis();
    }

    @Data
    public static class WxLoginRequest {
        private String code;
        private String openid;
        private String phone;
        private String nickname;
    }

    @Data
    public static class MemberLoginResult {
        private Long memberId;
        private String memberName;
        private String phone;
        private Boolean bound;
        private String token;
    }

    @Data
    public static class MemberInfo {
        private Long memberId;
        private String name;
        private String phone;
        private Integer points;
        private java.math.BigDecimal totalAmount;
        private String[] healthTags;
        private Integer activeReminders;
    }
}
