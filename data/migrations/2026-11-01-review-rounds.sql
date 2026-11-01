-- Independent review rounds. Legacy review tables remain unchanged and read-only.
CREATE TABLE IF NOT EXISTS biz_review_round (
 round_id BIGINT PRIMARY KEY AUTO_INCREMENT,
 name VARCHAR(120) NOT NULL, review_year INT NULL COMMENT 'NULL covers 2025-2029',
 starts_at DATETIME(3) NOT NULL, ends_at DATETIME(3) NOT NULL,
 description VARCHAR(2000) NOT NULL DEFAULT '', published TINYINT NOT NULL DEFAULT 0,
 version BIGINT NOT NULL DEFAULT 0, create_by BIGINT NOT NULL, update_by BIGINT NOT NULL,
 create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 KEY ix_review_round_period(published,starts_at,ends_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS biz_review_member (
 round_id BIGINT NOT NULL,user_id BIGINT NOT NULL,active TINYINT NOT NULL DEFAULT 1,
 PRIMARY KEY(round_id,user_id),KEY ix_review_member_user(user_id,active,round_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS biz_review_scope (
 round_id BIGINT NOT NULL,user_id BIGINT NOT NULL,task_id BIGINT NOT NULL,
 PRIMARY KEY(round_id,user_id,task_id),KEY ix_review_scope_task(round_id,task_id,user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS biz_round_opinion (
 opinion_id BIGINT PRIMARY KEY AUTO_INCREMENT,round_id BIGINT NOT NULL,expert_id BIGINT NOT NULL,task_id BIGINT NOT NULL,
 draft_text VARCHAR(8000) NOT NULL DEFAULT '',draft_result VARCHAR(16) NULL,draft_scope JSON NOT NULL,
 version BIGINT NOT NULL DEFAULT 0,current_revision_id BIGINT NULL,update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_round_opinion(round_id,expert_id,task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS biz_round_revision (
 revision_id BIGINT PRIMARY KEY AUTO_INCREMENT,opinion_id BIGINT NOT NULL,revision_no BIGINT NOT NULL,
 round_id BIGINT NOT NULL,expert_id BIGINT NOT NULL,expert_name VARCHAR(100) NOT NULL,
 task_id BIGINT NOT NULL,task_code VARCHAR(100) NULL,task_name VARCHAR(2000) NULL,task_level INT NOT NULL,
 review_text VARCHAR(8000) NOT NULL,review_result VARCHAR(16) NOT NULL,scope_json JSON NOT NULL,
 operator_id BIGINT NOT NULL,create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_round_revision(opinion_id,revision_no),KEY ix_round_revision_lookup(round_id,expert_id,task_id,revision_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
