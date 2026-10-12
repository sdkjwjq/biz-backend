-- 先恢复应用；有管理修改或实际完成证据时拒绝删除，须先备份新增属性。
DELIMITER $$
CREATE PROCEDURE rollback_task_attributes()
BEGIN
 IF EXISTS(SELECT 1 FROM biz_task WHERE management_version>0 OR actual_completion_date IS NOT NULL OR is_priority<>0 OR is_difficult<>0) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Task attributes contain business data; backup and retain before rollback';
 END IF;
 ALTER TABLE biz_task DROP COLUMN is_priority,DROP COLUMN is_difficult,DROP COLUMN planned_completion_date,DROP COLUMN actual_completion_date,DROP COLUMN management_version;
END$$
DELIMITER ;
CALL rollback_task_attributes();
DROP PROCEDURE rollback_task_attributes;
