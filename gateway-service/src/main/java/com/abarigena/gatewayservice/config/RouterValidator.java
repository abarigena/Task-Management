package com.abarigena.gatewayservice.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

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
/*            "/task-service/v3/api-docs",
            "/task-service/swagger-ui"*/
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> {
                String path = request.getURI().getPath();

                // First check if it's a swagger-related path
                for (String swaggerPath : SWAGGER_PATHS) {
                    if (path.contains(swaggerPath)) {
                        return false; // Not secured for swagger paths
                    }
                }

                // Then check other open endpoints
                return openEndpoints.stream()
                        .noneMatch(uri -> request.getURI().getPath().contains(uri));
            };
}