package org.example.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

/** Read-only enrichment of known audit records; does not grant access to records. */
@Service
public class AuditHistoryTimeService {
    private final JdbcTemplate jdbc;
    private static final Map<String,String> TABLES=Map.of("tasks","biz_audit_log",
            "performance","biz_performance_audit_log","achievements","biz_achievement_audit_log",
            "core","biz_core_audit_log");
    public AuditHistoryTimeService(JdbcTemplate jdbc){this.jdbc=jdbc;}

    public Map<Long,Object> latest(String domain, Collection<Long> recordIds){
        String table=TABLES.get(domain);
        if(table==null)throw new IllegalArgumentException("Unsupported audit domain");
        List<Long> ids=recordIds.stream().filter(Objects::nonNull).distinct().toList();
        Map<Long,Object> result=new HashMap<>();
        String column=domain.equals("core")?"report_id":"sub_id";
        String action=domain.equals("core")?"action IN('通过','退回')":"LOWER(action_type) IN('pass','reject')";
        // Keep ordinary lists in one aggregation while staying below JDBC parameter limits.
        for(int start=0;start<ids.size();start+=10000){
            var batch=ids.subList(start,Math.min(start+10000,ids.size()));
            String sql="SELECT "+column+" id,MAX(create_time) handledTime FROM "+table+" WHERE "+action
                    +" AND "+column+" IN ("+String.join(",",Collections.nCopies(batch.size(),"?"))+") GROUP BY "+column;
            for(var row:jdbc.queryForList(sql,batch.toArray()))
                result.put(((Number)row.get("id")).longValue(),row.get("handledTime"));
        }
        return result;
    }
}
