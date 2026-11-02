"""Build the confirmed workbook relation-only migration, verification and guarded rollback.

The input is an immutable reviewed manifest, not an untrusted workbook execution path.
No application source, user database or workbook is modified by this builder.
"""
from pathlib import Path
import json,re

ROOT=Path(__file__).resolve().parents[1]
PREFIX=ROOT/'data/migrations/2026-11-02-performance-links'
KEY='performance-links-workbook-20261102-v1'
BATCH='perf-links-20261102-v1'

def write(suffix,value):Path(str(PREFIX)+suffix+'.sql').write_text(value,encoding='utf-8',newline='\n')
def quote(s):return "'"+str(s).replace("'","''")+"'"
def taskcode(year,ref):
 a,b,c=map(int,ref.split('-'));return f'SG{a:02d}{b:02d}{c:02d}{year%100:02d}'

def main():
 data=json.loads((ROOT/'data/repairs/performance-links-20261102.json').read_text(encoding='utf-8'))
 rows=data['approved'];assert rows and len({(r['code'],r['year']) for r in rows})==len(rows)
 for r in rows:
  assert re.fullmatch(r'1\.[12]\.\d+',r['code']) and r['year'] in range(2025,2030)
  assert not r['invalid'] and r['code'] not in data['percentageIndicatorsDeferred']
 seed="""SET NAMES utf8mb4;
SET SESSION group_concat_max_len=16777216;
DROP TEMPORARY TABLE IF EXISTS pl_plan;
CREATE TEMPORARY TABLE pl_plan(code VARCHAR(64) COLLATE utf8mb4_bin,year INT,old_codes TEXT COLLATE utf8mb4_bin,new_codes TEXT COLLATE utf8mb4_bin,PRIMARY KEY(code,year));
INSERT INTO pl_plan VALUES
"""+',\n'.join('('+','.join([quote(r['code']),str(r['year']),quote(','.join(sorted(taskcode(r['year'],t) for t in r['current']))),quote(','.join(sorted(taskcode(r['year'],t) for t in r['proposed'])))])+')' for r in rows)+";\n"
 seed+='DROP TEMPORARY TABLE IF EXISTS pl_wanted;CREATE TEMPORARY TABLE pl_wanted(code VARCHAR(64) COLLATE utf8mb4_bin,year INT,task_code VARCHAR(64) COLLATE utf8mb4_bin,PRIMARY KEY(code,year,task_code));\n'
 wanted=[(r['code'],r['year'],taskcode(r['year'],t)) for r in rows for t in r['proposed']]
 if wanted:seed+='INSERT INTO pl_wanted VALUES\n'+',\n'.join('('+','.join([quote(c),str(y),quote(t)])+')' for c,y,t in wanted)+';\n'
 resolve="""DROP TEMPORARY TABLE IF EXISTS pl_cells;
CREATE TEMPORARY TABLE pl_cells AS SELECT a.*,p.perf_id,y.year_id FROM pl_plan a
JOIN biz_performance p ON BINARY p.perf_code=BINARY a.code AND COALESCE(p.is_delete,0)=0
JOIN biz_performance_year y ON y.perf_id=p.perf_id AND y.year=a.year AND COALESCE(y.is_delete,0)=0;
DROP TEMPORARY TABLE IF EXISTS pl_desired;
CREATE TEMPORARY TABLE pl_desired AS SELECT c.perf_id,c.year_id,t.task_id,t.task_code,t.data_type FROM pl_wanted a
JOIN pl_cells c ON BINARY c.code=BINARY a.code AND c.year=a.year
JOIN biz_task t ON BINARY t.task_code=BINARY a.task_code AND t.phase=a.year AND t.level=3 AND COALESCE(t.is_delete,0)=0 AND COALESCE(t.data_type,'1')<>'0';
DROP TEMPORARY TABLE IF EXISTS pl_current;
CREATE TEMPORARY TABLE pl_current AS SELECT c.perf_id,c.year_id,
COALESCE(GROUP_CONCAT(t.task_code ORDER BY t.task_code SEPARATOR ','),'') current_codes,
COUNT(r.id) row_count,COUNT(DISTINCT t.task_id) task_count
FROM pl_cells c LEFT JOIN rel_task_performance r ON r.perf_id=c.perf_id AND r.year_id=c.year_id AND COALESCE(r.is_delete,0)=0
LEFT JOIN biz_task t ON t.task_id=r.task_id GROUP BY c.perf_id,c.year_id;
"""
 recompute="""DROP TEMPORARY TABLE IF EXISTS pl_values;
CREATE TEMPORARY TABLE pl_values AS SELECT c.year_id,
CASE WHEN p.data_type='2' THEN LEAST(100,GREATEST(0,COALESCE(MAX(t.current_value),0))) ELSE COALESCE(SUM(t.current_value),0) END value
FROM biz_performance_year c JOIN biz_performance p ON p.perf_id=c.perf_id
LEFT JOIN rel_task_performance r ON r.perf_id=c.perf_id AND r.year_id=c.year_id AND COALESCE(r.is_delete,0)=0
LEFT JOIN biz_task t ON t.task_id=r.task_id AND COALESCE(t.is_delete,0)=0 AND (t.level IS NULL OR t.level NOT IN(1,2))
 AND COALESCE(t.data_type,'1')<>'0' AND t.phase=c.year
WHERE c.perf_id IN(SELECT perf_id FROM pl_cells) AND COALESCE(c.is_delete,0)=0
GROUP BY c.year_id,p.data_type;
"""
 total_values="""DROP TEMPORARY TABLE IF EXISTS pl_totals;
CREATE TEMPORARY TABLE pl_totals AS SELECT p.perf_id,
CASE WHEN p.data_type='2' THEN LEAST(100,GREATEST(0,COALESCE(MAX(y.actual_value),0))) ELSE COALESCE(SUM(y.actual_value),0) END value
FROM biz_performance p JOIN bak_20261102_perf_links_total b ON b.perf_id=p.perf_id
LEFT JOIN biz_performance_year y ON y.perf_id=p.perf_id AND COALESCE(y.is_delete,0)=0
GROUP BY p.perf_id,p.data_type;
"""
 # Scope is the affected performances, including their untouched years. Hashes protect
 # rollback from overwriting any later association, value, configuration or source-value edit.
 hash_specs=[
 ('rel','rel_task_performance','r','id','r.perf_id IN(SELECT perf_id FROM bak_20261102_perf_links_total)',
  'r.id,r.task_id,r.perf_id,r.year_id,r.weight,r.contribution_value,r.data_type,r.is_delete'),
 ('year','biz_performance_year','r','year_id','r.perf_id IN(SELECT perf_id FROM bak_20261102_perf_links_total)',
  'r.year_id,r.perf_id,r.year,r.target_value,r.actual_value,r.data_type,r.is_delete'),
 ('perf','biz_performance','r','perf_id','r.perf_id IN(SELECT perf_id FROM bak_20261102_perf_links_total)',
  'r.perf_id,r.perf_code,r.current_value,r.update_time,r.target_value,r.data_type,r.is_delete,r.leader_id,r.auditor_id,r.principal_id,r.dept_id'),
 ('task','biz_task','r','task_id','r.task_id IN(SELECT task_id FROM bak_20261102_perf_links_task)',
  'r.task_id,r.task_code,r.phase,r.level,r.data_type,r.target_value,r.current_value,r.is_delete')]
 hashes=''.join(f"SELECT SHA2(COALESCE(GROUP_CONCAT(SHA2(CAST(JSON_ARRAY({cols}) AS CHAR),256) ORDER BY r.{pk} SEPARATOR '|'),''),256) INTO @pl_{name}_hash FROM {table} r WHERE {where};\n" for name,table,alias,pk,where,cols in hash_specs)
 ddl="""CREATE TABLE IF NOT EXISTS bak_20261102_perf_links_rel LIKE rel_task_performance;
CREATE TABLE IF NOT EXISTS bak_20261102_perf_links_year LIKE biz_performance_year;
CREATE TABLE IF NOT EXISTS bak_20261102_perf_links_total LIKE biz_performance;
CREATE TABLE IF NOT EXISTS bak_20261102_perf_links_task LIKE biz_task;
CREATE TABLE IF NOT EXISTS sys_perf_links_20261102 (
 id INT PRIMARY KEY,source_sha256 CHAR(64) NOT NULL,state VARCHAR(20) NOT NULL,
 annual_cells INT NOT NULL,added INT NOT NULL,removed INT NOT NULL,
 rel_hash CHAR(64),year_hash CHAR(64),perf_hash CHAR(64),task_hash CHAR(64),
 database_operator VARCHAR(288) NOT NULL,applied_at DATETIME(3) NOT NULL,rolled_back_at DATETIME(3) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
"""
 locks="""SELECT p.perf_id locked_performance FROM biz_performance p WHERE p.perf_id IN(SELECT perf_id FROM pl_cells) ORDER BY p.perf_id FOR UPDATE;
SELECT y.year_id locked_year FROM biz_performance_year y WHERE y.perf_id IN(SELECT perf_id FROM pl_cells) ORDER BY y.year_id FOR UPDATE;
SELECT r.id locked_relation FROM rel_task_performance r WHERE r.perf_id IN(SELECT perf_id FROM pl_cells) ORDER BY r.id FOR UPDATE;
"""
 log_prefix="""INSERT INTO biz_performance_relation_log(perf_id,perf_code,perf_name,year,action,task_id,task_code,task_name,batch_id,operator_id,reason,create_time)
"""
 reason=quote('系统部署迁移；按确认的绩效分解表调整关联；来源SHA256='+data['sourceSha256']+'；数据库操作者：')
 forward='-- Stop application writes and take a full database backup before execution.\n'+seed+ddl+"""DROP PROCEDURE IF EXISTS performance_links_20261102;
DELIMITER $$
CREATE PROCEDURE performance_links_20261102()
main:BEGIN
 DECLARE v_count INT DEFAULT 0;DECLARE v_add INT DEFAULT 0;DECLARE v_remove INT DEFAULT 0;
 DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK;RESIGNAL;END;
 START TRANSACTION;
 IF EXISTS(SELECT 1 FROM sys_perf_links_20261102 WHERE id=1 AND state='ROLLED_BACK') THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Performance links migration was rolled back; use a new reviewed migration';
 END IF;
 IF EXISTS(SELECT 1 FROM sys_perf_links_20261102 WHERE id=1 AND state='APPLIED') THEN
  SELECT 'already_applied_preserving_later_changes' result;COMMIT;LEAVE main;
 END IF;
 IF EXISTS(SELECT 1 FROM bak_20261102_perf_links_rel) OR EXISTS(SELECT 1 FROM bak_20261102_perf_links_year)
 OR EXISTS(SELECT 1 FROM bak_20261102_perf_links_total) OR EXISTS(SELECT 1 FROM bak_20261102_perf_links_task) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Unexpected nonempty performance-link backup; no business rows changed';
 END IF;
"""+resolve+locks+f"""
 SELECT COUNT(*) INTO v_count FROM pl_cells;
 IF v_count<>{len(rows)} OR EXISTS(SELECT 1 FROM pl_cells GROUP BY code,year HAVING COUNT(*)<>1) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Performance/year identity is missing or duplicated; no rows committed';
 END IF;
 SELECT COUNT(*) INTO v_count FROM pl_desired;
 IF v_count<>{len(wanted)} OR EXISTS(SELECT 1 FROM pl_desired GROUP BY perf_id,year_id,task_id HAVING COUNT(*)<>1) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Desired task is missing, wrong-year, deleted, duplicated or excluded from aggregation';
 END IF;
 IF EXISTS(SELECT 1 FROM pl_cells c JOIN pl_current a ON a.perf_id=c.perf_id AND a.year_id=c.year_id
 WHERE a.row_count<>a.task_count OR (NOT(BINARY a.current_codes=BINARY c.old_codes) AND NOT(BINARY a.current_codes=BINARY c.new_codes))) THEN
  SELECT c.code,c.year,a.current_codes,c.old_codes,c.new_codes FROM pl_cells c JOIN pl_current a ON a.perf_id=c.perf_id AND a.year_id=c.year_id
  WHERE a.row_count<>a.task_count OR (NOT(BINARY a.current_codes=BINARY c.old_codes) AND NOT(BINARY a.current_codes=BINARY c.new_codes));
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Online performance links differ from the reviewed baseline; no rows committed';
 END IF;
 IF EXISTS(SELECT 1 FROM biz_performance_submission s WHERE s.perf_id IN(SELECT perf_id FROM pl_cells)
 AND COALESCE(s.is_delete,0)=0 AND s.flow_status IN(10,20)) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='An affected performance has active approval; finish it before retrying';
 END IF;
 INSERT INTO bak_20261102_perf_links_total SELECT p.* FROM biz_performance p WHERE p.perf_id IN(SELECT perf_id FROM pl_cells);
 INSERT INTO bak_20261102_perf_links_year SELECT y.* FROM biz_performance_year y JOIN bak_20261102_perf_links_total b ON b.perf_id=y.perf_id;
 INSERT INTO bak_20261102_perf_links_rel SELECT r.* FROM rel_task_performance r JOIN bak_20261102_perf_links_total b ON b.perf_id=r.perf_id;
 DROP TEMPORARY TABLE IF EXISTS pl_task_ids;CREATE TEMPORARY TABLE pl_task_ids(task_id BIGINT PRIMARY KEY);
 INSERT IGNORE INTO pl_task_ids SELECT task_id FROM bak_20261102_perf_links_rel;
 INSERT IGNORE INTO pl_task_ids SELECT task_id FROM pl_desired;
 SELECT t.task_id locked_source_task FROM biz_task t JOIN pl_task_ids d ON d.task_id=t.task_id ORDER BY t.task_id FOR UPDATE;
 INSERT INTO bak_20261102_perf_links_task SELECT t.* FROM biz_task t JOIN pl_task_ids d ON d.task_id=t.task_id;
"""+log_prefix+f"""SELECT r.perf_id,p.perf_code,p.perf_name,c.year,'REMOVE',t.task_id,t.task_code,t.task_name,'{BATCH}',0,CONCAT({reason},CURRENT_USER()),NOW()
 FROM rel_task_performance r JOIN pl_cells c ON c.perf_id=r.perf_id AND c.year_id=r.year_id
 JOIN biz_task t ON t.task_id=r.task_id JOIN biz_performance p ON p.perf_id=r.perf_id
 LEFT JOIN pl_desired d ON d.perf_id=r.perf_id AND d.year_id=r.year_id AND d.task_id=r.task_id
 WHERE COALESCE(r.is_delete,0)=0 AND d.task_id IS NULL;
 SET v_remove=ROW_COUNT();
 UPDATE rel_task_performance r JOIN pl_cells c ON c.perf_id=r.perf_id AND c.year_id=r.year_id
 LEFT JOIN pl_desired d ON d.perf_id=r.perf_id AND d.year_id=r.year_id AND d.task_id=r.task_id
 SET r.is_delete=1 WHERE COALESCE(r.is_delete,0)=0 AND d.task_id IS NULL;
 DROP TEMPORARY TABLE IF EXISTS pl_activate;
 CREATE TEMPORARY TABLE pl_activate AS SELECT d.*,MIN(r.id) id FROM pl_desired d
 LEFT JOIN rel_task_performance r ON r.perf_id=d.perf_id AND r.year_id=d.year_id AND r.task_id=d.task_id
 GROUP BY d.perf_id,d.year_id,d.task_id,d.task_code,d.data_type
 HAVING SUM(CASE WHEN r.id IS NOT NULL AND COALESCE(r.is_delete,0)=0 THEN 1 ELSE 0 END)=0;
"""+log_prefix+f"""SELECT a.perf_id,p.perf_code,p.perf_name,c.year,'ADD',t.task_id,t.task_code,t.task_name,'{BATCH}',0,CONCAT({reason},CURRENT_USER()),NOW()
 FROM pl_activate a JOIN pl_cells c ON c.perf_id=a.perf_id AND c.year_id=a.year_id
 JOIN biz_task t ON t.task_id=a.task_id JOIN biz_performance p ON p.perf_id=a.perf_id;
 SET v_add=ROW_COUNT();
 UPDATE rel_task_performance r JOIN pl_activate a ON a.id=r.id SET r.is_delete=0;
 INSERT INTO rel_task_performance(task_id,perf_id,year_id,weight,contribution_value,data_type,is_delete)
 SELECT task_id,perf_id,year_id,1,0,data_type,0 FROM pl_activate WHERE id IS NULL;
"""+recompute+"""UPDATE biz_performance_year y JOIN pl_values a ON a.year_id=y.year_id SET y.actual_value=a.value;
"""+total_values+"""UPDATE biz_performance p JOIN pl_totals a ON a.perf_id=p.perf_id
 SET p.current_value=a.value,p.update_time=GREATEST(NOW(),DATE_ADD(COALESCE(p.update_time,'1970-01-01'),INTERVAL 1 SECOND));
"""+hashes+f"""INSERT INTO sys_perf_links_20261102 VALUES(1,'{data['sourceSha256']}','APPLIED',{len(rows)},v_add,v_remove,
 @pl_rel_hash,@pl_year_hash,@pl_perf_hash,@pl_task_hash,CURRENT_USER(),NOW(3),NULL);
 INSERT INTO sys_release_migration(migration_key,description) VALUES('{KEY}','Confirmed 2025-2029 performance task links; blank removes; guarded exclusions; derived values recalculated');
 COMMIT;
 SELECT 'performance_links_applied' result,{len(rows)} annual_cells,v_add added,v_remove removed;
END$$
DELIMITER ;
CALL performance_links_20261102();DROP PROCEDURE performance_links_20261102;
"""
 check=seed+resolve+recompute+total_values+hashes+"""SELECT 'migration_applied' check_name,COUNT(*) result FROM sys_perf_links_20261102 WHERE id=1 AND state='APPLIED';
SELECT 'relation_target_mismatch',COUNT(*) FROM pl_cells c JOIN pl_current a ON a.perf_id=c.perf_id AND a.year_id=c.year_id
 WHERE a.row_count<>a.task_count OR NOT(BINARY a.current_codes=BINARY c.new_codes);
SELECT 'annual_value_mismatch',COUNT(*) FROM biz_performance_year y JOIN pl_values a ON a.year_id=y.year_id WHERE NOT(y.actual_value <=> a.value);
SELECT 'total_value_mismatch',COUNT(*) FROM biz_performance p JOIN pl_totals a ON a.perf_id=p.perf_id WHERE NOT(p.current_value <=> a.value);
SELECT 'task_source_changed',COUNT(*) FROM sys_perf_links_20261102 WHERE id=1 AND NOT(task_hash <=> @pl_task_hash);
SELECT 'post_migration_drift',COUNT(*) FROM sys_perf_links_20261102 WHERE id=1 AND
 (NOT(rel_hash <=> @pl_rel_hash) OR NOT(year_hash <=> @pl_year_hash) OR NOT(perf_hash <=> @pl_perf_hash));
SELECT state,annual_cells,added,removed,source_sha256,database_operator,applied_at FROM sys_perf_links_20261102;
"""
 rollback="""-- Stop writes and take a current full backup first. Audit logs and backups are retained.
SET NAMES utf8mb4;SET SESSION group_concat_max_len=16777216;
DROP PROCEDURE IF EXISTS rollback_performance_links_20261102;
DELIMITER $$
CREATE PROCEDURE rollback_performance_links_20261102()
main:BEGIN
 DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK;RESIGNAL;END;
 START TRANSACTION;
 IF EXISTS(SELECT 1 FROM sys_perf_links_20261102 WHERE id=1 AND state='ROLLED_BACK') THEN COMMIT;LEAVE main;END IF;
 IF NOT EXISTS(SELECT 1 FROM sys_perf_links_20261102 WHERE id=1 AND state='APPLIED') THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No applied performance-link migration to roll back';
 END IF;
 SELECT p.perf_id locked_performance FROM biz_performance p JOIN bak_20261102_perf_links_total b ON b.perf_id=p.perf_id ORDER BY p.perf_id FOR UPDATE;
 SELECT y.year_id locked_year FROM biz_performance_year y JOIN bak_20261102_perf_links_year b ON b.year_id=y.year_id ORDER BY y.year_id FOR UPDATE;
 SELECT r.id locked_relation FROM rel_task_performance r JOIN bak_20261102_perf_links_total b ON b.perf_id=r.perf_id ORDER BY r.id FOR UPDATE;
 SELECT t.task_id locked_task FROM biz_task t JOIN bak_20261102_perf_links_task b ON b.task_id=t.task_id ORDER BY t.task_id FOR UPDATE;
"""+hashes+"""IF EXISTS(SELECT 1 FROM sys_perf_links_20261102 WHERE id=1 AND
 (NOT(rel_hash <=> @pl_rel_hash) OR NOT(year_hash <=> @pl_year_hash) OR NOT(perf_hash <=> @pl_perf_hash) OR NOT(task_hash <=> @pl_task_hash)))
 OR EXISTS(SELECT 1 FROM biz_performance_submission s JOIN bak_20261102_perf_links_total b ON b.perf_id=s.perf_id
 WHERE COALESCE(s.is_delete,0)=0 AND s.flow_status IN(10,20)) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Later edits, task values or active approvals detected; rollback refused';
 END IF;
"""+log_prefix+f"""SELECT l.perf_id,l.perf_code,l.perf_name,l.year,IF(l.action='ADD','REMOVE','ADD'),l.task_id,l.task_code,l.task_name,
 '{BATCH}-rollback',0,CONCAT('系统回滚经确认的绩效关联迁移；数据库操作者：',CURRENT_USER()),NOW()
 FROM biz_performance_relation_log l WHERE BINARY l.batch_id=BINARY '{BATCH}';
 UPDATE rel_task_performance r JOIN bak_20261102_perf_links_total p ON p.perf_id=r.perf_id
 LEFT JOIN bak_20261102_perf_links_rel b ON b.id=r.id SET r.is_delete=1 WHERE b.id IS NULL;
 UPDATE rel_task_performance r JOIN bak_20261102_perf_links_rel b ON b.id=r.id SET r.is_delete=b.is_delete;
 UPDATE biz_performance_year y JOIN bak_20261102_perf_links_year b ON b.year_id=y.year_id SET y.actual_value=b.actual_value;
 UPDATE biz_performance p JOIN bak_20261102_perf_links_total b ON b.perf_id=p.perf_id SET p.current_value=b.current_value,p.update_time=b.update_time;
 UPDATE sys_perf_links_20261102 SET state='ROLLED_BACK',rolled_back_at=NOW(3) WHERE id=1;
 COMMIT;
 SELECT 'performance_links_rolled_back' result;
END$$
DELIMITER ;
CALL rollback_performance_links_20261102();DROP PROCEDURE rollback_performance_links_20261102;
"""
 write('',forward);write('-check',check);write('-rollback',rollback)
 print(json.dumps(data['counts'],ensure_ascii=False))

if __name__=='__main__':main()
