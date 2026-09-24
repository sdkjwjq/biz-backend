-- 2026-09-24 管理员归属移交：记录任务/绩效的归口部门、责任人、归口审核人变更。
CREATE TABLE IF NOT EXISTS `biz_ownership_change_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `target_type` varchar(20) NOT NULL COMMENT '对象类型 TASK / PERFORMANCE',
  `target_id` bigint(20) NOT NULL COMMENT '任务ID或绩效指标ID',
  `target_code` varchar(64) DEFAULT NULL COMMENT '任务编号或绩效编码',
  `target_name` varchar(500) DEFAULT NULL COMMENT '任务名称或绩效名称',
  `batch_id` varchar(40) DEFAULT NULL COMMENT '同一次批量移交的批次号',
  `operator_id` bigint(20) NOT NULL COMMENT '操作人ID',
  `reason` varchar(500) NOT NULL COMMENT '移交原因',
  `dept_before_id` bigint(20) DEFAULT NULL COMMENT '变更前归口部门ID',
  `dept_after_id` bigint(20) DEFAULT NULL COMMENT '变更后归口部门ID',
  `leader_before_id` bigint(20) DEFAULT NULL COMMENT '变更前责任人ID',
  `leader_after_id` bigint(20) DEFAULT NULL COMMENT '变更后责任人ID',
  `principal_before_id` bigint(20) DEFAULT NULL COMMENT '变更前归口审核人ID',
  `principal_after_id` bigint(20) DEFAULT NULL COMMENT '变更后归口审核人ID',
  `create_time` datetime NOT NULL COMMENT '变更时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_ownership_target` (`target_type`,`target_id`),
  KEY `idx_ownership_operator` (`operator_id`,`create_time`),
  KEY `idx_ownership_batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='归属变更日志';
