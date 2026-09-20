#!/usr/bin/env bash
# Read-only compatibility gate. Missing legacy business structure stops deployment.
set -Eeuo pipefail
BASE=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
MODE=${1:-after}
[[ "$MODE" == before || "$MODE" == after ]] || exit 2
: "${DB_HOST:=127.0.0.1}" "${DB_PORT:=3306}" "${DB_USER:=root}" "${DB_NAME:=biz}"
[[ "$DB_NAME" =~ ^[A-Za-z0-9_]+$ ]] || exit 2
TMP=$(mktemp -d)
trap 'rm -rf -- "$TMP"' EXIT
MYSQL=(mysql --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" --default-character-set=utf8mb4 --batch --skip-column-names "$DB_NAME")
"${MYSQL[@]}" -e 'SELECT TABLE_NAME,COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() ORDER BY TABLE_NAME,ORDINAL_POSITION' > "$TMP/actual"
awk -v mode="$MODE" '
  FILENAME==ARGV[1] { actual[$1 FS $2]=1; next }
  FILENAME==ARGV[2] { allowed[$1 FS $2]=1; next }
  { key=$1 FS $2; if (!actual[key] && !(mode=="before" && allowed[key])) { print "缺少不在自动迁移范围内的字段：" $1 "." $2; failed=1 } }
  END { exit failed }
' "$TMP/actual" "$BASE/sql/allowed-additions.tsv" "$BASE/sql/required-columns.tsv"
if [[ "$MODE" == after ]]; then
  "${MYSQL[@]}" -e "SELECT TABLE_NAME,GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX SEPARATOR ',') FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND NON_UNIQUE=0 GROUP BY TABLE_NAME,INDEX_NAME" > "$TMP/unique"
  awk 'FILENAME==ARGV[1] { present[$1 FS $2]=1; next } !present[$1 FS $2] {print "缺少唯一约束：" $1 "(" $2 ")"; bad=1} END {exit bad}' "$TMP/unique" "$BASE/sql/required-unique.tsv"
fi
printf '数据库结构检查通过（%s）。\n' "$MODE"
