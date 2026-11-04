package org.example;

import org.example.entity.BizTask;
import org.example.entity.SysNotice;
import org.example.entity.SysUser;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.example.service.AdminManagementException;
import org.example.service.PermissionService;
import org.example.service.TaskSupervisionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskSupervisionServiceTest {
    private final PermissionService access = mock(PermissionService.class);
    private final BizMapper tasks = mock(BizMapper.class);
    private final SysMapper users = mock(SysMapper.class);
    private final TaskSupervisionService service = new TaskSupervisionService(access, tasks, users);

    private BizTask task() {
        BizTask task = new BizTask();
        task.setTaskId(48L);
        task.setPhase(2026);
        task.setLevel(3);
        task.setIsDelete(0);
        task.setLeaderId(112212L);
        task.setPrincipalId(120796L);
        when(tasks.getTaskById(48L)).thenReturn(task);
        when(access.canRead(111497L, "tasks", 48L, 2026)).thenReturn(true);
        when(access.canSuperviseTask(111497L, task)).thenReturn(true);
        return task;
    }

    @Test void assignedReviewerSendsTaskLinkedReminderWithoutGenericMessagePermission() {
        task();
        SysUser recipient = new SysUser();
        recipient.setStatus("1");
        recipient.setIsDelete(0);
        when(users.getUserById(112212L)).thenReturn(recipient);

        service.send(111497L, new TaskSupervisionService.Reminder(48L, 112212L, "  请更新任务  ", "  请补充材料  "));

        var sent = ArgumentCaptor.forClass(SysNotice.class);
        verify(users).sendNotice(sent.capture());
        assertEquals(111497L, sent.getValue().getFromUserId());
        assertEquals(112212L, sent.getValue().getToUserId());
        assertEquals("0", sent.getValue().getSourceType());
        assertEquals(48L, sent.getValue().getSourceId());
        assertEquals("请更新任务", sent.getValue().getTitle());
        assertEquals("请补充材料", sent.getValue().getContent());
        verify(access, never()).require(111497L, "messages.send");
    }

    @Test void readableButUnassignedTaskCannotBeSupervised() {
        BizTask task = task();
        when(access.canSuperviseTask(111497L, task)).thenReturn(false);
        assertThrows(AdminManagementException.class, () -> service.send(111497L,
                new TaskSupervisionService.Reminder(48L, 112212L, "主题", "内容")));
        verify(users, never()).sendNotice(any());
    }

    @Test void unrelatedRecipientCannotBeSentATaskReminder() {
        task();
        assertThrows(AdminManagementException.class, () -> service.send(111497L,
                new TaskSupervisionService.Reminder(48L, 999999L, "主题", "内容")));
        verify(users, never()).sendNotice(any());
    }
}
