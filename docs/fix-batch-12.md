# 第十二批修复：12-1、12-2

## 范围与提交

- `3ee6586`：Performance.vue 内关联任务详情按打开序号和任务 ID 校验；切换、关闭、卸载使旧请求失效并清空旧详情。子任务、审核单和日志均检查上下文，审批前核对任务归属，固定单号、结果和意见。
- `229e42b`：Audit.vue 保存提交时的弹窗序号、单号、业务类型、结果和意见。旧响应不再关闭新弹窗，提示使用原提交结果，提交期间防重复调用。保留该文件此前未提交的历史筛选改动，没有混入提交。
- 12-3 按用户要求忽略。客户规则为允许覆盖，每次提交后仅保留当前和上一次提交的材料；本次不修改此逻辑。

## 本地闭环

Playwright + Edge，随机隔离 MySQL 与真实本地接口，全部使用合成数据。

1. `related-task --fixed`：延迟 A 审核请求，返回绩效详情后打开 B，放行 A；实际提交 B 单号 981002，B 从 10 到 20，A 保持 10。
2. `related-task --fixed --lifecycle`：已流转 B 不受 A 旧响应影响，不出现 A 的审批按钮；关闭重开 A 后可正常审批。
3. 同场景增加 `--fail-old`：旧请求返回合成 503，B 不被清空或改写；重新进入 A 正常。
4. `approval-dialog --fixed`：A 提交通过后切换至 B，选择驳回并填写草稿；A 响应返回时 B 弹窗、选择和草稿保留，提示“审批通过”。真实接口确认 A=20、B=10。
5. `approval-normal --fixed`：保持 B 弹窗直接提交通过，正确关闭弹窗并提示通过，接口确认 B=20。

以上五个场景通过，无浏览器脚本异常。[结构化证据](fix-evidence/batch-12/)。原始审计证据保留。

补测正常驳回时，直接 SQL 构造的绩效单缺少 `biz_performance_audit_snapshot`，后端保护拒绝恢复（PerformanceService.restorePerformanceSnapshot）。该单仍为待审，随后正常通过测试成功；驳回端到端不计入通过场景，不修改后端保护来迁就种子。本次只调整前端请求上下文，未变更业务审批规则。

Performance.vue、Audit.vue ESLint、前端生产构建、脚本语法及差异空白检查通过。Performance.vue 原文件混用 CRLF/LF，保持原有行尾，空白检查使用 `core.whitespace=cr-at-eol`。构建保留原有大分包提示。没有后端业务修改，因此未重复执行后端打包或 23 项回归。

## 复测顺序

全新 `serve.py --fixture review` 环境，按既有方式设置数据库密码环境变量：

```powershell
python scripts/ui-audit/seed-batch-8.py --batch 10
python scripts/ui-audit/seed-batch-8.py --batch 12
node scripts/ui-audit/batch-12-audit.cjs related-task --fixed
node scripts/ui-audit/batch-12-audit.cjs related-task --fixed --lifecycle
node scripts/ui-audit/batch-12-audit.cjs related-task --fixed --lifecycle --fail-old
node scripts/ui-audit/batch-12-audit.cjs approval-dialog --fixed
node scripts/ui-audit/batch-12-audit.cjs approval-normal --fixed
New-Item -ItemType File -Path target/ui-audit/STOP -Force
```

测试完成后临时服务和随机数据库已清理，原有服务及未提交工作保留。

## 部署与回滚

产物为 `biz/dist`，尚未部署。仅前端发布，无接口或数据库变更，无迁移 SQL。

回滚在前端仓库执行 `git revert 229e42b`、`git revert 3ee6586`，重新构建发布，或恢复前一版前端产物。不使用硬重置，不影响原有未提交工作。
