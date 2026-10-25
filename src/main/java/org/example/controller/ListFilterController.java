package org.example.controller;
import org.example.service.*;
import org.example.entity.vo.ErrorVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
@RestController
@RequestMapping("/list-filters")
public class ListFilterController {
 private final BusinessListFilterService filters;private final PermissionService access;
 public ListFilterController(BusinessListFilterService filters,PermissionService access){this.filters=filters;this.access=access;}
 @org.springframework.beans.factory.annotation.Autowired private TaskExportService taskExport;
 @org.springframework.beans.factory.annotation.Autowired private AuditListPageService audits;
 @org.springframework.beans.factory.annotation.Autowired private NoticeFilterPageService notices;
 @GetMapping("/tasks/export")public void exportTasks(@RequestParam MultiValueMap<String,String> params,jakarta.servlet.http.HttpServletResponse response)throws java.io.IOException{
  Long actor=access.actor();access.require(actor,"tasks.export");var result=filters.query("tasks",actor,params);
  var ids=((java.util.List<?>)result.get("ids")).stream().map(v->Long.valueOf(v.toString())).toList();if(ids.isEmpty())throw new AdminManagementException(400,"当前筛选没有可导出的任务");taskExport.exportTasks(ids,actor,response);
 }
 @GetMapping("/admin-users/filter-options")public Object accountOptions(@RequestParam MultiValueMap<String,String> params){
  return params.containsKey("field")||"true".equals(params.getFirst("definitions"))?filters.filterOptions("admin-users",access.actor(),params):filters.accountOptions(access.actor());
 }
 @GetMapping("/audits/locate")public Object locateAudit(@RequestParam MultiValueMap<String,String> params){return audits.locate(access.actor(),params);}
 @GetMapping("/{domain}/filter-options")public Object options(@PathVariable String domain,@RequestParam MultiValueMap<String,String> params){return filters.filterOptions(domain,access.actor(),params);}
 @GetMapping("/{domain}")public Object query(@PathVariable String domain,@RequestParam MultiValueMap<String,String> params){
  if(domain.equals("notices")&&"page".equals(params.getFirst("responseMode")))return notices.query(access.actor(),params);
  if(domain.equals("audits")&&"page".equals(params.getFirst("responseMode")))return audits.query(access.actor(),params);
  return filters.query(domain,access.actor(),params);
 }
 @ExceptionHandler(AdminManagementException.class)public ResponseEntity<ErrorVO> error(AdminManagementException e){return ResponseEntity.status(e.getCode()).body(new ErrorVO(e.getMessage(),e.getCode()));}
}
