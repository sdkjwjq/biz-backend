-- 2026-09-24 绩效关联任务编辑：关联行改为软删除留痕，并新增关联变更日志表。
-- 列添加用 information_schema 判断，可重复执行；表用 IF NOT EXISTS。
SET @exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'rel_task_performance' AND COLUMN_NAME = 'is_delete');
SET @ddl = IF(@exists = 0,
  'ALTER TABLE `rel_task_performance` ADD COLUMN `is_delete` tinyint(1) DEFAULT 0 COMMENT ''是否解除关联 0:正常 1:已解除''',
  'SELECT 1');
PREPARE release_stmt FROM @ddl;
EXECUTE release_stmt;
DEALLOCATE PREPARE release_stmt;

CREATE TABLE IF NOT EXISTS `biz_performance_relation_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `perf_id` bigint(20) NOT NULL COMMENT '绩效指标ID',
  `perf_code` varchar(64) DEFAULT NULL COMMENT '指标编码',
  `perf_name` varchar(500) DEFAULT NULL COMMENT '指标名称',
  `year` int(11) NOT NULL COMMENT '指标年度',
  `action` varchar(10) NOT NULL COMMENT 'ADD 新增关联 / REMOVE 解除关联',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `task_code` varchar(64) DEFAULT NULL COMMENT '任务编号',
  `task_name` varchar(500) DEFAULT NULL COMMENT '任务名称',
  `batch_id` varchar(40) DEFAULT NULL COMMENT '同一次批量提交的批次号',
  `operator_id` bigint(20) NOT NULL COMMENT '操作人ID',
  `reason` varchar(500) NOT NULL COMMENT '变更原因',
  `create_time` datetime NOT NULL COMMENT '变更时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_perf_relation_target` (`perf_id`, `year`),
  KEY `idx_perf_relation_operator` (`operator_id`, `create_time`),
  KEY `idx_perf_relation_batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效关联任务变更日志';
