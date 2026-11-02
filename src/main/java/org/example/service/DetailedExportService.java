package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.util.*;
import java.io.IOException;
import java.math.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/** Read-only analysis exports. Permission first, list predicates second, related rows last. */
@Service
public class DetailedExportService {
    public static final Map<String,String> TITLES=Map.ofEntries(
        Map.entry("tasks","建设任务"),Map.entry("performance","绩效"),Map.entry("achievements","建设成果"),Map.entry("core","核心指标"),
        Map.entry("records","工作纪实"),Map.entry("budget","预算"),Map.entry("budget-versions","预算版本"),Map.entry("notices","本人消息"),
        Map.entry("audits","审核记录"),Map.entry("supervision","督办任务"),Map.entry("admin-users","账号"),Map.entry("admin-departments","部门"),
        Map.entry("admin-roles","角色权限"),Map.entry("admin-operations","管理操作"),Map.entry("platform-notices","平台通知"),Map.entry("review-tasks","评审任务"));
    private static final String PEOPLE="deptId:归口部门ID|deptName:归口部门|leaderId:专业群负责人账号|leaderName:专业群负责人|auditorId:专业群审核人账号|auditorName:专业群审核人|principalId:归口审核人账号|principalName:归口审核人";
    private static final String TIMES="createTime:创建时间|updateTime:更新时间";
    private static final String TASK="taskId:任务ID|parentId:上级任务ID|level:任务层级|taskCode:任务编号|taskName:任务名称|phase:建设年度|rootName:一级改革任务|parentName:上级目录|"+PEOPLE+"|targetValue:目标值|currentValue:完成值|progress:计入进度(%)|status:状态代码|statusText:任务状态|dataType:数据类型代码|dataTypeText:数据类型|isPriority:重点任务|isDifficult:困难任务|plannedCompletionDate:计划完成日期|actualCompletionDate:实际完成日期|expTarget:预期达成目标|expLevel:预期成果级别|expEffect:预期成效|expMaterialDesc:预期材料清单|comment:完成情况或说明|"+TIMES;
    private static final String SUB="subId:填报单ID|taskId:任务ID|taskCode:任务编号|taskName:任务名称|perfId:绩效ID|perfCode:绩效编号|perfName:绩效名称|achId:成果ID|achName:成果名称|year:填报年度|reportedValue:任务填报值|actualValue:绩效填报值|submitBy:提交人账号|submitName:提交人|submitDeptId:提交部门ID|submitDeptName:提交部门|submitTime:提交时间|flowStatus:流程状态代码|flowStatusText:流程状态|currentHandlerId:当前处理人账号|currentHandlerName:当前处理人|comment:填报说明";
    private static final String LOG="logId:审核日志ID|subId:填报单ID|reportId:核心填报ID|taskId:任务ID|taskCode:任务编号|perfId:绩效ID|perfCode:绩效编号|achId:成果ID|achName:成果名称|operatorId:操作人账号|operatorName:操作人|actionType:操作代码|action:操作|preStatus:原状态代码|postStatus:后状态代码|comment:审核意见|createTime:操作时间";
    private final BusinessListFilterService filters;private final PermissionService access;private final JdbcTemplate db;
    private final ObjectMapper json;private final BudgetService budget;private final AuditListPageService audits;
    public DetailedExportService(BusinessListFilterService filters,PermissionService access,JdbcTemplate db,ObjectMapper json,BudgetService budget,AuditListPageService audits){this.filters=filters;this.access=access;this.db=db;this.json=json;this.budget=budget;this.audits=audits;}

    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public void export(String domain,Long actor,MultiValueMap<String,String> input,HttpServletResponse response)throws IOException{
        export(domain,actor,input,null,response);
    }
    /** Legacy selected-task endpoint keeps its original empty-selection means all-visible contract. */
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public void selectedTasks(Long actor,List<Long> ids,HttpServletResponse response)throws IOException{
        export("tasks",actor,new LinkedMultiValueMap<>(),ids==null||ids.isEmpty()?null:new LinkedHashSet<>(ids),response);
    }
    private void authorize(String domain,Long actor){
        if(!TITLES.containsKey(domain))throw new AdminManagementException(400,"不支持的导出模块");
        if(domain.startsWith("admin-")||Set.of("platform-notices","review-tasks").contains(domain)){access.requireAdmin(actor);return;}
        String module=switch(domain){case "supervision"->"tasks";case "budget-versions"->"budget";case "notices"->"messages";default->domain;};
        if(domain.equals("audits")){if(access.state(actor).permissions().stream().noneMatch(p->List.of("tasks","performance","achievements","core").stream().anyMatch(d->p.equals(d+".audit")||p.equals(d+".archive")||p.equals(d+".history"))))throw new AdminManagementException(403,"没有审核记录查阅权限");return;}
        access.require(actor,module+".read");if(!domain.equals("notices"))access.require(actor,module+".export");
        if(domain.equals("budget-versions"))access.require(actor,"budget.versions");
    }
    private void export(String domain,Long actor,MultiValueMap<String,String> input,Set<Long> selected,HttpServletResponse response)throws IOException{
        authorize(domain,actor);
        String scope=Optional.ofNullable(input.getFirst("scope")).orElse("all");
        if(!Set.of("all","filtered").contains(scope))throw new AdminManagementException(400,"导出范围不正确");
        var params=new LinkedMultiValueMap<String,String>();
        if(scope.equals("filtered"))input.forEach((k,v)->{if(k.startsWith("f.")||Set.of("section","tab").contains(k))params.put(k,new ArrayList<>(v));});
        // Todo and history have different visibility rules; preserve the chosen tab in both modes.
        if(input.containsKey("tab"))params.put("tab",List.of(input.getFirst("tab")));
        Names names=new Names();
        try(var book=new AnalysisWorkbook()){
            if(domain.equals("budget"))budgets(book,actor,params,names);
            else if(domain.equals("platform-notices"))platformNotices(book,names);
            else if(domain.equals("review-tasks"))reviewTasks(book,names);
            else {
                List<Map<String,Object>> matched=new ArrayList<>(),catalog=new ArrayList<>();
                if(domain.equals("audits"))matched.addAll(audits.exportRows(actor,params));
                else if(domain.equals("core")&&scope.equals("all")){
                    for(int year=2025;year<=2029;year++){var annual=new LinkedMultiValueMap<String,String>();annual.set("f.year",String.valueOf(year));annual.set("f.month","12");var source=filters.exportProjection(domain,actor,annual);matched.addAll(source.rows());}
                }else {var source=filters.exportProjection(domain,actor,params);matched.addAll(source.rows());catalog.addAll(source.catalog());}
                if(selected!=null)matched.removeIf(r->!selected.contains(id(r.get("id"))));
                var rows=matched.stream().map(this::flatten).map(names::enrich).toList();
                switch(domain){
                    case "tasks","supervision" -> tasks(book,actor,rows,catalog,names);
                    case "performance" -> performances(book,actor,rows,names,params);
                    case "achievements" -> achievements(book,actor,rows,names);
                    case "core" -> core(book,actor,rows,names,params);
                    case "records" -> records(book,actor,rows,names);
                    case "notices" -> book.sheet("本人消息","noticeId:消息ID|fromUserId:发送人账号|fromUserName:发送人|toUserId:接收人账号|toUserName:接收人|type:消息类型|triggerEvent:触发事件|title:标题|content:内容|sourceType:业务来源类型|sourceId:关联业务ID|isRead:已读标记|createTime:发送时间",rows);
                    case "audits" -> book.sheet("审核明细","type:业务类型|keyword:关联编号及名称|"+SUB+"|handledTime:最近处理时间|reportId:核心填报ID|indicatorId:核心指标ID|indicatorName:核心指标名称",rows);
                    case "admin-users" -> book.sheet("账号明细","userId:工号或账号ID|userName:账号名称|nickName:姓名|email:邮箱|deptId:部门ID|deptName:部门|role:原账号角色|status:启停状态代码|forcePasswordChange:强制改密标记|roles:角色模板|leadership:领导查阅范围|expert:专家身份|personal:个人授权|duties:活动职责|"+TIMES,rows);
                    case "admin-departments" -> book.sheet("部门明细","deptId:部门ID|deptCode:部门编号|deptName:部门名称|leaderId:负责人账号|leaderName:负责人|status:状态代码|activeUsers:正常账号数|activeTasks:有效任务数|activePerformances:有效绩效数|taskLeaderMismatches:任务审核人不一致数|performanceLeaderMismatches:绩效审核人不一致数|"+TIMES,rows);
                    case "admin-roles" -> book.sheet("角色权限","code:角色代码|name:角色名称|enabled:启用|builtin:内置角色|dynamic:动态身份|assigned:已分配|permissions:功能权限|scopes:数据范围|version:版本",rows);
                    case "admin-operations" -> book.sheet("管理操作","recordType:记录来源|recordId:记录ID|module:模块|action:动作|targetType:对象类型|targetId:对象ID|targetName:对象名称|batchId:批次号|operatorId:操作人账号|operatorName:操作人|reason:原因|beforeJson:变更前|afterJson:变更后|createTime:操作时间",rows);
                    case "budget-versions" -> book.sheet("预算版本","versionId:版本ID|sheetId:预算表ID|year:年度|month:月份|section:预算分区|versionNo:版本序号|operationType:版本来源|sourceVersionId:恢复来源版本ID|fileName:原文件名|createdBy:导入人账号|createdByName:导入人|createTime:导入时间|remark:备注|isCurrent:当前有效版本",rows);
                    default -> throw new AdminManagementException(400,"不支持的导出模块");
                }
            }
            book.sheet("导出说明","item:项目|value:说明",List.of(Map.of("item","模块","value",TITLES.get(domain)),Map.of("item","范围","value",scope.equals("all")?"当前账号获准查看的全部记录，跨年度、跨分页；仍执行各模块权限及草稿隐私规则":"当前已生效筛选条件下的全部命中记录，不限当前页"),Map.of("item","时间","value",LocalDateTime.now(ZoneId.of("Asia/Shanghai")).toString()),Map.of("item","内容","value","各明细表通过ID及编号关联；不包含专家私有评语、密码、Token或附件正文。空值表示未填写或未设置，不代表零。")));
            String file=TITLES.get(domain)+(scope.equals("all")?"-全量明细-":"-筛选明细-")+LocalDate.now()+".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");response.setHeader("Cache-Control","no-store");
            response.setHeader("Content-Disposition","attachment; filename*=UTF-8''"+URLEncoder.encode(file,StandardCharsets.UTF_8).replace("+","%20"));
            book.write(response.getOutputStream());
        }
    }
    private void tasks(AnalysisWorkbook book,Long actor,List<Map<String,Object>> leaves,List<Map<String,Object>> catalog,Names names){
        var all=catalog.stream().collect(Collectors.toMap(r->id(r.get("taskId")),r->r,(a,b)->a,LinkedHashMap::new));
        Set<Long> wanted=new HashSet<>();for(var leaf:leaves){long key=id(leaf.get("taskId"));Set<Long> seen=new HashSet<>();while(key>0&&seen.add(key)){wanted.add(key);var row=all.get(key);key=row==null?0:id(row.get("parentId"));}}
        var rows=new ArrayList<Map<String,Object>>();for(var raw:catalog)if(wanted.contains(id(raw.get("taskId")))){var row=names.enrich(raw);var parent=all.get(id(row.get("parentId")));row.put("parentName",parent==null?null:parent.get("taskName"));var current=row;Set<Long> seen=new HashSet<>();while(current!=null&&seen.add(id(current.get("taskId")))){if(id(current.get("level"))==1){row.put("rootName",current.get("taskName"));break;}current=all.get(id(current.get("parentId")));}rows.add(row);}
        book.sheet("任务明细",TASK,rows);var ids=ids(leaves,"taskId");
        var children=byIds("SELECT * FROM biz_level4_task WHERE COALESCE(is_delete,0)=0 AND parent_id IN (%s) ORDER BY parent_id,task_id",ids);
        for(var child:children){var target=decimal(child.get("targetValue"));var value=decimal(child.get("currentValue"));var actual=target==null||target.signum()<=0||value==null?null:value.multiply(BigDecimal.valueOf(100)).divide(target,8,RoundingMode.HALF_UP);child.put("actualProgress",actual);child.put("countedProgress",actual==null?null:actual.min(BigDecimal.valueOf(100)));}
        book.sheet("四级目标","taskId:四级目标ID|parentId:三级任务ID|parentCode:三级任务编号|parentName:三级任务名称|phase:年度|taskName:四级目标名称|leaderId:负责人账号|leaderName:负责人|deptName:归口部门|targetValue:目标值|currentValue:原始填报值|actualProgress:实际完成率(%)|countedProgress:计入完成率(%)|progress:保存进度(%)|status:状态代码|"+TIMES,decorate(children,names,all,"parentId","parent"));
        var submissionIds=new ArrayList<>(ids);for(var child:children){long key=id(child.get("taskId"));submissionIds.add(key);all.put(key,child);}
        submissions(book,actor,"tasks","biz_material_submission","biz_audit_log","taskId",submissionIds,names,all);
    }
    private void submissions(AnalysisWorkbook book,Long actor,String module,String table,String logTable,String objectKey,List<Long> ids,Names names,Map<Long,Map<String,Object>> objects){
        if(access.allowed(actor,module+".history")){
        String fk=snake(objectKey);var rows=byIds("SELECT * FROM "+table+" WHERE COALESCE(is_delete,0)=0 AND "+fk+" IN (%s) ORDER BY "+fk+",sub_id",ids);
        rows=decorate(rows,names,objects,objectKey,module.equals("tasks")?"task":module.equals("performance")?"perf":"ach");
        for(var row:rows)row.put("flowStatusText",flow(module,id(row.get("flowStatus"))));
        book.sheet("填报记录",SUB,rows);
        var logs=byIds("SELECT * FROM "+logTable+" WHERE sub_id IN (%s) ORDER BY sub_id,log_id",ids(rows,"subId"));
        var bySub=rows.stream().collect(Collectors.toMap(r->id(r.get("subId")),r->r));
        for(var log:logs){var sub=bySub.get(id(log.get("subId")));if(sub!=null)for(String key:List.of("taskId","taskCode","perfId","perfCode","achId","achName"))log.put(key,sub.get(key));}
        book.sheet("审核记录",LOG,logs.stream().map(names::enrich).toList());
        }
        if(module.equals("tasks")&&access.allowed(actor,"tasks.materials")){
            var files=byIds("SELECT s.sub_id,s.task_id,s.file_id,f.file_name,f.file_suffix,f.file_size,s.submit_time FROM biz_material_submission s LEFT JOIN sys_file f ON f.file_id=s.file_id AND COALESCE(f.is_delete,0)=0 WHERE COALESCE(s.is_delete,0)=0 AND s.task_id IN (%s) ORDER BY s.task_id,s.sub_id",ids);
            book.sheet("佐证材料清单","taskId:任务ID|taskCode:任务编号|taskName:任务名称|subId:填报单ID|fileId:材料ID|fileName:文件名|fileSuffix:文件类型|fileSize:字节数|submitTime:提交时间",decorate(files,names,objects,"taskId","task"));
        }
    }
    private void performances(AnalysisWorkbook book,Long actor,List<Map<String,Object>> rows,Names names,MultiValueMap<String,String> params){
        book.sheet("绩效指标","perfId:绩效ID|parentId:分类ID|perfCode:绩效编号|perfName:绩效名称|categoryName:指标分类|year:所选年度(空为项目期)|mode:填报方式|"+PEOPLE+"|targetValue:目标值|currentValue:完成值|dataType:数据类型代码|dataTypeText:数据类型|ratio:达成比例(%)|attainment:数值达标情况|"+TIMES,rows);
        var ids=ids(rows,"perfId");var years=byIds("SELECT * FROM biz_performance_year WHERE COALESCE(is_delete,0)=0 AND perf_id IN (%s) ORDER BY perf_id,year",ids);
        if(params.containsKey("f.year"))years.removeIf(r->!params.get("f.year").contains(String.valueOf(r.get("year"))));
        years.removeIf(r->!access.canRead(actor,"performance",id(r.get("perfId")),(int)id(r.get("year"))));
        var objects=rows.stream().collect(Collectors.toMap(r->id(r.get("perfId")),r->r,(a,b)->a));
        book.sheet("绩效年度值","perfId:绩效ID|perfCode:绩效编号|perfName:绩效名称|yearId:年度记录ID|year:年度|targetValue:年度目标值|actualValue:年度完成值|dataType:数据类型",decorate(years,names,objects,"perfId","perf"));
        var validYears=new HashSet<>(ids(years,"yearId"));
        // Only authorized annual submissions; do not expose a disallowed year's historical values.
        var eligible=byIds("SELECT * FROM biz_performance_submission WHERE COALESCE(is_delete,0)=0 AND perf_id IN (%s) ORDER BY perf_id,sub_id",ids);
        eligible.removeIf(r->!validYears.contains(id(r.get("yearId"))));
        if(access.allowed(actor,"performance.history")){
            var decorated=decorate(eligible,names,objects,"perfId","perf");decorated.forEach(r->r.put("flowStatusText",flow("performance",id(r.get("flowStatus")))));book.sheet("填报记录",SUB,decorated);
            var logs=byIds("SELECT * FROM biz_performance_audit_log WHERE sub_id IN (%s) ORDER BY sub_id,log_id",ids(decorated,"subId"));book.sheet("审核记录",LOG,logs.stream().map(names::enrich).toList());
        }
        var relations=byIds("SELECT * FROM rel_task_performance WHERE COALESCE(is_delete,0)=0 AND perf_id IN (%s) ORDER BY perf_id,task_id",ids);var taskObjects=access.visibleTasks(actor,null).stream().collect(Collectors.toMap(t->t.getTaskId(),t->filters.wireRow(t)));
        relations.removeIf(r->!validYears.contains(id(r.get("yearId")))||!taskObjects.containsKey(id(r.get("taskId"))));
        book.sheet("关联任务","perfId:绩效ID|perfCode:绩效编号|perfName:绩效名称|yearId:绩效年度ID|taskId:任务ID|taskCode:任务编号|taskName:任务名称|weight:权重|contributionValue:贡献值|dataType:数据类型",decorate(decorate(relations,names,objects,"perfId","perf"),names,taskObjects,"taskId","task"));
    }
    private void achievements(AnalysisWorkbook book,Long actor,List<Map<String,Object>> rows,Names names){
        book.sheet("成果明细","achId:成果ID|category:成果类别代码|level:成果级别|achName:成果名称|department:组织或颁发单位|gotTime:取得时间|deptId:报送部门ID|deptName:报送部门|createBy:报送人账号|createName:报送人|isCompetition:是否竞赛|teDengJiang:特等奖数|yiDengJiang:一等奖数|erDengJiang:二等奖数|sanDengJiang:三等奖数|jinJiang:金奖数|yinJiang:银奖数|tongJiang:铜奖数|youShengJiang:优胜奖数|budDengDengCi:不定等次数|auditStatus:审核状态代码|rejectionReason:当前驳回原因|submittedTime:最近提交时间|archivedTime:当前归档时间|comment:备注|"+TIMES,rows);
        var ids=ids(rows,"achId");var objects=rows.stream().collect(Collectors.toMap(r->id(r.get("achId")),r->r));
        submissions(book,actor,"achievements","biz_achievement_submission","biz_achievement_audit_log","achId",ids,names,objects);
        if(access.allowed(actor,"achievements.materials"))book.sheet("成果材料","achId:成果ID|achName:成果名称|fileId:材料ID|fileName:文件名|fileSuffix:文件类型|fileSize:字节数",byIds("SELECT a.ach_id,a.ach_name,a.file_id,f.file_name,f.file_suffix,f.file_size FROM biz_achievement a LEFT JOIN sys_file f ON f.file_id=a.file_id AND COALESCE(f.is_delete,0)=0 WHERE a.ach_id IN (%s) ORDER BY a.ach_id",ids));
    }
    private void core(AnalysisWorkbook book,Long actor,List<Map<String,Object>> rows,Names names,MultiValueMap<String,String> params){
        var summary=new ArrayList<Map<String,Object>>();for(var row:rows){var item=new LinkedHashMap<>(row);Object result=item.get("effectiveResult");if(result instanceof Map<?,?> archive){item.put("archivedValue",archive.get("actualValue"));item.put("archivedMonth",archive.get("month"));}summary.add(item);}
        book.sheet("核心指标","indicatorId:指标ID|indicatorCode:指标编号|sortOrder:改革任务序号|indicatorName:指标名称|year:统计年度|month:截至月份|targetValue:期满目标值|archivedValue:有效归档累计值|archivedMonth:归档所属月份|deptName:归口部门|principalId:归口审核人账号|principalName:归口审核人|configurationError:配置异常",summary);
        var reports=byIds("SELECT * FROM biz_core_report WHERE indicator_id IN (%s) ORDER BY indicator_id,record_year,record_month,report_id",ids(rows,"indicatorId"));
        var periods=rows.stream().map(r->id(r.get("indicatorId"))+":"+id(r.get("year"))).collect(Collectors.toSet());
        reports.removeIf(r->!periods.contains(id(r.get("indicatorId"))+":"+id(r.get("recordYear")))||((id(r.get("flowStatus"))==0||id(r.get("flowStatus"))==-2)&&!Objects.equals(id(r.get("submitBy")),actor))||(params.containsKey("f.month")&&!params.get("f.month").contains(String.valueOf(r.get("recordMonth")))));
        var mappings=new HashMap<Long,String>();for(var mapping:db.queryForList("SELECT indicator_id,indicator_code FROM biz_core_indicator"))mappings.put(id(mapping.get("indicator_id")),String.valueOf(mapping.get("indicator_code")));
        reports.forEach(r->{r.put("flowStatusText",flow("core",id(r.get("flowStatus"))));r.put("mappingCurrent",Objects.equals(mappings.get(id(r.get("indicatorId"))),r.get("performanceCode")));});
        book.sheet("核心填报历史","reportId:填报ID|indicatorId:指标ID|recordYear:年度|recordMonth:月份|performanceCode:提交时指标编号|indicatorName:提交时名称|targetValue:提交时目标值|previousYearValue:上年末累计值|actualValue:本次累计值|comment:工作说明|submitBy:提交人账号|submitName:提交人|principalId:归口审核人账号|principalName:归口审核人|archiveHandlerId:归档人账号|archiveHandlerName:归档人|flowStatus:状态代码|flowStatusText:状态|mappingCurrent:是否当前映射|submitTime:提交时间|archiveTime:归档时间|"+TIMES,reports.stream().map(names::enrich).toList());
        if(access.allowed(actor,"core.history"))book.sheet("核心审核记录",LOG,byIds("SELECT * FROM biz_core_audit_log WHERE report_id IN (%s) ORDER BY report_id,log_id",ids(reports,"reportId")).stream().map(names::enrich).toList());
    }
    private void records(AnalysisWorkbook book,Long actor,List<Map<String,Object>> rows,Names names){
        // Existing document exports cover submitted records only; keep that boundary for Excel.
        rows=rows.stream().filter(r->id(r.get("status"))==1).toList();
        book.sheet("纪实正文","recordId:纪实ID|ownerId:填报人账号|ownerName:填报人|recordYear:年度|recordMonth:月份|status:状态代码|problems:存在问题|nextFocus:下阶段重点|otherMatters:其他事项|submitTime:提交时间|"+TIMES,rows);
        var ids=ids(rows,"recordId");book.sheet("工作亮点和成果","recordId:纪实ID|reformTaskId:一级任务ID|reformTaskName:一级任务名称|sortOrder:顺序|keyProgress:关键进展|stageResults:阶段性成果|typicalPractices:典型做法",byIds("SELECT * FROM biz_work_record_entry WHERE record_id IN (%s) ORDER BY record_id,sort_order,entry_id",ids));
        var snapshots=byIds("SELECT * FROM biz_work_record_snapshot WHERE record_id IN (%s) ORDER BY record_id",ids);var stats=new ArrayList<Map<String,Object>>();var tasks=new ArrayList<Map<String,Object>>();
        for(var snapshot:snapshots){try{var data=json.readValue(String.valueOf(snapshot.get("statisticsJson")),new TypeReference<LinkedHashMap<String,Object>>(){});data.put("recordId",snapshot.get("recordId"));stats.add(data);if(data.get("tasks") instanceof List<?> list)for(Object value:list){var row=json.convertValue(value,new TypeReference<LinkedHashMap<String,Object>>(){});row.put("recordId",snapshot.get("recordId"));tasks.add(row);}}catch(IOException e){throw new AdminManagementException(409,"纪实冻结统计无法读取，请先核实记录 "+snapshot.get("recordId"));}}
        book.sheet("冻结统计","recordId:纪实ID|year:年度|month:月份|generatedAt:生成时间|cutoffAt:统计截止时间|totalTasks:任务总数|newCompleted:本月完成数|cumulativeCompleted:累计完成数|completionRate:完成率(%)|unverifiedTasks:待核实数|notice:统计说明",stats);
        book.sheet("冻结任务明细","recordId:纪实ID|taskId:任务ID|taskCode:任务编号|taskName:任务名称|leaderName:负责人|reformTaskId:一级任务ID|auditState:审核状态|firstCompletedAt:首次完成时间|completed:是否完成|completedThisMonth:本月完成|needsVerification:待核实|verificationReason:待核实原因|other:其他说明",tasks);
    }
    private void budgets(AnalysisWorkbook book,Long actor,MultiValueMap<String,String> params,Names names){
        var tables=new ArrayList<Map<String,Object>>();var sources=new ArrayList<Map<String,Object>>();var tasks=new ArrayList<Map<String,Object>>();var indicators=new ArrayList<Map<String,Object>>();
        var sheets=db.queryForList("SELECT sheet_id,year,month FROM biz_budget_sheet WHERE COALESCE(is_delete,0)=0 ORDER BY year,month,sheet_id");
        for(var sheet:sheets){int year=(int)id(sheet.get("year")),month=(int)id(sheet.get("month"));if(!access.budgetScope(actor,year)||params.containsKey("f.year")&&!params.get("f.year").contains(String.valueOf(year))||params.containsKey("f.month")&&!params.get("f.month").contains(String.valueOf(month)))continue;
            for(String section:List.of("annual","execution")){if(params.containsKey("section")&&!section.equals(params.getFirst("section")))continue;var value=budget.getBudget(year,month,section);if(!Objects.equals(value.getSheet().getSheetId(),id(sheet.get("sheet_id"))))continue;
                var head=names.enrich(filters.wireRow(value.getSheet()));head.put("currentVersionNo",value.getCurrentVersionNo());tables.add(head);
                for(var source:value.getSources()){var row=filters.wireRow(source);row.put("year",year);row.put("month",month);row.put("section",section);sources.add(row);}
                for(var task:value.getTasks()){var row=filters.wireRow(task);row.put("year",year);row.put("month",month);row.put("section",section);tasks.add(row);}
                for(var entry:value.getArchivedIndicators().entrySet()){var row=new LinkedHashMap<String,Object>(entry.getValue());row.put("sheetId",head.get("sheetId"));row.put("budgetYear",year);row.put("budgetMonth",month);row.put("taskIndex",entry.getKey());indicators.add(row);}
            }
        }
        // Financial imports and exports operate on whole monthly sheets, as on the budget page.
        book.sheet("预算月表","sheetId:预算表ID|year:年度|month:月份|section:分区|locked:锁定标记|currentVersionNo:当前版本|importBy:导入人账号|importName:导入人|importTime:导入时间|updateTime:更新时间",tables);
        book.sheet("资金来源","sheetId:预算表ID|year:年度|month:月份|section:分区|sourceKey:来源代码|sourceName:资金来源|fiveYearTotal:五年项目总预算(万元)|available:年度可使用资金(万元)|annualPlan:年度预算安排(万元)|carryover:上年结转结余(万元)|arrived:到位数(万元)",sources);
        book.sheet("改革任务资金","sheetId:预算表ID|year:年度|month:月份|section:分区|taskIndex:改革任务序号|taskName:改革任务|budgetAmount:年度预算金额(万元)|amount:金额(万元)|goodsBudget:商品和服务预算(万元)|goods:商品和服务支出(万元)|capitalBudget:资本性预算(万元)|capital:资本性支出(万元)|otherBudget:其他预算(万元)|other:其他支出(万元)|coreOutput:原导入核心指标文字|targetValue:原导入目标文字|achieved:原导入完成情况文字",tasks);
        book.sheet("预算引用核心指标","sheetId:预算表ID|budgetYear:预算年度|budgetMonth:预算月份|taskIndex:改革任务序号|indicatorName:指标名称|targetValue:归档目标值|actualValue:归档累计值|month:归档所属月份|comment:归档工作说明",indicators);
    }
    private void platformNotices(AnalysisWorkbook book,Names names){
        book.sheet("平台通知","noticeId:通知ID|title:标题|content:正文|important:重要标记|audienceType:接收范围类型|status:状态|revisionOf:更正来源ID|createdBy:发布账号|createdByName:发布人|publishedAt:发布时间|expiresAt:截止时间|withdrawnAt:撤回时间|withdrawReason:撤回原因|contentVersion:正文版本",sql("SELECT notice_id,title,content,important,audience_type,status,revision_of,created_by,published_at,expires_at,withdrawn_at,withdraw_reason,content_version FROM sys_platform_notice ORDER BY notice_id").stream().map(names::enrich).toList());
        book.sheet("通知接收记录","noticeId:通知ID|userId:接收人账号|userName:接收人姓名快照|deptName:部门快照|firstOpenedAt:首次打开时间|lastOpenedAt:最后打开时间|openCount:打开次数|confirmedAt:确认时间|confirmedVersion:确认版本",sql("SELECT notice_id,user_id,user_name,dept_name,first_opened_at,last_opened_at,open_count,confirmed_at,confirmed_version FROM sys_platform_notice_recipient ORDER BY notice_id,user_id"));
        book.sheet("通知浏览日志","logId:日志ID|noticeId:通知ID|userId:浏览人账号|userDisplayName:当前姓名|action:浏览动作|contentVersion:正文版本|createTime:操作时间",sql("SELECT log_id,notice_id,user_id,action,content_version,create_time FROM sys_platform_notice_view_log ORDER BY notice_id,log_id").stream().map(names::enrich).toList());
    }
    private void reviewTasks(AnalysisWorkbook book,Names names){
        book.sheet("评审任务","roundId:轮次ID|name:评审名称|reviewYear:建设年度(空为全年度)|startsAt:开始时间|endsAt:结束时间|description:说明|published:发布标记|createBy:创建人账号|createName:创建人|"+TIMES,sql("SELECT * FROM biz_review_round ORDER BY round_id").stream().map(names::enrich).toList());
        book.sheet("专家与授权任务","roundId:轮次ID|userId:专家账号|userDisplayName:专家姓名|active:成员有效标记|taskId:授权三级任务ID|taskCode:任务编号|taskName:任务名称|phase:年度",sql("SELECT m.round_id,m.user_id,m.active,s.task_id,t.task_code,t.task_name,t.phase FROM biz_review_member m LEFT JOIN biz_review_scope s ON s.round_id=m.round_id AND s.user_id=m.user_id LEFT JOIN biz_task t ON t.task_id=s.task_id ORDER BY m.round_id,m.user_id,s.task_id").stream().map(names::enrich).toList());
        book.sheet("正式评语及历次修订","revisionId:修订ID|roundId:轮次ID|expertId:专家账号|expertName:提交时专家姓名|taskId:任务ID|taskCode:提交时任务编号|taskName:提交时任务名称|taskLevel:层级|revisionNo:正式版本|reviewText:评语|reviewResult:结论代码|resultText:评审结论|operatorId:实际操作人账号|operatorName:实际操作人|scopeJson:提交时范围和完成快照|createTime:提交时间",sql("SELECT * FROM biz_round_revision ORDER BY round_id,expert_id,task_id,revision_no").stream().map(r->{r.put("resultText","pass".equals(r.get("reviewResult"))?"通过":"不通过");return names.enrich(r);}).toList());
    }
    private Map<String,Object> flatten(Map<String,Object> row){var result=new LinkedHashMap<String,Object>();if(row.get("item")!=null)result.putAll(json.convertValue(row.get("item"),new TypeReference<LinkedHashMap<String,Object>>(){}));row.forEach((k,v)->{if(!k.equals("item"))result.put(k,v);});return result;}
    private List<Map<String,Object>> decorate(List<Map<String,Object>> rows,Names names,Map<Long,Map<String,Object>> objects,String fk,String prefix){
        var result=new ArrayList<Map<String,Object>>();for(var value:rows){var row=names.enrich(value);var parent=objects.get(id(row.get(fk)));if(parent!=null){String key=prefix.equals("perf")?"perf":prefix.equals("ach")?"ach":"task";row.put(prefix+"Code",parent.get(key+"Code"));row.put(prefix+"Name",parent.get(key+"Name"));}result.add(row);}return result;
    }
    private List<Map<String,Object>> byIds(String sql,List<Long> ids){var result=new ArrayList<Map<String,Object>>();for(int start=0;start<ids.size();start+=500){var batch=ids.subList(start,Math.min(start+500,ids.size()));String marks=String.join(",",Collections.nCopies(batch.size(),"?"));result.addAll(db.queryForList(sql.formatted(marks),batch.toArray()).stream().map(this::camelRow).toList());}return result;}
    private List<Map<String,Object>> sql(String sql){return db.queryForList(sql).stream().map(this::camelRow).collect(Collectors.toCollection(ArrayList::new));}
    private Map<String,Object> camelRow(Map<String,Object> source){var row=new LinkedHashMap<String,Object>();source.forEach((k,v)->{var key=new StringBuilder();boolean upper=false;for(char c:k.toCharArray()){if(c=='_'){upper=true;continue;}key.append(upper?Character.toUpperCase(c):c);upper=false;}row.put(key.toString(),v);});return row;}
    private static List<Long> ids(List<Map<String,Object>> rows,String key){return rows.stream().map(r->id(r.get(key))).filter(v->v>0).distinct().toList();}
    private static long id(Object value){try{return Long.parseLong(String.valueOf(value));}catch(NumberFormatException e){return 0;}}
    private static String snake(String value){return value.replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase(Locale.ROOT);}
    private static BigDecimal decimal(Object value){try{return value==null?null:new BigDecimal(value.toString());}catch(NumberFormatException e){return null;}}
    private static String flow(String module,long status){if(status<0)return "已退回或撤回";if(status==0)return "草稿";if(status==40||status==30&&!module.equals("tasks"))return "已归档";return status==10?(module.equals("core")?"待归口审核":"待专业群审核"):status==20?(module.equals("tasks")?"待归口审核":"待归档"):status==30?"待归档":"状态待核实";}
    private final class Names {
        final Map<Long,String> users=new HashMap<>(),departments=new HashMap<>();
        Names(){for(var row:db.queryForList("SELECT user_id,COALESCE(nick_name,user_name) name FROM sys_user"))users.put(id(row.get("user_id")),String.valueOf(row.get("name")));for(var row:db.queryForList("SELECT dept_id,dept_name FROM sys_dept"))departments.put(id(row.get("dept_id")),String.valueOf(row.get("dept_name")));}
        Map<String,Object> enrich(Map<String,Object> input){var row=new LinkedHashMap<>(input);
            for(String prefix:List.of("leader","auditor","principal","operator","currentHandler","archiveHandler"))row.putIfAbsent(prefix+"Name",users.get(id(row.get(prefix+"Id"))));
            for(String prefix:List.of("submit","create","import"))row.putIfAbsent(prefix+"Name",users.get(id(row.get(prefix+"By"))));
            for(String prefix:List.of("fromUser","toUser","createdBy"))row.putIfAbsent(prefix+"Name",users.get(id(row.get(prefix.equals("createdBy")?prefix:prefix+"Id"))));
            row.putIfAbsent("userDisplayName",users.get(id(row.get("userId"))));row.putIfAbsent("deptName",departments.get(id(row.get("deptId"))));row.putIfAbsent("submitDeptName",departments.get(id(row.get("submitDeptId"))));
            row.put("statusText",switch(String.valueOf(row.get("status"))){case "0"->"未开始";case "1"->"进行中";case "2"->"审核中";case "3"->"已完成";default->"";});row.put("dataTypeText",switch(String.valueOf(row.get("dataType"))){case "0"->"不关联数值";case "1"->"数值累加";case "2"->"百分比取大";default->"";});
            for(String key:List.of("scopes","beforeJson","afterJson"))if(row.get(key)!=null)row.put(key,safeJson(row.get(key)));
            for(String key:List.of("createTime","updateTime","submitTime","archiveTime","gotTime","actualCompletionDate"))if(row.get(key) instanceof Number value)row.put(key,new Date(value.longValue()));
            return row;
        }
        String safeJson(Object value){try{Object tree=value instanceof String text?json.readValue(text,Object.class):value;return json.writeValueAsString(redact(tree));}catch(IOException e){return "历史详情非结构化，请在原详情中核实";}}
        Object redact(Object value){if(value instanceof Map<?,?> map){var clean=new LinkedHashMap<String,Object>();map.forEach((k,v)->{if(!String.valueOf(k).toLowerCase(Locale.ROOT).matches(".*(password|token|secret|credential|viewkey|requestkey).*"))clean.put(String.valueOf(k),redact(v));});return clean;}if(value instanceof List<?> list)return list.stream().map(this::redact).toList();return value;}
    }
}
