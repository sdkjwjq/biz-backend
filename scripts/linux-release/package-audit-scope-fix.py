"""Package only the independently tested administrator audit scope fix."""
from pathlib import Path
import hashlib
import json
import shutil
import subprocess
import tarfile
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[2]
STAGE = ROOT / 'target/audit-scope-fix-20261026'
SOURCE = STAGE / 'source'
RELEASES = ROOT.parent / 'releases'
BASE = RELEASES / 'shuanggao-update-20261026'
OUT = RELEASES / 'shuanggao-update-20261026-r2'
JAR = 'backend/biz_backend-1.0-SNAPSHOT.jar'
RUNTIME = ['src/main/java/org/example/config/AdminTestFilter.java',
           'src/main/java/org/example/service/AuditListPageService.java']


def sha(data):
    return hashlib.sha256(data).hexdigest()


def main():
    assert not OUT.exists() and not OUT.with_suffix('.tar.gz').exists()
    assert sha(BASE.with_suffix('.tar.gz').read_bytes()) == '8c2f697c7cff92ddf4867eea70ee29f6e627ed651ad5bf8b36d9b00631c2324f'
    expected = {name: digest for digest, name in (line.split('  ', 1) for line in (BASE / 'SHA256SUMS').read_text().splitlines())}
    for name, digest in expected.items():
        assert sha((BASE / name).read_bytes()) == digest, name
    cases = {}
    for suite in ['AuditScopeConsistencyApiTest', 'AdminTestApiTest']:
        report = ET.parse(SOURCE / ('target/surefire-reports/TEST-org.example.' + suite + '.xml')).getroot()
        assert int(report.get('failures')) == int(report.get('errors')) == int(report.get('skipped')) == 0
        cases[suite] = int(report.get('tests'))
    browser = json.loads((STAGE / 'browser/results.json').read_text())
    assert browser['before'] == {'total': 0, 'todoCount': 34}
    assert browser['after'] == {'total': 34, 'todoCount': 34} and not browser['errors']
    changed = []
    for p in (SOURCE / 'src/main').rglob('*'):
        if not p.is_file():
            continue
        rel = p.relative_to(SOURCE).as_posix()
        if rel == 'src/main/resources/application.properties':
            continue
        baseline = subprocess.check_output(['git', 'show', 'HEAD:' + rel], cwd=ROOT)
        if p.read_bytes().replace(b'\r\n', b'\n') != baseline.replace(b'\r\n', b'\n'):
            changed.append(rel)
    # HEAD already contains the fix after its dedicated commit; staged source must match it exactly.
    assert not changed, changed
    artifact = SOURCE / 'target/biz_backend-1.0-SNAPSHOT.jar'
    with zipfile.ZipFile(artifact) as new, zipfile.ZipFile(BASE / JAR) as old:
        properties = new.read('BOOT-INF/classes/application.properties')
        assert properties == old.read('BOOT-INF/classes/application.properties')
        assert b'spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}' in properties
    shutil.copytree(BASE, OUT)
    shutil.copy2(artifact, OUT / JAR)
    text = (OUT / 'README.md').read_text(encoding='utf-8').replace('20261026', '20261026-r2')
    start = text.index('## 本包范围')
    end = text.index('## 本次主要内容', start)
    text = text[:start] + '''## 本包范围

修复管理员审核中心显示待处理数量但列表缺少记录的问题。管理员的计数、分页列表、
筛选选项和业务定位使用同一当前权限范围，普通账号权限不扩大。
前端、数据库脚本及更新脚本与20261026包完全相同，不包含预算保存、成果重提等暂停中的修改。
不修改任何审核状态或业务数据。先前备份继续保留，本次部署还会建立新的完整备份。

''' + text[end:]
    (OUT / 'README.md').write_text(text, encoding='utf-8', newline='\n')
    manifest = json.loads((OUT / 'manifest.json').read_text(encoding='utf-8'))
    manifest.update(release_name=OUT.name, source_release=BASE.name,
                    backend_commit=subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip(),
                    package_revision='Administrator audit count/list scope fix only',
                    changed_runtime_sources=RUNTIME, other_pending_bug_fixes_included=False)
    manifest.pop('runtime_byte_identical_to', None)
    manifest.pop('pending_bug_fixes_included', None)
    manifest['artifact_sha256']['backend'] = sha((OUT / JAR).read_bytes())
    (OUT / 'manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    unchanged = [name for name in expected if name not in {JAR, 'README.md', 'manifest.json', 'verification.json'}]
    for name in unchanged:
        assert (BASE / name).read_bytes() == (OUT / name).read_bytes(), name
    verification = {'checks': ['baseline-archive-and-all-files-verified', 'only-two-runtime-source-files-changed',
                             'backend-api-regressions-passed', 'paired-production-frontend-playwright-passed',
                             'deployment-configuration-exactly-preserved-with-external-password',
                             'frontend-sql-templates-update-scripts-unchanged'],
                    'http_regressions': cases, 'browser': browser,
                    'baseline_verification': {'release': BASE.name, 'inherited_only_for_unchanged_files': True,
                                              'unchanged_files_verified': len(unchanged)},
                    'production_database_accessed': False, 'production_deployed': False}
    (OUT / 'verification.json').write_text(json.dumps(verification, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    files = sorted(p for p in OUT.rglob('*') if p.is_file() and p.name != 'SHA256SUMS')
    (OUT / 'SHA256SUMS').write_text(''.join(sha(p.read_bytes()) + '  ' + p.relative_to(OUT).as_posix() + '\n' for p in files), encoding='ascii', newline='\n')
    def mode(info):
        info.uid = info.gid = 0
        info.uname = info.gname = 'root'
        info.mode = 0o700 if info.isdir() or info.name.endswith('.sh') else 0o600
        return info
    archive = OUT.with_suffix('.tar.gz')
    with tarfile.open(archive, 'w:gz') as tar:
        tar.add(OUT, arcname=OUT.name, filter=mode)
    with tarfile.open(archive, 'r:gz') as tar:
        for member in tar.getmembers():
            relative = Path(member.name).relative_to(OUT.name)
            assert '..' not in relative.parts and not member.issym() and not member.islnk()
            if member.isfile():
                assert tar.extractfile(member).read() == (OUT / relative).read_bytes(), relative
    digest = sha(archive.read_bytes())
    archive.with_suffix('.tar.gz.sha256').write_text(digest + '  ' + archive.name + '\n', encoding='ascii', newline='\n')
    record = {'release': OUT.name, 'sha256': digest, 'bytes': archive.stat().st_size,
              'fileChecksumsVerified': len(files), 'runtimeSourceChanges': RUNTIME,
              'httpRegressionCases': sum(cases.values()), 'browserChecks': len(browser['checks']),
              'productionDeployed': False}
    (ROOT / 'docs/release-20261026-r2.json').write_text(json.dumps(record, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(record, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
