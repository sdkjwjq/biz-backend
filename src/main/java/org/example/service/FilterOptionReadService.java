package org.example.service;

import org.example.mapper.ReviewNoticeSql;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import java.util.*;
import static org.example.service.PermissionService.*;

/** Field definitions do not load business objects. Large option sets are independent SQL projections. */
@Service
public class FilterOptionReadService {
    private final RemoteOptionPages pages;private final ReadScopeSql scopes;private final PermissionService permissions;private final AccountFilterPageService accounts;private final JdbcTemplate jdbc;
    public FilterOptionReadService(RemoteOptionPages pages,ReadScopeSql scopes,PermissionService permissions,AccountFilterPageService accounts,JdbcTemplate jdbc){this.pages=pages;this.scopes=scopes;this.permissions=permissions;this.accounts=accounts;this.jdbc=jdbc;}
    private record Source(String sql,List<Object> args){}
    @org.springframework.transaction.annotation.Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Map<String,Object> query(String domain,String key,Long actor,MultiValueMap<String,String> params){
        Source source=source(domain,key,actor,params);return source==null?null:pages.query(source.sql,source.args,params);
    }
    private Integer year(MultiValueMap<String,String> params){String y=params.getFirst("f.year");if(y==null||y.isBlank()||y.equals("missing"))return null;try{int value=Integer.parseInt(y);if(value>=2025&&value<=2029)return value;}catch(NumberFormatException ignored){}throw new AdminManagementException(400,"请选择2025—2029年度");}
    private String id(String column){return "CAST("+column+" AS CHAR)";}
    private String person(String expr){return "COALESCE((SELECT CASE WHEN u.nick_name IS NULL THEN COALESCE(u.user_name,'') ELSE u.nick_name END FROM sys_user u WHERE u.user_id="+expr+"),"+id(expr)+")";}
    private String dept(String expr){return "CASE WHEN EXISTS(SELECT 1 FROM sys_dept d WHERE d.dept_id="+expr+") THEN COALESCE((SELECT d.dept_name FROM sys_dept d WHERE d.dept_id="+expr+"),'') ELSE '未设置部门' END";}
    private Source source(String domain,String key,Long actor,MultiValueMap<String,String> params){
        Integer year=year(params);var args=new ArrayList<Object>();String base,expr,label;
        switch(domain){
            case "tasks","supervision":{
                var range=scopes.tasks(actor,"t",year,false);args.addAll(range.args());base=" FROM biz_task t WHERE t.level=3 AND (t.is_delete IS NULL OR t.is_delete<>1) AND ("+range.sql()+")";if(year!=null){base+=" AND t.phase=?";args.add(year);}
                if(Set.of("deptId","leaderId").contains(key)){expr=key.equals("deptId")?"t.dept_id":"t.leader_id";label=key.equals("deptId")?dept(expr):person(expr);return new Source("SELECT DISTINCT "+id(expr)+" value,"+label+" label"+base,args);}
                if(key.equals("parent"))return new Source("SELECT DISTINCT "+id("t.parent_id")+" value,COALESCE((SELECT p.task_name FROM biz_task p WHERE p.task_id=t.parent_id AND p.level<=3 AND (p.is_delete IS NULL OR p.is_delete<>1)),"+id("t.parent_id")+") label"+base,args);
                if(key.equals("root"))return taskRoots(base,args);
                if(Set.of("handler","handlerDept").contains(key)){
                    expr=key.equals("handler")?"s.current_handler_id":"d.dept_id";label=key.equals("handler")?person(expr):dept(expr);
                    String authorized="SELECT t.task_id"+base+" AND COALESCE(t.is_delete,0)=0";
                    return new Source("WITH allowed AS ("+authorized+"), links AS (SELECT task_id source_id FROM allowed UNION SELECT c.task_id FROM biz_level4_task c JOIN allowed a ON a.task_id=c.parent_id WHERE COALESCE(c.is_delete,0)=0), latest AS (SELECT task_id,MAX(sub_id) id FROM biz_material_submission GROUP BY task_id) SELECT DISTINCT "+id(expr)+" value,"+label+" label FROM links link JOIN latest l ON l.task_id=link.source_id JOIN biz_material_submission s ON s.sub_id=l.id LEFT JOIN sys_user u ON u.user_id=s.current_handler_id AND u.is_delete=0 LEFT JOIN sys_dept d ON d.dept_id=u.dept_id AND d.is_delete=0 WHERE s.is_delete=0 AND s.flow_status>0 AND s.flow_status<40",args);
                }return null;
            }
            case "performance":{
                var range=scopes.row(actor,"performance","p",year);
                var visible=performanceOptionIds(range,year);
                base=" FROM biz_performance p WHERE "+ReadScopeSql.in("p.perf_id",visible,args);
                if(Set.of("deptId","leaderId","auditorId","category").contains(key)){
                    expr=switch(key){case "deptId"->"p.dept_id";case "leaderId"->"p.leader_id";case "auditorId"->"p.auditor_id";default->"p.parent_id";};label=key.equals("deptId")?dept(expr):key.equals("category")?"COALESCE((SELECT d.perf_name FROM biz_performance d WHERE d.perf_id=p.parent_id AND (d.is_delete IS NULL OR d.is_delete<>1)), '')":person(expr);
                    return new Source("SELECT DISTINCT "+id(expr)+" value,"+label+" label"+base,args);
                }
                if(key.equals("root")){
                    var rangeTask=scopes.tasks(actor,"t",year,false);var taskArgs=new ArrayList<Object>(rangeTask.args());String tasks=" FROM biz_task t WHERE t.level=3 AND (t.is_delete IS NULL OR t.is_delete<>1) AND ("+rangeTask.sql()+")";if(year!=null){tasks+=" AND t.phase=?";taskArgs.add(year);}
                    tasks+=" AND EXISTS(SELECT 1 FROM rel_task_performance r JOIN biz_performance_year y ON y.year_id=r.year_id WHERE COALESCE(r.is_delete,0)=0 AND r.task_id=t.task_id AND y.year=t.phase AND EXISTS(SELECT 1"+base+" AND p.perf_id=r.perf_id))";taskArgs.addAll(args);return taskRoots(tasks,taskArgs);
                }return null;
            }
            case "achievements":{
                var range=scopes.row(actor,domain,"a",year);args.addAll(range.args());base=" FROM biz_achievement a WHERE a.is_delete=0 AND ("+range.sql()+")";if(year!=null){base+=" AND YEAR(a.got_time)=?";args.add(year);}else if("missing".equals(params.getFirst("f.year")))base+=" AND a.got_time IS NULL";
                expr=switch(key){case "deptId"->"a.dept_id";case "createBy"->"a.create_by";case "category"->"a.category";case "level"->"a.level";default->null;};if(expr==null)return null;
                label=key.equals("deptId")?dept(expr):key.equals("createBy")?person(expr):key.equals("category")?"CASE a.category WHEN 1 THEN '落实立德树人根本任务' WHEN 2 THEN '创新产教融合机制' WHEN 3 THEN '打造高水平专业群' WHEN 4 THEN '建设一流核心课程' WHEN 5 THEN '开放优质新形态教材' WHEN 6 THEN '建设高水平双师队伍' WHEN 7 THEN '建设产教融合实训基地' WHEN 8 THEN '构建数字化教学新生态' WHEN 9 THEN '拓展国际交流与合作' WHEN 10 THEN '打造一流区域型高端智库' ELSE CAST(a.category AS CHAR) END":id(expr);
                return new Source("SELECT DISTINCT "+id(expr)+" value,"+label+" label"+base,args);
            }
            case "notices":{
                if(!Set.of("type","fromUserId").contains(key))return null;expr=key.equals("type")?"n.type":"n.from_user_id";args.add(actor);return new Source("SELECT DISTINCT "+id(expr)+" value,"+(key.equals("type")?"CASE n.type WHEN '1' THEN '系统通知' WHEN '2' THEN '预警／驳回' WHEN '3' THEN '待办事项' ELSE n.type END":person(expr))+" label"+ReviewNoticeSql.jdbcVisibleFrom(),args);
            }
            case "records":{
                var range=scopes.recordRows(actor,"r","u.dept_id",year);args.add(actor);args.addAll(range.args());base=" FROM biz_work_record r LEFT JOIN sys_user u ON u.user_id=r.owner_id WHERE r.delete_marker=0 AND ((r.status<>1 AND "+(org.example.utils.AdminTestContext.enabled(actor)?"? IS NOT NULL":"r.owner_id=?")+") OR (r.status=1 AND ("+range.sql()+")))";if(year!=null){base+=" AND r.record_year=?";args.add(year);}
                if(Set.of("ownerId","deptId").contains(key)){expr=key.equals("ownerId")?"r.owner_id":"u.dept_id";return new Source("SELECT DISTINCT "+id(expr)+" value,"+(key.equals("ownerId")?person(expr):dept(expr))+" label"+base,args);}
                if(key.equals("root")){
                    String blank="^[\\\\x{0009}-\\\\x{000D}\\\\x{001C}-\\\\x{0020}\\\\x{1680}\\\\x{2000}-\\\\x{2006}\\\\x{2008}-\\\\x{200A}\\\\x{2028}\\\\x{2029}\\\\x{205F}\\\\x{3000}]*$";
                    return new Source("WITH allowed AS (SELECT r.record_id"+base+"), entries AS (SELECT e.reform_task_id,e.reform_task_name,ROW_NUMBER() OVER(PARTITION BY e.reform_task_id ORDER BY e.record_id DESC,e.entry_id DESC) rn FROM biz_work_record_entry e JOIN allowed a ON a.record_id=e.record_id WHERE NOT REGEXP_LIKE(COALESCE(e.key_progress,''),'"+blank+"','c') OR NOT REGEXP_LIKE(COALESCE(e.stage_results,''),'"+blank+"','c') OR NOT REGEXP_LIKE(COALESCE(e.typical_practices,''),'"+blank+"','c')) SELECT CAST(reform_task_id AS CHAR) value,reform_task_name label FROM entries WHERE rn=1",args);
                }return null;
            }
            case "admin-users":{
                if(key.equals("deptId"))return new Source("SELECT DISTINCT CAST(u.dept_id AS CHAR) value,COALESCE((SELECT d.dept_name FROM sys_dept d WHERE d.dept_id=u.dept_id AND COALESCE(d.is_delete,0)=0),CAST(u.dept_id AS CHAR)) label FROM sys_user u WHERE (u.is_delete IS NULL OR u.is_delete<>1)",List.of());
                if(key.equals("roles")){var assigned=accounts.assignedCodes();var roleRows=permissions.roles().stream().filter(r->assigned.contains(r.code())).toList();if(roleRows.isEmpty())return new Source("SELECT NULL value,NULL label WHERE 0=1",List.of());var terms=new ArrayList<String>();for(var role:roleRows){terms.add("SELECT CAST(? AS CHAR) value,CAST(? AS CHAR) label");args.add(role.code());args.add(role.name());}return new Source(String.join(" UNION ALL ",terms),args);}return null;
            }
            case "admin-departments":if(key.equals("leaderId"))return new Source("SELECT DISTINCT CAST(d.leader_id AS CHAR) value,"+person("d.leader_id")+" label FROM sys_dept d WHERE COALESCE(d.is_delete,0)=0",List.of());else return null;
            case "budget-versions":{
                if(!key.equals("createdBy"))return null;var years=new ArrayList<Integer>();for(int y=2025;y<=2029;y++)if(permissions.budgetScope(actor,y))years.add(y);if(years.isEmpty())return new Source("SELECT NULL value,NULL label WHERE 0=1",List.of());args.addAll(years);
                return new Source("SELECT DISTINCT CAST(v.created_by AS CHAR) value,COALESCE(u.nick_name,'') label FROM biz_budget_import_version v LEFT JOIN sys_user u ON u.user_id=v.created_by WHERE v.is_delete=0 AND v.year IN ("+String.join(",",Collections.nCopies(years.size(),"?"))+")",args);
            }
            default:return null;
        }
    }
    private record Link(Long parent,boolean leafEligible){}
    /** The original leaf/ancestor/year rules over ID-only batches; no performance bodies. */
    private Set<Long> performanceOptionIds(ReadScopeSql.Clause range,Integer year){
        if(range.sql().equals("0=1"))return Set.of();
        Map<Long,Link> graph=new LinkedHashMap<>();Set<Long> parents=new HashSet<>();
        for(int offset=0;;offset+=500){
            var batch=jdbc.queryForList("SELECT perf_id id,parent_id parentId,CASE WHEN COALESCE(is_delete,0)=0 THEN 1 ELSE 0 END eligible FROM biz_performance WHERE is_delete IS NULL OR is_delete<>1 ORDER BY perf_id LIMIT 500 OFFSET ?",offset);
            for(var row:batch){Long parent=row.get("parentId")==null?null:number(row.get("parentId"));graph.put(number(row.get("id")),new Link(parent,number(row.get("eligible"))==1));if(parent!=null)parents.add(parent);}
            if(batch.size()<500)break;
        }
        var leaves=graph.entrySet().stream().filter(e->e.getValue().leafEligible&&!parents.contains(e.getKey())).map(Map.Entry::getKey).toList();
        Set<Long> allowed=new LinkedHashSet<>();
        if(Set.of("1=1","(1=1)").contains(range.sql())&&range.args().isEmpty())allowed.addAll(leaves);
        else for(int start=0;start<leaves.size();start+=500){var batch=leaves.subList(start,Math.min(start+500,leaves.size()));var values=new ArrayList<Object>(batch);values.addAll(range.args());allowed.addAll(jdbc.queryForList("SELECT p.perf_id FROM biz_performance p WHERE p.perf_id IN ("+String.join(",",Collections.nCopies(batch.size(),"?"))+") AND ("+range.sql()+")",Long.class,values.toArray()));}
        Set<Long> visible=new LinkedHashSet<>();for(Long leaf:allowed){Set<Long> seen=new HashSet<>();for(Long cursor=leaf;cursor!=null&&graph.containsKey(cursor)&&seen.add(cursor);cursor=graph.get(cursor).parent)visible.add(cursor);}
        if(year!=null){var ids=new ArrayList<>(visible);Set<Long> annual=new HashSet<>();for(int start=0;start<ids.size();start+=500){var batch=ids.subList(start,Math.min(start+500,ids.size()));var values=new ArrayList<Object>();values.add(year);values.addAll(batch);annual.addAll(jdbc.queryForList("SELECT perf_id FROM biz_performance_year WHERE year=? AND perf_id IN ("+String.join(",",Collections.nCopies(batch.size(),"?"))+")",Long.class,values.toArray()));}visible.retainAll(annual);}
        Set<Long> catalogParents=new HashSet<>();for(Long id:visible)if(graph.get(id).parent!=null)catalogParents.add(graph.get(id).parent);visible.removeAll(catalogParents);return visible;
    }
    private Source taskRoots(String from,List<Object> args){return new Source("WITH RECURSIVE allowed AS (SELECT t.task_id,t.parent_id"+from+"), path_nodes(task_id,parent_id,level,task_name,seen) AS (SELECT t.task_id,t.parent_id,t.level,t.task_name,JSON_ARRAY(t.task_id) FROM biz_task t JOIN allowed a ON a.task_id=t.task_id UNION ALL SELECT p.task_id,p.parent_id,p.level,p.task_name,JSON_ARRAY_APPEND(n.seen,'$',p.task_id) FROM path_nodes n JOIN biz_task p ON p.task_id=n.parent_id AND p.level<=3 AND (p.is_delete IS NULL OR p.is_delete<>1) WHERE NOT JSON_CONTAINS(n.seen,CAST(p.task_id AS JSON))) SELECT DISTINCT CAST(task_id AS CHAR) value,task_name label FROM path_nodes WHERE level=1",args);}
}
