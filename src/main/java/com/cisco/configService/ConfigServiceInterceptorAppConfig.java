package com.cisco.configService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class ConfigServiceInterceptorAppConfig implements WebMvcConfigurer {

    @Autowired
    ConfigServiceInterceptor configServiceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(configServiceInterceptor);
    }
}

