-- 线上已有字段时不改结构、不更新任何用户标记或密码。
-- 缺失字段的环境新增标记，默认要求首次改密；不能以密码复杂度推断是否改过密码。
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sys_user' AND COLUMN_NAME='force_password_change')=0,
 'ALTER TABLE sys_user ADD COLUMN force_password_change TINYINT NOT NULL DEFAULT 1 COMMENT ''首次登录强制修改密码''', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
