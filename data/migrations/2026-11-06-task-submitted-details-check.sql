SELECT COLUMN_NAME FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_audit_snapshot'
  AND COLUMN_NAME IN ('submitted_value','submitted_comment','submitted_name','submitted_target_value','submitted_data_type')
ORDER BY COLUMN_NAME;
SELECT COUNT(*) AS submitted_snapshots,
       SUM(target_type='LEVEL4_TASK' AND submitted_value IS NOT NULL) AS level4_details
FROM biz_audit_snapshot;
