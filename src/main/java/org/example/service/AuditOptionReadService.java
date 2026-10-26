package org.example.service;
import org.example.utils.AdminTestContext;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import java.util.*;

/** Audit option membership, without materials, report text or history bodies. */
@Service
public class AuditOptionReadService {
 private final PermissionService permissions;private final ReadScopeSql scopes;private final RemoteOptionPages pages;
 public AuditOptionReadService(PermissionService permissions,ReadScopeSql scopes,RemoteOptionPages pages){this.permissions=permissions;this.scopes=scopes;this.pages=pages;}
 public Map<String,Object> query(String key,Long actor,MultiValueMap<String,String> params){
  if(!Set.of("type","submitDeptId","submitBy","year","stage","root").contains(key))return null;
  if(params.getFirst("tab")!=null&&!Set.of("todo","done").contains(params.getFirst("tab")))throw new AdminManagementException(400,"审核页签不正确");
  boolean todo=!"done".equals(params.getFirst("tab")),test=AdminTestContext.enabled(actor);var rights=permissions.state(actor).permissions();var args=new ArrayList<Object>();var parts=new ArrayList<String>();
  for(String domain:List.of("tasks","performance","achievements","core")){
   if(!rights.contains(domain+".read")||(todo?!(rights.contains(domain+".audit")||rights.contains(domain+".archive")):!rights.contains(domain+".history")))continue;
   if(key.equals("root")&&!domain.equals("tasks"))continue;
   String source,year,scope,predicate,type=domain.equals("tasks")?"task":domain.equals("achievements")?"achievement":domain;
   ReadScopeSql.Clause range;
   switch(domain){
    case "tasks":
     source="biz_material_submission s LEFT JOIN biz_task t ON t.task_id=s.task_id AND t.level=3 AND (t.is_delete IS NULL OR t.is_delete<>1)";
     year="t.phase";range=scopes.taskObjects(actor,"s.task_id");predicate=todo?"s.flow_status>=10 AND s.flow_status<40 AND "+(test?"s.current_handler_id IS NOT NULL AND EXISTS(SELECT 1 FROM biz_task b WHERE b.task_id=s.task_id AND COALESCE(b.is_delete,0)=0)":"s.current_handler_id=?"):
      "(s.flow_status=40 OR s.flow_status<0) AND t.task_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM biz_material_submission broken LEFT JOIN sys_file f ON f.file_id=broken.file_id WHERE broken.task_id=s.task_id AND broken.is_delete=0 AND f.file_id IS NULL)";
     if(todo&&!test)args.add(actor);break;
    case "performance":
     source="biz_performance_submission s JOIN biz_performance p ON p.perf_id=s.perf_id AND COALESCE(p.is_delete,0)=0";year="s.year";range=scopes.rowByYear(actor,domain,"p",year);
     predicate=todo?"s.flow_status IN(10,20)"+(test?"":" AND s.current_handler_id=?"):"(s.flow_status=30 OR s.flow_status<0) AND (s.submit_by=? OR s.current_handler_id=? OR EXISTS(SELECT 1 FROM biz_performance_audit_log l WHERE l.sub_id=s.sub_id AND l.operator_id=? AND l.action_type IN('pass','reject')))";
     if(todo&&!test)args.add(actor);if(!todo){args.add(actor);args.add(actor);args.add(actor);}break;
    case "achievements":
     if(!org.example.utils.AchievementPermissionUtil.isAchievementAuditor(permissions.user(actor)))continue;
     source="biz_achievement_submission s JOIN biz_achievement a ON a.ach_id=s.ach_id AND COALESCE(a.is_delete,0)=0";year="YEAR(a.got_time)";range=scopes.row(actor,domain,"a",null);predicate=todo?"s.flow_status=10":"(s.flow_status=30 OR s.flow_status<0)";break;
    default:
     source="biz_core_report s JOIN biz_core_indicator c ON c.indicator_id=s.indicator_id";year="s.record_year";range=scopes.rowByYear(actor,"core","c",year);
     predicate=todo?"s.flow_status IN(10,20) AND BINARY c.indicator_code=BINARY s.performance_code"+(test?"":" AND (s.current_handler_id=?"+(permissions.admin(actor)?" OR s.flow_status=20":"")+")"):
       "(s.flow_status=30 OR s.flow_status<0) AND EXISTS(SELECT 1 FROM biz_core_audit_log l WHERE l.report_id=s.report_id AND l.operator_id=? AND l.action IN('通过','退回'))";
     if(!todo||!test)args.add(actor);break;
   }
   scope=range.sql();args.addAll(range.args());
   String submitBy=domain.equals("core")?"s.submit_by":"s.submit_by",department=domain.equals("core")?"u.dept_id":"COALESCE(s.submit_dept_id,u.dept_id)";
   if(domain.equals("performance")||domain.equals("achievements"))department="u.dept_id";
   String value,label;
   switch(key){
    case "type":value="'"+type+"'";label="'"+(domain.equals("tasks")?"任务佐证":domain.equals("performance")?"绩效填报":domain.equals("achievements")?"成果归档":"核心指标")+"'";break;
    case "submitBy":value="CAST("+submitBy+" AS CHAR)";label="COALESCE(u.nick_name,'')";break;
    case "submitDeptId":value="CAST("+department+" AS CHAR)";label="COALESCE((SELECT d.dept_name FROM sys_dept d WHERE d.dept_id="+department+"),'')";break;
    case "year":value="CAST("+year+" AS CHAR)";label=value;break;
    case "root":
     value="CAST((WITH RECURSIVE parents(task_id,parent_id,level,seen) AS (SELECT t.task_id,t.parent_id,t.level,JSON_ARRAY(t.task_id) UNION ALL SELECT p.task_id,p.parent_id,p.level,JSON_ARRAY_APPEND(n.seen,'$',p.task_id) FROM parents n JOIN biz_task p ON p.task_id=n.parent_id WHERE p.level<=3 AND (p.is_delete IS NULL OR p.is_delete<>1) AND NOT JSON_CONTAINS(n.seen,CAST(p.task_id AS JSON))) SELECT task_id FROM parents WHERE level=1 LIMIT 1) AS CHAR)";
     label="(SELECT p.task_name FROM biz_task p WHERE p.task_id="+value+")";break;
    default:value="CONCAT('"+domain+":',s.flow_status)";label="CASE WHEN s.flow_status IN(10,20,30) THEN CONCAT('"+(domain.equals("tasks")?"任务":domain.equals("performance")?"绩效":domain.equals("achievements")?"成果":"核心指标")+" · ',"+(domain.equals("tasks")?"CASE s.flow_status WHEN 10 THEN '专业群审核' WHEN 20 THEN '归口审核' ELSE '归档' END":domain.equals("performance")?"CASE WHEN s.flow_status=10 THEN '专业群审核' ELSE '归档' END":domain.equals("core")?"CASE WHEN s.flow_status=10 THEN '归口审核' ELSE '归档' END":"'归档'")+") ELSE "+value+" END";
   }
   parts.add("SELECT DISTINCT CAST("+value+" AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_bin value,CAST("+label+" AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_bin label FROM "+source+" LEFT JOIN sys_user u ON u.user_id=s.submit_by WHERE "+(domain.equals("core")?"":"s.is_delete=0 AND ")+predicate+" AND ("+scope+")");
  }
  return pages.query(parts.isEmpty()?"SELECT NULL value,NULL label WHERE 0=1":"SELECT DISTINCT value,label FROM ("+String.join(" UNION ALL ",parts)+") audit_options",args,params);
 }
}
