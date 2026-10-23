package org.example.service;

import java.util.*;

/** 权限代码由应用定义；禁止将用户输入当作路径、SQL或可执行规则。 */
public final class PermissionCatalog {
    private PermissionCatalog() { }
    public static final Map<String,String> MODULES=new LinkedHashMap<>();
    public static final Map<String,String> ACTIONS=new LinkedHashMap<>();
    public record Role(String code,String name,List<String> permissions,Map<String,Object> scopes,boolean builtin,boolean dynamic,boolean enabled,long version) { }
    public static final Map<String,Role> BUILTINS=new LinkedHashMap<>();
    static {
        MODULES.put("tasks","建设任务");MODULES.put("performance","绩效");MODULES.put("achievements","成果");
        MODULES.put("core","核心指标");MODULES.put("records","工作纪实");MODULES.put("budget","预算");
        MODULES.put("dashboard","数据大屏");MODULES.put("overview","管理概览");MODULES.put("audit","审核中心");
        MODULES.put("messages","消息中心");MODULES.put("expert","专家评审");MODULES.put("management","业务管理");
        for(String module:List.of("tasks","performance","achievements","core","records","budget")) {
            add(module,"read","查阅");add(module,"materials","材料访问");add(module,"fill","填报");
            add(module,"export","导出");
            if(!List.of("records","budget").contains(module)){add(module,"audit","审核");add(module,"archive","归档");add(module,"withdraw","撤回");add(module,"history","审核记录");}
        }
        add("tasks","create","新增任务");add("tasks","transfer","任务移交");add("tasks","finish","完结任务");add("tasks","attributes","任务标记与日期");
        add("performance","transfer","绩效移交");add("performance","relations","绩效关联管理");
        add("achievements","delete","删除成果");add("records","delete","删除已提交纪实");
        add("budget","lock","锁定／解锁");add("budget","versions","历史版本");add("budget","restore","恢复版本");
        for(String module:List.of("dashboard","overview","audit","messages"))add(module,"read","查阅");
        add("messages","send","发送通知");add("expert","read","进入专家评审");add("expert","write","填写本人评语");
        add("management","refresh","业务重算／定时操作");
        role("USER","普通用户",List.of("tasks.read","tasks.materials","tasks.fill","tasks.audit","tasks.withdraw","tasks.history","tasks.export","performance.read","performance.materials","performance.fill","performance.audit","performance.withdraw","performance.history","performance.export","dashboard.read","audit.read","messages.read"),business("tasks","performance"),false);
        role("RESPONSIBLE","负责人",BUILTINS.get("USER").permissions(),business("tasks","performance"),false);
        role("LEADERSHIP","校级领导查阅",List.of("tasks.read","tasks.materials","tasks.history","tasks.export","performance.read","performance.materials","performance.history","performance.export","achievements.read","achievements.materials","achievements.history","core.read","records.read","records.export","budget.read","budget.materials","budget.export","budget.versions","dashboard.read","overview.read","messages.read"),merge(Map.of("tasks",Map.of("mode","LEADERSHIP"),"performance",Map.of("mode","LEADERSHIP")),all("achievements","core","records","budget")),true);
        role("OFFICE","双高办",List.of("core.read","core.fill","budget.read","budget.fill","budget.materials","budget.export","budget.versions"),all("core","budget"),true);
        role("FINANCE","财务",List.of("budget.read","budget.fill","budget.materials","budget.export","budget.versions"),all("budget"),true);
        role("BUDGET_VIEWER_INTERNAL","负责人预算查阅",List.of("budget.read","budget.materials","budget.export","budget.versions"),all("budget"),true);
        role("ACHIEVEMENT_REPORTER","成果报送",List.of("achievements.read","achievements.materials","achievements.fill","achievements.history","achievements.delete","achievements.export"),all("achievements"),true);
        role("ACHIEVEMENT_REVIEWER","成果审核",List.of("achievements.read","achievements.materials","achievements.audit","achievements.history","audit.read"),all("achievements"),true);
        role("RECORD_VIEWER","纪实查阅",List.of("records.read","records.export"),all("records"),true);
        role("RECORD_REPORTER","纪实填报资格",List.of("records.read","records.fill"),all("records"),true);
        role("CORE_REPORTER","核心指标填报资格",List.of("core.read","core.fill","core.history","core.withdraw"),all("core"),true);
        role("RECORD_OWNER","本人既有纪实",List.of("records.read"),Map.of("records",Map.of("mode","OWN")),true);
        role("CORE_REVIEWER","核心指标审核职责",List.of("core.read","core.audit","core.history","audit.read"),all("core"),true);
        role("EXPERT","专家",List.of("tasks.read","tasks.materials","tasks.history","expert.read","expert.write"),Map.of("tasks",Map.of("mode","ALL","years",List.of(2025,2026,2027,2028,2029))),false);
        role("ARCHIVER","双高办归档职责",List.of("tasks.read","tasks.materials","tasks.history","tasks.archive","performance.read","performance.materials","performance.history","performance.archive","core.read","core.history","core.archive","audit.read"),all("tasks","performance","core"),true);
        role("ADMIN","管理员",new ArrayList<>(ACTIONS.keySet()),all("tasks","performance","achievements","core","records","budget"),false);
    }
    private static void add(String module,String action,String label){ACTIONS.put(module+"."+action,label);}
    private static void role(String code,String name,List<String> permissions,Map<String,Object> scopes,boolean dynamic){BUILTINS.put(code,new Role(code,name,permissions,scopes,true,dynamic,true,0));}
    public static Map<String,Object> all(String...modules){Map<String,Object> result=new LinkedHashMap<>();for(String m:modules)result.put(m,Map.of("mode","ALL"));return result;}
    public static Map<String,Object> business(String...modules){Map<String,Object> result=new LinkedHashMap<>();for(String m:modules)result.put(m,Map.of("mode","BUSINESS"));return result;}
    private static Map<String,Object> merge(Map<String,Object>a,Map<String,Object>b){Map<String,Object> result=new LinkedHashMap<>(a);result.putAll(b);return result;}
}
