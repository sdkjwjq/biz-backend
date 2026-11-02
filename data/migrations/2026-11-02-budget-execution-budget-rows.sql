-- Preserve original values; no business UPDATE or DELETE.
SET @budget_ddl=IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_task_item' AND COLUMN_NAME='budget_amount'),'SELECT 1',"ALTER TABLE biz_budget_task_item ADD COLUMN budget_amount DECIMAL(20,4) NULL DEFAULT 0.0000");
PREPARE budget_stmt FROM @budget_ddl; EXECUTE budget_stmt; DEALLOCATE PREPARE budget_stmt;
SET @budget_ddl=IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_task_item' AND COLUMN_NAME='capital_budget'),'SELECT 1',"ALTER TABLE biz_budget_task_item ADD COLUMN capital_budget DECIMAL(20,4) NULL DEFAULT 0.0000");
PREPARE budget_stmt FROM @budget_ddl; EXECUTE budget_stmt; DEALLOCATE PREPARE budget_stmt;
SET @budget_ddl=IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_task_item' AND COLUMN_NAME='other_budget'),'SELECT 1',"ALTER TABLE biz_budget_task_item ADD COLUMN other_budget DECIMAL(20,4) NULL DEFAULT 0.0000");
PREPARE budget_stmt FROM @budget_ddl; EXECUTE budget_stmt; DEALLOCATE PREPARE budget_stmt;
