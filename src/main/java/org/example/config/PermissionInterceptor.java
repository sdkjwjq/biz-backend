package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import org.example.entity.vo.ErrorVO;
import org.example.service.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 请求入口只做权限判断；写操作在服务事务内再次核对，避免检查与写入之间的竞态。 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    private final PermissionRequestGuard guard; private final ObjectMapper json;
    public PermissionInterceptor(PermissionRequestGuard guard,ObjectMapper json){this.guard=guard;this.json=json;}
    @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler)throws Exception{
        if("OPTIONS".equals(request.getMethod()))return true;
        try {guard.check(request,null);return true;}
        catch(AdminManagementException error){response.setStatus(error.getCode());response.setContentType("application/json;charset=UTF-8");response.getWriter().write(json.writeValueAsString(new ErrorVO(error.getMessage(),error.getCode())));return false;}
    }
}
