package org.example.service;

import org.example.entity.BizAuditSnapshot;
import org.example.entity.BizLevel4Task;
import org.example.entity.BizMaterialSubmission;
import org.example.entity.BizTask;
import org.example.entity.dto.BizSubForthDTO;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class BizSubmittedDetailsTest {
    private final BizMapper mapper = mock(BizMapper.class);
    private final SysMapper files = mock(SysMapper.class);
    private final BizService service = new BizService();

    BizSubmittedDetailsTest() {
        ReflectionTestUtils.setField(service, "bizMapper", mapper);
        ReflectionTestUtils.setField(service, "sysMapper", files);
    }

    @Test
    void submittedValuesRemainIndependentOfCurrentTaskValues() {
        BizMaterialSubmission submission = new BizMaterialSubmission();
        submission.setSubId(41L); submission.setTaskId(7L);
        submission.setReportedValue(new BigDecimal("8.0000"));
        BizLevel4Task live = new BizLevel4Task();
        live.setParentId(7L); live.setTaskId(11L);
        live.setCurrentValue(BigDecimal.ZERO);
        BizAuditSnapshot task = new BizAuditSnapshot();
        task.setSubId(41L); task.setTargetType("TASK"); task.setSubmittedComment("提交时的完成情况");
        BizAuditSnapshot goal = new BizAuditSnapshot();
        goal.setSubId(41L); goal.setTargetType("LEVEL4_TASK"); goal.setTargetId(11L);
        goal.setPreviousValue(BigDecimal.ZERO); goal.setSubmittedValue(new BigDecimal("15"));
        goal.setSubmittedTargetValue(new BigDecimal("10")); goal.setSubmittedName("提交时的目标名称");
        goal.setSubmittedDataType("1");
        when(mapper.getForthLevelTasksByParentIds(anyList())).thenReturn(List.of(live));
        when(mapper.getAuditSnapshotsBySubIds(anyList())).thenReturn(List.of(task, goal));
        var result = service.auditListToAuditVoList(List.of(submission)).get(0);
        assertEquals("提交时的完成情况", result.getComment());
        assertEquals(Boolean.TRUE, result.getHistoricalDetailsAvailable());
        assertEquals(new BigDecimal("15"), result.getSub_list().get(0).getReported_value());
        assertEquals(new BigDecimal("150.00"), result.getSub_list().get(0).getActual_progress());
        assertEquals(new BigDecimal("100.00"), result.getSub_list().get(0).getCounted_progress());
        assertEquals(BigDecimal.ZERO, live.getCurrentValue());
    }

    @Test
    void legacySubmissionDoesNotBorrowCurrentLevelFourValue() {
        BizMaterialSubmission submission = new BizMaterialSubmission();
        submission.setSubId(40L); submission.setTaskId(7L);
        BizLevel4Task live = new BizLevel4Task(); live.setParentId(7L); live.setCurrentValue(new BigDecimal("999"));
        when(mapper.getForthLevelTasksByParentIds(anyList())).thenReturn(List.of(live));
        when(mapper.getAuditSnapshotsBySubIds(anyList())).thenReturn(List.of());
        var result = service.auditListToAuditVoList(List.of(submission)).get(0);
        assertEquals(Boolean.FALSE, result.getHistoricalDetailsAvailable());
        assertTrue(result.getSub_list().isEmpty());
    }

    @Test
    void levelFourCaptureStoresSubmittedValueBeforeRestoration() {
        BizTask parent = new BizTask(); parent.setTaskId(7L); parent.setTargetValue(new BigDecimal("10"));
        BizLevel4Task goal = new BizLevel4Task(); goal.setTaskId(11L); goal.setParentId(7L);
        goal.setCurrentValue(new BigDecimal("2")); goal.setTargetValue(new BigDecimal("10"));
        goal.setTaskName("原名称"); goal.setDataType("1");
        when(mapper.getActiveLevel4TasksByParentIdForUpdate(7L)).thenReturn(List.of(goal));
        var captures = new ArrayList<BizAuditSnapshot>();
        service.applySubmittedLevel4Values(parent,List.of(new BizSubForthDTO(11L,new BigDecimal("15"),"1")),captures);
        assertEquals(new BigDecimal("2"), captures.get(0).getPreviousValue());
        assertEquals(new BigDecimal("15"), captures.get(0).getSubmittedValue());
        assertEquals("原名称", captures.get(0).getSubmittedName());
    }
}
