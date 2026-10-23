package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.*;
import org.example.mapper.*;
import org.example.utils.AchievementPermissionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** 功能授权与数据范围的唯一计算入口。身份、业务处理人和管理员资格仍取数据库当前值。 */
@Service
public class PermissionService {
    public record Policy(String mode,Map<String,Boolean> overrides,Map<String,Object> scopes,List<String> roles,long version) { }
    public record State(Long userId,boolean admin,boolean configured,Policy policy,List<PermissionCatalog.Role> roles,
                        Set<String> permissions,long revision) { }
    private final JdbcTemplate jdbc;private final ObjectMapper json;private final SysMapper users;
    private final BizMapper tasks;private final PerformanceMapper performances;private final WorkRecordScopeService records;
    private final Set<Long> recordViewers;
    public PermissionService(JdbcTemplate jdbc,ObjectMapper json,SysMapper users,BizMapper tasks,PerformanceMapper performances,
                             WorkRecordScopeService records,@Value("${work-records.viewer-user-ids:}")String viewers) {
        this.jdbc=jdbc;this.json=json;this.users=users;this.tasks=tasks;this.performances=performances;this.records=records;
        recordViewers=Arrays.stream(viewers.split(",")).map(String::trim).filter(s->!s.isEmpty()).map(Long::valueOf).collect(Collectors.toSet());
    }
    public Long actor(){if(!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a))return null;String token=a.getRequest().getHeader("Authorization");if(token==null||token.isBlank())return null;try{return org.example.utils.JWTUtil.getUserIdFromToken(token);}catch(RuntimeException error){return null;}}
    public SysUser user(Long id){SysUser u=id==null?null:users.getUserById(id);if(u==null||!"1".equals(u.getStatus())||Integer.valueOf(1).equals(u.getIsDelete()))throw new AdminManagementException(403,"账号已停用或不存在");return u;}
    public boolean admin(Long id){try{return "0".equals(user(id).getRole());}catch(AdminManagementException e){return false;}}
    public boolean installed(){Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN('sys_permission_role','sys_user_permission','sys_user_permission_role','sys_permission_revision')",Integer.class);if(count!=0&&count!=4)throw new AdminManagementException(503,"权限数据结构不完整，请联系管理员完成迁移");return count==4;}
    public void requireInstalled(){if(!installed())throw new AdminManagementException(503,"请先执行权限与专家评审数据库迁移");}
    public void requireAdmin(Long id){if(!admin(id))throw new AdminManagementException(403,"仅管理员可以管理账号、角色和权限");}
    public long revision(){return installed()?Optional.ofNullable(jdbc.queryForObject("SELECT MAX(version) FROM sys_permission_revision WHERE revision_id=1",Long.class)).orElse(0L):0L;}
    public void lockOperation(){if(installed()&&TransactionSynchronizationManager.isActualTransactionActive())jdbc.queryForList("SELECT revision_id FROM sys_permission_revision WHERE revision_id=1 FOR SHARE");}
    public void lockChange(){requireInstalled();if(jdbc.queryForList("SELECT revision_id FROM sys_permission_revision WHERE revision_id=1 FOR UPDATE").isEmpty())jdbc.update("INSERT INTO sys_permission_revision(revision_id,version) VALUES(1,0)");}
    public void advance(){jdbc.update("UPDATE sys_permission_revision SET version=version+1 WHERE revision_id=1");}
    public Policy policy(Long id){
        if(!installed())return new Policy("LEGACY",Map.of(),Map.of(),List.of(),0);
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM sys_user_permission WHERE user_id=?",id);
        if(rows.isEmpty())return new Policy("LEGACY",Map.of(),Map.of(),List.of(),0);
        var r=rows.get(0);return new Policy(str(r.get("access_mode")),read(r.get("permissions_json"),new TypeReference<Map<String,Boolean>>(){}),read(r.get("scopes_json"),new TypeReference<Map<String,Object>>(){}),
                jdbc.queryForList("SELECT role_code FROM sys_user_permission_role WHERE user_id=? ORDER BY role_code",String.class,id),number(r.get("version")));
    }
    public List<PermissionCatalog.Role> roles(){
        Map<String,PermissionCatalog.Role> result=new LinkedHashMap<>(PermissionCatalog.BUILTINS);
        if(installed())for(var r:jdbc.queryForList("SELECT * FROM sys_permission_role ORDER BY role_code")){
            String code=str(r.get("role_code"));var base=PermissionCatalog.BUILTINS.get(code);
            result.put(code,new PermissionCatalog.Role(code,str(r.get("role_name")),read(r.get("permissions_json"),new TypeReference<List<String>>(){}),read(r.get("scopes_json"),new TypeReference<Map<String,Object>>(){}),base!=null,base!=null&&base.dynamic(),number(r.get("enabled"))==1,number(r.get("version"))));
        }
        return new ArrayList<>(result.values());
    }
    public State state(Long id){return cached("state:"+id,()->evaluate(id,policy(id),Map.of()));}
    public State evaluate(Long id,Policy policy,Map<String,PermissionCatalog.Role> replacement){
        SysUser u=user(id);Map<String,PermissionCatalog.Role> definitions=roles().stream().collect(Collectors.toMap(PermissionCatalog.Role::code,r->r));definitions.putAll(replacement);
        Set<String> codes=new LinkedHashSet<>();if("LEGACY".equals(policy.mode()))codes.addAll(automaticRoles(u));codes.addAll(policy.roles());
        List<PermissionCatalog.Role> active=codes.stream().map(definitions::get).filter(Objects::nonNull).filter(PermissionCatalog.Role::enabled).toList();
        Set<String> granted=new LinkedHashSet<>();active.forEach(r->granted.addAll(r.permissions()));
        policy.overrides().forEach((k,v)->{if(Boolean.TRUE.equals(v))granted.add(k);else granted.remove(k);});
        boolean admin="0".equals(u.getRole());if(admin)granted.addAll(PermissionCatalog.ACTIONS.keySet());
        // 账号、授权和他人评语管理不属于可委派的业务功能。
        granted.retainAll(PermissionCatalog.ACTIONS.keySet());
        for(String module:List.of("tasks","performance","achievements","core","records","budget"))if(!granted.contains(module+".read"))granted.removeIf(k->k.startsWith(module+"."));
        if(!granted.contains("tasks.read"))granted.removeAll(List.of("expert.read","expert.write"));if(!granted.contains("expert.read"))granted.remove("expert.write");
        boolean configured=!"LEGACY".equals(policy.mode())||!policy.overrides().isEmpty()||!policy.scopes().isEmpty()||!policy.roles().isEmpty()||codes.stream().map(definitions::get).filter(Objects::nonNull).anyMatch(r->r.version()>0||!r.builtin());
        return new State(id,admin,configured,policy,active,Collections.unmodifiableSet(granted),revision());
    }
    private Set<String> automaticRoles(SysUser u){
        Long id=u.getUserId();Set<String> result=new LinkedHashSet<>();result.add("2".equals(u.getRole())?"RESPONSIBLE":"USER");
        if("0".equals(u.getRole())){result.add("ADMIN");return result;}
        if(Objects.equals(id,110228L))result.add("ARCHIVER");
        var department=u.getDeptId()==null?null:users.getDeptById(u.getDeptId());
        boolean activeDept=department!=null&&!Integer.valueOf(1).equals(department.getIsDelete())&&!"1".equals(department.getStatus());
        if(activeDept&&Objects.equals(u.getDeptId(),100L))result.add("OFFICE");
        if(activeDept&&Objects.equals(u.getDeptId(),104L))result.add("FINANCE");
        if("2".equals(u.getRole()))result.add("BUDGET_VIEWER_INTERNAL"); // 补入下方的只读基础能力。
        if(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM sys_leadership_access WHERE user_id=? AND scope IN('ALL','DEPARTMENTS'))",Integer.class,id)==1)result.add("LEADERSHIP");
        if(AchievementPermissionUtil.isAchievementUploadAccount(u,department))result.add("ACHIEVEMENT_REPORTER");
        if(AchievementPermissionUtil.isAchievementAuditor(u))result.add("ACHIEVEMENT_REVIEWER");
        if(recordViewers.contains(id))result.add("RECORD_VIEWER");
        if(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM biz_work_record WHERE owner_id=? AND delete_marker=0)",Integer.class,id)==1)result.add("RECORD_OWNER");
        if(!records.fillableYears(u).isEmpty())result.add("RECORD_REPORTER");
        if(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM biz_task t WHERE t.level=3 AND t.phase BETWEEN 2025 AND 2029 AND COALESCE(t.is_delete,0)=0 AND (t.leader_id=? OR EXISTS(SELECT 1 FROM biz_level4_task c WHERE c.parent_id=t.task_id AND c.leader_id=? AND COALESCE(c.is_delete,0)=0)))",Integer.class,id,id)==1)result.add("CORE_REPORTER");
        if(jdbc.queryForObject("SELECT (EXISTS(SELECT 1 FROM biz_performance p JOIN biz_core_indicator c ON BINARY c.indicator_code=BINARY p.perf_code WHERE p.principal_id=? AND COALESCE(p.is_delete,0)=0) OR EXISTS(SELECT 1 FROM biz_core_report r JOIN biz_core_indicator c ON c.indicator_id=r.indicator_id AND BINARY c.indicator_code=BINARY r.performance_code WHERE r.current_handler_id=? AND r.flow_status IN(10,20)))",Integer.class,id,id)==1)result.add("CORE_REVIEWER");
        return result;
    }
    public boolean enforced(Long id){return id!=null&&installed();}
    public boolean configured(Long id){return id!=null&&state(id).configured();}
    public boolean allowed(Long id,String permission){return id!=null&&state(id).permissions().contains(permission);}
    public void require(Long id,String permission){if(!allowed(id,permission))throw new AdminManagementException(403,"没有"+PermissionCatalog.ACTIONS.getOrDefault(permission,"此操作")+"权限");}
    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> scopeRules(State s,String domain){
        if(s.admin())return List.of(Map.of("mode","ALL"));
        if(s.policy().scopes().containsKey(domain))return List.of((Map<String,Object>)s.policy().scopes().get(domain));
        List<Map<String,Object>> rules=new ArrayList<>();boolean leader=s.roles().stream().anyMatch(r->r.code().equals("LEADERSHIP"));
        for(var role:s.roles())if(role.scopes().get(domain) instanceof Map<?,?> rule){
            if("LEGACY".equals(s.policy().mode())&&leader&&Set.of("USER","RESPONSIBLE").contains(role.code())&&Set.of("tasks","performance").contains(domain))continue;
            rules.add((Map<String,Object>)rule);
        }
        return rules;
    }
    private boolean yearMatches(Map<String,Object> rule,Integer year){Set<Long> years=ids(rule.get("years"));return year==null||years.isEmpty()||years.contains(year.longValue());}
    public Set<Long> taskIds(State s,Integer year){
        if(!s.permissions().contains("tasks.read"))return Set.of();
        List<BizTask> all=tasks.getAllTasks().stream().filter(t->!Integer.valueOf(1).equals(t.getIsDelete())).toList();
        Map<Long,BizTask> byId=all.stream().collect(Collectors.toMap(BizTask::getTaskId,t->t));
        Set<Long> visible=new LinkedHashSet<>();Set<Long> business=tasks.getVisibleTasks(s.userId()).stream().map(BizTask::getTaskId).collect(Collectors.toSet());
        business.addAll(jdbc.queryForList("SELECT DISTINCT parent_id FROM biz_level4_task WHERE leader_id=? AND COALESCE(is_delete,0)=0",Long.class,s.userId()));
        List<Map<String,Object>> rules=scopeRules(s,"tasks");
        for(var task:all)if(Integer.valueOf(3).equals(task.getLevel())&&(year==null||Objects.equals(year,task.getPhase()))) {
            for(var rule:rules) {
                if(!yearMatches(rule,task.getPhase()))continue;String mode=str(rule.get("mode"));
                boolean match=switch(mode){case "ALL"->true;case "BUSINESS"->business.contains(task.getTaskId());case "OWN"->Objects.equals(task.getLeaderId(),s.userId());
                    case "LEADERSHIP"->leadershipAll(s.userId())||leadershipDepartments(s.userId()).contains(task.getDeptId())||Objects.equals(task.getLeaderId(),s.userId())||Objects.equals(task.getAuditorId(),s.userId())||Objects.equals(task.getPrincipalId(),s.userId());
                    case "CUSTOM"->ids(rule.get("departments")).contains(task.getDeptId())||ids(rule.get("items")).contains(task.getTaskId())||under(task,ids(rule.get("directories")),byId);default->false;};
                if(match){visible.add(task.getTaskId());break;}
            }
        }
        return visible;
    }
    public List<BizTask> visibleTasks(Long actor,Integer year){return visibleTasks(state(actor),year);}
    public List<BizTask> visibleTasks(State s,Integer year){
        List<BizTask> all=tasks.getAllTasks().stream().filter(t->!Integer.valueOf(1).equals(t.getIsDelete())).toList();Map<Long,BizTask> byId=all.stream().collect(Collectors.toMap(BizTask::getTaskId,t->t));
        Set<Long> leaves=taskIds(s,year),allowed=new HashSet<>(leaves);
        for(Long id:leaves){BizTask cursor=byId.get(id);Set<Long> seen=new HashSet<>();while(cursor!=null&&seen.add(cursor.getTaskId())){allowed.add(cursor.getTaskId());cursor=byId.get(cursor.getParentId());}}
        List<BizTask> result=new ArrayList<>();for(BizTask task:all)if(allowed.contains(task.getTaskId())){
            BizTask copy=new BizTask();BeanUtils.copyProperties(task,copy);
            if(!Integer.valueOf(3).equals(task.getLevel())) {
                List<BizTask> children=all.stream().filter(t->Integer.valueOf(3).equals(t.getLevel())&&under(t,Set.of(task.getTaskId()),byId)&&(year==null||Objects.equals(t.getPhase(),year))).toList();
                List<BizTask> selected=children.stream().filter(t->leaves.contains(t.getTaskId())).toList();
                int progress=selected.isEmpty()?0:(int)selected.stream().mapToInt(t->t.getProgress()==null?0:t.getProgress()).average().orElse(0);
                copy.setProgress(progress);copy.setCurrentValue(BigDecimal.valueOf(progress));copy.setTargetValue(BigDecimal.valueOf(100));copy.setStatus(progress==100?"3":"1");
                if(children.size()!=selected.size()){copy.setComment(null);copy.setExpTarget(null);copy.setExpEffect(null);copy.setExpMaterialDesc(null);copy.setLeaderId(null);copy.setAuditorId(null);copy.setPrincipalId(null);}
            }result.add(copy);
        }return result;
    }
    public boolean canTask(State s,Long id){
        if(id==null)return false;
        BizTask task=tasks.getTaskById(id);
        if(task!=null&&!Integer.valueOf(1).equals(task.getIsDelete())) {
            if(Integer.valueOf(3).equals(task.getLevel()))return taskIds(s,task.getPhase()).contains(id);
            return visibleTasks(s,null).stream().anyMatch(t->Objects.equals(t.getTaskId(),id));
        }
        List<Long> parent=jdbc.queryForList("SELECT parent_id FROM biz_level4_task WHERE task_id=? AND COALESCE(is_delete,0)=0",Long.class,id);return parent.size()==1&&canTask(s,parent.get(0));
    }
    public boolean canRead(Long actor,String domain,Long id,Integer year){return canRead(state(actor),domain,id,year);}
    public boolean canRead(State s,String domain,Long id,Integer year){
        if(!s.permissions().contains(domain+".read"))return false;if("tasks".equals(domain))return canTask(s,id);
        List<Map<String,Object>> rows=switch(domain){
            case "performance"->jdbc.queryForList("SELECT perf_id id,dept_id,leader_id,auditor_id,principal_id FROM biz_performance WHERE perf_id=? AND COALESCE(is_delete,0)=0",id);
            case "achievements"->jdbc.queryForList("SELECT ach_id id,dept_id,create_by owner_id,YEAR(got_time) record_year FROM biz_achievement WHERE ach_id=? AND COALESCE(is_delete,0)=0",id);
            case "core"->jdbc.queryForList("SELECT indicator_id id FROM biz_core_indicator WHERE indicator_id=?",id);
            case "records"->jdbc.queryForList("SELECT r.record_id id,r.owner_id,r.status,r.record_year,u.dept_id FROM biz_work_record r LEFT JOIN sys_user u ON u.user_id=r.owner_id WHERE r.record_id=? AND r.delete_marker=0",id);
            default->List.of();};
        if(rows.isEmpty())return false;return matchesRow(s,domain,id,year,rows.get(0));
    }
    private boolean matchesRow(State s,String domain,Long id,Integer year,Map<String,Object> row){
        if("records".equals(domain)){year=(int)number(row.get("record_year"));if(number(row.get("status"))!=1)return Objects.equals(number(row.get("owner_id")),s.userId())||org.example.utils.AdminTestContext.enabled(s.userId());}
        if("achievements".equals(domain))year=row.get("record_year")==null?null:(int)number(row.get("record_year"));
        if(s.admin())return true;SysUser u=user(s.userId());
        for(var rule:scopeRules(s,domain)){if(!yearMatches(rule,year)||"achievements".equals(domain)&&year==null&&!ids(rule.get("years")).isEmpty())continue;String mode=str(rule.get("mode"));
            boolean own=Objects.equals(number(row.get("owner_id")),s.userId())||Objects.equals(number(row.get("leader_id")),s.userId());
            boolean duty=own||Objects.equals(number(row.get("auditor_id")),s.userId())||Objects.equals(number(row.get("principal_id")),s.userId());
            if(switch(mode){case "ALL"->true;case "OWN"->own;case "CUSTOM"->ids(rule.get("items")).contains(id)||ids(rule.get("departments")).contains(number(row.get("dept_id")));
                case "LEADERSHIP"->leadershipAll(s.userId())||leadershipDepartments(s.userId()).contains(number(row.get("dept_id")))||duty;
                case "BUSINESS"->"performance".equals(domain)?duty||jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM sys_dept WHERE dept_id=? AND leader_id=? AND COALESCE(is_delete,0)=0)",Integer.class,row.get("dept_id"),s.userId())==1:own||Objects.equals(number(row.get("dept_id")),u.getDeptId());default->false;})return true;
        }return false;
    }
    public List<Long> recordIds(Long actor){State s=state(actor);if(!s.permissions().contains("records.read"))return List.of();return jdbc.queryForList("SELECT r.record_id id,r.owner_id,r.status,r.record_year,u.dept_id FROM biz_work_record r LEFT JOIN sys_user u ON u.user_id=r.owner_id WHERE r.delete_marker=0").stream().filter(r->matchesRow(s,"records",number(r.get("id")),null,r)).map(r->number(r.get("id"))).toList();}
    public boolean canRecordOwner(Long actor,Long owner,Long department,int year){Map<String,Object> row=new HashMap<>();row.put("owner_id",owner);row.put("dept_id",department);row.put("record_year",year);row.put("status",1);var state=state(actor);return state.permissions().contains("records.read")&&matchesRow(state,"records",0L,year,row);}
    public List<BizPerformance> visiblePerformances(Long actor,Integer year){State s=state(actor);List<BizPerformance> all=performances.getAllPerformance().stream().filter(p->!Integer.valueOf(1).equals(p.getIsDelete())).toList();Map<Long,BizPerformance> map=all.stream().collect(Collectors.toMap(BizPerformance::getPerfId,p->p));Set<Long> parents=all.stream().map(BizPerformance::getParentId).collect(Collectors.toSet());Set<Long> ids=new HashSet<>();
        for(var p:all)if(!parents.contains(p.getPerfId())&&canRead(s,"performance",p.getPerfId(),year)){Set<Long> visited=new HashSet<>();for(var cursor=p;cursor!=null&&visited.add(cursor.getPerfId());cursor=map.get(cursor.getParentId()))ids.add(cursor.getPerfId());}
        return all.stream().filter(p->ids.contains(p.getPerfId())).map(p->{BizPerformance copy=new BizPerformance();BeanUtils.copyProperties(p,copy);if(parents.contains(copy.getPerfId())||year==null&&scopeRules(s,"performance").stream().allMatch(r->!ids(r.get("years")).isEmpty())){copy.setCurrentValue(null);copy.setTargetValue(null);}return copy;}).toList();
    }
    public boolean budgetScope(Long actor,Integer year){State s=state(actor);return s.permissions().contains("budget.read")&&scopeRules(s,"budget").stream().anyMatch(r->yearMatches(r,year)&&"ALL".equals(r.get("mode")));}
    public boolean fullTaskScope(Long actor){State s=state(actor);return s.admin()||s.permissions().contains("tasks.read")&&scopeRules(s,"tasks").stream().anyMatch(r->ids(r.get("years")).isEmpty()&&("ALL".equals(r.get("mode"))||"LEADERSHIP".equals(r.get("mode"))&&leadershipAll(actor)));}
    public boolean canFile(Long actor,Long fileId){State s=state(actor);if(s.admin())return true;
        var linkedTasks=jdbc.queryForList("SELECT DISTINCT task_id FROM biz_material_submission WHERE file_id=?",Long.class,fileId);
        for(Long task:linkedTasks)if(s.permissions().contains("tasks.materials")&&canTask(s,task))return true;
        var linkedAchievements=jdbc.queryForList("SELECT ach_id FROM biz_achievement WHERE file_id=?",Long.class,fileId);
        var linkedPerformance=jdbc.queryForList("SELECT perf_id,year FROM biz_performance_submission WHERE file_id=?",fileId);
        for(var item:linkedPerformance)if(s.permissions().contains("performance.materials")&&canRead(s,"performance",number(item.get("perf_id")),(int)number(item.get("year"))))return true;
        for(Long ach:linkedAchievements)if(s.permissions().contains("achievements.materials")&&canRead(s,"achievements",ach,null))return true;
        var linkedBudgets=jdbc.queryForList("SELECT year FROM biz_budget_sheet WHERE file_id=? UNION SELECT year FROM biz_budget_import_version WHERE file_id=?",Integer.class,fileId,fileId);
        for(Integer year:linkedBudgets)if(s.permissions().contains("budget.materials")&&budgetScope(actor,year))return true;
        return linkedTasks.isEmpty()&&linkedAchievements.isEmpty()&&linkedBudgets.isEmpty()&&linkedPerformance.isEmpty()&&jdbc.queryForObject("SELECT COUNT(*) FROM sys_file WHERE file_id=? AND upload_by=? AND COALESCE(is_delete,0)=0",Integer.class,fileId,actor)>0&&s.permissions().stream().anyMatch(p->p.endsWith(".materials"));
    }
    public Map<String,Object> description(Long actor){State s=state(actor);Map<String,Object> result=new LinkedHashMap<>();result.put("permissions",s.permissions());result.put("permissionVersion",s.revision());result.put("permissionConfigured",s.configured());result.put("permissionEnforced",installed());result.put("permissionRoles",s.roles());Map<String,Object> ranges=new LinkedHashMap<>();for(String module:List.of("tasks","performance","achievements","core","records","budget"))ranges.put(module,scopeRules(s,module));result.put("dataScopes",ranges);return result;}
    public boolean leadershipAll(Long id){return jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM sys_leadership_access WHERE user_id=? AND scope='ALL')",Integer.class,id)==1;}
    public Set<Long> leadershipDepartments(Long id){return new HashSet<>(jdbc.queryForList("SELECT d.dept_id FROM sys_leadership_department d JOIN sys_leadership_access a ON a.user_id=d.user_id AND a.scope='DEPARTMENTS' WHERE d.user_id=?",Long.class,id));}
    private boolean under(BizTask task,Set<Long> roots,Map<Long,BizTask> byId){Set<Long> seen=new HashSet<>();for(BizTask p=task;p!=null&&seen.add(p.getTaskId());p=byId.get(p.getParentId()))if(roots.contains(p.getTaskId()))return true;return false;}
    public static Set<Long> ids(Object value){if(!(value instanceof Collection<?> values))return Set.of();return values.stream().filter(Objects::nonNull).map(PermissionService::number).collect(Collectors.toSet());}
    public static long number(Object value){return value==null?0:Long.parseLong(value.toString());}
    public static String str(Object value){return Objects.toString(value,"");}
    public <T>T read(Object value,TypeReference<T> type){try{return json.readValue(str(value),type);}catch(Exception e){throw new AdminManagementException(500,"授权配置无效，请联系管理员");}}
    public String write(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalArgumentException("无法保存授权配置",e);}}
    @SuppressWarnings("unchecked") private <T>T cached(String key,Supplier<T> supplier){if(!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a)||!"GET".equals(a.getRequest().getMethod()))return supplier.get();String name=PermissionService.class.getName()+":"+key;Object old=a.getRequest().getAttribute(name);if(old!=null)return(T)old;T value=supplier.get();a.getRequest().setAttribute(name,value);return value;}
}
