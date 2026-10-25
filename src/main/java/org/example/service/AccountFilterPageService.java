package org.example.service;

import org.example.entity.SysUser;
import org.example.utils.AchievementPermissionUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.util.MultiValueMap;
import java.util.*;
import static org.example.service.ListFilterQuery.*;

/** Safe current-page user fields, with exact derived qualification in bounded batches. */
@Service
public class AccountFilterPageService {
    private static final List<Field> FIELDS=List.of(field("keyword","text"),field("deptId","multi"),field("roles","multi"),select("status","0","1"),select("role","0","1","2"),
            select("leadership","ALL","DEPARTMENTS","NONE"),select("expert","yes","no"),select("force","yes","no"),select("personal","yes","no"),select("duties","yes","no"));
    private static final String FROM=" FROM sys_user u LEFT JOIN sys_dept d ON d.dept_id=u.dept_id LEFT JOIN sys_leadership_access l ON l.user_id=u.user_id";
    private static final String COLUMNS="u.user_id userId,u.dept_id deptId,u.user_name userName,u.nick_name nickName,u.email,u.role,u.status,u.force_password_change forcePasswordChange,u.create_time createTime,u.update_time updateTime,d.dept_name deptName,u.is_delete _isDelete";
    // Materialize the small set of active responsibility IDs once, rather than
    // running eight correlated relationship scans for every candidate account.
    private static final String DUTIES="u.user_id IN (SELECT responsibility.userId FROM ("
            +"SELECT leader_id userId FROM sys_dept WHERE status='0' AND COALESCE(is_delete,0)=0 "
            +"UNION SELECT leader_id FROM biz_task WHERE level=3 AND COALESCE(is_delete,0)=0 "
            +"UNION SELECT auditor_id FROM biz_task WHERE level=3 AND COALESCE(is_delete,0)=0 "
            +"UNION SELECT principal_id FROM biz_task WHERE level=3 AND COALESCE(is_delete,0)=0 "
            +"UNION SELECT leader_id FROM biz_performance WHERE COALESCE(is_delete,0)=0 "
            +"UNION SELECT auditor_id FROM biz_performance WHERE COALESCE(is_delete,0)=0 "
            +"UNION SELECT principal_id FROM biz_performance WHERE COALESCE(is_delete,0)=0 "
            +"UNION SELECT x.leader_id FROM biz_level4_task x JOIN biz_task t ON t.task_id=x.parent_id WHERE COALESCE(x.is_delete,0)=0 AND COALESCE(t.is_delete,0)=0 "
            +"UNION SELECT current_handler_id FROM biz_material_submission WHERE COALESCE(is_delete,0)=0 AND flow_status IN(10,20,30) "
            +"UNION SELECT current_handler_id FROM biz_performance_submission WHERE COALESCE(is_delete,0)=0 AND flow_status IN(10,20) "
            +"UNION SELECT current_handler_id FROM biz_achievement WHERE COALESCE(is_delete,0)=0 AND audit_status=10 "
            +"UNION SELECT current_handler_id FROM biz_core_report WHERE flow_status IN(10,20)) responsibility)";
    private final JdbcTemplate jdbc;private final PermissionService access;
    public AccountFilterPageService(JdbcTemplate jdbc,PermissionService access){this.jdbc=jdbc;this.access=access;}
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public Map<String,Object> query(Long actor,MultiValueMap<String,String> params){
        access.requireAdmin(actor);var predicate=new ListFilterQuery(params,FIELDS);int requested=positive(params.getFirst("page"),1),size=Math.min(100,positive(params.getFirst("pageSize"),10));
        boolean derived=List.of("roles","personal","expert").stream().anyMatch(k->params.containsKey("f."+k));
        boolean matching=derived||!predicate.first("keyword").isEmpty();StringBuilder where=new StringBuilder(" WHERE COALESCE(u.is_delete,0)=0");List<Object> args=new ArrayList<>();
        for(String key:List.of("deptId","role","status","force","leadership","duties")){
            var selected=params.getOrDefault("f."+key,List.of()).stream().filter(v->v!=null&&!v.isBlank()).distinct().toList();if(selected.isEmpty())continue;
            String column=switch(key){case "deptId"->"u.dept_id";case "force"->"CASE WHEN COALESCE(u.force_password_change,0)=1 THEN 'yes' ELSE 'no' END";case "leadership"->"CASE WHEN l.user_id IS NULL THEN 'NONE' ELSE l.scope END";case "duties"->"CASE WHEN "+DUTIES+" THEN 'yes' ELSE 'no' END";default->"u."+key;};
            where.append(" AND BINARY COALESCE(CAST(").append(column).append(" AS CHAR),'missing') IN (").append(String.join(",",Collections.nCopies(selected.size(),"?"))).append(")");args.addAll(selected);
        }
        long total;int page;List<Map<String,Object>> result;
        if(!matching){
            total=jdbc.queryForObject("SELECT COUNT(*)"+FROM+where,Long.class,args.toArray());page=(int)Math.min(requested,Math.max(1,(total+size-1)/size));var pageArgs=new ArrayList<>(args);pageArgs.add(size);pageArgs.add((long)(page-1)*size);
            result=jdbc.queryForList("SELECT "+COLUMNS+FROM+where+" ORDER BY u.user_id LIMIT ? OFFSET ?",pageArgs.toArray());result.forEach(this::finish);
        }else{
            var collector=new BoundedFilterPage<Map<String,Object>>(requested,size);
            for(int offset=0;;offset+=BoundedFilterPage.BATCH_SIZE){
                var batchArgs=new ArrayList<>(args);batchArgs.add(BoundedFilterPage.BATCH_SIZE);batchArgs.add(offset);
                var rows=jdbc.queryForList("SELECT "+COLUMNS+",CASE WHEN l.user_id IS NULL THEN 'NONE' ELSE l.scope END _leadership"+FROM+where+" ORDER BY u.user_id LIMIT ? OFFSET ?",batchArgs.toArray());
                var users=rows.stream().map(this::account).toList();if(derived)access.prepareAccountList(users);
                try{for(int i=0;i<rows.size();i++){
                    var item=rows.get(i);var user=users.get(i);var values=new HashMap<>(item);values.put("keyword",str(item.get("userId"))+" "+str(item.get("userName"))+" "+str(item.get("nickName")));values.put("leadership",item.get("_leadership"));
                    values.put("force",yes(item.get("forcePasswordChange"))?"yes":"no");values.put("duties",predicate.first("duties"));if(derived)values.putAll(access.accountFilterValues(user));
                    if(predicate.matches(values,FIELDS)){finish(item);collector.accept(item);}
                }}finally{if(derived)access.releaseAccountList(users);}
                if(rows.size()<BoundedFilterPage.BATCH_SIZE)break;
            }
            var collected=collector.result();total=collected.total();page=collected.page();result=collected.items();
        }
        return Map.of("items",result,"total",Math.toIntExact(total),"page",page,"pageSize",size);
    }
    public Set<String> assignedCodes(){
        Set<String> result=new HashSet<>();
        for(int offset=0;;offset+=500){var batch=jdbc.query("SELECT user_id,dept_id,user_name,nick_name,role,status,is_delete FROM sys_user WHERE (is_delete IS NULL OR is_delete<>1) ORDER BY user_id LIMIT 500 OFFSET ?",org.springframework.jdbc.core.BeanPropertyRowMapper.newInstance(SysUser.class),offset);access.prepareAccountList(batch);try{for(var user:batch)result.addAll(access.assignedRoleCodes(user));}finally{access.releaseAccountList(batch);}if(batch.size()<500)break;}return result;
    }
    private void finish(Map<String,Object> item){var user=account(item);var dept=access.department(user.getDeptId());item.keySet().removeIf(k->k.startsWith("_"));item.values().removeIf(Objects::isNull);item.put("canViewAchievement",AchievementPermissionUtil.canViewAchievement(user,dept));item.put("canUploadAchievement",AchievementPermissionUtil.canUploadAchievement(user,dept));}
    private SysUser account(Map<String,Object> row){var user=new SysUser();user.setUserId(number(row.get("userId")));user.setDeptId(number(row.get("deptId")));user.setUserName((String)row.get("userName"));user.setNickName((String)row.get("nickName"));user.setRole((String)row.get("role"));user.setStatus((String)row.get("status"));Object deleted=row.get("_isDelete");user.setIsDelete(deleted==null?null:deleted instanceof Boolean flag?(flag?1:0):((Number)deleted).intValue());return user;}
    private Long number(Object v){return v==null?null:((Number)v).longValue();}
    private boolean yes(Object v){return Boolean.TRUE.equals(v)||v instanceof Number n&&n.intValue()==1||"1".equals(str(v))||"true".equalsIgnoreCase(str(v));}
    private static Field field(String key,String type){return new Field(key,key,type,true,"",List.of());}
    private static Field select(String key,String... values){return new Field(key,key,"select",true,"",Arrays.stream(values).map(v->Map.<String,Object>of("value",v,"label",v)).toList());}
    private int positive(String value,int fallback){if(value==null||value.isBlank())return fallback;try{int n=Integer.parseInt(value);if(n>0)return n;}catch(NumberFormatException ignored){}throw new AdminManagementException(400,"分页参数不正确");}
}
