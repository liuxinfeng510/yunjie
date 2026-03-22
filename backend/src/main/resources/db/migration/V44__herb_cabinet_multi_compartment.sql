-- V44: 中药斗柜支持每斗多格（1-4个子格），按行配置子格数
-- herb_cabinet: 新增每行子格数配置
ALTER TABLE herb_cabinet ADD COLUMN row_cell_config VARCHAR(500) NULL
  COMMENT '每行子格数JSON，如{"1":3,"2":3,"3":2}';

-- herb_cabinet_cell: 新增子格序号
ALTER TABLE herb_cabinet_cell ADD COLUMN sub_index INT NOT NULL DEFAULT 1
  COMMENT '子格序号(1=A,2=B,3=C,4=D)';

-- 唯一索引确保同一斗内无重复子格
ALTER TABLE herb_cabinet_cell ADD UNIQUE INDEX uk_cabinet_position
  (cabinet_id, row_num, column_num, sub_index);

-- 回填已有数据的label为新格式
UPDATE herb_cabinet_cell SET label = CONCAT(row_num, '-', column_num, '-A')
  WHERE sub_index = 1 AND deleted = 0;
