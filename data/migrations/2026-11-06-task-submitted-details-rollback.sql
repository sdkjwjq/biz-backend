-- Safety stop: removing these columns after a new submission would destroy evidence.
DELIMITER $$
DROP PROCEDURE IF EXISTS rollback_task_submitted_details$$
CREATE PROCEDURE rollback_task_submitted_details()
BEGIN
  IF EXISTS (SELECT 1 FROM biz_audit_snapshot WHERE submitted_value IS NOT NULL) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Submitted details exist; preserve them before rollback';
  END IF;
  ALTER TABLE biz_audit_snapshot DROP COLUMN submitted_data_type,
    DROP COLUMN submitted_target_value, DROP COLUMN submitted_name,
    DROP COLUMN submitted_comment, DROP COLUMN submitted_value;
END$$
DELIMITER ;
CALL rollback_task_submitted_details();
DROP PROCEDURE rollback_task_submitted_details;
