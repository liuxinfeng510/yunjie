package com.yf.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 智能入库解析结果
 */
@Data
public class SmartStockInResult {

    private boolean success;
    private String errorMessage;
    /** 按(供应商+日期)分组后的入库单列表 */
    private List<SmartStockInOrder> orders = new ArrayList<>();
    private int totalDrugsCreated;
    private int totalSuppliersCreated;

    @Data
    public static class SmartStockInOrder {
        private String groupKey;
        // 供应商
        private Long supplierId;
        private String supplierName;
        private String supplierCreditCode;
        private String supplierPhone;
        private boolean supplierNewCreated;
        // 单据日期
        private String invoiceDate;
        // 药品明细
        private List<SmartDetailItem> details = new ArrayList<>();
        // 不完整字段警告
        private List<IncompleteField> incompleteFields = new ArrayList<>();
        private int drugsCreated;
    }

    @Data
    public static class SmartDetailItem {
        private int rowIndex;
        private Long drugId;
        private String genericName;
        private String tradeName;
        private String specification;
        private String manufacturer;
        private String approvalNo;
        private String dosageForm;
        private String barcode;
        private String marketingAuthHolder;
        private String batchNo;
        private String produceDate;
        private String expireDate;
        private Integer quantity;
        private String unit;
        private BigDecimal purchasePrice;
        private BigDecimal amount;
        private boolean drugNewCreated;
        private List<String> missingFields = new ArrayList<>();
    }

    @Data
    public static class IncompleteField {
        private String scope;       // "SUPPLIER" / "DRUG"
        private int rowIndex;       // DRUG时为行号, SUPPLIER为-1
        private String fieldName;
        private String fieldLabel;  // 中文名
        private String severity;    // "ERROR" / "WARNING"
    }

    public static SmartStockInResult fail(String errorMessage) {
        SmartStockInResult r = new SmartStockInResult();
        r.setSuccess(false);
        r.setErrorMessage(errorMessage);
        return r;
    }
}
