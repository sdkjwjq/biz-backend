-- 工作纪实第一批。仅新增独立表，不改变任务、审核或账号数据。
CREATE TABLE biz_work_record (
  record_id BIGINT NOT NULL AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  owner_name VARCHAR(128) NOT NULL,
  record_year SMALLINT NOT NULL,
  record_month TINYINT NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1已提交',
  version BIGINT NOT NULL DEFAULT 0,
  problems VARCHAR(1200) NOT NULL DEFAULT '',
  next_focus VARCHAR(1200) NOT NULL DEFAULT '',
  other_matters VARCHAR(1200) NOT NULL DEFAULT '',
  create_time DATETIME(3) NOT NULL,
  update_time DATETIME(3) NOT NULL,
  submit_time DATETIME(3) NULL,
  PRIMARY KEY (record_id),
  UNIQUE KEY uk_work_record_month (owner_id, record_year, record_month),
  KEY idx_work_record_list (status, record_year, record_month, owner_id),
  CONSTRAINT chk_work_record_period CHECK (record_year BETWEEN 2025 AND 2029 AND record_month BETWEEN 1 AND 12),
  CONSTRAINT chk_work_record_status CHECK (status IN (0,1)),
  CONSTRAINT chk_work_record_version CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE biz_work_record_entry (
  entry_id BIGINT NOT NULL AUTO_INCREMENT,
  record_id BIGINT NOT NULL,
  reform_task_id BIGINT NOT NULL,
  reform_task_name VARCHAR(1000) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  key_progress VARCHAR(1200) NOT NULL DEFAULT '',
  stage_results VARCHAR(1200) NOT NULL DEFAULT '',
  typical_practices VARCHAR(1200) NOT NULL DEFAULT '',
  PRIMARY KEY (entry_id),
  UNIQUE KEY uk_work_record_reform (record_id, reform_task_id),
  CONSTRAINT fk_work_record_entry FOREIGN KEY (record_id) REFERENCES biz_work_record(record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE biz_work_record_snapshot (
  record_id BIGINT NOT NULL,
  schema_version INT NOT NULL DEFAULT 1,
  statistics_json LONGTEXT NOT NULL,
  generated_time DATETIME(3) NOT NULL,
  PRIMARY KEY (record_id),
  CONSTRAINT fk_work_record_snapshot FOREIGN KEY (record_id) REFERENCES biz_work_record(record_id),
  CONSTRAINT chk_work_record_snapshot_json CHECK (JSON_VALID(statistics_json))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
