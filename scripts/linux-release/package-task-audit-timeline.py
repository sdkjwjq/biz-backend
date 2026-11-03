"""Ship only the verified task-timeline frontend; never run a database migration."""
from pathlib import Path
import hashlib,json,os,shutil,subprocess,tarfile,tempfile

ROOT=Path(__file__).resolve().parents[2];P=ROOT.parent
W=P/'.run/task-audit-release-20261103'
NAME='shuanggao-update-20261103-r4'
BASE=P/'releases/shuanggao-update-20261103-r3'
DIST=W/'frontend/dist';DEST=P/'releases'/NAME

def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def write_json(p,v):p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8',newline='\n')
def linux(p):
 p=p.resolve().as_posix();return '/mnt/'+p[0].lower()+p[2:]
def sums(p):
 files=sorted(f for f in p.rglob('*') if f.is_file() and f.name!='SHA256SUMS')
 (p/'SHA256SUMS').write_text(''.join(sha(f)+'  '+f.relative_to(p).as_posix()+'\n' for f in files),encoding='ascii',newline='\n')

def deployment_tests(package,run):
 web=run/'html';backup=run/'backups';shutil.copytree(BASE/'frontend',web)
 (web/'uploads').mkdir(exist_ok=True);(web/'uploads/keep.txt').write_text('Unrelated server file',encoding='utf-8')
 old={p.relative_to(web):p.read_bytes() for p in web.rglob('*') if p.is_file()}
 checks=[];logs=[]
 wrapper=run/'wrapper.sh'
 wrapper.write_text('''#!/usr/bin/env bash
mv(){
  local dest="${@: -1}"
  printf '%s\\n' "$dest" >> "$AUDIT_PATCH_MOVE_LOG"
  if [[ "${AUDIT_PATCH_FAIL:-0}" == 1 && "$dest" == */assets/*.js && ! -f "$AUDIT_PATCH_FAILED" ]]; then
    touch "$AUDIT_PATCH_FAILED"; return 19
  fi
  command mv "$@"
}
export -f mv
bash "$@"
''',encoding='utf-8',newline='\n')
 def call(*args,ok=True,extra=()):
  cmd=['wsl.exe','-d','Ubuntu','-u','root','--','env','FRONTEND_DIR='+linux(web),'FRONTEND_BACKUP_ROOT='+linux(backup),'AUDIT_PATCH_MOVE_LOG='+linux(run/'moves.log'),'AUDIT_PATCH_FAILED='+linux(run/'failed'),*extra,'bash',linux(wrapper),linux(package/'update.sh'),*args]
  r=subprocess.run(cmd,capture_output=True,timeout=90)
  text=(r.stdout+r.stderr).decode('utf-8',errors='replace');logs.append(text)
  assert (r.returncode==0)==ok,text
  return text
 def original():
  for relative,contents in old.items():assert (web/relative).read_bytes()==contents,str(relative)
 call('--check');original();assert not backup.exists();checks.append('Read-only preflight changes no installed files')
 (web/'index.html').write_bytes(b'<html>Unexpected version</html>')
 call('--update',ok=False);assert not backup.exists();(web/'index.html').write_bytes(old[Path('index.html')]);checks.append('Unsupported installed frontend is rejected')
 saved=(package/'frontend/index.html').read_bytes()
 try:
  (package/'frontend/index.html').write_bytes(saved+b'corrupt');call('--update',ok=False);original()
 finally:(package/'frontend/index.html').write_bytes(saved)
 checks.append('Corrupt package is rejected before publishing')
 call('--update')
 for f in (package/'frontend').rglob('*'):
  if f.is_file():assert (web/f.relative_to(package/'frontend')).read_bytes()==f.read_bytes()
 for relative,contents in old.items():
  if str(relative).startswith('assets') or relative==Path('uploads/keep.txt'):assert (web/relative).read_bytes()==contents
 checks.append('Exact tested frontend installed; old hashed assets and unrelated files preserved')
 moves=(run/'moves.log').read_text().splitlines();assert moves[-1]==linux(web/'index.html') and all(not x.endswith('/index.html') for x in moves[:-1]);checks.append('Entry atomically replaced after all assets')
 saved_backup=next(backup.glob('frontend-task-audit-*'))
 with tarfile.open(saved_backup/'frontend.tar.gz') as t:
  for relative,contents in old.items():assert t.extractfile('./'+relative.as_posix()).read()==contents
 checks.append('Backup exactly preserves all previous frontend files')
 call('--update');assert len(list(backup.glob('frontend-task-audit-*')))==1;checks.append('Repeat install verifies files without another backup')
 call('--rollback',linux(saved_backup));original();checks.append('Explicit rollback restores original frontend')
 call('--rollback',linux(run),ok=False);original();checks.append('Unrelated rollback path is rejected')
 saved=(saved_backup/'web.path').read_bytes()
 try:
  (saved_backup/'web.path').write_bytes(saved+b'corrupt');call('--rollback',linux(saved_backup),ok=False);original()
 finally:(saved_backup/'web.path').write_bytes(saved)
 checks.append('Corrupt backup is rejected')
 call('--update',ok=False,extra=['AUDIT_PATCH_FAIL=1']);original();checks.append('Simulated publication failure automatically restores original frontend')
 (run/'deployment.log').write_text('\n'.join(logs),encoding='utf-8')
 return checks

def main():
 archive=Path(str(DEST)+'.tar.gz');assert not DEST.exists() and not archive.exists(),'Never overwrite a delivered release'
 assert sha(Path(str(BASE)+'.tar.gz'))=='6b60d785f41402e5aed14e07f4f18a923b00194b9fc1a691489c065e9d7c090d'
 ui=json.loads((ROOT/'target/ui-audit-task-timeline-release-20261103/timeline-evidence/results.json').read_text())
 backend=json.loads((W/'backend-results.json').read_text());assert backend['passed'] and backend['isolatedDatabaseRemoved']
 assert ui['passed'] and len(ui['checks'])==5 and not ui['errors']
 assert not (W/'frontend-lint.log').read_text().strip()
 assert 'pass 32' in (W/'frontend-unit.log').read_text(encoding='utf-8')
 assert 'built in' in (W/'frontend-build.log').read_text(encoding='utf-8')
 for file,h in json.loads((P/'.run/review-task-ui-20261101/protected.json').read_text()).items():assert sha(P/file)==h,file
 for file in ['src/components/Works.vue','src/components/Performance.vue','src/utils/taskAuditTimeline.mjs','tests/taskAuditTimeline.test.mjs']:assert sha(P/'biz'/file)==sha(W/'frontend'/file),file
 run=Path(tempfile.mkdtemp(prefix='deploy-',dir=W));package=run/NAME;package.mkdir()
 shutil.copytree(DIST,package/'frontend');assert not any(f.is_symlink() for f in package.rglob('*'))
 script=(ROOT/'scripts/linux-release/task-audit-patch.sh').read_text(encoding='utf-8').replace('__BASE_INDEX_SHA__',sha(BASE/'frontend/index.html')).replace('__PATCH_INDEX_SHA__',sha(DIST/'index.html'))
 assert not any(x in script for x in ['mysql','mysqldump','systemctl','java '])
 (package/'update.sh').write_text(script,encoding='utf-8',newline='\n')
 files=sorted(f for f in (package/'frontend').rglob('*') if f.is_file())
 (package/'FRONTEND_SHA256SUMS').write_text(''.join(sha(f)+'  '+f.relative_to(package/'frontend').as_posix()+'\n' for f in files),encoding='ascii',newline='\n')
 subprocess.run(['wsl.exe','-d','Ubuntu','-u','root','--','bash','-n',linux(package/'update.sh')],check=True,timeout=30)
 commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=P/'biz',text=True).strip()
 manifest={'release':NAME,'kind':'frontend-only','frontend_commit':commit,'baseline_frontend_commit':json.loads((BASE/'manifest.json').read_text())['frontend_commit'],'baseline_index_sha256':sha(BASE/'frontend/index.html'),'frontend_index_sha256':sha(DIST/'index.html'),'backend_included':False,'database_migrations':False,'business_data_modified':False,'workflow_and_permissions_modified':False,'backup':'complete-frontend','failure_recovery':'automatic-frontend-restore','rollback':'explicit-frontend-only','frontend_files':len(files)}
 write_json(package/'manifest.json',manifest)
 readme=f'''任务审核步骤显示修复（2026年11月3日）

本包为前端补丁，适用于当前20261103前端基线（脚本核对入口SHA256，其他版本会停止）。不需要重新执行此前完整更新包。
不包含JAR、SQL或数据库备份，不连接数据库、不询问数据库密码、不停止后端，不改变填报审核流程、权限、完成值或历史记录。

修复：任务SG01010726本次6月24日提交仍在归口审核，原界面却引用同一单号下5月20日旧归档日志。现在步骤条只使用当前提交周期的证据，并以当前审核状态为准；任务页和绩效关联任务抽屉同步修复。旧日志仍在历史记录中保留，不代表本轮已归档。

将整个 {NAME} 文件夹复制到 /root/biz-backend/，执行：

cd /root/biz-backend/{NAME} &&
chmod -R go-rwx . &&
sha256sum -c SHA256SUMS --quiet &&
bash update.sh --update

脚本先核对文件及已装版本，再备份完整前端。资源就绪后最后原子替换入口，保留旧哈希资源及网站目录中的其他文件。失败自动恢复原前端。重复执行将校验已安装文件，不重复更新。
成功后按Ctrl+F5刷新浏览器；本包不会重启后端或nginx。

备份目录：/root/biz-backend/backups/frontend-task-audit-实际时间-PID
回滚命令会在成功输出中给出：
bash /root/biz-backend/{NAME}/update.sh --rollback /root/biz-backend/backups/frontend-task-audit-实际时间-PID

更新后核对：
1. 打开SG01010726详情，若该单仍为待归口审核，应显示归口审核“进行中”，归档“待流转”，不再把5月20日标为本轮归档。
2. 归口审核通过后为待管理员归档；管理员实际归档后才显示本轮归档时间。
3. 历史操作日志、填报值及原有审核入口保持原样；退回后按原流程重提。

权限和线上状态以后端当前记录为准，不能仅依据旧日志判定已归档。本包没有线上数据修复SQL，也不会删除旧证据或强行改变审批状态。如版本核对失败请保留提示，不要改校验值或绕过检查。
测试与证据见verification.json。当前未直接连接线上服务器，安装后仍需按以上步骤核对实际页面。
'''
 (package/'README.txt').write_text(readme,encoding='utf-8',newline='\n')
 sums(package)
 deployment=deployment_tests(package,run)
 proof={'status':'verified','frontendUnitTests':32,'frontendLint':'passed','productionBuild':'passed','browser':ui,'backendFlowRegression':backend,'deploymentChecks':deployment,'deploymentEnvironment':'WSL Ubuntu Linux as root; real flock, checksum, tar, cp and atomic mv; injected mv failure only','databaseChangesIncluded':False,'backendChangesIncluded':False,'productionDeployed':False}
 write_json(package/'verification.json',proof);sums(package)
 subprocess.run(['wsl.exe','-d','Ubuntu','-u','root','--cd',linux(package),'--','sha256sum','-c','SHA256SUMS','--quiet'],check=True,timeout=30)
 shutil.copytree(package,DEST)
 def perms(t):
  t.uid=t.gid=0;t.uname=t.gname='root';t.mode=0o700 if t.isdir() or t.name.endswith('.sh') else 0o600;return t
 with tarfile.open(archive,'w:gz') as t:t.add(DEST,arcname=NAME,filter=perms)
 with tarfile.open(archive,'r:gz') as t:
  for f in t:
   relative=Path(f.name).relative_to(NAME);assert '..' not in relative.parts and not f.issym() and not f.islnk()
   if f.isfile():assert t.extractfile(f).read()==(DEST/relative).read_bytes()
 digest=sha(archive);Path(str(archive)+'.sha256').write_text(digest+'  '+archive.name+'\n',encoding='ascii',newline='\n')
 result={'release':NAME,'directory':str(DEST),'archive':str(archive),'sha256':digest,'bytes':archive.stat().st_size,'frontendFiles':len(files),'frontendTests':32,'browserChecks':len(ui['checks']),'backendFlowTests':sum(s['tests'] for s in backend['suites']),'deploymentChecks':len(deployment),'databaseChangesIncluded':False,'productionDeployed':False,'deploymentEvidence':str(run)}
 write_json(W/'PACKAGE_RESULTS.json',result);print(json.dumps(result,ensure_ascii=False,indent=2))

if __name__=='__main__':main()
