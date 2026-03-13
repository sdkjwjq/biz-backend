# SpringBoot项目代码汇总

生成时间: 2026-03-13 15:24:11
项目目录: `D:\college\0workspace\biz-backend\biz-backend`

---

## 文件目录

- [data\biz.sql](#data\biz-sql)
- [data\insert.sql](#data\insert-sql)
- [data\insert_performance.sql](#data\insert_performance-sql)
- [pom.xml](#pom-xml)
- [README.md](#readme-md)
- [src\main\code_summary.md](#src\main\code_summary-md)
- [src\main\java\org\example\BizApplication.java](#src\main\java\org\example\bizapplication-java)
- [src\main\java\org\example\config\JacksonConfig.java](#src\main\java\org\example\config\jacksonconfig-java)
- [src\main\java\org\example\config\JwtInterceptor.java](#src\main\java\org\example\config\jwtinterceptor-java)
- [src\main\java\org\example\config\UserRoleInterceptor.java](#src\main\java\org\example\config\userroleinterceptor-java)
- [src\main\java\org\example\config\WebConfig.java](#src\main\java\org\example\config\webconfig-java)
- [src\main\java\org\example\controller\AchievementController.java](#src\main\java\org\example\controller\achievementcontroller-java)
- [src\main\java\org\example\controller\BizController.java](#src\main\java\org\example\controller\bizcontroller-java)
- [src\main\java\org\example\controller\DashboardController.java](#src\main\java\org\example\controller\dashboardcontroller-java)
- [src\main\java\org\example\controller\PerformanceController.java](#src\main\java\org\example\controller\performancecontroller-java)
- [src\main\java\org\example\controller\ScheduledTaskController.java](#src\main\java\org\example\controller\scheduledtaskcontroller-java)
- [src\main\java\org\example\controller\SysController.java](#src\main\java\org\example\controller\syscontroller-java)
- [src\main\java\org\example\controller\TrendDataController.java](#src\main\java\org\example\controller\trenddatacontroller-java)
- [src\main\java\org\example\entity\BizAchievement.java](#src\main\java\org\example\entity\bizachievement-java)
- [src\main\java\org\example\entity\BizAuditLog.java](#src\main\java\org\example\entity\bizauditlog-java)
- [src\main\java\org\example\entity\BizMaterialSubmission.java](#src\main\java\org\example\entity\bizmaterialsubmission-java)
- [src\main\java\org\example\entity\BizPerformance.java](#src\main\java\org\example\entity\bizperformance-java)
- [src\main\java\org\example\entity\BizPerformanceYear.java](#src\main\java\org\example\entity\bizperformanceyear-java)
- [src\main\java\org\example\entity\BizProject.java](#src\main\java\org\example\entity\bizproject-java)
- [src\main\java\org\example\entity\BizProjectPhase.java](#src\main\java\org\example\entity\bizprojectphase-java)
- [src\main\java\org\example\entity\BizTask.java](#src\main\java\org\example\entity\biztask-java)
- [src\main\java\org\example\entity\BizTrendData.java](#src\main\java\org\example\entity\biztrenddata-java)
- [src\main\java\org\example\entity\dto\BizAuditDTO.java](#src\main\java\org\example\entity\dto\bizauditdto-java)
- [src\main\java\org\example\entity\dto\BizReSubDTO.java](#src\main\java\org\example\entity\dto\bizresubdto-java)
- [src\main\java\org\example\entity\dto\BizSubDTO.java](#src\main\java\org\example\entity\dto\bizsubdto-java)
- [src\main\java\org\example\entity\dto\BizTaskDTO.java](#src\main\java\org\example\entity\dto\biztaskdto-java)
- [src\main\java\org\example\entity\dto\FileUploadDTO.java](#src\main\java\org\example\entity\dto\fileuploaddto-java)
- [src\main\java\org\example\entity\dto\SysAlertDTO.java](#src\main\java\org\example\entity\dto\sysalertdto-java)
- [src\main\java\org\example\entity\dto\SysLoginDTO.java](#src\main\java\org\example\entity\dto\syslogindto-java)
- [src\main\java\org\example\entity\dto\SysNoticeDTO.java](#src\main\java\org\example\entity\dto\sysnoticedto-java)
- [src\main\java\org\example\entity\dto\SysPwdDTO.java](#src\main\java\org\example\entity\dto\syspwddto-java)
- [src\main\java\org\example\entity\dto\SysUserDTO.java](#src\main\java\org\example\entity\dto\sysuserdto-java)
- [src\main\java\org\example\entity\RelTaskPerformance.java](#src\main\java\org\example\entity\reltaskperformance-java)
- [src\main\java\org\example\entity\SysDept.java](#src\main\java\org\example\entity\sysdept-java)
- [src\main\java\org\example\entity\SysFile.java](#src\main\java\org\example\entity\sysfile-java)
- [src\main\java\org\example\entity\SysNotice.java](#src\main\java\org\example\entity\sysnotice-java)
- [src\main\java\org\example\entity\SysUser.java](#src\main\java\org\example\entity\sysuser-java)
- [src\main\java\org\example\entity\TokenBlacklist.java](#src\main\java\org\example\entity\tokenblacklist-java)
- [src\main\java\org\example\entity\vo\BizAuditVO.java](#src\main\java\org\example\entity\vo\bizauditvo-java)
- [src\main\java\org\example\entity\vo\BizTaskVo.java](#src\main\java\org\example\entity\vo\biztaskvo-java)
- [src\main\java\org\example\entity\vo\DashboardSummaryVO.java](#src\main\java\org\example\entity\vo\dashboardsummaryvo-java)
- [src\main\java\org\example\entity\vo\DeptTaskStatsVO.java](#src\main\java\org\example\entity\vo\depttaskstatsvo-java)
- [src\main\java\org\example\entity\vo\ErrorVO.java](#src\main\java\org\example\entity\vo\errorvo-java)
- [src\main\java\org\example\entity\vo\FileUploadVO.java](#src\main\java\org\example\entity\vo\fileuploadvo-java)
- [src\main\java\org\example\entity\vo\FirstLevelTaskDetailVO.java](#src\main\java\org\example\entity\vo\firstleveltaskdetailvo-java)
- [src\main\java\org\example\entity\vo\SysLoginVO.java](#src\main\java\org\example\entity\vo\sysloginvo-java)
- [src\main\java\org\example\entity\vo\SysLogoutVO.java](#src\main\java\org\example\entity\vo\syslogoutvo-java)
- [src\main\java\org\example\entity\vo\SysPasswordVO.java](#src\main\java\org\example\entity\vo\syspasswordvo-java)
- [src\main\java\org\example\entity\vo\TaskCompletionVO.java](#src\main\java\org\example\entity\vo\taskcompletionvo-java)
- [src\main\java\org\example\mapper\AchievementMapper.java](#src\main\java\org\example\mapper\achievementmapper-java)
- [src\main\java\org\example\mapper\BizMapper.java](#src\main\java\org\example\mapper\bizmapper-java)
- [src\main\java\org\example\mapper\PerformanceMapper.java](#src\main\java\org\example\mapper\performancemapper-java)
- [src\main\java\org\example\mapper\SysMapper.java](#src\main\java\org\example\mapper\sysmapper-java)
- [src\main\java\org\example\mapper\TokenBlacklistMapper.java](#src\main\java\org\example\mapper\tokenblacklistmapper-java)
- [src\main\java\org\example\mapper\TrendDataMapper.java](#src\main\java\org\example\mapper\trenddatamapper-java)
- [src\main\java\org\example\service\AchievementService.java](#src\main\java\org\example\service\achievementservice-java)
- [src\main\java\org\example\service\BizService.java](#src\main\java\org\example\service\bizservice-java)
- [src\main\java\org\example\service\PerformanceService.java](#src\main\java\org\example\service\performanceservice-java)
- [src\main\java\org\example\service\ScheduledTaskService.java](#src\main\java\org\example\service\scheduledtaskservice-java)
- [src\main\java\org\example\service\SysService.java](#src\main\java\org\example\service\sysservice-java)
- [src\main\java\org\example\service\TrendDataService.java](#src\main\java\org\example\service\trenddataservice-java)
- [src\main\java\org\example\utils\FileUploadUtil.java](#src\main\java\org\example\utils\fileuploadutil-java)
- [src\main\java\org\example\utils\JWTUtil.java](#src\main\java\org\example\utils\jwtutil-java)
- [src\main\resources\application.properties](#src\main\resources\application-properties)
- [部署方法\部署方法.md](#部署方法\部署方法-md)

---

### <a id='data\biz-sql'></a>data\biz.sql

```sql
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
    `comment` varchar(500) DEFAULT '' COMMENT '任务情况描述',
    -- 组织归属
    `leader_id` bigint(20) DEFAULT NULL COMMENT '任务负责人ID',
    `auditor_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
    `principal_id` bigint(20) NOT NULL COMMENT '归口负责人ID',

    `dept_id` bigint(20) NOT NULL COMMENT '归口部门ID',

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

-- 2.4 发展趋势数据表
DROP TABLE IF EXISTS `biz_trend_data`;
CREATE TABLE `biz_trend_data` (
    `data_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '数据ID',
    `year` int(4) NOT NULL COMMENT '所属年份',
    `month` int(2) NOT NULL COMMENT '所属月份',
    `day` int(2) NOT NULL COMMENT '所属日',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    `total_tasks` int(11) DEFAULT 0 COMMENT '当年总任务数',
    `completion_count` int(11) DEFAULT 0 COMMENT '完成数量',
    `completion_rate` decimal(5,2) DEFAULT 0.00 COMMENT '完成率(%)',
    `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',

    PRIMARY KEY (`data_id`),
    KEY `idx_year_month` (`year`, `month`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发展趋势数据表';

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
    -- 10:待[机电审核人]审批
    -- 20:待[部门审核人]审批
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
    -- 10:待[机电审核人]审批
    -- 20:待[部门审核人]审批
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
    `auditor_id` bigint(20) NOT NULL COMMENT '专业群审核人ID',
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
--     类别
--     1.落实立德树人根本任务
--     2.创新产教融合机制
--     3.打造高水平专业群
--     4.建设一流核心课程
--     5.开放优质新形态教材
--     6.建设高水平双师队伍
--     7.建设产教融合实训基地
--     8.构建数字化教学新生态
--     9.拓展国际交流与合作
--     10.打造一流区域型高端智库
    `category` int(4) NOT NULL COMMENT '类别',
--     国家级/省级/市级
    `level` varchar(20) NOT NULL COMMENT '级别',
    `ach_name` varchar(255) NOT NULL COMMENT '成果名称',
    `department` varchar(255) NOT NULL COMMENT '组织部门 如：教育部办公厅等，注意不同于校内部门',
    `got_time` datetime NOT NULL COMMENT '颁发时间',
    `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
    `file_id` bigint(20) DEFAULT NULL COMMENT '佐证材料文件ID',
    `comment` varchar(500) NOT NULL COMMENT '备注',
    `is_competition` tinyint(1) DEFAULT 0 COMMENT '是否竞赛 0:不是竞赛 1:是竞赛',

--     不同类型奖项的数量，为方便区分，使用拼音
    `te_deng_jiang` int(4) DEFAULT 0 COMMENT '特等奖数量',
    `yi_deng_jiang` int(4) DEFAULT 0 COMMENT '一等奖数量',
    `er_deng_jiang` int(4) DEFAULT 0 COMMENT '二等奖数量',
    `san_deng_jiang` int(4) DEFAULT 0 COMMENT '三等奖数量',
    `jin_jiang` int(4) DEFAULT 0 COMMENT '金奖数量',
    `yin_jiang` int(4) DEFAULT 0 COMMENT '银奖数量',
    `tong_jiang` int(4) DEFAULT 0 COMMENT '铜奖数量',
    `you_sheng_jiang` int(4) DEFAULT 0 COMMENT '优胜奖数量',
    `bud_deng_deng_ci` int(4) DEFAULT 0 COMMENT '不定等次数量',

    `create_by` bigint(20) DEFAULT NULL COMMENT '创建人ID',
    `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`ach_id`),
    CONSTRAINT `fk_ach_user` FOREIGN KEY (`create_by`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标志性成果表';


```

---

### data\insert.sql

> 文件过大 (1377751 bytes)，已跳过

### <a id='data\insert_performance-sql'></a>data\insert_performance.sql

```sql
USE `biz`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 插入绩效指标数据 (包含明确的 perf_id)
-- ==========================================================================
-- 第四部分：绩效与资金模块
-- ==========================================================================

-- 4.1 绩效指标表
INSERT INTO `biz_performance` (`perf_id`, `project_id`, `parent_id`, `ancestors`, `perf_code`, `perf_name`, `target_value`, `current_value`, `data_type`, `dept_id`, `principal_id`, `auditor_id`, `leader_id`, `is_delete`, `create_time`, `update_time`) VALUES                                                                                                                                                                                                                                                               (1, 1, 0, '0', '1', '产出指标', 0.0000, 0.0000, '1', 100, 110228, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (2, 1, 0, '0', '2', '效益指标', 0.0000, 0.0000, '1', 100, 110228, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (3, 1, 0, '0', '3', '满意度指标', 0.0000, 0.0000, '1', 100, 110228, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (4, 1, 1, '0,1', '1.1', '数量指标', 0.0000, 0.0000, '1', 100, 110228, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (5, 1, 1, '0,1', '1.2', '质量指标', 0.0000, 0.0000, '1', 100, 110228, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (6, 1, 1, '0,1', '1.3', '时效指标', 0.0000, 0.0000, '1', 100, 110228, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (7, 1, 2, '0,2', '2.1', '社会效益指标', 0.0000, 0.0000, '1', 100, 110228, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (8, 1, 2, '0,2', '2.2', '可持续影响指标', 0.0000, 0.0000, '1', 100, 110228, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (9, 1, 3, '0,3', '3.1', '服务对象满意度指标', 0.0000, 0.0000, '1', 100, 110228, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (10, 1, 4, '0,1,1.1', '1.1.1', '与徐工集团共建工程机械党组织数量', 1.0000, 0.0000, '1', 109, 110009, 110228, 112212, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (11, 1, 4, '0,1,1.1', '1.1.2', '专业思政教育“大师资”中企业专家、劳动模范、大国工匠占比', 40.0000, 0.0000, '2', 106, 110029, 110228, 112212, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (12, 1, 4, '0,1,1.1', '1.1.3', '专业思政育人“新平台”建设整合相关企业展厅、基层党群服务中心数量', 10.0000, 0.0000, '1', 107, 110503, 110228, 112212, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (13, 1, 4, '0,1,1.1', '1.1.4', '打造“淮海战役”精神短视频等红色育人项目数量', 25.0000, 0.0000, '1', 137, 110503, 110228, 112212, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (14, 1, 4, '0,1,1.1', '1.1.5', '构建“云端”思政阵地数量', 1.0000, 0.0000, '1', 107, 110503, 110228, 112212, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (15, 1, 4, '0,1,1.1', '1.1.6', '专业群建设委员会有效整合政府、行业、企业、学校数量', 50.0000, 0.0000, '1', 125, 120311, 110240, 111634, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (16, 1, 4, '0,1,1.1', '1.1.7', '牵头或参加市域产教联合体、行业产教融合育人共同体数量', 5.0000, 0.0000, '1', 125, 120311, 110240, 111634, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (17, 1, 4, '0,1,1.1', '1.1.8', '四技服务经费', 3000.0000, 0.0000, '1', 125, 120311, 110240, 111634, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (18, 1, 4, '0,1,1.1', '1.1.9', '授权或转化知识产权数量', 150.0000, 0.0000, '1', 125, 120311, 110240, 111634, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (19, 1, 4, '0,1,1.1', '1.1.10', '企业参与研制人才培养方案人数占比', 20.0000, 0.0000, '2', 124, 110379, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (20, 1, 4, '0,1,1.1', '1.1.11', '行业、企业导师承担专业课程教学课时占比', 20.0000, 0.0000, '2', 124, 110379, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (21, 1, 4, '0,1,1.1', '1.1.12', '在企业提供场地教学的学时占比', 30.0000, 0.0000, '2', 124, 110379, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (22, 1, 4, '0,1,1.1', '1.1.13', '工程机械课程开发中心数量', 1.0000, 0.0000, '1', 124, 110379, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (23, 1, 4, '0,1,1.1', '1.1.14', '“三基四核六能”进阶式的能力图谱数量', 1.0000, 0.0000, '1', 124, 110379, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (24, 1, 4, '0,1,1.1', '1.1.15', '在线精品课程数量', 100.0000, 0.0000, '1', 124, 110379, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (25, 1, 4, '0,1,1.1', '1.1.16', '在线精品课程每年更新率', 15.0000, 0.0000, '2', 124, 110379, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (26, 1, 4, '0,1,1.1', '1.1.17', '接入数字校园的课堂教学情况覆盖率', 100.0000, 0.0000, '2', 124, 110379, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (27, 1, 4, '0,1,1.1', '1.1.18', '教师运用智慧教育平台资源教学的平均使用率', 90.0000, 0.0000, '2', 124, 110379, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (28, 1, 4, '0,1,1.1', '1.1.19', '具备“分析、增值、画像”功能的智能化评价系统数量', 1.0000, 0.0000, '1', 124, 110379, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (29, 1, 4, '0,1,1.1', '1.1.20', '引入企业真实案例和项目的活页式、工作手册式等新形态教材数量', 50.0000, 0.0000, '1', 124, 110379, 120222, 110264, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (30, 1, 4, '0,1,1.1', '1.1.21', '整合工程机械数字化教学资源的交互式数字化教材数量', 24.0000, 0.0000, '1', 124, 110379, 120222, 110264, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (31, 1, 4, '0,1,1.1', '1.1.22', '每年开展“炼师德、铸师魂”师德师风培训覆盖率', 100.0000, 0.0000, '2', 106, 110029, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (32, 1, 4, '0,1,1.1', '1.1.23', '聘请企业高水平兼职兼课教师数量', 30.0000, 0.0000, '1', 106, 110029, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (33, 1, 4, '0,1,1.1', '1.1.24', '引进或培养博士、教授数量', 20.0000, 0.0000, '1', 106, 110029, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (34, 1, 4, '0,1,1.1', '1.1.25', '培养省级及以上高层次人才数量', 10.0000, 0.0000, '1', 106, 110029, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (35, 1, 4, '0,1,1.1', '1.1.26', '深入企业开展服务的教师数量', 50.0000, 0.0000, '1', 106, 110029, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (36, 1, 4, '0,1,1.1', '1.1.27', '专任教师双师占比', 90.0000, 0.0000, '2', 106, 110029, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (37, 1, 4, '0,1,1.1', '1.1.28', '与徐工集团共建产教虚拟教研室数量', 2.0000, 0.0000, '1', 124, 110379, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (38, 1, 4, '0,1,1.1', '1.1.29', '“教师教学档案袋”在专业教师中的覆盖率', 100.0000, 0.0000, '2', 124, 110379, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (39, 1, 4, '0,1,1.1', '1.1.30', '具备工程机械关键零部件拆装等虚拟仿真功能的生产性实训基地数量', 3.0000, 0.0000, '1', 124, 110379, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (40, 1, 4, '0,1,1.1', '1.1.31', '数字化远程实训教学平台数量', 1.0000, 0.0000, '1', 124, 110379, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (41, 1, 4, '0,1,1.1', '1.1.32', '实践教学评价体系数量', 1.0000, 0.0000, '1', 124, 110379, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (42, 1, 4, '0,1,1.1', '1.1.33', '工程机械培训包数量', 5.0000, 0.0000, '1', 124, 110379, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (43, 1, 4, '0,1,1.1', '1.1.34', '融入教学内容的企业项目案例数量', 50.0000, 0.0000, '1', 124, 110379, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (44, 1, 4, '0,1,1.1', '1.1.35', '技能培训与职业体验数量', 35000.0000, 0.0000, '1', 125, 120311, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (45, 1, 4, '0,1,1.1', '1.1.36', '职业技能评价数量', 5000.0000, 0.0000, '1', 125, 120311, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (46, 1, 4, '0,1,1.1', '1.1.37', '人工智能技术融入专业课程占比', 40.0000, 0.0000, '2', 124, 110379, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (47, 1, 4, '0,1,1.1', '1.1.38', '专业核心课线上试题库覆盖率', 100.0000, 0.0000, '2', 124, 110379, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (48, 1, 4, '0,1,1.1', '1.1.39', '专业教学资源库内容覆盖专业课程内容占比', 80.0000, 0.0000, '2', 124, 110379, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (49, 1, 4, '0,1,1.1', '1.1.40', '企业参与专业群内部质量评价的课程占比', 50.0000, 0.0000, '2', 124, 110379, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (50, 1, 4, '0,1,1.1', '1.1.41', '基于大数据的学生学业评价和教师教学评价管理平台数量', 1.0000, 0.0000, '1', 124, 110379, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (51, 1, 4, '0,1,1.1', '1.1.42', '与徐工集团等企业成立海外工程机械技术学院数量', 1.0000, 0.0000, '1', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (52, 1, 4, '0,1,1.1', '1.1.43', '开展中资企业走出去员工“外文+职业技能”培训数量', 1000.0000, 0.0000, '1', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (53, 1, 4, '0,1,1.1', '1.1.44', '海外中资企业员工“中文+职业技能”培训数量', 1000.0000, 0.0000, '1', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (54, 1, 4, '0,1,1.1', '1.1.45', '与徐工集团合作开发国（境）外的专业标准、课程标准、企业标准等', 10.0000, 0.0000, '1', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (55, 1, 4, '0,1,1.1', '1.1.46', '与徐工集团等企业合作开发国（境）外的培训教材、课程资源包数量', 5.0000, 0.0000, '1', 125, 120311, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (56, 1, 4, '0,1,1.1', '1.1.47', '实施“定向+定制+定点”的国际生培养人数', 200.0000, 0.0000, '1', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (57, 1, 4, '0,1,1.1', '1.1.48', '成立“政企行”多方合作的工程机械产业发展研究院数量', 1.0000, 0.0000, '1', 125, 120311, 110240, 110234, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (58, 1, 4, '0,1,1.1', '1.1.49', '发挥“咨政”“启智”“聚才”“强国”功能，发布《工程机械产业人才需求分析报告》数量', 5.0000, 0.0000, '1', 125, 120311, 110240, 110234, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (59, 1, 4, '0,1,1.1', '1.1.50', '将徐工集团等企业规范、岗位要求转化为教学标准数量', 20.0000, 0.0000, '1', 124, 110379, 110240, 110234, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (60, 1, 4, '0,1,1.1', '1.1.51', '【新增】“双带头人”党支部书记育人工作室数量', 14.0000, 0.0000, '1', 109, 110009, 110228, 112212, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (61, 1, 4, '0,1,1.1', '1.1.52', '【新增】大学生思想政治教育中心暨“大思政课”实践教学基地数量', 1.0000, 0.0000, '1', 124, 110379, 110228, 112212, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (62, 1, 4, '0,1,1.1', '1.1.53', '【新增】产教联合体新加入企业数量', 50.0000, 0.0000, '1', 125, 120311, 110240, 111634, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (63, 1, 4, '0,1,1.1', '1.1.54', '【新增】企业名师工作室数量', 7.0000, 0.0000, '1', 124, 110379, 110240, 111634, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (64, 1, 4, '0,1,1.1', '1.1.55', '【新增】专业群质量评价标准', 1.0000, 0.0000, '1', 124, 110379, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (65, 1, 4, '0,1,1.1', '1.1.56', '【新增】专业群匹配产业链矩阵数量', 7.0000, 0.0000, '1', 124, 110379, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (66, 1, 4, '0,1,1.1', '1.1.57', '【新增】企业参与的新开发课程数量', 9.0000, 0.0000, '1', 124, 110379, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (67, 1, 4, '0,1,1.1', '1.1.58', '【新增】企业实景、直播互动等数智课堂环境', 1.0000, 0.0000, '1', 124, 110379, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (68, 1, 4, '0,1,1.1', '1.1.59', '【新增】“中高本”贯通培养教材数量', 3.0000, 0.0000, '1', 124, 110379, 120222, 110264, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (69, 1, 4, '0,1,1.1', '1.1.60', '【新增】教材管理系列文件', 1.0000, 0.0000, '1', 124, 110379, 120222, 110264, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (70, 1, 4, '0,1,1.1', '1.1.61', '【新增】新建大师工作室个数', 5.0000, 0.0000, '1', 106, 110029, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (71, 1, 4, '0,1,1.1', '1.1.62', '【新增】对外交流教师人次数', 100.0000, 0.0000, '1', 114, 110053, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (72, 1, 4, '0,1,1.1', '1.1.63', '【新增】高端装备智能制造产教融合实践中心', 1.0000, 0.0000, '1', 124, 110379, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (73, 1, 4, '0,1,1.1', '1.1.64', '【新增】典型生产实践项目数量', 25.0000, 0.0000, '1', 124, 110379, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (74, 1, 4, '0,1,1.1', '1.1.65', '【新增】职业启蒙课程数量', 5.0000, 0.0000, '1', 124, 110379, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (75, 1, 4, '0,1,1.1', '1.1.66', '【新增】智能制造专业垂类模型数量', 1.0000, 0.0000, '1', 132, 110472, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (76, 1, 4, '0,1,1.1', '1.1.67', '【新增】新增智慧教室数量', 50.0000, 0.0000, '1', 115, 110101, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (77, 1, 4, '0,1,1.1', '1.1.68', '【新增】智能制造数字化培训包', 1.0000, 0.0000, '1', 124, 110379, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (78, 1, 5, '0,1,1.2', '1.2.1', '国家级课程思政类项目或案例数量', 1.0000, 0.0000, '1', 124, 110379, 110228, 112212, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (79, 1, 5, '0,1,1.2', '1.2.2', '省级高校思想政治工作质量提升工程建设项目验收结果', 1.0000, 0.0000, '0', 109, 110009, 110228, 112212, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (80, 1, 5, '0,1,1.2', '1.2.3', '国家级“样板支部”或“双带头人党支部书记工作室”', 1.0000, 0.0000, '1', 109, 110009, 110228, 112212, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (81, 1, 5, '0,1,1.2', '1.2.4', '国家级“五育”方面项目或获奖', 1.0000, 0.0000, '1', 124, 110379, 110228, 112212, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (82, 1, 5, '0,1,1.2', '1.2.5', '省级产教融合案例数量', 1.0000, 0.0000, '1', 125, 120311, 110240, 111634, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (83, 1, 5, '0,1,1.2', '1.2.6', '国家级市域联合体数量', 1.0000, 0.0000, '1', 125, 120311, 110240, 111634, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (84, 1, 5, '0,1,1.2', '1.2.7', '省级现代产业学院验收结果', 1.0000, 0.0000, '0', 125, 120311, 110240, 111634, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (85, 1, 5, '0,1,1.2', '1.2.8', '服务企业产生经济效益', 10000.0000, 0.0000, '1', 125, 120311, 110240, 111634, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (86, 1, 5, '0,1,1.2', '1.2.9', '企业真实项目转化课时占专业课课时比例', 30.0000, 0.0000, '2', 124, 110379, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (87, 1, 5, '0,1,1.2', '1.2.10', '学徒制合作企业订单学生所占比例', 20.0000, 0.0000, '2', 124, 110379, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (88, 1, 5, '0,1,1.2', '1.2.11', '现代学徒制专业课工学交替课时比例', 30.0000, 0.0000, '2', 124, 110379, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (89, 1, 5, '0,1,1.2', '1.2.12', '学生省级比赛获奖数量', 50.0000, 0.0000, '1', 124, 110379, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (90, 1, 5, '0,1,1.2', '1.2.13', '学生国家级比赛获奖数量', 8.0000, 0.0000, '1', 124, 110379, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (91, 1, 5, '0,1,1.2', '1.2.14', '毕业生本地就业率', 65.0000, 0.0000, '2', 108, 120115, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (92, 1, 5, '0,1,1.2', '1.2.15', '毕业生就业相关度', 85.0000, 0.0000, '2', 108, 120115, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (93, 1, 5, '0,1,1.2', '1.2.16', '省级教学成果奖', 2.0000, 0.0000, '1', 124, 110379, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (94, 1, 5, '0,1,1.2', '1.2.17', '国家级教学成果奖', 1.0000, 0.0000, '1', 124, 110379, 120222, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (95, 1, 5, '0,1,1.2', '1.2.18', '省级课程数量', 4.0000, 0.0000, '1', 124, 110379, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (96, 1, 5, '0,1,1.2', '1.2.19', '国家级课程数量', 2.0000, 0.0000, '1', 124, 110379, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (97, 1, 5, '0,1,1.2', '1.2.20', '省级及以上课堂革命案例数量', 1.0000, 0.0000, '1', 124, 110379, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (98, 1, 5, '0,1,1.2', '1.2.21', '省级教改项目数量', 2.0000, 0.0000, '1', 125, 120311, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (99, 1, 5, '0,1,1.2', '1.2.22', '省级教材数量', 5.0000, 0.0000, '1', 124, 110379, 120222, 110264, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (100, 1, 5, '0,1,1.2', '1.2.23', '国家级教材数量', 3.0000, 0.0000, '1', 124, 110379, 120222, 110264, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (101, 1, 5, '0,1,1.2', '1.2.24', '国家级教材建设奖', 1.0000, 0.0000, '1', 124, 110379, 120222, 110264, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (102, 1, 5, '0,1,1.2', '1.2.25', '省级团队数量', 3.0000, 0.0000, '1', 106, 110029, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (103, 1, 5, '0,1,1.2', '1.2.26', '省级名师数量', 2.0000, 0.0000, '1', 106, 110029, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (104, 1, 5, '0,1,1.2', '1.2.27', '教师省级比赛获奖数量', 20.0000, 0.0000, '1', 124, 110379, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (105, 1, 5, '0,1,1.2', '1.2.28', '教师国家级比赛获奖数量', 2.0000, 0.0000, '1', 124, 110379, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (106, 1, 5, '0,1,1.2', '1.2.29', '国家级团队数量', 1.0000, 0.0000, '1', 106, 110029, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (107, 1, 5, '0,1,1.2', '1.2.30', '国家级名师数量', 2.0000, 0.0000, '1', 106, 110029, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (108, 1, 5, '0,1,1.2', '1.2.31', '国家级课题数量', 2.0000, 0.0000, '1', 125, 120311, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (109, 1, 5, '0,1,1.2', '1.2.32', '工程机械全球数智培训中心获省级及以上实训基地数量', 1.0000, 0.0000, '1', 124, 110379, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (110, 1, 5, '0,1,1.2', '1.2.33', '省级校企合作典型生产实践项目验收结果', 1.0000, 0.0000, '0', 124, 110379, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (111, 1, 5, '0,1,1.2', '1.2.34', '助力合作单位获省级及以上大赛奖励数量', 25.0000, 0.0000, '1', 124, 110379, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (112, 1, 5, '0,1,1.2', '1.2.35', '职业技能评价通过率', 85.0000, 0.0000, '2', 125, 120311, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (113, 1, 5, '0,1,1.2', '1.2.36', '助推企业成为专精特新“小巨人”、瞪羚企业等认定数量', 2.0000, 0.0000, '1', 125, 120311, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (114, 1, 5, '0,1,1.2', '1.2.37', '通过人工智能手段提升教学质量水平', 1.0000, 0.0000, '0', 124, 110379, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (115, 1, 5, '0,1,1.2', '1.2.38', '省级及以上专业教学资源库数量', 1.0000, 0.0000, '1', 124, 110379, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (116, 1, 5, '0,1,1.2', '1.2.39', '人工智能融入教学的省级及以上项目、案例或获奖', 1.0000, 0.0000, '1', 124, 110379, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (117, 1, 5, '0,1,1.2', '1.2.40', '教与学的评价结果和评价过程的质量', 1.0000, 0.0000, '0', 124, 110379, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (118, 1, 5, '0,1,1.2', '1.2.41', '对合作企业本土化人才培养的作用发挥', 1.0000, 0.0000, '0', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (119, 1, 5, '0,1,1.2', '1.2.42', '专业群的国际影响力', 1.0000, 0.0000, '0', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (120, 1, 5, '0,1,1.2', '1.2.43', '合作企业海外就业人数', 300.0000, 0.0000, '1', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (121, 1, 5, '0,1,1.2', '1.2.44', '学生国际大赛获奖数量', 5.0000, 0.0000, '1', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (122, 1, 5, '0,1,1.2', '1.2.45', '发布《工程机械产业发展报告》数量', 5.0000, 0.0000, '1', 125, 120311, 110240, 110234, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (123, 1, 5, '0,1,1.2', '1.2.46', '参与制定国家/国际技术标准数量', 2.0000, 0.0000, '1', 125, 120311, 110240, 110234, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (124, 1, 5, '0,1,1.2', '1.2.47', '牵头或参与制定行业职业技能标准数量', 5.0000, 0.0000, '1', 124, 110379, 110240, 110234, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (125, 1, 5, '0,1,1.2', '1.2.48', '输出到国（境）外的专业标准、课程标准、企业标准等数量', 10.0000, 0.0000, '1', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (126, 1, 5, '0,1,1.2', '1.2.49', '输出到国（境）外的培训教材、课程资源包数量', 5.0000, 0.0000, '1', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (127, 1, 5, '0,1,1.2', '1.2.50', '省级及以上媒体报道', 20.0000, 0.0000, '1', 107, 110503, 110228, 112212, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (128, 1, 5, '0,1,1.2', '1.2.51', '【新增】各类育人基地累计接待校内外人员人次', 30000.0000, 0.0000, '1', 124, 110379, 110228, 112212, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (129, 1, 5, '0,1,1.2', '1.2.52', '【新增】每年订单班、学徒制等培养的学生数', 1000.0000, 0.0000, '1', 124, 110379, 110240, 111634, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (130, 1, 5, '0,1,1.2', '1.2.53', '【新增】“招生-培养-就业-发展”全链条服务学生成长模式', 1.0000, 0.0000, '1', 124, 110379, 110750, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (131, 1, 5, '0,1,1.2', '1.2.54', '【新增】在线课程新增人数', 300000.0000, 0.0000, '1', 124, 110379, 120222, 111586, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (132, 1, 5, '0,1,1.2', '1.2.55', '【新增】产教融合数智赋能的课程教学模式', 1.0000, 0.0000, '0', 124, 110379, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (133, 1, 5, '0,1,1.2', '1.2.56', '【新增】专业教材建设委员会工作机制', 1.0000, 0.0000, '0', 124, 110379, 120222, 110264, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (134, 1, 5, '0,1,1.2', '1.2.57', '【新增】教材建设经验辐射影响', 500.0000, 0.0000, '1', 124, 110379, 120222, 110264, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (135, 1, 5, '0,1,1.2', '1.2.58', '【新增】对外开展教材培训场次', 5.0000, 0.0000, '1', 124, 110379, 120222, 110264, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (136, 1, 5, '0,1,1.2', '1.2.59', '【新增】凡编必审，凡选必审的审核、选用制度及执行情况', 100.0000, 0.0000, '2', 124, 110379, 120222, 110264, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (137, 1, 5, '0,1,1.2', '1.2.60', '【新增】师德师风评价监督体系', 1.0000, 0.0000, '0', 106, 110029, 110279, 110254, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (138, 1, 5, '0,1,1.2', '1.2.61', '【新增】接纳联合体院校学生实习实训人次数量', 5000.0000, 0.0000, '1', 125, 120311, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (139, 1, 5, '0,1,1.2', '1.2.62', '【新增】累计接纳学习交流院校数量', 50.0000, 0.0000, '1', 105, 110009, 110750, 112120, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (140, 1, 5, '0,1,1.2', '1.2.63', '【新增】辐射校内专业垂类大模型数量', 6.0000, 0.0000, '1', 132, 110472, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (141, 1, 5, '0,1,1.2', '1.2.64', '【新增】专业垂类大模型辐射校外院校数量', 20.0000, 0.0000, '1', 132, 110472, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (142, 1, 5, '0,1,1.2', '1.2.65', '【新增】校外使用学校开发的智能体数量', 12.0000, 0.0000, '1', 132, 110472, 110750, 110261, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (143, 1, 5, '0,1,1.2', '1.2.66', '【新增】省级郑和学院数量', 1.0000, 0.0000, '1', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (144, 1, 5, '0,1,1.2', '1.2.67', '【新增】郑和学院当地招生培养人数', 50.0000, 0.0000, '1', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (145, 1, 5, '0,1,1.2', '1.2.68', '【新增】服务走出去企业及其所在国家数量', 3.0000, 0.0000, '1', 114, 110053, 110279, 110260, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (146, 1, 5, '0,1,1.2', '1.2.69', '【新增】牵头或参与制定行业或企业标准数量', 15.0000, 0.0000, '1', 125, 120311, 110240, 110234, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (147, 1, 5, '0,1,1.2', '1.2.70', '【新增】技能人才和技术创新需求清单', 1.0000, 0.0000, '1', 125, 120311, 110240, 110234, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (148, 1, 6, '0,1,1.3', '1.3.1', '任务终期完成度（%）', 100.0000, 0.0000, '2', 100, 110228, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (149, 1, 6, '0,1,1.3', '1.3.2', '收入预算执行率（%）', 100.0000, 0.0000, '2', 104, 110129, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (150, 1, 6, '0,1,1.3', '1.3.3', '支出预算执行率（%）', 100.0000, 0.0000, '2', 104, 110129, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (151, 1, 6, '0,1,1.3', '1.3.4', '年度任务落实率（%）', 100.0000, 0.0000, '2', 100, 110228, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (152, 1, 7, '0,2,2.1', '2.1.1', '专业群每年平均招生人数', 1000.0000, 0.0000, '1', 124, 110379, 110279, 110750, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (153, 1, 7, '0,2,2.1', '2.1.2', '专业群为企业解决发展难题的能力', 1.0000, 0.0000, '0', 125, 120311, 110279, 110750, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (154, 1, 7, '0,2,2.1', '2.1.3', '专业群服务西部高质量发展的贡献度', 1.0000, 0.0000, '0', 105, 110009, 110279, 110750, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (155, 1, 7, '0,2,2.1', '2.1.4', '专业群服务终身教育体系建设的贡献度', 1.0000, 0.0000, '0', 122, 110124, 110279, 110750, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (156, 1, 7, '0,2,2.1', '2.1.5', '专业群建设的示范作用', 1.0000, 0.0000, '0', 124, 110379, 110279, 110750, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (157, 1, 8, '0,2,2.2', '2.2.1', '对专业集群高质量均衡发展的影响', 1.0000, 0.0000, '0', 124, 110379, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (158, 1, 8, '0,2,2.2', '2.2.2', '对促进职业教育内涵发展影响', 1.0000, 0.0000, '0', 124, 110379, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (159, 1, 8, '0,2,2.2', '2.2.3', '对促进技能社会建设的影响', 1.0000, 0.0000, '0', 125, 120311, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (160, 1, 8, '0,2,2.2', '2.2.4', '对产业转型升级的影响', 1.0000, 0.0000, '0', 125, 120311, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (161, 1, 8, '0,2,2.2', '2.2.5', '对提升职业教育国际影响力的影响', 1.0000, 0.0000, '0', 114, 110053, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (162, 1, 8, '0,2,2.2', '2.2.6', '【新增】学校安全管理体系', 1.0000, 0.0000, '0', 101, 110056, 110279, 112327, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (163, 1, 9, '0,3,3.1', '3.1.1', '在校生满意度（%）', 96.0000, 0.0000, '2', 100, 110228, 110279, 110750, 0, CURRENT_TIMESTAMP, NULL),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                (164, 1, 9, '0,3,3.1', '3.1.2', '教职工满意度（%）', 96.0000, 0.0000, '2', 100, 110228, 110279, 110750, 0, CURRENT_TIMESTAMP, NULL);


-- ==========================================================================
-- 第四部分：绩效与资金模块
-- ==========================================================================

-- 4.2 绩效年度分解表
-- 注意：target_value 暂时全部设置为 0，后续根据实际规划更新

-- 指标ID: 10 (与徐工集团共建工程机械党组织数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (10, 2025, 0.0000, 0.0000, '1', 0);

-- 指标ID: 11 (专业思政教育“大师资”中企业专家、劳动模范、大国工匠占比)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (11, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (11, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (11, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (11, 2028, 0.0000, 0.0000, '2', 0),
                                                                                                                     (11, 2029, 0.0000, 0.0000, '2', 0);

-- 指标ID: 12 (专业思政育人“新平台”建设整合相关企业展厅、基层党群服务中心数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (12, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (12, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (12, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (12, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 13 (打造“淮海战役”精神短视频等红色育人项目数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (13, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (13, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (13, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (13, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (13, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 14 (构建“云端”思政阵地数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (14, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (14, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (14, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (14, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (14, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 15 (专业群建设委员会有效整合政府、行业、企业、学校数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (15, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (15, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (15, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (15, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 16 (牵头或参加市域产教联合体、行业产教融合育人共同体数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (16, 2025, 0.0000, 0.0000, '1', 0);

-- 指标ID: 17 (四技服务经费)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (17, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (17, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (17, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (17, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (17, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 18 (授权或转化知识产权数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (18, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (18, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (18, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (18, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (18, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 19 (企业参与研制人才培养方案人数占比)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (19, 2025, 0.0000, 0.0000, '2', 0);

-- 指标ID: 20 (行业、企业导师承担专业课程教学课时占比)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (20, 2026, 0.0000, 0.0000, '2', 0);

-- 指标ID: 21 (在企业提供场地教学的学时占比)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (21, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (21, 2027, 0.0000, 0.0000, '2', 0);

-- 指标ID: 22 (工程机械课程开发中心数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (22, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (22, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (22, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (22, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 23 (“三基四核六能”进阶式的能力图谱数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (23, 2027, 0.0000, 0.0000, '1', 0);

-- 指标ID: 24 (在线精品课程数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (24, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (24, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (24, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (24, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (24, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 25 (在线精品课程每年更新率)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (25, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (25, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (25, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (25, 2028, 0.0000, 0.0000, '2', 0),
                                                                                                                     (25, 2029, 0.0000, 0.0000, '2', 0);

-- 指标ID: 26 (接入数字校园的课堂教学情况覆盖率)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (26, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (26, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (26, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (26, 2028, 0.0000, 0.0000, '2', 0),
                                                                                                                     (26, 2029, 0.0000, 0.0000, '2', 0);

-- 指标ID: 27 (教师运用智慧教育平台资源教学的平均使用率)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (27, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (27, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (27, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (27, 2028, 0.0000, 0.0000, '2', 0),
                                                                                                                     (27, 2029, 0.0000, 0.0000, '2', 0);

-- 指标ID: 28 (具备“分析、增值、画像”功能的智能化评价系统数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (28, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (28, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (28, 2027, 0.0000, 0.0000, '1', 0);

-- 指标ID: 29 (引入企业真实案例和项目的活页式、工作手册式等新形态教材数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (29, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (29, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (29, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (29, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (29, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 30 (整合工程机械数字化教学资源的交互式数字化教材数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (30, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (30, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (30, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (30, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (30, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 31 (每年开展“炼师德、铸师魂”师德师风培训覆盖率)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (31, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (31, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (31, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (31, 2028, 0.0000, 0.0000, '2', 0),
                                                                                                                     (31, 2029, 0.0000, 0.0000, '2', 0);

-- 指标ID: 32 (聘请企业高水平兼职兼课教师数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (32, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (32, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (32, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (32, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (32, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 33 (引进或培养博士、教授数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (33, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (33, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (33, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (33, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (33, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 34 (培养省级及以上高层次人才数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (34, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (34, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (34, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (34, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 35 (深入企业开展服务的教师数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (35, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (35, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (35, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (35, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (35, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 36 (专任教师双师占比)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (36, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (36, 2026, 0.0000, 0.0000, '2', 0);

-- 指标ID: 37 (与徐工集团共建产教虚拟教研室数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (37, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (37, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (37, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (37, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (37, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 38 (“教师教学档案袋”在专业教师中的覆盖率)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (38, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (38, 2027, 0.0000, 0.0000, '2', 0);

-- 指标ID: 39 (具备工程机械关键零部件拆装等虚拟仿真功能的生产性实训基地数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (39, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (39, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (39, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (39, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 40 (数字化远程实训教学平台数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (40, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (40, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (40, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (40, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 41 (实践教学评价体系数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (41, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (41, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (41, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (41, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (41, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 42 (工程机械培训包数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (42, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (42, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (42, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 43 (融入教学内容的企业项目案例数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (43, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (43, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (43, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 44 (技能培训与职业体验数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (44, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (44, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (44, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (44, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (44, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 45 (职业技能评价数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (45, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (45, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (45, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (45, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (45, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 46 (人工智能技术融入专业课程占比)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (46, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (46, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (46, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (46, 2028, 0.0000, 0.0000, '2', 0),
                                                                                                                     (46, 2029, 0.0000, 0.0000, '2', 0);

-- 指标ID: 47 (专业核心课线上试题库覆盖率)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (47, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (47, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (47, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (47, 2028, 0.0000, 0.0000, '2', 0),
                                                                                                                     (47, 2029, 0.0000, 0.0000, '2', 0);

-- 指标ID: 48 (专业教学资源库内容覆盖专业课程内容占比)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (48, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (48, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (48, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (48, 2028, 0.0000, 0.0000, '2', 0),
                                                                                                                     (48, 2029, 0.0000, 0.0000, '2', 0);

-- 指标ID: 49 (企业参与专业群内部质量评价的课程占比)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (49, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (49, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (49, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (49, 2028, 0.0000, 0.0000, '2', 0);

-- 指标ID: 50 (基于大数据的学生学业评价和教师教学评价管理平台数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (50, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (50, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (50, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (50, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (50, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 51 (与徐工集团等企业成立海外工程机械技术学院数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (51, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (51, 2026, 0.0000, 0.0000, '1', 0);

-- 指标ID: 52 (开展中资企业走出去员工“外文+职业技能”培训数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (52, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (52, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (52, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (52, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (52, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 53 (海外中资企业员工“中文+职业技能”培训数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (53, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (53, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (53, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (53, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (53, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 54 (与徐工集团合作开发国（境）外的专业标准、课程标准、企业标准等)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (54, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (54, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (54, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (54, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (54, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 55 (与徐工集团等企业合作开发国（境）外的培训教材、课程资源包数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (55, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (55, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (55, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (55, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (55, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 56 (实施“定向+定制+定点”的国际生培养人数)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (56, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (56, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (56, 2027, 0.0000, 0.0000, '1', 0);

-- 指标ID: 57 (成立“政企行”多方合作的工程机械产业发展研究院数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (57, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (57, 2026, 0.0000, 0.0000, '1', 0);

-- 指标ID: 58 (发挥“咨政”“启智”“聚才”“强国”功能，发布《工程机械产业人才需求分析报告》数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (58, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (58, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (58, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (58, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (58, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 59 (将徐工集团等企业规范、岗位要求转化为教学标准数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (59, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (59, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (59, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (59, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (59, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 60 (【新增】“双带头人”党支部书记育人工作室数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (60, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (60, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (60, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (60, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 61 (【新增】大学生思想政治教育中心暨“大思政课”实践教学基地数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (61, 2026, 0.0000, 0.0000, '1', 0);

-- 指标ID: 62 (【新增】产教联合体新加入企业数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (62, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (62, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (62, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (62, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (62, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 63 (【新增】企业名师工作室数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (63, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (63, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (63, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (63, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (63, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 64 (【新增】专业群质量评价标准)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (64, 2025, 0.0000, 0.0000, '1', 0);

-- 指标ID: 65 (【新增】专业群匹配产业链矩阵数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (65, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (65, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (65, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (65, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (65, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 66 (【新增】企业参与的新开发课程数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (66, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (66, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (66, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (66, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 67 (【新增】企业实景、直播互动等数智课堂环境)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (67, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (67, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (67, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (67, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 68 (【新增】“中高本”贯通培养教材数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (68, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (68, 2027, 0.0000, 0.0000, '1', 0);

-- 指标ID: 69 (【新增】教材管理系列文件)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (69, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (69, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (69, 2027, 0.0000, 0.0000, '1', 0);

-- 指标ID: 70 (【新增】新建大师工作室个数)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (70, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (70, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (70, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (70, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (70, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 71 (【新增】对外交流教师人次数)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (71, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (71, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (71, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (71, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (71, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 72 (【新增】高端装备智能制造产教融合实践中心)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (72, 2027, 0.0000, 0.0000, '1', 0);

-- 指标ID: 73 (【新增】典型生产实践项目数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (73, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (73, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (73, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 74 (【新增】职业启蒙课程数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (74, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (74, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (74, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 75 (【新增】智能制造专业垂类模型数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (75, 2026, 0.0000, 0.0000, '1', 0);

-- 指标ID: 76 (【新增】新增智慧教室数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (76, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (76, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (76, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (76, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (76, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 77 (【新增】智能制造数字化培训包)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (77, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 78 (国家级课程思政类项目或案例数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (78, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (78, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (78, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (78, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (78, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 79 (省级高校思想政治工作质量提升工程建设项目验收结果)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (79, 2025, 0.0000, 0.0000, '0', 0),
                                                                                                                     (79, 2026, 0.0000, 0.0000, '0', 0),
                                                                                                                     (79, 2027, 0.0000, 0.0000, '0', 0),
                                                                                                                     (79, 2028, 0.0000, 0.0000, '0', 0),
                                                                                                                     (79, 2029, 0.0000, 0.0000, '0', 0);

-- 指标ID: 80 (国家级“样板支部”或“双带头人党支部书记工作室”)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (80, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (80, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (80, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (80, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (80, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 81 (国家级“五育”方面项目或获奖)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (81, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (81, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (81, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (81, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (81, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 82 (省级产教融合案例数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (82, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (82, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (82, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (82, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (82, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 83 (国家级市域联合体数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (83, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (83, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (83, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (83, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (83, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 84 (省级现代产业学院验收结果)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (84, 2025, 0.0000, 0.0000, '0', 0),
                                                                                                                     (84, 2026, 0.0000, 0.0000, '0', 0),
                                                                                                                     (84, 2027, 0.0000, 0.0000, '0', 0);

-- 指标ID: 85 (服务企业产生经济效益)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (85, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (85, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (85, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (85, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (85, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 86 (企业真实项目转化课时占专业课课时比例)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (86, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (86, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (86, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (86, 2028, 0.0000, 0.0000, '2', 0),
                                                                                                                     (86, 2029, 0.0000, 0.0000, '2', 0);

-- 指标ID: 87 (学徒制合作企业订单学生所占比例)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (87, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (87, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (87, 2028, 0.0000, 0.0000, '2', 0);

-- 指标ID: 88 (现代学徒制专业课工学交替课时比例)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (88, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (88, 2027, 0.0000, 0.0000, '2', 0);

-- 指标ID: 89 (学生省级比赛获奖数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (89, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (89, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (89, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (89, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (89, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 90 (学生国家级比赛获奖数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (90, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (90, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (90, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (90, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 91 (毕业生本地就业率)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (91, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (91, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (91, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (91, 2028, 0.0000, 0.0000, '2', 0),
                                                                                                                     (91, 2029, 0.0000, 0.0000, '2', 0);

-- 指标ID: 92 (毕业生就业相关度)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (92, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (92, 2028, 0.0000, 0.0000, '2', 0),
                                                                                                                     (92, 2029, 0.0000, 0.0000, '2', 0);

-- 指标ID: 93 (省级教学成果奖)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (93, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (93, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (93, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (93, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (93, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 94 (国家级教学成果奖)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (94, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (94, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (94, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (94, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (94, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 95 (省级课程数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (95, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (95, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (95, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (95, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (95, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 96 (国家级课程数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (96, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (96, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (96, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (96, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (96, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 97 (省级及以上课堂革命案例数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (97, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (97, 2027, 0.0000, 0.0000, '1', 0);

-- 指标ID: 98 (省级教改项目数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (98, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (98, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (98, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (98, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 99 (省级教材数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (99, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (99, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (99, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (99, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (99, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 100 (国家级教材数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (100, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (100, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (100, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (100, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (100, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 101 (国家级教材建设奖)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (101, 2027, 0.0000, 0.0000, '1', 0);

-- 指标ID: 102 (省级团队数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (102, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (102, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (102, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (102, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (102, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 103 (省级名师数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (103, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (103, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (103, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (103, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (103, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 104 (教师省级比赛获奖数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (104, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (104, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (104, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (104, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (104, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 105 (教师国家级比赛获奖数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (105, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (105, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (105, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (105, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 106 (国家级团队数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (106, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (106, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 107 (国家级名师数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (107, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (107, 2027, 0.0000, 0.0000, '1', 0);

-- 指标ID: 108 (国家级课题数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (108, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (108, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 109 (工程机械全球数智培训中心获省级及以上实训基地数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (109, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (109, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (109, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (109, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (109, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 110 (省级校企合作典型生产实践项目验收结果)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (110, 2025, 0.0000, 0.0000, '0', 0),
                                                                                                                     (110, 2026, 0.0000, 0.0000, '0', 0),
                                                                                                                     (110, 2027, 0.0000, 0.0000, '0', 0),
                                                                                                                     (110, 2028, 0.0000, 0.0000, '0', 0);

-- 指标ID: 111 (助力合作单位获省级及以上大赛奖励数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (111, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (111, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (111, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (111, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (111, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 112 (职业技能评价通过率)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (112, 2025, 0.0000, 0.0000, '2', 0),
                                                                                                                     (112, 2026, 0.0000, 0.0000, '2', 0),
                                                                                                                     (112, 2027, 0.0000, 0.0000, '2', 0),
                                                                                                                     (112, 2028, 0.0000, 0.0000, '2', 0),
                                                                                                                     (112, 2029, 0.0000, 0.0000, '2', 0);

-- 指标ID: 113 (助推企业成为专精特新“小巨人”、瞪羚企业等认定数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (113, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (113, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (113, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (113, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (113, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 114 (通过人工智能手段提升教学质量水平)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (114, 2025, 0.0000, 0.0000, '0', 0),
                                                                                                                     (114, 2026, 0.0000, 0.0000, '0', 0),
                                                                                                                     (114, 2027, 0.0000, 0.0000, '0', 0),
                                                                                                                     (114, 2028, 0.0000, 0.0000, '0', 0),
                                                                                                                     (114, 2029, 0.0000, 0.0000, '0', 0);

-- 指标ID: 115 (省级及以上专业教学资源库数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (115, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (115, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (115, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (115, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (115, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 116 (人工智能融入教学的省级及以上项目、案例或获奖)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (116, 2026, 0.0000, 0.0000, '1', 0);

-- 指标ID: 117 (教与学的评价结果和评价过程的质量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (117, 2025, 0.0000, 0.0000, '0', 0),
                                                                                                                     (117, 2026, 0.0000, 0.0000, '0', 0),
                                                                                                                     (117, 2027, 0.0000, 0.0000, '0', 0),
                                                                                                                     (117, 2028, 0.0000, 0.0000, '0', 0),
                                                                                                                     (117, 2029, 0.0000, 0.0000, '0', 0);

-- 指标ID: 118 (对合作企业本土化人才培养的作用发挥)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (118, 2025, 0.0000, 0.0000, '0', 0),
                                                                                                                     (118, 2026, 0.0000, 0.0000, '0', 0),
                                                                                                                     (118, 2027, 0.0000, 0.0000, '0', 0),
                                                                                                                     (118, 2028, 0.0000, 0.0000, '0', 0),
                                                                                                                     (118, 2029, 0.0000, 0.0000, '0', 0);

-- 指标ID: 119 (专业群的国际影响力)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (119, 2025, 0.0000, 0.0000, '0', 0),
                                                                                                                     (119, 2026, 0.0000, 0.0000, '0', 0),
                                                                                                                     (119, 2027, 0.0000, 0.0000, '0', 0),
                                                                                                                     (119, 2028, 0.0000, 0.0000, '0', 0),
                                                                                                                     (119, 2029, 0.0000, 0.0000, '0', 0);

-- 指标ID: 120 (合作企业海外就业人数)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (120, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (120, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (120, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (120, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (120, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 121 (学生国际大赛获奖数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (121, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (121, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (121, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (121, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (121, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 122 (发布《工程机械产业发展报告》数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (122, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (122, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (122, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (122, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (122, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 123 (参与制定国家/国际技术标准数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (123, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (123, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (123, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (123, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 124 (牵头或参与制定行业职业技能标准数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (124, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (124, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (124, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (124, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (124, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 125 (输出到国（境）外的专业标准、课程标准、企业标准等数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (125, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (125, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (125, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (125, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (125, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 126 (输出到国（境）外的培训教材、课程资源包数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (126, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (126, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (126, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (126, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (126, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 127 (省级及以上媒体报道)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (127, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (127, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (127, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (127, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 128 (【新增】各类育人基地累计接待校内外人员人次)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (128, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (128, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (128, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (128, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (128, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 129 (【新增】每年订单班、学徒制等培养的学生数)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (129, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (129, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (129, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 130 (【新增】“招生-培养-就业-发展”全链条服务学生成长模式)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (130, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (130, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (130, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (130, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (130, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 131 (【新增】在线课程新增人数)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (131, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (131, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (131, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (131, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (131, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 132 (【新增】产教融合数智赋能的课程教学模式)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (132, 2025, 0.0000, 0.0000, '0', 0),
                                                                                                                     (132, 2026, 0.0000, 0.0000, '0', 0),
                                                                                                                     (132, 2027, 0.0000, 0.0000, '0', 0),
                                                                                                                     (132, 2028, 0.0000, 0.0000, '0', 0),
                                                                                                                     (132, 2029, 0.0000, 0.0000, '0', 0);

-- 指标ID: 133 (【新增】专业教材建设委员会工作机制)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (133, 2025, 0.0000, 0.0000, '0', 0),
                                                                                                                     (133, 2027, 0.0000, 0.0000, '0', 0);

-- 指标ID: 134 (【新增】教材建设经验辐射影响)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (134, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (134, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (134, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (134, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (134, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 135 (【新增】对外开展教材培训场次)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (135, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (135, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (135, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (135, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (135, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 136 (【新增】凡编必审，凡选必审的审核、选用制度及执行情况)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (136, 2025, 0.0000, 0.0000, '2', 0);

-- 指标ID: 137 (【新增】师德师风评价监督体系)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (137, 2025, 0.0000, 0.0000, '0', 0);

-- 指标ID: 138 (【新增】接纳联合体院校学生实习实训人次数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (138, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (138, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (138, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (138, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 139 (【新增】累计接纳学习交流院校数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (139, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (139, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (139, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (139, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (139, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 140 (【新增】辐射校内专业垂类大模型数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (140, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (140, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (140, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (140, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 141 (【新增】专业垂类大模型辐射校外院校数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (141, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (141, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (141, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (141, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 142 (【新增】校外使用学校开发的智能体数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (142, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (142, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (142, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (142, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 143 (【新增】省级郑和学院数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
    (143, 2025, 0.0000, 0.0000, '1', 0);

-- 指标ID: 144 (【新增】郑和学院当地招生培养人数)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (144, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (144, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (144, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (144, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (144, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 145 (【新增】服务走出去企业及其所在国家数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (145, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (145, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (145, 2028, 0.0000, 0.0000, '1', 0);

-- 指标ID: 146 (【新增】牵头或参与制定行业或企业标准数量)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (146, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (146, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (146, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (146, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (146, 2029, 0.0000, 0.0000, '1', 0);

-- 指标ID: 147 (【新增】技能人才和技术创新需求清单)
INSERT INTO `biz_performance_year` (`perf_id`, `year`, `target_value`, `actual_value`, `data_type`, `is_delete`) VALUES
                                                                                                                     (147, 2025, 0.0000, 0.0000, '1', 0),
                                                                                                                     (147, 2026, 0.0000, 0.0000, '1', 0),
                                                                                                                     (147, 2027, 0.0000, 0.0000, '1', 0),
                                                                                                                     (147, 2028, 0.0000, 0.0000, '1', 0),
                                                                                                                     (147, 2029, 0.0000, 0.0000, '1', 0);
-- ==========================================================================
-- 第四部分：绩效与资金模块
-- ==========================================================================

-- 4.3 任务-绩效关联表
-- 注意：weight 默认设置为 1.00，contribution_value 默认设置为 0.0000
-- 这些值后续可以根据实际情况进行调整

-- 指标ID: 10 (与徐工集团共建工程机械党组织数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (47, 10, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 10 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(48, 10, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 10 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 11 (专业思政教育“大师资”中企业专家、劳动模范、大国工匠占比)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (96, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(100, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(105, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(93, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(94, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(95, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(56, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(55, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(61, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(191, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(193, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(196, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(200, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(50, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(67, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(73, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(215, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(220, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(243, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(74, 11, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 11 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 12 (专业思政育人“新平台”建设整合相关企业展厅、基层党群服务中心数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (97, 12, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 12 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(102, 12, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 12 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(99, 12, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 12 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(104, 12, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 12 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(109, 12, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 12 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 13 (打造“淮海战役”精神短视频等红色育人项目数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (96, 13, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 13 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(100, 13, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 13 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(105, 13, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 13 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(112, 13, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 13 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(115, 13, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 13 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 14 (构建“云端”思政阵地数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (49, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(51, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(93, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(94, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(95, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(97, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(102, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(53, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(54, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(56, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(98, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(99, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(100, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(101, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(59, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(60, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(62, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(103, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(104, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(105, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(106, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(108, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(65, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(66, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(68, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(71, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(72, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(74, 14, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 14 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 15 (专业群建设委员会有效整合政府、行业、企业、学校数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (116, 15, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 15 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(120, 15, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 15 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(125, 15, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 15 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(130, 15, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 15 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(134, 15, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 15 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 16 (牵头或参加市域产教联合体、行业产教融合育人共同体数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (119, 16, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 16 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(120, 16, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 16 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 17 (四技服务经费)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (160, 17, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 17 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(161, 17, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 17 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(167, 17, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 17 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(173, 17, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 17 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(178, 17, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 17 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(183, 17, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 17 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 18 (授权或转化知识产权数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (160, 18, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 18 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(167, 18, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 18 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(173, 18, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 18 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(178, 18, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 18 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(183, 18, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 18 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 19 (企业参与研制人才培养方案人数占比)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (185, 19, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 19 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(203, 19, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 19 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(228, 19, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 19 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 20 (行业、企业导师承担专业课程教学课时占比)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (208, 20, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 20 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 21 (在企业提供场地教学的学时占比)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (191, 21, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 21 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(197, 21, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 21 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 22 (工程机械课程开发中心数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (245, 22, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 22 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(246, 22, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 22 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(250, 22, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 22 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(257, 22, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 22 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(263, 22, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 22 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 23 (“三基四核六能”进阶式的能力图谱数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (260, 23, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 23 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 24 (在线精品课程数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (247, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(248, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(252, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(253, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(254, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(258, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(259, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(261, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(264, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(265, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(266, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(268, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(269, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(270, 24, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 24 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 25 (在线精品课程每年更新率)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (248, 25, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 25 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(253, 25, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 25 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(259, 25, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 25 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(265, 25, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 25 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(270, 25, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 25 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 26 (接入数字校园的课堂教学情况覆盖率)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (275, 26, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 26 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(280, 26, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 26 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(285, 26, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 26 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(289, 26, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 26 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(293, 26, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 26 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 27 (教师运用智慧教育平台资源教学的平均使用率)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (276, 27, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 27 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(281, 27, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 27 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(286, 27, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 27 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(290, 27, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 27 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(294, 27, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 27 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 28 (具备“分析、增值、画像”功能的智能化评价系统数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (295, 28, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 28 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(297, 28, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 28 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(300, 28, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 28 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 29 (引入企业真实案例和项目的活页式、工作手册式等新形态教材数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (309, 29, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 29 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(314, 29, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 29 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(317, 29, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 29 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(322, 29, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 29 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(325, 29, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 29 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 30 (整合工程机械数字化教学资源的交互式数字化教材数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (326, 30, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 30 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(327, 30, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 30 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(328, 30, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 30 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(329, 30, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 30 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(330, 30, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 30 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(331, 30, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 30 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(332, 30, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 30 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(334, 30, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 30 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(336, 30, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 30 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(337, 30, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 30 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 31 (每年开展“炼师德、铸师魂”师德师风培训覆盖率)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (354, 31, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 31 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(358, 31, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 31 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(363, 31, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 31 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(368, 31, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 31 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(373, 31, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 31 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 32 (聘请企业高水平兼职兼课教师数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (378, 32, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 32 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(382, 32, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 32 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(386, 32, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 32 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(391, 32, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 32 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(395, 32, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 32 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 33 (引进或培养博士、教授数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (379, 33, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 33 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(383, 33, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 33 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(387, 33, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 33 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(392, 33, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 33 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(396, 33, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 33 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 34 (培养省级及以上高层次人才数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (383, 34, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 34 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(387, 34, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 34 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(392, 34, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 34 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(396, 34, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 34 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 35 (深入企业开展服务的教师数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (378, 35, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 35 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(382, 35, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 35 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(386, 35, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 35 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(391, 35, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 35 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(395, 35, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 35 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 36 (专任教师双师占比)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (402, 36, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 36 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(407, 36, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 36 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 37 (与徐工集团共建产教虚拟教研室数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (398, 37, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 37 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(403, 37, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 37 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(408, 37, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 37 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(413, 37, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 37 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(417, 37, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 37 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 38 (“教师教学档案袋”在专业教师中的覆盖率)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (399, 38, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 38 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(412, 38, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 38 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 39 (具备工程机械关键零部件拆装等虚拟仿真功能的生产性实训基地数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (421, 39, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 39 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(422, 39, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 39 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(425, 39, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 39 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(426, 39, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 39 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(429, 39, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 39 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(430, 39, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 39 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(431, 39, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 39 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(434, 39, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 39 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(435, 39, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 39 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 40 (数字化远程实训教学平台数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (440, 40, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 40 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(442, 40, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 40 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(445, 40, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 40 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(451, 40, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 40 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 41 (实践教学评价体系数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (441, 41, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 41 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(443, 41, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 41 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(446, 41, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 41 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(448, 41, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 41 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(450, 41, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 41 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 42 (工程机械培训包数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (427, 42, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 42 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(428, 42, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 42 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(432, 42, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 42 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(436, 42, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 42 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 43 (融入教学内容的企业项目案例数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (427, 43, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 43 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(428, 43, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 43 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(432, 43, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 43 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(436, 43, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 43 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 44 (技能培训与职业体验数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (454, 44, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 44 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(459, 44, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 44 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(464, 44, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 44 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(469, 44, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 44 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(473, 44, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 44 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 45 (职业技能评价数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (455, 45, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 45 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(460, 45, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 45 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(465, 45, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 45 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(470, 45, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 45 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(474, 45, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 45 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 46 (人工智能技术融入专业课程占比)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (482, 46, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 46 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(486, 46, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 46 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(490, 46, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 46 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(494, 46, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 46 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(498, 46, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 46 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 47 (专业核心课线上试题库覆盖率)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (479, 47, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 47 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(480, 47, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 47 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(483, 47, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 47 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(484, 47, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 47 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(487, 47, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 47 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(488, 47, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 47 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(491, 47, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 47 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(492, 47, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 47 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(495, 47, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 47 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2'),
(496, 47, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 47 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 48 (专业教学资源库内容覆盖专业课程内容占比)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (501, 48, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 48 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(506, 48, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 48 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(516, 48, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 48 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(524, 48, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 48 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(533, 48, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 48 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 49 (企业参与专业群内部质量评价的课程占比)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (540, 49, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 49 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(542, 49, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 49 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(544, 49, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 49 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(546, 49, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 49 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 50 (基于大数据的学生学业评价和教师教学评价管理平台数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (539, 50, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 50 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(540, 50, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 50 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(297, 50, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 50 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(541, 50, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 50 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(542, 50, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 50 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(300, 50, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 50 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(543, 50, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 50 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(544, 50, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 50 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(545, 50, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 50 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(546, 50, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 50 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(547, 50, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 50 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(548, 50, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 50 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 51 (与徐工集团等企业成立海外工程机械技术学院数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (583, 51, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 51 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(588, 51, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 51 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 52 (开展中资企业走出去员工“外文+职业技能”培训数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (550, 52, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 52 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(554, 52, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 52 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(557, 52, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 52 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(560, 52, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 52 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(563, 52, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 52 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 53 (海外中资企业员工“中文+职业技能”培训数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (551, 53, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 53 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(555, 53, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 53 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(558, 53, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 53 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(561, 53, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 53 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(564, 53, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 53 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 54 (与徐工集团合作开发国（境）外的专业标准、课程标准、企业标准等)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (566, 54, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 54 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(567, 54, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 54 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(568, 54, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 54 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(571, 54, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 54 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(572, 54, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 54 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(574, 54, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 54 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(575, 54, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 54 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(577, 54, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 54 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(578, 54, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 54 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(580, 54, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 54 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(581, 54, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 54 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 55 (与徐工集团等企业合作开发国（境）外的培训教材、课程资源包数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (552, 55, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 55 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(556, 55, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 55 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(559, 55, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 55 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(562, 55, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 55 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(565, 55, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 55 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 56 (实施“定向+定制+定点”的国际生培养人数)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (585, 56, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 56 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(590, 56, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 56 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(594, 56, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 56 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 57 (成立“政企行”多方合作的工程机械产业发展研究院数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (608, 57, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 57 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(612, 57, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 57 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 58 (发挥“咨政”“启智”“聚才”“强国”功能，发布《工程机械产业人才需求分析报告》数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (633, 58, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 58 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(635, 58, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 58 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(637, 58, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 58 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(639, 58, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 58 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(641, 58, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 58 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 59 (将徐工集团等企业规范、岗位要求转化为教学标准数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (632, 59, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 59 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(634, 59, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 59 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(636, 59, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 59 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(638, 59, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 59 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(640, 59, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 59 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 60 (【新增】“双带头人”党支部书记育人工作室数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (52, 60, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 60 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(58, 60, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 60 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(64, 60, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 60 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(70, 60, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 60 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 61 (【新增】大学生思想政治教育中心暨“大思政课”实践教学基地数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (101, 61, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 61 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 62 (【新增】产教联合体新加入企业数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (116, 62, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 62 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(122, 62, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 62 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(127, 62, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 62 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(131, 62, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 62 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(136, 62, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 62 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 63 (【新增】企业名师工作室数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (121, 63, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 63 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(126, 63, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 63 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(129, 63, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 63 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(135, 63, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 63 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(139, 63, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 63 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 64 (【新增】专业群质量评价标准)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (229, 64, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 64 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 65 (【新增】专业群匹配产业链矩阵数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (230, 65, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 65 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(235, 65, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 65 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(238, 65, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 65 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(241, 65, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 65 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(244, 65, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 65 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 66 (【新增】企业参与的新开发课程数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (252, 66, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 66 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(258, 66, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 66 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(264, 66, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 66 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(269, 66, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 66 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 67 (【新增】企业实景、直播互动等数智课堂环境)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (272, 67, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 67 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(277, 67, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 67 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(282, 67, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 67 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(283, 67, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 67 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 68 (【新增】“中高本”贯通培养教材数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (315, 68, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 68 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(318, 68, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 68 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 69 (【新增】教材管理系列文件)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (327, 69, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 69 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(329, 69, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 69 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(331, 69, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 69 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 70 (【新增】新建大师工作室个数)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (378, 70, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 70 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(382, 70, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 70 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(386, 70, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 70 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(391, 70, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 70 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(395, 70, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 70 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 71 (【新增】对外交流教师人次数)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (380, 71, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 71 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(384, 71, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 71 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(388, 71, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 71 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(393, 71, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 71 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(397, 71, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 71 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 72 (【新增】高端装备智能制造产教融合实践中心)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (433, 72, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 72 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 73 (【新增】典型生产实践项目数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (428, 73, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 73 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(432, 73, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 73 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(436, 73, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 73 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 74 (【新增】职业启蒙课程数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (458, 74, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 74 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(463, 74, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 74 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(468, 74, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 74 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 75 (【新增】智能制造专业垂类模型数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (509, 75, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 75 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 76 (【新增】新增智慧教室数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (503, 76, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 76 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(510, 76, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 76 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(518, 76, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 76 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(526, 76, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 76 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(535, 76, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 76 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 77 (【新增】智能制造数字化培训包)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (527, 77, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 77 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 78 (国家级课程思政类项目或案例数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (49, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(50, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(76, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(77, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(79, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(54, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(55, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(61, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(80, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(81, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(82, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(83, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(84, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(66, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(67, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(68, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(86, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(87, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(88, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(73, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(74, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(89, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(90, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(91, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(71, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(72, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(92, 78, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 78 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 79 (省级高校思想政治工作质量提升工程建设项目验收结果)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (93, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(94, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(95, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(96, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(98, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(99, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(100, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(101, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(103, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(104, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(105, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(106, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(109, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(110, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(113, 79, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 79 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0');

-- 指标ID: 80 (国家级“样板支部”或“双带头人党支部书记工作室”)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (47, 80, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 80 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(48, 80, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 80 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(52, 80, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 80 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(53, 80, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 80 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(58, 80, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 80 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(59, 80, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 80 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(64, 80, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 80 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(65, 80, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 80 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(70, 80, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 80 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(71, 80, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 80 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(75, 80, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 80 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 81 (国家级“五育”方面项目或获奖)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (51, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(76, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(78, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(56, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(80, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(81, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(82, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(84, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(60, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(62, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(85, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(87, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(108, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(68, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(91, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(112, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(115, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(226, 81, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 81 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 82 (省级产教融合案例数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (117, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(119, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(141, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(142, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(143, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(144, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(157, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(122, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(123, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(124, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(145, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(146, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(147, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(148, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(163, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(127, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(128, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(129, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(149, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(150, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(131, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(132, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(151, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(152, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(153, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(154, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(136, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(137, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(155, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(156, 82, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 82 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 83 (国家级市域联合体数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (116, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(118, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(141, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(142, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(143, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(144, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(159, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(161, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(122, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(125, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(145, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(146, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(147, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(148, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(166, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(167, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(127, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(151, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(170, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(172, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(173, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(131, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(132, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(134, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(152, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(153, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(154, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(174, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(175, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(177, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(178, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(138, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(155, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(156, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(179, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(180, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(182, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(183, 83, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 83 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 84 (省级现代产业学院验收结果)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (158, 84, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 84 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(164, 84, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 84 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(165, 84, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 84 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(169, 84, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 84 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0');

-- 指标ID: 85 (服务企业产生经济效益)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (160, 85, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 85 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(165, 85, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 85 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(171, 85, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 85 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(176, 85, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 85 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(181, 85, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 85 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 86 (企业真实项目转化课时占专业课课时比例)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (187, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(188, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(203, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(209, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(230, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(189, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(192, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(208, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(235, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(194, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(195, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(213, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(238, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(214, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(241, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(244, 86, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 86 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 87 (学徒制合作企业订单学生所占比例)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (191, 87, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 87 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(193, 87, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 87 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(196, 87, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 87 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(200, 87, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 87 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 88 (现代学徒制专业课工学交替课时比例)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (193, 88, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 88 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(196, 88, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 88 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(197, 88, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 88 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 89 (学生省级比赛获奖数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (207, 89, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 89 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(211, 89, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 89 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(216, 89, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 89 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(221, 89, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 89 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(225, 89, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 89 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 90 (学生国家级比赛获奖数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (207, 90, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 90 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(211, 90, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 90 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(216, 90, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 90 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(221, 90, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 90 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 91 (毕业生本地就业率)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (203, 91, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 91 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(206, 91, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 91 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(208, 91, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 91 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(213, 91, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 91 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(223, 91, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 91 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2'),
(243, 91, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 91 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 92 (毕业生就业相关度)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (215, 92, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 92 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(220, 92, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 92 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(243, 92, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 92 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 93 (省级教学成果奖)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (185, 93, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 93 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(186, 93, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 93 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(190, 93, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 93 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(208, 93, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 93 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(209, 93, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 93 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(210, 93, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 93 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(213, 93, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 93 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(214, 93, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 93 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(198, 93, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 93 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(218, 93, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 93 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(202, 93, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 93 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(223, 93, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 93 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 94 (国家级教学成果奖)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (204, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(228, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(229, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(232, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(233, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(234, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(236, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(237, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(199, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(219, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(239, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(240, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(201, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(218, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(223, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(224, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(226, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(242, 94, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 94 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 95 (省级课程数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (245, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(246, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(247, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(248, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(272, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(273, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(274, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(295, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(252, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(253, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(254, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(277, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(278, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(279, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(297, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(298, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(257, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(258, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(259, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(261, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(282, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(283, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(284, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(300, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(264, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(265, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(266, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(287, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(288, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(289, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(268, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(269, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(270, 95, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 95 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 96 (国家级课程数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (245, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(275, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(276, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(250, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(251, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(252, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(253, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(256, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(280, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(281, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(260, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(285, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(286, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(263, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(264, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(265, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(266, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(290, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(294, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(296, 96, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 96 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 97 (省级及以上课堂革命案例数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (302, 97, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 97 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(304, 97, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 97 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 98 (省级教改项目数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (299, 98, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 98 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(301, 98, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 98 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(303, 98, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 98 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(305, 98, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 98 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 99 (省级教材数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (306, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(307, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(308, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(309, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(339, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(340, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(312, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(315, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(329, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(318, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(331, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(332, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(323, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(324, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(325, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(335, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(350, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(352, 99, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 99 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 100 (国家级教材数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (310, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(326, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(327, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(328, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(338, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(341, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(313, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(314, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(330, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(343, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(344, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(316, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(317, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(318, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(333, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(346, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(347, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(348, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(321, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(322, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(323, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(334, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(336, 100, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 100 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 101 (国家级教材建设奖)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (319, 101, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 101 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 102 (省级团队数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (354, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(355, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(357, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(376, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(377, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(378, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(379, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(380, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(398, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(400, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(401, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(402, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(359, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(361, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(382, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(383, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(384, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(403, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(404, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(406, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(365, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(369, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(385, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(386, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(388, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(389, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(408, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(409, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(410, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(411, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(412, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(371, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(390, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(391, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(392, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(393, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(414, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(415, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(375, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(396, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(418, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(419, 102, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 102 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 103 (省级名师数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (356, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(358, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(360, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(399, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(358, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(360, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(362, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(381, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(384, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(405, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(407, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(363, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(364, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(366, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(367, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(368, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(387, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(410, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(413, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(368, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(370, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(372, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(394, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(395, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(397, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(413, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(416, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(417, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(373, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(374, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(394, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(395, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(397, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(417, 103, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 103 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 104 (教师省级比赛获奖数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (401, 104, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 104 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(406, 104, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 104 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(410, 104, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 104 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(415, 104, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 104 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(419, 104, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 104 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 105 (教师国家级比赛获奖数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (401, 105, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 105 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(406, 105, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 105 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(410, 105, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 105 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(415, 105, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 105 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 106 (国家级团队数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (407, 106, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 106 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(416, 106, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 106 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 107 (国家级名师数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (362, 107, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 107 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(366, 107, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 107 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 108 (国家级课题数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (367, 108, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 108 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(372, 108, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 108 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 109 (工程机械全球数智培训中心获省级及以上实训基地数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (121, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(420, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(421, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(422, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(423, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(440, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(441, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(452, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(453, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(454, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(455, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(126, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(425, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(426, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(427, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(428, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(442, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(443, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(444, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(458, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(460, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(429, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(430, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(431, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(432, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(433, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(445, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(446, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(448, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(463, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(465, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(135, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(434, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(435, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(436, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(437, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(438, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(439, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(448, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(468, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(470, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(139, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(438, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(439, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(450, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(451, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(474, 109, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 109 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 110 (省级校企合作典型生产实践项目验收结果)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (424, 110, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 110 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(427, 110, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 110 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(428, 110, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 110 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(432, 110, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 110 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(436, 110, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 110 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0');

-- 指标ID: 111 (助力合作单位获省级及以上大赛奖励数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (456, 111, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 111 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(461, 111, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 111 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(466, 111, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 111 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(471, 111, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 111 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(475, 111, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 111 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 112 (职业技能评价通过率)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (455, 112, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 112 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2'),
(460, 112, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 112 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '2'),
(465, 112, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 112 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '2'),
(470, 112, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 112 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '2'),
(474, 112, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 112 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 113 (助推企业成为专精特新“小巨人”、瞪羚企业等认定数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (452, 113, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 113 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(459, 113, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 113 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(464, 113, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 113 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(469, 113, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 113 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(473, 113, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 113 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(477, 113, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 113 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 114 (通过人工智能手段提升教学质量水平)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (478, 114, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 114 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(482, 114, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 114 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(502, 114, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 114 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(486, 114, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 114 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(507, 114, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 114 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(490, 114, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 114 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(516, 114, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 114 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(494, 114, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 114 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(524, 114, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 114 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(498, 114, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 114 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0'),
(532, 114, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 114 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0');

-- 指标ID: 115 (省级及以上专业教学资源库数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (479, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(480, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(481, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(499, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(500, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(501, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(483, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(484, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(485, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(504, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(505, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(506, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(487, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(488, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(489, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(514, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(515, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(517, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(491, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(492, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(493, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(522, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(523, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(525, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(495, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(496, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(497, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(531, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(532, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(533, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(534, 115, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 115 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 116 (人工智能融入教学的省级及以上项目、案例或获奖)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (508, 116, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 116 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 117 (教与学的评价结果和评价过程的质量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (539, 117, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 117 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(540, 117, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 117 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(541, 117, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 117 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(542, 117, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 117 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(543, 117, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 117 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(544, 117, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 117 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(545, 117, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 117 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(546, 117, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 117 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(547, 117, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 117 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0'),
(548, 117, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 117 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0');

-- 指标ID: 118 (对合作企业本土化人才培养的作用发挥)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (550, 118, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 118 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(551, 118, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 118 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(554, 118, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 118 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(555, 118, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 118 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(557, 118, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 118 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(558, 118, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 118 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(560, 118, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 118 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(561, 118, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 118 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(563, 118, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 118 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0'),
(564, 118, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 118 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0');

-- 指标ID: 119 (专业群的国际影响力)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (549, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(553, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(566, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(567, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(568, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(583, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(584, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(585, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(586, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(587, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(553, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(571, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(572, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(588, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(589, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(590, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(591, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(592, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(574, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(575, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(594, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(595, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(596, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(597, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(577, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(578, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(599, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(600, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(601, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(602, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(580, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0'),
(581, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0'),
(604, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0'),
(605, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0'),
(606, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0'),
(607, 119, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 119 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0');

-- 指标ID: 120 (合作企业海外就业人数)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (586, 120, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 120 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(591, 120, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 120 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(595, 120, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 120 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(599, 120, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 120 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(600, 120, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 120 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(604, 120, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 120 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(605, 120, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 120 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 121 (学生国际大赛获奖数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (587, 121, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 121 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(592, 121, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 121 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(596, 121, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 121 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(601, 121, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 121 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(606, 121, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 121 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 122 (发布《工程机械产业发展报告》数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (608, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(609, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(610, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(612, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(613, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(635, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(615, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(637, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(617, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(618, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(639, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(620, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(621, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(641, 122, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 122 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 123 (参与制定国家/国际技术标准数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (624, 123, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 123 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(625, 123, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 123 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(627, 123, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 123 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(628, 123, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 123 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(629, 123, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 123 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(630, 123, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 123 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(631, 123, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 123 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 124 (牵头或参与制定行业职业技能标准数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (632, 124, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 124 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(634, 124, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 124 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(636, 124, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 124 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(638, 124, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 124 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(640, 124, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 124 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 125 (输出到国（境）外的专业标准、课程标准、企业标准等数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (566, 125, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 125 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(567, 125, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 125 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(568, 125, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 125 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(571, 125, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 125 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(572, 125, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 125 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(574, 125, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 125 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(575, 125, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 125 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(577, 125, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 125 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(578, 125, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 125 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(580, 125, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 125 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1'),
(581, 125, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 125 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 126 (输出到国（境）外的培训教材、课程资源包数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (552, 126, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 126 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(556, 126, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 126 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(559, 126, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 126 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(527, 126, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 126 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(562, 126, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 126 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(565, 126, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 126 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 127 (省级及以上媒体报道)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (57, 127, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 127 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(444, 127, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 127 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(63, 127, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 127 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(447, 127, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 127 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(597, 127, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 127 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(69, 127, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 127 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(449, 127, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 127 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(602, 127, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 127 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(607, 127, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 127 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 128 (【新增】各类育人基地累计接待校内外人员人次)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (97, 128, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 128 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(102, 128, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 128 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(107, 128, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 128 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(111, 128, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 128 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(114, 128, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 128 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 129 (【新增】每年订单班、学徒制等培养的学生数)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (162, 129, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 129 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(168, 129, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 129 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(184, 129, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 129 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 130 (【新增】“招生-培养-就业-发展”全链条服务学生成长模式)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (231, 130, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 130 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(212, 130, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 130 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(217, 130, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 130 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(222, 130, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 130 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(227, 130, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 130 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 131 (【新增】在线课程新增人数)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (249, 131, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 131 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(255, 131, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 131 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(262, 131, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 131 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(267, 131, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 131 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(271, 131, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 131 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 132 (【新增】产教融合数智赋能的课程教学模式)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (503, 132, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 132 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(510, 132, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 132 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '0'),
(283, 132, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 132 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(518, 132, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 132 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0'),
(526, 132, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 132 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '0'),
(535, 132, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 132 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '0');

-- 指标ID: 133 (【新增】专业教材建设委员会工作机制)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (311, 133, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 133 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0'),
(320, 133, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 133 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '0');

-- 指标ID: 134 (【新增】教材建设经验辐射影响)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (342, 134, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 134 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(345, 134, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 134 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(349, 134, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 134 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(351, 134, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 134 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(353, 134, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 134 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 135 (【新增】对外开展教材培训场次)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (342, 135, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 135 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(345, 135, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 135 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(349, 135, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 135 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(351, 135, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 135 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(353, 135, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 135 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 136 (【新增】凡编必审，凡选必审的审核、选用制度及执行情况)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (342, 136, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 136 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '2');

-- 指标ID: 137 (【新增】师德师风评价监督体系)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (355, 137, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 137 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '0');

-- 指标ID: 138 (【新增】接纳联合体院校学生实习实训人次数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (455, 138, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 138 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(459, 138, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 138 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(463, 138, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 138 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(467, 138, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 138 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(470, 138, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 138 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 139 (【新增】累计接纳学习交流院校数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (457, 139, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 139 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(427, 139, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 139 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(462, 139, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 139 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(467, 139, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 139 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(472, 139, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 139 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(476, 139, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 139 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 140 (【新增】辐射校内专业垂类大模型数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (509, 140, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 140 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(511, 140, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 140 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(519, 140, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 140 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(528, 140, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 140 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(536, 140, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 140 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 141 (【新增】专业垂类大模型辐射校外院校数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (512, 141, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 141 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(520, 141, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 141 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(529, 141, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 141 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(537, 141, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 141 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 142 (【新增】校外使用学校开发的智能体数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (513, 142, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 142 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(521, 142, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 142 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(530, 142, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 142 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(538, 142, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 142 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 143 (【新增】省级郑和学院数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (569, 143, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 143 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 144 (【新增】郑和学院当地招生培养人数)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (570, 144, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 144 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(573, 144, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 144 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(576, 144, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 144 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(579, 144, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 144 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(582, 144, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 144 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 145 (【新增】服务走出去企业及其所在国家数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (593, 145, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 145 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(598, 145, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 145 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(603, 145, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 145 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 146 (【新增】牵头或参与制定行业或企业标准数量)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (624, 146, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 146 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(625, 146, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 146 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(627, 146, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 146 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(629, 146, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 146 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(630, 146, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 146 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');

-- 指标ID: 147 (【新增】技能人才和技术创新需求清单)
INSERT INTO `rel_task_performance` (`task_id`, `perf_id`, `year_id`, `weight`, `contribution_value`, `data_type`) VALUES
                                                                                                                      (611, 147, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 147 AND `year` = 2025 LIMIT 1), 1.00, 0.0000, '1'),
(614, 147, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 147 AND `year` = 2026 LIMIT 1), 1.00, 0.0000, '1'),
(616, 147, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 147 AND `year` = 2027 LIMIT 1), 1.00, 0.0000, '1'),
(619, 147, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 147 AND `year` = 2028 LIMIT 1), 1.00, 0.0000, '1'),
(622, 147, (SELECT `year_id` FROM `biz_performance_year` WHERE `perf_id` = 147 AND `year` = 2029 LIMIT 1), 1.00, 0.0000, '1');



-- ==========================================================================
-- 更新 biz_performance_year 表的 target_value
-- 根据关联任务的目标值和数据类型进行聚合计算
-- ==========================================================================

-- 开启事务，确保数据一致性
START TRANSACTION;

-- 1. 先备份原始数据（可选，建议先执行）
-- CREATE TABLE biz_performance_year_backup_20260307 AS SELECT * FROM biz_performance_year;

-- 2. 更新 target_value
-- 使用子查询计算每个绩效指标每年的目标值
UPDATE biz_performance_year py
    INNER JOIN (
    SELECT
    rtp.perf_id,
    py2.year,
    -- 根据数据类型进行不同的聚合
    CASE p.data_type
    WHEN '1' THEN COALESCE(SUM(t.target_value), 0)  -- 数值累加
    WHEN '2' THEN COALESCE(MAX(t.target_value), 0)  -- 百分比取大
    ELSE p.target_value  -- 类型0：使用绩效指标本身的目标值
    END AS calculated_target
    FROM rel_task_performance rtp
    INNER JOIN biz_performance_year py2 ON rtp.year_id = py2.year_id
    INNER JOIN biz_performance p ON rtp.perf_id = p.perf_id
    INNER JOIN biz_task t ON rtp.task_id = t.task_id
    WHERE t.is_delete = 0  -- 只统计未删除的任务
    GROUP BY rtp.perf_id, py2.year, p.data_type, p.target_value
    ) AS calc ON py.perf_id = calc.perf_id AND py.year = calc.year
    SET py.target_value = calc.calculated_target
WHERE py.is_delete = 0;  -- 只更新未删除的年度记录

-- 3. 特殊处理：对于类型0的指标，如果没有关联任务，保持原值
-- 但通常类型0的指标已经在上面被正确处理了

-- 4. 提交事务
COMMIT;

-- 5. 验证更新结果
-- 查看更新后的数据
SELECT
    p.perf_id,
    p.perf_code,
    p.perf_name,
    p.data_type,
    py.year,
    py.target_value,
    p.target_value AS perf_total_target
FROM biz_performance_year py
         INNER JOIN biz_performance p ON py.perf_id = p.perf_id
ORDER BY p.perf_id, py.year;

SET FOREIGN_KEY_CHECKS = 1;
```

---

### <a id='pom-xml'></a>pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>biz_backend</artifactId>
    <version>1.0-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.2</version>
    </parent>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>3.0.3</version>
        </dependency>

        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
        </dependency>

        <!-- Lombok依赖 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>com.auth0</groupId>
            <artifactId>java-jwt</artifactId>
            <version>4.4.0</version>
        </dependency>

        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.5.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- 配置maven-compiler-plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <encoding>${project.build.sourceEncoding}</encoding>
                    <!-- 重要：配置annotationProcessorPaths -->
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <mainClass>org.example.BizApplication</mainClass>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### <a id='readme-md'></a>README.md

```markdown
# biz-backend 系统接口文档 v2.0

## 项目概述

`biz-backend` 是一个基于 Spring Boot 的业务管理后端系统，提供用户认证、任务管理、文件上传下载、审批流程、通知预警等核心功能，支持部门级任务协作与流程审批。

## 技术栈

- **核心框架**：Spring Boot 3.3.2
- **持久层**：MyBatis 3.0.3
- **数据库**：MySQL 9.1.0
- **认证授权**：JWT (java-jwt 4.4.0)
- **工具类**：Lombok 1.18.30、SLF4J 2.0.13

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 部署步骤

1. **克隆仓库**
   ```bash
   git clone https://github.com/sdkjwjq/biz-backend.git
   cd biz-backend
   ```

2. **配置数据库**
   - 新建 MySQL 数据库（推荐名称：`biz`）
   - 执行 `data/biz.sql` 初始化表结构
   - 执行 `data/insert.sql` 插入初始化数据
   - 修改 `application.properties` 配置数据库连接：
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/biz_db?useSSL=false&serverTimezone=UTC
     spring.datasource.username=root
     spring.datasource.password=your_password
     ```

3. **构建并启动**
   ```bash
   mvn clean package
   java -jar target/biz_backend-1.0-SNAPSHOT.jar
   ```

### 生产环境部署

#### 服务器信息
```
IP地址: 172.19.2.81
用户名: root
密码: Xzcit@xg.2025.8

数据库账号密码:
账号: root
密码: Xxxy@123
```

#### 后端部署
1. 修改数据库配置为生产环境密码
2. 使用 Maven 打包：先后执行 `clean` 和 `package`
3. 将生成的 `target/biz_backend-1.0-SNAPSHOT.jar` 上传到服务器
4. 停止旧进程：`ps aux|grep java` → `kill -9 [PID]`
5. 启动新进程：`nohup java -jar biz_backend-1.0-SNAPSHOT.jar &`

#### 前端部署
1. 修改 `vite.config.js`，将 IP 地址改为服务器 IP
2. 运行 `npm run builds` 生成 `dist` 文件夹
3. 将 `dist` 文件夹内容上传到 `/usr/share/nginx/html` 目录下

### 迭代记录
- 2025-12-08：完成登录功能
- 2025-12-11：完成登录、注销、修改密码
- 2025-12-21：补充实现根据部门ID获取任务、提交审批材料、获取审批单、文件上传/下载、通知发送/查看、审批任务、重新提交审批材料等接口
- 2025-12-26：修改登录方式为user_id登录，修改第一次审批人为AuditorId
- 2026-01-31：新增标志性成果管理、任务管理、定时任务触发接口

## 基础信息

- **基础路径**：无（接口路径已包含具体前缀）
- **认证方式**：JWT Token（登录后获取，请求时通过 `Authorization` 请求头携带）
- **服务地址**：`https://api.example.com`（实际调用时替换为部署的服务地址）
- **示例Token**：`eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxLCJ1c2VyX25hbWUiOiJhZG1pbiIsImV4cCI6MTcxMjEwMzYwMH0.xxxxxx`

## Postman接口文档
https://www.postman.com/litianyi981119/biz/collection/21001135-3309c751-c3ca-4fdb-9d3e-f9aa8529b9c0/

## 接口列表

### 一、系统管理接口

#### 1.1 用户登录
- **接口**：`POST /system/login`
- **描述**：用户登录，返回Token
- **无需认证**：是
- **请求体**：
  ```json
  {
    "user_id": 110228,
    "password": "123456"
  }
  ```
- **响应**：
  ```json
  {
    "nick_name": "系统管理员",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```

#### 1.2 用户注销
- **接口**：`POST /system/logout`
- **描述**：用户注销，Token加入黑名单
- **请求头**：`Authorization: Bearer {token}`
- **响应**：
  ```json
  {
    "message": "注销成功"
  }
  ```

#### 1.3 修改密码
- **接口**：`POST /system/password`
- **描述**：修改当前用户密码
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "new_password": "654321"
  }
  ```
- **响应**：
  ```json
  {
    "new_password": "密码修改成功"
  }
  ```

#### 1.4 获取所有用户
- **接口**：`GET /system/allUsers`
- **描述**：获取系统所有用户信息
- **请求头**：`Authorization: Bearer {token}`
- **响应**：
  ```json
  [
    {
      "userId": 1,
      "deptId": 1,
      "userName": "admin",
      "nickName": "系统管理员",
      "email": "admin@example.com",
      "role": "0",
      "status": "0",
      "isDelete": 0,
      "createTime": "2024-01-01 00:00:00",
      "updateTime": "2024-01-01 00:00:00"
    }
  ]
  ```

#### 1.5 添加用户
- **接口**：`POST /system/users/add`
- **描述**：管理员添加新用户
- **请求头**：`Authorization: Bearer {token}`
- **权限**：仅管理员（role=0）
- **请求体**：
  ```json
  {
    "userId": 110229,
    "deptId": 1,
    "userName": "newuser",
    "nickName": "新用户",
    "email": "new@example.com",
    "password": "123456",
    "role": "1",
    "status": "0",
    "isDelete": 0
  }
  ```
- **响应**：`"用户 新用户 添加成功"`

#### 1.6 更新用户
- **接口**：`POST /system/users/update`
- **描述**：管理员更新用户信息
- **请求头**：`Authorization: Bearer {token}`
- **权限**：仅管理员（role=0）
- **请求体**：同添加用户
- **响应**：`"用户 新用户 更新成功"`

#### 1.7 删除用户
- **接口**：`POST /system/users/delete/{userId}`
- **描述**：管理员删除用户（逻辑删除）
- **请求头**：`Authorization: Bearer {token}`
- **权限**：仅管理员（role=0）
- **路径参数**：`userId`（用户ID）
- **响应**：`"用户 110229 删除成功"`

#### 1.8 获取部门信息
- **接口**：`GET /system/dept/{deptId}`
- **描述**：根据部门ID获取部门信息
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`deptId`（部门ID）
- **响应**：
  ```json
  {
    "deptId": 1,
    "deptName": "技术部",
    "leaderId": 110228,
    "status": "0",
    "isDelete": 0,
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
  ```

### 二、任务管理接口

#### 2.1 获取全量任务数据
- **接口**：`GET /biz/tasks`
- **描述**：根据用户角色获取任务列表
- **请求头**：`Authorization: Bearer {token}`
- **权限规则**：
  - 管理员(role=0)：返回所有任务
  - 普通用户(role=1)：返回本人负责或参与的任务
  - 负责人(role=2)：返回本人归口负责的任务
- **响应**：
  ```json
  [
    {
      "taskId": 1,
      "projectId": 1,
      "parentId": 0,
      "ancestors": "0",
      "phase": 2024,
      "taskCode": "TSK001",
      "taskName": "项目初始化",
      "level": 1,
      "comment": "任务描述",
      "deptId": 1,
      "deptName": "技术部",
      "auditorId": 110228,
      "auditorName": "管理员",
      "principalId": 110228,
      "principalName": "管理员",
      "leaderId": 110228,
      "leaderName": "管理员",
      "expTarget": "完成框架搭建",
      "expLevel": "国家级",
      "expEffect": "提升效率",
      "expMaterialDesc": "文档材料",
      "dataType": "1",
      "targetValue": 100.00,
      "currentValue": 80.00,
      "weight": 0.5,
      "progress": 80,
      "status": "1",
      "isDelete": 0,
      "createTime": "2024-01-01 00:00:00",
      "updateTime": "2024-01-02 00:00:00"
    }
  ]
  ```

#### 2.2 根据ID获取任务
- **接口**：`GET /biz/tasks/{taskId}`
- **描述**：根据任务ID获取任务详情
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`taskId`（任务ID）
- **响应**：单个任务对象（同2.1格式）

#### 2.3 获取所有子任务
- **接口**：`GET /biz/tasks/children`
- **描述**：获取当前任务的所有子任务
- **请求头**：`Authorization: Bearer {token}`
- **查询参数**：`task_id`（父任务ID）
- **响应**：任务列表（同2.1格式）

#### 2.4 获取父任务
- **接口**：`GET /biz/tasks/parent`
- **描述**：获取当前任务的父任务
- **请求头**：`Authorization: Bearer {token}`
- **查询参数**：`task_id`（子任务ID）
- **响应**：单个任务对象（同2.1格式）

#### 2.5 根据部门ID获取任务
- **接口**：`GET /biz/tasks/dept`
- **描述**：根据部门ID获取该部门所有任务
- **请求头**：`Authorization: Bearer {token}`
- **查询参数**：`dept_id`（部门ID）
- **响应**：任务列表（同2.1格式）

#### 2.6 添加任务
- **接口**：`POST /biz/tasks/manage/add`
- **描述**：添加新任务（仅限三级任务）
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "taskId": null,
    "projectId": 1,
    "parentId": 2,
    "ancestors": "0,1,2",
    "phase": 2024,
    "taskCode": "TSK003",
    "taskName": "三级任务示例",
    "level": 3,
    "comment": "任务描述",
    "deptId": 1,
    "auditorId": 110228,
    "principalId": 110228,
    "leaderId": 110229,
    "expTarget": "完成具体实施",
    "expLevel": "省级",
    "expEffect": "实现目标",
    "expMaterialDesc": "实施材料",
    "dataType": "1",
    "targetValue": 50.00,
    "currentValue": 0.00,
    "weight": 0.3,
    "progress": 0,
    "status": "0"
  }
  ```
- **响应**：`"任务三级任务示例添加成功"`

#### 2.7 更新任务
- **接口**：`POST /biz/tasks/manage/update`
- **描述**：更新任务信息
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：同添加任务（需包含taskId）
- **响应**：`"任务三级任务示例更新成功"`

#### 2.8 完成任务
- **接口**：`POST /biz/tasks/manage/finish/{taskId}`
- **描述**：管理员标记任务为完成状态
- **请求头**：`Authorization: Bearer {token}`
- **权限**：仅管理员（role=0）
- **路径参数**：`taskId`（任务ID）
- **响应**：`"任务三级任务示例已完成"`

### 三、审批流程接口

#### 3.1 提交审批材料
- **接口**：`POST /biz/submit`
- **描述**：提交任务审批材料，启动审批流程
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "task_id": 3,
    "reported_value": 100,
    "data_type": "1",
    "file_id": 1,
    "comment": "备注信息"
  }
  ```
- **响应**：`"提交成功，下一位审批人是张三"`

#### 3.2 获取审批单
- **接口**：`GET /biz/audit/{taskId}`
- **描述**：根据任务ID获取当前用户的审批单
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`taskId`（任务ID）
- **响应**：
  ```json
  [
    {
      "subId": 1,
      "taskId": 3,
      "fileId": 1,
      "filename": "report.pdf",
      "reportedValue": 100.00,
      "dataType": "1",
      "submitBy": 110229,
      "submitDeptId": 1,
      "manageDeptId": 1,
      "submitTime": "2024-01-05 00:00:00",
      "fileSuffix": "pdf",
      "flowStatus": 10,
      "currentHandlerId": 110228,
      "isDelete": 0
    }
  ]
  ```

#### 3.3 获取待我审批列表
- **接口**：`GET /biz/audit/todo`
- **描述**：获取当前用户待处理的审批单
- **请求头**：`Authorization: Bearer {token}`
- **响应**：审批单列表（同3.2格式）

#### 3.4 获取任务全部审批单
- **接口**：`GET /biz/audit/task/{taskId}`
- **描述**：获取指定任务的全部审批记录
- **请求头**：`Authorization: Bearer {token}`
- **权限**：管理员/任务负责人/归口负责人/提交人
- **路径参数**：`taskId`（任务ID）
- **响应**：审批单列表（同3.2格式）

#### 3.5 获取审批操作日志
- **接口**：`GET /biz/audit/logs/{subId}`
- **描述**：获取审批单的操作日志
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`subId`（提交ID）
- **响应**：
  ```json
  [
    {
      "logId": 1,
      "subId": 1,
      "operatorId": 110229,
      "actionType": "提交",
      "preStatus": 0,
      "postStatus": 10,
      "comment": "提交任务",
      "createTime": "2024-01-05 00:00:00"
    }
  ]
  ```

#### 3.6 审批操作
- **接口**：`POST /biz/audit`
- **描述**：审批通过或退回
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "sub_id": 1,
    "is_pass": true,
    "title": "审批意见标题",
    "content": "详细审批意见"
  }
  ```
- **响应**：
  - 通过：`"已审批，下一位审批人为李四"`
  - 退回：`"已退回，退回到王五"`

#### 3.7 撤回提交
- **接口**：`POST /biz/drawback/{taskId}`
- **描述**：撤回已提交的审批申请
- **请求头**：`Authorization: Bearer {token}`
- **权限**：仅提交人
- **路径参数**：`taskId`（任务ID）
- **响应**：`"已撤回提交"`

#### 3.8 重新提交材料
- **接口**：`POST /biz/resub`
- **描述**：重新提交被退回的审批材料
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "sub_id": 1,
    "reported_value": 95,
    "data_type": "1",
    "file_id": 2
  }
  ```
- **响应**：`"已重新提交,审核人为张三"`

#### 3.9 获取上次审批文件
- **接口**：`GET /biz/audit/file/{taskId}`
- **描述**：获取任务上一次审批通过的文件
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`taskId`（任务ID）
- **响应**：
  ```json
  {
    "fileId": 1,
    "filename": "last_report.pdf",
    "filepath": "/uploads/last_report.pdf"
  }
  ```

### 四、文件管理接口

#### 4.1 上传文件
- **接口**：`POST /system/upload/{task_id}`
- **描述**：上传文件并关联任务
- **请求头**：`Authorization: Bearer {token}`
- **Content-Type**：`multipart/form-data`
- **路径参数**：`task_id`（任务ID）
- **表单参数**：`file`（文件，支持pdf/doc/docx）
- **响应**：
  ```json
  {
    "fileId": 1,
    "filename": "report.pdf",
    "filepath": "/uploads/report.pdf"
  }
  ```

#### 4.2 下载文件
- **接口**：`GET /system/download/{file_id}`
- **描述**：根据文件ID下载文件
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`file_id`（文件ID）
- **响应**：文件流

### 五、通知管理接口

#### 5.1 发送通知
- **接口**：`POST /system/notice`
- **描述**：发送系统通知
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "to_user_id": 110229,
    "type": "1",
    "trigger_event": "任务审核",
    "title": "新的审批单",
    "content": "您有新的任务需要审核",
    "source_type": "0",
    "source_id": 3
  }
  ```
- **响应**：`"发送成功"`

#### 5.2 查看通知
- **接口**：`GET /system/notice`
- **描述**：查看当前用户收到的通知
- **请求头**：`Authorization: Bearer {token}`
- **响应**：
  ```json
  [
    {
      "noticeId": 1,
      "fromUserId": 110228,
      "toUserId": 110229,
      "type": "1",
      "triggerEvent": "任务审核",
      "title": "新的审批单",
      "content": "您有新的任务需要审核",
      "sourceType": "0",
      "sourceId": 3,
      "isRead": "0",
      "isDelete": 0,
      "createTime": "2024-01-06 00:00:00"
    }
  ]
  ```

#### 5.3 标记已读
- **接口**：`POST /system/notice/{notice_id}`
- **描述**：将通知标记为已读
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`notice_id`（通知ID）
- **响应**：`"已读"`

#### 5.4 发送预警
- **接口**：`POST /system/alert`
- **描述**：发送站内预警信息
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：
  ```json
  {
    "to_user_nick_name": "张三",
    "title": "任务预警",
    "content": "任务进度滞后",
    "source_id": 3
  }
  ```
- **响应**：`"发送成功"`

### 六、标志性成果接口

#### 6.1 查询单个成果
- **接口**：`GET /achievement/{achId}`
- **描述**：根据ID查询标志性成果
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`achId`（成果ID）
- **响应**：
  ```json
  {
    "achId": 1,
    "category": 1,
    "level": "国家级",
    "achName": "国家级教学成果奖",
    "department": "教育部",
    "gotTime": "2024-01-01 00:00:00",
    "deptId": 1,
    "fileId": 1,
    "comment": "备注信息",
    "isCompetition": 0,
    "teDengJiang": 0,
    "yiDengJiang": 1,
    "erDengJiang": 0,
    "sanDengJiang": 0,
    "jinJiang": 0,
    "yinJiang": 0,
    "tongJiang": 0,
    "youShengJiang": 0,
    "budDengDengCi": 0,
    "createBy": 110228,
    "isDelete": 0,
    "createTime": "2024-01-01 00:00:00"
  }
  ```

#### 6.2 查询所有成果
- **接口**：`GET /achievement/`
- **描述**：查询所有未删除的标志性成果
- **请求头**：`Authorization: Bearer {token}`
- **响应**：成果列表（同6.1格式）

#### 6.3 新增成果
- **接口**：`POST /achievement/add`
- **描述**：新增标志性成果
- **请求头**：`Authorization: Bearer {token}`
- **请求体**：同6.1响应格式（不含achId）
- **响应**：`"标志性成果「国家级教学成果奖」添加成功，成果ID：1"`

#### 6.4 更新成果
- **接口**：`POST /achievement/update/{id}`
- **描述**：更新标志性成果
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`id`（成果ID）
- **请求体**：同6.1响应格式
- **响应**：`"标志性成果「国家级教学成果奖」修改成功"`

#### 6.5 删除成果
- **接口**：`POST /achievement/delete/{achId}`
- **描述**：逻辑删除标志性成果
- **请求头**：`Authorization: Bearer {token}`
- **路径参数**：`achId`（成果ID）
- **响应**：`"标志性成果「国家级教学成果奖」删除成功"`

### 七、定时任务接口

#### 7.1 触发月度部门负责人提醒
- **接口**：`POST /scheduled/month_leader_trigger`
- **描述**：手动触发月度部门负责人提醒（原自动每月1号9:00执行）
- **请求头**：`Authorization: Bearer {token}`
- **响应**：`"向X名用户发送月度提醒成功"`

#### 7.2 触发月度审核人提醒
- **接口**：`POST /scheduled/month_auditor_trigger`
- **描述**：手动触发月度审核人提醒（原自动每月1号9:00执行）
- **请求头**：`Authorization: Bearer {token}`
- **响应**：`"向X名用户发送月度提醒成功"`

#### 7.3 触发年度提醒
- **接口**：`POST /scheduled/year_trigger`
- **描述**：手动触发年度提醒（原自动每年1月1号10:00执行）
- **请求头**：`Authorization: Bearer {token}`
- **响应**：`"年度提醒完成，成功发送 X 条提醒"`

#### 7.4 更新任务状态
- **接口**：`POST /scheduled/update_task_status`
- **描述**：手动触发任务状态更新（原自动每年1月1号10:00执行）
- **请求头**：`Authorization: Bearer {token}`
- **响应**：无

### 八、看板数据接口（新增）

#### 8.1 获取看板数据汇总

- **接口**：`GET /dashboard/summary`

- **描述**：获取完整的看板数据汇总，包含所有统计维度

- **请求头**：`Authorization: Bearer {token}`

- **响应**：

  ```json
  {
    "currentYear": "2025",
    "midTermEndYear": "2028",
    "updateTime": "2025-12-31 10:30:00",
    "overallCompletion": {
      "totalTasks": 100,
      "completedTasks": 65,
      "completionRate": 65.00,
      "period": "all",
      "description": "所有任务完成率"
    },
    "yearCompletion": {
      "totalTasks": 30,
      "completedTasks": 20,
      "completionRate": 66.67,
      "period": "year",
      "description": "2025年度任务完成率"
    },
    "midTermCompletion": {
      "totalTasks": 80,
      "completedTasks": 50,
      "completionRate": 62.50,
      "period": "midterm",
      "description": "中期（2028年前）任务完成率"
    },
    "firstLevelCompletion": {
      "totalTasks": 10,
      "completedTasks": 6,
      "completionRate": 60.00,
      "period": "firstLevel",
      "description": "一级任务完成率"
    },
    "deptOverallStats": [
      {
        "deptId": 1,
        "deptName": "技术部",
        "totalTasks": 30,
        "completedTasks": 20,
        "completionRate": 66.67
      },
      {
        "deptId": 2,
        "deptName": "市场部",
        "totalTasks": 25,
        "completedTasks": 15,
        "completionRate": 60.00
      }
    ],
    "deptYearStats": [...],
    "deptMidTermStats": [...],
    "firstLevelTasks": [
      {
        "taskId": 1,
        "taskName": "项目初始化",
        "deptId": 1,
        "deptName": "技术部",
        "status": "3",
        "targetValue": 100.00,
        "currentValue": 100.00,
        "progress": 100,
        "createTime": "2024-01-01 00:00:00",
        "updateTime": "2024-06-01 00:00:00"
      }
    ]
  }
  ```

#### 8.2 获取所有任务完成率

- **接口**：`GET /dashboard/completion/overall`

- **描述**：获取系统所有任务的完成率统计

- **请求头**：`Authorization: Bearer {token}`

- **响应**：

  ```json
  {
    "totalTasks": 100,
    "completedTasks": 65,
    "completionRate": 65.00,
    "period": "all",
    "description": "所有任务完成率"
  }
  ```

#### 8.3 获取本年度任务完成率

- **接口**：`GET /dashboard/completion/year`

- **描述**：获取指定年份的任务完成率（默认当前年份）

- **请求头**：`Authorization: Bearer {token}`

- **查询参数**：

  - `year`（可选）：年份，默认为当前年份

- **响应**：

  ```json
  {
    "totalTasks": 30,
    "completedTasks": 20,
    "completionRate": 66.67,
    "period": "year",
    "description": "2025年度任务完成率"
  }
  ```

#### 8.4 获取中期任务完成率

- **接口**：`GET /dashboard/completion/midterm`

- **描述**：获取中期任务（phase在指定年份之前）完成率

- **请求头**：`Authorization: Bearer {token}`

- **查询参数**：

  - `endYear`（可选）：截止年份，默认为2028

- **响应**：

  ```json
  {
    "totalTasks": 80,
    "completedTasks": 50,
    "completionRate": 62.50,
    "period": "midterm",
    "description": "中期（2028年前）任务完成率"
  }
  ```

#### 8.5 获取一级任务完成率

- **接口**：`GET /dashboard/completion/first-level`

- **描述**：获取一级任务的完成率统计

- **请求头**：`Authorization: Bearer {token}`

- **响应**：

  ```json
  {
    "totalTasks": 10,
    "completedTasks": 6,
    "completionRate": 60.00,
    "period": "firstLevel",
    "description": "一级任务完成率"
  }
  ```

#### 8.6 获取各部门整体任务完成率

- **接口**：`GET /dashboard/dept/overall`

- **描述**：获取各部门所有任务的完成率统计

- **请求头**：`Authorization: Bearer {token}`

- **响应**：

  ```json
  [
    {
      "deptId": 1,
      "deptName": "技术部",
      "totalTasks": 30,
      "completedTasks": 20,
      "completionRate": 66.67
    },
    {
      "deptId": 2,
      "deptName": "市场部",
      "totalTasks": 25,
      "completedTasks": 15,
      "completionRate": 60.00
    }
  ]
  ```

#### 8.7 获取各部门本年度任务完成率

- **接口**：`GET /dashboard/dept/year`
- **描述**：获取各部门本年度任务的完成率统计
- **请求头**：`Authorization: Bearer {token}`
- **查询参数**：
  - `year`（可选）：年份，默认为当前年份
- **响应**：部门任务完成率列表（同8.6格式）

#### 8.8 获取各部门中期任务完成率

- **接口**：`GET /dashboard/dept/midterm`
- **描述**：获取各部门中期任务的完成率统计
- **请求头**：`Authorization: Bearer {token}`
- **查询参数**：
  - `endYear`（可选）：截止年份，默认为2028
- **响应**：部门任务完成率列表（同8.6格式）

#### 8.9 获取一级任务详细情况

- **接口**：`GET /dashboard/tasks/first-level`

- **描述**：获取一级任务的详细信息列表

- **请求头**：`Authorization: Bearer {token}`

- **响应**：

  ```json
  [
    {
      "taskId": 1,
      "taskName": "项目初始化",
      "deptId": 1,
      "deptName": "技术部",
      "status": "3",
      "targetValue": 100.00,
      "currentValue": 100.00,
      "progress": 100,
      "createTime": "2024-01-01 00:00:00",
      "updateTime": "2024-06-01 00:00:00"
    },
    {
      "taskId": 2,
      "taskName": "系统架构设计",
      "deptId": 1,
      "deptName": "技术部",
      "status": "2",
      "targetValue": 80.00,
      "currentValue": 60.00,
      "progress": 75,
      "createTime": "2024-01-15 00:00:00",
      "updateTime": "2024-05-20 00:00:00"
    }
  ]
  ```

#### 8.10 获取单个部门详细统计信息

- **接口**：`GET /dashboard/dept/{deptId}`

- **描述**：获取单个部门的详细统计信息，包含整体、年度、中期完成率

- **请求头**：`Authorization: Bearer {token}`

- **路径参数**：`deptId`（部门ID）

- **响应**：

  ```json
  {
    "deptId": 1,
    "deptName": "技术部",
    "leaderId": 110228,
    "leaderName": "系统管理员",
    "overall": {
      "totalTasks": 30,
      "completedTasks": 20,
      "completionRate": 66.67
    },
    "year": {
      "totalTasks": 10,
      "completedTasks": 8,
      "completionRate": 80.00
    },
    "midterm": {
      "totalTasks": 25,
      "completedTasks": 16,
      "completionRate": 64.00
    }
  }
  ```

#### 

## 通用说明

### 状态码说明
| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 401 | 未认证或Token无效 |
| 403 | 权限不足 |
| 500 | 服务器内部错误 |

### 认证说明
- 除登录接口外，所有接口需要在请求头携带Token
- Token格式：`Authorization: Bearer {token}`
- Token有效期为1小时
- 注销后Token会被加入黑名单

### 权限说明
| 角色值 | 角色 | 权限说明 |
|--------|------|----------|
| 0 | 管理员 | 所有权限 |
| 1 | 普通用户 | 本人任务相关权限 |
| 2 | 负责人 | 归口负责任务权限 |

### 审批流程状态
| 状态值 | 状态说明 |
|--------|----------|
| 0 | 草稿 |
| 10 | 待部门负责人审批 |
| 20 | 待归口负责人审批 |
| 30 | 待管理员审批 |
| 40 | 已归档 |
| -10 | 被部门负责人退回 |
| -20 | 被归口负责人退回 |
| -30 | 被管理员退回 |

### 任务状态
| 状态值 | 状态说明 |
|--------|----------|
| 0 | 未开始 |
| 1 | 进行中 |
| 2 | 审核中 |
| 3 | 已完成 |

### 数据类型
| 类型值 | 类型说明 |
|--------|----------|
| 0 | 对指标没有影响 |
| 1 | 数值累加 |
| 2 | 百分比取大 |

## 注意事项

1. **文件格式限制**：仅支持pdf、doc、docx格式
2. **任务层级**：系统采用三级任务结构，只能提交三级任务的审批
3. **审批流程**：部门负责人→归口负责人→管理员三级审批
4. **定时任务**：系统内置定时提醒功能，也可手动触发
5. **数据安全**：敏感操作需管理员权限，所有操作记录日志

## 更新日志

### v2.0 (2026-01-31)
- 新增标志性成果管理接口
- 新增定时任务触发接口
- 完善任务管理接口
- 补充完整审批流程接口
- 规范接口文档格式

### v1.2 (2025-12-26)
- 修改登录方式为user_id登录
- 调整首次审批人为任务AuditorId

### v1.1 (2025-12-21)
- 新增审批流程相关接口
- 新增文件管理接口
- 新增通知系统接口

### v1.0 (2025-12-11)
- 基础用户认证功能
- 基础任务管理功能
```

---

### <a id='src\main\code_summary-md'></a>src\main\code_summary.md

```markdown
# 代码文件汇总

生成时间: 2026-02-08 21:49:05
源目录: `D:\college\0workspace\biz-backend\biz-backend\src\main`

---

## 目录

- [code_summary.md](#code_summarymd)
- [get_code.py](#get_codepy)
- [java\org\example\BizApplication.java](#bizapplicationjava)
- [java\org\example\config\JacksonConfig.java](#jacksonconfigjava)
- [java\org\example\config\JwtInterceptor.java](#jwtinterceptorjava)
- [java\org\example\config\UserRoleInterceptor.java](#userroleinterceptorjava)
- [java\org\example\config\WebConfig.java](#webconfigjava)
- [java\org\example\controller\AchievementController.java](#achievementcontrollerjava)
- [java\org\example\controller\BizController.java](#bizcontrollerjava)
- [java\org\example\controller\DashboardController.java](#dashboardcontrollerjava)
- [java\org\example\controller\ScheduledTaskController.java](#scheduledtaskcontrollerjava)
- [java\org\example\controller\SysController.java](#syscontrollerjava)
- [java\org\example\controller\TrendDataController.java](#trenddatacontrollerjava)
- [java\org\example\entity\BizAchievement.java](#bizachievementjava)
- [java\org\example\entity\BizAuditLog.java](#bizauditlogjava)
- [java\org\example\entity\BizMaterialSubmission.java](#bizmaterialsubmissionjava)
- [java\org\example\entity\BizPerformance.java](#bizperformancejava)
- [java\org\example\entity\BizPerformanceYear.java](#bizperformanceyearjava)
- [java\org\example\entity\BizProject.java](#bizprojectjava)
- [java\org\example\entity\BizProjectPhase.java](#bizprojectphasejava)
- [java\org\example\entity\BizTask.java](#biztaskjava)
- [java\org\example\entity\BizTrendData.java](#biztrenddatajava)
- [java\org\example\entity\dto\BizAuditDTO.java](#bizauditdtojava)
- [java\org\example\entity\dto\BizReSubDTO.java](#bizresubdtojava)
- [java\org\example\entity\dto\BizSubDTO.java](#bizsubdtojava)
- [java\org\example\entity\dto\BizTaskDTO.java](#biztaskdtojava)
- [java\org\example\entity\dto\FileUploadDTO.java](#fileuploaddtojava)
- [java\org\example\entity\dto\SysAlertDTO.java](#sysalertdtojava)
- [java\org\example\entity\dto\SysLoginDTO.java](#syslogindtojava)
- [java\org\example\entity\dto\SysNoticeDTO.java](#sysnoticedtojava)
- [java\org\example\entity\dto\SysPwdDTO.java](#syspwddtojava)
- [java\org\example\entity\dto\SysUserDTO.java](#sysuserdtojava)
- [java\org\example\entity\RelTaskPerformance.java](#reltaskperformancejava)
- [java\org\example\entity\SysDept.java](#sysdeptjava)
- [java\org\example\entity\SysFile.java](#sysfilejava)
- [java\org\example\entity\SysNotice.java](#sysnoticejava)
- [java\org\example\entity\SysUser.java](#sysuserjava)
- [java\org\example\entity\TokenBlacklist.java](#tokenblacklistjava)
- [java\org\example\entity\vo\BizAuditVO.java](#bizauditvojava)
- [java\org\example\entity\vo\BizTaskVo.java](#biztaskvojava)
- [java\org\example\entity\vo\DashboardSummaryVO.java](#dashboardsummaryvojava)
- [java\org\example\entity\vo\DeptTaskStatsVO.java](#depttaskstatsvojava)
- [java\org\example\entity\vo\ErrorVO.java](#errorvojava)
- [java\org\example\entity\vo\FileUploadVO.java](#fileuploadvojava)
- [java\org\example\entity\vo\FirstLevelTaskDetailVO.java](#firstleveltaskdetailvojava)
- [java\org\example\entity\vo\SysLoginVO.java](#sysloginvojava)
- [java\org\example\entity\vo\SysLogoutVO.java](#syslogoutvojava)
- [java\org\example\entity\vo\SysPasswordVO.java](#syspasswordvojava)
- [java\org\example\entity\vo\TaskCompletionVO.java](#taskcompletionvojava)
- [java\org\example\mapper\AchievementMapper.java](#achievementmapperjava)
- [java\org\example\mapper\BizMapper.java](#bizmapperjava)
- [java\org\example\mapper\SysMapper.java](#sysmapperjava)
- [java\org\example\mapper\TokenBlacklistMapper.java](#tokenblacklistmapperjava)
- [java\org\example\mapper\TrendDataMapper.java](#trenddatamapperjava)
- [java\org\example\service\AchievementService.java](#achievementservicejava)
- [java\org\example\service\BizService.java](#bizservicejava)
- [java\org\example\service\ScheduledTaskService.java](#scheduledtaskservicejava)
- [java\org\example\service\SysService.java](#sysservicejava)
- [java\org\example\service\TrendDataService.java](#trenddataservicejava)
- [java\org\example\utils\FileUploadUtil.java](#fileuploadutiljava)
- [java\org\example\utils\JWTUtil.java](#jwtutiljava)
- [resources\application.properties](#applicationproperties)

---

#### code_summary.md

```markdown
```

#### get_code.py

```python
#!/usr/bin/env python3
"""
代码文件转Markdown脚本
将当前目录及其子目录中的所有代码文件转换为Markdown格式
"""

import os
import argparse
from pathlib import Path
from datetime import datetime

# 常见代码文件的扩展名映射
FILE_EXTENSIONS = {
    # 编程语言
    '.py': 'python',
    '.java': 'java',
    '.js': 'javascript',
    '.ts': 'typescript',
    '.jsx': 'jsx',
    '.tsx': 'tsx',
    '.c': 'c',
    '.cpp': 'cpp',
    '.cc': 'cpp',
    '.h': 'c',
    '.hpp': 'cpp',
    '.cs': 'csharp',
    '.go': 'go',
    '.rs': 'rust',
    '.rb': 'ruby',
    '.php': 'php',
    '.swift': 'swift',
    '.kt': 'kotlin',
    '.kts': 'kotlin',
    '.scala': 'scala',

    # 脚本和配置
    '.sh': 'bash',
    '.bash': 'bash',
    '.zsh': 'bash',
    '.ps1': 'powershell',
    '.yml': 'yaml',
    '.yaml': 'yaml',
    '.json': 'json',
    '.xml': 'xml',
    '.html': 'html',
    '.css': 'css',
    '.scss': 'scss',
    '.less': 'less',
    '.sql': 'sql',
    '.md': 'markdown',

    # 其他
    '.txt': 'text',
    '.cfg': 'ini',
    '.ini': 'ini',
    '.toml': 'toml',
}

# 要排除的目录
EXCLUDE_DIRS = {
    '.git', '__pycache__', 'node_modules', 'venv', '.env',
    'dist', 'build', 'target', 'out', 'bin', 'obj'
}

# 要排除的文件
EXCLUDE_FILES = {
    '.DS_Store', 'Thumbs.db', '.gitignore', '.gitattributes'
}

def get_language_from_extension(file_path):
    """根据文件扩展名获取编程语言"""
    ext = Path(file_path).suffix.lower()
    return FILE_EXTENSIONS.get(ext, 'text')

def should_exclude_file(file_path):
    """检查文件是否应该被排除"""
    path = Path(file_path)

    # 检查是否在排除的目录中
    for part in path.parts:
        if part in EXCLUDE_DIRS:
            return True

    # 检查是否是排除的文件
    if path.name in EXCLUDE_FILES:
        return True

    # 检查是否是隐藏文件（以点开头）
    if path.name.startswith('.'):
        return True

    return False

def read_file_content(file_path):
    """读取文件内容，处理编码问题"""
    try:
        # 尝试多种编码
        encodings = ['utf-8', 'gbk', 'latin-1', 'iso-8859-1']

        for encoding in encodings:
            try:
                with open(file_path, 'r', encoding=encoding) as f:
                    return f.read()
            except UnicodeDecodeError:
                continue

        # 如果所有编码都失败，尝试二进制读取
        with open(file_path, 'rb') as f:
            content = f.read()
            # 尝试解码为utf-8，忽略错误
            return content.decode('utf-8', errors='ignore')

    except Exception as e:
        return f"无法读取文件: {e}"

def collect_code_files(root_dir):
    """收集所有代码文件"""
    code_files = []

    for root, dirs, files in os.walk(root_dir):
        # 排除不需要的目录
        dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]

        for file in files:
            file_path = os.path.join(root, file)

            if should_exclude_file(file_path):
                continue

            # 获取相对路径
            rel_path = os.path.relpath(file_path, root_dir)
            code_files.append((rel_path, file_path))

    # 按文件路径排序
    code_files.sort(key=lambda x: x[0].lower())

    return code_files

def generate_markdown(root_dir, output_file, max_file_size=1024*1024):  # 默认1MB
    """生成Markdown文档"""

    print(f"正在扫描目录: {root_dir}")
    code_files = collect_code_files(root_dir)

    if not code_files:
        print("未找到任何代码文件")
        return

    print(f"找到 {len(code_files)} 个文件")

    with open(output_file, 'w', encoding='utf-8') as md_file:
        # 写入标题
        md_file.write(f"# 代码文件汇总\n\n")
        md_file.write(f"生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        md_file.write(f"源目录: `{root_dir}`\n\n")
        md_file.write("---\n\n")

        # 写入目录
        md_file.write("## 目录\n\n")
        for rel_path, _ in code_files:
            filename = os.path.basename(rel_path)
            md_file.write(f"- [{rel_path}](#{filename.replace('.', '').lower()})\n")
        md_file.write("\n---\n\n")

        # 写入每个文件的内容
        for rel_path, full_path in code_files:
            filename = os.path.basename(rel_path)
            language = get_language_from_extension(filename)

            print(f"处理文件: {rel_path}")

            # 检查文件大小
            file_size = os.path.getsize(full_path)
            if file_size > max_file_size:
                print(f"  警告: 文件过大 ({file_size} bytes)，跳过")
                md_file.write(f"#### {rel_path}\n")
                md_file.write(f"\n文件过大 ({file_size} bytes)，已跳过\n\n")
                continue

            # 读取文件内容
            content = read_file_content(full_path)

            # 写入Markdown
            md_file.write(f"#### {rel_path}\n")
            md_file.write(f"\n```{language}\n")
            md_file.write(content)

            # 确保代码块以换行结束
            if content and not content.endswith('\n'):
                md_file.write('\n')

            md_file.write("```\n\n")

    print(f"\nMarkdown文件已生成: {output_file}")

def main():
    parser = argparse.ArgumentParser(description='将代码文件转换为Markdown格式')
    parser.add_argument('-o', '--output', default='code_summary.md',
                       help='输出Markdown文件名 (默认: code_summary.md)')
    parser.add_argument('-d', '--dir', default='.',
                       help='要扫描的目录 (默认: 当前目录)')
    parser.add_argument('-s', '--max-size', type=int, default=1024*1024,
                       help='最大文件大小(bytes)，超过此大小的文件将被跳过 (默认: 1MB)')

    args = parser.parse_args()

    root_dir = os.path.abspath(args.dir)
    output_file = args.output

    if not os.path.exists(root_dir):
        print(f"错误: 目录不存在: {root_dir}")
        return

    generate_markdown(root_dir, output_file, args.max_size)

if __name__ == "__main__":
    main()
```

#### java\org\example\BizApplication.java

```java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 业务应用主启动类
 */
@SpringBootApplication
@EnableScheduling  // 启用定时任务支持
public class BizApplication {
    /**
     * 应用入口方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BizApplication.class, args);
    }
}
```

#### java\org\example\config\JacksonConfig.java

```java
package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Jackson全局配置：统一Date类型序列化/反序列化格式
 */
@Configuration
public class JacksonConfig {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. 指定Date类型的解析/生成格式（匹配yyyy-MM-dd HH:mm:ss）
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 2. 设置时区（避免时间偏移，建议使用东八区）
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 3. 解决序列化时Date类型为时间戳的问题
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 4. 支持Java 8时间类型（可选，兼容LocalDateTime等）
        objectMapper.registerModule(new JavaTimeModule());

        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
```

#### java\org\example\config\JwtInterceptor.java

```java
package org.example.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.vo.ErrorVO;
import org.example.mapper.TokenBlacklistMapper;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * JWT拦截器：验证请求中的Token有效性
 * - 检查Token是否存在
 * - 验证Token是否在黑名单中
 * - 解析Token并设置用户角色信息
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

    /**
     * 前置处理：验证Token
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @return 验证通过返回true，否则返回false
     * @throws Exception 异常信息
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            sendError(response, 401, "No Token");
            return false;
        }

        // 检查黑名单
        if (tokenBlacklistMapper.findByToken(token) != null) {
            sendError(response, 401, "Invalid Token");
            return false;
        }

        try {
            DecodedJWT decodedJWT = JWTUtil.verifyJwtToken(token);
            request.setAttribute("userRole", decodedJWT.getClaim("role").asString());
            return true;
        } catch (Exception e) {
            sendError(response, 401, "No Token: " + e.getMessage());
            return false;
        }
    }

    /**
     * 发送错误响应
     * @param response HTTP响应
     * @param code 错误码
     * @param message 错误信息
     * @throws IOException IO异常
     */
    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(new ErrorVO(message, code)));
    }
}
```

#### java\org\example\config\UserRoleInterceptor.java

```java
package org.example.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.utils.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 用户角色拦截器：验证用户角色权限
 * 仅允许管理员角色（role=0）访问受保护接口
 */
@Component
public class UserRoleInterceptor implements HandlerInterceptor {

    /** 允许访问的角色：0-管理员 */
    private static final String ALLOWED_ROLE = "0";

    /**
     * 角色验证，待调试，待完善
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头解析Token获取角色（你的真实逻辑）
        String token = request.getHeader("Authorization");
        String userRole = JWTUtil.getRoleFromToken(token);

        // 2. 角色校验：无角色/角色非0则拒绝访问
        if (userRole == null || !ALLOWED_ROLE.equals(userRole)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\":403,\"msg\":\"无权限访问该接口，仅管理员可访问\"}");
            writer.flush();
            writer.close();
            return false;
        }

        // 角色校验通过，放行请求
        return true;
    }
}
```

#### java\org\example\config\WebConfig.java

```java
package org.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类：配置CORS、拦截器、路径匹配等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置路径匹配
     * @param configurer 路径匹配配置器
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 不区分尾部斜杠
        configurer.setUseTrailingSlashMatch(true);
    }

    /**
     * 配置CORS跨域
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("*")
                .allowedOriginPatterns("*") // 允许的域名
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private UserRoleInterceptor userRoleInterceptor;

    /**
     * 配置拦截器
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/system/**")
                .addPathPatterns("/biz/**")
                .addPathPatterns("/achievement/**")
                .excludePathPatterns("/system/login");

        registry.addInterceptor(userRoleInterceptor)
                .addPathPatterns("/system/users/**");
    }
}
```

#### java\org\example\controller\AchievementController.java

```java
package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.BizAchievement;
import org.example.entity.vo.ErrorVO;
import org.example.service.AchievementService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标志性成果控制层
 * 处理标志性成果基础增删查改HTTP请求
 */
@RestController
@RequestMapping("/achievement")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    /**
     * 根据成果ID查询单条标志性成果信息
     * @param achId 成果ID
     * @return 成果实体/错误信息
     */
    @GetMapping("/{achId}")
    public Object getAchievementById(@PathVariable Long achId) {
        try {
            return achievementService.getAchievementById(achId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 查询所有未删除的标志性成果列表
     * @return 成果实体列表/错误信息
     */
    @GetMapping("/")
    public Object listAllAchievements() {
        try {
            List<BizAchievement> achievementList = achievementService.listAllAchievements();
            return achievementList;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 新增标志性成果
     * @param achievement 成果实体（JSON请求体）
     * @return 操作结果/错误信息
     */
    @PostMapping("/add")
    public Object addAchievement(@RequestBody BizAchievement achievement, HttpServletRequest  request) {
        try {
            Long achId = achievementService.addAchievement(achievement, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
            return "标志性成果「" + achievement.getAchName() + "」添加成功，成果ID：" + achId;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 修改标志性成果信息
     * @param achievement 成果实体（含成果ID，JSON请求体）
     * @return 操作结果/错误信息
     */
    @PostMapping("/update/{id}")
    public Object updateAchievement(@PathVariable("id") Long id,@RequestBody BizAchievement achievement) {
        try {
            achievementService.updateAchievement(id,achievement);
            return "标志性成果「" + achievement.getAchName() + "」修改成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 逻辑删除标志性成果
     * @param achId 成果ID（路径参数）
     * @return 操作结果/错误信息
     */
    @PostMapping("/delete/{achId}")
    public Object deleteAchievement(@PathVariable("achId") Long achId) {
        try {
            // 查询成果名称，用于返回友好提示
            BizAchievement achievement = achievementService.getAchievementById(achId);
            achievementService.deleteAchievement(achId);
            return "标志性成果「" + achievement.getAchName() + "」删除成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
}
```

#### java\org\example\controller\BizController.java

```java
package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.dto.BizAuditDTO;
import org.example.entity.dto.BizSubDTO;
import org.example.entity.dto.BizReSubDTO;
import org.example.entity.dto.BizTaskDTO;
import org.example.entity.vo.ErrorVO;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.example.service.BizService;

/**
 * 业务控制器：处理所有业务相关请求
 * 包含任务管理、材料提交、审批流程等功能
 */
@RestController
@RequestMapping("/biz")
public class BizController {
    @Autowired
    private BizService bizService;

    /**
     * 获取全量任务数据
     * @param request HTTP请求
     * @return 任务列表或错误信息
     */
    @GetMapping("/tasks")
    public Object getTasks(HttpServletRequest request){
        try{
            return bizService.getTasksByUserRole(JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据id获取任务
     * @param taskId 任务ID
     * @return 任务详情或错误信息
     */
    @GetMapping("/tasks/{taskId}")
    public Object getTaskById(@PathVariable("taskId") Long taskId){
        try{
            return bizService.getTaskById(taskId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取当前任务的所有子任务
     * @param taskId 任务ID
     * @return 子任务列表或错误信息
     */
    @GetMapping("/tasks/children")
    public Object getAllChildrenTasks(@RequestParam("task_id") Long taskId){
        try{
            return bizService.getAllChildrenTasks(taskId);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取当前任务的父任务
     * @param taskId 任务ID
     * @return 父任务信息或错误信息
     */
    @GetMapping("/tasks/parent")
    public Object getParentTask(@RequestParam("task_id") Long taskId){
        try{
            return bizService.getParentTask(taskId);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据部门id获取任务
     * @param deptId 部门ID
     * @return 任务列表或错误信息
     */
    @GetMapping("/tasks/dept")
    public Object getTasksByDeptId(@RequestParam("dept_id") Long deptId){
        try{
            return bizService.getTasksByDeptId(deptId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 添加任务
     * @param task 任务数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/tasks/manage/add")
    public Object addTask(@RequestBody BizTaskDTO task, HttpServletRequest request){
        try{
            bizService.addTask(task);
            return "任务"+task.getTaskName()+"添加成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 更新任务
     * @param task 任务数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/tasks/manage/update")
    public Object updateTask(@RequestBody BizTaskDTO task, HttpServletRequest request){
        try{
            bizService.updateTask(task);
            return "任务"+task.getTaskName()+"更新成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 完成任务
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/tasks/manage/finish/{taskId}")
    public Object finishTask(@PathVariable("taskId") Long taskId, HttpServletRequest request){
        try{
            return bizService.finishTask(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 提交审批材料
     * @param bizSubDTO 提交数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/submit")
    public Object submitMaterial(@RequestBody BizSubDTO bizSubDTO, HttpServletRequest request){
        try{
            return bizService.submitMaterial(bizSubDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")) );
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取审批单
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 审批单信息或错误信息
     */
    @GetMapping("/audit/{taskId}")
    public Object getAudit(@PathVariable("taskId") Long taskId,HttpServletRequest request){
        try{
            return bizService.getAudit(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取"待我审批"的审批单列表（按 current_handler_id 查询）
     * @param request HTTP请求
     * @return 待审批列表或错误信息
     */
    @GetMapping("/audit/todo")
    public Object getTodoAudits(HttpServletRequest request) {
        try {
            return bizService.getTodoAudits(JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取某任务的全部审批单（用于任务详情抽屉展示完整流程）
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 审批单列表或错误信息
     */
    @GetMapping("/audit/task/{taskId}")
    public Object getAuditByTaskId(@PathVariable("taskId") Long taskId, HttpServletRequest request) {
        try {
            return bizService.getAuditByTaskId(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据任务id获取上次周期上传的文件
     * @param taskId 任务ID
     * @param response HTTP响应
     * @return 文件信息或错误信息
     */
    @GetMapping("/audit/file/{taskId}")
    public Object getLastCycleFiles(@PathVariable("taskId") Long taskId, HttpServletResponse  response) {
        try {
            return bizService.getLastCycleFiles(taskId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取审批操作日志（biz_audit_log）
     * @param subId 提交ID
     * @param request HTTP请求
     * @return 操作日志或错误信息
     */
    @GetMapping("/audit/logs/{subId}")
    public Object getAuditLogs(@PathVariable("subId") Long subId, HttpServletRequest request) {
        try {
            return bizService.getAuditLogs(subId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 审批操作
     * @param bizAuditDTO 审批数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/audit")
    public Object audit(@RequestBody BizAuditDTO bizAuditDTO, HttpServletRequest request){
        try{
            return bizService.audit(bizAuditDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 撤回提交申请
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/drawback/{taskId}")
    public Object drawbackSubmit(@PathVariable("taskId") Long taskId, HttpServletRequest request){
        try{
            return bizService.drawbackSubmit(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 重新提交材料
     * @param bizReSubDTO 重新提交数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/resub")
    public Object reSubmitMaterial(@RequestBody BizReSubDTO bizReSubDTO, HttpServletRequest request){
        try{
            return bizService.reSubmitMaterial(bizReSubDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
}
```

#### java\org\example\controller\DashboardController.java

```java
package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.vo.*;
import org.example.service.BizService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 看板数据控制器
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private BizService bizService;

    /**
     * 获取看板数据汇总
     * @param request HTTP请求
     * @return 看板数据汇总
     */
    @GetMapping("/summary")
    public Object getDashboardSummary(HttpServletRequest request) {
        try {
            // 验证token（可选，如果需要权限控制）
            String token = request.getHeader("Authorization");
            if (token != null) {
                JWTUtil.verifyJwtToken(token);
            }

            return bizService.getDashboardSummary();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取所有任务完成率
     * @return 所有任务完成率
     */
    @GetMapping("/completion/overall")
    public Object getAllTaskCompletionRate() {
        try {
            return bizService.getAllTaskCompletionRate();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取本年度任务完成率
     * @param year 年份（可选，默认当前年份）
     * @return 本年度任务完成率
     */
    @GetMapping("/completion/year")
    public Object getYearTaskCompletionRate(@RequestParam(required = false) Integer year) {
        try {
            return bizService.getYearTaskCompletionRate(year);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取中期任务完成率
     * @param endYear 截止年份（可选，默认2028）
     * @return 中期任务完成率
     */
    @GetMapping("/completion/midterm")
    public Object getMidTermTaskCompletionRate(@RequestParam(required = false) Integer endYear) {
        try {
            return bizService.getMidTermTaskCompletionRate(endYear);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取一级任务完成率
     * @return 一级任务完成率
     */
    @GetMapping("/completion/first-level")
    public Object getFirstLevelTaskCompletionRate() {
        try {
            return bizService.getFirstLevelTaskCompletionRate();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取各部门整体任务完成率
     * @return 各部门任务完成率列表
     */
    @GetMapping("/dept/overall")
    public Object getDeptTaskCompletionRates() {
        try {
            return bizService.getDeptTaskCompletionRates();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取各部门本年度任务完成率
     * @param year 年份（可选，默认当前年份）
     * @return 各部门本年度任务完成率列表
     */
    @GetMapping("/dept/year")
    public Object getDeptYearTaskCompletionRates(@RequestParam(required = false) Integer year) {
        try {
            return bizService.getDeptYearTaskCompletionRates(year);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取各部门中期任务完成率
     * @param endYear 截止年份（可选，默认2028）
     * @return 各部门中期任务完成率列表
     */
    @GetMapping("/dept/midterm")
    public Object getDeptMidTermTaskCompletionRates(@RequestParam(required = false) Integer endYear) {
        try {
            return bizService.getDeptMidTermTaskCompletionRates(endYear);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取一级任务详细情况
     * @return 一级任务详情列表
     */
    @GetMapping("/tasks/first-level")
    public Object getFirstLevelTaskDetails() {
        try {
            return bizService.getFirstLevelTaskDetails();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取单个部门详细统计信息
     * @param deptId 部门ID
     * @return 部门统计详情
     */
    @GetMapping("/dept/{deptId}")
    public Object getDeptStatsDetail(@PathVariable Long deptId) {
        try {
            return bizService.getDeptStatsDetail(deptId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 批量获取多个部门统计信息
     * @param deptIds 部门ID列表（逗号分隔）
     * @return 部门统计列表
     */
    @GetMapping("/dept/batch")
    public Object getBatchDeptStats(@RequestParam String deptIds) {
        try {
            String[] ids = deptIds.split(",");
            List<Map<String, Object>> result = new java.util.ArrayList<>();

            for (String idStr : ids) {
                try {
                    Long deptId = Long.parseLong(idStr.trim());
                    Map<String, Object> stats = bizService.getDeptStatsDetail(deptId);
                    result.add(stats);
                } catch (NumberFormatException e) {
                    // 跳过无效的ID
                }
            }

            return result;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取对比数据：不同维度的完成率对比
     * @param dimension 对比维度：dept, year, level
     * @return 对比数据
     */
    @GetMapping("/comparison/{dimension}")
    public Object getCompletionComparison(@PathVariable String dimension) {
        try {
            Map<String, Object> result = new java.util.HashMap<>();

            switch (dimension.toLowerCase()) {
                case "dept":
                    // 各部门完成率对比
                    result.put("data", bizService.getDeptTaskCompletionRates());
                    result.put("dimension", "部门");
                    break;

                case "year":
                    // 历年完成率对比（需要扩展Mapper）
                    result.put("data", getYearComparisonData());
                    result.put("dimension", "年度");
                    break;

                case "level":
                    // 各级别任务完成率对比
                    result.put("data", getLevelComparisonData());
                    result.put("dimension", "任务级别");
                    break;

                default:
                    throw new RuntimeException("不支持的对比维度: " + dimension);
            }

            return result;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    // 辅助方法：获取年度对比数据（需要扩展实现）
    private List<Map<String, Object>> getYearComparisonData() {
        // 这里简化实现，实际需要从数据库查询历年数据
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        // 示例数据
        for (int year = 2023; year <= 2025; year++) {
            Map<String, Object> yearData = new java.util.HashMap<>();
            yearData.put("year", year);
            yearData.put("totalTasks", 50 + (year - 2023) * 10);
            yearData.put("completedTasks", 30 + (year - 2023) * 15);
            yearData.put("completionRate", 60 + (year - 2023) * 10);
            result.add(yearData);
        }

        return result;
    }

    // 辅助方法：获取级别对比数据
    private List<Map<String, Object>> getLevelComparisonData() {
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        // 一级任务
        TaskCompletionVO firstLevel = bizService.getFirstLevelTaskCompletionRate();
        result.add(createLevelData("一级任务", firstLevel));

        // 二级任务（需要扩展Mapper）
        result.add(createLevelData("二级任务", 100, 60));

        // 三级任务（需要扩展Mapper）
        result.add(createLevelData("三级任务", 200, 120));

        return result;
    }

    private Map<String, Object> createLevelData(String levelName, TaskCompletionVO vo) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("level", levelName);
        data.put("totalTasks", vo.getTotalTasks());
        data.put("completedTasks", vo.getCompletedTasks());
        data.put("completionRate", vo.getCompletionRate());
        return data;
    }

    private Map<String, Object> createLevelData(String levelName, int totalTasks, int completedTasks) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("level", levelName);
        data.put("totalTasks", totalTasks);
        data.put("completedTasks", completedTasks);

        if (totalTasks > 0) {
            data.put("completionRate",
                    java.math.BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                            .setScale(2, java.math.BigDecimal.ROUND_HALF_UP)
            );
        } else {
            data.put("completionRate", java.math.BigDecimal.ZERO);
        }

        return data;
    }
}
```

#### java\org\example\controller\ScheduledTaskController.java

```java
package org.example.controller;

import org.example.service.ScheduledTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduled")
public class ScheduledTaskController {

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    @PostMapping("/month_leader_trigger")
    public String triggerManualReminder() {
        return scheduledTaskService.triggerMonthDeptLeaderReminder();
    }

    @PostMapping("/month_auditor_trigger")
    public String triggerAuditorMonthReminder() {
        return scheduledTaskService.triggerMonthAuditorReminder();

    }

    @PostMapping("/year_trigger")
    public String triggerYearReminder() {
        return scheduledTaskService.triggerYearlyReminder();
    }

    @PostMapping("/update_task_status")
    public void updateTaskStatus() {
        scheduledTaskService.updateTaskStatus();
    }
}
```

#### java\org\example\controller\SysController.java

```java
package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.SysNotice;
import org.example.entity.SysUser;
import org.example.entity.dto.*;
import org.example.entity.vo.ErrorVO;
import org.example.entity.vo.SysLogoutVO;
import org.example.service.SysService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 系统控制器：处理用户管理、登录认证、文件上传等系统功能
 */
@RestController
@RequestMapping("/system")
public class SysController {

    @Autowired
    private SysService sysService;

    /**
     * 获取所有用户
     * @return 用户列表或错误信息
     */
    @GetMapping("/allUsers")
    public Object getAllUsers() {
        try{
            return sysService.getAllUsers();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 添加用户
     * @param user 用户数据
     * @return 操作结果或错误信息
     */
    @PostMapping("/users/add")
    public Object addUser(@RequestBody SysUserDTO  user) {
        try{
            sysService.addUser(user);
            return "用户 "+user.getUserName()+" 添加成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 更新用户
     * @param user 用户数据
     * @return 操作结果或错误信息
     */
    @PostMapping("/users/update")
    public Object updateUser(@RequestBody SysUserDTO user) {
        try{
            sysService.updateUser(user);
            return "用户 "+user.getUserName()+" 更新成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 删除用户
     * @param userId 用户ID
     * @return 操作结果或错误信息
     */
    @PostMapping("/users/delete/{userId}")
    public Object deleteUser(@PathVariable Long userId) {
        try{
            sysService.deleteUser(userId);
            return "用户 "+userId+" 删除成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据部门ID获取部门信息（含 leaderId）
     * @param deptId 部门ID
     * @return 部门信息或错误信息
     */
    @GetMapping("/dept/{deptId}")
    public Object getDeptById(@PathVariable("deptId") Long deptId) {
        try {
            return sysService.getDeptById(deptId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 用户登录
     * @param sysLoginDTO 登录数据
     * @return 登录结果或错误信息
     */
    @PostMapping("/login")
    public Object login(@RequestBody SysLoginDTO sysLoginDTO){
        try{
            return sysService.login(sysLoginDTO.getUser_id(), sysLoginDTO.getPassword());
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 修改密码
     * @param sysPasswordDTO 密码数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/password")
    public Object changePassword(@RequestBody SysPwdDTO sysPasswordDTO, HttpServletRequest request){
        try{
            //根据token获取用户ID
            Long userId=JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            sysService.changePassword(userId,sysPasswordDTO.getNew_password());
            return new SysPwdDTO("密码修改成功");
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 用户注销
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/logout")
    public Object logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null) {
            try {
                sysService.logout(token);
                return new SysLogoutVO("注销成功");
            } catch (Exception e) {
                return new ErrorVO(e.getMessage(), 500);
            }
        }
        return new ErrorVO("token不存在", 401);
    }

    /**
     * 上传文件
     * @param file 文件对象
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 文件信息或错误信息
     */
    @PostMapping("/upload/{task_id}")
    public Object uploadFile(@RequestParam("file") MultipartFile file, @PathVariable("task_id") Long taskId, HttpServletRequest request){
        try{
            return sysService.uploadFile(file,taskId,request);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据file_id下载文件
     * @param fileId 文件ID
     * @param response HTTP响应
     * @return 文件流或错误信息
     */
    @GetMapping("/download/{file_id}")
    public Object downloadFile(@PathVariable("file_id") Long fileId, HttpServletResponse response){
        try{
            sysService.downloadFile(fileId,response);
            return "下载成功";
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 发送通知
     * @param sysNoticeDTO 通知数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/notice")
    public Object sendNotice(@RequestBody SysNoticeDTO sysNoticeDTO, HttpServletRequest request){
        try{
            Long userId=JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            sysService.sendNotice(sysNoticeDTO,userId);
            return "发送成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 查看当前用户收到的通知
     * @param request HTTP请求
     * @return 通知列表或异常
     */
    @GetMapping("/notice")
    public List<SysNotice> getNotices(HttpServletRequest request) {
        try{
            Long userId=JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            return sysService.getNotices(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设为已读
     * @param noticeId 通知ID
     * @return 操作结果或错误信息
     */
    @PostMapping("/notice/{notice_id}")
    public Object setRead(@PathVariable("notice_id") Long noticeId){
        try{
            sysService.setRead(noticeId);
            return "已读";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 站内信息预警
     * input token to_user_nick_name title/content source_id
     * @param sysAlertDTO 预警数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/alert")
    public Object sendAlert(@RequestBody SysAlertDTO sysAlertDTO, HttpServletRequest request){
        try{
            Long userId=JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            sysService.sendAlert(sysAlertDTO,userId);
            return "发送成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }


}
```

#### java\org\example\controller\TrendDataController.java

```java
package org.example.controller;



import org.example.entity.BizTrendData;
import org.example.entity.vo.ErrorVO;
import org.example.service.TrendDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard/trend")
public class TrendDataController {

    @Autowired
    private TrendDataService trendDataService;

    /**
     * 获取趋势数据
     * @param year 年份（可选，默认当前年份）
     */
    @GetMapping("/{year}")
    public Object getTrendData(@PathVariable(required = false) Integer year) {
        try {
            List<BizTrendData> trendData = trendDataService.getTrendDataByYear(year);
            return trendData;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取当前年份的趋势数据
     */
    @GetMapping("/")
    public Object getCurrentYearTrendData() {
        try {
            List<BizTrendData> trendData = trendDataService.getTrendDataByYear(null);
            return trendData;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 手动记录（测试用）
     */
    @PostMapping("/record")
    public Object manualRecord() {
        try {
            return trendDataService.triggerRecord();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
}
```

#### java\org\example\entity\BizAchievement.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 标志性成果表实体类
 * 对应表：biz_achievement
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAchievement {
    private Long achId; // 成果ID(主键)

    /**
     * 成果类别枚举：
     * 1:落实立德树人根本任务 2:创新产教融合机制 3:打造高水平专业群
     * 4:建设一流核心课程 5:开放优质新形态教材 6:建设高水平双师队伍
     * 7:建设产教融合实训基地 8:构建数字化教学新生态 9:拓展国际交流与合作
     * 10:打造一流区域型高端智库
     */
    private Integer category; // 类别

    /**
     * 成果级别：国家级/省级/市级
     */
    private String level; // 级别
    private String achName; // 成果名称
    private String department; // 组织部门(如：教育部办公厅等，区别于校内部门)
    private Date gotTime; // 颁发时间
    private Long deptId; // 部门ID
    private Long fileId; // 佐证材料文件ID
    private String comment; // 备注

    /**
     * 是否竞赛：0:不是竞赛 1:是竞赛
     */
    private Integer isCompetition; // 是否竞赛

    private Integer teDengJiang; // 特等奖数量
    private Integer yiDengJiang; // 一等奖数量
    private Integer erDengJiang; // 二等奖数量
    private Integer sanDengJiang; // 三等奖数量
    private Integer jinJiang; // 金奖数量
    private Integer yinJiang; // 银奖数量
    private Integer tongJiang; // 铜奖数量
    private Integer youShengJiang; // 优胜奖数量
    private Integer budDengDengCi; // 不定等次数量

    private Long createBy; // 创建人ID(关联sys_user表userId)
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间(默认当前时间)
}
```

#### java\org\example\entity\BizAuditLog.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditLog {
    private Long logId; // 日志ID
    private Long subId; // 提交ID
    private Long operatorId; // 操作人ID
    private String actionType; // 动作
    /**
     * 流程状态参考 BizMaterialSubmission 的 flowStatus 枚举
     */
    private Integer preStatus; // 前状态
    private Integer postStatus; // 后状态
    private String comment; // 意见
    private Date createTime; // 创建时间
}
```

#### java\org\example\entity\BizMaterialSubmission.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizMaterialSubmission {
    private Long subId; // 提交ID
    private Long taskId; // 任务ID
    private Long fileId; // 文件ID

    // 填报数据相关
    private BigDecimal reportedValue; // 本次填报完成值
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
    private Long submitBy; // 提交人ID
    private Long submitDeptId; // 提交人部门ID
    private Long manageDeptId; // 归口部门ID
    private Date submitTime; // 提交时间
    private String fileSuffix; // 后缀（仅允许pdf/doc/docx）

    /**
     * 流程状态枚举：
     * 0:草稿
     * 10:待[所在部门]审批 20:待[归口部门]审批 30:待[管理员]审批
     * 40:已归档
     * -10:被所在部门退回 -20:被归口部门退回 -30:被管理员退回
     */
    private Integer flowStatus;
    private Long currentHandlerId; // 当前处理人ID
    private Integer isDelete; // 0:存在 1:删除
}
```

#### java\org\example\entity\BizPerformance.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizPerformance {
    private Long perfId; // 指标ID
    private Long projectId; // 项目ID
    private Long parentId; // 父指标ID
    private String ancestors; // 祖先指标ID集合
    private String perfCode; // 编码
    private String perfName; // 名称

    private BigDecimal targetValue; // 总目标值
    private BigDecimal currentValue; // 当前完成值(计算得出)
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大

    private Long deptId; // 归口部门ID
    private Long principalId; // 归口负责人ID
    private Long leaderId; // 任务负责人ID

    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

#### java\org\example\entity\BizPerformanceYear.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizPerformanceYear {
    private Long yearId; // 年度ID
    private Long perfId; // 指标ID
    private Integer year; // 年份
    private BigDecimal targetValue; // 年度目标值
    private BigDecimal actualValue; // 年度实际完成
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
    private Integer isDelete; // 0:存在 1:删除
}
```

#### java\org\example\entity\BizProject.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizProject {
    private Long projectId; // 项目ID
    private String projectName; // 项目名称
    private String projectCode; // 项目编码
    private Long leaderId; // 项目负责人ID
    private Date startDate; // 开始日期
    private Date endDate; // 结束日期
    /**
     * 项目状态枚举：
     * 0:未开始 1:进行中 2:已完成
     * 3:暂停 4:逾期
     */
    private String status;
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

#### java\org\example\entity\BizProjectPhase.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizProjectPhase {
    private Long phaseId; // 阶段ID
    private Long projectId; // 项目ID
    private String phaseName; // 阶段名称
    private Date startDate; // 开始日期
    private Date endDate; // 结束日期
    private String isCurrent; // 是否当前阶段 0:否 1:是
    private Integer isDelete; // 0:存在 1:删除
}
```

#### java\org\example\entity\BizTask.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTask {
    private Long taskId; // 任务ID
    private Long projectId; // 项目ID
    private Long parentId; // 父任务节点ID
    private String ancestors; // 祖先节点ID集合
    private Integer phase; // 所属年份
    private String taskCode; // 任务编号
    private String taskName; // 任务名称
    private Integer level; // 任务层级
    private String comment;// 任务描述

    // 组织归属相关
    private Long deptId; // 归口部门ID
    private Long auditorId;//审核人ID
    private Long principalId; // 归口负责人ID
    private Long leaderId; // 任务负责人ID

    // 数据需求配置相关
    private String expTarget; // 预期达成情况
    private String expLevel; // 预期成果级别（国/省/市）
    private String expEffect; // 预期效果
    private String expMaterialDesc; // 预期过程（佐证）材料清单(文本描述)
    /**
     * 数据类型枚举：
     * 0:对指标没有影响
     * 1:数值(累加)
     * 2:百分比(取大)
     */
    private String dataType;
    private BigDecimal targetValue; // 目标值
    private BigDecimal currentValue; // 当前完成值(缓存统计)
    private BigDecimal weight; // 权重(冗余)
    private Integer progress; // 任务进度(冗余)
    /**
     * 任务状态枚举：
     * 0:未开始 1:进行中
     * 2:审核中 3:已完成
     */
    private String status;
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

#### java\org\example\entity\BizTrendData.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTrendData {
    private Long dataId;
    private Integer year;
    private Integer month;
    private Integer day;
    private Date createTime;
    private Double completionRate; // 用Double简单处理
    private Integer isDelete;
}
```

#### java\org\example\entity\dto\BizAuditDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditDTO {
    private Long sub_id;
    private Boolean is_pass;
    private String title;
    private String content;
//    private String file_id;
}
```

#### java\org\example\entity\dto\BizReSubDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizReSubDTO {
    private Long sub_id;
    private BigDecimal reported_value;
    private String data_type;
    private Long file_id;
}
```

#### java\org\example\entity\dto\BizSubDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
//reported_value(可空 value_type) task_id
public class BizSubDTO {
    private Long task_id;
    private BigDecimal reported_value;
    private String data_type;
    private Long file_id;
    private String comment;
}
```

#### java\org\example\entity\dto\BizTaskDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTaskDTO {
    private Long taskId; // 任务ID
    private Long projectId; // 项目ID
    private Long parentId; // 父任务节点ID
    private String ancestors; // 祖先节点ID集合
    private Integer phase; // 所属年份
    private String taskCode; // 任务编号
    private String taskName; // 任务名称
    private Integer level; // 任务层级
    private String comment;// 任务描述

    // 组织归属相关
    private Long deptId; // 归口部门ID
    private Long auditorId;//审核人ID
    private Long principalId; // 归口负责人ID
    private Long leaderId; // 任务负责人ID

    // 数据需求配置相关
    private String expTarget; // 预期达成情况
    private String expLevel; // 预期成果级别（国/省/市）
    private String expEffect; // 预期效果
    private String expMaterialDesc; // 预期过程（佐证）材料清单(文本描述)
    /**
     * 数据类型枚举：
     * 0:对指标没有影响
     * 1:数值(累加)
     * 2:百分比(取大)
     */
    private String dataType;
    private BigDecimal targetValue; // 目标值
    private BigDecimal currentValue; // 当前完成值(缓存统计)
    private BigDecimal weight; // 权重(冗余)
    private Integer progress; // 任务进度(冗余)
    /**
     * 任务状态枚举：
     * 0:未开始 1:进行中
     * 2:审核中 3:已完成
     */
    private String status;

}
```

#### java\org\example\entity\dto\FileUploadDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//上传文件 post
//input token reported_value(可空 value_type) task_id文件
//新生成file数据
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadDTO {
    private String filename;
    private String filepath;
}
```

#### java\org\example\entity\dto\SysAlertDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//站内信息 预警
//input token to_user_nick_name title/content source_id
public class SysAlertDTO {
    private String to_user_nick_name;
    private String title;
    private String content;
    private Long source_id;
}
```

#### java\org\example\entity\dto\SysLoginDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysLoginDTO {
    private Long user_id;
    private String password;
}
```

#### java\org\example\entity\dto\SysNoticeDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysNoticeDTO {
    private Long to_user_id;
    private String type;
    private String trigger_event;
    private String title;
    private String content;
    private String source_type;
    private Long source_id;
    private String is_read;
}
```

#### java\org\example\entity\dto\SysPwdDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysPwdDTO {
    private String new_password;
}
```

#### java\org\example\entity\dto\SysUserDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUserDTO {
    private Long userId; // 用户ID
    private Long deptId; // 所属部门ID
    private String userName; // 账号
    private String nickName; // 姓名
    private String email; // 邮箱
    private String password; // 密码
    private String role; // 角色 0:admin 1:user 2:leader
    private String status; // 状态 0:正常 1:停用
    private Integer isDelete; // 0:存在 1:删除
}
```

#### java\org\example\entity\RelTaskPerformance.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelTaskPerformance {
    private Long id; // 关联ID
    private Long taskId; // 任务ID
    private Long perfId; // 指标ID
    private Long yearId; // 绩效年度ID
    private BigDecimal weight; // 权重
    private BigDecimal contributionValue; // 该任务为KPI贡献的数值
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
}
```

#### java\org\example\entity\SysDept.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysDept {
    private Long deptId; // 部门ID
    private String deptName; // 部门名称
    private Long leaderId; // 部门负责人ID
    private String status; // 状态 0:正常 1:停用
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

#### java\org\example\entity\SysFile.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysFile {
    private Long fileId; // 文件ID
    private String fileName; // 文件名
    private String filePath; // 路径
    private String fileUrl; // URL
    private String fileSuffix; // 后缀
    private Long fileSize; // 大小
    private Long uploadBy; // 上传人ID
    private Integer isDelete; // 0:存在 1:删除
    private Date uploadTime; // 上传时间
}
```

#### java\org\example\entity\SysNotice.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysNotice {
    private Long noticeId; // 通知ID
    private Long fromUserId; // 发起人ID
    private Long toUserId; // 接收人ID
    private String type; // 类型
    private String triggerEvent; // 触发事件
    private String title; // 标题
    private String content; // 内容
    private String sourceType; // 关联来源类型
    private Long sourceId; // 关联业务ID
    private String isRead; // 阅读状态 0:未读 1:已读
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
}
```

#### java\org\example\entity\SysUser.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUser {
    private Long userId; // 用户ID
    private Long deptId; // 所属部门ID
    private String userName; // 账号
    private String nickName; // 姓名
    private String email; // 邮箱
    private String password; // 密码
    private String role; // 角色 0:admin 1:user 2:leader
    private String status; // 状态 0:正常 1:停用
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

#### java\org\example\entity\TokenBlacklist.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenBlacklist {
    private String token; // 失效token
    private Date expiryTime; // 过期时间
}
```

#### java\org\example\entity\vo\BizAuditVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditVO {
    private Long subId; // 提交ID
    private Long taskId; // 任务ID
    private Long fileId; // 文件ID
    private String filename; // 文件名

    // 填报数据相关
    private BigDecimal reportedValue; // 本次填报完成值
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
    private Long submitBy; // 提交人ID
    private Long submitDeptId; // 提交人部门ID
    private Long manageDeptId; // 归口部门ID
    private Date submitTime; // 提交时间
    private String fileSuffix; // 后缀（仅允许pdf/doc/docx）

    /**
     * 流程状态枚举：
     * 0:草稿
     * 10:待[所在部门]审批 20:待[归口部门]审批 30:待[管理员]审批
     * 40:已归档
     * -10:被所在部门退回 -20:被归口部门退回 -30:被管理员退回
     */
    private Integer flowStatus;
    private Long currentHandlerId; // 当前处理人ID
    private Integer isDelete; // 0:存在 1:删除


}
```

#### java\org\example\entity\vo\BizTaskVo.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTaskVo {
    private Long taskId; // 任务ID
    private Long projectId; // 项目ID
    private Long parentId; // 父任务节点ID
    private String ancestors; // 祖先节点ID集合
    private Integer phase; // 所属年份
    private String taskCode; // 任务编号
    private String taskName; // 任务名称
    private Integer level; // 任务层级
    private String comment;// 任务描述

    // 组织归属相关
    private Long deptId; // 归口部门ID
    private String deptName;
    private Long principalId; // 归口负责人ID
    private String principalName;
    private Long auditorId;//审核人ID
    private String auditorName;
    private Long leaderId; // 任务负责人ID
    private String leaderName;

    // 数据需求配置相关
    private String expTarget; // 预期达成情况
    private String expLevel; // 预期成果级别（国/省/市）
    private String expEffect; // 预期效果
    private String expMaterialDesc; // 预期过程（佐证）材料清单(文本描述)
    /**
     * 数据类型枚举：
     * 0:对指标没有影响
     * 1:数值(累加)
     * 2:百分比(取大)
     */
    private String dataType;
    private BigDecimal targetValue; // 目标值
    private BigDecimal currentValue; // 当前完成值(缓存统计)
    private BigDecimal weight; // 权重(冗余)
    private Integer progress; // 任务进度(冗余)
    /**
     * 任务状态枚举：
     * 0:未开始 1:进行中
     * 2:审核中 3:已完成
     */
    private String status;
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

#### java\org\example\entity\vo\DashboardSummaryVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 看板数据汇总VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryVO {
    // 整体数据
    private TaskCompletionVO overallCompletion;      // 所有任务完成率
    private TaskCompletionVO yearCompletion;         // 本年度任务完成率
    private TaskCompletionVO midTermCompletion;      // 中期任务完成率

    // 一级任务
    private TaskCompletionVO firstLevelCompletion;   // 一级任务完成率

    // 部门数据
    private List<DeptTaskStatsVO> deptOverallStats;  // 各部门整体完成率
    private List<DeptTaskStatsVO> deptYearStats;     // 各部门本年度完成率
    private List<DeptTaskStatsVO> deptMidTermStats;  // 各部门中期完成率

    // 一级任务详情
    private List<FirstLevelTaskDetailVO> firstLevelTasks;

    // 统计时间
    private String currentYear;
    private String midTermEndYear = "2028";
    private String updateTime;
}
```

#### java\org\example\entity\vo\DeptTaskStatsVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 部门任务统计VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeptTaskStatsVO {
    private Long deptId;
    private String deptName;
    private Integer totalTasks;
    private Integer completedTasks;
    private BigDecimal completionRate;

    // 计算完成率
    public void calculateCompletionRate() {
        if (totalTasks != null && totalTasks > 0) {
            this.completionRate = BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.completionRate = BigDecimal.ZERO;
        }
    }
}
```

#### java\org\example\entity\vo\ErrorVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorVO {
    private String message;
    private Integer code;
}
```

#### java\org\example\entity\vo\FileUploadVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadVO {
    private Long fileId;
    private String filename;
    private String filepath;
}
```

#### java\org\example\entity\vo\FirstLevelTaskDetailVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 一级任务详情VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FirstLevelTaskDetailVO {
    private Long taskId;
    private String taskName;
    private Long deptId;
    private String deptName;
    private String status;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private Integer progress;
    private String createTime;
    private String updateTime;

    // 判断是否完成
    public boolean isCompleted() {
        return "3".equals(status);
    }

    // 获取完成率
    public BigDecimal getCompletionRate() {
        if (targetValue != null && targetValue.compareTo(BigDecimal.ZERO) > 0) {
            return currentValue.multiply(BigDecimal.valueOf(100))
                    .divide(targetValue, 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
```

#### java\org\example\entity\vo\SysLoginVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysLoginVO {

    private String nick_name;
    private String token;
}
```

#### java\org\example\entity\vo\SysLogoutVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysLogoutVO {
    private String message;
}
```

#### java\org\example\entity\vo\SysPasswordVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysPasswordVO {
    private String message;
}
```

#### java\org\example\entity\vo\TaskCompletionVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 任务完成率统计VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCompletionVO {
    private Integer totalTasks;         // 总任务数
    private Integer completedTasks;     // 已完成任务数
    private BigDecimal completionRate;  // 完成率（百分比）
    private String period;             // 统计周期：all, year, midterm
    private String description;        // 描述

    // 方便构造的静态方法
    public TaskCompletionVO(Integer totalTasks, Integer completedTasks, String period, String description) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.period = period;
        this.description = description;

        // 计算完成率
        if (totalTasks != null && totalTasks > 0) {
            this.completionRate = BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.completionRate = BigDecimal.ZERO;
        }
    }
}



```

#### java\org\example\mapper\AchievementMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import org.example.entity.BizAchievement;

import java.util.List;

/**
 * 标志性成果数据访问接口
 * 对应表：biz_achievement
 */
@Mapper
public interface AchievementMapper {

    /**
     * 根据成果ID查询单条成果信息
     * @param achId 成果ID
     * @return 标志性成果对象
     */
    @Select("SELECT * FROM biz_achievement WHERE ach_id = #{achId} AND is_delete = 0")
    BizAchievement getAchievementById(Long achId);

    /**
     * 查询所有未删除的标志性成果列表
     * @return 标志性成果列表
     */
    @Select("SELECT * FROM biz_achievement WHERE is_delete = 0")
    List<BizAchievement> listAllAchievements();


    /**
     * 查询最新的一个标志性成果
     * @return 最新的标志性成果对象
     */
    @Select("SELECT * FROM biz_achievement WHERE is_delete = 0 ORDER BY create_time DESC LIMIT 1")
    BizAchievement getLatestAchievement();

    /**
     * 新增标志性成果（自增主键返回）
     * @param achievement 标志性成果实体
     * @return 自增的成果ID
     */
    @Insert("INSERT INTO biz_achievement (category, level, ach_name, department, got_time, dept_id, file_id, " +
            "comment, is_competition, te_deng_jiang, yi_deng_jiang, er_deng_jiang, san_deng_jiang, jin_jiang, " +
            "yin_jiang, tong_jiang, you_sheng_jiang, bud_deng_deng_ci, create_by, is_delete, create_time) " +
            "VALUES (#{category}, #{level}, #{achName}, #{department}, #{gotTime}, #{deptId}, #{fileId}, " +
            "#{comment}, #{isCompetition}, #{teDengJiang}, #{yiDengJiang}, #{erDengJiang}, #{sanDengJiang}, #{jinJiang}, " +
            "#{yinJiang}, #{tongJiang}, #{youShengJiang}, #{budDengDengCi}, #{createBy}, #{isDelete}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "achId", keyColumn = "ach_id")
    void addAchievement(BizAchievement achievement);

    /**
     * 根据成果ID修改标志性成果信息
     * @param achievement 标志性成果实体（含需修改的成果ID）
     */
    @Update("UPDATE biz_achievement SET category = #{category}, level = #{level}, ach_name = #{achName}, " +
            "department = #{department}, got_time = #{gotTime}, dept_id = #{deptId}, file_id = #{fileId}, " +
            "comment = #{comment}, is_competition = #{isCompetition}, te_deng_jiang = #{teDengJiang}, " +
            "yi_deng_jiang = #{yiDengJiang}, er_deng_jiang = #{erDengJiang}, san_deng_jiang = #{sanDengJiang}, " +
            "jin_jiang = #{jinJiang}, yin_jiang = #{yinJiang}, tong_jiang = #{tongJiang}, you_sheng_jiang = #{youShengJiang}, " +
            "bud_deng_deng_ci = #{budDengDengCi}, create_by = #{createBy} " +
            "WHERE ach_id = #{achId} AND is_delete = 0")
    void updateAchievement(BizAchievement achievement);

    /**
     * 逻辑删除标志性成果（置is_delete=1）
     * @param achId 成果ID
     */
    @Update("UPDATE biz_achievement SET is_delete = 1 WHERE ach_id = #{achId}")
    void deleteAchievement(Long achId);
}
```

#### java\org\example\mapper\BizMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.BizAuditLog;
import org.example.entity.BizMaterialSubmission;
import org.example.entity.BizTask;
import org.example.entity.vo.DeptTaskStatsVO;
import org.example.entity.vo.FirstLevelTaskDetailVO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 业务数据访问接口
 */
@Mapper
public interface BizMapper {

    /**
     * 任务相关操作
     */

    /**
     * 获取全部任务
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task")
    List<BizTask> getAllTasks();

    /**
     * 根据id获取任务
     * @param taskId 任务ID
     * @return 任务对象
     */
    @Select("SELECT * FROM biz_task WHERE task_id = #{taskId}")
    BizTask getTaskById(Long taskId);

    /**
     * 根据部门id获取任务
     * @param deptId 部门ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE dept_id = #{deptId}")
    List<BizTask> getTasksByDeptId(Long deptId);

//    getTasksByDeptIdAndPhase
    @Select("SELECT * FROM biz_task WHERE dept_id = #{deptId} AND phase = #{phase}")
    List<BizTask> getTasksByDeptIdAndPhase(Long deptId, Integer phase);



    /**
     * 根据归口负责人获取任务
     * @param principalId 负责人ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE principal_id = #{principalId}")
    List<BizTask> getTasksByPrincipalId(Long principalId);

    /**
     * 根据负责人ID获取任务
     * @param leaderId 负责人ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE leader_id = #{leaderId}")
    List<BizTask> getTasksByLeaderId(Long leaderId);

    /**
     * 根据负责人ID或归口负责人ID或审核人ID获取任务
     * @param userId 用户ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE leader_id = #{userId} OR principal_id = #{userId} OR auditor_id=#{userId}")
    List<BizTask> getTasks(Long userId);

    /**
     * 获取所有一级任务
     * @return 一级任务列表
     */
    @Select("SELECT * FROM biz_task WHERE level=1")
    List<BizTask> getAllFirstLevelTasks();

    /**
     * 获取当前任务的所有子任务
     * @param taskId 任务ID
     * @return 子任务列表
     */
    @Select("SELECT * FROM biz_task WHERE parent_id = #{taskId}")
    List<BizTask> getAllChildrenTasks(Long taskId);

    /**
     * 根据一级任务id获取其二级子任务
     * @param parentId 父任务ID
     * @return 二级任务列表
     */
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=2")
    List<BizTask> getSecondLevelTasksByParentId(Long parentId);

    /**
     * 根据二级任务id获取其三级子任务
     * @param parentId 父任务ID
     * @return 三级任务列表
     */
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=3")
    List<BizTask> getThirdLevelTasksByParentId(Long parentId);


    /**
     * 根据任务阶段获取任务
     * @param phase 任务阶段
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE phase = #{phase}")
    List<BizTask> getTasksByPhase(Integer phase);

    /**
     * 新增任务
     * @param task 任务实体（taskId会自增，无需传入）
     * @return 影响的行数（1表示新增成功）
     */
    @Insert("INSERT INTO biz_task (" +
            "project_id, parent_id, ancestors, phase, task_code, task_name, level, comment, " +
            "leader_id, auditor_id, principal_id, dept_id, exp_target, exp_level, exp_effect, " +
            "exp_material_desc, data_type, target_value, current_value, weight, progress, " +
            "status, is_delete, create_time, update_time" +
            ") VALUES (" +
            "#{projectId}, #{parentId}, #{ancestors}, #{phase}, #{taskCode}, #{taskName}, #{level}, #{comment}, " +
            "#{leaderId}, #{auditorId}, #{principalId}, #{deptId}, #{expTarget}, #{expLevel}, #{expEffect}, " +
            "#{expMaterialDesc}, #{dataType}, #{targetValue}, #{currentValue}, #{weight}, #{progress}, " +
            "#{status}, #{isDelete}, #{createTime}, #{updateTime}" +
            ")")
    // 新增注解：返回自增的taskId到实体对象中
    @Options(useGeneratedKeys = true, keyProperty = "taskId", keyColumn = "task_id")
    void addTask(BizTask task);

    /**
     * 更新任务
     * 全字段更新任务（根据taskId更新所有字段）
     * @param task 任务实体
     * @return 影响的行数
     */
    @Update("UPDATE biz_task SET project_id=#{projectId},parent_id=#{parentId},ancestors=#{ancestors},phase=#{phase},task_code=#{taskCode},task_name=#{taskName},level=#{level},comment=#{comment},leader_id=#{leaderId},auditor_id=#{auditorId},principal_id=#{principalId},dept_id=#{deptId},exp_target=#{expTarget},exp_level=#{expLevel},exp_effect=#{expEffect},exp_material_desc=#{expMaterialDesc},data_type=#{dataType},target_value=#{targetValue},current_value=#{currentValue},weight=#{weight},progress=#{progress},status=#{status},is_delete=#{isDelete},create_time=#{createTime},update_time=NOW() WHERE task_id=#{taskId}")
    int updateTask(BizTask task);

    /**
     * 提交后更新任务
     * @param task 任务实体
     */
    @Update("UPDATE biz_task SET current_value = #{currentValue}, status = #{status}, update_time = NOW() WHERE task_id = #{taskId}")
    void updateCurrentTask(BizTask task);

    /**
     * 根据任务id获取部门leaderid
     * @param taskId 任务ID
     * @return 负责人ID
     */
    @Select("SELECT leader_id FROM sys_dept WHERE dept_id = (SELECT dept_id FROM biz_task WHERE task_id = #{taskId})")
    Long getTaskLeaderId(Long taskId);

    /**
     * 根据任务id获取归口负责人Id
     * @param taskId 任务ID
     * @return 归口负责人ID
     */
    @Select("SELECT principal_id FROM biz_task WHERE task_id = #{taskId}")
    Long getTaskPrincipalId(Long taskId);

    /**
     * 日志相关操作
     */

    /**
     * 创建审批单日志
     * @param auditLog 审批日志实体
     */
    @Insert("insert into biz_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment,create_time) values(#{subId},#{operatorId},#{actionType},#{preStatus},#{postStatus},#{comment},#{createTime})")
    void createAuditLog(BizAuditLog auditLog);

    /**
     * 更新日志
     * @param auditLog 审批日志实体
     */
    @Update("update biz_audit_log set sub_id=#{subId},operator_id=#{operatorId},action_type=#{actionType},pre_status=#{preStatus},post_status=#{postStatus},comment=#{comment},create_time=#{createTime} where log_id=#{logId}")
    void updateAuditLog(BizAuditLog auditLog);

    /**
     * 根据 subId 获取审批操作日志（biz_audit_log）
     * @param subId 提交ID
     * @return 审批日志列表
     */
    @Select("SELECT " +
            "log_id AS logId, " +
            "sub_id AS subId, " +
            "operator_id AS operatorId, " +
            "action_type AS actionType, " +
            "pre_status AS preStatus, " +
            "post_status AS postStatus, " +
            "comment, " +
            "create_time AS createTime " +
            "FROM biz_audit_log WHERE sub_id = #{subId} " +
            "ORDER BY create_time DESC, log_id DESC")
    List<BizAuditLog> getAuditLogsBySubId(@Param("subId") Long subId);

    /**
     * 材料提交相关操作
     */

    /**
     * 创建审批流程
     * @param bizMaterialSubmission 材料提交实体
     * @return 提交ID
     */
    @Insert("insert into biz_material_submission(" +
            "task_id, file_id, reported_value, data_type, submit_by, submit_dept_id, " +
            "manage_dept_id, submit_time, file_suffix, flow_status, current_handler_id, is_delete" +
            ") values(" +
            "#{taskId}, #{fileId}, #{reportedValue}, #{dataType}, #{submitBy}, #{submitDeptId}, " +
            "#{manageDeptId}, #{submitTime}, #{fileSuffix}, #{flowStatus}, #{currentHandlerId}, #{isDelete}" +
            ")")
    void createAudit(BizMaterialSubmission bizMaterialSubmission);

    /**
     * 获取最新的审批单id
     * @return 最新的提交ID
     */
    @Select("SELECT sub_id FROM biz_material_submission ORDER BY sub_id DESC LIMIT 1")
    Long getNewestSubId();

    /**
     * 更新审批单 【核心修复点1：注解改为@Update；核心修复点2：where前加空格】
     * @param bizMaterialSubmission 材料提交实体
     */
    @Update("update biz_material_submission set task_id=#{taskId},file_id=#{fileId},reported_value=#{reportedValue},data_type=#{dataType},"
            + "submit_by=#{submitBy},submit_dept_id=#{submitDeptId},manage_dept_id=#{manageDeptId},submit_time=#{submitTime},file_suffix=#{fileSuffix},"
            + "flow_status=#{flowStatus},current_handler_id=#{currentHandlerId},is_delete=#{isDelete} " + // 此处添加空格
            "where sub_id=#{subId}")
    void updateAudit(BizMaterialSubmission bizMaterialSubmission);

    /**
     * 更新任务状态 【优化：参数名统一为驼峰风格，与Java规范一致】
     * @param taskId 任务ID
     * @param status 状态
     */
    @Update("UPDATE biz_task SET status = #{status} WHERE task_id = #{taskId}")
    void updateTaskStatus(@Param("taskId") Long taskId, @Param("status") String status);

    /**
     * 更新审批单flow_status 【优化：参数名统一为驼峰风格】
     * @param subId 提交ID
     * @param flowStatus 流程状态
     */
    @Update("UPDATE biz_material_submission SET flow_status = #{flowStatus} WHERE sub_id = #{subId}")
    void updateAuditFlowStatus(@Param("subId") Long subId, @Param("flowStatus") Integer flowStatus);

    /**
     * 根据taskId及current_handler_id获取审批单
     * @param taskId 任务ID
     * @param currentHandlerId 当前处理人ID
     * @return 材料提交列表
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND current_handler_id = #{currentHandlerId} AND is_delete = 0")
    List<BizMaterialSubmission> getAudit(@Param("taskId") Long taskId,
                                         @Param("currentHandlerId") Long currentHandlerId);

    /**
     * 根据任务id获取最新的审批单
     * @param taskId 任务ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND is_delete = 0 ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getNewestAudit(@Param("taskId") Long taskId);

    /**
     * 获取"待我审批"的审批单（按当前处理人查询）
     * @param userId 用户ID
     * @return 待审批列表
     */
    @Select("SELECT * FROM biz_material_submission " +
            "WHERE current_handler_id = #{userId} AND is_delete = 0 AND flow_status >= 10 AND flow_status < 40 AND is_delete = 0 " +
            "ORDER BY submit_time DESC, sub_id DESC")
    List<BizMaterialSubmission> getTodoAudits(@Param("userId") Long userId);

    /**
     * 按 task_id 获取该任务全部审批单（用于任务详情抽屉展示完整流程）
     * @param taskId 任务ID
     * @return 审批单列表
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND is_delete = 0 ORDER BY sub_id DESC")
    List<BizMaterialSubmission> getAuditsByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据subId获取审批单
     * @param subId 提交ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE sub_id = #{subId} AND is_delete = 0")
    BizMaterialSubmission getAuditBySubId(Long subId);

    /**
     * 根据subId获取提交人
     * @param subId 提交ID
     * @return 提交人ID
     */
    @Select("SELECT submit_by FROM biz_material_submission WHERE sub_id = #{subId} AND is_delete = 0")
    Long getAuditSubmitBy(Long subId);

    /**
     * 根据任务id获取最近的审批单
     * @param taskId 任务ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND is_delete = 0 ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getLatestAuditByTaskId(Long taskId);

    /**
     * 根据任务id获取最近的、且状态为40的审批单
     * @param taskId 任务ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND flow_status = 40 AND is_delete = 0 ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getLatestApprovedAuditByTaskId(Long taskId);

    /**
     * 获取所有待审核的审批单（flow_status在10-30之间）
     * @return 待审核审批单列表
     */
    @Select("SELECT * FROM biz_material_submission " +
            "WHERE flow_status >= 10 AND flow_status < 40 AND is_delete = 0 " +
            "ORDER BY submit_time DESC")
    List<BizMaterialSubmission> getAllPendingAudits();

    /**
     * 根据处理人ID统计待审核任务数量
     * @param handlerId 处理人ID
     * @return 待审核任务数量
     */
    @Select("SELECT COUNT(*) FROM biz_material_submission " +
            "WHERE current_handler_id = #{handlerId} " +
            "AND flow_status >= 10 AND flow_status < 40 AND is_delete = 0")
    Integer countPendingAuditsByHandler(@Param("handlerId") Long handlerId);


    // ... 原有的其他方法 ...

    /**
     * 获取所有任务数量
     * @return 任务总数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE is_delete = 0")
    Integer getTotalTaskCount();

    /**
     * 获取已完成任务数量
     * @return 已完成任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE status = '3' AND is_delete = 0")
    Integer getCompletedTaskCount();

    /**
     * 获取本年度任务数量
     * @param year 年份
     * @return 本年度任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase = #{year} AND is_delete = 0")
    Integer getYearTaskCount(@Param("year") Integer year);

    /**
     * 获取本年度已完成任务数量
     * @param year 年份
     * @return 本年度已完成任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase = #{year} AND status = '3' AND is_delete = 0")
    Integer getYearCompletedTaskCount(@Param("year") Integer year);

    /**
     * 获取中期任务数量（phase在2028年之前）
     * @param endYear 截止年份
     * @return 中期任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase < #{endYear} AND is_delete = 0")
    Integer getMidTermTaskCount(@Param("endYear") Integer endYear);

    /**
     * 获取中期已完成任务数量
     * @param endYear 截止年份
     * @return 中期已完成任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase < #{endYear} AND status = '3' AND is_delete = 0")
    Integer getMidTermCompletedTaskCount(@Param("endYear") Integer endYear);

    /**
     * 获取一级任务数量
     * @return 一级任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE level = 1 AND is_delete = 0")
    Integer getFirstLevelTaskCount();

    /**
     * 获取一级已完成任务数量
     * @return 一级已完成任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE level = 1 AND status = '3' AND is_delete = 0")
    Integer getFirstLevelCompletedTaskCount();


    /**
     * 获取部门任务数量统计
     * @return 部门任务统计列表
     */
    @Select("SELECT " +
            "d.dept_id as deptId, " +
            "d.dept_name as deptName, " +
            "COUNT(t.task_id) as totalTasks, " +
            "SUM(CASE WHEN t.status = '3' THEN 1 ELSE 0 END) as completedTasks " +
            "FROM sys_dept d " +
            "LEFT JOIN biz_task t ON d.dept_id = t.dept_id AND t.is_delete = 0 " +
            "WHERE d.is_delete = 0 " +
            "GROUP BY d.dept_id, d.dept_name " +
            "ORDER BY d.dept_id")
    List<DeptTaskStatsVO> getDeptTaskStats();

    /**
     * 获取部门本年度任务统计
     * @param year 年份
     * @return 部门本年度任务统计
     */
    @Select("SELECT " +
            "d.dept_id as deptId, " +
            "d.dept_name as deptName, " +
            "COUNT(t.task_id) as totalTasks, " +
            "SUM(CASE WHEN t.status = '3' THEN 1 ELSE 0 END) as completedTasks " +
            "FROM sys_dept d " +
            "LEFT JOIN biz_task t ON d.dept_id = t.dept_id AND t.phase = #{year} AND t.is_delete = 0 " +
            "WHERE d.is_delete = 0 " +
            "GROUP BY d.dept_id, d.dept_name " +
            "ORDER BY d.dept_id")
    List<DeptTaskStatsVO> getDeptYearTaskStats(@Param("year") Integer year);

    /**
     * 获取部门中期任务统计（phase在2028年之前）
     * @param endYear 截止年份
     * @return 部门中期任务统计
     */
    @Select("SELECT " +
            "d.dept_id as deptId, " +
            "d.dept_name as deptName, " +
            "COUNT(t.task_id) as totalTasks, " +
            "SUM(CASE WHEN t.status = '3' THEN 1 ELSE 0 END) as completedTasks " +
            "FROM sys_dept d " +
            "LEFT JOIN biz_task t ON d.dept_id = t.dept_id AND t.phase < #{endYear} AND t.is_delete = 0 " +
            "WHERE d.is_delete = 0 " +
            "GROUP BY d.dept_id, d.dept_name " +
            "ORDER BY d.dept_id")
    List<DeptTaskStatsVO> getDeptMidTermTaskStats(@Param("endYear") Integer endYear);

    /**
     * 获取一级任务的详细完成情况
     * @return 一级任务完成情况列表
     */
    @Select("SELECT " +
            "task_id as taskId, " +
            "task_name as taskName, " +
            "dept_id as deptId, " +
            "(SELECT dept_name FROM sys_dept WHERE dept_id = t.dept_id) as deptName, " +
            "status, " +
            "target_value as targetValue, " +
            "current_value as currentValue, " +
            "progress, " +
            "create_time as createTime, " +
            "update_time as updateTime " +
            "FROM biz_task t " +
            "WHERE level = 1 AND is_delete = 0 " +
            "ORDER BY task_id")
    List<FirstLevelTaskDetailVO> getFirstLevelTaskDetails();
}
```

#### java\org\example\mapper\SysMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import org.example.entity.*;

import java.util.List;

/**
 * 系统数据访问接口
 */
@Mapper
public interface SysMapper {
    /**
     * 获取所有用户
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user")
    public List<SysUser> getAllUsers();


    /**
     * 获取所有用户ID
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM sys_user")
    public List<Long> getAllUserIds();

    /**
     * 根据id获取部门
     * @param deptId 部门ID
     * @return 部门对象
     */
    @Select("SELECT * FROM sys_dept WHERE dept_id = #{deptId}")
    public SysDept getDeptById(Long deptId);

    /**
     * 根据userId获取部门
     * @param userId 用户ID
     * @return 部门对象
     */
    @Select("SELECT * FROM sys_dept WHERE dept_id = (SELECT dept_id FROM sys_user WHERE user_id = #{userId})")
    public SysDept getDeptByUserId(Long userId);

    /**
     * 根据userId获取部门负责人id
     * @param userId 用户ID
     * @return 负责人ID
     */
    @Select("SELECT leader_id FROM sys_dept WHERE dept_id = (SELECT dept_id FROM sys_user WHERE user_id = #{userId})")
    public Long getDeptLeaderId(Long userId);

    /**
     * 根据部门ID获取部门名称
     * @param deptId 部门ID
     * @return 部门名称
     */
    @Select("SELECT dept_name FROM sys_dept WHERE dept_id = #{deptId}")
    public String getDeptNameByDeptId(Long deptId);


//    获取所有的部门负责人ID
    @Select("SELECT leader_id FROM sys_dept")
    public List<Long> getAllDeptLeaders();

    /**
     * 根据id获取用户
     * @param userId 用户ID
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE user_id = #{userId}")
    public SysUser getUserById(Long userId);

    /**
     * 根据用户名获取用户
     * @param userName 用户名
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE user_name = #{userName}")
    public SysUser getUserByName(String userName);

    /**
     * 根据昵称获取用户
     * @param nickName 昵称
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE nick_name = #{nickName}")
    public SysUser getUserByNickName(String nickName);

    /**
     * 添加用户
     * userId手动添加而非自增
     * @param user 用户实体
     * @return 用户ID
     */
    @Insert("INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, email, password, role, status, is_delete, create_time, update_time) VALUES (#{userId}, #{deptId}, #{userName}, #{nickName}, #{email}, #{password}, #{role}, #{status}, #{isDelete}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    public void addUser(SysUser user);

    /**
     * 修改用户信息
     * @param user 用户实体
     */
    @Update("UPDATE sys_user SET dept_id = #{deptId}, user_name = #{userName}, nick_name = #{nickName}, email = #{email}, password = #{password}, role = #{role}, status = #{status}, update_time = #{updateTime} WHERE user_id = #{userId}")
    public void updateUser(SysUser user);

    /**
     * 删除用户
     * @param userId 用户ID
     */
    @Update("UPDATE sys_user SET is_delete = 1 WHERE user_id = #{userId}")
    public void deleteUser(Long userId);

    /**
     * 上传文件
     * @param file 文件实体
     * @return 文件ID
     */
    @Insert("INSERT INTO sys_file (file_name, file_path, file_url, file_suffix, file_size, upload_by, is_delete, upload_time) VALUES (#{fileName}, #{filePath}, #{fileUrl}, #{fileSuffix}, #{fileSize}, #{uploadBy}, #{isDelete}, #{uploadTime})")
    @Options(useGeneratedKeys = true, keyProperty = "fileId", keyColumn = "file_id")
    public void uploadFile(SysFile file);

    /**
     * 根据名称查询文件
     * @param fileName 文件名
     * @return 文件对象
     */
    @Select("SELECT * FROM sys_file WHERE file_name = #{fileName}")
    public SysFile getFileByName(String fileName);

    /**
     * 根据ID查询文件
     * @param fileId 文件ID
     * @return 文件对象
     */
    @Select("SELECT * FROM sys_file WHERE file_id = #{fileId}")
    public SysFile getFileById(Long fileId);

    /**
     * 发送通知（带返回通知ID）
     * @param notice 通知实体
     * @return 通知ID
     */
    @Insert("INSERT INTO sys_notice (from_user_id, to_user_id, type, trigger_event, title, content, source_type, source_id, is_read, is_delete, create_time) " +
            "VALUES (#{fromUserId}, #{toUserId}, #{type}, #{triggerEvent}, #{title}, #{content}, #{sourceType}, #{sourceId}, #{isRead}, #{isDelete}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "noticeId", keyColumn = "notice_id")
    void sendNotice(SysNotice notice);
    /**
     * 查看当前用户收到的信息
     * @param userId 用户ID
     * @return 通知列表
     */
    @Select("SELECT * FROM sys_notice WHERE to_user_id = #{userId}")
    public List<SysNotice> getNotices(Long userId);

    /**
     * 根据ID获取通知
     * @param noticeId 通知ID
     * @return 通知对象
     */
    @Select("SELECT * FROM sys_notice WHERE notice_id = #{noticeId}")
    public SysNotice getNoticeById(Long noticeId);

    /**
     * 设为已读
     * @param noticeId 通知ID
     */
    @Update("UPDATE sys_notice SET is_read = 1 WHERE notice_id = #{noticeId}")
    public void setRead(Long noticeId);
}
```

#### java\org\example\mapper\TokenBlacklistMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.TokenBlacklist;

/**
 * Token黑名单数据访问接口
 */
@Mapper
public interface TokenBlacklistMapper {
    /**
     * 添加Token到黑名单
     * @param tokenBlacklist Token黑名单实体
     */
    @Insert("INSERT INTO token_blacklist(token, expiry_time) VALUES(#{token}, #{expiryTime})")
    void addToBlacklist(TokenBlacklist tokenBlacklist);

    /**
     * 根据Token查询黑名单
     * @param token Token字符串
     * @return Token黑名单实体
     */
    @Select("SELECT * FROM token_blacklist WHERE token = #{token}")
    TokenBlacklist findByToken(String token);

    /**
     * 清理过期的Token
     */
    @Select("DELETE FROM token_blacklist WHERE expiry_time < NOW()")
    void cleanupExpiredTokens();
}
```

#### java\org\example\mapper\TrendDataMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.BizTrendData;
import java.util.List;

@Mapper
public interface TrendDataMapper {

    /**
     * 插入趋势数据
     */
    @Insert("INSERT INTO biz_trend_data (year, month, day, completion_rate) " +
            "VALUES (#{year}, #{month}, #{day}, #{completionRate})")
    void insertTrendData(BizTrendData trendData);

    /**
     * 根据年份获取所有数据
     */
    @Select("SELECT * FROM biz_trend_data WHERE year = #{year} AND is_delete = 0 " +
            "ORDER BY year DESC, month DESC, day DESC")
    List<BizTrendData> getTrendDataByYear(@Param("year") Integer year);

    /**
     * 获取最近一年的数据
     */
    @Select("SELECT * FROM biz_trend_data WHERE year = #{year} AND is_delete = 0 " +
            "ORDER BY month, day LIMIT 365")
    List<BizTrendData> getLatestTrendData(@Param("year") Integer year);

    /**
     * 检查今天是否已记录
     */
    @Select("SELECT COUNT(*) FROM biz_trend_data " +
            "WHERE year = YEAR(CURDATE()) " +
            "AND month = MONTH(CURDATE()) " +
            "AND day = DAY(CURDATE()) " +
            "AND is_delete = 0")
    Integer checkTodayRecorded();
}
```

#### java\org\example\service\AchievementService.java

```java
package org.example.service;

import org.example.entity.BizAchievement;
import org.example.mapper.AchievementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 标志性成果业务服务类
 * 处理标志性成果的基础增删查改业务逻辑
 */
@Service
public class AchievementService {

    /**
     * 注入成果Mapper接口，操作数据库
     */
    @Autowired
    private AchievementMapper achievementMapper;

    /**
     * 根据成果ID查询未删除的标志性成果
     * @param achId 成果ID
     * @return 标志性成果实体
     */
    public BizAchievement getAchievementById(Long achId) {
        // 参数非空校验
        if (achId == null) {
            throw new RuntimeException("成果ID不能为空");
        }
        // 查询成果
        BizAchievement achievement = achievementMapper.getAchievementById(achId);
        // 成果不存在校验
        if (achievement == null) {
            throw new RuntimeException("标志性成果不存在或已被删除");
        }
        return achievement;
    }

    /**
     * 查询所有未删除的标志性成果列表
     * @return 标志性成果实体列表
     */
    public List<BizAchievement> listAllAchievements() {
        try {
            return achievementMapper.listAllAchievements();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 新增标志性成果
     * 自动填充创建时间、默认删除状态，返回自增成果ID
     * @param achievement 标志性成果实体（含核心业务字段）
     * @return 自增的成果ID
     */
    public Long addAchievement(BizAchievement achievement,Long createBy) {
        // 核心参数非空校验
        if (achievement == null) {
            throw new RuntimeException("新增成果信息不能为空");
        }
        if (achievement.getCategory() == null) {
            throw new RuntimeException("成果类别不能为空");
        }
        if (achievement.getLevel() == null || achievement.getLevel().trim().isEmpty()) {
            throw new RuntimeException("成果级别不能为空");
        }
        if (achievement.getAchName() == null || achievement.getAchName().trim().isEmpty()) {
            throw new RuntimeException("成果名称不能为空");
        }
        if (achievement.getGotTime() == null) {
            throw new RuntimeException("成果颁发时间不能为空");
        }
        // 自动填充公共字段
        achievement.setIsDelete(0); // 默认未删除
        achievement.setCreateTime(new Date()); // 填充当前创建时间
        try {
            // 调用Mapper新增，返回自增ID
            achievementMapper.addAchievement(achievement);
            return achievementMapper.getLatestAchievement().getAchId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改标志性成果信息
     * 仅修改未删除的成果，根据成果ID更新
     * @param achievement 标志性成果实体（含成果ID和需修改字段）
     */
    public void updateAchievement(Long id,BizAchievement achievement) {
        // 核心参数非空校验
        if (achievement == null) {
            throw new RuntimeException("修改成果信息不能为空");
        }
        if (achievementMapper.getAchievementById(id) == null) {
            throw new RuntimeException("成果ID不存在，无法修改");
        }
        try {
            achievementMapper.updateAchievement(achievement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 逻辑删除标志性成果
     * 置is_delete=1，并非物理删除
     * @param achId 成果ID
     */
    public void deleteAchievement(Long achId) {
        // 参数非空校验
        if (achId == null) {
            throw new RuntimeException("成果ID不能为空");
        }
        // 校验成果是否存在
        if (achievementMapper.getAchievementById(achId) == null) {
            throw new RuntimeException("标志性成果不存在或已被删除，无法删除");
        }
        try {
            achievementMapper.deleteAchievement(achId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

#### java\org\example\service\BizService.java

```java
package org.example.service;

import org.example.entity.*;
import org.example.entity.dto.BizAuditDTO;
import org.example.entity.dto.BizSubDTO;
import org.example.entity.dto.BizReSubDTO;
import org.example.entity.dto.BizTaskDTO;
import org.example.entity.vo.*;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 业务服务类：处理所有业务逻辑
 */
@Service
public class BizService {

    /** 管理员ID */
    private Long ADMIN_ID = 110228L;

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private SysMapper sysMapper;

    /**
     * 获取全部任务
     * @return 任务列表
     */
    public List<BizTask> getAllTasks() {
        return bizMapper.getAllTasks();
    }

    /**
     * 根据id获取任务
     * @param taskId 任务ID
     * @return 任务对象
     */
    public BizTask getTaskById(Long taskId) {
        try {
            if (taskId == null) {
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            return bizMapper.getTaskById(taskId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取当前任务的所有子任务
     * @param taskId 任务ID
     * @return 子任务列表
     */
    public List<BizTask> getAllChildrenTasks(Long taskId) {
        try {
            if (taskId == null) {
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            return bizMapper.getAllChildrenTasks(taskId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取当前任务的父任务
     * @param taskId 任务ID
     * @return 父任务对象
     */
    public BizTask getParentTask(Long taskId) {
        try {
            if (taskId == null) {
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            Long parentId = bizMapper.getTaskById(taskId).getParentId();
            return bizMapper.getTaskById(parentId);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据部门ID获取任务
     * @param deptId 部门ID
     * @return 任务列表
     */
    public List<BizTask> getTasksByDeptId(Long deptId) {
        try {
            return bizMapper.getTasksByDeptId(deptId);
        } catch (Exception e) {
            throw new RuntimeException("获取任务失败,请检查部门id是否正确");
        }
    }

    /**
     * 根据用户角色获取任务
     * 角色为0和2返回所有任务，角色为1返回leaderid以及为自己的任务
     * @param userId 用户ID
     * @return 任务视图对象列表
     */
    public List<BizTaskVo> getTasksByUserRole(Long userId) {
        try {
            SysUser sysUser = sysMapper.getUserById(userId);
            if (sysUser == null) {
                throw new RuntimeException("用户不存在");
            }

            if (sysUser.getRole().equals("0")) {
                return taskListToTaskVoList(bizMapper.getAllTasks());
            } else if (sysUser.getRole().equals("1")) {
                return taskListToTaskVoList(bizMapper.getTasks(sysUser.getUserId()));
            } else if (sysUser.getRole().equals("2")) {
                return taskListToTaskVoList(bizMapper.getTasksByPrincipalId(sysUser.getUserId()));
            } else {
                throw new RuntimeException("用户角色错误");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加任务
     * 只能添加三级任务,根据parent字段判断二级任务是否正确
     * @param taskDTO 任务数据
     */
    public void addTask(BizTaskDTO taskDTO) {
        try {
            // 只能添加三级任务,根据parent字段判断二级任务是否正确
            if (bizMapper.getTaskById(taskDTO.getParentId()) == null) {
                throw new RuntimeException("该二级任务不存在");
            }
            if (bizMapper.getTaskById(taskDTO.getParentId()).getLevel() != 2) {
                throw new RuntimeException("该任务不是二级任务,无法添加");
            }
            if (bizMapper.getTaskById(taskDTO.getParentId()).getDeptId() != taskDTO.getDeptId()) {
                throw new RuntimeException("该任务所属部门与二级任务部门不一致");
            }
            if(taskDTO.getProjectId()!=1){
                throw new RuntimeException("该任务所属项目id不为1");
            }

            BizTask task = taskDTO2Task(taskDTO);
            task.setIsDelete(0);
            task.setCreateTime(new Date());
            task.setUpdateTime(new Date());
            bizMapper.addTask(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新任务
     * @param taskDTO 任务数据
     */
    public void updateTask(BizTaskDTO taskDTO) {
        try {
            if (bizMapper.getTaskById(taskDTO.getTaskId()) == null) {
                throw new RuntimeException("该任务不存在");
            }
            BizTask task = taskDTO2Task(taskDTO);
            task.setUpdateTime(new Date());
            bizMapper.updateTask(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 完成任务
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public String finishTask(Long taskId, Long userId){
        try {
            if (userId == null) {
                throw new RuntimeException("用户ID为空");
            }
            SysUser sysUser = sysMapper.getUserById(userId);
            if (sysUser == null) {
                throw new RuntimeException("用户不存在");
            }
            if (!sysUser.getRole().equals("0")) {
                throw new RuntimeException("仅限管理员访问");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            if (bizMapper.getTaskById(taskId).getStatus().equals("2")) {
                throw new RuntimeException("该任务正在审核中，请先完成审核");
            }
            if (bizMapper.getTaskById(taskId).getStatus().equals("3")) {
                throw new RuntimeException("该任务已完成，请勿重复提交");
            }
            BizTask taskById = bizMapper.getTaskById(taskId);
            taskById.setStatus("3");
            taskById.setCurrentValue(taskById.getTargetValue());
            bizMapper.updateTask(taskById);
            sendNotice(userId, taskById.getLeaderId(), "任务完成", "任务已完成", "任务"+taskById.getTaskName()+"已完成", "0", taskId);
            createAuditLog(taskId, userId, "任务完成", 1, 3, "任务完成");
            return "任务"+taskById.getTaskName()+"已完成";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 提交材料
     * @param bizSubDTO 提交数据
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public String submitMaterial(BizSubDTO bizSubDTO, Long userId) {
        try {
            // 检查taskid是否存在
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()) == null) {
                throw new RuntimeException("该任务不存在");
            }
            // 验证任务必须为三级任务，否则无法提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getLevel() != 3) {
                throw new RuntimeException("该任务不是三级任务,无法提交");
            }
            // 验证任务状态，如果当前status为2，则禁止提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getStatus().equals("2")) {
                throw new RuntimeException("该任务状态未开始或正在审核中,无法提交");
            }
            // 验证任务状态，如果当前status为3，则禁止提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getStatus().equals("3")) {
                throw new RuntimeException("该任务状态已完成,无法提交");
            }
            // 检查文件是否存在
            SysFile sysFile = sysMapper.getFileById(bizSubDTO.getFile_id());
            if (sysFile == null) {
                throw new RuntimeException("该文件不存在");
            }
            // 验证文件后缀，只能为pdf,doc,docx
            if (!sysFile.getFileName().endsWith(".pdf") && !sysFile.getFileName().endsWith(".doc")
                    && !sysFile.getFileName().endsWith(".docx")) {
                throw new RuntimeException("文件格式错误,请上传pdf,doc,docx格式的文件");
            }

            BizTask task = bizMapper.getTaskById(bizSubDTO.getTask_id());
            BizMaterialSubmission bizMaterialSubmission = new BizMaterialSubmission();
            bizMaterialSubmission.setTaskId(bizSubDTO.getTask_id());
            bizMaterialSubmission.setFileId(sysMapper.getFileByName(sysFile.getFileName()).getFileId());

            // 本次填报值只保留整数，并写入任务 current_value（过程即显示进度）
            BigDecimal rv = bizSubDTO.getReported_value() != null ? bizSubDTO.getReported_value() : BigDecimal.ZERO;
            rv = rv.setScale(0, RoundingMode.HALF_UP);
            bizMaterialSubmission.setReportedValue(rv);
            bizMaterialSubmission.setDataType(bizSubDTO.getData_type());
            bizMaterialSubmission.setSubmitBy(userId);
            bizMaterialSubmission.setSubmitDeptId(sysMapper.getUserById(userId).getDeptId());
            bizMaterialSubmission.setManageDeptId(task.getDeptId());
            bizMaterialSubmission.setSubmitTime(new Date());
            bizMaterialSubmission.setFileSuffix(sysMapper.getFileByName(sysFile.getFileName()).getFileSuffix());
            bizMaterialSubmission.setFlowStatus(10);
            // 已修改，修改内容及原因：将部门审核人从任务的AuditorId改为提交人所在部门的负责人，确保flowStatus=10时审核人能正确收到通知
            // flowStatus = 10 表示"待[所在部门]审批"
            Long handlerId = task.getAuditorId();
            bizMaterialSubmission.setCurrentHandlerId(handlerId);
            bizMaterialSubmission.setIsDelete(0);

            bizMapper.createAudit(bizMaterialSubmission);
            Long subId = bizMapper.getNewestSubId();

            // 提交后任务进入"审核中"，并把 current_value 覆盖写为本次填报值
            BizTask bizTask = bizMapper.getTaskById(bizSubDTO.getTask_id());
            if (bizTask != null) {
                bizTask.setCurrentValue(rv);
                bizTask.setStatus("2");
                bizTask.setComment(bizSubDTO.getComment());
                bizTask.setUpdateTime(new Date());
                bizMapper.updateTask(bizTask);
            }

            // 发送审批信息（使用封装方法）
            // 已修改，修改内容及原因：添加null检查，避免handlerId为null时发送通知导致数据库约束错误
            // 只有当 handlerId 不为 null 时才发送通知
            if (handlerId != null) {
                sendNotice(userId,
                        handlerId,
                        "任务审核",
                        "任务审核",
                        "您有新的任务需要审核",
                        "0",
                        bizSubDTO.getTask_id());
            }

            // 创建审批日志（使用封装方法）
            createAuditLog(subId, userId, "提交", 0, 10, "提交任务");
            // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
            String resultMsg = "提交成功";
            if (handlerId != null) {
                try {
                    SysUser handler = sysMapper.getUserById(handlerId);
                    if (handler != null) {
                        resultMsg += "，下一位审批人是" + handler.getUserName();
                    } else {
                        resultMsg += "，下一位审批人ID为" + handlerId;
                    }
                } catch (Exception e) {
                    resultMsg += "，下一位审批人ID为" + handlerId;
                }
            }
            return resultMsg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据taskId查询审批单
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> getAudit(Long taskId, Long userId) {
        try {
            return auditListToAuditVoList(bizMapper.getAudit(taskId, userId));
        } catch (RuntimeException e) {
            throw new RuntimeException("获取审批单失败,请检查任务id是否正确");
        }
    }

    /**
     * 获取"待我审批"的审批单（按 current_handler_id 查询）
     * @param userId 用户ID
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> getTodoAudits(Long userId) {
        try {
            if (userId == null)
                throw new RuntimeException("userId为空");
            return auditListToAuditVoList(bizMapper.getTodoAudits(userId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据 taskId 查询该任务全部审批单（用于任务详情抽屉展示完整流程）
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> getAuditByTaskId(Long taskId, Long userId) {
        try {
            if (taskId == null)
                throw new RuntimeException("taskId为空");
            BizTask task = bizMapper.getTaskById(taskId);
            if (task == null)
                throw new RuntimeException("该任务不存在");

            SysUser me = sysMapper.getUserById(userId);
            if (me == null)
                throw new RuntimeException("用户不存在");

            // 最小权限校验：管理员/任务负责人/归口负责人可查看；或本人提交过该任务审批单也可查看
            if ("0".equals(me.getRole())
                    || (task.getLeaderId() != null && task.getLeaderId().equals(userId))
                    || (task.getAuditorId() != null && task.getAuditorId().equals(userId))
                    || (task.getPrincipalId() != null && task.getPrincipalId().equals(userId))) {
                return auditListToAuditVoList(bizMapper.getAuditsByTaskId(taskId));
            }

            List<BizAuditVO> list = auditListToAuditVoList(bizMapper.getAuditsByTaskId(taskId));
            boolean submittedByMe = false;
            for (BizAuditVO s : list) {
                if (s != null && s.getSubmitBy() != null && s.getSubmitBy().equals(userId)) {
                    submittedByMe = true;
                    break;
                }
            }
            if (!submittedByMe) {
                throw new RuntimeException("无权限查看该任务的审批记录");
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据 subId 查询审批操作日志（biz_audit_log）
     * @param subId 提交ID
     * @param userId 用户ID
     * @return 审批日志列表
     */
    public List<BizAuditLog> getAuditLogs(Long subId, Long userId) {
        try {
            if (subId == null)
                throw new RuntimeException("subId为空");
            BizMaterialSubmission submission = bizMapper.getAuditBySubId(subId);
            if (submission == null)
                throw new RuntimeException("审批单不存在");

            BizTask task = bizMapper.getTaskById(submission.getTaskId());
            if (task == null)
                throw new RuntimeException("任务不存在");

            SysUser me = sysMapper.getUserById(userId);
            if (me == null)
                throw new RuntimeException("用户不存在");

            // 最小权限校验：管理员/任务部门负责人/归口负责人/提交人可查看
            boolean allowed = "0".equals(me.getRole())
                    || (task.getLeaderId() != null && task.getLeaderId().equals(userId))
                    || (task.getAuditorId() != null && task.getAuditorId().equals(userId))
                    || (task.getPrincipalId() != null && task.getPrincipalId().equals(userId))
                    || (submission.getSubmitBy() != null && submission.getSubmitBy().equals(userId));
            if (!allowed) {
                throw new RuntimeException("无权限查看该审批单的操作日志");
            }

            return bizMapper.getAuditLogsBySubId(subId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 审批任务
     * @param bizAuditDTO 审批数据
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public Object audit(BizAuditDTO bizAuditDTO, Long userId) {
        Long subId = bizAuditDTO.getSub_id();
        try {
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(subId);
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }

            // 已修改，修改内容及原因：添加currentHandlerId的null检查，避免在equals比较时出现空指针异常
            // 检查 currentHandlerId 是否为 null
            Long currentHandlerId = bizMaterialSubmission.getCurrentHandlerId();
            if (currentHandlerId == null) {
                throw new RuntimeException("审批单的当前处理人未设置，无法进行审核");
            }

            Map<Integer, Long> nextHandlerMap = Map.of(
                    10, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    20, ADMIN_ID);

            // 已修改，修改内容及原因：安全获取任务的AuditorId，避免getTaskById返回null时出现空指针异常
            // 安全获取任务的 AuditorId
            BizTask task = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
            Long taskAuditorId = (task != null) ? task.getAuditorId() : null;

            Map<Integer, Long> backHandlerMap = Map.of(
                    10, bizMaterialSubmission.getSubmitBy(),
                    20, taskAuditorId != null ? taskAuditorId : bizMaterialSubmission.getSubmitBy(),
                    30, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    -20, bizMaterialSubmission.getSubmitBy(),
                    -30, taskAuditorId != null ? taskAuditorId : bizMaterialSubmission.getSubmitBy());

            // 分支1：当前用户是处理人
            if (currentHandlerId.equals(userId)) {
                if (bizMaterialSubmission.getFlowStatus() == 10) {
                    // 部门负责人审核逻辑
                    if (bizAuditDTO.getIs_pass()) {
                        Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, nextHandlerId, 20);

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                nextHandlerId,
                                "任务审核",
                                "任务审核",
                                "您有新的任务需要审核",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "通过", 10, 20, bizAuditDTO.getTitle());

                        System.out.println("已审批，下一位审批人id为" + nextHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已审批";
                        if (nextHandlerId != null) {
                            try {
                                SysUser nextUser = sysMapper.getUserById(nextHandlerId);
                                if (nextUser != null) {
                                    resultMsg += "，下一位审批人为" + nextUser.getUserName();
                                } else {
                                    resultMsg += "，下一位审批人ID为" + nextHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，下一位审批人ID为" + nextHandlerId;
                            }
                        }
                        return resultMsg;
                    } else {
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -10);
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 10, -10, bizAuditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == 20) {
                    if (bizAuditDTO.getIs_pass()) {
                        Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                        ;// 管理员

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, nextHandlerId, 30);

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                nextHandlerId,
                                "任务审核",
                                "任务审核",
                                "您有新的任务需要审核",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "通过", 20, 30, bizAuditDTO.getTitle());

                        System.out.println("已审批，下一位审批人id为" + nextHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已审批";
                        if (nextHandlerId != null) {
                            try {
                                SysUser nextUser = sysMapper.getUserById(nextHandlerId);
                                if (nextUser != null) {
                                    resultMsg += "，下一位审批人为" + nextUser.getUserName();
                                } else {
                                    resultMsg += "，下一位审批人ID为" + nextHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，下一位审批人ID为" + nextHandlerId;
                            }
                        }
                        return resultMsg;
                    } else {
                        // 退回到提交人的部门负责人
                        // 根据提交人id获取部门负责人id
                        Long submitBy = bizMaterialSubmission.getSubmitBy();
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -20);
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 20, -20, bizAuditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == 30) {
                    if (bizAuditDTO.getIs_pass()) {

                        // 更新审批单状态（使用封装方法）
                        bizMaterialSubmission.setFlowStatus(40);
                        bizMapper.updateAudit(bizMaterialSubmission);
                        // 更新任务状态
                        BizTask bizTask = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
                        // 口径调整：任务 current_value 以"本次填报值"为准（覆盖写），不再在最终通过时重复累加
                        BigDecimal rv = bizMaterialSubmission.getReportedValue() != null ? bizMaterialSubmission.getReportedValue() : BigDecimal.ZERO;
                        rv = rv.setScale(0, RoundingMode.HALF_UP);
                        bizTask.setCurrentValue(rv);
                        if (bizTask.getCurrentValue().compareTo(bizTask.getTargetValue()) >= 0) {
                            bizTask.setStatus("3");
                        } else {
                            bizTask.setStatus("1");
                        }
                        bizMapper.updateCurrentTask(bizTask);

                        // 发送通知，告知提交人审批过程已完成
                        sendNotice(userId,
                                bizMaterialSubmission.getSubmitBy(),
                                "任务审核完成",
                                "审核完成",
                                "您提交的审核任务已完成",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "通过", 30, 40, bizAuditDTO.getTitle());

                        System.out.println("审批完成");
                        return "审批完成";
                    } else {
                        // 退回到归口负责人
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -30);
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 30, -30, bizAuditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == -20) {
                    if (bizAuditDTO.getIs_pass()) {
                        throw new RuntimeException("请重新提交材料");
                    } else {
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                        updateAuditStatus(subId, backHandlerId, -10);
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());
                        createAuditLog(subId, userId, "退回", -20, -10, bizAuditDTO.getTitle());
                        System.out.println("已退回，退回到id为" + backHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == -30) {
                    if (bizAuditDTO.getIs_pass()) {
                        throw new RuntimeException("请重新提交材料");
                    }
                    Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                    updateAuditStatus(subId, backHandlerId, -20);
                    // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                    bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");
                    sendNotice(userId,
                            backHandlerId,
                            "任务退回",
                            "任务退回",
                            "您提交的材料被退回",
                            "0",
                            bizMaterialSubmission.getTaskId());
                    createAuditLog(subId, userId, "退回", -30, -20, bizAuditDTO.getTitle());
                    System.out.println("已退回，退回到id为" + backHandlerId);
                    return "已退回，退回到" + sysMapper.getUserById(backHandlerId).getUserName();
                } else {// 补充：flowStatus不在枚举值范围内的返回值
                    throw new RuntimeException("不支持的审批流程状态：" + bizMaterialSubmission.getFlowStatus());
                }
            } else {
                // 分支2：当前用户不是处理人
                throw new RuntimeException("您不是该任务的当前审批人，无法操作");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 退回重新提交材料
     * @param resubDTOBiz 重新提交数据
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public String reSubmitMaterial(BizReSubDTO resubDTOBiz, Long userId) {
        try {
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(resubDTOBiz.getSub_id());
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }
            Map<Integer, Long> nextHandlerMap = Map.of(
                    -10, sysMapper.getDeptLeaderId(userId),
                    -20, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    -30, 1L);

            Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());
            if (bizMaterialSubmission.getFlowStatus() >= 0) {
                throw new RuntimeException("该任务状态不是退回状态,无法重新提交");
            }
            // 重新提交同样只保留整数，并覆盖写任务 current_value
            BigDecimal rv = resubDTOBiz.getReported_value() != null ? resubDTOBiz.getReported_value() : BigDecimal.ZERO;
            rv = rv.setScale(0, RoundingMode.HALF_UP);
            bizMaterialSubmission.setReportedValue(rv);
            bizMaterialSubmission.setDataType(resubDTOBiz.getData_type());
            bizMaterialSubmission.setFileId(resubDTOBiz.getFile_id());
            bizMaterialSubmission.setSubmitTime(new Date());
            bizMaterialSubmission.setFlowStatus(-bizMaterialSubmission.getFlowStatus());
            bizMaterialSubmission.setCurrentHandlerId(nextHandlerId);
            bizMapper.updateAudit(bizMaterialSubmission);

            // 重新提交后恢复任务状态为"审核中"，并覆盖写 current_value
            BizTask bizTask = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
            if (bizTask != null) {
                bizTask.setCurrentValue(rv);
                bizTask.setStatus("2");
                bizMapper.updateCurrentTask(bizTask);
            }

            // 已修改，修改内容及原因：添加null检查，避免nextHandlerId为null时发送通知导致数据库约束错误
            // 只有当 nextHandlerId 不为 null 时才发送通知
            if (nextHandlerId != null) {
                sendNotice(userId,
                        nextHandlerId,
                        "任务审核",
                        "任务审核",
                        "您有新的任务需要审核",
                        "0",
                        bizMaterialSubmission.getTaskId());
            }

            createAuditLog(resubDTOBiz.getSub_id(), userId, "重新提交", -bizMaterialSubmission.getFlowStatus(),
                    bizMaterialSubmission.getFlowStatus(), "重新提交");
            // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
            String resultMsg = "已重新提交";
            if (nextHandlerId != null) {
                try {
                    SysUser handler = sysMapper.getUserById(nextHandlerId);
                    if (handler != null) {
                        resultMsg += ",审核人为" + handler.getUserName();
                    }
                } catch (Exception e) {
                    // 忽略获取用户信息失败的情况
                }
            }
            return resultMsg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 撤回提交申请
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 操作结果
     */
    public Object drawbackSubmit(Long taskId, Long userId){
        try{
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getNewestAudit(taskId);
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }
            if (!bizMaterialSubmission.getSubmitBy().equals(userId)){
                throw new RuntimeException("您不是该任务的提交人，无法撤回");
            }
            bizMaterialSubmission.setIsDelete(1);
            bizMapper.updateAudit(bizMaterialSubmission);
            createAuditLog(bizMaterialSubmission.getSubId(), userId, "撤回提交", bizMaterialSubmission.getFlowStatus(), -bizMaterialSubmission.getFlowStatus(), "撤回提交");
            return "已撤回提交";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取该任务上一次审批通过的文件
     * @param taskId 任务ID
     * @return 文件上传视图对象
     */
    public FileUploadVO getLastCycleFiles(Long taskId) {
        try{
            BizTask task = bizMapper.getTaskById(taskId);
            if (task == null) {
                throw new RuntimeException("任务不存在");
            }
            BizMaterialSubmission submission = bizMapper.getLatestApprovedAuditByTaskId(taskId);
            if (submission == null) {
                return null;
            }
            FileUploadVO fileUploadVo = new FileUploadVO();
            fileUploadVo.setFileId(submission.getFileId());
            fileUploadVo.setFilename(sysMapper.getFileById(submission.getFileId()).getFileName());
            fileUploadVo.setFilepath(sysMapper.getFileById(submission.getFileId()).getFileUrl());
            return fileUploadVo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据父任务ID获取三级任务
     * @param parentId 父任务ID
     * @return 任务列表
     */
    public List<BizTask> getThirdLevelTasksByParentId(Long parentId) {
        try {
            return bizMapper.getThirdLevelTasksByParentId(parentId);
        } catch (RuntimeException e) {
            throw new RuntimeException("获取任务失败,请检查任务id是否正确");
        }
    }

    /**
     * 根据归口负责人ID获取任务
     * @param principalId 归口负责人ID
     * @return 任务列表
     */
    public List<BizTask> getTasksByPrincipalId(Long principalId) {
        try {
            return bizMapper.getTasksByPrincipalId(principalId);
        } catch (Exception e) {
            throw new RuntimeException("获取任务失败,请检查负责人id是否正确");
        }
    }

    /**
     * 封装方法：发送系统通知
     * @param fromUserId 发送人ID
     * @param toUserId 接收人ID
     * @param triggerEvent 触发事件
     * @param title 标题
     * @param content 内容
     * @param sourceType 来源类型
     * @param sourceId 来源ID
     */
    private void sendNotice(Long fromUserId, Long toUserId, String triggerEvent,
                            String title, String content, String sourceType, Long sourceId) {
        // 已修改，修改内容及原因：添加toUserId的null检查，避免toUserId为null时插入数据库导致约束错误
        // 如果 toUserId 为 null，不发送通知，避免数据库约束错误
        if (toUserId == null) {
            System.out.println("警告：无法发送通知，接收人ID为空。标题：" + title);
            return;
        }
        SysNotice sysNotice = new SysNotice();
        sysNotice.setFromUserId(fromUserId);
        sysNotice.setToUserId(toUserId);
        sysNotice.setTriggerEvent(triggerEvent);
        sysNotice.setTitle(title);
        sysNotice.setContent(content);
        sysNotice.setSourceType(sourceType);
        sysNotice.setSourceId(sourceId);
        sysNotice.setIsRead("0");

        sysMapper.sendNotice(sysNotice);
        System.out.println("id=" + toUserId + "的用户收到一条通知：" + title);
    }

    /**
     * 封装方法：创建审批日志
     * @param subId 提交ID
     * @param operatorId 操作人ID
     * @param actionType 动作类型
     * @param preStatus 前状态
     * @param postStatus 后状态
     * @param comment 意见
     */
    private void createAuditLog(Long subId, Long operatorId, String actionType,
                                Integer preStatus, Integer postStatus, String comment) {
        BizAuditLog bizAuditLog = new BizAuditLog();
        bizAuditLog.setLogId(null);
        bizAuditLog.setSubId(subId);
        bizAuditLog.setOperatorId(operatorId);
        bizAuditLog.setActionType(actionType);
        bizAuditLog.setPreStatus(preStatus);
        bizAuditLog.setPostStatus(postStatus);
        bizAuditLog.setComment(comment);
        bizAuditLog.setCreateTime(new Date());

        bizMapper.createAuditLog(bizAuditLog);
    }

    /**
     * 封装方法：更新审批单状态和处理人
     * @param subId 提交ID
     * @param currentHandlerId 当前处理人ID
     * @param flowStatus 流程状态
     */
    private void updateAuditStatus(Long subId, Long currentHandlerId, Integer flowStatus) {
        BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(subId);
        if (bizMaterialSubmission != null) {
            bizMaterialSubmission.setCurrentHandlerId(currentHandlerId);
            bizMaterialSubmission.setFlowStatus(flowStatus);
            bizMapper.updateAudit(bizMaterialSubmission);
        }
    }

    /**
     * TaskToTaskVo转换方法
     * @param task 任务实体
     * @return 任务视图对象
     */
    public BizTaskVo taskToTaskVo(BizTask task) {
        BizTaskVo taskVo = new BizTaskVo();
        taskVo.setTaskId(task.getTaskId());
        taskVo.setProjectId(task.getProjectId());
        taskVo.setParentId(task.getParentId());
        taskVo.setAncestors(task.getAncestors());
        taskVo.setPhase(task.getPhase());
        taskVo.setTaskCode(task.getTaskCode());
        taskVo.setTaskName(task.getTaskName());
        taskVo.setLevel(task.getLevel());
        taskVo.setComment(task.getComment());
        taskVo.setDeptId(task.getDeptId());
        taskVo.setDeptName(sysMapper.getDeptById(task.getDeptId()).getDeptName());
        taskVo.setPrincipalId(task.getPrincipalId());
        taskVo.setPrincipalName(sysMapper.getUserById(task.getPrincipalId()).getUserName());
        // 避免空指针错误
        if(task.getAuditorId()!=null){
            taskVo.setAuditorId(task.getAuditorId());
            taskVo.setAuditorName(sysMapper.getUserById(task.getAuditorId()).getUserName());
        }else {
            taskVo.setAuditorId(null);
            taskVo.setAuditorName("无");
        }
        taskVo.setLeaderId(task.getLeaderId());
        taskVo.setLeaderName(sysMapper.getUserById(task.getLeaderId()).getUserName());
        taskVo.setExpTarget(task.getExpTarget());
        taskVo.setExpLevel(task.getExpLevel());
        taskVo.setExpEffect(task.getExpEffect());
        taskVo.setExpMaterialDesc(task.getExpMaterialDesc());
        taskVo.setDataType(task.getDataType());
        taskVo.setTargetValue(task.getTargetValue());
        taskVo.setCurrentValue(task.getCurrentValue());
        taskVo.setWeight(task.getWeight());
        taskVo.setProgress(task.getProgress());
        taskVo.setStatus(task.getStatus());
        taskVo.setIsDelete(task.getIsDelete());
        taskVo.setCreateTime(task.getCreateTime());
        taskVo.setUpdateTime(task.getUpdateTime());
        return taskVo;
    }

    /**
     * TaskListToTaskVoList转换方法
     * @param tasks 任务实体列表
     * @return 任务视图对象列表
     */
    public List<BizTaskVo> taskListToTaskVoList(List<BizTask> tasks) {
        List<BizTaskVo> taskVos = new ArrayList<>();
        for (BizTask task : tasks) {
            taskVos.add(taskToTaskVo(task));
        }
        return taskVos;
    }

    /**
     * TaskDTO转Task转换方法
     * @param taskDTO 任务数据传输对象
     * @return 任务实体
     */
    public BizTask taskDTO2Task(BizTaskDTO taskDTO) {
        BizTask task = new BizTask();
        task.setTaskId(taskDTO.getTaskId());
        task.setProjectId(taskDTO.getProjectId());
        task.setParentId(taskDTO.getParentId());
        task.setAncestors(taskDTO.getAncestors());
        task.setPhase(taskDTO.getPhase());
        task.setTaskCode(taskDTO.getTaskCode());
        task.setTaskName(taskDTO.getTaskName());
        task.setLevel(taskDTO.getLevel());
        task.setComment(taskDTO.getComment());
        task.setDeptId(taskDTO.getDeptId());
        task.setAuditorId(taskDTO.getAuditorId());
        task.setPrincipalId(taskDTO.getPrincipalId());
        task.setLeaderId(taskDTO.getLeaderId());
        task.setExpTarget(taskDTO.getExpTarget());
        task.setExpLevel(taskDTO.getExpLevel());
        task.setExpEffect(taskDTO.getExpEffect());
        task.setExpMaterialDesc(taskDTO.getExpMaterialDesc());
        task.setDataType(taskDTO.getDataType());
        task.setTargetValue(taskDTO.getTargetValue());
        task.setCurrentValue(taskDTO.getCurrentValue());
        task.setWeight(taskDTO.getWeight());
        task.setProgress(taskDTO.getProgress());
        task.setStatus(taskDTO.getStatus());
        return task;
    }

    /**
     * Audit转AuditVO转换方法
     * @param audit 材料提交实体
     * @return 审批视图对象
     */
    public BizAuditVO auditToAuditVo(BizMaterialSubmission audit){
        BizAuditVO bizAuditVO = new BizAuditVO();
        bizAuditVO.setSubId(audit.getSubId());
        bizAuditVO.setTaskId(audit.getTaskId());
        bizAuditVO.setFileId(audit.getFileId());
        bizAuditVO.setFilename(sysMapper.getFileById(audit.getFileId()).getFileName());
        bizAuditVO.setReportedValue(audit.getReportedValue());
        bizAuditVO.setDataType(audit.getDataType());
        bizAuditVO.setSubmitBy(audit.getSubmitBy());
        bizAuditVO.setSubmitDeptId(audit.getSubmitDeptId());
        bizAuditVO.setManageDeptId(audit.getManageDeptId());
        bizAuditVO.setSubmitTime(audit.getSubmitTime());
        bizAuditVO.setFileSuffix(audit.getFileSuffix());
        bizAuditVO.setFlowStatus(audit.getFlowStatus());
        bizAuditVO.setCurrentHandlerId(audit.getCurrentHandlerId());
        bizAuditVO.setIsDelete(audit.getIsDelete());
        return bizAuditVO;
    }

    /**
     * AuditList转AuditVoList转换方法
     * @param audits 材料提交实体列表
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> auditListToAuditVoList(List<BizMaterialSubmission> audits){
        List<BizAuditVO> auditVos = new ArrayList<>();
        for (BizMaterialSubmission audit : audits) {
            auditVos.add(auditToAuditVo(audit));
        }
        return auditVos;
    }

    // 中期截止年份
    private static final Integer MID_TERM_END_YEAR = 2028;

    /**
     * 获取看板数据汇总
     * @return 看板数据汇总
     */
    public DashboardSummaryVO getDashboardSummary() {
        DashboardSummaryVO summary = new DashboardSummaryVO();

        try {
            // 设置统计时间
            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);
            summary.setCurrentYear(String.valueOf(currentYear));
            summary.setMidTermEndYear(String.valueOf(MID_TERM_END_YEAR));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            summary.setUpdateTime(sdf.format(new Date()));

            // 1. 计算整体数据
            calculateOverallData(summary, currentYear);

            // 2. 计算部门数据
            calculateDeptData(summary, currentYear);

            // 3. 获取一级任务详情
            List<FirstLevelTaskDetailVO> firstLevelTasks = bizMapper.getFirstLevelTaskDetails();
            summary.setFirstLevelTasks(firstLevelTasks);

        } catch (Exception e) {
            throw new RuntimeException("获取看板数据失败: " + e.getMessage());
        }

        return summary;
    }

    /**
     * 计算整体数据
     */
    private void calculateOverallData(DashboardSummaryVO summary, int currentYear) {
        // 1.1 所有任务完成率
        Integer totalTasks = bizMapper.getTotalTaskCount();
        Integer completedTasks = bizMapper.getCompletedTaskCount();
        summary.setOverallCompletion(new TaskCompletionVO(
                totalTasks, completedTasks, "all", "所有任务完成率"
        ));

        // 1.2 本年度任务完成率
        Integer yearTotalTasks = bizMapper.getYearTaskCount(currentYear);
        Integer yearCompletedTasks = bizMapper.getYearCompletedTaskCount(currentYear);
        summary.setYearCompletion(new TaskCompletionVO(
                yearTotalTasks, yearCompletedTasks, "year", currentYear + "年度任务完成率"
        ));

        // 1.3 中期任务完成率（phase在2028年之前）
        Integer midTermTotalTasks = bizMapper.getMidTermTaskCount(MID_TERM_END_YEAR);
        Integer midTermCompletedTasks = bizMapper.getMidTermCompletedTaskCount(MID_TERM_END_YEAR);
        summary.setMidTermCompletion(new TaskCompletionVO(
                midTermTotalTasks, midTermCompletedTasks, "midterm", "中期（" + MID_TERM_END_YEAR + "年前）任务完成率"
        ));

        // 1.4 一级任务完成率
        Integer firstLevelTotalTasks = bizMapper.getFirstLevelTaskCount();
        Integer firstLevelCompletedTasks = bizMapper.getFirstLevelCompletedTaskCount();
        summary.setFirstLevelCompletion(new TaskCompletionVO(
                firstLevelTotalTasks, firstLevelCompletedTasks, "firstLevel", "一级任务完成率"
        ));
    }

    /**
     * 计算部门数据
     */
    private void calculateDeptData(DashboardSummaryVO summary, int currentYear) {
        // 2.1 各部门整体完成率
        List<DeptTaskStatsVO> deptOverallStats = bizMapper.getDeptTaskStats();
        deptOverallStats.forEach(DeptTaskStatsVO::calculateCompletionRate);
        summary.setDeptOverallStats(deptOverallStats);

        // 2.2 各部门本年度完成率
        List<DeptTaskStatsVO> deptYearStats = bizMapper.getDeptYearTaskStats(currentYear);
        deptYearStats.forEach(DeptTaskStatsVO::calculateCompletionRate);
        summary.setDeptYearStats(deptYearStats);

        // 2.3 各部门中期完成率
        List<DeptTaskStatsVO> deptMidTermStats = bizMapper.getDeptMidTermTaskStats(MID_TERM_END_YEAR);
        deptMidTermStats.forEach(DeptTaskStatsVO::calculateCompletionRate);
        summary.setDeptMidTermStats(deptMidTermStats);
    }

    /**
     * 获取所有任务完成率（单独的接口）
     * @return 所有任务完成率
     */
    public TaskCompletionVO getAllTaskCompletionRate() {
        try {
            Integer totalTasks = bizMapper.getTotalTaskCount();
            Integer completedTasks = bizMapper.getCompletedTaskCount();
            return new TaskCompletionVO(
                    totalTasks, completedTasks, "all", "所有任务完成率"
            );
        } catch (Exception e) {
            throw new RuntimeException("获取所有任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取本年度任务完成率（单独的接口）
     * @param year 年份，为空时使用当前年份
     * @return 本年度任务完成率
     */
    public TaskCompletionVO getYearTaskCompletionRate(Integer year) {
        try {
            if (year == null) {
                Calendar cal = Calendar.getInstance();
                year = cal.get(Calendar.YEAR);
            }

            Integer totalTasks = bizMapper.getYearTaskCount(year);
            Integer completedTasks = bizMapper.getYearCompletedTaskCount(year);
            return new TaskCompletionVO(
                    totalTasks, completedTasks, "year", year + "年度任务完成率"
            );
        } catch (Exception e) {
            throw new RuntimeException("获取本年度任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取中期任务完成率（单独的接口）
     * @param endYear 截止年份，为空时使用默认值2028
     * @return 中期任务完成率
     */
    public TaskCompletionVO getMidTermTaskCompletionRate(Integer endYear) {
        try {
            if (endYear == null) {
                endYear = MID_TERM_END_YEAR;
            }

            Integer totalTasks = bizMapper.getMidTermTaskCount(endYear);
            Integer completedTasks = bizMapper.getMidTermCompletedTaskCount(endYear);
            return new TaskCompletionVO(
                    totalTasks, completedTasks, "midterm", "中期（" + endYear + "年前）任务完成率"
            );
        } catch (Exception e) {
            throw new RuntimeException("获取中期任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取一级任务完成率（单独的接口）
     * @return 一级任务完成率
     */
    public TaskCompletionVO getFirstLevelTaskCompletionRate() {
        try {
            Integer totalTasks = bizMapper.getFirstLevelTaskCount();
            Integer completedTasks = bizMapper.getFirstLevelCompletedTaskCount();
            return new TaskCompletionVO(
                    totalTasks, completedTasks, "firstLevel", "一级任务完成率"
            );
        } catch (Exception e) {
            throw new RuntimeException("获取一级任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取各部门任务完成率（单独的接口）
     * @return 各部门任务完成率列表
     */
    public List<DeptTaskStatsVO> getDeptTaskCompletionRates() {
        try {
            List<DeptTaskStatsVO> stats = bizMapper.getDeptTaskStats();
            stats.forEach(DeptTaskStatsVO::calculateCompletionRate);
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取各部门任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取各部门本年度任务完成率（单独的接口）
     * @param year 年份，为空时使用当前年份
     * @return 各部门本年度任务完成率列表
     */
    public List<DeptTaskStatsVO> getDeptYearTaskCompletionRates(Integer year) {
        try {
            if (year == null) {
                Calendar cal = Calendar.getInstance();
                year = cal.get(Calendar.YEAR);
            }

            List<DeptTaskStatsVO> stats = bizMapper.getDeptYearTaskStats(year);
            stats.forEach(DeptTaskStatsVO::calculateCompletionRate);
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取各部门本年度任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取各部门中期任务完成率（单独的接口）
     * @param endYear 截止年份，为空时使用默认值2028
     * @return 各部门中期任务完成率列表
     */
    public List<DeptTaskStatsVO> getDeptMidTermTaskCompletionRates(Integer endYear) {
        try {
            if (endYear == null) {
                endYear = MID_TERM_END_YEAR;
            }

            List<DeptTaskStatsVO> stats = bizMapper.getDeptMidTermTaskStats(endYear);
            stats.forEach(DeptTaskStatsVO::calculateCompletionRate);
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取各部门中期任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取一级任务详细完成情况（单独的接口）
     * @return 一级任务详情列表
     */
    public List<FirstLevelTaskDetailVO> getFirstLevelTaskDetails() {
        try {
            return bizMapper.getFirstLevelTaskDetails();
        } catch (Exception e) {
            throw new RuntimeException("获取一级任务详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个部门的所有统计信息
     * @param deptId 部门ID
     * @return 部门统计信息
     */
    public Map<String, Object> getDeptStatsDetail(Long deptId) {
        try {
            Map<String, Object> result = new HashMap<>();

            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);

            // 获取部门信息
            String deptName = sysMapper.getDeptNameByDeptId(deptId);
            if (deptName == null) {
                throw new RuntimeException("部门不存在");
            }

            result.put("deptId", deptId);
            result.put("deptName", deptName);

            // 计算各种完成率
            result.put("overall", calculateDeptCompletionRate(deptId, null, null));
            result.put("year", calculateDeptCompletionRate(deptId, currentYear, null));
            result.put("midterm", calculateDeptCompletionRate(deptId, null, MID_TERM_END_YEAR));

            // 获取部门负责人
            Long leaderId = sysMapper.getDeptLeaderId(deptId);
            if (leaderId != null) {
                String leaderName = sysMapper.getUserById(leaderId).getNickName();
                result.put("leaderId", leaderId);
                result.put("leaderName", leaderName);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("获取部门统计详情失败: " + e.getMessage());
        }
    }

    /**
     * 计算部门的任务完成率（辅助方法）
     */
    private Map<String, Object> calculateDeptCompletionRate(Long deptId, Integer year, Integer endYear) {
        Map<String, Object> result = new HashMap<>();

        // 这里简化处理，实际应该添加相应的Mapper方法
        // 临时使用查询所有数据再过滤的方式（后续可以优化）
        List<DeptTaskStatsVO> allDeptStats = bizMapper.getDeptTaskStats();
        for (DeptTaskStatsVO stats : allDeptStats) {
            if (stats.getDeptId().equals(deptId)) {
                result.put("totalTasks", stats.getTotalTasks());
                result.put("completedTasks", stats.getCompletedTasks());
                result.put("completionRate", stats.getCompletionRate());
                break;
            }
        }

        // 如果没有找到数据，设置默认值
        if (!result.containsKey("totalTasks")) {
            result.put("totalTasks", 0);
            result.put("completedTasks", 0);
            result.put("completionRate", BigDecimal.ZERO);
        }

        return result;
    }
}
```

#### java\org\example\service\ScheduledTaskService.java

```java
package org.example.service;

import org.example.entity.*;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.example.mapper.TokenBlacklistMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 定时任务服务类
 * 负责执行定期提醒、清理等后台任务
 */
@Service
public class ScheduledTaskService {

    private Long ADMIN_ID = 110228L;

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private SysMapper sysMapper;

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

    /**
     * 每月初提醒审核任务
     * 每月1号上午9:00执行
     * 提醒所有部门负责人本部门任务完成情况
     */
    @Scheduled(cron = "0 0 9 1 * ?")  // 每月1号上午9:00
    @Transactional
    public String monthlyDeptLeaderReminder() {
        try {
            System.out.println("[" + new Date() + "] 开始执行月度部门负责人审核任务提醒...");

            Set<Long> deptLeaderIds = getDeptLeadersId();

            if (deptLeaderIds.isEmpty()) {
                System.out.println("没有需要发送年度提醒的用户");
                return "没有需要发送年度提醒的用户";
            }

            int successCount = 0;
            for (Long deptLeaderId: deptLeaderIds){
                SysDept dept = sysMapper.getDeptByUserId(deptLeaderId);
//                获取当前时间，将年份转为Integer
                int currentYear = java.time.LocalDate.now().getYear();
//                获取本年度本部门的所有任务
                List<BizTask> currentYearTasks = bizMapper.getTasksByDeptIdAndPhase(dept.getDeptId(),currentYear);
                if (currentYearTasks.isEmpty()) {
                    continue;
                }
                // 统计截止本月有进展的任务数量
                Integer monthCount = 0;
                for (BizTask task: currentYearTasks){
                    if (task.getCurrentValue().signum() != 0) {
                        monthCount++;
                    }
                }

//                统计已完成任务数量
                Integer completeCount = 0;
                for (BizTask task: currentYearTasks){
                    if (task.getStatus().equals("3")) {
                        completeCount++;
                    }
                }

//                统计待审核任务数量
                Integer auditCount = 0;
                for (BizTask task: currentYearTasks){
                    if (task.getStatus().equals("2")) {
                        auditCount++;
                    }
                }

                String title = "部门双高建设任务完成情况月度提醒";
                String content = "尊敬的 "+ sysMapper.getUserById(deptLeaderId).getNickName() +
                        " 您好,本年度贵部门有 " + currentYearTasks.size() + " 个双高建设任务，\n" +
                        "截至本月，共" +  monthCount + "个任务已有进展。\n"+
                        "已完成任务" + completeCount + " 个，" + auditCount + " 个任务尚未审核\n"+
                        "请及时关注本部门双高建设任务进展情况。";
                sendSystemNotice(deptLeaderId, title, content, "月度提醒");
                System.out.println("向用户ID " + deptLeaderId + " 发送月度提醒成功");
                successCount++;
            }
            return "向"+successCount+"名用户发送月度提醒成功";

        } catch (Exception e) {
            System.err.println("执行月度审核提醒任务时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


    /**
     * 每月初提醒审核任务
     * 每月1号上午9:00执行
     * 提醒所有有待审核任务的负责人
     */
    @Scheduled(cron = "0 0 9 1 * ?")  // 每月1号上午9:00
    @Transactional
    public String monthlyAuditorReminder() {
        try {
            System.out.println("[" + new Date() + "] 开始执行月度审核人审核任务提醒...");
            List<BizMaterialSubmission> allPendingAudits = bizMapper.getAllPendingAudits();


            Set<Long> auditorIds = new HashSet<>();
            for (BizMaterialSubmission pendingAudit: allPendingAudits){
                auditorIds.add(pendingAudit.getCurrentHandlerId());
            }

            if (auditorIds.isEmpty()) {
                System.out.println("没有需要发送年度提醒的用户");
                return "没有需要发送年度提醒的用户";
            }
            int successCount = 0;
            for (Long auditorId: auditorIds){
//              获取待审核任务数量
                Integer auditCount = 0;
                for (BizMaterialSubmission pendingAudit: allPendingAudits){
                    if (pendingAudit.getCurrentHandlerId().equals(auditorId)) {
                        auditCount++;
                    }
                }

                String title = "月度审核任务提醒";
                String content = "尊敬的 " + sysMapper.getUserById(auditorId).getNickName() +
                        " 您好,截至目前，您有 " + auditCount + " 个待审核任务。\n" +
                        "请及时处理。";

                sendSystemNotice(auditorId, title, content, "月度提醒");
                System.out.println("向用户ID " + auditorId + " 发送月度提醒成功");
                successCount++;
            }
            return "向"+successCount+"名用户发送月度提醒成功";
        } catch (Exception e) {
            System.err.println("执行月度审核提醒任务时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
//
    /**
     * 年度提醒任务
     * 每年1月1日上午10:00执行
     * 提醒所有部门负责人年度任务情况
     */
    @Scheduled(cron = "0 0 10 1 1 ?")  // 每年1月1日上午10:00
    @Transactional
    public String yearlyReminder() {
        try {
            System.out.println("[" + new Date() + "] 开始执行年度提醒任务...");

            // 获取所有需要发送年度提醒的用户ID
            Set<Long> leadersId = getDeptLeadersId();

            if (leadersId.isEmpty()) {
                System.out.println("没有需要发送年度提醒的用户");
                return "没有需要发送年度提醒的用户";
            }
            int currentYear = java.time.LocalDate.now().getYear();
            int successCount = 0;
            for(Long leaderId: leadersId){
                SysDept dept = sysMapper.getDeptByUserId(leaderId);
//                获取本年度本部门的所有任务
                List<BizTask> currentYearTasks = bizMapper.getTasksByDeptIdAndPhase(dept.getDeptId(),currentYear);
                if (currentYearTasks.isEmpty()) {
                    continue;
                }

                String title = "部门双高建设任务年度提醒";
                String content = "尊敬的 "+ sysMapper.getUserById(leaderId).getNickName() +
                        " 您好,本年度贵部门有 " + currentYearTasks.size() + " 个双高建设任务，\n" +
                        "请及时关注本部门双高建设任务进展情况。";
                sendSystemNotice(leaderId, title, content, "月度提醒");
                successCount++;
                System.out.println("向用户ID " + leaderId + " 发送月度提醒成功");

            }

            System.out.println("年度提醒完成，成功发送 " + successCount + " 条提醒");
            return "年度提醒完成，成功发送 " + successCount + " 条提醒";

        } catch (Exception e) {
            System.err.println("执行年度提醒任务时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
//
    /**
     * 封装方法：发送系统通知
     *
     * @param toUserId 接收用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param triggerEvent 触发事件类型
     */
    private void sendSystemNotice(Long toUserId, String title, String content, String triggerEvent) {
        SysNotice notice = new SysNotice();
        notice.setFromUserId(ADMIN_ID);
        notice.setToUserId(toUserId);
        notice.setType("1");  // 提醒类型
        notice.setTriggerEvent(triggerEvent);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setSourceType("0");
        notice.setSourceId(0L);
        notice.setIsRead("0");
        notice.setIsDelete(0);
        notice.setCreateTime(new Date());

        sysMapper.sendNotice(notice);

        System.out.println("系统通知已发送 - 接收人ID: " + toUserId + ", 标题: " + title);
    }

    /**
     * 获取需要发送年度提醒的所有用户ID
     * 您可以自定义这里的逻辑
     *
     * @return 用户ID集合
     */
    private Set<Long> getDeptLeadersId() {
        Set<Long> userIds = new HashSet<>();

        try {
            // 获取所有部门负责人
            List<Long> deptLeaders = sysMapper.getAllDeptLeaders();
            if (deptLeaders != null) {
                userIds.addAll(deptLeaders);
            }

            // 可以根据需要添加其他用户组
            // 例如：所有管理员、所有用户等

        } catch (Exception e) {
            System.err.println("获取用户列表时发生错误: " + e.getMessage());
        }

        return userIds;
    }
    /**
     * 测试方法：手动触发月度提醒
     */
    public String triggerMonthDeptLeaderReminder() {
        System.out.println("手动触发月度审核提醒...");
        return monthlyDeptLeaderReminder();
    }

    /**
     * 测试方法：手动触发月度提醒
     */
    public String triggerMonthAuditorReminder() {
        System.out.println("手动触发月度审核提醒...");
        return monthlyAuditorReminder();
    }


    /**
     * 测试方法：手动触发年度提醒
     */
    public String triggerYearlyReminder() {
        System.out.println("手动触发年度提醒...");
        return yearlyReminder();
    }
    /**
     * 每日清理过期的Token黑名单
     * 每天凌晨2:00执行
     */
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2:00
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            System.out.println("[" + new Date() + "] 开始清理过期Token...");
            tokenBlacklistMapper.cleanupExpiredTokens();
            System.out.println("Token清理完成");
        } catch (Exception e) {
            System.err.println("清理过期Token时发生错误: " + e.getMessage());
        }
    }


    /**
     * 每年更新任务状态
     * 每天凌晨10:00执行
     */
    @Scheduled(cron = "0 0 10 1 1 ?")
    @Transactional
    public void updateTaskStatus() {
        try {
            System.out.println("[" + new Date() + "] 开始更新任务状态...");
            int currentYear = java.time.LocalDate.now().getYear();
            List<BizTask> tasks = bizMapper.getTasksByPhase(currentYear);
            for (BizTask task : tasks) {
                task.setStatus("1");
                bizMapper.updateTask(task);
            }
            System.out.println("已将"+currentYear+"所有任务的状态修改为进行中");
        } catch (Exception e) {
            System.err.println("更新任务状态时发生错误: " + e.getMessage());
        }
    }


    @Autowired
    private TrendDataService trendDataService;
    /**
     * 趋势数据定时任务
     * 每天23:30执行
     */
    @Scheduled(cron = "0 30 23 * * ?")
    public void dailyTrendRecord() {
        System.out.println("[" + new Date() + "] 开始执行趋势数据记录...");
        trendDataService.recordDailyTrendData();
    }

}
```

#### java\org\example\service\SysService.java

```java
package org.example.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.*;
import org.example.entity.dto.FileUploadDTO;
import org.example.entity.dto.SysAlertDTO;
import org.example.entity.dto.SysNoticeDTO;
import org.example.entity.dto.SysUserDTO;
import org.example.entity.vo.FileUploadVO;
import org.example.entity.vo.SysLoginVO;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.example.mapper.TokenBlacklistMapper;
import org.example.utils.FileUploadUtil;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 系统服务类：处理用户管理、登录认证、文件上传等系统功能
 */
@Service
public class SysService {

    @Autowired
    private SysMapper sysMapper;
    @Autowired
    private BizMapper bizMapper;

    /**
     * 获取所有用户
     * @return 用户列表
     */
    public List<SysUser> getAllUsers() {
        return sysMapper.getAllUsers();
    }

    /**
     * 获取部门信息（用于前端根据 deptId 查询 leaderId 等信息）
     * @param deptId 部门ID
     * @return 部门对象
     */
    public SysDept getDeptById(Long deptId) {
        if (deptId == null) {
            throw new RuntimeException("deptId为空");
        }
        SysDept dept = sysMapper.getDeptById(deptId);
        if (dept == null) {
            throw new RuntimeException("部门不存在");
        }
        return dept;
    }

    /**
     * 用户登录
     * 改成了user_id
     * @param userId 用户ID
     * @param password 密码
     * @return 登录视图对象
     */
    public SysLoginVO login(Long userId, String password) {
        SysUser user = sysMapper.getUserById(userId);
        if (user != null && user.getPassword().equals(password)) {
            SysLoginVO sysLoginVo = new SysLoginVO(user.getNickName(), JWTUtil.generateJwtToken(user));
            return sysLoginVo;
        } else if (user == null){
            throw new RuntimeException("用户不存在");
        } else if (!user.getPassword().equals(password)){
            throw new RuntimeException("密码错误");
        }
        return null;
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    public void changePassword(Long userId, String newPassword) {
        SysUser user = sysMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(newPassword);
        sysMapper.updateUser(user);
    }

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

    /**
     * 用户注销
     * @param token Token字符串
     */
    public void logout(String token) {
        try{
            tokenBlacklistMapper.addToBlacklist(new TokenBlacklist(token, JWTUtil.verifyJwtToken(token).getExpiresAt()));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传文件
     * @param file 文件对象
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 文件上传视图对象
     */
    public Object uploadFile(MultipartFile file, Long taskId,HttpServletRequest request) {
        try{
            if (file.isEmpty()) {
                throw new RuntimeException("文件不能为空");
            }
            // 如果文件后缀不是doc,docx,pdf中的一个，报错
            if (!file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1).matches("doc|docx|pdf")) {
                throw new RuntimeException("文件格式错误");
            }
            FileUploadDTO fileUploadDTO = FileUploadUtil.upload(file);
            SysFile sysFile = new SysFile();
            sysFile.setFilePath(fileUploadDTO.getFilepath());
            sysFile.setFileName(fileUploadDTO.getFilename());
            sysFile.setFileUrl(fileUploadDTO.getFilepath());
            sysFile.setFileSuffix(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1));
            sysFile.setFileSize(file.getSize());
            Long userId = JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            sysFile.setUploadBy(userId);
            sysFile.setUploadTime(new Date());
            System.out.println(sysFile);
            sysMapper.uploadFile(sysFile);
            return new FileUploadVO(sysMapper.getFileByName(sysFile.getFileName()).getFileId(), fileUploadDTO.getFilename(), fileUploadDTO.getFilepath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据file_id下载文件
     * @param fileId 文件ID
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    public void downloadFile(Long fileId, HttpServletResponse response) throws IOException {
        // 1. 查询文件信息
        SysFile sysFile = sysMapper.getFileById(fileId);
        if (sysFile == null) {
            throw new RuntimeException("文件不存在");
        }

        // 2. 构建文件路径
        String fullPath = System.getProperty("user.dir") + sysFile.getFilePath();
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new RuntimeException("文件已被删除或移动");
        }

        // 3. 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(sysFile.getFileName(), StandardCharsets.UTF_8.name()) + "\"");
        response.setContentLengthLong(file.length());

        // 4. 写入文件流
        try (InputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
    }

    /**
     * 发送消息
     * @param sysNoticeDTO 通知数据
     * @param userId 用户ID
     */
    public void sendNotice(SysNoticeDTO sysNoticeDTO, Long userId) {
        try{
            SysNotice sysNotice = new SysNotice();
            sysNotice.setFromUserId(userId);
            sysNotice.setToUserId(sysNoticeDTO.getTo_user_id());
            sysNotice.setType(sysNoticeDTO.getType());
            sysNotice.setTriggerEvent(sysNoticeDTO.getTrigger_event());
            sysNotice.setTitle(sysNoticeDTO.getTitle());
            sysNotice.setContent(sysNoticeDTO.getContent());
            sysNotice.setSourceType("1");
            sysNotice.setSourceId(sysNoticeDTO.getSource_id());
            sysNotice.setIsRead("0");
            sysMapper.sendNotice(sysNotice);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查看当前用户收到的通知
     * @param userId 用户ID
     * @return 通知列表
     */
    public List<SysNotice> getNotices(Long userId) {
        try{
            return sysMapper.getNotices(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设为已读setRead
     * @param noticeId 通知ID
     */
    public void setRead(Long noticeId) {
        try{
            sysMapper.setRead(noticeId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 站内预警
     * @param sysAlertDTO 预警数据
     * @param userId 用户ID
     */
    public void sendAlert(SysAlertDTO sysAlertDTO, Long userId) {
        try{
            SysNotice sysNotice = new SysNotice();
            sysNotice.setFromUserId(userId);
            sysNotice.setToUserId(sysMapper.getUserByNickName(sysAlertDTO.getTo_user_nick_name()).getUserId());
            sysNotice.setType("1");
            sysNotice.setTriggerEvent("1");
            sysNotice.setTitle(sysAlertDTO.getTitle());
            sysNotice.setContent(sysAlertDTO.getContent());
            sysNotice.setSourceType("1");
            sysNotice.setSourceId(sysAlertDTO.getSource_id());
            sysNotice.setIsRead("0");
            sysMapper.sendNotice(sysNotice);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据ID获取用户
     * @param userId 用户ID
     * @return 用户对象
     */
    public SysUser getUserById(Long userId) {
        return sysMapper.getUserById(userId);
    }

    /**
     * 根据用户名获取用户
     * @param userName 用户名
     * @return 用户对象
     */
    public SysUser getUserByName(String userName) {
        return sysMapper.getUserByName(userName);
    }

    /**
     * 添加用户
     * @param user 用户数据传输对象
     */
    public void addUser(SysUserDTO user) {
        // id已存在，报错
        if(sysMapper.getUserById(user.getUserId()) != null){
            throw new RuntimeException("用户id已存在");
        }
        if(sysMapper.getUserByName(user.getUserName()) != null){
            throw new RuntimeException("用户名已存在，请添加文字进行区分");
        }
        // 验证deptId是否存在
        if(sysMapper.getDeptById(user.getDeptId()) == null){
            throw new RuntimeException("部门不存在");
        }

        sysMapper.addUser(userDTO2User(user));
    }

    /**
     * 更新用户
     * @param user 用户数据传输对象
     */
    public void updateUser(SysUserDTO  user) {
        if(sysMapper.getUserByName(user.getUserName()) != null){
            throw new RuntimeException("用户名已存在，请添加文字进行区分");
        }
        if(sysMapper.getDeptById(user.getDeptId()) == null){
            throw new RuntimeException("部门不存在");
        }

        sysMapper.updateUser(userDTO2User(user));
    }

    /**
     * 删除用户
     * @param userId 用户ID
     */
    public void deleteUser(Long userId) {
        if(sysMapper.getUserById(userId) == null){
            throw new RuntimeException("用户不存在");
        }

        if(!bizMapper.getTasks(userId).isEmpty()){
            throw new RuntimeException("该用户名下有任务，请先修改任务负责人");
        }
        sysMapper.deleteUser(userId);
    }

    /**
     * UserDTO转User转换方法
     * @param user 用户数据传输对象
     * @return 用户实体
     */
    public SysUser userDTO2User(SysUserDTO user) {
        SysUser newUser = new SysUser();
        newUser.setUserId(user.getUserId());
        newUser.setDeptId(user.getDeptId());
        newUser.setUserName(user.getUserName());
        newUser.setNickName(user.getNickName());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setRole(user.getRole());
        newUser.setCreateTime(new Date());
        newUser.setUpdateTime(new Date());
        newUser.setStatus("1");
        newUser.setIsDelete(0);
        return newUser;
    }
}
```

#### java\org\example\service\TrendDataService.java

```java
package org.example.service;

import org.example.entity.BizTask;
import org.example.entity.BizTrendData;
import org.example.mapper.BizMapper;
import org.example.mapper.TrendDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class TrendDataService {

    @Autowired
    private TrendDataMapper trendDataMapper;

    @Autowired
    private BizMapper bizMapper;

    /**
     * 每日记录趋势数据（定时任务）
     */
    @Transactional
    public void recordDailyTrendData() {
        try {
            // 检查今天是否已记录
            Integer count = trendDataMapper.checkTodayRecorded();
            if (count > 0) {
                System.out.println("今天已记录趋势数据，跳过");
                return;
            }

            LocalDate today = LocalDate.now();
            int year = today.getYear();
            int month = today.getMonthValue();
            int day = today.getDayOfMonth();

            // 计算当年的三级任务完成率
            List<BizTask> tasks = bizMapper.getTasksByPhase(year);

            // 只计算三级任务
            List<BizTask> thirdLevelTasks = tasks.stream()
                    .filter(task -> task.getLevel() == 3 && task.getIsDelete() == 0)
                    .toList();

            if (thirdLevelTasks.isEmpty()) {
                return; // 没有三级任务，不记录
            }

            // 计算完成率
            double completionRate = calculateCompletionRate(thirdLevelTasks);

            // 保存到数据库
            BizTrendData trendData = new BizTrendData();
            trendData.setYear(year);
            trendData.setMonth(month);
            trendData.setDay(day);
            trendData.setCompletionRate(completionRate);

            trendDataMapper.insertTrendData(trendData);

            System.out.println("趋势数据记录成功：" + year + "-" + month + "-" + day +
                    "，完成率：" + completionRate + "%");

        } catch (Exception e) {
            System.err.println("记录趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 计算完成率（所有三级任务的平均完成率）
     */
    private double calculateCompletionRate(List<BizTask> tasks) {
        if (tasks.isEmpty()) {
            return 0.0;
        }

        BigDecimal totalRate = BigDecimal.ZERO;
        int validTasks = 0;

        for (BizTask task : tasks) {
            BigDecimal target = task.getTargetValue();
            BigDecimal current = task.getCurrentValue();

            if (target != null && target.compareTo(BigDecimal.ZERO) > 0) {
                // 计算单个任务的完成率
                BigDecimal taskRate = current
                        .multiply(BigDecimal.valueOf(100))
                        .divide(target, 2, RoundingMode.HALF_UP);

                // 限制在0-100之间
                if (taskRate.compareTo(BigDecimal.ZERO) < 0) {
                    taskRate = BigDecimal.ZERO;
                } else if (taskRate.compareTo(BigDecimal.valueOf(100)) > 0) {
                    taskRate = BigDecimal.valueOf(100);
                }

                totalRate = totalRate.add(taskRate);
                validTasks++;
            }
        }

        if (validTasks > 0) {
            return totalRate.divide(BigDecimal.valueOf(validTasks), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        return 0.0;
    }

    /**
     * 根据年份获取趋势数据（直接返回数据库记录）
     */
    public List<BizTrendData> getTrendDataByYear(Integer year) {
        try {
            if (year == null) {
                // 默认当前年份
                year = LocalDate.now().getYear();
            }
            return trendDataMapper.getTrendDataByYear(year);
        } catch (Exception e) {
            throw new RuntimeException("获取趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发记录（测试用）
     */
    public String triggerRecord() {
        try {
            recordDailyTrendData();
            return "手动记录成功";
        } catch (Exception e) {
            return "手动记录失败: " + e.getMessage();
        }
    }
}
```

#### java\org\example\utils\FileUploadUtil.java

```java
package org.example.utils;

import org.example.entity.dto.FileUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件上传工具类
 */
public class FileUploadUtil {
    /** 上传目录 */
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    /** 非法字符正则 */
    private static final Pattern INVALID_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");

    /** 重复文件模式正则 */
    private static final Pattern DUPLICATE_PATTERN = Pattern.compile("(.+?)(?:\\((\\d+)\\))?(\\.[^.]*)?$");

    /**
     * 上传文件
     * @param file 文件对象
     * @return 文件上传数据传输对象
     * @throws IOException IO异常
     */
    public static FileUploadDTO upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 创建上传目录
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 获取并处理原始文件名
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }

        // 安全处理
        String safeFileName = Paths.get(originalName).getFileName().toString();
        safeFileName = INVALID_CHARS.matcher(safeFileName).replaceAll("");

        if (safeFileName.isEmpty()) {
            safeFileName = "uploaded_file";
        }

        // 生成唯一文件名
        String finalFileName = getUniqueFileName(uploadPath, safeFileName);

        // 保存文件
        Path filePath = uploadPath.resolve(finalFileName);
        Files.copy(file.getInputStream(), filePath);
        filePath.toFile().setReadable(true, false);

        return new FileUploadDTO(finalFileName, "/uploads/" + finalFileName);
    }

    /**
     * 智能生成唯一文件名
     * 示例：
     * file.txt -> file.txt (不存在时)
     * file.txt -> file(1).txt (file.txt已存在)
     * file(1).txt -> file(2).txt (file(1).txt已存在)
     * file(2).txt -> file(3).txt (file(2).txt已存在)
     * @param uploadPath 上传路径
     * @param fileName 文件名
     * @return 唯一文件名
     */
    private static String getUniqueFileName(Path uploadPath, String fileName) {
        // 解析文件名，提取基础名称、数字后缀和扩展名
        Matcher matcher = DUPLICATE_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            throw new RuntimeException("文件名格式不正确");
        }

        String baseName = matcher.group(1);      // 基础名称
        String numberStr = matcher.group(2);     // 数字后缀，可能为null
        String extension = matcher.group(3) != null ? matcher.group(3) : ""; // 扩展名

        int startNumber = 1;
        if (numberStr != null) {
            // 如果文件名已经有数字后缀，则从该数字+1开始
            try {
                startNumber = Integer.parseInt(numberStr) + 1;
                // 如果有数字后缀，baseName就是原始基础名
                // 例如：file(1).txt -> baseName = "file", numberStr = "1"
            } catch (NumberFormatException e) {
                // 如果数字格式错误，从1开始
                startNumber = 1;
            }
        }

        // 先检查原始文件名（没有后缀的情况）
        if (numberStr == null && !Files.exists(uploadPath.resolve(fileName))) {
            return fileName;
        }

        // 寻找下一个可用的数字后缀
        for (int i = startNumber; i <= 1000; i++) {
            String newFileName;
            if (i == 1) {
                // 第一次添加后缀
                newFileName = String.format("%s(%d)%s", baseName, i, extension);
            } else {
                // 更新数字后缀
                newFileName = String.format("%s(%d)%s", baseName, i, extension);
            }

            if (!Files.exists(uploadPath.resolve(newFileName))) {
                return newFileName;
            }
        }

        throw new RuntimeException("文件名冲突过多，请重命名文件后上传");
    }
}
```

#### java\org\example\utils\JWTUtil.java

```java
package org.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.example.entity.SysUser;
import org.example.entity.TokenBlacklist;
import org.example.mapper.TokenBlacklistMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * JWT工具类：生成、验证和解析Token
 */
public class JWTUtil {
    /** 密钥，与生成 Token 时使用的密钥一致 */
    private static final String SECRET_KEY = "secretKey";

    /**
     * 生成jwt token
     * @param user 用户对象
     * @return Token字符串
     */
    public static String generateJwtToken(SysUser user) {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + 3600000; // 令牌有效期为 1 小时
        Date exp = new Date(expMillis);

        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withClaim("id",user.getUserId())
                .withClaim("username", user.getUserName())
                .withClaim("role", user.getRole())
                .withIssuer("auth0")
                .withExpiresAt(exp)
                .sign(algorithm);
    }

    /**
     * 验证jwt token
     * @param token Token字符串
     * @return 解码后的JWT对象
     */
    public static DecodedJWT verifyJwtToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException exception) {
            // Invalid signature/claims
            throw new RuntimeException("Invalid token: " + exception.getMessage());
        }
    }

    /**
     * 从Token获取用户ID
     * @param token Token字符串
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        return decodedJWT.getClaim("id").asLong();
    }

    /**
     * 从Token获取角色
     * @param token Token字符串
     * @return 角色字符串
     */
    public static String getRoleFromToken(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        return decodedJWT.getClaim("role").asString();
    }

    /**
     * 检查Token是否过期
     * @param token Token字符串
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        Date expiresAt = decodedJWT.getExpiresAt();
        return expiresAt.before(new Date());
    }

    /**
     * 检查Token是否在黑名单中
     * @param token Token字符串
     * @return 是否在黑名单中
     */
    public static boolean isTokenInBlacklist(String token) {
        // TODO: 添加黑名单检查逻辑
        return false;
    }
}
```

#### resources\application.properties

```text
# ?????
spring.datasource.url=jdbc:mysql://localhost:3306/biz
spring.datasource.username=root
spring.datasource.password=981119

# ??????
spring.web.resources.static-locations=file:${user.dir}/uploads/
spring.mvc.static-path-pattern=/uploads/**

# MyBatis??
mybatis.type-aliases-package=org.example.entity
mybatis.configuration.map-underscore-to-camel-case=true

# ??????
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# ??????
spring.task.scheduling.pool.size=5
spring.task.scheduling.thread-name-prefix=scheduled-task-

# ???????????true?
schedule.enabled=true

# ????????
schedule.monthly-reminder.enabled=true
schedule.monthly-reminder.cron=0 0 9 1 * ?
schedule.token-cleanup.enabled=true
schedule.token-cleanup.cron=0 0 2 * * ?
```

```

---

### <a id='src\main\java\org\example\bizapplication-java'></a>src\main\java\org\example\BizApplication.java

```java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 业务应用主启动类
 */
@SpringBootApplication
@EnableScheduling  // 启用定时任务支持
public class BizApplication {
    /**
     * 应用入口方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BizApplication.class, args);
    }
}
```

---

### <a id='src\main\java\org\example\config\jacksonconfig-java'></a>src\main\java\org\example\config\JacksonConfig.java

```java
package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Jackson全局配置：统一Date类型序列化/反序列化格式
 */
@Configuration
public class JacksonConfig {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. 指定Date类型的解析/生成格式（匹配yyyy-MM-dd HH:mm:ss）
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 2. 设置时区（避免时间偏移，建议使用东八区）
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 3. 解决序列化时Date类型为时间戳的问题
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 4. 支持Java 8时间类型（可选，兼容LocalDateTime等）
        objectMapper.registerModule(new JavaTimeModule());

        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
```

---

### <a id='src\main\java\org\example\config\jwtinterceptor-java'></a>src\main\java\org\example\config\JwtInterceptor.java

```java
package org.example.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.vo.ErrorVO;
import org.example.mapper.TokenBlacklistMapper;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * JWT拦截器：验证请求中的Token有效性
 * - 检查Token是否存在
 * - 验证Token是否在黑名单中
 * - 解析Token并设置用户角色信息
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

    /**
     * 前置处理：验证Token
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @return 验证通过返回true，否则返回false
     * @throws Exception 异常信息
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            sendError(response, 401, "No Token");
            return false;
        }

        // 检查黑名单
        if (tokenBlacklistMapper.findByToken(token) != null) {
            sendError(response, 401, "Invalid Token");
            return false;
        }

        try {
            DecodedJWT decodedJWT = JWTUtil.verifyJwtToken(token);
            request.setAttribute("userRole", decodedJWT.getClaim("role").asString());
            return true;
        } catch (Exception e) {
            sendError(response, 401, "No Token: " + e.getMessage());
            return false;
        }
    }

    /**
     * 发送错误响应
     * @param response HTTP响应
     * @param code 错误码
     * @param message 错误信息
     * @throws IOException IO异常
     */
    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(new ErrorVO(message, code)));
    }
}
```

---

### <a id='src\main\java\org\example\config\userroleinterceptor-java'></a>src\main\java\org\example\config\UserRoleInterceptor.java

```java
package org.example.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.utils.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 用户角色拦截器：验证用户角色权限
 * 仅允许管理员角色（role=0）访问受保护接口
 */
@Component
public class UserRoleInterceptor implements HandlerInterceptor {

    /** 允许访问的角色：0-管理员 */
    private static final String ALLOWED_ROLE = "0";

    /**
     * 角色验证，待调试，待完善
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头解析Token获取角色（你的真实逻辑）
        String token = request.getHeader("Authorization");
        String userRole = JWTUtil.getRoleFromToken(token);

        // 2. 角色校验：无角色/角色非0则拒绝访问
        if (userRole == null || !ALLOWED_ROLE.equals(userRole)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\":403,\"msg\":\"无权限访问该接口，仅管理员可访问\"}");
            writer.flush();
            writer.close();
            return false;
        }

        // 角色校验通过，放行请求
        return true;
    }
}
```

---

### <a id='src\main\java\org\example\config\webconfig-java'></a>src\main\java\org\example\config\WebConfig.java

```java
package org.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类：配置CORS、拦截器、路径匹配等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置路径匹配
     * @param configurer 路径匹配配置器
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 不区分尾部斜杠
        configurer.setUseTrailingSlashMatch(true);
    }

    /**
     * 配置CORS跨域
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("*")
                .allowedOriginPatterns("*") // 允许的域名
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private UserRoleInterceptor userRoleInterceptor;

    /**
     * 配置拦截器
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/system/**")
                .addPathPatterns("/biz/**")
                .addPathPatterns("/achievement/**")
                .addPathPatterns("/dashboard/**")
                .addPathPatterns("/performance/**")
                .excludePathPatterns("/system/login");

        registry.addInterceptor(userRoleInterceptor)
                .addPathPatterns("/system/users/**");
    }
}
```

---

### <a id='src\main\java\org\example\controller\achievementcontroller-java'></a>src\main\java\org\example\controller\AchievementController.java

```java
package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.BizAchievement;
import org.example.entity.vo.ErrorVO;
import org.example.service.AchievementService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标志性成果控制层
 * 处理标志性成果基础增删查改HTTP请求
 */
@RestController
@RequestMapping("/achievement")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    /**
     * 根据成果ID查询单条标志性成果信息
     * @param achId 成果ID
     * @return 成果实体/错误信息
     */
    @GetMapping("/{achId}")
    public Object getAchievementById(@PathVariable("achId") Long achId) {
        try {
            return achievementService.getAchievementById(achId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 查询所有未删除的标志性成果列表
     * @return 成果实体列表/错误信息
     */
    @GetMapping("/")
    public Object listAllAchievements() {
        try {
            List<BizAchievement> achievementList = achievementService.listAllAchievements();
            return achievementList;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 新增标志性成果
     * @param achievement 成果实体（JSON请求体）
     * @return 操作结果/错误信息
     */
    @PostMapping("/add")
    public Object addAchievement(@RequestBody BizAchievement achievement, HttpServletRequest  request) {
        try {
            Long achId = achievementService.addAchievement(achievement, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
            return "标志性成果「" + achievement.getAchName() + "」添加成功，成果ID：" + achId;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 修改标志性成果信息
     * @param achievement 成果实体（含成果ID，JSON请求体）
     * @return 操作结果/错误信息
     */
    @PostMapping("/update/{id}")
    public Object updateAchievement(@PathVariable("id") Long id,@RequestBody BizAchievement achievement) {
        try {
            achievementService.updateAchievement(id,achievement);
            return "标志性成果「" + achievement.getAchName() + "」修改成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 逻辑删除标志性成果
     * @param achId 成果ID（路径参数）
     * @return 操作结果/错误信息
     */
    @PostMapping("/delete/{achId}")
    public Object deleteAchievement(@PathVariable("achId") Long achId) {
        try {
            // 查询成果名称，用于返回友好提示
            BizAchievement achievement = achievementService.getAchievementById(achId);
            achievementService.deleteAchievement(achId);
            return "标志性成果「" + achievement.getAchName() + "」删除成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
}
```

---

### <a id='src\main\java\org\example\controller\bizcontroller-java'></a>src\main\java\org\example\controller\BizController.java

```java
package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.dto.BizAuditDTO;
import org.example.entity.dto.BizSubDTO;
import org.example.entity.dto.BizReSubDTO;
import org.example.entity.dto.BizTaskDTO;
import org.example.entity.vo.ErrorVO;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.example.service.BizService;

/**
 * 业务控制器：处理所有业务相关请求
 * 包含任务管理、材料提交、审批流程等功能
 */
@RestController
@RequestMapping("/biz")
public class BizController {
    @Autowired
    private BizService bizService;

    /**
     * 获取全量任务数据
     * @param request HTTP请求
     * @return 任务列表或错误信息
     */
    @GetMapping("/tasks")
    public Object getTasks(HttpServletRequest request){
        try{
            return bizService.getTasksByUserRole(JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据id获取任务
     * @param taskId 任务ID
     * @return 任务详情或错误信息
     */
    @GetMapping("/tasks/{taskId}")
    public Object getTaskById(@PathVariable("taskId") Long taskId){
        try{
            return bizService.getTaskById(taskId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取当前任务的所有子任务
     * @param taskId 任务ID
     * @return 子任务列表或错误信息
     */
    @GetMapping("/tasks/children")
    public Object getAllChildrenTasks(@RequestParam("task_id") Long taskId){
        try{
            return bizService.getAllChildrenTasks(taskId);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取当前任务的父任务
     * @param taskId 任务ID
     * @return 父任务信息或错误信息
     */
    @GetMapping("/tasks/parent")
    public Object getParentTask(@RequestParam("task_id") Long taskId){
        try{
            return bizService.getParentTask(taskId);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据部门id获取任务
     * @param deptId 部门ID
     * @return 任务列表或错误信息
     */
    @GetMapping("/tasks/dept")
    public Object getTasksByDeptId(@RequestParam("dept_id") Long deptId){
        try{
            return bizService.getTasksByDeptId(deptId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 添加任务
     * @param task 任务数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/tasks/manage/add")
    public Object addTask(@RequestBody BizTaskDTO task, HttpServletRequest request){
        try{
            bizService.addTask(task);
            return "任务"+task.getTaskName()+"添加成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 更新任务
     * @param task 任务数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/tasks/manage/update")
    public Object updateTask(@RequestBody BizTaskDTO task, HttpServletRequest request){
        try{
            bizService.updateTask(task);
            return "任务"+task.getTaskName()+"更新成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 完成任务
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/tasks/manage/finish/{taskId}")
    public Object finishTask(@PathVariable("taskId") Long taskId, HttpServletRequest request){
        try{
            return bizService.finishTask(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 提交审批材料
     * @param bizSubDTO 提交数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/submit")
    public Object submitMaterial(@RequestBody BizSubDTO bizSubDTO, HttpServletRequest request){
        try{
            return bizService.submitMaterial(bizSubDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")) );
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取审批单
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 审批单信息或错误信息
     */
    @GetMapping("/audit/{taskId}")
    public Object getAudit(@PathVariable("taskId") Long taskId,HttpServletRequest request){
        try{
            return bizService.getAudit(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取"待我审批"的审批单列表（按 current_handler_id 查询）
     * @param request HTTP请求
     * @return 待审批列表或错误信息
     */
    @GetMapping("/audit/todo")
    public Object getTodoAudits(HttpServletRequest request) {
        try {
            return bizService.getTodoAudits(JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取某任务的全部审批单（用于任务详情抽屉展示完整流程）
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 审批单列表或错误信息
     */
    @GetMapping("/audit/task/{taskId}")
    public Object getAuditByTaskId(@PathVariable("taskId") Long taskId, HttpServletRequest request) {
        try {
            return bizService.getAuditByTaskId(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据任务id获取上次周期上传的文件
     * @param taskId 任务ID
     * @param response HTTP响应
     * @return 文件信息或错误信息
     */
    @GetMapping("/audit/file/{taskId}")
    public Object getLastCycleFiles(@PathVariable("taskId") Long taskId, HttpServletResponse  response) {
        try {
            return bizService.getLastCycleFiles(taskId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取审批操作日志（biz_audit_log）
     * @param subId 提交ID
     * @param request HTTP请求
     * @return 操作日志或错误信息
     */
    @GetMapping("/audit/logs/{subId}")
    public Object getAuditLogs(@PathVariable("subId") Long subId, HttpServletRequest request) {
        try {
            return bizService.getAuditLogs(subId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 审批操作
     * @param bizAuditDTO 审批数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/audit")
    public Object audit(@RequestBody BizAuditDTO bizAuditDTO, HttpServletRequest request){
        try{
            return bizService.audit(bizAuditDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 撤回提交申请
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/drawback/{taskId}")
    public Object drawbackSubmit(@PathVariable("taskId") Long taskId, HttpServletRequest request){
        try{
            return bizService.drawbackSubmit(taskId, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 重新提交材料
     * @param bizReSubDTO 重新提交数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/resub")
    public Object reSubmitMaterial(@RequestBody BizReSubDTO bizReSubDTO, HttpServletRequest request){
        try{
            return bizService.reSubmitMaterial(bizReSubDTO, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
}
```

---

### <a id='src\main\java\org\example\controller\dashboardcontroller-java'></a>src\main\java\org\example\controller\DashboardController.java

```java
package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.vo.*;
import org.example.service.BizService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 看板数据控制器
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private BizService bizService;

    /**
     * 获取看板数据汇总
     * @param request HTTP请求
     * @return 看板数据汇总
     */
    @GetMapping("/summary")
    public Object getDashboardSummary(HttpServletRequest request) {
        try {
            // 验证token（可选，如果需要权限控制）
            String token = request.getHeader("Authorization");
            if (token != null) {
                JWTUtil.verifyJwtToken(token);
            }

            return bizService.getDashboardSummary();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取所有任务完成率
     * @return 所有任务完成率
     */
    @GetMapping("/completion/overall")
    public Object getAllTaskCompletionRate() {
        try {
            return bizService.getAllTaskCompletionRate();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取本年度任务完成率
     * @param year 年份（可选，默认当前年份）
     * @return 本年度任务完成率
     */
    @GetMapping("/completion/year")
    public Object getYearTaskCompletionRate(@RequestParam(required = false,value = "year") Integer year) {
        try {
            return bizService.getYearTaskCompletionRate(year);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取中期任务完成率
     * @param endYear 截止年份（可选，默认2028）
     * @return 中期任务完成率
     */
    @GetMapping("/completion/midterm")
    public Object getMidTermTaskCompletionRate(@RequestParam(required = false,value = "year") Integer endYear) {
        try {
            return bizService.getMidTermTaskCompletionRate(endYear);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取一级任务完成率
     * @return 一级任务完成率
     */
    @GetMapping("/completion/first-level")
    public Object getFirstLevelTaskCompletionRate() {
        try {
            return bizService.getFirstLevelTaskCompletionRate();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取各部门整体任务完成率
     * @return 各部门任务完成率列表
     */
    @GetMapping("/dept/overall")
    public Object getDeptTaskCompletionRates() {
        try {
            return bizService.getDeptTaskCompletionRates();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取各部门本年度任务完成率
     * @param year 年份（可选，默认当前年份）
     * @return 各部门本年度任务完成率列表
     */
    @GetMapping("/dept/year")
    public Object getDeptYearTaskCompletionRates(@RequestParam(required = false,value = "year") Integer year) {
        try {
            return bizService.getDeptYearTaskCompletionRates(year);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取各部门中期任务完成率
     * @param endYear 截止年份（可选，默认2028）
     * @return 各部门中期任务完成率列表
     */
    @GetMapping("/dept/midterm")
    public Object getDeptMidTermTaskCompletionRates(@RequestParam(required = false,value = "endYear") Integer endYear) {
        try {
            return bizService.getDeptMidTermTaskCompletionRates(endYear);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取一级任务详细情况
     * @return 一级任务详情列表
     */
    @GetMapping("/tasks/first-level")
    public Object getFirstLevelTaskDetails() {
        try {
            return bizService.getFirstLevelTaskDetails();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

//    获取全量任务详细情况
    @GetMapping("/tasks/all_level")
    public Object getAllTaskDetails() {
        try {
            return bizService.getAllTaskDetails();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取单个部门详细统计信息
     * @param deptId 部门ID
     * @return 部门统计详情
     */
    @GetMapping("/dept/{deptId}")
    public Object getDeptStatsDetail(@PathVariable("deptId") Long deptId) {
        try {
            return bizService.getDeptStatsDetail(deptId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 批量获取多个部门统计信息
     * @param deptIds 部门ID列表（逗号分隔）
     * @return 部门统计列表
     */
    @GetMapping("/dept/batch")
    public Object getBatchDeptStats(@RequestParam("deptIds") String deptIds) {
        try {
            String[] ids = deptIds.split(",");
            List<Map<String, Object>> result = new java.util.ArrayList<>();

            for (String idStr : ids) {
                try {
                    Long deptId = Long.parseLong(idStr.trim());
                    Map<String, Object> stats = bizService.getDeptStatsDetail(deptId);
                    result.add(stats);
                } catch (NumberFormatException e) {
                    // 跳过无效的ID
                }
            }

            return result;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取对比数据：不同维度的完成率对比
     * @param dimension 对比维度：dept, year, level
     * @return 对比数据
     */
    @GetMapping("/comparison/{dimension}")
    public Object getCompletionComparison(@PathVariable("dimension") String dimension) {
        try {
            Map<String, Object> result = new java.util.HashMap<>();

            switch (dimension.toLowerCase()) {
                case "dept":
                    // 各部门完成率对比
                    result.put("data", bizService.getDeptTaskCompletionRates());
                    result.put("dimension", "部门");
                    break;

                case "year":
                    // 历年完成率对比（需要扩展Mapper）
                    result.put("data", getYearComparisonData());
                    result.put("dimension", "年度");
                    break;

                case "level":
                    // 各级别任务完成率对比
                    result.put("data", getLevelComparisonData());
                    result.put("dimension", "任务级别");
                    break;

                default:
                    throw new RuntimeException("不支持的对比维度: " + dimension);
            }

            return result;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    // 辅助方法：获取年度对比数据（需要扩展实现）
    private List<Map<String, Object>> getYearComparisonData() {
        // 这里简化实现，实际需要从数据库查询历年数据
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        // 示例数据
        for (int year = 2023; year <= 2025; year++) {
            Map<String, Object> yearData = new java.util.HashMap<>();
            yearData.put("year", year);
            yearData.put("totalTasks", 50 + (year - 2023) * 10);
            yearData.put("completedTasks", 30 + (year - 2023) * 15);
            yearData.put("completionRate", 60 + (year - 2023) * 10);
            result.add(yearData);
        }

        return result;
    }

    // 辅助方法：获取级别对比数据
    private List<Map<String, Object>> getLevelComparisonData() {
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        // 一级任务
        TaskCompletionVO firstLevel = bizService.getFirstLevelTaskCompletionRate();
        result.add(createLevelData("一级任务", firstLevel));

        // 二级任务（需要扩展Mapper）
        result.add(createLevelData("二级任务", 100, 60));

        // 三级任务（需要扩展Mapper）
        result.add(createLevelData("三级任务", 200, 120));

        return result;
    }

    private Map<String, Object> createLevelData(String levelName, TaskCompletionVO vo) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("level", levelName);
        data.put("totalTasks", vo.getTotalTasks());
        data.put("completedTasks", vo.getCompletedTasks());
        data.put("completionRate", vo.getCompletionRate());
        return data;
    }

    private Map<String, Object> createLevelData(String levelName, int totalTasks, int completedTasks) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("level", levelName);
        data.put("totalTasks", totalTasks);
        data.put("completedTasks", completedTasks);

        if (totalTasks > 0) {
            data.put("completionRate",
                    java.math.BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                            .setScale(2, java.math.BigDecimal.ROUND_HALF_UP)
            );
        } else {
            data.put("completionRate", java.math.BigDecimal.ZERO);
        }

        return data;
    }
}
```

---

### <a id='src\main\java\org\example\controller\performancecontroller-java'></a>src\main\java\org\example\controller\PerformanceController.java

```java
package org.example.controller;

import org.example.entity.vo.ErrorVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.example.service.PerformanceService;

@RestController
@RequestMapping("/performance")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    @GetMapping
    public Object getAllPerformance() {
        try {
            return performanceService.getAllPerformance();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @PostMapping("/refresh")
    public Object refreshPerformance() {
        try {
            return performanceService.calcuateAllPerformance();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @GetMapping("/{perfId}")
    public Object getPerformanceById(@PathVariable("perfId") Long perfId) {
        try {
            return performanceService.getPerformanceById(perfId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    @GetMapping("/task/{taskId}")
    public Object getPerformanceByTaskId(@PathVariable("taskId") Long taskId) {
        try {
            return performanceService.getPerfByTaskId(taskId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

}
```

---

### <a id='src\main\java\org\example\controller\scheduledtaskcontroller-java'></a>src\main\java\org\example\controller\ScheduledTaskController.java

```java
package org.example.controller;

import org.example.service.ScheduledTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduled")
public class ScheduledTaskController {

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    @PostMapping("/month_leader_trigger")
    public String triggerManualReminder() {
        return scheduledTaskService.triggerMonthDeptLeaderReminder();
    }

    @PostMapping("/month_auditor_trigger")
    public String triggerAuditorMonthReminder() {
        return scheduledTaskService.triggerMonthAuditorReminder();

    }

    @PostMapping("/year_trigger")
    public String triggerYearReminder() {
        return scheduledTaskService.triggerYearlyReminder();
    }

    @PostMapping("/update_task_status")
    public void updateTaskStatus() {
        scheduledTaskService.updateTaskStatus();
    }
}
```

---

### <a id='src\main\java\org\example\controller\syscontroller-java'></a>src\main\java\org\example\controller\SysController.java

```java
package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.SysNotice;
import org.example.entity.SysUser;
import org.example.entity.dto.*;
import org.example.entity.vo.ErrorVO;
import org.example.entity.vo.SysLogoutVO;
import org.example.service.SysService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 系统控制器：处理用户管理、登录认证、文件上传等系统功能
 */
@RestController
@RequestMapping("/system")
public class SysController {

    @Autowired
    private SysService sysService;

    /**
     * 获取所有用户
     * @return 用户列表或错误信息
     */
    @GetMapping("/allUsers")
    public Object getAllUsers() {
        try{
            return sysService.getAllUsers();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 添加用户
     * @param user 用户数据
     * @return 操作结果或错误信息
     */
    @PostMapping("/users/add")
    public Object addUser(@RequestBody SysUserDTO  user) {
        try{
            sysService.addUser(user);
            return "用户 "+user.getUserName()+" 添加成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 更新用户
     * @param user 用户数据
     * @return 操作结果或错误信息
     */
    @PostMapping("/users/update")
    public Object updateUser(@RequestBody SysUserDTO user) {
        try{
            sysService.updateUser(user);
            return "用户 "+user.getUserName()+" 更新成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 删除用户
     * @param userId 用户ID
     * @return 操作结果或错误信息
     */
    @PostMapping("/users/delete/{userId}")
    public Object deleteUser(@PathVariable("userId") Long userId) {
        try{
            sysService.deleteUser(userId);
            return "用户 "+userId+" 删除成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据部门ID获取部门信息（含 leaderId）
     * @param deptId 部门ID
     * @return 部门信息或错误信息
     */
    @GetMapping("/dept/{deptId}")
    public Object getDeptById(@PathVariable("deptId") Long deptId) {
        try {
            return sysService.getDeptById(deptId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 用户登录
     * @param sysLoginDTO 登录数据
     * @return 登录结果或错误信息
     */
    @PostMapping("/login")
    public Object login(@RequestBody SysLoginDTO sysLoginDTO){
        try{
            return sysService.login(sysLoginDTO.getUser_id(), sysLoginDTO.getPassword());
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 修改密码
     * @param sysPasswordDTO 密码数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/password")
    public Object changePassword(@RequestBody SysPwdDTO sysPasswordDTO, HttpServletRequest request){
        try{
            //根据token获取用户ID
            Long userId=JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            sysService.changePassword(userId,sysPasswordDTO.getNew_password());
            return new SysPwdDTO("密码修改成功");
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 用户注销
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/logout")
    public Object logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null) {
            try {
                sysService.logout(token);
                return new SysLogoutVO("注销成功");
            } catch (Exception e) {
                return new ErrorVO(e.getMessage(), 500);
            }
        }
        return new ErrorVO("token不存在", 401);
    }

    /**
     * 上传文件
     * @param file 文件对象
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 文件信息或错误信息
     */
    @PostMapping("/upload/{task_id}")
    public Object uploadFile(@RequestParam("file") MultipartFile file, @PathVariable("task_id") Long taskId, HttpServletRequest request){
        try{
            return sysService.uploadFile(file,taskId,request);
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 根据file_id下载文件
     * @param fileId 文件ID
     * @param response HTTP响应
     * @return 文件流或错误信息
     */
    @GetMapping("/download/{file_id}")
    public Object downloadFile(@PathVariable("file_id") Long fileId, HttpServletResponse response){
        try{
            sysService.downloadFile(fileId,response);
            return "下载成功";
        }catch (Exception e){
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 发送通知
     * @param sysNoticeDTO 通知数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/notice")
    public Object sendNotice(@RequestBody SysNoticeDTO sysNoticeDTO, HttpServletRequest request){
        try{
            Long userId=JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            sysService.sendNotice(sysNoticeDTO,userId);
            return "发送成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 查看当前用户收到的通知
     * @param request HTTP请求
     * @return 通知列表或异常
     */
    @GetMapping("/notice")
    public List<SysNotice> getNotices(HttpServletRequest request) {
        try{
            Long userId=JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            return sysService.getNotices(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设为已读
     * @param noticeId 通知ID
     * @return 操作结果或错误信息
     */
    @PostMapping("/notice/{notice_id}")
    public Object setRead(@PathVariable("notice_id") Long noticeId){
        try{
            sysService.setRead(noticeId);
            return "已读";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 站内信息预警
     * input token to_user_nick_name title/content source_id
     * @param sysAlertDTO 预警数据
     * @param request HTTP请求
     * @return 操作结果或错误信息
     */
    @PostMapping("/alert")
    public Object sendAlert(@RequestBody SysAlertDTO sysAlertDTO, HttpServletRequest request){
        try{
            Long userId=JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            sysService.sendAlert(sysAlertDTO,userId);
            return "发送成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }


}
```

---

### <a id='src\main\java\org\example\controller\trenddatacontroller-java'></a>src\main\java\org\example\controller\TrendDataController.java

```java
package org.example.controller;



import org.example.entity.BizTrendData;
import org.example.entity.vo.ErrorVO;
import org.example.service.TrendDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard/trend")
public class TrendDataController {

    @Autowired
    private TrendDataService trendDataService;

    /**
     * 获取趋势数据
     * @param year 年份（可选，默认当前年份）
     */
    @GetMapping("/{year}")
    public Object getTrendData(@PathVariable(value = "year",required = false) Integer year) {
        try {
            List<BizTrendData> trendData = trendDataService.getTrendDataByYear(year);
            return trendData;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 获取当前年份的趋势数据
     */
    @GetMapping("/")
    public Object getCurrentYearTrendData() {
        try {
            List<BizTrendData> trendData = trendDataService.getTrendDataByYear(null);
            return trendData;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 手动记录（测试用）
     */
    @PostMapping("/record")
    public Object manualRecord() {
        try {
            return trendDataService.triggerRecord();
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
}
```

---

### <a id='src\main\java\org\example\entity\bizachievement-java'></a>src\main\java\org\example\entity\BizAchievement.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 标志性成果表实体类
 * 对应表：biz_achievement
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAchievement {
    private Long achId; // 成果ID(主键)

    /**
     * 成果类别枚举：
     * 1:落实立德树人根本任务 2:创新产教融合机制 3:打造高水平专业群
     * 4:建设一流核心课程 5:开放优质新形态教材 6:建设高水平双师队伍
     * 7:建设产教融合实训基地 8:构建数字化教学新生态 9:拓展国际交流与合作
     * 10:打造一流区域型高端智库
     */
    private Integer category; // 类别

    /**
     * 成果级别：国家级/省级/市级
     */
    private String level; // 级别
    private String achName; // 成果名称
    private String department; // 组织部门(如：教育部办公厅等，区别于校内部门)
    private Date gotTime; // 颁发时间
    private Long deptId; // 部门ID
    private Long fileId; // 佐证材料文件ID
    private String comment; // 备注

    /**
     * 是否竞赛：0:不是竞赛 1:是竞赛
     */
    private Integer isCompetition; // 是否竞赛

    private Integer teDengJiang; // 特等奖数量
    private Integer yiDengJiang; // 一等奖数量
    private Integer erDengJiang; // 二等奖数量
    private Integer sanDengJiang; // 三等奖数量
    private Integer jinJiang; // 金奖数量
    private Integer yinJiang; // 银奖数量
    private Integer tongJiang; // 铜奖数量
    private Integer youShengJiang; // 优胜奖数量
    private Integer budDengDengCi; // 不定等次数量

    private Long createBy; // 创建人ID(关联sys_user表userId)
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间(默认当前时间)
}
```

---

### <a id='src\main\java\org\example\entity\bizauditlog-java'></a>src\main\java\org\example\entity\BizAuditLog.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditLog {
    private Long logId; // 日志ID
    private Long subId; // 提交ID
    private Long operatorId; // 操作人ID
    private String actionType; // 动作
    /**
     * 流程状态参考 BizMaterialSubmission 的 flowStatus 枚举
     */
    private Integer preStatus; // 前状态
    private Integer postStatus; // 后状态
    private String comment; // 意见
    private Date createTime; // 创建时间
}
```

---

### <a id='src\main\java\org\example\entity\bizmaterialsubmission-java'></a>src\main\java\org\example\entity\BizMaterialSubmission.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizMaterialSubmission {
    private Long subId; // 提交ID
    private Long taskId; // 任务ID
    private Long fileId; // 文件ID

    // 填报数据相关
    private BigDecimal reportedValue; // 本次填报完成值
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
    private Long submitBy; // 提交人ID
    private Long submitDeptId; // 提交人部门ID
    private Long manageDeptId; // 归口部门ID
    private Date submitTime; // 提交时间
    private String fileSuffix; // 后缀（仅允许pdf/doc/docx）

    /**
     * 流程状态枚举：
     * 0:草稿
     * 10:待[所在部门]审批 20:待[归口部门]审批 30:待[管理员]审批
     * 40:已归档
     * -10:被所在部门退回 -20:被归口部门退回 -30:被管理员退回
     */
    private Integer flowStatus;
    private Long currentHandlerId; // 当前处理人ID
    private Integer isDelete; // 0:存在 1:删除
}
```

---

### <a id='src\main\java\org\example\entity\bizperformance-java'></a>src\main\java\org\example\entity\BizPerformance.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizPerformance {
    private Long perfId; // 指标ID
    private Long projectId; // 项目ID
    private Long parentId; // 父指标ID
    private String ancestors; // 祖先指标ID集合
    private String perfCode; // 编码
    private String perfName; // 名称

    private BigDecimal targetValue; // 总目标值
    private BigDecimal currentValue; // 当前完成值(计算得出)
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大

    private Long deptId; // 归口部门ID
    private Long principalId; // 归口负责人ID
    private Long auditorId;// 审核人ID
    private Long leaderId; // 任务负责人ID

    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

---

### <a id='src\main\java\org\example\entity\bizperformanceyear-java'></a>src\main\java\org\example\entity\BizPerformanceYear.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizPerformanceYear {
    private Long yearId; // 年度ID
    private Long perfId; // 指标ID
    private Integer year; // 年份
    private BigDecimal targetValue; // 年度目标值
    private BigDecimal actualValue; // 年度实际完成
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
    private Integer isDelete; // 0:存在 1:删除
}
```

---

### <a id='src\main\java\org\example\entity\bizproject-java'></a>src\main\java\org\example\entity\BizProject.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizProject {
    private Long projectId; // 项目ID
    private String projectName; // 项目名称
    private String projectCode; // 项目编码
    private Long leaderId; // 项目负责人ID
    private Date startDate; // 开始日期
    private Date endDate; // 结束日期
    /**
     * 项目状态枚举：
     * 0:未开始 1:进行中 2:已完成
     * 3:暂停 4:逾期
     */
    private String status;
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

---

### <a id='src\main\java\org\example\entity\bizprojectphase-java'></a>src\main\java\org\example\entity\BizProjectPhase.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizProjectPhase {
    private Long phaseId; // 阶段ID
    private Long projectId; // 项目ID
    private String phaseName; // 阶段名称
    private Date startDate; // 开始日期
    private Date endDate; // 结束日期
    private String isCurrent; // 是否当前阶段 0:否 1:是
    private Integer isDelete; // 0:存在 1:删除
}
```

---

### <a id='src\main\java\org\example\entity\biztask-java'></a>src\main\java\org\example\entity\BizTask.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTask {
    private Long taskId; // 任务ID
    private Long projectId; // 项目ID
    private Long parentId; // 父任务节点ID
    private String ancestors; // 祖先节点ID集合
    private Integer phase; // 所属年份
    private String taskCode; // 任务编号
    private String taskName; // 任务名称
    private Integer level; // 任务层级
    private String comment;// 任务描述

    // 组织归属相关
    private Long deptId; // 归口部门ID
    private Long auditorId;//审核人ID
    private Long principalId; // 归口负责人ID
    private Long leaderId; // 任务负责人ID

    // 数据需求配置相关
    private String expTarget; // 预期达成情况
    private String expLevel; // 预期成果级别（国/省/市）
    private String expEffect; // 预期效果
    private String expMaterialDesc; // 预期过程（佐证）材料清单(文本描述)
    /**
     * 数据类型枚举：
     * 0:对指标没有影响
     * 1:数值(累加)
     * 2:百分比(取大)
     */
    private String dataType;
    private BigDecimal targetValue; // 目标值
    private BigDecimal currentValue; // 当前完成值(缓存统计)
    private BigDecimal weight; // 权重(冗余)
    private Integer progress; // 任务进度(冗余)
    /**
     * 任务状态枚举：
     * 0:未开始 1:进行中
     * 2:审核中 3:已完成
     */
    private String status;
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

---

### <a id='src\main\java\org\example\entity\biztrenddata-java'></a>src\main\java\org\example\entity\BizTrendData.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTrendData {
    private Long dataId;
    private Integer year;
    private Integer month;
    private Integer day;
    private Date createTime;
    private Integer totalTasks;      // 新增：当年总任务数
    private Integer completionCount; // 完成数量
    private Double completionRate;   // 完成率
    private Integer isDelete;
}
```

---

### <a id='src\main\java\org\example\entity\dto\bizauditdto-java'></a>src\main\java\org\example\entity\dto\BizAuditDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditDTO {
    private Long sub_id;
    private Boolean is_pass;
    private String title;
    private String content;
//    private String file_id;
}
```

---

### <a id='src\main\java\org\example\entity\dto\bizresubdto-java'></a>src\main\java\org\example\entity\dto\BizReSubDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizReSubDTO {
    private Long sub_id;
    private BigDecimal reported_value;
    private String data_type;
    private Long file_id;
    private String comment;
}
```

---

### <a id='src\main\java\org\example\entity\dto\bizsubdto-java'></a>src\main\java\org\example\entity\dto\BizSubDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
//reported_value(可空 value_type) task_id
public class BizSubDTO {
    private Long task_id;
    private BigDecimal reported_value;
    private String data_type;
    private Long file_id;
    private String comment;
}
```

---

### <a id='src\main\java\org\example\entity\dto\biztaskdto-java'></a>src\main\java\org\example\entity\dto\BizTaskDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTaskDTO {
    private Long taskId; // 任务ID
    private Long projectId; // 项目ID
    private Long parentId; // 父任务节点ID
    private String ancestors; // 祖先节点ID集合
    private Integer phase; // 所属年份
    private String taskCode; // 任务编号
    private String taskName; // 任务名称
    private Integer level; // 任务层级
    private String comment;// 任务描述

    // 组织归属相关
    private Long deptId; // 归口部门ID
    private Long auditorId;//审核人ID
    private Long principalId; // 归口负责人ID
    private Long leaderId; // 任务负责人ID

    // 数据需求配置相关
    private String expTarget; // 预期达成情况
    private String expLevel; // 预期成果级别（国/省/市）
    private String expEffect; // 预期效果
    private String expMaterialDesc; // 预期过程（佐证）材料清单(文本描述)
    /**
     * 数据类型枚举：
     * 0:对指标没有影响
     * 1:数值(累加)
     * 2:百分比(取大)
     */
    private String dataType;
    private BigDecimal targetValue; // 目标值
    private BigDecimal currentValue; // 当前完成值(缓存统计)
    private BigDecimal weight; // 权重(冗余)
    private Integer progress; // 任务进度(冗余)
    /**
     * 任务状态枚举：
     * 0:未开始 1:进行中
     * 2:审核中 3:已完成
     */
    private String status;

}
```

---

### <a id='src\main\java\org\example\entity\dto\fileuploaddto-java'></a>src\main\java\org\example\entity\dto\FileUploadDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//上传文件 post
//input token reported_value(可空 value_type) task_id文件
//新生成file数据
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadDTO {
    private String filename;
    private String filepath;
}
```

---

### <a id='src\main\java\org\example\entity\dto\sysalertdto-java'></a>src\main\java\org\example\entity\dto\SysAlertDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//站内信息 预警
//input token to_user_nick_name title/content source_id
public class SysAlertDTO {
    private String to_user_nick_name;
    private String title;
    private String content;
    private Long source_id;
}
```

---

### <a id='src\main\java\org\example\entity\dto\syslogindto-java'></a>src\main\java\org\example\entity\dto\SysLoginDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysLoginDTO {
    private Long user_id;
    private String password;
}
```

---

### <a id='src\main\java\org\example\entity\dto\sysnoticedto-java'></a>src\main\java\org\example\entity\dto\SysNoticeDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysNoticeDTO {
    private Long to_user_id;
    private String type;
    private String trigger_event;
    private String title;
    private String content;
    private String source_type;
    private Long source_id;
    private String is_read;
}
```

---

### <a id='src\main\java\org\example\entity\dto\syspwddto-java'></a>src\main\java\org\example\entity\dto\SysPwdDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysPwdDTO {
    private String new_password;
}
```

---

### <a id='src\main\java\org\example\entity\dto\sysuserdto-java'></a>src\main\java\org\example\entity\dto\SysUserDTO.java

```java
package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUserDTO {
    private Long userId; // 用户ID
    private Long deptId; // 所属部门ID
    private String userName; // 账号
    private String nickName; // 姓名
    private String email; // 邮箱
    private String password; // 密码
    private String role; // 角色 0:admin 1:user 2:leader
    private String status; // 状态 0:正常 1:停用
    private Integer isDelete; // 0:存在 1:删除
}
```

---

### <a id='src\main\java\org\example\entity\reltaskperformance-java'></a>src\main\java\org\example\entity\RelTaskPerformance.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelTaskPerformance {
    private Long id; // 关联ID
    private Long taskId; // 任务ID
    private Long perfId; // 指标ID
    private Long yearId; // 绩效年度ID
    private BigDecimal weight; // 权重
    private BigDecimal contributionValue; // 该任务为KPI贡献的数值
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
}
```

---

### <a id='src\main\java\org\example\entity\sysdept-java'></a>src\main\java\org\example\entity\SysDept.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysDept {
    private Long deptId; // 部门ID
    private String deptName; // 部门名称
    private Long leaderId; // 部门负责人ID
    private String status; // 状态 0:正常 1:停用
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

---

### <a id='src\main\java\org\example\entity\sysfile-java'></a>src\main\java\org\example\entity\SysFile.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysFile {
    private Long fileId; // 文件ID
    private String fileName; // 文件名
    private String filePath; // 路径
    private String fileUrl; // URL
    private String fileSuffix; // 后缀
    private Long fileSize; // 大小
    private Long uploadBy; // 上传人ID
    private Integer isDelete; // 0:存在 1:删除
    private Date uploadTime; // 上传时间
}
```

---

### <a id='src\main\java\org\example\entity\sysnotice-java'></a>src\main\java\org\example\entity\SysNotice.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysNotice {
    private Long noticeId; // 通知ID
    private Long fromUserId; // 发起人ID
    private Long toUserId; // 接收人ID
    private String type; // 类型
    private String triggerEvent; // 触发事件
    private String title; // 标题
    private String content; // 内容
    private String sourceType; // 关联来源类型
    private Long sourceId; // 关联业务ID
    private String isRead; // 阅读状态 0:未读 1:已读
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
}
```

---

### <a id='src\main\java\org\example\entity\sysuser-java'></a>src\main\java\org\example\entity\SysUser.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUser {
    private Long userId; // 用户ID
    private Long deptId; // 所属部门ID
    private String userName; // 账号
    private String nickName; // 姓名
    private String email; // 邮箱
    private String password; // 密码
    private String role; // 角色 0:admin 1:user 2:leader
    private String status; // 状态 0:正常 1:停用
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

---

### <a id='src\main\java\org\example\entity\tokenblacklist-java'></a>src\main\java\org\example\entity\TokenBlacklist.java

```java
package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenBlacklist {
    private String token; // 失效token
    private Date expiryTime; // 过期时间
}
```

---

### <a id='src\main\java\org\example\entity\vo\bizauditvo-java'></a>src\main\java\org\example\entity\vo\BizAuditVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAuditVO {
    private Long subId; // 提交ID
    private Long taskId; // 任务ID
    private Long fileId; // 文件ID
    private String filename; // 文件名

    // 填报数据相关
    private BigDecimal reportedValue; // 本次填报完成值
    private String dataType; // 数据类型 0:无影响 1:数值累加 2:百分比取大
    private Long submitBy; // 提交人ID
    private Long submitDeptId; // 提交人部门ID
    private Long manageDeptId; // 归口部门ID
    private Date submitTime; // 提交时间
    private String fileSuffix; // 后缀（仅允许pdf/doc/docx）

    /**
     * 流程状态枚举：
     * 0:草稿
     * 10:待[所在部门]审批 20:待[归口部门]审批 30:待[管理员]审批
     * 40:已归档
     * -10:被所在部门退回 -20:被归口部门退回 -30:被管理员退回
     */
    private Integer flowStatus;
    private Long currentHandlerId; // 当前处理人ID
    private Integer isDelete; // 0:存在 1:删除


}
```

---

### <a id='src\main\java\org\example\entity\vo\biztaskvo-java'></a>src\main\java\org\example\entity\vo\BizTaskVo.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizTaskVo {
    private Long taskId; // 任务ID
    private Long projectId; // 项目ID
    private Long parentId; // 父任务节点ID
    private String ancestors; // 祖先节点ID集合
    private Integer phase; // 所属年份
    private String taskCode; // 任务编号
    private String taskName; // 任务名称
    private Integer level; // 任务层级
    private String comment;// 任务描述

    // 组织归属相关
    private Long deptId; // 归口部门ID
    private String deptName;
    private Long principalId; // 归口负责人ID
    private String principalName;
    private Long auditorId;//审核人ID
    private String auditorName;
    private Long leaderId; // 任务负责人ID
    private String leaderName;

    // 数据需求配置相关
    private String expTarget; // 预期达成情况
    private String expLevel; // 预期成果级别（国/省/市）
    private String expEffect; // 预期效果
    private String expMaterialDesc; // 预期过程（佐证）材料清单(文本描述)
    /**
     * 数据类型枚举：
     * 0:对指标没有影响
     * 1:数值(累加)
     * 2:百分比(取大)
     */
    private String dataType;
    private BigDecimal targetValue; // 目标值
    private BigDecimal currentValue; // 当前完成值(缓存统计)
    private BigDecimal weight; // 权重(冗余)
    private Integer progress; // 任务进度(冗余)
    /**
     * 任务状态枚举：
     * 0:未开始 1:进行中
     * 2:审核中 3:已完成
     */
    private String status;
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
```

---

### <a id='src\main\java\org\example\entity\vo\dashboardsummaryvo-java'></a>src\main\java\org\example\entity\vo\DashboardSummaryVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 看板数据汇总VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryVO {
    // 整体数据
    private TaskCompletionVO overallCompletion;      // 所有任务完成率
    private TaskCompletionVO yearCompletion;         // 本年度任务完成率
    private TaskCompletionVO midTermCompletion;      // 中期任务完成率

    // 一级任务
    private TaskCompletionVO firstLevelCompletion;   // 一级任务完成率

    // 部门数据
    private List<DeptTaskStatsVO> deptOverallStats;  // 各部门整体完成率
    private List<DeptTaskStatsVO> deptYearStats;     // 各部门本年度完成率
    private List<DeptTaskStatsVO> deptMidTermStats;  // 各部门中期完成率

    // 一级任务详情
    private List<FirstLevelTaskDetailVO> firstLevelTasks;

    // 统计时间
    private String currentYear;
    private String midTermEndYear = "2028";
    private String updateTime;
}
```

---

### <a id='src\main\java\org\example\entity\vo\depttaskstatsvo-java'></a>src\main\java\org\example\entity\vo\DeptTaskStatsVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 部门任务统计VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeptTaskStatsVO {
    private Long deptId;
    private String deptName;
    private Integer totalTasks;
    private Integer completedTasks;
    private BigDecimal completionRate;

    // 新增：任务状态统计
    private Integer notStartedCount;   // 未开始任务数
    private Integer inProgressCount;   // 进行中任务数
    private Integer inReviewCount;     // 审核中任务数
    private Integer finishedCount;     // 已完成任务数

    private BigDecimal notStartedRate;  // 未开始占比
    private BigDecimal inProgressRate;  // 进行中占比
    private BigDecimal inReviewRate;    // 审核中占比
    private BigDecimal finishedRate;    // 已完成占比

    // 计算完成率和状态占比
    public void calculateCompletionRate() {
        if (totalTasks != null && totalTasks > 0) {
            this.completionRate = BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);

            // 计算各个状态占比
            if (notStartedCount != null) {
                this.notStartedRate = BigDecimal.valueOf(notStartedCount * 100.0 / totalTasks)
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                this.notStartedRate = BigDecimal.ZERO;
            }

            if (inProgressCount != null) {
                this.inProgressRate = BigDecimal.valueOf(inProgressCount * 100.0 / totalTasks)
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                this.inProgressRate = BigDecimal.ZERO;
            }

            if (inReviewCount != null) {
                this.inReviewRate = BigDecimal.valueOf(inReviewCount * 100.0 / totalTasks)
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                this.inReviewRate = BigDecimal.ZERO;
            }

            if (finishedCount != null) {
                this.finishedRate = BigDecimal.valueOf(finishedCount * 100.0 / totalTasks)
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                this.finishedRate = BigDecimal.ZERO;
            }
        } else {
            this.completionRate = BigDecimal.ZERO;
            this.notStartedRate = BigDecimal.ZERO;
            this.inProgressRate = BigDecimal.ZERO;
            this.inReviewRate = BigDecimal.ZERO;
            this.finishedRate = BigDecimal.ZERO;
        }
    }
}
```

---

### <a id='src\main\java\org\example\entity\vo\errorvo-java'></a>src\main\java\org\example\entity\vo\ErrorVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorVO {
    private String message;
    private Integer code;
}
```

---

### <a id='src\main\java\org\example\entity\vo\fileuploadvo-java'></a>src\main\java\org\example\entity\vo\FileUploadVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadVO {
    private Long fileId;
    private String filename;
    private String filepath;
}
```

---

### <a id='src\main\java\org\example\entity\vo\firstleveltaskdetailvo-java'></a>src\main\java\org\example\entity\vo\FirstLevelTaskDetailVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 一级任务详情VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FirstLevelTaskDetailVO {
    private Long taskId;
    private String taskName;
    private Long deptId;
    private String deptName;
    private String status;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private Integer progress;
    private String createTime;
    private String updateTime;

    // 判断是否完成
    public boolean isCompleted() {
        return "3".equals(status);
    }

    // 获取状态中文描述
    public String getStatusText() {
        switch (status) {
            case "0": return "未开始";
            case "1": return "进行中";
            case "2": return "审核中";
            case "3": return "已完成";
            default: return "未知";
        }
    }

    // 获取完成率
    public BigDecimal getCompletionRate() {
        if (targetValue != null && targetValue.compareTo(BigDecimal.ZERO) > 0) {
            return currentValue.multiply(BigDecimal.valueOf(100))
                    .divide(targetValue, 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
```

---

### <a id='src\main\java\org\example\entity\vo\sysloginvo-java'></a>src\main\java\org\example\entity\vo\SysLoginVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysLoginVO {

    private String nick_name;
    private String token;
}
```

---

### <a id='src\main\java\org\example\entity\vo\syslogoutvo-java'></a>src\main\java\org\example\entity\vo\SysLogoutVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysLogoutVO {
    private String message;
}
```

---

### <a id='src\main\java\org\example\entity\vo\syspasswordvo-java'></a>src\main\java\org\example\entity\vo\SysPasswordVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysPasswordVO {
    private String message;
}
```

---

### <a id='src\main\java\org\example\entity\vo\taskcompletionvo-java'></a>src\main\java\org\example\entity\vo\TaskCompletionVO.java

```java
package org.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 任务完成率统计VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCompletionVO {
    private Integer totalTasks;         // 总任务数
    private Integer completedTasks;     // 已完成任务数
    private BigDecimal completionRate;  // 完成率（百分比）
    private String period;             // 统计周期：all, year, midterm
    private String description;        // 描述

    // 新增：任务状态统计
    private Integer notStartedCount;   // 未开始任务数
    private Integer inProgressCount;   // 进行中任务数
    private Integer inReviewCount;     // 审核中任务数
    private Integer finishedCount;     // 已完成任务数

    private BigDecimal notStartedRate;  // 未开始占比
    private BigDecimal inProgressRate;  // 进行中占比
    private BigDecimal inReviewRate;    // 审核中占比
    private BigDecimal finishedRate;    // 已完成占比

    // 方便构造的静态方法
    public TaskCompletionVO(Integer totalTasks, Integer completedTasks, String period, String description,
                            Integer notStartedCount, Integer inProgressCount, Integer inReviewCount, Integer finishedCount) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.period = period;
        this.description = description;

        // 设置状态统计
        this.notStartedCount = notStartedCount;
        this.inProgressCount = inProgressCount;
        this.inReviewCount = inReviewCount;
        this.finishedCount = finishedCount;

        // 计算完成率
        if (totalTasks != null && totalTasks > 0) {
            this.completionRate = BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);

            // 计算各个状态占比
            this.notStartedRate = BigDecimal.valueOf(notStartedCount * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);
            this.inProgressRate = BigDecimal.valueOf(inProgressCount * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);
            this.inReviewRate = BigDecimal.valueOf(inReviewCount * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);
            this.finishedRate = BigDecimal.valueOf(finishedCount * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.completionRate = BigDecimal.ZERO;
            this.notStartedRate = BigDecimal.ZERO;
            this.inProgressRate = BigDecimal.ZERO;
            this.inReviewRate = BigDecimal.ZERO;
            this.finishedRate = BigDecimal.ZERO;
        }
    }

    // 原有构造方法的重载，保持向后兼容
    public TaskCompletionVO(Integer totalTasks, Integer completedTasks, String period, String description) {
        this(totalTasks, completedTasks, period, description, 0, 0, 0, completedTasks);
    }
}
```

---

### <a id='src\main\java\org\example\mapper\achievementmapper-java'></a>src\main\java\org\example\mapper\AchievementMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import org.example.entity.BizAchievement;

import java.util.List;

/**
 * 标志性成果数据访问接口
 * 对应表：biz_achievement
 */
@Mapper
public interface AchievementMapper {

    /**
     * 根据成果ID查询单条成果信息
     * @param achId 成果ID
     * @return 标志性成果对象
     */
    @Select("SELECT * FROM biz_achievement WHERE ach_id = #{achId} AND is_delete = 0")
    BizAchievement getAchievementById(Long achId);

    /**
     * 查询所有未删除的标志性成果列表
     * @return 标志性成果列表
     */
    @Select("SELECT * FROM biz_achievement WHERE is_delete = 0")
    List<BizAchievement> listAllAchievements();


    /**
     * 查询最新的一个标志性成果
     * @return 最新的标志性成果对象
     */
    @Select("SELECT * FROM biz_achievement WHERE is_delete = 0 ORDER BY create_time DESC LIMIT 1")
    BizAchievement getLatestAchievement();

    /**
     * 新增标志性成果（自增主键返回）
     * @param achievement 标志性成果实体
     * @return 自增的成果ID
     */
    @Insert("INSERT INTO biz_achievement (category, level, ach_name, department, got_time, dept_id, file_id, " +
            "comment, is_competition, te_deng_jiang, yi_deng_jiang, er_deng_jiang, san_deng_jiang, jin_jiang, " +
            "yin_jiang, tong_jiang, you_sheng_jiang, bud_deng_deng_ci, create_by, is_delete, create_time) " +
            "VALUES (#{category}, #{level}, #{achName}, #{department}, #{gotTime}, #{deptId}, #{fileId}, " +
            "#{comment}, #{isCompetition}, #{teDengJiang}, #{yiDengJiang}, #{erDengJiang}, #{sanDengJiang}, #{jinJiang}, " +
            "#{yinJiang}, #{tongJiang}, #{youShengJiang}, #{budDengDengCi}, #{createBy}, #{isDelete}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "achId", keyColumn = "ach_id")
    void addAchievement(BizAchievement achievement);

    /**
     * 根据成果ID修改标志性成果信息
     * @param achievement 标志性成果实体（含需修改的成果ID）
     */
    @Update("UPDATE biz_achievement SET category = #{category}, level = #{level}, ach_name = #{achName}, " +
            "department = #{department}, got_time = #{gotTime}, dept_id = #{deptId}, file_id = #{fileId}, " +
            "comment = #{comment}, is_competition = #{isCompetition}, te_deng_jiang = #{teDengJiang}, " +
            "yi_deng_jiang = #{yiDengJiang}, er_deng_jiang = #{erDengJiang}, san_deng_jiang = #{sanDengJiang}, " +
            "jin_jiang = #{jinJiang}, yin_jiang = #{yinJiang}, tong_jiang = #{tongJiang}, you_sheng_jiang = #{youShengJiang}, " +
            "bud_deng_deng_ci = #{budDengDengCi}, create_by = #{createBy} " +
            "WHERE ach_id = #{achId} AND is_delete = 0")
    void updateAchievement(BizAchievement achievement);

    /**
     * 逻辑删除标志性成果（置is_delete=1）
     * @param achId 成果ID
     */
    @Update("UPDATE biz_achievement SET is_delete = 1 WHERE ach_id = #{achId}")
    void deleteAchievement(Long achId);
}
```

---

### <a id='src\main\java\org\example\mapper\bizmapper-java'></a>src\main\java\org\example\mapper\BizMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.BizAuditLog;
import org.example.entity.BizMaterialSubmission;
import org.example.entity.BizTask;
import org.example.entity.vo.DeptTaskStatsVO;
import org.example.entity.vo.FirstLevelTaskDetailVO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 业务数据访问接口
 */
@Mapper
public interface BizMapper {

    /**
     * 任务相关操作
     */

    /**
     * 获取全部任务
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task")
    List<BizTask> getAllTasks();

    /**
     * 根据id获取任务
     * @param taskId 任务ID
     * @return 任务对象
     */
    @Select("SELECT * FROM biz_task WHERE task_id = #{taskId}")
    BizTask getTaskById(Long taskId);

    /**
     * 根据部门id获取任务
     * @param deptId 部门ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE dept_id = #{deptId}")
    List<BizTask> getTasksByDeptId(Long deptId);

    // getTasksByDeptIdAndPhase
    @Select("SELECT * FROM biz_task WHERE dept_id = #{deptId} AND phase = #{phase}")
    List<BizTask> getTasksByDeptIdAndPhase(@Param("deptId") Long deptId, @Param("phase") Integer phase);
    /**
     * 根据归口负责人获取任务
     * @param principalId 负责人ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE principal_id = #{principalId}")
    List<BizTask> getTasksByPrincipalId(Long principalId);

    /**
     * 根据负责人ID获取任务
     * @param leaderId 负责人ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE leader_id = #{leaderId}")
    List<BizTask> getTasksByLeaderId(Long leaderId);

    /**
     * 根据负责人ID或归口负责人ID或审核人ID获取任务
     * @param userId 用户ID
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE leader_id = #{userId} OR principal_id = #{userId} OR auditor_id=#{userId}")
    List<BizTask> getTasks(Long userId);

    /**
     * 获取所有一级任务
     * @return 一级任务列表
     */
    @Select("SELECT * FROM biz_task WHERE level=1")
    List<BizTask> getAllFirstLevelTasks();

    /**
     * 获取当前任务的所有子任务
     * @param taskId 任务ID
     * @return 子任务列表
     */
    @Select("SELECT * FROM biz_task WHERE parent_id = #{taskId}")
    List<BizTask> getAllChildrenTasks(Long taskId);

    /**
     * 根据一级任务id获取其二级子任务
     * @param parentId 父任务ID
     * @return 二级任务列表
     */
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=2")
    List<BizTask> getSecondLevelTasksByParentId(Long parentId);

    /**
     * 根据二级任务id获取其三级子任务
     * @param parentId 父任务ID
     * @return 三级任务列表
     */
    @Select("SELECT * FROM biz_task WHERE parent_id = #{parentId} AND level=3")
    List<BizTask> getThirdLevelTasksByParentId(Long parentId);

    /**
     * 根据任务阶段获取任务
     * @param phase 任务阶段
     * @return 任务列表
     */
    @Select("SELECT * FROM biz_task WHERE phase = #{phase}")
    List<BizTask> getTasksByPhase(Integer phase);

    /**
     * 新增任务
     * @param task 任务实体（taskId会自增，无需传入）
     * @return 影响的行数（1表示新增成功）
     */
    @Insert("INSERT INTO biz_task (" +
            "project_id, parent_id, ancestors, phase, task_code, task_name, level, comment, " +
            "leader_id, auditor_id, principal_id, dept_id, exp_target, exp_level, exp_effect, " +
            "exp_material_desc, data_type, target_value, current_value, weight, progress, " +
            "status, is_delete, create_time, update_time" +
            ") VALUES (" +
            "#{projectId}, #{parentId}, #{ancestors}, #{phase}, #{taskCode}, #{taskName}, #{level}, #{comment}, " +
            "#{leaderId}, #{auditorId}, #{principalId}, #{deptId}, #{expTarget}, #{expLevel}, #{expEffect}, " +
            "#{expMaterialDesc}, #{dataType}, #{targetValue}, #{currentValue}, #{weight}, #{progress}, " +
            "#{status}, #{isDelete}, #{createTime}, #{updateTime}" +
            ")")
    // 新增注解：返回自增的taskId到实体对象中
    @Options(useGeneratedKeys = true, keyProperty = "taskId", keyColumn = "task_id")
    void addTask(BizTask task);

    /**
     * 更新任务
     * 全字段更新任务（根据taskId更新所有字段）
     * @param task 任务实体
     * @return 影响的行数
     */
    @Update("UPDATE biz_task SET project_id=#{projectId},parent_id=#{parentId},ancestors=#{ancestors},phase=#{phase},task_code=#{taskCode},task_name=#{taskName},level=#{level},comment=#{comment},leader_id=#{leaderId},auditor_id=#{auditorId},principal_id=#{principalId},dept_id=#{deptId},exp_target=#{expTarget},exp_level=#{expLevel},exp_effect=#{expEffect},exp_material_desc=#{expMaterialDesc},data_type=#{dataType},target_value=#{targetValue},current_value=#{currentValue},weight=#{weight},progress=#{progress},status=#{status},is_delete=#{isDelete},create_time=#{createTime},update_time=NOW() WHERE task_id=#{taskId}")
    int updateTask(BizTask task);

    /**
     * 提交后更新任务
     * @param task 任务实体
     */
    @Update("UPDATE biz_task SET current_value = #{currentValue}, status = #{status}, update_time = NOW() WHERE task_id = #{taskId}")
    void updateCurrentTask(BizTask task);

    /**
     * 根据任务id获取部门leaderid
     * @param taskId 任务ID
     * @return 负责人ID
     */
    @Select("SELECT leader_id FROM sys_dept WHERE dept_id = (SELECT dept_id FROM biz_task WHERE task_id = #{taskId})")
    Long getTaskLeaderId(Long taskId);

    /**
     * 根据任务id获取归口负责人Id
     * @param taskId 任务ID
     * @return 归口负责人ID
     */
    @Select("SELECT principal_id FROM biz_task WHERE task_id = #{taskId}")
    Long getTaskPrincipalId(Long taskId);

    /**
     * 日志相关操作
     */

    /**
     * 创建审批单日志
     * @param auditLog 审批日志实体
     */
    @Insert("insert into biz_audit_log(sub_id,operator_id,action_type,pre_status,post_status,comment,create_time) values(#{subId},#{operatorId},#{actionType},#{preStatus},#{postStatus},#{comment},#{createTime})")
    void createAuditLog(BizAuditLog auditLog);

    /**
     * 更新日志
     * @param auditLog 审批日志实体
     */
    @Update("update biz_audit_log set sub_id=#{subId},operator_id=#{operatorId},action_type=#{actionType},pre_status=#{preStatus},post_status=#{postStatus},comment=#{comment},create_time=#{createTime} where log_id=#{logId}")
    void updateAuditLog(BizAuditLog auditLog);

    /**
     * 根据 subId 获取审批操作日志（biz_audit_log）
     * @param subId 提交ID
     * @return 审批日志列表
     */
    @Select("SELECT " +
            "log_id AS logId, " +
            "sub_id AS subId, " +
            "operator_id AS operatorId, " +
            "action_type AS actionType, " +
            "pre_status AS preStatus, " +
            "post_status AS postStatus, " +
            "comment, " +
            "create_time AS createTime " +
            "FROM biz_audit_log WHERE sub_id = #{subId} " +
            "ORDER BY create_time DESC, log_id DESC")
    List<BizAuditLog> getAuditLogsBySubId(@Param("subId") Long subId);

    /**
     * 材料提交相关操作
     */

    /**
     * 创建审批流程
     * @param bizMaterialSubmission 材料提交实体
     * @return 提交ID
     */
    @Insert("insert into biz_material_submission(" +
            "task_id, file_id, reported_value, data_type, submit_by, submit_dept_id, " +
            "manage_dept_id, submit_time, file_suffix, flow_status, current_handler_id, is_delete" +
            ") values(" +
            "#{taskId}, #{fileId}, #{reportedValue}, #{dataType}, #{submitBy}, #{submitDeptId}, " +
            "#{manageDeptId}, #{submitTime}, #{fileSuffix}, #{flowStatus}, #{currentHandlerId}, #{isDelete}" +
            ")")
    void createAudit(BizMaterialSubmission bizMaterialSubmission);

    /**
     * 获取最新的审批单id
     * @return 最新的提交ID
     */
    @Select("SELECT sub_id FROM biz_material_submission ORDER BY sub_id DESC LIMIT 1")
    Long getNewestSubId();

    /**
     * 更新审批单 【核心修复点1：注解改为@Update；核心修复点2：where前加空格】
     * @param bizMaterialSubmission 材料提交实体
     */
    @Update("update biz_material_submission set task_id=#{taskId},file_id=#{fileId},reported_value=#{reportedValue},data_type=#{dataType},"
            + "submit_by=#{submitBy},submit_dept_id=#{submitDeptId},manage_dept_id=#{manageDeptId},submit_time=#{submitTime},file_suffix=#{fileSuffix},"
            + "flow_status=#{flowStatus},current_handler_id=#{currentHandlerId},is_delete=#{isDelete} " + // 此处添加空格
            "where sub_id=#{subId}")
    void updateAudit(BizMaterialSubmission bizMaterialSubmission);

    /**
     * 更新任务状态 【优化：参数名统一为驼峰风格，与Java规范一致】
     * @param taskId 任务ID
     * @param status 状态
     */
    @Update("UPDATE biz_task SET status = #{status} WHERE task_id = #{taskId}")
    void updateTaskStatus(@Param("taskId") Long taskId, @Param("status") String status);

    /**
     * 更新审批单flow_status 【优化：参数名统一为驼峰风格】
     * @param subId 提交ID
     * @param flowStatus 流程状态
     */
    @Update("UPDATE biz_material_submission SET flow_status = #{flowStatus} WHERE sub_id = #{subId}")
    void updateAuditFlowStatus(@Param("subId") Long subId, @Param("flowStatus") Integer flowStatus);

    /**
     * 根据taskId及current_handler_id获取审批单
     * @param taskId 任务ID
     * @param currentHandlerId 当前处理人ID
     * @return 材料提交列表
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND current_handler_id = #{currentHandlerId} AND is_delete = 0")
    List<BizMaterialSubmission> getAudit(@Param("taskId") Long taskId,
                                         @Param("currentHandlerId") Long currentHandlerId);

    /**
     * 根据任务id获取最新的审批单
     * @param taskId 任务ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND is_delete = 0 ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getNewestAudit(@Param("taskId") Long taskId);

    /**
     * 获取"待我审批"的审批单（按当前处理人查询）
     * @param userId 用户ID
     * @return 待审批列表
     */
    @Select("SELECT * FROM biz_material_submission " +
            "WHERE current_handler_id = #{userId} AND is_delete = 0 AND flow_status >= 10 AND flow_status < 40 AND is_delete = 0 " +
            "ORDER BY submit_time DESC, sub_id DESC")
    List<BizMaterialSubmission> getTodoAudits(@Param("userId") Long userId);

    /**
     * 按 task_id 获取该任务全部审批单（用于任务详情抽屉展示完整流程）
     * @param taskId 任务ID
     * @return 审批单列表
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND is_delete = 0 ORDER BY sub_id DESC")
    List<BizMaterialSubmission> getAuditsByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据subId获取审批单
     * @param subId 提交ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE sub_id = #{subId} AND is_delete = 0")
    BizMaterialSubmission getAuditBySubId(Long subId);

    /**
     * 根据subId获取提交人
     * @param subId 提交ID
     * @return 提交人ID
     */
    @Select("SELECT submit_by FROM biz_material_submission WHERE sub_id = #{subId} AND is_delete = 0")
    Long getAuditSubmitBy(Long subId);

    /**
     * 根据任务id获取最近的审批单
     * @param taskId 任务ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND is_delete = 0 ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getLatestAuditByTaskId(Long taskId);

    /**
     * 根据任务id获取最近的、且状态为40的审批单
     * @param taskId 任务ID
     * @return 材料提交对象
     */
    @Select("SELECT * FROM biz_material_submission WHERE task_id = #{taskId} AND flow_status = 40 AND is_delete = 0 ORDER BY sub_id DESC LIMIT 1")
    BizMaterialSubmission getLatestApprovedAuditByTaskId(Long taskId);

    /**
     * 获取所有待审核的审批单（flow_status在10-30之间）
     * @return 待审核审批单列表
     */
    @Select("SELECT * FROM biz_material_submission " +
            "WHERE flow_status >= 10 AND flow_status < 40 AND is_delete = 0 " +
            "ORDER BY submit_time DESC")
    List<BizMaterialSubmission> getAllPendingAudits();

    /**
     * 根据处理人ID统计待审核任务数量
     * @param handlerId 处理人ID
     * @return 待审核任务数量
     */
    @Select("SELECT COUNT(*) FROM biz_material_submission " +
            "WHERE current_handler_id = #{handlerId} " +
            "AND flow_status >= 10 AND flow_status < 40 AND is_delete = 0")
    Integer countPendingAuditsByHandler(@Param("handlerId") Long handlerId);

    /**
     * 获取所有任务数量
     * @return 任务总数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE is_delete = 0")
    Integer getTotalTaskCount();

    /**
     * 获取特定状态的任务数量（新增通用方法）
     * @param status 状态码
     * @return 特定状态的任务数量
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE status = #{status} AND is_delete = 0")
    Integer getTaskCountByStatus(@Param("status") String status);

    /**
     * 根据年份获取特定状态的任务数量（新增）
     * @param year 年份
     * @param status 状态码
     * @return 特定状态的任务数量
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase = #{year} AND status = #{status} AND is_delete = 0")
    Integer getYearTaskCountByStatus(@Param("year") Integer year, @Param("status") String status);

    /**
     * 获取中期特定状态的任务数量（新增）
     * @param endYear 截止年份
     * @param status 状态码
     * @return 特定状态的任务数量
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase < #{endYear} AND status = #{status} AND is_delete = 0")
    Integer getMidTermTaskCountByStatus(@Param("endYear") Integer endYear, @Param("status") String status);

    /**
     * 获取一级特定状态的任务数量（新增）
     * @param status 状态码
     * @return 特定状态的任务数量
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE level = 1 AND status = #{status} AND is_delete = 0")
    Integer getFirstLevelTaskCountByStatus(@Param("status") String status);

    /**
     * 获取已完成任务数量
     * @return 已完成任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE status = '3' AND is_delete = 0")
    Integer getCompletedTaskCount();

    /**
     * 获取本年度任务数量
     * @param year 年份
     * @return 本年度任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase = #{year} AND is_delete = 0")
    Integer getYearTaskCount(@Param("year") Integer year);

    /**
     * 获取本年度已完成任务数量
     * @param year 年份
     * @return 本年度已完成任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase = #{year} AND status = '3' AND is_delete = 0")
    Integer getYearCompletedTaskCount(@Param("year") Integer year);

    /**
     * 获取中期任务数量（phase在2028年之前）
     * @param endYear 截止年份
     * @return 中期任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase < #{endYear} AND is_delete = 0")
    Integer getMidTermTaskCount(@Param("endYear") Integer endYear);

    /**
     * 获取中期已完成任务数量
     * @param endYear 截止年份
     * @return 中期已完成任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE phase < #{endYear} AND status = '3' AND is_delete = 0")
    Integer getMidTermCompletedTaskCount(@Param("endYear") Integer endYear);

    /**
     * 获取一级任务数量
     * @return 一级任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE level = 1 AND is_delete = 0")
    Integer getFirstLevelTaskCount();

    /**
     * 获取一级已完成任务数量
     * @return 一级已完成任务数
     */
    @Select("SELECT COUNT(*) FROM biz_task WHERE level = 1 AND status = '3' AND is_delete = 0")
    Integer getFirstLevelCompletedTaskCount();

    /**
     * 获取部门任务数量统计（不带状态统计，Service层处理）
     * @return 部门任务统计列表
     */
    @Select("SELECT " +
            "d.dept_id as deptId, " +
            "d.dept_name as deptName, " +
            "COUNT(t.task_id) as totalTasks, " +
            "SUM(CASE WHEN t.status = '3' THEN 1 ELSE 0 END) as completedTasks " +
            "FROM sys_dept d " +
            "LEFT JOIN biz_task t ON d.dept_id = t.dept_id AND t.is_delete = 0 AND t.level = 3 " +
            "WHERE d.is_delete = 0 " +
            "GROUP BY d.dept_id, d.dept_name " +
            "HAVING COUNT(t.task_id) > 0 "+
            "ORDER BY d.dept_id")
    List<DeptTaskStatsVO> getDeptTaskStats();

    /**
     * 获取部门本年度任务统计（不带状态统计，Service层处理）
     * @param year 年份
     * @return 部门本年度任务统计
     */
    @Select("SELECT " +
            "d.dept_id as deptId, " +
            "d.dept_name as deptName, " +
            "COUNT(t.task_id) as totalTasks, " +
            "SUM(CASE WHEN t.status = '3' THEN 1 ELSE 0 END) as completedTasks " +
            "FROM sys_dept d " +
            "LEFT JOIN biz_task t ON d.dept_id = t.dept_id AND t.phase = #{year} AND t.level = 3 AND t.is_delete = 0 " +
            "WHERE d.is_delete = 0 " +
            "GROUP BY d.dept_id, d.dept_name " +
            "HAVING COUNT(t.task_id) > 0 "+
            "ORDER BY d.dept_id")
    List<DeptTaskStatsVO> getDeptYearTaskStats(@Param("year") Integer year);

    /**
     * 获取部门中期任务统计（不带状态统计，Service层处理）
     * @param endYear 截止年份
     * @return 部门中期任务统计
     */
    @Select("SELECT " +
            "d.dept_id as deptId, " +
            "d.dept_name as deptName, " +
            "COUNT(t.task_id) as totalTasks, " +
            "SUM(CASE WHEN t.status = '3' THEN 1 ELSE 0 END) as completedTasks " +
            "FROM sys_dept d " +
            "LEFT JOIN biz_task t ON d.dept_id = t.dept_id AND t.phase < #{endYear} AND t.level = 3 AND t.is_delete = 0 " +
            "WHERE d.is_delete = 0 " +
            "GROUP BY d.dept_id, d.dept_name " +
            "HAVING COUNT(t.task_id) > 0 "+
            "ORDER BY d.dept_id")
    List<DeptTaskStatsVO> getDeptMidTermTaskStats(@Param("endYear") Integer endYear);

    /**
     * 获取一级任务的详细完成情况
     * @return 一级任务完成情况列表
     */
    @Select("SELECT " +
            "task_id as taskId, " +
            "task_name as taskName, " +
            "dept_id as deptId, " +
            "(SELECT dept_name FROM sys_dept WHERE dept_id = t.dept_id) as deptName, " +
            "status, " +
            "target_value as targetValue, " +
            "current_value as currentValue, " +
            "progress, " +
            "create_time as createTime, " +
            "update_time as updateTime " +
            "FROM biz_task t " +
            "WHERE level = 1 AND is_delete = 0 " +
            "ORDER BY task_id")
    List<FirstLevelTaskDetailVO> getFirstLevelTaskDetails();

//    获取全量任务详细情况
    @Select("SELECT " +
            "task_id as taskId, " +
            "task_name as taskName, " +
            "dept_id as deptId, " +
            "(SELECT dept_name FROM sys_dept WHERE dept_id = t.dept_id) as deptName, " +
            "status, " +
            "target_value as targetValue, " +
            "current_value as currentValue, " +
            "progress, " +
            "create_time as createTime, " +
            "update_time as updateTime " +
            "FROM biz_task t " +
            "WHERE is_delete = 0 " +
            "ORDER BY task_id"
    )
    List<FirstLevelTaskDetailVO> getAllTaskDetails();
}
```

---

### <a id='src\main\java\org\example\mapper\performancemapper-java'></a>src\main\java\org\example\mapper\PerformanceMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.entity.BizPerformance;
import org.example.entity.RelTaskPerformance;

import java.util.List;

//-- 4.1 绩效指标表
//DROP TABLE IF EXISTS `biz_performance`;
//CREATE TABLE `biz_performance` (
//        `perf_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '指标ID',
//        `project_id` bigint(20) NOT NULL,
//    `parent_id` bigint(20) DEFAULT 0,
//        `ancestors` varchar(500) DEFAULT '',
//        `perf_code` varchar(50) DEFAULT NULL COMMENT '编码',
//        `perf_name` varchar(255) NOT NULL COMMENT '名称',
//
//        `target_value` decimal(20,4) DEFAULT 0.00 COMMENT '总目标值',
//        `current_value` decimal(20,4) DEFAULT 0.00 COMMENT '当前完成值(计算得出)',
//        `data_type` char(1) DEFAULT '1' COMMENT '数据类型 0:对指标没有影响 1:数值(累加) 2:百分比(取大)',
//
//        `dept_id` bigint(20) NOT NULL COMMENT '归口部门ID',
//        `principal_id` bigint(20) NOT NULL COMMENT '归口负责人ID',
//        `auditor_id` bigint(20) NOT NULL COMMENT '专业群审核人ID',
//        `leader_id` bigint(20) DEFAULT NULL COMMENT '任务负责人ID',
//
//        `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
//        `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
//        `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
//PRIMARY KEY (`perf_id`),
//CONSTRAINT `fk_perf_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`dept_id`) ON DELETE RESTRICT,
//CONSTRAINT `fk_perf_principal` FOREIGN KEY (`principal_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT
//) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效指标库';
//
//        -- 4.2 绩效年度分解
//DROP TABLE IF EXISTS `biz_performance_year`;
//CREATE TABLE `biz_performance_year` (
//        `year_id` bigint(20) NOT NULL AUTO_INCREMENT,
//    `perf_id` bigint(20) NOT NULL COMMENT '指标ID',
//        `year` int(4) NOT NULL,
//    `target_value` decimal(20,4) DEFAULT 0.00,
//        `actual_value` decimal(20,4) DEFAULT 0.00 COMMENT '年度实际完成',
//        `data_type` char(1) DEFAULT '1' COMMENT '数据类型 0:对指标没有影响 1:数值(累加) 2:百分比(取大)',
//        `is_delete` tinyint(1) DEFAULT 0 COMMENT '0:存在 1:删除',
//PRIMARY KEY (`year_id`),
//CONSTRAINT `fk_year_perf` FOREIGN KEY (`perf_id`) REFERENCES `biz_performance` (`perf_id`) ON DELETE CASCADE
//) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效年度表';
//
//        -- 4.3 任务-绩效关联
//DROP TABLE IF EXISTS `rel_task_performance`;
//CREATE TABLE `rel_task_performance` (
//        `id` bigint(20) NOT NULL AUTO_INCREMENT,
//    `task_id` bigint(20) NOT NULL,
//    `perf_id` bigint(20) NOT NULL,
//    `year_id` bigint(20) NOT NULL,
//    `weight` decimal(5,2) DEFAULT 1.00 ,
//        `contribution_value` decimal(20,4) DEFAULT 0.00 COMMENT '该任务为KPI贡献的数值',
//        `data_type` char(1) DEFAULT '1' COMMENT '数据类型 0:对指标没有影响 1:数值(累加) 2:百分比(取大)',
//PRIMARY KEY (`id`),
//UNIQUE KEY `uk_tp` (`task_id`, `perf_id`, `year_id`),
//CONSTRAINT `fk_rel_task` FOREIGN KEY (`task_id`) REFERENCES `biz_task` (`task_id`) ON DELETE CASCADE,
//CONSTRAINT `fk_rel_perf` FOREIGN KEY (`perf_id`) REFERENCES `biz_performance` (`perf_id`) ON DELETE CASCADE,
//CONSTRAINT `fk_rel_year` FOREIGN KEY (`year_id`) REFERENCES `biz_performance_year` (`year_id`) ON DELETE CASCADE
//) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务绩效关联表';
@Mapper
public interface PerformanceMapper {
//    getAllPerformance
    @Select("select * from biz_performance ")
    List<BizPerformance> getAllPerformance();

    @Select("select * from biz_performance where perf_id = #{perfId}")
    BizPerformance getPerformanceById(Long perfId);


//    updatePerformance
    @Update(
            "update biz_performance set " +
                    "perf_code = #{perfCode}, " +
                    "perf_name = #{perfName}, " +
                    "target_value = #{targetValue}, " +
                    "current_value = #{currentValue}, " +
                    "data_type = #{dataType}, " +
                    "dept_id = #{deptId}, " +
                    "principal_id = #{principalId}, " +
                    "auditor_id = #{auditorId}, " +
                    "leader_id = #{leaderId}, " +
                    "is_delete = #{isDelete}, " +
                    "update_time = #{updateTime} " +
                    "where perf_id = #{perfId}"
    )
    void updatePerformance(BizPerformance bizPerformance);

//    获取该绩效相关的所有任务
    @Select("select * from rel_task_performance where perf_id = #{perfId}")
    List<RelTaskPerformance> getAllTaskPerformanceByPerfId(Long perfId);

//   根据任务id获取所有的相关绩效
    @Select("select * from rel_task_performance where task_id = #{taskId}")
    List<RelTaskPerformance> getAllPerformanceByTaskId(Long taskId);

//    根据taskId找到prefId
    @Select("select perf_id from rel_task_performance where task_id = #{taskId}")
    List<Long> getPerfIdByTaskId(Long taskId);

//    根据perfId找到taskId
    @Select("select task_id from rel_task_performance where perf_id = #{perfId}")
    List<Long> getTaskIdByPerfId(Long perfId);

}
```

---

### <a id='src\main\java\org\example\mapper\sysmapper-java'></a>src\main\java\org\example\mapper\SysMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import org.example.entity.*;

import java.util.List;

/**
 * 系统数据访问接口
 */
@Mapper
public interface SysMapper {
    /**
     * 获取所有用户
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user")
    public List<SysUser> getAllUsers();


    /**
     * 获取所有用户ID
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM sys_user")
    public List<Long> getAllUserIds();

    /**
     * 根据id获取部门
     * @param deptId 部门ID
     * @return 部门对象
     */
    @Select("SELECT * FROM sys_dept WHERE dept_id = #{deptId}")
    public SysDept getDeptById(Long deptId);

    /**
     * 根据userId获取部门
     * @param userId 用户ID
     * @return 部门对象
     */
    @Select("SELECT * FROM sys_dept WHERE dept_id = (SELECT dept_id FROM sys_user WHERE user_id = #{userId})")
    public SysDept getDeptByUserId(Long userId);

    /**
     * 根据userId获取部门负责人id
     * @param userId 用户ID
     * @return 负责人ID
     */
    @Select("SELECT leader_id FROM sys_dept WHERE dept_id = (SELECT dept_id FROM sys_user WHERE user_id = #{userId})")
    public Long getDeptLeaderId(Long userId);

    /**
     * 根据部门ID获取部门名称
     * @param deptId 部门ID
     * @return 部门名称
     */
    @Select("SELECT dept_name FROM sys_dept WHERE dept_id = #{deptId}")
    public String getDeptNameByDeptId(Long deptId);


//    获取所有的部门负责人ID
    @Select("SELECT leader_id FROM sys_dept")
    public List<Long> getAllDeptLeaders();

    /**
     * 根据id获取用户
     * @param userId 用户ID
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE user_id = #{userId}")
    public SysUser getUserById(Long userId);

    /**
     * 根据用户名获取用户
     * @param userName 用户名
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE user_name = #{userName}")
    public SysUser getUserByName(String userName);

    /**
     * 根据昵称获取用户
     * @param nickName 昵称
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE nick_name = #{nickName}")
    public SysUser getUserByNickName(String nickName);

    /**
     * 添加用户
     * userId手动添加而非自增
     * @param user 用户实体
     * @return 用户ID
     */
    @Insert("INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, email, password, role, status, is_delete, create_time, update_time) VALUES (#{userId}, #{deptId}, #{userName}, #{nickName}, #{email}, #{password}, #{role}, #{status}, #{isDelete}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    public void addUser(SysUser user);

    /**
     * 修改用户信息
     * @param user 用户实体
     */
    @Update("UPDATE sys_user SET dept_id = #{deptId}, user_name = #{userName}, nick_name = #{nickName}, email = #{email}, password = #{password}, role = #{role}, status = #{status}, update_time = #{updateTime} WHERE user_id = #{userId}")
    public void updateUser(SysUser user);

    /**
     * 删除用户
     * @param userId 用户ID
     */
    @Update("UPDATE sys_user SET is_delete = 1 WHERE user_id = #{userId}")
    public void deleteUser(Long userId);

    /**
     * 上传文件
     * @param file 文件实体
     * @return 文件ID
     */
    @Insert("INSERT INTO sys_file (file_name, file_path, file_url, file_suffix, file_size, upload_by, is_delete, upload_time) VALUES (#{fileName}, #{filePath}, #{fileUrl}, #{fileSuffix}, #{fileSize}, #{uploadBy}, #{isDelete}, #{uploadTime})")
    @Options(useGeneratedKeys = true, keyProperty = "fileId", keyColumn = "file_id")
    public void uploadFile(SysFile file);

    /**
     * 根据名称查询文件
     * @param fileName 文件名
     * @return 文件对象
     */
    @Select("SELECT * FROM sys_file WHERE file_name = #{fileName}")
    public SysFile getFileByName(String fileName);

    /**
     * 根据ID查询文件
     * @param fileId 文件ID
     * @return 文件对象
     */
    @Select("SELECT * FROM sys_file WHERE file_id = #{fileId}")
    public SysFile getFileById(Long fileId);

    /**
     * 发送通知（带返回通知ID）
     * @param notice 通知实体
     * @return 通知ID
     */
    @Insert("INSERT INTO sys_notice (from_user_id, to_user_id, type, trigger_event, title, content, source_type, source_id, is_read, is_delete, create_time) " +
            "VALUES (#{fromUserId}, #{toUserId}, #{type}, #{triggerEvent}, #{title}, #{content}, #{sourceType}, #{sourceId}, #{isRead}, #{isDelete}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "noticeId", keyColumn = "notice_id")
    void sendNotice(SysNotice notice);
    /**
     * 查看当前用户收到的信息
     * @param userId 用户ID
     * @return 通知列表
     */
    @Select("SELECT * FROM sys_notice WHERE to_user_id = #{userId}")
    public List<SysNotice> getNotices(Long userId);

    /**
     * 根据ID获取通知
     * @param noticeId 通知ID
     * @return 通知对象
     */
    @Select("SELECT * FROM sys_notice WHERE notice_id = #{noticeId}")
    public SysNotice getNoticeById(Long noticeId);

    /**
     * 设为已读
     * @param noticeId 通知ID
     */
    @Update("UPDATE sys_notice SET is_read = 1 WHERE notice_id = #{noticeId}")
    public void setRead(Long noticeId);
}
```

---

### <a id='src\main\java\org\example\mapper\tokenblacklistmapper-java'></a>src\main\java\org\example\mapper\TokenBlacklistMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.TokenBlacklist;

/**
 * Token黑名单数据访问接口
 */
@Mapper
public interface TokenBlacklistMapper {
    /**
     * 添加Token到黑名单
     * @param tokenBlacklist Token黑名单实体
     */
    @Insert("INSERT INTO token_blacklist(token, expiry_time) VALUES(#{token}, #{expiryTime})")
    void addToBlacklist(TokenBlacklist tokenBlacklist);

    /**
     * 根据Token查询黑名单
     * @param token Token字符串
     * @return Token黑名单实体
     */
    @Select("SELECT * FROM token_blacklist WHERE token = #{token}")
    TokenBlacklist findByToken(String token);

    /**
     * 清理过期的Token
     */
    @Select("DELETE FROM token_blacklist WHERE expiry_time < NOW()")
    void cleanupExpiredTokens();
}
```

---

### <a id='src\main\java\org\example\mapper\trenddatamapper-java'></a>src\main\java\org\example\mapper\TrendDataMapper.java

```java
package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.BizTrendData;
import java.util.List;

@Mapper
public interface TrendDataMapper {

    /**
     * 插入趋势数据
     */
    @Insert("INSERT INTO biz_trend_data (year, month, day, total_tasks, completion_count, completion_rate) " +
            "VALUES (#{year}, #{month}, #{day}, #{totalTasks}, #{completionCount}, #{completionRate})")
    void insertTrendData(BizTrendData trendData);

    /**
     * 根据年份获取所有数据
     */
    @Select("SELECT * FROM biz_trend_data WHERE year = #{year} AND is_delete = 0 " +
            "ORDER BY year DESC, month DESC, day DESC")
    List<BizTrendData> getTrendDataByYear(@Param("year") Integer year);

    /**
     * 获取最近一年的数据
     */
    @Select("SELECT * FROM biz_trend_data WHERE year = #{year} AND is_delete = 0 " +
            "ORDER BY month, day LIMIT 365")
    List<BizTrendData> getLatestTrendData(@Param("year") Integer year);

    /**
     * 检查今天是否已记录
     */
    @Select("SELECT COUNT(*) FROM biz_trend_data " +
            "WHERE year = YEAR(CURDATE()) " +
            "AND month = MONTH(CURDATE()) " +
            "AND day = DAY(CURDATE()) " +
            "AND is_delete = 0")
    Integer checkTodayRecorded();
}
```

---

### <a id='src\main\java\org\example\service\achievementservice-java'></a>src\main\java\org\example\service\AchievementService.java

```java
package org.example.service;

import org.example.entity.BizAchievement;
import org.example.mapper.AchievementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 标志性成果业务服务类
 * 处理标志性成果的基础增删查改业务逻辑
 */
@Service
public class AchievementService {

    /**
     * 注入成果Mapper接口，操作数据库
     */
    @Autowired
    private AchievementMapper achievementMapper;

    /**
     * 根据成果ID查询未删除的标志性成果
     * @param achId 成果ID
     * @return 标志性成果实体
     */
    public BizAchievement getAchievementById(Long achId) {
        // 参数非空校验
        if (achId == null) {
            throw new RuntimeException("成果ID不能为空");
        }
        // 查询成果
        BizAchievement achievement = achievementMapper.getAchievementById(achId);
        // 成果不存在校验
        if (achievement == null) {
            throw new RuntimeException("标志性成果不存在或已被删除");
        }
        return achievement;
    }

    /**
     * 查询所有未删除的标志性成果列表
     * @return 标志性成果实体列表
     */
    public List<BizAchievement> listAllAchievements() {
        try {
            return achievementMapper.listAllAchievements();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 新增标志性成果
     * 自动填充创建时间、默认删除状态，返回自增成果ID
     * @param achievement 标志性成果实体（含核心业务字段）
     * @return 自增的成果ID
     */
    public Long addAchievement(BizAchievement achievement,Long createBy) {
        // 核心参数非空校验
        if (achievement == null) {
            throw new RuntimeException("新增成果信息不能为空");
        }
        if (achievement.getCategory() == null) {
            throw new RuntimeException("成果类别不能为空");
        }
        if (achievement.getLevel() == null || achievement.getLevel().trim().isEmpty()) {
            throw new RuntimeException("成果级别不能为空");
        }
        if (achievement.getAchName() == null || achievement.getAchName().trim().isEmpty()) {
            throw new RuntimeException("成果名称不能为空");
        }
        if (achievement.getGotTime() == null) {
            throw new RuntimeException("成果颁发时间不能为空");
        }
        // 自动填充公共字段
        achievement.setIsDelete(0); // 默认未删除
        achievement.setCreateTime(new Date()); // 填充当前创建时间
        try {
            // 调用Mapper新增，返回自增ID
            achievementMapper.addAchievement(achievement);
            return achievementMapper.getLatestAchievement().getAchId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改标志性成果信息
     * 仅修改未删除的成果，根据成果ID更新
     * @param achievement 标志性成果实体（含成果ID和需修改字段）
     */
    public void updateAchievement(Long id,BizAchievement achievement) {
        // 核心参数非空校验
        if (achievement == null) {
            throw new RuntimeException("修改成果信息不能为空");
        }
        if (achievementMapper.getAchievementById(id) == null) {
            throw new RuntimeException("成果ID不存在，无法修改");
        }
        try {
            achievementMapper.updateAchievement(achievement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 逻辑删除标志性成果
     * 置is_delete=1，并非物理删除
     * @param achId 成果ID
     */
    public void deleteAchievement(Long achId) {
        // 参数非空校验
        if (achId == null) {
            throw new RuntimeException("成果ID不能为空");
        }
        // 校验成果是否存在
        if (achievementMapper.getAchievementById(achId) == null) {
            throw new RuntimeException("标志性成果不存在或已被删除，无法删除");
        }
        try {
            achievementMapper.deleteAchievement(achId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

---

### <a id='src\main\java\org\example\service\bizservice-java'></a>src\main\java\org\example\service\BizService.java

```java
package org.example.service;

import org.example.entity.*;
import org.example.entity.dto.BizAuditDTO;
import org.example.entity.dto.BizSubDTO;
import org.example.entity.dto.BizReSubDTO;
import org.example.entity.dto.BizTaskDTO;
import org.example.entity.vo.*;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 业务服务类：处理所有业务逻辑
 */
@Service
public class BizService {

    /** 管理员ID */
    private Long ADMIN_ID = 110228L;

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private SysMapper sysMapper;

    @Autowired
    private PerformanceService performanceService;

    /**
     * 获取全部任务
     * @return 任务列表
     */
    public List<BizTask> getAllTasks() {
        return bizMapper.getAllTasks();
    }

    /**
     * 根据id获取任务
     * @param taskId 任务ID
     * @return 任务对象
     */
    public BizTask getTaskById(Long taskId) {
        try {
            if (taskId == null) {
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            return bizMapper.getTaskById(taskId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取当前任务的所有子任务
     * @param taskId 任务ID
     * @return 子任务列表
     */
    public List<BizTask> getAllChildrenTasks(Long taskId) {
        try {
            if (taskId == null) {
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            return bizMapper.getAllChildrenTasks(taskId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取当前任务的父任务
     * @param taskId 任务ID
     * @return 父任务对象
     */
    public BizTask getParentTask(Long taskId) {
        try {
            if (taskId == null) {
                throw new RuntimeException("taskID为空");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            Long parentId = bizMapper.getTaskById(taskId).getParentId();
            return bizMapper.getTaskById(parentId);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据部门ID获取任务
     * @param deptId 部门ID
     * @return 任务列表
     */
    public List<BizTask> getTasksByDeptId(Long deptId) {
        try {
            return bizMapper.getTasksByDeptId(deptId);
        } catch (Exception e) {
            throw new RuntimeException("获取任务失败,请检查部门id是否正确");
        }
    }

    /**
     * 根据用户角色获取任务
     * 角色为0和2返回所有任务，角色为1返回leaderid以及为自己的任务
     * @param userId 用户ID
     * @return 任务视图对象列表
     */
    public List<BizTaskVo> getTasksByUserRole(Long userId) {
        try {
            SysUser sysUser = sysMapper.getUserById(userId);
            if (sysUser == null) {
                throw new RuntimeException("用户不存在");
            }

            if (sysUser.getRole().equals("0")) {
                return taskListToTaskVoList(bizMapper.getAllTasks());
            } else if (sysUser.getRole().equals("1")) {
                return taskListToTaskVoList(bizMapper.getTasks(sysUser.getUserId()));
            } else if (sysUser.getRole().equals("2")) {
                return taskListToTaskVoList(bizMapper.getTasksByPrincipalId(sysUser.getUserId()));
            } else {
                throw new RuntimeException("用户角色错误");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加任务
     * 只能添加三级任务,根据parent字段判断二级任务是否正确
     * @param taskDTO 任务数据
     */
    public void addTask(BizTaskDTO taskDTO) {
        try {
            // 只能添加三级任务,根据parent字段判断二级任务是否正确
            if (bizMapper.getTaskById(taskDTO.getParentId()) == null) {
                throw new RuntimeException("该二级任务不存在");
            }
            if (bizMapper.getTaskById(taskDTO.getParentId()).getLevel() != 2) {
                throw new RuntimeException("该任务不是二级任务,无法添加");
            }
            if (bizMapper.getTaskById(taskDTO.getParentId()).getDeptId() != taskDTO.getDeptId()) {
                throw new RuntimeException("该任务所属部门与二级任务部门不一致");
            }
            if(taskDTO.getProjectId()!=1){
                throw new RuntimeException("该任务所属项目id不为1");
            }

            BizTask task = taskDTO2Task(taskDTO);
            task.setIsDelete(0);
            task.setCreateTime(new Date());
            task.setUpdateTime(new Date());
            bizMapper.addTask(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新任务
     * @param taskDTO 任务数据
     */
    public void updateTask(BizTaskDTO taskDTO) {
        try {
            if (bizMapper.getTaskById(taskDTO.getTaskId()) == null) {
                throw new RuntimeException("该任务不存在");
            }
            BizTask task = taskDTO2Task(taskDTO);
            task.setUpdateTime(new Date());
            bizMapper.updateTask(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 完成任务
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public String finishTask(Long taskId, Long userId){
        try {
            if (userId == null) {
                throw new RuntimeException("用户ID为空");
            }
            SysUser sysUser = sysMapper.getUserById(userId);
            if (sysUser == null) {
                throw new RuntimeException("用户不存在");
            }
            if (!sysUser.getRole().equals("0")) {
                throw new RuntimeException("仅限管理员访问");
            }
            if (bizMapper.getTaskById(taskId) == null) {
                throw new RuntimeException("该任务不存在");
            }
            if (bizMapper.getTaskById(taskId).getStatus().equals("2")) {
                throw new RuntimeException("该任务正在审核中，请先完成审核");
            }
            if (bizMapper.getTaskById(taskId).getStatus().equals("3")) {
                throw new RuntimeException("该任务已完成，请勿重复提交");
            }
            BizTask taskById = bizMapper.getTaskById(taskId);
            taskById.setStatus("3");
            taskById.setCurrentValue(taskById.getTargetValue());
            bizMapper.updateTask(taskById);
            sendNotice(userId, taskById.getLeaderId(), "任务完成", "任务已完成", "任务"+taskById.getTaskName()+"已完成", "0", taskId);
            createAuditLog(taskId, userId, "任务完成", 1, 3, "任务完成");
            performanceService.calcuateAllPerformance();
            return "任务"+taskById.getTaskName()+"已完成";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 提交材料
     * @param bizSubDTO 提交数据
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public String submitMaterial(BizSubDTO bizSubDTO, Long userId) {
        try {
            // 检查taskid是否存在
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()) == null) {
                throw new RuntimeException("该任务不存在");
            }
            // 验证任务必须为三级任务，否则无法提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getLevel() != 3) {
                throw new RuntimeException("该任务不是三级任务,无法提交");
            }
            // 验证任务状态，如果当前status为2，则禁止提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getStatus().equals("2")) {
                throw new RuntimeException("该任务状态未开始或正在审核中,无法提交");
            }
            // 验证任务状态，如果当前status为3，则禁止提交
            if (bizMapper.getTaskById(bizSubDTO.getTask_id()).getStatus().equals("3")) {
                throw new RuntimeException("该任务状态已完成,无法提交");
            }
            // 检查文件是否存在
            SysFile sysFile = sysMapper.getFileById(bizSubDTO.getFile_id());
            if (sysFile == null) {
                throw new RuntimeException("该文件不存在");
            }
            // 验证文件后缀，只能为pdf,doc,docx
            if (!sysFile.getFileName().endsWith(".pdf") && !sysFile.getFileName().endsWith(".doc")
                    && !sysFile.getFileName().endsWith(".docx")) {
                throw new RuntimeException("文件格式错误,请上传pdf,doc,docx格式的文件");
            }

            BizTask task = bizMapper.getTaskById(bizSubDTO.getTask_id());
            BizMaterialSubmission bizMaterialSubmission = new BizMaterialSubmission();
            bizMaterialSubmission.setTaskId(bizSubDTO.getTask_id());
            bizMaterialSubmission.setFileId(sysMapper.getFileByName(sysFile.getFileName()).getFileId());

            // 本次填报值只保留整数，并写入任务 current_value（过程即显示进度）
            BigDecimal rv = bizSubDTO.getReported_value() != null ? bizSubDTO.getReported_value() : BigDecimal.ZERO;
            rv = rv.setScale(0, RoundingMode.HALF_UP);
            bizMaterialSubmission.setReportedValue(rv);
            bizMaterialSubmission.setDataType(bizSubDTO.getData_type());
            bizMaterialSubmission.setSubmitBy(userId);
            bizMaterialSubmission.setSubmitDeptId(sysMapper.getUserById(userId).getDeptId());
            bizMaterialSubmission.setManageDeptId(task.getDeptId());
            bizMaterialSubmission.setSubmitTime(new Date());
            bizMaterialSubmission.setFileSuffix(sysMapper.getFileByName(sysFile.getFileName()).getFileSuffix());
            bizMaterialSubmission.setFlowStatus(10);
            // 已修改，修改内容及原因：将部门审核人从任务的AuditorId改为提交人所在部门的负责人，确保flowStatus=10时审核人能正确收到通知
            // flowStatus = 10 表示"待[所在部门]审批"
            Long handlerId = task.getAuditorId();
            bizMaterialSubmission.setCurrentHandlerId(handlerId);
            bizMaterialSubmission.setIsDelete(0);

            bizMapper.createAudit(bizMaterialSubmission);
            Long subId = bizMapper.getNewestSubId();

            // 提交后任务进入"审核中"，并把 current_value 覆盖写为本次填报值
            BizTask bizTask = bizMapper.getTaskById(bizSubDTO.getTask_id());
            if (bizTask != null) {
                bizTask.setCurrentValue(rv);
                bizTask.setStatus("2");
                bizTask.setComment(bizSubDTO.getComment());
                bizTask.setUpdateTime(new Date());
                bizMapper.updateTask(bizTask);
            }

            // 发送审批信息（使用封装方法）
            // 已修改，修改内容及原因：添加null检查，避免handlerId为null时发送通知导致数据库约束错误
            // 只有当 handlerId 不为 null 时才发送通知
            if (handlerId != null) {
                sendNotice(userId,
                        handlerId,
                        "任务审核",
                        "任务审核",
                        "您有新的任务需要审核",
                        "0",
                        bizSubDTO.getTask_id());
            }

            // 创建审批日志（使用封装方法）
            createAuditLog(subId, userId, "提交", 0, 10, "提交任务");
            performanceService.updatePerformanceByTaskId(task.getTaskId());
            // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
            String resultMsg = "提交成功";
            if (handlerId != null) {
                try {
                    SysUser handler = sysMapper.getUserById(handlerId);
                    if (handler != null) {
                        resultMsg += "，下一位审批人是" + handler.getUserName();
                    } else {
                        resultMsg += "，下一位审批人ID为" + handlerId;
                    }
                } catch (Exception e) {
                    resultMsg += "，下一位审批人ID为" + handlerId;
                }
            }
            return resultMsg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据taskId查询审批单
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> getAudit(Long taskId, Long userId) {
        try {
            return auditListToAuditVoList(bizMapper.getAudit(taskId, userId));
        } catch (RuntimeException e) {
            throw new RuntimeException("获取审批单失败,请检查任务id是否正确");
        }
    }

    /**
     * 获取"待我审批"的审批单（按 current_handler_id 查询）
     * @param userId 用户ID
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> getTodoAudits(Long userId) {
        try {
            if (userId == null)
                throw new RuntimeException("userId为空");
            return auditListToAuditVoList(bizMapper.getTodoAudits(userId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据 taskId 查询该任务全部审批单（用于任务详情抽屉展示完整流程）
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> getAuditByTaskId(Long taskId, Long userId) {
        try {
            if (taskId == null)
                throw new RuntimeException("taskId为空");
            BizTask task = bizMapper.getTaskById(taskId);
            if (task == null)
                throw new RuntimeException("该任务不存在");

            SysUser me = sysMapper.getUserById(userId);
            if (me == null)
                throw new RuntimeException("用户不存在");

            // 最小权限校验：管理员/任务负责人/归口负责人可查看；或本人提交过该任务审批单也可查看
            if ("0".equals(me.getRole())
                    || (task.getLeaderId() != null && task.getLeaderId().equals(userId))
                    || (task.getAuditorId() != null && task.getAuditorId().equals(userId))
                    || (task.getPrincipalId() != null && task.getPrincipalId().equals(userId))) {
                return auditListToAuditVoList(bizMapper.getAuditsByTaskId(taskId));
            }

            List<BizAuditVO> list = auditListToAuditVoList(bizMapper.getAuditsByTaskId(taskId));
            boolean submittedByMe = false;
            for (BizAuditVO s : list) {
                if (s != null && s.getSubmitBy() != null && s.getSubmitBy().equals(userId)) {
                    submittedByMe = true;
                    break;
                }
            }
            if (!submittedByMe) {
                throw new RuntimeException("无权限查看该任务的审批记录");
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据 subId 查询审批操作日志（biz_audit_log）
     * @param subId 提交ID
     * @param userId 用户ID
     * @return 审批日志列表
     */
    public List<BizAuditLog> getAuditLogs(Long subId, Long userId) {
        try {
            if (subId == null)
                throw new RuntimeException("subId为空");
            BizMaterialSubmission submission = bizMapper.getAuditBySubId(subId);
            if (submission == null)
                throw new RuntimeException("审批单不存在");

            BizTask task = bizMapper.getTaskById(submission.getTaskId());
            if (task == null)
                throw new RuntimeException("任务不存在");

            SysUser me = sysMapper.getUserById(userId);
            if (me == null)
                throw new RuntimeException("用户不存在");

            // 最小权限校验：管理员/任务部门负责人/归口负责人/提交人可查看
            boolean allowed = "0".equals(me.getRole())
                    || (task.getLeaderId() != null && task.getLeaderId().equals(userId))
                    || (task.getAuditorId() != null && task.getAuditorId().equals(userId))
                    || (task.getPrincipalId() != null && task.getPrincipalId().equals(userId))
                    || (submission.getSubmitBy() != null && submission.getSubmitBy().equals(userId));
            if (!allowed) {
                throw new RuntimeException("无权限查看该审批单的操作日志");
            }

            return bizMapper.getAuditLogsBySubId(subId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 审批任务
     * @param bizAuditDTO 审批数据
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public Object audit(BizAuditDTO bizAuditDTO, Long userId) {
        Long subId = bizAuditDTO.getSub_id();
        try {
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(subId);
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }

            // 已修改，修改内容及原因：添加currentHandlerId的null检查，避免在equals比较时出现空指针异常
            // 检查 currentHandlerId 是否为 null
            Long currentHandlerId = bizMaterialSubmission.getCurrentHandlerId();
            if (currentHandlerId == null) {
                throw new RuntimeException("审批单的当前处理人未设置，无法进行审核");
            }
            // 安全获取任务的 AuditorId
            BizTask task = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
            if(task==null){
                throw new RuntimeException("任务不存在");
            }
            Long taskAuditorId = (task != null) ? task.getAuditorId() : null;

            Map<Integer, Long> nextHandlerMap = Map.of(
                    10, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    20, ADMIN_ID);

            // 已修改，修改内容及原因：安全获取任务的AuditorId，避免getTaskById返回null时出现空指针异常


            Map<Integer, Long> backHandlerMap = Map.of(
                    10, bizMaterialSubmission.getSubmitBy(),
                    20, taskAuditorId != null ? taskAuditorId : bizMaterialSubmission.getSubmitBy(),
                    30, bizMapper.getTaskPrincipalId(bizMaterialSubmission.getTaskId()),
                    -20, bizMaterialSubmission.getSubmitBy(),
                    -30, taskAuditorId != null ? taskAuditorId : bizMaterialSubmission.getSubmitBy());

            // 分支1：当前用户是处理人
            if (currentHandlerId.equals(userId)) {
                if (bizMaterialSubmission.getFlowStatus() == 10) {
                    // 部门负责人审核逻辑
                    if (bizAuditDTO.getIs_pass()) {
                        Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, nextHandlerId, 20);

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                nextHandlerId,
                                "任务审核",
                                "任务审核",
                                "您有新的任务需要审核",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "通过", 10, 20, bizAuditDTO.getTitle());

                        System.out.println("已审批，下一位审批人id为" + nextHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已审批";
                        if (nextHandlerId != null) {
                            try {
                                SysUser nextUser = sysMapper.getUserById(nextHandlerId);
                                if (nextUser != null) {
                                    resultMsg += "，下一位审批人为" + nextUser.getUserName();
                                } else {
                                    resultMsg += "，下一位审批人ID为" + nextHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，下一位审批人ID为" + nextHandlerId;
                            }
                        }
                        return resultMsg;
                    } else {
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -10);

                        task.setStatus("1");
                // 正确写法：使用 BigDecimal 的 subtract() 方法做减法
                        BigDecimal currentValue = task.getCurrentValue();
                        BigDecimal reportedValue = bizMaterialSubmission.getReportedValue();
                // 注意：需处理 null 避免空指针异常（推荐）
                        if (currentValue == null) {
                            currentValue = BigDecimal.ZERO;
                        }
                        if (reportedValue == null) {
                            reportedValue = BigDecimal.ZERO;
                        }
                        task.setCurrentValue(currentValue.subtract(reportedValue));                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        bizMapper.updateTask(task);

                        performanceService.updatePerformanceByTaskId(task.getTaskId());

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 10, -10, bizAuditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == 20) {
                    if (bizAuditDTO.getIs_pass()) {
                        Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                        ;// 管理员

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, nextHandlerId, 30);

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                nextHandlerId,
                                "任务审核",
                                "任务审核",
                                "您有新的任务需要审核",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "通过", 20, 30, bizAuditDTO.getTitle());

                        System.out.println("已审批，下一位审批人id为" + nextHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已审批";
                        if (nextHandlerId != null) {
                            try {
                                SysUser nextUser = sysMapper.getUserById(nextHandlerId);
                                if (nextUser != null) {
                                    resultMsg += "，下一位审批人为" + nextUser.getUserName();
                                } else {
                                    resultMsg += "，下一位审批人ID为" + nextHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，下一位审批人ID为" + nextHandlerId;
                            }
                        }
                        return resultMsg;
                    } else {
                        // 退回到提交人的部门负责人
                        // 根据提交人id获取部门负责人id
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -20);
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        task.setStatus("1");
                        // 正确写法：使用 BigDecimal 的 subtract() 方法做减法
                        BigDecimal currentValue = task.getCurrentValue();
                        BigDecimal reportedValue = bizMaterialSubmission.getReportedValue();
                        // 注意：需处理 null 避免空指针异常（推荐）
                        if (currentValue == null) {
                            currentValue = BigDecimal.ZERO;
                        }
                        if (reportedValue == null) {
                            reportedValue = BigDecimal.ZERO;
                        }
                        task.setCurrentValue(currentValue.subtract(reportedValue));                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        bizMapper.updateTask(task);
                        performanceService.updatePerformanceByTaskId(task.getTaskId());

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 20, -20, bizAuditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == 30) {
                    if (bizAuditDTO.getIs_pass()) {

                        // 更新审批单状态（使用封装方法）
                        bizMaterialSubmission.setFlowStatus(40);
                        bizMapper.updateAudit(bizMaterialSubmission);
                        // 更新任务状态
                        BizTask bizTask = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
                        // 口径调整：任务 current_value 以"本次填报值"为准（覆盖写），不再在最终通过时重复累加
                        BigDecimal rv = bizMaterialSubmission.getReportedValue() != null ? bizMaterialSubmission.getReportedValue() : BigDecimal.ZERO;
                        rv = rv.setScale(0, RoundingMode.HALF_UP);
                        bizTask.setCurrentValue(rv);
                        if (bizTask.getCurrentValue().compareTo(bizTask.getTargetValue()) >= 0) {
                            bizTask.setStatus("3");
                        } else {
                            bizTask.setStatus("1");
                        }
                        bizMapper.updateCurrentTask(bizTask);

                        // 发送通知，告知提交人审批过程已完成
                        sendNotice(userId,
                                bizMaterialSubmission.getSubmitBy(),
                                "任务审核完成",
                                "审核完成",
                                "您提交的审核任务已完成",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "通过", 30, 40, bizAuditDTO.getTitle());

                        System.out.println("审批完成");
                        return "审批完成";
                    } else {
                        // 退回到归口负责人
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());

                        // 更新审批单状态（使用封装方法）
                        updateAuditStatus(subId, backHandlerId, -30);
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        task.setStatus("1");
                        // 正确写法：使用 BigDecimal 的 subtract() 方法做减法
                        BigDecimal currentValue = task.getCurrentValue();
                        BigDecimal reportedValue = bizMaterialSubmission.getReportedValue();
                        // 注意：需处理 null 避免空指针异常（推荐）
                        if (currentValue == null) {
                            currentValue = BigDecimal.ZERO;
                        }
                        if (reportedValue == null) {
                            reportedValue = BigDecimal.ZERO;
                        }
                        task.setCurrentValue(currentValue.subtract(reportedValue));                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        bizMapper.updateTask(task);
                        performanceService.updatePerformanceByTaskId(task.getTaskId());

                        // 发送通知（使用封装方法）
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());

                        // 创建审批日志（使用封装方法）
                        createAuditLog(subId, userId, "退回", 30, -30, bizAuditDTO.getTitle());

                        System.out.println("已退回，退回到id为" + backHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == -20) {
                    if (bizAuditDTO.getIs_pass()) {
                        throw new RuntimeException("请重新提交材料");
                    } else {
                        Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                        updateAuditStatus(subId, backHandlerId, -10);
                        // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                        bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");
                        sendNotice(userId,
                                backHandlerId,
                                "任务退回",
                                "任务退回",
                                "您提交的材料被退回",
                                "0",
                                bizMaterialSubmission.getTaskId());
                        createAuditLog(subId, userId, "退回", -20, -10, bizAuditDTO.getTitle());
                        System.out.println("已退回，退回到id为" + backHandlerId);
                        // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
                        String resultMsg = "已退回";
                        if (backHandlerId != null) {
                            try {
                                SysUser backUser = sysMapper.getUserById(backHandlerId);
                                if (backUser != null) {
                                    resultMsg += "，退回到" + backUser.getUserName();
                                } else {
                                    resultMsg += "，退回到ID为" + backHandlerId;
                                }
                            } catch (Exception e) {
                                resultMsg += "，退回到ID为" + backHandlerId;
                            }
                        }
                        return resultMsg;
                    }
                } else if (bizMaterialSubmission.getFlowStatus() == -30) {
                    if (bizAuditDTO.getIs_pass()) {
                        throw new RuntimeException("请重新提交材料");
                    }
                    Long backHandlerId = backHandlerMap.get(bizMaterialSubmission.getFlowStatus());
                    updateAuditStatus(subId, backHandlerId, -20);
                    // 退回后任务不应保持"审核中"，恢复为"进行中"，允许重新提交
                    bizMapper.updateTaskStatus(bizMaterialSubmission.getTaskId(), "1");
                    sendNotice(userId,
                            backHandlerId,
                            "任务退回",
                            "任务退回",
                            "您提交的材料被退回",
                            "0",
                            bizMaterialSubmission.getTaskId());
                    createAuditLog(subId, userId, "退回", -30, -20, bizAuditDTO.getTitle());
                    System.out.println("已退回，退回到id为" + backHandlerId);
                    return "已退回，退回到" + sysMapper.getUserById(backHandlerId).getUserName();
                } else {// 补充：flowStatus不在枚举值范围内的返回值
                    throw new RuntimeException("不支持的审批流程状态：" + bizMaterialSubmission.getFlowStatus());
                }
            } else {
                // 分支2：当前用户不是处理人
                throw new RuntimeException("您不是该任务的当前审批人，无法操作");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 退回重新提交材料
     * @param resubDTOBiz 重新提交数据
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public String reSubmitMaterial(BizReSubDTO resubDTOBiz, Long userId) {
        try {
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(resubDTOBiz.getSub_id());
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }
            BizTask task = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
            if(task==null){
                throw new RuntimeException("该任务不存在");
            }
            Map<Integer, Long> nextHandlerMap = Map.of(
                    -10, task.getAuditorId(),
                    -20, task.getPrincipalId(),
                    -30, ADMIN_ID);

            Long nextHandlerId = nextHandlerMap.get(bizMaterialSubmission.getFlowStatus());
            if (bizMaterialSubmission.getFlowStatus() >= 0) {
                throw new RuntimeException("该任务状态不是退回状态,无法重新提交");
            }
            // 重新提交同样只保留整数，并覆盖写任务 current_value
            BigDecimal rv = resubDTOBiz.getReported_value() != null ? resubDTOBiz.getReported_value() : BigDecimal.ZERO;
            rv = rv.setScale(0, RoundingMode.HALF_UP);
            bizMaterialSubmission.setReportedValue(rv);
            bizMaterialSubmission.setDataType(resubDTOBiz.getData_type());
            bizMaterialSubmission.setFileId(resubDTOBiz.getFile_id());
            bizMaterialSubmission.setSubmitTime(new Date());
            bizMaterialSubmission.setFlowStatus(-bizMaterialSubmission.getFlowStatus());
            bizMaterialSubmission.setCurrentHandlerId(nextHandlerId);
            bizMapper.updateAudit(bizMaterialSubmission);

            // 重新提交后恢复任务状态为"审核中"，并覆盖写 current_value
            BizTask bizTask = bizMapper.getTaskById(bizMaterialSubmission.getTaskId());
            if (bizTask != null) {
                bizTask.setCurrentValue(rv);
                bizTask.setStatus("2");
                bizTask.setUpdateTime(new Date());
                bizTask.setComment(resubDTOBiz.getComment());
                bizMapper.updateCurrentTask(bizTask);
            }

            // 已修改，修改内容及原因：添加null检查，避免nextHandlerId为null时发送通知导致数据库约束错误
            // 只有当 nextHandlerId 不为 null 时才发送通知
            if (nextHandlerId != null) {
                sendNotice(userId,
                        nextHandlerId,
                        "任务审核",
                        "任务审核",
                        "您有新的任务需要审核",
                        "0",
                        bizMaterialSubmission.getTaskId());
            }

            createAuditLog(resubDTOBiz.getSub_id(), userId, "重新提交", -bizMaterialSubmission.getFlowStatus(),
                    bizMaterialSubmission.getFlowStatus(), "重新提交");
            // 已修改，修改内容及原因：添加null检查和异常处理，避免getUserById返回null时出现空指针异常
            String resultMsg = "已重新提交";
            if (nextHandlerId != null) {
                try {
                    SysUser handler = sysMapper.getUserById(nextHandlerId);
                    if (handler != null) {
                        resultMsg += ",审核人为" + handler.getUserName();
                    }
                } catch (Exception e) {
                    // 忽略获取用户信息失败的情况
                }
            }
            return resultMsg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 撤回提交申请
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 操作结果
     */
    public Object drawbackSubmit(Long taskId, Long userId){
        try{
            BizMaterialSubmission bizMaterialSubmission = bizMapper.getNewestAudit(taskId);
            if (bizMaterialSubmission == null) {
                throw new RuntimeException("该任务不存在");
            }
            if (!bizMaterialSubmission.getSubmitBy().equals(userId)){
                throw new RuntimeException("您不是该任务的提交人，无法撤回");
            }
            bizMaterialSubmission.setIsDelete(1);
            bizMapper.updateAudit(bizMaterialSubmission);
            createAuditLog(bizMaterialSubmission.getSubId(), userId, "撤回提交", bizMaterialSubmission.getFlowStatus(), -bizMaterialSubmission.getFlowStatus(), "撤回提交");
            return "已撤回提交";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取该任务上一次审批通过的文件
     * @param taskId 任务ID
     * @return 文件上传视图对象
     */
    public FileUploadVO getLastCycleFiles(Long taskId) {
        try{
            BizTask task = bizMapper.getTaskById(taskId);
            if (task == null) {
                throw new RuntimeException("任务不存在");
            }
            BizMaterialSubmission submission = bizMapper.getLatestApprovedAuditByTaskId(taskId);
            if (submission == null) {
                return null;
            }
            FileUploadVO fileUploadVo = new FileUploadVO();
            fileUploadVo.setFileId(submission.getFileId());
            fileUploadVo.setFilename(sysMapper.getFileById(submission.getFileId()).getFileName());
            fileUploadVo.setFilepath(sysMapper.getFileById(submission.getFileId()).getFileUrl());
            return fileUploadVo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据父任务ID获取三级任务
     * @param parentId 父任务ID
     * @return 任务列表
     */
    public List<BizTask> getThirdLevelTasksByParentId(Long parentId) {
        try {
            return bizMapper.getThirdLevelTasksByParentId(parentId);
        } catch (RuntimeException e) {
            throw new RuntimeException("获取任务失败,请检查任务id是否正确");
        }
    }

    /**
     * 根据归口负责人ID获取任务
     * @param principalId 归口负责人ID
     * @return 任务列表
     */
    public List<BizTask> getTasksByPrincipalId(Long principalId) {
        try {
            return bizMapper.getTasksByPrincipalId(principalId);
        } catch (Exception e) {
            throw new RuntimeException("获取任务失败,请检查负责人id是否正确");
        }
    }

    /**
     * 封装方法：发送系统通知
     * @param fromUserId 发送人ID
     * @param toUserId 接收人ID
     * @param triggerEvent 触发事件
     * @param title 标题
     * @param content 内容
     * @param sourceType 来源类型
     * @param sourceId 来源ID
     */
    private void sendNotice(Long fromUserId, Long toUserId, String triggerEvent,
                            String title, String content, String sourceType, Long sourceId) {
        // 已修改，修改内容及原因：添加toUserId的null检查，避免toUserId为null时插入数据库导致约束错误
        // 如果 toUserId 为 null，不发送通知，避免数据库约束错误
        if (toUserId == null) {
            System.out.println("警告：无法发送通知，接收人ID为空。标题：" + title);
            return;
        }
        SysNotice sysNotice = new SysNotice();
        sysNotice.setFromUserId(fromUserId);
        sysNotice.setToUserId(toUserId);
        sysNotice.setTriggerEvent(triggerEvent);
        sysNotice.setTitle(title);
        sysNotice.setContent(content);
        sysNotice.setSourceType(sourceType);
        sysNotice.setSourceId(sourceId);
        sysNotice.setIsRead("0");

        sysMapper.sendNotice(sysNotice);
        System.out.println("id=" + toUserId + "的用户收到一条通知：" + title);
    }

    /**
     * 封装方法：创建审批日志
     * @param subId 提交ID
     * @param operatorId 操作人ID
     * @param actionType 动作类型
     * @param preStatus 前状态
     * @param postStatus 后状态
     * @param comment 意见
     */
    private void createAuditLog(Long subId, Long operatorId, String actionType,
                                Integer preStatus, Integer postStatus, String comment) {
        BizAuditLog bizAuditLog = new BizAuditLog();
        bizAuditLog.setLogId(null);
        bizAuditLog.setSubId(subId);
        bizAuditLog.setOperatorId(operatorId);
        bizAuditLog.setActionType(actionType);
        bizAuditLog.setPreStatus(preStatus);
        bizAuditLog.setPostStatus(postStatus);
        bizAuditLog.setComment(comment);
        bizAuditLog.setCreateTime(new Date());

        bizMapper.createAuditLog(bizAuditLog);
    }

    /**
     * 封装方法：更新审批单状态和处理人
     * @param subId 提交ID
     * @param currentHandlerId 当前处理人ID
     * @param flowStatus 流程状态
     */
    private void updateAuditStatus(Long subId, Long currentHandlerId, Integer flowStatus) {
        BizMaterialSubmission bizMaterialSubmission = bizMapper.getAuditBySubId(subId);
        if (bizMaterialSubmission != null) {
            bizMaterialSubmission.setCurrentHandlerId(currentHandlerId);
            bizMaterialSubmission.setFlowStatus(flowStatus);
            bizMapper.updateAudit(bizMaterialSubmission);
        }
    }

    /**
     * TaskToTaskVo转换方法
     * @param task 任务实体
     * @return 任务视图对象
     */
    public BizTaskVo taskToTaskVo(BizTask task) {
        BizTaskVo taskVo = new BizTaskVo();
        taskVo.setTaskId(task.getTaskId());
        taskVo.setProjectId(task.getProjectId());
        taskVo.setParentId(task.getParentId());
        taskVo.setAncestors(task.getAncestors());
        taskVo.setPhase(task.getPhase());
        taskVo.setTaskCode(task.getTaskCode());
        taskVo.setTaskName(task.getTaskName());
        taskVo.setLevel(task.getLevel());
        taskVo.setComment(task.getComment());
        taskVo.setDeptId(task.getDeptId());
        taskVo.setDeptName(sysMapper.getDeptById(task.getDeptId()).getDeptName());
        taskVo.setPrincipalId(task.getPrincipalId());
        taskVo.setPrincipalName(sysMapper.getUserById(task.getPrincipalId()).getUserName());
        // 避免空指针错误
        if(task.getAuditorId()!=null){
            taskVo.setAuditorId(task.getAuditorId());
            taskVo.setAuditorName(sysMapper.getUserById(task.getAuditorId()).getUserName());
        }else {
            taskVo.setAuditorId(null);
            taskVo.setAuditorName("无");
        }
        taskVo.setLeaderId(task.getLeaderId());
        taskVo.setLeaderName(sysMapper.getUserById(task.getLeaderId()).getUserName());
        taskVo.setExpTarget(task.getExpTarget());
        taskVo.setExpLevel(task.getExpLevel());
        taskVo.setExpEffect(task.getExpEffect());
        taskVo.setExpMaterialDesc(task.getExpMaterialDesc());
        taskVo.setDataType(task.getDataType());
        taskVo.setTargetValue(task.getTargetValue());
        taskVo.setCurrentValue(task.getCurrentValue());
        taskVo.setWeight(task.getWeight());
        taskVo.setProgress(task.getProgress());
        taskVo.setStatus(task.getStatus());
        taskVo.setIsDelete(task.getIsDelete());
        taskVo.setCreateTime(task.getCreateTime());
        taskVo.setUpdateTime(task.getUpdateTime());
        return taskVo;
    }

    /**
     * TaskListToTaskVoList转换方法
     * @param tasks 任务实体列表
     * @return 任务视图对象列表
     */
    public List<BizTaskVo> taskListToTaskVoList(List<BizTask> tasks) {
        List<BizTaskVo> taskVos = new ArrayList<>();
        for (BizTask task : tasks) {
            taskVos.add(taskToTaskVo(task));
        }
        return taskVos;
    }

    /**
     * TaskDTO转Task转换方法
     * @param taskDTO 任务数据传输对象
     * @return 任务实体
     */
    public BizTask taskDTO2Task(BizTaskDTO taskDTO) {
        BizTask task = new BizTask();
        task.setTaskId(taskDTO.getTaskId());
        task.setProjectId(taskDTO.getProjectId());
        task.setParentId(taskDTO.getParentId());
        task.setAncestors(taskDTO.getAncestors());
        task.setPhase(taskDTO.getPhase());
        task.setTaskCode(taskDTO.getTaskCode());
        task.setTaskName(taskDTO.getTaskName());
        task.setLevel(taskDTO.getLevel());
        task.setComment(taskDTO.getComment());
        task.setDeptId(taskDTO.getDeptId());
        task.setAuditorId(taskDTO.getAuditorId());
        task.setPrincipalId(taskDTO.getPrincipalId());
        task.setLeaderId(taskDTO.getLeaderId());
        task.setExpTarget(taskDTO.getExpTarget());
        task.setExpLevel(taskDTO.getExpLevel());
        task.setExpEffect(taskDTO.getExpEffect());
        task.setExpMaterialDesc(taskDTO.getExpMaterialDesc());
        task.setDataType(taskDTO.getDataType());
        task.setTargetValue(taskDTO.getTargetValue());
        task.setCurrentValue(taskDTO.getCurrentValue());
        task.setWeight(taskDTO.getWeight());
        task.setProgress(taskDTO.getProgress());
        task.setStatus(taskDTO.getStatus());
        return task;
    }

    /**
     * Audit转AuditVO转换方法
     * @param audit 材料提交实体
     * @return 审批视图对象
     */
    public BizAuditVO auditToAuditVo(BizMaterialSubmission audit){
        BizAuditVO bizAuditVO = new BizAuditVO();
        bizAuditVO.setSubId(audit.getSubId());
        bizAuditVO.setTaskId(audit.getTaskId());
        bizAuditVO.setFileId(audit.getFileId());
        bizAuditVO.setFilename(sysMapper.getFileById(audit.getFileId()).getFileName());
        bizAuditVO.setReportedValue(audit.getReportedValue());
        bizAuditVO.setDataType(audit.getDataType());
        bizAuditVO.setSubmitBy(audit.getSubmitBy());
        bizAuditVO.setSubmitDeptId(audit.getSubmitDeptId());
        bizAuditVO.setManageDeptId(audit.getManageDeptId());
        bizAuditVO.setSubmitTime(audit.getSubmitTime());
        bizAuditVO.setFileSuffix(audit.getFileSuffix());
        bizAuditVO.setFlowStatus(audit.getFlowStatus());
        bizAuditVO.setCurrentHandlerId(audit.getCurrentHandlerId());
        bizAuditVO.setIsDelete(audit.getIsDelete());
        return bizAuditVO;
    }

    /**
     * AuditList转AuditVoList转换方法
     * @param audits 材料提交实体列表
     * @return 审批视图对象列表
     */
    public List<BizAuditVO> auditListToAuditVoList(List<BizMaterialSubmission> audits){
        List<BizAuditVO> auditVos = new ArrayList<>();
        for (BizMaterialSubmission audit : audits) {
            auditVos.add(auditToAuditVo(audit));
        }
        return auditVos;
    }

    // 中期截止年份
    private static final Integer MID_TERM_END_YEAR = 2028;

    /**
     * 获取看板数据汇总
     * @return 看板数据汇总
     */
    public DashboardSummaryVO getDashboardSummary() {
        DashboardSummaryVO summary = new DashboardSummaryVO();

        try {
            // 设置统计时间
            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);
            summary.setCurrentYear(String.valueOf(currentYear));
            summary.setMidTermEndYear(String.valueOf(MID_TERM_END_YEAR));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            summary.setUpdateTime(sdf.format(new Date()));

            // 1. 计算整体数据
            calculateOverallData(summary, currentYear);

            // 2. 计算部门数据
            calculateDeptData(summary, currentYear);

            // 3. 获取一级任务详情
            List<FirstLevelTaskDetailVO> firstLevelTasks = bizMapper.getFirstLevelTaskDetails();
            summary.setFirstLevelTasks(firstLevelTasks);

        } catch (Exception e) {
            throw new RuntimeException("获取看板数据失败: " + e.getMessage());
        }

        return summary;
    }

    /**
     * 计算整体数据（在Service中查询各状态数量）
     */
    private void calculateOverallData(DashboardSummaryVO summary, int currentYear) {
        // 1.1 所有任务完成率（在Service中查询各状态数量）
        Integer totalTasks = bizMapper.getTotalTaskCount();
        Integer completedTasks = bizMapper.getCompletedTaskCount();

        // 在Service中查询各状态数量
        Integer notStartedCount = bizMapper.getTaskCountByStatus("0");
        Integer inProgressCount = bizMapper.getTaskCountByStatus("1");
        Integer inReviewCount = bizMapper.getTaskCountByStatus("2");
        Integer finishedCount = bizMapper.getTaskCountByStatus("3");

        summary.setOverallCompletion(new TaskCompletionVO(
                totalTasks, completedTasks, "all", "所有任务完成率",
                notStartedCount != null ? notStartedCount : 0,
                inProgressCount != null ? inProgressCount : 0,
                inReviewCount != null ? inReviewCount : 0,
                finishedCount != null ? finishedCount : 0
        ));

        // 1.2 本年度任务完成率（在Service中查询各状态数量）
        Integer yearTotalTasks = bizMapper.getYearTaskCount(currentYear);
        Integer yearCompletedTasks = bizMapper.getYearCompletedTaskCount(currentYear);

        // 在Service中查询本年度各状态数量
        Integer yearNotStartedCount = bizMapper.getYearTaskCountByStatus(currentYear, "0");
        Integer yearInProgressCount = bizMapper.getYearTaskCountByStatus(currentYear, "1");
        Integer yearInReviewCount = bizMapper.getYearTaskCountByStatus(currentYear, "2");
        Integer yearFinishedCount = bizMapper.getYearTaskCountByStatus(currentYear, "3");

        summary.setYearCompletion(new TaskCompletionVO(
                yearTotalTasks, yearCompletedTasks, "year", currentYear + "年度任务完成率",
                yearNotStartedCount != null ? yearNotStartedCount : 0,
                yearInProgressCount != null ? yearInProgressCount : 0,
                yearInReviewCount != null ? yearInReviewCount : 0,
                yearFinishedCount != null ? yearFinishedCount : 0
        ));

        // 1.3 中期任务完成率（在Service中查询各状态数量）
        Integer midTermTotalTasks = bizMapper.getMidTermTaskCount(MID_TERM_END_YEAR);
        Integer midTermCompletedTasks = bizMapper.getMidTermCompletedTaskCount(MID_TERM_END_YEAR);

        // 在Service中查询中期各状态数量
        Integer midTermNotStartedCount = bizMapper.getMidTermTaskCountByStatus(MID_TERM_END_YEAR, "0");
        Integer midTermInProgressCount = bizMapper.getMidTermTaskCountByStatus(MID_TERM_END_YEAR, "1");
        Integer midTermInReviewCount = bizMapper.getMidTermTaskCountByStatus(MID_TERM_END_YEAR, "2");
        Integer midTermFinishedCount = bizMapper.getMidTermTaskCountByStatus(MID_TERM_END_YEAR, "3");

        summary.setMidTermCompletion(new TaskCompletionVO(
                midTermTotalTasks, midTermCompletedTasks, "midterm", "中期（" + MID_TERM_END_YEAR + "年前）任务完成率",
                midTermNotStartedCount != null ? midTermNotStartedCount : 0,
                midTermInProgressCount != null ? midTermInProgressCount : 0,
                midTermInReviewCount != null ? midTermInReviewCount : 0,
                midTermFinishedCount != null ? midTermFinishedCount : 0
        ));

        // 1.4 一级任务完成率（在Service中查询各状态数量）
        Integer firstLevelTotalTasks = bizMapper.getFirstLevelTaskCount();
        Integer firstLevelCompletedTasks = bizMapper.getFirstLevelCompletedTaskCount();

        // 在Service中查询一级各状态数量
        Integer firstLevelNotStartedCount = bizMapper.getFirstLevelTaskCountByStatus("0");
        Integer firstLevelInProgressCount = bizMapper.getFirstLevelTaskCountByStatus("1");
        Integer firstLevelInReviewCount = bizMapper.getFirstLevelTaskCountByStatus("2");
        Integer firstLevelFinishedCount = bizMapper.getFirstLevelTaskCountByStatus("3");

        summary.setFirstLevelCompletion(new TaskCompletionVO(
                firstLevelTotalTasks, firstLevelCompletedTasks, "firstLevel", "一级任务完成率",
                firstLevelNotStartedCount != null ? firstLevelNotStartedCount : 0,
                firstLevelInProgressCount != null ? firstLevelInProgressCount : 0,
                firstLevelInReviewCount != null ? firstLevelInReviewCount : 0,
                firstLevelFinishedCount != null ? firstLevelFinishedCount : 0
        ));
    }

    /**
     * 计算部门数据（在Service中补充状态统计）
     */
    private void calculateDeptData(DashboardSummaryVO summary, int currentYear) {
        // 2.1 各部门整体完成率（在Service中补充状态统计）
        List<DeptTaskStatsVO> deptOverallStats = bizMapper.getDeptTaskStats();
        // 补充每个部门的状态统计
        for (DeptTaskStatsVO stats : deptOverallStats) {
            if (stats.getDeptId() != null) {
                // 查询该部门的各状态任务数量
                List<BizTask> deptTasks = bizMapper.getTasksByDeptId(stats.getDeptId());
                // 统计各状态数量
                int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;
                for (BizTask task : deptTasks) {
                    String status = task.getStatus();
                    if (status == null) continue;

                    switch (status) {
                        case "0": notStarted++; break;
                        case "1": inProgress++; break;
                        case "2": inReview++; break;
                        case "3": finished++; break;
                    }
                }
                stats.setNotStartedCount(notStarted);
                stats.setInProgressCount(inProgress);
                stats.setInReviewCount(inReview);
                stats.setFinishedCount(finished);
            }
        }
        deptOverallStats.forEach(DeptTaskStatsVO::calculateCompletionRate);
        summary.setDeptOverallStats(deptOverallStats);

        // 2.2 各部门本年度完成率（在Service中补充状态统计）
        List<DeptTaskStatsVO> deptYearStats = bizMapper.getDeptYearTaskStats(currentYear);
        // 补充每个部门的本年度状态统计
        for (DeptTaskStatsVO stats : deptYearStats) {
            if (stats.getDeptId() != null) {
                // 查询该部门本年度各状态任务数量
                List<BizTask> deptYearTasks = bizMapper.getTasksByDeptIdAndPhase(stats.getDeptId(), currentYear);
                // 统计各状态数量
                int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;
                for (BizTask task : deptYearTasks) {
                    String status = task.getStatus();
                    if (status == null) continue;

                    switch (status) {
                        case "0": notStarted++; break;
                        case "1": inProgress++; break;
                        case "2": inReview++; break;
                        case "3": finished++; break;
                    }
                }
                stats.setNotStartedCount(notStarted);
                stats.setInProgressCount(inProgress);
                stats.setInReviewCount(inReview);
                stats.setFinishedCount(finished);
            }
        }
        deptYearStats.forEach(DeptTaskStatsVO::calculateCompletionRate);
        summary.setDeptYearStats(deptYearStats);

        // 2.3 各部门中期完成率（在Service中补充状态统计）
        List<DeptTaskStatsVO> deptMidTermStats = bizMapper.getDeptMidTermTaskStats(MID_TERM_END_YEAR);
        // 补充每个部门的中期状态统计
        for (DeptTaskStatsVO stats : deptMidTermStats) {
            if (stats.getDeptId() != null) {
                // 查询该部门中期各状态任务数量（需要单独查询）
                List<BizTask> deptTasks = bizMapper.getTasksByDeptId(stats.getDeptId());
                int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;
                for (BizTask task : deptTasks) {
                    // 添加空值检查
                    if (task.getPhase() != null && task.getPhase() < MID_TERM_END_YEAR) {
                        String status = task.getStatus();
                        if (status == null) continue;

                        switch (status) {
                            case "0": notStarted++; break;
                            case "1": inProgress++; break;
                            case "2": inReview++; break;
                            case "3": finished++; break;
                        }
                    }
                }
                stats.setNotStartedCount(notStarted);
                stats.setInProgressCount(inProgress);
                stats.setInReviewCount(inReview);
                stats.setFinishedCount(finished);
            }
        }
        deptMidTermStats.forEach(DeptTaskStatsVO::calculateCompletionRate);
        summary.setDeptMidTermStats(deptMidTermStats);
    }

    /**
     * 获取所有任务完成率（单独的接口）
     * @return 所有任务完成率
     */
    public TaskCompletionVO getAllTaskCompletionRate() {
        try {
            Integer totalTasks = bizMapper.getTotalTaskCount();
            Integer completedTasks = bizMapper.getCompletedTaskCount();

            // 在Service中查询各状态数量
            Integer notStartedCount = bizMapper.getTaskCountByStatus("0");
            Integer inProgressCount = bizMapper.getTaskCountByStatus("1");
            Integer inReviewCount = bizMapper.getTaskCountByStatus("2");
            Integer finishedCount = bizMapper.getTaskCountByStatus("3");

            return new TaskCompletionVO(
                    totalTasks, completedTasks, "all", "所有任务完成率",
                    notStartedCount != null ? notStartedCount : 0,
                    inProgressCount != null ? inProgressCount : 0,
                    inReviewCount != null ? inReviewCount : 0,
                    finishedCount != null ? finishedCount : 0
            );
        } catch (Exception e) {
            throw new RuntimeException("获取所有任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取本年度任务完成率（单独的接口）
     * @param year 年份，为空时使用当前年份
     * @return 本年度任务完成率
     */
    public TaskCompletionVO getYearTaskCompletionRate(Integer year) {
        try {
            if (year == null) {
                Calendar cal = Calendar.getInstance();
                year = cal.get(Calendar.YEAR);
            }

            Integer totalTasks = bizMapper.getYearTaskCount(year);
            Integer completedTasks = bizMapper.getYearCompletedTaskCount(year);

            // 在Service中查询本年度各状态数量
            Integer notStartedCount = bizMapper.getYearTaskCountByStatus(year, "0");
            Integer inProgressCount = bizMapper.getYearTaskCountByStatus(year, "1");
            Integer inReviewCount = bizMapper.getYearTaskCountByStatus(year, "2");
            Integer finishedCount = bizMapper.getYearTaskCountByStatus(year, "3");

            return new TaskCompletionVO(
                    totalTasks, completedTasks, "year", year + "年度任务完成率",
                    notStartedCount != null ? notStartedCount : 0,
                    inProgressCount != null ? inProgressCount : 0,
                    inReviewCount != null ? inReviewCount : 0,
                    finishedCount != null ? finishedCount : 0
            );
        } catch (Exception e) {
            throw new RuntimeException("获取本年度任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取中期任务完成率（单独的接口）
     * @param endYear 截止年份，为空时使用默认值2028
     * @return 中期任务完成率
     */
    public TaskCompletionVO getMidTermTaskCompletionRate(Integer endYear) {
        try {
            if (endYear == null) {
                endYear = MID_TERM_END_YEAR;
            }

            Integer totalTasks = bizMapper.getMidTermTaskCount(endYear);
            Integer completedTasks = bizMapper.getMidTermCompletedTaskCount(endYear);

            // 在Service中查询中期各状态数量
            Integer notStartedCount = bizMapper.getMidTermTaskCountByStatus(endYear, "0");
            Integer inProgressCount = bizMapper.getMidTermTaskCountByStatus(endYear, "1");
            Integer inReviewCount = bizMapper.getMidTermTaskCountByStatus(endYear, "2");
            Integer finishedCount = bizMapper.getMidTermTaskCountByStatus(endYear, "3");

            return new TaskCompletionVO(
                    totalTasks, completedTasks, "midterm", "中期（" + endYear + "年前）任务完成率",
                    notStartedCount != null ? notStartedCount : 0,
                    inProgressCount != null ? inProgressCount : 0,
                    inReviewCount != null ? inReviewCount : 0,
                    finishedCount != null ? finishedCount : 0
            );
        } catch (Exception e) {
            throw new RuntimeException("获取中期任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取一级任务完成率（单独的接口）
     * @return 一级任务完成率
     */
    public TaskCompletionVO getFirstLevelTaskCompletionRate() {
        try {
            Integer totalTasks = bizMapper.getFirstLevelTaskCount();
            Integer completedTasks = bizMapper.getFirstLevelCompletedTaskCount();

            // 在Service中查询一级各状态数量
            Integer notStartedCount = bizMapper.getFirstLevelTaskCountByStatus("0");
            Integer inProgressCount = bizMapper.getFirstLevelTaskCountByStatus("1");
            Integer inReviewCount = bizMapper.getFirstLevelTaskCountByStatus("2");
            Integer finishedCount = bizMapper.getFirstLevelTaskCountByStatus("3");

            return new TaskCompletionVO(
                    totalTasks, completedTasks, "firstLevel", "一级任务完成率",
                    notStartedCount != null ? notStartedCount : 0,
                    inProgressCount != null ? inProgressCount : 0,
                    inReviewCount != null ? inReviewCount : 0,
                    finishedCount != null ? finishedCount : 0
            );
        } catch (Exception e) {
            throw new RuntimeException("获取一级任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取各部门任务完成率（单独的接口）
     * @return 各部门任务完成率列表
     */
    public List<DeptTaskStatsVO> getDeptTaskCompletionRates() {
        try {
            List<DeptTaskStatsVO> stats = bizMapper.getDeptTaskStats();
            // 补充每个部门的状态统计
            for (DeptTaskStatsVO stat : stats) {
                if (stat.getDeptId() != null) {
                    // 查询该部门的各状态任务数量
                    List<BizTask> deptTasks = bizMapper.getTasksByDeptId(stat.getDeptId());
                    // 统计各状态数量
                    int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;
                    for (BizTask task : deptTasks) {
                        switch (task.getStatus()) {
                            case "0": notStarted++; break;
                            case "1": inProgress++; break;
                            case "2": inReview++; break;
                            case "3": finished++; break;
                        }
                    }
                    stat.setNotStartedCount(notStarted);
                    stat.setInProgressCount(inProgress);
                    stat.setInReviewCount(inReview);
                    stat.setFinishedCount(finished);
                }
            }
            stats.forEach(DeptTaskStatsVO::calculateCompletionRate);
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取各部门任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取各部门本年度任务完成率（单独的接口）
     * @param year 年份，为空时使用当前年份
     * @return 各部门本年度任务完成率列表
     */
    public List<DeptTaskStatsVO> getDeptYearTaskCompletionRates(Integer year) {
        try {
            if (year == null) {
                Calendar cal = Calendar.getInstance();
                year = cal.get(Calendar.YEAR);
            }

            List<DeptTaskStatsVO> stats = bizMapper.getDeptYearTaskStats(year);
            // 补充每个部门的本年度状态统计
            for (DeptTaskStatsVO stat : stats) {
                if (stat.getDeptId() != null) {
                    // 查询该部门本年度各状态任务数量
                    List<BizTask> deptYearTasks = bizMapper.getTasksByDeptIdAndPhase(stat.getDeptId(), year);
                    // 统计各状态数量
                    int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;
                    for (BizTask task : deptYearTasks) {
                        switch (task.getStatus()) {
                            case "0": notStarted++; break;
                            case "1": inProgress++; break;
                            case "2": inReview++; break;
                            case "3": finished++; break;
                        }
                    }
                    stat.setNotStartedCount(notStarted);
                    stat.setInProgressCount(inProgress);
                    stat.setInReviewCount(inReview);
                    stat.setFinishedCount(finished);
                }
            }
            stats.forEach(DeptTaskStatsVO::calculateCompletionRate);
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取各部门本年度任务完成率失败: " + e.getMessage());
        }
    }

    /**
     * 获取各部门中期任务完成率（单独的接口）
     * @param endYear 截止年份，为空时使用默认值2028
     * @return 各部门中期任务完成率列表
     */
    public List<DeptTaskStatsVO> getDeptMidTermTaskCompletionRates(Integer endYear) {
        try {
            if (endYear == null) {
                endYear = MID_TERM_END_YEAR;
            }

            List<DeptTaskStatsVO> stats = bizMapper.getDeptMidTermTaskStats(endYear);
            // 补充每个部门的中期状态统计
            for (DeptTaskStatsVO stat : stats) {
                if (stat.getDeptId() != null) {
                    // 查询该部门中期各状态任务数量（需要单独查询）
                    List<BizTask> deptTasks = bizMapper.getTasksByDeptId(stat.getDeptId());
                    int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;
                    for (BizTask task : deptTasks) {
                        // 添加空值检查
                        if (task.getPhase() != null && task.getPhase() < endYear) {
                            String status = task.getStatus();
                            if (status == null) continue;

                            switch (status) {
                                case "0": notStarted++; break;
                                case "1": inProgress++; break;
                                case "2": inReview++; break;
                                case "3": finished++; break;
                            }
                        }
                    }
                    stat.setNotStartedCount(notStarted);
                    stat.setInProgressCount(inProgress);
                    stat.setInReviewCount(inReview);
                    stat.setFinishedCount(finished);
                }
            }
            stats.forEach(DeptTaskStatsVO::calculateCompletionRate);
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取各部门中期任务完成率失败: " + e.getMessage());
        }
    }
    /**
     * 获取一级任务详细完成情况（单独的接口）
     * @return 一级任务详情列表
     */
    public List<FirstLevelTaskDetailVO> getFirstLevelTaskDetails() {
        try {
            return bizMapper.getFirstLevelTaskDetails();
        } catch (Exception e) {
            throw new RuntimeException("获取一级任务详情失败: " + e.getMessage());
        }
    }

//    获取全量任务详细情况
    public List<BizTask> getAllTaskDetails() {

        try {
            return bizMapper.getAllTasks();
        } catch (Exception e) {
            throw new RuntimeException("获取全量任务详情失败: " + e.getMessage());
        }    }

    /**
     * 获取单个部门的所有统计信息
     * @param deptId 部门ID
     * @return 部门统计信息
     */
    public Map<String, Object> getDeptStatsDetail(Long deptId) {
        try {
            Map<String, Object> result = new HashMap<>();

            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);

            // 获取部门信息
            String deptName = sysMapper.getDeptNameByDeptId(deptId);
            if (deptName == null) {
                throw new RuntimeException("部门不存在");
            }

            result.put("deptId", deptId);
            result.put("deptName", deptName);

            // 计算各种完成率（包含状态统计）
            result.put("overall", calculateDeptCompletionRate(deptId, null, null));
            result.put("year", calculateDeptCompletionRate(deptId, currentYear, null));
            result.put("midterm", calculateDeptCompletionRate(deptId, null, MID_TERM_END_YEAR));

            // 获取部门负责人
            Long leaderId = sysMapper.getDeptLeaderId(deptId);
            if (leaderId != null) {
                String leaderName = sysMapper.getUserById(leaderId).getNickName();
                result.put("leaderId", leaderId);
                result.put("leaderName", leaderName);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("获取部门统计详情失败: " + e.getMessage());
        }
    }

    /**
     * 计算部门的任务完成率（辅助方法）
     */
    private Map<String, Object> calculateDeptCompletionRate(Long deptId, Integer year, Integer endYear) {
        Map<String, Object> result = new HashMap<>();

        // 根据条件查询部门任务
        List<BizTask> deptTasks;
        if (year != null) {
            deptTasks = bizMapper.getTasksByDeptIdAndPhase(deptId, year);
        } else if (endYear != null) {
            // 中期任务需要单独过滤
            List<BizTask> allDeptTasks = bizMapper.getTasksByDeptId(deptId);
            deptTasks = new ArrayList<>();
            for (BizTask task : allDeptTasks) {
                if (task.getPhase() < endYear) {
                    deptTasks.add(task);
                }
            }
        } else {
            deptTasks = bizMapper.getTasksByDeptId(deptId);
        }

        // 统计任务数量
        int totalTasks = deptTasks.size();
        int completedTasks = 0;
        int notStarted = 0, inProgress = 0, inReview = 0, finished = 0;

        for (BizTask task : deptTasks) {
            switch (task.getStatus()) {
                case "0": notStarted++; break;
                case "1": inProgress++; break;
                case "2": inReview++; break;
                case "3":
                    finished++;
                    completedTasks++;
                    break;
            }
        }

        result.put("totalTasks", totalTasks);
        result.put("completedTasks", completedTasks);
        result.put("notStartedCount", notStarted);
        result.put("inProgressCount", inProgress);
        result.put("inReviewCount", inReview);
        result.put("finishedCount", finished);

        // 计算完成率和占比
        if (totalTasks > 0) {
            BigDecimal completionRate = BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP);
            result.put("completionRate", completionRate);

            result.put("notStartedRate", BigDecimal.valueOf(notStarted * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP));
            result.put("inProgressRate", BigDecimal.valueOf(inProgress * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP));
            result.put("inReviewRate", BigDecimal.valueOf(inReview * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP));
            result.put("finishedRate", BigDecimal.valueOf(finished * 100.0 / totalTasks)
                    .setScale(2, RoundingMode.HALF_UP));
        } else {
            result.put("completionRate", BigDecimal.ZERO);
            result.put("notStartedRate", BigDecimal.ZERO);
            result.put("inProgressRate", BigDecimal.ZERO);
            result.put("inReviewRate", BigDecimal.ZERO);
            result.put("finishedRate", BigDecimal.ZERO);
        }

        return result;
    }
}
```

---

### <a id='src\main\java\org\example\service\performanceservice-java'></a>src\main\java\org\example\service\PerformanceService.java

```java
package org.example.service;

import org.example.entity.BizPerformance;
import org.example.entity.BizTask;
import org.example.entity.RelTaskPerformance;
import org.example.mapper.BizMapper;
import org.example.mapper.PerformanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PerformanceService {

    @Autowired
    private PerformanceMapper performanceMapper;

    @Autowired
    private BizMapper taskMapper;

    public Object calcuateAllPerformance(){
        List<BizTask> allTasks = taskMapper.getAllTasks();
        for(BizTask task : allTasks){
            List<Long> prefIds= performanceMapper.getPerfIdByTaskId(task.getTaskId());
            for(Long prefId : prefIds){
                BizPerformance pref = performanceMapper.getPerformanceById(prefId);

                if(pref.getDataType().equals("0")){
                    continue;
                }else if(pref.getDataType().equals("1")){
                    pref.setCurrentValue(pref.getCurrentValue().add(task.getCurrentValue()));
                    pref.setUpdateTime(new Date());
                    performanceMapper.updatePerformance(pref);

                }else if(pref.getDataType().equals("2")){
                    if(task.getCurrentValue().compareTo(pref.getCurrentValue())>0){
                        pref.setCurrentValue(task.getCurrentValue());
                        pref.setUpdateTime(new Date());
                        performanceMapper.updatePerformance(pref);
                    }

                }
            }
        }
//        返回已更新了多少个绩效
        return "绩效数据已更新";

    }

//    根据任务id更新绩效
    public Object updatePerformanceByTaskId(Long taskId){
        if(taskId==null){
            throw new RuntimeException("请输入有效的任务ID");
        }
        if(taskMapper.getTaskById(taskId)==null){
            throw new RuntimeException("没有找到该任务");
        }
        List<Long> prefIds = performanceMapper.getPerfIdByTaskId(taskId);
        for(Long prefId : prefIds){
            BizPerformance pref = performanceMapper.getPerformanceById(prefId);
            if(pref.getDataType().equals("0")){
                continue;
            }else if(pref.getDataType().equals("1")){
                pref.setCurrentValue(pref.getCurrentValue().add(taskMapper.getTaskById(taskId).getCurrentValue()));
                pref.setUpdateTime(new Date());
                performanceMapper.updatePerformance(pref);

            }else if(pref.getDataType().equals("2")){
                if(taskMapper.getTaskById(taskId).getCurrentValue().compareTo(pref.getCurrentValue())>0){
                    pref.setCurrentValue(taskMapper.getTaskById(taskId).getCurrentValue());
                    pref.setUpdateTime(new Date());
                    performanceMapper.updatePerformance(pref);
                }
            }
        }
        return "绩效数据已更新";
    }



    public Object getAllPerformance(){
        return performanceMapper.getAllPerformance();
    }

    public Object getPerformanceById(Long perfId){
        if(perfId==null){
            throw new RuntimeException("请输入有效的绩效ID");
        }
        if(performanceMapper.getPerformanceById(perfId)==null){
            throw new RuntimeException("没有找到该绩效");
        }
        if(performanceMapper.getPerformanceById(perfId).getIsDelete().equals("1")){
            throw new RuntimeException("该绩效已被删除");
        }
        return performanceMapper.getPerformanceById(perfId);
    }

    public Object getPerfByTaskId(Long taskId){
        if(taskId==null){
            throw new RuntimeException("请输入有效的任务ID");
        }
        if(taskMapper.getTaskById(taskId)==null){
            throw new RuntimeException("没有找到该任务");
        }
        if(taskMapper.getTaskById(taskId).getIsDelete().equals("1")){
            throw new RuntimeException("该任务已被删除");
        }
        List<Long> prefIds = performanceMapper.getPerfIdByTaskId(taskId);
        List<BizPerformance> prefList = new ArrayList<>();
        for(Long prefId : prefIds){
            prefList.add(performanceMapper.getPerformanceById(prefId));
        }
        return prefList;
    }

    public Object getTaskByPerfId(Long perfId){
        if(perfId==null){
            throw new RuntimeException("请输入有效的绩效ID");
        }
        if(performanceMapper.getPerformanceById(perfId)==null){
            throw new RuntimeException("没有找到该绩效");
        }
        if(performanceMapper.getPerformanceById(perfId).getIsDelete().equals("1")){
            throw new RuntimeException("该绩效已被删除");
        }
        List<Long> taskIds = performanceMapper.getPerfIdByTaskId(perfId);
        List<BizTask> taskList = new ArrayList<>();
        for(Long taskId : taskIds){
            taskList.add(taskMapper.getTaskById(taskId));
        }
        return taskList;
    }
}
```

---

### <a id='src\main\java\org\example\service\scheduledtaskservice-java'></a>src\main\java\org\example\service\ScheduledTaskService.java

```java
package org.example.service;

import org.example.entity.*;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.example.mapper.TokenBlacklistMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 定时任务服务类
 * 负责执行定期提醒、清理等后台任务
 */
@Service
public class ScheduledTaskService {

    private Long ADMIN_ID = 110228L;

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private SysMapper sysMapper;

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

    /**
     * 每月初提醒审核任务
     * 每月1号上午9:00执行
     * 提醒所有部门负责人本部门任务完成情况
     */
    @Scheduled(cron = "0 0 9 1 * ?")  // 每月1号上午9:00
    @Transactional
    public String monthlyDeptLeaderReminder() {
        try {
            System.out.println("[" + new Date() + "] 开始执行月度部门负责人审核任务提醒...");

            Set<Long> deptLeaderIds = getDeptLeadersId();

            if (deptLeaderIds.isEmpty()) {
                System.out.println("没有需要发送年度提醒的用户");
                return "没有需要发送年度提醒的用户";
            }

            int successCount = 0;
            for (Long deptLeaderId: deptLeaderIds){
                SysDept dept = sysMapper.getDeptByUserId(deptLeaderId);
//                获取当前时间，将年份转为Integer
                int currentYear = java.time.LocalDate.now().getYear();
//                获取本年度本部门的所有任务
                List<BizTask> currentYearTasks = bizMapper.getTasksByDeptIdAndPhase(dept.getDeptId(),currentYear);
                if (currentYearTasks.isEmpty()) {
                    continue;
                }
                // 统计截止本月有进展的任务数量
                Integer monthCount = 0;
                for (BizTask task: currentYearTasks){
                    if (task.getCurrentValue().signum() != 0) {
                        monthCount++;
                    }
                }

//                统计已完成任务数量
                Integer completeCount = 0;
                for (BizTask task: currentYearTasks){
                    if (task.getStatus().equals("3")) {
                        completeCount++;
                    }
                }

//                统计待审核任务数量
                Integer auditCount = 0;
                for (BizTask task: currentYearTasks){
                    if (task.getStatus().equals("2")) {
                        auditCount++;
                    }
                }

                String title = "部门双高建设任务完成情况月度提醒";
                String content = "尊敬的 "+ sysMapper.getUserById(deptLeaderId).getNickName() +
                        " 您好,本年度贵部门有 " + currentYearTasks.size() + " 个双高建设任务，\n" +
                        "截至本月，共" +  monthCount + "个任务已有进展。\n"+
                        "已完成任务" + completeCount + " 个，" + auditCount + " 个任务尚未审核\n"+
                        "请及时关注本部门双高建设任务进展情况。";
                sendSystemNotice(deptLeaderId, title, content, "月度提醒");
                System.out.println("向用户ID " + deptLeaderId + " 发送月度提醒成功");
                successCount++;
            }
            return "向"+successCount+"名用户发送月度提醒成功";

        } catch (Exception e) {
            System.err.println("执行月度审核提醒任务时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


    /**
     * 每月初提醒审核任务
     * 每月1号上午9:00执行
     * 提醒所有有待审核任务的负责人
     */
    @Scheduled(cron = "0 0 9 1 * ?")  // 每月1号上午9:00
    @Transactional
    public String monthlyAuditorReminder() {
        try {
            System.out.println("[" + new Date() + "] 开始执行月度审核人审核任务提醒...");
            List<BizMaterialSubmission> allPendingAudits = bizMapper.getAllPendingAudits();


            Set<Long> auditorIds = new HashSet<>();
            for (BizMaterialSubmission pendingAudit: allPendingAudits){
                auditorIds.add(pendingAudit.getCurrentHandlerId());
            }

            if (auditorIds.isEmpty()) {
                System.out.println("没有需要发送年度提醒的用户");
                return "没有需要发送年度提醒的用户";
            }
            int successCount = 0;
            for (Long auditorId: auditorIds){
//              获取待审核任务数量
                Integer auditCount = 0;
                for (BizMaterialSubmission pendingAudit: allPendingAudits){
                    if (pendingAudit.getCurrentHandlerId().equals(auditorId)) {
                        auditCount++;
                    }
                }

                String title = "月度审核任务提醒";
                String content = "尊敬的 " + sysMapper.getUserById(auditorId).getNickName() +
                        " 您好,截至目前，您有 " + auditCount + " 个待审核任务。\n" +
                        "请及时处理。";

                sendSystemNotice(auditorId, title, content, "月度提醒");
                System.out.println("向用户ID " + auditorId + " 发送月度提醒成功");
                successCount++;
            }
            return "向"+successCount+"名用户发送月度提醒成功";
        } catch (Exception e) {
            System.err.println("执行月度审核提醒任务时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
//
    /**
     * 年度提醒任务
     * 每年1月1日上午10:00执行
     * 提醒所有部门负责人年度任务情况
     */
    @Scheduled(cron = "0 0 10 1 1 ?")  // 每年1月1日上午10:00
    @Transactional
    public String yearlyReminder() {
        try {
            System.out.println("[" + new Date() + "] 开始执行年度提醒任务...");

            // 获取所有需要发送年度提醒的用户ID
            Set<Long> leadersId = getDeptLeadersId();

            if (leadersId.isEmpty()) {
                System.out.println("没有需要发送年度提醒的用户");
                return "没有需要发送年度提醒的用户";
            }
            int currentYear = java.time.LocalDate.now().getYear();
            int successCount = 0;
            for(Long leaderId: leadersId){
                SysDept dept = sysMapper.getDeptByUserId(leaderId);
//                获取本年度本部门的所有任务
                List<BizTask> currentYearTasks = bizMapper.getTasksByDeptIdAndPhase(dept.getDeptId(),currentYear);
                if (currentYearTasks.isEmpty()) {
                    continue;
                }

                String title = "部门双高建设任务年度提醒";
                String content = "尊敬的 "+ sysMapper.getUserById(leaderId).getNickName() +
                        " 您好,本年度贵部门有 " + currentYearTasks.size() + " 个双高建设任务，\n" +
                        "请及时关注本部门双高建设任务进展情况。";
                sendSystemNotice(leaderId, title, content, "月度提醒");
                successCount++;
                System.out.println("向用户ID " + leaderId + " 发送月度提醒成功");

            }

            System.out.println("年度提醒完成，成功发送 " + successCount + " 条提醒");
            return "年度提醒完成，成功发送 " + successCount + " 条提醒";

        } catch (Exception e) {
            System.err.println("执行年度提醒任务时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
//
    /**
     * 封装方法：发送系统通知
     *
     * @param toUserId 接收用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param triggerEvent 触发事件类型
     */
    private void sendSystemNotice(Long toUserId, String title, String content, String triggerEvent) {
        SysNotice notice = new SysNotice();
        notice.setFromUserId(ADMIN_ID);
        notice.setToUserId(toUserId);
        notice.setType("1");  // 提醒类型
        notice.setTriggerEvent(triggerEvent);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setSourceType("0");
        notice.setSourceId(0L);
        notice.setIsRead("0");
        notice.setIsDelete(0);
        notice.setCreateTime(new Date());

        sysMapper.sendNotice(notice);

        System.out.println("系统通知已发送 - 接收人ID: " + toUserId + ", 标题: " + title);
    }

    /**
     * 获取需要发送年度提醒的所有用户ID
     * 您可以自定义这里的逻辑
     *
     * @return 用户ID集合
     */
    private Set<Long> getDeptLeadersId() {
        Set<Long> userIds = new HashSet<>();

        try {
            // 获取所有部门负责人
            List<Long> deptLeaders = sysMapper.getAllDeptLeaders();
            if (deptLeaders != null) {
                userIds.addAll(deptLeaders);
            }

            // 可以根据需要添加其他用户组
            // 例如：所有管理员、所有用户等

        } catch (Exception e) {
            System.err.println("获取用户列表时发生错误: " + e.getMessage());
        }

        return userIds;
    }
    /**
     * 测试方法：手动触发月度提醒
     */
    public String triggerMonthDeptLeaderReminder() {
        System.out.println("手动触发月度审核提醒...");
        return monthlyDeptLeaderReminder();
    }

    /**
     * 测试方法：手动触发月度提醒
     */
    public String triggerMonthAuditorReminder() {
        System.out.println("手动触发月度审核提醒...");
        return monthlyAuditorReminder();
    }


    /**
     * 测试方法：手动触发年度提醒
     */
    public String triggerYearlyReminder() {
        System.out.println("手动触发年度提醒...");
        return yearlyReminder();
    }
    /**
     * 每日清理过期的Token黑名单
     * 每天凌晨2:00执行
     */
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2:00
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            System.out.println("[" + new Date() + "] 开始清理过期Token...");
            tokenBlacklistMapper.cleanupExpiredTokens();
            System.out.println("Token清理完成");
        } catch (Exception e) {
            System.err.println("清理过期Token时发生错误: " + e.getMessage());
        }
    }


    /**
     * 每年更新任务状态
     * 每天凌晨10:00执行
     */
    @Scheduled(cron = "0 0 10 1 1 ?")
    @Transactional
    public void updateTaskStatus() {
        try {
            System.out.println("[" + new Date() + "] 开始更新任务状态...");
            int currentYear = java.time.LocalDate.now().getYear();
            List<BizTask> tasks = bizMapper.getTasksByPhase(currentYear);
            for (BizTask task : tasks) {
                task.setStatus("1");
                bizMapper.updateTask(task);
            }
            System.out.println("已将"+currentYear+"所有任务的状态修改为进行中");
        } catch (Exception e) {
            System.err.println("更新任务状态时发生错误: " + e.getMessage());
        }
    }


    @Autowired
    private TrendDataService trendDataService;
    /**
     * 趋势数据定时任务
     * 每天23:30执行
     */
    @Scheduled(cron = "0 30 23 * * ?")
    public void dailyTrendRecord() {
        System.out.println("[" + new Date() + "] 开始执行趋势数据记录...");
        trendDataService.recordDailyTrendData();
    }

}
```

---

### <a id='src\main\java\org\example\service\sysservice-java'></a>src\main\java\org\example\service\SysService.java

```java
package org.example.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.*;
import org.example.entity.dto.FileUploadDTO;
import org.example.entity.dto.SysAlertDTO;
import org.example.entity.dto.SysNoticeDTO;
import org.example.entity.dto.SysUserDTO;
import org.example.entity.vo.FileUploadVO;
import org.example.entity.vo.SysLoginVO;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.example.mapper.TokenBlacklistMapper;
import org.example.utils.FileUploadUtil;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 系统服务类：处理用户管理、登录认证、文件上传等系统功能
 */
@Service
public class SysService {

    @Autowired
    private SysMapper sysMapper;
    @Autowired
    private BizMapper bizMapper;

    /**
     * 获取所有用户
     * @return 用户列表
     */
    public List<SysUser> getAllUsers() {
        return sysMapper.getAllUsers();
    }

    /**
     * 获取部门信息（用于前端根据 deptId 查询 leaderId 等信息）
     * @param deptId 部门ID
     * @return 部门对象
     */
    public SysDept getDeptById(Long deptId) {
        if (deptId == null) {
            throw new RuntimeException("deptId为空");
        }
        SysDept dept = sysMapper.getDeptById(deptId);
        if (dept == null) {
            throw new RuntimeException("部门不存在");
        }
        return dept;
    }

    /**
     * 用户登录
     * 改成了user_id
     * @param userId 用户ID
     * @param password 密码
     * @return 登录视图对象
     */
    public SysLoginVO login(Long userId, String password) {
        SysUser user = sysMapper.getUserById(userId);
        if (user != null && user.getPassword().equals(password)) {
            SysLoginVO sysLoginVo = new SysLoginVO(user.getNickName(), JWTUtil.generateJwtToken(user));
            return sysLoginVo;
        } else if (user == null){
            throw new RuntimeException("用户不存在");
        } else if (!user.getPassword().equals(password)){
            throw new RuntimeException("密码错误");
        }
        return null;
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    public void changePassword(Long userId, String newPassword) {
        SysUser user = sysMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(newPassword);
        sysMapper.updateUser(user);
    }

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

    /**
     * 用户注销
     * @param token Token字符串
     */
    public void logout(String token) {
        try{
            tokenBlacklistMapper.addToBlacklist(new TokenBlacklist(token, JWTUtil.verifyJwtToken(token).getExpiresAt()));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传文件
     * @param file 文件对象
     * @param taskId 任务ID
     * @param request HTTP请求
     * @return 文件上传视图对象
     */
    public Object uploadFile(MultipartFile file, Long taskId,HttpServletRequest request) {
        try{
            if (file.isEmpty()) {
                throw new RuntimeException("文件不能为空");
            }
            // 如果文件后缀不是doc,docx,pdf中的一个，报错
            if (!file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1).matches("doc|docx|pdf")) {
                throw new RuntimeException("文件格式错误");
            }
            FileUploadDTO fileUploadDTO = FileUploadUtil.upload(file);
            SysFile sysFile = new SysFile();
            sysFile.setFilePath(fileUploadDTO.getFilepath());
            sysFile.setFileName(fileUploadDTO.getFilename());
            sysFile.setFileUrl(fileUploadDTO.getFilepath());
            sysFile.setFileSuffix(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1));
            sysFile.setFileSize(file.getSize());
            Long userId = JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
            sysFile.setUploadBy(userId);
            sysFile.setUploadTime(new Date());
            System.out.println(sysFile);
            sysMapper.uploadFile(sysFile);
            return new FileUploadVO(sysMapper.getFileByName(sysFile.getFileName()).getFileId(), fileUploadDTO.getFilename(), fileUploadDTO.getFilepath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据file_id下载文件
     * @param fileId 文件ID
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    public void downloadFile(Long fileId, HttpServletResponse response) throws IOException {
        // 1. 查询文件信息
        SysFile sysFile = sysMapper.getFileById(fileId);
        if (sysFile == null) {
            throw new RuntimeException("文件不存在");
        }

        // 2. 构建文件路径
        String fullPath = System.getProperty("user.dir") + sysFile.getFilePath();
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new RuntimeException("文件已被删除或移动");
        }

        // 3. 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(sysFile.getFileName(), StandardCharsets.UTF_8.name()) + "\"");
        response.setContentLengthLong(file.length());

        // 4. 写入文件流
        try (InputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
    }

    /**
     * 发送消息
     * @param sysNoticeDTO 通知数据
     * @param userId 用户ID
     */
    public void sendNotice(SysNoticeDTO sysNoticeDTO, Long userId) {
        try{
            SysNotice sysNotice = new SysNotice();
            sysNotice.setFromUserId(userId);
            sysNotice.setToUserId(sysNoticeDTO.getTo_user_id());
            sysNotice.setType(sysNoticeDTO.getType());
            sysNotice.setTriggerEvent(sysNoticeDTO.getTrigger_event());
            sysNotice.setTitle(sysNoticeDTO.getTitle());
            sysNotice.setContent(sysNoticeDTO.getContent());
            sysNotice.setSourceType("1");
            sysNotice.setSourceId(sysNoticeDTO.getSource_id());
            sysNotice.setIsRead("0");
            sysMapper.sendNotice(sysNotice);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查看当前用户收到的通知
     * @param userId 用户ID
     * @return 通知列表
     */
    public List<SysNotice> getNotices(Long userId) {
        try{
            return sysMapper.getNotices(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设为已读setRead
     * @param noticeId 通知ID
     */
    public void setRead(Long noticeId) {
        try{
            sysMapper.setRead(noticeId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 站内预警
     * @param sysAlertDTO 预警数据
     * @param userId 用户ID
     */
    public void sendAlert(SysAlertDTO sysAlertDTO, Long userId) {
        try{
            SysNotice sysNotice = new SysNotice();
            sysNotice.setFromUserId(userId);
            sysNotice.setToUserId(sysMapper.getUserByNickName(sysAlertDTO.getTo_user_nick_name()).getUserId());
            sysNotice.setType("1");
            sysNotice.setTriggerEvent("1");
            sysNotice.setTitle(sysAlertDTO.getTitle());
            sysNotice.setContent(sysAlertDTO.getContent());
            sysNotice.setSourceType("1");
            sysNotice.setSourceId(sysAlertDTO.getSource_id());
            sysNotice.setIsRead("0");
            sysMapper.sendNotice(sysNotice);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据ID获取用户
     * @param userId 用户ID
     * @return 用户对象
     */
    public SysUser getUserById(Long userId) {
        return sysMapper.getUserById(userId);
    }

    /**
     * 根据用户名获取用户
     * @param userName 用户名
     * @return 用户对象
     */
    public SysUser getUserByName(String userName) {
        return sysMapper.getUserByName(userName);
    }

    /**
     * 添加用户
     * @param user 用户数据传输对象
     */
    public void addUser(SysUserDTO user) {
        // id已存在，报错
        if(sysMapper.getUserById(user.getUserId()) != null){
            throw new RuntimeException("用户id已存在");
        }
        if(sysMapper.getUserByName(user.getUserName()) != null){
            throw new RuntimeException("用户名已存在，请添加文字进行区分");
        }
        // 验证deptId是否存在
        if(sysMapper.getDeptById(user.getDeptId()) == null){
            throw new RuntimeException("部门不存在");
        }

        sysMapper.addUser(userDTO2User(user));
    }

    /**
     * 更新用户
     * @param user 用户数据传输对象
     */
    public void updateUser(SysUserDTO  user) {
        if(sysMapper.getUserByName(user.getUserName()) != null){
            throw new RuntimeException("用户名已存在，请添加文字进行区分");
        }
        if(sysMapper.getDeptById(user.getDeptId()) == null){
            throw new RuntimeException("部门不存在");
        }

        sysMapper.updateUser(userDTO2User(user));
    }

    /**
     * 删除用户
     * @param userId 用户ID
     */
    public void deleteUser(Long userId) {
        if(sysMapper.getUserById(userId) == null){
            throw new RuntimeException("用户不存在");
        }

        if(!bizMapper.getTasks(userId).isEmpty()){
            throw new RuntimeException("该用户名下有任务，请先修改任务负责人");
        }
        sysMapper.deleteUser(userId);
    }

    /**
     * UserDTO转User转换方法
     * @param user 用户数据传输对象
     * @return 用户实体
     */
    public SysUser userDTO2User(SysUserDTO user) {
        SysUser newUser = new SysUser();
        newUser.setUserId(user.getUserId());
        newUser.setDeptId(user.getDeptId());
        newUser.setUserName(user.getUserName());
        newUser.setNickName(user.getNickName());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setRole(user.getRole());
        newUser.setCreateTime(new Date());
        newUser.setUpdateTime(new Date());
        newUser.setStatus("1");
        newUser.setIsDelete(0);
        return newUser;
    }
}
```

---

### <a id='src\main\java\org\example\service\trenddataservice-java'></a>src\main\java\org\example\service\TrendDataService.java

```java
package org.example.service;

import org.example.entity.BizTask;
import org.example.entity.BizTrendData;
import org.example.mapper.BizMapper;
import org.example.mapper.TrendDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class TrendDataService {

    @Autowired
    private TrendDataMapper trendDataMapper;

    @Autowired
    private BizMapper bizMapper;

    /**
     * 每日记录趋势数据（定时任务）
     */
    @Transactional
    public void recordDailyTrendData() {
        try {
            // 检查今天是否已记录
            Integer count = trendDataMapper.checkTodayRecorded();
            if (count > 0) {
                System.out.println("今天已记录趋势数据，跳过");
                return;
            }

            LocalDate today = LocalDate.now();
            int year = today.getYear();
            int month = today.getMonthValue();
            int day = today.getDayOfMonth();

            // 获取今年的所有三级任务
            List<BizTask> tasks = bizMapper.getTasksByPhase(year);

            // 筛选三级任务
            List<BizTask> thirdLevelTasks = tasks.stream()
                    .filter(task -> task.getLevel() == 3 && task.getIsDelete() == 0)
                    .toList();

            if (thirdLevelTasks.isEmpty()) {
                return; // 没有三级任务，不记录
            }

            // 计算统计数据
            int totalTasks = thirdLevelTasks.size(); // 总任务数
            int completionCount = (int) thirdLevelTasks.stream()
                    .filter(task -> "3".equals(task.getStatus()))
                    .count(); // 完成数量

            double completionRate = calculateCompletionRate(thirdLevelTasks); // 完成率

            // 保存到数据库
            BizTrendData trendData = new BizTrendData();
            trendData.setYear(year);
            trendData.setMonth(month);
            trendData.setDay(day);
            trendData.setTotalTasks(totalTasks);        // 设置总任务数
            trendData.setCompletionCount(completionCount);
            trendData.setCompletionRate(completionRate);

            trendDataMapper.insertTrendData(trendData);

            System.out.println("趋势数据记录成功：" + year + "-" + month + "-" + day +
                    "，总任务数：" + totalTasks +
                    "，完成数量：" + completionCount +
                    "，完成率：" + completionRate + "%");

        } catch (Exception e) {
            System.err.println("记录趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 计算完成率（所有三级任务的平均完成率）
     */
    private double calculateCompletionRate(List<BizTask> tasks) {
        if (tasks.isEmpty()) {
            return 0.0;
        }

        BigDecimal totalRate = BigDecimal.ZERO;
        int validTasks = 0;

        for (BizTask task : tasks) {
            BigDecimal target = task.getTargetValue();
            BigDecimal current = task.getCurrentValue();

            if (target != null && target.compareTo(BigDecimal.ZERO) > 0) {
                // 计算单个任务的完成率
                BigDecimal taskRate = current
                        .multiply(BigDecimal.valueOf(100))
                        .divide(target, 2, RoundingMode.HALF_UP);

                // 限制在0-100之间
                if (taskRate.compareTo(BigDecimal.ZERO) < 0) {
                    taskRate = BigDecimal.ZERO;
                } else if (taskRate.compareTo(BigDecimal.valueOf(100)) > 0) {
                    taskRate = BigDecimal.valueOf(100);
                }

                totalRate = totalRate.add(taskRate);
                validTasks++;
            }
        }

        if (validTasks > 0) {
            return totalRate.divide(BigDecimal.valueOf(validTasks), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        return 0.0;
    }
    /**
     * 根据年份获取趋势数据（直接返回数据库记录）
     */
    public List<BizTrendData> getTrendDataByYear(Integer year) {
        try {
            if (year == null) {
                // 默认当前年份
                year = LocalDate.now().getYear();
            }
            return trendDataMapper.getTrendDataByYear(year);
        } catch (Exception e) {
            throw new RuntimeException("获取趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发记录（测试用）
     */
    public String triggerRecord() {
        try {
            recordDailyTrendData();
            return "手动记录成功";
        } catch (Exception e) {
            return "手动记录失败: " + e.getMessage();
        }
    }
}
```

---

### <a id='src\main\java\org\example\utils\fileuploadutil-java'></a>src\main\java\org\example\utils\FileUploadUtil.java

```java
package org.example.utils;

import org.example.entity.dto.FileUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件上传工具类
 */
public class FileUploadUtil {
    /** 上传目录 */
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    /** 非法字符正则 */
    private static final Pattern INVALID_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");

    /** 重复文件模式正则 */
    private static final Pattern DUPLICATE_PATTERN = Pattern.compile("(.+?)(?:\\((\\d+)\\))?(\\.[^.]*)?$");

    /**
     * 上传文件
     * @param file 文件对象
     * @return 文件上传数据传输对象
     * @throws IOException IO异常
     */
    public static FileUploadDTO upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 创建上传目录
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 获取并处理原始文件名
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }

        // 安全处理
        String safeFileName = Paths.get(originalName).getFileName().toString();
        safeFileName = INVALID_CHARS.matcher(safeFileName).replaceAll("");

        if (safeFileName.isEmpty()) {
            safeFileName = "uploaded_file";
        }

        // 生成唯一文件名
        String finalFileName = getUniqueFileName(uploadPath, safeFileName);

        // 保存文件
        Path filePath = uploadPath.resolve(finalFileName);
        Files.copy(file.getInputStream(), filePath);
        filePath.toFile().setReadable(true, false);

        return new FileUploadDTO(finalFileName, "/uploads/" + finalFileName);
    }

    /**
     * 智能生成唯一文件名
     * 示例：
     * file.txt -> file.txt (不存在时)
     * file.txt -> file(1).txt (file.txt已存在)
     * file(1).txt -> file(2).txt (file(1).txt已存在)
     * file(2).txt -> file(3).txt (file(2).txt已存在)
     * @param uploadPath 上传路径
     * @param fileName 文件名
     * @return 唯一文件名
     */
    private static String getUniqueFileName(Path uploadPath, String fileName) {
        // 解析文件名，提取基础名称、数字后缀和扩展名
        Matcher matcher = DUPLICATE_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            throw new RuntimeException("文件名格式不正确");
        }

        String baseName = matcher.group(1);      // 基础名称
        String numberStr = matcher.group(2);     // 数字后缀，可能为null
        String extension = matcher.group(3) != null ? matcher.group(3) : ""; // 扩展名

        int startNumber = 1;
        if (numberStr != null) {
            // 如果文件名已经有数字后缀，则从该数字+1开始
            try {
                startNumber = Integer.parseInt(numberStr) + 1;
                // 如果有数字后缀，baseName就是原始基础名
                // 例如：file(1).txt -> baseName = "file", numberStr = "1"
            } catch (NumberFormatException e) {
                // 如果数字格式错误，从1开始
                startNumber = 1;
            }
        }

        // 先检查原始文件名（没有后缀的情况）
        if (numberStr == null && !Files.exists(uploadPath.resolve(fileName))) {
            return fileName;
        }

        // 寻找下一个可用的数字后缀
        for (int i = startNumber; i <= 1000; i++) {
            String newFileName;
            if (i == 1) {
                // 第一次添加后缀
                newFileName = String.format("%s(%d)%s", baseName, i, extension);
            } else {
                // 更新数字后缀
                newFileName = String.format("%s(%d)%s", baseName, i, extension);
            }

            if (!Files.exists(uploadPath.resolve(newFileName))) {
                return newFileName;
            }
        }

        throw new RuntimeException("文件名冲突过多，请重命名文件后上传");
    }
}
```

---

### <a id='src\main\java\org\example\utils\jwtutil-java'></a>src\main\java\org\example\utils\JWTUtil.java

```java
package org.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.example.entity.SysUser;
import org.example.entity.TokenBlacklist;
import org.example.mapper.TokenBlacklistMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * JWT工具类：生成、验证和解析Token
 */
public class JWTUtil {
    /** 密钥，与生成 Token 时使用的密钥一致 */
    private static final String SECRET_KEY = "secretKey";

    /**
     * 生成jwt token
     * @param user 用户对象
     * @return Token字符串
     */
    public static String generateJwtToken(SysUser user) {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + 3600000; // 令牌有效期为 1 小时
        Date exp = new Date(expMillis);

        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withClaim("id",user.getUserId())
                .withClaim("username", user.getUserName())
                .withClaim("role", user.getRole())
                .withIssuer("auth0")
                .withExpiresAt(exp)
                .sign(algorithm);
    }

    /**
     * 验证jwt token
     * @param token Token字符串
     * @return 解码后的JWT对象
     */
    public static DecodedJWT verifyJwtToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException exception) {
            // Invalid signature/claims
            throw new RuntimeException("Invalid token: " + exception.getMessage());
        }
    }

    /**
     * 从Token获取用户ID
     * @param token Token字符串
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        return decodedJWT.getClaim("id").asLong();
    }

    /**
     * 从Token获取角色
     * @param token Token字符串
     * @return 角色字符串
     */
    public static String getRoleFromToken(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        return decodedJWT.getClaim("role").asString();
    }

    /**
     * 检查Token是否过期
     * @param token Token字符串
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        Date expiresAt = decodedJWT.getExpiresAt();
        return expiresAt.before(new Date());
    }

    /**
     * 检查Token是否在黑名单中
     * @param token Token字符串
     * @return 是否在黑名单中
     */
    public static boolean isTokenInBlacklist(String token) {
        // TODO: 添加黑名单检查逻辑
        return false;
    }
}
```

---

### <a id='src\main\resources\application-properties'></a>src\main\resources\application.properties

```properties
# ?????
spring.datasource.url=jdbc:mysql://localhost:3306/biz
spring.datasource.username=root
spring.datasource.password=981119

# ??????
spring.web.resources.static-locations=file:${user.dir}/uploads/
spring.mvc.static-path-pattern=/uploads/**

# MyBatis??
mybatis.type-aliases-package=org.example.entity
mybatis.configuration.map-underscore-to-camel-case=true

# ??????
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# ??????
spring.task.scheduling.pool.size=5
spring.task.scheduling.thread-name-prefix=scheduled-task-

# ???????????true?
schedule.enabled=true

# ????????
schedule.monthly-reminder.enabled=true
schedule.monthly-reminder.cron=0 0 9 1 * ?
schedule.token-cleanup.enabled=true
schedule.token-cleanup.cron=0 0 2 * * ?

server.servlet.context-path=/api
```

---

### <a id='部署方法\部署方法-md'></a>部署方法\部署方法.md

```markdown
### 服务器账号密码

```
ip地址:172.19.2.81
用户名:root
密码:Xzcit@xg.2025.8

数据库账号密码
账号：root
密码：Xxxy@123
```

### 后端部署

打包之前记得将数据库密码改为服务器的数据库密码

先后双击clean和package

![image-20260108170446549](./image-20260108170446549.png)

找到target目录下生成的jar包

![image-20260108170529510](./image-20260108170529510.png)

将jar包移动到服务器任意位置

![image-20260108170600476](./image-20260108170600476.png)

输入命令```ps aux|grep java```查看当前运行的程序

![image-20260108170708655](./image-20260108170708655.png)

输入 ```kill -9 [PID]```中断原有后端程序（如```kill -9 53586```)

![image-20260108170910759](./image-20260108170910759.png)

输入命令```nohup java -jar biz_backend-1.0-SNAPSHOT.jar &```，后台运行

![image-20260108171052923](./image-20260108171052923.png)

### 前端部署

修改```vite.config.js```，将ip地址改为服务器ip地址

![image-20260108171155096](./image-20260108171155096.png)

运行```npm run builds```生成dist文件夹

![image-20260108171306969](./image-20260108171306969.png)

在服务器中找到目录```/usr/share/nginx/html```

![image-20260108171407788](./image-20260108171407788.png)

将生成的dist文件**里面**的内容，上传到该目录下，替换原有文件![image-20260108171513749](./image-20260108171513749.png)

部署完毕
```

---

