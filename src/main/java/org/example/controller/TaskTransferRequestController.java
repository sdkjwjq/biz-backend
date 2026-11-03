package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.service.TaskTransferRequestService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 任务移交申请：提交、待我确认、我发起的、确认/拒绝。 */
@RestController
@RequestMapping("/task-transfer")
public class TaskTransferRequestController {

    @Autowired private TaskTransferRequestService transfers;

    private Long actor(HttpServletRequest request){return JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));}

    @GetMapping("/pending")public Object pending(HttpServletRequest request){return transfers.mine(actor(request));}

    @GetMapping("/users")public Object users(@RequestParam(required=false) Long taskId,@RequestParam(required=false) java.util.List<Long> taskIds,@RequestParam String fieldType,@RequestParam(required=false) String keyword,HttpServletRequest request){
        java.util.List<Long> ids=taskIds!=null&&!taskIds.isEmpty()?taskIds:(taskId==null?java.util.List.of():java.util.List.of(taskId));
        return transfers.searchUsers(actor(request),ids,fieldType,keyword);
    }

    @GetMapping("/all-users")public Object allUsers(@RequestParam(required=false) String fieldType,@RequestParam(required=false) String keyword,
                                                       @RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,HttpServletRequest request){
        return transfers.allUsers(actor(request),fieldType,keyword,page,size);
    }

    @GetMapping("/logs")public Object logs(@RequestParam Long taskId,HttpServletRequest request){return transfers.logs(actor(request),taskId);}

    @GetMapping("/{requestId}/task")public Object pendingTask(@PathVariable Long requestId,HttpServletRequest request){return transfers.pendingTask(actor(request),requestId);}

    @ExceptionHandler(org.example.service.AdminManagementException.class)
    public org.springframework.http.ResponseEntity<org.example.entity.vo.ErrorVO> error(org.example.service.AdminManagementException e){return org.springframework.http.ResponseEntity.status(e.getCode()).body(new org.example.entity.vo.ErrorVO(e.getMessage(),e.getCode()));}
    @ExceptionHandler({org.springframework.web.bind.MissingServletRequestParameterException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,org.springframework.http.converter.HttpMessageNotReadableException.class})
    public org.springframework.http.ResponseEntity<org.example.entity.vo.ErrorVO> invalid(Exception e){return org.springframework.http.ResponseEntity.badRequest().body(new org.example.entity.vo.ErrorVO("请求参数不正确",400));}

    @GetMapping("/sent")public Object sent(HttpServletRequest request){return transfers.sent(actor(request));}

    @PostMapping("/request")public Object create(@RequestBody TaskTransferRequestService.CreateInput body,HttpServletRequest request){return transfers.create(actor(request),body);}

    @PostMapping("/batch")public Object batch(@RequestBody TaskTransferRequestService.BatchCreateInput body,HttpServletRequest request){return transfers.createBatch(actor(request),body);}

    public record BatchAction(java.util.List<Long> requestIds,String remark){}

    @PostMapping("/batch-accept")public Object batchAccept(@RequestBody BatchAction body,HttpServletRequest request){return transfers.batchAccept(actor(request),body==null?null:body.requestIds());}

    @PostMapping("/batch-reject")public Object batchReject(@RequestBody BatchAction body,HttpServletRequest request){return transfers.batchReject(actor(request),body==null?null:body.requestIds(),body==null?null:body.remark());}

    @PostMapping("/{requestId}/accept")public Object accept(@PathVariable Long requestId,HttpServletRequest request){return transfers.accept(actor(request),requestId);}

    @PostMapping("/{requestId}/reject")public Object reject(@PathVariable Long requestId,@RequestBody(required=false) Map<String,String> body,HttpServletRequest request){
        return transfers.reject(actor(request),requestId,body==null?null:body.get("remark"));
    }
}
