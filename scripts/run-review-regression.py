"""Run HTTP regression tests in a temporary MySQL schema, never the business schema.

Requires MySQL client tools, Java/Maven and SHUANGGAO_TEST_DB_PASSWORD in the environment.
Usage: python scripts/run-review-regression.py [methodName]
Only the schema of the local biz database is copied; no business records are copied.
"""
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import uuid


def executable(name):
    found = shutil.which(name)
    if found:
        return found
    candidate = Path(os.environ.get("ProgramFiles", "C:/Program Files")) / "MySQL/MySQL Server 8.0/bin" / (name + ".exe")
    if candidate.is_file():
        return str(candidate)
    raise RuntimeError("Missing executable: " + name)


def main():
    root = Path(__file__).resolve().parents[1]
    password = os.environ.get("SHUANGGAO_TEST_DB_PASSWORD")
    if password is None:
        raise RuntimeError("Set SHUANGGAO_TEST_DB_PASSWORD before running this script")
    method = sys.argv[1] if len(sys.argv) > 1 else None
    if method and not re.fullmatch(r"(?:admin:)?[A-Za-z][A-Za-z0-9_]*", method):
        raise RuntimeError("Expected a single regression test method name")
    database = "biz_review_test_" + uuid.uuid4().hex
    env = os.environ.copy()
    env["MYSQL_PWD"] = password
    user = env.get("SHUANGGAO_TEST_DB_USER", "root")
    connection = ["--host=127.0.0.1", "--port=3306", "--user=" + user, "--default-character-set=utf8mb4"]
    mysql = executable("mysql")
    dump = subprocess.run([executable("mysqldump"), *connection, "--no-data", "--skip-triggers",
                           "--set-gtid-purged=OFF", "biz"], env=env, check=True, capture_output=True).stdout
    if re.search(rb"(?im)^\s*(?:USE\s|CREATE\s+DATABASE\s)", dump):
        raise RuntimeError("Schema dump unexpectedly selects a database")

    def sql(statement):
        subprocess.run([mysql, *connection], input=statement.encode("utf-8"), env=env, check=True)

    sql("CREATE DATABASE `" + database + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    try:
        subprocess.run([mysql, *connection, database], input=dump, env=env, check=True)
        subprocess.run([mysql, *connection, database], input=(root / 'scripts/password/001_force_password_change.sql').read_bytes(), env=env, check=True)
        subprocess.run([mysql, *connection, database],
                       input=(root / 'data/migrations/2026-09-26-budget-import-version.sql').read_bytes(),
                       env=env, check=True)
        subprocess.run([mysql, *connection, database],
                       input=(root / 'data/migrations/2026-09-27-admin-management.sql').read_bytes(),
                       env=env, check=True)
        env["SHUANGGAO_REVIEW_TEST"] = "true"
        subprocess.run([mysql, *connection, database], input=(root / 'data/migrations/2026-10-12-leadership-access.sql').read_bytes(), env=env, check=True)
        subprocess.run([mysql, *connection, database], input=(root / 'data/migrations/2026-10-12-task-attributes.sql').read_bytes(), env=env, check=True)
        subprocess.run([mysql, *connection, database], input=(root / 'data/migrations/2026-10-12-core-indicators.sql').read_bytes(), env=env, check=True)
        subprocess.run([mysql, *connection, database], input=(root / 'data/migrations/2026-10-14-core-indicator-report-mapping.sql').read_bytes(), env=env, check=True)
        subprocess.run([mysql, *connection, database], input=(root / 'data/migrations/2026-10-24-permissions-expert-reviews.sql').read_bytes(), env=env, check=True)
        subprocess.run([mysql, *connection, database], input=(root / "data/migrations/2026-10-28-transfer-reviewers.sql").read_bytes(), env=env, check=True)
        env["SHUANGGAO_TEST_DB_USER"] = user
        env["SHUANGGAO_TEST_JDBC_URL"] = ("jdbc:mysql://127.0.0.1:3306/" + database
                                         + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai")
        if method == "browserrepairs":
            selector = "ReviewBatchRegressionApiTest#" + "+".join([
                "budgetWebEditsAreVersionedAtomicAndPreserveImportedColumns",
                "budgetWebEditsValidateDecimalsAndSerializeConcurrentSaves",
                "budgetImportsCreateVersionsAndRestoreWithoutOverwritingHistory",
                "financeAndOfficeCanImportBudgetWithoutAdministratorRole",
                "rejectedPerformanceStaysInActualReviewersHistoryAndLocate",
                "performanceRollbackPreservesOtherYearsAndCurrentTargets",
                "performanceActiveReviewStagesStillAllowRejection",
                "achievementResubmitRetainsRejectedEvidenceAndRejectsDuplicates",
                "achievementAdminResubmitSerializesDuplicatesAndKeepsReporter",
                "rejectedAchievementsStayInListWithLatestReasonAndOtherStatusesIgnoreOldReason",
                "deletedAchievementsNeverRemainPendingAndCompletedHistorySurvives",
                "noticeListHidesStaleRequestsPreservesOrdinaryMessagesAndIsolation",
                "administratorAuditPageAndBadgeUseSamePendingScope",
            ]) + ",AuditScopeConsistencyApiTest"
        elif method == "transferreviewers":
            selector = "ReviewBatchRegressionApiTest#" + "+".join([
                "ownershipTransferValidatesInputAndRequiresAdmin", "ownershipTransferBlocksActiveAuditsAndRecordsTheChange",
                "ownershipTransferHandlesPerformanceAndRejectsActiveAudits", "automaticAdminTraceDoesNotBlockLargeBatchTransfers",
                "transferReviewerFieldsStayIndependentAndRouteNewTaskSubmissions", "transferCanChangeOnlyReviewerAndRejectInactiveAssignmentsAtomically",
                "transferMixedReviewersCanRemainDistinctForLegacyAndBatchRequests", "performanceTransferUpdatesTheReviewerActuallyUsedBySubmission",
                "taskWithMissingReviewerCanStillBeOpenedAndReassigned", "transferRejectsStaleOwnershipPreviewWithoutOverwritingChanges",
            ]) + ",PermissionApiTest"
        elif method == "auditscope":
            selector = "AuditScopeConsistencyApiTest"
        elif method == "admindrawer":
            selector = "AdminTestApiTest,CoreIndicatorApiTest,ExpertReviewApiTest,ReviewBatchRegressionApiTest#automaticAdminTraceDoesNotBlockLargeBatchTransfers"
        elif method == "expert":
            selector = "ExpertReviewApiTest"
        elif method == "permissionsexpert":
            selector = "PermissionApiTest,ExpertReviewApiTest"
        elif method == "permissions":
            selector = "PermissionApiTest"
        elif method == "permissionsintegration":
            selector = "PermissionApiTest,AdminManagementApiTest,AdminOverviewApiTest"
        elif method == "overview":
            selector = "AdminOverviewApiTest"
        elif method == "budgettrends":
            selector = "BudgetTrendApiTest,ReviewBatchRegressionApiTest#budgetImportsCreateVersionsAndRestoreWithoutOverwritingHistory"
        elif method == "core":
            selector = "CoreIndicatorApiTest"
        elif method == "leadership":
            selector = "LeadershipAccessApiTest"
        elif method == "attributes":
            selector = "TaskAttributesApiTest,ReviewBatchRegressionApiTest#directSubmissionAndResubmissionStoreActualValuesThroughApproval+adminFinishRequiresEveryLevel4GoalAndKeepsRealValues"
        elif method == "admintest":
            selector = "AdminTestApiTest"
        elif method == "admin":
            selector = "AdminManagementApiTest"
        elif method and method.startswith("admin:"):
            selector = "AdminManagementApiTest#" + method.removeprefix("admin:")
        elif method:
            selector = "ReviewBatchRegressionApiTest#" + method
        else:
            selector = "ReviewBatchRegressionApiTest,AdminManagementApiTest,LeadershipAccessApiTest,TaskAttributesApiTest,CoreIndicatorApiTest,BudgetTrendApiTest,AdminTestApiTest,AdminOverviewApiTest,PermissionApiTest,ExpertReviewApiTest"
        print("Running " + selector + " in isolated schema " + database, flush=True)
        result = subprocess.run([shutil.which("mvn.cmd") or executable("mvn"), "-B", "-Dtest=" + selector, "test"],
                                cwd=root, env=env)
        if result.returncode == 0:
            subprocess.run([mysql, *connection, database], input=(root / 'data/migrations/2026-10-24-permissions-expert-reviews.sql').read_bytes(), env=env, check=True)
            subprocess.run([mysql, *connection, database], input=(root / 'data/migrations/2026-10-24-permissions-expert-reviews-check.sql').read_bytes(), env=env, check=True)
            subprocess.run([mysql, *connection, database], input=b"INSERT INTO sys_user_permission(user_id,access_mode,permissions_json,scopes_json,version) VALUES(999998,'CUSTOM','{}','{}',1) ON DUPLICATE KEY UPDATE version=version+1;", env=env, check=True)
            permissions_rollback = subprocess.run([mysql, *connection, database], input=(root / 'data/migrations/2026-10-24-permissions-expert-reviews-rollback.sql').read_bytes(), env=env, capture_output=True)
            if permissions_rollback.returncode == 0 or b'Permission configuration or expert history exists' not in permissions_rollback.stderr:
                raise RuntimeError("Permission rollback must retain authorization and expert history")
            print("Permission migration checked; rollback correctly retained configuration", flush=True)
            subprocess.run([mysql, *connection, database], input=(root / 'data/migrations/2026-10-12-core-indicators.sql').read_bytes(), env=env, check=True)
            subprocess.run([mysql, *connection, database], input=b"INSERT INTO biz_core_report(indicator_id,record_year,record_month,indicator_name,submit_by) VALUES(1,2026,1,'Rollback guard fixture',110228);", env=env, check=True)
            core_rollback = subprocess.run([mysql, *connection, database], input=(root / 'data/migrations/2026-10-12-core-indicators-rollback.sql').read_bytes(), env=env, capture_output=True)
            if core_rollback.returncode == 0 or b'Core indicator history exists' not in core_rollback.stderr:
                raise RuntimeError("Core rollback must retain existing reports and drafts")
            print("Core rollback correctly refused to discard reports and drafts", flush=True)
            subprocess.run([mysql, *connection, database], input=b"INSERT INTO sys_leadership_access(user_id,scope,version) VALUES(999999,'ALL',1) ON DUPLICATE KEY UPDATE scope='ALL';", env=env, check=True)
            blocked = subprocess.run([mysql, *connection, database], input=(root / 'data/migrations/2026-10-12-leadership-access-rollback.sql').read_bytes(), env=env, capture_output=True)
            if blocked.returncode == 0 or b'Leadership configuration or trends exist' not in blocked.stderr:
                raise RuntimeError("Leadership rollback must retain existing configuration")
            print("Leadership rollback correctly refused to discard configuration", flush=True)
            task_count = subprocess.run([mysql, *connection, '-N', '-B', database], input=b"SELECT COUNT(*) FROM biz_task;", env=env, capture_output=True, check=True)
            if int(task_count.stdout.strip()) > 0:
                subprocess.run([mysql, *connection, database], input=b"UPDATE biz_task SET management_version=1 LIMIT 1;", env=env, check=True)
                attribute_rollback = subprocess.run([mysql, *connection, database], input=(root / 'data/migrations/2026-10-12-task-attributes-rollback.sql').read_bytes(), env=env, capture_output=True)
                if attribute_rollback.returncode == 0 or b'Task attributes contain business data' not in attribute_rollback.stderr:
                    raise RuntimeError("Task attribute rollback must retain business data")
                print("Task attribute rollback correctly refused to discard business data", flush=True)
        return result.returncode
    finally:
        # Only the schema name created above can reach this cleanup.
        if not re.fullmatch(r"biz_review_test_[0-9a-f]{32}", database):
            raise RuntimeError("Refusing to drop unexpected database")
        sql("DROP DATABASE `" + database + "`")
        print("Removed isolated schema " + database, flush=True)


if __name__ == "__main__":
    sys.exit(main())
