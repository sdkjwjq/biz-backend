-- 仅在尚未产生管理日志时允许删除，避免丢失审计记录。
DELIMITER //
DROP PROCEDURE IF EXISTS `rollback_admin_management_20260927`//
CREATE PROCEDURE `rollback_admin_management_20260927`()
BEGIN
  DECLARE table_exists bigint DEFAULT 0;
  DECLARE used_count bigint DEFAULT 0;
  SELECT COUNT(*) INTO table_exists FROM information_schema.TABLES
    WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sys_admin_operation_log';
  IF table_exists > 0 THEN
    SELECT COUNT(*) INTO used_count FROM `sys_admin_operation_log`;
    IF used_count > 0 THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Admin operation logs exist; preserve audit history and stop rollback';
    END IF;
    DROP TABLE `sys_admin_operation_log`;
  END IF;
END//
CALL `rollback_admin_management_20260927`()//
DROP PROCEDURE `rollback_admin_management_20260927`//
DELIMITER ;
