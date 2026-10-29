"""Fixed approved Excel changes. No database connection and no workbook modification."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
PREFIX=ROOT/'data/migrations/2026-10-29-customer-assignments'
def write(suffix,text):Path(str(PREFIX)+suffix+'.sql').write_text(text,encoding='utf-8',newline='\n')
def main():
 d=json.loads((ROOT/'data/repairs/customer-assignments-20261029.json').read_text(encoding='utf-8'))
 assert len(d['task_flags'])==29 and len(d['performance_assignments'])==155 and sum(r['keep_auditor'] for r in d['performance_assignments'])==17
 tasks=','.join("(%d,'%s',%d,%d)"%(r['id'],r['code'],r['priority'],r['difficult']) for r in d['task_flags'])
 perf=','.join("(%d,'%s',%d,%d,%d,%d,%d,%d,%d,%d)"%(r['id'],r['code'],r['leader'],r['auditor'],r['dept'],r['principal'],r['keep_auditor'],r['old_auditor'],r['old_dept'],r['old_principal']) for r in d['performance_assignments'])
 write('-candidates',f'''SET NAMES utf8mb4;
DROP TEMPORARY TABLE IF EXISTS customer_tasks;
CREATE TEMPORARY TABLE customer_tasks(id BIGINT PRIMARY KEY,code VARCHAR(64) COLLATE utf8mb4_bin,priority TINYINT,difficult TINYINT);
INSERT INTO customer_tasks VALUES {tasks};
DROP TEMPORARY TABLE IF EXISTS customer_performance;
CREATE TEMPORARY TABLE customer_performance(id BIGINT PRIMARY KEY,code VARCHAR(64) COLLATE utf8mb4_bin,leader BIGINT,auditor BIGINT,dept BIGINT,principal BIGINT,keep_auditor TINYINT,old_auditor BIGINT,old_dept BIGINT,old_principal BIGINT);
INSERT INTO customer_performance VALUES {perf};
DROP TEMPORARY TABLE IF EXISTS customer_history;
CREATE TEMPORARY TABLE customer_history(target_type VARCHAR(20),target_id BIGINT,rolled_back_at DATETIME);
SET @customer_history_sql=IF(EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sys_customer_update_20261029'),'INSERT INTO customer_history SELECT target_type,target_id,rolled_back_at FROM sys_customer_update_20261029','SELECT 1');
PREPARE customer_history_stmt FROM @customer_history_sql; EXECUTE customer_history_stmt; DEALLOCATE PREPARE customer_history_stmt;
''')
 task_view='''DROP TEMPORARY TABLE IF EXISTS customer_task_preview;
CREATE TEMPORARY TABLE customer_task_preview AS SELECT e.*,t.task_name,t.update_time,
 CASE WHEN h.target_id IS NOT NULL THEN 'ALREADY_RECORDED'
 WHEN t.task_id IS NULL OR COALESCE(t.is_delete,0)<>0 OR NOT(t.level <=> 3) OR NOT(t.phase <=> 2026) OR NOT(BINARY t.task_code <=> BINARY e.code) THEN 'IDENTITY_CHANGED'
 WHEN COALESCE(t.is_priority,0)=e.priority AND COALESCE(t.is_difficult,0)=e.difficult THEN 'ALREADY_CORRECT'
 ELSE 'READY' END result
 FROM customer_tasks e LEFT JOIN biz_task t ON t.task_id=e.id LEFT JOIN customer_history h ON h.target_type='TASK' AND h.target_id=e.id;
'''
 perf_view='''DROP TEMPORARY TABLE IF EXISTS customer_perf_preview;
CREATE TEMPORARY TABLE customer_perf_preview AS SELECT e.*,p.perf_name,p.update_time,p.auditor_id current_auditor,
 CASE WHEN h.target_id IS NOT NULL THEN 'ALREADY_RECORDED'
 WHEN p.perf_id IS NULL OR COALESCE(p.is_delete,0)<>0 OR NOT(BINARY p.perf_code <=> BINARY e.code) THEN 'IDENTITY_CHANGED'
 WHEN NOT(p.leader_id <=> e.leader) THEN 'OWNER_NOT_CONFIRMED'
 WHEN (SELECT COUNT(*) FROM sys_user u WHERE u.user_id IN(e.leader,IF(e.keep_auditor=1,p.auditor_id,e.auditor),e.principal) AND status='1' AND COALESCE(is_delete,0)=0) <
      (1+NOT(e.leader <=> IF(e.keep_auditor=1,p.auditor_id,e.auditor))+NOT(e.principal <=> e.leader AND e.principal <=> IF(e.keep_auditor=1,p.auditor_id,e.auditor))) THEN 'INVALID_PEOPLE'
 WHEN NOT EXISTS(SELECT 1 FROM sys_dept dept WHERE dept.dept_id=e.dept AND dept.status='0' AND COALESCE(dept.is_delete,0)=0) THEN 'INVALID_DEPARTMENT'
 WHEN (e.keep_auditor=1 OR p.auditor_id <=> e.auditor) AND p.dept_id <=> e.dept AND p.principal_id <=> e.principal THEN 'ALREADY_CORRECT'
 WHEN (e.keep_auditor=0 AND NOT(p.auditor_id <=> e.old_auditor) AND NOT(p.auditor_id <=> e.auditor))
   OR (NOT(p.dept_id <=> e.old_dept) AND NOT(p.dept_id <=> e.dept)) OR (NOT(p.principal_id <=> e.old_principal) AND NOT(p.principal_id <=> e.principal)) THEN 'CONFIG_CHANGED'
 WHEN EXISTS(SELECT 1 FROM biz_performance_submission s WHERE s.perf_id=p.perf_id AND COALESCE(s.is_delete,0)=0 AND s.flow_status IN(10,20))
   OR EXISTS(SELECT 1 FROM biz_core_report c WHERE BINARY c.performance_code=BINARY p.perf_code AND c.flow_status IN(10,20)) THEN 'ACTIVE_AUDIT'
 ELSE 'READY' END result
 FROM customer_performance e LEFT JOIN biz_performance p ON p.perf_id=e.id LEFT JOIN customer_history h ON h.target_type='PERFORMANCE' AND h.target_id=e.id;
'''
 # Count distinct proposed people correctly, including legitimate same-account role assignments.
 old="(1+NOT(e.leader <=> IF(e.keep_auditor=1,p.auditor_id,e.auditor))+NOT(e.principal <=> e.leader AND e.principal <=> IF(e.keep_auditor=1,p.auditor_id,e.auditor)))"
 new="(1+IF(e.leader <=> IF(e.keep_auditor=1,p.auditor_id,e.auditor),0,1)+IF(e.principal <=> e.leader OR e.principal <=> IF(e.keep_auditor=1,p.auditor_id,e.auditor),0,1))"
 perf_view=perf_view.replace(old,new)
 summary="SELECT 'TASK' target_type,result,COUNT(*) row_count FROM customer_task_preview GROUP BY result UNION ALL SELECT 'PERFORMANCE',result,COUNT(*) FROM customer_perf_preview GROUP BY result;\nSELECT 'TASK' target_type,id,code,result FROM customer_task_preview WHERE result NOT IN('READY','ALREADY_CORRECT','ALREADY_RECORDED') UNION ALL SELECT 'PERFORMANCE',id,code,result FROM customer_perf_preview WHERE result NOT IN('READY','ALREADY_CORRECT','ALREADY_RECORDED');\n"
 write('-check',task_view+perf_view+summary)
 write('', '''-- Application writes must be stopped. Backups and journal survive rollback.
CREATE TABLE IF NOT EXISTS bak_20261029_customer_task LIKE biz_task;
CREATE TABLE IF NOT EXISTS bak_20261029_customer_performance LIKE biz_performance;
CREATE TABLE IF NOT EXISTS sys_customer_update_20261029(target_type VARCHAR(20),target_id BIGINT,batch_id VARCHAR(36),before_json JSON,after_json JSON,database_operator VARCHAR(288),create_time DATETIME(3),rolled_back_at DATETIME NULL,update_time_after DATETIME NULL,PRIMARY KEY(target_type,target_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP PROCEDURE IF EXISTS customer_update_20261029;
DELIMITER $$
CREATE PROCEDURE customer_update_20261029()
BEGIN
 DECLARE batch VARCHAR(36);
 DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK;RESIGNAL;END;
 START TRANSACTION;SET batch=UUID();
 SELECT t.task_id FROM biz_task t JOIN customer_tasks e ON e.id=t.task_id ORDER BY t.task_id FOR UPDATE;
 SELECT p.perf_id FROM biz_performance p JOIN customer_performance e ON e.id=p.perf_id ORDER BY p.perf_id FOR UPDATE;
''' + task_view+perf_view+summary+'''
 INSERT INTO bak_20261029_customer_task SELECT t.* FROM biz_task t JOIN customer_task_preview e ON e.id=t.task_id WHERE e.result IN('READY','ALREADY_CORRECT');
 INSERT INTO bak_20261029_customer_performance SELECT p.* FROM biz_performance p JOIN customer_perf_preview e ON e.id=p.perf_id WHERE e.result IN('READY','ALREADY_CORRECT');
 INSERT INTO sys_customer_update_20261029 SELECT 'TASK',t.task_id,batch,JSON_OBJECT('priority',t.is_priority,'difficult',t.is_difficult),JSON_OBJECT('priority',e.priority,'difficult',e.difficult),CURRENT_USER(),NOW(3),NULL,NULL FROM biz_task t JOIN customer_task_preview e ON e.id=t.task_id WHERE e.result IN('READY','ALREADY_CORRECT');
 INSERT INTO sys_customer_update_20261029 SELECT 'PERFORMANCE',p.perf_id,batch,JSON_OBJECT('leader',p.leader_id,'auditor',p.auditor_id,'dept',p.dept_id,'principal',p.principal_id),JSON_OBJECT('leader',p.leader_id,'auditor',IF(e.keep_auditor=1,p.auditor_id,e.auditor),'dept',e.dept,'principal',e.principal),CURRENT_USER(),NOW(3),NULL,NULL FROM biz_performance p JOIN customer_perf_preview e ON e.id=p.perf_id WHERE e.result IN('READY','ALREADY_CORRECT');
 UPDATE biz_task t JOIN customer_task_preview e ON e.id=t.task_id AND e.result='READY' SET t.is_priority=e.priority,t.is_difficult=e.difficult,t.management_version=COALESCE(t.management_version,0)+1,t.update_time=GREATEST(NOW(),DATE_ADD(COALESCE(t.update_time,'1970-01-01'),INTERVAL 1 SECOND));
 UPDATE biz_performance p JOIN customer_perf_preview e ON e.id=p.perf_id AND e.result='READY' SET p.auditor_id=IF(e.keep_auditor=1,p.auditor_id,e.auditor),p.dept_id=e.dept,p.principal_id=e.principal,p.update_time=GREATEST(NOW(),DATE_ADD(COALESCE(p.update_time,'1970-01-01'),INTERVAL 1 SECOND));
 UPDATE sys_customer_update_20261029 h JOIN biz_task t ON h.target_type='TASK' AND h.target_id=t.task_id SET h.update_time_after=t.update_time WHERE BINARY h.batch_id=BINARY batch;
 UPDATE sys_customer_update_20261029 h JOIN biz_performance p ON h.target_type='PERFORMANCE' AND h.target_id=p.perf_id SET h.update_time_after=p.update_time WHERE BINARY h.batch_id=BINARY batch;
 COMMIT;
END$$
DELIMITER ;
CALL customer_update_20261029();DROP PROCEDURE customer_update_20261029;
''')
 write('-rollback','''-- Only return this batch's flags / three configuration fields; never erase later work.
DROP PROCEDURE IF EXISTS rollback_customer_update_20261029;
DELIMITER $$
CREATE PROCEDURE rollback_customer_update_20261029()
BEGIN
 DECLARE conflicts INT DEFAULT 0;
 DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK;RESIGNAL;END;
 START TRANSACTION;
 SELECT t.task_id FROM biz_task t JOIN sys_customer_update_20261029 h ON h.target_type='TASK' AND h.target_id=t.task_id AND h.rolled_back_at IS NULL ORDER BY t.task_id FOR UPDATE;
 SELECT p.perf_id FROM biz_performance p JOIN sys_customer_update_20261029 h ON h.target_type='PERFORMANCE' AND h.target_id=p.perf_id AND h.rolled_back_at IS NULL ORDER BY p.perf_id FOR UPDATE;
 SELECT COUNT(*) INTO conflicts FROM sys_customer_update_20261029 h LEFT JOIN biz_task t ON t.task_id=h.target_id AND h.target_type='TASK' LEFT JOIN biz_performance p ON p.perf_id=h.target_id AND h.target_type='PERFORMANCE'
 WHERE h.rolled_back_at IS NULL AND ((h.target_type='TASK' AND (t.task_id IS NULL OR COALESCE(t.is_delete,0)<>0 OR NOT(t.update_time <=> h.update_time_after) OR NOT(COALESCE(t.is_priority,0) <=> CAST(JSON_UNQUOTE(JSON_EXTRACT(h.after_json,'$.priority')) AS SIGNED)) OR NOT(COALESCE(t.is_difficult,0) <=> CAST(JSON_UNQUOTE(JSON_EXTRACT(h.after_json,'$.difficult')) AS SIGNED))))
 OR (h.target_type='PERFORMANCE' AND (p.perf_id IS NULL OR COALESCE(p.is_delete,0)<>0 OR NOT(p.update_time <=> h.update_time_after) OR NOT(p.leader_id <=> CAST(JSON_UNQUOTE(JSON_EXTRACT(h.after_json,'$.leader')) AS SIGNED)) OR NOT(p.auditor_id <=> CAST(JSON_UNQUOTE(JSON_EXTRACT(h.after_json,'$.auditor')) AS SIGNED)) OR NOT(p.dept_id <=> CAST(JSON_UNQUOTE(JSON_EXTRACT(h.after_json,'$.dept')) AS SIGNED)) OR NOT(p.principal_id <=> CAST(JSON_UNQUOTE(JSON_EXTRACT(h.after_json,'$.principal')) AS SIGNED)))));
 IF conflicts>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Customer assignments changed later; rollback refused';END IF;
 UPDATE biz_task t JOIN bak_20261029_customer_task b ON b.task_id=t.task_id JOIN sys_customer_update_20261029 h ON h.target_type='TASK' AND h.target_id=t.task_id AND h.rolled_back_at IS NULL SET t.is_priority=b.is_priority,t.is_difficult=b.is_difficult,t.management_version=COALESCE(t.management_version,0)+1,t.update_time=GREATEST(NOW(),DATE_ADD(t.update_time,INTERVAL 1 SECOND));
 UPDATE biz_performance p JOIN bak_20261029_customer_performance b ON b.perf_id=p.perf_id JOIN sys_customer_update_20261029 h ON h.target_type='PERFORMANCE' AND h.target_id=p.perf_id AND h.rolled_back_at IS NULL SET p.auditor_id=b.auditor_id,p.dept_id=b.dept_id,p.principal_id=b.principal_id,p.update_time=GREATEST(NOW(),DATE_ADD(p.update_time,INTERVAL 1 SECOND));
 UPDATE sys_customer_update_20261029 SET rolled_back_at=NOW() WHERE rolled_back_at IS NULL;
 COMMIT;
END$$
DELIMITER ;
CALL rollback_customer_update_20261029();DROP PROCEDURE rollback_customer_update_20261029;
''')
if __name__=='__main__':main()
