# 双高平台更新包 20260920-r3

适配 172.19.2.81：Java 17、MySQL 8、Bash 4.2+，服务端不需要 Python 或 Node。

## 本次一键更新（保留当前数据）

上传整个 `shuanggao-update-20260920-r3` 文件夹到 `/root/biz-backend/`，不要覆盖旧包。本次包含新版登录页排版和强制改密弹窗“返回登录”按钮。

```bash
cd /root/biz-backend/shuanggao-update-20260920-r3
chmod -R go-rwx .
bash update.sh --update
```

按提示输入服务器 MySQL root 密码，不回显。请使用文件传输工具的二进制传输模式，保留目录结构和文件内容。

**本次命令不恢复数据库，不覆盖业务数据，上传材料保持原样。不要添加 `--restore-backup`。** 包中保留的恢复能力仅供尚未执行此前已确认恢复操作的环境使用；显式增加该参数将使数据库回到 20260920backup.sql 时点，后续数据仅保留在更新前备份中。

本次步骤：文件校验 → 验证进程及空间 → 备份程序和前端 → 停止后端 → 完整备份当前数据库及 uploads → 补齐缺失结构 → 启动后端并验证数据库连通 → 更新前端入口。

同一备份成功恢复后会记录标记，禁止重复恢复覆盖新数据。之后普通更新使用：

```bash
bash update.sh --update
```

普通更新只补充结构，不导入备份、不覆盖业务行。`bash update.sh --check` 只读检查当前环境及普通增量更新兼容性，不执行数据恢复。

## 本次功能

- 管理员工作纪实逻辑删除、同月重新填报，保留删除原因和原文快照。
- 工作纪实从现有改革任务逐项添加，兼容原文及 Word 导出。
- 强制改密读取线上 `force_password_change`；首次改密前工号可登录，成功改密置 0；按已确认规则，所有有效账号也可凭工号重置密码。
- 工作纪实及相关缺失结构迁移；保留已有线上改密标记，不按密码复杂度改写。

## 文件位置

- 后端：`/root/biz-backend/biz_backend-1.0-SNAPSHOT.jar`。
- 前端：`/usr/share/nginx/html`，保留旧哈希资源及现有上传目录。
- 原材料：`/root/biz-backend/uploads`，预算模板仅缺失时补充。
- Word 模板内置 JAR；前端下载模板在 `/usr/share/nginx/html/templates`。
- 数据库外部配置：`/root/biz-backend/.release.properties`，权限 600。
- 更新备份：`/root/biz-backend/backups/release-日期时间-PID`，包括更新前 SQL、JAR、前端和材料。
- 启动日志：`/root/biz-backend/logs/release-startup.log`；迁移和恢复日志在本次备份目录。

**更新包包含真实业务备份及其中的用户密码，只放在 root 私有目录，不放 nginx 目录或公开分享。** JAR 不内置本地或服务器 MySQL 密码，原始备份不提交 Git。

## 失败与回滚

失败时自动恢复旧程序和前端；如果已开始替换数据库，先恢复更新前数据库再启动旧程序。数据库自动恢复失败时保持后端停止并显示备份路径。

恢复过数据的更新需同时回滚数据库及应用：

```bash
bash update.sh --rollback-database /root/biz-backend/backups/release-实际日期时间-PID
```

回滚先停止写入并额外保存当前数据库到 `before-rollback-*.sql.gz`，再恢复更新前数据与应用。回滚后的在线数据回到更新前，更新后数据保留在额外备份中。不要把此命令当作重启。

普通增量更新可用 `--rollback 备份目录` 恢复应用；存在逻辑删除纪实时，禁止直接回滚到可能不识别删除标记的旧应用。任何回滚均不删除上传材料。

## 验证范围

结果见 `verification.json`，代码版本及备份摘要见 `manifest.json`，文件校验见 `SHA256SUMS`。本地测试使用合成数据及随机数据库，未连接生产服务器；Linux /proc 进程管理不在 Windows 上实际执行。
