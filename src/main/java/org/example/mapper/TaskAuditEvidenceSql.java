package org.example.mapper;

/** A log belongs to a submission cycle only when that submission already existed. */
public final class TaskAuditEvidenceSql {
    private TaskAuditEvidenceSql() { }
    public static final String VALID = "EXISTS (SELECT 1 FROM biz_material_submission evidence_submission "
            + "WHERE evidence_submission.sub_id=l.sub_id AND l.create_time>=evidence_submission.submit_time)";
}
