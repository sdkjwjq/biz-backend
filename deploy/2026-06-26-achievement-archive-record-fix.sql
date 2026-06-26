-- Fix historical achievement archive records.
-- Purpose:
-- 1. Backfill missing archive submissions for achievements already marked archived.
-- 2. Correct pending submissions for archived achievements to archived status.
-- 3. Backfill a minimal audit log for archived submissions that do not have logs.
-- 4. Hide active audit submissions that belong to deleted achievements.
--
-- Safe scope:
-- - Only touches biz_achievement_submission and biz_achievement_audit_log.
-- - Does not modify biz_achievement business fields.

SET NAMES utf8mb4;

-- Backup tables. Keep them after deployment until the online check is confirmed.
CREATE TABLE IF NOT EXISTS bak_achievement_archive_missing_20260626 (
    ach_id BIGINT PRIMARY KEY,
    ach_name VARCHAR(255),
    create_by BIGINT,
    create_time DATETIME,
    update_time DATETIME,
    audit_status INT,
    backup_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS bak_achievement_submission_20260626 LIKE biz_achievement_submission;
CREATE TABLE IF NOT EXISTS bak_achievement_audit_log_20260626 LIKE biz_achievement_audit_log;

CREATE TABLE IF NOT EXISTS bak_achievement_archive_inserted_sub_20260626 (
    sub_id BIGINT PRIMARY KEY,
    ach_id BIGINT,
    backup_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS bak_achievement_archive_inserted_log_20260626 (
    log_id BIGINT PRIMARY KEY,
    sub_id BIGINT,
    backup_time DATETIME NOT NULL
);

-- Preview: archived achievements without an active submission.
SELECT
    a.ach_id,
    a.ach_name,
    a.create_by,
    a.create_time,
    a.update_time,
    a.audit_status
FROM biz_achievement a
LEFT JOIN biz_achievement_submission s
    ON s.ach_id = a.ach_id
   AND s.is_delete = 0
WHERE a.is_delete = 0
  AND a.audit_status = 30
  AND s.sub_id IS NULL;

-- Backup achievements that will receive a synthetic archived submission.
INSERT INTO bak_achievement_archive_missing_20260626 (
    ach_id,
    ach_name,
    create_by,
    create_time,
    update_time,
    audit_status,
    backup_time
)
SELECT
    a.ach_id,
    a.ach_name,
    a.create_by,
    a.create_time,
    a.update_time,
    a.audit_status,
    NOW()
FROM biz_achievement a
LEFT JOIN biz_achievement_submission s
    ON s.ach_id = a.ach_id
   AND s.is_delete = 0
LEFT JOIN bak_achievement_archive_missing_20260626 b
    ON b.ach_id = a.ach_id
WHERE a.is_delete = 0
  AND a.audit_status = 30
  AND s.sub_id IS NULL
  AND b.ach_id IS NULL;

-- Backup submissions that will be updated by this script.
INSERT INTO bak_achievement_submission_20260626
SELECT s.*
FROM biz_achievement_submission s
JOIN biz_achievement a ON a.ach_id = s.ach_id
LEFT JOIN bak_achievement_submission_20260626 b ON b.sub_id = s.sub_id
WHERE b.sub_id IS NULL
  AND (
      (a.is_delete = 0 AND a.audit_status = 30 AND s.is_delete = 0 AND s.flow_status = 10)
      OR
      (a.is_delete = 1 AND s.is_delete = 0)
  );

-- Backup existing logs of submissions that will be touched.
INSERT INTO bak_achievement_audit_log_20260626
SELECT l.*
FROM biz_achievement_audit_log l
JOIN biz_achievement_submission s ON s.sub_id = l.sub_id
JOIN biz_achievement a ON a.ach_id = s.ach_id
LEFT JOIN bak_achievement_audit_log_20260626 b ON b.log_id = l.log_id
WHERE b.log_id IS NULL
  AND (
      (a.is_delete = 0 AND a.audit_status = 30 AND s.is_delete = 0 AND s.flow_status = 10)
      OR
      (a.is_delete = 1 AND s.is_delete = 0)
  );

-- Backfill missing submissions for already archived achievements.
INSERT INTO biz_achievement_submission (
    ach_id,
    submit_by,
    submit_time,
    flow_status,
    current_handler_id,
    comment,
    is_delete
)
SELECT
    a.ach_id,
    COALESCE(a.create_by, 110228),
    COALESCE(a.create_time, a.update_time, NOW()),
    30,
    110228,
    a.comment,
    0
FROM biz_achievement a
LEFT JOIN biz_achievement_submission s
    ON s.ach_id = a.ach_id
   AND s.is_delete = 0
WHERE a.is_delete = 0
  AND a.audit_status = 30
  AND s.sub_id IS NULL;

-- Remember the submission rows inserted by this script for rollback.
INSERT INTO bak_achievement_archive_inserted_sub_20260626 (
    sub_id,
    ach_id,
    backup_time
)
SELECT
    s.sub_id,
    s.ach_id,
    NOW()
FROM biz_achievement_submission s
JOIN bak_achievement_archive_missing_20260626 b ON b.ach_id = s.ach_id
LEFT JOIN bak_achievement_archive_inserted_sub_20260626 x ON x.sub_id = s.sub_id
WHERE x.sub_id IS NULL
  AND s.is_delete = 0
  AND s.flow_status = 30;

-- Correct pending submissions if the achievement itself is already archived.
UPDATE biz_achievement_submission s
JOIN biz_achievement a ON a.ach_id = s.ach_id
SET
    s.flow_status = 30,
    s.current_handler_id = COALESCE(s.current_handler_id, 110228)
WHERE a.is_delete = 0
  AND a.audit_status = 30
  AND s.is_delete = 0
  AND s.flow_status = 10;

-- Backfill a minimal archive audit log for archived submissions without any audit log.
INSERT INTO biz_achievement_audit_log (
    sub_id,
    operator_id,
    action_type,
    pre_status,
    post_status,
    comment,
    create_time
)
SELECT
    s.sub_id,
    COALESCE(s.current_handler_id, 110228),
    'pass',
    10,
    30,
    'historical archived record backfill',
    COALESCE(a.update_time, s.submit_time, NOW())
FROM biz_achievement_submission s
JOIN biz_achievement a ON a.ach_id = s.ach_id
LEFT JOIN biz_achievement_audit_log l ON l.sub_id = s.sub_id
WHERE a.is_delete = 0
  AND a.audit_status = 30
  AND s.is_delete = 0
  AND s.flow_status = 30
  AND l.log_id IS NULL;

-- Remember the audit log rows inserted by this script for rollback.
INSERT INTO bak_achievement_archive_inserted_log_20260626 (
    log_id,
    sub_id,
    backup_time
)
SELECT
    l.log_id,
    l.sub_id,
    NOW()
FROM biz_achievement_audit_log l
LEFT JOIN bak_achievement_archive_inserted_log_20260626 b ON b.log_id = l.log_id
WHERE b.log_id IS NULL
  AND l.action_type = 'pass'
  AND l.pre_status = 10
  AND l.post_status = 30
  AND l.comment = 'historical archived record backfill'
  AND (
      EXISTS (
          SELECT 1
          FROM bak_achievement_archive_inserted_sub_20260626 x
          WHERE x.sub_id = l.sub_id
      )
      OR EXISTS (
          SELECT 1
          FROM bak_achievement_submission_20260626 s
          WHERE s.sub_id = l.sub_id
      )
  );

-- Hide submissions for achievements that have already been deleted.
-- This prevents deleted test achievements from appearing in the audit center.
UPDATE biz_achievement_submission s
JOIN biz_achievement a ON a.ach_id = s.ach_id
SET s.is_delete = 1
WHERE a.is_delete = 1
  AND s.is_delete = 0;

-- Verify after running.
SELECT
    a.ach_id,
    a.ach_name,
    a.audit_status,
    s.sub_id,
    s.flow_status,
    s.current_handler_id,
    COUNT(l.log_id) AS log_count
FROM biz_achievement a
LEFT JOIN biz_achievement_submission s
    ON s.ach_id = a.ach_id
   AND s.is_delete = 0
LEFT JOIN biz_achievement_audit_log l ON l.sub_id = s.sub_id
WHERE a.is_delete = 0
  AND a.audit_status = 30
GROUP BY
    a.ach_id,
    a.ach_name,
    a.audit_status,
    s.sub_id,
    s.flow_status,
    s.current_handler_id
ORDER BY a.ach_id DESC;

-- Rollback SQL, run manually only if online validation fails.
-- 1. Delete audit logs inserted by this script.
-- DELETE l
-- FROM biz_achievement_audit_log l
-- JOIN bak_achievement_archive_inserted_log_20260626 b ON b.log_id = l.log_id;
--
-- 2. Delete submissions inserted by this script.
-- DELETE s
-- FROM biz_achievement_submission s
-- JOIN bak_achievement_archive_inserted_sub_20260626 b ON b.sub_id = s.sub_id;
--
-- 3. Restore submissions updated by this script.
-- UPDATE biz_achievement_submission s
-- JOIN bak_achievement_submission_20260626 b ON b.sub_id = s.sub_id
-- SET
--     s.ach_id = b.ach_id,
--     s.submit_by = b.submit_by,
--     s.submit_time = b.submit_time,
--     s.flow_status = b.flow_status,
--     s.current_handler_id = b.current_handler_id,
--     s.comment = b.comment,
--     s.is_delete = b.is_delete;
--
-- 4. Optional cleanup after rollback is confirmed.
-- DROP TABLE IF EXISTS bak_achievement_archive_inserted_log_20260626;
-- DROP TABLE IF EXISTS bak_achievement_archive_inserted_sub_20260626;
-- DROP TABLE IF EXISTS bak_achievement_audit_log_20260626;
-- DROP TABLE IF EXISTS bak_achievement_submission_20260626;
-- DROP TABLE IF EXISTS bak_achievement_archive_missing_20260626;
