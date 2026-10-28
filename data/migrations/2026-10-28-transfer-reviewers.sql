-- Audit metadata only. Do not rewrite task assignments or fabricate old reviewer history.
SET @transfer_sql = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_ownership_change_log' AND COLUMN_NAME='auditor_before_id'), 'SELECT 1', 'ALTER TABLE biz_ownership_change_log ADD COLUMN auditor_before_id BIGINT NULL COMMENT ''变更前专业群审核人ID''');
PREPARE transfer_stmt FROM @transfer_sql;
EXECUTE transfer_stmt;
DEALLOCATE PREPARE transfer_stmt;
SET @transfer_sql = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_ownership_change_log' AND COLUMN_NAME='auditor_after_id'), 'SELECT 1', 'ALTER TABLE biz_ownership_change_log ADD COLUMN auditor_after_id BIGINT NULL COMMENT ''变更后专业群审核人ID''');
PREPARE transfer_stmt FROM @transfer_sql;
EXECUTE transfer_stmt;
DEALLOCATE PREPARE transfer_stmt;
