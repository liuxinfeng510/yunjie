-- 首营企业表
CREATE TABLE first_marketing_supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    -- 基本信息
    apply_no VARCHAR(30) NOT NULL COMMENT '申请编号',
    supplier_id BIGINT COMMENT '关联供应商ID',
    supplier_name VARCHAR(200) NOT NULL COMMENT '企业名称',
    credit_code VARCHAR(50) COMMENT '统一社会信用代码',
    legal_person VARCHAR(50) COMMENT '法定代表人',
    registered_capital VARCHAR(50) COMMENT '注册资本',
    business_scope TEXT COMMENT '经营范围',
    company_address VARCHAR(300) COMMENT '企业地址',
    contact_name VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    -- 营业执照
    business_license_no VARCHAR(100) COMMENT '营业执照编号',
    business_license_image VARCHAR(500) COMMENT '营业执照图片',
    business_license_valid_from DATE COMMENT '营业执照有效期起',
    business_license_valid_until DATE COMMENT '营业执照有效期止',
    -- 药品经营许可证
    drug_license_no VARCHAR(100) COMMENT '药品经营许可证编号',
    drug_license_image VARCHAR(500) COMMENT '药品经营许可证图片',
    drug_license_valid_from DATE COMMENT '有效期起',
    drug_license_valid_until DATE COMMENT '有效期止',
    -- GSP认证证书
    gsp_cert_no VARCHAR(100) COMMENT 'GSP认证证书编号',
    gsp_cert_image VARCHAR(500) COMMENT 'GSP证书图片',
    gsp_cert_valid_from DATE COMMENT '有效期起',
    gsp_cert_valid_until DATE COMMENT '有效期止',
    -- 委托授权
    legal_auth_image VARCHAR(500) COMMENT '法人委托书图片',
    sales_person_name VARCHAR(50) COMMENT '销售人员姓名',
    sales_person_id_image VARCHAR(500) COMMENT '销售人员身份证图片',
    sales_auth_image VARCHAR(500) COMMENT '销售人员授权委托书图片',
    -- 开票信息
    billing_name VARCHAR(200) COMMENT '开票名称',
    billing_tax_no VARCHAR(50) COMMENT '纳税人识别号',
    billing_address VARCHAR(300) COMMENT '开票地址',
    billing_phone VARCHAR(20) COMMENT '开票电话',
    billing_bank_name VARCHAR(100) COMMENT '开户行',
    billing_bank_account VARCHAR(50) COMMENT '开户账号',
    -- 审批流程
    applicant_id BIGINT COMMENT '申请人ID',
    applicant_name VARCHAR(50) COMMENT '申请人姓名',
    apply_time DATETIME COMMENT '申请时间',
    first_approver_id BIGINT COMMENT '一审审批人ID',
    first_approver_name VARCHAR(50) COMMENT '一审审批人',
    first_approve_time DATETIME COMMENT '一审时间',
    first_approve_opinion VARCHAR(500) COMMENT '一审意见',
    first_approve_result VARCHAR(20) COMMENT '一审结果',
    second_approver_id BIGINT COMMENT '二审审批人ID',
    second_approver_name VARCHAR(50) COMMENT '二审审批人',
    second_approve_time DATETIME COMMENT '二审时间',
    second_approve_opinion VARCHAR(500) COMMENT '二审意见',
    second_approve_result VARCHAR(20) COMMENT '二审结果',
    status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态:draft/pending_first/pending_second/approved/rejected',
    remark VARCHAR(500) COMMENT '备注',
    -- 审计字段
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_fms_tenant_status (tenant_id, status),
    INDEX idx_fms_supplier (supplier_id),
    INDEX idx_fms_apply_no (apply_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首营企业表';

-- 首营品种表
CREATE TABLE first_marketing_drug (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    -- 基本信息
    apply_no VARCHAR(30) NOT NULL COMMENT '申请编号',
    first_supplier_id BIGINT COMMENT '关联首营企业记录ID',
    supplier_id BIGINT COMMENT '供应商ID',
    supplier_name VARCHAR(200) COMMENT '供应商名称',
    drug_id BIGINT COMMENT '关联药品ID',
    generic_name VARCHAR(100) NOT NULL COMMENT '通用名',
    trade_name VARCHAR(100) COMMENT '商品名',
    dosage_form VARCHAR(30) COMMENT '剂型',
    specification VARCHAR(100) COMMENT '规格',
    unit VARCHAR(20) COMMENT '单位',
    manufacturer VARCHAR(200) COMMENT '生产企业',
    approval_no VARCHAR(50) COMMENT '批准文号',
    -- 证照资料
    registration_cert_no VARCHAR(100) COMMENT '药品注册证号',
    registration_cert_image VARCHAR(500) COMMENT '注册证图片',
    registration_cert_valid_from DATE COMMENT '注册证有效期起',
    registration_cert_valid_until DATE COMMENT '注册证有效期止',
    quality_standard VARCHAR(200) COMMENT '质量标准',
    quality_standard_image VARCHAR(500) COMMENT '质量标准文件图片',
    inspection_report_image VARCHAR(500) COMMENT '药品检验报告图片',
    instruction_image VARCHAR(500) COMMENT '说明书图片',
    packaging_image VARCHAR(500) COMMENT '包装标签样稿图片',
    -- 审批流程
    applicant_id BIGINT COMMENT '申请人ID',
    applicant_name VARCHAR(50) COMMENT '申请人姓名',
    apply_time DATETIME COMMENT '申请时间',
    first_approver_id BIGINT COMMENT '一审审批人ID',
    first_approver_name VARCHAR(50) COMMENT '一审审批人',
    first_approve_time DATETIME COMMENT '一审时间',
    first_approve_opinion VARCHAR(500) COMMENT '一审意见',
    first_approve_result VARCHAR(20) COMMENT '一审结果',
    second_approver_id BIGINT COMMENT '二审审批人ID',
    second_approver_name VARCHAR(50) COMMENT '二审审批人',
    second_approve_time DATETIME COMMENT '二审时间',
    second_approve_opinion VARCHAR(500) COMMENT '二审意见',
    second_approve_result VARCHAR(20) COMMENT '二审结果',
    status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态:draft/pending_first/pending_second/approved/rejected',
    remark VARCHAR(500) COMMENT '备注',
    -- 审计字段
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_fmd_tenant_status (tenant_id, status),
    INDEX idx_fmd_supplier (supplier_id),
    INDEX idx_fmd_first_supplier (first_supplier_id),
    INDEX idx_fmd_apply_no (apply_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首营品种表';

-- 首营审批级别配置
INSERT INTO sys_config (tenant_id, config_group, config_key, config_value, value_type, description, created_at, updated_at, deleted)
VALUES (0, 'gsp', 'gsp.first_marketing_approval_level', '1', 'number', '首营审批级别(1=一级/2=二级)', NOW(), NOW(), 0);
