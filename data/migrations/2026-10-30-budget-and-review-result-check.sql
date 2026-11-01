SELECT 'review_result_columns' check_name,COUNT(*) actual,2 expected
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
AND TABLE_NAME IN ('biz_expert_review','biz_expert_review_revision') AND COLUMN_NAME='review_result';
SELECT 'total_budget_cells' check_name,COUNT(*) actual FROM biz_budget_total;
SELECT 'unexpected_review_result' check_name,COUNT(*) actual FROM biz_expert_review_revision
WHERE review_result IS NOT NULL AND review_result NOT IN ('','pass','reject');
