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
        var inputs=List.of(ADMIN_OPERATION_SUMMARY_SQL,OWNERSHIP_OPERATION_SQL,RELATION_OPERATION_SQL,BUDGET_OPERATION_SQL,WORK_RECORD_OPERATION_SQL);var sources=new ArrayList<String>();
        for(int i=0;i<inputs.size();i++){var columns=new ArrayList<String>();for(String key:List.of("recordType","module","action","targetType","targetId","targetName","batchId","operatorName","reason"))columns.add("CAST(x."+key+" AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_bin "+key);
            sources.add("SELECT "+i+" sourceRank,x.recordId,x.operatorId,x.createTime,"+String.join(",",columns)+" FROM ("+inputs.get(i)+") x");}
        return "("+String.join(" UNION ALL ",sources)+") q";
    }
    private static final String ORDER=" ORDER BY q.createTime IS NULL,q.createTime DESC,q.sourceRank,q.recordId DESC";
    private static final Set<String> SCALARS=Set.of("module","operatorId","action","targetType");
    private static final List<Field> FIELDS=List.of(field("module","multi"),field("operatorId","multi"),field("action","multi"),field("targetType","multi"),
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
        var query=new Query(" WHERE 1=1",List.of(),null,false);var result=filteredPage(query,page,size,matcher);
        var items=page>result.page()?List.<Map<String,Object>>of():management.operationRowsByKeys(result.items(),actor);
        return Map.of("items",items,"total",Math.toIntExact(result.total()),"page",page,"pageSize",size);
    }
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public void legacyExport(Long actor,String module,Long operator,String keyword,String action,String start,String end,HttpServletResponse response){
        permissions.requireAdmin(actor);var matcher=legacyMatcher(module,operator,keyword,action,start,end);var query=new Query(" WHERE 1=1",List.of(),null,false);
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
        SXSSFWorkbook workbook=new SXSSFWorkbook(100);
        try(workbook){
            var sheet=workbook.createSheet("管理员操作记录");sheet.trackAllColumnsForAutoSizing();String[] headers={"模块","动作","对象类型","对象ID","对象名称","操作人ID","操作人","原因","时间"};Row header=sheet.createRow(0);for(int c=0;c<headers.length;c++)header.createCell(c).setCellValue(headers[c]);int[] index={1};
            source.accept(batch->{for(var item:batch){Row row=sheet.createRow(index[0]++);Object[] values={item.get("module"),item.get("action"),item.get("targetType"),item.get("targetId"),item.get("targetName"),item.get("operatorId"),item.get("operatorName"),item.get("reason"),item.get("createTime")};for(int c=0;c<values.length;c++)row.createCell(c).setCellValue(str(values[c]));}});
            for(int c=0;c<headers.length;c++)sheet.autoSizeColumn(c);response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");response.setHeader("Content-Disposition","attachment; filename*=UTF-8''"+URLEncoder.encode("管理员操作记录.xlsx",StandardCharsets.UTF_8).replace("+","%20"));workbook.write(response.getOutputStream());
        }catch(IOException e){throw new AdminManagementException(500,"操作记录导出失败");}finally{workbook.dispose();}
    }
    private static Field field(String key,String type){return new Field(key,key,type,true,"",List.of());}
    private int positive(String value,int fallback){if(value==null||value.isBlank())return fallback;try{int n=Integer.parseInt(value);if(n>0)return n;}catch(NumberFormatException ignored){}throw new AdminManagementException(400,"分页参数不正确");}
}
