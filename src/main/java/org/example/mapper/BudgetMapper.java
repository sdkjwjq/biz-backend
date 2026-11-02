package org.example.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.entity.BizBudgetImportVersion;
import org.example.entity.BizBudgetSheet;
import org.example.entity.BizBudgetSourceItem;
import org.example.entity.BizBudgetTaskItem;
import org.example.entity.vo.BudgetVersionVO;

import java.util.List;

@Mapper
public interface BudgetMapper {
    @Select("SELECT * FROM biz_budget_sheet WHERE year = #{year} AND month = #{month} AND section = #{section} AND is_delete = 0 LIMIT 1")
    BizBudgetSheet getSheet(@Param("year") Integer year, @Param("month") Integer month, @Param("section") String section);
    default BizBudgetSheet getSheet(Integer year,Integer month){return getEffectiveSheet(year,month);}

    @Select("SELECT s.sheet_id,s.month,s.section FROM biz_budget_sheet s WHERE s.year=#{year} AND s.month BETWEEN 1 AND 12 AND s.is_delete=0 AND (s.section='execution' OR (s.section='annual' AND NOT EXISTS(SELECT 1 FROM biz_budget_sheet e WHERE e.year=s.year AND e.month=s.month AND e.section='execution' AND e.is_delete=0)))")
    List<BizBudgetSheet> trendSheets(Integer year);

    @Select("SELECT sheet_id,month,section FROM biz_budget_sheet WHERE year=#{year} AND month BETWEEN 1 AND 12 AND section='annual' AND is_delete=0 ORDER BY sheet_id")
    List<BizBudgetSheet> annualTrendSheets(Integer year);

    @Select("<script>SELECT sheet_id,task_index,amount,budget_amount budgetAmount,goods,capital,other,goods_budget goodsBudget,capital_budget capitalBudget,other_budget otherBudget FROM biz_budget_task_item WHERE sheet_id IN "
            +"<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> ORDER BY sheet_id,task_index</script>")
    List<BizBudgetTaskItem> trendTasks(@Param("ids") List<Long> ids);

    @Select("<script>SELECT sheet_id,annual_plan FROM biz_budget_source_item WHERE sheet_id IN "
            +"<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> ORDER BY sheet_id,display_order</script>")
    List<BizBudgetSourceItem> trendSources(@Param("ids") List<Long> ids);

    @Select("SELECT * FROM biz_budget_sheet WHERE year = #{year} AND month = #{month} AND section = #{section} AND is_delete = 0 LIMIT 1 FOR UPDATE")
    BizBudgetSheet getSheetForUpdate(@Param("year") Integer year, @Param("month") Integer month, @Param("section") String section);
    default BizBudgetSheet getSheetForUpdate(Integer year,Integer month){return getSheetForUpdate(year,month,"annual");}

    @Select("SELECT * FROM biz_budget_sheet WHERE year=#{year} AND month=#{month} AND is_delete=0 AND section IN('annual','execution') ORDER BY (section='execution') DESC,sheet_id LIMIT 1")
    BizBudgetSheet getEffectiveSheet(@Param("year") Integer year,@Param("month") Integer month);

    @Select("SELECT * FROM biz_budget_sheet WHERE sheet_id = #{sheetId} AND is_delete = 0")
    BizBudgetSheet getSheetById(Long sheetId);

    @Insert("INSERT INTO biz_budget_sheet (year, month, section, locked, file_id, import_by, import_time, update_time, is_delete) " +
            "VALUES (#{year}, #{month}, #{section}, #{locked}, #{fileId}, #{importBy}, #{importTime}, #{updateTime}, #{isDelete})")
    @Options(useGeneratedKeys = true, keyProperty = "sheetId", keyColumn = "sheet_id")
    void insertSheet(BizBudgetSheet sheet);

    @Update("UPDATE biz_budget_sheet SET locked = #{locked}, file_id = #{fileId}, import_by = #{importBy}, " +
            "import_time = #{importTime}, update_time = #{updateTime}, is_delete = #{isDelete} WHERE sheet_id = #{sheetId}")
    void updateSheet(BizBudgetSheet sheet);

    @Update("UPDATE biz_budget_sheet SET locked = #{locked}, update_time = NOW() WHERE sheet_id = #{sheetId}")
    void updateLock(Long sheetId, Integer locked);

    @Update("UPDATE biz_budget_sheet SET update_time=#{updateTime} WHERE sheet_id=#{sheetId}")
    void updateEditTime(@Param("sheetId") Long sheetId, @Param("updateTime") java.util.Date updateTime);

    @Update("UPDATE biz_budget_source_item SET annual_plan=#{annualPlan},arrived=#{arrived} WHERE item_id=#{itemId} AND sheet_id=#{sheetId}")
    void updateEditableSource(BizBudgetSourceItem item);

    @Update("UPDATE biz_budget_task_item SET goods=#{goods},capital=#{capital},other=#{other},goods_budget=#{goodsBudget},budget_amount=#{budgetAmount},capital_budget=#{capitalBudget},other_budget=#{otherBudget} WHERE item_id=#{itemId} AND sheet_id=#{sheetId}")
    void updateEditableTask(BizBudgetTaskItem item);

    @Delete("DELETE FROM biz_budget_source_item WHERE sheet_id = #{sheetId}")
    void deleteSources(Long sheetId);

    @Delete("DELETE FROM biz_budget_task_item WHERE sheet_id = #{sheetId}")
    void deleteTasks(Long sheetId);

    @Insert("INSERT INTO biz_budget_source_item (sheet_id, source_key, source_name, display_order, five_year_total, available, annual_plan, carryover, arrived) " +
            "VALUES (#{sheetId}, #{sourceKey}, #{sourceName}, #{displayOrder}, #{fiveYearTotal}, #{available}, #{annualPlan}, #{carryover}, #{arrived})")
    @Options(useGeneratedKeys = true, keyProperty = "itemId", keyColumn = "item_id")
    void insertSource(BizBudgetSourceItem item);

    @Insert("INSERT INTO biz_budget_task_item (sheet_id, task_index, task_name, amount, budget_amount, goods, capital, other, goods_budget, capital_budget, other_budget, core_output, target_value, achieved) " +
            "VALUES (#{sheetId}, #{taskIndex}, #{taskName}, #{amount}, #{budgetAmount}, #{goods}, #{capital}, #{other}, #{goodsBudget}, #{capitalBudget}, #{otherBudget}, #{coreOutput}, #{targetValue}, #{achieved})")
    @Options(useGeneratedKeys = true, keyProperty = "itemId", keyColumn = "item_id")
    void insertTask(BizBudgetTaskItem item);

    @Select("SELECT * FROM biz_budget_source_item WHERE sheet_id = #{sheetId} ORDER BY display_order ASC")
    List<BizBudgetSourceItem> getSources(Long sheetId);

    @Select("SELECT * FROM biz_budget_task_item WHERE sheet_id = #{sheetId} ORDER BY task_index ASC")
    List<BizBudgetTaskItem> getTasks(Long sheetId);

    @Select("SELECT COALESCE(MAX(version_no), 0) + 1 FROM biz_budget_import_version " +
            "WHERE sheet_id = #{sheetId} AND is_delete = 0")
    Integer nextVersionNo(Long sheetId);

    @Insert("INSERT INTO biz_budget_import_version (sheet_id, year, month, version_no, operation_type, " +
            "source_version_id, file_id, file_name, snapshot_json, created_by, create_time, remark, is_delete) " +
            "VALUES (#{sheetId}, #{year}, #{month}, #{versionNo}, #{operationType}, #{sourceVersionId}, " +
            "#{fileId}, #{fileName}, #{snapshotJson}, #{createdBy}, #{createTime}, #{remark}, #{isDelete})")
    @Options(useGeneratedKeys = true, keyProperty = "versionId", keyColumn = "version_id")
    void insertVersion(BizBudgetImportVersion version);

    @Select("SELECT * FROM biz_budget_import_version WHERE version_id = #{versionId} AND is_delete = 0")
    BizBudgetImportVersion getVersion(Long versionId);

    @Select("SELECT v.version_id, v.sheet_id, v.year, v.month, v.version_no, v.operation_type, " +
            "v.source_version_id, v.file_id, v.file_name, v.created_by, " +
            "COALESCE(NULLIF(u.nick_name, ''), u.user_name, CONCAT('用户', v.created_by)) AS created_by_name, " +
            "v.create_time, v.remark, s.section FROM biz_budget_import_version v " +
            "LEFT JOIN sys_user u ON u.user_id = v.created_by " +
            "JOIN biz_budget_sheet s ON s.sheet_id = v.sheet_id " +
            "WHERE v.year = #{year} AND v.month = #{month} AND s.section = #{section} AND v.is_delete = 0 " +
            "ORDER BY v.version_no DESC")
    List<BudgetVersionVO> getVersions(@Param("year") Integer year, @Param("month") Integer month, @Param("section") String section);
    default List<BudgetVersionVO> getVersions(Integer year,Integer month){return getVersions(year,month,"annual");}

    @Select("SELECT v.version_id, v.sheet_id, v.year, v.month, v.version_no, v.operation_type, " +
            "v.source_version_id, v.file_id, v.file_name, v.created_by, " +
            "COALESCE(NULLIF(u.nick_name, ''), u.user_name, CONCAT('用户', v.created_by)) AS created_by_name, " +
            "v.create_time, v.remark, s.section FROM biz_budget_import_version v " +
            "LEFT JOIN sys_user u ON u.user_id = v.created_by " +
            "JOIN biz_budget_sheet s ON s.sheet_id=v.sheet_id WHERE v.version_id = #{versionId} AND v.is_delete = 0")
    BudgetVersionVO getVersionVO(Long versionId);
}
