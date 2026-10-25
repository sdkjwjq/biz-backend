package org.example.service;

import org.example.entity.*;
import org.example.entity.vo.PerformanceYearVO;
import org.example.mapper.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** 首页、图表、分页明细共用同一只读数据集。禁止调用业务重算、草稿或写接口。 */
@Service
public class AdminOverviewService {
    @org.springframework.beans.factory.annotation.Autowired private PermissionService permissions;
    private static final List<String> MODULES=List.of("tasks","performance","core","achievements","budget","records");
    private static final Map<String,List<String>> DIMENSIONS=Map.of(
        "tasks",List.of("status","reform","department"), "performance",List.of("status","department","category"),
        "core",List.of("indicator"), "achievements",List.of("category","level","department","status"),
        "budget",List.of("reform","structure","trend"), "records",List.of("department","status"));
    private static final String[] CATEGORIES={"未分类","落实立德树人根本任务","创新产教融合机制","打造高水平专业群","建设一流核心课程","开放优质新形态教材","建设高水平双师队伍","建设产教融合实训基地","构建数字化教学新生态","拓展国际交流与合作","打造一流区域型高端智库"};
    private final LeadershipAccessService access;
    private final PerformanceMapper performance;
    private final AchievementMapper achievements;
    private final WorkRecordScopeService scopes;
    private final BudgetMapper budgets;
    private final BudgetTrendService trends;
    private final JdbcTemplate jdbc;

    public AdminOverviewService(LeadershipAccessService access, PerformanceMapper performance, AchievementMapper achievements,
                                WorkRecordScopeService scopes, BudgetMapper budgets, BudgetTrendService trends, JdbcTemplate jdbc) {
        this.access=access;this.performance=performance;this.achievements=achievements;this.scopes=scopes;
        this.budgets=budgets;this.trends=trends;this.jdbc=jdbc;
    }
    private record Data(String title,String period,String note,List<Map<String,Object>> metrics,
                        List<Map<String,Object>> rows,Map<String,Object> extra) { }
    private record Names(Map<String,String> departments,Map<String,String> users) { }
    private Names names() {
        if(org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() instanceof org.springframework.web.context.request.ServletRequestAttributes a
                && "GET".equals(a.getRequest().getMethod())){
            String key=AdminOverviewService.class.getName()+".names";
            if(a.getRequest().getAttribute(key) instanceof Names cached)return cached;
            Names result=loadNames();a.getRequest().setAttribute(key,result);return result;
        }
        return loadNames();
    }
    private Names loadNames() {
        Map<String,String> departments=new HashMap<>(),users=new HashMap<>();
        jdbc.queryForList("SELECT dept_id,dept_name FROM sys_dept").forEach(r->departments.put(str(r.get("dept_id")),str(r.get("dept_name"))));
        jdbc.queryForList("SELECT user_id,COALESCE(NULLIF(nick_name,''),user_name) name FROM sys_user").forEach(r->users.put(str(r.get("user_id")),str(r.get("name"))));
        return new Names(departments,users);
    }
    private void authorize(int year,int month,Long actor) {
        if(!access.isAdmin(actor)&&!access.isLeader(actor)&&!permissions.allowed(actor,"overview.read")) throw new AdminManagementException(403,"仅管理员和已配置的校级领导可查看概览");
        if(year<2025||year>2029||month<1||month>12) throw new AdminManagementException(400,"请选择2025—2029年度及有效月份");
    }
    @Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Map<String,Object> summary(int year,int month,String module,Long actor) {
        authorize(year,month,actor);
        if(module!=null&&!MODULES.contains(module)) throw new AdminManagementException(400,"未知统计模块");
        Map<String,Object> sections=new LinkedHashMap<>();
        boolean administrator=access.isAdmin(actor);
        // 各模块独立失败，绝不以零代替异常；重试可只请求一个模块。
        for(String key:module==null?MODULES:List.of(module)) {
            try {
                if(permissions.enforced(actor)&&!permissions.allowed(actor,key+".read")){sections.put(key,map("status","forbidden","message","未获准查阅此模块"));continue;}
                Data d=administrator&&key.equals("tasks")?taskSummary(year)
                        :administrator&&key.equals("achievements")?achievementSummary(year)
                        :administrator&&key.equals("performance")&&permissions.enforced(actor)?performanceSummary(year,actor):data(key,year,month,actor);
                sections.put(key,map("status","ok","title",d.title,"period",d.period,"note",d.note,"metrics",d.metrics,"extra",d.extra));
            } catch(Exception error) {
                org.slf4j.LoggerFactory.getLogger(getClass()).error("Overview module failed: {}",key,error);
                sections.put(key,map("status","error","message","统计加载失败，请重试"));
            }
        }
        return map("year",year,"month",month,"generatedAt",Instant.now().toString(),"readOnly",!administrator,
            "scope",access.scope(actor),"modules",sections);
    }
    @Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Map<String,Object> analysis(int year,int month,String metric,String dimension,String groupKey,int page,int pageSize,Long actor) {
        return analysis(year,month,metric,dimension,groupKey,page,pageSize,actor,new org.springframework.util.LinkedMultiValueMap<>());
    }
    @Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Map<String,Object> analysis(int year,int month,String metric,String dimension,String groupKey,int page,int pageSize,Long actor,org.springframework.util.MultiValueMap<String,String> filters) {
        authorize(year,month,actor);
        String[] parts=metric.split("\\.",-1);
        if(parts.length!=2||!MODULES.contains(parts[0])||page<1||pageSize<1||pageSize>100)
            throw new AdminManagementException(400,"统计指标或分页参数不正确");
        String module=parts[0],key=parts[1];
        String dim=dimension==null?DIMENSIONS.get(module).get(0):dimension;
        if(!DIMENSIONS.get(module).contains(dim)) throw new AdminManagementException(400,"此指标不支持所选维度");
        Data d=data(module,year,month,actor);
        Map<String,Object> selected=d.metrics.stream().filter(m->key.equals(m.get("key"))).findFirst()
                .orElseThrow(()->new AdminManagementException(400,"未知统计指标"));
        List<Map<String,Object>> rows=d.rows.stream().filter(matches(module,key)).toList();
        List<ListFilterQuery.Field> fields=new ArrayList<>();
        for(String field:List.of("department","category","status"))if(rows.stream().anyMatch(r->r.containsKey(field))){
            var options=rows.stream().filter(r->r.get(field)!=null).map(r->str(r.get(field))).distinct().sorted().map(v->Map.<String,Object>of("value",v,"label",v)).toList();
            fields.add(new ListFilterQuery.Field(field,field.equals("department")?"部门":field.equals("category")?"类别":"状态","multi",true,"基础",options));
        }
        var query=new ListFilterQuery(filters,fields);rows=rows.stream().filter(r->query.matches(r,fields)).toList();
        List<Map<String,Object>> chart;
        if(module.equals("budget")&&dim.equals("trend")) chart=trend(year,month);
        else if(module.equals("budget")&&dim.equals("structure")) chart=structure(rows);
        else if(module.equals("core")) chart=rows.stream().map(r->map("key",str(r.get("id")),"name",r.get("name"),"value",r.get("ratio"))).toList();
        else chart=groups(rows,dim,module.equals("achievements")&&key.equals("awards")?"quantity":module.equals("budget")?key.equals("annualPlan")?"target":key.equals("executionRate")?"ratio":"value":null);
        if(module.equals("records")&&dim.equals("department")) for(var group:chart) {
            List<Map<String,Object>> members=rows.stream().filter(r->Objects.equals(group.get("key"),r.get("departmentKey"))).toList();
            group.put("submitted",members.stream().filter(r->Boolean.TRUE.equals(r.get("submitted"))).count());
            group.put("missing",members.stream().filter(r->!Boolean.TRUE.equals(r.get("submitted"))).count());
        }
        // 非分类图表不能用图形值作为任意过滤条件。
        boolean groupable=!(module.equals("budget")&&(dim.equals("structure")||dim.equals("trend")));
        if(groupKey!=null&&!groupKey.isBlank()) {
            if(!groupable)throw new AdminManagementException(400,"此图表不支持分组筛选");
            String field=module.equals("core")?"id":dim+"Key";
            rows=rows.stream().filter(r->groupKey.equals(str(r.get(field)))).toList();
        }
        long total=rows.size();
        int safePage=Math.min(page,Math.max(1,(int)((total+pageSize-1)/pageSize)));
        int start=(safePage-1)*pageSize;
        return map("module",module,"metric",metric,"label",selected.get("label"),"value",selected.get("value"),"unit",selected.get("unit"),
            "filterApplied",query.active()||(groupKey!=null&&!groupKey.isBlank()),"fields",fields,"period",d.period,"note",d.note,"dimension",dim,"dimensions",DIMENSIONS.get(module),"chart",chart,"groupable",groupable,
            "chartUnit",module.equals("budget")&&dim.equals("reform")&&key.equals("executionRate")?"%":module.equals("budget")?"万元":module.equals("core")?"%":"数量",
            "total",total,"page",safePage,"pageSize",pageSize,"rows",rows.subList(start,Math.min(start+pageSize,rows.size())),"extra",d.extra);
    }
    private Data data(String module,int year,int month,Long actor) {
        if(permissions.enforced(actor)){permissions.require(actor,module+".read");if(module.equals("budget")&&!permissions.budgetScope(actor,year))throw new AdminManagementException(403,"预算年度不在授权范围内");}
        return switch(module) {
            case "tasks"->tasks(year,actor,names());case "performance"->performances(year,actor,names());case "core"->core(year,month,actor);
            case "achievements"->achievements(year,names(),actor);case "budget"->budget(year,month);case "records"->records(year,month,names(),actor);
            default->throw new AdminManagementException(400,"未知统计模块");
        };
    }
    private Data tasks(int year,Long actor,Names n) {
        List<BizTask> all=access.scopedTasks(actor);
        Map<Long,BizTask> index=all.stream().collect(Collectors.toMap(BizTask::getTaskId,t->t));
        List<Map<String,Object>> rows=new ArrayList<>();
        LocalDate today=LocalDate.now(ZoneId.of("Asia/Shanghai"));
        for(BizTask t:all) if(Integer.valueOf(3).equals(t.getLevel())&&Integer.valueOf(year).equals(t.getPhase())) {
            String state=switch(str(t.getStatus())){case "0"->"未开始";case "1"->"进行中";case "2"->"审核中";case "3"->"已完成";default->"状态待核实";};
            Map<String,Object> r=base(t.getTaskId(),t.getTaskCode(),t.getTaskName(),t.getDeptId(),n);
            r.putAll(map("kind","task","statusKey",state,"status",state,"progress",t.getProgress(),"priority",Boolean.TRUE.equals(t.getIsPriority()),
                "difficult",Boolean.TRUE.equals(t.getIsDifficult()),"overdue",!"3".equals(t.getStatus())&&today.isAfter(LocalDate.of(year,12,31)),"owner",n.users.getOrDefault(str(t.getLeaderId()),"未配置")));
            BizTask root=t;Set<Long> seen=new HashSet<>();
            while(root!=null&&!Integer.valueOf(1).equals(root.getLevel())&&seen.add(root.getTaskId())) root=index.get(root.getParentId());
            r.put("reformKey",root==null?"unknown":str(root.getTaskId()));r.put("reform",root==null?"目录待核实":root.getTaskName());rows.add(r);
        }
        Map<String,Long> counts=new LinkedHashMap<>();
        for(String key:List.of("total","completed","reviewing","priority","difficult","overdue"))counts.put(key,count(rows,"tasks",key));
        boolean valid=rows.stream().allMatch(r->r.get("progress")!=null);
        BigDecimal average=rows.isEmpty()||!valid?null:rows.stream().map(r->decimal(r.get("progress"))).reduce(BigDecimal.ZERO,BigDecimal::add).divide(BigDecimal.valueOf(rows.size()),2,RoundingMode.HALF_UP);
        return taskData(counts,average,rows);
    }
    /** Summary-only aggregation; administrator scope is fixed to all effective tasks. */
    private Data taskSummary(int year) {
        var counts=jdbc.queryForMap("SELECT COUNT(*) total,"
                +"COALESCE(SUM(CASE WHEN BINARY status=BINARY '3' THEN 1 ELSE 0 END),0) completed,"
                +"COALESCE(SUM(CASE WHEN BINARY status=BINARY '2' THEN 1 ELSE 0 END),0) reviewing,"
                +"COALESCE(SUM(CASE WHEN is_priority<>0 THEN 1 ELSE 0 END),0) priority,"
                +"COALESCE(SUM(CASE WHEN is_difficult<>0 THEN 1 ELSE 0 END),0) difficult,"
                +"COUNT(progress) progressCount,SUM(progress) progressSum "
                +"FROM biz_task WHERE level=3 AND phase=? AND (is_delete IS NULL OR is_delete<>1)",year);
        Map<String,Long> values=new LinkedHashMap<>();
        for(String key:List.of("total","completed","reviewing","priority","difficult"))values.put(key,((Number)counts.get(key)).longValue());
        long total=values.get("total");
        values.put("overdue",LocalDate.now(ZoneId.of("Asia/Shanghai")).isAfter(LocalDate.of(year,12,31))?total-values.get("completed"):0L);
        BigDecimal average=total==0||((Number)counts.get("progressCount")).longValue()!=total?null
                :decimal(counts.get("progressSum")).divide(BigDecimal.valueOf(total),2,RoundingMode.HALF_UP);
        return taskData(values,average,List.of());
    }
    private Data taskData(Map<String,Long> counts,BigDecimal average,List<Map<String,Object>> rows) {
        List<Map<String,Object>> metrics=new ArrayList<>();
        for(String key:List.of("total","completed","reviewing","priority","difficult","overdue"))
            metrics.add(metric(key,switch(key){case "total"->"总任务";case "completed"->"已完成";case "reviewing"->"审核中";case "priority"->"重点";case "difficult"->"困难";default->"逾期";},counts.get(key),"项"));
        metrics.add(metric("completionRate","完成率",rate(counts.get("completed"),counts.get("total")),"%"));
        metrics.add(metric("averageProgress","平均推进进度",average,"%"));
        return new Data("任务推进","年度当前状态","有效三级任务去重；完成率按完成任务数，平均推进进度按保存的三级进度计算。重点、困难、逾期可能重叠；逾期仍按所属年度年末判断。",metrics,rows,Map.of());
    }
    private Data performances(int year,Long actor,Names n) {
        Map<Long,BizPerformanceYear> annual=new HashMap<>();
        performance.getPerformanceYearByYear(year).stream().filter(y->!Integer.valueOf(1).equals(y.getIsDelete())).forEach(y->annual.put(y.getPerfId(),y));
        List<PerformanceYearVO> values=new ArrayList<>();
        for(BizPerformance p:performance.getAllPerformance()) {
            BizPerformanceYear y=annual.get(p.getPerfId());
            if(y==null){y=new BizPerformanceYear();y.setYear(year);}
            values.add(new PerformanceYearVO(p,y));
        }
        List<Map<String,Object>> rows=new ArrayList<>();
        var candidates=values.stream().filter(p->!Integer.valueOf(1).equals(p.getIsDelete())&&p.getAncestors()!=null&&Arrays.stream(p.getAncestors().split(",")).filter(s->!s.isBlank()).count()>=3).toList();
        Set<Long> visible=permissions.enforced(actor)?permissions.readablePerformanceIds(actor,candidates.stream().map(PerformanceYearVO::getPerfId).toList(),year):null;
        for(var p:candidates) {
            if(visible!=null){if(!visible.contains(p.getPerfId()))continue;}else if(access.restricted(actor)&&!access.scope(actor).contains(p.getDeptId()))continue;
            String state=p.getTargetValue()==null||p.getTargetValue().signum()<=0||p.getCurrentValue()==null?"数据待核实":p.getCurrentValue().compareTo(p.getTargetValue())>=0?"数值达标":"未达标";
            Map<String,Object> r=base(p.getPerfId(),p.getPerfCode(),p.getPerfName(),p.getDeptId(),n);
            String code=str(p.getPerfCode()),category=Arrays.stream(p.getAncestors().split(",")).filter(s->!s.isBlank()).toList().get(2).trim();
            String categoryName=Map.of("1.1","数量指标","1.2","质量指标","1.3","时效指标","2.1","社会效益指标","2.2","可持续影响指标","3.1","服务对象满意度指标").getOrDefault(category,category);
            r.putAll(map("kind","performance","statusKey",state,"status",state,"target",p.getTargetValue(),"value",p.getCurrentValue(),"categoryKey",category,"category",categoryName,
                "mode",code.startsWith("1.1.")||code.startsWith("1.2.")?"自动":"手动","owner",n.users.getOrDefault(str(p.getLeaderId()),"未配置")));rows.add(r);
        }
        Map<String,Long> counts=new LinkedHashMap<>();
        for(String key:List.of("total","attained","unattained","uncertain","manual","automatic"))counts.put(key,count(rows,"performance",key));
        return performanceData(counts,rows);
    }
    /** Administrator summary only. Unusual ancestry and ambiguous annual rows retain the original projection. */
    private Data performanceSummary(int year,Long actor) {
        var result=jdbc.queryForMap("WITH base AS (SELECT perf_id,perf_code,"
                +"TRIM(BOTH ',' FROM REGEXP_REPLACE(REPLACE(ancestors,' ',''),',+',',')) path,"
                +"REGEXP_LIKE(ancestors,'[^0-9., ]','c') unusual FROM biz_performance WHERE COALESCE(is_delete,0)=0),"
                +"annual AS (SELECT perf_id,COUNT(*) annual_rows,MIN(target_value) target_value,MIN(actual_value) actual_value "
                +"FROM biz_performance_year WHERE year=? AND (is_delete IS NULL OR is_delete<>1) GROUP BY perf_id),"
                +"items AS (SELECT b.perf_code,y.annual_rows,y.target_value,y.actual_value FROM base b LEFT JOIN annual y ON y.perf_id=b.perf_id "
                +"WHERE LENGTH(b.path)-LENGTH(REPLACE(b.path,',',''))>=2) "
                +"SELECT COUNT(*) total,"
                +"COALESCE(SUM(CASE WHEN target_value>0 AND actual_value>=target_value THEN 1 ELSE 0 END),0) attained,"
                +"COALESCE(SUM(CASE WHEN target_value>0 AND actual_value<target_value THEN 1 ELSE 0 END),0) unattained,"
                +"COALESCE(SUM(CASE WHEN target_value IS NULL OR target_value<=0 OR actual_value IS NULL THEN 1 ELSE 0 END),0) uncertain,"
                +"COALESCE(SUM(CASE WHEN BINARY perf_code LIKE BINARY '1.1.%' OR BINARY perf_code LIKE BINARY '1.2.%' THEN 1 ELSE 0 END),0) automatic,"
                +"COALESCE(MAX(annual_rows),0) maxAnnualRows,(SELECT COUNT(*) FROM base WHERE unusual) unusualPaths FROM items",year);
        if(((Number)result.get("maxAnnualRows")).longValue()>1||((Number)result.get("unusualPaths")).longValue()>0)
            return performances(year,actor,names());
        Map<String,Long> counts=new LinkedHashMap<>();
        for(String key:List.of("total","attained","unattained","uncertain","automatic"))counts.put(key,((Number)result.get(key)).longValue());
        counts.put("manual",counts.get("total")-counts.get("automatic"));
        return performanceData(counts,List.of());
    }
    private Data performanceData(Map<String,Long> counts,List<Map<String,Object>> rows) {
        List<Map<String,Object>> metrics=new ArrayList<>();
        String[] keys={"total","attained","unattained","uncertain","manual","automatic"},labels={"实际指标","数值达标","未达标","数据待核实","手动指标","自动指标"};
        for(int i=0;i<keys.length;i++)metrics.add(metric(keys[i],labels[i],counts.get(keys[i]),"项"));
        return new Data("绩效达成","年度当前状态","排除分类目录，使用既有年度目标值及年度完成值判断数值达标；缺少有效目标或完成值列入待核实，不相加不同单位的完成值。",metrics,rows,Map.of());
    }
    private Data core(int year,int month,Long actor) {
        // 整批读取映射及有效归档；与 CoreIndicatorService.effective 使用相同映射、月份和排序条件。
        List<Map<String,Object>> mappings=jdbc.queryForList("SELECT c.indicator_id id,c.indicator_code code,c.sort_order,p.perf_id,p.perf_name,p.target_value FROM biz_core_indicator c LEFT JOIN biz_performance p ON BINARY p.perf_code=BINARY c.indicator_code AND COALESCE(p.is_delete,0)=0 ORDER BY c.sort_order,p.perf_id");
        List<Map<String,Object>> archived=jdbc.queryForList("SELECT * FROM (SELECT r.*,ROW_NUMBER() OVER(PARTITION BY r.indicator_id ORDER BY r.record_month DESC,r.archive_time DESC,r.report_id DESC) rn FROM biz_core_report r JOIN biz_core_indicator c ON c.indicator_id=r.indicator_id AND BINARY c.indicator_code=BINARY r.performance_code WHERE r.flow_status=30 AND r.record_year=? AND r.record_month<=?) x WHERE rn=1",year,month);
        Map<String,Map<String,Object>> effective=new HashMap<>();archived.forEach(r->effective.put(str(r.get("indicator_id")),r));
        Map<String,List<Map<String,Object>>> distinct=mappings.stream().collect(Collectors.groupingBy(m->str(m.get("id")),LinkedHashMap::new,Collectors.toList()));
        List<Map<String,Object>> rows=new ArrayList<>();long invalid=0;
        for(var group:distinct.values()) {
            var m=group.get(0);if(permissions.enforced(actor)&&!permissions.canRead(actor,"core",((Number)m.get("id")).longValue(),year))continue;String id=str(m.get("id"));var e=effective.get(id);
            boolean valid=group.size()==1&&m.get("perf_id")!=null&&(!id.equals("10")||"发布《工程机械产业发展报告》数量".equals(m.get("perf_name")));
            if(!valid)invalid++;
            Object target=e==null?m.get("target_value"):e.get("target_value"),value=e==null?null:e.get("actual_value");
            boolean usable=valid&&e!=null;BigDecimal ratio=usable?ratio(value,target):null;
            rows.add(map("id",m.get("id"),"code",m.get("code"),"name",Objects.toString(usable?e.get("indicator_name"):m.get("perf_name"),"映射待核实"),"kind","core","target",target,"value",usable?value:null,
                "month",usable?e.get("record_month"):null,"reportId",usable?e.get("report_id"):null,"ratio",ratio,
                "status",!valid?"映射异常":usable?"已有有效归档":"尚未归档","archived",usable,"attained",usable&&decimal(value)!=null&&decimal(target)!=null&&decimal(target).signum()>0&&decimal(value).compareTo(decimal(target))>=0));
        }
        return new Data("核心指标","截至所选月份","仅取同年度、不晚于所选月份的最近有效归档；使用归档时名称、目标及完成值快照，尚未归档留空。",List.of(
            metric("total","指标总数",rows.size(),"项"),
            metric("archived","已有有效归档",count(rows,"core","archived"),"项"),metric("unarchived","尚未归档",count(rows,"core","unarchived"),"项"),
            metric("attained","达到期满目标",count(rows,"core","attained"),"项"),metric("invalid","映射异常",invalid,"项")),rows,
            map("warning",distinct.size()!=10?"核心指标配置应为十项，请核实映射":invalid>0?"存在缺失、重复或名称不符的映射，请联系管理员":""));
    }
    private Data achievements(int year,Names n,Long actor) {
        List<Map<String,Object>> rows=new ArrayList<>();long missing=0;
        var all=achievements.listAllAchievements();
        Set<Long> visible=permissions.enforced(actor)?permissions.readableAchievementIds(actor,all.stream().map(BizAchievement::getAchId).toList()):null;
        for(var a:all) {
            if(visible!=null&&!visible.contains(a.getAchId()))continue;
            if(a.getGotTime()==null){missing++;continue;}
            if(a.getGotTime().toInstant().atZone(ZoneId.of("Asia/Shanghai")).getYear()!=year)continue;
            String state=a.getAuditStatus()==null?"状态待核实":a.getAuditStatus()==30?"已归档":a.getAuditStatus()<0?"已驳回":a.getAuditStatus()==10?"待审核":"状态待核实";
            long quantity=Arrays.asList(a.getTeDengJiang(),a.getYiDengJiang(),a.getErDengJiang(),a.getSanDengJiang(),a.getJinJiang(),a.getYinJiang(),a.getTongJiang(),a.getYouShengJiang(),a.getBudDengDengCi()).stream().filter(Objects::nonNull).mapToLong(Integer::longValue).sum();
            int category=a.getCategory()==null||a.getCategory()<1||a.getCategory()>10?0:a.getCategory();
            Map<String,Object> r=base(a.getAchId(),"",a.getAchName(),a.getDeptId(),n);
            r.putAll(map("kind","achievement","categoryKey",str(category),"category",CATEGORIES[category],"levelKey",Objects.toString(a.getLevel(),"未填写"),"level",Objects.toString(a.getLevel(),"未填写"),"statusKey",state,"status",state,"quantity",quantity,"reason",a.getRejectionReason()));rows.add(r);
        }
        Map<String,Long> counts=new LinkedHashMap<>();
        for(String key:List.of("total","archived","pending","rejected"))counts.put(key,count(rows,"achievements",key));
        counts.put("awards",rows.stream().filter(matches("achievements","archived")).mapToLong(r->((Number)r.get("quantity")).longValue()).sum());
        return achievementData(counts,missing,rows);
    }
    /** Summary reads no attachment, text body or per-record audit history. */
    private Data achievementSummary(int year) {
        var result=jdbc.queryForMap("SELECT COALESCE(SUM(in_year),0) total,"
                +"COALESCE(SUM(CASE WHEN in_year AND audit_status=30 THEN 1 ELSE 0 END),0) archived,"
                +"COALESCE(SUM(CASE WHEN in_year AND audit_status=10 THEN 1 ELSE 0 END),0) pending,"
                +"COALESCE(SUM(CASE WHEN in_year AND audit_status<0 THEN 1 ELSE 0 END),0) rejected,"
                +"COALESCE(SUM(CASE WHEN in_year AND audit_status=30 THEN quantity ELSE 0 END),0) awards,"
                +"COALESCE(SUM(got_time IS NULL),0) missingDates FROM ("
                +"SELECT got_time,audit_status,(got_time>=? AND got_time<?) in_year,"
                +"COALESCE(te_deng_jiang,0)+COALESCE(yi_deng_jiang,0)+COALESCE(er_deng_jiang,0)"
                +"+COALESCE(san_deng_jiang,0)+COALESCE(jin_jiang,0)+COALESCE(yin_jiang,0)"
                +"+COALESCE(tong_jiang,0)+COALESCE(you_sheng_jiang,0)+COALESCE(bud_deng_deng_ci,0) quantity "
                +"FROM biz_achievement WHERE is_delete=0) a",year+"-01-01",(year+1)+"-01-01");
        Map<String,Long> counts=new LinkedHashMap<>();
        for(String key:List.of("total","archived","pending","rejected","awards"))counts.put(key,((Number)result.get(key)).longValue());
        return achievementData(counts,((Number)result.get("missingDates")).longValue(),List.of());
    }
    private Data achievementData(Map<String,Long> counts,long missing,List<Map<String,Object>> rows) {
        List<Map<String,Object>> metrics=new ArrayList<>();String[] keys={"total","archived","pending","rejected"},labels={"成果条数","已归档","待审核","已驳回"};
        for(int i=0;i<keys.length;i++)metrics.add(metric(keys[i],labels[i],counts.get(keys[i]),"条"));
        metrics.add(metric("awards","已归档奖项数量",counts.get("awards"),"个"));
        return new Data("建设成果","取得年份／审核为当前状态","按取得日期归入年度；成果条数和奖项数量分别统计。驳回记录保留当前驳回原因。",metrics,rows,map("missingDates",missing,"warning",missing>0?"另有 "+missing+" 条成果缺少取得日期，未计入任何年度":""));
    }
    @SuppressWarnings("unchecked")
    private List<Map<String,Object>> trend(int year,int month) {
        return ((List<Map<String,Object>>)trends.trends(year,0).get("points")).stream().filter(r->((Number)r.get("month")).intValue()<=month).toList();
    }
    private Data budget(int year,int month) {
        var sheet=budgets.getSheet(year,month);List<Map<String,Object>> rows=new ArrayList<>();
        Map<String,Object> point=sheet==null?Map.of():trend(year,month).get(month-1);
        BigDecimal annual=null;
        if(sheet!=null) {
            var sources=budgets.getSources(sheet.getSheetId());
            if(!sources.isEmpty()&&sources.stream().allMatch(s->s.getAnnualPlan()!=null))annual=sources.stream().map(BizBudgetSourceItem::getAnnualPlan).reduce(BigDecimal.ZERO,BigDecimal::add);
            for(var t:budgets.getTasks(sheet.getSheetId())) if(t.getTaskIndex()!=null&&t.getTaskIndex()>=1&&t.getTaskIndex()<=10) {
                BigDecimal expense=t.getGoods()==null||t.getCapital()==null||t.getOther()==null?null:t.getGoods().add(t.getCapital()).add(t.getOther());
                rows.add(map("id",t.getTaskIndex(),"name",t.getTaskName(),"reformKey",str(t.getTaskIndex()),"reform",t.getTaskName(),"kind","budget","target",t.getAmount(),"value",expense,"ratio",ratio(expense,t.getAmount()),"goods",t.getGoods(),"capital",t.getCapital(),"other",t.getOther()));
            }
        }
        return new Data("预算执行","所选月份","金额单位：万元。只读所选年月当前有效版本；年度预算安排按资金来源汇总，改革任务对比使用各任务分项预算。累计支出为商品和服务、资本性及其他支出之和。缺月留空、不跨月相减，负增长可能为财务调账。",List.of(
            metric("annualPlan","年度预算安排",annual,"万元"),metric("expense","累计支出",point.get("cumulative"),"万元"),metric("executionRate","执行率",point.get("executionRate"),"%")),rows,
            map("missing",sheet==null,"dataMonth",sheet==null?null:month,"importTime",sheet==null?null:sheet.getImportTime(),"warning",sheet==null?"尚未导入":point.get("cumulative")==null?"预算字段缺失，支出待核实":""));
    }
    private Data records(int year,int month,Names n,Long actor) {
        List<SysUser> eligible=scopes.eligibleAuthors(year).stream().filter(u->!permissions.enforced(actor)||permissions.canRecordOwner(actor,u.getUserId(),u.getDeptId(),year)).toList();
        List<Map<String,Object>> submitted=jdbc.queryForList("SELECT record_id,owner_id,owner_name,submit_time FROM biz_work_record WHERE record_year=? AND record_month=? AND status=1 AND delete_marker=0 ORDER BY owner_id,record_id",year,month).stream().filter(r->!permissions.enforced(actor)||permissions.canRead(actor,"records",((Number)r.get("record_id")).longValue(),year)).toList();
        Map<String,Map<String,Object>> byOwner=new LinkedHashMap<>();submitted.forEach(r->byOwner.put(str(r.get("owner_id")),r));
        Set<String> ids=eligible.stream().map(u->str(u.getUserId())).collect(Collectors.toSet());List<Map<String,Object>> rows=new ArrayList<>();
        for(SysUser u:eligible) {
            var s=byOwner.get(str(u.getUserId()));Map<String,Object> r=base(u.getUserId(),str(u.getUserId()),u.getNickName(),u.getDeptId(),n);
            r.putAll(map("kind","record","recordId",s==null?null:s.get("record_id"),"submitTime",s==null?null:s.get("submit_time"),"eligible",true,"submitted",s!=null,"statusKey",s==null?"未提交":"已提交","status",s==null?"未提交":"已提交"));rows.add(r);
        }
        // 保留已失去资格人员的已提交记录入口，但不混入提交率分子。
        for(var s:submitted) if(!ids.contains(str(s.get("owner_id"))))rows.add(map("id",s.get("owner_id"),"code",str(s.get("owner_id")),"name",s.get("owner_name"),"kind","record","recordId",s.get("record_id"),"eligible",false,"submitted",true,"status","原资格已提交","statusKey","原资格已提交","departmentKey","historical","department","原资格记录","submitTime",s.get("submit_time")));
        long done=rows.stream().filter(matches("records","submitted")).count();
        return new Data("工作纪实","所选月份／当前填报资格","应填、已提交、未提交及提交率使用所选年度当前有资格的同一人员集合。原资格已提交记录仍可查阅；不读取或展示他人草稿。部门按人员当前所属部门统计。",List.of(
            metric("expected","当前应填",eligible.size(),"人"),metric("submitted","已提交",done,"人"),metric("missing","未提交",eligible.size()-done,"人"),metric("submissionRate","提交率",rate(done,eligible.size()),"%"),
            metric("historical","原资格已提交",rows.stream().filter(r->Boolean.FALSE.equals(r.get("eligible"))).count(),"人")),rows,Map.of());
    }
    private Predicate<Map<String,Object>> matches(String module,String key) {
        return r->switch(module+"."+key) {
            case "tasks.completed"->"已完成".equals(r.get("status"));case "tasks.reviewing"->"审核中".equals(r.get("status"));
            case "tasks.priority","tasks.difficult","tasks.overdue"->Boolean.TRUE.equals(r.get(key));
            case "performance.attained"->"数值达标".equals(r.get("status"));case "performance.unattained"->"未达标".equals(r.get("status"));case "performance.uncertain"->"数据待核实".equals(r.get("status"));
            case "performance.manual"->"手动".equals(r.get("mode"));case "performance.automatic"->"自动".equals(r.get("mode"));
            case "core.archived"->Boolean.TRUE.equals(r.get("archived"));case "core.unarchived"->"尚未归档".equals(r.get("status"));case "core.attained"->Boolean.TRUE.equals(r.get("attained"));case "core.invalid"->"映射异常".equals(r.get("status"));
            case "achievements.archived","achievements.awards"->"已归档".equals(r.get("status"));case "achievements.pending"->"待审核".equals(r.get("status"));case "achievements.rejected"->"已驳回".equals(r.get("status"));
            case "records.expected","records.submissionRate"->Boolean.TRUE.equals(r.get("eligible"));case "records.submitted"->Boolean.TRUE.equals(r.get("eligible"))&&Boolean.TRUE.equals(r.get("submitted"));case "records.missing"->Boolean.TRUE.equals(r.get("eligible"))&&!Boolean.TRUE.equals(r.get("submitted"));case "records.historical"->Boolean.FALSE.equals(r.get("eligible"));
            default->true;
        };
    }
    private long count(List<Map<String,Object>> rows,String module,String key){return rows.stream().filter(matches(module,key)).count();}
    private Map<String,Object> base(Object id,String code,String name,Long dept,Names n){return map("id",id,"code",code,"name",name,"departmentKey",dept==null?"unknown":str(dept),"department",n.departments.getOrDefault(str(dept),"未配置部门"));}
    private List<Map<String,Object>> groups(List<Map<String,Object>> rows,String dim,String weight) {
        Map<String,Map<String,Object>> grouped=new LinkedHashMap<>();
        for(var r:rows) {
            String key=str(r.get(dim+"Key"));Map<String,Object> g=grouped.computeIfAbsent(key,k->map("key",k,"name",r.get(dim),"value",BigDecimal.ZERO,"incomplete",false));
            BigDecimal value=weight==null?BigDecimal.ONE:decimal(r.get(weight));
            if(value==null)g.put("incomplete",true);else g.put("value",((BigDecimal)g.get("value")).add(value));
        }
        grouped.values().forEach(g->{if(Boolean.TRUE.equals(g.get("incomplete")))g.put("value",null);});
        return new ArrayList<>(grouped.values());
    }
    private List<Map<String,Object>> structure(List<Map<String,Object>> rows) {
        List<Map<String,Object>> result=new ArrayList<>();String[] keys={"goods","capital","other"},labels={"商品和服务支出","资本性支出","其他支出"};
        for(int i=0;i<keys.length;i++){String k=keys[i];result.add(map("key",k,"name",labels[i],"value",rows.isEmpty()||rows.stream().anyMatch(r->r.get(k)==null)?null:rows.stream().map(r->decimal(r.get(k))).reduce(BigDecimal.ZERO,BigDecimal::add)));}return result;
    }
    private static Map<String,Object> metric(String key,String label,Object value,String unit){return map("key",key,"label",label,"value",value,"unit",unit);}
    private static BigDecimal rate(long count,long total){return total==0?null:BigDecimal.valueOf(count*100).divide(BigDecimal.valueOf(total),2,RoundingMode.HALF_UP);}
    private static BigDecimal ratio(Object value,Object target){BigDecimal v=decimal(value),t=decimal(target);return v==null||t==null||t.signum()<=0?null:v.multiply(BigDecimal.valueOf(100)).divide(t,2,RoundingMode.HALF_UP);}
    private static BigDecimal decimal(Object value){return value==null?null:new BigDecimal(value.toString());}
    private static String str(Object value){return value==null?"":value.toString();}
    private static Map<String,Object> map(Object...pairs){Map<String,Object> m=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)m.put((String)pairs[i],pairs[i+1]);return m;}
}
