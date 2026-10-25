package org.example.service;

import org.example.mapper.WorkRecordQualificationSql;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import java.util.*;
import java.math.*;
import java.time.*;

/** SQL-only read projections. Scope, metric, grouping and page are separate query stages. */
@Service
public class OverviewReadQueries {
    public record Source(String sql,List<Object> args){}
    private final JdbcTemplate jdbc;private final ReadScopeSql scopes;
    public OverviewReadQueries(JdbcTemplate jdbc,ReadScopeSql scopes){this.jdbc=jdbc;this.scopes=scopes;}
    public static boolean supports(String module){return Set.of("tasks","performance","achievements","records").contains(module);}
    private String department(String alias){return "CASE WHEN d.dept_id IS NULL THEN '未配置部门' ELSE COALESCE(d.dept_name,'') END";}
    private String names(){return " LEFT JOIN sys_dept d ON d.dept_id=b.dept_id LEFT JOIN sys_user u ON u.user_id=b.leader_id";}
    private String owner(){return "CASE WHEN u.user_id IS NULL THEN '未配置' ELSE COALESCE(NULLIF(u.nick_name,''),u.user_name,'') END";}
    public Source source(String module,int year,int month,Long actor){
        var args=new ArrayList<Object>();String sql;
        switch(module){
            case "tasks":{
                var range=scopes.tasks(actor,"t",year,true);args.add(year);args.addAll(range.args());
                sql="WITH RECURSIVE authorized AS (SELECT t.task_id,t.parent_id,t.task_code,t.task_name,t.dept_id,t.leader_id,t.status,t.progress,t.is_priority,t.is_difficult FROM biz_task t WHERE t.level=3 AND t.phase=? AND (t.is_delete IS NULL OR t.is_delete<>1) AND ("+range.sql()+")), path_nodes(leaf_id,task_id,parent_id,level,task_name,seen) AS (SELECT b.task_id,b.task_id,b.parent_id,3,b.task_name,JSON_ARRAY(b.task_id) FROM authorized b UNION ALL SELECT n.leaf_id,p.task_id,p.parent_id,p.level,p.task_name,JSON_ARRAY_APPEND(n.seen,'$',p.task_id) FROM path_nodes n JOIN biz_task p ON p.task_id=n.parent_id AND p.level<=3 AND (p.is_delete IS NULL OR p.is_delete<>1) WHERE n.level<>1 AND NOT JSON_CONTAINS(n.seen,CAST(p.task_id AS JSON))) SELECT b.task_id id,b.task_code code,b.task_name name,'task' kind,COALESCE(CAST(b.dept_id AS CHAR),'unknown') departmentKey,"+department("b")+" department,"+owner()+" owner,CASE BINARY b.status WHEN '0' THEN '未开始' WHEN '1' THEN '进行中' WHEN '2' THEN '审核中' WHEN '3' THEN '已完成' ELSE '状态待核实' END status,b.progress,COALESCE(b.is_priority<>0,0) priority,COALESCE(b.is_difficult<>0,0) difficult,"+(LocalDate.now(ZoneId.of("Asia/Shanghai")).isAfter(LocalDate.of(year,12,31))?"CASE WHEN BINARY b.status='3' THEN 0 ELSE 1 END":"0")+" overdue,COALESCE(CAST(root.task_id AS CHAR),'unknown') reformKey,CASE WHEN root.task_id IS NULL THEN '目录待核实' ELSE root.task_name END reform,b.task_id _order FROM authorized b LEFT JOIN path_nodes root ON root.leaf_id=b.task_id AND root.level=1"+names();break;
            }
            case "performance":{
                var range=scopes.row(actor,"performance","p",year);args.addAll(range.args());args.add(year);
                // Character.isWhitespace, rather than database \s, defines blank ancestry segments.
                String blank="[\\\\x{0009}-\\\\x{000D}\\\\x{001C}-\\\\x{0020}\\\\x{1680}\\\\x{2000}-\\\\x{2006}\\\\x{2008}-\\\\x{200A}\\\\x{2028}\\\\x{2029}\\\\x{205F}\\\\x{3000}]";
                sql="WITH authorized AS (SELECT p.perf_id,p.perf_code,p.perf_name,p.dept_id,p.leader_id,p.ancestors FROM biz_performance p WHERE COALESCE(p.is_delete,0)=0 AND ("+range.sql()+")), tokens AS (SELECT b.perf_id,j.token,ROW_NUMBER() OVER(PARTITION BY b.perf_id ORDER BY j.ord) pos FROM authorized b JOIN JSON_TABLE(CONCAT('[',REPLACE(JSON_QUOTE(b.ancestors),',','\",\"'),']'),'$[*]' COLUMNS(ord FOR ORDINALITY,token LONGTEXT PATH '$')) j WHERE NOT REGEXP_LIKE(j.token,'^"+blank+"*$','c')), paths AS (SELECT perf_id,COUNT(*) depth,MAX(CASE WHEN pos=3 THEN REGEXP_REPLACE(token,'^[\\\\x{0000}-\\\\x{0020}]+|[\\\\x{0000}-\\\\x{0020}]+$','') END) categoryKey FROM tokens GROUP BY perf_id), annual AS (SELECT perf_id,target_value,actual_value,1 rn FROM biz_performance_year WHERE year=? AND (is_delete IS NULL OR is_delete<>1) "+annualExceptions(year)+") SELECT b.perf_id id,b.perf_code code,b.perf_name name,'performance' kind,COALESCE(CAST(b.dept_id AS CHAR),'unknown') departmentKey,"+department("b")+" department,"+owner()+" owner,CASE WHEN y.target_value IS NULL OR y.target_value<=0 OR y.actual_value IS NULL THEN '数据待核实' WHEN y.actual_value>=y.target_value THEN '数值达标' ELSE '未达标' END status,y.target_value target,y.actual_value value,path.categoryKey,CASE BINARY path.categoryKey WHEN '1.1' THEN '数量指标' WHEN '1.2' THEN '质量指标' WHEN '1.3' THEN '时效指标' WHEN '2.1' THEN '社会效益指标' WHEN '2.2' THEN '可持续影响指标' WHEN '3.1' THEN '服务对象满意度指标' ELSE path.categoryKey END category,CASE WHEN BINARY b.perf_code LIKE BINARY '1.1.%' OR BINARY b.perf_code LIKE BINARY '1.2.%' THEN '自动' ELSE '手动' END mode,b.perf_id _order FROM authorized b JOIN paths path ON path.perf_id=b.perf_id AND path.depth>=3 LEFT JOIN annual y ON y.perf_id=b.perf_id AND y.rn=1"+names();break;
            }
            case "achievements":{
                var range=scopes.row(actor,"achievements","a",year);args.add(year);args.addAll(range.args());
                sql="SELECT a.ach_id id,'' code,a.ach_name name,'achievement' kind,COALESCE(CAST(a.dept_id AS CHAR),'unknown') departmentKey,CASE WHEN d.dept_id IS NULL THEN '未配置部门' ELSE COALESCE(d.dept_name,'') END department,CASE WHEN a.audit_status=30 THEN '已归档' WHEN a.audit_status<0 THEN '已驳回' WHEN a.audit_status=10 THEN '待审核' ELSE '状态待核实' END status,COALESCE(a.te_deng_jiang,0)+COALESCE(a.yi_deng_jiang,0)+COALESCE(a.er_deng_jiang,0)+COALESCE(a.san_deng_jiang,0)+COALESCE(a.jin_jiang,0)+COALESCE(a.yin_jiang,0)+COALESCE(a.tong_jiang,0)+COALESCE(a.you_sheng_jiang,0)+COALESCE(a.bud_deng_deng_ci,0) quantity,CAST(CASE WHEN a.category BETWEEN 1 AND 10 THEN a.category ELSE 0 END AS CHAR) categoryKey,CASE a.category WHEN 1 THEN '落实立德树人根本任务' WHEN 2 THEN '创新产教融合机制' WHEN 3 THEN '打造高水平专业群' WHEN 4 THEN '建设一流核心课程' WHEN 5 THEN '开放优质新形态教材' WHEN 6 THEN '建设高水平双师队伍' WHEN 7 THEN '建设产教融合实训基地' WHEN 8 THEN '构建数字化教学新生态' WHEN 9 THEN '拓展国际交流与合作' WHEN 10 THEN '打造一流区域型高端智库' ELSE '未分类' END category,COALESCE(a.level,'未填写') level,COALESCE(a.level,'未填写') levelKey,a.ach_id _order FROM biz_achievement a LEFT JOIN sys_dept d ON d.dept_id=a.dept_id WHERE a.is_delete=0 AND YEAR(a.got_time)=? AND ("+range.sql()+")";break;
            }
            case "records":{
                var owners=scopes.recordOwners(actor,"u.user_id","u.dept_id",year);var visible=scopes.recordRows(actor,"r","u.dept_id",year);
                args.addAll(owners.args());args.add(year);args.add(month);args.addAll(visible.args());
                sql="WITH eligible AS (SELECT u.user_id,u.nick_name,u.dept_id FROM sys_user u WHERE "+WorkRecordQualificationSql.reporter("u","= "+year)+" AND ("+owners.sql()+")), submitted AS (SELECT r.record_id,r.owner_id,r.owner_name,r.submit_time,ROW_NUMBER() OVER(PARTITION BY r.owner_id ORDER BY r.record_id DESC) rn FROM biz_work_record r LEFT JOIN sys_user u ON u.user_id=r.owner_id WHERE r.record_year=? AND r.record_month=? AND r.status=1 AND r.delete_marker=0 AND ("+visible.sql()+")), rows_source AS (SELECT u.user_id id,CAST(u.user_id AS CHAR) code,u.nick_name name,'record' kind,COALESCE(CAST(u.dept_id AS CHAR),'unknown') departmentKey,CASE WHEN d.dept_id IS NULL THEN '未配置部门' ELSE COALESCE(d.dept_name,'') END department,1 eligible,CASE WHEN s.record_id IS NULL THEN 0 ELSE 1 END submitted,s.record_id recordId,s.submit_time submitTime,CASE WHEN s.record_id IS NULL THEN '未提交' ELSE '已提交' END status,0 _historical,u.user_id _order FROM eligible u LEFT JOIN submitted s ON s.owner_id=u.user_id AND s.rn=1 LEFT JOIN sys_dept d ON d.dept_id=u.dept_id UNION ALL SELECT s.owner_id,CAST(s.owner_id AS CHAR),s.owner_name,'record','historical','原资格记录',0,1,s.record_id,s.submit_time,'原资格已提交',1,s.owner_id FROM submitted s WHERE s.rn=1 AND NOT EXISTS(SELECT 1 FROM eligible e WHERE e.user_id=s.owner_id)) SELECT * FROM rows_source";break;
            }
            default:throw new IllegalArgumentException(module);
        }
        return new Source(sql,List.copyOf(args));
    }
    /** Only ambiguous annual groups are resolved in their original JDBC encounter order. */
    private String annualExceptions(int year){
        String cacheKey=getClass().getName()+".annualExceptions:"+year;
        var attributes=org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if(attributes!=null&&attributes.getAttribute(cacheKey,0) instanceof String cached)return cached;
        var ids=jdbc.queryForList("SELECT perf_id FROM biz_performance_year WHERE year=? AND (is_delete IS NULL OR is_delete<>1) GROUP BY perf_id HAVING COUNT(*)>1",Long.class,year);
        if(ids.isEmpty()){if(attributes!=null)attributes.setAttribute(cacheKey,"",0);return "";}
        Set<Long> chosen=new LinkedHashSet<>();
        for(int offset=0;offset<ids.size();offset+=500){Set<Long> batch=new HashSet<>(ids.subList(offset,Math.min(offset+500,ids.size())));Map<Long,Long> last=new HashMap<>();
            jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Void>)connection->{try(var statement=connection.prepareStatement("SELECT perf_id,year_id,CAST(is_delete AS SIGNED) deleted FROM biz_performance_year WHERE year=?",java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY)){statement.setInt(1,year);statement.setFetchSize(Integer.MIN_VALUE);try(var rows=statement.executeQuery()){while(rows.next()){long id=rows.getLong("perf_id");if(batch.contains(id)&&rows.getInt("deleted")!=1)last.put(id,rows.getLong("year_id"));}}}return null;});chosen.addAll(last.values());
        }
        String clause=" AND (perf_id NOT IN ("+ids.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","))+") OR year_id IN ("+chosen.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","))+"))";
        if(attributes!=null)attributes.setAttribute(cacheKey,clause,0);return clause;
    }
    private String order(String module){return module.equals("records")?"_historical,_order":"_order";}
    private String metricWhere(String module,String key){return switch(module+"."+key){
        case "tasks.completed"->"status='已完成'";case "tasks.reviewing"->"status='审核中'";case "tasks.priority","tasks.difficult","tasks.overdue"->key+"=1";
        case "performance.attained"->"status='数值达标'";case "performance.unattained"->"status='未达标'";case "performance.uncertain"->"status='数据待核实'";case "performance.manual"->"mode='手动'";case "performance.automatic"->"mode='自动'";
        case "achievements.archived","achievements.awards"->"status='已归档'";case "achievements.pending"->"status='待审核'";case "achievements.rejected"->"status='已驳回'";
        case "records.expected","records.submissionRate"->"eligible=1";case "records.submitted"->"eligible=1 AND submitted=1";case "records.missing"->"eligible=1 AND submitted=0";case "records.historical"->"eligible=0";default->"1=1";};}
    public Map<String,Object> summary(Source source,String module){
        var columns=new ArrayList<String>();
        for(String key:switch(module){case "tasks"->List.of("total","completed","reviewing","priority","difficult","overdue");case "performance"->List.of("total","attained","unattained","uncertain","manual","automatic");case "achievements"->List.of("total","archived","pending","rejected");default->List.of("expected","submitted","missing","historical");})columns.add("COALESCE(SUM(CASE WHEN "+metricWhere(module,key)+" THEN 1 ELSE 0 END),0) "+key);
        if(module.equals("tasks")){columns.add("COUNT(progress) progressCount");columns.add("SUM(progress) progressSum");}
        if(module.equals("achievements"))columns.add("COALESCE(SUM(CASE WHEN status='已归档' THEN quantity ELSE 0 END),0) awards");
        return jdbc.queryForMap("SELECT "+String.join(",",columns)+" FROM ("+source.sql()+") authorized_rows",source.args().toArray());
    }
    public long missingAchievementDates(Long actor){var range=scopes.row(actor,"achievements","a",null);return jdbc.queryForObject("SELECT COUNT(*) FROM biz_achievement a WHERE a.is_delete=0 AND a.got_time IS NULL AND ("+range.sql()+")",Long.class,range.args().toArray());}
    public Map<String,Object> analysis(Source source,String module,String key,String dimension,String group,int requested,int size,MultiValueMap<String,String> filters){
        String from=" FROM ("+source.sql()+") authorized_rows WHERE "+metricWhere(module,key);var args=new ArrayList<>(source.args());var fields=new ArrayList<ListFilterQuery.Field>();
        for(String field:List.of("department","category","status")){
            if(field.equals("category")&&!Set.of("performance","achievements").contains(module))continue;
            var options=jdbc.queryForList("SELECT option_values.value FROM (SELECT DISTINCT CAST("+field+" AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_bin value"+from+") option_values ORDER BY CAST(CONVERT(option_values.value USING utf16) AS BINARY)",args.toArray()).stream().map(r->{String v=Objects.toString(r.get("value"),"");return Map.<String,Object>of("value",v,"label",v);}).toList();
            fields.add(new ListFilterQuery.Field(field,field.equals("department")?"部门":field.equals("category")?"类别":"状态","multi",true,"基础",options));
        }
        var matcher=new ListFilterQuery(filters,fields);
        // These three fields contain nonblank, server-defined labels; binary equality is the original multi matcher.
        for(var field:fields){var values=filters.getOrDefault("f."+field.key(),List.of()).stream().filter(v->v!=null&&!v.isBlank()).distinct().toList();if(values.isEmpty())continue;
            String column=field.key();String blank="^[\\\\x{0009}-\\\\x{000D}\\\\x{001C}-\\\\x{0020}\\\\x{1680}\\\\x{2000}-\\\\x{2006}\\\\x{2008}-\\\\x{200A}\\\\x{2028}\\\\x{2029}\\\\x{205F}\\\\x{3000}]*$";
            from+=" AND BINARY CASE WHEN "+column+" IS NULL OR REGEXP_LIKE("+column+",'"+blank+"','c') THEN 'missing' ELSE "+column+" END IN ("+String.join(",",Collections.nCopies(values.size(),"?"))+")";args.addAll(values);
        }
        String field=dimension.equals("status")?"status":dimension+"Key",weight=module.equals("achievements")&&key.equals("awards")?"quantity":"1";
        var chart=jdbc.queryForList("SELECT MIN("+field+") `key`,MIN("+dimension+") name,SUM("+weight+") value,MIN(_order) sortKey"+from+" GROUP BY BINARY "+field+" ORDER BY "+(module.equals("records")?"MIN(_historical),":"")+"sortKey",args.toArray());
        for(var row:chart){row.remove("sortKey");row.put("key",Objects.toString(row.get("key"),""));row.put("incomplete",false);if(row.get("value")!=null)row.put("value",new BigDecimal(row.get("value").toString()));}
        if(module.equals("records")&&dimension.equals("department")){
            var additional=jdbc.queryForList("SELECT MIN("+field+") `key`,SUM(submitted=1) submitted,SUM(submitted=0) missing"+from+" GROUP BY BINARY "+field,args.toArray());var index=new HashMap<String,Map<String,Object>>();additional.forEach(r->index.put(Objects.toString(r.get("key"),""),r));for(var r:chart){var row=index.get(r.get("key"));r.put("submitted",row.get("submitted"));r.put("missing",row.get("missing"));}
        }
        if(group!=null&&!group.isBlank()){from+=" AND BINARY "+field+"=BINARY ?";args.add(group);}
        long total=jdbc.queryForObject("SELECT COUNT(*)"+from,Long.class,args.toArray());int page=(int)Math.min(requested,Math.max(1,(total+size-1)/size));args.add(size);args.add((long)(page-1)*size);
        var rows=jdbc.queryForList("SELECT *"+from+" ORDER BY "+order(module)+" LIMIT ? OFFSET ?",args.toArray());
        for(var row:rows){row.keySet().removeIf(k->k.startsWith("_"));row.put("statusKey",row.get("status"));for(String bool:List.of("priority","difficult","overdue","eligible","submitted"))if(row.containsKey(bool))row.put(bool,((Number)row.get(bool)).intValue()!=0);}
        if(module.equals("achievements")){Map<Long,Object> reasons=new HashMap<>();if(!rows.isEmpty()){var ids=rows.stream().map(r->r.get("id")).toList();for(var r:jdbc.queryForList("SELECT a.ach_id,CASE WHEN a.audit_status<0 THEN (SELECT l.comment FROM biz_achievement_audit_log l JOIN biz_achievement_submission s ON s.sub_id=l.sub_id WHERE s.ach_id=a.ach_id AND s.is_delete=0 AND l.post_status<0 ORDER BY l.create_time DESC,l.log_id DESC LIMIT 1) END reason FROM biz_achievement a WHERE a.ach_id IN ("+String.join(",",Collections.nCopies(ids.size(),"?"))+")",ids.toArray()))reasons.put(((Number)r.get("ach_id")).longValue(),r.get("reason"));}rows.forEach(r->r.put("reason",reasons.get(((Number)r.get("id")).longValue())));}
        var result=new LinkedHashMap<String,Object>();result.put("filterApplied",matcher.active()||group!=null&&!group.isBlank());result.put("fields",fields);result.put("chart",chart);result.put("total",total);result.put("page",page);result.put("pageSize",size);result.put("rows",rows);return result;
    }
}
