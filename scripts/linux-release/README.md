# 双高平台更新包 20261012

适用环境：Java 17、MySQL 8、Bash 4.2+。服务器不需要安装 Python 或 Node。

## 一键更新

将完整文件夹 `shuanggao-update-20261012` 上传到 `/root/biz-backend/`，不要覆盖旧更新包，然后执行：

```bash
cd /root/biz-backend/shuanggao-update-20261012
chmod -R go-rwx .
sha256sum -c SHA256SUMS --quiet
bash update.sh --check
bash update.sh --update
```

`--check` 只读检查环境、文件和数据库结构。`--update` 会再次提示输入 MySQL root 密码，密码不回显，也不会写入脚本、日志或 JAR。

本次仅做增量更新，保留服务器当前业务数据。包内不含旧数据库备份，不支持 `--restore-backup`。

## 更新步骤

脚本依次执行：

1. 校验更新包、Java、MySQL、nginx、端口、磁盘空间和当前后端进程。
2. 备份当前 JAR、前端、数据库和上传材料到 `/root/biz-backend/backups/release-时间-PID`。
3. 停止后端，补充缺失表和字段。
4. 为现有预算建立只读初始版本快照，重复执行不会重复生成。
5. 只执行尚未执行的一次性迁移：历史任务归属修正、成果归档记录修复、四级目标等权重算及自动绩效重算。
6. 清理已经失效的待审核通知，保留审批日志和仍需处理的待办。
7. 启动新 JAR，检查数据库连通和登录接口，再更新前端文件。

一次性迁移记录在 `sys_release_migration`。脚本也识别旧版本留下的迁移备份表，避免重复运行旧数据修正，覆盖后来由管理员完成的任务移交。

迁移或启动失败时，脚本会自动恢复本次更新前的完整数据库和应用文件。更新成功后输出备份路径及准确回滚命令。

## 本次主要内容

- 校级领导全校或分管部门查阅、只读管理概览，后端统一校验权限。仅首次按已核实工号及姓名初始化刘莹、鲍桂楠全校权限，其他领导由管理员配置。
- 三级任务重点／困难标记与筛选、计划完成日期和有可靠归档证据的实际完成日期。
- 核心指标独立月度填报、归口审核及归档，接入通知和审核中心；不反写原绩效。
- 工作纪实按三级／四级任务责任人判定资格和统计范围；已提交快照不变。
- 预算页面及导出使用所选年月之前最近已归档核心指标；原导入内容保留。
- 预算折线图暂时隐藏，后端月度预算、版本记录和趋势接口保留。

新迁移仅首次初始化必要配置及可证明的日期，不覆盖后续管理员修改；不生成核心指标历史填报。各迁移核对结果保存在本次备份目录的 `*-check.log` 中。

## 数据保护

- `additive.sql` 只创建缺失结构，不删除表、字段或业务行。
- 四级目标重算不修改原始四级填报值和历史审核日志；重算前会建立专用备份表，校验不通过则整库恢复。
- 历史数据中若三级目标值为空或为 0，且只有一个有效四级目标，则在备份后采用该唯一目标值；无法唯一判断时停止更新，不猜测业务值。
- 旧任务归属修正、成果归档修复和四级目标重算均只执行一次。
- 本次更新包不含线上历史备份；更新前自动备份当前线上数据。请将更新包和自动备份保存在 root 私有目录。
- JAR 不内置本地或服务器 MySQL 密码，运行密码保存在服务器 `/root/biz-backend/.release.properties`，权限为 600。

## 文件位置

- 后端：`/root/biz-backend/biz_backend-1.0-SNAPSHOT.jar`
- 前端：`/usr/share/nginx/html`
- 上传材料：`/root/biz-backend/uploads`
- 前端模板：`/usr/share/nginx/html/templates`
- 更新备份：`/root/biz-backend/backups/release-时间-PID`
- 启动日志：`/root/biz-backend/logs/release-startup.log`

## 回滚

本包包含数据迁移，成功后应使用脚本输出的数据库及应用完整回滚命令：

```bash
bash /root/biz-backend/shuanggao-update-20261012/update.sh --rollback-database /root/biz-backend/backups/release-实际时间-PID
```

回滚前脚本会额外保存当时的数据库到 `before-rollback-*.sql.gz`，再恢复更新前数据库、JAR 和前端。不要把回滚命令当作重启命令。

包内 `manual-*-rollback.sql` 只供人工诊断。正常回滚使用上面的完整备份恢复命令。

## 验证资料

- `verification.json`：本地隔离数据库、JAR 和迁移测试结果。
- `manifest.json`：代码版本、迁移清单和备份摘要。
- `SHA256SUMS`：全部交付文件的 SHA-256 校验值。

本地验证仅使用随机隔离数据库和合成数据，不连接生产服务器。Windows 环境不能实际验证 Linux `/proc` 进程管理，服务器上的 `--check` 会在更新前完成对应检查。
