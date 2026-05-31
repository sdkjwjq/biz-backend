-- Add task comment rollback support to existing audit snapshot tables.
-- Safe to run after 2026-05-30-audit-snapshot.sql if the table was created before previous_comment existed.

SET @column_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_audit_snapshot'
    AND COLUMN_NAME = 'previous_comment'
);

SET @ddl := IF(
  @column_exists = 0,
  'ALTER TABLE `biz_audit_snapshot` ADD COLUMN `previous_comment` varchar(500) DEFAULT NULL COMMENT ''提交前任务备注'' AFTER `previous_status`',
  'SELECT ''biz_audit_snapshot.previous_comment already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
