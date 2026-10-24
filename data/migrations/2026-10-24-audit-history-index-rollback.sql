-- Drop only this migration's marked index. Never drop a reused/pre-existing index.
DROP PROCEDURE IF EXISTS rollback_audit_history_index_20261024;
DELIMITER $$
CREATE PROCEDURE rollback_audit_history_index_20261024()
BEGIN
 IF EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE()
   AND table_name='biz_audit_log' AND index_name='idx_audit_history_sub_time'
   AND index_comment='performance-audit-history-20261024') THEN
  IF (SELECT GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',')
      FROM information_schema.statistics WHERE table_schema=DATABASE()
      AND table_name='biz_audit_log' AND index_name='idx_audit_history_sub_time') <> 'sub_id,create_time' THEN
   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Audit history index changed; rollback refused';
  END IF;
  ALTER TABLE biz_audit_log DROP INDEX idx_audit_history_sub_time;
 END IF;
END$$
DELIMITER ;
CALL rollback_audit_history_index_20261024();
DROP PROCEDURE rollback_audit_history_index_20261024;
