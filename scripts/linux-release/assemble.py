"""Build an incremental Linux release without credentials or production backups."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tarfile
import zipfile

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT.parent / ('releases/' + os.environ.get('RELEASE_NAME', 'shuanggao-update-20261015'))
assert re.fullmatch(r'shuanggao-update-\d{8}(?:-r\d+)?', OUT.name)
assert OUT.resolve().parent == (ROOT.parent/'releases').resolve()
MYSQL = shutil.which('mysql') or r'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
NEW_SOURCES = [
    'data/migrations/2026-05-30-audit-snapshot.sql',
    'data/migrations/2026-05-30-performance-audit.sql',
    'data/migrations/2026-05-31-achievement-audit.sql',
    'data/migrations/2026-05-31-budget.sql',
    'scripts/work-records/001_work_records.sql',
    'data/migrations/2026-09-24-ownership-change-log.sql',
    'data/migrations/2026-09-24-performance-relation-edit.sql',
    'data/migrations/2026-09-26-budget-import-version.sql',
    'data/migrations/2026-09-27-admin-management.sql',
    'data/migrations/2026-09-28-release-migration.sql',
    'data/migrations/2026-10-12-leadership-access.sql',
    'data/migrations/2026-10-12-core-indicators.sql',
]
ADDITIONS = {
    ('biz_core_report','performance_code'): 'varchar(30) DEFAULT NULL',
    ('biz_task','is_priority'): 'tinyint NOT NULL DEFAULT 0',
    ('biz_task','is_difficult'): 'tinyint NOT NULL DEFAULT 0',
    ('biz_task','planned_completion_date'): 'date DEFAULT NULL',
    ('biz_task','actual_completion_date'): 'datetime DEFAULT NULL',
    ('biz_task','management_version'): 'bigint NOT NULL DEFAULT 0',
    ('biz_audit_snapshot','previous_comment'): 'varchar(500) DEFAULT NULL',
    ('biz_performance_audit_snapshot','previous_year_target_value'): 'decimal(20,4) DEFAULT 0.0000',
    ('biz_achievement','audit_status'): 'int NOT NULL DEFAULT 30',
    ('biz_achievement','current_handler_id'): 'bigint DEFAULT NULL',
    ('biz_achievement','update_time'): 'datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP',
    ('rel_task_performance','is_delete'): 'tinyint(1) DEFAULT 0',
}

def query(sql):
    env = os.environ.copy()
    env['MYSQL_PWD'] = os.environ['SHUANGGAO_TEST_DB_PASSWORD']
    return subprocess.check_output([MYSQL,'--host=127.0.0.1','--user=root','--default-character-set=utf8mb4',
                                    '--batch','--skip-column-names',os.environ.get('RELEASE_METADATA_DB','biz'),'-e',sql],env=env).decode('utf-8').replace('\r\n','\n')

def main():
    if OUT.exists():
        shutil.rmtree(OUT)
    for directory in ['backend','frontend','sql','templates']:
        (OUT/directory).mkdir(parents=True,exist_ok=True)
    source = ROOT/'target/linux-release-build/target/biz_backend-1.0-SNAPSHOT.jar'
    with zipfile.ZipFile(source) as jar:
        properties = jar.read('BOOT-INF/classes/application.properties').decode('utf-8')
        assert 'spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}' in properties
        assert not re.search(r'^\s*#?\s*spring.datasource.password=(?!\$\{)',properties,re.M)
        assert len(jar.read('BOOT-INF/classes/templates/work-record.docx')) > 1000
    shutil.copy2(source,OUT/'backend'/source.name)
    shutil.copytree(ROOT.parent/'biz/dist',OUT/'frontend',dirs_exist_ok=True)
    shutil.copy2(ROOT.parent/'biz/public/templates/budget-template.xlsx',OUT/'templates/budget-template.xlsx')
    for name in ['update.sh','schema-check.sh','database-restore.sh','README.md']:
        text=(Path(__file__).parent/name).read_text(encoding='utf-8')
        (OUT/name).write_text(text,encoding='utf-8',newline='\n')
    verification=ROOT/'target/linux-release-build/verification.json'
    if verification.exists(): shutil.copy2(verification,OUT/'verification.json')

    creates=[]
    added_tables=set()
    for filename in NEW_SOURCES:
        contents=(ROOT/filename).read_text(encoding='utf-8')
        for match in re.finditer(r'CREATE TABLE(?: IF NOT EXISTS)?\s+`?(\w+)`?\s*\(.*?\)\s*ENGINE=.*?;',contents,re.S):
            name=match.group(1)
            assert name not in added_tables
            added_tables.add(name)
            creates.append(re.sub(r'^CREATE TABLE(?: IF NOT EXISTS)?','CREATE TABLE IF NOT EXISTS',match.group(0)))
    assert len(added_tables)==23
    sql='-- Additive structure only. No historical data repair, account writes or seed records.\n'+ '\n\n'.join(creates)+'\n'
    for (table,column),definition in ADDITIONS.items():
        sql+=f"""
SET @exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='{table}' AND COLUMN_NAME='{column}');
SET @ddl = IF(@exists=0, 'ALTER TABLE `{table}` ADD COLUMN `{column}` {definition}', 'SELECT 1');
PREPARE release_stmt FROM @ddl;
EXECUTE release_stmt;
DEALLOCATE PREPARE release_stmt;
"""
    assert not re.search(r'^\s*(?:DROP|DELETE|TRUNCATE|INSERT|UPDATE|REPLACE)\b',sql,re.M|re.I)
    for migration in ['scripts/work-records/002_work_record_delete.sql', 'scripts/password/001_force_password_change.sql']:
        sql += '\n' + (ROOT/migration).read_text(encoding='utf-8')
    (OUT/'sql/additive.sql').write_text(sql,encoding='utf-8',newline='\n')
    for module in ['leadership-access', 'task-attributes', 'core-indicators']:
        for suffix in ['', '-check', '-rollback']:
            source = ROOT / ('data/migrations/2026-10-12-' + module + suffix + '.sql')
            contents = source.read_text(encoding='utf-8')
            if module == 'core-indicators' and suffix == '-check':
                contents = contents.replace('p.perf_code=c.indicator_code', 'BINARY p.perf_code=BINARY c.indicator_code')
            (OUT/'sql'/source.name).write_text(contents,encoding='utf-8',newline='\n')
    for suffix in ['', '-check', '-rollback']:
        source = ROOT / ('data/migrations/2026-10-14-core-indicator-report-mapping'+suffix+'.sql')
        (OUT/'sql'/source.name).write_text(source.read_text(encoding='utf-8'),encoding='utf-8',newline='\n')
    for suffix in ['', '-check', '-rollback']:
        source = ROOT / ('data/migrations/2026-10-23-leadership-schoolwide'+suffix+'.sql')
        (OUT/'sql'/source.name).write_text(source.read_text(encoding='utf-8'),encoding='utf-8',newline='\n')
    budget_version_migration = (ROOT/'data/migrations/2026-09-26-budget-import-version.sql').read_text(encoding='utf-8')
    backfill_marker = '-- 为升级前已有的当前预算建立只读初始版本。脚本重复执行不会重复写入。'
    assert budget_version_migration.count(backfill_marker) == 1
    budget_version_backfill = budget_version_migration[budget_version_migration.index(backfill_marker):]
    assert 'INSERT INTO `biz_budget_import_version`' in budget_version_backfill
    assert 'NOT EXISTS' in budget_version_backfill
    assert not re.search(r'(?im)^\s*(?:DROP|TRUNCATE|DELETE|UPDATE|REPLACE)\b', budget_version_backfill)
    (OUT/'sql/budget-version-backfill.sql').write_text(budget_version_backfill,encoding='utf-8',newline='\n')
    budget_version_check = (ROOT/'data/migrations/2026-09-26-budget-import-version-check.sql').read_text(encoding='utf-8')
    assert not re.search(r'(?im)^\s*(?:ALTER|CREATE|DROP|TRUNCATE|DELETE|INSERT|UPDATE|REPLACE)\b', budget_version_check)
    (OUT/'sql/budget-version-check.sql').write_text(budget_version_check,encoding='utf-8',newline='\n')
    for source, destination in [
        ('deploy/2026-06-26-achievement-archive-record-fix.sql', 'achievement-archive-record-fix.sql'),
        ('data/migrations/2026-09-26-level4-equal-weight-recalculate.sql', 'level4-equal-weight-recalculate.sql'),
        ('data/migrations/2026-09-26-level4-equal-weight-verify.sql', 'level4-equal-weight-verify.sql'),
        ('data/migrations/2026-09-26-level4-equal-weight-rollback.sql', 'manual-level4-equal-weight-rollback.sql'),
        ('data/migrations/2026-09-26-budget-import-version-rollback.sql', 'manual-budget-version-rollback.sql'),
        ('data/migrations/2026-09-27-admin-management-rollback.sql', 'manual-admin-management-rollback.sql'),
    ]:
        text = (ROOT/source).read_text(encoding='utf-8')
        (OUT/'sql'/destination).write_text(text,encoding='utf-8',newline='\n')
    data_update = ROOT/'data/migrations/2026-09-18-task-auditor-dept-principal-update.sql'
    data_sql = data_update.read_text(encoding='utf-8')
    assert data_sql.startswith('-- 2026-09-18 task update')
    assert 'START TRANSACTION;' in data_sql and 'COMMIT;' in data_sql
    assert not re.search(r'(?im)^\s*(?:DROP\s+TABLE|TRUNCATE|DELETE\s+FROM)\b', data_sql)
    assert 'bak_20260918_task_auditor_dept_principal' in data_sql
    (OUT/'sql/task-update-20260918.sql').write_text(data_sql,encoding='utf-8',newline='\n')
    stale = (ROOT/'src/main/resources/sql/stale-review-notices.sql').read_text(encoding='utf-8').strip()
    (OUT/'sql/retire-stale-notices.sql').write_text(
        'START TRANSACTION;\nUPDATE sys_notice n SET n.is_delete=1 WHERE '+stale+';\nSELECT ROW_COUNT();\nCOMMIT;\n',
        encoding='utf-8',newline='\n')
    (OUT/'sql/backup-stale-notices.sql').write_text(
        "SELECT CONCAT('UPDATE sys_notice SET is_delete=', COALESCE(CAST(n.is_delete AS CHAR), 'NULL'), "
        "' WHERE notice_id=', n.notice_id, ' AND is_delete=1;') FROM sys_notice n WHERE "+stale+';\n',
        encoding='utf-8',newline='\n')
    base=(ROOT/'data/biz.sql').read_text(encoding='utf-8')
    tables=set(re.findall(r'CREATE TABLE\s+`?(\w+)',base))|added_tables
    selection=','.join("'"+name+"'" for name in sorted(tables))
    columns=query('SELECT TABLE_NAME,COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('+selection+') ORDER BY TABLE_NAME,ORDINAL_POSITION')
    assert {line.split('\t')[0] for line in columns.splitlines()}==tables
    additional = {('sys_user', 'force_password_change'), *[('biz_work_record', c) for c in ['delete_marker','deleted_by','deleted_time','delete_reason']]}
    columns = '\n'.join(sorted(set(columns.splitlines()) | {'\t'.join(pair) for pair in additional}))+'\n'
    (OUT/'sql/required-columns.tsv').write_text(columns,encoding='utf-8',newline='\n')
    allowed='\n'.join(line for line in columns.splitlines() if line.split('\t')[0] in added_tables or tuple(line.split('\t')) in ADDITIONS or tuple(line.split('\t')) in additional)+'\n'
    (OUT/'sql/allowed-additions.tsv').write_text(allowed,encoding='utf-8',newline='\n')
    new_selection=','.join("'"+name+"'" for name in sorted(added_tables))
    unique=query("SELECT TABLE_NAME,GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX SEPARATOR ',') FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND NON_UNIQUE=0 AND TABLE_NAME IN ("+new_selection+") GROUP BY TABLE_NAME,INDEX_NAME ORDER BY TABLE_NAME,INDEX_NAME")
    unique = unique.replace('biz_work_record\towner_id,record_year,record_month\n', 'biz_work_record\towner_id,record_year,record_month,delete_marker\n')
    (OUT/'sql/required-unique.tsv').write_text(unique,encoding='utf-8',newline='\n')
    manifest={
        'backend_commit': subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),
        'frontend_commit': subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT.parent/'biz',text=True).strip(),
        'frontend_working_changes': subprocess.check_output(['git','diff','--name-only'],cwd=ROOT.parent/'biz',text=True).splitlines(),
        'runtime':'Java 17 / MySQL 8 / Bash 4.2+',
        'database_rows_included':False,'backup_contains_user_passwords':False,'runtime_database_password_included':False,
        'restore_supported':False,
        'notice_cleanup':'Only obsolete task/performance/achievement review requests are soft-deleted; audit data unchanged',
        'data_updates':['sql/budget-version-backfill.sql','sql/task-update-20260918.sql',
                        'sql/achievement-archive-record-fix.sql','sql/level4-equal-weight-recalculate.sql'],
        'one_time_migrations':['task-auditor-dept-principal-20260918',
                               'achievement-archive-record-fix-20260626',
                               'level4-equal-weight-recalculate-20260926'],
        'data_update_backup_tables':['bak_20260918_task_code_fix','bak_20260918_task_auditor_dept_principal',
                                     'biz_task_backup_before_level4_equal_weight_20260926',
                                     'biz_level4_task_backup_before_equal_weight_20260926',
                                     'biz_material_submission_backup_before_equal_weight_20260926',
                                     'biz_performance_backup_before_equal_weight_20260926',
                                     'biz_performance_year_backup_before_equal_weight_20260926',
                                     'bak_20261014_core_mapping','bak_20261014_core_report_source','bak_20261014_core_notice'],
        'new_tables':sorted(added_tables),'supported_existing_column_additions':['.'.join(key) for key in ADDITIONS],
    }
    manifest['release_name'] = OUT.name
    manifest['budget_trend_visible'] = False
    manifest['core_indicators_layout'] = 'table-with-expandable-recent-reports'
    manifest['core_indicator_fillers'] = ['active-admin', 'active-office-department-100', 'year-task-owner']
    manifest['data_updates'] += ['sql/2026-10-12-'+module+'.sql' for module in ['leadership-access','task-attributes','core-indicators']]
    manifest['data_updates'].append('sql/2026-10-14-core-indicator-report-mapping.sql')
    manifest['one_time_migrations'].append('core-indicator-report-mapping-20261014')
    manifest['data_updates'].append('sql/2026-10-23-leadership-schoolwide.sql')
    manifest['one_time_migrations'].append('leadership-schoolwide-20261023')
    manifest['data_update_backup_tables'] += ['bak_20261023_leadership','bak_20261023_leadership_department']
    manifest['budget_fillers'] = ['active-admin','active-office-department-100','active-finance-department-104']
    (OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
    files=sorted(p for p in OUT.rglob('*') if p.is_file() and p.name!='SHA256SUMS')
    (OUT/'SHA256SUMS').write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.relative_to(OUT).as_posix()+'\n' for p in files),encoding='ascii',newline='\n')
    archive=OUT.with_suffix('.tar.gz')
    def mode(info):
        info.uid=info.gid=0; info.uname=info.gname='root'
        info.mode=0o700 if info.isdir() or info.name.endswith('.sh') else 0o600
        return info
    with tarfile.open(archive,'w:gz') as tar:
        tar.add(OUT,arcname=OUT.name,filter=mode)
    archive.with_suffix(archive.suffix+'.sha256').write_text(hashlib.sha256(archive.read_bytes()).hexdigest()+'  '+archive.name+'\n',encoding='ascii',newline='\n')
    print(archive)
    print('bytes='+str(archive.stat().st_size))

if __name__=='__main__': main()
