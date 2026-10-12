-- 独立指标映射、完整填报历史及审核日志；不改原绩效值、不生成历史审核。
CREATE TABLE IF NOT EXISTS biz_core_indicator (
 indicator_id bigint NOT NULL, indicator_code varchar(30) NOT NULL, sort_order int NOT NULL,
 PRIMARY KEY(indicator_id), UNIQUE KEY uk_core_code(indicator_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO biz_core_indicator(indicator_id,indicator_code,sort_order) VALUES
(1,'1.1.5',1),(2,'1.1.7',2),(3,'1.1.55',3),(4,'1.1.14',4),(5,'1.1.20',5),
(6,'1.1.28',6),(7,'1.1.63',7),(8,'1.1.66',8),(9,'1.1.45',9),(10,'1.2.15',10);
CREATE TABLE IF NOT EXISTS biz_core_report (
 report_id bigint NOT NULL AUTO_INCREMENT, indicator_id bigint NOT NULL, record_year int NOT NULL, record_month int NOT NULL,
 draft_key varchar(100) NULL, source_draft_id bigint NULL,
 indicator_name varchar(500) NOT NULL, target_value decimal(20,4) NULL, data_type varchar(10) NULL,
 previous_year_value decimal(20,4) NULL, actual_value decimal(20,4) NULL, comment varchar(500) NOT NULL DEFAULT '',
 submit_by bigint NOT NULL, submit_name varchar(100) NULL, principal_id bigint NULL, archive_handler_id bigint NULL,
 current_handler_id bigint NULL, flow_status int NOT NULL DEFAULT 0, version bigint NOT NULL DEFAULT 0,
 create_time datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), update_time datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 submit_time datetime(3) NULL, archive_time datetime(3) NULL,
 PRIMARY KEY(report_id), UNIQUE KEY uk_core_draft(draft_key), UNIQUE KEY uk_core_source_draft(source_draft_id),
 KEY idx_core_period(indicator_id,record_year,record_month,flow_status), KEY idx_core_handler(current_handler_id,flow_status),
 KEY idx_core_owner(submit_by,flow_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS biz_core_audit_log (
 log_id bigint NOT NULL AUTO_INCREMENT, report_id bigint NOT NULL, operator_id bigint NOT NULL,
 action varchar(30) NOT NULL, pre_status int NOT NULL, post_status int NOT NULL, comment varchar(500) NOT NULL DEFAULT '',
 create_time datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), PRIMARY KEY(log_id), KEY idx_core_log_report(report_id,log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
