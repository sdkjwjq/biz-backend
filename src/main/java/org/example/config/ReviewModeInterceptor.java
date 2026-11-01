package org.example.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import org.example.service.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Set;
/** The signed context gates API families, including administrator tokens; membership/time is checked again by each operation. */
@Component
public class ReviewModeInterceptor implements HandlerInterceptor {
 private final ReviewContextService contexts;private final ReviewRoundService rounds;private final ObjectMapper json;
 public ReviewModeInterceptor(ReviewContextService contexts,ReviewRoundService rounds,ObjectMapper json){this.contexts=contexts;this.rounds=rounds;this.json=json;}
 @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler)throws Exception{
  String token=request.getHeader("Authorization");if(token==null||token.isBlank()||"OPTIONS".equals(request.getMethod())||!rounds.installed())return true;
  String path=request.getRequestURI().substring(request.getContextPath().length()).replaceAll("/+$","");
  if(path.equals("/system/login")||path.startsWith("/system/password")||Set.of("/system/access","/system/logout","/system/review-context").contains(path)||path.startsWith("/platform-notices/")||path.equals("/platform-notices/status"))return true;
  try{String mode=contexts.mode(token);
   if(path.startsWith("/expert-reviews")||path.matches("/admin/permissions/.*expert-tasks.*"))throw new AdminManagementException(410,"旧评审入口已停用，请在独立专家角色开展评审；旧意见保留在历史评审");
   if(path.startsWith("/review-history"))return true;
   if(mode.equals("SELECT"))throw new AdminManagementException(403,"请先选择信息填报或专家评审角色");
   if(mode.equals("EXPERT")&&!path.startsWith("/review-work"))throw new AdminManagementException(403,"当前为专家角色，请先切换信息填报角色");
   if(!mode.equals("EXPERT")&&path.startsWith("/review-work"))throw new AdminManagementException(403,"请先切换为专家评审角色");
   return true;
  }catch(AdminManagementException e){response.setStatus(e.getCode());response.setContentType("application/json;charset=UTF-8");response.getWriter().write(json.writeValueAsString(java.util.Map.of("code",e.getCode(),"message",e.getMessage())));return false;}
 }
}
