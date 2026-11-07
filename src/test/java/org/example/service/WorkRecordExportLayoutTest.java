package org.example.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.example.entity.BizWorkRecord;
import org.example.entity.vo.WorkRecordVO.Detail;
import org.example.entity.vo.WorkRecordVO.Entry;
import org.example.entity.vo.WorkRecordVO.Statistics;
import org.example.entity.vo.WorkRecordVO.TaskSummary;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkRecordExportLayoutTest {
    @Test
    void mergedExportKeepsOneCoverBreakPerRecordAndHeadingsWithFollowingContent() throws Exception {
        WorkRecordExportService service = new WorkRecordExportService(
                Clock.fixed(Instant.parse("2026-11-07T03:00:00Z"), ZoneId.of("Asia/Shanghai")));
        byte[] document = service.render(List.of(detail("张甲"), detail("李乙")));
        try (XWPFDocument word = new XWPFDocument(new ByteArrayInputStream(document))) {
            long contentHeadings = word.getParagraphs().stream()
                    .filter(paragraph -> paragraph.getText().startsWith("一、基本情况")).count();
            assertEquals(2, contentHeadings);
            for (XWPFParagraph paragraph : word.getParagraphs()) {
                boolean explicitBreak = paragraph.getRuns().stream()
                        .flatMap(run -> run.getCTR().getBrList().stream())
                        .anyMatch(br -> br.getType() != null && "page".equals(br.getType().toString()));
                assertFalse(explicitBreak && paragraph.getText().isBlank(), "空段落分页会产生空白页");
                if (paragraph.getText().startsWith("一、基本情况"))
                    assertTrue(paragraph.getCTP().xmlText().contains("pageBreakBefore"));
                if (paragraph.getText().startsWith("二、") || paragraph.getText().startsWith("表2."))
                    assertTrue(paragraph.getCTP().xmlText().contains("keepNext"));
            }
        }
    }

    private Detail detail(String owner) {
        BizWorkRecord record = new BizWorkRecord();
        record.setOwnerName(owner);
        record.setRecordYear(2026);
        record.setRecordMonth(11);
        Statistics statistics = new Statistics(1, 2026, 11, "2026-11-06T10:00:00+08:00",
                "2026-11-06T10:00:00+08:00", 1, 0, 0, BigDecimal.ZERO, 0,
                "按提交时数据统计", List.of(new TaskSummary(1L, "SG1", "任务名称", owner,
                1L, "未填报", null, false, false, false, null, "")), List.of());
        Entry entry = new Entry();
        entry.setReformTaskName("改革任务");
        entry.setKeyProgress("关键进展");
        return new Detail(record, statistics, false, List.of(entry));
    }
}
