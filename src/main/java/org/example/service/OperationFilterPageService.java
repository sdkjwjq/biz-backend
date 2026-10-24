package org.example.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import java.util.*;
import static org.example.service.ListFilterQuery.*;

/** Optional SQL page. Ambiguous timestamp ordering and text predicates retain the legacy path. */
@Service
public class OperationFilterPageService {
    private static final Set<String> KEYS=Set.of("f.module","f.operatorId","f.action","f.targetType",
            "f.createTimeFrom","f.createTimeTo");
    private static final String SOURCES="(SELECT log_id recordId,'ADMIN' recordType,create_time createTime,module,operator_id operatorId,action,target_type targetType FROM sys_admin_operation_log "
            +"UNION ALL SELECT log_id,'OWNERSHIP',create_time,'OWNERSHIP',operator_id,'TRANSFER',target_type FROM biz_ownership_change_log "
            +"UNION ALL SELECT log_id,'RELATION',create_time,'PERFORMANCE_RELATION',operator_id,action,'PERFORMANCE_RELATION' FROM biz_performance_relation_log "
            +"UNION ALL SELECT version_id,'BUDGET',create_time,'BUDGET',created_by,operation_type,'BUDGET_SHEET' FROM biz_budget_import_version WHERE COALESCE(is_delete,0)=0 "
            +"UNION ALL SELECT record_id,'WORK_RECORD',deleted_time,'WORK_RECORD',deleted_by,'DELETE','WORK_RECORD' FROM biz_work_record WHERE COALESCE(delete_marker,0)<>0) q";
    private final PermissionService permissions;
    private final AdminManagementService management;
    private final JdbcTemplate jdbc;
    public OperationFilterPageService(PermissionService permissions,AdminManagementService management,JdbcTemplate jdbc){
        this.permissions=permissions;this.management=management;this.jdbc=jdbc;
    }
    public boolean supports(MultiValueMap<String,String> params){
        return "page".equals(params.getFirst("responseMode"))&&"false".equals(params.getFirst("includeFields"))
                &&!"export".equals(params.getFirst("purpose"))
                &&params.keySet().stream().filter(k->k.startsWith("f.")).allMatch(KEYS::contains)
                &&params.entrySet().stream().filter(e->e.getKey().startsWith("f."))
                    .noneMatch(e->e.getValue().contains("missing"))
                &&params.entrySet().stream().filter(e->e.getKey().equals("f.createTimeFrom")||e.getKey().equals("f.createTimeTo"))
                    .flatMap(e->e.getValue().stream()).filter(v->v!=null&&!v.isBlank())
                    .allMatch(v->v.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")&&!v.startsWith("0000"));
    }
    /** Null requests the compatible fallback; no partial page is returned. */
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public Map<String,Object> query(Long actor,MultiValueMap<String,String> params){
        permissions.requireAdmin(actor);
        List<Field> fields=new ArrayList<>();
        for(String key:List.of("module","operatorId","action","targetType"))fields.add(new Field(key,key,"multi",true,"",List.of()));
        fields.add(new Field("createTime","操作时间","date",true,"",List.of()));
        var query=new ListFilterQuery(params,fields);
        int requested=positive(params.getFirst("page"),1),size=Math.min(100,positive(params.getFirst("pageSize"),10));
        StringBuilder where=new StringBuilder(" WHERE 1=1");List<Object> args=new ArrayList<>();
        for(String key:List.of("module","operatorId","action","targetType")){
            var values=params.getOrDefault("f."+key,List.of()).stream().filter(v->v!=null&&!v.isBlank()).distinct().toList();
            if(values.isEmpty())continue;
            // Java's multi predicates compare exact strings, including case, accents and trailing spaces.
            where.append(" AND CAST(COALESCE(NULLIF(CAST(q.").append(key)
                    .append(" AS CHAR),''),'missing') AS BINARY) IN (")
                    .append(String.join(",",Collections.nCopies(values.size(),"CAST(? AS BINARY)"))).append(")");
            args.addAll(values);
        }
        if(!query.first("createTimeFrom").isBlank()){where.append(" AND q.createTime>=?");args.add(query.first("createTimeFrom"));}
        if(!query.first("createTimeTo").isBlank()){where.append(" AND DATE(q.createTime)<=?");args.add(query.first("createTimeTo"));}
        var count=jdbc.queryForMap("SELECT COUNT(*) total,COUNT(DISTINCT createTime) distinctTimes FROM "+SOURCES+where,args.toArray());
        long total=((Number)count.get("total")).longValue();
        // Original stable sorting preserves source encounter order for ties; do not invent a new tie-breaker.
        if(total!=((Number)count.get("distinctTimes")).longValue())return null;
        int page=(int)Math.min(requested,Math.max(1,(total+size-1)/size));
        if(total==0)return Map.of("items",List.of(),"total",0,"page",1,"pageSize",size);
        args.add(size);args.add((long)(page-1)*size);
        var keys=jdbc.queryForList("SELECT recordType,recordId FROM "+SOURCES+where+" ORDER BY createTime DESC LIMIT ? OFFSET ?",args.toArray());
        return Map.of("items",management.operationRowsByKeys(keys,actor),"total",Math.toIntExact(total),"page",page,"pageSize",size);
    }
    private int positive(String value,int fallback){
        if(value==null||value.isBlank())return fallback;
        try{int n=Integer.parseInt(value);if(n>0)return n;}catch(NumberFormatException ignored){}
        throw new AdminManagementException(400,"分页参数不正确");
    }
}
