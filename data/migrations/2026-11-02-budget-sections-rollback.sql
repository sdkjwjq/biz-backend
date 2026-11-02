-- Stop backend writes and back up first. Refuse rollback while new execution data exists.
DROP PROCEDURE IF EXISTS rollback_budget_sections_20261102;
DELIMITER $$
CREATE PROCEDURE rollback_budget_sections_20261102()
BEGIN
 DECLARE n BIGINT DEFAULT 0;
 IF EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_sheet' AND COLUMN_NAME='section') THEN
  SELECT COUNT(*) INTO n FROM biz_budget_sheet WHERE section<>'annual' OR section IS NULL;
  IF n>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Execution budget data exists; preserve data and refuse schema rollback'; END IF;
 END IF;
 IF EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_task_item' AND COLUMN_NAME='budget_amount') THEN
  SELECT COUNT(*) INTO n FROM biz_budget_task_item WHERE budget_amount IS NOT NULL AND budget_amount<>0;
  IF n>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='New budget values exist; schema rollback refused'; END IF;
 END IF;
 IF EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_task_item' AND COLUMN_NAME='goods_budget') THEN
  SELECT COUNT(*) INTO n FROM biz_budget_task_item WHERE goods_budget IS NOT NULL AND goods_budget<>0;
  IF n>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='New budget values exist; schema rollback refused'; END IF;
 END IF;
 IF EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_task_item' AND COLUMN_NAME='capital_budget') THEN
  SELECT COUNT(*) INTO n FROM biz_budget_task_item WHERE capital_budget IS NOT NULL AND capital_budget<>0;
  IF n>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='New budget values exist; schema rollback refused'; END IF;
 END IF;
 IF EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_task_item' AND COLUMN_NAME='other_budget') THEN
  SELECT COUNT(*) INTO n FROM biz_budget_task_item WHERE other_budget IS NOT NULL AND other_budget<>0;
  IF n>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='New budget values exist; schema rollback refused'; END IF;
 END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_sheet' AND INDEX_NAME='uk_budget_sheet_year_month') THEN
  ALTER TABLE biz_budget_sheet ADD UNIQUE KEY uk_budget_sheet_year_month(year,month,is_delete);
 END IF;
 IF EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_sheet' AND INDEX_NAME='uk_budget_sheet_year_month_section') THEN
  ALTER TABLE biz_budget_sheet DROP INDEX uk_budget_sheet_year_month_section;
 END IF;
 IF EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_task_item' AND COLUMN_NAME='budget_amount') THEN ALTER TABLE biz_budget_task_item DROP COLUMN budget_amount; END IF;
 IF EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_task_item' AND COLUMN_NAME='goods_budget') THEN ALTER TABLE biz_budget_task_item DROP COLUMN goods_budget; END IF;
 IF EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_task_item' AND COLUMN_NAME='capital_budget') THEN ALTER TABLE biz_budget_task_item DROP COLUMN capital_budget; END IF;
 IF EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_task_item' AND COLUMN_NAME='other_budget') THEN ALTER TABLE biz_budget_task_item DROP COLUMN other_budget; END IF;
 IF EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_budget_sheet' AND COLUMN_NAME='section') THEN ALTER TABLE biz_budget_sheet DROP COLUMN section; END IF;
END$$
DELIMITER ;
CALL rollback_budget_sections_20261102();
DROP PROCEDURE rollback_budget_sections_20261102;
