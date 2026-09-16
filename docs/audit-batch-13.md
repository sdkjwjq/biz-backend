# 第十三批：填报和撤回返回后页面串线

前置：12-1、12-2 已修复；12-3 按客户要求忽略。材料允许覆盖、只保留当前及上一次提交的规则保持不动。本批只审计，不修改业务代码。

## 13-1【高 / BUG】绩效内关联任务的填报仍会写入另一个任务的值

- 位置：`biz/src/components/Performance.vue:2713`，`submitTaskFeedback`。
- 实测：在自动绩效的关联任务入口打开 A，填报 3 并提交文件；延迟真实上传响应，返回绩效详情后打开当前值为 7 的 B。放行响应，实际提交 `task_id=932001, reported_value=7`，A 审核单真实保存 7。
- 原因：上传前只保存任务 ID，上传完成后读取共享的任务填报值/子任务集合/审核单。11-1 修复的是 Works.vue 独立入口，此处有独立实现；12-1 处理的是审批加载，不包含填报提交。
- 建议：首次异步操作前保存完整提交快照，提交完成后只更新对应任务详情，避免修改后来打开的任务。

## 13-2【中 / BUG】撤回 A 的回调把 B 详情切回 A，清空 B 草稿

- 位置：`biz/src/components/Works.vue:2469`，`handleWithdrawTask`。
- 实测：撤回上述真实提交的 A，暂停撤回接口成功响应；关闭 A 后打开 B，填写新草稿。放行响应后，抽屉从 B 自动变成 A，B 草稿清空。
- 原因：回调无条件清空共享表单，并将 `currentTask` 设置为被撤回的 A，没有检查当前详情是否仍是 A。
- 建议：捕获撤回时的详情上下文；仍可刷新列表，但只有原详情未改变时才能清空表单、更新当前任务或结束其 loading。

## 13-3【中 / BUG】绩效 C 提交返回后，D 详情显示 C 的数值

- 位置：`biz/src/components/Performance.vue:1117`，`submitManualPerformance`。
- 实测：C 目标 10，提交完成值 7；延迟真实提交响应，关闭 C 并打开 D（目标 20，完成值 0）。放行后名称仍为 D，目标值变为 10、完成值变为 7、完成率变为 70%。真实年度接口中 D 仍为目标 20、完成值 0。
- 影响：当前绩效详情展示另一指标的数据，可能误导后续填报。没有证据表明本次操作改写了 D 的数据库记录。
- 原因：回调将 C 的更新字段合并到共享 `currentPerformance`，未校验绩效 ID、年份和详情打开序号。
- 建议：固定提交上下文，回调只更新对应绩效/年份；异步完成时不要覆盖新详情或其表单。

## 证据

[Playwright JSON 与截图](audit-evidence/2026-09-16/batch-13/)：`related-upload`、`withdraw-context`、`manual-submit`。

三个场景均使用 Edge、真实本地 HTTP、随机隔离 MySQL 和合成账号/记录。仅延迟真实响应，不伪造接口内容或修改 Vue 状态。任务上传、绩效提交和撤回均真实执行，通知仅落隔离库。所有最终缺陷复现断言通过，无浏览器脚本异常；通过表示问题存在，并非修复。

第三场景第一次附加的“草稿已重置”断言未成立，故不作为缺陷结论。后续等待尝试的测试进程已停止，使用真实撤回恢复 C 后重新复现；最终证据仅断言详情串值和 D 数据库保持原值。

## 全新环境复测

启动既有 `serve.py --fixture review`，按既有方式设置数据库密码环境变量：

```powershell
python scripts/ui-audit/seed-batch-8.py --batch 11
python scripts/ui-audit/seed-batch-8.py --batch 13
node scripts/ui-audit/batch-13-audit.cjs related-upload
node scripts/ui-audit/batch-13-audit.cjs withdraw-context
node scripts/ui-audit/batch-13-audit.cjs manual-submit
New-Item -ItemType File -Path target/ui-audit/STOP -Force
```

`withdraw-context` 依赖第一场景真实生成的提交及快照。重复测试手动绩效时可加 `--reset-manual`，仅撤回指定合成用户 910001 对合成绩效 950031 的待审单，不涉及真实数据。

脚本语法和改动空白检查通过。临时服务与随机数据库已清理，原有未提交改动保留。本批未修复、未部署；等待用户选择。
