package org.example;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.util.ContentCachingResponseWrapper;
import javax.sql.DataSource;
import java.lang.reflect.*;
import java.sql.*;

/** Disposable benchmark only. Records counts/timing, never SQL text, parameters, tokens, or bodies. */
@Configuration
@ConditionalOnProperty(name="isolated.performance.probe",havingValue="true")
public class PerformanceProbeConfig {
    private static final ThreadLocal<long[]> METRICS=new ThreadLocal<>();
    @Bean static BeanPostProcessor performanceDataSource(){return new BeanPostProcessor(){public Object postProcessAfterInitialization(Object bean,String name){
        if(!(bean instanceof DataSource))return bean;
        return Proxy.newProxyInstance(DataSource.class.getClassLoader(),new Class[]{DataSource.class},(proxy,method,args)->{
            Object value=invoke(bean,method,args);return value instanceof Connection connection?wrap(connection,Connection.class):value;
        });
    }};}
    @SuppressWarnings("unchecked") private static <T>T wrap(T target,Class<T> type){return (T)Proxy.newProxyInstance(type.getClassLoader(),new Class[]{type},(proxy,method,args)->{
        long[] metrics=METRICS.get();boolean execute=metrics!=null&&method.getName().startsWith("execute");long started=execute?System.nanoTime():0;
        try{Object value=invoke(target,method,args);if(value instanceof CallableStatement statement)return wrap(statement,CallableStatement.class);if(value instanceof PreparedStatement statement)return wrap(statement,PreparedStatement.class);if(value instanceof Statement statement)return wrap(statement,Statement.class);return value;}
        finally{if(execute){metrics[0]++;metrics[1]+=System.nanoTime()-started;}}
    });}
    private static Object invoke(Object target,Method method,Object[] args)throws Throwable{try{return method.invoke(target,args);}catch(InvocationTargetException error){throw error.getCause();}}
    @Bean FilterRegistrationBean<Filter> performanceRequests(){FilterRegistrationBean<Filter> registration=new FilterRegistrationBean<>();registration.setOrder(Integer.MIN_VALUE);
        registration.setFilter((request,response,chain)->{if(!(request instanceof HttpServletRequest)||!(response instanceof HttpServletResponse servlet)){chain.doFilter(request,response);return;}
            var cached=new ContentCachingResponseWrapper(servlet);long[] metrics={0,0};METRICS.set(metrics);long start=System.nanoTime();
            try{chain.doFilter(request,cached);}finally{METRICS.remove();cached.setHeader("X-Perf-Sql-Count",Long.toString(metrics[0]));cached.setHeader("X-Perf-Sql-Ms",Double.toString(metrics[1]/1_000_000d));cached.setHeader("X-Perf-Server-Ms",Double.toString((System.nanoTime()-start)/1_000_000d));cached.copyBodyToResponse();}
        });return registration;
    }
}
