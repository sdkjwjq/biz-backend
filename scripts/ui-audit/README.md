# 本地浏览器审计

这些脚本包括缺陷复现与修复回归，请按下面说明区分。仅使用随机隔离数据库和合成账号。当前固定端口为前端 15173、后端 18080；运行前保证这两个端口空闲。

## 准备

需要 Java 17、Maven、Node、Python、MySQL 客户端、本机 Edge，以及已安装依赖的相邻 `biz` 前端。进入后端目录执行：

```powershell
mvn.cmd -B -DskipTests test-compile
mvn.cmd -B dependency:build-classpath '-DincludeScope=test' '-Dmdep.outputFile=target/ui-audit-classpath.txt'
npm.cmd install --prefix target/ui-audit-tools --no-save --package-lock=false playwright@1.63.0
```

`IsolatedUiAuditServer.java` 复用 `ReviewBatchRegressionApiTest.NoAutomaticSchedules` 测试配置，保留真实业务服务并关闭自动调度。测试进程也关闭开发热重载，防止输出证据文件导致服务重启。

## 运行与停止

为当前进程设置 `SHUANGGAO_TEST_DB_PASSWORD`（本地 MySQL 密码），然后执行：

```powershell
python scripts/ui-audit/serve.py
```

出现 `UI_AUDIT_READY` 后，在另一个终端的后端目录按需执行：

```powershell
node scripts/ui-audit/browser-audit.cjs cache
node scripts/ui-audit/browser-audit.cjs notices
node scripts/ui-audit/browser-audit.cjs deep-link
node scripts/ui-audit/browser-audit.cjs badge
node scripts/ui-audit/browser-audit.cjs polling
```

每次启动全新浏览器上下文；跨账号缓存测试在同一个标签页内真实登录和退出。外部网络资源被阻止，截图和请求路径写入 `target/ui-audit/evidence`，不记录 JWT 或本地数据库密码。`inspect` 可用于查看基础页面结构。

### 业务页面专项（第六批）

先停止上一轮环境，再加载扩展的合成绩效数据：

```powershell
python scripts/ui-audit/serve.py --fixture business
```

出现 `UI_AUDIT_READY` 后，按下面顺序在另一个终端执行：

```powershell
node scripts/ui-audit/business-audit.cjs zero-audit
node scripts/ui-audit/business-audit.cjs performance-year
node scripts/ui-audit/business-audit.cjs dashboard-unmount
node scripts/ui-audit/business-audit.cjs performance-race
```

`zero-audit` 对比真实接口的零值与审核列表显示；`performance-year` 是正常年份显示的对照检查；`dashboard-unmount` 使用普通时钟验证离开页面后的延迟响应。`performance-race` 已改为修复回归：延迟 A 的真实请求后打开 B，确认 A 不能覆盖 B；实际审批 B，在审批响应返回前切回 A，确认 A 的意见不被重置，A 仍为状态 10，B 为状态 20。它会改变合成记录的状态，每个新环境只运行一次；再次运行或随后重跑 `zero-audit` 前，请停止并重启隔离环境。

结果保存在 `target/ui-audit/evidence-business`。`dashboard-unmount`、`zero-audit`、`performance-race` 已改为修复回归，通过表示修复后的行为正确，年份检查通过表示该疑点已排除。历史复现证据仍保留在 `docs/audit-evidence/2026-09-16/batch-6`。

大屏补充生命周期回归（`business` 或 `review` 环境）：

```powershell
node scripts/ui-audit/dashboard-lifecycle.cjs delayed-init
node scripts/ui-audit/dashboard-lifecycle.cjs chart-request
node scripts/ui-audit/dashboard-lifecycle.cjs repeat
node scripts/ui-audit/dashboard-lifecycle.cjs scroll
```

分别覆盖初始化延迟、图表数据请求中途退出、三轮进入退出、滚动停止，并验证重进后的五个图表。`delayed-init` 仅延长 800ms 初始化定时器，用于确定性退出；`scroll` 明确注入六条合成成果激活滚动，其余场景使用真实接口。结果位于 `target/ui-audit/evidence-dashboard`。

同一 `business` 隔离环境中可运行额外的只读绩效回归：

```powershell
node scripts/ui-audit/performance-context.cjs stale-failure
node scripts/ui-audit/performance-context.cjs same-record
node scripts/ui-audit/performance-context.cjs year-change
node scripts/ui-audit/performance-context.cjs unmount
```

覆盖旧请求失败与加载状态、A→B→A 关闭重开、2026→2027 年份切换、组件卸载及重新进入。成功响应均来自真实接口；`stale-failure` 对旧请求主动模拟网络失败。结果写入 `target/ui-audit/evidence-performance-context`。

### 审核列表及成果表单（第七批）

停止上一轮环境后，以 `python scripts/ui-audit/serve.py --fixture review` 启动。此数据集包含 15 条待审绩效、1 条已归档绩效以及成果填报账号。出现 `UI_AUDIT_READY` 后执行：

```powershell
node scripts/ui-audit/review-audit.cjs pagination
node scripts/ui-audit/review-audit.cjs history-tab
node scripts/ui-audit/review-audit.cjs achievement-count
node scripts/ui-audit/review-audit.cjs achievement-validation
```

`pagination`、`history-tab` 已改为修复回归：15 条按 10＋5 分页不重复，历史标签自动加载、归档不计入待办。`achievement-count` 已改为修复回归：小数 1.5 被拒绝且不上传文件、不提交新增；改为 2 后真实保存，查询及页面显示为 2。`achievement-validation` 检查空表单、非法文件扩展名及整数数量保存，属于正常行为对照。成果场景通过页面实际新增合成记录，按新增接口返回的 ID 核对保存结果；多次运行会新增更多测试记录，随隔离库一起清理。结果写入 `target/ui-audit/evidence-review`。

成果数量后端专项使用 `scripts/run-review-regression.py achievementQuantitiesRejectInvalidWithoutWrites` 和 `scripts/run-review-regression.py achievementQuantitiesPreserveCompatibleInputs`。先设置 `SHUANGGAO_TEST_DB_PASSWORD` 环境变量；脚本独立创建并清理临时数据库。第一个方法覆盖九字段、新增/修改的 198 次非法请求与无写入断言；第二个覆盖全部兼容输入。全量执行该脚本时也会包含这两个新增方法。

审核中心补充回归使用同一全新 `review` 环境，先执行上述分页/历史测试，再执行：

```powershell
node scripts/ui-audit/business-audit.cjs zero-audit
node scripts/ui-audit/audit-center-regression.cjs value-contract
node scripts/ui-audit/audit-center-regression.cjs tab-race
node scripts/ui-audit/audit-center-regression.cjs filters
node scripts/ui-audit/audit-center-regression.cjs deep-link
node scripts/ui-audit/audit-center-regression.cjs last-page
```

`value-contract` 明确使用响应注入，覆盖任务数值零、字符串零、空值、缺省和非零值；其他场景使用真实接口。`tab-race` 延迟真实历史请求，验证快速切换后的待办不被覆盖。`last-page` 实际审批末页 5 条，只能最后执行；重跑需重建隔离库。补充结果位于 `target/ui-audit/evidence-audit-center`，原始缺陷证据保留不变。

### 清理

结束时执行：

```powershell
New-Item -ItemType File -Path target/ui-audit/STOP -Force
```

等待服务脚本打印 `Removed isolated UI schema`。它只停止自己启动的进程，并删除本次生成、通过名称校验的数据库；正常运行最长一小时后自动清理。请通过 STOP 文件停止，避免强杀管理进程导致清理未执行。

若修改了前端或后端实现，复现场景中的“缺陷仍存在”断言应改为正确行为预期，再用于回归测试。
