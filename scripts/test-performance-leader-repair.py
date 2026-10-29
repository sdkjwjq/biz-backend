"""Owner repair rehearsal in a random disposable database; never writes the debug database."""
from pathlib import Path
import hashlib
import json
import os
import re
import subprocess
import uuid

ROOT = Path(__file__).resolve().parents[1]
PREFIX = ROOT / 'data/migrations/2026-10-29-performance-leader-repair'


def main():
    password = os.environ.get('SHUANGGAO_TEST_DB_PASSWORD')
    if password is None:
        raise RuntimeError('Set SHUANGGAO_TEST_DB_PASSWORD')
    mysql = r'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
    schema = 'biz_perf_owner_test_' + uuid.uuid4().hex
    env = dict(os.environ, MYSQL_PWD=password)
    source = (ROOT / 'data/20260920backup.sql').read_bytes()
    assert not re.search(rb'(?im)^\s*(USE\s|CREATE\s+DATABASE\s)', source)
    checks = []

    def sql(text, database=schema, success=True):
        result = subprocess.run([mysql, '--host=127.0.0.1', '--user=root', '--default-character-set=utf8mb4',
                                 '--batch', '--skip-column-names', *([database] if database else [])],
                                input=text if isinstance(text, bytes) else text.encode(), env=env, capture_output=True)
        assert (result.returncode == 0) == success, result.stderr.decode('utf-8', errors='replace')
        return result.stdout.decode('utf-8').strip()

    def reset():
        sql('DROP TABLE IF EXISTS bak_20261029_performance_leader,sys_performance_leader_repair_20261029,biz_ownership_change_log')
        sql(source)
        sql((ROOT / 'data/migrations/2026-09-24-ownership-change-log.sql').read_bytes())

    seed = Path(str(PREFIX) + '-candidates.sql').read_text(encoding='utf-8')
    forward = seed + Path(str(PREFIX) + '.sql').read_text(encoding='utf-8')
    preview = seed + Path(str(PREFIX) + '-check.sql').read_text(encoding='utf-8')
    rollback = Path(str(PREFIX) + '-rollback.sql').read_text(encoding='utf-8')
    manifest = json.loads((ROOT / 'data/repairs/performance-leader-20261029.json').read_text(encoding='utf-8'))['performance']

    def fields_digest():
        columns = sql("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_performance' AND COLUMN_NAME NOT IN('leader_id','update_time') ORDER BY ORDINAL_POSITION").splitlines()
        return hashlib.sha256(sql('SELECT JSON_ARRAY(' + ','.join('`' + c + '`' for c in columns) + ') FROM biz_performance ORDER BY perf_id').encode()).hexdigest()

    def others():
        tables = sql("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_TYPE='BASE TABLE' AND TABLE_NAME NOT IN('biz_performance','bak_20261029_performance_leader','sys_performance_leader_repair_20261029') ORDER BY TABLE_NAME").splitlines()
        return sql('CHECKSUM TABLE ' + ','.join('`' + t + '`' for t in tables))

    owners = lambda: sql('SELECT perf_id,leader_id FROM biz_performance ORDER BY perf_id')
    count = lambda q: int(sql(q))
    sql('CREATE DATABASE `' + schema + '` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci', database=None)
    try:
        reset()
        before_owner, before_fields, before_other = owners(), fields_digest(), others()
        assert 'READY\t155' in sql(preview)
        assert 'repaired_performances\t155' in sql(forward)
        assert count('SELECT COUNT(*) FROM bak_20261029_performance_leader') == 155
        for r in manifest:
            assert count('SELECT leader_id FROM biz_performance WHERE perf_id=' + str(r['perf_id'])) == r['owner_before']
        assert fields_digest() == before_fields and others() == before_other
        assert count('SELECT COUNT(*) FROM biz_performance p JOIN sys_performance_leader_repair_20261029 h ON h.perf_id=p.perf_id WHERE p.leader_id=112120 AND p.auditor_id=110750') == 17
        checks.append('Historical backup: all 155 professional owners restored; all reviewers, departments, values, histories, task data and other tables unchanged; Chen Aolin 17 reviewers remain Quan Ning')
        state = sql('SELECT perf_id,leader_id,update_time FROM biz_performance ORDER BY perf_id')
        assert 'repaired_performances\t0' in sql(forward)
        assert state == sql('SELECT perf_id,leader_id,update_time FROM biz_performance ORDER BY perf_id')
        assert count('SELECT COUNT(*) FROM bak_20261029_performance_leader') == 155
        checks.append('Repeated execution preserves original backups and is a no-op')
        assert 'rolled_back_performances\t155' in sql(rollback)
        assert owners() == before_owner and fields_digest() == before_fields and others() == before_other
        assert 'rolled_back_performances\t0' in sql(rollback)
        assert 'repaired_performances\t0' in sql(forward)
        checks.append('Rollback restores only owners, retains evidence, and prevents silent reapplication')

        reset()
        a,b,c,d,e,f = manifest[:6]
        sql(f"UPDATE biz_performance SET leader_id={a['owner_before']} WHERE perf_id={a['perf_id']}; UPDATE biz_performance SET leader_id=110228 WHERE perf_id={b['perf_id']}; UPDATE biz_performance SET perf_code='changed' WHERE perf_id={c['perf_id']}; UPDATE biz_performance SET is_delete=1 WHERE perf_id={d['perf_id']}")
        sql(f"INSERT INTO biz_ownership_change_log(target_type,target_id,operator_id,reason,leader_before_id,leader_after_id,create_time) VALUES('PERFORMANCE',{e['perf_id']},110228,'Synthetic legitimate transfer',1,2,NOW())")
        sql(f"UPDATE biz_performance SET auditor_id=110228,principal_id=110228 WHERE perf_id={f['perf_id']}; INSERT INTO biz_ownership_change_log(target_type,target_id,operator_id,reason,leader_before_id,leader_after_id,create_time) VALUES('PERFORMANCE',{f['perf_id']},110228,'Synthetic reviewer-only transfer',{f['owner_after']},{f['owner_after']},NOW())")
        before_fields,before_other=fields_digest(),others()
        assert 'repaired_performances\t150' in sql(forward)
        assert fields_digest() == before_fields and others() == before_other
        assert sql(f"SELECT leader_id,auditor_id,principal_id FROM biz_performance WHERE perf_id={f['perf_id']}") == f"{f['owner_before']}\t110228\t110228"
        checks.append('Already-correct, changed identity/owner, deleted records and legitimate owner transfers skipped; later reviewer-only changes retained')
        sql(f"UPDATE biz_performance SET current_value=current_value+1,update_time=DATE_ADD(update_time,INTERVAL 1 SECOND) WHERE perf_id={f['perf_id']}")
        state=owners()
        sql(rollback,success=False)
        assert owners()==state and count('SELECT COUNT(*) FROM sys_performance_leader_repair_20261029 WHERE rolled_back_at IS NOT NULL')==0
        checks.append('Rollback refuses to overwrite later business edits, with no partial rollback')

        reset()
        sql('UPDATE sys_user SET status=0 WHERE user_id=112120')
        assert 'OWNER_UNAVAILABLE\t17' in sql(preview)
        checks.append('Disabled original professional owners are excluded from automatic repair')
        reset()
        before_owner,before_fields,before_other=owners(),fields_digest(),others()
        sql("CREATE TRIGGER fail_owner_repair BEFORE UPDATE ON biz_performance FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic transaction failure'")
        sql(forward,success=False)
        assert owners()==before_owner and fields_digest()==before_fields and others()==before_other
        assert count('SELECT COUNT(*) FROM bak_20261029_performance_leader')==0
        assert count('SELECT COUNT(*) FROM sys_performance_leader_repair_20261029')==0
        checks.append('Injected update failure rolls back owner changes, backups and success evidence atomically')
    finally:
        assert re.fullmatch(r'biz_perf_owner_test_[0-9a-f]{32}',schema)
        sql('DROP DATABASE `' + schema + '`',database=None)
    out=ROOT.parent/'.run/owner-field-audit-20261029/performance-repair-results.json'
    report={'passed':True,'checks':checks,'temporaryDatabaseRemoved':True,'localBusinessDatabaseModified':False,'productionDatabaseAccessed':False}
    out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(report,ensure_ascii=False,indent=2))


if __name__=='__main__':
    main()
