package org.example.service;

import org.example.entity.BizWorkRecord;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import java.util.*;
import static org.example.service.ListFilterQuery.*;

/** Optional paged projection; complex narrative predicates retain their original path. */
@Service
public class WorkRecordFilterPageService {
    private static final Set<String> SUPPORTED = Set.of("year", "month", "ownerId", "status", "deptId", "mine");
    private final PermissionService permissions;
    private final WorkRecordService records;
    private final JdbcTemplate jdbc;

    public WorkRecordFilterPageService(PermissionService permissions, WorkRecordService records, JdbcTemplate jdbc) {
        this.permissions = permissions;
        this.records = records;
        this.jdbc = jdbc;
    }

    public boolean supports(MultiValueMap<String,String> params) {
        return "page".equals(params.getFirst("responseMode"))
                && "false".equals(params.getFirst("includeFields"))
                && !"export".equals(params.getFirst("purpose"))
                && params.keySet().stream().filter(k -> k.startsWith("f."))
                    .allMatch(k -> SUPPORTED.contains(k.substring(2)));
    }

    @Transactional(readOnly=true, isolation=Isolation.REPEATABLE_READ)
    public Map<String,Object> query(Long actor, MultiValueMap<String,String> params) {
        permissions.require(actor,"records.read");
        List<Field> fields = List.of(
                field("year","select", List.of("2025","2026","2027","2028","2029")),
                field("month","select", java.util.stream.IntStream.rangeClosed(1,12).mapToObj(String::valueOf).toList()),
                field("ownerId","multi", List.of()), field("deptId","multi", List.of()),
                field("status","select", List.of("0","1")), field("mine","select", List.of("yes","no")));
        ListFilterQuery query = new ListFilterQuery(params,fields);
        int size = Math.min(100,positive(params.getFirst("pageSize"),10));
        int requestedPage = positive(params.getFirst("page"),1);
        List<Long> visible = records.visibleFilterIds(actor);
        if (visible.isEmpty()) return result(List.of(),0,1,size);
        StringBuilder where = new StringBuilder(" WHERE r.delete_marker=0 AND r.record_id IN (")
                .append(String.join(",",Collections.nCopies(visible.size(),"?"))).append(")");
        List<Object> args = new ArrayList<>(visible);
        Map<String,String> columns = Map.of("year","r.record_year","month","r.record_month",
                "ownerId","r.owner_id","status","r.status","deptId","u.dept_id");
        for (var entry : columns.entrySet()) {
            var selected = params.getOrDefault("f."+entry.getKey(),List.of()).stream()
                    .filter(v -> v!=null && !v.isBlank()).distinct().toList();
            if (selected.isEmpty()) continue;
            where.append(" AND COALESCE(CAST(").append(entry.getValue()).append(" AS CHAR),'missing') IN (")
                    .append(String.join(",",Collections.nCopies(selected.size(),"?"))).append(")");
            args.addAll(selected);
        }
        String mine = query.first("mine");
        if (mine!=null && !mine.isBlank()) {
            where.append(" AND CASE WHEN r.owner_id=? THEN 'yes' ELSE 'no' END=?");
            args.add(actor); args.add(mine);
        }
        // Match existing current-account department lookup, including deleted accounts.
        String from = " FROM biz_work_record r LEFT JOIN sys_user u ON u.user_id=r.owner_id";
        long total = jdbc.queryForObject("SELECT COUNT(*)"+from+where,Long.class,args.toArray());
        int page = (int)Math.min(requestedPage,Math.max(1,(total+size-1)/size));
        args.add(size); args.add((long)(page-1)*size);
        var items = jdbc.query("SELECT r.*"+from+where
                +" ORDER BY r.record_year DESC,r.record_month DESC,r.owner_id,r.record_id LIMIT ? OFFSET ?",
                BeanPropertyRowMapper.newInstance(BizWorkRecord.class),args.toArray());
        return result(items,total,page,size);
    }

    private Field field(String key,String type,List<String> values) {
        return new Field(key,key,type,true,"",values.stream()
                .map(v -> Map.<String,Object>of("value",v,"label",v)).toList());
    }
    private int positive(String value,int fallback) {
        if (value==null || value.isBlank()) return fallback;
        try { int number=Integer.parseInt(value); if(number>0)return number; } catch(NumberFormatException ignored) { }
        throw new AdminManagementException(400,"分页参数不正确");
    }
    private Map<String,Object> result(List<BizWorkRecord> items,long total,int page,int size) {
        return Map.of("items",items,"total",total,"page",page,"pageSize",size);
    }
}
