-- 人工回滚仅限修复后尚无新填报时；正式发布优先使用更新前整库备份。
DELIMITER //
DROP PROCEDURE IF EXISTS rollback_core_mapping_20261014//
CREATE PROCEDURE rollback_core_mapping_20261014()
BEGIN
 IF EXISTS(SELECT 1 FROM biz_core_report r LEFT JOIN bak_20261014_core_report_source b ON b.report_id=r.report_id WHERE b.report_id IS NULL) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='New core reports exist; preserve business history and use reviewed backup recovery';
 END IF;
 START TRANSACTION;
 UPDATE biz_core_indicator c JOIN bak_20261014_core_mapping b ON b.indicator_id=c.indicator_id SET c.indicator_code=b.indicator_code;
 UPDATE biz_core_report r JOIN bak_20261014_core_report_source b ON b.report_id=r.report_id SET r.performance_code=b.performance_code,r.draft_key=b.draft_key;
 UPDATE sys_notice n JOIN bak_20261014_core_notice b ON b.notice_id=n.notice_id SET n.is_delete=b.is_delete;
 COMMIT;
END//
CALL rollback_core_mapping_20261014()//
DROP PROCEDURE rollback_core_mapping_20261014//
DELIMITER ;
