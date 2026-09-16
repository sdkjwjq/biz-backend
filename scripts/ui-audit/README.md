# 本地浏览器审计

这些脚本复现现有缺陷，不是修复后的通过用例。仅使用随机隔离数据库和合成账号。当前固定端口为前端 15173、后端 18080；运行前保证这两个端口空闲。

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

`zero-audit` 对比真实接口的零值与审核列表显示；`performance-year` 是正常年份显示的对照检查；`dashboard-unmount` 使用普通时钟验证离开页面后的延迟响应。`performance-race` 延迟真实请求，随后实际点击审批按钮，检查错误审批对象及两条记录的真实状态。它会改变合成记录的状态，每个新环境只运行一次；再次运行或随后重跑 `zero-audit` 前，请停止并重启隔离环境。

结果保存在 `target/ui-audit/evidence-business`。其中 3 个缺陷复现场景断言通过表示问题仍存在，年份检查通过表示该疑点已排除。

结束时执行：

```powershell
New-Item -ItemType File -Path target/ui-audit/STOP -Force
```

等待服务脚本打印 `Removed isolated UI schema`。它只停止自己启动的进程，并删除本次生成、通过名称校验的数据库；正常运行最长一小时后自动清理。请通过 STOP 文件停止，避免强杀管理进程导致清理未执行。

若修改了前端或后端实现，复现场景中的“缺陷仍存在”断言应改为正确行为预期，再用于回归测试。
