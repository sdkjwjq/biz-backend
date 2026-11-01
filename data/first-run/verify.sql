-- Read-only verification for the restored snapshot plus current application schema.
SELECT DATABASE() AS initialized_database;
SELECT 'sys_user' AS table_name, COUNT(*) AS row_count FROM sys_user
UNION ALL SELECT 'sys_dept', COUNT(*) FROM sys_dept
UNION ALL SELECT 'biz_task', COUNT(*) FROM biz_task
UNION ALL SELECT 'biz_level4_task', COUNT(*) FROM biz_level4_task
UNION ALL SELECT 'biz_performance', COUNT(*) FROM biz_performance
UNION ALL SELECT 'biz_performance_year', COUNT(*) FROM biz_performance_year
UNION ALL SELECT 'biz_material_submission', COUNT(*) FROM biz_material_submission
UNION ALL SELECT 'biz_audit_log', COUNT(*) FROM biz_audit_log
UNION ALL SELECT 'biz_achievement', COUNT(*) FROM biz_achievement
UNION ALL SELECT 'sys_file', COUNT(*) FROM sys_file;
SELECT COUNT(*) AS core_indicator_count FROM biz_core_indicator;
SELECT COUNT(*) AS initial_budget_versions FROM biz_budget_import_version;
SELECT COUNT(*) AS ownership_review_metadata_count
FROM biz_ownership_change_log WHERE auditor_before_id IS NOT NULL OR auditor_after_id IS NOT NULL;
SELECT COUNT(*) AS work_record_count FROM biz_work_record WHERE delete_marker = 0;
SELECT COUNT(*) AS expert_review_count FROM biz_expert_review;
SELECT COUNT(*) AS account_permission_count FROM sys_user_permission;
SELECT 'First-run database verification completed.' AS result;
