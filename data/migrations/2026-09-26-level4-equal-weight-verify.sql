DELIMITER $$
DROP PROCEDURE IF EXISTS prepare_level4_verify_relations_20260926$$
CREATE PROCEDURE prepare_level4_verify_relations_20260926()
BEGIN
  DROP TEMPORARY TABLE IF EXISTS tmp_active_performance_relation_20260926;
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'rel_task_performance'
      AND column_name = 'is_delete'
  ) THEN
    SET @active_rel_sql = CONCAT(
      'CREATE TEMPORARY TABLE tmp_active_performance_relation_20260926 AS ',
      'SELECT task_id, perf_id, year_id FROM rel_task_performance WHERE COALESCE(is_delete, 0) = 0');
  ELSE
    SET @active_rel_sql = CONCAT(
      'CREATE TEMPORARY TABLE tmp_active_performance_relation_20260926 AS ',
      'SELECT task_id, perf_id, year_id FROM rel_task_performance');
  END IF;
  PREPARE active_rel_stmt FROM @active_rel_sql;
  EXECUTE active_rel_stmt;
  DEALLOCATE PREPARE active_rel_stmt;
  DROP TEMPORARY TABLE IF EXISTS tmp_active_performance_relation_sum_20260926;
  CREATE TEMPORARY TABLE tmp_active_performance_relation_sum_20260926 AS
    SELECT * FROM tmp_active_performance_relation_20260926;
END$$
CALL prepare_level4_verify_relations_20260926()$$
DROP PROCEDURE prepare_level4_verify_relations_20260926$$
DELIMITER ;

SELECT 'invalid_target' AS check_name, COUNT(*) AS mismatch_count
FROM biz_level4_task c
JOIN biz_task t ON t.task_id = c.parent_id
WHERE COALESCE(c.is_delete, 0) = 0 AND COALESCE(t.is_delete, 0) = 0
  AND (c.target_value IS NULL OR c.target_value <= 0 OR t.target_value IS NULL OR t.target_value <= 0)
UNION ALL
SELECT 'level4_progress', COUNT(*)
FROM biz_level4_task c
JOIN biz_task t ON t.task_id = c.parent_id
WHERE COALESCE(c.is_delete, 0) = 0 AND COALESCE(t.is_delete, 0) = 0
  AND c.progress <> FLOOR(LEAST(100, GREATEST(0, COALESCE(c.current_value, 0) * 100 / c.target_value)))
UNION ALL
SELECT 'parent_progress', COUNT(*)
FROM biz_task t
JOIN (
  SELECT parent_id,
         SUM(LEAST(100, GREATEST(0, COALESCE(current_value, 0) * 100 / target_value))) / COUNT(*) AS average_progress
  FROM biz_level4_task WHERE COALESCE(is_delete, 0) = 0 GROUP BY parent_id
) a ON a.parent_id = t.task_id
WHERE COALESCE(t.is_delete, 0) = 0
  AND (t.progress <> FLOOR(a.average_progress)
   OR t.current_value <> TRUNCATE(t.target_value * a.average_progress / 100, 4)
  )
UNION ALL
SELECT 'active_submission', COUNT(*)
FROM biz_material_submission s
JOIN biz_task t ON t.task_id = s.task_id
WHERE COALESCE(s.is_delete, 0) = 0 AND s.flow_status IN (10,20,30)
  AND EXISTS (SELECT 1 FROM biz_level4_task c WHERE c.parent_id = s.task_id AND COALESCE(c.is_delete, 0) = 0)
  AND s.reported_value <> t.current_value
UNION ALL
SELECT 'original_level4_value', COUNT(*)
FROM biz_level4_task c
JOIN biz_level4_task_backup_before_equal_weight_20260926 b ON b.task_id = c.task_id
WHERE NOT (c.current_value <=> b.current_value)
UNION ALL
SELECT 'auto_performance_year', COUNT(*)
FROM biz_performance_year py
JOIN biz_performance p ON p.perf_id = py.perf_id
WHERE COALESCE(p.is_delete, 0) = 0 AND COALESCE(py.is_delete, 0) = 0
  AND p.perf_code IS NOT NULL
  AND p.perf_code NOT LIKE '1.3%' AND p.perf_code NOT LIKE '2%' AND p.perf_code NOT LIKE '3%'
  AND NOT (py.actual_value <=> CASE
    WHEN p.data_type = '2' THEN LEAST(100, COALESCE((
      SELECT MAX(t.current_value)
      FROM tmp_active_performance_relation_sum_20260926 r JOIN biz_task t ON t.task_id = r.task_id
      WHERE r.perf_id = p.perf_id AND r.year_id = py.year_id
        AND COALESCE(t.is_delete, 0) = 0
        AND t.level NOT IN (1,2) AND t.data_type <> '0' AND t.phase = py.year
    ), 0))
    ELSE COALESCE((
      SELECT SUM(t.current_value)
      FROM tmp_active_performance_relation_20260926 r JOIN biz_task t ON t.task_id = r.task_id
      WHERE r.perf_id = p.perf_id AND r.year_id = py.year_id
        AND COALESCE(t.is_delete, 0) = 0
        AND t.level NOT IN (1,2) AND t.data_type <> '0' AND t.phase = py.year
    ), 0)
  END)
UNION ALL
SELECT 'auto_performance_total', COUNT(*)
FROM biz_performance p
WHERE COALESCE(p.is_delete, 0) = 0 AND p.perf_code IS NOT NULL
  AND p.perf_code NOT LIKE '1.3%' AND p.perf_code NOT LIKE '2%' AND p.perf_code NOT LIKE '3%'
  AND NOT (p.current_value <=> CASE
    WHEN p.data_type = '2' THEN LEAST(100, COALESCE((
      SELECT MAX(py.actual_value) FROM biz_performance_year py
      WHERE py.perf_id = p.perf_id AND COALESCE(py.is_delete, 0) = 0
    ), 0))
    ELSE COALESCE((
      SELECT SUM(py.actual_value) FROM biz_performance_year py
      WHERE py.perf_id = p.perf_id AND COALESCE(py.is_delete, 0) = 0
    ), 0)
  END);

SELECT c.task_id AS level4_task_id, c.parent_id, t.task_code, t.task_name,
       c.target_value AS level4_target_value, t.target_value AS parent_target_value
FROM biz_level4_task c
JOIN biz_task t ON t.task_id = c.parent_id
WHERE COALESCE(c.is_delete, 0) = 0 AND COALESCE(t.is_delete, 0) = 0
  AND (c.target_value IS NULL OR c.target_value <= 0 OR t.target_value IS NULL OR t.target_value <= 0)
ORDER BY c.parent_id, c.task_id;

SELECT COUNT(*) AS affected_parent_count,
       SUM(progress = 100) AS fully_complete_count,
       SUM(progress BETWEEN 1 AND 99) AS partial_count,
       SUM(progress = 0) AS zero_count
FROM biz_task
WHERE EXISTS (SELECT 1 FROM biz_level4_task c WHERE c.parent_id = biz_task.task_id AND COALESCE(c.is_delete, 0) = 0)
  AND COALESCE(biz_task.is_delete, 0) = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_active_performance_relation_sum_20260926;
DROP TEMPORARY TABLE IF EXISTS tmp_active_performance_relation_20260926;
