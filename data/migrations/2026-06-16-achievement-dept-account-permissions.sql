-- Achievement permissions: department accounts upload, six named users audit.
-- Department account rule: user_id = 990000 + dept_id.

SET NAMES utf8mb4;

-- 1. Verify six achievement auditors.
SELECT user_id, dept_id, user_name, nick_name, role, status, is_delete
FROM sys_user
WHERE user_id IN (110050, 110228, 110038, 110750, 112327, 110279)
ORDER BY user_id;

-- 2. Create missing department accounts.
INSERT INTO sys_user (
    user_id,
    dept_id,
    user_name,
    nick_name,
    email,
    password,
    role,
    status,
    is_delete,
    create_time,
    update_time
)
SELECT
    990000 + d.dept_id,
    d.dept_id,
    d.dept_name,
    d.dept_name,
    '',
    CAST(990000 + d.dept_id AS CHAR),
    '1',
    '1',
    0,
    NOW(),
    NOW()
FROM sys_dept d
LEFT JOIN sys_user u ON u.user_id = 990000 + d.dept_id
WHERE COALESCE(d.is_delete, 0) = 0
  AND u.user_id IS NULL;

-- 3. Repair existing department accounts if they were disabled or renamed.
UPDATE sys_user u
JOIN sys_dept d ON u.user_id = 990000 + d.dept_id
SET
    u.dept_id = d.dept_id,
    u.user_name = d.dept_name,
    u.nick_name = d.dept_name,
    u.password = CAST(990000 + d.dept_id AS CHAR),
    u.role = '1',
    u.status = '1',
    u.is_delete = 0,
    u.update_time = NOW()
WHERE COALESCE(d.is_delete, 0) = 0;

-- 4. Keep the three fixed achievement upload accounts enabled.
INSERT INTO sys_user (
    user_id,
    dept_id,
    user_name,
    nick_name,
    email,
    password,
    role,
    status,
    is_delete,
    create_time,
    update_time
) VALUES
    (800001, 118, '成果上传账号1', '成果上传账号1', '', '800001', '1', '1', 0, NOW(), NOW()),
    (800002, 118, '成果上传账号2', '成果上传账号2', '', '800002', '1', '1', 0, NOW(), NOW()),
    (800003, 118, '成果上传账号3', '成果上传账号3', '', '800003', '1', '1', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    dept_id = VALUES(dept_id),
    user_name = VALUES(user_name),
    nick_name = VALUES(nick_name),
    password = VALUES(password),
    role = VALUES(role),
    status = VALUES(status),
    is_delete = VALUES(is_delete),
    update_time = NOW();

-- 5. Verification queries.
SELECT
    d.dept_id,
    d.dept_name,
    u.user_id AS dept_account_id,
    u.user_name,
    u.status,
    u.is_delete
FROM sys_dept d
LEFT JOIN sys_user u ON u.user_id = 990000 + d.dept_id
WHERE COALESCE(d.is_delete, 0) = 0
ORDER BY d.dept_id;

SELECT user_id, dept_id, user_name, nick_name, status, is_delete
FROM sys_user
WHERE user_id IN (800001, 800002, 800003)
ORDER BY user_id;
