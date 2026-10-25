package org.example.mapper;

/** Read-side counterpart of the existing valid three-level record catalog. Aliases are code-owned. */
public final class WorkRecordQualificationSql {
    public static String fillableAccountIds(){return "<script>SELECT u.user_id FROM sys_user u WHERE u.user_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> AND "+reporter("u","BETWEEN 2025 AND 2029").replace("<>","&lt;&gt;")+"</script>";}
    public static String validTasks(String year){return " FROM biz_task t JOIN biz_task m ON m.task_id=t.parent_id AND m.level=2 AND COALESCE(m.is_delete,0)=0 "
            +"JOIN biz_task root ON root.task_id=m.parent_id AND root.level=1 AND COALESCE(root.is_delete,0)=0 "
            +"AND (root.parent_id IS NULL OR root.parent_id=0) WHERE t.level=3 AND COALESCE(t.is_delete,0)=0 "
            +"AND t.phase "+year+" AND t.project_id IS NOT NULL AND t.project_id=m.project_id AND t.project_id=root.project_id ";}
    public static String reporter(String alias,String year){
        String catalog=validTasks(year);
        String goals=" FROM biz_level4_task g JOIN (SELECT t.task_id,t.phase"+catalog+") eligible_task ON eligible_task.task_id=g.parent_id "
                +"WHERE COALESCE(g.is_delete,0)=0 AND (g.phase IS NULL OR g.phase=eligible_task.phase)";
        // Keep a derived boundary: MySQL can rewrite a bare IN (UNION) to
        // correlated scans of each task relation for every account.
        String responsible="SELECT t.leader_id userId"+catalog+" UNION SELECT g.leader_id"+goals;
        String departments="SELECT t.dept_id deptId"+catalog+" UNION SELECT m.dept_id"+catalog+" UNION SELECT g.dept_id"+goals;
        return "(BINARY "+alias+".status=BINARY '1' AND ("+alias+".is_delete IS NULL OR "+alias+".is_delete<>1) AND ("
                +alias+".user_id IN (SELECT responsible.userId FROM ("+responsible+") responsible) OR (EXISTS(SELECT 1 FROM sys_dept ud WHERE ud.dept_id="+alias+".dept_id "
                +"AND (ud.is_delete IS NULL OR ud.is_delete<>1) AND (ud.status IS NULL OR BINARY ud.status<>BINARY '1')) "
                +"AND "+alias+".dept_id IN (SELECT participating.deptId FROM ("+departments+") participating))))";
    }
    private WorkRecordQualificationSql(){}
}
