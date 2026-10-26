"""Freeze, assemble and verify the 2026-10-26 browser repair release."""
from pathlib import Path
import hashlib
import io
import json
import os
import re
import shutil
import subprocess
import sys
import tarfile
import zipfile

ROOT = Path(__file__).resolve().parents[2]
PROJECT = ROOT.parent
NAME = 'shuanggao-update-20261026-r3'
BASE = PROJECT / 'releases/shuanggao-update-20261026-r2'
OUT = PROJECT / 'releases' / NAME
ARCHIVE = Path(str(OUT) + '.tar.gz')
BUILD = ROOT / 'target/linux-release-build-20261026-r3'
FRONT = PROJECT / '.run/release-20261026-r3/frontend'
REPAIRS = PROJECT / '.run/repair-resume-20261026'
UI_EVIDENCE = ROOT / 'target/ui-audit-repair-resume-20261026/browser-repair-evidence'
JAR = 'backend/biz_backend-1.0-SNAPSHOT.jar'
BASE_HASH = '1854a22a97c1e8b5465d94fb3d89dac254881e508915b2e26a4d72b057e9b170'


def sha(data):
    return hashlib.sha256(data).hexdigest()


def git(*args, repo=ROOT):
    return subprocess.check_output(['git', *args], cwd=repo)


def manifest_hashes(folder):
    return {name: digest for digest, name in (line.split('  ', 1) for line in
            (folder / 'SHA256SUMS').read_text(encoding='ascii').splitlines())}


def verify_baseline():
    assert sha(Path(str(BASE) + '.tar.gz').read_bytes()) == BASE_HASH
    for name, digest in manifest_hashes(BASE).items():
        assert '..' not in Path(name).parts and not Path(name).is_absolute()
        assert sha((BASE / name).read_bytes()) == digest, name


def protect_changes():
    expected = json.loads((ROOT / 'target/browser-bugfix-20261026/protected.json').read_text())
    for name, digest in expected.items():
        assert sha(Path(name).read_bytes()) == digest, name
    # Runtime backend sources are committed; unrelated tests/manual scripts remain untouched.
    assert not git('diff', '--name-only', '--', 'src/main').strip()


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')


def prepare():
    verify_baseline()
    protect_changes()
    assert BUILD.resolve().parent == (ROOT / 'target').resolve()
    assert not BUILD.exists() and not OUT.exists() and not ARCHIVE.exists(), 'Never overwrite a release'
    BUILD.mkdir()
    source_archive = git('archive', 'HEAD', 'pom.xml', 'src/main')
    with zipfile.ZipFile(BASE / JAR) as archive:
        properties = archive.read('BOOT-INF/classes/application.properties')
    assert b'spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}' in properties
    with tarfile.open(fileobj=io.BytesIO(source_archive)) as archive:
        for member in archive:
            relative = Path(member.name)
            assert not relative.is_absolute() and '..' not in relative.parts
            assert not member.issym() and not member.islnk()
            if not member.isfile():
                continue
            target = BUILD / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(properties if member.name == 'src/main/resources/application.properties'
                               else archive.extractfile(member).read())
    FRONT.mkdir(parents=True)
    state = {'release': NAME, 'backend_commit': git('rev-parse', 'HEAD').decode().strip(),
             'frontend_commit': git('rev-parse', 'HEAD', repo=PROJECT / 'biz').decode().strip(),
             'backend_source_archive_sha256': sha(source_archive),
             'production_properties_sha256': sha(properties),
             'frontend_working_changes': git('diff', '--name-only', repo=PROJECT / 'biz').decode().splitlines()}
    write_json(BUILD / 'source-state.json', state)
    print('Frozen committed backend sources; production configuration externally supplies credentials')


def checksums():
    files = sorted(p for p in OUT.rglob('*') if p.is_file() and p.name != 'SHA256SUMS')
    (OUT / 'SHA256SUMS').write_text(''.join(sha(p.read_bytes()) + '  ' + p.relative_to(OUT).as_posix() + '\n'
                                         for p in files), encoding='ascii', newline='\n')
    return files


def assemble():
    verify_baseline()
    protect_changes()
    assert not OUT.exists() and not ARCHIVE.exists()
    state = json.loads((BUILD / 'source-state.json').read_text())
    assert state['backend_commit'] == git('rev-parse', 'HEAD').decode().strip()
    assert state['frontend_commit'] == git('rev-parse', 'HEAD', repo=PROJECT / 'biz').decode().strip()
    proof = json.loads((REPAIRS / 'verification.json').read_text(encoding='utf-8'))
    assert proof['backendTests'] == 16 and proof['browserChecks'] == 18
    assert proof['isolatedSchemaRemoved'] and proof['protectedFilesUnchanged']
    reports = [json.loads((UI_EVIDENCE / (mode + '-results.json')).read_text(encoding='utf-8'))
               for mode in ['budget', 'performance', 'achievement', 'audit', 'finance', 'admin-tabs']]
    assert sum(len(r['checks']) for r in reports) == 18 and all(not r['errors'] for r in reports)
    assert b'[INFO] Tests run: 16, Failures: 0, Errors: 0' in (REPAIRS / 'browserrepairs.log').read_bytes()
    artifact = BUILD / 'target/biz_backend-1.0-SNAPSHOT.jar'
    with zipfile.ZipFile(artifact) as new, zipfile.ZipFile(BASE / JAR) as old:
        properties = new.read('BOOT-INF/classes/application.properties')
        assert sha(properties) == state['production_properties_sha256']
        assert properties == old.read('BOOT-INF/classes/application.properties')
        assert 'BOOT-INF/classes/org/example/entity/dto/BudgetSaveRequest.class' in new.namelist()
        assert new.read('BOOT-INF/classes/org/example/service/BudgetService.class') != old.read('BOOT-INF/classes/org/example/service/BudgetService.class')
        assert len(new.read('BOOT-INF/classes/templates/work-record.docx')) > 1000
    tested = REPAIRS / 'frontend/dist'
    fresh = FRONT / 'dist'
    assert fresh.is_dir()
    # A fresh build must reproduce the actual browser-tested production artifacts.
    for item in fresh.rglob('*'):
        if item.is_file():
            assert item.read_bytes() == (tested / item.relative_to(fresh)).read_bytes(), item.name
    OUT.mkdir()
    for item in BASE.iterdir():
        if item.name == 'frontend':
            continue
        if item.is_dir():
            shutil.copytree(item, OUT / item.name)
        else:
            shutil.copy2(item, OUT / item.name)
    shutil.copytree(fresh, OUT / 'frontend')
    shutil.copy2(artifact, OUT / JAR)
    manual = PROJECT / 'docs/双高平台使用说明20261026.docx'
    with zipfile.ZipFile(manual) as document:
        assert document.testzip() is None and 'word/document.xml' in document.namelist()
    shutil.copy2(manual, OUT / 'manual/user-guide-20261026.docx')
    shutil.copy2(ROOT / 'docs/fix-browser-workflows-20261026.md', OUT / 'FIXES-20261026.md')
    readme = (BASE / 'README.md').read_text(encoding='utf-8').replace(BASE.name, NAME).replace('20261026-r2', '20261026-r3')
    start, end = readme.index('## 本包范围'), readme.index('## 本次主要内容')
    readme = readme[:start] + '''## 本包范围

包含已完成的浏览器巡检问题修复，并保留此前的性能优化、权限及专家评审功能。
本轮修复不新增表、字段或业务数据迁移；累计迁移脚本与20261026-r2包逐文件相同，
已执行的一次性迁移仍会跳过。更新前自动建立新的完整备份，保留旧备份。

''' + readme[end:]
    start, end = readme.index('## 本次主要内容'), readme.index('## 数据保护')
    readme = readme[:start] + '''## 本次主要内容

- 预算网页修改可以保存并形成在线编辑版本；保存、锁定、刷新及导出保持一致。
- 预算历史每次使用主表当前年月；未保存内容在查询、离开及页签切换前得到保护。
- 绩效切换年度及打开年度链接不再串值，后台刷新保留正在填写的内容。
- 绩效审核人可以在审批记录中查询本人已处理的驳回记录。
- 驳回成果可在修改保存后明确重新提交，保留原填报人、部门和全部旧审核记录。
- 保留管理员待办数量与列表一致性修复、无关联消息空值保护。
- 附最新版普通用户 Word 手册：`manual/user-guide-20261026.docx`。
- `FIXES-20261026.md` 记录本轮修复范围；旧图文手册仍可离线查阅。
- 16项隔离后端回归、18项生产构建浏览器检查通过；容量优化暂缓项保持原安排。

''' + readme[end:]
    (OUT / 'README.md').write_text(readme, encoding='utf-8', newline='\n')
    manifest = json.loads((BASE / 'manifest.json').read_text(encoding='utf-8'))
    for key in ['runtime_byte_identical_to', 'changed_runtime_sources', 'pending_bug_fixes_included',
                'other_pending_bug_fixes_included', 'backend_source_snapshot_sha256']:
        manifest.pop(key, None)
    manifest.update(state)
    manifest.update(release_name=NAME, source_release=BASE.name,
                    package_revision='Verified browser workflow repairs; cumulative deployment scripts unchanged',
                    browser_bug_fixes_included=['BUG-01', 'BUG-02', 'BUG-03', 'BUG-04', 'BUG-05', 'BUG-06', 'BUG-07'],
                    new_database_migrations=False, word_operation_manual='manual/user-guide-20261026.docx',
                    frontend_matches_browser_tested_build=True)
    manifest['artifact_sha256'] = {'backend': sha(artifact.read_bytes()),
                                   'frontend_index': sha((fresh / 'index.html').read_bytes()),
                                   'word_manual': sha(manual.read_bytes())}
    entries = re.findall(r'<script[^>]+type="module"[^>]+src="([^"]+)"', (fresh / 'index.html').read_text(encoding='utf-8'))
    assert entries and entries != json.loads((BASE / 'manifest.json').read_text())['frontend_module_entries']
    manifest['frontend_module_entries'] = entries
    write_json(OUT / 'manifest.json', manifest)
    unchanged = [p.relative_to(BASE).as_posix() for p in (BASE / 'sql').rglob('*') if p.is_file()]
    unchanged += ['update.sh', 'schema-check.sh', 'database-restore.sh', 'templates/budget-template.xlsx']
    for name in unchanged:
        assert (BASE / name).read_bytes() == (OUT / name).read_bytes(), name
    verification = {'checks': ['source-frozen-with-external-production-configuration',
                               'backend-browser-repair-api-regressions-passed',
                               'frontend-fresh-build-byte-matches-tested-build',
                               'production-browser-workflows-passed', 'baseline-deployment-files-unchanged',
                               'word-manual-and-template-integrity'],
                    'backend_tests': 16, 'browser_checks': 18, 'browser_results': reports,
                    'frontend_lint': 'passed', 'frontend_build': 'passed',
                    'baseline_release': BASE.name, 'unchanged_deployment_files_verified': unchanged,
                    'new_database_migrations': False, 'production_database_accessed': False,
                    'production_deployed': False, 'linux_process_test': 'not run on Windows'}
    write_json(OUT / 'verification.json', verification)
    checksums()
    print('Assembled fresh backend, tested frontend, Word manual and unchanged deployment scripts')


def finalize():
    protect_changes()
    assert not ARCHIVE.exists()
    deployment = json.loads((BUILD / 'verification.json').read_text())
    assert len(deployment['checks']) >= 25 and not deployment['production_database_accessed']
    verification = json.loads((OUT / 'verification.json').read_text())
    verification['packaged_jar_and_deployment_checks'] = deployment
    verification['checks'].append('packaged-jar-migrations-checksums-and-rollback-tests-passed')
    write_json(OUT / 'verification.json', verification)
    files = checksums()
    bash = next(p for p in [r'D:\Program Files\Git\bin\bash.exe', r'C:\Program Files\Git\bin\bash.exe'] if Path(p).is_file())
    for item in [OUT / name for name in ['update.sh', 'schema-check.sh', 'database-restore.sh', 'SHA256SUMS']]:
        assert b'\r' not in item.read_bytes()
    for name in ['update.sh', 'schema-check.sh', 'database-restore.sh']:
        subprocess.run([bash, '-n', str(OUT / name)], check=True)
    native_path = '/' + OUT.as_posix()[0].lower() + OUT.as_posix()[2:]
    subprocess.run([bash, '-c', 'cd "$1" && sha256sum -c SHA256SUMS --quiet', 'verify', native_path], check=True)
    def mode(info):
        info.uid = info.gid = 0
        info.uname = info.gname = 'root'
        info.mode = 0o700 if info.isdir() or info.name.endswith('.sh') else 0o600
        return info
    with tarfile.open(ARCHIVE, 'w:gz') as archive:
        archive.add(OUT, arcname=OUT.name, filter=mode)
    with tarfile.open(ARCHIVE, 'r:gz') as archive:
        for member in archive:
            assert not member.issym() and not member.islnk()
            relative = Path(member.name).relative_to(OUT.name)
            assert '..' not in relative.parts
            if member.isfile():
                assert archive.extractfile(member).read() == (OUT / relative).read_bytes(), relative
    digest = sha(ARCHIVE.read_bytes())
    Path(str(ARCHIVE) + '.sha256').write_text(digest + '  ' + ARCHIVE.name + '\n', encoding='ascii', newline='\n')
    record = {'release': NAME, 'archive': str(ARCHIVE), 'bytes': ARCHIVE.stat().st_size,
              'sha256': digest, 'fileChecksumsVerified': len(files), 'archiveMatchesDirectory': True,
              'backendTests': 16, 'browserChecks': 18, 'packagedDeploymentChecks': len(deployment['checks']),
              'productionDeployed': False}
    write_json(ROOT / 'docs/release-20261026-r3.json', record)
    print(json.dumps(record, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    {'prepare': prepare, 'assemble': assemble, 'finalize': finalize}[sys.argv[1]]()
