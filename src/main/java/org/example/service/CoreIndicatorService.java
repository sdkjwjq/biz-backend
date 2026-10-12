package org.example.service;

import org.example.entity.SysNotice;
import org.example.entity.SysUser;
import org.example.mapper.SysMapper;
import org.example.mapper.WorkRecordMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

/** 核心指标采用独立数据与状态机，正式填报每次保留，不反写绩效。 */
@Service
public class CoreIndicatorService {
 private static final long ARCHIVER=110228L;
 private static final String SELECT="SELECT r.report_id reportId,r.indicator_id indicatorId,r.record_year year,r.record_month month,r.indicator_name indicatorName,r.target_value targetValue,r.data_type dataType,r.previous_year_value previousYearValue,r.actual_value actualValue,r.comment,r.submit_by submitBy,r.submit_name submitName,r.principal_id principalId,r.archive_handler_id archiveHandlerId,r.current_handler_id currentHandlerId,r.flow_status flowStatus,r.version,r.submit_time submitTime,r.archive_time archiveTime FROM biz_core_report r ";
 private final JdbcTemplate jdbc; private final SysMapper users; private final WorkRecordMapper responsibilities; private final LeadershipAccessService access;
 public CoreIndicatorService(JdbcTemplate jdbc,SysMapper users,WorkRecordMapper responsibilities,LeadershipAccessService access){this.jdbc=jdbc;this.users=users;this.responsibilities=responsibilities;this.access=access;}
 public record Fill(Integer year,Integer month,Long version,BigDecimal previousYearValue,BigDecimal actualValue,String comment){}
 public record Decision(Long version,Boolean pass,String comment){}
 private SysUser user(Long id){SysUser user=id==null?null:users.getUserById(id);if(user==null||!"1".equals(user.getStatus())||Integer.valueOf(1).equals(user.getIsDelete()))throw new TaskManagementException(403,"账号不存在或已停用");return user;}
 private boolean reviewer(Long id){return jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM biz_performance p JOIN biz_core_indicator c ON BINARY c.indicator_code=BINARY p.perf_code WHERE p.principal_id=? AND COALESCE(p.is_delete,0)=0)",Integer.class,id)==1
   || jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM biz_core_report WHERE current_handler_id=? AND flow_status IN(10,20))",Integer.class,id)==1;}
 private void readable(Long id){SysUser u=user(id);if(!"0".equals(u.getRole())&&!access.isLeader(id)&&responsibilities.fillableYears(id).isEmpty()&&!reviewer(id))throw new TaskManagementException(403,"没有核心指标查阅权限");}
 public Map<String,Object> capabilities(Long id){SysUser u=user(id);List<Integer> years=responsibilities.fillableYears(id);return Map.of("canView","0".equals(u.getRole())||access.isLeader(id)||!years.isEmpty()||reviewer(id),"fillableYears",years,"canFill",!years.isEmpty());}
 private void period(int year,int month){if(year<2025||year>2029||month<1||month>12||YearMonth.of(year,month).isAfter(YearMonth.now(ZoneId.of("Asia/Shanghai"))))throw new TaskManagementException(400,"请选择2025—2029年内非未来的填报月份");}
 private void filler(Long id,int year){user(id);if(!responsibilities.fillableYears(id).contains(year))throw new TaskManagementException(403,"仅该年度有效三级任务或四级目标责任人（专业群）可填报");}
 private Map<String,Object> mapping(Long id,boolean lock){List<Map<String,Object>> rows=jdbc.queryForList("SELECT indicator_id indicatorId,indicator_code indicatorCode,sort_order sortOrder FROM biz_core_indicator WHERE indicator_id=?"+(lock?" FOR UPDATE":""),id);if(rows.isEmpty())throw new TaskManagementException(404,"核心指标不存在");return rows.get(0);}
 private Map<String,Object> performance(Map<String,Object> mapping){return performance(mapping,false);}
 private Map<String,Object> performance(Map<String,Object> mapping,boolean lock){List<Map<String,Object>> rows=jdbc.queryForList("SELECT perf_id perfId,perf_name indicatorName,target_value targetValue,data_type dataType,principal_id principalId FROM biz_performance WHERE perf_code=? AND COALESCE(is_delete,0)=0"+(lock?" FOR UPDATE":""),mapping.get("indicatorCode"));if(rows.size()!=1)throw new TaskManagementException(409,"指标"+mapping.get("indicatorCode")+"对应绩效缺失或重复，请联系管理员核实");return rows.get(0);}
 private long number(Object value){return ((Number)value).longValue();}
 private int state(Map<String,Object> row){return ((Number)row.get("flowStatus")).intValue();}
 private Map<String,Object> row(Long id,boolean lock){List<Map<String,Object>> rows=jdbc.queryForList(SELECT+"WHERE r.report_id=?"+(lock?" FOR UPDATE":""),id);if(rows.isEmpty())throw new TaskManagementException(404,"核心指标填报不存在");return rows.get(0);}
 private Map<String,Object> locked(Long id){Map<String,Object> initial=row(id,false);mapping(number(initial.get("indicatorId")),true);return row(id,true);}
 private void version(Map<String,Object> row,Long version){if(version==null||number(row.get("version"))!=version)throw new TaskManagementException(409,"记录已变更，请重新加载");}
 public Map<String,Object> detail(Long id,Long actor){user(actor);Map<String,Object> row=row(id,false);if(state(row)==0||state(row)==-2){if(number(row.get("submitBy"))!=actor)throw new TaskManagementException(404,"填报不存在或无权查看");}else if(number(row.get("submitBy"))!=actor && (row.get("principalId")==null || number(row.get("principalId"))!=actor) && !access.isAdmin(actor))readable(actor);Map<String,Object> result=new LinkedHashMap<>(row);result.put("logs",jdbc.queryForList("SELECT l.log_id logId,l.operator_id operatorId,COALESCE(u.nick_name,u.user_name) operatorName,l.action,l.pre_status preStatus,l.post_status postStatus,l.comment,l.create_time createTime FROM biz_core_audit_log l LEFT JOIN sys_user u ON u.user_id=l.operator_id WHERE l.report_id=? ORDER BY l.log_id",id));return result;}
 public List<Map<String,Object>> list(Long actor,Integer year,Integer month){readable(actor);if(year!=null&&(year<2025||year>2029)||month!=null&&(month<1||month>12))throw new TaskManagementException(400,"查询年月不正确");List<Map<String,Object>> result=new ArrayList<>();for(Map<String,Object> m:jdbc.queryForList("SELECT indicator_id indicatorId,indicator_code indicatorCode,sort_order sortOrder FROM biz_core_indicator ORDER BY sort_order")){Map<String,Object> item=new LinkedHashMap<>(m);try{item.putAll(performance(m));}catch(TaskManagementException error){item.put("configurationError",error.getMessage());}
   String where="WHERE r.indicator_id=? AND r.submit_time IS NOT NULL"+(year==null?"":" AND r.record_year="+year)+(month==null?"":" AND r.record_month="+month);
   item.put("recentReports",jdbc.queryForList(SELECT+where+" ORDER BY r.submit_time DESC,r.report_id DESC LIMIT 2",m.get("indicatorId")));
   List<Map<String,Object>> archived=jdbc.queryForList(SELECT+where+" AND r.flow_status=30 ORDER BY r.record_year DESC,r.record_month DESC,r.archive_time DESC,r.report_id DESC LIMIT 1",m.get("indicatorId"));item.put("effectiveResult",archived.isEmpty()?null:archived.get(0));result.add(item);}return result;}
 public List<Map<String,Object>> history(Long indicator,Long actor,Integer year,Integer month){readable(actor);mapping(indicator,false);if(year!=null&&(year<2025||year>2029)||month!=null&&(month<1||month>12))throw new TaskManagementException(400,"查询年月不正确");return jdbc.queryForList(SELECT+"WHERE r.indicator_id=? AND r.submit_time IS NOT NULL"+(year==null?"":" AND r.record_year="+year)+(month==null?"":" AND r.record_month="+month)+" ORDER BY r.submit_time DESC,r.report_id DESC",indicator);}
 @Transactional(rollbackFor=Exception.class)
 public Map<String,Object> draft(Long indicator,Long actor,int year,int month){
  period(year,month);filler(actor,year);
  Map<String,Object> p=performance(mapping(indicator,true));
  String key=indicator+":"+year+":"+month+":"+actor;
  List<Long> old=jdbc.queryForList("SELECT report_id FROM biz_core_report WHERE draft_key=? FOR UPDATE",Long.class,key);
  if(!old.isEmpty())return detail(old.get(0),actor);
  // 退回、撤回后的重新填报复制本人原内容；原正式记录与审核证据始终保留。
  List<Map<String,Object>> returned=jdbc.queryForList(SELECT+"WHERE r.indicator_id=? AND r.record_year=? AND r.record_month=? AND r.submit_by=? AND r.submit_time IS NOT NULL AND r.flow_status IN(-1,-10,-20) ORDER BY r.report_id DESC LIMIT 1 FOR UPDATE",indicator,year,month,actor);
  Map<String,Object> previous=returned.isEmpty()?Collections.emptyMap():returned.get(0);
  SysUser owner=user(actor);
  jdbc.update("INSERT INTO biz_core_report(indicator_id,record_year,record_month,draft_key,indicator_name,target_value,data_type,submit_by,submit_name,previous_year_value,actual_value,comment) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",indicator,year,month,key,p.get("indicatorName"),p.get("targetValue"),p.get("dataType"),actor,owner.getNickName(),previous.get("previousYearValue"),previous.get("actualValue"),previous.getOrDefault("comment",""));
  return detail(jdbc.queryForObject("SELECT report_id FROM biz_core_report WHERE draft_key=?",Long.class,key),actor);
 }
 private BigDecimal value(BigDecimal value,boolean required){if(value==null){if(required)throw new TaskManagementException(400,"请填写两项累计完成值");return null;}if(value.signum()<0||value.stripTrailingZeros().scale()>4||value.precision()-value.scale()>16)throw new TaskManagementException(400,"完成值必须为非负数，最多4位小数及16位整数");return value;}
 @Transactional(rollbackFor=Exception.class)
 public Map<String,Object> save(Long id,Long actor,Fill input){if(input==null)throw new TaskManagementException(400,"请提供填报内容");Map<String,Object> old=locked(id);filler(actor,((Number)old.get("year")).intValue());version(old,input.version());if(number(old.get("submitBy"))!=actor||state(old)!=0)throw new TaskManagementException(403,"只能编辑本人草稿");String comment=input.comment()==null?"":input.comment();if(comment.codePointCount(0,comment.length())>500)throw new TaskManagementException(400,"工作说明最多500字");jdbc.update("UPDATE biz_core_report SET previous_year_value=?,actual_value=?,comment=?,version=version+1,update_time=NOW(3) WHERE report_id=?",value(input.previousYearValue(),false),value(input.actualValue(),false),comment,id);return detail(id,actor);}
 @Transactional(rollbackFor=Exception.class)
 public Map<String,Object> submit(Long id,Long actor,Long expectedVersion){Map<String,Object> draft=locked(id);int year=((Number)draft.get("year")).intValue(),month=((Number)draft.get("month")).intValue();period(year,month);filler(actor,year);version(draft,expectedVersion);if(number(draft.get("submitBy"))!=actor||state(draft)!=0)throw new TaskManagementException(409,"只能提交本人未提交草稿");value((BigDecimal)draft.get("actualValue"),true);value((BigDecimal)draft.get("previousYearValue"),true);
  Long indicator=number(draft.get("indicatorId"));Map<String,Object> p=performance(mapping(indicator,false),true);Long principal=p.get("principalId")==null?null:number(p.get("principalId"));if(principal==null)throw new TaskManagementException(409,"未配置归口审核人，请联系管理员");
  try{user(principal);}catch(TaskManagementException error){throw new TaskManagementException(409,"归口审核人缺失或失效，请联系管理员");}if(!access.isAdmin(ARCHIVER))throw new TaskManagementException(409,"双高办归档账号缺失或失效，请联系管理员");
  if(!jdbc.queryForList("SELECT report_id FROM biz_core_report WHERE indicator_id=? AND record_year=? AND record_month=? AND flow_status IN(10,20) FOR UPDATE",Long.class,indicator,year,month).isEmpty())throw new TaskManagementException(409,"该指标本月已有待审填报，请先处理或由提交人撤回");
  jdbc.update("INSERT INTO biz_core_report(indicator_id,record_year,record_month,source_draft_id,indicator_name,target_value,data_type,previous_year_value,actual_value,comment,submit_by,submit_name,principal_id,archive_handler_id,current_handler_id,flow_status,submit_time) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,10,NOW(3))",indicator,year,month,id,p.get("indicatorName"),p.get("targetValue"),p.get("dataType"),draft.get("previousYearValue"),draft.get("actualValue"),draft.get("comment"),actor,user(actor).getNickName(),principal,ARCHIVER,principal);
  long submitted=jdbc.queryForObject("SELECT report_id FROM biz_core_report WHERE source_draft_id=?",Long.class,id);
  jdbc.update("UPDATE biz_core_report SET flow_status=-2,draft_key=NULL,version=version+1,update_time=NOW(3) WHERE report_id=?",id);log(submitted,actor,"提交",0,10,"");notice(actor,principal,submitted,true,"核心指标待归口审核");return detail(submitted,actor);}
 @Transactional(rollbackFor=Exception.class)
 public Map<String,Object> audit(Long id,Long actor,Decision input){user(actor);if(input==null||input.pass()==null)throw new TaskManagementException(400,"请选择审核结论");Map<String,Object> report=locked(id);version(report,input.version());int before=state(report);if(before!=10&&before!=20)throw new TaskManagementException(409,"该填报已处理，请刷新");boolean allowed=before==10?report.get("currentHandlerId")!=null && number(report.get("currentHandlerId"))==actor:(access.isAdmin(actor));if(!allowed)throw new TaskManagementException(403,"您不是当前审核处理人");String comment=input.comment()==null?"":input.comment().strip();if(comment.codePointCount(0,comment.length())>500||(!input.pass()&&comment.isBlank()))throw new TaskManagementException(400,"退回需填写原因，审核意见最多500字");int after=input.pass()?(before==10?20:30):-before;Long next=input.pass()&&before==10?number(report.get("archiveHandlerId")):null;if(next!=null)user(next);
  jdbc.update("UPDATE biz_core_report SET flow_status=?,current_handler_id=?,archive_time=IF(?=30,NOW(3),archive_time),version=version+1,update_time=NOW(3) WHERE report_id=?",after,next,after,id);log(id,actor,input.pass()?"通过":"退回",before,after,comment);expireNotices(id);notice(actor,next==null?number(report.get("submitBy")):next,id,next!=null,next!=null?"核心指标待双高办归档":after==30?"核心指标已归档":"核心指标已退回");return detail(id,actor);}
 @Transactional(rollbackFor=Exception.class)
 public Map<String,Object> withdraw(Long id,Long actor,Long expected){user(actor);Map<String,Object> report=locked(id);version(report,expected);if(number(report.get("submitBy"))!=actor)throw new TaskManagementException(403,"仅本次提交人可以撤回");if(state(report)!=10||jdbc.queryForObject("SELECT COUNT(*) FROM biz_core_audit_log WHERE report_id=? AND action<>'提交'",Integer.class,id)>0)throw new TaskManagementException(409,"仅尚无审核动作的待审记录可以撤回");jdbc.update("UPDATE biz_core_report SET flow_status=-1,current_handler_id=NULL,version=version+1,update_time=NOW(3) WHERE report_id=?",id);log(id,actor,"撤回",10,-1,"");expireNotices(id);return detail(id,actor);}
 public List<Map<String,Object>> audits(Long actor,boolean todo){SysUser u=user(actor);String condition=todo?("0".equals(u.getRole())?"(r.current_handler_id=? OR r.flow_status=20) AND r.flow_status IN(10,20)":"r.current_handler_id=? AND r.flow_status IN(10,20)"):"EXISTS(SELECT 1 FROM biz_core_audit_log l WHERE l.report_id=r.report_id AND l.operator_id=? AND l.action IN('通过','退回'))";return jdbc.queryForList(SELECT+"WHERE "+condition+" ORDER BY r.submit_time,r.report_id",actor);}
 /** 预算读取只能取同年、不晚于所选月份的归档快照，后续月份不得提前引用。 */
 public Map<String,Object> effective(Long indicator,int year,int month){List<Map<String,Object>> rows=jdbc.queryForList(SELECT+"WHERE r.indicator_id=? AND r.record_year=? AND r.record_month<=? AND r.flow_status=30 ORDER BY r.record_month DESC,r.archive_time DESC,r.report_id DESC LIMIT 1",indicator,year,month);return rows.isEmpty()?null:rows.get(0);}
 private void log(Long id,Long actor,String action,int before,int after,String comment){jdbc.update("INSERT INTO biz_core_audit_log(report_id,operator_id,action,pre_status,post_status,comment) VALUES(?,?,?,?,?,?)",id,actor,action,before,after,comment);}
 private void expireNotices(Long id){jdbc.update("UPDATE sys_notice SET is_delete=1 WHERE source_type='4' AND source_id=? AND trigger_event='CORE_TODO' AND COALESCE(is_delete,0)=0",id);}
 private void notice(Long from,Long to,Long id,boolean todo,String title){SysNotice notice=new SysNotice();notice.setFromUserId(from);notice.setToUserId(to);notice.setType("1");notice.setTriggerEvent(todo?"CORE_TODO":"CORE_RESULT");notice.setTitle(title);notice.setContent(title+"，请查看核心指标填报记录");notice.setSourceType("4");notice.setSourceId(id);notice.setIsRead("0");notice.setIsDelete(0);notice.setCreateTime(new Date());users.sendNotice(notice);}
}
