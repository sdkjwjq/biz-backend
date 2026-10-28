-- Never discard recorded review assignments. Export and preserve audit data before reverting code.
DROP PROCEDURE IF EXISTS rollback_transfer_reviewers_20261028;
DELIMITER $$
CREATE PROCEDURE rollback_transfer_reviewers_20261028()
BEGIN
  IF EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_ownership_change_log' AND COLUMN_NAME='auditor_before_id') THEN
    IF EXISTS(SELECT 1 FROM biz_ownership_change_log WHERE auditor_before_id IS NOT NULL) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Reviewer transfer history exists; preserve it before rollback';
    END IF;
  END IF;
  IF EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_ownership_change_log' AND COLUMN_NAME='auditor_after_id') THEN
    IF EXISTS(SELECT 1 FROM biz_ownership_change_log WHERE auditor_after_id IS NOT NULL) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Reviewer transfer history exists; preserve it before rollback';
    END IF;
    ALTER TABLE biz_ownership_change_log DROP COLUMN auditor_after_id;
  END IF;
  IF EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_ownership_change_log' AND COLUMN_NAME='auditor_before_id') THEN
    ALTER TABLE biz_ownership_change_log DROP COLUMN auditor_before_id;
  END IF;
END$$
DELIMITER ;
CALL rollback_transfer_reviewers_20261028();
DROP PROCEDURE rollback_transfer_reviewers_20261028;
