SELECT COLUMN_NAME,DATA_TYPE,IS_NULLABLE FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_ownership_change_log'
AND COLUMN_NAME IN ('auditor_before_id','auditor_after_id') ORDER BY COLUMN_NAME;
-- Expect two nullable BIGINT columns; old rows intentionally remain NULL.
