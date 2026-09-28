-- Recalculate level-three tasks with equal-weight level-four completion rates.
-- Original level-four current_value and archived audit history are never changed.

DELIMITER $$
DROP PROCEDURE IF EXISTS migrate_level4_equal_weight_20260926$$
CREATE PROCEDURE migrate_level4_equal_weight_20260926()
BEGIN
  -- CTAS intentionally avoids copying source CHECK constraint names. MySQL requires
  -- those names to be unique within a schema, so CREATE TABLE ... LIKE would fail.
  CREATE TABLE IF NOT EXISTS biz_task_backup_before_level4_equal_weight_20260926
    AS SELECT * FROM biz_task WHERE 1 = 0;
  CREATE TABLE IF NOT EXISTS biz_level4_task_backup_before_equal_weight_20260926
    AS SELECT * FROM biz_level4_task WHERE 1 = 0;
  CREATE TABLE IF NOT EXISTS biz_material_submission_backup_before_equal_weight_20260926
    AS SELECT * FROM biz_material_submission WHERE 1 = 0;
  CREATE TABLE IF NOT EXISTS biz_performance_backup_before_equal_weight_20260926
    AS SELECT * FROM biz_performance WHERE 1 = 0;
  CREATE TABLE IF NOT EXISTS biz_performance_year_backup_before_equal_weight_20260926
    AS SELECT * FROM biz_performance_year WHERE 1 = 0;

  INSERT INTO biz_task_backup_before_level4_equal_weight_20260926
  SELECT t.* FROM biz_task t
  WHERE EXISTS (SELECT 1 FROM biz_level4_task c WHERE c.parent_id = t.task_id AND COALESCE(c.is_delete, 0) = 0)
    AND NOT EXISTS (SELECT 1 FROM biz_task_backup_before_level4_equal_weight_20260926 b
                    WHERE b.task_id = t.task_id);

  INSERT INTO biz_level4_task_backup_before_equal_weight_20260926
  SELECT c.* FROM biz_level4_task c
  WHERE COALESCE(c.is_delete, 0) = 0
    AND NOT EXISTS (SELECT 1 FROM biz_level4_task_backup_before_equal_weight_20260926 b
                    WHERE b.task_id = c.task_id);

  INSERT INTO biz_material_submission_backup_before_equal_weight_20260926
  SELECT s.* FROM biz_material_submission s
  WHERE COALESCE(s.is_delete, 0) = 0 AND s.flow_status IN (10, 20, 30)
    AND EXISTS (SELECT 1 FROM biz_level4_task c WHERE c.parent_id = s.task_id AND COALESCE(c.is_delete, 0) = 0)
    AND NOT EXISTS (SELECT 1 FROM biz_material_submission_backup_before_equal_weight_20260926 b
                    WHERE b.sub_id = s.sub_id);

  INSERT INTO biz_performance_backup_before_equal_weight_20260926
  SELECT p.* FROM biz_performance p
  WHERE NOT EXISTS (SELECT 1 FROM biz_performance_backup_before_equal_weight_20260926 b
                    WHERE b.perf_id = p.perf_id);
  INSERT INTO biz_performance_year_backup_before_equal_weight_20260926
  SELECT py.* FROM biz_performance_year py
  WHERE NOT EXISTS (SELECT 1 FROM biz_performance_year_backup_before_equal_weight_20260926 b
                    WHERE b.year_id = py.year_id);

  -- Two historical imports have a zero parent target but exactly one valid level-four
  -- target. In that unambiguous case the parent uses the only child's target. The full
  -- parent row has already been backed up above. Any other invalid target remains a hard
  -- stop so the migration never guesses a business value.
  UPDATE biz_task t
  JOIN (
    SELECT parent_id, COUNT(*) AS child_count, MAX(target_value) AS only_target
    FROM biz_level4_task
    WHERE COALESCE(is_delete, 0) = 0
    GROUP BY parent_id
  ) c ON c.parent_id = t.task_id
  SET t.target_value = c.only_target,
      t.update_time = NOW()
  WHERE COALESCE(t.is_delete, 0) = 0
    AND (t.target_value IS NULL OR t.target_value <= 0)
    AND c.child_count = 1
    AND c.only_target > 0;

  IF EXISTS (
    SELECT 1 FROM biz_level4_task c
    JOIN biz_task t ON t.task_id = c.parent_id
    WHERE COALESCE(c.is_delete, 0) = 0 AND COALESCE(t.is_delete, 0) = 0
      AND (c.target_value IS NULL OR c.target_value <= 0
           OR t.target_value IS NULL OR t.target_value <= 0)
  ) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Level-four or parent target_value is not positive; migration stopped';
  END IF;

  DROP TEMPORARY TABLE IF EXISTS tmp_level4_equal_weight_20260926;
  CREATE TEMPORARY TABLE tmp_level4_equal_weight_20260926 AS
  SELECT c.parent_id,
         CAST(SUM(LEAST(100, GREATEST(0, COALESCE(c.current_value, 0) * 100 / c.target_value))) / COUNT(*) AS DECIMAL(20,8)) AS average_progress,
         MIN(COALESCE(c.current_value, 0) >= c.target_value) AS all_complete,
         MAX(COALESCE(c.current_value, 0) > 0) AS has_progress
  FROM biz_level4_task c
  JOIN biz_task t ON t.task_id = c.parent_id AND COALESCE(t.is_delete, 0) = 0
  WHERE COALESCE(c.is_delete, 0) = 0
  GROUP BY c.parent_id;

  START TRANSACTION;

  UPDATE biz_level4_task c
  JOIN biz_task t ON t.task_id = c.parent_id AND COALESCE(t.is_delete, 0) = 0
  SET c.progress = FLOOR(LEAST(100, GREATEST(0, COALESCE(c.current_value, 0) * 100 / c.target_value))),
      c.update_time = NOW()
  WHERE COALESCE(c.is_delete, 0) = 0;

  UPDATE biz_task t
  JOIN tmp_level4_equal_weight_20260926 a ON a.parent_id = t.task_id
  SET t.current_value = TRUNCATE(t.target_value * a.average_progress / 100, 4),
      t.progress = FLOOR(a.average_progress),
      t.status = CASE
        WHEN EXISTS (SELECT 1 FROM biz_material_submission s
                     WHERE s.task_id = t.task_id AND COALESCE(s.is_delete, 0) = 0 AND s.flow_status IN (10,20,30)) THEN '2'
        WHEN a.all_complete = 1 AND EXISTS (SELECT 1 FROM biz_material_submission s
                     WHERE s.task_id = t.task_id AND COALESCE(s.is_delete, 0) = 0 AND s.flow_status = 40) THEN '3'
        WHEN a.has_progress = 1 OR EXISTS (SELECT 1 FROM biz_material_submission s
                     WHERE s.task_id = t.task_id AND COALESCE(s.is_delete, 0) = 0 AND s.flow_status = 40) THEN '1'
        ELSE '0'
      END,
      t.update_time = NOW();

  UPDATE biz_material_submission s
  JOIN biz_task t ON t.task_id = s.task_id
  JOIN tmp_level4_equal_weight_20260926 a ON a.parent_id = s.task_id
  SET s.reported_value = t.current_value
  WHERE COALESCE(s.is_delete, 0) = 0 AND s.flow_status IN (10,20,30);

  -- The production backup predates relation soft-delete support. Build one active
  -- relation view that works both before and after that column is deployed.
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

  -- Match PerformanceService.calculateAllPerformanceV2: manual indicators remain untouched.
  UPDATE biz_performance_year py
  JOIN biz_performance p ON p.perf_id = py.perf_id
  SET py.actual_value = CASE
    WHEN p.data_type = '2' THEN LEAST(100, COALESCE((
      SELECT MAX(t.current_value)
      FROM tmp_active_performance_relation_sum_20260926 r
      JOIN biz_task t ON t.task_id = r.task_id
      WHERE r.perf_id = p.perf_id AND r.year_id = py.year_id
        AND COALESCE(t.is_delete, 0) = 0
        AND t.level NOT IN (1,2) AND t.data_type <> '0'
        AND t.phase = py.year
    ), 0))
    ELSE COALESCE((
      SELECT SUM(t.current_value)
      FROM tmp_active_performance_relation_20260926 r
      JOIN biz_task t ON t.task_id = r.task_id
      WHERE r.perf_id = p.perf_id AND r.year_id = py.year_id
        AND COALESCE(t.is_delete, 0) = 0
        AND t.level NOT IN (1,2) AND t.data_type <> '0'
        AND t.phase = py.year
    ), 0)
  END
  WHERE COALESCE(p.is_delete, 0) = 0 AND COALESCE(py.is_delete, 0) = 0
    AND p.perf_code IS NOT NULL
    AND p.perf_code NOT LIKE '1.3%' AND p.perf_code NOT LIKE '2%' AND p.perf_code NOT LIKE '3%';

  UPDATE biz_performance p
  SET p.current_value = CASE
    WHEN p.data_type = '2' THEN LEAST(100, COALESCE((
      SELECT MAX(py.actual_value) FROM biz_performance_year py
      WHERE py.perf_id = p.perf_id AND COALESCE(py.is_delete, 0) = 0
    ), 0))
    ELSE COALESCE((
      SELECT SUM(py.actual_value) FROM biz_performance_year py
      WHERE py.perf_id = p.perf_id AND COALESCE(py.is_delete, 0) = 0
    ), 0)
  END,
  p.update_time = NOW()
  WHERE COALESCE(p.is_delete, 0) = 0 AND p.perf_code IS NOT NULL
    AND p.perf_code NOT LIKE '1.3%' AND p.perf_code NOT LIKE '2%' AND p.perf_code NOT LIKE '3%';

  COMMIT;
  DROP TEMPORARY TABLE IF EXISTS tmp_active_performance_relation_sum_20260926;
  DROP TEMPORARY TABLE IF EXISTS tmp_active_performance_relation_20260926;
  DROP TEMPORARY TABLE IF EXISTS tmp_level4_equal_weight_20260926;
END$$

CALL migrate_level4_equal_weight_20260926()$$
DROP PROCEDURE migrate_level4_equal_weight_20260926$$
DELIMITER ;
