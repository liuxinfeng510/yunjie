package com.yf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 缺药登记实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("out_of_stock_request")
public class OutOfStockRequest extends BaseEntity {
    
    /**
     * 门店ID
     */
    private Long storeId;
    
    /**
     * 会员ID（可选，非会员也可登记）
     */
    private Long memberId;
    
    /**
     * 会员姓名
     */
    private String memberName;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 药品名称
     */
    private String drugName;
    
    /**
     * 规格
     */
    private String specification;
    
    /**
     * 生产厂家
     */
    private String manufacturer;
    
    /**
     * 需求数量
     */
    private Integer quantity;
    
    /**
     * 需求原因/备注
     */
    private String remark;
    
    /**
     * 状态：pending-待处理, processing-处理中, resolved-已解决, cancelled-已取消
     */
    private String status;
    
    /**
     * 处理人ID
     */
    private Long handlerId;
    
    /**
     * 处理人姓名
     */
    private String handlerName;
    
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
    
    /**
     * 处理结果
     */
    private String handleResult;
    
    /**
     * 是否已通知客户
     */
    private Boolean notified;
    
    /**
     * 通知时间
     */
    private LocalDateTime notifyTime;
}
