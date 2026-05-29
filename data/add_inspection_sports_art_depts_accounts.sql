USE `biz`;
SET NAMES utf8mb4;

-- Add missing departments and their department accounts.
--
-- 140: 党委巡察办 -> account 990140
-- 141: 体育与艺术教学部 -> account 990141

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
VALUES
    (140, '党委巡察办', NULL, '0', 0, NOW(), NOW()),
    (141, '体育与艺术教学部', NULL, '0', 0, NOW(), NOW())
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
VALUES
    (990140, 140, '党委巡察办', '党委巡察办', '', '990140', '1', '1', 0, NOW(), NOW()),
    (990141, 141, '体育与艺术教学部', '体育与艺术教学部', '', '990141', '1', '1', 0, NOW(), NOW())
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
LEFT JOIN `sys_user` u ON u.`dept_id` = d.`dept_id`
WHERE d.`dept_id` IN (140, 141)
  AND u.`user_id` IN (990140, 990141)
ORDER BY d.`dept_id`;

-- Manually choose one:
-- COMMIT;
-- ROLLBACK;
