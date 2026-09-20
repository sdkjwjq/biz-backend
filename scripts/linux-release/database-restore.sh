#!/usr/bin/env bash
# Sourced by update.sh. Restoring data is explicit; ordinary --update never imports it.
STAGE_DB=''
DB_REPLACED=0
restore_target_guard() {
  [[ "$DB_NAME" == biz || "$DB_NAME" =~ ^biz_review_test_[0-9a-f]{32}$ ]] || die '拒绝恢复非预期数据库'
}
blank_target_database() {
  restore_target_guard
  "${MYSQL_ADMIN[@]}" -e "DROP DATABASE IF EXISTS \`$DB_NAME\`; CREATE DATABASE \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
}
drop_stage_database() {
  [[ -n "$STAGE_DB" ]] || return 0
  [[ "$STAGE_DB" =~ ^biz_release_stage_[0-9_]+$ ]] || return 1
  "${MYSQL_ADMIN[@]}" -e "DROP DATABASE IF EXISTS \`$STAGE_DB\`;"
  STAGE_DB=''
}
restore_packaged_database() {
  restore_target_guard
  [[ -s "$PACKAGE/restore/20260920backup.sql" ]] || die '缺少指定恢复文件'
  STAGE_DB="biz_release_stage_$(date +%Y%m%d%H%M%S)_$$"
  "${MYSQL_ADMIN[@]}" -e "CREATE DATABASE \`$STAGE_DB\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
  # The assembler rejects database-selection statements in the supplied dump.
  "${MYSQL_ADMIN[@]}" "$STAGE_DB" < "$PACKAGE/restore/20260920backup.sql" > "$BACKUP/restore-stage.log"
  DB_NAME="$STAGE_DB" bash "$PACKAGE/schema-check.sh" before
  "${MYSQL_ADMIN[@]}" "$STAGE_DB" < "$PACKAGE/sql/additive.sql" >> "$BACKUP/restore-stage.log"
  DB_NAME="$STAGE_DB" bash "$PACKAGE/schema-check.sh" after
  mysqldump --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" --default-character-set=utf8mb4 \
    --single-transaction --routines --triggers --events --hex-blob --set-gtid-purged=OFF "$STAGE_DB" | gzip > "$BACKUP/restored-ready.sql.gz"
  gzip -t "$BACKUP/restored-ready.sql.gz"
  [[ $(stat -c %s "$BACKUP/restored-ready.sql.gz") -gt 100 ]] || die '恢复暂存数据异常'
  # Mark BEFORE the first destructive statement, so every partial failure can restore the fresh backup.
  DB_REPLACED=1
  printf 'restored\n' > "$BACKUP/database-replaced"
  blank_target_database
  gzip -dc "$BACKUP/restored-ready.sql.gz" | "${MYSQL[@]}"
  drop_stage_database
}
restore_saved_database() {
  [[ -s "$BACKUP/database.sql.gz" ]] || die '缺少更新前数据库备份'
  gzip -t "$BACKUP/database.sql.gz"
  blank_target_database
  gzip -dc "$BACKUP/database.sql.gz" | "${MYSQL[@]}"
}
