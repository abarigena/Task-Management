package com.abarigena.taskservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для настройки авторизации в сервисах, использующих Feign.
 */
@Configuration
public class FeignServiceAuthConfig {

    /**
     * Создаёт интерцептор, который добавляет заголовок авторизации для запросов,
     * отправляемых через Feign.
     *
     * @return интерцептор для добавления заголовка авторизации
     */
    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Service-Auth", "internal-service-key");
        };
    }
}
