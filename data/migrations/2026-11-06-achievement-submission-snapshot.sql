-- New submissions retain the exact achievement contents and attachment at submit time.
-- Existing submissions are intentionally not backfilled: their historical text is unknown.
CREATE TABLE IF NOT EXISTS biz_achievement_submission_snapshot (
  sub_id BIGINT NOT NULL PRIMARY KEY,
  ach_id BIGINT NOT NULL,
  category INT NULL,
  level VARCHAR(20) NULL,
  ach_name VARCHAR(255) NULL,
  department VARCHAR(255) NULL,
  got_time DATETIME NULL,
  dept_id BIGINT NULL,
  file_id BIGINT NULL,
  achievement_comment VARCHAR(500) NULL,
  is_competition TINYINT NULL,
  te_deng_jiang INT NULL,
  yi_deng_jiang INT NULL,
  er_deng_jiang INT NULL,
  san_deng_jiang INT NULL,
  jin_jiang INT NULL,
  yin_jiang INT NULL,
  tong_jiang INT NULL,
  you_sheng_jiang INT NULL,
  bud_deng_deng_ci INT NULL,
  captured_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY idx_achievement_snapshot_ach_id (ach_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
