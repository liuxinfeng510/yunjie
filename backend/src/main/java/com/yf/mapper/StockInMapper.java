package com.yf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yf.entity.StockIn;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库单Mapper
 */
@Mapper
public interface StockInMapper extends BaseMapper<StockIn> {
}
