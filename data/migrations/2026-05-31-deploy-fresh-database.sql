-- 2026-05-31 新电脑完整初始化部署脚本
-- 用法：在 biz-backend 目录执行：
-- mysql --default-character-set=utf8mb4 -u root -p --execute="source data/migrations/2026-05-31-deploy-fresh-database.sql"
--
-- 适用场景：
-- 1. 目标电脑尚未创建 biz 数据库，或确认可以用线上原始快照重新初始化。
-- 2. 需要一次性导入线上原始数据快照，并执行本轮全部迁移/数据修正。
--
-- 注意：
-- 1. data/biz_2026-05-29_154602.sql 是服务器原始数据快照，内部会 CREATE DATABASE biz、USE biz，并 DROP/CREATE 各业务表。
-- 2. 不要在已有重要数据的数据库上执行本脚本。
-- 3. 数据库客户端必须使用 utf8mb4，避免中文脚本乱码。

SET NAMES utf8mb4;

SELECT '00/10 导入服务器原始数据快照' AS migration_step;
source data/biz_2026-05-29_154602.sql;

SELECT '开始执行本轮完整迁移' AS migration_step;
source data/migrations/2026-05-31-deploy-all.sql;

SELECT '新电脑完整初始化完成。' AS migration_step;
