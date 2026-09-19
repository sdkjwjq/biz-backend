package org.example.service;

import org.apache.poi.xwpf.usermodel.*;
import org.example.entity.vo.WorkRecordVO.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 只渲染经过权限验证的提交快照，不查询当前任务或重新统计。 */
@Service
public class WorkRecordExportService {
    private final java.time.Clock clock;
    public WorkRecordExportService(@Qualifier("workRecordClock") java.time.Clock clock) { this.clock = clock; }

    public byte[] render(List<Detail> details) throws IOException {
        try (XWPFDocument output = fill(details.get(0)); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            for (int i=1; i<details.size(); i++) {
                try (XWPFDocument next = fill(details.get(i))) {
                    boolean first = true;
                    for (IBodyElement element : next.getBodyElements()) {
                        if (element instanceof XWPFParagraph paragraph) {
                            XWPFParagraph copy = output.createParagraph(); copy.getCTP().set(paragraph.getCTP().copy());
                            if (first) copy.setPageBreak(true);
                        } else if (element instanceof XWPFTable table) {
                            output.createTable().getCTTbl().set(table.getCTTbl().copy());
                        }
                        first = false;
                    }
                }
            }
            output.write(bytes); return bytes.toByteArray();
        }
    }

    private XWPFDocument fill(Detail detail) throws IOException {
        XWPFDocument doc;
        try (InputStream input = new ClassPathResource("templates/work-record.docx").getInputStream()) { doc = new XWPFDocument(input); }
        try {
            var record = detail.record(); var stats = detail.statistics();
            String period = record.getRecordYear()+"年"+record.getRecordMonth()+"月";
            Map<String,String> values = new HashMap<>();
            values.put("title", "“双高建设计划”"+period+"份工作纪实");
            values.put("owner", "填报人："+record.getOwnerName());
            values.put("date", LocalDate.now(clock.withZone(ZoneId.of("Asia/Shanghai"))).format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))+"（导出日期）");
            values.put("summary", record.getRecordMonth()+"月新增完成任务"+stats.newCompleted()+"条，年度累计完成"+stats.cumulativeCompleted()
                    +"条，全年任务数"+stats.totalTasks()+"条，完成率"+(stats.completionRate()==null ? "—" : stats.completionRate().stripTrailingZeros().toPlainString()+"%")+"。"
                    +"\n统计截止："+format(stats.cutoffAt())+"；统计生成："+format(stats.generatedAt())+"。");
            values.put("taskCaption", "表1. 截止到"+period+"，三级任务完成情况统计表");
            values.put("notice", stats.notice());
            values.put("problems", blank(record.getProblems())); values.put("nextFocus", blank(record.getNextFocus())); values.put("otherMatters", blank(record.getOtherMatters()));
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                String text = paragraph.getText();
                for (var entry : values.entrySet()) text = text.replace("{{"+entry.getKey()+"}}", entry.getValue());
                if (!text.equals(paragraph.getText())) replace(paragraph,text);
            }
            List<List<String>> tasks = new ArrayList<>(); int index=1;
            for (TaskSummary task : stats.tasks()) tasks.add(Arrays.asList(String.valueOf(index++),task.taskCode(),task.taskName(),task.leaderName(),
                    task.auditState()+(task.verificationReason()==null || task.verificationReason().isBlank() ? "" : "\n"+task.verificationReason()),""));
            fillTable(doc.getTables().get(0), tasks);
            List<List<String>> reforms = new ArrayList<>(); index=1;
            for (Entry entry : detail.entries()) {
                if (!hasContent(entry.getKeyProgress()) && !hasContent(entry.getStageResults()) && !hasContent(entry.getTypicalPractices())) continue;
                reforms.add(List.of(String.valueOf(index++),entry.getReformTaskName(),"关键进展：\n"+blank(entry.getKeyProgress())+"\n典型做法：\n"+blank(entry.getTypicalPractices()),blank(entry.getStageResults())));
            }
            fillTable(doc.getTables().get(1), reforms);
            return doc;
        } catch (RuntimeException e) { doc.close(); throw e; }
    }

    private String format(String text) { return OffsetDateTime.parse(text).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); }
    private boolean hasContent(String text) { return text != null && text.codePoints().anyMatch(value -> !Character.isWhitespace(value) && !Character.isSpaceChar(value)); }
    private String blank(String text) { return hasContent(text) ? text : "未填写"; }

    private void replace(XWPFParagraph paragraph, String text) {
        var style = paragraph.getRuns().isEmpty() || paragraph.getRuns().get(0).getCTR().getRPr()==null ? null
                : paragraph.getRuns().get(0).getCTR().getRPr().copy();
        for (int i=paragraph.getRuns().size()-1;i>=0;i--) paragraph.removeRun(i);
        XWPFRun run = paragraph.createRun();
        if (style!=null) run.getCTR().addNewRPr().set(style);
        String[] lines = text.split("\\R",-1);
        for (int i=0;i<lines.length;i++) { if(i>0) run.addBreak(); run.setText(lines[i]); }
    }

    private void fillTable(XWPFTable table, List<List<String>> rows) {
        var prototype = table.getRow(1).getCtRow().copy();
        while(table.getNumberOfRows()>1) table.removeRow(1);
        table.getRow(0).setRepeatHeader(true);
        for(List<String> values : rows) {
            XWPFTableRow row = new XWPFTableRow((org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow) prototype.copy(),table);
            if(row.getCtRow().isSetTrPr()) row.getCtRow().unsetTrPr();
            row.setCantSplitRow(false);
            for(int i=0;i<values.size();i++) {
                XWPFTableCell cell=row.getCell(i);
                while(cell.getParagraphs().size()>1) cell.removeParagraph(1);
                XWPFParagraph p=cell.getParagraphs().get(0);
                if (!p.getCTP().isSetPPr()) p.getCTP().addNewPPr();
                p.setKeepNext(false);
                replace(p, Objects.toString(values.get(i),""));
                p.getRuns().forEach(run -> {run.setFontFamily("宋体"); run.setFontSize(10);});
            }
            table.addRow(row);
        }
    }
}
