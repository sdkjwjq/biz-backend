package org.example.entity.vo;

import lombok.Data;
import org.example.entity.BizTask;
import org.example.entity.BizWorkRecord;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/** 工作纪实第一批的权限、明细及版本化统计响应。 */
public final class WorkRecordVO {
    private WorkRecordVO() { }

    public record Capabilities(boolean canCreate, boolean canViewAll, boolean canExport,
                               boolean canViewOwnHistory, List<Integer> fillableYears) { }
    public record ReformTask(Long taskId, String taskCode, String taskName) { }
    public record TaskSummary(Long taskId, String taskCode, String taskName, String leaderName,
                              Long reformTaskId, String auditState, String firstCompletedAt,
                              boolean completed, boolean completedThisMonth, boolean needsVerification,
                              String verificationReason, String other) { }
    public record Statistics(int schemaVersion, int year, int month, String generatedAt, String cutoffAt,
                             int totalTasks, int newCompleted, int cumulativeCompleted,
                             BigDecimal completionRate, int unverifiedTasks, String notice,
                             List<TaskSummary> tasks, List<ReformTask> reformTasks) { }
    public record Detail(BizWorkRecord record, Statistics statistics, boolean editable) { }
    public record Page(long total, int page, int pageSize, List<BizWorkRecord> records) { }

    @Data
    public static class Task extends BizTask {
        private String leaderName;
    }

    @Data
    public static class Evidence {
        private Long parentTaskId;
        private Long sourceTaskId;
        private Long subId;
        private BigDecimal reportedValue;
        private Integer flowStatus;
        private Integer cutoffStatus;
        private Date submitTime;
        private Date archiveTime;
        private String previousStatus;
    }
}
