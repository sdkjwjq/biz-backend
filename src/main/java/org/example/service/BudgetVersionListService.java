package org.example.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

/** Metadata only: snapshots remain loaded by the existing version detail endpoint. */
@Service
public class BudgetVersionListService {
    private final JdbcTemplate jdbc;
    private final PermissionService permissions;
    public BudgetVersionListService(JdbcTemplate jdbc, PermissionService permissions) {
        this.jdbc=jdbc;
        this.permissions=permissions;
    }
    private static final String SELECT="SELECT v.version_id versionId,v.sheet_id sheetId,v.year,v.month,"
            +"v.version_no versionNo,v.operation_type operationType,v.source_version_id sourceVersionId,"
            +"v.file_id fileId,v.file_name fileName,v.created_by createdBy,u.nick_name createdByName,"
            +"v.create_time createTime,v.remark,CAST((v.version_no=latest.version_no) AS SIGNED) isCurrent ";
    private static final String FROM="FROM biz_budget_import_version v LEFT JOIN sys_user u ON u.user_id=v.created_by "
            +"LEFT JOIN (SELECT year,month,MAX(version_no) version_no FROM biz_budget_import_version "
            +"WHERE is_delete=0 GROUP BY year,month) latest ON latest.year=v.year AND latest.month=v.month ";
    private static final String ORDER=" ORDER BY v.year DESC,v.month DESC,v.version_no DESC";
    public List<Map<String,Object>> rows(Long actor) {
        permissions.require(actor,"budget.versions");
        var rows=jdbc.queryForList(SELECT+FROM+"WHERE v.is_delete=0"+ORDER);
        Map<Integer,Boolean> allowedYears=new HashMap<>();
        rows.removeIf(r->!allowedYears.computeIfAbsent(((Number)r.get("year")).intValue(),year->permissions.budgetScope(actor,year)));
        return rows;
    }
    @org.springframework.transaction.annotation.Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Map<String,Object> page(Long actor,org.springframework.util.MultiValueMap<String,String> params,List<ListFilterQuery.Field> fields){
        permissions.require(actor,"budget.versions");var query=new ListFilterQuery(params,fields);
        int size=number(params.getFirst("pageSize"),10,100),requested=number(params.getFirst("page"),1,Integer.MAX_VALUE);
        var allowed=jdbc.queryForList("SELECT DISTINCT year FROM biz_budget_import_version WHERE is_delete=0",Integer.class).stream().filter(y->permissions.budgetScope(actor,y)).toList();
        if(allowed.isEmpty())return Map.of("items",List.of(),"total",0,"page",1,"pageSize",size);
        String where="WHERE v.is_delete=0 AND v.year IN ("+String.join(",",Collections.nCopies(allowed.size(),"?"))+")";List<Object> args=new ArrayList<>(allowed);
        for(String key:List.of("year","month","createdBy","operationType","effective")){
            var values=params.getOrDefault("f."+key,List.of()).stream().filter(v->v!=null&&!v.isBlank()).distinct().toList();if(values.isEmpty())continue;
            String column=switch(key){case "createdBy"->"v.created_by";case "operationType"->"v.operation_type";case "effective"->"CASE WHEN v.version_no=latest.version_no THEN 'yes' ELSE 'no' END";default->"v."+key;};
            where+=" AND BINARY COALESCE(CAST("+column+" AS CHAR),'missing') IN ("+String.join(",",Collections.nCopies(values.size(),"?"))+")";args.addAll(values);
        }
        boolean date=params.keySet().stream().anyMatch(k->k.startsWith("f.createTime"));
        boolean ties=!jdbc.queryForList("SELECT v.year,v.month,v.version_no "+FROM+where+" GROUP BY v.year,v.month,v.version_no HAVING COUNT(*)>1 LIMIT 1",args.toArray()).isEmpty();
        long total;List<Map<String,Object>> items;
        if(date||ties){
            var matched=rows(actor).stream().peek(this::project).filter(r->query.matches(r,fields)).toList();total=matched.size();int page=(int)Math.min(requested,Math.max(1,(total+size-1)/size));items=matched.subList((page-1)*size,Math.min(page*size,matched.size()));
        }else{
            total=jdbc.queryForObject("SELECT COUNT(*) "+FROM+where,Long.class,args.toArray());int page=(int)Math.min(requested,Math.max(1,(total+size-1)/size));args.add(size);args.add((long)(page-1)*size);
            items=jdbc.queryForList(SELECT+FROM+where+ORDER+" LIMIT ? OFFSET ?",args.toArray());items.forEach(this::project);
        }
        var output=items.stream().map(r->{var item=new LinkedHashMap<>(r);item.remove("effective");return item;}).toList();
        return Map.of("items",output,"total",total,"page",(int)Math.min(requested,Math.max(1,(total+size-1)/size)),"pageSize",size);
    }
    private void project(Map<String,Object> row){boolean current=row.get("isCurrent") instanceof Number n?n.intValue()!=0:Boolean.TRUE.equals(row.get("isCurrent"));row.put("current",current);row.put("effective",current?"yes":"no");}
    private int number(String value,int fallback,int max){if(value==null||value.isBlank())return fallback;try{int n=Integer.parseInt(value);if(n>0)return Math.min(n,max);}catch(NumberFormatException ignored){}throw new AdminManagementException(400,"Invalid pagination");}
}
