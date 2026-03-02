package com.yf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yf.entity.RefundOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款订单Mapper
 */
@Mapper
public interface RefundOrderMapper extends BaseMapper<RefundOrder> {
}
