-- V12: 添加测试会员数据
INSERT INTO member (tenant_id, member_no, name, pinyin, pinyin_short, phone, gender, level_id, points, total_amount, status, created_at, updated_at, deleted)
VALUES 
(1, 'M202603001', '张三', 'zhangsan', 'zs', '13800138001', '男', 1, 100, 500.00, '正常', NOW(), NOW(), 0),
(1, 'M202603002', '李四', 'lisi', 'ls', '13800138002', '男', 2, 200, 1500.00, '正常', NOW(), NOW(), 0),
(1, 'M202603003', '王芳', 'wangfang', 'wf', '13800138003', '女', 1, 50, 200.00, '正常', NOW(), NOW(), 0),
(1, 'M202603004', '赵敏', 'zhaomin', 'zm', '13800138004', '女', 3, 500, 5000.00, '正常', NOW(), NOW(), 0),
(1, 'M202603005', '刘伟', 'liuwei', 'lw', '13800138005', '男', 2, 300, 2000.00, '正常', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);
