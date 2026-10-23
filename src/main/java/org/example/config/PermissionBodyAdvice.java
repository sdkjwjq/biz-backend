package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.PermissionRequestGuard;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import java.lang.reflect.Type;

@ControllerAdvice
public class PermissionBodyAdvice extends RequestBodyAdviceAdapter {
    private final ObjectMapper json;private final PermissionRequestGuard guard;
    public PermissionBodyAdvice(ObjectMapper json,PermissionRequestGuard guard){this.json=json;this.guard=guard;}
    @Override public boolean supports(MethodParameter p,Type t,Class<? extends HttpMessageConverter<?>> c){return true;}
    @Override public Object afterBodyRead(Object body,HttpInputMessage input,MethodParameter p,Type type,Class<? extends HttpMessageConverter<?>> converter){
        if(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a){var node=json.valueToTree(body);a.getRequest().setAttribute(PermissionRequestGuard.BODY,node);guard.check(a.getRequest(),node);}return body;
    }
}
