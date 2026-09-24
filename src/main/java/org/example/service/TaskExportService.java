package org.example.service;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entity.BizTask;
import org.example.entity.SysUser;
import org.example.mapper.BizMapper;
import org.example.mapper.SysMapper;
import org.example.utils.BusinessLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 任务明细导出：按当前用户可见范围生成 Excel。
 * 只导出前端传入的任务 ID（即页面上当前筛选出来的行），权限在服务端二次校验。
 */
@Service
public class TaskExportService {

    private static final String[] HEADERS = {
            "序号", "任务层级", "任务编号", "任务名称", "年度", "当前进度(%)", "状态",
            "归口部门", "负责人", "专业群审核人", "归口负责人", "目标值", "完成值", "数据类型",
            "起止时间", "预期达成目标", "预期成效", "材料清单", "完成情况", "更新时间"
    };
    private static final int[] WIDTHS = {
            6, 8, 14, 46, 8, 12, 10,
            16, 12, 14, 12, 12, 12, 14,
            22, 34, 34, 40, 30, 20
    };

    @Autowired
    private BizMapper bizMapper;

    @Autowired
    private SysMapper sysMapper;

    public void exportTasks(List<Long> requestedIds, Long userId, HttpServletResponse response) throws IOException {
        SysUser me = userId == null ? null : sysMapper.getUserById(userId);
        if (me == null || Integer.valueOf(1).equals(me.getIsDelete())) {
            throw new RuntimeException("用户不存在或已停用");
        }
        // 与任务列表同一套可见性口径：管理员取全部，其他用户取本人负责/审核/归口及所辖部门
        List<BizTask> visible = "0".equals(me.getRole())
                ? bizMapper.getAllTasks() : bizMapper.getVisibleTasks(userId);
        Map<Long, BizTask> visibleMap = new HashMap<>();
        for (BizTask task : visible) {
            if (task != null && task.getTaskId() != null) visibleMap.put(task.getTaskId(), task);
        }

        List<BizTask> rows = new ArrayList<>();
        if (requestedIds == null || requestedIds.isEmpty()) {
            rows.addAll(visible);
        } else {
            // 保持前端顺序；越权或已不可见的 ID 直接忽略，不报错
            Set<Long> seen = new HashSet<>();
            for (Long id : new LinkedHashSet<>(requestedIds)) {
                BizTask task = id == null ? null : visibleMap.get(id);
                if (task != null && seen.add(task.getTaskId())) rows.add(task);
            }
        }

        Map<Long, String> userNames = loadUserNames();
        Map<Long, String> deptNames = loadDeptNames(rows);
        SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("任务明细");
            CellStyle headStyle = headStyle(workbook);
            CellStyle wrapStyle = wrapStyle(workbook);
            Row head = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = head.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headStyle);
                sheet.setColumnWidth(i, WIDTHS[i] * 256);
            }
            sheet.createFreezePane(0, 1);

            int index = 1;
            for (BizTask task : rows) {
                Row row = sheet.createRow(index);
                int c = 0;
                row.createCell(c++).setCellValue(index);
                row.createCell(c++).setCellValue(levelText(task.getLevel()));
                row.createCell(c++).setCellValue(text(task.getTaskCode()));
                row.createCell(c++).setCellValue(text(task.getTaskName()));
                row.createCell(c++).setCellValue(task.getPhase() == null ? "" : String.valueOf(task.getPhase()));
                row.createCell(c++).setCellValue(task.getProgress() == null ? 0 : task.getProgress());
                row.createCell(c++).setCellValue(statusText(task.getStatus()));
                row.createCell(c++).setCellValue(deptNames.getOrDefault(task.getDeptId(), text(task.getDeptId())));
                row.createCell(c++).setCellValue(userNames.getOrDefault(task.getLeaderId(), ""));
                row.createCell(c++).setCellValue(userNames.getOrDefault(task.getAuditorId(), ""));
                row.createCell(c++).setCellValue(userNames.getOrDefault(task.getPrincipalId(), ""));
                numberCell(row.createCell(c++), task.getTargetValue());
                numberCell(row.createCell(c++), task.getCurrentValue());
                row.createCell(c++).setCellValue(dataTypeText(task.getDataType()));
                row.createCell(c++).setCellValue(periodText(task, day));
                cellWithWrap(row.createCell(c++), task.getExpTarget(), wrapStyle);
                cellWithWrap(row.createCell(c++), task.getExpEffect(), wrapStyle);
                cellWithWrap(row.createCell(c++), task.getExpMaterialDesc(), wrapStyle);
                cellWithWrap(row.createCell(c++), task.getComment(), wrapStyle);
                row.createCell(c++).setCellValue(task.getUpdateTime() == null ? "" : day.format(task.getUpdateTime()));
                index++;
            }

            String filename = "任务明细-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8.name()) + "\"");
            response.setHeader("Cache-Control", "no-store");
            workbook.write(response.getOutputStream());
        }

        BusinessLogUtil.info("任务明细导出",
                "result", "成功",
                "userId", userId,
                "role", me.getRole(),
                "visibleCount", visible.size(),
                "exportCount", rows.size(),
                "requested", requestedIds == null ? 0 : requestedIds.size());
    }

    private Map<Long, String> loadUserNames() {
        Map<Long, String> names = new HashMap<>();
        for (SysUser user : sysMapper.getAllUsers()) {
            if (user == null || user.getUserId() == null) continue;
            String nick = user.getNickName();
            names.put(user.getUserId(), nick == null || nick.trim().isEmpty() ? text(user.getUserName()) : nick);
        }
        return names;
    }

    /** 部门数量有限，按 ID 去重读取，避免每行一次查询。 */
    private Map<Long, String> loadDeptNames(List<BizTask> rows) {
        Map<Long, String> names = new HashMap<>();
        for (BizTask task : rows) {
            Long deptId = task.getDeptId();
            if (deptId == null || names.containsKey(deptId)) continue;
            try {
                var dept = sysMapper.getDeptById(deptId);
                names.put(deptId, dept == null ? "" : text(dept.getDeptName()));
            } catch (Exception e) {
                names.put(deptId, "");
            }
        }
        return names;
    }

    private CellStyle headStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle wrapStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private void cellWithWrap(Cell cell, String value, CellStyle style) {
        cell.setCellValue(text(value));
        cell.setCellStyle(style);
    }

    private void numberCell(Cell cell, BigDecimal value) {
        if (value == null) {
            cell.setCellValue("");
        } else {
            cell.setCellValue(value.doubleValue());
        }
    }

    /** 与页面一致：一、二级任务按项目周期，三级任务按所属年度。 */
    private String periodText(BizTask task, SimpleDateFormat day) {
        Integer level = task.getLevel();
        if (Integer.valueOf(1).equals(level) || Integer.valueOf(2).equals(level)) {
            return "2025-01-01 至 2029-12-31";
        }
        Integer phase = task.getPhase();
        if (phase == null || phase <= 1900) {
            return task.getCreateTime() == null ? "" : day.format(task.getCreateTime()) + " 至 "
                    + (task.getUpdateTime() == null ? "" : day.format(task.getUpdateTime()));
        }
        return phase + "-01-01 至 " + phase + "-12-31";
    }

    private String levelText(Integer level) {
        if (level == null) return "";
        return switch (level) {
            case 1 -> "一级";
            case 2 -> "二级";
            case 3 -> "三级";
            case 4 -> "四级";
            default -> String.valueOf(level);
        };
    }

    private String statusText(String status) {
        if (status == null) return "";
        return switch (status) {
            case "0" -> "未开始";
            case "1" -> "进行中";
            case "2" -> "审核中";
            case "3" -> "已完成";
            default -> status;
        };
    }

    private String dataTypeText(String dataType) {
        if (dataType == null) return "";
        return switch (dataType) {
            case "0" -> "对指标无影响";
            case "1" -> "数值累加";
            case "2" -> "百分比取大";
            default -> dataType;
        };
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
