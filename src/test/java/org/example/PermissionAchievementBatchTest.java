package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.SysUser;
import org.example.mapper.*;
import org.example.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import java.util.stream.LongStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PermissionAchievementBatchTest {
    @Test void scopeMetadataIsBoundedAndRelationshipsAreReadOncePerInvocation(){
        var jdbc=mock(JdbcTemplate.class);var service=spy(new PermissionService(jdbc,new ObjectMapper(),null,null,null,null,""));
        var policy=new PermissionService.Policy("CUSTOM",Map.of(),Map.of("achievements",Map.of("mode","LEADERSHIP")),List.of(),1);
        doReturn(new PermissionService.State(1L,false,true,policy,List.of(),Set.of("achievements.read"),1)).when(service).state(1L);
        doReturn(new SysUser()).when(service).user(1L);doReturn(false).when(service).leadershipAll(1L);
        doReturn(Set.of(5L)).when(service).leadershipDepartments(1L);
        when(jdbc.queryForList(anyString(),any(Object[].class))).thenAnswer(invocation->{
            List<Map<String,Object>> result=new ArrayList<>();Object[] args=invocation.getArguments();
            for(int i=1;i<args.length;i++)result.add(Map.of("id",args[i],"dept_id",5L,"owner_id",2L,"record_year",2026));
            return result;
        });
        List<Long> ids=new ArrayList<>(LongStream.rangeClosed(1000,1500).boxed().toList());ids.add(1000L);ids.add(null);
        assertEquals(501,service.readableAchievementIds(1L,ids).size());
        var calls=mockingDetails(jdbc).getInvocations();assertEquals(2,calls.size());
        assertEquals(500,calls.iterator().next().getArguments().length-1);
        verify(service).user(1L);verify(service).leadershipAll(1L);verify(service).leadershipDepartments(1L);
        // A second invocation re-reads current relationships; this is not a permission cache.
        doReturn(Set.of()).when(service).leadershipDepartments(1L);
        assertTrue(service.readableAchievementIds(1L,List.of(1000L)).isEmpty());
        verify(service,times(2)).user(1L);verify(service,times(2)).leadershipAll(1L);verify(service,times(2)).leadershipDepartments(1L);
    }
    @Test void emptyAndForbiddenReadsDoNotQueryObjects(){
        var jdbc=mock(JdbcTemplate.class);var service=spy(new PermissionService(jdbc,new ObjectMapper(),null,null,null,null,""));
        assertTrue(service.readableAchievementIds(1L,List.of()).isEmpty());verify(service,never()).state(any());
        doReturn(new PermissionService.State(1L,false,true,null,List.of(),Set.of(),1)).when(service).state(1L);
        assertTrue(service.readableAchievementIds(1L,List.of(2L)).isEmpty());verifyNoInteractions(jdbc);verify(service,never()).user(any());
    }
}
