"""Build a data-free Linux release from verified artifacts. Local build tool only."""
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
OUT = ROOT.parent / 'releases/shuanggao-update-20260920'
MYSQL = shutil.which('mysql') or r'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
NEW_SOURCES = [
    'data/migrations/2026-05-30-audit-snapshot.sql',
    'data/migrations/2026-05-30-performance-audit.sql',
    'data/migrations/2026-05-31-achievement-audit.sql',
    'data/migrations/2026-05-31-budget.sql',
    'scripts/work-records/001_work_records.sql',
]
ADDITIONS = {
    ('biz_audit_snapshot','previous_comment'): 'varchar(500) DEFAULT NULL',
    ('biz_performance_audit_snapshot','previous_year_target_value'): 'decimal(20,4) DEFAULT 0.0000',
    ('biz_achievement','audit_status'): 'int NOT NULL DEFAULT 30',
    ('biz_achievement','current_handler_id'): 'bigint DEFAULT NULL',
    ('biz_achievement','update_time'): 'datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP',
}

def query(sql):
    env = os.environ.copy()
    env['MYSQL_PWD'] = os.environ['SHUANGGAO_TEST_DB_PASSWORD']
    return subprocess.check_output([MYSQL,'--host=127.0.0.1','--user=root','--default-character-set=utf8mb4',
                                    '--batch','--skip-column-names','biz','-e',sql],env=env).decode('utf-8')

def main():
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
    for name in ['update.sh','schema-check.sh','README.md']:
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
    assert len(added_tables)==12
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
    (OUT/'sql/additive.sql').write_text(sql,encoding='utf-8',newline='\n')
    base=(ROOT/'data/biz.sql').read_text(encoding='utf-8')
    tables=set(re.findall(r'CREATE TABLE\s+`?(\w+)',base))|added_tables
    selection=','.join("'"+name+"'" for name in sorted(tables))
    columns=query('SELECT TABLE_NAME,COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('+selection+') ORDER BY TABLE_NAME,ORDINAL_POSITION')
    assert {line.split('\t')[0] for line in columns.splitlines()}==tables
    (OUT/'sql/required-columns.tsv').write_text(columns,encoding='utf-8',newline='\n')
    allowed='\n'.join(line for line in columns.splitlines() if line.split('\t')[0] in added_tables or tuple(line.split('\t')) in ADDITIONS)+'\n'
    (OUT/'sql/allowed-additions.tsv').write_text(allowed,encoding='utf-8',newline='\n')
    new_selection=','.join("'"+name+"'" for name in sorted(added_tables))
    unique=query("SELECT TABLE_NAME,GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX SEPARATOR ',') FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND NON_UNIQUE=0 AND TABLE_NAME IN ("+new_selection+") GROUP BY TABLE_NAME,INDEX_NAME ORDER BY TABLE_NAME,INDEX_NAME")
    (OUT/'sql/required-unique.tsv').write_text(unique,encoding='utf-8',newline='\n')
    manifest={
        'backend_commit': subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),
        'frontend_commit': subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT.parent/'biz',text=True).strip(),
        'frontend_working_changes': subprocess.check_output(['git','diff','--name-only'],cwd=ROOT.parent/'biz',text=True).splitlines(),
        'runtime':'Java 17 / MySQL 8 / Bash 4.2+',
        'database_rows_included':False,'password_included':False,
        'new_tables':sorted(added_tables),'supported_existing_column_additions':['.'.join(key) for key in ADDITIONS],
    }
    (OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
    files=sorted(p for p in OUT.rglob('*') if p.is_file() and p.name!='SHA256SUMS')
    (OUT/'SHA256SUMS').write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.relative_to(OUT).as_posix()+'\n' for p in files),encoding='ascii',newline='\n')
    archive=OUT.with_suffix('.tar.gz')
    def mode(info):
        info.uid=info.gid=0; info.uname=info.gname='root'
        info.mode=0o755 if info.isdir() or info.name.endswith('.sh') else 0o644
        return info
    with tarfile.open(archive,'w:gz') as tar:
        tar.add(OUT,arcname=OUT.name,filter=mode)
    archive.with_suffix(archive.suffix+'.sha256').write_text(hashlib.sha256(archive.read_bytes()).hexdigest()+'  '+archive.name+'\n',encoding='ascii',newline='\n')
    print(archive)
    print('bytes='+str(archive.stat().st_size))

if __name__=='__main__': main()
