# 第十一批：任务填报上传期间切换任务

## 11-1【高 / BUG】A 填报 3，切到 B 后实际保存为 7

- 位置：`biz/src/components/Works.vue:2705` 的 `submitFeedback`，上传返回后读取 `feedbackForm.progress`、`forthTasks`、`latestAudit`，提交成功后无条件清空当前表单。
- 已证实影响：任务 A 使用任务 B 的数值生成审核单；B 未提交的完成情况草稿被清空。与 10-1 的审批加载问题不同，本项发生在材料上传后的填报提交链路。
- 建议：点击提交时固定任务 ID、填报值、子任务列表、完成情况和重提单号；异步完成后只更新仍属于本次提交的详情，关闭或切换后不清空新任务的表单。是否允许切换后继续提交原任务可在修复时沿用现有提交行为，确保整个请求使用原快照。

## 真实闭环复现

Playwright + Edge、随机隔离 MySQL、真实本地接口，未修改组件状态或伪造上传/业务响应。仅暂停上传接口的真实响应。

1. 合成用户 910001 是 A（932001）和 B（932002）的负责人，两任务均可填报，无四级子任务。
2. 打开 A，输入值 3、完成情况“A本次填报3”，选择合成 PDF，点击提交反馈。
3. 上传接口已完成，暂缓响应；通过抽屉关闭按钮离开 A，打开 B，其当前值为 7；输入“B尚未提交的草稿”。
4. 放行 A 的上传响应，实际 `/biz/sub` 提交 `task_id=932001, reported_value=7`，完成情况仍为 A 的文本。
5. 查询 A 审核单，真实保存的 `reportedValue=7`；B 的完成情况输入框变为空。

[结构化记录](audit-evidence/2026-09-16/batch-11/feedback-upload-race.json) · [放行上传响应前截图](audit-evidence/2026-09-16/batch-11/before-upload-return.png)

复现断言通过代表 BUG 存在，尚未修复。其余分支如四级任务、退回重提不在本次实测结论中，不推断其业务结果。

## 复测

启动全新 `scripts/ui-audit/serve.py --fixture review` 隔离环境，按已有方式设置数据库密码环境变量，再执行：

```powershell
python scripts/ui-audit/seed-batch-8.py --batch 11
node scripts/ui-audit/batch-11-audit.cjs
New-Item -ItemType File -Path target/ui-audit/STOP -Force
```

仅使用合成记录、合成文件，实际提交及通知都在隔离库。结束后临时服务、上传目录和随机数据库由既有服务脚本清理；原有服务和未提交改动保留。

本批仅新增审计脚本和文档。脚本语法及改动空白检查通过，未改业务代码，未运行无关构建和后端回归。等待用户选择是否修复。
