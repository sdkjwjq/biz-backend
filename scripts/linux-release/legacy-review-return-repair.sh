#!/usr/bin/env bash
# Sourced by update.sh; credentials remain in environment, never command arguments.
legacy_return_repair() {
  local mode=$1 report=$2 log_file=$3
  [[ "$mode" == check || "$mode" == apply ]] || die '未知审核修复模式'
  [[ ! -e "$report" ]] || die '审核修复报告已存在，不覆盖原始证据'
  if ! (cd "$BACKEND"; export SPRING_DATASOURCE_URL="jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    export SPRING_DATASOURCE_USERNAME="$DB_USER" SPRING_DATASOURCE_PASSWORD="$DB_PASSWORD";
    export LOGGING_FILE_NAME="$log_file";
    "$JAVA_BIN" -jar "$PACKAGE/backend/$JAR" --legacy-review-return-repair "$mode" "$report"
  ) > "$log_file" 2>&1; then
    die "审核修复检查未通过，已停止后续步骤。私有检查报告：$report；诊断：$log_file"
  fi
  grep -q '^LEGACY_REVIEW_RETURN_REPAIR_OK ' "$log_file" || die '审核修复未返回成功标记'
  local count
  count=$(sed -n 's/^LEGACY_REVIEW_RETURN_REPAIR_OK .*changes=\([0-9]*\).*/\1/p' "$log_file" | tail -n 1)
  [[ "$count" =~ ^[0-9]+$ ]] || die '无法核对审核修复数量'
  say "审核过渡期记录：$mode；符合条件 $count 条；原始审核日志、填报值保留"
}
