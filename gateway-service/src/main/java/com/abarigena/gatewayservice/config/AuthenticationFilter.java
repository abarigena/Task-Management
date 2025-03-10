package com.abarigena.gatewayservice.config;

import com.abarigena.gatewayservice.service.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@RefreshScope
@Component
public class AuthenticationFilter implements GatewayFilter {

    @Autowired
    private RouterValidator validator;
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (validator.isSecured.test(request)) {
            if (authMissing(request)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            final String token = request.getHeaders().getOrEmpty("Authorization").get(0);

            // Проверяем формат Bearer токена
            if (!token.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            // Удаляем префикс "Bearer " из токена
            String jwtToken = token.substring(7);

            if (jwtUtils.isExpired(jwtToken)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            // Добавляем данные из токена в заголовки запроса
            addAuthorizationHeaders(exchange, jwtToken);
        }
        return chain.filter(exchange);
    }

    private void addAuthorizationHeaders(ServerWebExchange exchange, String token) {
        try {
            String userId = jwtUtils.getClaims(token).getSubject();
            String role = jwtUtils.getClaims(token).get("role", String.class);

            // Добавляем данные пользователя в заголовки запроса для микросервисов
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .build();

            exchange.mutate().request(request).build();
        } catch (Exception e) {
            // Ошибка при получении данных из токена
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private boolean authMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }
}
