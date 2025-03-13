package com.abarigena.gatewayservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Autowired
    private AuthenticationFilter filter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Для документации API
                .route("task-service-api-docs", r -> r
                        .path("/task-service/v3/api-docs")
                        .filters(f -> f
                                .stripPrefix(1))
                        .uri("lb://task-service"))

                // Для UI Swagger
                .route("task-service-swagger-ui", r -> r
                        .path("/task-service/swagger-ui/**")
                        .filters(f -> f
                                .stripPrefix(1))
                        .uri("lb://task-service"))

                // Для документации API
                .route("user-service-api-docs", r -> r
                        .path("/user-service/v3/api-docs")
                        .filters(f -> f
                                .stripPrefix(1))
                        .uri("lb://user-service"))

                // Для UI Swagger
                .route("user-service-swagger-ui", r -> r
                        .path("/user-service/swagger-ui/**")
                        .filters(f -> f
                                .stripPrefix(1))
                        .uri("lb://user-service"))

                .route("user-service", r -> r.path("/users/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://user-service"))
                .route("authenthication-service", r -> r.path("/auth/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://authenthication-service"))
                .route("task-service", r -> r.path("/tasks/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://task-service"))

                // Auth Service Swagger
                .route("auth-service-swagger-ui", r -> r.path("/auth-service/swagger-ui/**")
                        .uri("lb://authenthication-service/swagger-ui/"))
                .route("auth-service-api-docs", r -> r.path("/auth-service/v3/api-docs/**")
                        .uri("lb://authenthication-service/v3/api-docs"))

                .build();
    }
}
