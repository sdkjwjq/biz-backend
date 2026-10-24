package org.example;

import org.example.service.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditCountScopeTest {
    @Test void groupedCountsUseTheSameObjectAndYearScopeAsListResponses(){
        var permissions=mock(PermissionService.class);var state=mock(PermissionService.State.class);
        when(permissions.enforced(1L)).thenReturn(true);when(permissions.state(1L)).thenReturn(state);
        when(permissions.canRead(state,"performance",11L,2026)).thenReturn(true);
        var service=new AuditCountScopeService(permissions);
        var groups=List.of(Map.<String,Object>of("objectId",11L,"auditYear",2026,"amount",3L),
                Map.<String,Object>of("objectId",11L,"auditYear",2025,"amount",8L),
                Map.<String,Object>of("objectId",12L,"auditYear",2026,"amount",5L));
        assertEquals(3L,service.visibleCount("performance",1L,groups));
        verify(permissions).canRead(state,"performance",11L,2026);
        verify(permissions).canRead(state,"performance",11L,2025);
        verify(permissions).canRead(state,"performance",12L,2026);
        when(permissions.admin(1L)).thenReturn(true);
        assertEquals(16L,service.visibleCount("performance",1L,groups));
    }
    @Test void invalidMaterialReferencesRetainTheOriginalReadFailure(){
        var permissions=mock(PermissionService.class);var service=new AuditCountScopeService(permissions);
        assertThrows(RuntimeException.class,()->service.visibleCount("tasks",1L,List.of(Map.of("objectId",11L,"amount",-1L))));
        verifyNoInteractions(permissions);
    }
}
