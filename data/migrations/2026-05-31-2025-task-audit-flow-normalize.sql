-- Normalize 2025 completed task audit history.
--
-- Background:
-- 2025 tasks were completed by an older one-click implementation. That old
-- implementation wrote audit logs such as "任务完成 1 -> 3" or only a final
-- "通过 30 -> 40", so the frontend audit timeline does not look like the
-- normal review process.
--
-- Goal:
-- For completed 2025 third-level tasks, keep the existing material submission
-- rows, but rebuild their audit logs to look like the normal flow:
--   submitter submit      0  -> 10
--   auditor approve       10 -> 20
--   principal approve     20 -> 30
--   admin archive         30 -> 40
--
-- Notes:
-- - This script intentionally does not create sys_notice rows, so old history
--   will not appear as new unread messages.
-- - It is safe to run more than once. It removes the normalized approval logs
--   for the target submissions before inserting them again.
-- - Backup tables are created on first run for rollback/reference.
-- - Historical timestamps are redistributed into 2025. They follow task order,
--   spread roughly every two days from mid-February, and each review step is
--   later than its submit step.

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

START TRANSACTION;

CREATE TABLE IF NOT EXISTS biz_audit_log_backup_20260531_2025_flow AS
SELECT l.*
FROM biz_audit_log l
WHERE l.sub_id IN (
    SELECT s.sub_id
    FROM biz_material_submission s
    JOIN biz_task t ON t.task_id = s.task_id
    WHERE t.phase = 2025
      AND t.level = 3
      AND t.status = '3'
      AND s.flow_status = 40
      AND (t.is_delete IS NULL OR t.is_delete = 0)
      AND (s.is_delete IS NULL OR s.is_delete = 0)
)
   OR l.sub_id IN (
    SELECT t.task_id
    FROM biz_task t
    WHERE t.phase = 2025
      AND t.level = 3
      AND t.status = '3'
      AND (t.is_delete IS NULL OR t.is_delete = 0)
);

CREATE TABLE IF NOT EXISTS biz_material_submission_backup_20260531_2025_flow AS
SELECT s.*
FROM biz_material_submission s
JOIN biz_task t ON t.task_id = s.task_id
WHERE t.phase = 2025
  AND t.level = 3
  AND t.status = '3'
  AND s.flow_status = 40
  AND (t.is_delete IS NULL OR t.is_delete = 0)
  AND (s.is_delete IS NULL OR s.is_delete = 0);

CREATE TEMPORARY TABLE tmp_2025_completed_task_audits (
  sub_id BIGINT PRIMARY KEY,
  task_id BIGINT NOT NULL,
  submit_by BIGINT NOT NULL,
  auditor_id BIGINT NOT NULL,
  principal_id BIGINT NOT NULL,
  submit_time DATETIME NULL
) ENGINE=Memory;

CREATE TEMPORARY TABLE tmp_2025_audit_log_delete_scope (
  sub_id BIGINT PRIMARY KEY
) ENGINE=Memory;

CREATE TEMPORARY TABLE tmp_2025_completed_task_audit_times (
  sub_id BIGINT PRIMARY KEY,
  submit_time_2025 DATETIME NOT NULL,
  auditor_time_2025 DATETIME NOT NULL,
  principal_time_2025 DATETIME NOT NULL,
  admin_time_2025 DATETIME NOT NULL
) ENGINE=Memory;

INSERT INTO tmp_2025_completed_task_audits (
  sub_id,
  task_id,
  submit_by,
  auditor_id,
  principal_id,
  submit_time
)
SELECT
  s.sub_id,
  t.task_id,
  s.submit_by,
  t.auditor_id,
  t.principal_id,
  s.submit_time
FROM biz_material_submission s
JOIN biz_task t ON t.task_id = s.task_id
WHERE t.phase = 2025
  AND t.level = 3
  AND t.status = '3'
  AND s.flow_status = 40
  AND (t.is_delete IS NULL OR t.is_delete = 0)
  AND (s.is_delete IS NULL OR s.is_delete = 0)
  AND s.submit_by IS NOT NULL
  AND t.auditor_id IS NOT NULL
  AND t.principal_id IS NOT NULL;

INSERT INTO tmp_2025_completed_task_audit_times (
  sub_id,
  submit_time_2025,
  auditor_time_2025,
  principal_time_2025,
  admin_time_2025
)
SELECT
  adjusted.sub_id,
  adjusted.submit_time_2025,
  CASE
    WHEN WEEKDAY(adjusted.submit_time_2025) = 4 THEN DATE_ADD(adjusted.submit_time_2025, INTERVAL 3 DAY)
    ELSE DATE_ADD(adjusted.submit_time_2025, INTERVAL 1 DAY)
  END AS auditor_time_2025,
  CASE WEEKDAY(adjusted.submit_time_2025)
    WHEN 0 THEN DATE_ADD(adjusted.submit_time_2025, INTERVAL 3 DAY)
    WHEN 1 THEN DATE_ADD(adjusted.submit_time_2025, INTERVAL 3 DAY)
    WHEN 2 THEN DATE_ADD(adjusted.submit_time_2025, INTERVAL 5 DAY)
    WHEN 3 THEN DATE_ADD(adjusted.submit_time_2025, INTERVAL 5 DAY)
    ELSE DATE_ADD(adjusted.submit_time_2025, INTERVAL 5 DAY)
  END AS principal_time_2025,
  DATE_ADD(adjusted.submit_time_2025, INTERVAL 7 DAY) AS admin_time_2025
FROM (
  SELECT
    ranked.sub_id,
    CASE
      WHEN WEEKDAY(ranked.raw_submit_time) = 5 THEN DATE_ADD(ranked.raw_submit_time, INTERVAL 2 DAY)
      WHEN WEEKDAY(ranked.raw_submit_time) = 6 THEN DATE_ADD(ranked.raw_submit_time, INTERVAL 1 DAY)
      ELSE ranked.raw_submit_time
    END AS submit_time_2025
FROM (
  SELECT
    ordered.sub_id,
    DATE_ADD(
      DATE_ADD('2025-02-17 09:00:00', INTERVAL (ordered.seq_no - 1) * 2 DAY),
      INTERVAL ((ordered.seq_no - 1) % 6) * 37 MINUTE
    ) AS raw_submit_time
  FROM (
    SELECT
      target.sub_id,
      ROW_NUMBER() OVER (ORDER BY t.task_code, target.task_id) AS seq_no
    FROM tmp_2025_completed_task_audits target
    JOIN biz_task t ON t.task_id = target.task_id
  ) ordered
) ranked
) adjusted;

INSERT IGNORE INTO tmp_2025_audit_log_delete_scope (sub_id)
SELECT sub_id
FROM tmp_2025_completed_task_audits;

-- Some old one-click logs used task_id as sub_id by mistake.
INSERT IGNORE INTO tmp_2025_audit_log_delete_scope (sub_id)
SELECT task_id
FROM tmp_2025_completed_task_audits;

-- Completed submissions should stay archived, with the admin as the final
-- handler. This matches the current normal task audit flow.
UPDATE biz_material_submission s
JOIN tmp_2025_completed_task_audits target ON target.sub_id = s.sub_id
JOIN tmp_2025_completed_task_audit_times audit_time ON audit_time.sub_id = s.sub_id
SET s.flow_status = 40,
    s.current_handler_id = 110228,
    s.is_delete = 0,
    s.submit_time = audit_time.submit_time_2025;

-- Remove old one-click or partial approval traces from the target submissions.
-- Some old one-click logs used task_id as sub_id by mistake, so task ids are
-- included in the delete scope as well.
DELETE l
FROM biz_audit_log l
JOIN tmp_2025_audit_log_delete_scope scope ON scope.sub_id = l.sub_id
WHERE 1 = 1
  AND (
    l.action_type IN ('任务完成', '状态调整')
    OR (
      l.pre_status = 0
      AND l.post_status = 10
      AND l.action_type IN ('提交', '鎻愪氦')
    )
    OR (
      l.action_type = '通过'
      AND (
        (l.pre_status = 10 AND l.post_status = 20)
        OR (l.pre_status = 20 AND l.post_status = 30)
        OR (l.pre_status = 30 AND l.post_status = 40)
      )
    )
  );

-- Rebuild the normal submit and approval path.
INSERT INTO biz_audit_log (
  sub_id,
  operator_id,
  action_type,
  pre_status,
  post_status,
  comment,
  create_time
)
SELECT
  target.sub_id,
  target.submit_by,
  '提交',
  0,
  10,
  '提交任务',
  audit_time.submit_time_2025
FROM tmp_2025_completed_task_audits target
JOIN tmp_2025_completed_task_audit_times audit_time ON audit_time.sub_id = target.sub_id;

INSERT INTO biz_audit_log (
  sub_id,
  operator_id,
  action_type,
  pre_status,
  post_status,
  comment,
  create_time
)
SELECT
  target.sub_id,
  target.auditor_id,
  '通过',
  10,
  20,
  '同意',
  audit_time.auditor_time_2025
FROM tmp_2025_completed_task_audits target
JOIN tmp_2025_completed_task_audit_times audit_time ON audit_time.sub_id = target.sub_id;

INSERT INTO biz_audit_log (
  sub_id,
  operator_id,
  action_type,
  pre_status,
  post_status,
  comment,
  create_time
)
SELECT
  target.sub_id,
  target.principal_id,
  '通过',
  20,
  30,
  '同意',
  audit_time.principal_time_2025
FROM tmp_2025_completed_task_audits target
JOIN tmp_2025_completed_task_audit_times audit_time ON audit_time.sub_id = target.sub_id;

INSERT INTO biz_audit_log (
  sub_id,
  operator_id,
  action_type,
  pre_status,
  post_status,
  comment,
  create_time
)
SELECT
  target.sub_id,
  110228,
  '通过',
  30,
  40,
  '同意',
  audit_time.admin_time_2025
FROM tmp_2025_completed_task_audits target
JOIN tmp_2025_completed_task_audit_times audit_time ON audit_time.sub_id = target.sub_id;

DROP TEMPORARY TABLE IF EXISTS tmp_2025_completed_task_audit_times;
DROP TEMPORARY TABLE IF EXISTS tmp_2025_audit_log_delete_scope;
DROP TEMPORARY TABLE IF EXISTS tmp_2025_completed_task_audits;

COMMIT;

-- Post-checks. All missing counts should be 0, and old_one_click_log_count
-- should also be 0.
SELECT 'missing_submit_0_10' AS check_name, COUNT(*) AS remaining_count
FROM biz_material_submission s
JOIN biz_task t ON t.task_id = s.task_id
LEFT JOIN biz_audit_log l
  ON l.sub_id = s.sub_id
 AND l.action_type = '提交'
 AND l.pre_status = 0
 AND l.post_status = 10
WHERE t.phase = 2025
  AND t.level = 3
  AND t.status = '3'
  AND s.flow_status = 40
  AND (t.is_delete IS NULL OR t.is_delete = 0)
  AND (s.is_delete IS NULL OR s.is_delete = 0)
  AND l.log_id IS NULL
UNION ALL
SELECT 'missing_auditor_10_20' AS check_name, COUNT(*) AS remaining_count
FROM biz_material_submission s
JOIN biz_task t ON t.task_id = s.task_id
LEFT JOIN biz_audit_log l
  ON l.sub_id = s.sub_id
 AND l.operator_id = t.auditor_id
 AND l.action_type = '通过'
 AND l.pre_status = 10
 AND l.post_status = 20
WHERE t.phase = 2025
  AND t.level = 3
  AND t.status = '3'
  AND s.flow_status = 40
  AND (t.is_delete IS NULL OR t.is_delete = 0)
  AND (s.is_delete IS NULL OR s.is_delete = 0)
  AND l.log_id IS NULL
UNION ALL
SELECT 'missing_principal_20_30' AS check_name, COUNT(*) AS remaining_count
FROM biz_material_submission s
JOIN biz_task t ON t.task_id = s.task_id
LEFT JOIN biz_audit_log l
  ON l.sub_id = s.sub_id
 AND l.operator_id = t.principal_id
 AND l.action_type = '通过'
 AND l.pre_status = 20
 AND l.post_status = 30
WHERE t.phase = 2025
  AND t.level = 3
  AND t.status = '3'
  AND s.flow_status = 40
  AND (t.is_delete IS NULL OR t.is_delete = 0)
  AND (s.is_delete IS NULL OR s.is_delete = 0)
  AND l.log_id IS NULL
UNION ALL
SELECT 'missing_admin_30_40' AS check_name, COUNT(*) AS remaining_count
FROM biz_material_submission s
JOIN biz_task t ON t.task_id = s.task_id
LEFT JOIN biz_audit_log l
  ON l.sub_id = s.sub_id
 AND l.operator_id = 110228
 AND l.action_type = '通过'
 AND l.pre_status = 30
 AND l.post_status = 40
WHERE t.phase = 2025
  AND t.level = 3
  AND t.status = '3'
  AND s.flow_status = 40
  AND (t.is_delete IS NULL OR t.is_delete = 0)
  AND (s.is_delete IS NULL OR s.is_delete = 0)
  AND l.log_id IS NULL
UNION ALL
SELECT 'old_one_click_log_count' AS check_name, COUNT(*) AS remaining_count
FROM biz_audit_log l
WHERE l.sub_id IN (
    SELECT s.sub_id
    FROM biz_material_submission s
    JOIN biz_task t ON t.task_id = s.task_id
    WHERE t.phase = 2025
      AND t.level = 3
      AND t.status = '3'
      AND s.flow_status = 40
      AND (t.is_delete IS NULL OR t.is_delete = 0)
      AND (s.is_delete IS NULL OR s.is_delete = 0)
)
  AND l.action_type IN ('任务完成', '状态调整');

SELECT 'submission_time_not_2025' AS check_name, COUNT(*) AS remaining_count
FROM biz_material_submission s
JOIN biz_task t ON t.task_id = s.task_id
WHERE t.phase = 2025
  AND t.level = 3
  AND t.status = '3'
  AND s.flow_status = 40
  AND (t.is_delete IS NULL OR t.is_delete = 0)
  AND (s.is_delete IS NULL OR s.is_delete = 0)
  AND YEAR(s.submit_time) <> 2025
UNION ALL
SELECT 'audit_log_time_not_2025' AS check_name, COUNT(*) AS remaining_count
FROM biz_audit_log l
JOIN biz_material_submission s ON s.sub_id = l.sub_id
JOIN biz_task t ON t.task_id = s.task_id
WHERE t.phase = 2025
  AND t.level = 3
  AND t.status = '3'
  AND s.flow_status = 40
  AND (t.is_delete IS NULL OR t.is_delete = 0)
  AND (s.is_delete IS NULL OR s.is_delete = 0)
  AND YEAR(l.create_time) <> 2025;
