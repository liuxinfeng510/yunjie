package com.yf.dto;

import com.yf.entity.Drug;
import com.yf.entity.DrugBarcode;
import lombok.Data;

import java.util.List;

/**
 * 药品请求DTO（包含条形码列表）
 */
@Data
public class DrugRequest {

    /**
     * 药品基本信息
     */
    private Drug drug;

    /**
     * 条形码列表
     */
    private List<DrugBarcode> barcodes;
}
