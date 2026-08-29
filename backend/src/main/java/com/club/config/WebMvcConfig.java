package com.club.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final RequestLogInterceptor requestLogInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor, RequestLogInterceptor requestLogInterceptor) {
        this.authInterceptor = authInterceptor;
        this.requestLogInterceptor = requestLogInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLogInterceptor)
                .addPathPatterns("/api/**")
                .order(0);
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/error")
                .order(1);
    }
}
