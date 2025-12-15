/*
 * 数据库名：biz
 */

-- 1. 创建数据库时指定字符集为utf8mb4
DROP DATABASE IF EXISTS `biz`;
CREATE DATABASE `biz` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 使用数据库
USE `biz`;

-- 3. 设置会话字符集（确保当前连接使用utf8mb4）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================================================
-- 第一部分：系统基础支撑模块
-- ==========================================================================

-- 1.1 部门表
-- 循环依赖处理：先创建表，Leader的外键约束 user创建后通过 ALTER 添加
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
                            `dept_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门ID',
                            `dept_name` varchar(50) NOT NULL COMMENT '部门名称',
                            `leader_id` bigint(20) DEFAULT NULL COMMENT '部门负责人ID',
                            `status` char(1) DEFAULT '0' COMMENT '状态 0:正常 1:停用',
                            `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                            `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织机构表';

-- 1.2 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `dept_id` bigint(20) DEFAULT NULL COMMENT '所属部门ID',
                            `user_name` varchar(30) NOT NULL COMMENT '账号',
                            `nick_name` varchar(30) NOT NULL COMMENT '姓名',
                            `email` varchar(50) DEFAULT '' COMMENT '邮箱',
                            `password` varchar(100) DEFAULT '' COMMENT '密码',
                            `role` char(1) DEFAULT '0' COMMENT '角色 0:admin 1:user 2:leader',
                            `status` char(1) DEFAULT '0' COMMENT '状态 0:正常 1:停用',
                            `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                            `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`user_id`),
                            KEY `idx_dept` (`dept_id`),
                            CONSTRAINT `fk_user_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`dept_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 补全 sys_dept 的外键约束 (解决循环依赖)
ALTER TABLE `sys_dept`
    ADD CONSTRAINT `fk_dept_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL;

-- 1.3 统一文件表
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
                            `file_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
                            `file_name` varchar(255) NOT NULL COMMENT '文件名',
                            `file_path` varchar(500) NOT NULL COMMENT '路径',
                            `file_url` varchar(500) NOT NULL COMMENT 'URL',
                            `file_suffix` varchar(20) NOT NULL COMMENT '后缀',
                            `file_size` bigint(20) DEFAULT 0 COMMENT '大小',
                            `upload_by` bigint(20) DEFAULT NULL COMMENT '上传人ID',
                            `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
                            `upload_time` datetime DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`file_id`),
                            CONSTRAINT `fk_file_user` FOREIGN KEY (`upload_by`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统文件表';

-- 1.4 系统通知表
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
                              `notice_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
                              `from_user_id` bigint(20) DEFAULT NULL COMMENT '发起人ID',
                              `to_user_id` bigint(20) NOT NULL COMMENT '接收人ID',
                              `type` char(1) DEFAULT '1' COMMENT '类型',
                              `trigger_event` varchar(50) DEFAULT 'SYSTEM' COMMENT '触发事件',
                              `title` varchar(100) NOT NULL COMMENT '标题',
                              `content` TEXT NOT NULL COMMENT '内容',
                              `source_type` char(1) NOT NULL,
                              `source_id` bigint(20) DEFAULT NULL COMMENT '关联业务ID',
                              `is_read` char(1) DEFAULT '0' COMMENT '阅读状态 0:未读 1:已读',
                              `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`notice_id`),
                              KEY `idx_user_read` (`from_user_id`, `to_user_id`, `is_read`),
                              CONSTRAINT `fk_sup_from` FOREIGN KEY (`from_user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL,
                              CONSTRAINT `fk_sup_to` FOREIGN KEY (`to_user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- 1.5 补充：token黑名单表，用于存放失效token
CREATE TABLE token_blacklist (
                                 token VARCHAR(255) NOT NULL PRIMARY KEY COMMENT '失效token',
                                 expiry_time DATETIME NOT NULL COMMENT '过期时间',
                                 INDEX idx_expiry_time (expiry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='token黑名单表';


-- ==========================================================================
-- 第二部分：项目与任务核心模块
-- ==========================================================================

-- 2.1 项目主表
DROP TABLE IF EXISTS `biz_project`;
CREATE TABLE `biz_project` (
                               `project_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '项目ID',
                               `project_name` varchar(200) NOT NULL COMMENT '项目名称',
                               `project_code` varchar(50) DEFAULT NULL COMMENT '项目编码',
                               `leader_id` bigint(20) DEFAULT NULL COMMENT '项目负责人ID',
                               `start_date` date DEFAULT NULL,
                               `end_date` date DEFAULT NULL,
                               `status` char(1) DEFAULT '1' COMMENT '0:未开始 1:进行中 2:已完成 3:暂停 4:逾期',
                               `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                               `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`project_id`),
                               CONSTRAINT `fk_project_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目信息表';

-- 2.2 项目阶段表
DROP TABLE IF EXISTS `biz_project_phase`;
CREATE TABLE `biz_project_phase` (
                                     `phase_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '阶段ID',
                                     `project_id` bigint(20) NOT NULL COMMENT '项目ID',
                                     `phase_name` varchar(100) NOT NULL COMMENT '阶段名称',
                                     `start_date` date NOT NULL,
                                     `end_date` date NOT NULL,
                                     `is_current` char(1) DEFAULT '0',
                                     `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
                                     PRIMARY KEY (`phase_id`),
                                     CONSTRAINT `fk_phase_project` FOREIGN KEY (`project_id`) REFERENCES `biz_project` (`project_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目阶段表';

-- 2.3 任务分解表
DROP TABLE IF EXISTS `biz_task`;
CREATE TABLE `biz_task` (
                            `task_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
                            `project_id` bigint(20) NOT NULL COMMENT '项目ID',
                            `parent_id` bigint(20) DEFAULT 0 COMMENT '父任务节点ID',
                            `ancestors` varchar(500) DEFAULT '' COMMENT '祖先',
                            `phase` int(4) DEFAULT NULL COMMENT '所属年份',
                            `task_code` varchar(64) DEFAULT NULL COMMENT '任务编号',
                            `task_name` varchar(500) NOT NULL COMMENT '任务名称',
                            `level` int(2) DEFAULT 1 COMMENT '任务层级',

    -- 组织归属
                            `dept_id` bigint(20) NOT NULL COMMENT '归口部门ID',
                            `principal_id` bigint(20) NOT NULL COMMENT '归口负责人ID',
                            `leader_id` bigint(20) DEFAULT NULL COMMENT '任务负责人ID',

    -- 数据需求配置
                            `exp_target` text COMMENT '预期达成情况',
                            `exp_level` varchar(20) DEFAULT NULL COMMENT '预期成果级别（国/省/市）',
                            `exp_effect` text COMMENT '预期效果',
                            `exp_material_desc` text COMMENT '预期过程（佐证）材料清单(文本描述)',
                            `data_type` char(1) DEFAULT '1' COMMENT '数据类型 0:对指标没有影响 1:数值(累加) 2:百分比(取大)',
                            `target_value` decimal(20,4) DEFAULT 0.00 COMMENT '目标值',
                            `current_value` decimal(20,4) DEFAULT 0.00 COMMENT '当前完成值(缓存统计)',
                            `weight` decimal(5,2) DEFAULT 1.00 COMMENT '权重(冗余)',
                            `progress` int(3) DEFAULT 0 COMMENT '任务进度(冗余)',
                            `status` char(1) DEFAULT '0' COMMENT '0:未开始 1:进行中 2:审核中 3:已完成',
                            `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                            `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

                            PRIMARY KEY (`task_id`),
                            KEY `idx_project` (`project_id`),
                            CONSTRAINT `fk_task_project` FOREIGN KEY (`project_id`) REFERENCES `biz_project` (`project_id`) ON DELETE CASCADE,
                            CONSTRAINT `fk_task_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`dept_id`) ON DELETE RESTRICT,
                            CONSTRAINT `fk_task_principal` FOREIGN KEY (`principal_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT,
                            CONSTRAINT `fk_task_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务分解表';

-- ==========================================================================
-- 第三部分：审批流转与数据填报
-- ==========================================================================

-- 3.1 材料提交与审批 (包含数据填报)
DROP TABLE IF EXISTS `biz_material_submission`;
CREATE TABLE `biz_material_submission` (
                                           `sub_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '提交ID',
                                           `task_id` bigint(20) NOT NULL COMMENT '任务ID',
                                           `file_id` bigint(20) NOT NULL COMMENT '文件ID',

    -- 提交时填报的数值
                                           `reported_value` decimal(20,4) DEFAULT 0.00 COMMENT '本次填报完成值',
                                           `data_type` char(1) DEFAULT '1' COMMENT '数据类型 0:对指标没有影响 1:数值(累加) 2:百分比(取大)',
                                           `submit_by` bigint(20) NOT NULL COMMENT '提交人ID',
                                           `submit_dept_id` bigint(20) NOT NULL COMMENT '提交人部门ID',
                                           `manage_dept_id` bigint(20) NOT NULL COMMENT '归口部门ID',
                                           `submit_time` datetime DEFAULT CURRENT_TIMESTAMP,

                                           `file_suffix` varchar(10) NOT NULL COMMENT '后缀',
                                           CONSTRAINT `chk_file_format` CHECK (`file_suffix` IN ('pdf', 'doc', 'docx')),
    -- 0:草稿
    -- 10:待[所在部门]审批
    -- 20:待[归口部门]审批
    -- 30:待[管理员]审批
    -- 40:已归档
    -- -10:被所在部门退回
    -- -20:被归口部门退回
    -- -30:被管理员退回
                                           `flow_status` int(4) DEFAULT 0 COMMENT '流程状态',
                                           `current_handler_id` bigint(20) DEFAULT NULL COMMENT '当前处理人ID',
                                           `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',

                                           PRIMARY KEY (`sub_id`),

                                           CONSTRAINT `fk_sub_file` FOREIGN KEY (`file_id`) REFERENCES `sys_file` (`file_id`) ON DELETE RESTRICT,
                                           CONSTRAINT `fk_sub_user` FOREIGN KEY (`submit_by`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT,
                                           CONSTRAINT `fk_sub_task` FOREIGN KEY (`task_id`) REFERENCES `biz_task` (`task_id`) ON DELETE CASCADE,
                                           CONSTRAINT `fk_sub_handler` FOREIGN KEY (`current_handler_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL,
                                           CONSTRAINT `fk_sub_dept1` FOREIGN KEY (`submit_dept_id`) REFERENCES `sys_dept` (`dept_id`) ON DELETE RESTRICT,
                                           CONSTRAINT `fk_sub_dept2` FOREIGN KEY (`manage_dept_id`) REFERENCES `sys_dept` (`dept_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批流转与填报表';

-- 3.2 审批日志
DROP TABLE IF EXISTS `biz_audit_log`;
CREATE TABLE `biz_audit_log` (
                                 `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                 `sub_id` bigint(20) NOT NULL COMMENT '提交ID',
                                 `operator_id` bigint(20) NOT NULL COMMENT '操作人ID',
                                 `action_type` varchar(20) NOT NULL COMMENT '动作',
    -- 0:草稿
    -- 10:待[所在部门]审批
    -- 20:待[归口部门]审批
    -- 30:待[管理员]审批
    -- 40:已归档
    -- -10:被所在部门退回
    -- -20:被归口部门退回
    -- -30:被管理员退回
                                 `pre_status` int(4) NOT NULL COMMENT '前状态',
                                 `post_status` int(4) NOT NULL COMMENT '后状态',
                                 `comment` varchar(500) NOT NULL COMMENT '意见',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`log_id`),
                                 CONSTRAINT `fk_log_op` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批日志表';


-- ==========================================================================
-- 第四部分：绩效与资金模块
-- ==========================================================================

-- 4.1 绩效指标表
DROP TABLE IF EXISTS `biz_performance`;
CREATE TABLE `biz_performance` (
                                   `perf_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '指标ID',
                                   `project_id` bigint(20) NOT NULL,
                                   `parent_id` bigint(20) DEFAULT 0,
                                   `ancestors` varchar(500) DEFAULT '',
                                   `perf_code` varchar(50) DEFAULT NULL COMMENT '编码',
                                   `perf_name` varchar(255) NOT NULL COMMENT '名称',

                                   `target_value` decimal(20,4) DEFAULT 0.00 COMMENT '总目标值',
                                   `current_value` decimal(20,4) DEFAULT 0.00 COMMENT '当前完成值(计算得出)',
                                   `data_type` char(1) DEFAULT '1' COMMENT '数据类型 0:对指标没有影响 1:数值(累加) 2:百分比(取大)',

                                   `dept_id` bigint(20) NOT NULL COMMENT '归口部门ID',
                                   `principal_id` bigint(20) NOT NULL COMMENT '归口负责人ID',
                                   `leader_id` bigint(20) DEFAULT NULL COMMENT '任务负责人ID',

                                   `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                   `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`perf_id`),
                                   CONSTRAINT `fk_perf_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`dept_id`) ON DELETE RESTRICT,
                                   CONSTRAINT `fk_perf_principal` FOREIGN KEY (`principal_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效指标库';

-- 4.2 绩效年度分解
DROP TABLE IF EXISTS `biz_performance_year`;
CREATE TABLE `biz_performance_year` (
                                        `year_id` bigint(20) NOT NULL AUTO_INCREMENT,
                                        `perf_id` bigint(20) NOT NULL COMMENT '指标ID',
                                        `year` int(4) NOT NULL,
                                        `target_value` decimal(20,4) DEFAULT 0.00,
                                        `actual_value` decimal(20,4) DEFAULT 0.00 COMMENT '年度实际完成',
                                        `data_type` char(1) DEFAULT '1' COMMENT '数据类型 0:对指标没有影响 1:数值(累加) 2:百分比(取大)',
                                        `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
                                        PRIMARY KEY (`year_id`),
                                        CONSTRAINT `fk_year_perf` FOREIGN KEY (`perf_id`) REFERENCES `biz_performance` (`perf_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效年度表';

-- 4.3 任务-绩效关联
DROP TABLE IF EXISTS `rel_task_performance`;
CREATE TABLE `rel_task_performance` (
                                        `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                        `task_id` bigint(20) NOT NULL,
                                        `perf_id` bigint(20) NOT NULL,
                                        `year_id` bigint(20) NOT NULL,
                                        `weight` decimal(5,2) DEFAULT 1.00 ,
                                        `contribution_value` decimal(20,4) DEFAULT 0.00 COMMENT '该任务为KPI贡献的数值',
                                        `data_type` char(1) DEFAULT '1' COMMENT '数据类型 0:对指标没有影响 1:数值(累加) 2:百分比(取大)',
                                        PRIMARY KEY (`id`),
                                        UNIQUE KEY `uk_tp` (`task_id`, `perf_id`, `year_id`),
                                        CONSTRAINT `fk_rel_task` FOREIGN KEY (`task_id`) REFERENCES `biz_task` (`task_id`) ON DELETE CASCADE,
                                        CONSTRAINT `fk_rel_perf` FOREIGN KEY (`perf_id`) REFERENCES `biz_performance` (`perf_id`) ON DELETE CASCADE,
                                        CONSTRAINT `fk_rel_year` FOREIGN KEY (`year_id`) REFERENCES `biz_performance_year` (`year_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务绩效关联表';


-- ==========================================================================
-- 第五部分：辅助业务模块
-- ==========================================================================

-- 5.1 标志性成果
DROP TABLE IF EXISTS `biz_achievement`;
CREATE TABLE `biz_achievement` (
                                   `ach_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '成果ID',
                                   `project_id` bigint(20) NOT NULL,
                                   `ach_name` varchar(255) NOT NULL,
                                   `level` varchar(20) NOT NULL,
                                   `source_type` char(1) DEFAULT '3',
                                   `source_id` bigint(20) DEFAULT NULL,
                                   `obtain_date` date DEFAULT NULL,
                                   `create_by` bigint(20) DEFAULT NULL COMMENT '创建人ID',
                                   `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`ach_id`),
                                   CONSTRAINT `fk_ach_user` FOREIGN KEY (`create_by`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标志性成果表';

-- ==========================================================================
-- 第六部分：初始化数据
-- ==========================================================================

-- 6.1 插入部门
INSERT INTO `sys_dept` (`dept_name`, `leader_id`, `status`, `is_delete`, `create_time`) VALUES
    ('测试部', NULL, '0', 0, NOW());

-- 6.2 插入管理员用户（密码：admin123，使用MD5加密）
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`) VALUES
    (1, 'admin', '系统管理员', 'admin@example.com', '888888', '0', '0', 0, NOW());

