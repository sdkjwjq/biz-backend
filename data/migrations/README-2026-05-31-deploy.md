# 2026-05-31 上线数据库迁移说明

## 适用范围

这些脚本用于把本轮对话中涉及的任务审核、绩效审核、绩效年度目标、历史数据清理、2025 年任务审核流伪造等数据库变更同步到线上库。

执行前必须先备份线上数据库。

## 推荐执行顺序

按下面顺序执行，不建议按文件名排序直接批量跑。

1. `2026-05-30-audit-snapshot.sql`

   新增任务审核快照表 `biz_audit_snapshot`，用于任务提交/退回/撤回时恢复原值。

2. `2026-05-30-audit-snapshot-comment.sql`

   兼容补丁：如果 `biz_audit_snapshot` 旧表缺少 `previous_comment`，则补充该字段。

3. `2026-05-30-performance-audit.sql`

   新增绩效手动填报审核相关表：
   - `biz_performance_submission`
   - `biz_performance_audit_snapshot`
   - `biz_performance_audit_log`

4. `2026-05-30-performance-relation-year-fix.sql`

   数据修正脚本：
   - 按客户原始 Excel 修正年度绩效目标值。
   - 修正 `rel_task_performance.year_id`，使其以任务年度为准。
   - 删除明确确认的脏关联数据。
   - 清理 2027 年及以后填报/审核/日志/消息数据。
   - 重置 2027 年及以后任务、四级任务、年度绩效实际值。
   - 重算绩效总完成值。

5. `2026-05-30-task-progress-cap.sql`

   任务进度封顶到 100%，但不改数值型任务的实际完成值。

6. `2026-05-30-percent-value-cap.sql`

   百分比类型数据封顶到 100；数值型数据不封顶。

7. `2026-05-31-performance-notice-zh.sql`

   修复绩效审核消息：
   - 旧英文消息改中文。
   - 乱码问号消息按流程修复为中文。
   - 清理 2027 年及以后绩效审核消息。

8. `2026-05-31-2025-task-audit-flow-normalize.sql`

   伪造/规范 2025 年已完成任务审核流：
   - 保留原审批单。
   - 清理旧版一键完成产生的异常审批日志。
   - 重建正常四步日志：提交、专业群审核、归口审核、管理员归档。
   - 不生成历史消息通知。
   - 生成备份表：
     - `biz_audit_log_backup_20260531_2025_flow`
     - `biz_material_submission_backup_20260531_2025_flow`

9. `2026-05-31-achievement-audit.sql`

   新增成果归档审核能力：
   - 给 `biz_achievement` 增加 `audit_status`、`current_handler_id`、`update_time`。
   - 新增成果审核单表 `biz_achievement_submission`。
   - 新增成果审核日志表 `biz_achievement_audit_log`。
   - 新增三个固定成果上传账号：`800001`、`800002`、`800003`，密码分别与账号相同。
   - 若历史环境已创建过 `990201`、`990202`、`990203` 三个旧成果账号，本脚本会将其作废，避免继续误用旧账号。
   - 三个账号所属部门为 `机电工程学院`（`dept_id=118`）。

## 执行命令

在服务器进入 `biz-backend` 目录后执行。请把用户名、密码、库名替换为线上实际值。

```bash
mysql --default-character-set=utf8mb4 -u root -p biz --execute="source data/migrations/2026-05-30-audit-snapshot.sql"
mysql --default-character-set=utf8mb4 -u root -p biz --execute="source data/migrations/2026-05-30-audit-snapshot-comment.sql"
mysql --default-character-set=utf8mb4 -u root -p biz --execute="source data/migrations/2026-05-30-performance-audit.sql"
mysql --default-character-set=utf8mb4 -u root -p biz --execute="source data/migrations/2026-05-30-performance-relation-year-fix.sql"
mysql --default-character-set=utf8mb4 -u root -p biz --execute="source data/migrations/2026-05-30-task-progress-cap.sql"
mysql --default-character-set=utf8mb4 -u root -p biz --execute="source data/migrations/2026-05-30-percent-value-cap.sql"
mysql --default-character-set=utf8mb4 -u root -p biz --execute="source data/migrations/2026-05-31-performance-notice-zh.sql"
mysql --default-character-set=utf8mb4 -u root -p biz --execute="source data/migrations/2026-05-31-2025-task-audit-flow-normalize.sql"
mysql --default-character-set=utf8mb4 -u root -p biz --execute="source data/migrations/2026-05-31-achievement-audit.sql"
```

Windows PowerShell 中不要使用 `< script.sql` 这种重定向方式，建议也使用上面的 `--execute="source ..."`。

## 执行后必须检查

### 1. 表结构检查

```sql
SHOW TABLES LIKE 'biz_audit_snapshot';
SHOW TABLES LIKE 'biz_performance_submission';
SHOW TABLES LIKE 'biz_performance_audit_snapshot';
SHOW TABLES LIKE 'biz_performance_audit_log';
SHOW TABLES LIKE 'biz_achievement_submission';
SHOW TABLES LIKE 'biz_achievement_audit_log';
```

### 2. 2027 年及以后填报数据清理检查

执行 `2026-05-30-performance-relation-year-fix.sql` 后，脚本末尾会输出：

```text
future_task_submission_count
future_performance_submission_count
future_performance_year_actual_nonzero_count
```

这三项应为 `0`。

### 3. 任务进度封顶检查

执行 `2026-05-30-task-progress-cap.sql` 后，脚本末尾会输出：

```text
biz_task_progress_over_100
biz_level4_task_progress_over_100
```

两项应为 `0`。

### 4. 百分比封顶检查

执行 `2026-05-30-percent-value-cap.sql` 后，脚本末尾会输出：

```text
percent_task_over_100
percent_level4_task_over_100
percent_performance_over_100
percent_performance_year_over_100
```

四项应为 `0`。

### 5. 2025 任务审核流检查

执行 `2026-05-31-2025-task-audit-flow-normalize.sql` 后，脚本末尾会输出：

```text
missing_submit_0_10
missing_auditor_10_20
missing_principal_20_30
missing_admin_30_40
old_one_click_log_count
submission_time_not_2025
audit_log_time_not_2025
```

以上各项应为 `0`。

### 6. 成果审核迁移检查

执行 `2026-05-31-achievement-audit.sql` 后检查：

```sql
SELECT user_id, dept_id, user_name, nick_name, password, role
FROM sys_user
WHERE user_id IN (800001, 800002, 800003, 990201, 990202, 990203);

SELECT COUNT(*) AS achievement_count,
       SUM(COALESCE(audit_status, 30) = 30) AS archived_count
FROM biz_achievement
WHERE is_delete = 0;
```

`800001`、`800002`、`800003` 三个成果上传账号应显示为中文名称；若存在旧 `990xxx` 成果账号，应显示为作废或已删除状态。历史成果应默认归档。

## 上线注意

- 建议停后端服务或至少暂停用户操作后执行，避免迁移时有人提交/审核。
- 先备份数据库，再执行迁移。
- 数据库客户端必须使用 `utf8mb4`，否则中文脚本可能导致乱码。
- 这些脚本面向 MySQL 8 执行环境；`2026-05-31-performance-notice-zh.sql` 使用了窗口函数。
- 执行完成后再启动新后端代码。
