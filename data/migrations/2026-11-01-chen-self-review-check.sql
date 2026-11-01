CREATE TABLE IF NOT EXISTS bak_20261101_chen_self_review LIKE biz_performance;
CREATE TABLE IF NOT EXISTS sys_chen_self_review_20261101(perf_id BIGINT PRIMARY KEY,perf_code VARCHAR(64),before_auditor BIGINT,after_auditor BIGINT,before_time DATETIME,after_time DATETIME,database_operator VARCHAR(288),create_time DATETIME(3),rolled_back_at DATETIME(3) NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DROP TEMPORARY TABLE IF EXISTS chen_targets;CREATE TEMPORARY TABLE chen_targets(id BIGINT PRIMARY KEY,code VARCHAR(64) COLLATE utf8mb4_bin);
INSERT INTO chen_targets VALUES (39,'1.1.30'),(40,'1.1.31'),(41,'1.1.32'),(42,'1.1.33'),(43,'1.1.34'),(44,'1.1.35'),(45,'1.1.36'),(72,'1.1.63'),(73,'1.1.64'),(74,'1.1.65'),(109,'1.2.32'),(110,'1.2.33'),(111,'1.2.34'),(112,'1.2.35'),(113,'1.2.36'),(138,'1.2.61'),(139,'1.2.62');
DROP TEMPORARY TABLE IF EXISTS chen_preview;
CREATE TEMPORARY TABLE chen_preview AS SELECT e.id,e.code,p.auditor_id before_auditor,p.update_time before_time,
CASE WHEN j.perf_id IS NOT NULL THEN 'ALREADY_RECORDED'
WHEN p.perf_id IS NULL OR COALESCE(p.is_delete,0)<>0 OR NOT(BINARY p.perf_code <=> BINARY e.code) THEN 'IDENTITY_CHANGED'
WHEN NOT(p.leader_id <=> 112120) OR (NOT(p.auditor_id <=> 110750) AND NOT(p.auditor_id <=> 112120)) THEN 'CONFIG_CHANGED'
WHEN NOT EXISTS(SELECT 1 FROM sys_user WHERE user_id=112120 AND BINARY nick_name=BINARY '陈奥林' AND status='1' AND COALESCE(is_delete,0)=0) THEN 'ACCOUNT_INVALID'
WHEN p.auditor_id <=> 112120 THEN 'ALREADY_CORRECT'
WHEN EXISTS(SELECT 1 FROM biz_performance_submission s WHERE s.perf_id=p.perf_id AND COALESCE(s.is_delete,0)=0 AND s.flow_status IN(10,20))
 OR EXISTS(SELECT 1 FROM biz_core_report r WHERE BINARY r.performance_code=BINARY p.perf_code AND r.flow_status IN(10,20)) THEN 'ACTIVE_AUDIT'
ELSE 'READY' END result
FROM chen_targets e LEFT JOIN biz_performance p ON p.perf_id=e.id LEFT JOIN sys_chen_self_review_20261101 j ON j.perf_id=e.id;
SELECT result,COUNT(*) row_count FROM chen_preview GROUP BY result;SELECT id,code,result FROM chen_preview WHERE result NOT IN('READY','ALREADY_CORRECT','ALREADY_RECORDED');
