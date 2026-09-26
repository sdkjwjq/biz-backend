-- 管理中心统一操作日志。仅新增表，不修改现有业务数据。
CREATE TABLE IF NOT EXISTS `sys_admin_operation_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `module` varchar(30) NOT NULL COMMENT 'USER/DEPARTMENT/TASK等',
  `action` varchar(40) NOT NULL COMMENT '操作动作',
  `target_type` varchar(30) NOT NULL COMMENT '对象类型',
  `target_id` varchar(64) DEFAULT NULL COMMENT '对象ID',
  `target_name` varchar(500) DEFAULT NULL COMMENT '对象名称快照',
  `batch_id` varchar(40) DEFAULT NULL COMMENT '批次号',
  `operator_id` bigint NOT NULL COMMENT '管理员ID',
  `reason` varchar(500) DEFAULT NULL COMMENT '操作原因',
  `before_json` json DEFAULT NULL COMMENT '脱敏后的变更前内容',
  `after_json` json DEFAULT NULL COMMENT '脱敏后的变更后内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_admin_log_module_time` (`module`,`create_time`),
  KEY `idx_admin_log_operator_time` (`operator_id`,`create_time`),
  KEY `idx_admin_log_target` (`target_type`,`target_id`),
  KEY `idx_admin_log_batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员统一操作日志';
