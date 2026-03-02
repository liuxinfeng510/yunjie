package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门店表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store")
public class Store extends BaseEntity {
    /** 门店名称 */
    private String name;
    /** 门店编码 */
    private String code;
    /** 门店类型: headquarter / store */
    private String type;
    /** 省 */
    private String province;
    /** 市 */
    private String city;
    /** 区 */
    private String district;
    /** 详细地址 */
    private String address;
    /** 联系电话 */
    private String phone;
    /** 经度 */
    private Double longitude;
    /** 纬度 */
    private Double latitude;
    /** 营业执照号 */
    private String licenseNo;
    /** GSP证书号 */
    private String gspCertNo;
    /** 是否有独立仓库 */
    private Boolean hasWarehouse;
    /** 状态: active / disabled */
    private String status;
}
