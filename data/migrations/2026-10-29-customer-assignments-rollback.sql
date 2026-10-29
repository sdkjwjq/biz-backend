-- Only return this batch's flags / three configuration fields; never erase later work.
DROP PROCEDURE IF EXISTS rollback_customer_update_20261029;
DELIMITER $$
CREATE PROCEDURE rollback_customer_update_20261029()
BEGIN
 DECLARE conflicts INT DEFAULT 0;
 DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK;RESIGNAL;END;
 START TRANSACTION;
 SELECT t.task_id FROM biz_task t JOIN sys_customer_update_20261029 h ON h.target_type='TASK' AND h.target_id=t.task_id AND h.rolled_back_at IS NULL ORDER BY t.task_id FOR UPDATE;
 SELECT p.perf_id FROM biz_performance p JOIN sys_customer_update_20261029 h ON h.target_type='PERFORMANCE' AND h.target_id=p.perf_id AND h.rolled_back_at IS NULL ORDER BY p.perf_id FOR UPDATE;
 SELECT COUNT(*) INTO conflicts FROM sys_customer_update_20261029 h LEFT JOIN biz_task t ON t.task_id=h.target_id AND h.target_type='TASK' LEFT JOIN biz_performance p ON p.perf_id=h.target_id AND h.target_type='PERFORMANCE'
 WHERE h.rolled_back_at IS NULL AND ((h.target_type='TASK' AND (t.task_id IS NULL OR COALESCE(t.is_delete,0)<>0 OR NOT(t.update_time <=> h.update_time_after) OR NOT(COALESCE(t.is_priority,0) <=> CAST(JSON_UNQUOTE(JSON_EXTRACT(h.after_json,'$.priority')) AS SIGNED)) OR NOT(COALESCE(t.is_difficult,0) <=> CAST(JSON_UNQUOTE(JSON_EXTRACT(h.after_json,'$.difficult')) AS SIGNED))))
 OR (h.target_type='PERFORMANCE' AND (p.perf_id IS NULL OR COALESCE(p.is_delete,0)<>0 OR NOT(p.update_time <=> h.update_time_after) OR NOT(p.leader_id <=> CAST(JSON_UNQUOTE(JSON_EXTRACT(h.after_json,'$.leader')) AS SIGNED)) OR NOT(p.auditor_id <=> CAST(JSON_UNQUOTE(JSON_EXTRACT(h.after_json,'$.auditor')) AS SIGNED)) OR NOT(p.dept_id <=> CAST(JSON_UNQUOTE(JSON_EXTRACT(h.after_json,'$.dept')) AS SIGNED)) OR NOT(p.principal_id <=> CAST(JSON_UNQUOTE(JSON_EXTRACT(h.after_json,'$.principal')) AS SIGNED)))));
 IF conflicts>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Customer assignments changed later; rollback refused';END IF;
 UPDATE biz_task t JOIN bak_20261029_customer_task b ON b.task_id=t.task_id JOIN sys_customer_update_20261029 h ON h.target_type='TASK' AND h.target_id=t.task_id AND h.rolled_back_at IS NULL SET t.is_priority=b.is_priority,t.is_difficult=b.is_difficult,t.management_version=COALESCE(t.management_version,0)+1,t.update_time=GREATEST(NOW(),DATE_ADD(t.update_time,INTERVAL 1 SECOND));
 UPDATE biz_performance p JOIN bak_20261029_customer_performance b ON b.perf_id=p.perf_id JOIN sys_customer_update_20261029 h ON h.target_type='PERFORMANCE' AND h.target_id=p.perf_id AND h.rolled_back_at IS NULL SET p.auditor_id=b.auditor_id,p.dept_id=b.dept_id,p.principal_id=b.principal_id,p.update_time=GREATEST(NOW(),DATE_ADD(p.update_time,INTERVAL 1 SECOND));
 UPDATE sys_customer_update_20261029 SET rolled_back_at=NOW() WHERE rolled_back_at IS NULL;
 COMMIT;
END$$
DELIMITER ;
CALL rollback_customer_update_20261029();DROP PROCEDURE rollback_customer_update_20261029;
