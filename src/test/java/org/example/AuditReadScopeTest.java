package org.example;

import org.example.service.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuditReadScopeTest {
    @Test void auditResponseKeepsOrderDuplicateRowsMissingKeysAndEveryOriginalField(){
        var permissions=mock(PermissionService.class);var scopes=new AuditReadScopeService(permissions);
        var json=new com.fasterxml.jackson.databind.ObjectMapper();
        var advice=new org.example.config.PermissionResponseAdvice(permissions,json,scopes);
        when(permissions.actor()).thenReturn(1L);when(permissions.enforced(1L)).thenReturn(true);
        when(permissions.readablePerformanceIds(eq(1L),anyCollection(),eq(2026))).thenReturn(Set.of(11L));
        var rows=List.of(Map.of("perfId",11,"year",2026,"label","first","fileName","retained.pdf"),
                Map.of("perfId",11,"year",2025,"label","blocked"),Map.of("label","missing-key"),
                Map.of("perfId",11,"year",2026,"label","duplicate-object"));
        var request=new org.springframework.http.server.ServletServerHttpRequest(new org.springframework.mock.web.MockHttpServletRequest("GET","/api/performance/audit/todo"));
        var response=new org.springframework.http.server.ServletServerHttpResponse(new org.springframework.mock.web.MockHttpServletResponse());
        var result=advice.beforeBodyWrite(rows,null,org.springframework.http.MediaType.APPLICATION_JSON,org.springframework.http.converter.json.MappingJackson2HttpMessageConverter.class,request,response);
        assertEquals(json.valueToTree(List.of(rows.get(0),rows.get(2),rows.get(3))),result);
        when(permissions.admin(1L)).thenReturn(true);
        assertSame(rows,advice.beforeBodyWrite(rows,null,org.springframework.http.MediaType.APPLICATION_JSON,org.springframework.http.converter.json.MappingJackson2HttpMessageConverter.class,request,response));
        verify(permissions).readablePerformanceIds(1L,List.of(11L),2026);
        verify(permissions).readablePerformanceIds(1L,List.of(11L),2025);
    }
    @Test void groupedYearsDoNotGrantOtherYearsOrLoseUnknownYearSemantics(){
        var permissions=mock(PermissionService.class);var scopes=new AuditReadScopeService(permissions);
        when(permissions.readablePerformanceIds(eq(1L),anyCollection(),eq(2026))).thenReturn(Set.of(11L));
        when(permissions.readablePerformanceIds(eq(1L),anyCollection(),isNull())).thenReturn(Set.of(12L));
        var current=new AuditReadScopeService.Ref(11L,2026);var past=new AuditReadScopeService.Ref(11L,2025);
        var unknown=new AuditReadScopeService.Ref(12L,null);
        assertEquals(Set.of(current,unknown),scopes.readable(1L,"performance",List.of(current,current,past,unknown)));
        verify(permissions).readablePerformanceIds(1L,List.of(11L),2026);
        verify(permissions).readablePerformanceIds(1L,List.of(11L),2025);
        verify(permissions).readablePerformanceIds(1L,List.of(12L),null);
        verifyNoMoreInteractions(permissions);
    }
    @Test void taskAndAchievementScopesIgnoreSuppliedYearExactlyAsCanReadDoes(){
        for(String domain:List.of("tasks","achievements")){
            var permissions=mock(PermissionService.class);var scopes=new AuditReadScopeService(permissions);
            when(permissions.readableTaskIds(eq(1L),anyCollection())).thenReturn(Set.of(2L));
            when(permissions.readableAchievementIds(eq(1L),anyCollection())).thenReturn(Set.of(2L));
            var refs=List.of(new AuditReadScopeService.Ref(2L,2026),new AuditReadScopeService.Ref(2L,2025),new AuditReadScopeService.Ref(3L,null));
            assertEquals(Set.of(refs.get(0),refs.get(1)),scopes.readable(1L,domain,refs));
            if(domain.equals("tasks"))verify(permissions).readableTaskIds(1L,List.of(2L,3L));
            else verify(permissions).readableAchievementIds(1L,List.of(2L,3L));
        }
    }
    @Test void emptyInputsSkipAllPermissionReads(){
        var permissions=mock(PermissionService.class);var scopes=new AuditReadScopeService(permissions);
        assertTrue(scopes.readable(1L,"core",List.of()).isEmpty());verifyNoInteractions(permissions);
        assertThrows(IllegalArgumentException.class,()->scopes.readable(1L,"raw-sql",List.of()));
    }
}
