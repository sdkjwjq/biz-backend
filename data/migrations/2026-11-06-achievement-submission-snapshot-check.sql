SELECT COUNT(*) AS snapshot_table_exists FROM information_schema.TABLES
 WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_achievement_submission_snapshot';
SELECT COUNT(*) AS frozen_submissions FROM biz_achievement_submission_snapshot;
SELECT COUNT(*) AS snapshots_without_submission
 FROM biz_achievement_submission_snapshot d
 LEFT JOIN biz_achievement_submission s ON s.sub_id=d.sub_id
 WHERE s.sub_id IS NULL;
