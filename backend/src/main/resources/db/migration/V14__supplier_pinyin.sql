-- V14: 供应商表增加拼音字段

ALTER TABLE supplier ADD COLUMN pinyin VARCHAR(200) COMMENT '拼音';
ALTER TABLE supplier ADD COLUMN pinyin_short VARCHAR(50) COMMENT '拼音首字母';

-- 创建索引
CREATE INDEX idx_supplier_pinyin_short ON supplier(pinyin_short);
