package com.photo.config;

import com.photo.interceptor.AuthInterceptor;
import com.photo.interceptor.TokenRefreshInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private TokenRefreshInterceptor tokenRefreshInterceptor;

    @Autowired
    private AuthInterceptor authInterceptor;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Token刷新拦截器 - 拦截所有请求
        registry.addInterceptor(tokenRefreshInterceptor)
                .addPathPatterns("/**")
                .order(1);

        // 认证拦截器 - 拦截除登录、注册外的所有请求
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register"
                )
                .order(2);
    }

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传文件访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
        
        // 导出文件访问路径
        registry.addResourceHandler("/exports/**")
                .addResourceLocations("file:./exports/");
        
        // 模板文件访问路径
        registry.addResourceHandler("/templates/**")
                .addResourceLocations("file:./templates/");
    }
}