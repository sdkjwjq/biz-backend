"""Paired final acceptance. Owns only guarded servers, synthetic schemas and evidence."""
import os,sys,time,json,subprocess,shutil,uuid,hashlib
from pathlib import Path
root=Path(__file__).resolve().parents[2];env=dict(os.environ,SHUANGGAO_PERF_METRICS='1');baseline=root/'target/performance-close-baseline-ea7d4ab8';candidate=Path((root/'target/performance-close-candidate-path.txt').read_text().strip())
package_log=root/'target/performance-close-package-last.log'
for _ in range(600):
 if package_log.exists() and 'BUILD SUCCESS' in package_log.read_text(errors='replace'):break
 time.sleep(2)
else:raise RuntimeError('Final regressions/package not ready')
for origin,destination in [(root/'src',candidate/'backend/src'),(root/'target/classes',candidate/'backend/target/classes'),(root/'target/test-classes',candidate/'backend/target/test-classes'),(root.parent/'biz/src',candidate/'frontend/src'),(root.parent/'biz/dist',candidate/'frontend/dist')]:shutil.copytree(origin,destination,dirs_exist_ok=True)
manifest=[{'path':str(p.relative_to(candidate)).replace('\\','/'),'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for folder in ['backend/src/main','frontend/src','frontend/dist'] for p in (candidate/folder).rglob('*') if p.is_file()]
(candidate/'manifest-final.json').write_text(json.dumps(manifest,indent=2),encoding='utf8')
folders=[Path(os.environ['CLOSE_PREVIOUS'])] if os.environ.get('CLOSE_PREVIOUS') else []
def run(command,log,extra=None):
 with log.open('w',encoding='utf8') as output:subprocess.run(command,cwd=root,env=dict(env,**(extra or {})),stdout=output,stderr=subprocess.STDOUT,check=True)
for scale in json.loads(os.environ.get('CLOSE_SCALES','[1,5]')):
 run_id=f'close-accept-{scale}-'+uuid.uuid4().hex[:6];folder=root/('target/ui-audit-'+run_id);folders.append(folder);server_log=root/('target/'+run_id+'.log');log_handle=server_log.open('w',encoding='utf8')
 command=[sys.executable,'scripts/ui-audit/serve.py','--fixture','permissions','--production','--run-id',run_id,'--synthetic-scale',str(scale),'--frontend-port','15381','--backend-port','18381','--source-root',str(baseline/'backend'),'--frontend-root',str(baseline/'frontend'),'--comparison-backend-root',str(candidate/'backend'),'--comparison-frontend-root',str(candidate/'frontend')]
 server=subprocess.Popen(command,cwd=root,env=env,stdout=log_handle,stderr=subprocess.STDOUT,creationflags=getattr(subprocess,'CREATE_NO_WINDOW',0))
 try:
  for _ in range(600):
   if 'UI_AUDIT_READY' in server_log.read_text(errors='replace'):break
   if server.poll() is not None:raise RuntimeError('Server failed '+str(server_log))
   time.sleep(.3)
  else:raise RuntimeError('Server startup timed out')
  env['UI_AUDIT_STATE_FILE']=str(folder/'state.json');print(f'ACCEPTANCE_READY scale={scale} folder={folder}',flush=True)
  run([sys.executable,'scripts/ui-audit/verify-close-equality.py'],folder/'compatibility.log')
  run(['node','scripts/ui-audit/virtual-tree.cjs'],folder/'virtual-tree.log')
  run(['node','scripts/ui-audit/management-candidates.cjs'],folder/'management-candidates.log')
  classpath='target/ui-audit-tools;target/classes;'+(root/'target/ui-audit-classpath.txt').read_text().strip()
  run(['java','-cp',classpath,'org.example.CloseQueryPlans'],folder/'query-plans.log')
  run([sys.executable,'scripts/ui-audit/benchmark-close.py'],folder/'optimized-probe.log',{'PERF_BUILD':'optimized','PERF_PROFILE':'admin','PERF_WORKLOADS':'accountRoles,overviewRecords,overviewRecordAnalysis','PERF_SUFFIX':'-probe','PERF_ENFORCE':'1'})
  print(f'PROBES_PASSED scale={scale}',flush=True)
  for label in ['optimized','baseline']:
   for profile in ['admin','ordinary','leaderAll','leaderDepartment','expert','finance']:
    run([sys.executable,'scripts/ui-audit/benchmark-close.py'],folder/f'capacity-{label}-{profile}.log',{'PERF_BUILD':label,'PERF_PROFILE':profile,'PERF_ENFORCE':'0'})
    print(f'CAPACITY_DONE scale={scale} build={label} profile={profile}',flush=True)
  run([sys.executable,'scripts/ui-audit/benchmark-close-candidates.py'],folder/'candidate-capacity.log')
  for label in ['optimized','baseline']:
   run(['node','scripts/ui-audit/benchmark-close-pages.cjs'],folder/f'pages-{label}.log',{'PERF_BUILD':label})
   print(f'PAGES_DONE scale={scale} build={label}',flush=True)
 finally:
  if folder.exists():(folder/'STOP').write_text('stop')
  server.wait(timeout=120);log_handle.close()
(root/'target/performance-close-acceptance-folders.json').write_text(json.dumps([str(p) for p in folders],indent=2))
run([sys.executable,'scripts/ui-audit/summarize-close.py',*[str(p) for p in folders]],root/'target/performance-close-capacity-validation.log')
print('ACCEPTANCE_COMPLETE',flush=True)
