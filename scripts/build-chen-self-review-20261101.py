"""Generate the newly confirmed seventeen assignments; never rewrite the delivered October migration."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[1];prefix=R/'data/migrations/2026-11-01-chen-self-review'
source=json.loads((R/'data/repairs/customer-assignments-20261029.json').read_text(encoding='utf-8'))
rows=[r for r in source['performance_assignments'] if r['keep_auditor']];assert len(rows)==17 and all(r['leader']==r['auditor']==112120 for r in rows)
(R/'data/repairs/chen-self-review-20261101.json').write_text(json.dumps({'confirmed':'Owner and reviewer are Chen Aolin 112120; manual review, no automatic approval','source':'customer-assignments-20261029.json','rows':rows},ensure_ascii=False,indent=2),encoding='utf-8')
seed="DROP TEMPORARY TABLE IF EXISTS chen_targets;CREATE TEMPORARY TABLE chen_targets(id BIGINT PRIMARY KEY,code VARCHAR(64) COLLATE utf8mb4_bin);\nINSERT INTO chen_targets VALUES "+','.join("(%d,'%s')"%(r['id'],r['code']) for r in rows)+";\n"
preview="""DROP TEMPORARY TABLE IF EXISTS chen_preview;
CREATE TEMPORARY TABLE chen_preview AS SELECT e.id,e.code,p.auditor_id before_auditor,p.update_time before_time,
CASE WHEN j.perf_id IS NOT NULL THEN 'ALREADY_RECORDED'
WHEN p.perf_id IS NULL OR COALESCE(p.is_delete,0)<>0 OR NOT(BINARY p.perf_code <=> BINARY e.code) THEN 'IDENTITY_CHANGED'
WHEN NOT(p.leader_id <=> 112120) OR (NOT(p.auditor_id <=> 110750) AND NOT(p.auditor_id <=> 112120)) THEN 'CONFIG_CHANGED'
WHEN NOT EXISTS(SELECT 1 FROM sys_user WHERE user_id=112120 AND BINARY nick_name=BINARY '陈奥林' AND status='1' AND COALESCE(is_delete,0)=0) THEN 'ACCOUNT_INVALID'
WHEN p.auditor_id <=> 112120 THEN 'ALREADY_CORRECT'
WHEN EXISTS(SELECT 1 FROM biz_performance_submission s WHERE s.perf_id=p.perf_id AND COALESCE(s.is_delete,0)=0 AND s.flow_status IN(10,20))
 OR EXISTS(SELECT 1 FROM biz_core_report r WHERE BINARY r.performance_code=BINARY p.perf_code AND r.flow_status IN(10,20)) THEN 'ACTIVE_AUDIT'
ELSE 'READY' END result
FROM chen_targets e LEFT JOIN biz_performance p ON p.perf_id=e.id LEFT JOIN sys_chen_self_review_20261101 j ON j.perf_id=e.id;
"""
ddl="""CREATE TABLE IF NOT EXISTS bak_20261101_chen_self_review LIKE biz_performance;
CREATE TABLE IF NOT EXISTS sys_chen_self_review_20261101(perf_id BIGINT PRIMARY KEY,perf_code VARCHAR(64),before_auditor BIGINT,after_auditor BIGINT,before_time DATETIME,after_time DATETIME,database_operator VARCHAR(288),create_time DATETIME(3),rolled_back_at DATETIME(3) NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
"""
summary="SELECT result,COUNT(*) row_count FROM chen_preview GROUP BY result;SELECT id,code,result FROM chen_preview WHERE result NOT IN('READY','ALREADY_CORRECT','ALREADY_RECORDED');\n"
forward=ddl+seed+"""DROP PROCEDURE IF EXISTS chen_self_review_20261101;
DELIMITER $$
CREATE PROCEDURE chen_self_review_20261101()
BEGIN
 DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK;RESIGNAL;END;
 START TRANSACTION;
 SELECT p.perf_id FROM biz_performance p JOIN chen_targets t ON t.id=p.perf_id ORDER BY p.perf_id FOR UPDATE;
"""+preview+"""
 INSERT INTO bak_20261101_chen_self_review SELECT p.* FROM biz_performance p JOIN chen_preview e ON e.id=p.perf_id WHERE e.result IN('READY','ALREADY_CORRECT');
 INSERT INTO sys_chen_self_review_20261101 SELECT p.perf_id,p.perf_code,p.auditor_id,112120,p.update_time,NULL,CURRENT_USER(),NOW(3),NULL FROM biz_performance p JOIN chen_preview e ON e.id=p.perf_id WHERE e.result IN('READY','ALREADY_CORRECT');
 UPDATE biz_performance p JOIN chen_preview e ON e.id=p.perf_id AND e.result='READY' SET p.auditor_id=112120,p.update_time=GREATEST(NOW(),DATE_ADD(COALESCE(p.update_time,'1970-01-01'),INTERVAL 1 SECOND));
 UPDATE sys_chen_self_review_20261101 j JOIN biz_performance p ON p.perf_id=j.perf_id SET j.after_time=p.update_time WHERE j.after_time IS NULL;
 COMMIT;
"""+summary+"END$$\nDELIMITER ;\nCALL chen_self_review_20261101();DROP PROCEDURE chen_self_review_20261101;\n"
prefix.with_suffix('.sql').write_text(forward,encoding='utf-8',newline='\n')
Path(str(prefix)+'-check.sql').write_text(ddl+seed+preview+summary,encoding='utf-8',newline='\n')
Path(str(prefix)+'-rollback.sql').write_text("""-- Restore only our reviewer field when no later edits or active workflows exist; retain journal and backups.
START TRANSACTION;
UPDATE biz_performance p JOIN sys_chen_self_review_20261101 j ON j.perf_id=p.perf_id
SET p.auditor_id=j.before_auditor,p.update_time=j.before_time,j.rolled_back_at=NOW(3)
WHERE j.rolled_back_at IS NULL AND BINARY p.perf_code=BINARY j.perf_code AND p.leader_id=112120 AND p.auditor_id=j.after_auditor AND p.update_time <=> j.after_time
AND NOT EXISTS(SELECT 1 FROM biz_performance_submission s WHERE s.perf_id=p.perf_id AND COALESCE(s.is_delete,0)=0 AND s.flow_status IN(10,20))
AND NOT EXISTS(SELECT 1 FROM biz_core_report r WHERE BINARY r.performance_code=BINARY p.perf_code AND r.flow_status IN(10,20));
COMMIT;
SELECT perf_id,perf_code,rolled_back_at FROM sys_chen_self_review_20261101;
""",encoding='utf-8',newline='\n')
print('Prepared 17 guarded manual self-review assignments')
