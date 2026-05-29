USE `biz`;
SET NAMES utf8mb4;

-- Add department and its department account.
-- Department: 招生就业处、校友工作办公室
-- dept_id: 139
-- account: 990139

START TRANSACTION;

INSERT INTO `sys_dept` (
    `dept_id`,
    `dept_name`,
    `leader_id`,
    `status`,
    `is_delete`,
    `create_time`,
    `update_time`
)
VALUES (
    139,
    '招生就业处、校友工作办公室',
    NULL,
    '0',
    0,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    `dept_name` = VALUES(`dept_name`),
    `status` = VALUES(`status`),
    `is_delete` = VALUES(`is_delete`),
    `update_time` = NOW();

INSERT INTO `sys_user` (
    `user_id`,
    `dept_id`,
    `user_name`,
    `nick_name`,
    `email`,
    `password`,
    `role`,
    `status`,
    `is_delete`,
    `create_time`,
    `update_time`
)
VALUES (
    990139,
    139,
    '招生就业处、校友工作办公室',
    '招生就业处、校友工作办公室',
    '',
    '990139',
    '1',
    '1',
    0,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    `dept_id` = VALUES(`dept_id`),
    `user_name` = VALUES(`user_name`),
    `nick_name` = VALUES(`nick_name`),
    `password` = VALUES(`password`),
    `role` = VALUES(`role`),
    `status` = VALUES(`status`),
    `is_delete` = VALUES(`is_delete`),
    `update_time` = NOW();

SELECT
    d.`dept_id`,
    d.`dept_name`,
    d.`status` AS `dept_status`,
    d.`is_delete` AS `dept_is_delete`,
    u.`user_id`,
    u.`user_name`,
    u.`nick_name`,
    u.`password`,
    u.`role`,
    u.`status` AS `user_status`,
    u.`is_delete` AS `user_is_delete`
FROM `sys_dept` d
LEFT JOIN `sys_user` u ON u.`dept_id` = d.`dept_id` AND u.`user_id` = 990139
WHERE d.`dept_id` = 139;

-- Manually choose one:
-- COMMIT;
-- ROLLBACK;
