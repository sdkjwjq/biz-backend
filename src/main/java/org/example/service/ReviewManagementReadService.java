package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.example.service.ReviewRoundService.map;

/** Administrative read model. Drafts are deliberately never selected. */
@Service
@Transactional(readOnly=true)
public class ReviewManagementReadService {
 private final JdbcTemplate db;
 private final PermissionService access;
 private final ReviewRoundService rounds;
 private static final String LATEST="NOT EXISTS(SELECT 1 FROM biz_round_revision newer WHERE newer.opinion_id=v.opinion_id AND newer.revision_no>v.revision_no)";
 private static final String ACTOR="COALESCE(NULLIF(u.nick_name,''),u.user_name,CAST(x.operator_id AS CHAR))";
 public ReviewManagementReadService(JdbcTemplate db,PermissionService access,ReviewRoundService rounds){this.db=db;this.access=access;this.rounds=rounds;}
 private void require(long actor,long round){access.requireAdmin(actor);rounds.adminDetail(actor,round);}
 private void page(int page,int size){if(page<1||page>100000||size<1||size>100)throw new AdminManagementException(400,"分页参数不正确");}
 private String text(String value){if(value==null)return "";if(value.length()>200)throw new AdminManagementException(400,"查询文字最多200字");return value.trim();}
 private String like(String value){return "%"+value.replace("!","!!").replace("%","!%").replace("_","!_")+"%";}
 public Map<String,Object> overview(long actor,long id){
  var round=rounds.adminDetail(actor,id);
  var summary=db.queryForMap("SELECT COUNT(*) opinionCount,COUNT(DISTINCT v.expert_id) submittedExperts,COALESCE(SUM(v.task_level=3),0) leafOpinions,COALESCE(SUM(v.task_level=1),0) overallOpinions,COALESCE(SUM(v.review_result='pass'),0) passed,COALESCE(SUM(v.review_result='reject'),0) rejected FROM biz_round_revision v WHERE v.round_id=? AND "+LATEST,id);
  summary.put("revisionCount",db.queryForObject("SELECT COUNT(*) FROM biz_round_revision WHERE round_id=?",Long.class,id));
  summary.put("activeExperts",db.queryForObject("SELECT COUNT(*) FROM biz_review_member WHERE round_id=? AND active=1",Long.class,id));
  summary.put("assignedTasks",db.queryForObject("SELECT COUNT(DISTINCT s.task_id) FROM biz_review_scope s JOIN biz_review_member m ON m.round_id=s.round_id AND m.user_id=s.user_id AND m.active=1 JOIN biz_task t ON t.task_id=s.task_id AND t.level=3 AND COALESCE(t.is_delete,0)=0 JOIN biz_review_round r ON r.round_id=s.round_id WHERE s.round_id=? AND t.phase BETWEEN 2025 AND 2029 AND (r.review_year IS NULL OR t.phase=r.review_year)",Long.class,id));
  // Include removed experts and administrator contributions so no formal record disappears.
  var experts=db.queryForList("SELECT e.expert_id expertId,COALESCE(NULLIF(u.nick_name,''),u.user_name,CAST(e.expert_id AS CHAR)) expertName,COALESCE(m.active,0) active,CASE WHEN u.role='0' AND u.status='1' AND COALESCE(u.is_delete,0)=0 THEN 1 ELSE 0 END administrator,CASE WHEN m.active=1 THEN (SELECT COUNT(*) FROM biz_review_scope s JOIN biz_task t ON t.task_id=s.task_id JOIN biz_review_round r ON r.round_id=s.round_id WHERE s.round_id=? AND s.user_id=e.expert_id AND t.level=3 AND COALESCE(t.is_delete,0)=0 AND t.phase BETWEEN 2025 AND 2029 AND (r.review_year IS NULL OR t.phase=r.review_year)) ELSE 0 END taskCount,COALESCE(f.leafCount,0) leafCount,COALESCE(f.overallCount,0) overallCount,f.lastSubmitted FROM (SELECT user_id expert_id FROM biz_review_member WHERE round_id=? UNION SELECT expert_id FROM biz_round_revision WHERE round_id=?) e LEFT JOIN sys_user u ON u.user_id=e.expert_id LEFT JOIN biz_review_member m ON m.round_id=? AND m.user_id=e.expert_id LEFT JOIN (SELECT v.expert_id,SUM(v.task_level=3) leafCount,SUM(v.task_level=1) overallCount,MAX(v.create_time) lastSubmitted FROM biz_round_revision v WHERE v.round_id=? AND "+LATEST+" GROUP BY v.expert_id) f ON f.expert_id=e.expert_id ORDER BY e.expert_id",id,id,id,id,id);
  return map("round",round,"summary",summary,"experts",experts);
 }
 public Map<String,Object> opinions(long actor,long id,Long expert,Integer level,String result,String keyword,boolean allVersions,int page,int size){
  require(actor,id);page(page,size);
  if(expert!=null&&expert<=0||level!=null&&!Set.of(1,3).contains(level)||result!=null&&!Set.of("","pass","reject").contains(result))throw new AdminManagementException(400,"评语查询条件不正确");
  String where="v.round_id=?";List<Object> args=new ArrayList<>(List.of(id));
  if(!allVersions)where+=" AND "+LATEST;
  if(expert!=null){where+=" AND v.expert_id=?";args.add(expert);}
  if(level!=null){where+=" AND v.task_level=?";args.add(level);}
  if(result!=null&&!result.isBlank()){where+=" AND v.review_result=?";args.add(result);}
  String term=text(keyword);if(!term.isEmpty()){where+=" AND (v.task_code LIKE ? ESCAPE '!' OR v.task_name LIKE ? ESCAPE '!' OR v.review_text LIKE ? ESCAPE '!')";for(int n=0;n<3;n++)args.add(like(term));}
  long total=db.queryForObject("SELECT COUNT(*) FROM biz_round_revision v WHERE "+where,Long.class,args.toArray());
  args.add(size);args.add((page-1)*size);
  var rows=db.queryForList("SELECT v.revision_id,v.opinion_id,v.expert_id,v.expert_name,v.task_id,v.task_code,v.task_name,v.task_level,v.revision_no,v.review_result,v.create_time,LEFT(v.review_text,160) preview,CHAR_LENGTH(v.review_text) textLength,("+LATEST+") currentVersion FROM biz_round_revision v WHERE "+where+" ORDER BY v.create_time DESC,v.revision_id DESC LIMIT ? OFFSET ?",args.toArray());
  return map("items",rows,"total",total,"page",page,"pageSize",size);
 }
 public Map<String,Object> opinion(long actor,long id,long revision){
  require(actor,id);
  var rows=db.queryForList("SELECT v.*,COALESCE(NULLIF(u.nick_name,''),u.user_name,CAST(v.operator_id AS CHAR)) operatorName FROM biz_round_revision v LEFT JOIN sys_user u ON u.user_id=v.operator_id WHERE v.round_id=? AND v.revision_id=?",id,revision);
  if(rows.isEmpty())throw new AdminManagementException(404,"正式评语不存在或不属于本评审任务");
  var row=rows.get(0);row.put("scope",access.read(row.remove("scope_json"),new TypeReference<Map<String,Object>>(){}));
  row.put("revisions",db.queryForList("SELECT revision_id,revision_no,review_result,create_time FROM biz_round_revision WHERE round_id=? AND opinion_id=? ORDER BY revision_no DESC",id,row.get("opinion_id")));
  return row;
 }
 private String events(){return "(SELECT 'management' COLLATE utf8mb4_unicode_ci type,l.log_id id,l.action COLLATE utf8mb4_unicode_ci action,l.operator_id,l.reason COLLATE utf8mb4_unicode_ci reason,l.create_time,'' COLLATE utf8mb4_unicode_ci task_code,l.target_name COLLATE utf8mb4_unicode_ci task_name FROM sys_admin_operation_log l WHERE l.module='EXPERT_REVIEW' AND l.target_type='REVIEW_ROUND' AND l.target_id=? UNION ALL SELECT 'submission' COLLATE utf8mb4_unicode_ci type,v.revision_id id,IF(v.revision_no=1,'OPINION_SUBMIT','OPINION_RESUBMIT') COLLATE utf8mb4_unicode_ci action,v.operator_id,CONCAT('正式意见 V',v.revision_no) COLLATE utf8mb4_unicode_ci reason,v.create_time,v.task_code COLLATE utf8mb4_unicode_ci,v.task_name COLLATE utf8mb4_unicode_ci FROM biz_round_revision v WHERE v.round_id=?) x";}
 public Map<String,Object> activity(long actor,long id,String type,int page,int size){
  require(actor,id);page(page,size);if(type==null)type="";if(!Set.of("","management","submission").contains(type))throw new AdminManagementException(400,"日志类型不正确");
  String where=type.isEmpty()?"":" WHERE x.type=?";List<Object> args=new ArrayList<>(List.of(String.valueOf(id),id));if(!type.isEmpty())args.add(type);
  long total=db.queryForObject("SELECT COUNT(*) FROM "+events()+where,Long.class,args.toArray());args.add(size);args.add((page-1)*size);
  var rows=db.queryForList("SELECT x.*,"+ACTOR+" operatorName FROM "+events()+" LEFT JOIN sys_user u ON u.user_id=x.operator_id"+where+" ORDER BY x.create_time DESC,x.type,x.id DESC LIMIT ? OFFSET ?",args.toArray());
  return map("items",rows,"total",total,"page",page,"pageSize",size);
 }
 public Map<String,Object> activityDetail(long actor,long id,String type,long log){
  require(actor,id);
  if("submission".equals(type))return opinion(actor,id,log);
  if(!"management".equals(type))throw new AdminManagementException(400,"日志类型不正确");
  var rows=db.queryForList("SELECT l.log_id,l.action,l.reason,l.operator_id,l.create_time,l.before_json,l.after_json,COALESCE(NULLIF(u.nick_name,''),u.user_name,CAST(l.operator_id AS CHAR)) operatorName FROM sys_admin_operation_log l LEFT JOIN sys_user u ON u.user_id=l.operator_id WHERE l.module='EXPERT_REVIEW' AND l.target_type='REVIEW_ROUND' AND l.target_id=? AND l.log_id=?",String.valueOf(id),log);
  if(rows.isEmpty())throw new AdminManagementException(404,"操作记录不存在或不属于本评审任务");
  return rows.get(0);
 }
}
