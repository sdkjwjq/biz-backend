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

结果保存在 `target/ui-audit/evidence-business`。目前 `zero-audit`、`dashboard-unmount` 通过表示缺陷仍存在；`performance-race` 通过表示修复后的行为正确，年份检查通过表示该疑点已排除。历史复现证据仍保留在 `docs/audit-evidence/2026-09-16/batch-6`。

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

前 3 个场景验证现有缺陷：审核分页重复数据、切换历史记录后需手动刷新、小数奖项数量提交成功但保存时截断。`achievement-validation` 检查空表单、非法文件扩展名及整数数量保存，属于正常行为对照。成果场景通过页面实际新增合成记录，按新增接口返回的 ID 核对保存结果；多次运行会新增更多测试记录，随隔离库一起清理。结果写入 `target/ui-audit/evidence-review`。

### 清理

结束时执行：

```powershell
New-Item -ItemType File -Path target/ui-audit/STOP -Force
```

等待服务脚本打印 `Removed isolated UI schema`。它只停止自己启动的进程，并删除本次生成、通过名称校验的数据库；正常运行最长一小时后自动清理。请通过 STOP 文件停止，避免强杀管理进程导致清理未执行。

若修改了前端或后端实现，复现场景中的“缺陷仍存在”断言应改为正确行为预期，再用于回归测试。
