package org.example.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.vo.ErrorVO;
import org.example.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/data-exports")
public class DetailedExportController {
    private final DetailedExportService exports;private final PermissionService permissions;
    public DetailedExportController(DetailedExportService exports,PermissionService permissions){this.exports=exports;this.permissions=permissions;}
    @GetMapping("/{domain}")
    public void export(@PathVariable String domain,@RequestParam MultiValueMap<String,String> params,HttpServletResponse response)throws IOException{
        exports.export(domain,permissions.actor(),params,response);
    }
    @ExceptionHandler(AdminManagementException.class)
    public ResponseEntity<ErrorVO> error(AdminManagementException e){return ResponseEntity.status(e.getCode()).body(new ErrorVO(e.getMessage(),e.getCode()));}
    @ExceptionHandler(TaskManagementException.class)
    public ResponseEntity<ErrorVO> taskError(TaskManagementException e){return ResponseEntity.status(e.getCode()).body(new ErrorVO(e.getMessage(),e.getCode()));}
    @ExceptionHandler(WorkRecordException.class)
    public ResponseEntity<ErrorVO> recordError(WorkRecordException e){return ResponseEntity.status(e.getCode()).body(new ErrorVO(e.getMessage(),e.getCode()));}
}
