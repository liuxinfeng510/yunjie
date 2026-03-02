-- =====================================================
-- YF 药房管理系统 - 核心数据库表
-- V1: 基础表 + 药品 + 中药 + 库存 + 销售 + 会员 + GSP
-- =====================================================

-- -------------------------------------------
-- 一、系统基础表
-- -------------------------------------------

CREATE TABLE sys_tenant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(100) NOT NULL COMMENT '租户名称',
    credit_code VARCHAR(50) COMMENT '统一社会信用代码',
    contact_name VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    business_mode VARCHAR(20) NOT NULL DEFAULT 'single_store' COMMENT '经营模式: single_store/chain_store',
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(200) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    store_id BIGINT COMMENT '所属门店ID',
    role VARCHAR(30) NOT NULL DEFAULT 'clerk' COMMENT 'admin/manager/pharmacist/clerk',
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    UNIQUE KEY uk_username (username, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE store (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL COMMENT '门店名称',
    code VARCHAR(30) COMMENT '门店编码',
    type VARCHAR(20) DEFAULT 'store' COMMENT 'headquarter/store',
    province VARCHAR(30),
    city VARCHAR(30),
    district VARCHAR(30),
    address VARCHAR(200),
    phone VARCHAR(20),
    longitude DOUBLE,
    latitude DOUBLE,
    license_no VARCHAR(50) COMMENT '营业执照号',
    gsp_cert_no VARCHAR(50) COMMENT 'GSP证书号',
    has_warehouse TINYINT(1) DEFAULT 0 COMMENT '是否有独立仓库',
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店表';

-- -------------------------------------------
-- 二、药品管理表
-- -------------------------------------------

CREATE TABLE drug_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品分类表';

CREATE TABLE drug (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    category_id BIGINT COMMENT '分类ID',
    drug_code VARCHAR(30) COMMENT '药品编码',
    barcode VARCHAR(30) COMMENT '条形码',
    generic_name VARCHAR(100) NOT NULL COMMENT '通用名',
    trade_name VARCHAR(100) COMMENT '商品名',
    pinyin VARCHAR(100) COMMENT '拼音',
    pinyin_short VARCHAR(30) COMMENT '拼音首字母',
    approval_no VARCHAR(50) COMMENT '批准文号',
    dosage_form VARCHAR(30) COMMENT '剂型',
    specification VARCHAR(100) COMMENT '规格',
    unit VARCHAR(20) COMMENT '单位',
    manufacturer VARCHAR(200) COMMENT '生产企业',
    otc_type VARCHAR(20) COMMENT 'OTC甲/OTC乙/处方药',
    storage_condition VARCHAR(50) COMMENT '储存条件',
    valid_period INT COMMENT '有效期(月)',
    purchase_price DECIMAL(10,2) COMMENT '采购价',
    retail_price DECIMAL(10,2) COMMENT '零售价',
    member_price DECIMAL(10,2) COMMENT '会员价',
    is_antibiotics TINYINT(1) DEFAULT 0,
    is_psychotropic TINYINT(1) DEFAULT 0,
    is_narcotic TINYINT(1) DEFAULT 0,
    medical_insurance VARCHAR(10) COMMENT '医保类型:甲/乙/丙',
    image_url VARCHAR(500) COMMENT '药品图片',
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_barcode (barcode),
    INDEX idx_generic_name (generic_name),
    INDEX idx_pinyin_short (pinyin_short),
    INDEX idx_approval_no (approval_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品表';

CREATE TABLE drug_batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    batch_no VARCHAR(50) NOT NULL COMMENT '批号',
    produce_date DATE COMMENT '生产日期',
    expire_date DATE NOT NULL COMMENT '有效期至',
    purchase_price DECIMAL(10,2) COMMENT '批次采购价',
    supplier_id BIGINT COMMENT '供应商ID',
    status VARCHAR(20) DEFAULT 'active',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_drug_batch (drug_id, batch_no),
    INDEX idx_expire (expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品批次表';

CREATE TABLE drug_trace_code (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    trace_code VARCHAR(30) NOT NULL COMMENT '追溯码',
    drug_id BIGINT NOT NULL,
    batch_id BIGINT,
    batch_no VARCHAR(50),
    produce_date DATE,
    expire_date DATE,
    supplier_id BIGINT,
    purchase_order_id BIGINT COMMENT '入库单ID',
    sale_order_id BIGINT COMMENT '销售单ID',
    status VARCHAR(20) DEFAULT 'in_stock' COMMENT 'in_stock/sold/returned/damaged',
    trace_time DATETIME,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    UNIQUE KEY uk_trace_code (trace_code, tenant_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='追溯码表';

-- -------------------------------------------
-- 三、供应商管理表
-- -------------------------------------------

CREATE TABLE supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL COMMENT '供应商名称',
    short_name VARCHAR(50) COMMENT '简称',
    credit_code VARCHAR(50) COMMENT '统一社会信用代码',
    contact_name VARCHAR(50),
    contact_phone VARCHAR(20),
    address VARCHAR(300),
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

CREATE TABLE supplier_qualification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    license_type VARCHAR(50) NOT NULL COMMENT '证照类型',
    license_no VARCHAR(100) COMMENT '证照编号',
    license_image VARCHAR(500) COMMENT '证照图片URL',
    valid_from DATE,
    valid_until DATE,
    verify_status VARCHAR(20) DEFAULT 'pending',
    approval_status VARCHAR(20) DEFAULT 'pending',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商资质表';

-- -------------------------------------------
-- 四、库存管理表
-- -------------------------------------------

CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL COMMENT '门店ID',
    drug_id BIGINT NOT NULL COMMENT '药品ID',
    batch_id BIGINT COMMENT '批次ID',
    batch_no VARCHAR(50),
    quantity DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '库存数量',
    unit VARCHAR(20),
    cost_price DECIMAL(10,2) COMMENT '成本价',
    location VARCHAR(50) COMMENT '货位',
    safe_stock DECIMAL(12,2) DEFAULT 0 COMMENT '安全库存',
    max_stock DECIMAL(12,2) DEFAULT 0 COMMENT '最大库存',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_store_drug (store_id, drug_id),
    INDEX idx_batch (batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

CREATE TABLE stock_in (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    order_no VARCHAR(30) NOT NULL COMMENT '入库单号',
    store_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL COMMENT 'purchase/transfer/return/gift',
    supplier_id BIGINT,
    total_amount DECIMAL(12,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'draft' COMMENT 'draft/pending/approved/completed',
    remark VARCHAR(500),
    approved_by BIGINT,
    approved_at DATETIME,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    UNIQUE KEY uk_order_no (order_no, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单';

CREATE TABLE stock_in_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    stock_in_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    batch_no VARCHAR(50),
    produce_date DATE,
    expire_date DATE,
    quantity DECIMAL(12,2) NOT NULL,
    unit VARCHAR(20),
    purchase_price DECIMAL(10,2),
    amount DECIMAL(12,2),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单明细';

CREATE TABLE stock_out (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    order_no VARCHAR(30) NOT NULL,
    store_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL COMMENT 'sale/transfer/damage/return_supplier',
    target_store_id BIGINT COMMENT '调拨目标门店',
    total_amount DECIMAL(12,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'draft',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    UNIQUE KEY uk_order_no (order_no, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单';

CREATE TABLE stock_out_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    stock_out_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    batch_id BIGINT,
    batch_no VARCHAR(50),
    quantity DECIMAL(12,2) NOT NULL,
    unit VARCHAR(20),
    cost_price DECIMAL(10,2),
    amount DECIMAL(12,2),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单明细';

CREATE TABLE stock_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    order_no VARCHAR(30) NOT NULL,
    store_id BIGINT NOT NULL,
    type VARCHAR(20) COMMENT 'full/partial/random',
    status VARCHAR(20) DEFAULT 'draft',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单';

CREATE TABLE stock_check_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    stock_check_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    batch_id BIGINT,
    system_quantity DECIMAL(12,2) COMMENT '系统库存',
    actual_quantity DECIMAL(12,2) COMMENT '实际库存',
    diff_quantity DECIMAL(12,2) COMMENT '差异数量',
    diff_reason VARCHAR(200),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单明细';

-- -------------------------------------------
-- 五、销售管理表
-- -------------------------------------------

CREATE TABLE sale_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    order_no VARCHAR(30) NOT NULL,
    store_id BIGINT NOT NULL,
    member_id BIGINT COMMENT '会员ID',
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    pay_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    pay_method VARCHAR(30) COMMENT 'cash/wechat/alipay/card/medical_insurance/combined',
    points_earned INT DEFAULT 0,
    points_used INT DEFAULT 0,
    cashier_id BIGINT COMMENT '收银员',
    pharmacist_id BIGINT COMMENT '审核药师',
    status VARCHAR(20) DEFAULT 'completed' COMMENT 'completed/refunded/partial_refunded',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    UNIQUE KEY uk_order_no (order_no, tenant_id),
    INDEX idx_member (member_id),
    INDEX idx_store_date (store_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单';

CREATE TABLE sale_order_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    sale_order_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    drug_name VARCHAR(100),
    specification VARCHAR(100),
    batch_id BIGINT,
    batch_no VARCHAR(50),
    quantity DECIMAL(12,2) NOT NULL,
    unit VARCHAR(20),
    unit_price DECIMAL(10,2) NOT NULL,
    discount DECIMAL(5,2) DEFAULT 100 COMMENT '折扣(%)',
    amount DECIMAL(12,2) NOT NULL,
    cost_price DECIMAL(10,2),
    trace_code VARCHAR(30),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单明细';

CREATE TABLE refund_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    refund_no VARCHAR(30) NOT NULL,
    sale_order_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    refund_amount DECIMAL(12,2) NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) DEFAULT 'pending',
    approved_by BIGINT,
    approved_at DATETIME,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款单';

-- -------------------------------------------
-- 六、会员管理表
-- -------------------------------------------

CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    member_no VARCHAR(30) COMMENT '会员号',
    name VARCHAR(50),
    phone VARCHAR(20) NOT NULL,
    gender VARCHAR(10),
    birthday DATE,
    id_card VARCHAR(20),
    wechat_openid VARCHAR(100),
    level_id BIGINT COMMENT '等级ID',
    points INT DEFAULT 0,
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '累计消费',
    allergy_info VARCHAR(500) COMMENT '过敏信息',
    chronic_disease VARCHAR(500) COMMENT '慢病信息',
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_phone (phone),
    INDEX idx_member_no (member_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

CREATE TABLE member_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(30) NOT NULL COMMENT '等级名称',
    min_amount DECIMAL(12,2) DEFAULT 0 COMMENT '达标金额',
    discount DECIMAL(5,2) DEFAULT 100 COMMENT '折扣(%)',
    points_rate DECIMAL(5,2) DEFAULT 1 COMMENT '积分倍率',
    sort_order INT DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

CREATE TABLE member_points_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    points INT NOT NULL COMMENT '积分变动(正:获得 负:消耗)',
    type VARCHAR(30) COMMENT 'purchase/refund/exchange/sign_in/manual',
    related_order_id BIGINT,
    balance INT COMMENT '变动后余额',
    remark VARCHAR(200),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分变动记录';

-- -------------------------------------------
-- 七、中药饮片管理表
-- -------------------------------------------

CREATE TABLE herb (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    herb_code VARCHAR(30) COMMENT '中药编码',
    name VARCHAR(50) NOT NULL COMMENT '药材名称',
    alias VARCHAR(200) COMMENT '别名',
    pinyin VARCHAR(100),
    pinyin_short VARCHAR(30),
    category VARCHAR(50) COMMENT '分类(解表药/清热药等)',
    nature VARCHAR(20) COMMENT '性: 寒/热/温/凉/平',
    flavor VARCHAR(50) COMMENT '味: 酸/苦/甘/辛/咸',
    meridian VARCHAR(100) COMMENT '归经',
    efficacy VARCHAR(500) COMMENT '功效',
    origin VARCHAR(100) COMMENT '道地产区',
    processing_method VARCHAR(100) COMMENT '炮制方法',
    dosage_min DECIMAL(6,1) COMMENT '常用量下限(g)',
    dosage_max DECIMAL(6,1) COMMENT '常用量上限(g)',
    storage VARCHAR(100) COMMENT '储存方法',
    is_toxic TINYINT(1) DEFAULT 0,
    toxic_level VARCHAR(20) COMMENT '大毒/有毒/小毒',
    is_precious TINYINT(1) DEFAULT 0,
    purchase_price DECIMAL(10,2),
    retail_price DECIMAL(10,2) COMMENT '零售价(元/g)',
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_pinyin_short (pinyin_short)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中药饮片表';

CREATE TABLE herb_cabinet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL COMMENT '药柜名称',
    row_count INT DEFAULT 5,
    column_count INT DEFAULT 10,
    location VARCHAR(100) COMMENT '摆放位置',
    status VARCHAR(20) DEFAULT 'active',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药柜表';

CREATE TABLE herb_cabinet_cell (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    cabinet_id BIGINT NOT NULL,
    row_num INT NOT NULL,
    column_num INT NOT NULL,
    label VARCHAR(20) COMMENT '斗格标签(如A1)',
    herb_id BIGINT COMMENT '中药ID',
    current_stock DECIMAL(12,2) DEFAULT 0 COMMENT '当前库存(g)',
    min_stock DECIMAL(12,2) DEFAULT 0 COMMENT '最低库存(g)',
    status VARCHAR(20) DEFAULT 'active',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_cabinet (cabinet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药柜斗格表';

CREATE TABLE herb_cabinet_fill_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    herb_id BIGINT NOT NULL,
    herb_name VARCHAR(50),
    batch_no VARCHAR(50),
    produce_date DATE,
    expire_date DATE,
    fill_weight DECIMAL(10,2) NOT NULL COMMENT '装斗重量(g)',
    source_package_id BIGINT COMMENT '来源包装ID',
    target_cabinet_id BIGINT NOT NULL,
    target_cell_id BIGINT NOT NULL,
    pre_check_result VARCHAR(20) COMMENT '装斗前检查: pass/fail',
    appearance_desc VARCHAR(500) COMMENT '外观描述',
    image_url VARCHAR(500),
    operator_id BIGINT NOT NULL,
    fill_time DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'completed',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装斗记录表';

CREATE TABLE herb_cabinet_clean_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    cabinet_id BIGINT NOT NULL,
    cell_id BIGINT NOT NULL,
    cell_location VARCHAR(20),
    original_herb_id BIGINT,
    original_herb_name VARCHAR(50),
    remaining_weight DECIMAL(10,2) COMMENT '剩余重量(g)',
    remaining_disposal VARCHAR(30) COMMENT '剩余去向: return_stock/damage/continue',
    clean_reason VARCHAR(30) COMMENT 'periodic/change_herb/abnormal/change_batch',
    clean_method VARCHAR(30) COMMENT 'dry_wipe/wet_wipe/disinfect',
    cell_status_after VARCHAR(20) COMMENT '清洁后状态',
    image_url VARCHAR(500),
    operator_id BIGINT NOT NULL,
    clean_time DATETIME NOT NULL,
    next_herb_id BIGINT COMMENT '下一个入斗药品ID',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='清斗记录表';

CREATE TABLE herb_acceptance_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    herb_id BIGINT NOT NULL,
    herb_name VARCHAR(50),
    batch_no VARCHAR(50),
    supplier_id BIGINT,
    quantity DECIMAL(12,2),
    appearance_check VARCHAR(100) COMMENT '外观检查',
    odor_check VARCHAR(100) COMMENT '气味检查',
    texture_check VARCHAR(100) COMMENT '质地检查',
    special_test VARCHAR(200) COMMENT '特殊检验',
    origin_check VARCHAR(100) COMMENT '产地核实',
    package_check VARCHAR(100) COMMENT '包装检查',
    overall_result VARCHAR(20) COMMENT 'pass/fail',
    reject_reason VARCHAR(500),
    images VARCHAR(2000) COMMENT '照片URL数组JSON',
    acceptor_id BIGINT,
    accept_time DATETIME,
    pharmacist_sign VARCHAR(50),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中药饮片验收记录表';

CREATE TABLE herb_maintenance_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    cabinet_id BIGINT,
    cell_id BIGINT,
    herb_id BIGINT,
    herb_name VARCHAR(50),
    maintenance_type VARCHAR(30) COMMENT 'pest/mold/moisture/oil_loss/discolor/fragrance_loss',
    appearance_result VARCHAR(100),
    pest_result VARCHAR(100),
    mold_result VARCHAR(100),
    odor_result VARCHAR(100),
    storage_condition VARCHAR(100),
    overall_result VARCHAR(20) COMMENT 'normal/abnormal',
    abnormal_desc VARCHAR(500),
    treatment VARCHAR(500),
    images VARCHAR(2000),
    operator_id BIGINT,
    maintenance_time DATETIME,
    next_maintenance DATE,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中药养护记录表';

CREATE TABLE herb_prescription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    prescription_no VARCHAR(30) NOT NULL,
    store_id BIGINT NOT NULL,
    member_id BIGINT,
    patient_name VARCHAR(50),
    patient_gender VARCHAR(10),
    patient_age INT,
    prescriber VARCHAR(50) COMMENT '开方医师',
    hospital VARCHAR(100),
    diagnosis VARCHAR(500),
    total_dose INT DEFAULT 1 COMMENT '剂数',
    decoction_method VARCHAR(200) COMMENT '煎服方法',
    dispenser_id BIGINT COMMENT '调配人',
    checker_id BIGINT COMMENT '复核人',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/dispensing/checked/completed',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中药处方表';

CREATE TABLE herb_prescription_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    prescription_id BIGINT NOT NULL,
    herb_id BIGINT NOT NULL,
    herb_name VARCHAR(50) NOT NULL,
    dose_per_day DECIMAL(8,1) NOT NULL COMMENT '每剂用量(g)',
    special_process VARCHAR(30) COMMENT '特殊处理: pre_decoct/post_add/wrap/separate',
    cell_location VARCHAR(20) COMMENT '斗格位置',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方明细表';

CREATE TABLE herb_dispensing_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    prescription_id BIGINT NOT NULL,
    item_index INT,
    herb_id BIGINT,
    herb_name VARCHAR(50),
    target_weight DECIMAL(10,2) COMMENT '应称重量(g)',
    actual_weight DECIMAL(10,2) COMMENT '实际重量(g)',
    weight_deviation DECIMAL(5,2) COMMENT '偏差(%)',
    vision_check_result VARCHAR(20) COMMENT '品种校验: pass/fail',
    vision_confidence DECIMAL(5,2),
    cell_location VARCHAR(20),
    dispenser_id BIGINT,
    dispense_time DATETIME,
    image_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'normal',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调配记录表';

-- -------------------------------------------
-- 八、GSP合规管理表
-- -------------------------------------------

CREATE TABLE drug_acceptance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    stock_in_id BIGINT COMMENT '关联入库单',
    store_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    drug_name VARCHAR(100),
    batch_no VARCHAR(50),
    supplier_id BIGINT,
    quantity DECIMAL(12,2),
    appearance_check VARCHAR(20) COMMENT 'pass/fail',
    package_check VARCHAR(20),
    label_check VARCHAR(20),
    expire_check VARCHAR(20),
    overall_result VARCHAR(20) COMMENT 'pass/fail',
    reject_reason VARCHAR(500),
    images VARCHAR(2000),
    acceptor_id BIGINT,
    accept_time DATETIME,
    sign_image VARCHAR(500) COMMENT '电子签名',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品验收记录';

CREATE TABLE drug_maintenance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    drug_id BIGINT,
    drug_name VARCHAR(100),
    maintenance_type VARCHAR(30) COMMENT 'routine/key/special',
    appearance_check VARCHAR(100),
    package_check VARCHAR(100),
    expire_check VARCHAR(100),
    storage_check VARCHAR(100),
    overall_result VARCHAR(20) COMMENT 'normal/abnormal',
    abnormal_desc VARCHAR(500),
    treatment VARCHAR(500),
    images VARCHAR(2000),
    operator_id BIGINT,
    maintenance_time DATETIME,
    next_maintenance DATE,
    sign_image VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品养护记录';

CREATE TABLE temperature_humidity_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    device_id VARCHAR(50),
    location VARCHAR(100) COMMENT '安装位置',
    temperature DECIMAL(5,1),
    humidity DECIMAL(5,1),
    record_time DATETIME NOT NULL,
    is_abnormal TINYINT(1) DEFAULT 0,
    alarm_sent TINYINT(1) DEFAULT 0,
    handle_status VARCHAR(20),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_store_time (store_id, record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='温湿度记录表';

-- -------------------------------------------
-- 九、智能设备表
-- -------------------------------------------

CREATE TABLE scale_device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    device_name VARCHAR(50),
    connection_type VARCHAR(20) COMMENT 'serial/usb/bluetooth',
    port_config VARCHAR(100),
    precision_g DECIMAL(4,2) COMMENT '精度(g)',
    max_weight DECIMAL(10,2) COMMENT '最大量程(g)',
    status VARCHAR(20) DEFAULT 'offline',
    last_heartbeat DATETIME,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子秤设备表';

CREATE TABLE weighing_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    device_id BIGINT,
    herb_id BIGINT,
    herb_name VARCHAR(50),
    weight DECIMAL(10,2) NOT NULL COMMENT '称重值(g)',
    operation_type VARCHAR(20) COMMENT 'stock_in/stock_out/dispense/check',
    recognition_method VARCHAR(20) COMMENT 'ai/manual',
    confidence DECIMAL(5,2),
    operator_id BIGINT,
    related_order_id BIGINT,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='称重记录表';

-- -------------------------------------------
-- 十、知识库表（全局共享，不按租户隔离）
-- -------------------------------------------

CREATE TABLE drug_knowledge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    drug_code VARCHAR(30),
    generic_name VARCHAR(100) NOT NULL,
    trade_name VARCHAR(100),
    english_name VARCHAR(200),
    pinyin VARCHAR(100),
    pinyin_short VARCHAR(30),
    approval_no VARCHAR(50),
    dosage_form VARCHAR(30),
    specification VARCHAR(100),
    manufacturer VARCHAR(200),
    category VARCHAR(50),
    otc_type VARCHAR(20),
    storage_condition VARCHAR(50),
    valid_period INT,
    indication TEXT COMMENT '适应症',
    dosage TEXT COMMENT '用法用量',
    adverse_reaction TEXT,
    contraindication TEXT COMMENT '禁忌',
    precaution TEXT COMMENT '注意事项',
    interaction TEXT COMMENT '药物相互作用',
    pregnancy_category VARCHAR(10),
    is_antibiotics TINYINT(1) DEFAULT 0,
    is_psychotropic TINYINT(1) DEFAULT 0,
    is_narcotic TINYINT(1) DEFAULT 0,
    medical_insurance VARCHAR(10),
    reference_price DECIMAL(10,2),
    barcode VARCHAR(30),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品知识库';

CREATE TABLE herb_knowledge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    herb_code VARCHAR(30),
    herb_name VARCHAR(50) NOT NULL,
    alias VARCHAR(200),
    pinyin VARCHAR(100),
    latin_name VARCHAR(200),
    source VARCHAR(100) COMMENT '来源',
    origin VARCHAR(200) COMMENT '道地产区',
    nature VARCHAR(20) COMMENT '性',
    flavor VARCHAR(50) COMMENT '味',
    meridian VARCHAR(200) COMMENT '归经',
    efficacy TEXT COMMENT '功效',
    indication TEXT COMMENT '主治',
    dosage_usage VARCHAR(200) COMMENT '用法',
    dosage_min DECIMAL(6,1),
    dosage_max DECIMAL(6,1),
    dosage_note VARCHAR(200),
    processing_method VARCHAR(200),
    contraindication TEXT,
    precaution TEXT,
    incompatibility TEXT COMMENT '配伍禁忌',
    storage VARCHAR(200),
    identification TEXT COMMENT '性状鉴别要点',
    is_toxic TINYINT(1) DEFAULT 0,
    toxic_level VARCHAR(20),
    is_precious TINYINT(1) DEFAULT 0,
    medical_insurance VARCHAR(10),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中药知识库';

CREATE TABLE herb_incompatibility (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    type VARCHAR(20) NOT NULL COMMENT '18_oppose/19_fear/pregnancy',
    herb_a VARCHAR(50) NOT NULL,
    herb_b VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    severity VARCHAR(20) COMMENT 'forbidden/caution',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配伍禁忌表';

CREATE TABLE combined_medication (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    symptom_name VARCHAR(100) NOT NULL,
    main_drugs VARCHAR(500) COMMENT '主药',
    auxiliary_drugs VARCHAR(500) COMMENT '辅药',
    health_products VARCHAR(500) COMMENT '保健品',
    contraindication VARCHAR(500),
    usage_guide VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联合用药推荐表';

-- -------------------------------------------
-- 十一、系统配置表
-- -------------------------------------------

CREATE TABLE sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    description VARCHAR(200),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    UNIQUE KEY uk_key (tenant_id, config_key, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT,
    username VARCHAR(50),
    module VARCHAR(50),
    operation VARCHAR(50),
    method VARCHAR(200),
    params TEXT,
    ip VARCHAR(50),
    result VARCHAR(20),
    error_msg TEXT,
    duration BIGINT COMMENT '执行时长(ms)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- -------------------------------------------
-- 初始化数据
-- -------------------------------------------

-- 默认租户
INSERT INTO sys_tenant (id, tenant_id, name, business_mode, status) VALUES (1, 0, '默认药房', 'single_store', 'active');

-- 默认管理员 (密码: admin123, BCrypt加密)
INSERT INTO sys_user (id, tenant_id, username, password, real_name, role, status) VALUES
(1, 1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin', 'active');

-- 默认门店
INSERT INTO store (id, tenant_id, name, code, type, status) VALUES (1, 1, '总店', 'S001', 'headquarter', 'active');

-- 默认会员等级
INSERT INTO member_level (tenant_id, name, min_amount, discount, points_rate, sort_order) VALUES
(1, '普通会员', 0, 100, 1, 1),
(1, '银卡会员', 1000, 95, 1.5, 2),
(1, '金卡会员', 5000, 90, 2, 3),
(1, '钻石会员', 20000, 85, 3, 4);

-- 药品分类
INSERT INTO drug_category (tenant_id, name, parent_id, sort_order) VALUES
(1, '西药', 0, 1),
(1, '中成药', 0, 2),
(1, '中药饮片', 0, 3),
(1, '保健品', 0, 4),
(1, '医疗器械', 0, 5),
(1, '其他', 0, 6);

-- 十八反配伍禁忌
INSERT INTO herb_incompatibility (type, herb_a, herb_b, description, severity) VALUES
('18_oppose', '甘草', '甘遂', '甘草反甘遂，增毒减效', 'forbidden'),
('18_oppose', '甘草', '大戟', '甘草反大戟，增毒减效', 'forbidden'),
('18_oppose', '甘草', '海藻', '甘草反海藻，增毒减效', 'forbidden'),
('18_oppose', '甘草', '芫花', '甘草反芫花，增毒减效', 'forbidden'),
('18_oppose', '乌头', '贝母', '乌头反贝母，产生毒性', 'forbidden'),
('18_oppose', '乌头', '瓜蒌', '乌头反瓜蒌，产生毒性', 'forbidden'),
('18_oppose', '乌头', '半夏', '乌头反半夏，产生毒性', 'forbidden'),
('18_oppose', '乌头', '白蔹', '乌头反白蔹，产生毒性', 'forbidden'),
('18_oppose', '乌头', '白及', '乌头反白及，产生毒性', 'forbidden'),
('18_oppose', '藜芦', '人参', '藜芦反人参，产生毒性', 'forbidden'),
('18_oppose', '藜芦', '沙参', '藜芦反沙参，产生毒性', 'forbidden'),
('18_oppose', '藜芦', '丹参', '藜芦反丹参，产生毒性', 'forbidden'),
('18_oppose', '藜芦', '玄参', '藜芦反玄参，产生毒性', 'forbidden'),
('18_oppose', '藜芦', '细辛', '藜芦反细辛，产生毒性', 'forbidden'),
('18_oppose', '藜芦', '芍药', '藜芦反芍药，产生毒性', 'forbidden');

-- 十九畏配伍禁忌
INSERT INTO herb_incompatibility (type, herb_a, herb_b, description, severity) VALUES
('19_fear', '硫黄', '朴硝', '硫黄畏朴硝，相畏减效', 'caution'),
('19_fear', '水银', '砒霜', '水银畏砒霜，相畏增毒', 'forbidden'),
('19_fear', '狼毒', '密陀僧', '狼毒畏密陀僧，相畏', 'caution'),
('19_fear', '巴豆', '牵牛', '巴豆畏牵牛，相畏增泻', 'caution'),
('19_fear', '丁香', '郁金', '丁香畏郁金，相畏减效', 'caution'),
('19_fear', '川乌', '犀角', '川乌畏犀角，相畏', 'caution'),
('19_fear', '牙硝', '三棱', '牙硝畏三棱，相畏', 'caution'),
('19_fear', '官桂', '赤石脂', '官桂畏赤石脂，相畏', 'caution'),
('19_fear', '人参', '五灵脂', '人参畏五灵脂，相畏减效', 'caution');

-- 联合用药推荐
INSERT INTO combined_medication (symptom_name, main_drugs, auxiliary_drugs, health_products, usage_guide) VALUES
('感冒发热', '感冒灵颗粒,板蓝根颗粒', '维C银翘片', '维生素C', '多饮水，注意休息'),
('咳嗽有痰', '止咳糖浆,川贝枇杷膏', '氨溴索片', '蜂蜜', '避免辛辣刺激'),
('胃痛胃酸', '奥美拉唑肠溶胶囊,达喜', '吗丁啉', '益生菌', '饭前服用，忌辛辣'),
('腹泻', '蒙脱石散,黄连素', '益生菌', '口服补液盐', '注意补水，饮食清淡'),
('便秘', '乳果糖口服液,开塞露', '芦荟胶囊', '膳食纤维素', '多饮水，增加运动'),
('失眠', '安神补脑液,褪黑素', '谷维素', '酸枣仁', '睡前服用，规律作息'),
('过敏性鼻炎', '氯雷他定片,布地奈德喷剂', '生理盐水鼻喷', '维生素C', '避免过敏原');
