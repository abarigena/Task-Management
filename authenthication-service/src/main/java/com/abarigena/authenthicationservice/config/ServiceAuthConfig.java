package com.abarigena.authenthicationservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для настройки аутентификации сервиса.
 * Создает заголовок аутентификации в запросы к внешним сервисам.
 */
@Configuration
public class ServiceAuthConfig {

    /**
     * Создает и настраивает интерсептор, который добавляет заголовок аутентификации в каждый исходящий запрос.
     * Заголовок содержит ключ аутентификации для внутреннего сервиса.
     *
     * @return {@link RequestInterceptor} для добавления заголовка аутентификации.
     */
    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Service-Auth", "internal-service-key");
        };
    }
}
