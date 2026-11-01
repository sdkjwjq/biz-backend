SELECT 'review_tables' check_name,COUNT(*) actual,5 expected FROM information_schema.TABLES
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('biz_review_round','biz_review_member','biz_review_scope','biz_round_opinion','biz_round_revision');
SELECT 'invalid_periods' check_name,COUNT(*) actual FROM biz_review_round WHERE starts_at>=ends_at;
SELECT 'ordinary_overlapping_members' check_name,COUNT(*) actual
FROM biz_review_member a JOIN biz_review_member b ON a.user_id=b.user_id AND a.round_id<b.round_id
JOIN biz_review_round r ON r.round_id=a.round_id JOIN biz_review_round s ON s.round_id=b.round_id
JOIN sys_user u ON u.user_id=a.user_id AND u.role<>'0'
WHERE a.active=1 AND b.active=1 AND r.starts_at<s.ends_at AND s.starts_at<r.ends_at;
SELECT 'invalid_formal_results' check_name,COUNT(*) actual FROM biz_round_revision WHERE review_result NOT IN ('pass','reject');
