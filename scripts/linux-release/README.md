# 双高平台 Linux 更新包（2026-09-20）

适配服务器：172.19.2.81，Java 17、MySQL 8，普通 Java 后台进程（不创建 systemd 服务）。服务端不需要 Python 或 Node。

## 一键更新

上传 `shuanggao-update-20260920.tar.gz` 到 `/root`，执行：

```bash
cd /root
tar -xzf shuanggao-update-20260920.tar.gz
cd shuanggao-update-20260920
bash update.sh --update
```

根据提示输入服务器 MySQL root 密码。密码不回显，不放进更新包、命令行或 Git；写入服务器 `/root/biz-backend/.release.properties`，权限 600。下次启动应保留脚本使用的 `--spring.config.additional-location=file:/root/biz-backend/.release.properties` 参数。

可先运行 `bash update.sh --check`，只检查依赖、进程、配置和数据库兼容性，不停服务、不修改数据库。默认数据库为本机 TCP 3306、biz、root；端口为 8080。

## 文件位置

| 内容 | 位置 |
|---|---|
| 后端程序 | `/root/biz-backend/biz_backend-1.0-SNAPSHOT.jar` |
| 前端文件 | `/usr/share/nginx/html` |
| 原上传材料 | `/root/biz-backend/uploads`，完整保留 |
| 预算下载模板 | `/usr/share/nginx/html/templates/budget-template.xlsx` |
| 预算兼容副本 | uploads 中缺少同名文件时才补充，不覆盖现有文件 |
| 工作纪实导出模板 | JAR 内 `templates/work-record.docx`，包含仿宋 GB2312 小四表格字体处理 |
| 启动输出 | `/root/biz-backend/logs/release-startup.log` |
| 自动备份 | `/root/biz-backend/backups/release-时间-进程号/` |

更新不改 nginx 配置，不删除旧前端哈希资源、上传材料、`.well-known` 或服务器其他文件；前端入口最后发布。浏览器更新后按 Ctrl+F5 刷新。

## 数据库保护

- 先校验包和当前环境、备份程序及前端，再停止目标后端，完整备份数据库、触发器/存储过程/事件及 uploads；任一备份失败即停止更新并尝试恢复旧服务。
- 只执行 `sql/additive.sql`：按需创建工作纪实三表，以及兼容旧环境的审核快照、绩效审核、成果审核和预算表；仅补充白名单中的缺失字段。
- 已有表和列不重建，不导入本地业务记录；没有业务 INSERT、UPDATE、DELETE、TRUNCATE、DROP，不执行历史数据纠偏、账号权限调整、密码重置或审核日志重建。
- 增量 DDL 在 MySQL 中不是整体事务；中途失败可能已新增部分结构，脚本保留这些结构，重试可跳过已有对象。不会为了回滚而删除新业务数据。
- 检测到不在迁移范围内的缺失基础字段，会明确报错；不猜测旧数据格式、不覆盖线上数据。
- 如原进程含显式数据库或自定义 Spring 配置参数，脚本会在修改前停止，需按报错核对参数。
- 新版正常启动后继续执行平台原有定时业务，本包没有额外数据修复任务。

## 回滚

更新失败时脚本自动尝试恢复旧 JAR、旧前端、旧启动参数。失败或需要人工回滚时，使用更新输出的准确备份路径：

```bash
bash /root/shuanggao-update-20260920/update.sh --rollback /root/biz-backend/backups/release-实际备份目录
```

回滚不会导入数据库备份，也不覆盖上传材料。数据库备份 `database.sql.gz` 留作人工灾难恢复；禁止在继续有用户操作的线上库中直接覆盖导入。需要恢复数据时应单独确认恢复时间点。

## 内容与验证边界

包含当前前后端代码及现有前端未提交的成果页、审核中心修改（详见 manifest），不包含测试数据、日志和数据库密码。管理员功能仅包含已经完成的“新增三级任务”，任务移交和绩效关联管理尚未开发。

验证包括构建、Shell 语法、隔离 MySQL 增量迁移、重复执行的数据保留检查，以及使用实际发布 JAR 和外部密码配置启动、登录、查询管理员和纪实权限接口；另用模拟进程操作验证失败恢复确实保留上传材料和前端资源。详见 verification.json。程序业务已通过 46 项接口回归及 7 组新增任务 Playwright 操作。脚本完整 Linux 进程启停仍需在目标服务器首次执行时由内置检查验证；本地 Windows 不冒充服务器实测。

原手动备份 `/root/biz-backend/20260920backup.sql` 不会被修改。更新时请暂停用户操作；脚本不会自动安装软件、变更系统服务或修改防火墙。
