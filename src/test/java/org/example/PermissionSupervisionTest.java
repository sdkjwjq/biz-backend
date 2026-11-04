package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.SysUser;
import org.example.mapper.BizMapper;
import org.example.mapper.PerformanceMapper;
import org.example.mapper.SysMapper;
import org.example.service.PermissionCatalog;
import org.example.service.PermissionService;
import org.example.service.WorkRecordScopeService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PermissionSupervisionTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final PermissionService permissions = spy(new PermissionService(jdbc, new ObjectMapper(),
            mock(SysMapper.class), mock(BizMapper.class), mock(PerformanceMapper.class),
            mock(WorkRecordScopeService.class), ""));

    private SysUser account() {
        SysUser user = new SysUser();
        user.setUserId(90103L);
        user.setRole("1");
        user.setStatus("1");
        user.setIsDelete(0);
        user.setDeptId(106L);
        doReturn(user).when(permissions).user(90103L);
        doReturn(null).when(permissions).department(106L);
        doReturn(List.of(PermissionCatalog.BUILTINS.get("USER"))).when(permissions).roles();
        doReturn(true).when(permissions).reviewRoundsInstalled();
        doReturn(1L).when(permissions).revision();
        return user;
    }

    @Test void taskReviewerGetsSupervisionAutomaticallyButNotGenericNoticeSending() {
        account();
        when(jdbc.queryForList(contains("sys_leadership_access"), eq(Long.class))).thenReturn(List.of());
        when(jdbc.queryForList(startsWith("SELECT t.task_id"), eq(Long.class), eq(90103L), eq(90103L), eq(90103L)))
                .thenReturn(List.of(48L));

        var state = permissions.evaluate(90103L,
                new PermissionService.Policy("CUSTOM", Map.of(), Map.of(), List.of("USER"), 1), Map.of());
        assertTrue(state.permissions().contains("tasks.supervise"));
        assertFalse(state.permissions().contains("messages.send"));

        var denied = permissions.evaluate(90103L,
                new PermissionService.Policy("CUSTOM", Map.of("tasks.supervise", false), Map.of(), List.of("USER"), 1), Map.of());
        assertFalse(denied.permissions().contains("tasks.supervise"));
    }

    @Test void configuredSchoolLeaderGetsSupervisionWithoutReviewerAssignment() {
        account();
        when(jdbc.queryForList(contains("sys_leadership_access"), eq(Long.class))).thenReturn(List.of(90103L));
        var state = permissions.evaluate(90103L,
                new PermissionService.Policy("CUSTOM", Map.of(), Map.of(), List.of("USER"), 1), Map.of());
        assertTrue(state.permissions().contains("tasks.supervise"));
    }
}
