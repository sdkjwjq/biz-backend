-- Restore only our reviewer field when no later edits or active workflows exist; retain journal and backups.
START TRANSACTION;
UPDATE biz_performance p JOIN sys_chen_self_review_20261101 j ON j.perf_id=p.perf_id
SET p.auditor_id=j.before_auditor,p.update_time=j.before_time,j.rolled_back_at=NOW(3)
WHERE j.rolled_back_at IS NULL AND BINARY p.perf_code=BINARY j.perf_code AND p.leader_id=112120 AND p.auditor_id=j.after_auditor AND p.update_time <=> j.after_time
AND NOT EXISTS(SELECT 1 FROM biz_performance_submission s WHERE s.perf_id=p.perf_id AND COALESCE(s.is_delete,0)=0 AND s.flow_status IN(10,20))
AND NOT EXISTS(SELECT 1 FROM biz_core_report r WHERE BINARY r.performance_code=BINARY p.perf_code AND r.flow_status IN(10,20));
COMMIT;
SELECT perf_id,perf_code,rolled_back_at FROM sys_chen_self_review_20261101;
