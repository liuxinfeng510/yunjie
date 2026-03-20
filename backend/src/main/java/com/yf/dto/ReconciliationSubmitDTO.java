package com.yf.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 对账提交DTO
 */
@Data
public class ReconciliationSubmitDTO {

    /** 门店ID */
    private Long storeId;

    /** 收银员ID（null表示全部） */
    private Long cashierId;

    /** 对账日期（yyyy-MM-dd） */
    private String reconcileDate;

    /** 备注 */
    private String remark;

    /** 各支付方式的实际金额明细 */
    private List<DetailItem> details;

    @Data
    public static class DetailItem {
        /** 支付方式 */
        private String payMethod;
        /** 实际金额 */
        private BigDecimal actualAmount;
        /** 备注 */
        private String remark;
    }
}
