package com.yf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yf.entity.Member;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员Mapper
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
