package org.example.service;

import org.example.entity.SysNotice;
import org.example.mapper.SysMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.util.*;
import static org.example.service.PermissionService.*;

/** 收紧授权前显式移交实际审核职责。只改处理配置和在途处理人，不改审批结论和历史。 */
@Service
public class PermissionDutyService {
    public record Change(String domain,Long id,Long handlerId,String token,String reason){}
    private final PermissionService access;private final PermissionManagementService management;private final JdbcTemplate jdbc;private final AdminManagementService logs;private final SysMapper users;
    public PermissionDutyService(PermissionService access,PermissionManagementService management,JdbcTemplate jdbc,AdminManagementService logs,SysMapper users){this.access=access;this.management=management;this.jdbc=jdbc;this.logs=logs;this.users=users;}
    private Map<String,Object> target(String domain,Long id){return management.pendingSnapshot().values().stream().filter(r->domain.equals(r.get("domain"))&&number(r.get("id"))==id).findFirst().orElseThrow(()->new AdminManagementException(409,"待办已处理或发生变化，请重新预览权限"));}
    private List<Map<String,Object>> affected(Map<String,Object> target){return management.pendingSnapshot().values().stream().filter(r->Objects.equals(r.get("domain"),target.get("domain"))&&number(r.get("objectId"))==number(target.get("objectId"))&&number(r.get("stage"))==number(target.get("stage"))).sorted(Comparator.comparingLong(r->number(r.get("id")))).toList();}
    private Object configuration(Map<String,Object> target){Long object=number(target.get("objectId"));return switch(str(target.get("domain"))){case "tasks"->jdbc.queryForList("SELECT task_id,dept_id,leader_id,auditor_id,principal_id,update_time FROM biz_task WHERE task_id=?",object);case "performance"->jdbc.queryForList("SELECT perf_id,dept_id,leader_id,auditor_id,principal_id,update_time FROM biz_performance WHERE perf_id=?",object);case "core"->jdbc.queryForList("SELECT p.perf_id,p.principal_id,p.update_time FROM biz_core_indicator c JOIN biz_performance p ON BINARY p.perf_code=BINARY c.indicator_code WHERE c.indicator_id=? AND COALESCE(p.is_delete,0)=0",object);default->Map.of("achievementId",object);};}
    private String token(List<Map<String,Object>> rows){try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(access.write(Map.of("pending",rows,"configuration",configuration(rows.get(0)),"permissionRevision",access.revision())).getBytes(java.nio.charset.StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    public Map<String,Object> preview(Long actor,String domain,Long id){return preview(actor,domain,id,false);}
    public Map<String,Object> preview(Long actor,String domain,Long id,boolean separate){access.requireAdmin(actor);if(id==null||!Set.of("tasks","performance","core","achievements").contains(domain))throw new AdminManagementException(400,"请选择有效待办");var target=target(domain,id);var rows=affected(target);Map<String,Object> result=new LinkedHashMap<>();result.put("target",target);result.put("affected",rows);result.put("token",token(rows));result.put("candidates",separate?List.of():users.getAllUsers().stream().filter(u->"1".equals(u.getStatus())&&!Integer.valueOf(1).equals(u.getIsDelete())).map(u->Map.of("userId",u.getUserId(),"name",Objects.toString(u.getNickName(),u.getUserName()))).toList());return result;}
    @Transactional(rollbackFor=Exception.class,isolation=Isolation.READ_COMMITTED)
    public Object save(Long actor,Change input){
        access.requireAdmin(actor);access.lockChange();if(input==null||input.id()==null||input.handlerId()==null||!Set.of("tasks","performance","core","achievements").contains(str(input.domain()))||input.reason()==null||input.reason().isBlank()||input.reason().length()>500)throw new AdminManagementException(400,"请选择新处理人并填写移交原因（最多500字）");
        access.user(input.handlerId());var pendingBefore=management.pendingSnapshot();var original=target(input.domain(),input.id());Long object=number(original.get("objectId"));int stage=(int)number(original.get("stage"));var before=affected(original);Object beforeConfiguration=configuration(original);if(!Objects.equals(token(before),input.token()))throw new AdminManagementException(409,"职责或待办已变化，请重新预览");if(before.stream().allMatch(r->number(r.get("handler"))==input.handlerId()))throw new AdminManagementException(400,"新处理人与当前处理人相同");
        // 权限独占锁与所有业务写事务互斥；此处再取业务锁，审批/撤回不能穿插到移交中间。
        String submissionTable,sid,source;boolean archive=Boolean.TRUE.equals(original.get("archive"));
        switch(input.domain()){
            case "tasks"->{jdbc.queryForList("SELECT task_id FROM biz_task WHERE task_id=? FOR UPDATE",object);if(!archive){String field=stage==10?"auditor_id":"principal_id";jdbc.update("UPDATE biz_task SET "+field+"=?,update_time=NOW() WHERE task_id=?",input.handlerId(),object);}submissionTable="biz_material_submission";sid="sub_id";source="0";}
            case "performance"->{jdbc.queryForList("SELECT perf_id FROM biz_performance WHERE perf_id=? FOR UPDATE",object);if(!archive)jdbc.update("UPDATE biz_performance SET auditor_id=?,update_time=NOW() WHERE perf_id=?",input.handlerId(),object);submissionTable="biz_performance_submission";sid="sub_id";source="2";}
            case "core"->{var perf=jdbc.queryForList("SELECT p.perf_id FROM biz_core_indicator c JOIN biz_performance p ON BINARY p.perf_code=BINARY c.indicator_code WHERE c.indicator_id=? AND COALESCE(p.is_delete,0)=0 FOR UPDATE",Long.class,object);if(perf.size()!=1)throw new AdminManagementException(409,"核心指标映射异常，请先修正配置");if(!archive)jdbc.update("UPDATE biz_performance SET principal_id=?,update_time=NOW() WHERE perf_id=?",input.handlerId(),perf.get(0));submissionTable="biz_core_report";sid="report_id";source="4";}
            default->{if(!access.admin(input.handlerId()))throw new AdminManagementException(409,"成果归档需移交给当前正常管理员");submissionTable="biz_achievement";sid="ach_id";source="3";}
        }
        if(archive&&!access.admin(input.handlerId()))throw new AdminManagementException(409,"归档职责需移交给当前正常管理员");
        if(!archive&&!input.domain().equals("achievements"))management.requireHandler(input.handlerId(),input.domain(),object,original.get("year")==null?null:(int)number(original.get("year")),stage);
        for(var row:before){Long id=number(row.get("id"));jdbc.queryForList("SELECT "+sid+" FROM "+submissionTable+" WHERE "+sid+"=? FOR UPDATE",id);String extra=input.domain().equals("core")?",version=version+1":"";jdbc.update("UPDATE "+submissionTable+" SET current_handler_id=?"+extra+" WHERE "+sid+"=?",input.handlerId(),id);
            Long noticeId=id;
            if(input.domain().equals("achievements")){var submissions=jdbc.queryForList("SELECT sub_id FROM biz_achievement_submission WHERE ach_id=? AND flow_status=10 AND COALESCE(is_delete,0)=0 ORDER BY sub_id DESC FOR UPDATE",Long.class,object);jdbc.update("UPDATE biz_achievement_submission SET current_handler_id=? WHERE ach_id=? AND flow_status=10 AND COALESCE(is_delete,0)=0",input.handlerId(),object);if(!submissions.isEmpty())noticeId=submissions.get(0);}
            jdbc.update("UPDATE sys_notice SET is_delete=1 WHERE to_user_id=? AND source_type=? AND source_id=? AND COALESCE(is_delete,0)=0 AND (trigger_event LIKE '%TODO%' OR trigger_event='审核提醒' OR trigger_event='成果归档审核' OR trigger_event='PERMISSION_DUTY_TRANSFER')",row.get("handler"),source,noticeId);
            SysNotice notice=new SysNotice();notice.setFromUserId(actor);notice.setToUserId(input.handlerId());notice.setType("1");notice.setTriggerEvent("PERMISSION_DUTY_TRANSFER");notice.setTitle("审核职责已移交，请处理待办");notice.setContent("管理员已将审核单 "+noticeId+" 移交给您，请进入审核中心处理。");notice.setSourceType(source);notice.setSourceId(noticeId);notice.setIsRead("0");notice.setIsDelete(0);notice.setCreateTime(new Date());users.sendNotice(notice);
        }
        management.validateChangedPending(pendingBefore);access.advance();logs.recordOperation("PERMISSION","DUTY_TRANSFER",input.domain(),str(object),"审核职责移交",actor,input.reason().trim(),Map.of("pending",before,"configuration",beforeConfiguration),Map.of("pending",affected(target(input.domain(),input.id())),"configuration",configuration(original)));return Map.of("message","审核职责和在途处理人已移交，请重新预览权限变更","affected",before.size());
    }
}
