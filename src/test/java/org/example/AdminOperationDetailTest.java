package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.SysUser;
import org.example.mapper.*;
import org.example.service.*;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminOperationDetailTest {
    @Test void singleDetailDoesNotReadOrSortAnyLogLists() {
        var mapper=mock(AdminManagementMapper.class);var users=mock(SysMapper.class);
        var actor=new SysUser();actor.setRole("0");actor.setStatus("1");actor.setIsDelete(0);
        when(users.getUserById(1L)).thenReturn(actor);
        var expected=Map.<String,Object>of("recordType","ADMIN","recordId",7L);
        when(mapper.adminOperation(7L)).thenReturn(expected);
        var service=new AdminManagementService(mapper,users,new ObjectMapper(),null,null,null);
        assertEquals(expected,service.operation("admin",7L,1L));
        verify(mapper).adminOperation(7L);verifyNoMoreInteractions(mapper);
        clearInvocations(mapper);
        assertThrows(AdminManagementException.class,()->service.operation("unknown",7L,1L));
        verifyNoInteractions(mapper);
        actor.setStatus("0");
        assertThrows(AdminManagementException.class,()->service.operation("ADMIN",7L,1L));
        verifyNoInteractions(mapper);
    }
}
