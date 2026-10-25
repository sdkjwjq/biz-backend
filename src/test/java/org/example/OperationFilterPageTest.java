package org.example;
import org.example.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class OperationFilterPageTest {
 private LinkedMultiValueMap<String,String> params(){var p=new LinkedMultiValueMap<String,String>();p.add("responseMode","page");p.add("includeFields","false");return p;}
 @Test void tiesUseFixedDatabaseOrderWithoutFullHistory(){
  var jdbc=mock(JdbcTemplate.class);var management=mock(AdminManagementService.class);var permissions=mock(PermissionService.class);var service=new OperationFilterPageService(permissions,management,jdbc);
  when(jdbc.queryForObject(anyString(),eq(Long.class),any(Object[].class))).thenReturn(10L);
  var result=service.query(1L,params());assertEquals(10,result.get("total"));assertFalse(result.containsKey("fields"));verify(management).operationRowsByKeys(List.of(),1L);verifyNoMoreInteractions(management);
  var queries=mockingDetails(jdbc).getInvocations().stream().map(i->String.valueOf(i.getArguments()[0])).toList();assertTrue(queries.stream().anyMatch(sql->sql.contains("q.createTime IS NULL,q.createTime DESC,q.sourceRank,q.recordId DESC")));assertTrue(queries.stream().noneMatch(sql->sql.contains("before_json")||sql.contains("after_json")));
 }
 @Test void pageFetchHydratesOnlySelectedKeys(){
  var jdbc=mock(JdbcTemplate.class);var management=mock(AdminManagementService.class);var permissions=mock(PermissionService.class);var service=new OperationFilterPageService(permissions,management,jdbc);
  var keys=List.of(Map.<String,Object>of("recordType","ADMIN","recordId",7L));var items=List.of(Map.<String,Object>of("recordType","ADMIN","recordId",7L,"beforeJson","original"));
  when(jdbc.queryForObject(anyString(),eq(Long.class),any(Object[].class))).thenReturn(1L);when(jdbc.queryForList(anyString(),any(Object[].class))).thenReturn(keys);when(management.operationRowsByKeys(keys,1L)).thenReturn(items);
  var result=service.query(1L,params());assertEquals(items,result.get("items"));assertEquals(1,result.get("total"));assertFalse(result.containsKey("fields"));assertFalse(result.containsKey("ids"));verify(permissions).requireAdmin(1L);
 }
 @Test void complexPredicatesAreSupportedAndExportsUseStreamingEntry(){var service=new OperationFilterPageService(null,null,null);for(var entry:Map.of("f.keyword","名称","f.createTime","missing","f.module","missing","f.createTimeTo","+10000-01-01").entrySet()){var next=params();next.add(entry.getKey(),entry.getValue());assertTrue(service.supports(next));}var p=params();p.add("purpose","export");assertFalse(service.supports(p));}
}
