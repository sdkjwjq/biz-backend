"""Create the cumulative 20261027 release without rebuilding unchanged backend artifacts."""
from pathlib import Path
import hashlib
import json
import re
import shutil
import subprocess
import tarfile
import zipfile

ROOT = Path(__file__).resolve().parents[2]
PROJECT = ROOT.parent
NAME = 'shuanggao-update-20261027'
BASE = PROJECT / 'releases/shuanggao-update-20261026-r3'
OUT = PROJECT / 'releases' / NAME
ARCHIVE = Path(str(OUT) + '.tar.gz')
WORK = PROJECT / '.run/usability-fixes-20261027'
EVIDENCE = ROOT / 'target/ui-audit-usability-fixes-20261027'


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')


def git(*args, repo=ROOT):
    return subprocess.check_output(['git', *args], cwd=repo, text=True).strip()


def main():
    assert OUT.resolve().parent == (PROJECT / 'releases').resolve()
    assert not OUT.exists() and not ARCHIVE.exists(), 'Never overwrite a release'
    assert sha(Path(str(BASE) + '.tar.gz')) == 'cf4f051ffe7381691165081bacd233d22ee5db708e433ed51e6298f19e8b57e6'
    originals = {name: digest for digest, name in (line.split('  ', 1) for line in (BASE / 'SHA256SUMS').read_text().splitlines())}
    for name, digest in originals.items():
        assert not Path(name).is_absolute() and '..' not in Path(name).parts
        assert sha(BASE / name) == digest, name
    manifest = json.loads((BASE / 'manifest.json').read_text(encoding='utf-8'))
    assert not git('diff', manifest['backend_commit'], '--', 'src/main'), 'Backend runtime must match the tested baseline'
    original_proof = json.loads((BASE / 'verification.json').read_text(encoding='utf-8'))
    assert len(original_proof['packaged_jar_and_deployment_checks']['checks']) == 27
    ux = json.loads((EVIDENCE / 'usability-evidence/results.json').read_text(encoding='utf-8'))
    assert len(ux['checks']) >= 18 and not ux['errors']
    for measurement in ux['measurements']:
        before, after = measurement['before']['scroll'], measurement['after']['scroll']
        assert before['rowKey'] and before['rowKey'] == after['rowKey']
        assert abs(before['offset'] - after['offset']) < 2
    assert len(ux['measurements']) == 8
    regression = [json.loads((EVIDENCE / 'browser-repair-evidence' / (mode + '-results.json')).read_text(encoding='utf-8'))
                  for mode in ['budget','performance','achievement','audit','finance','admin-tabs','logout','admin-scroll']]
    assert sum(len(row['checks']) for row in regression) == 30 and all(not row['errors'] for row in regression)
    assert b'tests 12' in (WORK / 'unit.log').read_bytes() and b'fail 0' in (WORK / 'unit.log').read_bytes()
    assert not (WORK / 'lint.log').read_text(encoding='utf-8').strip()
    source_state = json.loads((WORK / 'source.json').read_text())
    for name, digest in source_state['protected'].items():
        assert sha(PROJECT / 'biz' / name) == digest, name
    OUT.mkdir()
    for item in BASE.iterdir():
        if item.name == 'frontend':
            continue
        if item.is_dir():
            shutil.copytree(item, OUT / item.name)
        else:
            shutil.copy2(item, OUT / item.name)
    shutil.copytree(WORK / 'frontend/dist', OUT / 'frontend')
    for source, destination in [('fix-recheck-followups-20261026.md','FIXES-FOLLOWUPS-20261026.md'),
                                ('fix-usability-20261027.md','FIXES-USABILITY-20261027.md')]:
        shutil.copy2(ROOT / 'docs' / source, OUT / destination)
    readme = (BASE / 'README.md').read_text(encoding='utf-8').replace(BASE.name, NAME)
    readme = readme.replace('双高平台更新包 20261026-r3', '双高平台更新包 20261027')
    start, end = readme.index('## 本包范围'), readme.index('## 数据保护')
    readme = readme[:start] + '''## 本包范围

这是累计完整更新包，包含此前尚未实装的原七项修复、退出登录草稿保护、数据维护页滚动，以及本次四项体验修复。无需先安装20261026-r3。

后端JAR、SQL及备份回滚脚本与已验证的20261026-r3逐文件相同；本次没有新增数据库结构或数据迁移。保留服务器现有业务数据，更新前仍自动备份。

## 本次主要内容

- 任务、绩效加载遮罩固定在表格区域，不再遮住侧栏和顶部导航；取消整页串行淡出淡入。
- 后台自动刷新保留列表、阅读位置、展开和勾选状态，不再阻塞或挤动内容。
- 初次加载和主动查询不误报“条件已修改”；未提交的筛选仍会提示。
- 预算未确认或读取失败时显示加载、失败状态，不再展示示例金额；成功后只展示对应年月的实际数据。
- 包含此前预算保存和版本、绩效年度和审核历史、驳回成果重提、待办数量及无关联消息修复。
- 包含退出前草稿处理、管理数据维护页小窗口滚动修复。
- 普通用户Word手册：`manual/user-guide-20261026.docx`。
- 本次验证和历史基线的适用范围分别记录在 `verification.json`；未重新宣称容量优化全部达标。

''' + readme[end:]
    (OUT / 'README.md').write_text(readme, encoding='utf-8', newline='\n')
    manifest.update(release_name=NAME, release=NAME, source_release=BASE.name,
                    frontend_commit=git('rev-parse','HEAD',repo=PROJECT/'biz'),
                    backend_repository_commit=git('rev-parse','HEAD'),
                    backend_byte_identical_to=BASE.name, new_database_migrations=False,
                    included_followup_fixes=['NEW-01','NEW-02'], included_usability_fixes=['UX-01','UX-02','UX-03','UX-04'],
                    package_revision='Cumulative unreleased fixes and verified usability improvements',
                    frontend_working_changes=git('diff','--name-only',repo=PROJECT/'biz').splitlines())
    manifest['artifact_sha256']['frontend_index'] = sha(OUT / 'frontend/index.html')
    manifest['frontend_module_entries'] = re.findall(r'<script[^>]+type="module"[^>]+src="([^"]+)"',(OUT/'frontend/index.html').read_text(encoding='utf-8'))
    assert manifest['frontend_module_entries']
    assert manifest['artifact_sha256']['frontend_index'] != json.loads((BASE/'manifest.json').read_text())['artifact_sha256']['frontend_index']
    write_json(OUT / 'manifest.json', manifest)
    immutable = ['backend/biz_backend-1.0-SNAPSHOT.jar','update.sh','schema-check.sh','database-restore.sh','templates/budget-template.xlsx']
    immutable += [p.relative_to(BASE).as_posix() for p in (BASE/'sql').rglob('*') if p.is_file()]
    for name in immutable:
        assert (OUT/name).read_bytes() == (BASE/name).read_bytes(), name
    with zipfile.ZipFile(OUT/'backend/biz_backend-1.0-SNAPSHOT.jar') as jar:
        properties=jar.read('BOOT-INF/classes/application.properties')
        assert b'spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}' in properties
    proof={'unitTests':12,'browserWorkflowChecks':30,'usabilityChecks':len(ux['checks']),
           'usability':ux,'browserWorkflowResults':regression,'frontendLint':'passed','frontendProductionBuild':'passed',
           'unchangedBackendAndDeploymentFiles':immutable,
           'inheritedReleaseChecks':{'sourceRelease':BASE.name,'appliesOnlyToUnchangedBackendSqlAndDeploymentFiles':True,
                                     'checks':original_proof['packaged_jar_and_deployment_checks']},
           'newDatabaseMigrations':False,'productionDatabaseAccessed':False,'productionDeployed':False,
           'linuxProcessTest':'not run on Windows'}
    write_json(OUT/'verification.json',proof)
    files=sorted(p for p in OUT.rglob('*') if p.is_file() and p.name!='SHA256SUMS')
    (OUT/'SHA256SUMS').write_text(''.join(sha(p)+'  '+p.relative_to(OUT).as_posix()+'\n' for p in files),encoding='ascii',newline='\n')
    bash=next(p for p in [r'D:\Program Files\Git\bin\bash.exe',r'C:\Program Files\Git\bin\bash.exe'] if Path(p).is_file())
    for name in ['update.sh','schema-check.sh','database-restore.sh']:
        assert b'\r' not in (OUT/name).read_bytes()
        subprocess.run([bash,'-n',str(OUT/name)],check=True)
    native='/'+OUT.as_posix()[0].lower()+OUT.as_posix()[2:]
    subprocess.run([bash,'-c','cd "$1" && sha256sum -c SHA256SUMS --quiet','verify',native],check=True)
    def mode(info):
        info.uid=info.gid=0;info.uname=info.gname='root'
        info.mode=0o700 if info.isdir() or info.name.endswith('.sh') else 0o600
        return info
    with tarfile.open(ARCHIVE,'w:gz') as archive:archive.add(OUT,arcname=NAME,filter=mode)
    with tarfile.open(ARCHIVE,'r:gz') as archive:
        for item in archive:
            assert not item.issym() and not item.islnk()
            rel=Path(item.name).relative_to(NAME);assert '..' not in rel.parts
            if item.isfile():assert archive.extractfile(item).read()==(OUT/rel).read_bytes(),rel
    digest=sha(ARCHIVE)
    Path(str(ARCHIVE)+'.sha256').write_text(digest+'  '+ARCHIVE.name+'\n',encoding='ascii',newline='\n')
    record={'release':NAME,'archive':str(ARCHIVE),'sha256':digest,'bytes':ARCHIVE.stat().st_size,
            'fileChecksumsVerified':len(files),'archiveMatchesDirectory':True,'backendUnchangedFrom':BASE.name,
            'unitTests':12,'browserWorkflowChecks':30,'usabilityChecks':len(ux['checks']),
            'inheritedBackendDeploymentChecks':27,'productionDeployed':False}
    write_json(ROOT/'docs/release-20261027.json',record)
    print(json.dumps(record,ensure_ascii=False,indent=2))


if __name__=='__main__':main()
