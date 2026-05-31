-- 2026-05-31 完整迁移脚本（包含历史数据修正）
-- 用法：在 biz-backend 目录执行：
-- mysql --default-character-set=utf8mb4 -u root -p biz --execute="source data/migrations/2026-05-31-deploy-all.sql"
--
-- 说明：
-- 1. 本脚本只负责按正确顺序编排执行，原子迁移脚本仍保留。
-- 2. 本脚本包含历史数据清理/纠偏/2025审核流规范化，不适合“不改历史提交数据”的线上迁移场景。
-- 3. 执行前必须先备份数据库。
-- 4. 数据库客户端必须使用 utf8mb4，避免中文脚本乱码。

SET NAMES utf8mb4;

SELECT '01/09 任务审核快照表' AS migration_step;
source data/migrations/2026-05-30-audit-snapshot.sql;

SELECT '02/09 任务审核快照表补充 previous_comment' AS migration_step;
source data/migrations/2026-05-30-audit-snapshot-comment.sql;

SELECT '03/09 绩效手动填报审核表' AS migration_step;
source data/migrations/2026-05-30-performance-audit.sql;

SELECT '04/09 绩效年度目标、年度关联、未来填报数据清理' AS migration_step;
source data/migrations/2026-05-30-performance-relation-year-fix.sql;

SELECT '05/09 任务进度封顶到100%' AS migration_step;
source data/migrations/2026-05-30-task-progress-cap.sql;

SELECT '06/09 百分比类型数据封顶到100' AS migration_step;
source data/migrations/2026-05-30-percent-value-cap.sql;

SELECT '07/09 绩效审核消息中文化与清理' AS migration_step;
source data/migrations/2026-05-31-performance-notice-zh.sql;

SELECT '08/09 2025年任务审核流规范化' AS migration_step;
source data/migrations/2026-05-31-2025-task-audit-flow-normalize.sql;

SELECT '09/09 成果归档审核与固定上传账号' AS migration_step;
source data/migrations/2026-05-31-achievement-audit.sql;

SELECT '2026-05-31 数据库迁移脚本执行完成，请按 README-2026-05-31-deploy.md 执行检查 SQL。' AS migration_step;
