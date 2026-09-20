"""Verify release SQL against synthetic data in a disposable MySQL schema."""
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import uuid
import zipfile
import tempfile
import socket
import time
import urllib.request

ROOT=Path(__file__).resolve().parents[2]
PACKAGE=ROOT.parent/'releases/shuanggao-update-20260920'
MYSQL=Path(r'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe')
BASH=r'C:\Program Files\Git\bin\bash.exe'
env=os.environ.copy()
env['MYSQL_PWD']=os.environ['SHUANGGAO_TEST_DB_PASSWORD']
schema='biz_review_test_'+uuid.uuid4().hex
args=['--host=127.0.0.1','--user=root','--default-character-set=utf8mb4']
checks=[]

def sql(value,db=schema):
    return subprocess.run([str(MYSQL),*args,'--batch','--skip-column-names',*([db] if db else [])],
                          input=value.encode('utf-8') if isinstance(value,str) else value,env=env,check=True,capture_output=True).stdout

def check_schema(mode,success=True):
    bash_path='/'+PACKAGE.as_posix()[0].lower()+PACKAGE.as_posix()[2:]
    selected=env.copy(); selected['DB_NAME']=schema
    result=subprocess.run([BASH,'-c','export PATH="/c/Program Files/MySQL/MySQL Server 8.0/bin:$PATH"; bash "$1/schema-check.sh" "$2"',
                           'test',bash_path,mode],env=selected,capture_output=True)
    assert (result.returncode==0)==success,result.stdout.decode('utf-8',errors='replace')+result.stderr.decode('utf-8',errors='replace')

def snapshot():
    values={}
    for line in sql('SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_TYPE="BASE TABLE"').decode().splitlines():
        assert re.fullmatch('[A-Za-z0-9_]+',line)
        values[line]=sql('SELECT * FROM `'+line+'`')
    return values

def smoke_jar():
    sql("UPDATE sys_user SET password='ReleaseSmoke123';")
    # Pick a local free port; all uploads, logs and credentials stay in a temporary test directory.
    with socket.socket() as listener:
        listener.bind(('127.0.0.1',0)); port=listener.getsockname()[1]
    with tempfile.TemporaryDirectory(prefix='release-jar-',dir=ROOT/'target') as temp:
        directory=Path(temp); properties=directory/'release.properties'
        properties.write_text('spring.datasource.url=jdbc:mysql://127.0.0.1:3306/'+schema+'?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai\n'
                              +'spring.datasource.username=root\nspring.datasource.password='+env['MYSQL_PWD']+'\nschedule.enabled=false\n',encoding='utf-8')
        with (ROOT/'target/linux-release-build/jar-smoke.log').open('wb') as log:
            process=subprocess.Popen(['java','-jar',str(PACKAGE/'backend/biz_backend-1.0-SNAPSHOT.jar'),
                                      '--server.port='+str(port),'--spring.config.additional-location='+properties.as_uri()],
                                     cwd=directory,stdout=log,stderr=subprocess.STDOUT,creationflags=getattr(subprocess,'CREATE_NO_WINDOW',0))
            try:
                address='http://127.0.0.1:'+str(port)+'/api'
                def call(path,data=None,token=None):
                    headers={'Content-Type':'application/json'}
                    if token: headers['Authorization']=token
                    request=urllib.request.Request(address+path,data=json.dumps(data).encode() if data is not None else None,headers=headers)
                    with urllib.request.urlopen(request,timeout=3) as response:return json.load(response)
                for attempt in range(60):
                    assert process.poll() is None,'Release JAR stopped; inspect target/linux-release-build/jar-smoke.log'
                    try:
                        result=call('/system/login',{'user_id':-1,'password':'release-connectivity-probe'})
                        if result.get('message')=='用户不存在':break
                    except OSError:pass
                    time.sleep(1)
                else:raise AssertionError('Release JAR did not become healthy')
                token=call('/system/login',{'user_id':110228,'password':'ReleaseSmoke123'})['token']
                assert call('/biz/tasks/manage/capabilities',token=token)['canCreate'] is True
                capabilities=call('/work-records/capabilities',token=token)
                assert isinstance(capabilities,dict) and capabilities.get('code',0)<400
            finally:
                process.terminate(); process.wait(timeout=30)
    checks.append('packaged-jar-real-start-external-password-login-and-new-module-apis')

def main():
    for script in ['update.sh','schema-check.sh']:
        subprocess.run([BASH,'-n',str(PACKAGE/script)],check=True)
    checks.append('bash-syntax')
    # Exercise rollback functions using real files and tar, but mocked process/health operations.
    # Never run the Linux deployment entry point or access production paths on Windows.
    with tempfile.TemporaryDirectory(prefix='release-shell-',dir=ROOT/'target') as temp:
        base=Path(temp)
        prefix=(PACKAGE/'update.sh').read_text(encoding='utf-8').split('[[ $EUID == 0 ]]')[0]
        harness=prefix+'''
BACKEND="$1/backend"; WEB="$1/web"; BACKUP="$1/backup"
mkdir -p "$BACKEND/uploads" "$WEB" "$BACKUP"
printf 'old-jar' > "$BACKUP/old.jar"
printf 'old-index' > "$WEB/index.html"
tar -czf "$BACKUP/frontend.tar.gz" -C "$WEB" .
printf 'new-index' > "$WEB/index.html"
printf 'keep-new-hashed-asset' > "$WEB/new-asset.js"
printf 'keep-upload' > "$BACKEND/uploads/material.docx"
printf 'new-config' > "$BACKEND/.release.properties"
stop_app() { printf 'stopped' > "$BACKUP/stopped"; }
start_old() { printf 'started' > "$BACKUP/started"; }
health() { return 0; }
CHANGED=1
trap cleanup EXIT
exit 17
'''
        script=base/'test.sh';script.write_text(harness,encoding='utf-8',newline='\n')
        bash_base='/'+base.as_posix()[0].lower()+base.as_posix()[2:]
        result=subprocess.run([BASH,str(script),bash_base],capture_output=True)
        assert result.returncode==1,result.stderr
        assert (base/'backend/biz_backend-1.0-SNAPSHOT.jar').read_text()=='old-jar'
        assert (base/'web/index.html').read_text()=='old-index'
        assert (base/'web/new-asset.js').exists() and (base/'backend/uploads/material.docx').exists()
        assert not (base/'backend/.release.properties').exists()
        assert (base/'backup/started').exists()
    checks.append('simulated-failure-restores-app-and-web-keeps-upload-and-assets')
    for line in (PACKAGE/'SHA256SUMS').read_text().splitlines():
        digest,name=line.split('  ',1)
        assert hashlib.sha256((PACKAGE/name).read_bytes()).hexdigest()==digest
    with zipfile.ZipFile(PACKAGE/'backend/biz_backend-1.0-SNAPSHOT.jar') as jar:
        p=jar.read('BOOT-INF/classes/application.properties').decode()
        assert 'spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}' in p
        assert not re.search(r'^\s*#?\s*spring.datasource.password=(?!\$\{)',p,re.M)
        assert 'BOOT-INF/classes/templates/work-record.docx' in jar.namelist()
    assert (PACKAGE/'frontend/templates/budget-template.xlsx').read_bytes()==(PACKAGE/'templates/budget-template.xlsx').read_bytes()
    checks.append('checksums-password-exclusion-and-templates')
    ddl=(PACKAGE/'sql/additive.sql').read_text(encoding='utf-8')
    assert not re.search(r'^\s*(DROP|DELETE|UPDATE|INSERT|TRUNCATE|REPLACE)\b',ddl,re.M|re.I)
    dump=subprocess.check_output([str(MYSQL.with_name('mysqldump.exe')),*args,'--no-data','--skip-triggers','--set-gtid-purged=OFF','biz'],env=env)
    assert not re.search(rb'^\s*(CREATE DATABASE|USE )',dump,re.M)
    sql('CREATE DATABASE `'+schema+'` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci',None)
    try:
        sql(dump)
        sql((ROOT/'scripts/ui-audit/fixture.sql').read_bytes())
        sql('DROP TABLE biz_work_record_snapshot; DROP TABLE biz_work_record_entry; DROP TABLE biz_work_record;')
        before=snapshot()
        check_schema('before')
        check_schema('after',False)
        sql(ddl)
        check_schema('after')
        after=snapshot()
        assert all(after[name]==data for name,data in before.items())
        checks.append('missing-work-record-tables-created-existing-rows-unchanged')
        sql("INSERT INTO biz_work_record(record_id,owner_id,owner_name,record_year,record_month,status,version,create_time,update_time) VALUES(1,910003,'Synthetic',2026,7,1,1,NOW(),NOW());")
        sql("INSERT INTO biz_work_record_snapshot(record_id,statistics_json,generated_time) VALUES(1,'{}',NOW());")
        before=snapshot(); sql(ddl); check_schema('after'); assert snapshot()==before
        checks.append('repeat-migration-preserves-all-rows-including-submitted-work-record')
        sql('ALTER TABLE biz_audit_snapshot DROP COLUMN previous_comment; ALTER TABLE biz_performance_audit_snapshot DROP COLUMN previous_year_target_value;')
        check_schema('before'); sql(ddl); check_schema('after')
        checks.append('approved-legacy-missing-columns-added')
        smoke_jar()
        sql('ALTER TABLE biz_task DROP COLUMN exp_effect;')
        check_schema('before',False)
        checks.append('unknown-missing-business-column-blocks-deployment')
    finally:
        assert re.fullmatch(r'biz_review_test_[0-9a-f]{32}',schema)
        sql('DROP DATABASE `'+schema+'`',None)
    checks.append('isolated-database-removed')
    result={'checks':checks,'production_database_accessed':False,'linux_process_test':'not run on Windows'}
    (ROOT/'target/linux-release-build/verification.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
    print(json.dumps(result,indent=2))

if __name__=='__main__': main()
