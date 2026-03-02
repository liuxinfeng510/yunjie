package com.yf.controller;

import com.yf.service.WechatAuthService;
import com.yf.vo.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 微信认证控制器
 */
@RestController
@RequestMapping("/wechat")
@RequiredArgsConstructor
public class WechatAuthController {

    private final WechatAuthService wechatAuthService;

    /**
     * 小程序登录
     */
    @PostMapping("/miniapp/login")
    public ApiResponse<WechatAuthService.WechatLoginResult> miniappLogin(@RequestBody LoginRequest req) {
        return ApiResponse.success(wechatAuthService.miniappLogin(req.getCode()));
    }

    /**
     * 绑定会员账号
     */
    @PostMapping("/bind")
    public ApiResponse<WechatAuthService.WechatLoginResult> bindMember(@RequestBody BindRequest req) {
        return ApiResponse.success(wechatAuthService.bindMember(
                req.getOpenId(), req.getPhone(), req.getVerifyCode()));
    }

    /**
     * 解绑会员
     */
    @PostMapping("/unbind")
    public ApiResponse<Void> unbind(@RequestBody UnbindRequest req) {
        wechatAuthService.unbind(req.getOpenId());
        return ApiResponse.success(null);
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/userinfo")
    public ApiResponse<Void> updateUserInfo(@RequestBody UserInfoRequest req) {
        wechatAuthService.updateUserInfo(req.getOpenId(), req.getNickname(), req.getAvatarUrl());
        return ApiResponse.success(null);
    }

    @Data
    public static class LoginRequest {
        private String code;
    }

    @Data
    public static class BindRequest {
        private String openId;
        private String phone;
        private String verifyCode;
    }

    @Data
    public static class UnbindRequest {
        private String openId;
    }

    @Data
    public static class UserInfoRequest {
        private String openId;
        private String nickname;
        private String avatarUrl;
    }
}
