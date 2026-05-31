-- Add audit rollback snapshots for task material submissions.
-- Run this once on existing databases before deploying the audit-flow changes.

CREATE TABLE IF NOT EXISTS `biz_audit_snapshot` (
  `snapshot_id` bigint NOT NULL AUTO_INCREMENT COMMENT '快照ID',
  `sub_id` bigint NOT NULL COMMENT '提交ID',
  `target_type` varchar(20) NOT NULL COMMENT '快照对象类型：TASK/LEVEL4_TASK',
  `target_id` bigint NOT NULL COMMENT '快照对象ID',
  `previous_value` decimal(20,4) DEFAULT '0.0000' COMMENT '提交前完成值',
  `previous_status` char(1) DEFAULT NULL COMMENT '提交前状态',
  `previous_comment` varchar(500) DEFAULT NULL COMMENT '提交前任务备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`snapshot_id`),
  UNIQUE KEY `uk_snapshot_sub_target` (`sub_id`,`target_type`,`target_id`),
  KEY `idx_snapshot_sub_id` (`sub_id`),
  CONSTRAINT `fk_snapshot_submission`
    FOREIGN KEY (`sub_id`) REFERENCES `biz_material_submission` (`sub_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审核提交前数据快照表';
