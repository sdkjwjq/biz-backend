"""Verify the omitted-owner repair against a disposable historical-copy schema."""
from pathlib import Path
import hashlib,json,os,re,shutil,subprocess,uuid
ROOT=Path(__file__).resolve().parents[1]
PREFIX=ROOT/'data/migrations/2026-11-02-task-leader-completion'
def main():
    password=os.environ['SHUANGGAO_TEST_DB_PASSWORD'];env=dict(os.environ,MYSQL_PWD=password)
    mysql=shutil.which('mysql') or 'C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe'
    schema='biz_owner_completion_test_'+uuid.uuid4().hex;checks=[]
    def sql(value,db=schema,success=True):
        result=subprocess.run([mysql,'--host=127.0.0.1','--user=root','--default-character-set=utf8mb4','--batch','--skip-column-names',*([db] if db else [])],input=value if isinstance(value,bytes) else value.encode(),env=env,capture_output=True)
        assert (result.returncode==0)==success,result.stderr.decode('utf-8',errors='replace')
        return result.stdout.decode('utf-8').strip()
    source=(ROOT/'data/20260920backup.sql').read_bytes();assert not re.search(rb'(?im)^\s*(USE |CREATE DATABASE)',source)
    data=json.loads((ROOT/'data/repairs/task-owner-completion-20261102.json').read_text(encoding='utf-8'));assert len(data['items'])==368
    def file(suffix):return Path(str(PREFIX)+suffix+'.sql').read_text(encoding='utf-8')
    forward=file('-candidates')+file('');preview=file('-candidates')+file('-check');rollback=file('-rollback')
    def reset():
        sql('DROP TRIGGER IF EXISTS fail_owner_completion; DROP TABLE IF EXISTS sys_task_leader_repair_20261102,bak_20261102_task_leader,sys_task_leader_repair_20261029,bak_20261029_task_leader,biz_ownership_change_log')
        sql(source);sql((ROOT/'data/migrations/2026-09-24-ownership-change-log.sql').read_bytes())
        sql((ROOT/'data/migrations/2026-10-29-task-leader-repair-candidates.sql').read_bytes()+(ROOT/'data/migrations/2026-10-29-task-leader-repair.sql').read_bytes())
    def owners():return sql('SELECT task_id,leader_id,update_time FROM biz_task ORDER BY task_id')
    def fields():
        cols=sql("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_task' AND COLUMN_NAME NOT IN('leader_id','update_time') ORDER BY ORDINAL_POSITION").splitlines()
        return hashlib.sha256(sql('SELECT JSON_ARRAY('+','.join('`'+c+'`' for c in cols)+') FROM biz_task ORDER BY task_id').encode()).hexdigest()
    def other():
        tables=sql("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_TYPE='BASE TABLE' AND TABLE_NAME NOT IN('biz_task','sys_task_leader_repair_20261102','bak_20261102_task_leader') ORDER BY TABLE_NAME").splitlines()
        return sql('CHECKSUM TABLE '+','.join('`'+t+'`' for t in tables))
    sql('CREATE DATABASE `'+schema+'` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci',None)
    try:
        reset();before,bf,bo=owners(),fields(),other();assert 'READY\t368' in sql(preview)
        assert 'repaired_tasks\t368' in sql(forward)
        assert sql('SELECT COUNT(*) FROM bak_20261102_task_leader')=='368'
        assert sql('SELECT COUNT(*) FROM biz_task t JOIN biz_task p ON p.task_id=t.parent_id JOIN biz_task r ON r.task_id=p.parent_id WHERE t.level=3 AND t.leader_id<>r.leader_id')=='0'
        assert sql('SELECT task_id,leader_id FROM biz_task WHERE task_id IN(628,629) ORDER BY task_id').splitlines()==['628\t110234','629\t110234']
        assert fields()==bf and other()==bo
        checks.append('226 previous repairs plus 368 omissions cover all 595 historical leaf owners; all other task fields and business tables unchanged')
        state=owners();assert 'repaired_tasks\t0' in sql(forward);assert owners()==state
        checks.append('Repeated repair preserves first backups, journals and timestamps')
        sql(rollback);assert sql('SELECT COUNT(*) FROM sys_task_leader_repair_20261102 WHERE rolled_back_at IS NOT NULL')=='368'
        assert [x.rsplit('\t',1)[0] for x in owners().splitlines()]==[x.rsplit('\t',1)[0] for x in before.splitlines()]
        assert 'repaired_tasks\t0' in sql(forward)
        checks.append('Rollback restores only this batch owners, retains evidence and prevents silent reapplication')
        reset();sample=data['items'][:7];ids=[x['task_id'] for x in sample]
        sql(f"UPDATE biz_task SET leader_id={sample[0]['old_leader_id']} WHERE task_id={ids[0]}; UPDATE biz_task SET leader_id=110228 WHERE task_id={ids[1]}; UPDATE biz_task SET task_code=CONCAT(task_code,'X') WHERE task_id={ids[2]}; UPDATE biz_task SET parent_id=0 WHERE task_id={ids[3]}; UPDATE biz_task SET is_delete=1 WHERE task_id={ids[4]};")
        sql(f"INSERT INTO biz_ownership_change_log(target_type,target_id,leader_before_id,leader_after_id,operator_id,reason,create_time) VALUES('TASK',{ids[5]},{sample[5]['old_leader_id']},{sample[5]['wrong_leader_id']},110228,'Synthetic later owner assignment',NOW()); UPDATE biz_task SET auditor_id=110228,principal_id=110228 WHERE task_id={ids[6]}")
        bf,bo=fields(),other();assert 'repaired_tasks\t362' in sql(forward);assert fields()==bf and other()==bo
        assert sql(f'SELECT leader_id,auditor_id,principal_id FROM biz_task WHERE task_id={ids[6]}')==f"{sample[6]['old_leader_id']}\t110228\t110228"
        checks.append('Already-correct, changed/deleted/moved tasks and later owner assignments are skipped; independent reviewer changes are preserved')
        sql(f'UPDATE biz_task SET leader_id=110228,update_time=DATE_ADD(update_time,INTERVAL 1 SECOND) WHERE task_id={ids[6]}');state=owners();sql(rollback,success=False);assert owners()==state
        checks.append('Rollback refuses the whole batch after subsequent edits')
        reset();sql('UPDATE biz_task SET leader_id=110228 WHERE task_id=1');assert 'ROOT_OR_SCOPE_CHANGED' in sql(preview)
        sql("UPDATE sys_user SET status='0' WHERE user_id=110254");assert 'OWNER_UNAVAILABLE' in sql(preview)
        checks.append('Changed root configuration and inactive expected owners are not overwritten')
        reset();before,bf,bo=owners(),fields(),other();sql("CREATE TRIGGER fail_owner_completion BEFORE UPDATE ON biz_task FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic completion failure'")
        sql(forward,success=False);assert owners()==before and fields()==bf and other()==bo
        assert sql('SELECT COUNT(*) FROM bak_20261102_task_leader')=='0' and sql('SELECT COUNT(*) FROM sys_task_leader_repair_20261102')=='0'
        checks.append('Failed writes roll back owner changes, backups and success journals atomically')
    finally:
        assert re.fullmatch('biz_owner_completion_test_[0-9a-f]{32}',schema);sql('DROP DATABASE `'+schema+'`',None)
    report={'passed':True,'checks':checks,'isolatedDatabaseRemoved':True,'businessDatabasesModified':False}
    output=ROOT/'target/task-owner-completion-results.json';output.write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8');print(json.dumps(report,ensure_ascii=False,indent=2))
if __name__=='__main__':main()
