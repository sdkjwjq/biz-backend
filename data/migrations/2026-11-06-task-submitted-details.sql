-- Preserve the submitted values separately from the existing rollback snapshot fields.
-- Each statement is safe to run again. Legacy rows remain NULL: their raw values are unknown.
SET @ddl := (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE biz_audit_snapshot ADD COLUMN submitted_value DECIMAL(20,4) NULL',
  'SELECT 1') FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_audit_snapshot' AND COLUMN_NAME='submitted_value');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE biz_audit_snapshot ADD COLUMN submitted_comment VARCHAR(500) NULL',
  'SELECT 1') FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_audit_snapshot' AND COLUMN_NAME='submitted_comment');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE biz_audit_snapshot ADD COLUMN submitted_name VARCHAR(500) NULL',
  'SELECT 1') FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_audit_snapshot' AND COLUMN_NAME='submitted_name');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE biz_audit_snapshot ADD COLUMN submitted_target_value DECIMAL(20,4) NULL',
  'SELECT 1') FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_audit_snapshot' AND COLUMN_NAME='submitted_target_value');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE biz_audit_snapshot ADD COLUMN submitted_data_type VARCHAR(10) NULL',
  'SELECT 1') FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_audit_snapshot' AND COLUMN_NAME='submitted_data_type');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
