package com.intellif.consumer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @author inori
 * @create 2019-07-15 14:55
 */
@Configuration
public class WebMvcConfigurer extends WebMvcConfigurationSupport {


    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PrometheusHandlerInterceptor()).addPathPatterns("/**").excludePathPatterns("/prometheus");
        super.addInterceptors(registry);
    }
}