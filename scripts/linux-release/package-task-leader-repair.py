"""Prepare a database-only repair folder; never rebuild or replace application binaries."""
from pathlib import Path
import hashlib
import json
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[2]
NAME = 'shuanggao-task-owner-fix-20261029'
OUT = ROOT.parent / 'releases' / NAME


def main():
    assert OUT.resolve().parent == (ROOT.parent / 'releases').resolve()
    OUT.mkdir(exist_ok=True)
    (OUT / 'sql').mkdir(exist_ok=True)
    files = {'repair.sh': Path(__file__).with_name('repair-task-leader.sh')}
    for suffix in ['-candidates', '', '-check', '-rollback']:
        name = '2026-10-29-task-leader-repair' + suffix + '.sql'
        files['sql/' + name] = ROOT / 'data/migrations' / name
    for name, source in files.items():
        (OUT / name).write_text(source.read_text(encoding='utf-8'), encoding='utf-8', newline='\n')
    (OUT / 'README.txt').write_text('''历史任务负责人定向修复（2026-10-29）

适用问题
任务负责人应为 leader_id 对应的专业群负责人。历史数据把部分 leader_id 改为其他人员，影响显示、填报资格及督办对象。本修复直接修正数据，不通过前端替换姓名掩盖问题。

使用方法
将本文件夹整体复制到 /root/biz-backend/，然后以 root 执行：
  cd /root/biz-backend/shuanggao-task-owner-fix-20261029
  chmod -R go-rwx .
  sha256sum -c SHA256SUMS --quiet && bash repair.sh --check
  bash repair.sh --apply

输入服务器现有 MySQL root 密码（不回显）。--check 不修改业务数据。
--apply 会短暂停止现有后端，完整备份数据库，执行定向修复，再按原启动参数恢复同一个后端。前端、JAR、配置、上传材料均不替换。操作完成后刷新任务页面。
运行环境与现有部署一致：Linux、Bash 4.2+、MySQL 8、目标服务 /root/biz-backend/biz_backend-1.0-SNAPSHOT.jar，8080端口。不要与其他更新、迁移或手工改库同时执行。

自动修复范围
1. 固定清单只包含线上20260920备份中226条已确认的历史负责人变更，不扫描后任意重配其他任务。
2. ID、编号、年度、项目、父目录和原错误负责人均须精确吻合；当前一级任务负责人也须与历史原负责人一致。
3. 已有后续负责人移交记录、已删除或移动任务、原负责人已失效、配置发生变化的记录自动跳过。
4. 专业群审核人 auditor_id、归口审核人 principal_id、归口部门、四级目标、任务完成值、绩效、原提交人、在途处理人、审核历史和通知均不修改。
5. 后续仅修改过审核人的任务仍可修正负责人，同时保留新的审核人。
6. 本次不修复37条绩效历史负责人差异；本次授权针对任务，绩效数据保持原样。

核对状态
READY：满足全部保护条件，可以自动修正。
ALREADY_CORRECT / ALREADY_REPAIRED：已正确或此前已修复，本次不重复处理。
OWNER_TRANSFER_RECORDED：有正式负责人移交，保留配置。
OWNER_CHANGED / IDENTITY_CHANGED / ROOT_OR_SCOPE_CHANGED：当前配置已变化，保留并待人工核实。
OWNER_UNAVAILABLE / MISSING_OR_DELETED：人员失效或任务无效，不自动修改。
REPAIR_PREVIOUSLY_ROLLED_BACK / POST_REPAIR_CHANGED：存在本修复历史，不再次覆盖。
线上本次实际处理数以 operation.tsv 为准，不能把历史226条直接当作当前待修复数。

备份、留痕与回滚
备份目录：/root/biz-backend/backups/task-leader-20261029-年月日-时分秒-进程号/
包含 database.sql.gz 全库备份、before.tsv、after.tsv、operation.tsv 和原启动参数。
数据库中 bak_20261029_task_leader 保存首次修复前的完整任务行，sys_task_leader_repair_20261029 记录前后负责人、批次、数据库执行身份与时间；不伪造平台管理员操作身份。
必要时执行：bash repair.sh --rollback
回滚只恢复本次负责人，不恢复整库；任一任务修复后又发生修改时整批拒绝，避免覆盖新数据。备份和修复历史永久保留。回滚后不会自动重新应用。

验证
已在随机隔离库使用线上备份演练，验证定向修复、字段与其他表不变、重复执行、跳过保护、回滚和注入失败的事务一致性。隔离库已清理，本地调试库未修改。
真实接口已核验 leaderId、auditorId、principalId 分别返回、非负责人无法代替填报及两级审核流转。
Windows 环境完成 Bash 语法检查；实际 Linux 停启流程沿用原部署方式，未连接线上执行。服务器执行完成才代表线上生效。
''', encoding='utf-8', newline='\n')
    proof = json.loads((ROOT.parent / '.run/owner-field-audit-20261029/repair-results.json').read_text(encoding='utf-8'))
    regression_log = ROOT.parent / '.run/owner-field-audit-20261029/role-routing-repair-test.log'
    assert 'BUILD SUCCESS' in regression_log.read_text(encoding='utf-8', errors='replace')
    proof['roleRoutingHttpRegression'] = 'passed: owner IDs and name, non-owner rejection, independent reviewers'
    proof['linuxProcessLifecycleTest'] = 'not executed on Windows; shell syntax checked'
    proof['applicationBinariesReplaced'] = False
    (OUT / 'verification.json').write_text(json.dumps(proof, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    bash = next((str(p) for p in [Path('D:/Program Files/Git/bin/bash.exe'), Path('C:/Program Files/Git/bin/bash.exe')] if p.is_file()), None) or shutil.which('bash')
    assert bash, 'Bash is required for syntax verification'
    subprocess.run([bash, '-n', str(OUT / 'repair.sh')], check=True)
    files = sorted(p for p in OUT.rglob('*') if p.is_file() and p.name != 'SHA256SUMS')
    for p in files:
        assert b'\r' not in p.read_bytes(), p
    sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
    (OUT / 'SHA256SUMS').write_text(''.join(sha(p) + '  ' + p.relative_to(OUT).as_posix() + '\n' for p in files), encoding='ascii', newline='\n')
    for line in (OUT / 'SHA256SUMS').read_text().splitlines():
        digest, name = line.split('  ', 1)
        assert sha(OUT / name) == digest
    print(str(OUT))


if __name__ == '__main__':
    main()
