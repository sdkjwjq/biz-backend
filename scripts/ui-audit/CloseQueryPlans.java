package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.AccountFilterPageService;
import org.example.mapper.WorkRecordQualificationSql;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
/** Read-only plans for code-owned queries, restricted to a disposable workload schema. */
public class CloseQueryPlans {
 public static void main(String[] args)throws Exception {
  var json=new ObjectMapper();Path stateFile=Path.of(System.getenv("UI_AUDIT_STATE_FILE"));var state=json.readTree(stateFile.toFile());String schema=state.get("schema").asText();
  if(!schema.matches("biz_review_test_[0-9a-f]{32}"))throw new IllegalArgumentException("Disposable schema required");
  var plans=new LinkedHashMap<String,Object>();var queries=new LinkedHashMap<String,String>();
  var duties=AccountFilterPageService.class.getDeclaredField("DUTIES");duties.setAccessible(true);
  queries.put("activeAccountResponsibilities","SELECT u.user_id FROM sys_user u WHERE COALESCE(u.is_delete,0)=0 AND "+duties.get(null)+" ORDER BY u.user_id LIMIT 10");
  queries.put("recordEligibleAccounts","SELECT u.user_id FROM sys_user u WHERE "+WorkRecordQualificationSql.reporter("u","=2026"));
  queries.put("noticeStableOrder","SELECT notice_id,create_time FROM sys_notice WHERE to_user_id=5100000 AND COALESCE(is_delete,0)=0 ORDER BY create_time IS NULL,create_time DESC,notice_id DESC LIMIT 10");
  queries.put("departmentTaskSummary","SELECT COUNT(*),SUM(progress),SUM(status='3') FROM biz_task WHERE COALESCE(is_delete,0)=0 AND level=3 AND phase=2026 AND dept_id=920002");
  queries.put("currentVersionPage","SELECT version_id,year,month,version_no,created_by,create_time FROM biz_budget_import_version ORDER BY year DESC,month DESC,version_no DESC,version_id DESC LIMIT 10");
  try(Connection connection=DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/"+schema,"root",System.getenv("SHUANGGAO_TEST_DB_PASSWORD"))){connection.setReadOnly(true);connection.setAutoCommit(false);
   var counts=new LinkedHashMap<String,Long>();for(String table:List.of("sys_user","biz_task","biz_performance","biz_material_submission","sys_notice","biz_work_record","sys_admin_operation_log","biz_budget_import_version"))try(var stmt=connection.createStatement();var rs=stmt.executeQuery("SELECT COUNT(*) FROM "+table)){rs.next();counts.put(table,rs.getLong(1));}
   for(var query:queries.entrySet()){var output=new LinkedHashMap<String,Object>();for(String mode:List.of("FORMAT=JSON","ANALYZE")){var rows=new ArrayList<String>();try(var stmt=connection.createStatement();var rs=stmt.executeQuery("EXPLAIN "+mode+" "+query.getValue())){while(rs.next())rows.add(rs.getString(1));}output.put(mode,rows);}plans.put(query.getKey(),output);}connection.rollback();
   var result=new LinkedHashMap<String,Object>();result.put("schema",schema);result.put("counts",counts);result.put("plans",plans);json.writerWithDefaultPrettyPrinter().writeValue(stateFile.resolveSibling("close-execution-plans.json").toFile(),result);
  }
  System.out.println("Read-only execution plans: "+plans.size());
 }
}
