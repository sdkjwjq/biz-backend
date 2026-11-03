package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.utils.JWTUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.sql.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;

/** Platform notices share the inbox with supervision but have independent immutable content and receipts. */
@Service
public class PlatformNoticeService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private volatile Boolean installed;
    public PlatformNoticeService(JdbcTemplate jdbc,ObjectMapper json){this.jdbc=jdbc;this.json=json;}
    public boolean installed(){
        if(installed==null)installed=jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sys_platform_notice'",Integer.class)==1;
        return installed;
    }
    public record Draft(Long id,Long version,String title,String content,Boolean important,String audienceType,
                        List<Long> audienceIds,List<Long> fileIds,LocalDateTime expiresAt,Long revisionOf){}
    public record ReadAction(String requestId,Integer contentVersion,String viewKey){}
    public record Change(Long version,String reason){}
    private static final String SELECT="SELECT n.notice_id id,n.title,n.content,n.important,n.audience_type audienceType,n.audience_json audienceJson,n.status,n.version,n.revision_of revisionOf,n.content_version contentVersion,n.created_by createdBy,n.updated_at updatedAt,n.published_at publishedAt,n.expires_at expiresAt,n.withdraw_reason withdrawReason FROM sys_platform_notice n ";
    private static final String ACTIVE="n.status='PUBLISHED' AND (n.expires_at IS NULL OR n.expires_at>NOW(3))";
    private long number(Object v){return v==null?0:((Number)v).longValue();}
    private String str(Object v){return Objects.toString(v,"");}
    private String text(String v,int max,String label){String s=v==null?"":v.strip();if(s.isEmpty()||s.length()>max)throw new AdminManagementException(400,label+"不能为空且不得超过"+max+"字");return s;}
    private List<Long> ids(List<Long> values){if(values==null)return List.of();if(values.size()>5000||values.stream().anyMatch(v->v==null||v<=0))throw new AdminManagementException(400,"所选对象不正确");return values.stream().distinct().toList();}
    private String encode(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalArgumentException("通知内容无法保存");}}
    private List<Long> decode(Object value){try{return json.readValue(str(value),new TypeReference<List<Long>>(){});}catch(Exception e){throw new AdminManagementException(500,"通知范围配置异常");}}
    public void admin(Long actor){
        if(actor==null||jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE user_id=? AND role='0' AND status='1' AND COALESCE(is_delete,0)=0",Integer.class,actor)!=1)
            throw new AdminManagementException(403,"仅当前有效管理员可发布和管理平台通知");
    }
    private long insert(String sql,Object... args){GeneratedKeyHolder key=new GeneratedKeyHolder();jdbc.update(c->{var s=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);for(int i=0;i<args.length;i++)s.setObject(i+1,args[i]);return s;},key);return key.getKey().longValue();}
    private Map<String,Object> get(long id,boolean lock){var rows=jdbc.queryForList(SELECT+"WHERE n.notice_id=?"+(lock?" FOR UPDATE":""),id);if(rows.isEmpty())throw new AdminManagementException(404,"通知不存在");return rows.get(0);}
    private void audit(String action,long id,Long actor,String title,String reason,Object summary){
        jdbc.update("INSERT INTO sys_admin_operation_log(module,action,target_type,target_id,target_name,operator_id,reason,after_json) VALUES('PLATFORM_NOTICE',?,'PLATFORM_NOTICE',?,?,?,?,?)",action,String.valueOf(id),title,actor,reason,encode(summary));
    }
    public Map<String,Object> detail(long id,Long actor){admin(actor);var n=get(id,false);n.put("audienceIds",decode(n.remove("audienceJson")));n.put("attachments",attachments(id));return n;}
    private List<Map<String,Object>> attachments(long id){return jdbc.queryForList("SELECT f.file_id id,f.file_name name,f.file_size size FROM sys_platform_notice_attachment a JOIN sys_file f ON f.file_id=a.file_id WHERE a.notice_id=? AND COALESCE(f.is_delete,0)=0 ORDER BY f.file_id",id);}
    private List<Map<String,Object>> audience(String mode,List<Long> selected){
        if(mode==null||!Set.of("ALL","DEPARTMENTS","USERS").contains(mode))throw new AdminManagementException(400,"请选择有效的接收范围");
        if(!mode.equals("ALL")&&selected.isEmpty())throw new AdminManagementException(400,"请选择接收部门或人员");
        List<Object> args=new ArrayList<>();String where="";
        if(!mode.equals("ALL")){where=" AND u."+(mode.equals("USERS")?"user_id":"dept_id")+" IN ("+String.join(",",Collections.nCopies(selected.size(),"?"))+")";args.addAll(selected);}
        return jdbc.queryForList("SELECT u.user_id id,COALESCE(NULLIF(u.nick_name,''),u.user_name) name,u.dept_id deptId,COALESCE(d.dept_name,'未设置部门') deptName FROM sys_user u LEFT JOIN sys_dept d ON d.dept_id=u.dept_id WHERE u.status='1' AND COALESCE(u.is_delete,0)=0"+where+" ORDER BY u.user_id",args.toArray());
    }
    public Map<String,Object> preview(String mode,List<Long> selected,Long actor){admin(actor);var rows=audience(mode,ids(selected));return Map.of("total",rows.size(),"recipients",rows.subList(0,Math.min(100,rows.size())));}
    public Map<String,Object> options(String keyword,int page,Long actor){
        admin(actor);int p=Math.max(1,page);String k=keyword==null?"":keyword.strip();if(k.length()>100)throw new AdminManagementException(400,"搜索内容过长");
        String from=" FROM sys_user u LEFT JOIN sys_dept d ON d.dept_id=u.dept_id WHERE u.status='1' AND COALESCE(u.is_delete,0)=0 AND (LOCATE(?,COALESCE(u.nick_name,''))>0 OR LOCATE(?,u.user_name)>0 OR LOCATE(?,CAST(u.user_id AS CHAR))>0)";
        var rows=jdbc.queryForList("SELECT u.user_id id,COALESCE(NULLIF(u.nick_name,''),u.user_name) name,d.dept_name deptName"+from+" ORDER BY u.user_id LIMIT 50 OFFSET ?",k,k,k,(p-1)*50);
        return Map.of("users",rows,"total",jdbc.queryForObject("SELECT COUNT(*)"+from,Long.class,k,k,k),"departments",jdbc.queryForList("SELECT dept_id id,dept_name name FROM sys_dept WHERE status='0' AND COALESCE(is_delete,0)=0 ORDER BY dept_id"));
    }
    @Transactional public Map<String,Object> save(Draft input,Long actor){
        admin(actor);String title=text(input.title(),100,"标题"),content=text(input.content(),20000,"正文");String mode=Objects.toString(input.audienceType(),"");var selected=ids(input.audienceIds());audience(mode,selected);
        if(input.expiresAt()!=null&&!input.expiresAt().isAfter(LocalDateTime.now(ZoneId.of("Asia/Shanghai"))))throw new AdminManagementException(400,"有效截止时间必须晚于当前时间");
        var files=ids(input.fileIds());if(files.size()>10)throw new AdminManagementException(400,"最多上传10份附件");
        for(Long file:files)if(jdbc.queryForObject("SELECT COUNT(*) FROM sys_file f WHERE f.file_id=? AND COALESCE(f.is_delete,0)=0 AND f.file_path LIKE '/uploads/platform-notices/%' AND (f.upload_by=? OR EXISTS(SELECT 1 FROM sys_platform_notice_attachment a WHERE a.file_id=f.file_id))",Integer.class,file,actor)!=1)throw new AdminManagementException(400,"附件不存在或不属于平台通知");
        long id;int contentVersion=1;
        if(input.id()==null){
            if(input.revisionOf()!=null){var original=get(input.revisionOf(),true);if("DRAFT".equals(original.get("status")))throw new AdminManagementException(400,"草稿请直接编辑");contentVersion=(int)number(original.get("contentVersion"))+1;}
            id=insert("INSERT INTO sys_platform_notice(title,content,important,audience_type,audience_json,created_by,updated_by,expires_at,revision_of,content_version) VALUES(?,?,?,?,?,?,?,?,?,?)",title,content,Boolean.TRUE.equals(input.important())?1:0,mode,encode(selected),actor,actor,input.expiresAt(),input.revisionOf(),contentVersion);
        }else{
            id=input.id();var existing=get(id,true);if(!"DRAFT".equals(existing.get("status")))throw new AdminManagementException(409,"已发布通知不能覆盖正文，请发布更正版本");
            if(input.version()==null||input.version()!=number(existing.get("version")))throw new AdminManagementException(409,"通知已被其他管理员修改，请刷新");
            jdbc.update("UPDATE sys_platform_notice SET title=?,content=?,important=?,audience_type=?,audience_json=?,expires_at=?,updated_by=?,updated_at=NOW(3),version=version+1 WHERE notice_id=?",title,content,Boolean.TRUE.equals(input.important())?1:0,mode,encode(selected),input.expiresAt(),actor,id);
        }
        jdbc.update("DELETE FROM sys_platform_notice_attachment WHERE notice_id=?",id);
        for(Long file:files)jdbc.update("INSERT INTO sys_platform_notice_attachment(notice_id,file_id) VALUES(?,?)",id,file);
        audit("SAVE_DRAFT",id,actor,title,"保存平台通知草稿",Map.of("important",Boolean.TRUE.equals(input.important()),"attachmentCount",files.size()));
        return detail(id,actor);
    }
    @Transactional public Map<String,Object> publish(long id,long version,Long actor){
        admin(actor);var n=get(id,true);if("PUBLISHED".equals(n.get("status")))return detail(id,actor);
        if(!"DRAFT".equals(n.get("status"))||version!=number(n.get("version")))throw new AdminManagementException(409,"通知状态已变化，请刷新后重试");
        if(n.get("revisionOf")!=null){
            long original=number(n.get("revisionOf"));get(original,true);
            if(jdbc.queryForObject("SELECT COUNT(*) FROM sys_platform_notice WHERE revision_of=? AND status='PUBLISHED' AND notice_id<>?",Integer.class,original,id)>0)throw new AdminManagementException(409,"已存在其他更正版本，请基于最新通知修改");
        }
        Object expires=n.get("expiresAt");
        if(expires!=null&&(expires instanceof Timestamp t?t.toLocalDateTime():(LocalDateTime)expires).isBefore(LocalDateTime.now(ZoneId.of("Asia/Shanghai"))))throw new AdminManagementException(400,"通知已过有效时间，请调整后发布");
        var recipients=audience(str(n.get("audienceType")),decode(n.get("audienceJson")));if(recipients.isEmpty())throw new AdminManagementException(400,"接收范围内没有正常账号，不能发布");
        jdbc.update("UPDATE sys_platform_notice SET status='PUBLISHED',published_at=NOW(3),updated_at=NOW(3),updated_by=?,version=version+1 WHERE notice_id=?",actor,id);
        for(var user:recipients){
            long inbox=insert("INSERT INTO sys_notice(from_user_id,to_user_id,type,trigger_event,title,content,source_type,source_id,is_read,is_delete,create_time) VALUES(?,?,'1','PLATFORM_NOTICE',?,?,'5',?,'0',0,NOW())",actor,user.get("id"),n.get("title"),str(n.get("content")).substring(0,Math.min(300,str(n.get("content")).length())),id);
            jdbc.update("INSERT INTO sys_platform_notice_recipient(notice_id,user_id,user_name,dept_id,dept_name,inbox_id) VALUES(?,?,?,?,?,?)",id,user.get("id"),user.get("name"),user.get("deptId"),user.get("deptName"),inbox);
        }
        if(n.get("revisionOf")!=null){
            long original=number(n.get("revisionOf"));
            jdbc.update("UPDATE sys_platform_notice SET status='WITHDRAWN',withdrawn_at=NOW(3),withdraw_reason='已发布更正版本',updated_by=?,updated_at=NOW(3),version=version+1 WHERE notice_id=? AND status='PUBLISHED'",actor,original);
            jdbc.update("UPDATE sys_notice m JOIN sys_platform_notice_recipient r ON r.inbox_id=m.notice_id SET m.is_delete=1 WHERE r.notice_id=?",original);
        }
        audit("PUBLISH",id,actor,str(n.get("title")),"发布平台通知",Map.of("recipientCount",recipients.size(),"important",n.get("important"),"contentVersion",n.get("contentVersion")));
        return detail(id,actor);
    }
    @Transactional public void withdraw(long id,Change input,Long actor){
        admin(actor);String reason=text(input.reason(),500,"撤回原因");var n=get(id,true);
        if("WITHDRAWN".equals(n.get("status")))return;
        if(!"PUBLISHED".equals(n.get("status"))||input.version()==null||input.version()!=number(n.get("version")))throw new AdminManagementException(409,"通知状态已变化，请刷新");
        jdbc.update("UPDATE sys_platform_notice SET status='WITHDRAWN',withdrawn_at=NOW(3),withdraw_reason=?,updated_by=?,updated_at=NOW(3),version=version+1 WHERE notice_id=?",reason,actor,id);
        jdbc.update("UPDATE sys_notice m JOIN sys_platform_notice_recipient r ON r.inbox_id=m.notice_id SET m.is_delete=1 WHERE r.notice_id=?",id);
        audit("WITHDRAW",id,actor,str(n.get("title")),reason,Map.of("status","WITHDRAWN"));
    }
    public Map<String,Object> list(Long actor,int page,String keyword,String status){
        admin(actor);String k=keyword==null?"":keyword.strip();if(k.length()>100||status!=null&&!status.isBlank()&&!Set.of("DRAFT","PUBLISHED","WITHDRAWN").contains(status))throw new AdminManagementException(400,"通知查询条件不正确");
        String where=" WHERE LOCATE(?,n.title)>0"+(status==null||status.isBlank()?"":" AND n.status=?");List<Object> args=new ArrayList<>(List.of(k));if(status!=null&&!status.isBlank())args.add(status);
        long total=jdbc.queryForObject("SELECT COUNT(*) FROM sys_platform_notice n"+where,Long.class,args.toArray());int p=Math.max(1,page);args.add((p-1)*10);
        var rows=jdbc.queryForList("SELECT n.notice_id id,n.title,n.important,n.status,n.version,n.content_version contentVersion,n.published_at publishedAt,n.updated_at updatedAt,n.expires_at expiresAt,(SELECT COUNT(*) FROM sys_platform_notice_recipient r WHERE r.notice_id=n.notice_id) recipientCount,(SELECT COUNT(*) FROM sys_platform_notice_recipient r WHERE r.notice_id=n.notice_id AND r.confirmed_at IS NOT NULL) confirmedCount FROM sys_platform_notice n"+where+" ORDER BY n.notice_id DESC LIMIT 10 OFFSET ?",args.toArray());return Map.of("items",rows,"total",total,"page",p);
    }
    public Map<String,Object> receipts(long id,Long actor,int page,String keyword,String state){
        admin(actor);get(id,false);String k=keyword==null?"":keyword.strip();if(k.length()>100||state!=null&&!Set.of("","confirmed","unconfirmed").contains(state))throw new AdminManagementException(400,"浏览记录筛选不正确");
        String where=" WHERE r.notice_id=? AND (LOCATE(?,r.user_name)>0 OR LOCATE(?,CAST(r.user_id AS CHAR))>0 OR LOCATE(?,COALESCE(r.dept_name,''))>0)"+("confirmed".equals(state)?" AND r.confirmed_at IS NOT NULL":"unconfirmed".equals(state)?" AND r.confirmed_at IS NULL":"");
        var args=new ArrayList<Object>(List.of(id,k,k,k));long total=jdbc.queryForObject("SELECT COUNT(*) FROM sys_platform_notice_recipient r"+where,Long.class,args.toArray());args.add((Math.max(1,page)-1)*20);
        var items=jdbc.queryForList("SELECT r.user_id userId,r.user_name userName,r.dept_name deptName,r.first_opened_at firstOpenedAt,r.last_opened_at lastOpenedAt,r.open_count openCount,r.confirmed_at confirmedAt,r.confirmed_version confirmedVersion FROM sys_platform_notice_recipient r"+where+" ORDER BY r.user_id LIMIT 20 OFFSET ?",args.toArray());
        var summary=jdbc.queryForMap("SELECT COUNT(*) total,SUM(confirmed_at IS NOT NULL) confirmed,SUM(first_opened_at IS NOT NULL) opened FROM sys_platform_notice_recipient WHERE notice_id=?",id);
        return Map.of("items",items,"total",total,"summary",summary);
    }
    public List<Map<String,Object>> events(long id,long recipient,Long actor){admin(actor);get(id,false);return jdbc.queryForList("SELECT action,content_version contentVersion,create_time createTime FROM sys_platform_notice_view_log WHERE notice_id=? AND user_id=? ORDER BY log_id DESC LIMIT 200",id,recipient);}
    public byte[] export(long id,Long actor)throws Exception{
        admin(actor);var n=get(id,false);var rows=jdbc.queryForList("SELECT user_id,user_name,dept_name,first_opened_at,last_opened_at,open_count,confirmed_at,confirmed_version FROM sys_platform_notice_recipient WHERE notice_id=? ORDER BY user_id",id);
        try(var book=new XSSFWorkbook();var out=new ByteArrayOutputStream()){
            var sheet=book.createSheet("通知浏览记录");String[] titles={"工号","姓名","发布时所属部门","首次打开时间","最近打开时间","查看次数","确认已阅时间","确认版本"};var head=sheet.createRow(0);for(int i=0;i<titles.length;i++)head.createCell(i).setCellValue(titles[i]);
            String[] fields={"user_id","user_name","dept_name","first_opened_at","last_opened_at","open_count","confirmed_at","confirmed_version"};int index=1;for(var item:rows){var row=sheet.createRow(index++);for(int i=0;i<fields.length;i++)row.createCell(i).setCellValue(str(item.get(fields[i])));}
            sheet.createFreezePane(0,1);for(int i=0;i<8;i++)sheet.setColumnWidth(i,(i==2?40:i==0||i==1?16:24)*256);book.write(out);return out.toByteArray();
        }
    }
    public long loginCutoff(String token){var decoded=JWTUtil.verifyJwtToken(token);Long explicit=decoded.getClaim("loginAt").asLong();return explicit!=null?explicit:decoded.getExpiresAt().getTime()-3600000L;}
    public List<Map<String,Object>> pending(Long actor,String token){
        if(!installed())return List.of();
        return jdbc.queryForList("SELECT n.notice_id id,n.title,n.content_version contentVersion,n.published_at publishedAt FROM sys_platform_notice_recipient r JOIN sys_platform_notice n ON n.notice_id=r.notice_id WHERE r.user_id=? AND r.confirmed_at IS NULL AND n.important=1 AND "+ACTIVE+" AND n.published_at<=? ORDER BY n.published_at,n.notice_id",actor,new Timestamp(loginCutoff(token)));
    }
    public boolean mustRead(Long actor,String token){
        if(!installed())return false;
        return jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM sys_platform_notice_recipient r JOIN sys_platform_notice n ON n.notice_id=r.notice_id WHERE r.user_id=? AND r.confirmed_at IS NULL AND n.important=1 AND "+ACTIVE+" AND n.published_at<=?)",Integer.class,actor,new Timestamp(loginCutoff(token)))==1;
    }
    private Map<String,Object> receipt(long id,Long actor,boolean lock){var rows=jdbc.queryForList("SELECT * FROM sys_platform_notice_recipient WHERE notice_id=? AND user_id=?"+(lock?" FOR UPDATE":""),id,actor);if(rows.isEmpty())throw new AdminManagementException(404,"通知不存在或未向当前账号发布");return rows.get(0);}
    @Transactional public Map<String,Object> open(long id,ReadAction input,Long actor){
        var n=get(id,true);receipt(id,actor,true);if(!"PUBLISHED".equals(n.get("status")))throw new AdminManagementException(409,"通知已撤回或未发布，请刷新");
        String key=text(input.requestId(),64,"浏览请求编号");String view=UUID.randomUUID().toString();
        int added=jdbc.update("INSERT IGNORE INTO sys_platform_notice_view_log(notice_id,user_id,action,request_key,content_version) VALUES(?,?,'OPEN',?,?)",id,actor,key,n.get("contentVersion"));
        if(added==1)jdbc.update("UPDATE sys_platform_notice_recipient SET first_opened_at=COALESCE(first_opened_at,NOW(3)),last_opened_at=NOW(3),open_count=open_count+1,view_key=COALESCE(view_key,?) WHERE notice_id=? AND user_id=?",view,id,actor);
        var r=receipt(id,actor,false);n.remove("audienceJson");n.put("attachments",attachments(id));n.put("viewKey",r.get("view_key"));n.put("confirmedAt",r.get("confirmed_at"));return n;
    }
    @Transactional public void confirm(long id,ReadAction input,Long actor){
        var n=get(id,true);var r=receipt(id,actor,true);
        if(!"PUBLISHED".equals(n.get("status")))throw new AdminManagementException(409,"通知状态已变化，请重新加载");
        if(r.get("confirmed_at")!=null){markInboxRead(id,actor,r);return;}
        if(input.contentVersion()==null||input.contentVersion()!=number(n.get("contentVersion")))throw new AdminManagementException(409,"通知版本已变化，请重新阅读");
        if(r.get("first_opened_at")==null||input.viewKey()==null||!input.viewKey().equals(r.get("view_key")))throw new AdminManagementException(409,"请先打开并阅读通知，再确认已阅");
        jdbc.update("UPDATE sys_platform_notice_recipient SET confirmed_at=NOW(3),confirmed_version=? WHERE notice_id=? AND user_id=?",n.get("contentVersion"),id,actor);
        jdbc.update("INSERT INTO sys_platform_notice_view_log(notice_id,user_id,action,request_key,content_version) VALUES(?,?,'CONFIRM',?,?)",id,actor,UUID.randomUUID().toString(),n.get("contentVersion"));
        markInboxRead(id,actor,r);
    }
    /** 确认记录与收件箱已读状态对齐：即使之前已确认过，也把消息中心里的那条标记为已读。 */
    private void markInboxRead(long id,Long actor,Map<String,Object> receipt){
        Object inbox=receipt.get("inbox_id");
        if(inbox!=null)jdbc.update("UPDATE sys_notice SET is_read='1' WHERE notice_id=? AND to_user_id=? AND COALESCE(is_read,'0')<>'1'",inbox,actor);
        jdbc.update("UPDATE sys_notice SET is_read='1' WHERE source_type='5' AND source_id=? AND to_user_id=? AND COALESCE(is_read,'0')<>'1'",id,actor);
    }
    public boolean protectedInbox(long inbox,Long actor){if(!installed())return false;return jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM sys_platform_notice_recipient r JOIN sys_platform_notice n ON n.notice_id=r.notice_id WHERE r.inbox_id=? AND r.user_id=? AND r.confirmed_at IS NULL AND n.important=1 AND "+ACTIVE+")",Integer.class,inbox,actor)==1;}
    public long upload(MultipartFile file,Long actor)throws Exception{
        admin(actor);String name=Objects.toString(file.getOriginalFilename(),"").replace('\\','/');name=name.substring(name.lastIndexOf('/')+1);
        if(file.isEmpty()||file.getSize()>20*1024*1024||name.length()>150||!name.toLowerCase(Locale.ROOT).matches(".+\\.(pdf|doc|docx|xls|xlsx|png|jpg|jpeg)"))throw new AdminManagementException(400,"附件支持PDF、Word、Excel及图片，单份不超过20MB");
        String suffix=name.substring(name.lastIndexOf('.')+1).toLowerCase(Locale.ROOT);String location="/uploads/platform-notices/"+UUID.randomUUID()+"."+suffix;
        Path path=Path.of(System.getProperty("user.dir")).resolve(location.substring(1));Files.createDirectories(path.getParent());
        try(var stream=file.getInputStream()){Files.copy(stream,path);}try{return insert("INSERT INTO sys_file(file_name,file_path,file_url,file_suffix,file_size,upload_by,upload_time,is_delete) VALUES(?,?,?,?,?,?,NOW(),0)",name,location,location,suffix,file.getSize(),actor);}catch(RuntimeException e){Files.deleteIfExists(path);throw e;}
    }
    public void requireAttachment(long id,long file,Long actor){receipt(id,actor,false);if(!"PUBLISHED".equals(get(id,false).get("status"))||jdbc.queryForObject("SELECT COUNT(*) FROM sys_platform_notice_attachment WHERE notice_id=? AND file_id=?",Integer.class,id,file)!=1)throw new AdminManagementException(404,"通知附件不存在或无权下载");}
}
