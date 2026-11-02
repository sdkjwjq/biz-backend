package org.example.service;

import org.example.utils.AdminTestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 任务移交申请：专业群负责人 / 专业群审核人 / 归口部门审核人只能移交自己对应的字段，
 * 接收人确认后生效，双方与管理员都会收到重要通知。
 */
@Service
public class TaskTransferRequestService {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private PermissionService access;
    @Autowired private org.example.mapper.OwnershipTransferMapper ownership;
    @Autowired private PermissionManagementService permissionManagement;
    @Autowired private org.mybatis.spring.SqlSessionTemplate sessions;

    /** 字段与数据库列、中文名、日志列前缀的映射。 */
    private static final Map<String,String> COLUMN = Map.of(
            "leader","leader_id","auditor","auditor_id","principal","principal_id");
    private static final Map<String,String> LABEL = Map.of(
            "leader","专业群负责人","auditor","专业群审核人","principal","归口部门审核人");

    public record CreateInput(Long taskId,String fieldType,Long toUserId,String reason){}

    private String label(String field){return LABEL.getOrDefault(field,field);}

    private Map<String,Object> task(long taskId,boolean lock){
        var rows=jdbc.queryForList("SELECT task_id,task_code,task_name,level,phase,dept_id,leader_id,auditor_id,principal_id FROM biz_task WHERE task_id=? AND COALESCE(is_delete,0)=0"+(lock?" FOR UPDATE":""),taskId);
        if(rows.isEmpty())throw new AdminManagementException(404,"任务不存在");
        if(!Integer.valueOf(3).equals(((Number)rows.get(0).get("level")).intValue()))throw new AdminManagementException(400,"仅三级任务可以申请移交");
        return rows.get(0);
    }

    private void actor(Long actor){access.user(actor);access.require(actor,"tasks.read");}
    private void visible(Long actor,Long taskId){actor(actor);if(!access.canRead(actor,"tasks",taskId,null))throw new AdminManagementException(403,"任务不在查阅范围内");}
    private void noActiveAudit(Long taskId){if(ownership.countActiveTaskAudits(taskId)>0)throw new AdminManagementException(409,"任务存在审核中的单据，请先处理完成后再移交");}
    private void receiver(Long user,Long taskId,String field){
        access.user(user);
        if(jdbc.queryForObject("SELECT COUNT(*) FROM biz_task WHERE level=3 AND COALESCE(is_delete,0)=0 AND "+COLUMN.get(field)+"=?",Integer.class,user)==0)
            throw new AdminManagementException(400,"请选择已承担同类职责的正常账号");
        if(!access.allowed(user,field.equals("leader")?"tasks.fill":"tasks.audit")||!access.canReceiveTask(user,taskId,field))
            throw new AdminManagementException(409,"接收人没有该任务的填报或审核权限，请联系管理员调整");
    }

    private static Long num(Object value){return value==null?null:Long.valueOf(String.valueOf(value));}

    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> create(Long actor,CreateInput input){
        actor(actor);
        if(input==null||input.taskId()==null||input.toUserId()==null)throw new AdminManagementException(400,"请选择任务与接收人");
        String field=input.fieldType();
        if(field==null||!COLUMN.containsKey(field))throw new AdminManagementException(400,"移交类型不正确");
        String reason=input.reason()==null?"":input.reason().trim();
        if(reason.isEmpty())throw new AdminManagementException(400,"请填写移交原因");
        if(reason.length()>500)throw new AdminManagementException(400,"移交原因不能超过500字");
        if(Objects.equals(actor,input.toUserId()))throw new AdminManagementException(400,"不能移交给本人");
        var task=task(input.taskId(),true);
        visible(actor,input.taskId());noActiveAudit(input.taskId());
        Object holder=task.get(COLUMN.get(field));
        if(!Objects.equals(num(holder),actor))throw new AdminManagementException(403,"只能移交本人负责的"+label(field));
        receiver(input.toUserId(),input.taskId(),field);
        Long pending=jdbc.queryForObject("SELECT COUNT(*) FROM biz_task_transfer_request WHERE task_id=? AND field_type=? AND status='PENDING'",Long.class,input.taskId(),field);
        if(pending!=null&&pending>0)throw new AdminManagementException(409,"该任务此字段已有待确认的移交申请");
        jdbc.update("INSERT INTO biz_task_transfer_request(task_id,field_type,from_user,to_user,reason,status) VALUES(?,?,?,?,?,'PENDING')",input.taskId(),field,actor,input.toUserId(),reason);
        Long requestId=jdbc.queryForObject("SELECT LAST_INSERT_ID()",Long.class);
        String title="任务移交申请："+task.get("task_name");
        String content=String.format("%s 申请把任务《%s》的「%s」移交给该账号。原因：%s。请进入任务页确认后完成移交。",
                name(actor),task.get("task_name"),label(field),reason);
        Long noticeId=notify(actor,List.of(input.toUserId()),title,content);
        jdbc.update("UPDATE biz_task_transfer_request SET notice_id=? WHERE request_id=?",noticeId,requestId);
        notify(actor,adminIds(),"任务移交申请："+task.get("task_name"),
                String.format("%s 发起移交申请：《%s》的「%s」→ %s。原因：%s。",name(actor),task.get("task_name"),label(field),name(input.toUserId()),reason));
        AdminTestContext.record(actor,"TASK_TRANSFER",String.valueOf(requestId),String.valueOf(task.get("task_name")),"申请移交"+label(field));
        return detail(requestId);
    }

    public List<Map<String,Object>> mine(Long actor){actor(actor);return list("r.to_user",actor,"PENDING");}
    public List<Map<String,Object>> sent(Long actor){actor(actor);return list("r.from_user",actor,null);}

    /** 某任务的移交/归属变更记录（登录即可查看，不要求移交权限）。 */
    public List<Map<String,Object>> logs(Long actor,Long taskId){
        visible(actor,taskId);
        return jdbc.queryForList("SELECT l.log_id logId,l.target_type targetType,l.target_id targetId,l.target_code targetCode,l.target_name targetName,"
                +"l.operator_id operatorId,COALESCE(NULLIF(u.nick_name,''),u.user_name) operatorName,l.reason,l.create_time createTime "
                +"FROM biz_ownership_change_log l LEFT JOIN sys_user u ON u.user_id=l.operator_id WHERE l.target_type='TASK' AND l.target_id=? ORDER BY l.log_id DESC LIMIT 200",taskId);
    }

    /** 接收人候选：只列出与移交类型相同的角色（负责人→负责人，审核人→审核人，归口审核人→归口审核人）。 */
    public List<Map<String,Object>> searchUsers(Long actor,Long taskId,String fieldType,String keyword){
        visible(actor,taskId);var task=task(taskId,false);
        if(fieldType==null||!COLUMN.containsKey(fieldType)||!Objects.equals(num(task.get(COLUMN.get(fieldType))),actor))throw new AdminManagementException(403,"只能为本人负责的职责查询接收人");
        String column=COLUMN.get(fieldType);
        String k=keyword==null?"":keyword.trim();String like="%"+k+"%";
        String sql="SELECT DISTINCT u.user_id userId,COALESCE(NULLIF(u.nick_name,''),u.user_name) name FROM sys_user u "
                +"WHERE u.status='1' AND COALESCE(u.is_delete,0)=0 AND u.user_id<>? AND EXISTS(SELECT 1 FROM biz_task t WHERE t.level=3 AND t."+column+"=u.user_id AND COALESCE(t.is_delete,0)=0) "
                +"AND (COALESCE(u.nick_name,'') LIKE ? OR u.user_name LIKE ? OR CAST(u.user_id AS CHAR) LIKE ?) ORDER BY u.user_id LIMIT 30";
        if(k.length()>100)throw new AdminManagementException(400,"搜索关键词不能超过100字");
        return jdbc.queryForList(sql,actor,like,like,like).stream().filter(r->{try{receiver(num(r.get("userId")),taskId,fieldType);return true;}catch(AdminManagementException e){return false;}}).toList();
    }

    private List<Map<String,Object>> list(String column,Long actor,String status){
        String sql="SELECT r.*,t.task_code,t.task_name,u.nick_name from_name FROM biz_task_transfer_request r "
                +"LEFT JOIN biz_task t ON t.task_id=r.task_id LEFT JOIN sys_user u ON u.user_id=r.from_user "
                +"WHERE "+column+"=?"+(status==null?"":" AND r.status='"+status+"'")+" ORDER BY r.request_id DESC LIMIT 200";
        var rows=new ArrayList<Map<String,Object>>();
        for(var r:jdbc.queryForList(sql,actor))rows.add(row(r));
        return rows;
    }

    private Map<String,Object> row(Map<String,Object> r){
        Map<String,Object> out=new LinkedHashMap<>();
        out.put("requestId",r.get("request_id"));out.put("taskId",r.get("task_id"));
        out.put("taskCode",r.get("task_code"));out.put("taskName",r.get("task_name"));
        out.put("fieldType",r.get("field_type"));out.put("fieldLabel",label(String.valueOf(r.get("field_type"))));
        out.put("fromUser",r.get("from_user"));out.put("fromName",r.get("from_name"));
        out.put("toUser",r.get("to_user"));out.put("toName",name(num(r.get("to_user"))));
        out.put("reason",r.get("reason"));out.put("status",r.get("status"));
        out.put("createTime",r.get("create_time"));out.put("handleTime",r.get("handle_time"));
        out.put("handleRemark",r.get("handle_remark"));
        return out;
    }

    private Map<String,Object> detail(Long requestId){
        var rows=jdbc.queryForList("SELECT r.*,t.task_code,t.task_name,u.nick_name from_name FROM biz_task_transfer_request r "
                +"LEFT JOIN biz_task t ON t.task_id=r.task_id LEFT JOIN sys_user u ON u.user_id=r.from_user WHERE r.request_id=?",requestId);
        if(rows.isEmpty())throw new AdminManagementException(404,"移交申请不存在");
        return row(rows.get(0));
    }

    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> accept(Long actor,Long requestId){
        actor(actor);
        var req=require(actor,requestId,"ACCEPTED");
        long taskId=num(req.get("task_id"));String field=String.valueOf(req.get("field_type"));
        var task=task(taskId,true);noActiveAudit(taskId);receiver(actor,taskId,field);
        if(!Objects.equals(num(task.get(COLUMN.get(field))),num(req.get("from_user"))))
            throw new AdminManagementException(409,"该字段的负责人已变化，无法完成移交");
        if(jdbc.update("UPDATE biz_task SET "+COLUMN.get(field)+"=?,update_time=NOW() WHERE task_id=? AND "+COLUMN.get(field)+"=? AND COALESCE(is_delete,0)=0",actor,taskId,num(req.get("from_user")))!=1)throw new AdminManagementException(409,"任务归属已变化，请刷新后重试");
        // JDBC writes must invalidate the request's MyBatis session reads before verifying the new duty.
        sessions.clearCache();
        if(field.equals("leader")){if(!access.canRead(actor,"tasks",taskId,((Number)task.get("phase")).intValue()))throw new AdminManagementException(409,"接收人没有该任务的查阅权限");}
        else permissionManagement.requireHandler(actor,"tasks",taskId,((Number)task.get("phase")).intValue(),field.equals("auditor")?10:20);
        jdbc.update("UPDATE biz_task_transfer_request SET status='ACCEPTED',handle_time=NOW(3),handle_remark='接收人已确认' WHERE request_id=?",requestId);
        String before=COLUMN.get(field).replace("_id","")+"_before_id", after=before.replace("before","after");
        jdbc.update("INSERT INTO biz_ownership_change_log(target_type,target_id,target_code,target_name,operator_id,reason,"+before+","+after+",create_time) VALUES('TASK',?,?,?,?,?,?,?,NOW(3))",
                taskId,task.get("task_code"),task.get("task_name"),actor,
                "接收移交申请 #"+requestId+"："+label(field)+"；申请原因："+String.valueOf(req.get("reason")).substring(0,Math.min(350,String.valueOf(req.get("reason")).length())),
                num(req.get("from_user")),actor);
        notify(actor,List.of(num(req.get("from_user"))),"移交已完成："+task.get("task_name"),
                String.format("%s 已确认接收《%s》的「%s」，移交完成。",name(actor),task.get("task_name"),label(field)));
        notify(actor,adminIds(),"移交已完成："+task.get("task_name"),
                String.format("%s 已确认接收《%s》的「%s」（原负责人 %s）。",name(actor),task.get("task_name"),label(field),name(num(req.get("from_user")))));
        AdminTestContext.record(actor,"TASK_TRANSFER",String.valueOf(requestId),String.valueOf(task.get("task_name")),"确认移交"+label(field));
        return detail(requestId);
    }

    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> reject(Long actor,Long requestId,String remark){
        actor(actor);
        if(remark==null||remark.isBlank()||remark.length()>500)throw new AdminManagementException(400,"请填写500字以内的拒绝原因");
        var req=require(actor,requestId,"REJECTED");
        var task=jdbc.queryForList("SELECT task_name FROM biz_task WHERE task_id=?",num(req.get("task_id"))).stream().findFirst().orElse(Map.of("task_name","已删除任务"));
        jdbc.update("UPDATE biz_task_transfer_request SET status='REJECTED',handle_time=NOW(3),handle_remark=? WHERE request_id=?",remark==null?"":remark.trim(),requestId);
        notify(actor,List.of(num(req.get("from_user"))),"移交被拒绝："+task.get("task_name"),
                String.format("%s 拒绝接收《%s》的「%s」。备注：%s",name(actor),task.get("task_name"),label(String.valueOf(req.get("field_type"))),remark==null?"无":remark.trim()));
        notify(actor,adminIds(),"移交被拒绝："+task.get("task_name"),
                String.format("%s 拒绝接收 %s 发起的《%s》的「%s」移交。",name(actor),name(num(req.get("from_user"))),task.get("task_name"),label(String.valueOf(req.get("field_type")))));
        return detail(requestId);
    }

    private Map<String,Object> require(Long actor,Long requestId,String action){
        var rows=jdbc.queryForList("SELECT * FROM biz_task_transfer_request WHERE request_id=? FOR UPDATE",requestId);
        if(rows.isEmpty())throw new AdminManagementException(404,"移交申请不存在");
        var req=rows.get(0);
        if(!"PENDING".equals(String.valueOf(req.get("status"))))throw new AdminManagementException(409,"该移交申请已处理");
        if(!Objects.equals(num(req.get("to_user")),actor))throw new AdminManagementException(403,"只有接收人可以处理该移交申请");
        return req;
    }

    private List<Long> adminIds(){return jdbc.queryForList("SELECT user_id FROM sys_user WHERE role='0' AND status='1' AND COALESCE(is_delete,0)=0",Long.class);}

    private String name(Long userId){
        if(userId==null)return "—";
        var rows=jdbc.queryForList("SELECT nick_name,user_name FROM sys_user WHERE user_id=?",userId);
        if(rows.isEmpty())return String.valueOf(userId);
        Object nick=rows.get(0).get("nick_name"),user=rows.get(0).get("user_name");
        return nick==null||String.valueOf(nick).isBlank()?String.valueOf(user):String.valueOf(nick);
    }

    /** 以重要通知的方式推送给指定用户（同时写入消息中心收件箱）。 */
    private Long notify(Long actor,List<Long> users,String title,String content){
        title=title.substring(0,Math.min(100,title.length()));
        List<Long> ids=users.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if(ids.isEmpty())return null;
        String audience="["+ids.stream().map(String::valueOf).collect(Collectors.joining(","))+"]";
        jdbc.update("INSERT INTO sys_platform_notice(title,content,important,audience_type,audience_json,status,version,content_version,created_by,updated_by,created_at,updated_at,published_at) "
                +"VALUES(?,?,1,'USERS',CAST(? AS JSON),'PUBLISHED',1,1,?,?,NOW(3),NOW(3),NOW(3))",title,content,audience,actor,actor);
        Long noticeId=jdbc.queryForObject("SELECT LAST_INSERT_ID()",Long.class);
        for(Long id:ids){
            var rows=jdbc.queryForList("SELECT user_id,nick_name,dept_id FROM sys_user WHERE user_id=?",id);
            if(rows.isEmpty())continue;
            var user=rows.get(0);String nickname=name(id);
            Object deptId=user.get("dept_id");String deptName=null;
            if(deptId!=null){var dept=jdbc.queryForList("SELECT dept_name FROM sys_dept WHERE dept_id=?",deptId);if(!dept.isEmpty())deptName=String.valueOf(dept.get(0).get("dept_name"));}
            jdbc.update("INSERT INTO sys_notice(from_user_id,to_user_id,type,trigger_event,title,content,source_type,source_id,is_read,is_delete,create_time) VALUES(?,?,'1','PLATFORM_NOTICE',?,?,'5',?,'0',0,NOW())",actor,id,title,content,noticeId);
            Long inbox=jdbc.queryForObject("SELECT LAST_INSERT_ID()",Long.class);
            jdbc.update("INSERT INTO sys_platform_notice_recipient(notice_id,user_id,user_name,dept_id,dept_name,inbox_id) VALUES(?,?,?,?,?,?)",noticeId,id,nickname,deptId,deptName,inbox);
        }
        return noticeId;
    }
}
