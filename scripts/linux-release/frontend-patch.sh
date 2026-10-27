#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

PACKAGE=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
WEB_INPUT=${FRONTEND_DIR:-/usr/share/nginx/html}
BACKUP_INPUT=${FRONTEND_BACKUP_ROOT:-/root/biz-backend/backups}
BASE_INDEX_SHA=__BASE_INDEX_SHA__
PATCH_INDEX_SHA=__PATCH_INDEX_SHA__
MODE=${1:---update}
BACKUP=''
PUBLISHING=0

say() { printf '[前端补丁] %s\n' "$*"; }
die() { printf '错误：%s\n' "$*" >&2; exit 1; }
digest() { sha256sum "$1" | cut -d ' ' -f 1; }
[[ $(id -u) == 0 ]] || die '请使用 root 执行'
[[ ${BASH_VERSINFO[0]} -ge 4 ]] || die '需要 Bash 4 或更新版本'
case "$MODE" in
  --check|--update) [[ $# -le 1 ]] || die '用法：bash update.sh [--check|--update|--rollback 备份目录]' ;;
  --rollback) [[ $# == 2 ]] || die '回滚需要指定本补丁生成的备份目录' ;;
  *) die '用法：bash update.sh [--check|--update|--rollback 备份目录]' ;;
esac
for tool in sha256sum cut find cp mv chmod mkdir tar gzip cmp flock; do
  command -v "$tool" >/dev/null || die "缺少命令：$tool"
done
(cd "$PACKAGE" && sha256sum -c SHA256SUMS --quiet) || die '补丁文件校验失败，请重新复制完整文件夹'
[[ "$WEB_INPUT" == /* && -d "$WEB_INPUT" ]] || die '前端目录不存在或不是绝对路径'
WEB=$(cd -- "$WEB_INPUT" && pwd -P)
[[ "$WEB" != / && -f "$WEB/index.html" && ! -L "$WEB/index.html" ]] || die '前端目录或入口不符合要求'
[[ "$BACKUP_INPUT" == /* && "$BACKUP_INPUT" != / && "/$BACKUP_INPUT/" != */../* ]] || die '备份目录必须是无上级跳转的绝对路径'
[[ "$PACKAGE/" != "$WEB/"* && "$BACKUP_INPUT/" != "$WEB/"* ]] || die '补丁和备份目录不能位于网页目录中'

validate_backup() {
  [[ -f "$BACKUP/FRONTEND_BACKUP_SHA256SUMS" && -f "$BACKUP/frontend.tar.gz" && -f "$BACKUP/web.path" && -f "$BACKUP/index.sha256" ]] || return 1
  (cd "$BACKUP" && sha256sum -c FRONTEND_BACKUP_SHA256SUMS --quiet) || return 1
  [[ $(cat "$BACKUP/web.path") == "$WEB" ]] || return 1
  gzip -t "$BACKUP/frontend.tar.gz" || return 1
}

restore_backup() {
  validate_backup || return 1
  # Retain new hashed files; the restored entry references all original assets.
  tar -xzf "$BACKUP/frontend.tar.gz" -C "$WEB" || return 1
  [[ $(digest "$WEB/index.html") == "$(cat "$BACKUP/index.sha256")" ]] || return 1
}

handle_error() {
  local result=$1
  trap - ERR INT TERM
  if [[ $PUBLISHING == 1 ]]; then
    if restore_backup; then
      say "更新未完成，已恢复原前端。备份：$BACKUP"
    else
      printf '错误：自动恢复失败，请保留备份并按说明回滚：%s\n' "$BACKUP" >&2
    fi
  fi
  exit "$result"
}
trap 'handle_error $?' ERR
trap 'handle_error 130' INT
trap 'handle_error 143' TERM

if [[ "$MODE" != --rollback ]]; then
  CURRENT=$(digest "$WEB/index.html")
  [[ "$CURRENT" == "$BASE_INDEX_SHA" || "$CURRENT" == "$PATCH_INDEX_SHA" ]] || die '当前前端不是20261027基线或本补丁版本，已停止；请核对实际安装目录与版本'
  while IFS= read -r -d '' directory; do
    relative=${directory#"$PACKAGE/frontend"}
    [[ ! -L "$WEB$relative" ]] || die "目标静态目录为符号链接：$WEB$relative"
  done < <(find "$PACKAGE/frontend" -type d -print0)
  if [[ "$MODE" == --check ]]; then
    say "只读检查通过；目标：$WEB；本包仅修复成果名称排版"
    exit 0
  fi
fi

mkdir -p "$BACKUP_INPUT"
BACKUP_ROOT=$(cd -- "$BACKUP_INPUT" && pwd -P)
[[ "$BACKUP_ROOT/" != "$WEB/"* && "$BACKUP_ROOT/" != "$PACKAGE/"* ]] || die '备份目录位置不符合要求'
exec 9>"$BACKUP_ROOT/.frontend-patch.lock"
flock -n 9 || die '另一项前端补丁正在执行，请稍后重试'
if [[ "$MODE" == --rollback ]]; then
  [[ -d "$2" ]] || die '备份目录不存在'
  BACKUP=$(cd -- "$2" && pwd -P)
  [[ "$BACKUP" == "$BACKUP_ROOT"/frontend-achievement-title-* ]] || die '不是本补丁备份目录'
  validate_backup || die '备份校验或目标目录核对失败'
  restore_backup || die '回滚失败，请保留备份'
  say "原前端已恢复：$BACKUP；请按 Ctrl+F5 刷新"
  exit 0
fi

# Recheck the installed entry after acquiring the shared deployment lock.
CURRENT=$(digest "$WEB/index.html")
[[ "$CURRENT" == "$BASE_INDEX_SHA" || "$CURRENT" == "$PATCH_INDEX_SHA" ]] || die '等待期间前端版本已变化，已停止'
if [[ "$CURRENT" == "$PATCH_INDEX_SHA" ]] && (cd "$WEB" && sha256sum -c "$PACKAGE/FRONTEND_SHA256SUMS" --quiet >/dev/null 2>&1); then
  say '补丁已安装且文件校验通过，无需重复更新'
  exit 0
fi
BACKUP="$BACKUP_ROOT/frontend-achievement-title-$(date +%Y%m%d-%H%M%S)-$$"
mkdir "$BACKUP"
printf '%s\n' "$WEB" > "$BACKUP/web.path"
printf '%s\n' "$CURRENT" > "$BACKUP/index.sha256"
tar -czf "$BACKUP/frontend.tar.gz" -C "$WEB" .
gzip -t "$BACKUP/frontend.tar.gz"
(cd "$BACKUP" && sha256sum frontend.tar.gz web.path index.sha256 > FRONTEND_BACKUP_SHA256SUMS)
validate_backup || die '备份核验失败，未替换前端'
say "原前端已备份：$BACKUP"

PUBLISHING=1
while IFS= read -r -d '' directory; do
  relative=${directory#"$PACKAGE/frontend"}
  if [[ ! -d "$WEB$relative" ]]; then mkdir -p "$WEB$relative"; chmod 755 "$WEB$relative"; fi
done < <(find "$PACKAGE/frontend" -type d -print0)
while IFS= read -r -d '' file; do
  relative=${file#"$PACKAGE/frontend/"}
  [[ "$relative" == index.html ]] && continue
  cp "$file" "$WEB/$relative.frontend-new"
  chmod 644 "$WEB/$relative.frontend-new"
  mv -f "$WEB/$relative.frontend-new" "$WEB/$relative"
done < <(find "$PACKAGE/frontend" -type f -print0)
cp "$PACKAGE/frontend/index.html" "$WEB/index.html.frontend-new"
chmod 644 "$WEB/index.html.frontend-new"
mv -f "$WEB/index.html.frontend-new" "$WEB/index.html"
(cd "$WEB" && sha256sum -c "$PACKAGE/FRONTEND_SHA256SUMS" --quiet)
PUBLISHING=0
say '更新成功；无需重启后端或 nginx，请按 Ctrl+F5 刷新浏览器'
say "回滚命令：bash '$PACKAGE/update.sh' --rollback '$BACKUP'"
