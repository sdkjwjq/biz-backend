USE `biz`;
SET NAMES utf8mb4;

/*
 * 成果展示专用账号
 *
 * 规则：
 * 1. 每个部门自动生成 1 个账号。
 * 2. user_id = 990000 + dept_id，例如 dept_id=124 的账号是 990124。
 * 3. user_name 和 nick_name 都使用部门名称，方便识别。
 * 4. password = user_id，例如 990124。
 * 5. role = 1，普通用户；成果接口只要求登录，不要求管理员权限。
 */

-- 部分部门名称超过 30 个字符，先放宽账号名称字段长度。
ALTER TABLE `sys_user`
    MODIFY COLUMN `user_name` varchar(100) NOT NULL COMMENT '账号',
    MODIFY COLUMN `nick_name` varchar(100) NOT NULL COMMENT '姓名';

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
SELECT
    990000 + d.`dept_id` AS `user_id`,
    d.`dept_id`,
    d.`dept_name` AS `user_name`,
    d.`dept_name` AS `nick_name`,
    '' AS `email`,
    CAST(990000 + d.`dept_id` AS CHAR) AS `password`,
    '1' AS `role`,
    '1' AS `status`,
    0 AS `is_delete`,
    NOW() AS `create_time`,
    NOW() AS `update_time`
FROM `sys_dept` d
WHERE d.`is_delete` = 0
ON DUPLICATE KEY UPDATE
    `dept_id` = VALUES(`dept_id`),
    `user_name` = VALUES(`user_name`),
    `nick_name` = VALUES(`nick_name`),
    `password` = VALUES(`password`),
    `role` = VALUES(`role`),
    `status` = VALUES(`status`),
    `is_delete` = VALUES(`is_delete`),
    `update_time` = NOW();

-- 查看生成的账号
SELECT
    u.`user_id` AS `登录账号`,
    u.`password` AS `登录密码`,
    u.`user_name` AS `账号名称`,
    d.`dept_name` AS `对应部门`
FROM `sys_user` u
JOIN `sys_dept` d ON d.`dept_id` = u.`dept_id`
WHERE u.`user_id` BETWEEN 990100 AND 990199
ORDER BY u.`user_id`;
