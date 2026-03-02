package com.yf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yf.entity.Drug;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品Mapper接口
 */
@Mapper
public interface DrugMapper extends BaseMapper<Drug> {
}
