-- Restore only values changed by 2026-09-26-level4-equal-weight-recalculate.sql.
START TRANSACTION;

UPDATE biz_task t
JOIN biz_task_backup_before_level4_equal_weight_20260926 b ON b.task_id = t.task_id
SET t.current_value = b.current_value,
    t.progress = b.progress,
    t.status = b.status,
    t.update_time = b.update_time;

UPDATE biz_level4_task t
JOIN biz_level4_task_backup_before_equal_weight_20260926 b ON b.task_id = t.task_id
SET t.progress = b.progress,
    t.update_time = b.update_time;

UPDATE biz_material_submission t
JOIN biz_material_submission_backup_before_equal_weight_20260926 b ON b.sub_id = t.sub_id
SET t.reported_value = b.reported_value;

UPDATE biz_performance t
JOIN biz_performance_backup_before_equal_weight_20260926 b ON b.perf_id = t.perf_id
SET t.current_value = b.current_value,
    t.update_time = b.update_time;

UPDATE biz_performance_year t
JOIN biz_performance_year_backup_before_equal_weight_20260926 b ON b.year_id = t.year_id
SET t.actual_value = b.actual_value;

COMMIT;
