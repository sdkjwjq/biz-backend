package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.*;
import org.example.mapper.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import java.util.*;
import java.math.*;
import java.time.*;
import java.util.stream.Collectors;
import static org.example.service.ListFilterQuery.*;

/** Read-only projections: access is resolved before field options and predicates. */
@Service
public class BusinessListFilterService {
    private final PermissionService access; private final BizService biz; private final PerformanceService performance;
    private final AchievementService achievements;private final SysMapper users;private final WorkShortcutMapper shortcuts;
    private final JdbcTemplate jdbc;private final ObjectMapper json;
    public BusinessListFilterService(PermissionService access,BizService biz,PerformanceService performance,AchievementService achievements,SysMapper users,WorkShortcutMapper shortcuts,JdbcTemplate jdbc,ObjectMapper json){this.access=access;this.biz=biz;this.performance=performance;this.achievements=achievements;this.users=users;this.shortcuts=shortcuts;this.jdbc=jdbc;this.json=json;}
    @org.springframework.beans.factory.annotation.Autowired private CoreIndicatorService core;
    @org.springframework.beans.factory.annotation.Autowired private WorkRecordService records;
    @org.springframework.beans.factory.annotation.Autowired private WorkRecordMapper recordMapper;
    @org.springframework.beans.factory.annotation.Autowired private SysService system;
    @org.springframework.beans.factory.annotation.Autowired private AdminManagementMapper administration;
    @org.springframework.beans.factory.annotation.Autowired private AdminManagementService management;
    @org.springframework.beans.factory.annotation.Autowired private BudgetService budget;
    public record Data(List<Map<String,Object>> rows,List<Field> fields){}
    public Map<String,Object> query(String domain,Long actor,MultiValueMap<String,String> params){
        if(domain.equals("budget"))return budgetQuery(actor,params);
        boolean exporting="export".equals(params.getFirst("purpose"));
        if(exporting){if(domain.equals("admin-operations"))access.requireAdmin(actor);else if(domain.equals("achievements"))access.require(actor,"achievements.export");else throw new AdminManagementException(400,"该模块不支持此导出方式");}
        Data data=domain.equals("audits")?auditData(actor,params.getFirst("tab")):data(domain,actor,params.getFirst("f.year"),params.getFirst("f.month"));var query=new ListFilterQuery(params,data.fields());
        var matched=data.rows().stream().filter(row->query.matches(row,data.fields())).filter(row->auditMatches(row,query,data.fields())).toList();
        int page=positive(params.getFirst("page"),1),size=Math.min(100,positive(params.getFirst("pageSize"),10));int safe=Math.min(page,Math.max(1,(matched.size()+size-1)/size));
        Map<String,Object> result=new LinkedHashMap<>();result.put("ids",matched.stream().map(r->r.get("id")).toList());result.put("total",matched.size());result.put("fields",data.fields());result.put("page",safe);result.put("pageSize",size);
        result.put("items",(exporting?matched:matched.subList((safe-1)*size,Math.min(safe*size,matched.size()))).stream().map(r->r.getOrDefault("item",Map.of())).toList());return result;
    }
    @SuppressWarnings("unchecked")
    private boolean auditMatches(Map<String,Object> row,ListFilterQuery query,List<Field> fields){
        if(!row.containsKey("auditRows"))return true;
        var relevant=fields.stream().filter(f->Set.of("stage","handler","handlerDept").contains(f.key())).toList();
        var rows=(List<Map<String,Object>>)row.get("auditRows");
        if(rows.isEmpty())return query.matches(Map.of(),relevant);
        return rows.stream().anyMatch(r->{Map<String,Object> value=new HashMap<>();value.put("stage",r.get("flowStatus"));value.put("handler",r.get("handlerId"));value.put("handlerDept",r.get("handlerDeptId"));return query.matches(value,relevant);});
    }
    private int positive(String value,int fallback){if(value==null||value.isBlank())return fallback;try{int n=Integer.parseInt(value);if(n>0)return n;}catch(Exception ignored){}throw new AdminManagementException(400,"分页参数不正确");}
    public Data data(String domain,Long actor,String year){return data(domain,actor,year,null);}
    public Data data(String domain,Long actor,String year,String month){
        if(domain.equals("admin-users")){access.requireAdmin(actor);return userData();}
        if(domain.equals("admin-roles")){access.requireAdmin(actor);return roleData();}
        if(domain.equals("admin-operations")){access.requireAdmin(actor);return operationData(actor);}
        if(domain.equals("admin-departments")){access.requireAdmin(actor);return departmentData();}
        if(domain.equals("budget-versions")){access.require(actor,"budget.versions");return versionData(actor);}
        access.require(actor,switch(domain){case "tasks"->"tasks.read";case "performance"->"performance.read";case "achievements"->"achievements.read";case "core"->"core.read";case "records"->"records.read";case "notices"->"messages.read";case "supervision"->"tasks.read";default->throw new AdminManagementException(400,"不支持的查询模块");});
        Integer y=null;if(year!=null&&!year.isBlank()&&!year.equals("missing")){try{y=Integer.valueOf(year);}catch(Exception e){throw new AdminManagementException(400,"年度不正确");}if(y<2025||y>2029)throw new AdminManagementException(400,"请选择2025—2029年度");}
        List<Map<String,Object>> rows;List<Field> fields=new ArrayList<>();
        Map<String,String> person=new HashMap<>(),dept=new HashMap<>();
        for(var u:users.getAllUsers())person.put(str(u.getUserId()),u.getNickName()==null?str(u.getUserName()):u.getNickName());
        // Names are returned only for IDs encountered in authorized rows.
        if(domain.equals("tasks")||domain.equals("supervision")){rows=withinYear(tasks(actor),year);taskFields(fields,rows,person,dept);if(domain.equals("supervision"))plain(fields,"overdueDays","逾期天数","number",false,"进度与时间");}
        else if(domain.equals("performance")){rows=performances(actor,y);performanceFields(fields,rows,person,dept);}
        else if(domain.equals("achievements")){rows=withinYear(achievementRows(actor),year);achievementFields(fields,rows,person,dept);}
        else if(domain.equals("core")){int selectedMonth=month==null||month.isBlank()?LocalDate.now(ZoneId.of("Asia/Shanghai")).getMonthValue():positive(month,1);if(selectedMonth>12)throw new AdminManagementException(400,"月份不正确");rows=coreRows(actor,y==null?2026:y,selectedMonth);coreFields(fields,rows,person);}
        else if(domain.equals("records")){rows=withinYear(recordRows(actor),year);recordFields(fields,rows,person,dept);}
        else{rows=noticeRows(actor);noticeFields(fields,rows,person);}
        return new Data(rows,fields);
    }
    private Data auditData(Long actor,String tab){
        if(tab!=null&&!Set.of("todo","done").contains(tab))throw new AdminManagementException(400,"审核页签不正确");
        boolean todo=!"done".equals(tab);var state=access.state(actor);var permissions=state.permissions();
        List<Map<String,Object>> rows=new ArrayList<>();Map<String,String> persons=new HashMap<>(),departments=new HashMap<>();Map<Long,SysUser> people=new HashMap<>();
        for(var u:users.getAllUsers()){people.put(u.getUserId(),u);persons.put(str(u.getUserId()),u.getNickName());}
        for(var d:administration.departments())departments.put(str(d.get("deptId")),str(d.get("deptName")));
        Map<Long,Map<String,Object>> taskRows=new HashMap<>();
        if(permissions.contains("tasks.read"))for(var t:tasks(actor))taskRows.put(id(t.get("id")),t);
        boolean any=false;
        for(String domain:List.of("tasks","performance","achievements","core")){
            if(todo?!(permissions.contains(domain+".audit")||permissions.contains(domain+".archive")):!permissions.contains(domain+".history"))continue;any=true;
            List<?> source;
            if(domain.equals("tasks")){
                if(todo)source=biz.getTodoAudits(actor);
                else if(taskRows.isEmpty())source=List.of();
                else source=jdbc.queryForList("SELECT sub_id subId,task_id taskId,submit_by submitBy,submit_dept_id submitDeptId,submit_time submitTime,flow_status flowStatus FROM biz_material_submission WHERE COALESCE(is_delete,0)=0 AND (flow_status=40 OR flow_status<0) AND task_id IN ("+String.join(",",Collections.nCopies(taskRows.size(),"?"))+")",taskRows.keySet().toArray());
            }else if(domain.equals("performance"))source=(List<?>)(todo?performance.getTodoPerformanceAudits(actor):performance.getPerformanceAuditRecords(actor));
            else if(domain.equals("achievements"))source=(List<?>)(todo?achievements.getTodoAchievementAudits(actor):achievements.getAchievementAuditRecords(actor));
            else source=core.audits(actor,todo);
            Map<Long,Object> handled=new HashMap<>();if(!todo){String table=Map.of("tasks","biz_audit_log","performance","biz_performance_audit_log","achievements","biz_achievement_audit_log","core","biz_core_audit_log").get(domain);String column=domain.equals("core")?"report_id":"sub_id";String action=domain.equals("core")?"action IN('通过','退回')":"LOWER(action_type) IN('pass','reject')";for(var log:jdbc.queryForList("SELECT "+column+" id,MAX(create_time) handledTime FROM "+table+" WHERE "+action+" GROUP BY "+column))handled.put(id(log.get("id")),log.get("handledTime"));}
            for(var value:source){var r=row(value);long object=id(r.get(domain.equals("tasks")?"taskId":domain.equals("performance")?"perfId":domain.equals("achievements")?"achId":"indicatorId"));
                if(!access.canRead(state,domain,object,r.get("year")==null?null:(int)id(r.get("year"))))continue;
                int status=(int)id(r.get("flowStatus"));if(!todo&&status>=0&&status!=(domain.equals("tasks")?40:domain.equals("core")?30:30))continue;
                String type=domain.equals("tasks")?"task":domain.equals("achievements")?"achievement":domain;Object record=r.get(domain.equals("core")?"reportId":"subId");
                r.put("handledTime",handled.get(id(record)));r.put("id",type+":"+str(record));r.put("type",type);var person=people.get(id(r.get("submitBy")));if(r.get("submitDeptId")==null&&person!=null)r.put("submitDeptId",person.getDeptId());
                String name=str(r.get(domain.equals("performance")?"perfName":domain.equals("achievements")?"achName":"indicatorName"));String code=str(r.get(domain.equals("core")?"performanceCode":"perfCode"));
                var task=taskRows.get(object);if(domain.equals("tasks")&&task!=null){name=str(task.get("taskName"));code=str(task.get("taskCode"));r.put("year",task.get("year"));r.put("root",task.get("root"));r.put("rootOptions",task.get("rootOptions"));}
                if(domain.equals("achievements")&&r.get("gotTime")!=null)r.put("year",LocalDate.parse(ListFilterQuery.date(r.get("gotTime"))).getYear());
                r.put("keyword",code+" "+name+" "+str(record));r.put("stage",domain+":"+status);r.put("result",status<0?"rejected":"passed");
                LocalDate submitted=r.get("submitTime")==null?null:LocalDate.parse(ListFilterQuery.date(r.get("submitTime")));r.put("waitingDays",todo&&submitted!=null?Math.max(0,java.time.temporal.ChronoUnit.DAYS.between(submitted,LocalDate.now(ZoneId.of("Asia/Shanghai")))):null);
                rows.add(r);
            }
        }
        if(!any)throw new AdminManagementException(403,"没有审核中心查阅权限");
        List<Field> f=new ArrayList<>();multi(f,"type","业务类型",true,rows,Map.of("task","任务佐证","performance","绩效填报","achievement","成果归档","core","核心指标"));multi(f,"submitDeptId","提交部门",true,rows,departments);multi(f,"submitBy","提交人",true,rows,persons);plain(f,"keyword","编号／名称","text",true,"基础");
        multi(f,"year","业务年度",false,rows,Map.of());plain(f,"submitTime","提交时间","date",false,"时间");
        if(todo){Map<String,String> stages=new HashMap<>();for(String d:List.of("tasks","performance","achievements","core"))for(int n:List.of(10,20,30))stages.put(d+":"+n,(d.equals("tasks")?"任务":d.equals("performance")?"绩效":d.equals("achievements")?"成果":"核心指标")+" · "+(d.equals("tasks")?(n==10?"专业群审核":n==20?"归口审核":"归档"):d.equals("performance")?(n==10?"专业群审核":"归档"):d.equals("core")?(n==10?"归口审核":"归档"):"归档"));multi(f,"stage","待审阶段",false,rows,stages);plain(f,"waitingDays","等待天数","number",false,"时间");}
        else {select(f,"result","处理结果",false,"passed","通过","rejected","驳回");plain(f,"handledTime","最近处理时间","date",false,"时间");}
        Map<String,String> roots=new HashMap<>();for(var r:rows)if(r.get("rootOptions") instanceof List<?> options)for(var value:options){var o=(Map<?,?>)value;roots.put(str(o.get("value")),str(o.get("label")));}multi(f,"root","一级改革任务",false,rows,roots);return new Data(rows,f);
    }
    private Map<String,Object> budgetQuery(Long actor,MultiValueMap<String,String> params){
        access.require(actor,"budget.read");
        int year=positive(params.getFirst("f.year"),2026),month=positive(params.getFirst("f.month"),LocalDate.now(ZoneId.of("Asia/Shanghai")).getMonthValue());
        if(year<2025||year>2029||month>12)throw new AdminManagementException(400,"请选择有效的建设年度和月份");
        if(!access.budgetScope(actor,year))throw new AdminManagementException(403,"无权查看该年度预算");
        if(params.getOrDefault("f.expense",List.of()).stream().anyMatch(v->!Set.of("goods","capital","other").contains(v)))throw new AdminManagementException(400,"支出类别不正确");
        var sheet=budget.getBudget(year,month);var rows=new ArrayList<Map<String,Object>>();Map<String,String> tasks=new LinkedHashMap<>(),sources=new LinkedHashMap<>();
        for(var source:sheet.getSources())sources.put(source.getSourceKey(),source.getSourceName());
        for(var task:sheet.getTasks()){
            Map<String,Object> r=new LinkedHashMap<>();r.put("task",task.getTaskIndex());r.put("year",year);r.put("month",month);tasks.put(str(task.getTaskIndex()),task.getTaskName());
            BigDecimal sum=task.getGoods()==null||task.getCapital()==null||task.getOther()==null?null:task.getGoods().add(task.getCapital()).add(task.getOther());
            r.put("hasExpense",sum==null?"unknown":sum.signum()==0?"no":"yes");
            r.put("ratio",sum==null||task.getAmount()==null||task.getAmount().signum()<=0?null:sum.multiply(BigDecimal.valueOf(100)).divide(task.getAmount(),8,RoundingMode.HALF_UP));rows.add(r);
        }
        List<Field> f=new ArrayList<>();years(f,false);months(f);multi(f,"task","改革任务",true,rows,tasks);
        f.add(new Field("source","资金来源","multi",true,"基础",sources.entrySet().stream().map(e->Map.<String,Object>of("value",e.getKey(),"label",e.getValue())).toList()));
        add(f,"expense","支出类别","multi",false,"执行情况",opts("goods","商品和服务支出","capital","资本性支出","other","其他支出"));
        select(f,"hasExpense","有无支出",false,"yes","有支出","no","无支出","unknown","待核实");plain(f,"ratio","年度预算执行率（%）","number",false,"执行情况");
        var query=new ListFilterQuery(params,f);var taskFields=f.stream().filter(x->!Set.of("source","expense").contains(x.key())).toList();
        var result=new LinkedHashMap<String,Object>();result.put("fields",f);result.put("ids",rows.stream().filter(r->query.matches(r,taskFields)).map(r->r.get("task")).toList());result.put("budget",sheet);result.put("imported",sheet.getSheet().getSheetId()!=null);return result;
    }
    private Data userData(){
        var rows=new ArrayList<Map<String,Object>>();Map<Long,SysUser> accounts=users.getAllUsers().stream().collect(Collectors.toMap(SysUser::getUserId,u->u));Map<String,String> roles=new LinkedHashMap<>(),departments=new LinkedHashMap<>();access.roles().forEach(r->roles.put(r.code(),r.name()));administration.departments().forEach(d->departments.put(str(d.get("deptId")),str(d.get("deptName"))));
        Map<Long,String> leadership=new HashMap<>();for(var r:jdbc.queryForList("SELECT user_id,scope FROM sys_leadership_access"))leadership.put(id(r.get("user_id")),str(r.get("scope")));
        Set<Long> extraDuties=new HashSet<>(jdbc.queryForList("SELECT c.leader_id FROM biz_level4_task c JOIN biz_task t ON t.task_id=c.parent_id WHERE COALESCE(c.is_delete,0)=0 AND COALESCE(t.is_delete,0)=0 AND c.leader_id IS NOT NULL UNION SELECT current_handler_id FROM biz_core_report WHERE flow_status IN(10,20) AND current_handler_id IS NOT NULL",Long.class));
        for(var item:administration.users()){
            long id=id(item.get("userId"));var user=accounts.get(id);if(user==null)continue;var r=new LinkedHashMap<String,Object>(item);r.put("id",id);r.put("keyword",id+" "+str(item.get("userName"))+" "+str(item.get("nickName")));var roleCodes=access.assignedRoleCodes(user);r.put("roles",roleCodes);r.put("leadership",leadership.getOrDefault(id,"NONE"));
            var policy=access.policy(id);r.put("personal",flag(!policy.overrides().isEmpty()||!policy.scopes().isEmpty()||!policy.roles().isEmpty()||!"LEGACY".equals(policy.mode())));boolean expert=access.roles().stream().anyMatch(role->roleCodes.contains(role.code())&&role.enabled()&&role.permissions().contains("expert.read"));if(policy.overrides().containsKey("expert.read"))expert=Boolean.TRUE.equals(policy.overrides().get("expert.read"));r.put("expert",flag(expert));r.put("force",flag(yes(item.get("forcePasswordChange"))));
            var impact=administration.userImpact(id);r.put("duties",flag(extraDuties.contains(id)||impact.entrySet().stream().filter(e->!e.getKey().equals("draftWorkRecordCount")).anyMatch(e->id(e.getValue())>0)));
            var dept=user.getDeptId()==null?null:users.getDeptById(user.getDeptId());item.put("canViewAchievement",org.example.utils.AchievementPermissionUtil.canViewAchievement(user,dept));item.put("canUploadAchievement",org.example.utils.AchievementPermissionUtil.canUploadAchievement(user,dept));r.put("item",item);rows.add(r);
        }
        List<Field> f=new ArrayList<>();plain(f,"keyword","姓名／工号","text",true,"基础");multi(f,"deptId","部门",true,rows,departments);multi(f,"roles","角色模板",true,rows,roles);select(f,"status","启停状态",true,"1","正常","0","停用");select(f,"role","原账号身份",false,"0","管理员","1","普通用户","2","负责人");select(f,"leadership","领导查阅配置",false,"ALL","全校","DEPARTMENTS","指定部门","NONE","未配置");select(f,"expert","专家身份",false,"yes","是","no","否");select(f,"force","强制改密",false,"yes","是","no","否");select(f,"personal","个人功能／范围授权",false,"yes","有","no","无");select(f,"duties","活动职责",false,"yes","有","no","无");return new Data(rows,f);
    }
    private Data roleData(){
        var rows=new ArrayList<Map<String,Object>>();Set<String> assigned=new HashSet<>();for(var account:users.getAllUsers())if(!Integer.valueOf(1).equals(account.getIsDelete()))assigned.addAll(access.assignedRoleCodes(account));
        for(var role:access.roles()){var r=row(role);r.put("id",role.code());r.put("keyword",role.name()+" "+role.code());r.put("enabled",flag(role.enabled()));r.put("builtin",flag(role.builtin()));r.put("assigned",flag(assigned.contains(role.code())));r.put("modules",role.permissions().stream().map(p->p.split("\\.")[0]).distinct().toList());List<String> modes=new ArrayList<>();for(var scope:role.scopes().values())if(scope instanceof Map<?,?> m)modes.add(str(m.get("mode")));r.put("scopeTypes",modes);rows.add(r);}
        List<Field> f=new ArrayList<>();plain(f,"keyword","角色名称","text",true,"基础");select(f,"enabled","启停状态",true,"yes","启用","no","停用");select(f,"builtin","模板来源",true,"yes","内置","no","自定义");select(f,"assigned","已分配人员",true,"yes","是","no","否");multi(f,"permissions","包含功能权限",false,rows,PermissionCatalog.ACTIONS);multi(f,"modules","业务模块",false,rows,PermissionCatalog.MODULES);multi(f,"scopeTypes","数据范围类型",false,rows,Map.of("ALL","全校","OWN","本人","NONE","无","CUSTOM","自定义"));return new Data(rows,f);
    }
    private Data operationData(Long actor){
        var rows=new ArrayList<Map<String,Object>>();Map<String,String> names=new HashMap<>();
        for(var item:management.operationRows(actor)){var r=new LinkedHashMap<String,Object>(item);r.put("id",str(item.get("recordType"))+":"+str(item.get("recordId")));r.put("item",item);r.put("keyword",str(item.get("targetName"))+" "+str(item.get("targetId")));names.put(str(item.get("operatorId")),str(item.get("operatorName")));rows.add(r);}
        List<Field> f=new ArrayList<>();multi(f,"module","模块",true,rows,Map.of());multi(f,"operatorId","操作人",true,rows,names);plain(f,"createTime","操作时间","date",true,"基础");plain(f,"keyword","对象关键词","text",true,"基础");multi(f,"action","动作",false,rows,Map.of());multi(f,"targetType","对象类型",false,rows,Map.of());plain(f,"targetId","对象编号","text",false,"操作对象");plain(f,"batchId","批次号","text",false,"操作对象");plain(f,"reason","原因关键词","text",false,"操作说明");return new Data(rows,f);
    }
    private Data departmentData(){
        List<Map<String,Object>> rows=new ArrayList<>();Map<String,String> names=new HashMap<>();
        for(var item:administration.departments()){var r=new LinkedHashMap<String,Object>(item);r.put("item",item);r.put("id",item.get("deptId"));r.put("keyword",str(item.get("deptId"))+" "+str(item.get("deptName")));r.put("missingLeader",flag(item.get("leaderId")==null));r.put("hasUsers",flag(id(item.get("activeUsers"))>0));r.put("hasBusiness",flag(id(item.get("activeTasks"))+id(item.get("activePerformances"))>0));r.put("mismatch",flag(id(item.get("taskLeaderMismatches"))+id(item.get("performanceLeaderMismatches"))>0));names.put(str(item.get("leaderId")),str(item.get("leaderName")));rows.add(r);}
        List<Field> f=new ArrayList<>();plain(f,"keyword","名称／编号","text",true,"基础");multi(f,"leaderId","负责人",true,rows,names);select(f,"status","启停状态",true,"0","正常","1","停用");select(f,"missingLeader","缺少负责人",false,"yes","是","no","否");select(f,"hasUsers","有正常账号",false,"yes","是","no","否");select(f,"hasBusiness","有有效任务／绩效",false,"yes","是","no","否");select(f,"mismatch","审核人与部门负责人不一致",false,"yes","是","no","否");return new Data(rows,f);
    }
    private Data versionData(Long actor){
        var rows=jdbc.queryForList("SELECT v.version_id versionId,v.sheet_id sheetId,v.year,v.month,v.version_no versionNo,v.operation_type operationType,v.source_version_id sourceVersionId,v.file_id fileId,v.file_name fileName,v.created_by createdBy,u.nick_name createdByName,v.create_time createTime,v.remark,(v.version_no=(SELECT MAX(x.version_no) FROM biz_budget_import_version x WHERE x.year=v.year AND x.month=v.month AND x.is_delete=0)) isCurrent FROM biz_budget_import_version v LEFT JOIN sys_user u ON u.user_id=v.created_by WHERE v.is_delete=0 ORDER BY v.year DESC,v.month DESC,v.version_no DESC");
        rows.removeIf(r->!access.budgetScope(actor,(int)id(r.get("year"))));
        Map<String,String> names=new HashMap<>();for(var r:rows){r.put("current",yes(r.get("isCurrent")));var item=new LinkedHashMap<>(r);r.put("item",item);r.put("id",r.get("versionId"));r.put("effective",flag(yes(r.get("isCurrent"))));names.put(str(r.get("createdBy")),str(r.get("createdByName")));}
        List<Field> f=new ArrayList<>();years(f,false);months(f);multi(f,"createdBy","导入人",true,rows,names);plain(f,"createTime","导入时间","date",true,"基础");select(f,"operationType","版本来源",false,"IMPORT","导入","RESTORE","恢复","INITIAL","初始版本");select(f,"effective","当前有效版本",false,"yes","是","no","否");return new Data(rows,f);
    }
    private List<Map<String,Object>> withinYear(List<Map<String,Object>> rows,String year){
        if(year==null||year.isBlank())return rows;
        return rows.stream().filter(r->year.equals(str(r.get("year")))||(year.equals("missing")&&str(r.get("year")).isBlank())).toList();
    }
    private Map<String,Object> row(Object object){return json.convertValue(object,new TypeReference<LinkedHashMap<String,Object>>(){});}
    private long id(Object value){try{return Long.parseLong(str(value));}catch(Exception e){return 0;}}
    private boolean yes(Object value){return Boolean.TRUE.equals(value)||"1".equals(str(value));}
    private String flag(boolean value){return value?"yes":"no";}
    private void kind(Map<String,Object> r){boolean p=yes(r.get("isPriority")),d=yes(r.get("isDifficult"));r.put("kind",p&&d?List.of("priority","difficult","both"):p?List.of("priority"):d?List.of("difficult"):List.of("normal"));}
    private List<Long> lineage(Map<String,Object> r,Map<Long,Map<String,Object>> all){List<Long> ids=new ArrayList<>();Set<Long> seen=new HashSet<>();for(var cur=all.get(id(r.get("parentId")));cur!=null&&seen.add(id(cur.get("id")));cur=all.get(id(cur.get("parentId"))))ids.add(id(cur.get("id")));return ids;}
    public List<Map<String,Object>> tasks(Long actor){
        var visible=access.visibleTasks(actor,null);Map<Long,Map<String,Object>> all=new HashMap<>();
        for(var t:visible){var r=row(t);r.put("id",t.getTaskId());all.put(t.getTaskId(),r);}
        var ids=visible.stream().filter(t->Integer.valueOf(3).equals(t.getLevel())).map(t->t.getTaskId()).toList();
        Map<Long,List<Map<String,Object>>> states=ids.isEmpty()?Map.of():shortcuts.taskStates(ids).stream().collect(Collectors.groupingBy(r->id(r.get("taskId"))));
        Set<Long> children=new HashSet<>(jdbc.queryForList("SELECT DISTINCT parent_id FROM biz_level4_task WHERE COALESCE(is_delete,0)=0",Long.class));
        List<Map<String,Object>> result=new ArrayList<>();
        for(Long task:ids){var r=all.get(task);r.put("year",r.get("phase"));r.put("keyword",str(r.get("taskCode"))+" "+str(r.get("taskName")));r.put("directory",lineage(r,all));
            var ancestors=(List<?>)r.get("directory");r.put("root",ancestors.stream().filter(v->id(all.get(id(v)).get("level"))==1).toList());r.put("parent",r.get("parentId"));
            for(String key:List.of("root","parent")){Object v=r.get(key);List<?> list=v instanceof List<?> l?l:List.of(v==null?0:v);r.put(key+"Options",list.stream().filter(a->all.containsKey(id(a))).map(a->Map.of("value",str(a),"label",str(all.get(id(a)).get("taskName")))).toList());}
            boolean overdue=id(r.get("phase"))>0&&LocalDate.now(ZoneId.of("Asia/Shanghai")).isAfter(LocalDate.of((int)id(r.get("phase")),12,31))&&!"3".equals(str(r.get("status")));r.put("overdue",flag(overdue));r.put("overdueDays",overdue?java.time.temporal.ChronoUnit.DAYS.between(LocalDate.of((int)id(r.get("phase")),12,31),LocalDate.now(ZoneId.of("Asia/Shanghai"))):0);kind(r);r.put("children",flag(children.contains(task)));
            var pending=states.getOrDefault(task,List.of());r.put("auditRows",pending.stream().filter(v->Set.of(10L,20L,30L).contains(id(v.get("flowStatus")))).toList());r.put("stage",pending.stream().filter(v->id(v.get("flowStatus"))>0&&id(v.get("flowStatus"))<40).map(v->str(v.get("flowStatus"))).toList());
            r.put("handler",pending.stream().filter(v->id(v.get("flowStatus"))>0&&id(v.get("flowStatus"))<40).map(v->v.get("handlerId")).filter(Objects::nonNull).toList());r.put("handlerDept",pending.stream().filter(v->id(v.get("flowStatus"))>0&&id(v.get("flowStatus"))<40).map(v->v.get("handlerDeptId")).filter(Objects::nonNull).toList());
            List<String> scope=new ArrayList<>();if(Objects.equals(id(r.get("leaderId")),actor))scope.add("mine");if(((List<?>)r.get("handler")).stream().anyMatch(v->id(v)==actor))scope.add("todo");if(pending.stream().anyMatch(v->id(v.get("flowStatus"))<0))scope.add("returned");r.put("scope",scope);result.add(r);
        }return result;
    }
    private List<Map<String,Object>> performances(Long actor,Integer year){
        Object source=year==null?performance.getAllPerformance(actor):performance.getPerformanceByYear(year,actor);
        List<Map<String,Object>> all=((List<?>)source).stream().map(this::row).toList();Set<Long> parents=all.stream().map(r->id(r.get("parentId"))).collect(Collectors.toSet());
        var directories=((List<?>)performance.getAllPerformance(actor)).stream().map(this::row).toList();
        Map<String,String> categories=new HashMap<>();directories.forEach(r->categories.put(str(r.get("perfId")),str(r.get("perfName"))));
        var taskMap=access.visibleTasks(actor,year).stream().collect(Collectors.toMap(BizTask::getTaskId,t->t));
        Map<Long,Long> roots=new HashMap<>();Map<Long,String> rootNames=new HashMap<>();
        for(var t:taskMap.values()){var current=t;Set<Long> seen=new HashSet<>();while(current!=null&&seen.add(current.getTaskId())){if(Integer.valueOf(1).equals(current.getLevel())){roots.put(t.getTaskId(),current.getTaskId());rootNames.put(current.getTaskId(),current.getTaskName());break;}current=taskMap.get(current.getParentId());}}
        var result=new ArrayList<Map<String,Object>>();var ids=all.stream().map(r->id(r.get("perfId"))).toList();var states=ids.isEmpty()?List.<Map<String,Object>>of():shortcuts.performanceStates(ids);
        var relations=jdbc.queryForList("SELECT r.perf_id,r.task_id,y.year FROM rel_task_performance r JOIN biz_performance_year y ON y.year_id=r.year_id JOIN biz_task t ON t.task_id=r.task_id WHERE COALESCE(r.is_delete,0)=0 AND COALESCE(t.is_delete,0)=0 AND t.level=3 AND t.phase=y.year");
        for(var r:all){long pid=id(r.get("perfId"));if(parents.contains(pid))continue;r.put("id",pid);r.put("year",year);r.put("keyword",str(r.get("perfCode"))+" "+str(r.get("perfName")));r.put("category",r.get("parentId"));r.put("categoryName",categories.get(str(r.get("parentId"))));r.put("mode",(str(r.get("perfCode")).startsWith("1.1.")||str(r.get("perfCode")).startsWith("1.2."))?"auto":"manual");
            Object target=r.get("targetValue"),actual=r.get("currentValue");BigDecimal t=target==null?null:new BigDecimal(str(target)),a=actual==null?null:new BigDecimal(str(actual));boolean valid=t!=null&&t.signum()>0&&a!=null;r.put("attainment",valid?(a.compareTo(t)>=0?"met":"unmet"):"unknown");r.put("ratio",valid?a.multiply(BigDecimal.valueOf(100)).divide(t,8,RoundingMode.HALF_UP):null);r.put("missingTarget",flag(t==null||t.signum()<=0));r.put("missingActual",flag(a==null));
            var pending=states.stream().filter(s->id(s.get("perfId"))==pid&&(year==null||id(s.get("year"))==year)).toList();r.put("stage",pending.stream().filter(s->Set.of(10L,20L).contains(id(s.get("flowStatus")))).map(s->str(s.get("flowStatus"))).toList());r.put("auditStatus",pending.isEmpty()?List.of("none"):pending.stream().map(s->{long status=id(s.get("flowStatus"));return status<0?"returned":status==30?"archived":status==10||status==20?"pending":"none";}).distinct().toList());
            List<String> scope=new ArrayList<>();if(id(r.get("leaderId"))==actor)scope.add("mine");if(pending.stream().anyMatch(s->id(s.get("handlerId"))==actor&&Set.of(10L,20L).contains(id(s.get("flowStatus")))))scope.add("todo");if(pending.stream().anyMatch(s->id(s.get("flowStatus"))<0))scope.add("returned");r.put("scope",scope);
            var related=relations.stream().filter(v->id(v.get("perf_id"))==pid&&(year==null||id(v.get("year"))==year)).toList();
            r.put("root",related.stream().map(v->roots.get(id(v.get("task_id")))).filter(Objects::nonNull).distinct().toList());r.put("rootNames",rootNames);
            r.put("linked",flag(relations.stream().anyMatch(v->id(v.get("perf_id"))==pid&&(year==null||id(v.get("year"))==year))));result.add(r);
        }return result;
    }
    private List<Map<String,Object>> achievementRows(Long actor){var result=new ArrayList<Map<String,Object>>();for(var a:achievements.listAllAchievements(actor)){var r=row(a);r.put("id",a.getAchId());r.put("item",a);String date=date(a.getGotTime());r.put("year",date.isEmpty()?null:date.substring(0,4));r.put("keyword",a.getAchName());r.put("material",flag(a.getFileId()!=null));r.put("competition",str(a.getIsCompetition()));r.put("professionalReview","unknown");r.put("grade",List.of("teDengJiang","yiDengJiang","erDengJiang","sanDengJiang","jinJiang","yinJiang","tongJiang","youShengJiang","budDengDengCi").stream().filter(k->id(r.get(k))>0).toList());result.add(r);}return result;}

    private void months(List<Field> f){List<Map<String,Object>> values=new ArrayList<>();for(int m=1;m<=12;m++)values.add(Map.of("value",String.valueOf(m),"label",m+"月"));add(f,"month","月份","select",true,"基础",values);}
    private List<Map<String,Object>> coreRows(Long actor,int year,int month){List<Map<String,Object>> rows=new ArrayList<>();for(var item:core.list(actor,year,null)){
        var r=new LinkedHashMap<>(item);r.put("id",item.get("indicatorId"));r.put("year",year);r.put("month",month);r.put("keyword",str(item.get("indicatorCode"))+" "+str(item.get("indicatorName")));r.put("root",item.get("sortOrder"));
        var history=core.history(id(item.get("indicatorId")),actor,year,month).stream().filter(v->!yes(v.get("mappingObsolete"))).toList();var latest=history.isEmpty()?Map.<String,Object>of():history.get(0);long state=id(latest.get("flowStatus"));
        r.put("periodStatus",latest.isEmpty()?"empty":state==30?"archived":state==10||state==20?"pending":state<0?"returned":"other");r.put("stage",latest.get("flowStatus"));r.put("submitBy",latest.get("submitBy"));
        var effective=core.effective(id(item.get("indicatorId")),year,month);r.put("archived",flag(effective!=null));Object actual=effective==null?null:effective.get("actualValue"),target=effective==null?item.get("targetValue"):effective.get("targetValue");
        BigDecimal t=target==null?null:new BigDecimal(str(target)),a=actual==null?null:new BigDecimal(str(actual));boolean valid=t!=null&&t.signum()>0&&a!=null;r.put("attainment",valid?(a.compareTo(t)>=0?"met":"unmet"):"unknown");r.put("ratio",valid?a.multiply(BigDecimal.valueOf(100)).divide(t,8,RoundingMode.HALF_UP):null);
        r.put("item",item);rows.add(r);
    }return rows;}
    private void coreFields(List<Field> f,List<Map<String,Object>> r,Map<String,String> p){years(f,false);months(f);select(f,"periodStatus","所选月份填报状态",true,"empty","尚未正式填报","pending","待审核","archived","已归档","returned","已退回／撤回","other","其他");plain(f,"keyword","编号／名称","text",true,"基础");multi(f,"root","一级改革任务序号",false,r,Map.of());multi(f,"submitBy","所选月份填报人",false,r,p);multi(f,"principalId","归口审核人",false,r,p);select(f,"stage","待审阶段",false,"10","待归口审核","20","待双高办归档");select(f,"archived","截至所选月份有效归档",false,"yes","已有归档","no","尚未归档");select(f,"attainment","期满目标达成",false,"met","已达标","unmet","未达标","unknown","待核实");plain(f,"ratio","归档达成比例（%）","number",false,"归档结果");}
    private List<Map<String,Object>> recordRows(Long actor){List<Map<String,Object>> rows=new ArrayList<>();var first=records.list(actor,null,null,null,null,1,100);List<BizWorkRecord> visible=new ArrayList<>(first.records());for(int page=2;(page-1L)*100<first.total();page++)visible.addAll(records.list(actor,null,null,null,null,page,100).records());
        Map<Long,List<Map<String,Object>>> entries=new HashMap<>();if(!visible.isEmpty()){String markers=String.join(",",Collections.nCopies(visible.size(),"?"));for(var e:jdbc.queryForList("SELECT record_id,reform_task_id,reform_task_name,key_progress,stage_results,typical_practices FROM biz_work_record_entry WHERE record_id IN ("+markers+")",visible.stream().map(BizWorkRecord::getRecordId).toArray()))entries.computeIfAbsent(id(e.get("record_id")),k->new ArrayList<>()).add(e);}
        Map<Long,SysUser> accounts=users.getAllUsers().stream().collect(Collectors.toMap(SysUser::getUserId,u->u));
        for(var item:visible){var r=row(item);r.put("id",item.getRecordId());r.put("year",item.getRecordYear());r.put("month",item.getRecordMonth());r.put("mine",flag(Objects.equals(actor,item.getOwnerId())));var owner=accounts.get(item.getOwnerId());r.put("deptId",owner==null?null:owner.getDeptId());var written=entries.getOrDefault(item.getRecordId(),List.of()).stream().filter(e->List.of("key_progress","stage_results","typical_practices").stream().anyMatch(k->!str(e.get(k)).isBlank())).toList();r.put("root",written.stream().map(e->e.get("reform_task_id")).toList());r.put("rootOptions",written.stream().map(e->Map.of("value",str(e.get("reform_task_id")),"label",str(e.get("reform_task_name")))).toList());r.put("keyword",str(item.getProblems())+" "+str(item.getNextFocus())+" "+str(item.getOtherMatters())+" "+written.stream().map(e->str(e.get("key_progress"))+" "+str(e.get("stage_results"))+" "+str(e.get("typical_practices"))).collect(Collectors.joining(" ")));r.put("item",item);rows.add(r);}return rows;}
    private void recordFields(List<Field> f,List<Map<String,Object>> r,Map<String,String> p,Map<String,String> d){years(f,false);months(f);multi(f,"ownerId","填报人",true,r,p);select(f,"status","状态",true,"0","草稿","1","已提交");departments(f,"deptId","填报人当前部门",false,r,d);select(f,"mine","本人记录",false,"yes","是","no","否");Map<String,String> names=new HashMap<>();for(var row:r)for(var o:(List<Map<String,Object>>)row.get("rootOptions"))names.put(str(o.get("value")),str(o.get("label")));multi(f,"root","正文涉及改革任务",false,r,names);plain(f,"keyword","正文关键词","text",false,"正文");plain(f,"submitTime","提交时间","date",false,"时间");plain(f,"updateTime","更新时间","date",false,"时间");}
    private List<Map<String,Object>> noticeRows(Long actor){
        var notices=system.getNotices(actor);Map<String,Map<Long,Long>> states=new HashMap<>();
        for(String source:List.of("0","1","2","3","4")){
            var ids=notices.stream().filter(n->source.equals(str(n.getSourceType()))&&n.getSourceId()!=null).map(SysNotice::getSourceId).distinct().toList();
            if(ids.isEmpty())continue;
            String table=switch(source){case "0","1"->"biz_material_submission";case "2"->"biz_performance_submission";case "3"->"biz_achievement_submission";default->"biz_core_report";};
            String pk=source.equals("4")?"report_id":"sub_id";
            String placeholders=String.join(",",Collections.nCopies(ids.size(),"?"));
            Map<Long,Long> byId=new HashMap<>();
            String sql=source.equals("1")?"SELECT t.task_id id,CASE WHEN EXISTS(SELECT 1 FROM biz_material_submission s WHERE s.task_id=t.task_id AND s.flow_status IN(10,20,30) AND COALESCE(s.is_delete,0)=0) THEN 10 ELSE 40 END flow_status FROM biz_task t WHERE t.task_id IN ("+placeholders+") AND COALESCE(t.is_delete,0)=0":"SELECT "+pk+" AS id,flow_status FROM "+table+" WHERE "+pk+" IN ("+placeholders+") AND COALESCE(is_delete,0)=0";
            if(source.equals("4"))sql="SELECT r.report_id id,r.flow_status FROM biz_core_report r JOIN biz_core_indicator c ON c.indicator_id=r.indicator_id AND BINARY c.indicator_code=BINARY r.performance_code WHERE r.report_id IN ("+placeholders+") AND r.submit_time IS NOT NULL";
            for(var r:jdbc.queryForList(sql,ids.toArray()))byId.put(id(r.get("id")),id(r.get("flow_status")));
            states.put(source,byId);
        }
        List<Map<String,Object>> rows=new ArrayList<>();
        for(var item:notices){var r=row(item);r.put("id",item.getNoticeId());r.put("keyword",str(item.getTitle())+" "+str(item.getContent()));String source=str(item.getSourceType());Long state=states.getOrDefault(source,Map.of()).get(item.getSourceId());r.put("pending",state==null?"unknown":flag((source.equals("0")||source.equals("1"))?Set.of(10L,20L,30L).contains(state):source.equals("3")?state==10:Set.of(10L,20L).contains(state)));r.put("sourceType",source.equals("1")?"0":source);rows.add(r);}
        return rows;
    }
    private void noticeFields(List<Field> f,List<Map<String,Object>> r,Map<String,String> p){select(f,"isRead","阅读状态",true,"0","未读","1","已读");multi(f,"type","消息类型",true,r,Map.of("1","系统通知","2","预警／驳回","3","待办事项"));plain(f,"createTime","发送时间","date",true,"基础");plain(f,"keyword","关键词","text",true,"基础");select(f,"sourceType","业务模块",false,"0","任务","2","绩效","3","成果","4","核心指标");multi(f,"fromUserId","发送人",false,r,p);plain(f,"sourceId","关联编号","text",false,"关联业务");select(f,"pending","关联业务待处理",false,"yes","待处理","no","已处理","unknown","无关联／不可判定");}

    private void add(List<Field> f,String k,String label,String type,boolean basic,String group,List<Map<String,Object>> options){f.add(new Field(k,label,type,basic,group,options));}
    private List<Map<String,Object>> opts(String... pairs){List<Map<String,Object>> rows=new ArrayList<>();for(int i=0;i<pairs.length;i+=2)rows.add(Map.of("value",pairs[i],"label",pairs[i+1]));return rows;}
    private void select(List<Field> f,String k,String label,boolean basic,String...pairs){add(f,k,label,"select",basic,"状态",opts(pairs));}
    private void plain(List<Field> f,String k,String label,String type,boolean basic,String group){add(f,k,label,type,basic,group,List.of());}
    private void years(List<Field> f,boolean missing){var opts=opts("2025","2025年","2026","2026年","2027","2027年","2028","2028年","2029","2029年");if(missing)opts.add(Map.of("value","missing","label","日期缺失"));add(f,"year",missing?"取得年度":"年度","select",true,"基础",opts);}
    private void multi(List<Field> f,String key,String label,boolean basic,List<Map<String,Object>> rows,Map<String,String> names){Set<String> ids=new TreeSet<>();for(var r:rows){Object value=r.get(key);if(value instanceof Collection<?> c)c.forEach(v->ids.add(str(v)));else if(value!=null)ids.add(str(value));}add(f,key,label,"multi",basic,"归属与人员",ids.stream().filter(v->!v.isBlank()).map(v->Map.<String,Object>of("value",v,"label",names.getOrDefault(v,v))).toList());}
    private void departments(List<Field> f,String key,String label,boolean basic,List<Map<String,Object>> rows,Map<String,String> names){for(var r:rows){Object value=r.get(key);Collection<?> ids=value instanceof Collection<?> c?c:value==null?List.of():List.of(value);for(Object v:ids)if(!names.containsKey(str(v))){var d=users.getDeptById(id(v));names.put(str(v),d==null?"未设置部门":d.getDeptName());}}multi(f,key,label,basic,rows,names);}
    private void taskFields(List<Field> f,List<Map<String,Object>> r,Map<String,String> p,Map<String,String> d){years(f,false);departments(f,"deptId","归口部门",true,r,d);select(f,"status","任务状态",true,"0","未开始","1","进行中","2","审核中","3","已完成");plain(f,"keyword","编号／名称","text",true,"基础");
        for(String k:List.of("root","parent")){Map<String,String> names=new LinkedHashMap<>();for(var row:r)for(var o:(List<Map<String,Object>>)row.get(k+"Options"))names.put(str(o.get("value")),str(o.get("label")));multi(f,k,k.equals("root")?"一级改革任务":"二级目录",false,r,names);}
        multi(f,"leaderId","责任人",false,r,p);select(f,"kind","任务类型",false,"normal","一般","priority","重点","difficult","困难","both","重点且困难");select(f,"scope","任务范围",false,"mine","我负责的","todo","待我审核","returned","已退回");select(f,"stage","待审阶段",false,"10","待专业群审核","20","待归口审核","30","待双高办归档");departments(f,"handlerDept","当前处理部门",false,r,d);multi(f,"handler","当前处理人",false,r,p);plain(f,"progress","完成进度（%）","number",false,"进度与时间");select(f,"overdue","是否逾期",false,"yes","逾期","no","未逾期");plain(f,"plannedCompletionDate","计划完成日期","date",false,"进度与时间");plain(f,"actualCompletionDate","实际完成日期","date",false,"进度与时间");select(f,"children","包含四级目标",false,"yes","是","no","否");}
    private void performanceFields(List<Field> f,List<Map<String,Object>> r,Map<String,String> p,Map<String,String> d){years(f,false);departments(f,"deptId","归口部门",true,r,d);select(f,"attainment","年度达标情况",true,"met","数值达标","unmet","未达标","unknown","待核实");plain(f,"keyword","编号／名称","text",true,"基础");Map<String,String> categories=new HashMap<>(),roots=new HashMap<>();for(var row:r){categories.put(str(row.get("category")),str(row.get("categoryName")));((Map<Long,String>)row.get("rootNames")).forEach((k,v)->roots.put(str(k),v));}multi(f,"category","指标分类",false,r,categories);multi(f,"root","关联一级改革任务",false,r,roots);
        multi(f,"leaderId","责任人",false,r,p);select(f,"mode","填报方式",false,"auto","自动汇总","manual","手动填报");multi(f,"auditorId","专业群审核人",false,r,p);select(f,"scope","绩效范围",false,"mine","我负责的","todo","待我审核","returned","已退回");select(f,"auditStatus","审核状态",false,"none","无正式审核记录","pending","审核中","archived","已归档","returned","已退回");select(f,"stage","待审阶段",false,"10","待专业群审核","20","待归档");select(f,"linked","关联任务",false,"yes","有","no","无");plain(f,"ratio","年度达成比例（%）","number",false,"数值");select(f,"missingTarget","目标值待核实",false,"yes","是","no","否");select(f,"missingActual","完成值缺失",false,"yes","是","no","否");}
    private void achievementFields(List<Field> f,List<Map<String,Object>> r,Map<String,String> p,Map<String,String> d){years(f,true);departments(f,"deptId","报送部门",true,r,d);select(f,"auditStatus","审核状态",true,"10","待审核","30","已归档","-10","已驳回");plain(f,"keyword","成果名称","text",true,"基础");multi(f,"category","成果类别",false,r,Map.of("1","落实立德树人根本任务","2","创新产教融合机制","3","打造高水平专业群","4","建设一流核心课程","5","开放优质新形态教材","6","建设高水平双师队伍","7","建设产教融合实训基地","8","构建数字化教学新生态","9","拓展国际交流与合作","10","打造一流区域型高端智库"));multi(f,"level","成果级别",false,r,Map.of());add(f,"grade","成果等次","multi",false,"成果属性",opts("teDengJiang","特等奖","yiDengJiang","一等奖","erDengJiang","二等奖","sanDengJiang","三等奖","jinJiang","金奖","yinJiang","银奖","tongJiang","铜奖","youShengJiang","优胜奖","budDengDengCi","不定等次"));select(f,"competition","是否竞赛",false,"1","是","0","否");multi(f,"createBy","报送人",false,r,p);plain(f,"department","组织／颁发单位","text",false,"成果属性");for(var entry:Map.of("gotTime","取得日期","submittedTime","提交日期","archivedTime","归档日期").entrySet())plain(f,entry.getKey(),entry.getValue(),"date",false,"时间");select(f,"material","佐证材料",false,"yes","有","no","无");select(f,"professionalReview","专业群审核证据",false,"reviewed","已审核","skipped","归档时未审核","unknown","尚不可判定／缺少证据");plain(f,"rejectionReason","当前驳回原因","text",false,"审核");}
}
