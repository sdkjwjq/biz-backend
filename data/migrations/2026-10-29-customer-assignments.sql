-- Application writes must be stopped. Backups and journal survive rollback.
CREATE TABLE IF NOT EXISTS bak_20261029_customer_task LIKE biz_task;
CREATE TABLE IF NOT EXISTS bak_20261029_customer_performance LIKE biz_performance;
CREATE TABLE IF NOT EXISTS sys_customer_update_20261029(target_type VARCHAR(20),target_id BIGINT,batch_id VARCHAR(36),before_json JSON,after_json JSON,database_operator VARCHAR(288),create_time DATETIME(3),rolled_back_at DATETIME NULL,update_time_after DATETIME NULL,PRIMARY KEY(target_type,target_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP PROCEDURE IF EXISTS customer_update_20261029;
DELIMITER $$
CREATE PROCEDURE customer_update_20261029()
BEGIN
 DECLARE batch VARCHAR(36);
 DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK;RESIGNAL;END;
 START TRANSACTION;SET batch=UUID();
 SELECT t.task_id FROM biz_task t JOIN customer_tasks e ON e.id=t.task_id ORDER BY t.task_id FOR UPDATE;
 SELECT p.perf_id FROM biz_performance p JOIN customer_performance e ON e.id=p.perf_id ORDER BY p.perf_id FOR UPDATE;
DROP TEMPORARY TABLE IF EXISTS customer_task_preview;
CREATE TEMPORARY TABLE customer_task_preview AS SELECT e.*,t.task_name,t.update_time,
 CASE WHEN h.target_id IS NOT NULL THEN 'ALREADY_RECORDED'
 WHEN t.task_id IS NULL OR COALESCE(t.is_delete,0)<>0 OR NOT(t.level <=> 3) OR NOT(t.phase <=> 2026) OR NOT(BINARY t.task_code <=> BINARY e.code) THEN 'IDENTITY_CHANGED'
 WHEN COALESCE(t.is_priority,0)=e.priority AND COALESCE(t.is_difficult,0)=e.difficult THEN 'ALREADY_CORRECT'
 ELSE 'READY' END result
 FROM customer_tasks e LEFT JOIN biz_task t ON t.task_id=e.id LEFT JOIN customer_history h ON h.target_type='TASK' AND h.target_id=e.id;
DROP TEMPORARY TABLE IF EXISTS customer_perf_preview;
CREATE TEMPORARY TABLE customer_perf_preview AS SELECT e.*,p.perf_name,p.update_time,p.auditor_id current_auditor,
 CASE WHEN h.target_id IS NOT NULL THEN 'ALREADY_RECORDED'
 WHEN p.perf_id IS NULL OR COALESCE(p.is_delete,0)<>0 OR NOT(BINARY p.perf_code <=> BINARY e.code) THEN 'IDENTITY_CHANGED'
 WHEN NOT(p.leader_id <=> e.leader) THEN 'OWNER_NOT_CONFIRMED'
 WHEN (SELECT COUNT(*) FROM sys_user u WHERE u.user_id IN(e.leader,IF(e.keep_auditor=1,p.auditor_id,e.auditor),e.principal) AND status='1' AND COALESCE(is_delete,0)=0) <
      (1+IF(e.leader <=> IF(e.keep_auditor=1,p.auditor_id,e.auditor),0,1)+IF(e.principal <=> e.leader OR e.principal <=> IF(e.keep_auditor=1,p.auditor_id,e.auditor),0,1)) THEN 'INVALID_PEOPLE'
 WHEN NOT EXISTS(SELECT 1 FROM sys_dept dept WHERE dept.dept_id=e.dept AND dept.status='0' AND COALESCE(dept.is_delete,0)=0) THEN 'INVALID_DEPARTMENT'
 WHEN (e.keep_auditor=1 OR p.auditor_id <=> e.auditor) AND p.dept_id <=> e.dept AND p.principal_id <=> e.principal THEN 'ALREADY_CORRECT'
 WHEN (e.keep_auditor=0 AND NOT(p.auditor_id <=> e.old_auditor) AND NOT(p.auditor_id <=> e.auditor))
   OR (NOT(p.dept_id <=> e.old_dept) AND NOT(p.dept_id <=> e.dept)) OR (NOT(p.principal_id <=> e.old_principal) AND NOT(p.principal_id <=> e.principal)) THEN 'CONFIG_CHANGED'
 WHEN EXISTS(SELECT 1 FROM biz_performance_submission s WHERE s.perf_id=p.perf_id AND COALESCE(s.is_delete,0)=0 AND s.flow_status IN(10,20))
   OR EXISTS(SELECT 1 FROM biz_core_report c WHERE BINARY c.performance_code=BINARY p.perf_code AND c.flow_status IN(10,20)) THEN 'ACTIVE_AUDIT'
 ELSE 'READY' END result
 FROM customer_performance e LEFT JOIN biz_performance p ON p.perf_id=e.id LEFT JOIN customer_history h ON h.target_type='PERFORMANCE' AND h.target_id=e.id;
SELECT 'TASK' target_type,result,COUNT(*) row_count FROM customer_task_preview GROUP BY result UNION ALL SELECT 'PERFORMANCE',result,COUNT(*) FROM customer_perf_preview GROUP BY result;
SELECT 'TASK' target_type,id,code,result FROM customer_task_preview WHERE result NOT IN('READY','ALREADY_CORRECT','ALREADY_RECORDED') UNION ALL SELECT 'PERFORMANCE',id,code,result FROM customer_perf_preview WHERE result NOT IN('READY','ALREADY_CORRECT','ALREADY_RECORDED');

 INSERT INTO bak_20261029_customer_task SELECT t.* FROM biz_task t JOIN customer_task_preview e ON e.id=t.task_id WHERE e.result IN('READY','ALREADY_CORRECT');
 INSERT INTO bak_20261029_customer_performance SELECT p.* FROM biz_performance p JOIN customer_perf_preview e ON e.id=p.perf_id WHERE e.result IN('READY','ALREADY_CORRECT');
 INSERT INTO sys_customer_update_20261029 SELECT 'TASK',t.task_id,batch,JSON_OBJECT('priority',t.is_priority,'difficult',t.is_difficult),JSON_OBJECT('priority',e.priority,'difficult',e.difficult),CURRENT_USER(),NOW(3),NULL,NULL FROM biz_task t JOIN customer_task_preview e ON e.id=t.task_id WHERE e.result IN('READY','ALREADY_CORRECT');
 INSERT INTO sys_customer_update_20261029 SELECT 'PERFORMANCE',p.perf_id,batch,JSON_OBJECT('leader',p.leader_id,'auditor',p.auditor_id,'dept',p.dept_id,'principal',p.principal_id),JSON_OBJECT('leader',p.leader_id,'auditor',IF(e.keep_auditor=1,p.auditor_id,e.auditor),'dept',e.dept,'principal',e.principal),CURRENT_USER(),NOW(3),NULL,NULL FROM biz_performance p JOIN customer_perf_preview e ON e.id=p.perf_id WHERE e.result IN('READY','ALREADY_CORRECT');
 UPDATE biz_task t JOIN customer_task_preview e ON e.id=t.task_id AND e.result='READY' SET t.is_priority=e.priority,t.is_difficult=e.difficult,t.management_version=COALESCE(t.management_version,0)+1,t.update_time=GREATEST(NOW(),DATE_ADD(COALESCE(t.update_time,'1970-01-01'),INTERVAL 1 SECOND));
 UPDATE biz_performance p JOIN customer_perf_preview e ON e.id=p.perf_id AND e.result='READY' SET p.auditor_id=IF(e.keep_auditor=1,p.auditor_id,e.auditor),p.dept_id=e.dept,p.principal_id=e.principal,p.update_time=GREATEST(NOW(),DATE_ADD(COALESCE(p.update_time,'1970-01-01'),INTERVAL 1 SECOND));
 UPDATE sys_customer_update_20261029 h JOIN biz_task t ON h.target_type='TASK' AND h.target_id=t.task_id SET h.update_time_after=t.update_time WHERE BINARY h.batch_id=BINARY batch;
 UPDATE sys_customer_update_20261029 h JOIN biz_performance p ON h.target_type='PERFORMANCE' AND h.target_id=p.perf_id SET h.update_time_after=p.update_time WHERE BINARY h.batch_id=BINARY batch;
 COMMIT;
END$$
DELIMITER ;
CALL customer_update_20261029();DROP PROCEDURE customer_update_20261029;
