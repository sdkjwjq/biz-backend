#!/usr/bin/env bash
# Targeted data repair only: keep the installed frontend, JAR and configuration unchanged.
set -Eeuo pipefail
umask 077
PACKAGE=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
BACKEND=/root/biz-backend
JAR=biz_backend-1.0-SNAPSHOT.jar
MODE=${1:---check}
DB_HOST=${DB_HOST:-127.0.0.1}
DB_PORT=${DB_PORT:-3306}
DB_USER=${DB_USER:-root}
DB_NAME=${DB_NAME:-biz}
STOPPED=0
STOP_REQUESTED=0
RESTARTED=0
JAVA_BIN=''
OLD_ARGS=()
die() { printf '错误：%s\n' "$*" >&2; exit 1; }
say() { printf '[负责人修复] %s\n' "$*"; }
need() { command -v "$1" >/dev/null || die "缺少命令 $1"; }
find_app() {
  local proc cwd arg jar_path count=0 found i
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
    if [[ "$found" == 1 ]]; then APP_PID=${proc##*/}; count=$((count+1)); fi
  done
  [[ $count -le 1 ]] || die '发现多个目标后端，请先核实；尚未停止服务'
}
start_saved() {
  (cd "$BACKEND"; unset MYSQL_PWD; nohup "$JAVA_BIN" "${OLD_ARGS[@]}" >> "$BACKUP/restart.log" 2>&1 < /dev/null 9>&- & echo $! > "$BACKUP/restarted.pid")
  RESTARTED=1
}
health() {
  local response n
  for ((n=0;n<90;n++)); do
    response=$(curl --noproxy '*' -s --max-time 3 -H 'Content-Type: application/json' \
      --data '{"user_id":-1,"password":"release-connectivity-probe"}' 'http://127.0.0.1:8080/api/system/login' || true)
    if [[ "$response" == *'用户不存在'* ]]; then find_app; [[ -n "$APP_PID" ]] && return 0; fi
    sleep 1
  done
  return 1
}
finish() {
  local rc=$?
  trap - EXIT
  if [[ "$STOP_REQUESTED" == 1 && "$RESTARTED" == 0 ]]; then
    # A signal may arrive during graceful shutdown; never restart a second JVM or leave a stopped one down.
    for ((n=0;n<90;n++)); do
      if ! kill -0 "$OLD_PID" 2>/dev/null; then STOPPED=1; break; fi
      sleep 1
    done
    if [[ "$STOPPED" == 1 ]]; then
      say '操作未完成，正在按原启动参数恢复后端'
      if start_saved && health; then say '原后端已恢复'; else say "请检查启动日志：$BACKUP/restart.log"; fi
    else
      say '原后端仍在运行；未强制终止，请核对服务状态'
    fi
  fi
  exit "$rc"
}
[[ $# -le 1 && "$MODE" =~ ^--(check|apply|rollback)$ ]] || die '用法：bash repair.sh --check|--apply|--rollback'
for command in mysql sha256sum cat; do need "$command"; done
(cd "$PACKAGE"; sha256sum -c SHA256SUMS --quiet) || die '修复文件校验失败；未修改数据库'
if [[ -z ${MYSQL_PWD:-} ]]; then
  read -r -s -p '请输入服务器 MySQL root 密码（不回显）：' DB_PASSWORD < /dev/tty
  printf '\n'
  [[ -n "$DB_PASSWORD" ]] || die '数据库密码不能为空'
  export MYSQL_PWD=$DB_PASSWORD
fi
MYSQL=(mysql --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" --default-character-set=utf8mb4 --batch --skip-column-names "$DB_NAME")
[[ $("${MYSQL[@]}" -e 'SELECT VERSION()') == 8.* ]] || die '该修复要求 MySQL 8'
SEED="$PACKAGE/sql/2026-10-29-task-leader-repair-candidates.sql"
CHECK="$PACKAGE/sql/2026-10-29-task-leader-repair-check.sql"
if [[ "$MODE" == --check ]]; then
  cat "$SEED" "$CHECK" | "${MYSQL[@]}"
  say '只读核对完成；READY 为可自动修正记录，其他状态保留原数据'
  exit 0
fi
[[ "$EUID" == 0 ]] || die '执行修复或回滚需要 root，以便停止并恢复现有后端'
for command in mysqldump gzip flock readlink curl nohup awk; do need "$command"; done
[[ -d "$BACKEND" && -f "$BACKEND/$JAR" ]] || die '未找到既有后端部署'
exec 9> "$BACKEND/.task-leader-repair.lock"
flock -n 9 || die '另一个负责人修复正在运行'
find_app
[[ -n "$APP_PID" ]] || die '未发现运行中的目标后端，请先核对服务状态；未修改数据库'
OLD_PID=$APP_PID
JAVA_BIN=$(readlink -f "/proc/$OLD_PID/exe")
while IFS= read -r -d '' arg; do OLD_ARGS+=("$arg"); done < "/proc/$OLD_PID/cmdline"
OLD_ARGS=("${OLD_ARGS[@]:1}")
BACKUP="$BACKEND/backups/task-leader-20261029-$(date +%Y%m%d-%H%M%S)-$$"
mkdir -p "$BACKUP"
{ printf 'JAVA_BIN=%q\n' "$JAVA_BIN"; printf 'OLD_ARGS=('; printf ' %q' "${OLD_ARGS[@]}"; printf ' )\n'; } > "$BACKUP/state.sh"
cat "$SEED" "$CHECK" | "${MYSQL[@]}" > "$BACKUP/before.tsv"
trap finish EXIT
trap 'exit 130' INT
trap 'exit 143' TERM
say "停止目标后端 PID=$OLD_PID；数据库完整备份：$BACKUP"
STOP_REQUESTED=1
kill -TERM "$OLD_PID"
for ((n=0;n<90;n++)); do
  if ! kill -0 "$OLD_PID" 2>/dev/null; then STOPPED=1; break; fi
  sleep 1
done
[[ "$STOPPED" == 1 ]] || die '后端未正常停止；未强杀、未修改数据库'
mysqldump --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" --default-character-set=utf8mb4 \
  --single-transaction --routines --triggers --events --hex-blob --set-gtid-purged=OFF "$DB_NAME" | gzip > "$BACKUP/database.sql.gz"
gzip -t "$BACKUP/database.sql.gz"
if [[ "$MODE" == --apply ]]; then
  cat "$SEED" "$PACKAGE/sql/2026-10-29-task-leader-repair.sql" | "${MYSQL[@]}" > "$BACKUP/operation.tsv"
else
  "${MYSQL[@]}" < "$PACKAGE/sql/2026-10-29-task-leader-repair-rollback.sql" > "$BACKUP/operation.tsv"
fi
cat "$SEED" "$CHECK" | "${MYSQL[@]}" > "$BACKUP/after.tsv"
start_saved
health || die "后端未通过连通检查，请查看 $BACKUP/restart.log；数据库修复记录和完整备份已保留"
STOPPED=0
say '操作成功；现有前后端版本保持原样，请刷新任务页面'
awk -F '\t' '$1=="repaired_tasks" || $1=="rolled_back_tasks" {print "本次处理任务数：" $2}' "$BACKUP/operation.tsv"
say "详细核对和备份：$BACKUP"
