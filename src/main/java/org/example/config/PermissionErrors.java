package org.example.config;
import org.example.entity.vo.ErrorVO;
import org.example.service.AdminManagementException;
import org.springframework.http.ResponseEntity;
public final class PermissionErrors {
    private PermissionErrors(){}
    public static ResponseEntity<ErrorVO> response(Throwable error){for(int i=0;error!=null&&i<20;i++,error=error.getCause())if(error instanceof AdminManagementException e)return ResponseEntity.status(e.getCode()).body(new ErrorVO(e.getMessage(),e.getCode()));return null;}
}
