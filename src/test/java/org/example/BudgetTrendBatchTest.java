package org.example;

import org.example.entity.*;
import org.example.mapper.BudgetMapper;
import org.example.service.BudgetTrendService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BudgetTrendBatchTest {
    private BizBudgetSheet sheet(long id,int month){var s=new BizBudgetSheet();s.setSheetId(id);s.setMonth(month);return s;}
    private BizBudgetTaskItem task(long sheet,int index,int expense){var t=new BizBudgetTaskItem();t.setSheetId(sheet);t.setTaskIndex(index);t.setGoods(BigDecimal.valueOf(expense));t.setCapital(BigDecimal.ZERO);t.setOther(BigDecimal.ZERO);t.setAmount(BigDecimal.TEN);return t;}
    private BizBudgetSourceItem source(long sheet){var s=new BizBudgetSourceItem();s.setSheetId(sheet);s.setAnnualPlan(BigDecimal.valueOf(100));return s;}
    @Test void fullYearUsesThreeReadsAndSingleTaskSkipsSources(){
        var mapper=mock(BudgetMapper.class);List<BizBudgetSheet> sheets=new ArrayList<>();List<BizBudgetTaskItem> tasks=new ArrayList<>();List<BizBudgetSourceItem> sources=new ArrayList<>();
        for(int month=1;month<=12;month++){sheets.add(sheet(month,month));sources.add(source(month));for(int index=1;index<=10;index++)tasks.add(task(month,index,month));}
        when(mapper.trendSheets(2026)).thenReturn(sheets);when(mapper.trendTasks(anyList())).thenReturn(tasks);when(mapper.trendSources(anyList())).thenReturn(sources);
        var service=new BudgetTrendService(mapper);var result=service.trends(2026,0);
        @SuppressWarnings("unchecked") var points=(List<Map<String,Object>>)result.get("points");
        assertEquals(new BigDecimal("120"),points.get(11).get("cumulative"));assertEquals(new BigDecimal("10"),points.get(11).get("incremental"));
        assertEquals(new BigDecimal("120.0000"),points.get(11).get("executionRate"));
        verify(mapper).trendSheets(2026);verify(mapper).trendTasks(anyList());verify(mapper).trendSources(anyList());verifyNoMoreInteractions(mapper);
        clearInvocations(mapper);service.trends(2026,2);verify(mapper).trendSheets(2026);verify(mapper).trendTasks(anyList());verifyNoMoreInteractions(mapper);
    }
    @Test void duplicateMonthRetainsOriginalSheetSelectionAndDuplicateTaskRetainsOriginalOrder(){
        var mapper=mock(BudgetMapper.class);var a=sheet(1,1);var b=sheet(2,1);
        when(mapper.trendSheets(2026)).thenReturn(List.of(a,b));when(mapper.getSheet(2026,1)).thenReturn(b);
        List<BizBudgetTaskItem> original=new ArrayList<>();for(int i=1;i<=10;i++)original.add(task(2,i,1));
        when(mapper.getTasks(2L)).thenReturn(original);when(mapper.getSources(2L)).thenReturn(List.of(source(2)));
        var service=new BudgetTrendService(mapper);
        @SuppressWarnings("unchecked") var points=(List<Map<String,Object>>)service.trends(2026,0).get("points");
        assertEquals(new BigDecimal("10"),points.get(0).get("cumulative"));verify(mapper,never()).trendTasks(anyList());verify(mapper,times(12)).getSheet(eq(2026),anyInt());
        clearInvocations(mapper);when(mapper.trendSheets(2026)).thenReturn(List.of(b));
        var duplicate=task(2,1,99);original.add(duplicate);
        var batch=new ArrayList<>(original);Collections.reverse(batch);when(mapper.trendTasks(anyList())).thenReturn(batch);when(mapper.trendSources(anyList())).thenReturn(List.of(source(2)));
        points=(List<Map<String,Object>>)service.trends(2026,0).get("points");
        assertEquals(new BigDecimal("108"),points.get(0).get("cumulative"));verify(mapper).getTasks(2L);verify(mapper,never()).getSheet(anyInt(),anyInt());
    }
    @Test void emptyYearSkipsItemQueriesAndLegacyReadsMatchBatchValues(){
        var mapper=mock(BudgetMapper.class);var service=new BudgetTrendService(mapper);
        var empty=service.trends(2029,0);assertEquals(12,((List<?>)empty.get("points")).size());verify(mapper).trendSheets(2029);verifyNoMoreInteractions(mapper);
        when(mapper.trendSheets(2026)).thenReturn(List.of(sheet(1,1)));when(mapper.getSheet(2026,1)).thenReturn(sheet(1,1));
        List<BizBudgetTaskItem> tasks=new ArrayList<>();for(int i=1;i<=10;i++)tasks.add(task(1,i,1));
        when(mapper.trendTasks(anyList())).thenReturn(tasks);when(mapper.getTasks(1L)).thenReturn(tasks);
        when(mapper.trendSources(anyList())).thenReturn(List.of(source(1)));when(mapper.getSources(1L)).thenReturn(List.of(source(1)));
        Object legacy=ReflectionTestUtils.invokeMethod(service,"legacyYearValues",2026,0);
        assertEquals(legacy,ReflectionTestUtils.invokeMethod(service,"yearValues",2026,0));
    }
}
