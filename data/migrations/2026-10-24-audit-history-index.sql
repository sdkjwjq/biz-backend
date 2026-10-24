-- Index only. Existing indexes starting with sub_id are reused.
DROP PROCEDURE IF EXISTS apply_audit_history_index_20261024;
DELIMITER $$
CREATE PROCEDURE apply_audit_history_index_20261024()
BEGIN
 IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE()
   AND table_name='biz_audit_log' AND seq_in_index=1 AND column_name='sub_id') THEN
  IF EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE()
    AND table_name='biz_audit_log' AND index_name='idx_audit_history_sub_time') THEN
   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Audit history index name exists with incompatible definition';
  END IF;
  ALTER TABLE biz_audit_log ADD INDEX idx_audit_history_sub_time(sub_id,create_time)
    COMMENT 'performance-audit-history-20261024';
 END IF;
END$$
DELIMITER ;
CALL apply_audit_history_index_20261024();
DROP PROCEDURE apply_audit_history_index_20261024;
