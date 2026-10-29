package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import org.example.service.PlatformNoticeService;
import org.example.utils.JWTUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.*;

/** The exact login/session cutoff means newly published notices never interrupt an existing draft. */
@Component
public class PlatformNoticeGateInterceptor implements HandlerInterceptor {
    private final PlatformNoticeService notices;private final ObjectMapper json;
    public PlatformNoticeGateInterceptor(PlatformNoticeService notices,ObjectMapper json){this.notices=notices;this.json=json;}
    @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler)throws Exception{
        if("OPTIONS".equals(request.getMethod()))return true;
        String path=request.getRequestURI().substring(request.getContextPath().length()).replaceAll("/+$","");
        if(Set.of("/system/login","/system/password/reset","/system/password/status","/system/password","/system/logout","/system/access").contains(path))return true;
        if(("GET".equals(request.getMethod())&&(path.equals("/platform-notices/status")||path.matches("/platform-notices/[0-9]+/attachments/[0-9]+")))
          ||("POST".equals(request.getMethod())&&path.matches("/platform-notices/[0-9]+/(open|confirm)")))return true;
        String token=request.getHeader("Authorization");if(token==null||token.isBlank())return true;
        Long actor;try{actor=JWTUtil.getUserIdFromToken(token);}catch(RuntimeException e){return true;}
        if(!notices.mustRead(actor,token))return true;
        response.setStatus(423);response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json.writeValueAsString(Map.of("code",423,"message","请先阅读并确认全部重要平台通知","reason","PLATFORM_NOTICE_REQUIRED")));return false;
    }
}
