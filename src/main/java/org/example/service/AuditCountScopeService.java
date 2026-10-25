package org.example.service;

import org.springframework.stereotype.Service;
import java.util.*;

/** Same scope intersection as PermissionResponseAdvice, applied to grouped counts rather than full VOs. */
@Service
public class AuditCountScopeService {
    private final PermissionService permissions;
    private final AuditReadScopeService scopes;
    public AuditCountScopeService(PermissionService permissions,AuditReadScopeService scopes){this.permissions=permissions;this.scopes=scopes;}
    public long visibleCount(String domain,Long actor,List<Map<String,Object>> groups){
        if(!Set.of("tasks","performance","achievements","core").contains(domain))throw new IllegalArgumentException("Unsupported audit domain");
        // Original task projection fails before response filtering if a referenced file is missing.
        for(var row:groups)if(((Number)row.get("amount")).longValue()<0)throw new RuntimeException("审核材料记录不存在，请联系管理员核实");
        boolean filter=permissions.enforced(actor)&&!permissions.admin(actor);
        var allowed=filter?scopes.readable(actor,domain,groups.stream().map(row->new AuditReadScopeService.Ref(((Number)row.get("objectId")).longValue(),
                row.get("auditYear")==null?null:((Number)row.get("auditYear")).intValue())).toList()):Set.<AuditReadScopeService.Ref>of();
        long count=0;
        for(var row:groups){
            Integer year=row.get("auditYear")==null?null:((Number)row.get("auditYear")).intValue();
            if(!filter||allowed.contains(new AuditReadScopeService.Ref(((Number)row.get("objectId")).longValue(),year)))count+=((Number)row.get("amount")).longValue();
        }
        return count;
    }
}
