package org.example.controller;

import org.example.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.servlet.http.*;
import java.util.*;

@RestController
public class PlatformNoticeController {
    @ExceptionHandler(AdminManagementException.class) public ResponseEntity<org.example.entity.vo.ErrorVO> error(AdminManagementException error){return ResponseEntity.status(error.getCode()).body(new org.example.entity.vo.ErrorVO(error.getMessage(),error.getCode()));}
    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,org.springframework.web.bind.MissingServletRequestParameterException.class}) public ResponseEntity<org.example.entity.vo.ErrorVO> invalid(Exception error){return ResponseEntity.badRequest().body(new org.example.entity.vo.ErrorVO("请求参数不正确",400));}
    private final PlatformNoticeService notices;private final PermissionService access;private final SysService system;
    public PlatformNoticeController(PlatformNoticeService notices,PermissionService access,SysService system){this.notices=notices;this.access=access;this.system=system;}
    @GetMapping("/admin/platform-notices") public Object list(@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="")String keyword,@RequestParam(defaultValue="")String status){return notices.list(access.actor(),page,keyword,status);}
    @GetMapping("/admin/platform-notices/options") public Object options(@RequestParam(defaultValue="")String keyword,@RequestParam(defaultValue="1")int page){return notices.options(keyword,page,access.actor());}
    @PostMapping("/admin/platform-notices/preview") public Object preview(@RequestBody PlatformNoticeService.Draft body){return notices.preview(body.audienceType(),body.audienceIds(),access.actor());}
    @PostMapping("/admin/platform-notices") public Object save(@RequestBody PlatformNoticeService.Draft body){return notices.save(body,access.actor());}
    @GetMapping("/admin/platform-notices/{id}") public Object detail(@PathVariable long id){return notices.detail(id,access.actor());}
    @PostMapping("/admin/platform-notices/{id}/publish") public Object publish(@PathVariable long id,@RequestBody PlatformNoticeService.Change body){if(body.version()==null)throw new AdminManagementException(400,"请刷新通知后发布");return notices.publish(id,body.version(),access.actor());}
    @PostMapping("/admin/platform-notices/{id}/withdraw") public Object withdraw(@PathVariable long id,@RequestBody PlatformNoticeService.Change body){notices.withdraw(id,body,access.actor());return Map.of("success",true);}
    @PostMapping("/admin/platform-notices/attachments") public Object upload(@RequestParam("file")MultipartFile file)throws Exception{return Map.of("id",notices.upload(file,access.actor()),"name",Objects.toString(file.getOriginalFilename(),"附件"));}
    @GetMapping("/admin/platform-notices/{id}/receipts") public Object receipts(@PathVariable long id,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="")String keyword,@RequestParam(defaultValue="")String state){return notices.receipts(id,access.actor(),page,keyword,state);}
    @GetMapping("/admin/platform-notices/{id}/events/{user}") public Object events(@PathVariable long id,@PathVariable long user){return notices.events(id,user,access.actor());}
    @GetMapping("/admin/platform-notices/{id}/export") public ResponseEntity<byte[]> export(@PathVariable long id)throws Exception{return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=notice-reading-records.xlsx").body(notices.export(id,access.actor()));}
    @GetMapping("/platform-notices/status") public Object status(HttpServletRequest request){var pending=notices.pending(access.actor(),request.getHeader("Authorization"));return Map.of("pending",pending,"total",pending.size());}
    @PostMapping("/platform-notices/{id}/open") public Object open(@PathVariable long id,@RequestBody PlatformNoticeService.ReadAction body){return notices.open(id,body,access.actor());}
    @PostMapping("/platform-notices/{id}/confirm") public Object confirm(@PathVariable long id,@RequestBody PlatformNoticeService.ReadAction body){notices.confirm(id,body,access.actor());return Map.of("success",true);}
    @GetMapping("/platform-notices/{id}/attachments/{file}") public void download(@PathVariable long id,@PathVariable long file,HttpServletResponse response)throws Exception{notices.requireAttachment(id,file,access.actor());system.downloadFile(file,response);}
}
