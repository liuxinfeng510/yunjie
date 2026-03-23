-- =============================================
-- V49: 创建缺失的数据库表
-- 修复 out_of_stock_request、drug_combination、
-- staff_training、wechat_user 表不存在导致的500错误
-- =============================================

-- 1. 缺货登记表
CREATE TABLE IF NOT EXISTS out_of_stock_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    store_id BIGINT COMMENT '门店ID',
    member_id BIGINT COMMENT '会员ID',
    member_name VARCHAR(50) COMMENT '会员姓名',
    phone VARCHAR(20) COMMENT '联系电话',
    drug_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    specification VARCHAR(100) COMMENT '规格',
    manufacturer VARCHAR(200) COMMENT '生产厂家',
    quantity INT COMMENT '需求数量',
    remark VARCHAR(500) COMMENT '需求原因/备注',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待处理, processing-处理中, resolved-已解决, cancelled-已取消',
    handler_id BIGINT COMMENT '处理人ID',
    handler_name VARCHAR(50) COMMENT '处理人姓名',
    handle_time DATETIME COMMENT '处理时间',
    handle_result VARCHAR(500) COMMENT '处理结果',
    notified TINYINT(1) DEFAULT 0 COMMENT '是否已通知客户',
    notify_time DATETIME COMMENT '通知时间',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_store (store_id),
    INDEX idx_status (status),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缺货登记表';

-- 2. 联合用药推荐表（新版，基于药品ID关联）
CREATE TABLE IF NOT EXISTS drug_combination (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    primary_drug_id BIGINT COMMENT '主药品ID',
    primary_drug_name VARCHAR(200) COMMENT '主商品名称',
    recommend_drug_id BIGINT COMMENT '推荐药品ID',
    recommend_drug_name VARCHAR(200) COMMENT '推荐商品名称',
    recommend_type VARCHAR(30) COMMENT '推荐类型：enhance-增效, symptom-对症, prevent-预防副作用',
    indication VARCHAR(500) COMMENT '适应症/场景',
    reason VARCHAR(500) COMMENT '推荐理由',
    weight INT DEFAULT 0 COMMENT '推荐权重（越高越优先）',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active-启用, inactive-停用',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_primary_drug (primary_drug_id),
    INDEX idx_recommend_drug (recommend_drug_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联合用药推荐表（药品关联）';

-- 3. 员工培训记录表
CREATE TABLE IF NOT EXISTS staff_training (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    store_id BIGINT COMMENT '门店ID',
    title VARCHAR(200) NOT NULL COMMENT '培训主题',
    training_type VARCHAR(30) COMMENT '培训类型：gsp/drug_knowledge/service/safety/other',
    content TEXT COMMENT '培训内容',
    training_date DATE COMMENT '培训日期',
    duration INT COMMENT '培训时长（小时）',
    trainer VARCHAR(50) COMMENT '讲师',
    location VARCHAR(200) COMMENT '培训地点',
    attendee_ids TEXT COMMENT '参训人员ID列表（JSON）',
    attendee_names TEXT COMMENT '参训人员姓名列表（JSON）',
    attendee_count INT COMMENT '参训人数',
    materials VARCHAR(500) COMMENT '培训资料URL',
    assessment_type VARCHAR(20) COMMENT '考核方式：exam/practice/none',
    assessment_results TEXT COMMENT '考核结果JSON',
    sign_in_image VARCHAR(500) COMMENT '签到表图片',
    images TEXT COMMENT '培训照片',
    status VARCHAR(20) DEFAULT 'planned' COMMENT '状态：planned/completed/cancelled',
    remark VARCHAR(500) COMMENT '备注',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_store (store_id),
    INDEX idx_training_date (training_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工培训记录表';

-- 4. 微信用户绑定表
CREATE TABLE IF NOT EXISTS wechat_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    open_id VARCHAR(64) NOT NULL COMMENT '微信openId',
    union_id VARCHAR(64) COMMENT '微信unionId',
    member_id BIGINT COMMENT '会员ID',
    staff_id BIGINT COMMENT '员工ID',
    user_type VARCHAR(20) COMMENT '用户类型：member-会员, staff-员工',
    nickname VARCHAR(100) COMMENT '微信昵称',
    avatar_url VARCHAR(500) COMMENT '微信头像URL',
    phone VARCHAR(20) COMMENT '手机号',
    bind_status VARCHAR(20) DEFAULT 'unbound' COMMENT '绑定状态：bound-已绑定, unbound-未绑定',
    bind_time DATETIME COMMENT '绑定时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    session_key VARCHAR(200) COMMENT '小程序session_key',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    UNIQUE KEY uk_open_id (open_id, deleted),
    INDEX idx_member (member_id),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信用户绑定表';
