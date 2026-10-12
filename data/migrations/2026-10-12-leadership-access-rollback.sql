-- 先恢复旧应用。存在配置或统计记录时先备份保留，拒绝直接丢弃。
DROP PROCEDURE IF EXISTS rollback_leadership_access_20261012;
DELIMITER $$
CREATE PROCEDURE rollback_leadership_access_20261012()
BEGIN
  IF EXISTS(SELECT 1 FROM sys_leadership_access) OR EXISTS(SELECT 1 FROM sys_leadership_department)
     OR EXISTS(SELECT 1 FROM biz_department_trend) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Leadership configuration or trends exist; back up and retain before rollback';
  END IF;
  DROP TABLE sys_leadership_department;
  DROP TABLE sys_leadership_access;
  DROP TABLE biz_department_trend;
END$$
DELIMITER ;
CALL rollback_leadership_access_20261012();
DROP PROCEDURE rollback_leadership_access_20261012;
