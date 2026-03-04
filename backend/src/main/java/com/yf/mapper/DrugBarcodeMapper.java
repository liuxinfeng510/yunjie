package com.yf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yf.entity.DrugBarcode;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 药品条形码Mapper
 */
@Mapper
public interface DrugBarcodeMapper extends BaseMapper<DrugBarcode> {
    
}
