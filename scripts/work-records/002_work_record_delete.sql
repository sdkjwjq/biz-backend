-- 仅增加删除元数据及调整月度唯一约束，不修改已有纪实正文或统计快照。
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_work_record' AND COLUMN_NAME='delete_marker')=0,
 'ALTER TABLE biz_work_record ADD COLUMN delete_marker BIGINT NOT NULL DEFAULT 0 COMMENT ''0有效，删除后为本记录ID''', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_work_record' AND COLUMN_NAME='deleted_by')=0,
 'ALTER TABLE biz_work_record ADD COLUMN deleted_by BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_work_record' AND COLUMN_NAME='deleted_time')=0,
 'ALTER TABLE biz_work_record ADD COLUMN deleted_time DATETIME(3) NULL', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_work_record' AND COLUMN_NAME='delete_reason')=0,
 'ALTER TABLE biz_work_record ADD COLUMN delete_reason VARCHAR(1200) NULL', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- 同一月份仍最多只有一份未删除记录，多个删除版本各自保留。
SET @old_index = (SELECT GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_work_record' AND INDEX_NAME='uk_work_record_month');
SET @ddl = IF(@old_index='owner_id,record_year,record_month',
 'ALTER TABLE biz_work_record DROP INDEX uk_work_record_month, ADD UNIQUE KEY uk_work_record_month(owner_id,record_year,record_month,delete_marker)',
 IF(@old_index IS NULL, 'ALTER TABLE biz_work_record ADD UNIQUE KEY uk_work_record_month(owner_id,record_year,record_month,delete_marker)', 'SELECT 1'));
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
