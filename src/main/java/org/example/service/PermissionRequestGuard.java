package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;
import java.util.*;

/** 固定接口目录，不接收任何用户提供的接口路径、SQL 或表达式。 */
@Service
public class PermissionRequestGuard {
    public static final String BODY=PermissionRequestGuard.class.getName()+".body";
    private final PermissionService access;private final JdbcTemplate jdbc;
    public PermissionRequestGuard(PermissionService access,JdbcTemplate jdbc){this.access=access;this.jdbc=jdbc;}
    public void check(HttpServletRequest request,JsonNode body){
        Long actor=access.actor();if(actor==null)return;
        String path=request.getRequestURI().substring(request.getContextPath().length()).replaceAll("/+$","");
        if(path.startsWith("/system/password")||path.equals("/system/logout")||path.equals("/system/login"))return;
        var state=access.state(actor);boolean get="GET".equals(request.getMethod());
        if(path.equals("/system/access")||path.endsWith("/capabilities"))return;
        if(get && Set.of("/list-filters/admin-users/filter-options","/list-filters/tasks/export","/list-filters/admin-users","/list-filters/admin-roles","/list-filters/audits","/list-filters/budget","/list-filters/admin-operations","/list-filters/admin-departments","/list-filters/budget-versions","/list-filters/tasks","/list-filters/performance","/list-filters/achievements","/list-filters/core","/list-filters/records","/list-filters/notices","/list-filters/supervision").contains(path)){return;} // Controller resolves the exact module permission before reading any rows.
        if(path.startsWith("/system/download/")||path.startsWith("/uploads/")){
            Long file=path.startsWith("/system/download/")?last(path):jdbc.queryForList("SELECT file_id FROM sys_file WHERE (file_path=? OR file_url=?) AND COALESCE(is_delete,0)=0",Long.class,path,path).stream().findFirst().orElse(null);
            if(file==null||!access.canFile(actor,file))deny("材料不存在或不在授权范围内");return;
        }
        if(!access.enforced(actor)||state.admin())return; // 未部署权限结构时兼容旧入口；已迁移账号全部执行统一校验。
        if(path.equals("/biz/tasks/manage/update"))access.requireAdmin(actor); // 历史全字段接口不可用作委派移交入口。
        String action=action(path,get);
        if(path.equals("/manage/transfer/logs")&&"PERFORMANCE".equalsIgnoreCase(request.getParameter("targetType")))action="performance.transfer";
        if(action!=null) {
            if(action.equals("upload")){if(state.permissions().stream().noneMatch(p->p.endsWith(".fill")))deny("没有材料上传权限");}
            else if(action.endsWith(".pending")){String domain=action.substring(0,action.indexOf('.'));if(!access.allowed(actor,domain+".audit")&&!access.allowed(actor,domain+".archive"))deny("没有待办审核权限");}
            else access.require(actor,action);
        } else if(path.startsWith("/admin/")||path.startsWith("/system/users/"))access.requireAdmin(actor);
        else if(path.equals("/system/allUsers")||path.startsWith("/system/dept/")) {if(state.permissions().stream().noneMatch(p->p.endsWith(".read")))deny("没有业务查阅权限");}
        else deny("此接口未在当前账号的功能权限中开放");
        Map<String,String> variables=variables(request);Integer year=integer(request.getParameter("year"));if(year==null&&body!=null&&body.hasNonNull("year"))year=body.get("year").asInt();
        if(path.startsWith("/dashboard/dept/")&&variables.containsKey("deptId")){Long department=number(variables.get("deptId"));if(access.visibleTasks(actor,null).stream().noneMatch(t->Objects.equals(t.getDeptId(),department)))deny("部门不在授权范围内");}
        if(path.equals("/system/alert")&&body!=null&&body.hasNonNull("source_id"))scope(actor,"tasks",value(body,"source_id"),null);
        if(path.startsWith("/system/upload/")){Long object=number(variables.get("task_id"));if(object==null||!(access.allowed(actor,"tasks.fill")&&access.canRead(actor,"tasks",object,null)||access.allowed(actor,"performance.fill")&&access.canRead(actor,"performance",object,null)))deny("上传对象不存在或没有填报权限");}
        if(year==null)year=integer(variables.get("year"));
        if(path.startsWith("/biz/")) {
            Long task=number(variables.get("taskId"));
            if(request.getParameter("task_id")!=null)task=number(request.getParameter("task_id"));
            if(request.getParameter("parent_id")!=null)task=number(request.getParameter("parent_id"));
            if(body!=null){if(body.has("third_task_id"))task=value(body,"third_task_id");else if(body.has("task_id"))task=value(body,"task_id");else if(body.has("taskId"))task=value(body,"taskId");}
            Long sub=path.contains("/audit/logs/")?number(variables.get("subId")):body==null?null:value(body,"sub_id");
            if(sub!=null)task=linked("SELECT task_id FROM biz_material_submission WHERE sub_id=?",sub);
            if(task!=null)scope(actor,"tasks",task,year);
        }
        if(path.startsWith("/performance")) {
            Long perf=number(variables.get("perfId"));if(perf==null)perf=number(variables.get("prefId"));if(perf==null)perf=number(request.getParameter("pref_id"));
            Long sub=number(variables.get("subId"));if(sub==null&&body!=null)sub=value(body,"sub_id");
            if(sub!=null){var rows=jdbc.queryForList("SELECT perf_id,year,flow_status FROM biz_performance_submission WHERE sub_id=?",sub);if(rows.isEmpty())deny("审核记录不存在或无权访问");var row=rows.get(0);perf=PermissionService.number(row.get("perf_id"));year=(int)PermissionService.number(row.get("year"));if(!get&&path.endsWith("/audit"))access.require(actor,PermissionService.number(row.get("flow_status"))==20?"performance.archive":"performance.audit");}
            if(perf!=null)scope(actor,"performance",perf,year);
        }
        if(path.startsWith("/achievement")) {
            if(!get&&path.endsWith("/audit"))access.require(actor,"achievements.audit");
            Long ach=number(variables.get("achId"));if(ach==null)ach=number(variables.get("id"));
            Long sub=number(variables.get("subId"));if(sub==null&&body!=null)sub=value(body,"sub_id");
            if(sub!=null)ach=linked("SELECT ach_id FROM biz_achievement_submission WHERE sub_id=?",sub);
            if(ach!=null)scope(actor,"achievements",ach,year);
        }
        if(path.startsWith("/core-indicators")) {
            Long indicator=number(variables.get("indicatorId"));
            if(path.contains("/reports/")){Long report=number(variables.get("id"));var rows=jdbc.queryForList("SELECT indicator_id,record_year,flow_status FROM biz_core_report WHERE report_id=?",report);if(rows.isEmpty())deny("填报不存在或无权访问");var row=rows.get(0);indicator=PermissionService.number(row.get("indicator_id"));year=(int)PermissionService.number(row.get("record_year"));if(!get&&path.endsWith("/audit"))access.require(actor,PermissionService.number(row.get("flow_status"))==20?"core.archive":"core.audit");}
            if(year==null&&body!=null&&body.has("year"))year=body.get("year").asInt();if(indicator!=null)scope(actor,"core",indicator,year);
        }
        if(path.startsWith("/work-records/")) {Long id=number(variables.get("id"));if(id!=null)scope(actor,"records",id,year);if(body!=null&&path.endsWith("/export")&&body.path("ids").isArray())for(JsonNode idNode:body.get("ids"))scope(actor,"records",idNode.asLong(),year);}
        if(path.startsWith("/budget")) {if(path.contains("/versions/")&&variables.containsKey("versionId")){Long id=number(variables.get("versionId"));Long y=linked("SELECT year FROM biz_budget_import_version WHERE version_id=?",id);if(y==null)deny("预算版本不存在或无权访问");year=y.intValue();}if(!access.budgetScope(actor,year))deny("预算年度不在授权范围内");}
        if(path.startsWith("/manage/")) {
            String domain=path.contains("performance")||path.endsWith("/performances")?"performance":"tasks";
            Long id=number(variables.get("id"));if(id==null)id=number(request.getParameter("perfId"));if(id==null)id=number(request.getParameter("targetId"));if(id==null&&body!=null)id=value(body,"perfId");
            if(path.endsWith("/logs")&&"PERFORMANCE".equalsIgnoreCase(request.getParameter("targetType")))domain="performance";
            if(id!=null)scope(actor,domain,id,year);
            if(body!=null&&path.startsWith("/manage/transfer/")&&body.path("ids").isArray())for(JsonNode target:body.get("ids"))scope(actor,domain,target.asLong(),year);
            if(body!=null&&path.startsWith("/manage/performance-relation"))for(String field:List.of("addTaskIds","removeTaskIds"))if(body.path(field).isArray())for(JsonNode target:body.get(field))scope(actor,"tasks",target.asLong(),year);
            if(body!=null&&body.path("targets").isArray())for(JsonNode target:body.get("targets"))scope(actor,domain,target.path("targetId").asLong(),year);
        }
        if(!get&&path.equals("/biz/audit")&&body!=null){Long stage=linked("SELECT flow_status FROM biz_material_submission WHERE sub_id=?",value(body,"sub_id"));access.require(actor,Objects.equals(stage,30L)?"tasks.archive":"tasks.audit");}
    }
    private String action(String p,boolean get){
        if(p.startsWith("/expert-reviews"))return get?"expert.read":"expert.write";
        if(p.startsWith("/admin/overview"))return "overview.read";
        if(p.startsWith("/dashboard"))return get?"dashboard.read":"management.refresh";
        if(p.startsWith("/scheduled")||p.equals("/performance/refresh"))return "management.refresh";
        if(p.startsWith("/system/notice"))return get||p.matches("/system/notice/[0-9]+")?"messages.read":"messages.send";
        if(p.equals("/system/alert"))return "messages.send";
        if(p.startsWith("/system/upload/"))return "upload";
        if(p.startsWith("/manage/transfer")){if(p.endsWith("/logs"))return "tasks.transfer";return p.endsWith("/performances")?"performance.transfer":"tasks.transfer";}
        if(p.startsWith("/manage/performance-relation"))return "performance.relations";
        if(p.startsWith("/manage/tasks"))return "tasks.attributes";
        String domain=p.startsWith("/biz")?"tasks":p.startsWith("/performance")?"performance":p.startsWith("/achievement")?"achievements":p.startsWith("/core-indicators")?"core":p.startsWith("/work-records")?"records":p.startsWith("/budget")?"budget":null;
        if(domain==null)return null;
        if(p.contains("/withdraw")||p.contains("/drawback"))return domain+".withdraw";
        if(p.contains("/audit/file/"))return domain+".materials";
        if(p.contains("/audit"))return domain+(get?(p.endsWith("/todo")?".pending":".history"):".pending");
        if(p.contains("/export"))return domain+".export";
        if(p.contains("/delete"))return domain+".delete";
        if(p.contains("/withdraw")||p.contains("/drawback"))return domain+".withdraw";
        if(p.contains("/restore"))return "budget.restore";
        if(p.endsWith("/lock"))return "budget.lock";
        if(p.contains("/versions"))return "budget.versions";
        if(p.contains("/manage/add"))return "tasks.create";
        if(p.contains("/manage/finish"))return "tasks.finish";
        if(p.contains("/manage/update"))return "tasks.transfer";
        if(p.endsWith("/manage/options"))return "tasks.create";
        return domain+(get?".read":".fill");
    }
    public void scope(Long actor,String domain,Long id,Integer year){boolean ok=domain.equals("performance")&&year==null?access.visiblePerformances(actor,null).stream().anyMatch(p->Objects.equals(p.getPerfId(),id)):access.canRead(actor,domain,id,year);if(!ok)throw new AdminManagementException(domain.equals("records")?404:403,"对象不存在或不在授权范围内");}
    @SuppressWarnings("unchecked")private Map<String,String> variables(HttpServletRequest r){Object v=r.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);return v instanceof Map?(Map<String,String>)v:Map.of();}
    private Long linked(String sql,Long id){if(id==null)return null;return jdbc.queryForList(sql,Long.class,id).stream().findFirst().orElse(null);}
    private Long last(String path){return number(path.substring(path.lastIndexOf('/')+1));}
    private Long number(String v){try{return v==null?null:Long.valueOf(v);}catch(NumberFormatException e){return null;}}
    private Integer integer(String v){Long n=number(v);return n==null?null:n.intValue();}
    private Long value(JsonNode body,String name){return body.hasNonNull(name)?body.get(name).asLong():null;}
    private void deny(String message){throw new AdminManagementException(403,message);}
}
