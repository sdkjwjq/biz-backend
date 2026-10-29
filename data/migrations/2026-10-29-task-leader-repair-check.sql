-- Business read-only preview. Temporary tables are connection-local.
DROP TEMPORARY TABLE IF EXISTS tmp_task_leader_preview;
CREATE TEMPORARY TABLE tmp_task_leader_preview AS
SELECT e.*, t.task_name, t.leader_id current_leader_id,
  COALESCE(NULLIF(u.nick_name,''),u.user_name) restored_leader_name,
  (SELECT COUNT(*) FROM biz_material_submission s WHERE s.task_id=t.task_id
     AND COALESCE(s.is_delete,0)=0 AND s.flow_status IN(10,20,30)) active_audits,
  CASE
    WHEN h.task_id IS NOT NULL AND h.rolled_back_at IS NOT NULL THEN 'REPAIR_PREVIOUSLY_ROLLED_BACK'
    WHEN h.task_id IS NOT NULL AND t.leader_id <=> h.leader_after_id THEN 'ALREADY_REPAIRED'
    WHEN h.task_id IS NOT NULL THEN 'POST_REPAIR_CHANGED'
    WHEN t.task_id IS NULL OR COALESCE(t.is_delete,0)<>0 THEN 'MISSING_OR_DELETED'
    WHEN NOT(t.level <=> 3) OR NOT(t.task_code <=> e.task_code) OR NOT(t.phase <=> e.phase)
      OR NOT(t.project_id <=> e.project_id) OR NOT(t.parent_id <=> e.parent_id) THEN 'IDENTITY_CHANGED'
    WHEN t.leader_id <=> e.old_leader_id THEN 'ALREADY_CORRECT'
    WHEN NOT(t.leader_id <=> e.wrong_leader_id) THEN 'OWNER_CHANGED'
    WHEN EXISTS(SELECT 1 FROM biz_ownership_change_log l WHERE l.target_type='TASK'
      AND l.target_id=t.task_id AND NOT(l.leader_before_id <=> l.leader_after_id)) THEN 'OWNER_TRANSFER_RECORDED'
    WHEN p.task_id IS NULL OR NOT(p.level <=> 2) OR COALESCE(p.is_delete,0)<>0
      OR NOT(p.parent_id <=> e.root_id) OR r.task_id IS NULL OR NOT(r.level <=> 1)
      OR COALESCE(r.is_delete,0)<>0 OR NOT(r.leader_id <=> e.old_leader_id) THEN 'ROOT_OR_SCOPE_CHANGED'
    WHEN u.user_id IS NULL OR COALESCE(u.is_delete,0)<>0 OR NOT(BINARY u.status <=> BINARY '1') THEN 'OWNER_UNAVAILABLE'
    ELSE 'READY'
  END repair_status
FROM tmp_task_leader_expected e
LEFT JOIN biz_task t ON t.task_id=e.task_id
LEFT JOIN biz_task p ON p.task_id=t.parent_id
LEFT JOIN biz_task r ON r.task_id=e.root_id
LEFT JOIN sys_user u ON u.user_id=e.old_leader_id
LEFT JOIN tmp_task_leader_history h ON h.task_id=e.task_id;
SELECT repair_status, COUNT(*) task_count, SUM(active_audits) preserved_active_audits
FROM tmp_task_leader_preview GROUP BY repair_status ORDER BY repair_status;
SELECT task_id,task_code,phase,task_name,current_leader_id,old_leader_id restored_leader_id,
  restored_leader_name,repair_status,active_audits
FROM tmp_task_leader_preview ORDER BY task_id;
