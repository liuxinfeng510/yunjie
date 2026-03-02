package com.yf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yf.entity.StockInDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库单明细Mapper
 */
@Mapper
public interface StockInDetailMapper extends BaseMapper<StockInDetail> {
}
