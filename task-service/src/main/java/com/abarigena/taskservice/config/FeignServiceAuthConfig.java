package com.abarigena.taskservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignServiceAuthConfig {
    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Service-Auth", "internal-service-key");
        };
    }
}
