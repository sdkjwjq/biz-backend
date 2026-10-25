package org.example;

import org.example.service.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditCountScopeTest {
    @Test void groupedCountsUseTheSameObjectAndYearScopeAsListResponses(){
        var permissions=mock(PermissionService.class);var scopes=mock(AuditReadScopeService.class);
        when(permissions.enforced(1L)).thenReturn(true);
        when(scopes.readable(eq(1L),eq("performance"),anyCollection())).thenReturn(Set.of(new AuditReadScopeService.Ref(11L,2026)));
        var service=new AuditCountScopeService(permissions,scopes);
        var groups=List.of(Map.<String,Object>of("objectId",11L,"auditYear",2026,"amount",3L),
                Map.<String,Object>of("objectId",11L,"auditYear",2025,"amount",8L),
                Map.<String,Object>of("objectId",12L,"auditYear",2026,"amount",5L));
        assertEquals(3L,service.visibleCount("performance",1L,groups));
        verify(scopes).readable(1L,"performance",List.of(new AuditReadScopeService.Ref(11L,2026),
                new AuditReadScopeService.Ref(11L,2025),new AuditReadScopeService.Ref(12L,2026)));
        verify(permissions,never()).canRead(anyLong(),anyString(),anyLong(),any());
        when(permissions.admin(1L)).thenReturn(true);
        assertEquals(16L,service.visibleCount("performance",1L,groups));
    }
    @Test void invalidMaterialReferencesRetainTheOriginalReadFailure(){
        var permissions=mock(PermissionService.class);var scopes=mock(AuditReadScopeService.class);var service=new AuditCountScopeService(permissions,scopes);
        assertThrows(RuntimeException.class,()->service.visibleCount("tasks",1L,List.of(Map.of("objectId",11L,"amount",-1L))));
        verifyNoInteractions(permissions,scopes);
    }
    @Test void unfilteredCountDoesNotReadObjectMetadata(){
        var permissions=mock(PermissionService.class);var scopes=mock(AuditReadScopeService.class);
        when(permissions.enforced(1L)).thenReturn(true);when(permissions.admin(1L)).thenReturn(true);
        var row=new HashMap<String,Object>();row.put("objectId",null);row.put("amount",2L);
        assertEquals(2,new AuditCountScopeService(permissions,scopes).visibleCount("tasks",1L,List.of(row)));
        verifyNoInteractions(scopes);
    }
}
