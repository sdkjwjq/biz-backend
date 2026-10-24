package org.example;

import org.example.service.BudgetVersionListService;
import org.example.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BudgetVersionListTest {
    @Test void yearChecksAreOncePerCallAndNeverCachedAcrossCalls() {
        var jdbc=mock(JdbcTemplate.class);
        var access=mock(PermissionService.class);
        when(jdbc.queryForList(anyString())).thenAnswer(invocation->{
            List<Map<String,Object>> rows=new ArrayList<>();
            for(int i=0;i<120;i++)rows.add(Map.of("year",i<60?2025:2026,"versionId",i));
            return rows;
        });
        when(access.budgetScope(1L,2025)).thenReturn(true);
        when(access.budgetScope(1L,2026)).thenReturn(false);
        var service=new BudgetVersionListService(jdbc,access);
        assertEquals(60,service.rows(1L).size());
        verify(access,times(1)).budgetScope(1L,2025);
        verify(access,times(1)).budgetScope(1L,2026);
        when(access.budgetScope(1L,2025)).thenReturn(false);
        assertTrue(service.rows(1L).isEmpty());
        verify(access,times(2)).budgetScope(1L,2025);
        verify(access,times(2)).budgetScope(1L,2026);
        verify(access,times(2)).require(1L,"budget.versions");
    }
}
