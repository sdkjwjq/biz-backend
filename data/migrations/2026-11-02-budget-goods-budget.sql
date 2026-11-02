-- Plan fields are read-only projections in current execution views.
SET @budget_ddl=IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_task_item' AND COLUMN_NAME='goods_budget'),'SELECT 1',"ALTER TABLE biz_budget_task_item ADD COLUMN goods_budget DECIMAL(20,4) NULL DEFAULT 0.0000");
PREPARE budget_stmt FROM @budget_ddl; EXECUTE budget_stmt; DEALLOCATE PREPARE budget_stmt;
