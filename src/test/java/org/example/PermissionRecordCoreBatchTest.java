package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.SysUser;
import org.example.service.*;
import org.example.utils.AdminTestContext;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.*;
import java.util.*;
import java.util.stream.LongStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PermissionRecordCoreBatchTest {
    private PermissionService service(JdbcTemplate jdbc,String domain,Map<String,Object> rule,boolean admin){
        var s=spy(new PermissionService(jdbc,new ObjectMapper(),null,null,null,null,""));
        var policy=new PermissionService.Policy("CUSTOM",Map.of(),Map.of(domain,rule),List.of(),1);
        doReturn(new PermissionService.State(1L,admin,true,policy,List.of(),Set.of(domain+".read"),1)).when(s).state(1L);
        var user=new SysUser();user.setUserId(1L);user.setDeptId(5L);doReturn(user).when(s).user(1L);return s;
    }
    @Test void recordAndCoreMetadataAreBoundedAndRecordYearOverridesRequestedYear(){
        for(String domain:List.of("records","core")){
            var jdbc=mock(JdbcTemplate.class);var s=service(jdbc,domain,Map.of("mode","ALL","years",List.of(2026)),false);
            when(jdbc.queryForList(anyString(),any(Object[].class))).thenAnswer(call->{
                List<Map<String,Object>> rows=new ArrayList<>();Object[] args=call.getArguments();
                for(int i=1;i<args.length;i++)rows.add(Map.of("id",args[i],"owner_id",2L,"dept_id",5L,"status",1,"record_year",2026));return rows;
            });
            List<Long> ids=new ArrayList<>(LongStream.rangeClosed(1000,1500).boxed().toList());ids.add(1000L);ids.add(null);
            var visible=domain.equals("records")?s.readableRecordIds(1L,ids,2025):s.readableCoreIds(1L,ids,2026);
            assertEquals(501,visible.size());var calls=mockingDetails(jdbc).getInvocations();assertEquals(2,calls.size());assertEquals(500,calls.iterator().next().getArguments().length-1);verify(s).user(1L);
            when(jdbc.queryForList(anyString(),any(Object[].class))).thenReturn(List.of(Map.of("id",1000L,"owner_id",2L,"status",1,"record_year",2025)));
            assertTrue((domain.equals("records")?s.readableRecordIds(1L,List.of(1000L),2026):s.readableCoreIds(1L,List.of(1000L),2025)).isEmpty());
        }
    }
    @Test void draftChecksRequireOwnerOrCurrentServerValidatedAdminCapability(){
        var jdbc=mock(JdbcTemplate.class);var s=service(jdbc,"records",Map.of("mode","ALL"),true);
        when(jdbc.queryForList(anyString(),any(Object[].class))).thenReturn(List.of(Map.of("id",9L,"owner_id",2L,"status",0,"record_year",2026)));
        var request=new MockHttpServletRequest("GET","/work-records");RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try{
            assertTrue(s.readableRecordIds(1L,List.of(9L),2026).isEmpty());
            request.setAttribute(AdminTestContext.ACTOR,1L);assertEquals(Set.of(9L),s.readableRecordIds(1L,List.of(9L),2026));
            request.removeAttribute(AdminTestContext.ACTOR);assertTrue(s.readableRecordIds(1L,List.of(9L),2026).isEmpty());
            when(jdbc.queryForList(anyString(),any(Object[].class))).thenReturn(List.of(Map.of("id",9L,"owner_id",1L,"status",0,"record_year",2026)));
            assertEquals(Set.of(9L),s.readableRecordIds(1L,List.of(9L),2026));
        }finally{RequestContextHolder.resetRequestAttributes();}
    }
    @Test void ownerChecksReuseIdentityAndKeepSyntheticIdZeroSemantics(){
        var jdbc=mock(JdbcTemplate.class);var s=service(jdbc,"records",Map.of("mode","OWN"),false);
        var a=new SysUser();a.setUserId(1L);a.setDeptId(5L);var b=new SysUser();b.setUserId(2L);b.setDeptId(5L);
        assertEquals(Set.of(1L),s.readableRecordOwners(1L,List.of(a,b),2026));verify(s).user(1L);verifyNoInteractions(jdbc);
        s=service(jdbc,"records",Map.of("mode","CUSTOM","items",List.of(99)),false);
        assertTrue(s.readableRecordOwners(1L,List.of(a,b),2026).isEmpty());
        s=service(jdbc,"records",Map.of("mode","CUSTOM","items",List.of(0)),false);
        assertEquals(Set.of(1L,2L),s.readableRecordOwners(1L,List.of(a,b),2026));
    }
    @Test void recordListKeepsEncounterOrderWithOneIdentityAndLeadershipRead(){
        var jdbc=mock(JdbcTemplate.class);var s=service(jdbc,"records",Map.of("mode","LEADERSHIP"),false);
        when(jdbc.queryForList(anyString())).thenReturn(List.of(Map.of("id",9L,"owner_id",2L,"status",1,"record_year",2026),Map.of("id",3L,"owner_id",2L,"status",1,"record_year",2026)));
        doReturn(true).when(s).leadershipAll(1L);
        assertEquals(List.of(9L,3L),s.recordIds(1L));verify(s).user(1L);verify(s).leadershipAll(1L);
    }
}
