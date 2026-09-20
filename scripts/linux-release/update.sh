#!/usr/bin/env bash
# Linux / Bash 4.2+; data restoration requires explicit --restore-backup.
set -Eeuo pipefail
umask 077
PACKAGE=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
BACKEND=/root/biz-backend
WEB=/usr/share/nginx/html
JAR=biz_backend-1.0-SNAPSHOT.jar
DB_HOST=127.0.0.1
DB_PORT=3306
DB_USER=root
DB_NAME=biz
PORT=8080
BACKUP=''
CHANGED=0
FINISHED=0
OLD_PID=''
NEW_PID=''
JAVA_BIN=''
OLD_ARGS=()
NEW_ARGS=()
RESTORE_BACKUP=0
NOTICE_CLEANUP=0
die() { printf '错误：%s\n' "$*" >&2; exit 1; }
say() { printf '[更新] %s\n' "$*"; }
need() { command -v "$1" >/dev/null || die "缺少命令 $1；尚未更新程序"; }
source "$PACKAGE/database-restore.sh"

database_credentials() {
  if [[ -n ${MYSQL_PWD:-} ]]; then DB_PASSWORD=$MYSQL_PWD
  else read -r -s -p '请输入服务器 MySQL root 密码（不回显）：' DB_PASSWORD < /dev/tty; printf '\n'; fi
  [[ -n "$DB_PASSWORD" ]] || die '数据库密码不能为空'
  export MYSQL_PWD=$DB_PASSWORD
  MYSQL_ADMIN=(mysql --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" --default-character-set=utf8mb4 --batch --skip-column-names)
  MYSQL=("${MYSQL_ADMIN[@]}" "$DB_NAME")
}

find_app() {
  local proc arg jar_path found count=0 cwd i
  APP_PID=''
  for proc in /proc/[0-9]*; do
    [[ -r "$proc/cmdline" ]] || continue
    cwd=$(readlink -f "$proc/cwd" 2>/dev/null) || continue
    [[ "$cwd" == "$BACKEND" ]] || continue
    local argv=()
    while IFS= read -r -d '' arg; do argv+=("$arg"); done < "$proc/cmdline" || true
    found=0
    for ((i=0;i<${#argv[@]}-1;i++)); do
      if [[ "${argv[i]}" == -jar ]]; then
        jar_path=${argv[i+1]}
        [[ "$jar_path" == /* ]] || jar_path="$cwd/$jar_path"
        [[ "$(readlink -f "$jar_path")" == "$BACKEND/$JAR" ]] && found=1
      fi
    done
    if [[ $found == 1 ]]; then APP_PID=${proc##*/}; count=$((count+1)); fi
  done
  [[ $count -le 1 ]] || die '发现多个目标后端进程，请先核实；未停止任何进程'
}

stop_app() {
  find_app
  [[ -n "$APP_PID" ]] || return 0
  say "停止目标后端 PID=$APP_PID（仅发送 TERM，最多等待 90 秒）"
  kill -TERM "$APP_PID"
  local n
  for ((n=0;n<90;n++)); do
    if ! kill -0 "$APP_PID" 2>/dev/null; then return 0; fi
    sleep 1
  done
  die '后端未正常停止，未强杀；请查看进程后重试'
}

start_old() {
  (cd "$BACKEND"; unset MYSQL_PWD; nohup "$JAVA_BIN" "${OLD_ARGS[@]}" >> "$BACKEND/logs/release-startup.log" 2>&1 < /dev/null 9>&- & echo $! > "$BACKUP/restarted.pid")
}

health() {
  local n response
  for ((n=0;n<90;n++)); do
    # Account -1 cannot log in. This exercises the application's real database lookup.
    response=$(curl --noproxy '*' -s --max-time 3 -H 'Content-Type: application/json' \
      --data '{"user_id":-1,"password":"release-connectivity-probe"}' "http://127.0.0.1:$PORT/api/system/login" || true)
    if [[ "$response" == *'用户不存在'* ]]; then
      find_app
      [[ -n "$APP_PID" ]] && return 0
    fi
    sleep 1
  done
  return 1
}

restore_files() {
  stop_app
  cp -p "$BACKUP/old.jar" "$BACKEND/$JAR.restore"
  mv -f "$BACKEND/$JAR.restore" "$BACKEND/$JAR"
  if [[ -f "$BACKUP/old-release.properties" ]]; then
    cp -p "$BACKUP/old-release.properties" "$BACKEND/.release.properties"
  else
    rm -f -- "$BACKEND/.release.properties"
  fi
  # Restore previous files; keep any new hashed assets and all uploaded files.
  tar -xzf "$BACKUP/frontend.tar.gz" -C "$WEB"
  start_old
  health || { say '旧程序恢复后仍未通过检查，请查看 logs/release-startup.log'; return 1; }
}

cleanup() {
  local code=$?
  trap - EXIT INT TERM
  if [[ $CHANGED == 1 && $FINISHED == 0 ]]; then
    say "更新未完成，恢复旧程序和前端。备份：$BACKUP"
    set +e
    if [[ $NOTICE_CLEANUP == 1 && $DB_REPLACED == 0 ]]; then
      "${MYSQL[@]}" < "$BACKUP/restore-notice-flags.sql" || say '通知删除标记恢复失败，可使用备份目录中的 restore-notice-flags.sql 单独恢复'
    fi
    if [[ $DB_REPLACED == 1 ]]; then
      say '数据库恢复步骤已开始，先恢复本次更新前的完整数据库备份'
      (set -Eeuo pipefail; stop_app; restore_saved_database)
      if [[ $? != 0 ]]; then
        say "数据库自动恢复失败，后端保持停止。保留备份并执行 bash update.sh --rollback-database '$BACKUP'"
        unset MYSQL_PWD DB_PASSWORD; exit 1
      fi
    fi
    (set -Eeuo pipefail; restore_files)
    local restored=$?
    if [[ $restored == 0 ]]; then say '旧程序已恢复'
    elif [[ $DB_REPLACED == 1 ]]; then say "自动恢复失败，请执行 bash update.sh --rollback-database '$BACKUP'"
    else say "自动恢复失败，请执行 bash update.sh --rollback '$BACKUP'"; fi
    code=1
  fi
  if [[ -n "$STAGE_DB" ]]; then drop_stage_database || say "暂存库未清理：$STAGE_DB"; fi
  unset MYSQL_PWD DB_PASSWORD
  exit "$code"
}

[[ $EUID == 0 ]] || die '请使用 root 运行'
for command in bash readlink mysql mysqldump gzip tar sha256sum curl flock cp mv find awk sed sort comm stat cmp mktemp du df; do need "$command"; done
[[ "$(readlink -f "$BACKEND")" == "$BACKEND" && "$(readlink -f "$WEB")" == "$WEB" ]] || die '部署目录不存在或是符号链接，请先核实'
exec 9> "$BACKEND/.release.lock"
flock -n 9 || die '另一更新/回滚正在运行'
MODE=${1:---update}
if [[ "$MODE" == --rollback || "$MODE" == --rollback-database ]]; then
  [[ $# == 2 ]] || die '用法：bash update.sh --rollback /root/biz-backend/backups/备份目录'
  BACKUP=$(readlink -f "$2")
  [[ "$BACKUP" == "$BACKEND"/backups/release-* && -f "$BACKUP/state.sh" && -f "$BACKUP/old.jar" ]] || die '不是本脚本生成的完整备份目录'
  [[ "$(stat -c %u "$BACKUP/state.sh")" == 0 ]] || die '备份配置必须属于 root'
  # This file is generated with Bash %q, contains only saved executable/argv, and is root-only.
  source "$BACKUP/state.sh"
  if [[ "$MODE" == --rollback-database ]]; then
    database_credentials
    gzip -t "$BACKUP/database.sql.gz" || die '更新前数据库备份损坏，未停止服务'
    say '先保留当前数据库，再恢复本次更新前数据库及应用；更新后数据保留在额外备份中'
    stop_app
    rollback_copy="$BACKUP/before-rollback-$(date +%Y%m%d-%H%M%S)-$$.sql.gz"
    database_exists=$("${MYSQL_ADMIN[@]}" -e "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='$DB_NAME'")
    if [[ "$database_exists" == 1 ]]; then
      mysqldump --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" --single-transaction --routines --triggers --events --hex-blob --set-gtid-purged=OFF "$DB_NAME" | gzip > "$rollback_copy"
      gzip -t "$rollback_copy"
    else say '当前业务库不存在，从已校验的更新前备份恢复'; fi
    restore_saved_database
  else
    [[ ! -f "$BACKUP/database-replaced" ]] || die '该更新恢复过数据库，请使用 --rollback-database 备份目录，不能只回滚应用'
    database_credentials
    has_marker=$("${MYSQL[@]}" -e "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='biz_work_record' AND COLUMN_NAME='delete_marker'")
    if [[ "$has_marker" == 1 ]]; then
      archived=$("${MYSQL[@]}" -e 'SELECT COUNT(*) FROM biz_work_record WHERE delete_marker<>0')
      [[ "$archived" == 0 ]] || die '存在逻辑删除纪实，禁止直接恢复可能不识别删除标记的旧应用'
    fi
  fi
  say '恢复备份的程序和前端，上传材料保持不变'
  restore_files
  say '回滚完成'
  exit 0
fi
[[ "$MODE" == --check || "$MODE" == --update ]] || die '支持 --check、--update [--restore-backup]、--rollback 或 --rollback-database 备份目录'
if [[ $# == 2 && "$2" == --restore-backup && "$MODE" == --update ]]; then RESTORE_BACKUP=1
else [[ $# -le 1 ]] || die '仅 --update 可搭配 --restore-backup'; fi
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM
(cd "$PACKAGE"; sha256sum -c SHA256SUMS --quiet) || die '更新包文件校验失败'
[[ -f "$BACKEND/$JAR" && -f "$WEB/index.html" ]] || die '当前 JAR 或 index.html 不存在'
[[ -d "$BACKEND/uploads" ]] || die '原 uploads 目录不存在，请核实原上传材料位置'
find_app
[[ -n "$APP_PID" ]] || die '未找到运行目录和 JAR 都匹配的后端进程；不猜测启动参数'
OLD_PID=$APP_PID
JAVA_BIN=$(readlink -f "/proc/$OLD_PID/exe")
while IFS= read -r -d '' arg; do OLD_ARGS+=("$arg"); done < "/proc/$OLD_PID/cmdline"
OLD_ARGS=("${OLD_ARGS[@]:1}")
for arg in "${OLD_ARGS[@]}"; do
  case "$arg" in
    --spring.config.additional-location=file:/root/biz-backend/.release.properties|--server.port=8080) continue ;;
    --server.port|--server.port=*) die '现有端口参数与固定部署配置不符，请先核对' ;;
    *spring.datasource.*|*spring.config.*|*spring.application.json*) die '现有进程有显式数据库/配置参数，需核对后定制脚本；未修改系统' ;;
  esac
  NEW_ARGS+=("$arg")
done
"$JAVA_BIN" -version 2>&1 | head -n 1 | grep -Eq 'version "(17|18|19|2[0-9])\.' || die '需要 Java 17 或更新版本'
if command -v nginx >/dev/null; then nginx -t || die '现有 nginx 配置检查未通过'; fi
say "目标：$BACKEND/$JAR；前端：$WEB；数据库：$DB_HOST:$DB_PORT/$DB_NAME"
database_credentials
restore_digest=$(sha256sum "$PACKAGE/restore/20260920backup.sql" | awk '{print $1}')
RESTORE_MARKER="$BACKEND/.restored-$restore_digest"
if [[ $RESTORE_BACKUP == 1 ]]; then
  [[ ! -f "$RESTORE_MARKER" ]] || die '该备份已成功恢复过；再次更新请使用 --update，避免重复覆盖新数据'
  say '本次按指定要求恢复 20260920backup.sql；备份后数据库将回到该时点，之后数据仅保留在更新前备份中'
fi
version=$("${MYSQL[@]}" -e 'SELECT VERSION()')
[[ "$version" == 8.* ]] || die '当前迁移包要求 MySQL 8'
"${MYSQL[@]}" -e 'SELECT 1' >/dev/null
export DB_HOST DB_PORT DB_USER DB_NAME
if [[ $RESTORE_BACKUP == 0 ]]; then bash "$PACKAGE/schema-check.sh" before; fi
db_kb=$("${MYSQL[@]}" -e 'SELECT CEIL(COALESCE(SUM(DATA_LENGTH+INDEX_LENGTH),0)/1024) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE()')
files_kb=$(du -sk "$BACKEND/uploads" "$WEB" "$BACKEND/$JAR" | awk '{sum+=$1} END {print sum}')
free_kb=$(df -Pk "$BACKEND" | awk 'NR==2 {print $4}')
required_kb=$((db_kb*2 + files_kb*2 + 524288))
[[ $free_kb -gt $required_kb ]] || die '备份分区可用空间不足（预留数据库/文件备份及恢复空间）；未停止服务'
web_free_kb=$(df -Pk "$WEB" | awk 'NR==2 {print $4}')
web_required_kb=$(du -sk "$PACKAGE/frontend" | awk '{print $1+65536}')
[[ $web_free_kb -gt $web_required_kb ]] || die '前端分区空间不足；未停止服务'
if [[ $RESTORE_BACKUP == 1 ]]; then
  mysql_data_dir=$("${MYSQL[@]}" -e 'SELECT @@datadir')
  [[ -d "$mysql_data_dir" ]] || die '无法检查 MySQL 数据分区空间'
  mysql_free_kb=$(df -Pk "$mysql_data_dir" | awk 'NR==2 {print $4}')
  restore_kb=$(du -k "$PACKAGE/restore/20260920backup.sql" | awk '{print $1}')
  [[ $mysql_free_kb -gt $((db_kb + restore_kb*8 + 524288)) ]] || die 'MySQL 数据分区空间不足以建立恢复暂存库'
fi
[[ "$MODE" == --update ]] || { say '只读检查通过；尚未停止服务或修改数据库'; FINISHED=1; exit 0; }

BACKUP="$BACKEND/backups/release-$(date +%Y%m%d-%H%M%S)-$$"
mkdir -p "$BACKUP" "$BACKEND/logs"
chmod 700 "$BACKUP"
{ printf 'JAVA_BIN=%q\n' "$JAVA_BIN"; printf 'OLD_ARGS=('; printf ' %q' "${OLD_ARGS[@]}"; printf ' )\n'; } > "$BACKUP/state.sh"
cp -p "$BACKEND/$JAR" "$BACKUP/old.jar"
[[ ! -f "$BACKEND/.release.properties" ]] || cp -p "$BACKEND/.release.properties" "$BACKUP/old-release.properties"
tar -czf "$BACKUP/frontend.tar.gz" -C "$WEB" .
gzip -t "$BACKUP/frontend.tar.gz"
say "程序和前端备份完成，停止写入后备份数据库与上传材料：$BACKUP"
CHANGED=1
stop_app
mysqldump --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" --default-character-set=utf8mb4 \
  --single-transaction --routines --triggers --events --hex-blob --set-gtid-purged=OFF "$DB_NAME" | gzip > "$BACKUP/database.sql.gz"
gzip -t "$BACKUP/database.sql.gz"
[[ $(stat -c %s "$BACKUP/database.sql.gz") -gt 100 ]] || die '数据库备份文件异常'
tar -czf "$BACKUP/uploads.tar.gz" -C "$BACKEND" uploads
gzip -t "$BACKUP/uploads.tar.gz"
if [[ $RESTORE_BACKUP == 1 ]]; then
  say '备份完成，先在暂存库验证指定备份和迁移，再恢复业务库'
  restore_packaged_database
else
  say '备份完成，仅补充缺失表/字段；不修改已有业务数据'
fi
"${MYSQL[@]}" < "$PACKAGE/sql/additive.sql" > "$BACKUP/migration.log"
bash "$PACKAGE/schema-check.sh" after

# The backend is stopped and the complete database has already been backed up.
# Retire review requests only; preserve completed-result messages and audit history.
"${MYSQL[@]}" < "$PACKAGE/sql/backup-stale-notices.sql" > "$BACKUP/restore-notice-flags.sql"
NOTICE_CLEANUP=1
"${MYSQL[@]}" < "$PACKAGE/sql/retire-stale-notices.sql" > "$BACKUP/notice-cleanup.log"
say "失效待审核通知已逻辑删除 $(tr -d '\r\n' < "$BACKUP/notice-cleanup.log") 条；审核结果通知及业务记录保留"

# A root-readable properties file, not a password in command-line arguments or in the distributable JAR.
escaped_password=$(printf '%s' "$DB_PASSWORD" | sed -e 's/\\/\\\\/g' -e 's/ /\\ /g')
{
  printf 'spring.datasource.url=jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&serverTimezone=Asia/Shanghai\n' "$DB_HOST" "$DB_PORT" "$DB_NAME"
  printf 'spring.datasource.username=%s\nspring.datasource.password=%s\n' "$DB_USER" "$escaped_password"
} > "$BACKEND/.release.properties.new"
chmod 600 "$BACKEND/.release.properties.new"
mv -f "$BACKEND/.release.properties.new" "$BACKEND/.release.properties"
unset DB_PASSWORD escaped_password
cp "$PACKAGE/backend/$JAR" "$BACKEND/$JAR.new"
chmod 644 "$BACKEND/$JAR.new"
mv -f "$BACKEND/$JAR.new" "$BACKEND/$JAR"
(cd "$BACKEND"; unset MYSQL_PWD; nohup "$JAVA_BIN" "${NEW_ARGS[@]}" \
  --spring.config.additional-location="file:$BACKEND/.release.properties" --server.port="$PORT" \
  >> "$BACKEND/logs/release-startup.log" 2>&1 < /dev/null 9>&- & echo $! > "$BACKUP/new.pid")
NEW_PID=$(cat "$BACKUP/new.pid")
say "新后端启动 PID=$NEW_PID，等待数据库连通检查"
health || die '新后端未通过检查，将自动恢复旧程序'
[[ "$APP_PID" == "$NEW_PID" ]] || die '检查到的进程与本次启动进程不一致'

# Do not remove old hashed assets, uploads, .well-known or unrelated server files.
while IFS= read -r -d '' directory; do
  relative=${directory#"$PACKAGE/frontend"}
  if [[ ! -d "$WEB$relative" ]]; then mkdir -p "$WEB$relative"; chmod 755 "$WEB$relative"; fi
done < <(find "$PACKAGE/frontend" -type d -print0)
while IFS= read -r -d '' file; do
  relative=${file#"$PACKAGE/frontend/"}
  [[ "$relative" == index.html ]] && continue
  destination="$WEB/$relative"
  mkdir -p "$(dirname "$destination")"
  cp "$file" "$destination.release-new"
  chmod 644 "$destination.release-new"
  mv -f "$destination.release-new" "$destination"
done < <(find "$PACKAGE/frontend" -type f -print0)
cp "$PACKAGE/frontend/index.html" "$WEB/index.html.release-new"
chmod 644 "$WEB/index.html.release-new"
mv -f "$WEB/index.html.release-new" "$WEB/index.html"
# Compatibility copy only when absent; never replace an existing uploaded template.
if [[ ! -e "$BACKEND/uploads/budget-template.xlsx" ]]; then
  cp "$PACKAGE/templates/budget-template.xlsx" "$BACKEND/uploads/budget-template.xlsx"
fi
cmp "$PACKAGE/frontend/index.html" "$WEB/index.html" || die '前端入口校验失败'
if [[ $RESTORE_BACKUP == 1 ]]; then printf '%s\n' "$BACKUP" > "$RESTORE_MARKER"; fi
FINISHED=1
say "更新成功。备份：$BACKUP"
say "访问 http://172.19.2.81/ 并刷新浏览器。前端模板位于 $WEB/templates；纪实 Word 模板内置 JAR。"
if [[ $RESTORE_BACKUP == 1 ]]; then say "回滚命令（数据库及应用）：bash '$PACKAGE/update.sh' --rollback-database '$BACKUP'"
else say "回滚命令：bash '$PACKAGE/update.sh' --rollback '$BACKUP'"; fi
