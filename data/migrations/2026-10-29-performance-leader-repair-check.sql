-- Business read-only preview. Only connection-local temporary tables are written.
DROP TEMPORARY TABLE IF EXISTS tmp_performance_leader_preview;
CREATE TEMPORARY TABLE tmp_performance_leader_preview AS
SELECT e.*,p.perf_name,p.leader_id current_leader_id,
  COALESCE(NULLIF(u.nick_name,''),u.user_name) restored_leader_name,
  (SELECT COUNT(*) FROM biz_performance_submission s WHERE s.perf_id=p.perf_id
    AND COALESCE(s.is_delete,0)=0 AND s.flow_status IN(10,20)) active_audits,
  CASE
    WHEN h.perf_id IS NOT NULL AND h.rolled_back_at IS NOT NULL THEN 'REPAIR_PREVIOUSLY_ROLLED_BACK'
    WHEN h.perf_id IS NOT NULL AND p.leader_id <=> h.leader_after_id THEN 'ALREADY_REPAIRED'
    WHEN h.perf_id IS NOT NULL THEN 'POST_REPAIR_CHANGED'
    WHEN p.perf_id IS NULL OR COALESCE(p.is_delete,0)<>0 THEN 'MISSING_OR_DELETED'
    WHEN NOT(p.project_id <=> e.project_id) OR NOT(p.parent_id <=> e.parent_id)
      OR NOT(BINARY p.perf_code <=> BINARY e.perf_code) OR NOT(BINARY p.ancestors <=> BINARY e.ancestors) THEN 'IDENTITY_CHANGED'
    WHEN p.leader_id <=> e.old_leader_id THEN 'ALREADY_CORRECT'
    WHEN NOT(p.leader_id <=> e.wrong_leader_id) THEN 'OWNER_CHANGED'
    WHEN EXISTS(SELECT 1 FROM biz_ownership_change_log l WHERE l.target_type='PERFORMANCE'
      AND l.target_id=p.perf_id AND NOT(l.leader_before_id <=> l.leader_after_id)) THEN 'OWNER_TRANSFER_RECORDED'
    WHEN u.user_id IS NULL OR COALESCE(u.is_delete,0)<>0 OR NOT(BINARY u.status <=> BINARY '1') THEN 'OWNER_UNAVAILABLE'
    ELSE 'READY'
  END repair_status
FROM tmp_performance_leader_expected e LEFT JOIN biz_performance p ON p.perf_id=e.perf_id
LEFT JOIN sys_user u ON u.user_id=e.old_leader_id
LEFT JOIN tmp_performance_leader_history h ON h.perf_id=e.perf_id;
SELECT repair_status,COUNT(*) performance_count,SUM(active_audits) preserved_active_audits
FROM tmp_performance_leader_preview GROUP BY repair_status ORDER BY repair_status;
SELECT perf_id,perf_code,perf_name,current_leader_id,old_leader_id restored_leader_id,
  restored_leader_name,repair_status,active_audits FROM tmp_performance_leader_preview ORDER BY perf_id;
