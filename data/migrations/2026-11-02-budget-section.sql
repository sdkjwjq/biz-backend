-- Incremental, repeatable section split; existing monthly sheets remain annual.
SET NAMES utf8mb4;
SET @budget_ddl=IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_sheet' AND COLUMN_NAME='section'),'SELECT 1',"ALTER TABLE biz_budget_sheet ADD COLUMN section VARCHAR(16) NOT NULL DEFAULT 'annual' COMMENT 'annual/execution' AFTER month");
PREPARE budget_stmt FROM @budget_ddl; EXECUTE budget_stmt; DEALLOCATE PREPARE budget_stmt;
SET @budget_ddl=IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_sheet' AND INDEX_NAME='uk_budget_sheet_year_month_section'),'SELECT 1','ALTER TABLE biz_budget_sheet ADD UNIQUE KEY uk_budget_sheet_year_month_section(year,month,section,is_delete)');
PREPARE budget_stmt FROM @budget_ddl; EXECUTE budget_stmt; DEALLOCATE PREPARE budget_stmt;
SET @budget_ddl=IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_sheet' AND INDEX_NAME='uk_budget_sheet_year_month'),'ALTER TABLE biz_budget_sheet DROP INDEX uk_budget_sheet_year_month','SELECT 1');
PREPARE budget_stmt FROM @budget_ddl; EXECUTE budget_stmt; DEALLOCATE PREPARE budget_stmt;
