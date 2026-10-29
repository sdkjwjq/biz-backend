-- Additive only: published content and recipient/read evidence are retained.
CREATE TABLE IF NOT EXISTS sys_platform_notice (
 notice_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 title VARCHAR(200) NOT NULL,content MEDIUMTEXT NOT NULL,important TINYINT NOT NULL DEFAULT 0,
 audience_type VARCHAR(20) NOT NULL,audience_json JSON NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',version BIGINT NOT NULL DEFAULT 1,
 revision_of BIGINT NULL,content_version INT NOT NULL DEFAULT 1,
 created_by BIGINT NOT NULL,updated_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),published_at DATETIME(3) NULL,
 expires_at DATETIME(3) NULL,withdrawn_at DATETIME(3) NULL,withdraw_reason VARCHAR(500) NULL,
 KEY idx_platform_notice_status(status,published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS sys_platform_notice_recipient (
 notice_id BIGINT NOT NULL,user_id BIGINT NOT NULL,user_name VARCHAR(120) NOT NULL,
 dept_id BIGINT NULL,dept_name VARCHAR(255) NULL,inbox_id BIGINT NULL,
 first_opened_at DATETIME(3) NULL,last_opened_at DATETIME(3) NULL,open_count INT NOT NULL DEFAULT 0,
 confirmed_at DATETIME(3) NULL,confirmed_version INT NULL,view_key VARCHAR(36) NULL,
 PRIMARY KEY(notice_id,user_id),KEY idx_platform_recipient_pending(user_id,confirmed_at,notice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS sys_platform_notice_view_log (
 log_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,notice_id BIGINT NOT NULL,user_id BIGINT NOT NULL,
 action VARCHAR(20) NOT NULL,request_key VARCHAR(64) NOT NULL,content_version INT NOT NULL,
 create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_platform_view_request(notice_id,user_id,action,request_key),
 KEY idx_platform_view_notice(notice_id,create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS sys_platform_notice_attachment (
 notice_id BIGINT NOT NULL,file_id BIGINT NOT NULL,PRIMARY KEY(notice_id,file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
