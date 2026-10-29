-- Refuse to overwrite later work; retain backups and repair evidence.
DROP PROCEDURE IF EXISTS rollback_performance_leader_20261029;
DELIMITER $$
CREATE PROCEDURE rollback_performance_leader_20261029()
BEGIN
  DECLARE v_conflicts INT DEFAULT 0;
  DECLARE v_count INT DEFAULT 0;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
  START TRANSACTION;
  SELECT p.perf_id locked_perf_id FROM biz_performance p JOIN sys_performance_leader_repair_20261029 h
    ON h.perf_id=p.perf_id WHERE h.rolled_back_at IS NULL ORDER BY p.perf_id FOR UPDATE;
  SELECT COUNT(*) INTO v_conflicts FROM sys_performance_leader_repair_20261029 h
    LEFT JOIN biz_performance p ON p.perf_id=h.perf_id LEFT JOIN bak_20261029_performance_leader b ON b.perf_id=h.perf_id
    WHERE h.rolled_back_at IS NULL AND (p.perf_id IS NULL OR b.perf_id IS NULL OR COALESCE(p.is_delete,0)<>0
      OR NOT(p.leader_id <=> h.leader_after_id) OR NOT(p.update_time <=> h.update_time_after)
      OR NOT(BINARY p.perf_code <=> BINARY b.perf_code) OR NOT(p.project_id <=> b.project_id)
      OR NOT(p.parent_id <=> b.parent_id) OR NOT(BINARY p.ancestors <=> BINARY b.ancestors)
      OR NOT(b.leader_id <=> h.leader_before_id)
      OR EXISTS(SELECT 1 FROM biz_ownership_change_log l WHERE l.target_type='PERFORMANCE' AND l.target_id=h.perf_id
        AND l.create_time>=h.repaired_at AND NOT(l.leader_before_id <=> l.leader_after_id)));
  IF v_conflicts>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Performance changed after repair; rollback refused'; END IF;
  UPDATE biz_performance p JOIN sys_performance_leader_repair_20261029 h ON h.perf_id=p.perf_id AND h.rolled_back_at IS NULL
    SET p.leader_id=h.leader_before_id,p.update_time=GREATEST(NOW(),DATE_ADD(p.update_time,INTERVAL 1 SECOND));
  SET v_count=ROW_COUNT();
  UPDATE sys_performance_leader_repair_20261029 SET rolled_back_at=NOW() WHERE rolled_back_at IS NULL;
  COMMIT;
  SELECT 'rolled_back_performances' result,v_count performance_count;
END$$
DELIMITER ;
CALL rollback_performance_leader_20261029();
DROP PROCEDURE rollback_performance_leader_20261029;
