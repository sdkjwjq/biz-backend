package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.*;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.example.service.BizService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BizAuditBatchTest {
    @Test void batchPreservesProjectionAndReducesQueries() throws Exception {
        var tasks=mock(BizMapper.class);var users=mock(SysMapper.class);var service=new BizService();
        ReflectionTestUtils.setField(service,"bizMapper",tasks);
        ReflectionTestUtils.setField(service,"sysMapper",users);
        var file=new SysFile();file.setFileId(7L);file.setFileName("材料.pdf");
        var goal=new BizLevel4Task();goal.setTaskId(101L);goal.setParentId(3L);goal.setTaskName("超额完成目标");
        goal.setTargetValue(new BigDecimal("10"));goal.setCurrentValue(new BigDecimal("15"));
        when(users.getFileById(7L)).thenReturn(file);
        when(tasks.getForthLevelTasksByParentId(3L)).thenReturn(List.of(goal));
        when(tasks.getForthLevelTasksByParentId(4L)).thenReturn(List.of());
        when(users.getFilesByIds(List.of(7L))).thenReturn(List.of(file));
        when(tasks.getForthLevelTasksByParentIds(List.of(3L,4L))).thenReturn(List.of(goal));
        var audits=new ArrayList<BizMaterialSubmission>();
        for(int i=0;i<20;i++){
            var audit=new BizMaterialSubmission();audit.setSubId((long)i);audit.setTaskId(i%2==0?3L:4L);
            audit.setFileId(7L);audit.setReportedValue(BigDecimal.valueOf(i));audits.add(audit);
        }
        var expected=audits.stream().map(service::auditToAuditVo).toList();
        verify(users,times(20)).getFileById(7L);
        verify(tasks,times(10)).getForthLevelTasksByParentId(3L);
        verify(tasks,times(10)).getForthLevelTasksByParentId(4L);
        clearInvocations(users,tasks);
        var actual=service.auditListToAuditVoList(audits);
        var json=new ObjectMapper();assertEquals(json.valueToTree(expected),json.valueToTree(actual));
        verify(users).getFilesByIds(List.of(7L));verify(tasks).getForthLevelTasksByParentIds(List.of(3L,4L));
        verifyNoMoreInteractions(users,tasks);
        clearInvocations(users,tasks);assertTrue(service.auditListToAuditVoList(List.of()).isEmpty());
        verifyNoInteractions(users,tasks);
    }
}
