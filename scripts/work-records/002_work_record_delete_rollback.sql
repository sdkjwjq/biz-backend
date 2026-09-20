-- 仅在尚未发生逻辑删除时允许回滚结构；有删除记录则停止，不丢弃存档。
DELIMITER //
DROP PROCEDURE IF EXISTS rollback_work_record_delete_20260920//
CREATE PROCEDURE rollback_work_record_delete_20260920()
BEGIN
  IF EXISTS(SELECT 1 FROM biz_work_record WHERE delete_marker<>0) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Deleted work records exist; preserve archive data and stop rollback';
  END IF;
  ALTER TABLE biz_work_record DROP INDEX uk_work_record_month,
    ADD UNIQUE KEY uk_work_record_month(owner_id,record_year,record_month),
    DROP COLUMN delete_marker, DROP COLUMN deleted_by, DROP COLUMN deleted_time, DROP COLUMN delete_reason;
END//
DELIMITER ;
CALL rollback_work_record_delete_20260920();
DROP PROCEDURE rollback_work_record_delete_20260920;
