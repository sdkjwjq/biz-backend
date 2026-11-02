-- Only an unused table can be removed; applications, decisions and notification evidence must be retained.
DROP PROCEDURE IF EXISTS rollback_task_transfer_requests;
DELIMITER $$
CREATE PROCEDURE rollback_task_transfer_requests()
BEGIN
  IF EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_task_transfer_request') THEN
    IF EXISTS(SELECT 1 FROM biz_task_transfer_request LIMIT 1) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Transfer requests exist; preserve business records before rollback';
    END IF;
    DROP TABLE biz_task_transfer_request;
  END IF;
END$$
DELIMITER ;
CALL rollback_task_transfer_requests();
DROP PROCEDURE rollback_task_transfer_requests;
