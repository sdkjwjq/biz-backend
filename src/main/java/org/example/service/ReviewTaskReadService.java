package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.*;
import org.example.mapper.BizMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import java.util.*;
import java.util.stream.Collectors;
import static org.example.service.ReviewRoundService.map;
import static org.example.service.PermissionService.number;
import static org.example.service.ListFilterQuery.*;

/** All entry points resolve round membership/time before producing any task data or options. */
@Service
public class ReviewTaskReadService {
    private final ReviewRoundService rounds;private final BusinessListFilterService filters;
    private final TaskReadProjectionService projection;private final BizMapper tasks;private final BizService biz;
    private final JdbcTemplate db;private final ObjectMapper json;
    public ReviewTaskReadService(ReviewRoundService rounds,BusinessListFilterService filters,TaskReadProjectionService projection,BizMapper tasks,BizService biz,JdbcTemplate db,ObjectMapper json){this.rounds=rounds;this.filters=filters;this.projection=projection;this.tasks=tasks;this.biz=biz;this.db=db;this.json=json;}
    private record Scoped(ReviewRoundService.ReadContext context,BusinessListFilterService.Data data){}
    private Scoped data(long actor,long round,boolean history){
        var context=rounds.readContext(actor,round,history);var states=rounds.objects(actor,round,context);
        var byId=states.stream().collect(Collectors.toMap(r->number(r.get("taskId")),r->r));
        var visible=context.catalog().values().stream().filter(t->byId.containsKey(t.getTaskId())).toList();
        var data=filters.reviewTaskData(actor,visible);
        for(var row:data.catalog()){var state=byId.get(number(row.get("taskId")));row.put("reviewStatus",state.get("reviewStatus"));row.put("reviewResult",state.get("result"));row.put("canReview",state.get("canReview"));}
        for(var row:data.rows()){
            var state=byId.get(number(row.get("id")));row.put("reviewStatus",state.get("reviewStatus"));
            var roots=(List<?>)row.get("root");row.put("rootReviewStatus",roots.stream().map(id->byId.get(number(id))).filter(Objects::nonNull).map(r->r.get("reviewStatus")).toList());
        }
        var fields=new ArrayList<>(data.fields().stream().filter(f->!f.key().equals("scope")).toList());
        var statuses=List.<Map<String,Object>>of(map("value","unreviewed","label","未评审"),map("value","draft","label","草稿／修订中"),map("value","submitted","label","已提交"));
        fields.add(new Field("reviewStatus","本人三级评语状态","select",false,"专家评审",statuses));fields.add(new Field("rootReviewStatus","一级总体评语状态","select",false,"专家评审",statuses));
        Object year=context.round().get("review_year");
        if(year!=null){for(int i=0;i<fields.size();i++)if(fields.get(i).key().equals("year")){var f=fields.get(i);fields.set(i,new Field(f.key(),f.label(),f.type(),f.basic(),f.group(),List.of(map("value",String.valueOf(year),"label",year+"年"))));}}
        return new Scoped(context,new BusinessListFilterService.Data(data.rows(),fields,data.catalog()));
    }
    public Object query(long actor,long round,MultiValueMap<String,String> params,boolean history){
        if(params.getFirst("responseMode")==null||params.getFirst("responseMode").isBlank()){return rounds.objects(actor,round,rounds.readContext(actor,round,history));}
        if(!"tree".equals(params.getFirst("responseMode")))throw new AdminManagementException(400,"评审任务只支持树形响应");
        var source=data(actor,round,history);var data=source.data();
        var year=source.context().round().get("review_year");if(year!=null&&params.getFirst("f.year")!=null&&!params.getFirst("f.year").equals(String.valueOf(year)))throw new AdminManagementException(400,"本轮评审年度为"+year);
        var query=new ListFilterQuery(params,data.fields());var matched=data.rows().stream().filter(r->query.matches(r,data.fields())).filter(r->filters.taskReadMatches(r,query,data.fields())).toList();
        var result=filters.taskReadTree(data,matched,params);
        @SuppressWarnings("unchecked")var displayed=(List<Map<String,Object>>)result.get("items");
        for(var item:displayed)if(number(item.get("level"))<3){long parent=number(item.get("taskId"));item.put("targetValue",null);item.put("currentValue",null);item.put("progress",(int)matched.stream().filter(r->((List<?>)r.get("directory")).stream().anyMatch(v->number(v)==parent)).mapToLong(r->number(r.get("progress"))).average().orElse(0));}
        result.put("round",rounds.readRound(source.context(),history));return result;
    }
    public Object options(long actor,long round,MultiValueMap<String,String> params,boolean history){
        var source=data(actor,round,history);String key=params.getFirst("field");var fields=source.data().fields();
        if(key==null||key.isBlank())return map("fields",fields.stream().map(f->{Map<String,Object> value=json.convertValue(f,new TypeReference<Map<String,Object>>(){});if(f.type().equals("multi")){value.put("remote",true);value.put("options",List.of());}return value;}).toList(),"round",rounds.readRound(source.context(),history));
        var field=fields.stream().filter(f->f.key().equals(key)).findFirst().orElseThrow(()->new AdminManagementException(400,"不支持的筛选字段"));
        if(!Set.of("multi","select").contains(field.type()))throw new AdminManagementException(400,"该字段不支持候选查询");
        String keyword=str(params.getFirst("keyword")).trim().toLowerCase(Locale.ROOT);if(keyword.length()>300||params.getOrDefault("ids",List.of()).size()>100)throw new AdminManagementException(400,"候选查询参数过长");
        var candidates=field.options();String year=params.getFirst("f.year");
        if(year!=null&&!year.isBlank()&&field.type().equals("multi")){
            var possible=source.data().rows().stream().filter(r->year.equals(str(r.get("year")))).flatMap(r->{var v=r.get(key);return (v instanceof Collection<?> c?c.stream():v==null?java.util.stream.Stream.empty():java.util.stream.Stream.of(v));}).map(ListFilterQuery::str).collect(Collectors.toSet());candidates=candidates.stream().filter(o->possible.contains(str(o.get("value")))).toList();
        }
        var selected=params.getOrDefault("ids",List.of());var found=candidates.stream().filter(o->str(o.get("label")).toLowerCase(Locale.ROOT).contains(keyword)||str(o.get("value")).contains(keyword)).toList();
        int size=Math.min(100,positive(params.getFirst("pageSize"),50)),page=Math.min(positive(params.getFirst("page"),1),Math.max(1,(found.size()+size-1)/size));
        return map("items",found.subList((page-1)*size,Math.min(page*size,found.size())),"total",found.size(),"page",page,"pageSize",size,"selected",candidates.stream().filter(o->selected.contains(str(o.get("value")))).toList());
    }
    private int positive(String value,int fallback){try{int n=value==null?fallback:Integer.parseInt(value);if(n>0)return n;}catch(NumberFormatException ignored){}throw new AdminManagementException(400,"分页参数不正确");}
    public Map<String,Object> detail(long actor,long round,long task,boolean history){
        var context=rounds.readContext(actor,round,history);var review=rounds.opinionDetail(actor,round,task);var scoped=rounds.objects(actor,round,context).stream().map(r->number(r.get("taskId"))).collect(Collectors.toSet());
        if(!scoped.contains(task)||!Set.of(1,3).contains(context.catalog().get(task).getLevel()))throw new AdminManagementException(404,"任务不在评审范围内");
        if(history){review.put("canWrite",false);var formal=(Map<?,?>)review.get("formal");review.put("draftText",formal==null?"":formal.get("review_text"));review.put("result",formal==null?null:formal.get("review_result"));}
        var projected=projection.project(context.catalog().values().stream().filter(t->scoped.contains(t.getTaskId())).toList(),true);var byId=projected.stream().collect(Collectors.toMap(t->t.getTaskId(),t->t));
        var current=byId.get(task);
        if(current.getLevel()<3){current.setTargetValue(null);current.setCurrentValue(null);current.setProgress((int)context.leaves().stream().map(context.catalog()::get).filter(t->rounds.isDescendant(t,task,context.catalog())).mapToInt(t->t.getProgress()==null?0:t.getProgress()).average().orElse(0));}
        var goals=current.getLevel()==3?tasks.getForthLevelTasksByParentId(task):new ArrayList<BizLevel4Task>();biz.decorateLevel4Progress(goals);
        List<org.example.entity.vo.BizAuditVO> submissions=current.getLevel()==3?biz.auditListToAuditVoList(tasks.getAuditsByTaskId(task)):List.of();
        // The task row may have been restored after a later return. Expert review
        // shows the latest archived submission's frozen completion description.
        if(current.getLevel()==3)submissions.stream()
                .filter(s->Integer.valueOf(40).equals(s.getFlowStatus()) && s.getComment()!=null)
                .findFirst().ifPresent(s->current.setComment(s.getComment()));
        var logs=current.getLevel()==3?db.queryForList("SELECT l.log_id logId,l.sub_id subId,l.operator_id operatorId,l.action_type actionType,l.pre_status preStatus,l.post_status postStatus,l.comment,l.create_time createTime FROM biz_audit_log l JOIN biz_material_submission s ON s.sub_id=l.sub_id AND l.create_time>=s.submit_time WHERE s.task_id=? ORDER BY l.create_time DESC,l.log_id DESC",task):List.<Map<String,Object>>of();
        var children=current.getLevel()==1?projected.stream().filter(t->t.getLevel()==3&&rounds.isDescendant(context.catalog().get(t.getTaskId()),task,context.catalog())).toList():List.of();
        Set<Long> people=new HashSet<>(),departments=new HashSet<>();for(var t:projected){for(Long u:Arrays.asList(t.getLeaderId(),t.getAuditorId(),t.getPrincipalId()))if(u!=null)people.add(u);if(t.getDeptId()!=null)departments.add(t.getDeptId());}
        for(var s:submissions){if(s.getSubmitBy()!=null)people.add(s.getSubmitBy());if(s.getCurrentHandlerId()!=null)people.add(s.getCurrentHandlerId());if(s.getSubmitDeptId()!=null)departments.add(s.getSubmitDeptId());if(s.getManageDeptId()!=null)departments.add(s.getManageDeptId());}for(var l:logs)people.add(number(l.get("operatorId")));
        var deptRows=new ArrayList<Map<String,Object>>();for(var chunk:TaskReadProjectionService.chunks(departments))deptRows.addAll(db.queryForList("SELECT dept_id deptId,dept_name deptName,leader_id leaderId FROM sys_dept WHERE dept_id IN("+TaskReadProjectionService.marks(chunk.size())+")",chunk.toArray()));
        for(var d:deptRows)if(d.get("leaderId")!=null)people.add(number(d.get("leaderId")));
        var names=new ArrayList<Map<String,Object>>();for(var chunk:TaskReadProjectionService.chunks(people))names.addAll(db.queryForList("SELECT user_id userId,COALESCE(nick_name,user_name) nickName FROM sys_user WHERE user_id IN("+TaskReadProjectionService.marks(chunk.size())+")",chunk.toArray()));
        return map("task",current,"level4Tasks",goals,"submissions",submissions,"auditLogs",logs,"children",children,"review",review,"people",names,"departments",deptRows);
    }
}
