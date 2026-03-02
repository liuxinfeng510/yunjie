package com.yf.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Member;
import com.yf.entity.MemberPointsLog;
import com.yf.exception.BusinessException;
import com.yf.mapper.MemberMapper;
import com.yf.mapper.MemberPointsLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员服务
 */
@Service
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberMapper memberMapper;
    private final MemberPointsLogMapper memberPointsLogMapper;
    
    /**
     * 分页查询会员
     */
    public Page<Member> page(String name, String phone, int pageNum, int pageSize) {
        Page<Member> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(name)) {
            wrapper.like(Member::getName, name);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(Member::getPhone, phone);
        }
        
        wrapper.orderByDesc(Member::getCreatedAt);
        return memberMapper.selectPage(page, wrapper);
    }
    
    /**
     * 根据ID获取会员
     */
    public Member getById(Long id) {
        return memberMapper.selectById(id);
    }
    
    /**
     * 创建会员
     */
    @Transactional(rollbackFor = Exception.class)
    public Member create(Member member) {
        // 生成会员编号
        String memberNo = "M" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        member.setMemberNo(memberNo);
        member.setPoints(0);
        member.setStatus("正常");
        
        memberMapper.insert(member);
        return member;
    }
    
    /**
     * 更新会员
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Member member) {
        memberMapper.updateById(member);
    }
    
    /**
     * 增加积分
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long memberId, Integer points, String type, Long relatedOrderId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }
        
        Integer newPoints = member.getPoints() + points;
        member.setPoints(newPoints);
        memberMapper.updateById(member);
        
        // 记录积分日志
        MemberPointsLog log = new MemberPointsLog();
        log.setMemberId(memberId);
        log.setPoints(points);
        log.setType(type);
        log.setRelatedOrderId(relatedOrderId);
        log.setBalance(newPoints);
        memberPointsLogMapper.insert(log);
    }
    
    /**
     * 扣减积分
     */
    @Transactional(rollbackFor = Exception.class)
    public void deductPoints(Long memberId, Integer points, String type, Long relatedOrderId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }
        if (member.getPoints() < points) {
            throw new BusinessException("积分余额不足");
        }
        
        Integer newPoints = member.getPoints() - points;
        member.setPoints(newPoints);
        memberMapper.updateById(member);
        
        // 记录积分日志
        MemberPointsLog log = new MemberPointsLog();
        log.setMemberId(memberId);
        log.setPoints(-points);
        log.setType(type);
        log.setRelatedOrderId(relatedOrderId);
        log.setBalance(newPoints);
        memberPointsLogMapper.insert(log);
    }
    
    /**
     * 获取会员积分日志
     */
    public Page<MemberPointsLog> getPointsLogs(Long memberId, int pageNum, int pageSize) {
        Page<MemberPointsLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MemberPointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberPointsLog::getMemberId, memberId)
               .orderByDesc(MemberPointsLog::getCreatedAt);
        return memberPointsLogMapper.selectPage(page, wrapper);
    }
}
