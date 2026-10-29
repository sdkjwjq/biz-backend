-- Never drop published content, drafts or reading evidence during rollback.
DROP PROCEDURE IF EXISTS rollback_platform_notices_20261029;
DELIMITER $$
CREATE PROCEDURE rollback_platform_notices_20261029()
BEGIN
 IF EXISTS(SELECT 1 FROM sys_platform_notice LIMIT 1) OR EXISTS(SELECT 1 FROM sys_platform_notice_recipient LIMIT 1)
 OR EXISTS(SELECT 1 FROM sys_platform_notice_view_log LIMIT 1) OR EXISTS(SELECT 1 FROM sys_platform_notice_attachment LIMIT 1) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Platform notice content or reading history exists; preserve data before rollback';
 END IF;
 DROP TABLE sys_platform_notice_attachment,sys_platform_notice_view_log,sys_platform_notice_recipient,sys_platform_notice;
END$$
DELIMITER ;
CALL rollback_platform_notices_20261029();
DROP PROCEDURE rollback_platform_notices_20261029;
