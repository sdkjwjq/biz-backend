-- 成果模块权限与归档审核迁移
-- 约定：10=待管理员归档，30=已归档，-10=已退回

DROP PROCEDURE IF EXISTS add_biz_achievement_audit_columns;
DELIMITER //
CREATE PROCEDURE add_biz_achievement_audit_columns()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_achievement' AND COLUMN_NAME = 'audit_status'
    ) THEN
        ALTER TABLE `biz_achievement`
            ADD COLUMN `audit_status` int(11) NOT NULL DEFAULT 30 COMMENT '10待管理员归档 30已归档 -10已退回' AFTER `create_by`;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_achievement' AND COLUMN_NAME = 'current_handler_id'
    ) THEN
        ALTER TABLE `biz_achievement`
            ADD COLUMN `current_handler_id` bigint(20) DEFAULT NULL COMMENT '当前成果审核处理人ID' AFTER `audit_status`;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_achievement' AND COLUMN_NAME = 'update_time'
    ) THEN
        ALTER TABLE `biz_achievement`
            ADD COLUMN `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP AFTER `create_time`;
    END IF;
END//
DELIMITER ;
CALL add_biz_achievement_audit_columns();
DROP PROCEDURE IF EXISTS add_biz_achievement_audit_columns;

UPDATE `biz_achievement`
SET `audit_status` = 30,
    `current_handler_id` = NULL
WHERE `audit_status` IS NULL;

CREATE TABLE IF NOT EXISTS `biz_achievement_submission` (
    `sub_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '成果审核单ID',
    `ach_id` bigint(20) NOT NULL COMMENT '成果ID',
    `submit_by` bigint(20) NOT NULL COMMENT '提交人ID',
    `submit_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `flow_status` int(11) NOT NULL DEFAULT 10 COMMENT '10待管理员归档 30已归档 -10已退回',
    `current_handler_id` bigint(20) DEFAULT NULL COMMENT '当前处理人ID',
    `comment` varchar(1000) DEFAULT NULL COMMENT '提交说明或审核意见',
    `is_delete` tinyint(1) DEFAULT 0 COMMENT '0存在 1作废',
    PRIMARY KEY (`sub_id`),
    KEY `idx_ach_submission_handler` (`current_handler_id`, `flow_status`, `is_delete`),
    KEY `idx_ach_submission_ach` (`ach_id`, `flow_status`, `is_delete`),
    CONSTRAINT `fk_ach_submission_ach` FOREIGN KEY (`ach_id`) REFERENCES `biz_achievement` (`ach_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成果归档审核单';

CREATE TABLE IF NOT EXISTS `biz_achievement_audit_log` (
    `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '成果审核日志ID',
    `sub_id` bigint(20) NOT NULL COMMENT '成果审核单ID',
    `operator_id` bigint(20) NOT NULL COMMENT '操作人ID',
    `action_type` varchar(50) NOT NULL COMMENT 'submit/pass/reject',
    `pre_status` int(11) DEFAULT NULL COMMENT '操作前状态',
    `post_status` int(11) DEFAULT NULL COMMENT '操作后状态',
    `comment` varchar(1000) DEFAULT NULL COMMENT '意见',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`log_id`),
    KEY `idx_ach_audit_log_sub` (`sub_id`),
    CONSTRAINT `fk_ach_audit_log_submission` FOREIGN KEY (`sub_id`) REFERENCES `biz_achievement_submission` (`sub_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成果归档审核日志';

-- 固定成果上传账号：部门为机电工程学院（dept_id=118），账号统一为8开头。
UPDATE `sys_user`
SET `user_name` = CONCAT('作废-', `user_name`),
    `nick_name` = CONCAT('作废-', `nick_name`),
    `password` = CONCAT('disabled_', `user_id`, '_20260531'),
    `is_delete` = 1,
    `update_time` = NOW()
WHERE `user_id` IN (990201, 990202, 990203)
  AND (`user_name` LIKE '成果上传账号%' OR `nick_name` LIKE '成果上传账号%')
  AND `is_delete` = 0;

INSERT INTO `sys_user` (`user_id`, `dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`, `update_time`)
VALUES
    (800001, 118, '成果上传账号1', '成果上传账号1', '', '800001', '1', '1', 0, NOW(), NOW()),
    (800002, 118, '成果上传账号2', '成果上传账号2', '', '800002', '1', '1', 0, NOW(), NOW()),
    (800003, 118, '成果上传账号3', '成果上传账号3', '', '800003', '1', '1', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    `dept_id` = VALUES(`dept_id`),
    `user_name` = VALUES(`user_name`),
    `nick_name` = VALUES(`nick_name`),
    `email` = VALUES(`email`),
    `password` = VALUES(`password`),
    `role` = VALUES(`role`),
    `status` = VALUES(`status`),
    `is_delete` = 0,
    `update_time` = NOW();
