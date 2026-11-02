"""Real-MySQL isolated checks for the reviewed workbook-only association migration."""
from pathlib import Path
import os,re,json,subprocess,uuid,hashlib,decimal,sys

ROOT=Path(__file__).resolve().parents[1]
MYSQL='C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe'
PREFIX=ROOT/'data/migrations/2026-11-02-performance-links'
OUT=ROOT.parent/'.run/performance-relations-review-20261102/migration-results.json'
manifest=json.loads((ROOT/'data/repairs/performance-links-20261102.json').read_text(encoding='utf-8'))
password=re.search(r'^spring.datasource.password=(.*)$',(ROOT/'src/main/resources/application.properties').read_text(encoding='utf-8'),re.M).group(1).strip()
ENV=dict(os.environ,MYSQL_PWD=password)
schema='biz_perf_links_test_'+uuid.uuid4().hex
checks=[]

def sql(value,db=schema,success=True):
 p=subprocess.run([MYSQL,'-h127.0.0.1','-uroot','--default-character-set=utf8mb4','--batch','--skip-column-names',*([db] if db else [])],input=value if isinstance(value,bytes) else value.encode(),capture_output=True,env=ENV)
 if success and p.returncode:raise RuntimeError(p.stderr.decode('utf-8',errors='replace'))
 if not success:assert p.returncode,p.stdout[-1000:]
 return p.stdout.decode('utf-8').strip() if success else p.stderr.decode('utf-8',errors='replace')

def migration(suffix=''):return Path(str(PREFIX)+suffix+'.sql').read_bytes()
def rows(q):return [line.split('\t') for line in sql(q).splitlines() if line]
def fingerprint(table,fields='*',order='1'):return hashlib.sha256(sql('SELECT '+fields+' FROM '+table+' ORDER BY '+order).encode()).hexdigest()
def active():return sql('SELECT perf_id,year_id,task_id FROM rel_task_performance WHERE COALESCE(is_delete,0)=0 ORDER BY perf_id,year_id,task_id')
def record(name):checks.append(name);print('PASS',name,flush=True)

def main():
 source=subprocess.check_output([str(Path(MYSQL).with_name('mysqldump.exe')),'-h127.0.0.1','-uroot','--single-transaction','--skip-triggers','--set-gtid-purged=OFF','biz'],env=ENV)
 assert not re.search(rb'(?im)^\s*(USE |CREATE DATABASE)',source)
 def reset():
  tables=rows('SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE()')
  if tables:sql('SET FOREIGN_KEY_CHECKS=0;DROP TABLE '+','.join('`'+r[0]+'`' for r in tables)+';SET FOREIGN_KEY_CHECKS=1;')
  sql(source)
  sql((ROOT/'data/migrations/2026-09-28-release-migration.sql').read_bytes())
 sql('CREATE DATABASE `'+schema+'` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci',None)
 try:
  reset()
  original={t:fingerprint(t) for t in ['biz_task','biz_level4_task','biz_audit_log','biz_material_submission','biz_performance_submission','biz_performance_audit_log','sys_user']}
  old_active=active();old_year=fingerprint('biz_performance_year');old_perf=fingerprint('biz_performance')
  protected_perf=fingerprint('biz_performance','perf_id,perf_code,perf_name,leader_id,auditor_id,principal_id,dept_id,target_value,data_type,is_delete')
  targets=fingerprint('biz_performance_year','year_id,perf_id,year,target_value,data_type,is_delete')
  deferred={}
  for x in manifest['deferred']:
   key=(x['code'],x['year']);deferred[key]=sql("SELECT r.task_id FROM rel_task_performance r JOIN biz_performance p ON p.perf_id=r.perf_id JOIN biz_performance_year y ON y.year_id=r.year_id WHERE p.perf_code='%s' AND y.year=%d AND COALESCE(r.is_delete,0)=0 ORDER BY r.task_id"%key)
  result=sql(migration());record('Forward migration on an exact business-data clone')
  applied=sql('SELECT annual_cells,added,removed FROM sys_perf_links_20261102');assert applied=='231\t29\t624',applied
  verify=sql(migration('-check'))
  for n in ['relation_target_mismatch','annual_value_mismatch','total_value_mismatch','task_source_changed','post_migration_drift']:assert n+'\t0' in verify,verify[-2000:]
  assert 'migration_applied\t1' in verify
  record('231 cells, 29 added and 624 soft-removed; all result checks zero')
  for key,value in deferred.items():
   actual=sql("SELECT r.task_id FROM rel_task_performance r JOIN biz_performance p ON p.perf_id=r.perf_id JOIN biz_performance_year y ON y.year_id=r.year_id WHERE p.perf_code='%s' AND y.year=%d AND COALESCE(r.is_delete,0)=0 ORDER BY r.task_id"%key)
   assert actual==value,key
  assert fingerprint('biz_performance', 'perf_id,perf_code,perf_name,leader_id,auditor_id,principal_id,dept_id,target_value,data_type,is_delete')==protected_perf
  assert fingerprint('biz_performance_year','year_id,perf_id,year,target_value,data_type,is_delete')==targets
  for t,h in original.items():assert fingerprint(t)==h,t
  record('18 deferred cells, owners, targets, accounts, original task values and approval histories unchanged')
  D=decimal.Decimal
  tmap={int(r[0]):r for r in rows('SELECT task_id,phase,level,data_type,current_value,is_delete FROM biz_task')}
  pmap={int(r[0]):r for r in rows('SELECT perf_id,perf_code,data_type,current_value FROM biz_performance')}
  ymap={int(r[0]):r for r in rows('SELECT year_id,perf_id,year,actual_value,is_delete FROM biz_performance_year')}
  rels=rows('SELECT perf_id,year_id,task_id FROM rel_task_performance WHERE COALESCE(is_delete,0)=0')
  checked=0
  affected_codes={r['code'] for r in manifest['approved']}
  for code,y in {(pmap[int(r[1])][1],int(r[2])) for r in ymap.values() if pmap[int(r[1])][1] in affected_codes and r[4]!='1'}:
   perf=next(r for r in pmap.values() if r[1]==code);year=next(r for r in ymap.values() if r[1]==perf[0] and int(r[2])==y)
   values=[]
   for pr,yr,tid in rels:
    if pr!=perf[0] or yr!=year[0]:continue
    t=tmap[int(tid)]
    if t[5]=='1' or t[2] in ['1','2'] or t[3]=='0' or t[1]!=str(y):continue
    values.append(D(t[4]) if t[4]!='NULL' else D(0))
   expected=min(D(100),max([D(0),*values])) if perf[2]=='2' else sum(values,D(0))
   assert expected==D(year[3]),(code,y,expected,year[3]);checked+=1
  for pid in {int(r[0]) for r in rows('SELECT perf_id FROM bak_20261102_perf_links_total')}:
   p=pmap[pid];vs=[D(y[3]) if y[3]!='NULL' else D(0) for y in ymap.values() if int(y[1])==pid and y[4]!='1']
   expected=min(D(100),max([D(0),*vs])) if p[2]=='2' else sum(vs,D(0))
   assert expected==D(p[3]),(pid,expected,p[3])
  assert checked==355;record('Independent Decimal calculation matches all 355 affected annual values and 71 totals')
  assert sql("SELECT COUNT(*) FROM biz_performance_relation_log WHERE batch_id='perf-links-20261102-v1'")=='653'
  stable={t:fingerprint(t) for t in ['rel_task_performance','biz_performance_year','biz_performance','sys_perf_links_20261102','biz_performance_relation_log','bak_20261102_perf_links_rel']}
  sql(migration())
  for t,h in stable.items():assert fingerprint(t)==h,t
  record('Repeat deployment is a complete no-op, including audit and backup rows')
  sql(migration('-rollback'));assert active()==old_active
  assert fingerprint('biz_performance_year')==old_year and fingerprint('biz_performance')==old_perf
  assert sql("SELECT COUNT(*) FROM biz_performance_relation_log WHERE batch_id='perf-links-20261102-v1-rollback'")=='653'
  sql(migration('-rollback'));sql(migration(),success=False)
  record('Rollback restores original active associations and values, retains audit evidence, and cannot replay stale migration')
  reset();first=manifest['approved'][0]
  perf_id=sql("SELECT perf_id FROM biz_performance WHERE perf_code='%s'"%first['code'])
  year_id=sql('SELECT year_id FROM biz_performance_year WHERE perf_id='+perf_id+' AND year='+str(first['year']))
  sql('UPDATE rel_task_performance SET is_delete=1 WHERE perf_id='+perf_id+' AND year_id='+year_id+' ORDER BY id LIMIT 1')
  before=active();sql(migration(),success=False);assert active()==before and sql('SELECT COUNT(*) FROM sys_perf_links_20261102')=='0'
  record('Unreviewed online association drift aborts the entire transaction')
  reset()
  sql('INSERT INTO biz_performance_submission(perf_id,year_id,year,actual_value,submit_by,flow_status,current_handler_id,is_delete) VALUES('+perf_id+','+year_id+','+str(first['year'])+',0,110228,10,110228,0)')
  before=active();sql(migration(),success=False);assert active()==before
  record('An active approval blocks migration without changing any associations')
  reset();task_code=next('SG%02d%02d%02d%02d'%(*map(int,r['proposed'][0].split('-')),r['year']%100) for r in manifest['approved'] if r['proposed'])
  sql("UPDATE biz_task SET is_delete=1 WHERE task_code='"+task_code+"'")
  before=active();sql(migration(),success=False);assert active()==before
  record('Deleted desired task aborts migration instead of dropping or widening scope')
  reset();sql(migration())
  sql('UPDATE biz_performance_year SET actual_value=actual_value+1 WHERE year_id='+year_id)
  later=fingerprint('biz_performance_year');sql(migration());assert fingerprint('biz_performance_year')==later
  sql(migration('-rollback'),success=False);assert fingerprint('biz_performance_year')==later
  record('Later business edits survive repeated deployment and block unsafe rollback')
  reset();sql(migration());tid=sql('SELECT MIN(task_id) FROM bak_20261102_perf_links_task')
  sql('UPDATE biz_task SET current_value=COALESCE(current_value,0)+1 WHERE task_id='+tid)
  sql(migration('-rollback'),success=False)
  record('A newer source task value blocks rollback of derived performance values')
 finally:
  assert re.fullmatch('biz_perf_links_test_[0-9a-f]{32}',schema)
  sql('DROP DATABASE `'+schema+'`',None)
  print('Isolated test database removed',flush=True)
 OUT.parent.mkdir(exist_ok=True,parents=True)
 OUT.write_text(json.dumps({'passed':True,'checks':checks,'counts':manifest['counts'],'temporaryDatabaseRemoved':True,'businessDatabaseModified':False,'productionAccessed':False},ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

if __name__=='__main__':main()
