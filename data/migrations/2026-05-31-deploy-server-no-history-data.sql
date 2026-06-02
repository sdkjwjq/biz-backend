-- 2026-05-31 服务器上线迁移脚本（不改历史提交数据版）
-- 用法：在 biz-backend 目录执行：
-- mysql --default-character-set=utf8mb4 -u root -p biz --execute="source data/migrations/2026-05-31-deploy-server-no-history-data.sql"
--
-- 适用场景：
-- 1. 服务器已有线上业务数据。
-- 2. 只上线本轮功能所需的表结构、字段、固定成果账号。
-- 3. 不重写、不删除、不伪造历史填报/审批/日志/消息数据。
--
-- 本脚本刻意不执行以下历史数据修正脚本：
-- - 2026-05-30-performance-relation-year-fix.sql
-- - 2026-05-30-task-progress-cap.sql
-- - 2026-05-30-percent-value-cap.sql
-- - 2026-05-31-performance-notice-zh.sql
-- - 2026-05-31-2025-task-audit-flow-normalize.sql

SET NAMES utf8mb4;

SELECT '01/05 任务审核快照表' AS migration_step;
source data/migrations/2026-05-30-audit-snapshot.sql;

SELECT '02/05 任务审核快照表补充 previous_comment' AS migration_step;
source data/migrations/2026-05-30-audit-snapshot-comment.sql;

SELECT '03/05 绩效手动填报审核表' AS migration_step;
source data/migrations/2026-05-30-performance-audit.sql;

SELECT '04/05 成果归档审核与固定上传账号' AS migration_step;
source data/migrations/2026-05-31-achievement-audit.sql;

SELECT '05/05 预算Excel导入表结构' AS migration_step;
source data/migrations/2026-05-31-budget.sql;

SELECT '服务器上线迁移完成：已跳过历史提交数据清理/重写类脚本。' AS migration_step;
