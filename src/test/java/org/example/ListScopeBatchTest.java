package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.*;
import org.example.mapper.*;
import org.example.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ListScopeBatchTest {
    @Test void achievementsRetainSourceOrderDuplicatesAndReportFieldsWithOneBatchCheck() {
        var mapper=mock(AchievementMapper.class);var access=mock(PermissionService.class);
        var service=new AchievementService();
        ReflectionTestUtils.setField(service,"achievementMapper",mapper);
        ReflectionTestUtils.setField(service,"permissions",access);
        var first=new BizAchievement();first.setAchId(9L);first.setRejectionReason("Keep this rejection");
        var second=new BizAchievement();second.setAchId(2L);var denied=new BizAchievement();denied.setAchId(1L);
        when(mapper.listAllAchievements()).thenReturn(List.of(first,second,denied,second));
        when(access.enforced(10L)).thenReturn(true);
        when(access.readableAchievementIds(eq(10L),anyCollection())).thenReturn(Set.of(2L,9L));
        assertEquals(List.of(first,second,second),service.listAllAchievements(10L));
        verify(access).readableAchievementIds(10L,List.of(9L,2L,1L,2L));
        verify(access,never()).canRead(anyLong(),anyString(),anyLong(),any());
        // A subsequent read must use current permissions, without retaining the former results.
        when(access.readableAchievementIds(eq(10L),anyCollection())).thenReturn(Set.of());
        assertTrue(service.listAllAchievements(10L).isEmpty());
    }

    @Test void performanceTreeRetainsAncestorOrderAndYearValueMaskingWithoutPerLeafSql() {
        var mapper=mock(PerformanceMapper.class);var jdbc=mock(JdbcTemplate.class);
        var service=spy(new PermissionService(jdbc,new ObjectMapper(),null,null,mapper,null,""));
        var policy=new PermissionService.Policy("CUSTOM",Map.of(),Map.of("performance",
                Map.of("mode","CUSTOM","items",List.of(3,4),"years",List.of(2026))),List.of(),1);
        doReturn(new PermissionService.State(10L,false,true,policy,List.of(),Set.of("performance.read"),1)).when(service).state(10L);
        doReturn(new SysUser()).when(service).user(10L);
        var parent=performance(1,0);var a=performance(3,1);var b=performance(4,1);var denied=performance(5,0);
        when(mapper.getAllPerformance()).thenReturn(List.of(b,parent,a,denied));
        when(jdbc.queryForList(anyString(),any(Object[].class))).thenReturn(List.of(
                Map.of("id",3L,"dept_id",5L),Map.of("id",4L,"dept_id",5L),Map.of("id",5L,"dept_id",5L)));
        var rows=service.visiblePerformances(10L,2026);
        assertEquals(List.of(4L,1L,3L),rows.stream().map(BizPerformance::getPerfId).toList());
        assertNull(rows.get(1).getCurrentValue());assertNull(rows.get(1).getTargetValue());
        assertEquals(BigDecimal.TEN,rows.get(0).getCurrentValue());assertEquals(BigDecimal.TEN,parent.getCurrentValue());
        verify(service).state(10L);verify(service,never()).canRead(any(PermissionService.State.class),anyString(),anyLong(),any());
        verify(jdbc).queryForList(anyString(),any(Object[].class));
        assertTrue(service.visiblePerformances(10L,null).stream().allMatch(p->p.getTargetValue()==null&&p.getCurrentValue()==null));
        assertTrue(service.visiblePerformances(10L,2025).isEmpty());
    }

    private BizPerformance performance(long id,long parent) {
        var p=new BizPerformance();p.setPerfId(id);p.setParentId(parent);p.setCurrentValue(BigDecimal.TEN);p.setTargetValue(BigDecimal.ONE);return p;
    }
}
