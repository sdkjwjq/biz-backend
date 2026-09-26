-- 预算版本迁移核对：全部查询均为只读。
SELECT 'active_budget_sheets' AS check_item, COUNT(*) AS check_value
FROM `biz_budget_sheet`
WHERE `is_delete` = 0
UNION ALL
SELECT 'versioned_budget_sheets', COUNT(DISTINCT `sheet_id`)
FROM `biz_budget_import_version`
WHERE `is_delete` = 0
UNION ALL
SELECT 'missing_version_sheets', COUNT(*)
FROM `biz_budget_sheet` s
WHERE s.`is_delete` = 0
  AND NOT EXISTS (
      SELECT 1 FROM `biz_budget_import_version` v
      WHERE v.`sheet_id` = s.`sheet_id` AND v.`is_delete` = 0
  )
UNION ALL
SELECT 'orphan_versions', COUNT(*)
FROM `biz_budget_import_version` v
LEFT JOIN `biz_budget_sheet` s ON s.`sheet_id` = v.`sheet_id` AND s.`is_delete` = 0
WHERE v.`is_delete` = 0 AND s.`sheet_id` IS NULL;

SELECT `year`, `month`, COUNT(*) AS version_count,
       MAX(`version_no`) AS latest_version_no,
       SUM(`operation_type` = 'INITIAL') AS initial_version_count,
       SUM(`operation_type` = 'IMPORT') AS import_version_count,
       SUM(`operation_type` = 'RESTORE') AS restore_version_count
FROM `biz_budget_import_version`
WHERE `is_delete` = 0
GROUP BY `year`, `month`
ORDER BY `year`, `month`;
