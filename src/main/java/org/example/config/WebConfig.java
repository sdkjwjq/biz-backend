package org.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类：配置CORS、拦截器、路径匹配等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置路径匹配
     * @param configurer 路径匹配配置器
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 不区分尾部斜杠
        configurer.setUseTrailingSlashMatch(true);
    }

    /**
     * 配置CORS跨域
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("*")
                .allowedOriginPatterns("*") // 允许的域名
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private UserRoleInterceptor userRoleInterceptor;

    /**
     * 配置拦截器
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/system/**")
                .addPathPatterns("/biz/**")
                .addPathPatterns("/achievement/**")
                .excludePathPatterns("/system/login");

        registry.addInterceptor(userRoleInterceptor)
                .addPathPatterns("/system/users/**");
    }
}