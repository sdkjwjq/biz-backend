-- 管理中心迁移核对，全部查询只读。
SELECT 'admin_log_table_exists' AS check_item, COUNT(*) AS check_value
FROM information_schema.TABLES
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sys_admin_operation_log'
UNION ALL
SELECT 'admin_log_required_columns', COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sys_admin_operation_log'
  AND COLUMN_NAME IN ('log_id','module','action','target_type','target_id','target_name','batch_id',
                      'operator_id','reason','before_json','after_json','create_time');
