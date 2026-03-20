package com.yf.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Member;
import com.yf.entity.MemberLevel;
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
    private final MemberLevelService memberLevelService;
    
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
        // 自动填充拼音
        fillPinyin(member);
        // 未指定等级时自动分配默认等级
        if (member.getLevelId() == null) {
            MemberLevel defaultLevel = memberLevelService.getDefaultLevel();
            if (defaultLevel != null) {
                member.setLevelId(defaultLevel.getId());
            }
        }
        memberMapper.insert(member);
        return member;
    }
    
    /**
     * 更新会员
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Member member) {
        // 自动填充拼音
        fillPinyin(member);
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

    /**
     * 获取所有会员列表(用于下拉选择)
     */
    public List<Member> list() {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getStatus, "正常")
               .orderByDesc(Member::getCreatedAt);
        return memberMapper.selectList(wrapper);
    }
    
    /**
     * 搜索会员(用于自动补全)
     */
    public List<Member> search(String keyword) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getStatus, "正常");
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Member::getName, keyword)
                    .or().like(Member::getPinyin, keyword)
                    .or().like(Member::getPinyinShort, keyword)
                    .or().like(Member::getPhone, keyword));
        }
        wrapper.orderByDesc(Member::getCreatedAt)
               .last("LIMIT 20");
        return memberMapper.selectList(wrapper);
    }

    /**
     * 根据微信OpenID查找会员
     */
    public Member findByWechatOpenid(String openid) {
        if (!StringUtils.hasText(openid)) {
            return null;
        }
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getWechatOpenid, openid);
        return memberMapper.selectOne(wrapper);
    }

    /**
     * 创建会员（移动端使用）
     */
    @Transactional(rollbackFor = Exception.class)
    public Member createMember(Member member) {
        String memberNo = "M" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        member.setMemberNo(memberNo);
        if (member.getPoints() == null) {
            member.setPoints(0);
        }
        if (!StringUtils.hasText(member.getStatus())) {
            member.setStatus("正常");
        }
        // 自动填充拼音
        fillPinyin(member);
        if (member.getLevelId() == null) {
            MemberLevel defaultLevel = memberLevelService.getDefaultLevel();
            if (defaultLevel != null) {
                member.setLevelId(defaultLevel.getId());
            }
        }
        memberMapper.insert(member);
        return member;
    }

    /**
     * 批量生成所有会员的拼音（用于修复历史数据）
     */
    @Transactional(rollbackFor = Exception.class)
    public int generateAllPinyin() {
        List<Member> allMembers = memberMapper.selectList(null);
        int count = 0;
        for (Member member : allMembers) {
            if (StringUtils.hasText(member.getName())) {
                String pinyin = PinyinUtil.getPinyin(member.getName(), "");
                String pinyinShort = PinyinUtil.getFirstLetter(member.getName(), "");
                if (!pinyin.equals(member.getPinyin()) || !pinyinShort.equals(member.getPinyinShort())) {
                    Member update = new Member();
                    update.setId(member.getId());
                    update.setPinyin(pinyin);
                    update.setPinyinShort(pinyinShort);
                    memberMapper.updateById(update);
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 自动填充拼音
     */
    private void fillPinyin(Member member) {
        if (StringUtils.hasText(member.getName())) {
            member.setPinyin(PinyinUtil.getPinyin(member.getName(), ""));
            member.setPinyinShort(PinyinUtil.getFirstLetter(member.getName(), ""));
        }
    }
}
