-- Read-only checks. Historical orphan/early rows intentionally remain in the original table.
SELECT 'task_raw_audit_logs',COUNT(*) FROM biz_audit_log;
SELECT 'task_effective_audit_logs',COUNT(*) FROM biz_audit_log l JOIN biz_material_submission s
  ON s.sub_id=l.sub_id AND l.create_time>=s.submit_time;
SELECT 'task_orphan_audit_logs_retained',COUNT(*) FROM biz_audit_log l
  WHERE NOT EXISTS(SELECT 1 FROM biz_material_submission s WHERE s.sub_id=l.sub_id);
SELECT 'task_early_audit_logs_retained',COUNT(*) FROM biz_audit_log l JOIN biz_material_submission s
  ON s.sub_id=l.sub_id WHERE l.create_time<s.submit_time;
SELECT 'active_task_pending',COUNT(*) FROM biz_material_submission s JOIN biz_task t ON t.task_id=s.task_id
  WHERE s.flow_status IN(10,20,30) AND COALESCE(s.is_delete,0)=0 AND COALESCE(t.is_delete,0)=0;
SELECT 'active_principal_handler_mismatch',COUNT(*) FROM biz_material_submission s JOIN biz_task t ON t.task_id=s.task_id
  WHERE s.flow_status=20 AND COALESCE(s.is_delete,0)=0 AND COALESCE(t.is_delete,0)=0
  AND NOT(s.current_handler_id<=>t.principal_id);
SELECT 'deleted_achievement_pending_residual',COUNT(*) FROM biz_achievement_submission s JOIN biz_achievement a ON a.ach_id=s.ach_id
  WHERE s.flow_status=10 AND COALESCE(s.is_delete,0)=0 AND COALESCE(a.is_delete,0)=1;
