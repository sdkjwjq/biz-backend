# 第十二批：审批与预览异步上下文

前置：11-1 已修复，前端 `b96483e`，测试文档 `a5ceb99`。用户要求修复后继续检查，并每批集中汇报。本批仅审计，不修改业务代码。

后续状态：用户要求修复 12-1、12-2，忽略 12-3。12-1 已提交 `3ee6586`，12-2 已提交 `229e42b`，见 [闭环记录](fix-batch-12.md)。以下保留原始审计证据。

客户明确的材料规则：允许覆盖；每次提交后只保留当前材料和上一次提交的材料。12-3 按用户决定关闭，不再列为待修项。本次不更改材料存储、覆盖、保留或预览逻辑，也不据此声称已验证材料保留机制。

## 12-1【高 / BUG】绩效内的关联任务仍可审批错对象

- 位置：`biz/src/components/Performance.vue:2486`、`:2520`、`:2805`。
- 实测：从自动绩效的关联任务列表打开 A，延迟 A 审核响应，返回绩效详情并打开 B。放行 A 后，页面任务名仍是 B，点击确认却提交 A 的审核单 981001。真实接口复查 A 状态 10→20，B 保持 10。
- 原因：该组件有独立的任务详情实现，没有任务/request 上下文校验；旧响应覆盖 `taskAuditList`，提交直接使用 `taskLatestAudit`。此前 10-1 修复的是 Works.vue 入口，本次确认的是绩效页内入口。
- 建议：覆盖关联任务的详情、子任务、审核和日志请求，切换/关闭/卸载使旧请求失效，审批前核对单据所属任务并固定提交上下文。

## 12-2【中 / BUG】旧审批响应关闭新弹窗，提示结果与实际不符

- 位置：`biz/src/components/Audit.vue:1060`。
- 实测：提交 A“通过”，暂缓其真实响应；关闭 A，打开 B，选择“驳回”并填写尚未提交的意见。放行 A 响应后，B 弹窗被关闭，提示却为“已驳回”。真实状态是 A 已通过至 20，B 仍待审 10。
- 原因：成功回调无条件关闭共享弹窗，提示读取当前 `auditForm.result`，没有固定提交时的结果和弹窗上下文。
- 建议：保存提交时的单号、结果和弹窗序号；响应只影响原弹窗，提示使用原结果。不要清理另一单的编辑状态。

## 12-3【中 / BUG】B 材料预览被 A 的慢响应替换

- 位置：`biz/src/components/Audit.vue:981`。
- 实测：预览 A 文件并延迟真实下载响应，关闭后打开 B 审核单并预览 B 文件；B 文件名正常出现后放行 A，预览名称和内容被换成 A，底层审核单仍是 B。
- 影响：审核人可能基于另一单的材料判断当前申请。
- 建议：下载及 Blob 处理增加文件 ID 和请求序号校验；关闭时失效/取消旧请求，清理对象 URL，旧请求不得改写当前预览或 loading。

## 证据与测试边界

三个 Playwright + Edge 场景均通过缺陷复现断言，无浏览器脚本异常。全部使用真实本地 HTTP、随机隔离 MySQL、合成账号/业务记录/上传 PDF，只延迟真实响应，未伪造接口内容或修改 Vue 状态。

文件预览使用 11-1 闭环测试实际上传的两个 PDF，记录中包含真实文件 ID 和不同文件名；关联任务关系由合成种子构造。真实审批操作及其通知仅落在隔离库。

[结构化结果与截图](audit-evidence/2026-09-16/batch-12/)：`related-task`、`approval-dialog`、`file-preview`。用例通过意味着 BUG 存在，不代表修复。

## 全新环境复测顺序

启动既有 `serve.py --fixture review`，按已有方式设置数据库密码环境变量，然后：

```powershell
python scripts/ui-audit/seed-batch-8.py --batch 11
node scripts/ui-audit/batch-11-audit.cjs --fixed
node scripts/ui-audit/batch-11-audit.cjs --fixed --stay
python scripts/ui-audit/seed-batch-8.py --batch 10
python scripts/ui-audit/seed-batch-8.py --batch 12
node scripts/ui-audit/batch-12-audit.cjs file-preview
node scripts/ui-audit/batch-12-audit.cjs approval-dialog
node scripts/ui-audit/batch-12-audit.cjs related-task
New-Item -ItemType File -Path target/ui-audit/STOP -Force
```

脚本语法、改动空白检查通过。临时服务与随机数据库已清理，原有服务与未提交工作保留。本批未改业务代码、未部署，等待用户选择修复项。未验证的其他疑点不作为结论。
