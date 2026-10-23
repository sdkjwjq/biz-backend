package org.example.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.example.service.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.*;
import java.util.*;

/** 所有业务写事务先取授权共享锁，再取业务对象锁；权限调整先取独占锁。 */
@Aspect @Component @Order(100)
public class PermissionTransactionAspect {
    private final PermissionService access;private final PermissionRequestGuard guard;private final PermissionManagementService management;private final TransactionTemplate tx,snapshotTx;
    private final ThreadLocal<Boolean> nested=ThreadLocal.withInitial(()->false);
    public PermissionTransactionAspect(PermissionService access,PermissionRequestGuard guard,PermissionManagementService management,PlatformTransactionManager transactions){this.access=access;this.guard=guard;this.management=management;this.tx=new TransactionTemplate(transactions);this.tx.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);this.snapshotTx=new TransactionTemplate(transactions);this.snapshotTx.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ);}
    @Around("execution(public * org.example.service.*Service.*(..)) && !within(org.example.service.Permission*) && !within(org.example.service.LeadershipAccessService)")
    public Object write(ProceedingJoinPoint point)throws Throwable{
        if(nested.get()||!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a)||Set.of("GET","OPTIONS","HEAD").contains(a.getRequest().getMethod())||access.actor()==null||!access.installed())return point.proceed();
        String path=a.getRequest().getRequestURI();if(path.contains("/system/password")||path.contains("/system/login")||path.contains("/system/logout"))return point.proceed();
        nested.set(true);
        TransactionTemplate transaction=path.contains("/work-records")?snapshotTx:tx;
        boolean configuration=path.matches(".*/(admin/(users|departments)|system/users)(/.*)?");
        try{return transaction.execute(status->{if(configuration)access.lockChange();else access.lockOperation();guard.check(a.getRequest(),(JsonNode)a.getRequest().getAttribute(PermissionRequestGuard.BODY));var before=management.pendingSnapshot();try{Object result=point.proceed();management.validateChangedPending(before);if(configuration)access.advance();return result;}catch(Throwable error){status.setRollbackOnly();if(error instanceof RuntimeException r)throw r;throw new IllegalStateException(error);}});}finally{nested.remove();}
    }
}
