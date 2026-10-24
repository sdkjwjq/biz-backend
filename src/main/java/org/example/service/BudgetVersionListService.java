package org.example.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

/** Metadata only: snapshots remain loaded by the existing version detail endpoint. */
@Service
public class BudgetVersionListService {
    private final JdbcTemplate jdbc;
    private final PermissionService permissions;
    public BudgetVersionListService(JdbcTemplate jdbc, PermissionService permissions) {
        this.jdbc=jdbc;
        this.permissions=permissions;
    }
    public List<Map<String,Object>> rows(Long actor) {
        permissions.require(actor,"budget.versions");
        var rows=jdbc.queryForList("SELECT v.version_id versionId,v.sheet_id sheetId,v.year,v.month,"
                +"v.version_no versionNo,v.operation_type operationType,v.source_version_id sourceVersionId,"
                +"v.file_id fileId,v.file_name fileName,v.created_by createdBy,u.nick_name createdByName,"
                +"v.create_time createTime,v.remark,CAST((v.version_no=latest.version_no) AS SIGNED) isCurrent "
                +"FROM biz_budget_import_version v LEFT JOIN sys_user u ON u.user_id=v.created_by "
                +"LEFT JOIN (SELECT year,month,MAX(version_no) version_no FROM biz_budget_import_version "
                +"WHERE is_delete=0 GROUP BY year,month) latest ON latest.year=v.year AND latest.month=v.month "
                +"WHERE v.is_delete=0 ORDER BY v.year DESC,v.month DESC,v.version_no DESC");
        // Request-local only: later requests must see revocations and changed year scopes.
        Map<Integer,Boolean> allowedYears=new HashMap<>();
        rows.removeIf(r->!allowedYears.computeIfAbsent(((Number)r.get("year")).intValue(),
                year->permissions.budgetScope(actor,year)));
        return rows;
    }
}
