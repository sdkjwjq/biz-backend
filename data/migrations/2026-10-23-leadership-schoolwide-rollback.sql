-- 停服务后执行；发现上线后管理员又修改过配置时停止，避免覆盖新授权。
START TRANSACTION;
DROP TEMPORARY TABLE IF EXISTS leadership_rollback_guard;
CREATE TEMPORARY TABLE leadership_rollback_guard(ok INT NOT NULL CHECK(ok=1));
INSERT INTO leadership_rollback_guard
SELECT IF(COUNT(*)=0,1,0) FROM bak_20261023_leadership b LEFT JOIN sys_leadership_access a ON a.user_id=b.user_id
WHERE b.applied_version IS NOT NULL AND (a.user_id IS NULL OR a.version<>b.applied_version OR a.scope<>'ALL');
DROP TEMPORARY TABLE leadership_rollback_guard;
DELETE d FROM sys_leadership_department d JOIN bak_20261023_leadership b ON b.user_id=d.user_id WHERE b.applied_version IS NOT NULL;
INSERT INTO sys_leadership_department(user_id,dept_id)
SELECT d.user_id,d.dept_id FROM bak_20261023_leadership_department d JOIN bak_20261023_leadership b ON b.user_id=d.user_id WHERE b.applied_version IS NOT NULL;
UPDATE sys_leadership_access a JOIN bak_20261023_leadership b ON b.user_id=a.user_id
SET a.scope=b.scope,a.version=b.version,a.update_by=b.update_by,a.update_time=b.update_time
WHERE b.existed=1 AND b.applied_version IS NOT NULL;
DELETE a FROM sys_leadership_access a JOIN bak_20261023_leadership b ON b.user_id=a.user_id WHERE b.existed=0 AND b.applied_version IS NOT NULL;
UPDATE bak_20261023_leadership SET applied_version=NULL;
COMMIT;
-- 保留备份表，不删除任何填报或审核数据。
