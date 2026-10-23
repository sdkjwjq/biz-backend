-- 暂时全校查阅：仅处理已配置的校级领导，以及确认的三个账号。
-- 必须停服务后执行；不将普通 role=2 部门负责人自动识别为校级领导。
-- 备份只建立一次；后续重复执行不覆盖管理员的新配置。
CREATE TABLE IF NOT EXISTS bak_20261023_leadership AS
SELECT u.user_id, IF(a.user_id IS NULL,0,1) AS existed, a.scope, a.version,
       a.update_by, a.update_time, CAST(NULL AS SIGNED) AS applied_version
FROM sys_user u LEFT JOIN sys_leadership_access a ON a.user_id=u.user_id
WHERE u.status='1' AND COALESCE(u.is_delete,0)=0
  AND (a.scope IN('ALL','DEPARTMENTS') OR u.user_id IN(111651,110007,121552));
CREATE TABLE IF NOT EXISTS bak_20261023_leadership_department AS
SELECT d.* FROM sys_leadership_department d JOIN bak_20261023_leadership b ON b.user_id=d.user_id;

START TRANSACTION;
DROP TEMPORARY TABLE IF EXISTS leadership_change_guard;
CREATE TEMPORARY TABLE leadership_change_guard(ok INT NOT NULL CHECK(ok=1));
INSERT INTO leadership_change_guard
SELECT IF(COUNT(*)=0,1,0) FROM bak_20261023_leadership b
LEFT JOIN sys_leadership_access a ON a.user_id=b.user_id
WHERE b.applied_version IS NULL AND
 ((b.existed=1 AND (a.user_id IS NULL OR a.version<>b.version)) OR (b.existed=0 AND a.user_id IS NOT NULL));
DROP TEMPORARY TABLE leadership_change_guard;
INSERT INTO sys_leadership_access(user_id,scope,version,update_by)
SELECT user_id,'ALL',COALESCE(version,0)+1,NULL FROM bak_20261023_leadership WHERE applied_version IS NULL
ON DUPLICATE KEY UPDATE scope=VALUES(scope),version=VALUES(version),update_by=NULL,update_time=NOW();
DELETE d FROM sys_leadership_department d JOIN bak_20261023_leadership b ON b.user_id=d.user_id WHERE b.applied_version IS NULL;
UPDATE bak_20261023_leadership b JOIN sys_leadership_access a ON a.user_id=b.user_id
SET b.applied_version=a.version WHERE b.applied_version IS NULL;
COMMIT;
