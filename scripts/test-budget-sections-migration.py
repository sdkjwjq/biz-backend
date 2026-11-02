"""Repeat and safely roll back the section split in an isolated historical schema."""
from pathlib import Path
import os,re,subprocess,json,uuid,hashlib,shutil
ROOT=Path(__file__).resolve().parents[1]
def main():
 env=dict(os.environ,MYSQL_PWD=os.environ['SHUANGGAO_TEST_DB_PASSWORD'])
 mysql=shutil.which('mysql') or 'C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe'
 schema='biz_budget_sections_test_'+uuid.uuid4().hex
 def sql(value,db=schema,success=True):
  r=subprocess.run([mysql,'--host=127.0.0.1','--user=root','--batch','--skip-column-names','--default-character-set=utf8mb4',*([db] if db else [])],input=value if isinstance(value,bytes) else value.encode(),env=env,capture_output=True)
  assert (r.returncode==0)==success,r.stderr.decode('utf-8',errors='replace')
  return r.stdout.decode('utf-8').strip()
 source=(ROOT/'data/20260920backup.sql').read_bytes();assert not re.search(rb'(?im)^\s*(USE |CREATE DATABASE)',source)
 modules=['section','goods-budget','execution-budget-rows'];forward=b'\n'.join((ROOT/('data/migrations/2026-11-02-budget-'+m+'.sql')).read_bytes() for m in modules)
 rollback=(ROOT/'data/migrations/2026-11-02-budget-sections-rollback.sql').read_bytes();checks=[]
 sql('CREATE DATABASE `'+schema+'` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci',None)
 try:
  sql(source)
  original={}
  for table in ['biz_budget_sheet','biz_budget_task_item']:
   columns=sql("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='"+table+"' ORDER BY ORDINAL_POSITION").splitlines()
   original[table]=columns
  def values():
   return {t:hashlib.sha256(sql('SELECT '+','.join('`'+c+'`' for c in cols)+' FROM `'+t+'` ORDER BY 1').encode()).hexdigest() for t,cols in original.items()}
  before=values();sql(forward);assert values()==before;sql(forward);assert values()==before
  verified=sql((ROOT/'data/migrations/2026-11-02-budget-sections-check.sql').read_bytes());assert all(line.endswith('\t0') for line in verified.splitlines()),verified
  checks.append('Historical monthly sheets, cell values and original IDs survive the incremental migration and repeat')
  sql("INSERT INTO biz_budget_sheet(year,month,section,locked,is_delete) VALUES(2026,1,'execution',0,0)");execution=int(sql("SELECT sheet_id FROM biz_budget_sheet WHERE year=2026 AND month=1 AND section='execution'"))
  # Same period may have independent annual and execution rows.
  assert sql("SELECT COUNT(*) FROM biz_budget_sheet WHERE year=2026 AND month=1 AND section='execution'")=='1'
  sql(rollback,success=False);assert sql("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_sheet' AND COLUMN_NAME='section'")=='1'
  checks.append('Execution data blocks destructive schema rollback before any columns are dropped')
  sql('DELETE FROM biz_budget_sheet WHERE sheet_id='+str(execution));assert values()==before
  sql(rollback);assert values()==before;sql(rollback);assert values()==before
  checks.append('Pristine rollback restores the old unique key and retains every original cell; repeated rollback is safe')
  sql(forward);assert values()==before
  checks.append('Forward migration can be reapplied after pristine rollback')
 finally:
  assert re.fullmatch('biz_budget_sections_test_[0-9a-f]{32}',schema);sql('DROP DATABASE `'+schema+'`',None)
 report={'passed':True,'checks':checks,'isolatedDatabaseRemoved':True,'businessDatabasesModified':False}
 (ROOT/'target/budget-sections-migration-results.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8');print(json.dumps(report,ensure_ascii=False,indent=2))
if __name__=='__main__':main()
