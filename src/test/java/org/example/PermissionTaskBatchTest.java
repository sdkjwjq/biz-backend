package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.BizTask;
import org.example.mapper.BizMapper;
import org.example.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import java.util.stream.LongStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PermissionTaskBatchTest {
    @Test void oneGraphAndBoundedGoalsKeepCollisionsDeletedMainRowsAndLegacyChainResolution(){
        var jdbc=mock(JdbcTemplate.class);var mapper=mock(BizMapper.class);
        var service=spy(new PermissionService(jdbc,new ObjectMapper(),null,mapper,null,null,""));
        var policy=new PermissionService.Policy("CUSTOM",Map.of(),Map.of("tasks",Map.of("mode","LEADERSHIP","years",List.of(2026))),List.of(),1);
        var state=new PermissionService.State(10L,false,true,policy,List.of(),Set.of("tasks.read"),1);
        doReturn(state).when(service).state(10L);doReturn(false).when(service).leadershipAll(10L);
        doReturn(Set.of(5L)).when(service).leadershipDepartments(10L);
        var root=task(1,0,1,5,0);var directory=task(2,1,2,5,0);var allowed=task(3,2,3,5,0);
        when(mapper.getAllTasks()).thenReturn(List.of(root,directory,allowed,task(4,2,3,6,0),task(5,2,3,5,1)));
        when(mapper.getVisibleTasks(10L)).thenReturn(List.of());
        when(jdbc.queryForList(startsWith("SELECT task_id,CAST"),any(Object[].class))).thenAnswer(call->{
            for(Object arg:Arrays.copyOfRange(call.getArguments(),1,call.getArguments().length))
                if(Objects.equals(arg,2001L))return List.of(Map.of("task_id",2001L,"is_delete",0));
            return List.of();
        });
        when(jdbc.queryForList(startsWith("SELECT task_id,parent_id"),any(Object[].class))).thenAnswer(call->{
            List<Map<String,Object>> rows=new ArrayList<>();for(Object arg:Arrays.copyOfRange(call.getArguments(),1,call.getArguments().length)){
                Long id=(Long)arg;if(id==6L)continue;rows.add(Map.of("task_id",id,"parent_id",id==3000L?99999L:3L));
            }return rows;
        });
        doReturn(true).when(service).canRead(state,"tasks",3000L,null);
        List<Long> requested=new ArrayList<>(LongStream.rangeClosed(2000,2500).boxed().toList());
        requested.addAll(Arrays.asList(1L,2L,3L,3L,4L,5L,6L,3000L,null));
        Set<Long> expected=new HashSet<>(LongStream.rangeClosed(2000,2500).boxed().toList());expected.remove(2001L);
        expected.addAll(Set.of(1L,2L,3L,5L,3000L));
        assertEquals(expected,service.readableTaskIds(10L,requested));
        verify(mapper).getAllTasks();verify(mapper).getVisibleTasks(10L);verify(mapper,never()).getTaskById(any());
        verify(service).leadershipAll(10L);verify(service).leadershipDepartments(10L);
        verify(jdbc,times(2)).queryForList(startsWith("SELECT task_id,CAST"),any(Object[].class));
        verify(jdbc,times(2)).queryForList(startsWith("SELECT task_id,parent_id"),any(Object[].class));
        verify(service).canRead(state,"tasks",3000L,null);
        doReturn(Set.of()).when(service).leadershipDepartments(10L);
        assertTrue(service.readableTaskIds(10L,List.of(3L)).isEmpty());
        verify(service,times(2)).leadershipDepartments(10L);
    }
    @Test void noReadPermissionAndEmptyRequestDoNotReadTaskData(){
        var jdbc=mock(JdbcTemplate.class);var mapper=mock(BizMapper.class);
        var service=spy(new PermissionService(jdbc,new ObjectMapper(),null,mapper,null,null,""));
        assertTrue(service.readableTaskIds(10L,List.of()).isEmpty());verify(service,never()).state(any());
        doReturn(new PermissionService.State(10L,false,true,null,List.of(),Set.of(),1)).when(service).state(10L);
        assertTrue(service.readableTaskIds(10L,List.of(1L)).isEmpty());verifyNoInteractions(jdbc,mapper);
    }
    private BizTask task(long id,long parent,int level,long department,int deleted){
        var t=new BizTask();t.setTaskId(id);t.setParentId(parent);t.setLevel(level);t.setDeptId(department);t.setPhase(2026);t.setIsDelete(deleted);return t;
    }
}
