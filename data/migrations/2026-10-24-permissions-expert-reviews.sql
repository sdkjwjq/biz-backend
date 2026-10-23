-- 只增加授权和专家评审数据，不修改现有任务、填报、审核或账号身份。
-- 内置角色定义保存在代码中；本表保存自定义角色及内置模板的显式调整。
CREATE TABLE IF NOT EXISTS sys_permission_role (
 role_code varchar(64) NOT NULL PRIMARY KEY, role_name varchar(100) NOT NULL,
 permissions_json longtext NOT NULL, scopes_json longtext NOT NULL,
 enabled tinyint NOT NULL DEFAULT 1, version bigint NOT NULL DEFAULT 0,
 create_time datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 update_time datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), update_by bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS sys_user_permission (
 user_id bigint NOT NULL PRIMARY KEY, access_mode varchar(12) NOT NULL DEFAULT 'LEGACY',
 permissions_json longtext NOT NULL, scopes_json longtext NOT NULL,
 version bigint NOT NULL DEFAULT 0, update_time datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), update_by bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS sys_user_permission_role (
 user_id bigint NOT NULL, role_code varchar(64) NOT NULL, PRIMARY KEY(user_id,role_code), KEY(role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS sys_permission_revision (
 revision_id int NOT NULL PRIMARY KEY, version bigint NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO sys_permission_revision(revision_id,version) VALUES(1,0);
-- LEGACY 保留动态业务资格，不将当下负责人名单冻结成永久授权。
INSERT IGNORE INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json)
 SELECT user_id,'LEGACY','{}','{}' FROM sys_user WHERE COALESCE(is_delete,0)=0;
CREATE TABLE IF NOT EXISTS biz_expert_review (
 review_id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY, expert_id bigint NOT NULL, task_id bigint NOT NULL,
 review_year int NOT NULL, task_level int NOT NULL, draft_text text NOT NULL,
 current_revision_id bigint DEFAULT NULL, version bigint NOT NULL DEFAULT 0,
 create_time datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), update_time datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_expert_task(expert_id,task_id), KEY(task_id,review_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS biz_expert_review_revision (
 revision_id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY, review_id bigint NOT NULL, revision_no bigint NOT NULL,
 expert_id bigint NOT NULL, expert_name varchar(100) NOT NULL, task_id bigint NOT NULL,
 task_code varchar(100) DEFAULT NULL, task_name varchar(500) NOT NULL, task_level int NOT NULL, review_year int NOT NULL,
 review_text text NOT NULL, scope_json longtext NOT NULL,
 operator_id bigint NOT NULL, operator_name varchar(100) NOT NULL, delegated tinyint NOT NULL DEFAULT 0,
 create_time datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), UNIQUE KEY uk_review_revision(review_id,revision_no),
 KEY(expert_id,review_year), KEY(task_id,review_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
