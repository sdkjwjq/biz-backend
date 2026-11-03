package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entity.BizBudgetSheet;
import org.example.entity.BizBudgetSourceItem;
import org.example.entity.BizBudgetTaskItem;
import org.example.entity.BizBudgetImportVersion;
import org.example.entity.SysFile;
import org.example.entity.SysUser;
import org.example.entity.dto.FileUploadDTO;
import org.example.entity.vo.BudgetSheetVO;
import org.example.entity.vo.BudgetVersionDetailVO;
import org.example.entity.vo.BudgetVersionVO;
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
import java.util.Comparator;
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

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CoreIndicatorService coreIndicators;
    @Autowired
    private LeadershipAccessService budgetAccess;
    @Autowired private PermissionService permissions;

    private final DataFormatter formatter = new DataFormatter(Locale.CHINA);

    @Transactional(readOnly = true)
    public BudgetSheetVO getBudget(Integer year, Integer month, String section) {
        validateYearMonth(year, month);
        String part = normalizeSection(section);
        BizBudgetSheet sheet = budgetMapper.getSheet(year, month, part);
        if (sheet == null) {
            BudgetSheetVO empty = new BudgetSheetVO();
            BizBudgetSheet emptySheet = new BizBudgetSheet();
            emptySheet.setYear(year);
            emptySheet.setMonth(month);
            emptySheet.setSection(part);
            emptySheet.setLocked(0);
            emptySheet.setIsDelete(0);
            empty.setSheet(emptySheet);
            return attachIndicators(empty,year,month);
        }
        BudgetSheetVO current = toVO(sheet);
        return attachIndicators(current,year,month);
    }

    /** 预算表分区：annual=年度预算，execution=年度预算执行。 */
    private String normalizeSection(String section) {
        if(section==null||section.isBlank())return "annual";
        if(!java.util.Set.of("annual","execution").contains(section))throw new AdminManagementException(400,"预算分区必须为年度预算或年度预算执行");
        return section;
    }

    private BudgetSheetVO attachIndicators(BudgetSheetVO current,Integer year,Integer month) {
        attachFiveYearTotal(current);
        boolean execution=current.getSheet()!=null&&"execution".equals(current.getSheet().getSection());
        // 年度预算执行展示该年度最近一次归档（不再限定月份）；年度预算仍按所选月份取快照，避免引用未来月份。
        int archiveMonth=execution?12:(month==null?12:month);
        Map<String,Long> lookup=coreIndicators.indicatorLookup();
        for (var task : current.getTasks()) {
            if(task.getTaskIndex()==null)continue;
            Long indicator=indicatorOf(task,lookup);
            if(indicator==null)continue;
            Map<String,Object> effective=coreIndicators.effective(indicator,year,archiveMonth);
            if(effective!=null){
                Map<String,Object> row=new java.util.LinkedHashMap<>(effective);
                row.put("yearValue",yearValue(effective));
                current.getArchivedIndicators().put(task.getTaskIndex(),row);
            }
            if(execution){
                Map<String,Object> latest=coreIndicators.effectiveLatest(indicator);
                if(latest!=null)current.getCumulativeIndicators().put(task.getTaskIndex(),latest);
            }
        }
        return current;
    }

    /** 当年完成值 = 本次累计值 − 上年末累计值（与核心指标页面口径一致）。 */
    private java.math.BigDecimal yearValue(Map<String,Object> effective){
        java.math.BigDecimal actual=decimal(effective.get("actualValue"));
        if(actual==null)return null;
        java.math.BigDecimal previous=decimal(effective.get("previousYearValue"));
        return actual.subtract(previous==null?java.math.BigDecimal.ZERO:previous);
    }

    private java.math.BigDecimal decimal(Object value){
        if(value==null)return null;
        try{return new java.math.BigDecimal(String.valueOf(value));}catch(NumberFormatException e){return null;}
    }

    /** 预算任务按「指标名称」列关联核心指标；名称无法识别时沿用任务序号，保持旧数据可见。 */
    private Long indicatorOf(BizBudgetTaskItem task,Map<String,Long> lookup){
        String label=task.getCoreOutput();
        if(label!=null&&!label.isBlank()){
            String normalized=CoreIndicatorService.normalizeLabel(label);
            Long id=normalized.isEmpty()?null:lookup.get("name:"+normalized);
            if(id!=null)return id;
            java.util.regex.Matcher matcher=java.util.regex.Pattern.compile("^\\s*(\\d+(?:\\.\\d+)+)").matcher(label);
            if(matcher.find()){
                Long byCode=lookup.get("code:"+matcher.group(1));
                if(byCode!=null)return byCode;
            }
        }
        return task.getTaskIndex()==null?null:task.getTaskIndex().longValue();
    }

    /** 五年项目总预算为全系统一份：按资金来源取「总预算」页面的金额合计（各来源之和即合计列）。 */
    private void attachFiveYearTotal(BudgetSheetVO current){
        if(current.getSheet()==null||current.getSources()==null||current.getSources().isEmpty())return;
        java.util.Map<String,java.math.BigDecimal> totals=new java.util.HashMap<>();
        for(var row:budgetMapper.totalBySource()){
            java.math.BigDecimal amount=decimal(row.get("amount"));
            if(amount==null)continue;
            totals.merge(sourceKey(String.valueOf(row.get("source"))),amount,java.math.BigDecimal::add);
        }
        for(var source:current.getSources()){
            var total=totals.get(source.getSourceKey());
            if(total!=null)source.setFiveYearTotal(total);
        }
    }

    // Compatibility for existing internal callers and regression fixtures.
    @Transactional(readOnly=true)
    public BudgetSheetVO getBudget(Integer year,Integer month){return getBudget(year,month,"annual");}
    @Transactional
    public BudgetSheetVO importBudget(Integer year,Integer month,MultipartFile file,Long userId){return importBudget(year,month,"annual",file,userId);}
    public List<BudgetVersionVO> getVersions(Integer year,Integer month,Long userId){return getVersions(year,month,"annual",userId);}
    @Transactional
    public BudgetSheetVO updateLock(Integer year,Integer month,Boolean locked,Long userId){return updateLock(year,month,"annual",locked,userId);}
    public void exportBudget(Integer year,Integer month,HttpServletResponse response)throws IOException{exportBudget(year,month,"annual",response);}

    @Transactional
    public BudgetSheetVO importBudget(Integer year, Integer month, String section, MultipartFile file, Long userId) {
        validateEditor(userId);
        validateYearMonth(year, month);
        validateExcel(file);
        String part = normalizeSection(section);

        ParsedBudget parsed = parse(file);
        Long fileId = saveFile(file, userId);
        Date now = new Date();

        BizBudgetSheet sheet = budgetMapper.getSheetForUpdate(year, month, part);
        if (sheet == null) {
            sheet = new BizBudgetSheet();
            sheet.setYear(year);
            sheet.setMonth(month);
            sheet.setSection(part);
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
        }

        preserveOmittedTaskFields(sheet, parsed);
        replaceItems(sheet.getSheetId(), parsed.sources, parsed.tasks);
        BudgetSheetVO current = toVO(sheet);
        BizBudgetImportVersion version = saveVersion(sheet, current, "IMPORT", null,
                fileId, originalFileName(file), userId, now, null);

        BusinessLogUtil.info("预算导入",
                "result", "成功",
                "userId", userId,
                "year", year,
                "month", month,
                "sheetId", sheet.getSheetId(),
                "fileId", fileId,
                "versionId", version.getVersionId(),
                "versionNo", version.getVersionNo(),
                "sourceCount", parsed.sources.size(),
                "taskCount", parsed.tasks.size());
        return attachIndicators(current,year,month);
    }

    public List<BudgetVersionVO> getVersions(Integer year, Integer month, String section, Long userId) {
        if(permissions.enforced(userId))permissions.require(userId,"budget.versions");else validateEditor(userId);
        validateYearMonth(year, month);
        List<BudgetVersionVO> versions = budgetMapper.getVersions(year, month, normalizeSection(section));
        for (int index = 0; index < versions.size(); index++) {
            versions.get(index).setCurrent(index == 0);
        }
        return versions;
    }

    public BudgetVersionDetailVO getVersion(Long versionId, Long userId) {
        if(permissions.enforced(userId))permissions.require(userId,"budget.versions");else validateEditor(userId);
        BizBudgetImportVersion version = requireVersion(versionId);
        BudgetVersionVO metadata = budgetMapper.getVersionVO(versionId);
        metadata.setCurrent(isLatest(version));
        return new BudgetVersionDetailVO(metadata, snapshot(version));
    }

    @Transactional
    public BudgetSheetVO restoreVersion(Long versionId, String remark, Long userId) {
        if(permissions.enforced(userId))permissions.require(userId,"budget.restore");else validateAdmin(userId);
        String reason = normalizeRestoreRemark(remark);
        BizBudgetImportVersion source = requireVersion(versionId);
        BizBudgetSheet sourceSheet = budgetMapper.getSheetById(source.getSheetId());
        String part = normalizeSection(sourceSheet == null ? null : sourceSheet.getSection());
        BizBudgetSheet sheet = budgetMapper.getSheetForUpdate(source.getYear(), source.getMonth(), part);
        if (sheet == null || !Objects.equals(sheet.getSheetId(), source.getSheetId())) {
            throw new RuntimeException("当前预算表不存在，无法恢复历史版本");
        }
        if (isLatest(source)) {
            throw new RuntimeException("当前版本无需恢复");
        }

        BudgetSheetVO old = snapshot(source);
        if (old.getSources() == null || old.getSources().isEmpty() || old.getTasks() == null || old.getTasks().isEmpty()) {
            throw new RuntimeException("历史版本数据不完整，无法恢复");
        }

        Date now = new Date();
        sheet.setLocked(1);
        sheet.setFileId(source.getFileId());
        sheet.setImportBy(userId);
        sheet.setImportTime(now);
        sheet.setUpdateTime(now);
        sheet.setIsDelete(0);
        budgetMapper.updateSheet(sheet);
        replaceItems(sheet.getSheetId(), old.getSources(), old.getTasks());

        BudgetSheetVO current = toVO(sheet);
        BizBudgetImportVersion restored = saveVersion(sheet, current, "RESTORE", source.getVersionId(),
                source.getFileId(), source.getFileName(), userId, now, reason);
        BusinessLogUtil.info("预算版本恢复",
                "result", "成功",
                "userId", userId,
                "year", sheet.getYear(),
                "month", sheet.getMonth(),
                "sourceVersionId", source.getVersionId(),
                "newVersionId", restored.getVersionId(),
                "newVersionNo", restored.getVersionNo());
        return attachIndicators(current,sheet.getYear(),sheet.getMonth());
    }

    /** 把某个分区的预算复制成另一个分区（例如年度预算 → 年度预算执行），两者之后各自独立。 */
    @Transactional
    public BudgetSheetVO copySection(Integer year, Integer month, String from, String to, Long userId) {
        validateEditor(userId);
        validateYearMonth(year, month);
        String source = normalizeSection(from), target = normalizeSection(to);
        if (source.equals(target)) throw new AdminManagementException(400, "源与目标分区不能相同");
        BizBudgetSheet sourceSheet = budgetMapper.getSheetForUpdate(year, month, source);
        if (sourceSheet == null) throw new AdminManagementException(409, "源预算表尚未导入，无法复制");
        BudgetSheetVO sourceVO = toVO(sourceSheet);
        BizBudgetSheet targetSheet = budgetMapper.getSheetForUpdate(year, month, target);
        Date now = new Date();
        if (targetSheet == null) {
            targetSheet = new BizBudgetSheet();
            targetSheet.setYear(year);
            targetSheet.setMonth(month);
            targetSheet.setSection(target);
            targetSheet.setLocked(1);
            targetSheet.setImportBy(userId);
            targetSheet.setImportTime(now);
            targetSheet.setUpdateTime(now);
            targetSheet.setIsDelete(0);
            budgetMapper.insertSheet(targetSheet);
        } else if (Integer.valueOf(1).equals(targetSheet.getLocked())) {
            throw new AdminManagementException(409, "目标预算表已锁定，请先解锁");
        } else {
            targetSheet.setUpdateTime(now);
            budgetMapper.updateSheet(targetSheet);
        }
        replaceItems(targetSheet.getSheetId(), sourceVO.getSources(), sourceVO.getTasks());
        BudgetSheetVO current = toVO(targetSheet);
        saveVersion(targetSheet, current, "IMPORT", null, null, null, userId, now, "从" + ("annual".equals(source) ? "年度预算" : "年度预算执行") + "复制");
        return attachIndicators(current, year, month);
    }

    @Transactional
    public BudgetSheetVO updateLock(Integer year, Integer month, String section, Boolean locked, Long userId) {
        if(permissions.enforced(userId))permissions.require(userId,"budget.lock");else validateAdmin(userId);
        validateYearMonth(year, month);
        String part = normalizeSection(section);
        BizBudgetSheet sheet = budgetMapper.getSheetForUpdate(year, month, part);
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
        return getBudget(year, month, part);
    }

    /** Only editable cells are patched; imported precision and all other columns remain intact. */
    @Transactional
    public BudgetSheetVO saveBudget(com.fasterxml.jackson.databind.JsonNode input, Long actor) {
        validateEditor(actor);
        allowedKeys(input,java.util.Set.of("year","month","section","baseVersionNo","sources","tasks","lockAfterSave"));
        int year=requiredInt(input,"year"),month=requiredInt(input,"month"),base=requiredInt(input,"baseVersionNo");
        String part=normalizeSection(input.has("section")?input.get("section").asText():null);
        if(year<2025||year>2029||month<1||month>12||base<0)throw new AdminManagementException(400,"预算年月或版本不正确");
        if(input.has("lockAfterSave")&&!input.get("lockAfterSave").isBoolean())throw new AdminManagementException(400,"锁定选项不正确");
        boolean lock=input.path("lockAfterSave").asBoolean(false);
        if(lock){if(permissions.enforced(actor))permissions.require(actor,"budget.lock");else validateAdmin(actor);}
        var sheet=budgetMapper.getSheetForUpdate(year,month,part);
        if(sheet==null)throw new AdminManagementException(409,"请先导入该月预算");
        if(Integer.valueOf(1).equals(sheet.getLocked()))throw new AdminManagementException(409,"预算已锁定，请重新加载后再编辑");
        if(budgetMapper.nextVersionNo(sheet.getSheetId())-1!=base)throw new AdminManagementException(409,"预算版本已变化，请保留草稿并重新加载核对");
        var current=toVO(sheet);boolean changed=false;
        boolean execution="execution".equals(part);
        java.util.Set<String> sourceKeys=new java.util.HashSet<>();java.util.Set<Integer> taskKeys=new java.util.HashSet<>();
        for(var edit:edits(input,"sources")){
            allowedKeys(edit,execution?java.util.Set.of("sourceKey","annualPlan","available","carryover","arrived"):java.util.Set.of("sourceKey","annualPlan","arrived"));
            if(!edit.path("sourceKey").isTextual()||!sourceKeys.add(edit.path("sourceKey").asText()))throw new AdminManagementException(400,"资金来源缺失或重复");
            var item=current.getSources().stream().filter(v->Objects.equals(v.getSourceKey(),edit.path("sourceKey").asText())).findFirst().orElseThrow(()->new AdminManagementException(400,"资金来源不存在"));
            if(edit.has("annualPlan")){var value=editableAmount(edit.get("annualPlan"));changed|=!sameAmount(item.getAnnualPlan(),value);item.setAnnualPlan(value);}
            if(execution&&edit.has("available")){var value=editableAmount(edit.get("available"));changed|=!sameAmount(item.getAvailable(),value);item.setAvailable(value);}
            if(execution&&edit.has("carryover")){var value=editableAmount(edit.get("carryover"));changed|=!sameAmount(item.getCarryover(),value);item.setCarryover(value);}
            if(edit.has("arrived")){var value=editableAmount(edit.get("arrived"));changed|=!sameAmount(item.getArrived(),value);item.setArrived(value);}
            budgetMapper.updateEditableSource(item);
        }
        for(var edit:edits(input,"tasks")){
            allowedKeys(edit,execution?java.util.Set.of("taskIndex","amount","budgetAmount","goods","capital","other"):java.util.Set.of("taskIndex","goods","capital","other"));int index=requiredInt(edit,"taskIndex");
            if(!taskKeys.add(index))throw new AdminManagementException(400,"改革任务重复");
            var item=current.getTasks().stream().filter(v->Objects.equals(v.getTaskIndex(),index)).findFirst().orElseThrow(()->new AdminManagementException(400,"改革任务不存在"));
            if(execution&&edit.has("amount")){var value=editableAmount(edit.get("amount"));changed|=!sameAmount(item.getAmount(),value);item.setAmount(value);}
            if(execution&&edit.has("budgetAmount")){var value=editableAmount(edit.get("budgetAmount"));changed|=!sameAmount(item.getBudgetAmount(),value);item.setBudgetAmount(value);}
            if(edit.has("goods")){var value=editableAmount(edit.get("goods"));changed|=!sameAmount(item.getGoods(),value);item.setGoods(value);}
            if(edit.has("capital")){var value=editableAmount(edit.get("capital"));changed|=!sameAmount(item.getCapital(),value);item.setCapital(value);}
            if(edit.has("other")){var value=editableAmount(edit.get("other"));changed|=!sameAmount(item.getOther(),value);item.setOther(value);}
            budgetMapper.updateEditableTask(item);
        }
        if(lock){budgetMapper.updateLock(sheet.getSheetId(),1);sheet.setLocked(1);}
        if(changed){
            sheet.setUpdateTime(new Date());
            budgetMapper.updateEditTime(sheet.getSheetId(),sheet.getUpdateTime());
            String name=sheet.getFileId()==null?null:budgetMapper.getVersions(year,month,part).stream().map(BudgetVersionVO::getFileName).filter(Objects::nonNull).findFirst().orElse(null);
            saveVersion(sheet,current,"EDIT",null,sheet.getFileId(),name,actor,new Date(),"在线编辑预算；原文件为导入来源附件");
            BusinessLogUtil.info("预算在线保存","userId",actor,"year",year,"month",month,"result","成功");
        }
        return attachIndicators(toVO(sheet),year,month);
    }

    private static void allowedKeys(com.fasterxml.jackson.databind.JsonNode node,java.util.Set<String> allowed){
        if(node==null||!node.isObject())throw new AdminManagementException(400,"预算修改内容不正确");
        node.fieldNames().forEachRemaining(key->{if(!allowed.contains(key))throw new AdminManagementException(400,"不允许修改字段："+key);});
    }
    private static int requiredInt(com.fasterxml.jackson.databind.JsonNode node,String key){
        var value=node.get(key);if(value==null||!value.isIntegralNumber()||!value.canConvertToInt())throw new AdminManagementException(400,"缺少或无效参数："+key);return value.intValue();
    }
    private static Iterable<com.fasterxml.jackson.databind.JsonNode> edits(com.fasterxml.jackson.databind.JsonNode node,String key){
        var value=node.get(key);if(value==null)return java.util.List.of();if(!value.isArray()||value.size()>500)throw new AdminManagementException(400,"预算修改列表不正确");return value;
    }
    private static BigDecimal editableAmount(com.fasterxml.jackson.databind.JsonNode value){
        if(value==null||value.isNull()||!value.isNumber())throw new AdminManagementException(400,"金额须为非空数字");
        var amount=value.decimalValue();if(amount.signum()<0||amount.scale()>2||amount.compareTo(new BigDecimal("9999999999999999.99"))>0)throw new AdminManagementException(400,"金额须为非负数，最多2位小数且不超过16位整数");return amount;
    }
    private static boolean sameAmount(BigDecimal first,BigDecimal second){return first!=null&&first.compareTo(second)==0;}

    public void exportBudget(Integer year, Integer month, String section, HttpServletResponse response) throws IOException {
        BudgetSheetVO vo = getBudget(year, month, section);
        boolean execution="execution".equals(section);
        String tableName=year+"年"+(execution?"年度预算执行":"年度预算");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(tableName);
            writeRow(sheet, 0, "", execution ? "采集信息的维度" : "资金来源", "", "合计数");
            List<BizBudgetSourceItem> sources = vo.getSources();
            for (int i = 0; i < sources.size(); i++) {
                sheet.getRow(0).createCell(4 + i).setCellValue(sources.get(i).getSourceName());
            }
            writeSourceRow(sheet, 1, "预算安排（万元）", "五年项目总预算", sources, "fiveYearTotal");
            writeSourceRow(sheet, 2, "", year + "年可使用资金", sources, "available");
            writeSourceRow(sheet, 3, "", year + "年预算安排", sources, "annualPlan");
            writeSourceRow(sheet, 4, "", "上年结转结余资金", sources, "carryover");
            writeSourceRow(sheet, 5, "", execution ? "到位数（万元）" : "到位数", sources, "arrived");

            int base = 7;
            writeRow(sheet, base, "资金支持方向", execution ? "支出经济分类/用途（改革任务）" : "改革任务", "", "合计数");
            List<BizBudgetTaskItem> tasks = vo.getTasks();
            for(var task:tasks) {
                Map<String,Object> archived=vo.getArchivedIndicators().get(task.getTaskIndex());
                task.setAchieved(archived==null?"":execution?String.valueOf(archived.get("actualValue")):archived.get("actualValue")+"（"+year+"年"+archived.get("month")+"月归档）");
                if(archived!=null) {
                    // 指标名称以预算表填写内容为准，仅在为空时用归档指标名称补全。
                    if(task.getCoreOutput()==null||task.getCoreOutput().isBlank())task.setCoreOutput(String.valueOf(archived.get("indicatorName")));
                    task.setTargetValue(archived.get("targetValue")==null?"":String.valueOf(archived.get("targetValue")));
                }
            }
            for (int i = 0; i < tasks.size(); i++) {
                sheet.getRow(base).createCell(4 + i).setCellValue(tasks.get(i).getTaskIndex() + " " + tasks.get(i).getTaskName());
            }
            int row=base+1;
            if(execution)writeTaskRow(sheet,row++,"","预算数（万元）",tasks,"budgetAmount");
            writeTaskRow(sheet,row++,"",execution ? "总额（万元）" : "金额（万元）",tasks,"amount");
            writeTaskRow(sheet,row++,execution?"":"支出经济分类",execution?"其中：商品和服务支出":"商品和服务支出",tasks,"goods");
            writeTaskRow(sheet,row++,"","资本性支出",tasks,"capital");
            writeTaskRow(sheet,row++,"","其他支出",tasks,"other");
            // 「资金使用有效性」：年度预算执行保留 5 行（截至当前累计完成值 + 当年完成值 + 工作说明），年度预算不展示
            if (execution) {
                writeTaskTextRow(sheet,row++,"资金使用有效性","指标名称",tasks,"coreOutput");
                writeTaskTextRow(sheet,row++,"","项目期满的目标值",tasks,"targetValue");
                writeIndicatorRow(sheet,row++,"","截至当前累计完成值",tasks,vo.getCumulativeIndicators(),"actualValue");
                writeIndicatorRow(sheet,row++,"",""+year+"年完成值",tasks,vo.getArchivedIndicators(),"yearValue");
                java.util.Map<Integer,java.util.Map<String,Object>> comments=new java.util.LinkedHashMap<>();
                for(var task:tasks){
                    java.util.Map<String,Object> archivedRow=vo.getArchivedIndicators().get(task.getTaskIndex());
                    java.util.Map<String,Object> latestRow=vo.getCumulativeIndicators().get(task.getTaskIndex());
                    Object comment=archivedRow==null?null:archivedRow.get("comment");
                    if((comment==null||String.valueOf(comment).isBlank())&&latestRow!=null)comment=latestRow.get("comment");
                    java.util.Map<String,Object> commentCell=new java.util.LinkedHashMap<>();
                    if(comment!=null)commentCell.put("comment",comment);
                    comments.put(task.getTaskIndex(),commentCell);
                }
                writeIndicatorRow(sheet,row++,"","工作说明",tasks,comments,"comment");
            }
            // 左侧分组标签纵向合并，和页面表格保持一致的层级
            mergeGroupLabels(sheet, base, execution, row - 1);
            // 统一列宽/换行/边框，保证长任务名与长指标名称完整显示
            styleBudgetSheet(workbook, sheet, base, execution, row - 1, tasks.size());

            String filename = tableName + ".xlsx";
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
        vo.setCurrentVersionNo(budgetMapper.nextVersionNo(sheet.getSheetId())-1);
        return vo;
    }

    private void replaceItems(Long sheetId, List<BizBudgetSourceItem> sources, List<BizBudgetTaskItem> tasks) {
        budgetMapper.deleteSources(sheetId);
        budgetMapper.deleteTasks(sheetId);
        for (BizBudgetSourceItem source : sources) {
            source.setItemId(null);
            source.setSheetId(sheetId);
            budgetMapper.insertSource(source);
        }
        for (BizBudgetTaskItem task : tasks) {
            task.setItemId(null);
            task.setSheetId(sheetId);
            budgetMapper.insertTask(task);
        }
    }

    private BizBudgetImportVersion saveVersion(BizBudgetSheet sheet, BudgetSheetVO budget, String operationType,
                                                Long sourceVersionId, Long fileId, String fileName, Long userId,
                                                Date createTime, String remark) {
        BizBudgetImportVersion version = new BizBudgetImportVersion();
        version.setSheetId(sheet.getSheetId());
        version.setYear(sheet.getYear());
        version.setMonth(sheet.getMonth());
        version.setVersionNo(budgetMapper.nextVersionNo(sheet.getSheetId()));
        budget.setCurrentVersionNo(version.getVersionNo());
        version.setOperationType(operationType);
        version.setSourceVersionId(sourceVersionId);
        version.setFileId(fileId);
        version.setFileName(limit(fileName, 255));
        try {
            version.setSnapshotJson(objectMapper.writeValueAsString(budget));
        } catch (JsonProcessingException error) {
            throw new RuntimeException("预算版本快照保存失败", error);
        }
        version.setCreatedBy(userId);
        version.setCreateTime(createTime);
        version.setRemark(limit(remark, 200));
        version.setIsDelete(0);
        budgetMapper.insertVersion(version);
        return version;
    }

    private BizBudgetImportVersion requireVersion(Long versionId) {
        if (versionId == null || versionId <= 0) {
            throw new RuntimeException("预算版本不存在");
        }
        BizBudgetImportVersion version = budgetMapper.getVersion(versionId);
        if (version == null) {
            throw new RuntimeException("预算版本不存在");
        }
        return version;
    }

    private BudgetSheetVO snapshot(BizBudgetImportVersion version) {
        try {
            BudgetSheetVO budget = objectMapper.readValue(version.getSnapshotJson(), BudgetSheetVO.class);
            if (budget.getSources() == null) budget.setSources(new ArrayList<>());
            if (budget.getTasks() == null) budget.setTasks(new ArrayList<>());
            budget.getSources().sort(Comparator.comparing(item -> item.getDisplayOrder() == null ? 0 : item.getDisplayOrder()));
            budget.getTasks().sort(Comparator.comparing(item -> item.getTaskIndex() == null ? 0 : item.getTaskIndex()));
            BizBudgetSheet sheet = new BizBudgetSheet();
            sheet.setSheetId(version.getSheetId());
            sheet.setYear(version.getYear());
            sheet.setMonth(version.getMonth());
            sheet.setLocked(1);
            sheet.setFileId(version.getFileId());
            sheet.setImportBy(version.getCreatedBy());
            sheet.setImportTime(version.getCreateTime());
            sheet.setIsDelete(0);
            budget.setSheet(sheet);
            return budget;
        } catch (JsonProcessingException error) {
            throw new RuntimeException("预算版本快照读取失败", error);
        }
    }

    private boolean isLatest(BizBudgetImportVersion version) {
        BizBudgetSheet sourceSheet = budgetMapper.getSheetById(version.getSheetId());
        List<BudgetVersionVO> versions = budgetMapper.getVersions(version.getYear(), version.getMonth(), normalizeSection(sourceSheet == null ? null : sourceSheet.getSection()));
        return !versions.isEmpty() && Objects.equals(versions.get(0).getVersionId(), version.getVersionId());
    }

    private String normalizeRestoreRemark(String remark) {
        String value = remark == null ? "" : remark.trim();
        if (value.isEmpty()) throw new RuntimeException("请填写恢复原因");
        if (value.length() > 200) throw new RuntimeException("恢复原因不能超过200字");
        return value;
    }

    private String originalFileName(MultipartFile file) {
        String value = file == null ? null : file.getOriginalFilename();
        return value == null || value.isBlank() ? "预算导入文件" : value.trim();
    }

    private String limit(String value, int max) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.length() <= max ? normalized : normalized.substring(0, max);
    }

    private void validateEditor(Long userId) {
        if (!budgetAccess.canFillBudget(userId)) throw new AdminManagementException(403, "仅管理员、双高办及财务处正常账号可以填报预算");
    }

    private void validateAdmin(Long userId) {
        SysUser user = userId == null ? null : sysMapper.getUserById(userId);
        if (user == null || !"0".equals(user.getRole()) || !"1".equals(user.getStatus())
                || Integer.valueOf(1).equals(user.getIsDelete())) {
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
            parsed.tasks = parseTasks(sheet, evaluator, parsed);
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
        int headerRow = findLabelRow(sheet, evaluator, 0, true, "资金来源", "采集信息的维度");
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

    private List<BizBudgetTaskItem> parseTasks(Sheet sheet, FormulaEvaluator evaluator, ParsedBudget parsed) {
        int header = findRowContains(sheet, evaluator, "改革任务");
        int totalCol = findCellContains(sheet.getRow(header), evaluator, "合计数");
        Map<Integer,String> columns = taskColumns(sheet.getRow(header), evaluator, totalCol + 1);
        int amount = findLabelRow(sheet,evaluator,header+1,true,"金额","总额");
        int goods = findLabelRow(sheet,evaluator,header+1,true,"商品和服务支出","其中：商品和服务支出");
        int capital = findLabelRow(sheet,evaluator,header+1,true,"资本性支出");
        int other = findLabelRow(sheet,evaluator,header+1,true,"其他支出");
        Map<String,Integer> optional = new java.util.LinkedHashMap<>();
        optional.put("budgetAmount",findLabelRow(sheet,evaluator,header+1,false,"预算金额","预算数"));
        optional.put("goodsBudget",findLabelRow(sheet,evaluator,header+1,false,"商品和服务预算"));
        optional.put("capitalBudget",findLabelRow(sheet,evaluator,header+1,false,"资本性预算"));
        optional.put("otherBudget",findLabelRow(sheet,evaluator,header+1,false,"其他预算"));
        optional.put("coreOutput",findLabelRow(sheet,evaluator,header+1,false,"指标名称"));
        optional.put("targetValue",findLabelRow(sheet,evaluator,header+1,false,"项目期满的目标值"));
        optional.put("achieved",findLabelRow(sheet,evaluator,header+1,false,"截至当前累计完成值","目标累计实现情况","最近归档累计实现情况","归档累计实现情况"));
        optional.forEach((key,row)->{if(row<0)parsed.missingTaskFields.add(key);});
        List<BizBudgetTaskItem> result = new ArrayList<>();
        java.util.Set<Integer> indexes = new java.util.HashSet<>();
        for(var entry:columns.entrySet()) {
            int col=entry.getKey();TaskName name=parseTaskName(entry.getValue(),result.size()+1);
            if(!indexes.add(name.index))throw new RuntimeException("改革任务序号重复："+name.index);
            BizBudgetTaskItem item=new BizBudgetTaskItem();item.setTaskIndex(name.index);item.setTaskName(name.name);
            item.setAmount(numberAt(sheet,amount,col,evaluator));item.setGoods(numberAt(sheet,goods,col,evaluator));
            item.setCapital(numberAt(sheet,capital,col,evaluator));item.setOther(numberAt(sheet,other,col,evaluator));
            item.setBudgetAmount(optional.get("budgetAmount")<0?null:numberAt(sheet,optional.get("budgetAmount"),col,evaluator));
            item.setGoodsBudget(optional.get("goodsBudget")<0?null:numberAt(sheet,optional.get("goodsBudget"),col,evaluator));
            item.setCapitalBudget(optional.get("capitalBudget")<0?null:numberAt(sheet,optional.get("capitalBudget"),col,evaluator));
            item.setOtherBudget(optional.get("otherBudget")<0?null:numberAt(sheet,optional.get("otherBudget"),col,evaluator));
            item.setCoreOutput(optional.get("coreOutput")<0?"":textAt(sheet,optional.get("coreOutput"),col,evaluator));
            item.setTargetValue(optional.get("targetValue")<0?"":textAt(sheet,optional.get("targetValue"),col,evaluator));
            item.setAchieved(optional.get("achieved")<0?"":textAt(sheet,optional.get("achieved"),col,evaluator));
            result.add(item);
        }
        return result;
    }

    /** Read labels only in label columns: a long task name must never select a numeric row. */
    private int findLabelRow(Sheet sheet,FormulaEvaluator evaluator,int start,boolean required,String... aliases) {
        for(int r=start;r<=sheet.getLastRowNum();r++) {
            Row row=sheet.getRow(r);if(row==null)continue;
            for(int c=0;c<=2;c++) {
                String label=normalize(text(row.getCell(c),evaluator));
                for(String alias:aliases) {
                    String key=normalize(alias);
                    if(label.equals(key)||label.startsWith(key+"(")||(!java.util.Set.of("金额","总额","预算数","预算金额").contains(key)&&label.endsWith(key)))return r;
                }
            }
        }
        if(required)throw new RuntimeException("未找到预算表标题："+String.join("／",aliases));
        return -1;
    }

    /** Missing exported columns are omitted data, not instructions to erase existing imported metadata. */
    private void preserveOmittedTaskFields(BizBudgetSheet sheet,ParsedBudget parsed) {
        if(parsed.missingTaskFields.isEmpty())return;
        Map<Integer,BizBudgetTaskItem> old=new java.util.HashMap<>();
        for(var item:budgetMapper.getTasks(sheet.getSheetId()))old.put(item.getTaskIndex(),item);
        for(var item:parsed.tasks) {
            var previous=old.get(item.getTaskIndex());if(previous==null)continue;
            for(String field:parsed.missingTaskFields)switch(field) {
                case "budgetAmount" -> item.setBudgetAmount(previous.getBudgetAmount());
                case "goodsBudget" -> item.setGoodsBudget(previous.getGoodsBudget());
                case "capitalBudget" -> item.setCapitalBudget(previous.getCapitalBudget());
                case "otherBudget" -> item.setOtherBudget(previous.getOtherBudget());
                case "coreOutput" -> item.setCoreOutput(previous.getCoreOutput());
                case "targetValue" -> item.setTargetValue(previous.getTargetValue());
                case "achieved" -> item.setAchieved(previous.getAchieved());
                default -> throw new IllegalStateException("Unknown omitted budget field");
            }
        }
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
        var total=row.createCell(3);
        total.setCellValue(tasks.stream().map(taskValueGetter(field)).filter(Objects::nonNull).reduce(BigDecimal.ZERO,BigDecimal::add).doubleValue());
        for (int i = 0; i < tasks.size(); i++) {
            BigDecimal value = taskValueGetter(field).apply(tasks.get(i));
            row.createCell(4+i).setCellValue(value==null?0:value.doubleValue());
        }
    }

    private java.util.function.Function<BizBudgetTaskItem, BigDecimal> taskValueGetter(String field) {
        return switch (field) {
            case "amount" -> BizBudgetTaskItem::getAmount;
            case "goods" -> BizBudgetTaskItem::getGoods;
            case "goodsBudget" -> BizBudgetTaskItem::getGoodsBudget;
            case "budgetAmount" -> BizBudgetTaskItem::getBudgetAmount;
            case "capitalBudget" -> BizBudgetTaskItem::getCapitalBudget;
            case "otherBudget" -> BizBudgetTaskItem::getOtherBudget;
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

    /** 写一行核心指标关联数据（累计完成值/当年完成值/工作说明），按预算任务序号取归档记录字段。 */
    private void writeIndicatorRow(Sheet sheet, int rowIndex, String left, String label, List<BizBudgetTaskItem> tasks, Map<Integer, Map<String,Object>> data, String field) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(left);
        row.createCell(1).setCellValue(label);
        row.createCell(3).setCellValue("——");
        for (int i = 0; i < tasks.size(); i++) {
            Map<String,Object> record = data == null ? null : data.get(tasks.get(i).getTaskIndex());
            Object value = record == null ? null : record.get(field);
            Cell cell = row.createCell(4 + i);
            if (value instanceof Number number) { cell.setCellValue(number.doubleValue()); }
            else { cell.setCellValue(value == null ? "" : String.valueOf(value)); }
        }
    }
    private void mergeGroupLabels(Sheet sheet, int headerRow, boolean execution, int lastRow) {
        // 上半张表：预算安排（万元）纵向合并 4 行
        labelCell(sheet, 1, "预算安排（万元）");
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 4, 0, 0));
        int amountRow = headerRow + (execution ? 2 : 1);
        labelCell(sheet, headerRow, "资金支持方向");
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(headerRow, amountRow, 0, 0));
        int expenseStart = amountRow + 1;
        int expenseCount = 3;
        int expenseEnd = expenseStart + expenseCount - 1;
        labelCell(sheet, expenseStart, "支出经济分类");
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(expenseStart, expenseEnd, 0, 0));
        int effectStart = expenseEnd + 1;
        if (execution) {
            labelCell(sheet, effectStart, "资金使用有效性：选取一个最具代表性的核心产出指标");
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(effectStart, lastRow, 0, 0));
        }
    }

    private void labelCell(Sheet sheet, int rowIndex, String value) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) row = sheet.createRow(rowIndex);
        Cell cell = row.getCell(0);
        if (cell == null) cell = row.createCell(0);
        cell.setCellValue(value);
    }

    private void styleBudgetSheet(Workbook workbook, Sheet sheet, int headerRow, boolean execution, int lastRow, int taskCount) {
        // 导出不加任何装饰样式（无颜色填充、无边框、无加粗），只保留列宽、数字格式与自动换行
        sheet.setColumnWidth(0, 20 * 256);
        sheet.setColumnWidth(1, 30 * 256);
        sheet.setColumnWidth(2, 6 * 256);
        sheet.setColumnWidth(3, 14 * 256);
        for (int i = 0; i < taskCount; i++) sheet.setColumnWidth(4 + i, 40 * 256);

        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        moneyStyle.setAlignment(HorizontalAlignment.CENTER);
        moneyStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle textStyle = workbook.createCellStyle();
        textStyle.setAlignment(HorizontalAlignment.CENTER);
        textStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        textStyle.setWrapText(true);

        for (int r = 0; r <= lastRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) row = sheet.createRow(r);
            for (int c = 0; c < 4 + taskCount; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) cell = row.createCell(c);
                if (cell.getCellType() == CellType.NUMERIC) cell.setCellStyle(moneyStyle);
                else cell.setCellStyle(textStyle);
            }
            fitTextRowHeight(sheet, r, 0, 3 + taskCount);
        }
    }

    /** 按文字长度估算行高（中文字符按2个宽度计），保证工作说明等长文本显示完整。 */
    private void fitTextRowHeight(Sheet sheet, int rowIndex, int fromCol, int toCol) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) return;
        int lines = 0;
        for (int c = fromCol; c <= toCol; c++) {
            Cell cell = row.getCell(c);
            if (cell == null || cell.getCellType() != CellType.STRING) continue;
            String text = cell.getStringCellValue();
            if (text == null || text.isBlank()) continue;
            int units = 0;
            for (int i = 0; i < text.length(); i++) units += text.charAt(i) > 0x2E7F ? 2 : 1;
            int width = Math.max(8, sheet.getColumnWidth(c) / 256 - 1);
            int perLine = width;
            lines = Math.max(lines, (units + perLine - 1) / perLine);
        }
        if (lines <= 0) return;
        row.setHeightInPoints(Math.min(409f, Math.max(18f, lines * 15f)));
    }

    private static class ParsedBudget {
        private final java.util.Set<String> missingTaskFields = new java.util.HashSet<>();
        private List<BizBudgetSourceItem> sources = new ArrayList<>();
        private List<BizBudgetTaskItem> tasks = new ArrayList<>();
    }

    private static class TaskName {
        private Integer index;
        private String name;
    }
}
