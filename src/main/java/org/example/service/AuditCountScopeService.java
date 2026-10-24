package org.example.service;

import org.springframework.stereotype.Service;
import java.util.*;

/** Same scope intersection as PermissionResponseAdvice, applied to grouped counts rather than full VOs. */
@Service
public class AuditCountScopeService {
    private final PermissionService permissions;
    public AuditCountScopeService(PermissionService permissions){this.permissions=permissions;}
    public long visibleCount(String domain,Long actor,List<Map<String,Object>> groups){
        if(!Set.of("tasks","performance","achievements","core").contains(domain))throw new IllegalArgumentException("Unsupported audit domain");
        // Original task projection fails before response filtering if a referenced file is missing.
        for(var row:groups)if(((Number)row.get("amount")).longValue()<0)throw new RuntimeException("审核材料记录不存在，请联系管理员核实");
        boolean filter=permissions.enforced(actor)&&!permissions.admin(actor);
        var state=filter?permissions.state(actor):null;
        long count=0;
        for(var row:groups){
            Integer year=row.get("auditYear")==null?null:((Number)row.get("auditYear")).intValue();
            if(!filter||permissions.canRead(state,domain,((Number)row.get("objectId")).longValue(),year))count+=((Number)row.get("amount")).longValue();
        }
        return count;
    }
}
