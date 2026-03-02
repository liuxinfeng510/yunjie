-- =============================================
-- V4: 设备管理增强 & AI模块基础表
-- =============================================

-- 电子秤设备表增加校准信息字段
ALTER TABLE scale_device
    ADD COLUMN model VARCHAR(50) COMMENT '设备型号' AFTER device_name,
    ADD COLUMN serial_no VARCHAR(100) COMMENT '设备序列号' AFTER model,
    ADD COLUMN calibration_date DATE COMMENT '上次校准日期' AFTER last_heartbeat,
    ADD COLUMN calibration_due_date DATE COMMENT '下次校准截止日期' AFTER calibration_date;

-- 称重记录表增加处方关联
ALTER TABLE weighing_log
    ADD COLUMN prescription_id BIGINT COMMENT '关联处方ID' AFTER related_order_id,
    ADD COLUMN prescription_item_id BIGINT COMMENT '关联处方明细ID' AFTER prescription_id;

-- AI识别记录表
CREATE TABLE IF NOT EXISTS ai_recognition_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    recognition_type VARCHAR(30) NOT NULL COMMENT 'herb_identify/ocr/sales_assist/vision_checkout',
    input_type VARCHAR(20) COMMENT 'image/text',
    input_data TEXT COMMENT '输入内容（图片URL或文本）',
    result_data JSON COMMENT 'AI返回结果JSON',
    confidence DECIMAL(5,2) COMMENT '置信度',
    model_name VARCHAR(50) COMMENT '使用的AI模型',
    token_usage INT COMMENT 'Token消耗量',
    duration BIGINT COMMENT '响应耗时(ms)',
    operator_id BIGINT,
    related_type VARCHAR(30) COMMENT '关联业务类型',
    related_id BIGINT COMMENT '关联业务ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant_type (tenant_id, recognition_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI识别记录表';

-- 摄像头设备表
CREATE TABLE IF NOT EXISTS camera_device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    device_name VARCHAR(50),
    device_type VARCHAR(30) COMMENT 'usb/ip_camera/simulated',
    resolution VARCHAR(20) COMMENT '分辨率如1920x1080',
    connection_config VARCHAR(200) COMMENT '连接配置JSON',
    purpose VARCHAR(30) COMMENT 'herb_identify/vision_checkout/monitoring',
    status VARCHAR(20) DEFAULT 'offline',
    last_heartbeat DATETIME,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摄像头设备表';
