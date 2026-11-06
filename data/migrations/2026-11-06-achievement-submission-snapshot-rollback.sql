-- Never remove frozen evidence. Only an unused table can be rolled back.
DELIMITER $$
DROP PROCEDURE IF EXISTS rollback_achievement_submission_snapshot$$
CREATE PROCEDURE rollback_achievement_submission_snapshot()
BEGIN
  IF EXISTS (SELECT 1 FROM biz_achievement_submission_snapshot LIMIT 1) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Achievement submission snapshots exist; preserve them before rollback';
  END IF;
  DROP TABLE biz_achievement_submission_snapshot;
END$$
DELIMITER ;
CALL rollback_achievement_submission_snapshot();
DROP PROCEDURE rollback_achievement_submission_snapshot;
