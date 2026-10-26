"""Repackage the verified performance release, excluding paused bug-fix changes."""
from pathlib import Path
import hashlib
import json
import shutil
import subprocess
import tarfile

ROOT = Path(__file__).resolve().parents[2]
RELEASES = (ROOT.parent / 'releases').resolve()
SOURCE = RELEASES / 'shuanggao-update-20261025'
OUT = RELEASES / 'shuanggao-update-20261026'
ARCHIVE = OUT.with_suffix('.tar.gz')
SOURCE_ARCHIVE_HASH = 'b0f28779afcea83416f7e8b82f585326e97f004638a6f47206db1edea0f49c12'


def sha(data):
    return hashlib.sha256(data).hexdigest()


def checksums(folder):
    return {name: digest for digest, name in (
        line.split('  ', 1) for line in (folder / 'SHA256SUMS').read_text(encoding='ascii').splitlines())}


def main():
    assert OUT.parent == RELEASES and not OUT.exists() and not ARCHIVE.exists(), 'Never overwrite an existing release'
    assert sha(SOURCE.with_suffix('.tar.gz').read_bytes()) == SOURCE_ARCHIVE_HASH
    originals = checksums(SOURCE)
    for name, digest in originals.items():
        assert '..' not in Path(name).parts and sha((SOURCE / name).read_bytes()) == digest, name
    prior = json.loads((SOURCE / 'verification.json').read_text(encoding='utf-8'))
    assert len(prior['checks']) == 25
    shutil.copytree(SOURCE, OUT)
    readme = (OUT / 'README.md').read_text(encoding='utf-8').replace('20261025', '20261026')
    readme = readme.replace('## 本次主要内容', '''## 本包范围

本包交付此前已验证的性能优化版本。后端、前端、数据库脚本和更新脚本与
20261025包逐文件一致；仅调整包名、说明和校验清单。
本次发现的预算在线保存、成果重新提交及审核记录等问题仍在排查修复中，未混入本包。
若服务器已部署20261025包，无需为本次重新封装重复更新。

## 本次主要内容''')
    (OUT / 'README.md').write_text(readme, encoding='utf-8', newline='\n')
    manifest = json.loads((OUT / 'manifest.json').read_text(encoding='utf-8'))
    manifest.update(release_name=OUT.name, source_release=SOURCE.name,
                    package_revision='Previously verified performance baseline; pending bug fixes excluded',
                    runtime_byte_identical_to=SOURCE.name, pending_bug_fixes_included=False)
    (OUT / 'manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    immutable = [name for name in originals if name not in {'README.md', 'manifest.json', 'verification.json'}]
    for name in immutable:
        assert (SOURCE / name).read_bytes() == (OUT / name).read_bytes(), name
    verification = prior | {'repackage_verification': {
        'source_release': SOURCE.name, 'source_archive_sha256': SOURCE_ARCHIVE_HASH,
        'runtime_sql_update_scripts_byte_identical': True, 'unchanged_files_verified': len(immutable),
        'pending_bug_fixes_included': False, 'production_deployed': False,
        'validation_note': 'Source deployment checks are inherited only because runtime and deployment files are byte-identical. No new runtime build or migration.'}}
    (OUT / 'verification.json').write_text(json.dumps(verification, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    files = sorted(p for p in OUT.rglob('*') if p.is_file() and p.name != 'SHA256SUMS')
    (OUT / 'SHA256SUMS').write_text(''.join(sha(p.read_bytes()) + '  ' + p.relative_to(OUT).as_posix() + '\n' for p in files), encoding='ascii', newline='\n')
    for name, digest in checksums(OUT).items():
        assert sha((OUT / name).read_bytes()) == digest
    bash = next(p for p in [r'D:\Program Files\Git\bin\bash.exe', r'C:\Program Files\Git\bin\bash.exe'] if Path(p).is_file())
    for name in ['update.sh', 'schema-check.sh', 'database-restore.sh']:
        assert b'\r' not in (OUT / name).read_bytes()
        subprocess.run([bash, '-n', (OUT / name).as_posix()], check=True)
    def mode(info):
        info.uid = info.gid = 0
        info.uname = info.gname = 'root'
        info.mode = 0o700 if info.isdir() or info.name.endswith('.sh') else 0o600
        return info
    with tarfile.open(ARCHIVE, 'w:gz') as archive:
        archive.add(OUT, arcname=OUT.name, filter=mode)
    with tarfile.open(ARCHIVE, 'r:gz') as archive:
        for member in archive.getmembers():
            assert not member.issym() and not member.islnk()
            relative = Path(member.name).relative_to(OUT.name)
            assert '..' not in relative.parts
            if member.isfile():
                assert archive.extractfile(member).read() == (OUT / relative).read_bytes(), relative
    digest = sha(ARCHIVE.read_bytes())
    ARCHIVE.with_suffix('.tar.gz.sha256').write_text(digest + '  ' + ARCHIVE.name + '\n', encoding='ascii', newline='\n')
    record = {'release': OUT.name, 'archive': str(ARCHIVE), 'bytes': ARCHIVE.stat().st_size, 'sha256': digest,
              'fileChecksumsVerified': len(files), 'archiveMatchesDirectory': True,
              'runtimeUnchangedFrom': SOURCE.name, 'pendingBugFixesIncluded': False,
              'backendCommit': manifest['backend_commit'], 'frontendCommit': manifest['frontend_commit'],
              'inheritedDeploymentChecks': len(prior['checks']), 'productionDeployed': False}
    (ROOT / 'docs/release-20261026.json').write_text(json.dumps(record, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(record, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
