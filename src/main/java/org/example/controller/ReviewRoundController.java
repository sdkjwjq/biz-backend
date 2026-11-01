package org.example.controller;
import org.example.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.*;
import java.util.*;

@RestController
public class ReviewRoundController {
 private final ReviewRoundService rounds;private final ReviewContextService contexts;private final PermissionService access;private final SysService system;
 public ReviewRoundController(ReviewRoundService rounds,ReviewContextService contexts,PermissionService access,SysService system){this.rounds=rounds;this.contexts=contexts;this.access=access;this.system=system;}
 @GetMapping("/system/review-context")public Object choices(HttpServletRequest request){return contexts.choices(access.actor(),request.getHeader("Authorization"));}
 public record Switch(String experience){}
 @PostMapping("/system/review-context")public Object change(@RequestBody Switch body,HttpServletRequest request){String old=request.getHeader("Authorization");String token=contexts.switchToken(access.actor(),old,body.experience());system.logout(old);return Map.of("token",token,"experience",body.experience());}
 @GetMapping("/admin/review-tasks")public Object list(){return rounds.adminList(access.actor());}
 @GetMapping("/admin/review-tasks/accounts")public Object accounts(@RequestParam(defaultValue="")String keyword,@RequestParam(defaultValue="1")int page){return rounds.accounts(access.actor(),keyword,page);}
 @PostMapping("/admin/review-tasks")public Object create(@RequestBody ReviewRoundService.RoundInput b){return rounds.create(access.actor(),b);}
 @GetMapping("/admin/review-tasks/{id}")public Object detail(@PathVariable long id){return rounds.adminDetail(access.actor(),id);}
 @PutMapping("/admin/review-tasks/{id}")public Object update(@PathVariable long id,@RequestBody ReviewRoundService.RoundInput b){return rounds.update(access.actor(),id,b);}
 @GetMapping("/admin/review-tasks/{id}/catalog")public Object catalog(@PathVariable long id){return rounds.catalog(access.actor(),id);}
 @PostMapping("/admin/review-tasks/{id}/members")public Object add(@PathVariable long id,@RequestBody ReviewRoundService.Members b){return rounds.add(access.actor(),id,b);}
 @PostMapping("/admin/review-tasks/{id}/members/{user}/remove")public Object remove(@PathVariable long id,@PathVariable long user,@RequestBody ReviewRoundService.Change b){return rounds.remove(access.actor(),id,user,b);}
 @GetMapping("/admin/review-tasks/{id}/members/{user}/scope")public Object scope(@PathVariable long id,@PathVariable long user){return rounds.memberScope(access.actor(),id,user);}
 @PutMapping("/admin/review-tasks/{id}/scope")public Object scope(@PathVariable long id,@RequestBody ReviewRoundService.ScopeInput b){return rounds.scope(access.actor(),id,b);}
 @PostMapping("/admin/review-tasks/{id}/publish")public Object publish(@PathVariable long id,@RequestBody ReviewRoundService.Change b){return rounds.publish(access.actor(),id,b);}
 @GetMapping("/admin/review-tasks/{id}/opinions")public Object opinions(@PathVariable long id){return rounds.opinions(access.actor(),id);}
 @GetMapping("/review-work")public Object mine(){return rounds.mine(access.actor());}
 @GetMapping("/review-work/{id}/tasks")public Object tasks(@PathVariable long id){return rounds.objects(access.actor(),id);}
 @GetMapping("/review-work/{id}/tasks/{task}")public Object review(@PathVariable long id,@PathVariable long task){return rounds.detail(access.actor(),id,task);}
 @PostMapping("/review-work/{id}/tasks/{task}")public Object save(@PathVariable long id,@PathVariable long task,@RequestBody ReviewRoundService.Opinion b){return rounds.save(access.actor(),id,task,b);}
 @GetMapping("/review-work/{id}/files/{file}")public void file(@PathVariable long id,@PathVariable long file,HttpServletResponse response)throws java.io.IOException{rounds.file(access.actor(),id,file);system.downloadFile(file,response);}
 @GetMapping("/review-history")public Object legacy(@RequestParam(defaultValue="1")int page){return rounds.legacy(access.actor(),page);}
 @GetMapping("/admin/review-tasks/{id}/opinions/export")public ResponseEntity<byte[]> export(@PathVariable long id)throws java.io.IOException{return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=review-opinions.xlsx").body(rounds.export(access.actor(),id));}
 @GetMapping("/review-history/rounds")public Object ended(){return rounds.ended(access.actor());}
 @GetMapping("/review-history/work/{id}/tasks")public Object endedTasks(@PathVariable long id){rounds.requireEnded(id);return rounds.objects(access.actor(),id);}
 @GetMapping("/review-history/work/{id}/tasks/{task}")public Object endedDetail(@PathVariable long id,@PathVariable long task){return rounds.historicalDetail(access.actor(),id,task);}
 @GetMapping("/review-history/work/{id}/files/{file}")public void endedFile(@PathVariable long id,@PathVariable long file,HttpServletResponse response)throws java.io.IOException{rounds.requireEnded(id);rounds.file(access.actor(),id,file);system.downloadFile(file,response);}
 @ExceptionHandler(AdminManagementException.class)public ResponseEntity<?> error(AdminManagementException e){return ResponseEntity.status(e.getCode()).body(Map.of("code",e.getCode(),"message",e.getMessage()));}
}
