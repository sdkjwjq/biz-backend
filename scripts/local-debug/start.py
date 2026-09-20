import os,json,re,subprocess,shutil,argparse,socket
from pathlib import Path
r=Path(__file__).resolve().parents[2]; w=r/'target/backup-debug';w.mkdir(exist_ok=True)
env=os.environ.copy();env['MYSQL_PWD']=env['SHUANGGAO_TEST_DB_PASSWORD'];env['SHUANGGAO_TEST_DB_USER']='root'
mysql=[shutil.which('mysql'),'--host=127.0.0.1','--user=root','--default-character-set=utf8mb4','--batch','--skip-column-names']
schema='biz_debug_20260920'
def sql(s,db=None):
 return subprocess.check_output(mysql+([db] if db else []),input=s.encode() if isinstance(s,str) else s,env=env)
parser=argparse.ArgumentParser(description='Local private backup debug services; never overwrites an existing schema')
parser.add_argument('--initialize',action='store_true')
options=parser.parse_args()
for port in [5175,18082]:
 with socket.socket() as check:
  check.bind(('127.0.0.1',port))
exists=bool(sql("SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='"+schema+"'").strip())
if options.initialize:
 assert not exists, 'Debug schema already exists; refusing overwrite'
 backup=(r/'data/20260920backup.sql').read_bytes()
 assert not re.search(rb'(?im)^\s*(USE\s|CREATE DATABASE|DROP DATABASE)',backup)
 sql('CREATE DATABASE '+schema+' CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci')
 sql(backup,schema);sql((r.parent/'releases/shuanggao-update-20260920-r3/sql/additive.sql').read_bytes(),schema)
else:
 assert exists, 'Initialize the private debug schema first with --initialize'
source=(r/'scripts/ui-audit/IsolatedUiAuditServer.java').read_text(encoding='utf-8').replace('IsolatedUiAuditServer','BackupDebugServer').replace('biz_review_test_[0-9a-f]{32}',schema).replace('18080','18082')
(w/'BackupDebugServer.java').write_text(source,encoding='utf-8')
cp=os.pathsep.join([str(w),str(r/'target/classes'),str(r/'target/test-classes'),(r/'target/ui-audit-classpath.txt').read_text().strip()])
subprocess.run(['javac','-encoding','UTF-8','-cp',cp,'-d',str(w),str(w/'BackupDebugServer.java')],check=True)
env['SHUANGGAO_TEST_JDBC_URL']='jdbc:mysql://127.0.0.1:3306/'+schema+'?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
log=(w/'backend.log').open('wb'); p=subprocess.Popen(['java','-cp',cp,'org.example.BackupDebugServer'],cwd=w,env=env,stdout=log,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
front=r.parent/'biz';vite=w/'vite.mjs'
vite.write_text('import { createServer } from '+json.dumps((front/'node_modules/vite/dist/node/index.js').as_uri())+'; const s=await createServer({root:'+json.dumps(str(front))+',server:{host:"127.0.0.1",port:5175,strictPort:true,proxy:{"/api":{target:"http://127.0.0.1:18082",changeOrigin:true}}}});await s.listen();',encoding='utf-8')
f=subprocess.Popen(['node',str(vite)],cwd=w,stdout=(w/'frontend.log').open('wb'),stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
(w/'state.json').write_text(json.dumps({'schema':schema,'pids':[p.pid,f.pid],'frontend':'http://127.0.0.1:5175'}))
print('Persistent local debugging database ready:',schema,'frontend http://127.0.0.1:5175')
