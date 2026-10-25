"""Run existing browser regressions in their own guarded, purpose-specific schemas."""
import subprocess,os,time,json,sys
from pathlib import Path
root=Path(__file__).resolve().parents[2];candidate=Path((root/'target/performance-close-candidate-path.txt').read_text());env=dict(os.environ,UI_AUDIT_BROWSER_CHANNEL='chrome');results=[]
groups={'permissions':['list-filters','notice-list-loading','sidebar-refresh','expert-list-loading','supervision-loading'],'overview':['achievement-list-loading','overview-dashboard'],'continuous':['audit-consolidated']}
if len(sys.argv)>1:
 groups={'overview':['achievement-list-loading']}
 report=root/'target/performance-close-browser-regressions.json'
 results=json.loads(report.read_text()) if report.exists() else []
for fixture,scripts in groups.items():
 run_id='close-ui-'+fixture+('-retry' if len(sys.argv)>1 else '');folder=root/('target/ui-audit-'+run_id);log=root/('target/'+run_id+'.log');log_file=log.open('w',encoding='utf8')
 process=subprocess.Popen([sys.executable,str(root/'scripts/ui-audit/serve.py'),'--fixture',fixture,'--production','--run-id',run_id,'--frontend-port','15383','--backend-port','18383','--source-root',str(candidate/'backend'),'--frontend-root',str(candidate/'frontend')],cwd=root,env=env,stdout=log_file,stderr=subprocess.STDOUT,creationflags=getattr(subprocess,'CREATE_NO_WINDOW',0))
 try:
  for i in range(500):
   if 'UI_AUDIT_READY' in log.read_text(encoding='utf8',errors='replace'):break
   if process.poll() is not None:raise RuntimeError('Server failed '+str(log))
   time.sleep(.2)
  else:raise RuntimeError('Server timeout')
  test_env=dict(env,UI_AUDIT_STATE_FILE=str(folder/'state.json'))
  for script in scripts:
   output=folder/(script+'.log')
   with output.open('w',encoding='utf8') as handle:result=subprocess.run(['node',str(root/('scripts/ui-audit/'+script+'.cjs'))],cwd=root,env=test_env,stdout=handle,stderr=subprocess.STDOUT)
   results=[r for r in results if r['script']!=script]
   results.append({'fixture':fixture,'script':script,'exitCode':result.returncode,'log':str(output.relative_to(root))});print(json.dumps(results[-1]),flush=True)
 finally:
  if folder.exists():(folder/'STOP').write_text('stop')
  process.wait(timeout=60);log_file.close()
(root/'target/performance-close-browser-regressions.json').write_text(json.dumps(results,indent=2))
assert all(r['exitCode']==0 for r in results),results
