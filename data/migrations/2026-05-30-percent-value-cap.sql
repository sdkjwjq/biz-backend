-- Percentage values are capped at 100; numeric values remain uncapped.

START TRANSACTION;

UPDATE biz_task
SET current_value = 100,
    progress = 100,
    update_time = NOW()
WHERE data_type = '2'
  AND current_value > 100
  AND (is_delete IS NULL OR is_delete = 0);

UPDATE biz_level4_task
SET current_value = 100,
    progress = 100,
    update_time = NOW()
WHERE data_type = '2'
  AND current_value > 100
  AND (is_delete IS NULL OR is_delete = 0);

UPDATE biz_performance
SET current_value = 100,
    update_time = NOW()
WHERE data_type = '2'
  AND current_value > 100
  AND (is_delete IS NULL OR is_delete = 0);

UPDATE biz_performance_year
SET actual_value = 100
WHERE actual_value > 100
  AND (is_delete IS NULL OR is_delete = 0)
  AND perf_id IN (
    SELECT perf_id
    FROM biz_performance
    WHERE data_type = '2'
      AND (is_delete IS NULL OR is_delete = 0)
  );

COMMIT;

SELECT 'percent_task_over_100' AS check_name, COUNT(*) AS remaining_count
FROM biz_task
WHERE data_type = '2'
  AND current_value > 100
UNION ALL
SELECT 'percent_level4_task_over_100' AS check_name, COUNT(*) AS remaining_count
FROM biz_level4_task
WHERE data_type = '2'
  AND current_value > 100
UNION ALL
SELECT 'percent_performance_over_100' AS check_name, COUNT(*) AS remaining_count
FROM biz_performance
WHERE data_type = '2'
  AND current_value > 100
UNION ALL
SELECT 'percent_performance_year_over_100' AS check_name, COUNT(*) AS remaining_count
FROM biz_performance_year y
JOIN biz_performance p ON p.perf_id = y.perf_id
WHERE p.data_type = '2'
  AND y.actual_value > 100;
