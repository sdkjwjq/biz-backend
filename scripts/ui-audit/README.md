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

结束时执行：

```powershell
New-Item -ItemType File -Path target/ui-audit/STOP -Force
```

等待服务脚本打印 `Removed isolated UI schema`。它只停止自己启动的进程，并删除本次生成、通过名称校验的数据库；正常运行最长一小时后自动清理。请通过 STOP 文件停止，避免强杀管理进程导致清理未执行。

若修改了前端或后端实现，复现场景中的“缺陷仍存在”断言应改为正确行为预期，再用于回归测试。
