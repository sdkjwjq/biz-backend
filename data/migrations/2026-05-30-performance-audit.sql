CREATE TABLE IF NOT EXISTS `biz_performance_submission` (
    `sub_id` bigint(20) NOT NULL AUTO_INCREMENT,
    `perf_id` bigint(20) NOT NULL COMMENT '绩效指标ID',
    `year_id` bigint(20) NOT NULL COMMENT '绩效年度ID',
    `year` int(4) NOT NULL COMMENT '年份',
    `actual_value` decimal(20,4) DEFAULT 0.0000 COMMENT '本次填报的年度实际值',
    `submit_by` bigint(20) NOT NULL COMMENT '提交人ID',
    `submit_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `flow_status` int(11) NOT NULL DEFAULT 10 COMMENT '10待专业群审核 20待管理员审核 30已通过 -10专业群退回 -20管理员退回 0已撤回',
    `current_handler_id` bigint(20) DEFAULT NULL COMMENT '当前处理人ID',
    `comment` varchar(1000) DEFAULT NULL COMMENT '填报说明或审核意见',
    `is_delete` tinyint(1) DEFAULT 0 COMMENT '0存在 1作废',
    PRIMARY KEY (`sub_id`),
    KEY `idx_perf_submission_handler` (`current_handler_id`, `flow_status`, `is_delete`),
    KEY `idx_perf_submission_perf_year` (`perf_id`, `year`, `flow_status`, `is_delete`),
    CONSTRAINT `fk_perf_submission_perf` FOREIGN KEY (`perf_id`) REFERENCES `biz_performance` (`perf_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_perf_submission_year` FOREIGN KEY (`year_id`) REFERENCES `biz_performance_year` (`year_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效手动填报审核单';

CREATE TABLE IF NOT EXISTS `biz_performance_audit_snapshot` (
    `snapshot_id` bigint(20) NOT NULL AUTO_INCREMENT,
    `sub_id` bigint(20) NOT NULL COMMENT '绩效审核单ID',
    `perf_id` bigint(20) NOT NULL COMMENT '绩效指标ID',
    `year_id` bigint(20) NOT NULL COMMENT '绩效年度ID',
    `previous_performance_value` decimal(20,4) DEFAULT 0.0000 COMMENT '提交前总绩效完成值',
    `previous_performance_update_time` datetime DEFAULT NULL COMMENT '提交前总绩效更新时间',
    `previous_year_actual_value` decimal(20,4) DEFAULT 0.0000 COMMENT '提交前年度实际值',
    `previous_year_target_value` decimal(20,4) DEFAULT 0.0000 COMMENT '提交前年度目标值',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`snapshot_id`),
    UNIQUE KEY `uk_perf_snapshot_sub` (`sub_id`),
    CONSTRAINT `fk_perf_snapshot_submission` FOREIGN KEY (`sub_id`) REFERENCES `biz_performance_submission` (`sub_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效审核快照';

CREATE TABLE IF NOT EXISTS `biz_performance_audit_log` (
    `log_id` bigint(20) NOT NULL AUTO_INCREMENT,
    `sub_id` bigint(20) NOT NULL COMMENT '绩效审核单ID',
    `operator_id` bigint(20) NOT NULL COMMENT '操作人ID',
    `action_type` varchar(50) NOT NULL COMMENT '操作类型',
    `pre_status` int(11) DEFAULT NULL COMMENT '操作前状态',
    `post_status` int(11) DEFAULT NULL COMMENT '操作后状态',
    `comment` varchar(1000) DEFAULT NULL COMMENT '意见',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`log_id`),
    KEY `idx_perf_audit_log_sub` (`sub_id`),
    CONSTRAINT `fk_perf_audit_log_submission` FOREIGN KEY (`sub_id`) REFERENCES `biz_performance_submission` (`sub_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效审核日志';
