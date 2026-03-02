package com.yf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yf.entity.DrugCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品分类Mapper接口
 */
@Mapper
public interface DrugCategoryMapper extends BaseMapper<DrugCategory> {
}
