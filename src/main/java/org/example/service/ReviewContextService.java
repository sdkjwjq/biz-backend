package org.example.service;
import org.example.utils.JWTUtil;
import org.springframework.stereotype.Service;
import java.util.*;
import static org.example.service.ReviewRoundService.map;

@Service
public class ReviewContextService {
 private final ReviewRoundService rounds;private final PermissionService permissions;
 public ReviewContextService(ReviewRoundService rounds,PermissionService permissions){this.rounds=rounds;this.permissions=permissions;}
 public String mode(String token){var value=JWTUtil.verifyJwtToken(token).getClaim("experience").asString();return value==null?"BUSINESS":value;}
 public boolean business(long actor){var state=permissions.state(actor);return state.admin()||state.permissions().stream().anyMatch(p->p.endsWith(".read")&&!p.startsWith("expert."));}
 public Map<String,Object> choices(long actor,String token){permissions.user(actor);boolean installed=rounds.installed();return map("experience",mode(token),"businessAvailable",business(actor),"expertAvailable",installed&&rounds.canEnter(actor),"historyAvailable",installed&&rounds.hasHistory(actor),"reviewRoundsEnabled",installed);}
 public String loginToken(long actor,String token){if(!rounds.installed())return token;boolean business=business(actor),expert=rounds.canEnter(actor);return JWTUtil.withExperience(token,business?(expert?"SELECT":"BUSINESS"):"EXPERT");}
 public String switchToken(long actor,String token,String mode){if(mode==null||!Set.of("BUSINESS","EXPERT").contains(mode))throw new AdminManagementException(400,"请选择信息填报或专家评审");if(mode.equals("BUSINESS")&&!business(actor)||mode.equals("EXPERT")&&!rounds.canEnter(actor)&&!( !business(actor)&&rounds.hasHistory(actor)))throw new AdminManagementException(403,"当前没有可切换的有效身份");return JWTUtil.withExperience(token,mode);}
}
