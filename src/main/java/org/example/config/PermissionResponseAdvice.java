package org.example.config;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.example.service.PermissionService;
import org.example.service.AuditReadScopeService;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import java.util.*;

/** 旧审核中心列表仍按业务处理人读取，再以统一范围做交集；不扩大待办集合。 */
@RestControllerAdvice
public class PermissionResponseAdvice implements ResponseBodyAdvice<Object> {
    private final PermissionService access;private final ObjectMapper json;private final AuditReadScopeService scopes;
    public PermissionResponseAdvice(PermissionService access,ObjectMapper json,AuditReadScopeService scopes){this.access=access;this.json=json;this.scopes=scopes;}
    @Override public boolean supports(MethodParameter p,Class<? extends HttpMessageConverter<?>> c){return true;}
    @Override public Object beforeBodyWrite(Object body,MethodParameter p,MediaType media,Class<? extends HttpMessageConverter<?>> converter,ServerHttpRequest request,ServerHttpResponse response){
        Long actor=access.actor();if(actor==null||!access.enforced(actor)||access.admin(actor)||body==null)return body;
        String path=request.getURI().getPath();
        if(path.endsWith("/system/allUsers")&&body instanceof List<?> list){ArrayNode safe=json.createArrayNode();for(Object user:list){JsonNode u=json.valueToTree(user);ObjectNode row=json.createObjectNode();for(String key:List.of("userId","userName","nickName","deptId","role","status"))if(u.has(key))row.set(key,u.get(key));safe.add(row);}return safe;}
        if(!path.contains("/audit")||!(body instanceof List<?>))return body;
        String domain=path.contains("/core-indicators/")?"core":path.contains("/achievement/")?"achievements":path.contains("/performance/")?"performance":"tasks";
        String key=switch(domain){case "core"->"indicatorId";case "achievements"->"achId";case "performance"->"perfId";default->"taskId";};
        List<JsonNode> rows=new ArrayList<>();for(Object value:(List<?>)body)rows.add(json.valueToTree(value));
        var requested=rows.stream().filter(node->node.has(key)).map(node->new AuditReadScopeService.Ref(node.path(key).asLong(),node.hasNonNull("year")?node.get("year").asInt():null)).toList();
        var allowed=scopes.readable(actor,domain,requested);
        ArrayNode result=json.createArrayNode();for(var node:rows)if(!node.has(key)||allowed.contains(new AuditReadScopeService.Ref(node.path(key).asLong(),node.hasNonNull("year")?node.get("year").asInt():null)))result.add(node);return result;
    }
}
