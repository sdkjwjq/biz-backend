-- 先回滚应用；存在任何草稿、填报或审核记录时拒绝删表。
DELIMITER $$
DROP PROCEDURE IF EXISTS rollback_core_indicators$$
CREATE PROCEDURE rollback_core_indicators()
BEGIN
 IF EXISTS(SELECT 1 FROM biz_core_report) OR EXISTS(SELECT 1 FROM biz_core_audit_log) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Core indicator history exists; retain and backup before rollback';
 END IF;
 DROP TABLE biz_core_audit_log,biz_core_report,biz_core_indicator;
END$$
DELIMITER ;
CALL rollback_core_indicators();
DROP PROCEDURE rollback_core_indicators;
