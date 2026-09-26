-- 先回滚应用并备份数据库。存在新导入或恢复版本时停止，防止丢失审计记录。
DELIMITER //
DROP PROCEDURE IF EXISTS `rollback_budget_import_version_20260926`//
CREATE PROCEDURE `rollback_budget_import_version_20260926`()
BEGIN
    DECLARE used_count bigint DEFAULT 0;
    SELECT COUNT(*) INTO used_count
    FROM `biz_budget_import_version`
    WHERE `operation_type` <> 'INITIAL' AND `is_delete` = 0;
    IF used_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Budget import versions exist; preserve audit history and stop rollback';
    END IF;
    DROP TABLE `biz_budget_import_version`;
END//
CALL `rollback_budget_import_version_20260926`()//
DROP PROCEDURE `rollback_budget_import_version_20260926`//
DELIMITER ;
