package org.example.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.entity.BizBudgetSheet;
import org.example.entity.BizBudgetSourceItem;
import org.example.entity.BizBudgetTaskItem;

import java.util.List;

@Mapper
public interface BudgetMapper {
    @Select("SELECT * FROM biz_budget_sheet WHERE year = #{year} AND month = #{month} AND is_delete = 0 LIMIT 1")
    BizBudgetSheet getSheet(Integer year, Integer month);

    @Select("SELECT * FROM biz_budget_sheet WHERE sheet_id = #{sheetId} AND is_delete = 0")
    BizBudgetSheet getSheetById(Long sheetId);

    @Insert("INSERT INTO biz_budget_sheet (year, month, locked, file_id, import_by, import_time, update_time, is_delete) " +
            "VALUES (#{year}, #{month}, #{locked}, #{fileId}, #{importBy}, #{importTime}, #{updateTime}, #{isDelete})")
    @Options(useGeneratedKeys = true, keyProperty = "sheetId", keyColumn = "sheet_id")
    void insertSheet(BizBudgetSheet sheet);

    @Update("UPDATE biz_budget_sheet SET locked = #{locked}, file_id = #{fileId}, import_by = #{importBy}, " +
            "import_time = #{importTime}, update_time = #{updateTime}, is_delete = #{isDelete} WHERE sheet_id = #{sheetId}")
    void updateSheet(BizBudgetSheet sheet);

    @Update("UPDATE biz_budget_sheet SET locked = #{locked}, update_time = NOW() WHERE sheet_id = #{sheetId}")
    void updateLock(Long sheetId, Integer locked);

    @Delete("DELETE FROM biz_budget_source_item WHERE sheet_id = #{sheetId}")
    void deleteSources(Long sheetId);

    @Delete("DELETE FROM biz_budget_task_item WHERE sheet_id = #{sheetId}")
    void deleteTasks(Long sheetId);

    @Insert("INSERT INTO biz_budget_source_item (sheet_id, source_key, source_name, display_order, five_year_total, available, annual_plan, carryover, arrived) " +
            "VALUES (#{sheetId}, #{sourceKey}, #{sourceName}, #{displayOrder}, #{fiveYearTotal}, #{available}, #{annualPlan}, #{carryover}, #{arrived})")
    @Options(useGeneratedKeys = true, keyProperty = "itemId", keyColumn = "item_id")
    void insertSource(BizBudgetSourceItem item);

    @Insert("INSERT INTO biz_budget_task_item (sheet_id, task_index, task_name, amount, goods, capital, other, core_output, target_value, achieved) " +
            "VALUES (#{sheetId}, #{taskIndex}, #{taskName}, #{amount}, #{goods}, #{capital}, #{other}, #{coreOutput}, #{targetValue}, #{achieved})")
    @Options(useGeneratedKeys = true, keyProperty = "itemId", keyColumn = "item_id")
    void insertTask(BizBudgetTaskItem item);

    @Select("SELECT * FROM biz_budget_source_item WHERE sheet_id = #{sheetId} ORDER BY display_order ASC")
    List<BizBudgetSourceItem> getSources(Long sheetId);

    @Select("SELECT * FROM biz_budget_task_item WHERE sheet_id = #{sheetId} ORDER BY task_index ASC")
    List<BizBudgetTaskItem> getTasks(Long sheetId);
}
