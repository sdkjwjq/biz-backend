-- 仅新增查阅配置；不按姓名自动授权，不修改账号角色或业务数据。
CREATE TABLE IF NOT EXISTS sys_leadership_access (
  user_id BIGINT NOT NULL PRIMARY KEY,
  scope VARCHAR(16) NOT NULL DEFAULT 'NONE',
  version BIGINT NOT NULL DEFAULT 0,
  update_by BIGINT NULL,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 原趋势只有全校汇总，不能拆分历史部门数据；从部署后记录部门快照。
CREATE TABLE IF NOT EXISTS biz_department_trend (
  dept_id BIGINT NOT NULL,
  record_date DATE NOT NULL,
  total_tasks INT NOT NULL,
  completed_tasks INT NOT NULL,
  progress_sum DECIMAL(18,4) NOT NULL,
  PRIMARY KEY (dept_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS sys_leadership_department (
  user_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 编号由20260920线上备份核实；仅首次初始化，不覆盖管理员后续调整。
INSERT IGNORE INTO sys_leadership_access(user_id,scope,version)
SELECT user_id,'ALL',1 FROM sys_user
WHERE COALESCE(is_delete,0)=0
  AND ((user_id=110007 AND nick_name='鲍桂楠') OR (user_id=121552 AND nick_name='刘莹'));
