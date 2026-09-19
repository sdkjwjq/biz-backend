package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.BizTask;
import org.example.entity.BizWorkRecord;
import org.example.entity.vo.WorkRecordVO;
import java.util.Date;
import java.util.List;

@Mapper
public interface WorkRecordMapper {
    @Select("SELECT DISTINCT phase FROM biz_task WHERE auditor_id=#{userId} AND level=3 "
            + "AND COALESCE(is_delete,0)=0 AND phase BETWEEN 2025 AND 2029 ORDER BY phase")
    List<Integer> fillableYears(Long userId);

    @Select("SELECT t.*, COALESCE(NULLIF(u.nick_name,''),u.user_name,'—') leader_name "
            + "FROM biz_task t LEFT JOIN sys_user u ON u.user_id=t.leader_id "
            + "WHERE t.auditor_id=#{ownerId} AND t.phase=#{year} AND t.level=3 AND COALESCE(t.is_delete,0)=0 "
            + "ORDER BY t.task_code,t.task_id")
    List<WorkRecordVO.Task> ownedTasks(@Param("ownerId") Long ownerId, @Param("year") int year);

    @Select("SELECT task_id,parent_id,level,task_code,task_name FROM biz_task WHERE COALESCE(is_delete,0)=0")
    List<BizTask> structure();

    // 归档历史可以来自已替换材料对应的软删除审核单；绝不将其当前状态当作历史月份的状态。
    // 四级历史记录只用于识别缺失的三级汇总证据，不单独增加三级任务完成数。
    @Select("<script>SELECT t.task_id parent_task_id,s.task_id source_task_id,s.sub_id,s.reported_value,"
            + "s.flow_status,s.submit_time,"
            + "(SELECT MIN(l.create_time) FROM biz_audit_log l WHERE l.sub_id=s.sub_id AND l.post_status=40) archive_time,"
            + "(SELECT l.post_status FROM biz_audit_log l WHERE l.sub_id=s.sub_id AND l.create_time &lt;= #{cutoff} "
            + "ORDER BY l.create_time DESC,l.log_id DESC LIMIT 1) cutoff_status,"
            + "(SELECT sn.previous_status FROM biz_audit_snapshot sn WHERE sn.sub_id=s.sub_id "
            + "AND sn.target_type='TASK' AND sn.target_id=t.task_id ORDER BY sn.snapshot_id LIMIT 1) previous_status "
            + "FROM biz_task t JOIN biz_material_submission s ON (s.task_id=t.task_id OR s.task_id IN "
            + "(SELECT c.task_id FROM biz_level4_task c WHERE c.parent_id=t.task_id AND COALESCE(c.is_delete,0)=0)) "
            + "WHERE t.task_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "ORDER BY s.submit_time,s.sub_id</script>")
    List<WorkRecordVO.Evidence> evidence(@Param("ids") List<Long> ids, @Param("cutoff") Date cutoff);

    @Select("SELECT * FROM biz_work_record WHERE owner_id=#{ownerId} AND record_year=#{year} AND record_month=#{month}")
    BizWorkRecord byMonth(@Param("ownerId") Long ownerId, @Param("year") int year, @Param("month") int month);

    @Select("SELECT * FROM biz_work_record WHERE record_id=#{id}")
    BizWorkRecord byId(Long id);

    @Select("SELECT * FROM biz_work_record WHERE record_id=#{id} FOR UPDATE")
    BizWorkRecord lock(Long id);

    @Insert("INSERT INTO biz_work_record(owner_id,owner_name,record_year,record_month,create_time,update_time) "
            + "VALUES(#{ownerId},#{ownerName},#{recordYear},#{recordMonth},#{createTime},#{updateTime}) "
            + "ON DUPLICATE KEY UPDATE record_id=LAST_INSERT_ID(record_id)")
    @Options(useGeneratedKeys=true, keyProperty="recordId")
    void create(BizWorkRecord record);

    @Select("SELECT statistics_json FROM biz_work_record_snapshot WHERE record_id=#{id}")
    String snapshot(Long id);

    @Select("SELECT statistics_json FROM biz_work_record_snapshot WHERE record_id=#{id} FOR UPDATE")
    String lockSnapshot(Long id);

    @Insert("INSERT INTO biz_work_record_snapshot(record_id,statistics_json,generated_time) VALUES(#{id},#{json},#{time}) "
            + "ON DUPLICATE KEY UPDATE statistics_json=#{json},generated_time=#{time}")
    void saveSnapshot(@Param("id") Long id, @Param("json") String json, @Param("time") Date time);

    @Update("UPDATE biz_work_record SET version=version+1,update_time=#{time} "
            + "WHERE record_id=#{id} AND owner_id=#{ownerId} AND status=0 AND version=#{version}")
    int advanceVersion(@Param("id") Long id, @Param("ownerId") Long ownerId,
                       @Param("version") long version, @Param("time") Date time);

    String VISIBLE = " WHERE (owner_id=#{userId} OR (status=1 AND #{viewAll}=true)) "
            + "<if test='year != null'>AND record_year=#{year} </if>"
            + "<if test='month != null'>AND record_month=#{month} </if>"
            + "<if test='ownerId != null'>AND owner_id=#{ownerId} </if>"
            + "<if test='status != null'>AND status=#{status} </if>";

    @Select("<script>SELECT * FROM biz_work_record" + VISIBLE
            + "ORDER BY record_year DESC,record_month DESC,owner_id,record_id LIMIT #{limit} OFFSET #{offset}</script>")
    List<BizWorkRecord> list(@Param("userId") Long userId, @Param("viewAll") boolean viewAll,
                            @Param("year") Integer year, @Param("month") Integer month,
                            @Param("ownerId") Long ownerId, @Param("status") Integer status,
                            @Param("limit") int limit, @Param("offset") long offset);

    @Select("<script>SELECT COUNT(*) FROM biz_work_record" + VISIBLE + "</script>")
    long count(@Param("userId") Long userId, @Param("viewAll") boolean viewAll,
               @Param("year") Integer year, @Param("month") Integer month,
               @Param("ownerId") Long ownerId, @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM biz_work_record WHERE owner_id=#{userId} AND status=1")
    long ownSubmitted(Long userId);

    @Select("SELECT COUNT(*) FROM biz_work_record WHERE owner_id=#{userId}")
    long ownRecords(Long userId);

    @Select("SELECT owner_id,MAX(owner_name) owner_name FROM biz_work_record "
            + "WHERE owner_id=#{userId} OR (status=1 AND #{viewAll}=true) GROUP BY owner_id ORDER BY owner_id")
    List<WorkRecordVO.Author> authors(@Param("userId") Long userId, @Param("viewAll") boolean viewAll);

    @Select("SELECT record_id,reform_task_id,reform_task_name,sort_order,key_progress,stage_results,typical_practices "
            + "FROM biz_work_record_entry WHERE record_id=#{id} ORDER BY sort_order,entry_id")
    List<WorkRecordVO.Entry> entries(Long id);

    @Delete("DELETE FROM biz_work_record_entry WHERE record_id=#{id}")
    void deleteEntries(Long id);

    @Insert("INSERT INTO biz_work_record_entry(record_id,reform_task_id,reform_task_name,sort_order,key_progress,stage_results,typical_practices) "
            + "VALUES(#{recordId},#{reformTaskId},#{reformTaskName},#{sortOrder},#{keyProgress},#{stageResults},#{typicalPractices})")
    void insertEntry(WorkRecordVO.Entry entry);

    @Update("UPDATE biz_work_record SET problems=#{record.problems},next_focus=#{record.nextFocus},other_matters=#{record.otherMatters},"
            + "owner_name=#{record.ownerName},version=version+1,update_time=#{record.updateTime},"
            + "status=#{record.status},submit_time=#{record.submitTime} "
            + "WHERE record_id=#{record.recordId} AND owner_id=#{record.ownerId} AND status=0 AND version=#{record.version}")
    int saveBody(@Param("record") BizWorkRecord record);
}
