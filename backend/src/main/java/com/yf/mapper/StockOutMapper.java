package com.yf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yf.entity.StockOut;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库单Mapper
 */
@Mapper
public interface StockOutMapper extends BaseMapper<StockOut> {
}
