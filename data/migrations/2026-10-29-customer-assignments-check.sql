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
