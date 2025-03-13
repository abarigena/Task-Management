package com.abarigena.gatewayservice.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

/**
 * Класс для валидации маршрутов в Gateway.
 * Определяет публичные и защищенные эндпоинты и проверяет, требует ли маршрут аутентификации.
 */
@Service
public class RouterValidator {

    public static final List<String> openEndpoints = List.of(
            "/auth/register",
            "/auth/login"
    );

    private static final List<String> SWAGGER_PATHS = List.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars"
    );

    /**
     * Предикат, который проверяет, является ли запрос защищенным и требует ли он аутентификации.
     *
     * @param request Запрос.
     * @return true, если маршрут защищен, иначе false.
     */
    public Predicate<ServerHttpRequest> isSecured =
            request -> {
                String path = request.getURI().getPath();

                for (String swaggerPath : SWAGGER_PATHS) {
                    if (path.contains(swaggerPath)) {
                        return false;
                    }
                }

                return openEndpoints.stream()
                        .noneMatch(uri -> request.getURI().getPath().contains(uri));
            };
}