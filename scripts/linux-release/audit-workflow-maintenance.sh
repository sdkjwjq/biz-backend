#!/usr/bin/env bash
# Sourced by update.sh. Reports and the one-time marker remain root-only on the server.
WORKFLOW_MARKER="$BACKEND/.audit-workflow-20261103-v1"
WORKFLOW_APPLIED=0
workflow_command() {
  local action=$1 report=$2
  env SPRING_DATASOURCE_URL="jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai" \
    SPRING_DATASOURCE_USERNAME="$DB_USER" SPRING_DATASOURCE_PASSWORD="$MYSQL_PWD" \
    LOGGING_FILE_NAME="${report%.json}-app.log" \
    "$JAVA_BIN" -jar "$PACKAGE/backend/$JAR" --audit-workflow-maintenance "$action" "$report"
}
workflow_preflight() {
  [[ ! -f "$WORKFLOW_MARKER" ]] || return 0
  local directory result=0
  directory=$(mktemp -d "$BACKEND/.workflow-check.XXXXXX")
  workflow_command check "$directory/check.json" > "$directory/check.log" 2>&1 || result=$?
  if [[ $result != 0 ]]; then
    say "待办核对未通过，未修改业务数据；检查结果：$directory/check.log"
    return "$result"
  fi
  # These are files created by this invocation under the exact mktemp directory.
  rm -f -- "$directory/check.json" "$directory/check.log" "$directory/check-app.log"
  rmdir -- "$directory"
}
workflow_apply() {
  if [[ -f "$WORKFLOW_MARKER" ]]; then
    cp -p "$WORKFLOW_MARKER" "$BACKUP/workflow-marker-existing"
    printf '%s\n' '{"batch":"audit-workflow-20261103-v1","mode":"apply","changes":[],"skipped":["previously completed"]}' > "$BACKUP/workflow-maintenance.json"
    return 0
  fi
  workflow_command apply "$BACKUP/workflow-maintenance.json" > "$BACKUP/workflow-maintenance.log" 2>&1
  "${MYSQL[@]}" < "$PACKAGE/sql/2026-11-03-audit-workflow-check.sql" > "$BACKUP/workflow-check.tsv"
  local errors
  errors=$(awk -F'\t' '($1=="active_principal_handler_mismatch" || $1=="deleted_achievement_pending_residual") && $2!="0" {print $1 "=" $2}' "$BACKUP/workflow-check.tsv")
  [[ -z "$errors" ]] || { say "待办核对未通过：$errors"; return 1; }
  WORKFLOW_APPLIED=1
  printf '%s\n' "$BACKUP/workflow-maintenance.json" > "$WORKFLOW_MARKER"
}
