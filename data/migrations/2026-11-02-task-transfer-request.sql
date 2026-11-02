-- 2026-11-02：任务移交申请（接收人确认后生效）
CREATE TABLE IF NOT EXISTS biz_task_transfer_request (
  request_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  task_id BIGINT NOT NULL,
  field_type VARCHAR(16) NOT NULL COMMENT 'leader=专业群负责人 auditor=专业群审核人 principal=归口部门审核人',
  from_user BIGINT NOT NULL,
  to_user BIGINT NOT NULL,
  reason VARCHAR(500) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ACCEPTED/REJECTED',
  notice_id BIGINT NULL,
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  handle_time DATETIME(3) NULL,
  handle_remark VARCHAR(500) NULL,
  pending_slot TINYINT GENERATED ALWAYS AS (IF(status='PENDING',1,NULL)) STORED,
  UNIQUE KEY uq_transfer_pending (task_id,field_type,pending_slot),
  KEY idx_transfer_to (to_user, status),
  KEY idx_transfer_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务移交申请';

-- Upgrade an earlier draft of this table without changing existing applications.
SET @transfer_ddl=IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_task_transfer_request' AND COLUMN_NAME='pending_slot'),'SELECT 1','ALTER TABLE biz_task_transfer_request ADD COLUMN pending_slot TINYINT GENERATED ALWAYS AS (IF(status=''PENDING'',1,NULL)) STORED');
PREPARE transfer_stmt FROM @transfer_ddl; EXECUTE transfer_stmt; DEALLOCATE PREPARE transfer_stmt;
SET @transfer_ddl=IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_task_transfer_request' AND INDEX_NAME='uq_transfer_pending'),'SELECT 1','ALTER TABLE biz_task_transfer_request ADD UNIQUE KEY uq_transfer_pending(task_id,field_type,pending_slot)');
PREPARE transfer_stmt FROM @transfer_ddl; EXECUTE transfer_stmt; DEALLOCATE PREPARE transfer_stmt;
