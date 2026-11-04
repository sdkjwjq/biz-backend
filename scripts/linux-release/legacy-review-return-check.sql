-- Read-only candidate list. Final eligibility also checks current permissions,
-- values, latest log and absence of subsequent submissions in the Java helper.
SELECT t.task_id,t.task_code,s.sub_id,s.flow_status,s.current_handler_id,
       t.auditor_id,t.principal_id,s.reported_value,t.current_value,
       l.pre_status,l.post_status,l.create_time AS return_time
FROM biz_task t JOIN biz_material_submission s ON s.task_id=t.task_id
JOIN biz_audit_log l ON l.sub_id=s.sub_id
WHERE t.level=3 AND COALESCE(t.is_delete,0)=0
  AND s.flow_status<0 AND s.is_delete=1
  AND l.pre_status IN(20,30) AND l.post_status<0
  AND l.action_type IN('退回','驳回') AND l.create_time>='2026-11-03 00:00:00'
  AND l.create_time>=s.submit_time
  AND NOT EXISTS(SELECT 1 FROM biz_material_submission n WHERE n.task_id=t.task_id AND n.sub_id>s.sub_id)
  AND NOT EXISTS(SELECT 1 FROM biz_audit_log n WHERE n.sub_id=s.sub_id
      AND (n.create_time>l.create_time OR (n.create_time=l.create_time AND n.log_id>l.log_id)))
ORDER BY t.task_id,s.sub_id;
