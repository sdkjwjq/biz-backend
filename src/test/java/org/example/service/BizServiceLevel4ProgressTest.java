package org.example.service;

import org.example.entity.BizLevel4Task;
import org.example.entity.BizTask;
import org.example.entity.dto.BizSubForthDTO;
import org.example.mapper.BizMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BizServiceLevel4ProgressTest {

    private final BizService service = new BizService();

    @Test
    void usesEqualWeightsAndCapsEachGoalAtOneHundredPercent() {
        BizTask parent = parent("10");
        BizLevel4Task overfulfilled = child(1L, "10", "15");
        BizLevel4Task halfDone = child(2L, "100", "50");
        BizLevel4Task complete = child(3L, "1", "1");

        BizService.Level4Aggregate result = service.calculateLevel4Aggregate(parent,
                List.of(overfulfilled, halfDone, complete));

        assertEquals(new BigDecimal("83.3333"), result.getAverageProgress());
        assertEquals(new BigDecimal("8.3333"), result.getParentCurrentValue());
        assertEquals(83, result.getStoredProgress());
        assertEquals(new BigDecimal("150.00"), overfulfilled.getActualProgress());
        assertEquals(new BigDecimal("100.00"), overfulfilled.getCountedProgress());
        assertEquals(100, overfulfilled.getProgress());
        assertEquals(50, halfDone.getProgress());
        assertEquals(100, complete.getProgress());
        assertFalse(result.isComplete());
    }

    @Test
    void handlesZeroPartialAndAllCompleteGoals() {
        BizService.Level4Aggregate zero = service.calculateLevel4Aggregate(parent("8"),
                List.of(child(1L, "2", "0"), child(2L, "200", "0")));
        assertEquals(new BigDecimal("0.0000"), zero.getAverageProgress());
        assertEquals(new BigDecimal("0.0000"), zero.getParentCurrentValue());
        assertEquals(0, zero.getStoredProgress());
        assertFalse(zero.isComplete());

        BizService.Level4Aggregate partial = service.calculateLevel4Aggregate(parent("8"),
                List.of(child(1L, "2", "1"), child(2L, "200", "50")));
        assertEquals(new BigDecimal("37.5000"), partial.getAverageProgress());
        assertEquals(new BigDecimal("3.0000"), partial.getParentCurrentValue());
        assertEquals(37, partial.getStoredProgress());

        BizService.Level4Aggregate complete = service.calculateLevel4Aggregate(parent("8"),
                List.of(child(1L, "2", "2"), child(2L, "200", "300")));
        assertEquals(new BigDecimal("100.0000"), complete.getAverageProgress());
        assertEquals(new BigDecimal("8.0000"), complete.getParentCurrentValue());
        assertEquals(100, complete.getStoredProgress());
        assertTrue(complete.isComplete());
    }

    @Test
    void floorsStoredProgressSoAlmostCompleteDoesNotBecomeComplete() {
        BizService.Level4Aggregate result = service.calculateLevel4Aggregate(parent("5"),
                List.of(child(1L, "100", "99.999")));

        assertEquals(new BigDecimal("99.9990"), result.getAverageProgress());
        assertEquals(new BigDecimal("4.9999"), result.getParentCurrentValue());
        assertEquals(99, result.getStoredProgress());
    }

    @Test
    void rejectsNonPositiveTargets() {
        RuntimeException childError = assertThrows(RuntimeException.class,
                () -> service.calculateLevel4Aggregate(parent("1"), List.of(child(1L, "0", "0"))));
        assertEquals("四级目标值必须大于0，请联系管理员修正任务配置", childError.getMessage());

        RuntimeException parentError = assertThrows(RuntimeException.class,
                () -> service.calculateLevel4Aggregate(parent("0"), List.of(child(1L, "1", "1"))));
        assertEquals("三级任务目标值必须大于0，请联系管理员修正任务配置", parentError.getMessage());
    }

    @Test
    void requiresTheExactSetOfActiveGoals() {
        BizMapper mapper = mock(BizMapper.class);
        BizService mappedService = new BizService();
        ReflectionTestUtils.setField(mappedService, "bizMapper", mapper);
        List<BizLevel4Task> active = List.of(child(1L, "10", "0"), child(2L, "10", "0"));
        when(mapper.getActiveLevel4TasksByParentIdForUpdate(100L)).thenReturn(active);

        RuntimeException missing = assertThrows(RuntimeException.class,
                () -> mappedService.applySubmittedLevel4Values(parent("10"),
                        List.of(submission(1L, "5")), new ArrayList<>()));
        assertEquals("请完整填报全部四级目标", missing.getMessage());

        RuntimeException duplicate = assertThrows(RuntimeException.class,
                () -> mappedService.applySubmittedLevel4Values(parent("10"),
                        List.of(submission(1L, "5"), submission(1L, "6")), new ArrayList<>()));
        assertEquals("四级任务不能重复提交", duplicate.getMessage());

        RuntimeException deletedOrForeign = assertThrows(RuntimeException.class,
                () -> mappedService.applySubmittedLevel4Values(parent("10"),
                        List.of(submission(1L, "5"), submission(99L, "6")), new ArrayList<>()));
        assertEquals("提交的四级目标与当前有效目标不一致，请刷新后重试", deletedOrForeign.getMessage());
        verify(mapper, never()).updateLevel4Task(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void percentageGoalMayExceedOneHundredPercentOfItsOwnTarget() {
        BizMapper mapper = mock(BizMapper.class);
        BizService mappedService = new BizService();
        ReflectionTestUtils.setField(mappedService, "bizMapper", mapper);
        BizLevel4Task percentage = child(1L, "40", "0");
        percentage.setDataType("2");
        when(mapper.getActiveLevel4TasksByParentIdForUpdate(100L)).thenReturn(List.of(percentage));

        BizService.Level4Aggregate result = mappedService.applySubmittedLevel4Values(parent("10"),
                List.of(submission(1L, "100")), new ArrayList<>());

        assertEquals(new BigDecimal("250.00"), percentage.getActualProgress());
        assertEquals(new BigDecimal("100.00"), percentage.getCountedProgress());
        assertEquals(new BigDecimal("10.0000"), result.getParentCurrentValue());
        verify(mapper).updateLevel4Task(percentage);
    }

    private BizTask parent(String target) {
        BizTask task = new BizTask();
        task.setTaskId(100L);
        task.setLevel(3);
        task.setTargetValue(new BigDecimal(target));
        return task;
    }

    private BizLevel4Task child(Long id, String target, String current) {
        BizLevel4Task task = new BizLevel4Task();
        task.setTaskId(id);
        task.setParentId(100L);
        task.setTargetValue(new BigDecimal(target));
        task.setCurrentValue(new BigDecimal(current));
        task.setDataType("1");
        task.setTaskName("目标" + id);
        task.setIsDelete(0);
        return task;
    }

    private BizSubForthDTO submission(Long id, String value) {
        return new BizSubForthDTO(id, new BigDecimal(value), "1");
    }
}
