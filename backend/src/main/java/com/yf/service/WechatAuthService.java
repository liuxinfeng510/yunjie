package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yf.entity.Member;
import com.yf.entity.WechatUser;
import com.yf.mapper.MemberMapper;
import com.yf.mapper.WechatUserMapper;
import com.yf.config.security.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 微信认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatAuthService {

    private final WechatUserMapper wechatUserMapper;
    private final MemberMapper memberMapper;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${wechat.miniapp.appid:}")
    private String appId;

    @Value("${wechat.miniapp.secret:}")
    private String appSecret;

    /**
     * 小程序登录 - 通过code换取openId
     */
    @Transactional
    public WechatLoginResult miniappLogin(String code) {
        // 调用微信接口获取openId和session_key
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code);
        
        Map<String, Object> response;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
            response = resp;
        } catch (Exception e) {
            log.error("微信登录接口调用失败", e);
            throw new RuntimeException("微信登录失败");
        }
        
        if (response == null || response.containsKey("errcode")) {
            String errMsg = response != null ? (String) response.get("errmsg") : "未知错误";
            log.error("微信登录失败: {}", errMsg);
            throw new RuntimeException("微信登录失败: " + errMsg);
        }
        
        String openId = (String) response.get("openid");
        String sessionKey = (String) response.get("session_key");
        String unionId = (String) response.get("unionid");
        
        // 查找或创建微信用户
        WechatUser wechatUser = findByOpenId(openId);
        if (wechatUser == null) {
            wechatUser = new WechatUser();
            wechatUser.setOpenId(openId);
            wechatUser.setUnionId(unionId);
            wechatUser.setSessionKey(sessionKey);
            wechatUser.setUserType("member");
            wechatUser.setBindStatus("unbound");
            wechatUserMapper.insert(wechatUser);
            log.info("创建新微信用户: openId={}", openId);
        } else {
            wechatUser.setSessionKey(sessionKey);
            wechatUser.setLastLoginTime(LocalDateTime.now());
            wechatUserMapper.updateById(wechatUser);
        }
        
        WechatLoginResult result = new WechatLoginResult();
        result.setWechatUser(wechatUser);
        result.setBound("bound".equals(wechatUser.getBindStatus()));
        
        // 如果已绑定会员，生成JWT token
        if (result.isBound() && wechatUser.getMemberId() != null) {
            Member member = memberMapper.selectById(wechatUser.getMemberId());
            if (member != null) {
                result.setMember(member);
                result.setToken(jwtUtil.generateToken(member.getId(), member.getPhone(), member.getTenantId()));
            }
        }
        
        return result;
    }

    /**
     * 绑定会员账号（通过手机号验证）
     */
    @Transactional
    public WechatLoginResult bindMember(String openId, String phone, String verifyCode) {
        // TODO: 实际项目中应验证短信验证码
        
        WechatUser wechatUser = findByOpenId(openId);
        if (wechatUser == null) {
            throw new RuntimeException("微信用户不存在");
        }
        
        // 通过手机号查找会员
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getPhone, phone)
               .last("LIMIT 1");
        Member member = memberMapper.selectOne(wrapper);
        
        if (member == null) {
            // 自动创建会员
            member = new Member();
            member.setPhone(phone);
            member.setName("微信用户");
            member.setStatus("正常");
            member.setPoints(0);
            memberMapper.insert(member);
            log.info("自动创建会员: phone={}", phone);
        }
        
        // 绑定
        wechatUser.setMemberId(member.getId());
        wechatUser.setPhone(phone);
        wechatUser.setBindStatus("bound");
        wechatUser.setBindTime(LocalDateTime.now());
        wechatUserMapper.updateById(wechatUser);
        
        WechatLoginResult result = new WechatLoginResult();
        result.setWechatUser(wechatUser);
        result.setMember(member);
        result.setBound(true);
        result.setToken(jwtUtil.generateToken(member.getId(), member.getPhone(), member.getTenantId()));
        
        log.info("微信用户绑定成功: openId={}, memberId={}", openId, member.getId());
        return result;
    }

    /**
     * 解绑会员
     */
    @Transactional
    public void unbind(String openId) {
        WechatUser wechatUser = findByOpenId(openId);
        if (wechatUser != null) {
            wechatUser.setMemberId(null);
            wechatUser.setBindStatus("unbound");
            wechatUser.setBindTime(null);
            wechatUserMapper.updateById(wechatUser);
            log.info("微信用户已解绑: openId={}", openId);
        }
    }

    /**
     * 更新微信用户信息
     */
    @Transactional
    public void updateUserInfo(String openId, String nickname, String avatarUrl) {
        WechatUser wechatUser = findByOpenId(openId);
        if (wechatUser != null) {
            wechatUser.setNickname(nickname);
            wechatUser.setAvatarUrl(avatarUrl);
            wechatUserMapper.updateById(wechatUser);
        }
    }

    /**
     * 根据openId查找微信用户
     */
    public WechatUser findByOpenId(String openId) {
        LambdaQueryWrapper<WechatUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WechatUser::getOpenId, openId);
        return wechatUserMapper.selectOne(wrapper);
    }

    /**
     * 根据会员ID查找微信用户
     */
    public WechatUser findByMemberId(Long memberId) {
        LambdaQueryWrapper<WechatUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WechatUser::getMemberId, memberId)
               .last("LIMIT 1");
        return wechatUserMapper.selectOne(wrapper);
    }

    @Data
    public static class WechatLoginResult {
        private WechatUser wechatUser;
        private Member member;
        private boolean bound;
        private String token;
    }
}
