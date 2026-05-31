-- Keep task progress capped at 100% while preserving over-completed current_value.
-- This script only recalculates progress; it does not change task actual values.

START TRANSACTION;

UPDATE biz_task
SET progress = CASE
    WHEN target_value IS NULL OR target_value <= 0 OR current_value IS NULL OR current_value <= 0 THEN 0
    WHEN current_value >= target_value THEN 100
    ELSE ROUND(current_value * 100 / target_value)
  END,
  update_time = NOW()
WHERE is_delete IS NULL OR is_delete = 0;

UPDATE biz_level4_task
SET progress = CASE
    WHEN target_value IS NULL OR target_value <= 0 OR current_value IS NULL OR current_value <= 0 THEN 0
    WHEN current_value >= target_value THEN 100
    ELSE ROUND(current_value * 100 / target_value)
  END,
  update_time = NOW()
WHERE is_delete IS NULL OR is_delete = 0;

COMMIT;

SELECT 'biz_task_progress_over_100' AS check_name, COUNT(*) AS remaining_count
FROM biz_task
WHERE progress > 100
UNION ALL
SELECT 'biz_level4_task_progress_over_100' AS check_name, COUNT(*) AS remaining_count
FROM biz_level4_task
WHERE progress > 100;
