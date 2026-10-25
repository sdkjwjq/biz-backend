package org.example.service;

import org.springframework.stereotype.Service;
import java.util.*;

/** Read-only intersections for audit rows and counts, preserving each object's own year. */
@Service
public class AuditReadScopeService {
    public record Ref(Long id,Integer year) { }
    private final PermissionService permissions;
    public AuditReadScopeService(PermissionService permissions){this.permissions=permissions;}

    public Set<Ref> readable(Long actor,String domain,Collection<Ref> requested){
        if(!Set.of("tasks","performance","achievements","core").contains(domain))throw new IllegalArgumentException("Unsupported audit domain");
        var refs=requested.stream().filter(r->r.id()!=null).distinct().toList();if(refs.isEmpty())return Set.of();
        var ids=refs.stream().map(Ref::id).distinct().toList();
        if(domain.equals("tasks")||domain.equals("achievements")){
            var allowed=domain.equals("tasks")?permissions.readableTaskIds(actor,ids):permissions.readableAchievementIds(actor,ids);
            Set<Ref> result=new HashSet<>();for(var ref:refs)if(allowed.contains(ref.id()))result.add(ref);return result;
        }
        Map<Integer,List<Long>> byYear=new LinkedHashMap<>();
        for(var ref:refs)byYear.computeIfAbsent(ref.year(),key->new ArrayList<>()).add(ref.id());
        Set<Ref> result=new HashSet<>();
        for(var entry:byYear.entrySet()){
            var allowed=domain.equals("performance")?permissions.readablePerformanceIds(actor,entry.getValue(),entry.getKey())
                    :permissions.readableCoreIds(actor,entry.getValue(),entry.getKey());
            for(Long id:allowed)result.add(new Ref(id,entry.getKey()));
        }
        return result;
    }
}
