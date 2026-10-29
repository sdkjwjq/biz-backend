-- Run while application writes are stopped; make a database backup first.
-- Only leader_id and update_time change. No approval, performance or level-four writes.
CREATE TABLE IF NOT EXISTS bak_20261029_task_leader LIKE biz_task;
CREATE TABLE IF NOT EXISTS sys_task_leader_repair_20261029 (
  task_id BIGINT PRIMARY KEY, batch_id VARCHAR(36) NOT NULL,
  leader_before_id BIGINT NOT NULL, leader_after_id BIGINT NOT NULL,
  update_time_after DATETIME NOT NULL, repaired_at DATETIME NOT NULL,
  database_operator VARCHAR(288) NOT NULL, rolled_back_at DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DROP PROCEDURE IF EXISTS repair_task_leader_20261029;
DELIMITER $$
CREATE PROCEDURE repair_task_leader_20261029()
BEGIN
  DECLARE v_count INT DEFAULT 0;
  DECLARE v_applied INT DEFAULT 0;
  DECLARE v_batch VARCHAR(36);
  DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
  START TRANSACTION;
  SET v_batch=UUID();
  -- Lock the target and corroborating role/scope rows before classifying candidates.
  SELECT t.task_id locked_task_id FROM tmp_task_leader_expected e
    JOIN biz_task t ON t.task_id=e.task_id
    LEFT JOIN biz_task p ON p.task_id=t.parent_id
    LEFT JOIN biz_task r ON r.task_id=e.root_id
    LEFT JOIN sys_user u ON u.user_id=e.old_leader_id ORDER BY t.task_id FOR UPDATE;
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
  -- A rolled-back repair is never silently reapplied, and first backups are immutable.
  DELETE p FROM tmp_task_leader_preview p JOIN sys_task_leader_repair_20261029 h ON h.task_id=p.task_id;
  DELETE FROM tmp_task_leader_preview WHERE repair_status<>'READY';
  SELECT COUNT(*) INTO v_count FROM tmp_task_leader_preview;
  INSERT INTO bak_20261029_task_leader
    SELECT t.* FROM biz_task t JOIN tmp_task_leader_preview p ON p.task_id=t.task_id;
  INSERT INTO sys_task_leader_repair_20261029
    (task_id,batch_id,leader_before_id,leader_after_id,update_time_after,repaired_at,database_operator)
    SELECT t.task_id,v_batch,t.leader_id,p.old_leader_id,
      GREATEST(NOW(),DATE_ADD(COALESCE(t.update_time,'1970-01-01'),INTERVAL 1 SECOND)),NOW(),CURRENT_USER()
    FROM biz_task t JOIN tmp_task_leader_preview p ON p.task_id=t.task_id;
  UPDATE biz_task t JOIN sys_task_leader_repair_20261029 h ON h.task_id=t.task_id AND BINARY h.batch_id=BINARY v_batch
    SET t.leader_id=h.leader_after_id,t.update_time=h.update_time_after
    WHERE t.leader_id <=> h.leader_before_id;
  SET v_applied=ROW_COUNT();
  IF v_applied<>v_count THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Owner repair changed concurrently; no rows committed';
  END IF;
  COMMIT;
  SELECT 'repaired_tasks' result,v_applied task_count;
  SELECT 'repair_batch' result,v_batch batch_id;
END$$
DELIMITER ;
CALL repair_task_leader_20261029();
DROP PROCEDURE repair_task_leader_20261029;
