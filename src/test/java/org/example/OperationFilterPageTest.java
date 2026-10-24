package org.example;

import org.example.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OperationFilterPageTest {
    private LinkedMultiValueMap<String,String> params(){
        var p=new LinkedMultiValueMap<String,String>();p.add("responseMode","page");p.add("includeFields","false");return p;
    }
    @Test void tiedOrUnknownTimesFallBackWithoutReadingPageBodies(){
        var jdbc=mock(JdbcTemplate.class);var management=mock(AdminManagementService.class);var permissions=mock(PermissionService.class);
        var service=new OperationFilterPageService(permissions,management,jdbc);
        when(jdbc.queryForMap(anyString(),any(Object[].class))).thenReturn(Map.of("total",10L,"distinctTimes",9L));
        assertNull(service.query(1L,params()));verifyNoInteractions(management);
        verify(jdbc,times(1)).queryForMap(anyString(),any(Object[].class));verifyNoMoreInteractions(jdbc);
    }
    @Test void pageFetchHydratesOnlySelectedKeys(){
        var jdbc=mock(JdbcTemplate.class);var management=mock(AdminManagementService.class);var permissions=mock(PermissionService.class);
        var service=new OperationFilterPageService(permissions,management,jdbc);
        var keys=List.of(Map.<String,Object>of("recordType","ADMIN","recordId",7L));
        var items=List.of(Map.<String,Object>of("recordType","ADMIN","recordId",7L,"beforeJson","original"));
        when(jdbc.queryForMap(anyString(),any(Object[].class))).thenReturn(Map.of("total",1L,"distinctTimes",1L));
        when(jdbc.queryForList(anyString(),any(Object[].class))).thenReturn(keys);
        when(management.operationRowsByKeys(keys,1L)).thenReturn(items);
        var result=service.query(1L,params());assertEquals(items,result.get("items"));assertEquals(1,result.get("total"));
        assertFalse(result.containsKey("fields"));assertFalse(result.containsKey("ids"));
        verify(management).operationRowsByKeys(keys,1L);verifyNoMoreInteractions(management);
        verify(permissions).requireAdmin(1L);
    }
    @Test void unsupportedPredicatesAndExportsKeepLegacyPath(){
        var service=new OperationFilterPageService(null,null,null);var p=params();assertTrue(service.supports(p));
        for(var entry:Map.of("f.keyword","名称","f.createTime","missing","f.module","missing","f.createTimeTo","+10000-01-01","purpose","export").entrySet()){
            var next=params();next.add(entry.getKey(),entry.getValue());assertFalse(service.supports(next),entry.getKey());
        }
    }
}
