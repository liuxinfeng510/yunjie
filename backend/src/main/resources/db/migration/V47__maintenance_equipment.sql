-- 养护设备台帐
CREATE TABLE maintenance_equipment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  equipment_name VARCHAR(100) NOT NULL,
  equipment_type VARCHAR(30) COMMENT 'air_conditioner/dehumidifier/thermo_hygrometer/refrigerator/cool_cabinet/other',
  model VARCHAR(100),
  manufacturer VARCHAR(200),
  location VARCHAR(100),
  purchase_date DATE,
  inspection_cycle VARCHAR(20) DEFAULT 'annual' COMMENT 'monthly/quarterly/semi_annual/annual',
  last_inspection_date DATE,
  next_inspection_date DATE,
  status VARCHAR(20) DEFAULT 'active' COMMENT 'active/retired',
  remark VARCHAR(500),
  created_by BIGINT,
  created_at DATETIME,
  updated_by BIGINT,
  updated_at DATETIME,
  deleted INT DEFAULT 0
);

-- 设备检查记录
CREATE TABLE equipment_inspection_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  equipment_id BIGINT NOT NULL,
  equipment_name VARCHAR(100),
  inspection_date DATE NOT NULL,
  appearance_check VARCHAR(20) DEFAULT '正常',
  function_check VARCHAR(20) DEFAULT '正常',
  inspection_result VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '正常/异常',
  abnormal_desc VARCHAR(500),
  treatment VARCHAR(500),
  inspector_name VARCHAR(50),
  next_inspection_date DATE,
  remark VARCHAR(500),
  created_by BIGINT,
  created_at DATETIME,
  updated_by BIGINT,
  updated_at DATETIME,
  deleted INT DEFAULT 0,
  INDEX idx_equipment(equipment_id),
  INDEX idx_inspection_date(inspection_date)
);
