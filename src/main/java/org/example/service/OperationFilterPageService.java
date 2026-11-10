package org.example.service;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.util.MultiValueMap;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.function.*;
import static org.example.mapper.AdminManagementMapper.*;
import static org.example.service.ListFilterQuery.*;

/** Ordered, bounded projections shared by pages and exports; change bodies are page-only. */
@Service
public class OperationFilterPageService {
    private static final String SOURCES=sources();
    private static String sources(){
        var inputs=List.of(ADMIN_OPERATION_SUMMARY_SQL,OWNERSHIP_OPERATION_SQL,RELATION_OPERATION_SQL,BUDGET_OPERATION_SQL,WORK_RECORD_OPERATION_SQL,TASK_AUDIT_OPERATION_SQL,PERFORMANCE_AUDIT_OPERATION_SQL,ACHIEVEMENT_AUDIT_OPERATION_SQL,CORE_AUDIT_OPERATION_SQL,FILE_OPERATION_SQL);var sources=new ArrayList<String>();
        for(int i=0;i<inputs.size();i++){var columns=new ArrayList<String>();for(String key:List.of("recordType","module","action","targetType","targetId","targetName","batchId","operatorName","reason"))columns.add("CAST(x."+key+" AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_bin "+key);
            sources.add("SELECT "+i+" sourceRank,x.recordId,x.operatorId,x.createTime,"
                    +"CASE WHEN x.module IN ('ADMIN_OPERATION','ADMIN_TEST') THEN 'technical' ELSE 'business' END recordClass,"
                    +String.join(",",columns)+" FROM ("+inputs.get(i)+") x");}
        return "("+String.join(" UNION ALL ",sources)+") q";
    }
    private static final String ORDER=" ORDER BY q.createTime IS NULL,q.createTime DESC,q.sourceRank,q.recordId DESC";
    private static final Set<String> SCALARS=Set.of("recordClass","module","operatorId","action","targetType");
    private static final List<Field> FIELDS=List.of(new Field("recordClass","记录类别","select",true,"",List.of(Map.of("value","business"),Map.of("value","technical"))),field("module","multi"),field("operatorId","multi"),field("action","multi"),field("targetType","multi"),
            field("createTime","date"),field("keyword","text"),field("targetId","text"),field("batchId","text"),field("reason","text"));
    private final PermissionService permissions;
    private final AdminManagementService management;
    private final JdbcTemplate jdbc;
    public OperationFilterPageService(PermissionService permissions,AdminManagementService management,JdbcTemplate jdbc){this.permissions=permissions;this.management=management;this.jdbc=jdbc;}
    public boolean supports(MultiValueMap<String,String> params){return "page".equals(params.getFirst("responseMode"))&&"false".equals(params.getFirst("includeFields"))&&!"export".equals(params.getFirst("purpose"));}
    @org.springframework.beans.factory.annotation.Autowired private RemoteOptionPages optionPages;
    public Map<String,Object> options(Long actor,String field,MultiValueMap<String,String> params){
        permissions.requireAdmin(actor);if(!SCALARS.contains(field))return null;
        String value="CAST(q."+field+" AS CHAR)",label=field.equals("operatorId")?"q.operatorName":value;
        return optionPages.query("SELECT DISTINCT "+value+" value,"+label+" label FROM "+SOURCES,List.of(),params);
    }
    private record Query(String where,List<Object> args,ListFilterQuery matcher,boolean exactSql) {}
    private Query prepare(MultiValueMap<String,String> params){
        var matcher=new ListFilterQuery(params,FIELDS);StringBuilder where=new StringBuilder(" WHERE 1=1");List<Object> args=new ArrayList<>();boolean exact=true;
        for(String key:SCALARS){var values=params.getOrDefault("f."+key,List.of()).stream().filter(v->v!=null&&!v.isBlank()).distinct().toList();if(values.isEmpty())continue;
            if(values.contains("missing")){exact=false;continue;}
            where.append(" AND CAST(COALESCE(CAST(q.").append(key).append(" AS CHAR),'missing') AS BINARY) IN (").append(String.join(",",Collections.nCopies(values.size(),"CAST(? AS BINARY)"))).append(")");args.addAll(values);
        }
        for(String key:params.keySet())if(key.startsWith("f.")&&!SCALARS.contains(key.substring(2)))exact=false;
        return new Query(where.toString(),args,matcher,exact);
    }

    private BoundedFilterPage.Result<Map<String,Object>> filteredPage(Query query,int requested,int size,Predicate<Map<String,Object>> predicate){
        var page=new BoundedFilterPage<Map<String,Object>>(requested,size);
        JdbcProjectionStream.read(jdbc,"SELECT q.* FROM "+SOURCES+query.where+ORDER,query.args,row->{if(predicate.test(row))page.accept(row);});return page.result();
    }
    private void eachBatch(Query query,Consumer<List<Map<String,Object>>> consumer){
        var batch=new ArrayList<Map<String,Object>>(BoundedFilterPage.BATCH_SIZE);
        JdbcProjectionStream.read(jdbc,"SELECT q.* FROM "+SOURCES+query.where+ORDER,query.args,row->{batch.add(row);if(batch.size()==BoundedFilterPage.BATCH_SIZE){consumer.accept(batch);batch.clear();}});if(!batch.isEmpty())consumer.accept(batch);
    }
    private boolean matches(Map<String,Object> value,ListFilterQuery query){var row=new HashMap<>(value);row.put("keyword",str(value.get("targetName"))+" "+str(value.get("targetId")));return query.matches(row,FIELDS);}
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public Map<String,Object> query(Long actor,MultiValueMap<String,String> params){
        permissions.requireAdmin(actor);var query=prepare(params);int requested=positive(params.getFirst("page"),1),size=Math.min(100,positive(params.getFirst("pageSize"),10));
        List<Map<String,Object>> keys;long total;int page;
        if(query.exactSql){total=jdbc.queryForObject("SELECT COUNT(*) FROM "+SOURCES+query.where,Long.class,query.args.toArray());page=(int)Math.min(requested,Math.max(1,(total+size-1)/size));var args=new ArrayList<>(query.args);args.add(size);args.add((long)(page-1)*size);keys=jdbc.queryForList("SELECT q.recordType,q.recordId FROM "+SOURCES+query.where+ORDER+" LIMIT ? OFFSET ?",args.toArray());}
        else{var result=filteredPage(query,requested,size,row->matches(row,query.matcher));total=result.total();page=result.page();keys=result.items();}
        return Map.of("items",management.operationRowsByKeys(keys,actor),"total",Math.toIntExact(total),"page",page,"pageSize",size);
    }
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public void export(Long actor,MultiValueMap<String,String> params,HttpServletResponse response){permissions.requireAdmin(actor);var query=prepare(params);write(response,consumer->eachBatch(query,batch->consumer.accept(batch.stream().filter(row->matches(row,query.matcher)).toList())));}
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public Map<String,Object> legacyPage(Long actor,String module,Long operator,String keyword,String action,String start,String end,Integer requested,Integer pageSize){
        permissions.requireAdmin(actor);var matcher=legacyMatcher(module,operator,keyword,action,start,end);int page=requested==null?1:Math.max(1,requested),size=pageSize==null?10:Math.min(100,Math.max(1,pageSize));
        var query=new Query(" WHERE q.sourceRank<5",List.of(),null,false);var result=filteredPage(query,page,size,matcher);
        var items=page>result.page()?List.<Map<String,Object>>of():management.operationRowsByKeys(result.items(),actor);
        return Map.of("items",items,"total",Math.toIntExact(result.total()),"page",page,"pageSize",size);
    }
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public void legacyExport(Long actor,String module,Long operator,String keyword,String action,String start,String end,HttpServletResponse response){
        permissions.requireAdmin(actor);var matcher=legacyMatcher(module,operator,keyword,action,start,end);var query=new Query(" WHERE q.sourceRank<5",List.of(),null,false);
        write(response,consumer->eachBatch(query,batch->consumer.accept(batch.stream().filter(matcher).toList())));
    }
    private Predicate<Map<String,Object>> legacyMatcher(String module,Long operator,String keyword,String action,String start,String end){
        String term=str(keyword).trim().toLowerCase(Locale.ROOT);LocalDate first=day(start),last=day(end);
        return row->{if(!str(module).trim().isEmpty()&&!module.equalsIgnoreCase(str(row.get("module"))))return false;if(!str(action).trim().isEmpty()&&!action.equalsIgnoreCase(str(row.get("action"))))return false;
            if(operator!=null&&!operator.equals(row.get("operatorId")==null?null:((Number)row.get("operatorId")).longValue()))return false;
            String date=date(row.get("createTime"));LocalDate local=date.isEmpty()?null:LocalDate.parse(date);if(first!=null&&(local==null||local.isBefore(first)))return false;if(last!=null&&(local==null||local.isAfter(last)))return false;
            return (str(row.get("targetId"))+" "+str(row.get("targetName"))+" "+str(row.get("operatorName"))+" "+str(row.get("reason"))).toLowerCase(Locale.ROOT).contains(term);};
    }
    private LocalDate day(String value){if(value==null||value.trim().isEmpty())return null;try{return LocalDate.parse(value);}catch(RuntimeException e){throw new AdminManagementException(400,"日期格式必须为yyyy-MM-dd");}}
    private void write(HttpServletResponse response,Consumer<Consumer<List<Map<String,Object>>>> source){
        SXSSFWorkbook workbook=org.example.utils.SpreadsheetSupport.streamingWorkbook(100);
        try(workbook){
            var sheet=workbook.createSheet("管理员操作记录");String[] headers={"模块","动作","对象类型","对象ID","对象名称","操作人ID","操作人","原因","时间"};Row header=sheet.createRow(0);for(int c=0;c<headers.length;c++)header.createCell(c).setCellValue(headers[c]);int[] index={1};
            source.accept(batch->{for(var item:batch){Row row=sheet.createRow(index[0]++);boolean business="business".equals(item.get("recordClass"));Object[] values={business?moduleLabel(str(item.get("module"))):item.get("module"),business?actionLabel(str(item.get("action"))):item.get("action"),business?targetLabel(str(item.get("targetType"))):item.get("targetType"),item.get("targetId"),item.get("targetName"),item.get("operatorId"),item.get("operatorName"),item.get("reason"),item.get("createTime")};for(int c=0;c<values.length;c++)row.createCell(c).setCellValue(str(values[c]));}});
            org.example.utils.SpreadsheetSupport.setColumnWidths(sheet,18,20,18,22,48,22,20,65,24);response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");response.setHeader("Content-Disposition","attachment; filename*=UTF-8''"+URLEncoder.encode("管理员操作记录.xlsx",StandardCharsets.UTF_8).replace("+","%20"));workbook.write(response.getOutputStream());
        }catch(IOException e){throw new AdminManagementException(500,"操作记录导出失败");}finally{workbook.dispose();}
    }
    private static Field field(String key,String type){return new Field(key,key,type,true,"",List.of());}
    private String moduleLabel(String value){return switch(value){case "PERMISSION"->"角色与权限";case "PLATFORM_NOTICE"->"平台通知";case "EXPERT_REVIEW"->"专家评审";case "USER"->"账号";case "DEPARTMENT"->"部门";case "OWNERSHIP"->"任务与绩效移交";case "PERFORMANCE_RELATION"->"绩效关联";case "BUDGET"->"预算";case "WORK_RECORD"->"工作纪实";case "TASK"->"建设任务";case "PERFORMANCE"->"绩效指标";case "ACHIEVEMENTS"->"建设成果";case "CORE"->"核心指标";case "FILE"->"附件";default->"其他业务";};}
    private String actionLabel(String value){return switch(value.toUpperCase(Locale.ROOT)){case "CREATE"->"新增";case "UPDATE"->"修改";case "DELETE"->"删除";case "ENABLE"->"启用";case "DISABLE"->"停用";case "RESET_PASSWORD"->"重置密码";case "USER_POLICY"->"调整个人权限";case "ROLE_UPDATE"->"调整角色权限";case "ROLE_DELETE"->"删除角色";case "EXPERT_CREATE"->"新增专家账号";case "LEADERSHIP_ACCESS"->"调整领导查阅范围";case "ROUND_CREATE"->"创建评审任务";case "ROUND_UPDATE"->"修改评审任务";case "ROUND_PUBLISH"->"发布评审任务";case "MEMBER_ADD"->"添加评审专家";case "MEMBER_REMOVE"->"移除评审专家";case "ADD"->"新增关联";case "REMOVE"->"取消关联";case "TRANSFER"->"移交职责";case "IMPORT"->"导入预算";case "RESTORE"->"恢复预算版本";case "INITIAL"->"建立初始版本";case "EDIT"->"编辑预算";case "SAVE_DRAFT"->"保存草稿";case "PUBLISH"->"发布通知";case "WITHDRAW","WITHDRAW_SUBMISSION"->"撤回";case "SCOPE_UPDATE"->"调整评审范围";case "UPLOAD"->"上传附件";case "SUBMIT"->"提交";case "PASS","APPROVE","通过"->"审核通过";case "REJECT","退回"->"退回";case "ARCHIVE"->"归档";case "撤回提交"->"撤回提交";default->value.codePoints().anyMatch(c->Character.UnicodeScript.of(c)==Character.UnicodeScript.HAN)?value:"其他操作";};}
    private String targetLabel(String value){return switch(value){case "USER"->"账号";case "ROLE"->"角色";case "DEPARTMENT"->"部门";case "TASK"->"任务";case "PERFORMANCE"->"绩效";case "ACHIEVEMENT"->"成果";case "CORE_INDICATOR"->"核心指标";case "FILE"->"附件";case "WORK_RECORD"->"工作纪实";case "BUDGET_SHEET"->"预算表";case "PLATFORM_NOTICE"->"平台通知";case "REVIEW_ROUND"->"评审任务";default->"业务对象";};}
    private int positive(String value,int fallback){if(value==null||value.isBlank())return fallback;try{int n=Integer.parseInt(value);if(n>0)return n;}catch(NumberFormatException ignored){}throw new AdminManagementException(400,"分页参数不正确");}
}
