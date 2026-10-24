package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.SysUser;
import org.example.mapper.*;
import org.example.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionListBatchTest {
    @Test void policyReadsAreBoundedAndCachedOnlyForThisGet() {
        var jdbc=mock(JdbcTemplate.class);
        var service=spy(new PermissionService(jdbc,new ObjectMapper(),mock(SysMapper.class),
                mock(BizMapper.class),mock(PerformanceMapper.class),mock(WorkRecordScopeService.class),""));
        doReturn(1L).when(service).actor();doNothing().when(service).requireAdmin(1L);
        doReturn(true).when(service).installed();
        List<SysUser> accounts=new ArrayList<>();
        for(long id=1000;id<1501;id++){var user=new SysUser();user.setUserId(id);accounts.add(user);}
        accounts.add(accounts.get(0)); // Duplicate IDs must not duplicate query bindings.
        var request=new MockHttpServletRequest("GET","/list-filters/admin-users");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            service.prepareAccountList(accounts);
            var queries=mockingDetails(jdbc).getInvocations().stream()
                    .filter(i->i.getArguments().length>0&&String.valueOf(i.getArguments()[0]).contains(" WHERE user_id IN (")).toList();
            assertEquals(4,queries.size());
            for(String table:List.of("sys_user_permission_role","sys_user_permission")){
                Set<Long> bound=new HashSet<>();int count=0;
                for(var invocation:queries){
                    Object[] args=invocation.getArguments();
                    if(!String.valueOf(args[0]).contains("FROM "+table+" WHERE"))continue;
                    assertTrue(args.length-1<=500);
                    for(int i=1;i<args.length;i++){bound.add(((Number)args[i]).longValue());count++;}
                }
                assertEquals(501,count);assertEquals(501,bound.size());assertTrue(bound.containsAll(List.of(1000L,1500L)));
            }
            int calls=mockingDetails(jdbc).getInvocations().size();
            assertEquals("LEGACY",service.policy(1000L).mode());assertEquals("LEGACY",service.policy(1500L).mode());
            assertEquals(calls,mockingDetails(jdbc).getInvocations().size());
            clearInvocations(jdbc);service.prepareAccountList(List.of());verifyNoInteractions(jdbc);
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest("POST","/admin/permissions")));
            service.prepareAccountList(accounts);verifyNoInteractions(jdbc);
        } finally {RequestContextHolder.resetRequestAttributes();}
    }
}
