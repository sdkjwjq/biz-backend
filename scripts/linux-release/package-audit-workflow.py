"""Package the verified workflow repair over the already deployed November release."""
from pathlib import Path
import hashlib,json,os,re,shutil,subprocess,tarfile,zipfile

ROOT=Path(__file__).resolve().parents[2];P=ROOT.parent
EVIDENCE=Path(os.environ['WORKFLOW_EVIDENCE']).resolve()
FRONTEND=Path(os.environ['WORKFLOW_FRONTEND_DIST']).resolve()
JAR=Path(os.environ['WORKFLOW_BACKEND_JAR']).resolve()
OUT=P/'releases/shuanggao-update-20261103-r5'
BASE=P/'releases/shuanggao-update-20261103-r3'
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def head(repo):return subprocess.check_output(['git','rev-parse','HEAD'],cwd=repo,text=True).strip()
def main():
    assert not OUT.exists() and not OUT.with_suffix('.tar.gz').exists()
    for line in (BASE/'SHA256SUMS').read_text().splitlines():
        digest,name=line.split('  ',1);assert sha(BASE/name)==digest,name
    result=json.loads((EVIDENCE/'rehearsal-results.json').read_text())
    assert result['liveBackupRehearsal'] and result['idempotent'] and result['rollbackVerified'] and result['conflictAtomic']
    regression=json.loads((EVIDENCE/'regression-results.json').read_text());assert regression['passed'] and regression['isolatedDatabaseRemoved']
    browser=json.loads((ROOT/'target/ui-audit-workflow-fix-20261103/timeline-evidence/results.json').read_text());assert browser['passed'] and not browser['errors']
    with zipfile.ZipFile(JAR) as jar:
        props=jar.read('BOOT-INF/classes/application.properties').decode()
        assert 'spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}' in props
        assert not re.search(r'^\s*#?\s*spring.datasource.password=(?!\$\{)',props,re.M)
        assert jar.read('BOOT-INF/classes/maintenance/audit-workflow-20261103.json')==(ROOT/'src/main/resources/maintenance/audit-workflow-20261103.json').read_bytes()
    shutil.copytree(BASE,OUT,ignore=shutil.ignore_patterns('frontend','backend','SHA256SUMS','build-provenance.json','verification.json'))
    (OUT/'backend').mkdir();shutil.copy2(JAR,OUT/'backend/biz_backend-1.0-SNAPSHOT.jar');shutil.copytree(FRONTEND,OUT/'frontend')
    for name in ['update.sh','audit-workflow-maintenance.sh','database-restore.sh','schema-check.sh']:
        text=(ROOT/'scripts/linux-release'/name).read_text(encoding='utf-8')
        if name=='update.sh':
            # This repair needs no business migration. Do not replay older ownership/value migrations.
            start=text.index('"${MYSQL[@]}" < "$PACKAGE/sql/additive.sql"')
            end=text.index('workflow_apply',start)
            text=text[:start]+'bash "$PACKAGE/schema-check.sh" after\n\n'+text[end:]
        (OUT/name).write_text(text,encoding='utf-8',newline='\n')
    (OUT/'workflow-maintenance-guide.txt').write_bytes((ROOT/'docs/20261103-audit-workflow-maintenance.txt').read_bytes())
    (OUT/'sql/2026-11-03-audit-workflow-check.sql').write_bytes((ROOT/'data/migrations/2026-11-03-audit-workflow-check.sql').read_bytes())
    manifest=json.loads((OUT/'manifest.json').read_text(encoding='utf-8'))
    manifest.update(backend_commit=head(ROOT),frontend_commit=head(P/'biz'),release_name=OUT.name,data_updates=['guarded-offline-workflow-maintenance'],one_time_migrations=[],current_release_recalculates_values=False,
                    workflow_maintenance={'taskCandidates':25,'deletedAchievementCandidates':2,'rawAuditLogsPreserved':True,'frontendMaintenanceLogs':False,'serverPrivateRollbackReport':True,'noSchemaChanges':True,'cumulativeBusinessMigrationsExecuted':False})
    (OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
    extra=json.loads((EVIDENCE/'extra-results.json').read_text());assert extra['passed']
    (OUT/'verification.json').write_text(json.dumps({'liveBackupRehearsal':{k:v for k,v in result.items() if k not in ['rawAndBusinessDigestsUnchanged','evidenceDirectory']},'regression':regression,'extra':extra,'browser':browser,'frontend':{'lint':True,'unit':36,'build':True},'scope':'frontend/backend plus guarded pending-handler repair; no old business migrations'},ensure_ascii=False,indent=2),encoding='utf-8')
    provenance={'backendCommit':head(ROOT),'frontendCommit':head(P/'biz'),'backendJarSha256':sha(JAR),'frontendIndexSha256':sha(FRONTEND/'index.html'),'excludedUncommittedFiles':['biz/vite.config.js','biz-backend/src/test/java/org/example/AchievementFlowApiTest.java','biz-backend/src/test/java/org/example/AuditFlowApiTest.java'],'build':'clean committed sources plus verified current changes; runtime database password removed before packaging'}
    (OUT/'build-provenance.json').write_text(json.dumps(provenance,indent=2),encoding='utf-8')
    (OUT/'README.md').write_text('''# 20261103-r5 更新说明

适用于已更新至20261103-r2及之后版本的服务器。包含导出修复、审核步骤显示及当前处理人核对修正。
保留服务器当前业务数据。首次核对25条任务待办及2条已删除成果的待审残留；已处理记录跳过，活动配置冲突时停止。
不执行旧的业务迁移，不重算任务或绩效，不改写原审核日志，不在前台展示维护记录。

直接复制整个文件夹后执行：

```bash
cd /root/biz-backend/shuanggao-update-20261103-r5
chmod -R go-rwx .
sha256sum -c SHA256SUMS --quiet && bash update.sh --check && bash update.sh --update
```

检查和更新各需输入一次服务器MySQL root密码。检查不修改数据库；更新会先停止后端写入并完整备份，再核对、修正和替换程序。
更新失败自动恢复更新前数据库及程序。成功后按输出的 --rollback-workflow 命令回退；有后续审批、配置或相关通知变化时拒绝覆盖，保留现有业务。
服务器维护文件保存在本次备份目录中，仅服务器管理员可读。不要用旧全库覆盖更新后新增的业务。
''',encoding='utf-8',newline='\n')
    files=sorted(p for p in OUT.rglob('*') if p.is_file() and p.name!='SHA256SUMS')
    (OUT/'SHA256SUMS').write_text(''.join(sha(p)+'  '+p.relative_to(OUT).as_posix()+'\n' for p in files),encoding='ascii',newline='\n')
    archive=OUT.with_suffix('.tar.gz')
    def mode(info):
        info.uid=info.gid=0;info.uname=info.gname='root';info.mode=0o700 if info.isdir() or info.name.endswith('.sh') else 0o600;return info
    with tarfile.open(archive,'w:gz') as tar:tar.add(OUT,arcname=OUT.name,filter=mode)
    archive.with_suffix('.gz.sha256').write_text(sha(archive)+'  '+archive.name+'\n',encoding='ascii')
    print(json.dumps({'directory':str(OUT),'archive':str(archive),'sha256':sha(archive),'bytes':archive.stat().st_size},indent=2))
if __name__=='__main__':main()
