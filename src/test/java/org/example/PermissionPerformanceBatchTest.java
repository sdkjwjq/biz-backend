package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.SysUser;
import org.example.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import java.util.stream.LongStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PermissionPerformanceBatchTest {
    @Test void boundsObjectReadsAndReusesDepartmentDutyWithinOnlyThisInvocation(){
        var jdbc=mock(JdbcTemplate.class);var service=spy(new PermissionService(jdbc,new ObjectMapper(),null,null,null,null,""));
        var policy=new PermissionService.Policy("CUSTOM",Map.of(),Map.of("performance",Map.of("mode","BUSINESS")),List.of(),1);
        doReturn(new PermissionService.State(1L,false,true,policy,List.of(),Set.of("performance.read"),1)).when(service).state(1L);
        doReturn(new SysUser()).when(service).user(1L);
        when(jdbc.queryForList(anyString(),any(Object[].class))).thenAnswer(invocation->{
            List<Map<String,Object>> rows=new ArrayList<>();Object[] args=invocation.getArguments();
            for(int i=1;i<args.length;i++)rows.add(Map.of("id",args[i],"dept_id",5L,"leader_id",2L,"auditor_id",2L,"principal_id",2L));
            return rows;
        });
        when(jdbc.queryForObject(anyString(),eq(Integer.class),any(Object[].class))).thenReturn(1);
        List<Long> ids=new ArrayList<>(LongStream.rangeClosed(1000,1500).boxed().toList());ids.add(1000L);ids.add(null);
        assertEquals(501,service.readablePerformanceIds(1L,ids,2026).size());
        var reads=mockingDetails(jdbc).getInvocations().stream().filter(i->i.getMethod().getName().equals("queryForList")).toList();
        assertEquals(2,reads.size());assertEquals(500,reads.get(0).getArguments().length-1);
        verify(jdbc).queryForObject(anyString(),eq(Integer.class),any(Object[].class));verify(service).user(1L);
        when(jdbc.queryForObject(anyString(),eq(Integer.class),any(Object[].class))).thenReturn(0);
        assertTrue(service.readablePerformanceIds(1L,List.of(1000L),2026).isEmpty());
        verify(jdbc,times(2)).queryForObject(anyString(),eq(Integer.class),any(Object[].class));verify(service,times(2)).user(1L);
    }
    @Test void emptyAndForbiddenReadsSkipObjectMetadata(){
        var jdbc=mock(JdbcTemplate.class);var service=spy(new PermissionService(jdbc,new ObjectMapper(),null,null,null,null,""));
        assertTrue(service.readablePerformanceIds(1L,List.of(),2026).isEmpty());verify(service,never()).state(any());
        doReturn(new PermissionService.State(1L,false,true,null,List.of(),Set.of(),1)).when(service).state(1L);
        assertTrue(service.readablePerformanceIds(1L,List.of(2L),2026).isEmpty());verifyNoInteractions(jdbc);
    }
}
