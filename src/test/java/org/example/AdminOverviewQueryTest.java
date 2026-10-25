package org.example;

import org.example.service.*;
import org.example.mapper.AchievementMapper;
import org.example.mapper.PerformanceMapper;
import org.example.mapper.BudgetMapper;
import org.example.entity.*;
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
    @Test void coreOverviewProjectsOnlyNeededArchiveFieldsAndBatchesScope(){
        var access=mock(LeadershipAccessService.class);when(access.isAdmin(1L)).thenReturn(true);var jdbc=mock(JdbcTemplate.class);var permissions=mock(PermissionService.class);
        when(permissions.enforced(1L)).thenReturn(true);when(permissions.allowed(1L,"core.read")).thenReturn(true);when(permissions.readableCoreIds(eq(1L),anyCollection(),eq(2026))).thenReturn(Set.of(1L));
        when(jdbc.queryForList(anyString())).thenReturn(List.of(Map.of("id",1L,"code","1.1.5","perf_id",9L,"perf_name","Current name","target_value",10)));
        when(jdbc.queryForList(anyString(),any(Object[].class))).thenReturn(List.of(Map.of("indicator_id",1L,"report_id",10L,"indicator_name","Frozen name","target_value",10,"actual_value",15,"record_month",2)));
        var service=new AdminOverviewService(access,null,null,null,null,null,jdbc);ReflectionTestUtils.setField(service,"permissions",permissions);
        @SuppressWarnings("unchecked") var sections=(Map<String,Object>)service.summary(2026,4,"core",1L).get("modules");assertEquals("ok",((Map<?,?>)sections.get("core")).get("status"));
        verify(permissions).readableCoreIds(1L,List.of(1L),2026);verify(permissions,never()).canRead(anyLong(),anyString(),anyLong(),anyInt());
        var archiveQuery=mockingDetails(jdbc).getInvocations().stream().map(i->i.getArguments()[0].toString()).filter(sql->sql.contains("ROW_NUMBER")).findFirst().orElseThrow();
        assertFalse(archiveQuery.contains("SELECT *"));assertFalse(archiveQuery.contains("r.*"));assertTrue(archiveQuery.contains("BINARY c.indicator_code=BINARY r.performance_code"));
    }
    @Test void budgetSummaryReadsOnlySelectedMonthAndDoesNotLoadAnnualTrend(){
        var access=mock(LeadershipAccessService.class);when(access.isAdmin(1L)).thenReturn(true);
        var budgets=mock(BudgetMapper.class);var jdbc=mock(JdbcTemplate.class);var sheet=new BizBudgetSheet();sheet.setSheetId(10L);
        when(budgets.getSheet(2026,4)).thenReturn(sheet);
        var source=new BizBudgetSourceItem();source.setAnnualPlan(new BigDecimal("100"));when(budgets.getSources(10L)).thenReturn(List.of(source));
        List<BizBudgetTaskItem> tasks=new ArrayList<>();for(int i=1;i<=10;i++){var t=new BizBudgetTaskItem();t.setTaskIndex(i);t.setGoods(BigDecimal.ONE);t.setCapital(BigDecimal.ZERO);t.setOther(BigDecimal.ZERO);tasks.add(t);}
        when(budgets.getTasks(10L)).thenReturn(tasks);
        var service=new AdminOverviewService(access,null,null,null,budgets,new BudgetTrendService(budgets),jdbc);ReflectionTestUtils.setField(service,"permissions",mock(PermissionService.class));
        @SuppressWarnings("unchecked") var sections=(Map<String,Object>)service.summary(2026,4,"budget",1L).get("modules");
        assertEquals("ok",((Map<?,?>)sections.get("budget")).get("status"));
        verify(budgets).getSheet(2026,4);verify(budgets).getSources(10L);verify(budgets).getTasks(10L);verifyNoMoreInteractions(budgets);verifyNoInteractions(jdbc);
    }
    @Test void performanceSummaryUsesScopedAggregateWithoutFullProjectionOrNames(){
        var access=mock(LeadershipAccessService.class);var jdbc=mock(JdbcTemplate.class);var performance=mock(PerformanceMapper.class);var permissions=mock(PermissionService.class);var scopes=mock(ReadScopeSql.class);
        when(access.isAdmin(1L)).thenReturn(true);when(permissions.enforced(1L)).thenReturn(true);when(permissions.allowed(1L,"performance.read")).thenReturn(true);
        when(scopes.row(1L,"performance","p",2026)).thenReturn(new ReadScopeSql.Clause("p.dept_id=?",List.of(99L)));
        when(jdbc.queryForMap(anyString(),any(Object[].class))).thenReturn(Map.of("total",2L,"attained",1L,"unattained",0L,"uncertain",1L,"manual",1L,"automatic",1L));
        var service=new AdminOverviewService(access,performance,null,null,null,null,jdbc);ReflectionTestUtils.setField(service,"permissions",permissions);ReflectionTestUtils.setField(service,"readQueries",new OverviewReadQueries(jdbc,scopes));
        var result=service.summary(2026,10,"performance",1L);assertEquals("ok",((Map<?,?>)((Map<?,?>)result.get("modules")).get("performance")).get("status"));verifyNoInteractions(performance);
        var sql=mockingDetails(jdbc).getInvocations().stream().map(i->i.getArguments()[0].toString()).filter(q->q.contains("SUM(CASE")).findFirst().orElseThrow();assertTrue(sql.contains("p.dept_id=?"));assertTrue(sql.contains("JSON_TABLE"));assertFalse(sql.contains("SELECT *"));
    }
    @Test void scopedAncestryDoesNotInvokeWholeModuleFallback(){
        var jdbc=mock(JdbcTemplate.class);var scopes=mock(ReadScopeSql.class);when(scopes.row(1L,"performance","p",2026)).thenReturn(new ReadScopeSql.Clause("p.perf_id=?",List.of(3L)));
        var source=new OverviewReadQueries(jdbc,scopes).source("performance",2026,10,1L);assertEquals(List.of(3L,2026),source.args());assertTrue(source.sql().contains("JSON_TABLE"));assertFalse(source.sql().contains("ORDER BY year_id DESC"));verify(jdbc).queryForList(contains("HAVING COUNT(*)>1"),eq(Long.class),eq(2026));verifyNoMoreInteractions(jdbc);
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
