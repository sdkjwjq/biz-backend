DROP PROCEDURE IF EXISTS check_audit_history_index_20261024;
DELIMITER $$
CREATE PROCEDURE check_audit_history_index_20261024()
BEGIN
 IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE()
   AND table_name='biz_audit_log' AND seq_in_index=1 AND column_name='sub_id') THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Missing audit history sub_id index';
 END IF;
END$$
DELIMITER ;
CALL check_audit_history_index_20261024();
DROP PROCEDURE check_audit_history_index_20261024;
