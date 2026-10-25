package org.example.service;

import org.springframework.stereotype.Service;
import java.util.*;
import static org.example.service.PermissionService.*;

/** Read queries only. Compiles the current request's existing role/personal scope algebra. */
@Service
public class ReadScopeSql {
    public record Clause(String sql,List<Object> args) {}
    private final PermissionService permissions;private final LeadershipAccessService leadership;
    public ReadScopeSql(PermissionService permissions,LeadershipAccessService leadership){this.permissions=permissions;this.leadership=leadership;}
    public Clause tasks(Long actor,String alias,Integer year,boolean overview){
        if(overview&&!permissions.configured(actor)&&leadership.isLeader(actor)&&!leadership.isAdmin(actor)){
            var scope=leadership.scope(actor);List<Object> args=new ArrayList<>();return new Clause(scope.scope().equals("ALL")?"1=1":in(alias+".dept_id",scope.departmentIds(),args),args);
        }
        return scope(actor,"tasks",alias,year,null);
    }
    public Clause row(Long actor,String domain,String alias,Integer year){return scope(actor,domain,alias,year,null);}
    public Clause rowByYear(Long actor,String domain,String alias,String yearExpression){return scope(actor,domain,alias,null,null,"0",yearExpression);}
    public Clause taskObjects(Long actor,String id){
        var range=tasks(actor,"t",null,false);
        String authorized="WITH RECURSIVE allowed AS (SELECT t.task_id,t.parent_id,JSON_ARRAY(t.task_id) seen FROM biz_task t WHERE t.level=3 AND (t.is_delete IS NULL OR t.is_delete<>1) AND ("+range.sql()+")), visible(task_id,parent_id,seen) AS (SELECT task_id,parent_id,seen FROM allowed UNION ALL SELECT p.task_id,p.parent_id,JSON_ARRAY_APPEND(v.seen,'$',p.task_id) FROM visible v JOIN biz_task p ON p.task_id=v.parent_id WHERE p.level<=3 AND (p.is_delete IS NULL OR p.is_delete<>1) AND NOT JSON_CONTAINS(v.seen,CAST(p.task_id AS JSON))) SELECT task_id FROM visible UNION SELECT g.task_id FROM biz_level4_task g WHERE COALESCE(g.is_delete,0)=0 AND g.parent_id IN (SELECT task_id FROM visible) AND NOT EXISTS(SELECT 1 FROM biz_task t WHERE t.task_id=g.task_id AND t.level<=3 AND (t.is_delete IS NULL OR t.is_delete<>1))";
        return new Clause(id+" IN ("+authorized+")",range.args());
    }
    public Clause recordRows(Long actor,String alias,String department,Integer year){return scope(actor,"records",alias+".owner_id",year,department,alias+".record_id");}
    public Clause recordOwners(Long actor,String owner,String dept,Integer year){return scope(actor,"records",owner,year,dept);}
    private Clause scope(Long actor,String domain,String a,Integer year,String department){return scope(actor,domain,a,year,department,"0");}
    private Clause scope(Long actor,String domain,String a,Integer year,String department,String recordId){return scope(actor,domain,a,year,department,recordId,null);}
    private Clause scope(Long actor,String domain,String a,Integer year,String department,String recordId,String yearExpression){
        var state=permissions.state(actor);if(!state.permissions().contains(domain+".read"))return new Clause("0=1",List.of());if(state.admin())return new Clause("1=1",List.of());
        List<Object> args=new ArrayList<>();List<String> alternatives=new ArrayList<>();boolean task=domain.equals("tasks"),record=domain.equals("records");String owner=record?a:domain.equals("achievements")?a+".create_by":domain.equals("core")?"0":a+".leader_id",dept=record?department:domain.equals("core")?"NULL":a+".dept_id",id=record?recordId:a+(task?".task_id":domain.equals("performance")?".perf_id":domain.equals("achievements")?".ach_id":".indicator_id");
        for(var rule:permissions.scopeRules(state,domain)){
            var years=ids(rule.get("years"));if(year!=null&&!years.isEmpty()&&!years.contains(year.longValue()))continue;
            List<Object> local=new ArrayList<>();String condition;
            switch(str(rule.get("mode"))){
                case "ALL":condition="1=1";break;
                case "OWN":condition=equal(owner,actor,local);break;
                case "BUSINESS":
                    if(task){condition="("+duties(a,actor,local)+" OR EXISTS(SELECT 1 FROM sys_dept sd WHERE sd.dept_id="+dept+" AND sd.leader_id=? AND sd.is_delete=0) OR EXISTS(SELECT 1 FROM biz_level4_task g WHERE g.parent_id="+a+".task_id AND g.leader_id=? AND COALESCE(g.is_delete,0)=0))";local.add(actor);local.add(actor);}
                    else if(domain.equals("performance")){condition="("+duties(a,actor,local)+" OR EXISTS(SELECT 1 FROM sys_dept sd WHERE sd.dept_id="+dept+" AND sd.leader_id=? AND COALESCE(sd.is_delete,0)=0))";local.add(actor);}
                    else{condition="("+equal(owner,actor,local)+" OR "+equal(dept,permissions.user(actor).getDeptId(),local)+")";}
                    break;
                case "LEADERSHIP":
                    if(permissions.leadershipAll(actor))condition="1=1";
                    else{condition="("+in(task?dept:"COALESCE("+dept+",0)",permissions.leadershipDepartments(actor),local)+" OR "+(task||domain.equals("performance")?duties(a,actor,local):equal(owner,actor,local))+")";}break;
                case "CUSTOM":
                    condition="("+in(task?dept:"COALESCE("+dept+",0)",ids(rule.get("departments")),local)+" OR "+in(id,ids(rule.get("items")),local);
                    if(task&&!ids(rule.get("directories")).isEmpty()){
                        condition+=" OR EXISTS(WITH RECURSIVE path_nodes(task_id,parent_id,seen) AS (SELECT q.task_id,q.parent_id,JSON_ARRAY(q.task_id) FROM biz_task q WHERE q.task_id="+a+".task_id AND q.level<=3 AND (q.is_delete IS NULL OR q.is_delete<>1) UNION ALL SELECT p.task_id,p.parent_id,JSON_ARRAY_APPEND(n.seen,'$',p.task_id) FROM biz_task p JOIN path_nodes n ON p.task_id=n.parent_id WHERE p.level<=3 AND (p.is_delete IS NULL OR p.is_delete<>1) AND NOT JSON_CONTAINS(n.seen,CAST(p.task_id AS JSON))) SELECT 1 FROM path_nodes WHERE "+in("task_id",ids(rule.get("directories")),local)+")";
                    }condition+=")";break;
                default:condition="0=1";
            }
            if(task&&year==null&&!years.isEmpty())condition="("+condition+") AND ("+a+".phase IS NULL OR "+in(a+".phase",years,local)+")";
            if(domain.equals("achievements")&&!years.isEmpty()){condition="("+condition+") AND "+in("YEAR("+a+".got_time)",years,local);}
            if(yearExpression!=null&&!years.isEmpty())condition="("+condition+") AND ("+yearExpression+" IS NULL OR "+in(yearExpression,years,local)+")";
            alternatives.add("("+condition+")");args.addAll(local);
        }
        return new Clause(alternatives.isEmpty()?"0=1":String.join(" OR ",alternatives),List.copyOf(args));
    }
    private static String duties(String a,Long actor,List<Object> args){return "("+equal(a+".leader_id",actor,args)+" OR "+equal(a+".auditor_id",actor,args)+" OR "+equal(a+".principal_id",actor,args)+")";}
    private static String equal(String column,Object value,List<Object> args){if(value==null)return "0=1";args.add(value);return column+"=?";}
    public static String in(String column,Collection<?> values,List<Object> args){if(values.isEmpty())return "0=1";args.addAll(values);return column+" IN ("+String.join(",",Collections.nCopies(values.size(),"?"))+")";}
}
