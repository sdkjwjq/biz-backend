-- 预算导入版本：保留每次导入/恢复的完整快照，不修改现有预算当前表及业务数据。
CREATE TABLE IF NOT EXISTS `biz_budget_import_version` (
    `version_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '版本ID',
    `sheet_id` bigint(20) NOT NULL COMMENT '当前预算表ID',
    `year` int(4) NOT NULL COMMENT '年度',
    `month` int(2) NOT NULL COMMENT '月份',
    `version_no` int(11) NOT NULL COMMENT '同一预算表内的版本号',
    `operation_type` varchar(20) NOT NULL COMMENT 'INITIAL/IMPORT/RESTORE',
    `source_version_id` bigint(20) DEFAULT NULL COMMENT '恢复操作所引用的历史版本ID',
    `file_id` bigint(20) DEFAULT NULL COMMENT '原始导入文件ID',
    `file_name` varchar(255) DEFAULT NULL COMMENT '原始导入文件名',
    `snapshot_json` json NOT NULL COMMENT '资金来源和任务预算完整快照',
    `created_by` bigint(20) DEFAULT NULL COMMENT '操作人ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `remark` varchar(200) DEFAULT NULL COMMENT '恢复原因等备注',
    `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0存在 1删除',
    PRIMARY KEY (`version_id`),
    UNIQUE KEY `uk_budget_version_sheet_no` (`sheet_id`, `version_no`),
    KEY `idx_budget_version_period` (`year`, `month`, `is_delete`, `version_no`),
    KEY `idx_budget_version_source` (`source_version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算导入及恢复版本快照';

-- 为升级前已有的当前预算建立只读初始版本。脚本重复执行不会重复写入。
INSERT INTO `biz_budget_import_version`
    (`sheet_id`, `year`, `month`, `version_no`, `operation_type`, `file_id`, `file_name`,
     `snapshot_json`, `created_by`, `create_time`, `remark`, `is_delete`)
SELECT s.`sheet_id`, s.`year`, s.`month`, 1, 'INITIAL', s.`file_id`, f.`file_name`,
       JSON_OBJECT(
           'sources', COALESCE((
               SELECT JSON_ARRAYAGG(JSON_OBJECT(
                   'itemId', i.`item_id`, 'sheetId', i.`sheet_id`, 'sourceKey', i.`source_key`,
                   'sourceName', i.`source_name`, 'displayOrder', i.`display_order`,
                   'fiveYearTotal', i.`five_year_total`, 'available', i.`available`,
                   'annualPlan', i.`annual_plan`, 'carryover', i.`carryover`, 'arrived', i.`arrived`
               )) FROM `biz_budget_source_item` i WHERE i.`sheet_id` = s.`sheet_id`
           ), JSON_ARRAY()),
           'tasks', COALESCE((
               SELECT JSON_ARRAYAGG(JSON_OBJECT(
                   'itemId', t.`item_id`, 'sheetId', t.`sheet_id`, 'taskIndex', t.`task_index`,
                   'taskName', t.`task_name`, 'amount', t.`amount`, 'goods', t.`goods`,
                   'capital', t.`capital`, 'other', t.`other`, 'coreOutput', t.`core_output`,
                   'targetValue', t.`target_value`, 'achieved', t.`achieved`
               )) FROM `biz_budget_task_item` t WHERE t.`sheet_id` = s.`sheet_id`
           ), JSON_ARRAY())
       ),
       s.`import_by`, COALESCE(s.`import_time`, s.`update_time`, NOW()), '系统升级时保留的当前版本', 0
FROM `biz_budget_sheet` s
LEFT JOIN `sys_file` f ON f.`file_id` = s.`file_id`
WHERE s.`is_delete` = 0
  AND NOT EXISTS (
      SELECT 1 FROM `biz_budget_import_version` v
      WHERE v.`sheet_id` = s.`sheet_id` AND v.`is_delete` = 0
  );
