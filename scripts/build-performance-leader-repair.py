"""Generate an owner-only performance repair from fixed historical/customer evidence."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PREFIX = ROOT / 'data/migrations/2026-10-29-performance-leader-repair'


def write(suffix, text):
    Path(str(PREFIX) + suffix + '.sql').write_text(text, encoding='utf-8', newline='\n')


def main():
    records = json.loads((ROOT / 'data/repairs/performance-leader-20261029.json').read_text(encoding='utf-8'))['performance']
    assert len(records) == len({r['perf_id'] for r in records}) == 155
    values = []
    for r in records:
        assert re.fullmatch(r'\d+(?:\.\d+)+', r['code']) and re.fullmatch(r'[\d.,]+', r['ancestors'])
        assert r['owner_before'] != r['owner_after']
        values.append("(%d,%d,%d,'%s','%s',%d,%d)" % (r['perf_id'], r['project_id'], r['parent_id'], r['ancestors'], r['code'], r['owner_before'], r['owner_after']))
    write('-candidates', '''-- Load candidates before forward/check SQL in the SAME connection.
-- Fixed historical wrong owners, independently corroborated by customer workbook column L.
SET NAMES utf8mb4;
DROP TEMPORARY TABLE IF EXISTS tmp_performance_leader_expected;
CREATE TEMPORARY TABLE tmp_performance_leader_expected (
  perf_id BIGINT PRIMARY KEY,project_id BIGINT,parent_id BIGINT,ancestors VARCHAR(500) COLLATE utf8mb4_bin,
  perf_code VARCHAR(64) COLLATE utf8mb4_bin,old_leader_id BIGINT,wrong_leader_id BIGINT
) ENGINE=InnoDB;
INSERT INTO tmp_performance_leader_expected VALUES
''' + ',\n'.join(values) + ''';
DROP TEMPORARY TABLE IF EXISTS tmp_performance_leader_history;
CREATE TEMPORARY TABLE tmp_performance_leader_history(perf_id BIGINT PRIMARY KEY,leader_after_id BIGINT,rolled_back_at DATETIME);
SET @performance_history_sql=IF(EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE()
  AND TABLE_NAME='sys_performance_leader_repair_20261029'),
  'INSERT INTO tmp_performance_leader_history SELECT perf_id,leader_after_id,rolled_back_at FROM sys_performance_leader_repair_20261029','SELECT 1');
PREPARE performance_history_stmt FROM @performance_history_sql;
EXECUTE performance_history_stmt;
DEALLOCATE PREPARE performance_history_stmt;
''')
    preview = '''DROP TEMPORARY TABLE IF EXISTS tmp_performance_leader_preview;
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
'''
    write('-check', '-- Business read-only preview. Only connection-local temporary tables are written.\n' + preview + '''SELECT repair_status,COUNT(*) performance_count,SUM(active_audits) preserved_active_audits
FROM tmp_performance_leader_preview GROUP BY repair_status ORDER BY repair_status;
SELECT perf_id,perf_code,perf_name,current_leader_id,old_leader_id restored_leader_id,
  restored_leader_name,repair_status,active_audits FROM tmp_performance_leader_preview ORDER BY perf_id;
''')
    write('', '''-- Stop application writes and take a full database backup first.
-- Only leader_id and update_time change. Keep reviewers, departments, values and all histories.
CREATE TABLE IF NOT EXISTS bak_20261029_performance_leader LIKE biz_performance;
CREATE TABLE IF NOT EXISTS sys_performance_leader_repair_20261029 (
  perf_id BIGINT PRIMARY KEY,batch_id VARCHAR(36) NOT NULL,
  leader_before_id BIGINT NOT NULL,leader_after_id BIGINT NOT NULL,
  update_time_after DATETIME NOT NULL,repaired_at DATETIME NOT NULL,
  database_operator VARCHAR(288) NOT NULL,rolled_back_at DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DROP PROCEDURE IF EXISTS repair_performance_leader_20261029;
DELIMITER $$
CREATE PROCEDURE repair_performance_leader_20261029()
BEGIN
  DECLARE v_count INT DEFAULT 0;
  DECLARE v_applied INT DEFAULT 0;
  DECLARE v_batch VARCHAR(36);
  DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
  START TRANSACTION;
  SET v_batch=UUID();
  SELECT p.perf_id locked_perf_id FROM tmp_performance_leader_expected e
    JOIN biz_performance p ON p.perf_id=e.perf_id LEFT JOIN sys_user u ON u.user_id=e.old_leader_id
    ORDER BY p.perf_id FOR UPDATE;
''' + preview + '''  DELETE p FROM tmp_performance_leader_preview p JOIN sys_performance_leader_repair_20261029 h ON h.perf_id=p.perf_id;
  DELETE FROM tmp_performance_leader_preview WHERE repair_status<>'READY';
  SELECT COUNT(*) INTO v_count FROM tmp_performance_leader_preview;
  INSERT INTO bak_20261029_performance_leader
    SELECT p.* FROM biz_performance p JOIN tmp_performance_leader_preview e ON e.perf_id=p.perf_id;
  INSERT INTO sys_performance_leader_repair_20261029
    (perf_id,batch_id,leader_before_id,leader_after_id,update_time_after,repaired_at,database_operator)
    SELECT p.perf_id,v_batch,p.leader_id,e.old_leader_id,
      GREATEST(NOW(),DATE_ADD(COALESCE(p.update_time,'1970-01-01'),INTERVAL 1 SECOND)),NOW(),CURRENT_USER()
    FROM biz_performance p JOIN tmp_performance_leader_preview e ON e.perf_id=p.perf_id;
  UPDATE biz_performance p JOIN sys_performance_leader_repair_20261029 h
    ON h.perf_id=p.perf_id AND BINARY h.batch_id=BINARY v_batch
    SET p.leader_id=h.leader_after_id,p.update_time=h.update_time_after WHERE p.leader_id <=> h.leader_before_id;
  SET v_applied=ROW_COUNT();
  IF v_applied<>v_count THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Performance owners changed concurrently; no rows committed'; END IF;
  COMMIT;
  SELECT 'repaired_performances' result,v_applied performance_count;
END$$
DELIMITER ;
CALL repair_performance_leader_20261029();
DROP PROCEDURE repair_performance_leader_20261029;
''')
    write('-rollback', '''-- Refuse to overwrite later work; retain backups and repair evidence.
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
''')


if __name__ == '__main__':
    main()
