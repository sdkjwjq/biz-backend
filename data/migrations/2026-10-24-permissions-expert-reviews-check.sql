DELIMITER $$
DROP PROCEDURE IF EXISTS check_permissions_20261024$$
CREATE PROCEDURE check_permissions_20261024()
BEGIN
 IF (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN
 ('sys_permission_role','sys_user_permission','sys_user_permission_role','sys_permission_revision','biz_expert_review','biz_expert_review_revision'))<>6 THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Permission/expert review tables missing';
 END IF;
 IF EXISTS(SELECT 1 FROM sys_permission_role WHERE NOT JSON_VALID(permissions_json) OR NOT JSON_VALID(scopes_json))
 OR EXISTS(SELECT 1 FROM sys_user_permission WHERE NOT JSON_VALID(permissions_json) OR NOT JSON_VALID(scopes_json)) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Invalid permission JSON';
 END IF;
 IF NOT EXISTS(SELECT 1 FROM sys_permission_revision WHERE revision_id=1) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Permission revision lock missing';
 END IF;
END$$
CALL check_permissions_20261024()$$
DROP PROCEDURE check_permissions_20261024$$
DELIMITER ;
