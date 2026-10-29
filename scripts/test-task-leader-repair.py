"""Exercise the owner-only repair in a random disposable schema, never in biz."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import uuid

ROOT = Path(__file__).resolve().parents[1]
PREFIX = ROOT / 'data/migrations/2026-10-29-task-leader-repair'


def main():
    password = os.environ.get('SHUANGGAO_TEST_DB_PASSWORD')
    if password is None:
        raise RuntimeError('Set SHUANGGAO_TEST_DB_PASSWORD')
    mysql = shutil.which('mysql') or r'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
    schema = 'biz_owner_repair_test_' + uuid.uuid4().hex
    env = dict(os.environ, MYSQL_PWD=password)
    checks = []
    source = (ROOT / 'data/20260920backup.sql').read_bytes()
    assert not re.search(rb'(?im)^\s*(?:USE\s|CREATE\s+DATABASE\s)', source)

    def sql(value, database=schema, success=True):
        result = subprocess.run([mysql, '--host=127.0.0.1', '--user=root', '--default-character-set=utf8mb4',
                                 '--batch', '--skip-column-names', *([database] if database else [])],
                                input=value if isinstance(value, bytes) else value.encode(), env=env, capture_output=True)
        assert (result.returncode == 0) == success, result.stderr.decode('utf-8', errors='replace')
        return result.stdout.decode('utf-8').strip()

    def file(suffix):
        return Path(str(PREFIX) + suffix + '.sql').read_text(encoding='utf-8')

    seed = file('-candidates')
    forward = seed + file('')
    preview = seed + file('-check')
    rollback = file('-rollback')

    def reset():
        # All destructive fixture work is restricted to this randomly named disposable schema.
        sql('DROP TABLE IF EXISTS sys_task_leader_repair_20261029,bak_20261029_task_leader,biz_ownership_change_log')
        sql(source)
        sql((ROOT / 'data/migrations/2026-09-24-ownership-change-log.sql').read_bytes())

    def count(query):
        return int(sql(query))

    def task_digest():
        columns = sql("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_task' AND COLUMN_NAME NOT IN('leader_id','update_time') ORDER BY ORDINAL_POSITION").splitlines()
        return hashlib.sha256(sql('SELECT JSON_ARRAY(' + ','.join('`' + c + '`' for c in columns) + ') FROM biz_task ORDER BY task_id').encode()).hexdigest()

    def owners():
        return sql('SELECT task_id,leader_id FROM biz_task ORDER BY task_id')

    def other_checksums():
        tables = sql("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_TYPE='BASE TABLE' AND TABLE_NAME<>'biz_task' AND TABLE_NAME NOT IN('sys_task_leader_repair_20261029','bak_20261029_task_leader') ORDER BY TABLE_NAME").splitlines()
        return sql('CHECKSUM TABLE ' + ','.join('`' + t + '`' for t in tables))

    sql('CREATE DATABASE `' + schema + '` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci', database=None)
    try:
        reset()
        before_owners, before_fields, before_other = owners(), task_digest(), other_checksums()
        assert 'READY\t226' in sql(preview)
        assert 'repaired_tasks\t226' in sql(forward)
        assert count('SELECT COUNT(*) FROM bak_20261029_task_leader') == 226
        assert count('SELECT COUNT(*) FROM sys_task_leader_repair_20261029') == 226
        assert sql('SELECT leader_id,auditor_id,principal_id FROM biz_task WHERE task_id=47') == '112212\t120221\t110009'
        assert task_digest() == before_fields and other_checksums() == before_other
        checks.append('Online backup rehearsal: 226 exact matches repaired; all other task fields, all other tables, submissions and audit histories unchanged')
        after_time = sql('SELECT task_id,leader_id,update_time FROM biz_task ORDER BY task_id')
        assert 'repaired_tasks\t0' in sql(forward)
        assert sql('SELECT task_id,leader_id,update_time FROM biz_task ORDER BY task_id') == after_time
        assert count('SELECT COUNT(*) FROM bak_20261029_task_leader') == 226
        checks.append('Repeated forward execution is a no-op and preserves first backups')
        assert 'rolled_back_tasks\t226' in sql(rollback)
        assert owners() == before_owners and task_digest() == before_fields and other_checksums() == before_other
        assert 'rolled_back_tasks\t0' in sql(rollback)
        assert 'repaired_tasks\t0' in sql(forward)
        checks.append('Rollback restores only owners, preserves evidence, is idempotent, and prevents silent reapplication')

        reset()
        # Exercise each conservative skip independently on the actual historical task identities.
        sql("UPDATE biz_task SET leader_id=112212 WHERE task_id=41; UPDATE biz_task SET leader_id=110228 WHERE task_id=42; UPDATE biz_task SET task_code='MOVED-CODE' WHERE task_id=43; UPDATE biz_task SET is_delete=1 WHERE task_id=44; UPDATE biz_task SET parent_id=99999999 WHERE task_id=45;")
        sql("INSERT INTO biz_ownership_change_log(target_type,target_id,operator_id,reason,leader_before_id,leader_after_id,create_time) VALUES('TASK',46,110228,'Synthetic legitimate owner transfer',120796,120796+1,NOW())")
        # A reviewer-only transfer does not prevent repairing a separately corrupted owner.
        sql("INSERT INTO biz_ownership_change_log(target_type,target_id,operator_id,reason,leader_before_id,leader_after_id,create_time) VALUES('TASK',47,110228,'Synthetic reviewer-only change',120796,120796,NOW()); UPDATE biz_task SET auditor_id=110228,principal_id=110228 WHERE task_id=47")
        report = sql(preview)
        for status in ['ALREADY_CORRECT', 'OWNER_CHANGED', 'IDENTITY_CHANGED', 'MISSING_OR_DELETED', 'OWNER_TRANSFER_RECORDED']:
            assert status in report, status
        special_before = sql('SELECT task_id,leader_id,update_time FROM biz_task WHERE task_id BETWEEN 41 AND 46 ORDER BY task_id')
        before_fields, before_other = task_digest(), other_checksums()
        assert 'repaired_tasks\t220' in sql(forward)
        assert special_before == sql('SELECT task_id,leader_id,update_time FROM biz_task WHERE task_id BETWEEN 41 AND 46 ORDER BY task_id')
        assert sql('SELECT leader_id,auditor_id,principal_id FROM biz_task WHERE task_id=47') == '112212\t110228\t110228'
        assert task_digest() == before_fields and other_checksums() == before_other
        checks.append('Already-correct, changed owner/code/parent, deleted task and legitimate owner transfers are skipped; later reviewer-only changes preserved')
        sql("UPDATE biz_task SET leader_id=110228,update_time=DATE_ADD(update_time,INTERVAL 1 SECOND) WHERE task_id=47")
        state = owners()
        sql(rollback, success=False)
        assert owners() == state and count('SELECT COUNT(*) FROM sys_task_leader_repair_20261029 WHERE rolled_back_at IS NOT NULL') == 0
        checks.append('Rollback refuses the whole batch when later task edits exist')

        reset()
        sql('UPDATE biz_task SET leader_id=110228 WHERE task_id=1')
        assert 'ROOT_OR_SCOPE_CHANGED' in sql(preview)
        sql("UPDATE sys_user SET status='0' WHERE user_id=110254")
        assert 'OWNER_UNAVAILABLE' in sql(preview)
        checks.append('Changed root responsibility and disabled original owners are not overwritten')

        reset()
        before_owners, before_fields, before_other = owners(), task_digest(), other_checksums()
        sql("CREATE TRIGGER fail_owner_repair BEFORE UPDATE ON biz_task FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic rollback test'")
        sql(forward, success=False)
        assert owners() == before_owners and task_digest() == before_fields and other_checksums() == before_other
        assert count('SELECT COUNT(*) FROM bak_20261029_task_leader') == 0
        assert count('SELECT COUNT(*) FROM sys_task_leader_repair_20261029') == 0
        checks.append('Injected write failure rolls back task updates, backups and success evidence atomically')
    finally:
        assert re.fullmatch(r'biz_owner_repair_test_[0-9a-f]{32}', schema)
        sql('DROP DATABASE `' + schema + '`', database=None)
    report = {'passed': True, 'checks': checks, 'temporaryDatabaseRemoved': True,
              'localBusinessDatabaseModified': False, 'productionDatabaseAccessed': False}
    destination = ROOT.parent / '.run/owner-field-audit-20261029/repair-results.json'
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
