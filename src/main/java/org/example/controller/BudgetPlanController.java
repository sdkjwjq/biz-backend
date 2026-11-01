package org.example.controller;
import org.example.entity.vo.ErrorVO;
import org.example.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/budget/plan")
public class BudgetPlanController {
 private final BudgetPlanService plans;private final PermissionService access;
 public BudgetPlanController(BudgetPlanService plans,PermissionService access){this.plans=plans;this.access=access;}
 @GetMapping("/total")public Object total(){return plans.total(access.actor());}
 @ExceptionHandler(AdminManagementException.class)public ResponseEntity<ErrorVO> error(AdminManagementException e){return ResponseEntity.status(e.getCode()).body(new ErrorVO(e.getMessage(),e.getCode()));}
}
