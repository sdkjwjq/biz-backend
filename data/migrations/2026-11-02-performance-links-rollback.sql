-- Stop writes and take a current full backup first. Audit logs and backups are retained.
SET NAMES utf8mb4;SET SESSION group_concat_max_len=16777216;
DROP PROCEDURE IF EXISTS rollback_performance_links_20261102;
DELIMITER $$
CREATE PROCEDURE rollback_performance_links_20261102()
main:BEGIN
 DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK;RESIGNAL;END;
 START TRANSACTION;
 IF EXISTS(SELECT 1 FROM sys_perf_links_20261102 WHERE id=1 AND state='ROLLED_BACK') THEN COMMIT;LEAVE main;END IF;
 IF NOT EXISTS(SELECT 1 FROM sys_perf_links_20261102 WHERE id=1 AND state='APPLIED') THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No applied performance-link migration to roll back';
 END IF;
 SELECT p.perf_id locked_performance FROM biz_performance p JOIN bak_20261102_perf_links_total b ON b.perf_id=p.perf_id ORDER BY p.perf_id FOR UPDATE;
 SELECT y.year_id locked_year FROM biz_performance_year y JOIN bak_20261102_perf_links_year b ON b.year_id=y.year_id ORDER BY y.year_id FOR UPDATE;
 SELECT r.id locked_relation FROM rel_task_performance r JOIN bak_20261102_perf_links_total b ON b.perf_id=r.perf_id ORDER BY r.id FOR UPDATE;
 SELECT t.task_id locked_task FROM biz_task t JOIN bak_20261102_perf_links_task b ON b.task_id=t.task_id ORDER BY t.task_id FOR UPDATE;
SELECT SHA2(COALESCE(GROUP_CONCAT(SHA2(CAST(JSON_ARRAY(r.id,r.task_id,r.perf_id,r.year_id,r.weight,r.contribution_value,r.data_type,r.is_delete) AS CHAR),256) ORDER BY r.id SEPARATOR '|'),''),256) INTO @pl_rel_hash FROM rel_task_performance r WHERE r.perf_id IN(SELECT perf_id FROM bak_20261102_perf_links_total);
SELECT SHA2(COALESCE(GROUP_CONCAT(SHA2(CAST(JSON_ARRAY(r.year_id,r.perf_id,r.year,r.target_value,r.actual_value,r.data_type,r.is_delete) AS CHAR),256) ORDER BY r.year_id SEPARATOR '|'),''),256) INTO @pl_year_hash FROM biz_performance_year r WHERE r.perf_id IN(SELECT perf_id FROM bak_20261102_perf_links_total);
SELECT SHA2(COALESCE(GROUP_CONCAT(SHA2(CAST(JSON_ARRAY(r.perf_id,r.perf_code,r.current_value,r.update_time,r.target_value,r.data_type,r.is_delete,r.leader_id,r.auditor_id,r.principal_id,r.dept_id) AS CHAR),256) ORDER BY r.perf_id SEPARATOR '|'),''),256) INTO @pl_perf_hash FROM biz_performance r WHERE r.perf_id IN(SELECT perf_id FROM bak_20261102_perf_links_total);
SELECT SHA2(COALESCE(GROUP_CONCAT(SHA2(CAST(JSON_ARRAY(r.task_id,r.task_code,r.phase,r.level,r.data_type,r.target_value,r.current_value,r.is_delete) AS CHAR),256) ORDER BY r.task_id SEPARATOR '|'),''),256) INTO @pl_task_hash FROM biz_task r WHERE r.task_id IN(SELECT task_id FROM bak_20261102_perf_links_task);
IF EXISTS(SELECT 1 FROM sys_perf_links_20261102 WHERE id=1 AND
 (NOT(rel_hash <=> @pl_rel_hash) OR NOT(year_hash <=> @pl_year_hash) OR NOT(perf_hash <=> @pl_perf_hash) OR NOT(task_hash <=> @pl_task_hash)))
 OR EXISTS(SELECT 1 FROM biz_performance_submission s JOIN bak_20261102_perf_links_total b ON b.perf_id=s.perf_id
 WHERE COALESCE(s.is_delete,0)=0 AND s.flow_status IN(10,20)) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Later edits, task values or active approvals detected; rollback refused';
 END IF;
INSERT INTO biz_performance_relation_log(perf_id,perf_code,perf_name,year,action,task_id,task_code,task_name,batch_id,operator_id,reason,create_time)
SELECT l.perf_id,l.perf_code,l.perf_name,l.year,IF(l.action='ADD','REMOVE','ADD'),l.task_id,l.task_code,l.task_name,
 'perf-links-20261102-v1-rollback',0,CONCAT('系统回滚经确认的绩效关联迁移；数据库操作者：',CURRENT_USER()),NOW()
 FROM biz_performance_relation_log l WHERE BINARY l.batch_id=BINARY 'perf-links-20261102-v1';
 UPDATE rel_task_performance r JOIN bak_20261102_perf_links_total p ON p.perf_id=r.perf_id
 LEFT JOIN bak_20261102_perf_links_rel b ON b.id=r.id SET r.is_delete=1 WHERE b.id IS NULL;
 UPDATE rel_task_performance r JOIN bak_20261102_perf_links_rel b ON b.id=r.id SET r.is_delete=b.is_delete;
 UPDATE biz_performance_year y JOIN bak_20261102_perf_links_year b ON b.year_id=y.year_id SET y.actual_value=b.actual_value;
 UPDATE biz_performance_year y JOIN sys_perf_links_20261102_years n ON n.year_id=y.year_id SET y.actual_value=0,y.is_delete=1;
 UPDATE biz_performance p JOIN bak_20261102_perf_links_total b ON b.perf_id=p.perf_id SET p.current_value=b.current_value,p.update_time=b.update_time;
 UPDATE sys_perf_links_20261102 SET state='ROLLED_BACK',rolled_back_at=NOW(3) WHERE id=1;
 COMMIT;
 SELECT 'performance_links_rolled_back' result;
END$$
DELIMITER ;
CALL rollback_performance_links_20261102();DROP PROCEDURE rollback_performance_links_20261102;
