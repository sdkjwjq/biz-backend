"""Package the already tested frontend and exercise deployment in disposable folders."""
from pathlib import Path
import hashlib
import json
import os
import shutil
import subprocess
import tarfile
import tempfile

ROOT = Path(__file__).resolve().parents[2]
PROJECT = ROOT.parent
NAME = 'shuanggao-frontend-fix-20261027'
BASE = PROJECT / 'releases/shuanggao-update-20261027'
DIST = PROJECT / '.run/achievement-title-20261027/frontend/dist'
WORK = PROJECT / '.run/frontend-patch-20261027'
DEST = PROJECT / 'releases' / NAME
BASH = next(str(p) for p in [Path(r'C:\Program Files\Git\bin\bash.exe'), Path(r'D:\Program Files\Git\bin\bash.exe')] if p.is_file())


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')


def checksums(package):
    paths = sorted(p for p in package.rglob('*') if p.is_file() and p.name != 'SHA256SUMS')
    (package/'SHA256SUMS').write_text(''.join(sha(p)+'  '+p.relative_to(package).as_posix()+'\n' for p in paths), encoding='ascii', newline='\n')


def shell_path(path):
    p = path.resolve().as_posix()
    return '/' + p[0].lower() + p[2:]


def deployment_tests(package, run):
    web, backups = run/'html', run/'backups'
    shutil.copytree(BASE/'frontend', web)
    (web/'uploads').mkdir(exist_ok=True)
    (web/'uploads/keep.txt').write_text('Synthetic unrelated server file', encoding='utf-8')
    old = {p.relative_to(web):p.read_bytes() for p in web.rglob('*') if p.is_file()}
    checks, logs = [], []
    env = dict(os.environ, FRONTEND_DIR=shell_path(web), FRONTEND_BACKUP_ROOT=shell_path(backups),
               UI_PATCH_MV_LOG=shell_path(run/'moves.log'), UI_PATCH_FAIL_MARKER=shell_path(run/'failed-once'))
    # Git Bash lacks Linux root and flock semantics. Mock only those prerequisites;
    # checksum, backup, copy, atomic rename, validation and restore commands are real.
    wrapper = '''id(){ if [[ "${1-}" == -u ]]; then echo 0; else command id "$@"; fi; }
flock(){ return 0; }
mv(){
  local dest="${@: -1}"
  printf '%s\\n' "$dest" >> "$UI_PATCH_MV_LOG"
  if [[ "${UI_PATCH_FAIL:-0}" == 1 && "$dest" == */assets/*.js && ! -f "$UI_PATCH_FAIL_MARKER" ]]; then
    touch "$UI_PATCH_FAIL_MARKER"; return 19
  fi
  command mv "$@"
}
export -f id flock mv
bash "$@"
'''

    def call(*args, ok=True, extra=None):
        result = subprocess.run([BASH,'-c',wrapper,'test',shell_path(package/'update.sh'),*args], env=dict(env, **(extra or {})), capture_output=True)
        logs.append(result.stdout.decode('utf-8',errors='replace')+result.stderr.decode('utf-8',errors='replace'))
        assert (result.returncode == 0) == ok, logs[-1]
        return result

    def original():
        for relative, contents in old.items():
            assert (web/relative).read_bytes() == contents, str(relative)

    call('--check');original();assert not backups.exists()
    checks.append('Read-only check preserves every existing file and creates no backup')
    (web/'index.html').write_bytes(b'<html>Unexpected version</html>')
    call('--update',ok=False);assert (web/'index.html').read_bytes() == b'<html>Unexpected version</html>'
    (web/'index.html').write_bytes(old[Path('index.html')])
    checks.append('Wrong installed baseline is rejected without replacing files')
    saved = (package/'frontend/index.html').read_bytes()
    try:
        (package/'frontend/index.html').write_bytes(saved+b'corrupt')
        call('--update',ok=False);original()
    finally:
        (package/'frontend/index.html').write_bytes(saved)
    checks.append('Corrupt patch is rejected before changing the installed frontend')
    call('--update')
    for p in (package/'frontend').rglob('*'):
        if p.is_file():assert (web/p.relative_to(package/'frontend')).read_bytes() == p.read_bytes()
    checks.append('Successful update installs all files from the tested production build')
    for relative, contents in old.items():
        if relative.as_posix().startswith('assets/') or relative == Path('uploads/keep.txt'):
            assert (web/relative).read_bytes() == contents
    checks.append('Old hashed assets and unrelated server files remain available')
    moves = (run/'moves.log').read_text().splitlines()
    assert moves[-1] == shell_path(web/'index.html') and all(not p.endswith('/index.html') for p in moves[:-1])
    checks.append('Entry HTML is atomically published only after all static files')
    backup = next(backups.glob('frontend-achievement-title-*'))
    with tarfile.open(backup/'frontend.tar.gz') as archive:
        assert archive.extractfile('./index.html').read() == old[Path('index.html')]
        assert archive.extractfile('./uploads/keep.txt').read() == old[Path('uploads/keep.txt')]
    checks.append('Backup contains the exact previous entry and unrelated files')
    call('--update');assert len(list(backups.glob('frontend-achievement-title-*'))) == 1
    checks.append('Repeated update verifies the installed files and does not make another backup')
    call('--rollback',shell_path(backup));original()
    checks.append('Explicit rollback restores all original files')
    call('--rollback',shell_path(run),ok=False);original()
    checks.append('Unrelated rollback path is rejected')
    alternate = run/'other-html';shutil.copytree(BASE/'frontend',alternate)
    call('--rollback',shell_path(backup),ok=False,extra={'FRONTEND_DIR':shell_path(alternate)})
    assert (alternate/'index.html').read_bytes() == old[Path('index.html')]
    checks.append('Backup cannot be restored into a different website')
    saved = (backup/'web.path').read_bytes()
    try:
        (backup/'web.path').write_bytes(saved+b'corrupt')
        call('--rollback',shell_path(backup),ok=False);original()
    finally:
        (backup/'web.path').write_bytes(saved)
    checks.append('Corrupt backup is rejected without changing files')
    call('--update',ok=False,extra={'UI_PATCH_FAIL':'1'});original()
    checks.append('A publication failure automatically restores the previous frontend')
    (run/'deployment.log').write_text('\n'.join(logs),encoding='utf-8')
    return checks


def main():
    archive_path = Path(str(DEST)+'.tar.gz')
    assert not DEST.exists() and not archive_path.exists(), 'Never overwrite a delivered release'
    assert sha(Path(str(BASE)+'.tar.gz')) == '771199a4d288c033e0bcd6344663cee68ff30b56da8b91a6a1f468d383560f13'
    evidence = json.loads((ROOT/'target/ui-audit-achievement-title-20261027/title-evidence/after-results.json').read_text(encoding='utf-8'))
    assert len(evidence['checks']) == 9 and not evidence['errors']
    source = json.loads((PROJECT/'.run/achievement-title-20261027/source.json').read_text())
    for name,digest in source['protected'].items():assert sha(PROJECT/name) == digest
    assert not (PROJECT/'.run/achievement-title-20261027/lint.log').read_text().strip()
    assert 'built in' in (PROJECT/'.run/achievement-title-20261027/build.log').read_text(encoding='utf-8')
    WORK.mkdir(exist_ok=True)
    run = Path(tempfile.mkdtemp(prefix='attempt-',dir=WORK));package = run/NAME;package.mkdir()
    shutil.copytree(DIST, package/'frontend')
    assert not any(p.is_symlink() for p in package.rglob('*'))
    script = (ROOT/'scripts/linux-release/frontend-patch.sh').read_text(encoding='utf-8')
    script = script.replace('__BASE_INDEX_SHA__',sha(BASE/'frontend/index.html')).replace('__PATCH_INDEX_SHA__',sha(DIST/'index.html'))
    (package/'update.sh').write_text(script,encoding='utf-8',newline='\n')
    files = sorted(p for p in (package/'frontend').rglob('*') if p.is_file())
    (package/'FRONTEND_SHA256SUMS').write_text(''.join(sha(p)+'  '+p.relative_to(package/'frontend').as_posix()+'\n' for p in files),encoding='ascii',newline='\n')
    readme = f'''# 成果名称排版前端补丁（20261027）

适用于已经安装 `shuanggao-update-20261027` 的服务器。本包仅替换前端静态文件，修复成果类别的箭头、名称和数量挤换行。长名称悬停滚动、驳回原因和展开收起保持原有功能。

不含后端JAR、SQL或数据库备份，不询问MySQL密码，不停止后端或重载nginx。旧哈希资源和网页目录中的其他文件保留。

将整个 `{NAME}` 文件夹复制到 `/root/biz-backend/`，执行：

```bash
cd /root/biz-backend/{NAME} &&
chmod -R go-rwx . &&
sha256sum -c SHA256SUMS --quiet &&
bash update.sh --check &&
bash update.sh --update
```

需root、Bash4及标准系统命令（含flock）。默认前端目录为 `/usr/share/nginx/html`。先核对已安装入口版本，备份当前完整前端，再复制资源并最后原子替换入口；任一发布步骤失败会恢复原前端。重复执行会校验已安装文件，避免重复生成备份。

成功后按Ctrl+F5刷新浏览器。备份位于 `/root/biz-backend/backups/frontend-achievement-title-时间-PID`；脚本会输出包含实际目录的完整回滚命令：

```bash
bash /root/biz-backend/{NAME}/update.sh --rollback /root/biz-backend/backups/frontend-achievement-title-实际时间-PID
```

只读检查失败时请保留提示，不要跳过校验或手工改校验值。原完整更新包保持不变。

验证：使用已通过9项浏览器检查、静态检查和正式构建的原始前端产物，不重新编译；部署和回滚脚本在隔离文件夹中验证。Windows环境仅模拟root和flock前置条件，未宣称验证Linux进程或文件锁的原生行为。
'''
    (package/'README.md').write_text(readme,encoding='utf-8',newline='\n')
    commit = subprocess.check_output(['git','rev-parse','HEAD'],cwd=PROJECT/'biz',text=True).strip()
    manifest = {'release':NAME,'kind':'frontend-only','requires_release':BASE.name,'frontend_commit':commit,
                'baseline_index_sha256':sha(BASE/'frontend/index.html'),'frontend_index_sha256':sha(DIST/'index.html'),
                'frontend_matches_browser_tested_build':True,'backend_included':False,'database_migrations':False}
    write_json(package/'manifest.json',manifest);checksums(package)
    subprocess.run([BASH,'-n',str(package/'update.sh')],check=True)
    deployment = deployment_tests(package,run)
    proof = {'browserChecks':evidence,'deploymentChecks':deployment,'frontendLint':'passed','frontendProductionBuild':'passed',
             'testedFrontendByteIdentical':True,'rootAndFlockMockedInWindowsTests':True,'productionDeployed':False}
    write_json(package/'verification.json',proof);checksums(package)
    for p in DIST.rglob('*'):
        if p.is_file():assert p.read_bytes() == (package/'frontend'/p.relative_to(DIST)).read_bytes()
    subprocess.run([BASH,'-c','cd "$1" && sha256sum -c SHA256SUMS --quiet','verify',shell_path(package)],check=True)
    shutil.copytree(package,DEST)
    def permissions(info):
        info.uid=info.gid=0;info.uname=info.gname='root';info.mode=0o700 if info.isdir() or info.name.endswith('.sh') else 0o600
        return info
    with tarfile.open(archive_path,'w:gz') as archive:archive.add(DEST,arcname=NAME,filter=permissions)
    with tarfile.open(archive_path,'r:gz') as archive:
        for info in archive:
            relative=Path(info.name).relative_to(NAME);assert '..' not in relative.parts and not info.issym() and not info.islnk()
            if info.isfile():assert archive.extractfile(info).read() == (DEST/relative).read_bytes()
    digest=sha(archive_path)
    Path(str(archive_path)+'.sha256').write_text(digest+'  '+archive_path.name+'\n',encoding='ascii',newline='\n')
    record={'release':NAME,'archive':str(archive_path),'sha256':digest,'bytes':archive_path.stat().st_size,
            'frontendFiles':len(files),'browserChecks':len(evidence['checks']),'deploymentChecks':len(deployment),
            'archiveMatchesDirectory':True,'productionDeployed':False,'testDirectory':str(run)}
    write_json(ROOT/'docs/release-frontend-title-20261027.json',record)
    print(json.dumps(record,ensure_ascii=False,indent=2))


if __name__=='__main__':main()
