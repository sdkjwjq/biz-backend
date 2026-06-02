package org.example.service;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entity.BizBudgetSheet;
import org.example.entity.BizBudgetSourceItem;
import org.example.entity.BizBudgetTaskItem;
import org.example.entity.SysFile;
import org.example.entity.SysUser;
import org.example.entity.dto.FileUploadDTO;
import org.example.entity.vo.BudgetSheetVO;
import org.example.mapper.BudgetMapper;
import org.example.mapper.SysMapper;
import org.example.utils.BusinessLogUtil;
import org.example.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class BudgetService {
    @Autowired
    private BudgetMapper budgetMapper;

    @Autowired
    private SysMapper sysMapper;

    private final DataFormatter formatter = new DataFormatter(Locale.CHINA);

    public BudgetSheetVO getBudget(Integer year, Integer month) {
        validateYearMonth(year, month);
        BizBudgetSheet sheet = budgetMapper.getSheet(year, month);
        if (sheet == null) {
            BudgetSheetVO empty = new BudgetSheetVO();
            BizBudgetSheet emptySheet = new BizBudgetSheet();
            emptySheet.setYear(year);
            emptySheet.setMonth(month);
            emptySheet.setLocked(0);
            emptySheet.setIsDelete(0);
            empty.setSheet(emptySheet);
            return empty;
        }
        return toVO(sheet);
    }

    @Transactional
    public BudgetSheetVO importBudget(Integer year, Integer month, MultipartFile file, Long userId) {
        validateAdmin(userId);
        validateYearMonth(year, month);
        validateExcel(file);

        ParsedBudget parsed = parse(file);
        Long fileId = saveFile(file, userId);
        Date now = new Date();

        BizBudgetSheet sheet = budgetMapper.getSheet(year, month);
        if (sheet == null) {
            sheet = new BizBudgetSheet();
            sheet.setYear(year);
            sheet.setMonth(month);
            sheet.setLocked(1);
            sheet.setFileId(fileId);
            sheet.setImportBy(userId);
            sheet.setImportTime(now);
            sheet.setUpdateTime(now);
            sheet.setIsDelete(0);
            budgetMapper.insertSheet(sheet);
        } else {
            sheet.setLocked(1);
            sheet.setFileId(fileId);
            sheet.setImportBy(userId);
            sheet.setImportTime(now);
            sheet.setUpdateTime(now);
            sheet.setIsDelete(0);
            budgetMapper.updateSheet(sheet);
            budgetMapper.deleteSources(sheet.getSheetId());
            budgetMapper.deleteTasks(sheet.getSheetId());
        }

        for (BizBudgetSourceItem source : parsed.sources) {
            source.setSheetId(sheet.getSheetId());
            budgetMapper.insertSource(source);
        }
        for (BizBudgetTaskItem task : parsed.tasks) {
            task.setSheetId(sheet.getSheetId());
            budgetMapper.insertTask(task);
        }

        BusinessLogUtil.info("预算导入",
                "result", "成功",
                "userId", userId,
                "year", year,
                "month", month,
                "sheetId", sheet.getSheetId(),
                "fileId", fileId,
                "sourceCount", parsed.sources.size(),
                "taskCount", parsed.tasks.size());
        return toVO(sheet);
    }

    public BudgetSheetVO updateLock(Integer year, Integer month, Boolean locked, Long userId) {
        validateAdmin(userId);
        validateYearMonth(year, month);
        BizBudgetSheet sheet = budgetMapper.getSheet(year, month);
        if (sheet == null) {
            throw new RuntimeException("没有找到该预算表");
        }
        budgetMapper.updateLock(sheet.getSheetId(), Boolean.TRUE.equals(locked) ? 1 : 0);
        BusinessLogUtil.info("预算锁定",
                "result", Boolean.TRUE.equals(locked) ? "锁定" : "解锁",
                "userId", userId,
                "year", year,
                "month", month,
                "sheetId", sheet.getSheetId());
        return getBudget(year, month);
    }

    public void exportBudget(Integer year, Integer month, HttpServletResponse response) throws IOException {
        BudgetSheetVO vo = getBudget(year, month);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("预算表");
            writeRow(sheet, 0, "", "资金来源", "", "合计数");
            List<BizBudgetSourceItem> sources = vo.getSources();
            for (int i = 0; i < sources.size(); i++) {
                sheet.getRow(0).createCell(4 + i).setCellValue(sources.get(i).getSourceName());
            }
            writeSourceRow(sheet, 1, "预算安排（万元）", "五年项目总预算", sources, "fiveYearTotal");
            writeSourceRow(sheet, 2, "", year + "年可使用资金", sources, "available");
            writeSourceRow(sheet, 3, "", year + "年预算安排", sources, "annualPlan");
            writeSourceRow(sheet, 4, "", "上年结转结余资金", sources, "carryover");
            writeSourceRow(sheet, 5, "", "到位数", sources, "arrived");

            int base = 7;
            writeRow(sheet, base, "资金支持方向", "改革任务", "", "合计数");
            List<BizBudgetTaskItem> tasks = vo.getTasks();
            for (int i = 0; i < tasks.size(); i++) {
                sheet.getRow(base).createCell(4 + i).setCellValue(tasks.get(i).getTaskIndex() + " " + tasks.get(i).getTaskName());
            }
            writeTaskRow(sheet, base + 1, "", "金额（万元）", tasks, "amount");
            writeTaskRow(sheet, base + 2, "支出经济分类", "其中：商品和服务支出", tasks, "goods");
            writeTaskRow(sheet, base + 3, "", "资本性支出", tasks, "capital");
            writeTaskRow(sheet, base + 4, "", "其他支出", tasks, "other");
            writeTaskTextRow(sheet, base + 5, "资金使用有效性", "指标名称", tasks, "coreOutput");
            writeTaskTextRow(sheet, base + 6, "", "项目期满的目标值", tasks, "targetValue");
            writeTaskTextRow(sheet, base + 7, "", "截至" + year + "年年末目标累计实现情况", tasks, "achieved");

            String filename = "预算数据-" + year + "年" + month + "月.xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8.name()) + "\"");
            workbook.write(response.getOutputStream());
        }
    }

    private BudgetSheetVO toVO(BizBudgetSheet sheet) {
        BudgetSheetVO vo = new BudgetSheetVO();
        vo.setSheet(sheet);
        vo.setSources(budgetMapper.getSources(sheet.getSheetId()));
        vo.setTasks(budgetMapper.getTasks(sheet.getSheetId()));
        return vo;
    }

    private void validateAdmin(Long userId) {
        SysUser user = userId == null ? null : sysMapper.getUserById(userId);
        if (user == null || !"0".equals(user.getRole())) {
            throw new RuntimeException("仅管理员可以操作预算");
        }
    }

    private void validateYearMonth(Integer year, Integer month) {
        if (year == null || year < 2000 || year > 2100) {
            throw new RuntimeException("请输入有效年份");
        }
        if (month == null || month < 1 || month > 12) {
            throw new RuntimeException("请输入有效月份");
        }
    }

    private void validateExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("预算文件不能为空");
        }
        String name = file.getOriginalFilename();
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".xls") && !lower.endsWith(".xlsx")) {
            throw new RuntimeException("仅支持 .xls 或 .xlsx 文件");
        }
    }

    private Long saveFile(MultipartFile file, Long userId) {
        try {
            FileUploadDTO uploaded = FileUploadUtil.upload(file);
            SysFile sysFile = new SysFile();
            sysFile.setFileName(uploaded.getFilename());
            sysFile.setFilePath(uploaded.getFilepath());
            sysFile.setFileUrl(uploaded.getFilepath());
            String original = file.getOriginalFilename();
            String suffix = original == null || !original.contains(".") ? "" : original.substring(original.lastIndexOf(".") + 1);
            sysFile.setFileSuffix(suffix);
            sysFile.setFileSize(file.getSize());
            sysFile.setUploadBy(userId);
            sysFile.setIsDelete(0);
            sysFile.setUploadTime(new Date());
            sysMapper.uploadFile(sysFile);
            return sysFile.getFileId();
        } catch (Exception e) {
            throw new RuntimeException("预算文件保存失败：" + e.getMessage(), e);
        }
    }

    private ParsedBudget parse(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            ParsedBudget parsed = new ParsedBudget();
            parsed.sources = parseSources(sheet, evaluator);
            parsed.tasks = parseTasks(sheet, evaluator);
            if (parsed.sources.isEmpty()) {
                throw new RuntimeException("未识别到资金来源区域");
            }
            if (parsed.tasks.isEmpty()) {
                throw new RuntimeException("未识别到改革任务区域");
            }
            return parsed;
        } catch (IOException e) {
            throw new RuntimeException("预算文件读取失败：" + e.getMessage(), e);
        }
    }

    private List<BizBudgetSourceItem> parseSources(Sheet sheet, FormulaEvaluator evaluator) {
        int headerRow = findRowContains(sheet, evaluator, "资金来源");
        int totalCol = findCellContains(sheet.getRow(headerRow), evaluator, "合计数");
        Map<String, Integer> cols = sourceColumns(sheet.getRow(headerRow), evaluator, totalCol + 1);
        int totalRow = findRowContains(sheet, evaluator, "五年项目总预算");
        int availableRow = findRowContains(sheet, evaluator, "可使用资金");
        int planRow = findRowContains(sheet, evaluator, "预算安排", totalRow + 1);
        int carryRow = findRowContains(sheet, evaluator, "上年结转结余资金");
        int arrivedRow = findRowContains(sheet, evaluator, "到位数");
        List<BizBudgetSourceItem> result = new ArrayList<>();
        int order = 1;
        for (Map.Entry<String, Integer> entry : cols.entrySet()) {
            BizBudgetSourceItem item = new BizBudgetSourceItem();
            item.setSourceKey(sourceKey(entry.getKey()));
            item.setSourceName(entry.getKey());
            item.setDisplayOrder(order++);
            int col = entry.getValue();
            item.setFiveYearTotal(numberAt(sheet, totalRow, col, evaluator));
            item.setAvailable(numberAt(sheet, availableRow, col, evaluator));
            item.setAnnualPlan(numberAt(sheet, planRow, col, evaluator));
            item.setCarryover(numberAt(sheet, carryRow, col, evaluator));
            item.setArrived(numberAt(sheet, arrivedRow, col, evaluator));
            result.add(item);
        }
        return result;
    }

    private List<BizBudgetTaskItem> parseTasks(Sheet sheet, FormulaEvaluator evaluator) {
        int taskHeaderRow = findRowContains(sheet, evaluator, "改革任务");
        int totalCol = findCellContains(sheet.getRow(taskHeaderRow), evaluator, "合计数");
        Map<Integer, String> taskCols = taskColumns(sheet.getRow(taskHeaderRow), evaluator, totalCol + 1);
        int amountRow = findRowContains(sheet, evaluator, "金额");
        int goodsRow = findRowContains(sheet, evaluator, "商品和服务支出");
        int capitalRow = findRowContains(sheet, evaluator, "资本性支出");
        int otherRow = findRowContains(sheet, evaluator, "其他支出");
        int outputRow = findRowContains(sheet, evaluator, "指标名称");
        int targetRow = findRowContains(sheet, evaluator, "项目期满的目标值");
        int achievedRow = findRowContains(sheet, evaluator, "目标累计实现情况");
        List<BizBudgetTaskItem> result = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : taskCols.entrySet()) {
            int col = entry.getKey();
            TaskName taskName = parseTaskName(entry.getValue(), result.size() + 1);
            BizBudgetTaskItem item = new BizBudgetTaskItem();
            item.setTaskIndex(taskName.index);
            item.setTaskName(taskName.name);
            item.setAmount(numberAt(sheet, amountRow, col, evaluator));
            item.setGoods(numberAt(sheet, goodsRow, col, evaluator));
            item.setCapital(numberAt(sheet, capitalRow, col, evaluator));
            item.setOther(numberAt(sheet, otherRow, col, evaluator));
            item.setCoreOutput(textAt(sheet, outputRow, col, evaluator));
            item.setTargetValue(textAt(sheet, targetRow, col, evaluator));
            item.setAchieved(textAt(sheet, achievedRow, col, evaluator));
            result.add(item);
        }
        return result;
    }

    private int findRowContains(Sheet sheet, FormulaEvaluator evaluator, String keyword) {
        return findRowContains(sheet, evaluator, keyword, 0);
    }

    private int findRowContains(Sheet sheet, FormulaEvaluator evaluator, String keyword, int startRow) {
        for (int r = Math.max(0, startRow); r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            for (Cell cell : row) {
                if (normalize(text(cell, evaluator)).contains(normalize(keyword))) {
                    return r;
                }
            }
        }
        throw new RuntimeException("未找到预算表标题：" + keyword);
    }

    private int findCellContains(Row row, FormulaEvaluator evaluator, String keyword) {
        if (row == null) {
            throw new RuntimeException("未找到预算表标题：" + keyword);
        }
        for (Cell cell : row) {
            if (normalize(text(cell, evaluator)).contains(normalize(keyword))) {
                return cell.getColumnIndex();
            }
        }
        throw new RuntimeException("未找到预算表标题：" + keyword);
    }

    private Map<String, Integer> sourceColumns(Row row, FormulaEvaluator evaluator, int startCol) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (int c = startCol; c < row.getLastCellNum(); c++) {
            String value = text(row.getCell(c), evaluator);
            if (value.contains("投入资金") || value.contains("支持资金") || value.contains("自筹资金")) {
                result.put(value, c);
            }
        }
        return result;
    }

    private Map<Integer, String> taskColumns(Row row, FormulaEvaluator evaluator, int startCol) {
        Map<Integer, String> result = new LinkedHashMap<>();
        for (int c = startCol; c < row.getLastCellNum(); c++) {
            String value = text(row.getCell(c), evaluator);
            if (value.matches("^\\d+\\s+.*")) {
                result.put(c, value);
            }
        }
        return result;
    }

    private BigDecimal numberAt(Sheet sheet, int rowIndex, int colIndex, FormulaEvaluator evaluator) {
        String raw = textAt(sheet, rowIndex, colIndex, evaluator)
                .replace(",", "")
                .replace("万元", "")
                .replace("——", "")
                .trim();
        if (raw.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String textAt(Sheet sheet, int rowIndex, int colIndex, FormulaEvaluator evaluator) {
        Row row = sheet.getRow(rowIndex);
        return row == null ? "" : text(row.getCell(colIndex), evaluator);
    }

    private String text(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.FORMULA) {
            return formatter.formatCellValue(cell, evaluator).trim();
        }
        return formatter.formatCellValue(cell).trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").replace("（", "(").replace("）", ")");
    }

    private String sourceKey(String sourceName) {
        if (sourceName.contains("中央")) return "central";
        if (sourceName.contains("地方")) return "local";
        if (sourceName.contains("举办方")) return "sponsor";
        if (sourceName.contains("行业") || sourceName.contains("企业")) return "enterprise";
        if (sourceName.contains("学校")) return "school";
        return "source";
    }

    private TaskName parseTaskName(String raw, int fallbackIndex) {
        String value = raw == null ? "" : raw.trim();
        String[] parts = value.split("\\s+", 2);
        TaskName result = new TaskName();
        if (parts.length == 2 && parts[0].matches("\\d+")) {
            result.index = Integer.parseInt(parts[0]);
            result.name = parts[1].trim();
        } else {
            result.index = fallbackIndex;
            result.name = value;
        }
        return result;
    }

    private void writeRow(Sheet sheet, int rowIndex, String... values) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }

    private void writeSourceRow(Sheet sheet, int rowIndex, String left, String label, List<BizBudgetSourceItem> sources, String field) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(left);
        row.createCell(1).setCellValue(label);
        java.util.function.Function<BizBudgetSourceItem, BigDecimal> getter = sourceValueGetter(field);
        row.createCell(3).setCellValue(sources.stream().map(getter).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
        for (int i = 0; i < sources.size(); i++) {
            BigDecimal value = getter.apply(sources.get(i));
            row.createCell(4 + i).setCellValue(value == null ? 0 : value.doubleValue());
        }
    }

    private java.util.function.Function<BizBudgetSourceItem, BigDecimal> sourceValueGetter(String field) {
        return switch (field) {
            case "fiveYearTotal" -> BizBudgetSourceItem::getFiveYearTotal;
            case "available" -> BizBudgetSourceItem::getAvailable;
            case "annualPlan" -> BizBudgetSourceItem::getAnnualPlan;
            case "carryover" -> BizBudgetSourceItem::getCarryover;
            case "arrived" -> BizBudgetSourceItem::getArrived;
            default -> item -> BigDecimal.ZERO;
        };
    }

    private void writeTaskRow(Sheet sheet, int rowIndex, String left, String label, List<BizBudgetTaskItem> tasks, String field) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(left);
        row.createCell(1).setCellValue(label);
        row.createCell(3).setCellValue(tasks.stream().map(taskValueGetter(field)).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
        for (int i = 0; i < tasks.size(); i++) {
            BigDecimal value = taskValueGetter(field).apply(tasks.get(i));
            row.createCell(4 + i).setCellValue(value == null ? 0 : value.doubleValue());
        }
    }

    private java.util.function.Function<BizBudgetTaskItem, BigDecimal> taskValueGetter(String field) {
        return switch (field) {
            case "amount" -> BizBudgetTaskItem::getAmount;
            case "goods" -> BizBudgetTaskItem::getGoods;
            case "capital" -> BizBudgetTaskItem::getCapital;
            case "other" -> BizBudgetTaskItem::getOther;
            default -> item -> BigDecimal.ZERO;
        };
    }

    private void writeTaskTextRow(Sheet sheet, int rowIndex, String left, String label, List<BizBudgetTaskItem> tasks, String field) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(left);
        row.createCell(1).setCellValue(label);
        row.createCell(3).setCellValue("——");
        for (int i = 0; i < tasks.size(); i++) {
            BizBudgetTaskItem item = tasks.get(i);
            String value = switch (field) {
                case "coreOutput" -> item.getCoreOutput();
                case "targetValue" -> item.getTargetValue();
                case "achieved" -> item.getAchieved();
                default -> "";
            };
            row.createCell(4 + i).setCellValue(value == null ? "" : value);
        }
    }

    private static class ParsedBudget {
        private List<BizBudgetSourceItem> sources = new ArrayList<>();
        private List<BizBudgetTaskItem> tasks = new ArrayList<>();
    }

    private static class TaskName {
        private Integer index;
        private String name;
    }
}
