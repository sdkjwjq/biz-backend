-- 仅用于随机 biz_review_test_* 隔离库。2026 年趋势保持为空，用于空数据检查。
INSERT INTO biz_trend_data (year, month, day, total_tasks, completion_count, completion_rate)
VALUES (2025, 1, 1, 100, 77, 77), (2027, 1, 1, 100, 33, 33);

INSERT INTO biz_budget_sheet (sheet_id, year, month, locked, is_delete)
VALUES (989001, 2026, 1, 1, 0), (989002, 2026, 2, 0, 0);
INSERT INTO biz_budget_source_item
(sheet_id, source_key, source_name, display_order, five_year_total, available, annual_plan, carryover, arrived)
VALUES (989001, 'audit', '审计资金来源', 1, 111, 111, 111, 0, 111),
       (989002, 'audit', '审计资金来源', 1, 222, 222, 222, 0, 222);
