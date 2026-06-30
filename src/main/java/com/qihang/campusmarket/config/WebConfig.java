package com.qihang.campusmarket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final AccessLogInterceptor accessLogInterceptor;

    @Value("${campus.upload-dir:uploads}")
    private String uploadDir;

    public WebConfig(AuthInterceptor authInterceptor,
                     RateLimitInterceptor rateLimitInterceptor,
                     AccessLogInterceptor accessLogInterceptor) {
        this.authInterceptor = authInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.accessLogInterceptor = accessLogInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessLogInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico", "/error");
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/ai/**");
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/products/new", "/products/publish", "/products/*/reserve", "/products/*/comments",
                        "/products/*/status", "/favorites/**", "/orders/**", "/messages/**", "/user/**", "/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath.endsWith("/") ? uploadPath : uploadPath + "/");
    }
}
