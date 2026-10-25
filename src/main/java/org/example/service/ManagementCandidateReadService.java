package org.example.service;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.MultiValueMap;
import java.util.*;
@Service
public class ManagementCandidateReadService {
 private final PermissionService access;private final TaskManagementService tasks;private final JdbcTemplate jdbc;private final RemoteOptionPages pages;
 public ManagementCandidateReadService(PermissionService access,TaskManagementService tasks,JdbcTemplate jdbc,RemoteOptionPages pages){this.access=access;this.tasks=tasks;this.jdbc=jdbc;this.pages=pages;}
 @SuppressWarnings("unchecked")
 @org.springframework.transaction.annotation.Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
 public Map<String,Object> query(Long actor,String domain,boolean business,MultiValueMap<String,String> params){
  if(business){if(!tasks.canManage(actor))throw new AdminManagementException(403,"无权管理任务");}else access.requireAdmin(actor);
  String source,table,id,columns,extraSearch=null,order="CAST(value AS UNSIGNED)";List<Object> args=new ArrayList<>();
  switch(domain){
   case "users","active-users":table="sys_user u";id="u.user_id";columns="u.user_id userId,u.nick_name nickName,u.dept_id deptId";
    source="SELECT CAST(u.user_id AS CHAR) value,CONCAT(COALESCE(NULLIF(u.nick_name,''),'未命名'),'（',u.user_id,'）') label FROM sys_user u WHERE "+(business||domain.equals("active-users")?"(u.is_delete IS NULL OR u.is_delete<>1) AND BINARY u.status=BINARY '1'":"COALESCE(u.is_delete,0)=0");
    if(!business)extraSearch="COALESCE((SELECT u.user_name FROM sys_user u WHERE u.user_id=CAST(value AS UNSIGNED)),'')";break;
   case "tasks","directories":table="biz_task t LEFT JOIN biz_task p ON p.task_id=t.parent_id AND p.level IN(1,2) AND COALESCE(p.is_delete,0)=0";id="t.task_id";columns="t.task_id taskId,t.task_id id,t.task_code code,t.task_code taskCode,t.task_name name,t.task_name taskName,t.parent_id parentId,t.phase year,t.level,t.project_id projectId,p.task_name parentName";
    String where="COALESCE(t.is_delete,0)=0 AND "+(domain.equals("directories")?(business?"t.level=2 AND t.project_id=1":"t.level IN(1,2)"):"t.level=3");
    if(domain.equals("tasks")&&!params.getOrDefault("years",List.of()).isEmpty()){var years=params.get("years");for(String year:years)if(!Set.of("2025","2026","2027","2028","2029").contains(year))throw new AdminManagementException(400,"建设年度不正确");where+=" AND (t.phase IS NULL OR t.phase=0 OR t.phase IN("+String.join(",",Collections.nCopies(years.size(),"?"))+"))";args.addAll(years);}
    String label=business?"CONCAT(COALESCE(p.task_name,''),' / ',COALESCE(t.task_code,''),' ',COALESCE(t.task_name,''))":"CONCAT(COALESCE(t.task_code,''),' ',COALESCE(t.task_name,CONCAT('指标 ',t.task_id)),CASE WHEN t.phase IS NULL OR t.phase=0 THEN '' ELSE CONCAT('（',t.phase,'）') END)";
    source="SELECT CAST(t.task_id AS CHAR) value,"+label+" label FROM "+table+" WHERE "+where;
    if(!business)order="(SELECT t.level FROM biz_task t WHERE t.task_id=CAST(value AS UNSIGNED)),(SELECT t.task_code FROM biz_task t WHERE t.task_id=CAST(value AS UNSIGNED)),CAST(value AS UNSIGNED)";break;
   case "performance":table="biz_performance p";id="p.perf_id";columns="p.perf_id id,p.perf_code code,p.perf_name name,p.dept_id deptId";source="SELECT CAST(p.perf_id AS CHAR) value,CONCAT(COALESCE(p.perf_code,''),' ',COALESCE(p.perf_name,CONCAT('指标 ',p.perf_id))) label FROM biz_performance p WHERE COALESCE(p.is_delete,0)=0 AND LENGTH(p.ancestors)-LENGTH(REPLACE(p.ancestors,',',''))>=2";order="(SELECT p.perf_code FROM biz_performance p WHERE p.perf_id=CAST(value AS UNSIGNED)),CAST(value AS UNSIGNED)";break;
   case "achievements":table="biz_achievement a";id="a.ach_id";columns="a.ach_id id,a.ach_name name,a.dept_id deptId";source="SELECT CAST(a.ach_id AS CHAR) value,CONCAT(' ',COALESCE(a.ach_name,CONCAT('指标 ',a.ach_id))) label FROM biz_achievement a WHERE COALESCE(a.is_delete,0)=0";break;
   default:throw new AdminManagementException(400,"不支持的候选类型");
  }
  if(business&&!Set.of("users","directories").contains(domain))throw new AdminManagementException(400,"不支持的任务管理候选类型");
  var result=pages.query(source,args,params,order,extraSearch);var items=(List<Map<String,Object>>)result.get("items");var selected=(List<Map<String,Object>>)result.get("selected");Set<String> keys=new LinkedHashSet<>();items.forEach(r->keys.add(r.get("value").toString()));selected.forEach(r->keys.add(r.get("value").toString()));
  if(!keys.isEmpty()){
   var metadata=jdbc.queryForList("SELECT "+columns+" FROM "+table+" WHERE "+id+" IN ("+String.join(",",Collections.nCopies(keys.size(),"?"))+")",keys.toArray());Map<String,Map<String,Object>> index=new HashMap<>();for(var item:metadata)index.put(String.valueOf(item.get(Set.of("users","active-users").contains(domain)?"userId":domain.equals("tasks")||domain.equals("directories")?"taskId":"id")),item);
   items.forEach(r->r.put("item",index.get(r.get("value").toString())));selected.forEach(r->r.put("item",index.get(r.get("value").toString())));
  }
  return result;
 }
}
