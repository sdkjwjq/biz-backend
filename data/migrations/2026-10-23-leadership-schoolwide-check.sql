SELECT u.user_id,u.nick_name,u.status,COALESCE(a.scope,'NONE') AS leadership_scope,a.version
FROM sys_user u LEFT JOIN sys_leadership_access a ON a.user_id=u.user_id
WHERE u.user_id IN(111651,110007,121552) OR a.scope IN('ALL','DEPARTMENTS');
SELECT COUNT(*) AS migration_scope_mismatch FROM bak_20261023_leadership b
LEFT JOIN sys_leadership_access a ON a.user_id=b.user_id
WHERE b.applied_version IS NOT NULL AND (a.user_id IS NULL OR a.scope<>'ALL' OR a.version<>b.applied_version);
