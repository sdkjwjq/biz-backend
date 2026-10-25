package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.BizMaterialSubmission;
import org.example.mapper.SysMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import java.util.*;
import static org.example.service.ListFilterQuery.*;

/** Consolidated read model. It intersects existing sources; it never creates an approval duty. */
@Service
public class AuditListPageService {
    private final BusinessListFilterService filters;
    private final PermissionService permissions;
    private final BizService tasks;
    private final PerformanceService performance;
    private final AchievementService achievements;
    private final CoreIndicatorService core;
    private final SysMapper users;
    private final ObjectMapper json;
    public AuditListPageService(BusinessListFilterService filters,PermissionService permissions,BizService tasks,
            PerformanceService performance,AchievementService achievements,CoreIndicatorService core,SysMapper users,ObjectMapper json){
        this.filters=filters;this.permissions=permissions;this.tasks=tasks;this.performance=performance;
        this.achievements=achievements;this.core=core;this.users=users;this.json=json;
    }
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public Map<String,Object> query(Long actor,MultiValueMap<String,String> params){
        String tab=params.getFirst("tab");boolean todo=!"done".equals(tab);
        var data=filters.auditPageData(actor,tab);var query=new ListFilterQuery(params,data.fields());
        var rows=data.rows().stream().filter(r->pending(r)||!todo).filter(r->query.matches(r,data.fields())).toList();
        if(!todo){rows=new ArrayList<>(rows);rows.sort(Comparator.comparingInt(this::historyType)
                .thenComparingInt(r->"task".equals(r.get("type"))?(int)number(r.get("_taskOrder")):0)
                .thenComparing((a,b)->"task".equals(a.get("type"))&&"task".equals(b.get("type"))?Long.compare(number(b.get("subId")),number(a.get("subId"))):0));}
        int size=Math.min(100,positive(params.getFirst("pageSize"),10)),page=Math.min(positive(params.getFirst("page"),1),Math.max(1,(rows.size()+size-1)/size));
        var result=new LinkedHashMap<String,Object>();
        result.put("items",hydrate(rows.subList((page-1)*size,Math.min(page*size,rows.size()))));
        result.put("total",rows.size());result.put("page",page);result.put("pageSize",size);
        result.put("summary",Map.of("todoCount",todoCount(actor)));
        if(!"false".equals(params.getFirst("includeFields")))result.put("fields",data.fields());
        return result;
    }
    private int historyType(Map<String,Object> row){return switch(str(row.get("type"))){case "task"->0;case "core"->1;case "performance"->2;default->3;};}
    private boolean pending(Map<String,Object> row){long status=number(row.get("flowStatus"));return "task".equals(row.get("type"))?Set.of(10L,20L,30L).contains(status):"achievement".equals(row.get("type"))?status==10:status==10||status==20;}
    private long todoCount(Long actor){
        // Existing HTTP source endpoints establish the current administrator context before counting.
        // Reproduce that header count only; page membership still intersects the legacy filter scope.
        if(permissions.admin(actor)&&org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() instanceof org.springframework.web.context.request.ServletRequestAttributes attributes){
            var request=attributes.getRequest();Object previous=request.getAttribute(org.example.utils.AdminTestContext.ACTOR);
            request.setAttribute(org.example.utils.AdminTestContext.ACTOR,actor);
            try{return sourceTodoCount(actor);}finally{if(previous==null)request.removeAttribute(org.example.utils.AdminTestContext.ACTOR);else request.setAttribute(org.example.utils.AdminTestContext.ACTOR,previous);}
        }
        return sourceTodoCount(actor);
    }
    private long sourceTodoCount(Long actor){
        var p=permissions.state(actor).permissions();long total=0;
        if(canPending(p,"tasks"))total+=tasks.countTodoAudits(actor);
        if(canPending(p,"performance"))total+=performance.countTodoPerformanceAudits(actor);
        if(canPending(p,"achievements"))total+=achievements.countTodoAchievementAudits(actor);
        if(canPending(p,"core"))total+=core.countTodoAudits(actor);return total;
    }
    private boolean canPending(Set<String> p,String domain){return p.contains(domain+".audit")||p.contains(domain+".archive");}
    private List<Map<String,Object>> hydrate(List<Map<String,Object>> rows){
        var submissions=rows.stream().map(r->r.get("_submission")).filter(BizMaterialSubmission.class::isInstance).map(BizMaterialSubmission.class::cast).toList();
        Map<Long,Map<String,Object>> byId=new HashMap<>();
        for(var vo:tasks.auditListToAuditVoList(submissions))byId.put(vo.getSubId(),map(vo));
        List<Map<String,Object>> result=new ArrayList<>();
        for(var row:rows){var item=new LinkedHashMap<>(row);item.remove("_submission");item.remove("_wireSource");
            if(row.get("_wireSource") instanceof Map<?,?> wire)wire.forEach((key,value)->item.put(String.valueOf(key),value));
            if(byId.containsKey(number(row.get("subId"))))item.putAll(byId.get(number(row.get("subId"))));
            result.add(item);
        }
        var missing=result.stream().filter(r->r.get("fileId")!=null&&str(r.get("filename")).isBlank()).map(r->number(r.get("fileId"))).distinct().toList();
        Map<Long,String> names=new HashMap<>();if(!missing.isEmpty())for(var file:users.getFilesByIds(missing))names.put(file.getFileId(),file.getFileName());
        for(var item:result)if(item.get("fileId")!=null&&names.containsKey(number(item.get("fileId"))))item.put("filename",names.get(number(item.get("fileId"))));
        return result;
    }
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public Map<String,Object> locate(Long actor,MultiValueMap<String,String> params){
        String type=params.getFirst("type");Long taskId=optionalId(params.getFirst("taskId")),subId=optionalId(params.getFirst("subId"));
        var state=permissions.state(actor);Map<String,Object> selected=null;boolean pending=false;
        if("core".equals(type)&&subId!=null){
            var report=core.detail(subId,actor);permissions.require(actor,"core.read");
            if(!permissions.canRead(actor,"core",number(report.get("indicatorId")),(int)number(report.get("year"))))missing();
            selected=map(report);selected.put("type","core");
            pending=canPending(state.permissions(),"core")&&core.audits(actor,true).stream().anyMatch(r->number(r.get("reportId"))==subId);
        }else if(taskId!=null){
            permissions.require(actor,"tasks.read");if(!permissions.canRead(actor,"tasks",taskId,null))missing();
            if(!state.permissions().contains("tasks.history"))missing();
            var records=tasks.getAuditByTaskId(taskId,actor).stream().filter(r->!Integer.valueOf(1).equals(r.getIsDelete()))
                    .sorted(Comparator.comparing(org.example.entity.vo.BizAuditVO::getSubId).reversed()).toList();
            Set<Long> mine=new HashSet<>();if(canPending(state.permissions(),"tasks"))for(var r:records)if(r.getFlowStatus()!=null&&r.getFlowStatus()>=10&&r.getFlowStatus()<40&&(org.example.utils.AdminTestContext.enabled(actor)||Objects.equals(r.getCurrentHandlerId(),actor)))mine.add(r.getSubId());
            var source=records.stream().filter(r->mine.contains(r.getSubId())).findFirst().orElse(records.isEmpty()?null:records.get(0));
            if(source==null)missing();selected=map(source);selected.put("type","task");selected.put("_task",map(tasks.getTaskById(taskId)));
            pending=mine.contains(source.getSubId())&&pending(selected);
        }else if(subId!=null&&Set.of("performance","achievement").contains(str(type))){
            // Same old lookup: pending first, then visible handled records; no school-wide task histories.
            for(String tab:List.of("todo","done")){
                boolean may=tab.equals("todo")?canPending(state.permissions(),type.equals("achievement")?"achievements":type):state.permissions().contains((type.equals("achievement")?"achievements":type)+".history");
                if(!may)continue;
                Object source=type.equals("performance")?performance.locateAuditSummary(actor,subId,tab.equals("todo")):achievements.locateAuditSummary(actor,subId,tab.equals("todo"));
                if(source==null)continue;
                var row=map(source);String domain=type.equals("performance")?"performance":"achievements";
                Integer year=type.equals("performance")?(int)number(row.get("year")):null;
                Long objectId=number(row.get(type.equals("performance")?"perfId":"achId"));
                if(!state.permissions().contains(domain+".read")||!permissions.canRead(actor,domain,objectId,year))continue;
                row.put("type",type);selected=hydrate(List.of(row)).get(0);pending=tab.equals("todo")&&pending(selected);break;
            }
            if(selected==null)missing();
        }else throw new AdminManagementException(400,"业务定位参数不正确");
        var item=new LinkedHashMap<>(selected);var sender=users.getUserById(number(item.get("submitBy")));
        if(sender!=null){item.put("_submitName",sender.getNickName()==null?sender.getUserName():sender.getNickName());}
        Object dept=item.get("submitDeptId");if(dept==null)dept=item.get("deptId");
        if(dept!=null){var department=users.getDeptById(number(dept));if(department!=null)item.put("_deptName",department.getDeptName());}
        return Map.of("item",item,"isViewMode",!pending);
    }
    private void missing(){throw new AdminManagementException(404,"对应审核记录不存在或无权查看");}
    private Map<String,Object> map(Object value){return filters.wireRow(value);}
    private static long number(Object value){return PermissionService.number(value);}
    private int positive(String value,int fallback){if(value==null||value.isBlank())return fallback;try{int n=Integer.parseInt(value);if(n>0)return n;}catch(NumberFormatException ignored){}throw new AdminManagementException(400,"分页参数不正确");}
    private Long optionalId(String value){if(value==null||value.isBlank())return null;try{long n=Long.parseLong(value);if(n>0)return n;}catch(NumberFormatException ignored){}throw new AdminManagementException(400,"业务编号不正确");}
}
