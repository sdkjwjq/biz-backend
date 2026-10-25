package org.example;

import org.example.service.*;
import org.example.mapper.AchievementMapper;
import org.example.mapper.PerformanceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminOverviewQueryTest {
    @Test void adminPerformanceSummaryUsesOneAggregateWithoutFullProjectionOrNames(){
        var access=mock(LeadershipAccessService.class);var jdbc=mock(JdbcTemplate.class);var performance=mock(PerformanceMapper.class);var permissions=mock(PermissionService.class);
        when(access.isAdmin(1L)).thenReturn(true);when(permissions.enforced(1L)).thenReturn(true);when(permissions.allowed(1L,"performance.read")).thenReturn(true);
        when(jdbc.queryForMap(anyString(),any(Object[].class))).thenReturn(Map.of("total",3L,"attained",1L,"unattained",1L,"uncertain",1L,"automatic",2L,"maxAnnualRows",1L,"unusualPaths",0L));
        var service=new AdminOverviewService(access,performance,null,null,null,null,jdbc);ReflectionTestUtils.setField(service,"permissions",permissions);
        @SuppressWarnings("unchecked") var modules=(Map<String,Object>)service.summary(2026,10,"performance",1L).get("modules");
        assertEquals("ok",((Map<?,?>)modules.get("performance")).get("status"));
        verifyNoInteractions(performance);verify(jdbc).queryForMap(anyString(),any(Object[].class));verifyNoMoreInteractions(jdbc);
    }
    @Test void ambiguousAnnualRowsOrNonstandardAncestryRetainOriginalProjection(){
        for(String guard:List.of("maxAnnualRows","unusualPaths")){
            var access=mock(LeadershipAccessService.class);var jdbc=mock(JdbcTemplate.class);var performance=mock(PerformanceMapper.class);
            when(access.isAdmin(1L)).thenReturn(true);var permissions=mock(PermissionService.class);
            when(permissions.enforced(1L)).thenReturn(true);when(permissions.allowed(1L,"performance.read")).thenReturn(true);
            Map<String,Object> result=new HashMap<>(Map.of("maxAnnualRows",0L,"unusualPaths",0L));result.put(guard,2L);
            when(jdbc.queryForMap(anyString(),any(Object[].class))).thenReturn(result);
            var service=new AdminOverviewService(access,performance,null,null,null,null,jdbc);ReflectionTestUtils.setField(service,"permissions",permissions);
            @SuppressWarnings("unchecked") var modules=(Map<String,Object>)service.summary(2026,10,"performance",1L).get("modules");
            assertEquals("ok",((Map<?,?>)modules.get("performance")).get("status"));
            verify(performance).getAllPerformance();verify(performance).getPerformanceYearByYear(2026);
        }
    }
    @Test void adminAchievementSummaryUsesOneAggregateWithoutHistoryOrNames(){
        var access=mock(LeadershipAccessService.class);var jdbc=mock(JdbcTemplate.class);var achievements=mock(AchievementMapper.class);
        when(access.isAdmin(1L)).thenReturn(true);
        when(jdbc.queryForMap(anyString(),any(Object[].class))).thenReturn(Map.of("total",3L,"archived",1L,"pending",1L,"rejected",1L,"awards",9L,"missingDates",2L));
        var service=new AdminOverviewService(access,null,achievements,null,null,null,jdbc);
        ReflectionTestUtils.setField(service,"permissions",mock(PermissionService.class));
        @SuppressWarnings("unchecked") var modules=(Map<String,Object>)service.summary(2026,10,"achievements",1L).get("modules");
        assertEquals("ok",((Map<?,?>)modules.get("achievements")).get("status"));
        verifyNoInteractions(achievements);verify(jdbc).queryForMap(anyString(),any(Object[].class));verifyNoMoreInteractions(jdbc);
    }
    @Test void adminTaskSummaryUsesOneAggregateAndNoTaskTreeOrNameLists(){
        var access=mock(LeadershipAccessService.class);var jdbc=mock(JdbcTemplate.class);
        when(access.isAdmin(1L)).thenReturn(true);
        when(jdbc.queryForMap(anyString(),any(Object[].class))).thenReturn(Map.of("total",2L,"completed",1L,"reviewing",1L,
                "priority",1L,"difficult",1L,"progressCount",2L,"progressSum",new BigDecimal("150")));
        var service=new AdminOverviewService(access,null,null,null,null,null,jdbc);
        ReflectionTestUtils.setField(service,"permissions",mock(PermissionService.class));
        var result=service.summary(2026,10,"tasks",1L);
        assertNotNull(result.get("modules"));verify(access,never()).scopedTasks(any());
        verify(jdbc).queryForMap(anyString(),any(Object[].class));verifyNoMoreInteractions(jdbc);
    }
    @Test void nameLookupIsReusedOnlyInsideOneGetRequest(){
        var jdbc=mock(JdbcTemplate.class);when(jdbc.queryForList(anyString())).thenReturn(List.of());
        var service=new AdminOverviewService(null,null,null,null,null,null,jdbc);
        try{
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest("GET","/admin/overview/summary")));
            Object first=ReflectionTestUtils.invokeMethod(service,"names");
            assertSame(first,ReflectionTestUtils.invokeMethod(service,"names"));
            verify(jdbc,times(2)).queryForList(anyString());
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest("GET","/admin/overview/summary")));
            assertNotSame(first,ReflectionTestUtils.invokeMethod(service,"names"));
            verify(jdbc,times(4)).queryForList(anyString());
        }finally{RequestContextHolder.resetRequestAttributes();}
    }
}
