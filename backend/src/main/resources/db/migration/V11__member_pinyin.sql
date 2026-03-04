-- V11: 会员表增加拼音字段支持快速搜索
ALTER TABLE member 
    ADD COLUMN pinyin VARCHAR(100) COMMENT '姓名拼音全拼' AFTER name,
    ADD COLUMN pinyin_short VARCHAR(50) COMMENT '姓名拼音简拼' AFTER pinyin,
    ADD INDEX idx_pinyin (pinyin),
    ADD INDEX idx_pinyin_short (pinyin_short);
