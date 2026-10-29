-- Safe, all-or-nothing rollback; retain backups and repair evidence permanently.
-- Run with application writes stopped. Refuse any task modified after this repair.
DROP PROCEDURE IF EXISTS rollback_task_leader_20261029;
DELIMITER $$
CREATE PROCEDURE rollback_task_leader_20261029()
BEGIN
  DECLARE v_conflicts INT DEFAULT 0;
  DECLARE v_count INT DEFAULT 0;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
  START TRANSACTION;
  SELECT t.task_id locked_task_id FROM biz_task t JOIN sys_task_leader_repair_20261029 h
    ON h.task_id=t.task_id WHERE h.rolled_back_at IS NULL ORDER BY t.task_id FOR UPDATE;
  SELECT COUNT(*) INTO v_conflicts FROM sys_task_leader_repair_20261029 h
    LEFT JOIN biz_task t ON t.task_id=h.task_id
    LEFT JOIN bak_20261029_task_leader b ON b.task_id=h.task_id
    WHERE h.rolled_back_at IS NULL AND (t.task_id IS NULL OR b.task_id IS NULL
      OR COALESCE(t.is_delete,0)<>0 OR NOT(t.leader_id <=> h.leader_after_id)
      OR NOT(t.update_time <=> h.update_time_after) OR NOT(t.task_code <=> b.task_code)
      OR NOT(t.parent_id <=> b.parent_id) OR NOT(t.phase <=> b.phase)
      OR NOT(b.leader_id <=> h.leader_before_id)
      OR EXISTS(SELECT 1 FROM biz_ownership_change_log l WHERE l.target_type='TASK'
        AND l.target_id=h.task_id AND l.create_time>=h.repaired_at
        AND NOT(l.leader_before_id <=> l.leader_after_id)));
  IF v_conflicts>0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Tasks changed after owner repair; rollback refused; preserve later work';
  END IF;
  UPDATE biz_task t JOIN sys_task_leader_repair_20261029 h ON h.task_id=t.task_id AND h.rolled_back_at IS NULL
    SET t.leader_id=h.leader_before_id,
        t.update_time=GREATEST(NOW(),DATE_ADD(t.update_time,INTERVAL 1 SECOND));
  SET v_count=ROW_COUNT();
  UPDATE sys_task_leader_repair_20261029 SET rolled_back_at=NOW() WHERE rolled_back_at IS NULL;
  COMMIT;
  SELECT 'rolled_back_tasks' result,v_count task_count;
END$$
DELIMITER ;
CALL rollback_task_leader_20261029();
DROP PROCEDURE rollback_task_leader_20261029;
