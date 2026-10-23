-- 先恢复应用版本；有实际授权调整、草稿或评审历史时停止，须先备份保留。
DELIMITER $$
DROP PROCEDURE IF EXISTS rollback_permissions_20261024$$
CREATE PROCEDURE rollback_permissions_20261024()
BEGIN
 IF EXISTS(SELECT 1 FROM sys_permission_role) OR EXISTS(SELECT 1 FROM sys_user_permission WHERE access_mode<>'LEGACY' OR version>0)
 OR EXISTS(SELECT 1 FROM sys_user_permission_role) OR EXISTS(SELECT 1 FROM biz_expert_review) OR EXISTS(SELECT 1 FROM biz_expert_review_revision)
 OR EXISTS(SELECT 1 FROM sys_permission_revision WHERE version>0) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Permission configuration or expert history exists; back up and retain data';
 END IF;
 DROP TABLE IF EXISTS biz_expert_review_revision,biz_expert_review,sys_user_permission_role,sys_user_permission,sys_permission_role,sys_permission_revision;
END$$
CALL rollback_permissions_20261024()$$
DROP PROCEDURE rollback_permissions_20261024$$
DELIMITER ;
