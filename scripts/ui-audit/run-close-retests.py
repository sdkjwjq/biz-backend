"""Recheck changed query paths; retain unchanged paired capacity evidence verbatim."""
import os,sys,time,json,subprocess,uuid
from pathlib import Path
root=Path(__file__).resolve().parents[2];env=dict(os.environ,SHUANGGAO_PERF_METRICS='1');baseline=root/'target/performance-close-baseline-ea7d4ab8';candidate=Path((root/'target/performance-close-candidate-path.txt').read_text().strip());folders=[]
def run(command,log,extra=None):
 with log.open('w',encoding='utf8') as output:subprocess.run(command,cwd=root,env=dict(env,**(extra or {})),stdout=output,stderr=subprocess.STDOUT,check=True)
for scale in [1,5]:
 run_id=f'close-recheck-{scale}-'+uuid.uuid4().hex[:6];folder=root/('target/ui-audit-'+run_id);folders.append(folder);server_log=root/('target/'+run_id+'.log');handle=server_log.open('w',encoding='utf8')
 server=subprocess.Popen([sys.executable,'scripts/ui-audit/serve.py','--fixture','permissions','--production','--run-id',run_id,'--synthetic-scale',str(scale),'--frontend-port','15381','--backend-port','18381','--source-root',str(baseline/'backend'),'--frontend-root',str(baseline/'frontend'),'--comparison-backend-root',str(candidate/'backend'),'--comparison-frontend-root',str(candidate/'frontend')],cwd=root,env=env,stdout=handle,stderr=subprocess.STDOUT,creationflags=getattr(subprocess,'CREATE_NO_WINDOW',0))
 try:
  for _ in range(600):
   if 'UI_AUDIT_READY' in server_log.read_text(errors='replace'):break
   if server.poll() is not None:raise RuntimeError('Server failed')
   time.sleep(.3)
  else:raise RuntimeError('Server timeout')
  env['UI_AUDIT_STATE_FILE']=str(folder/'state.json');print(f'RECHECK_READY scale={scale} folder={folder}',flush=True)
  run([sys.executable,'scripts/ui-audit/verify-close-equality.py'],folder/'compatibility.log')
  print(f'RECHECK_COMPATIBLE scale={scale}',flush=True)
  if scale==1:
   for _ in range(600):
    progress=(root/'target/performance-close-browser-final2.log').read_text(errors='replace')
    if 'audit-consolidated' in progress:break
    time.sleep(1)
   else:raise RuntimeError('Browser regression not finished')
   time.sleep(3)
  profiles={'admin':'auditTodo,auditHistory,taskOptions,performanceOptions,auditPeople,tasks,performance,supervision','ordinary':'tasks,performance,taskOptions','leaderAll':'tasks,performance','leaderDepartment':'tasks,performance','expert':'tasks,taskOptions'}
  for build in ['optimized','baseline']:
   for profile,workloads in profiles.items():
    run([sys.executable,'scripts/ui-audit/benchmark-close.py'],folder/f'capacity-{build}-{profile}.log',{'PERF_BUILD':build,'PERF_PROFILE':profile,'PERF_WORKLOADS':workloads,'PERF_ENFORCE':'1'})
    print(f'RECHECK_CAPACITY scale={scale} build={build} profile={profile}',flush=True)
  run(['node','scripts/ui-audit/benchmark-close-pages.cjs'],folder/'pages-optimized.log',{'PERF_BUILD':'optimized'})
  print(f'RECHECK_PAGES scale={scale}',flush=True)
 finally:
  (folder/'STOP').write_text('stop');server.wait(timeout=120);handle.close()
(root/'target/performance-close-retest-folders.json').write_text(json.dumps([str(p) for p in folders],indent=2))
print('RECHECK_COMPLETE',flush=True)
