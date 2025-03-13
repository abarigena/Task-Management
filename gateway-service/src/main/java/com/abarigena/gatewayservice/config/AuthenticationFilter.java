package com.abarigena.gatewayservice.config;

import com.abarigena.gatewayservice.service.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Фильтр для аутентификации в Gateway, проверяющий наличие и валидность JWT токена.
 * Если токен отсутствует или недействителен, запрос отклоняется с кодом состояния 401 (Unauthorized).
 */
@RefreshScope
@Component
public class AuthenticationFilter implements GatewayFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Autowired
    private RouterValidator validator;
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Метод фильтрации запросов. Проверяет наличие и валидность JWT токена для защищенных эндпоинтов.
     *
     * @param exchange Контекст запроса и ответа.
     * @param chain Следующий фильтр в цепочке.
     * @return {@link Mono<Void>} Возвращает Mono, завершение цепочки фильтрации.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        logger.info("Запрос получен: {} {}", request.getMethod(), request.getURI().getPath());

        if (validator.isSecured.test(request)) {
            logger.debug("Обработка защищенного эндпоинта: {}", request.getURI().getPath());

            if (authMissing(request)) {
                logger.warn("Отказано в доступе: отсутствует заголовок авторизации");
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            final String token = request.getHeaders().getOrEmpty("Authorization").get(0);

            if (!token.startsWith("Bearer ")) {
                logger.warn("Отказано в доступе: неверный формат токена");
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String jwtToken = token.substring(7);

            if (jwtUtils.isExpired(jwtToken)) {
                logger.warn("Отказано в доступе: токен истёк");
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            // Добавляем данные из токена в заголовки запроса
            return addAuthorizationHeaders(exchange, jwtToken, chain);
        }

        logger.debug("Запрос к публичному эндпоинту: {}", request.getURI().getPath());
        return chain.filter(exchange);
    }

    /**
     * Метод для добавления заголовков авторизации в запрос.
     *
     * @param exchange Контекст запроса и ответа.
     * @param token JWT токен.
     * @param chain Следующий фильтр в цепочке.
     * @return {@link Mono<Void>} Возвращает Mono, завершение цепочки фильтрации.
     */
    private Mono<Void> addAuthorizationHeaders(ServerWebExchange exchange, String token, GatewayFilterChain chain) {
        try {
            String userId = jwtUtils.getClaims(token).getSubject();
            String role = jwtUtils.getClaims(token).get("role", String.class);

            logger.debug("Добавление заголовков авторизации для пользователя ID: {}, роль: {}", userId, role);

            // Создаем новый запрос с добавленными заголовками
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .header("Authorization", "Bearer " + token)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();

            return chain.filter(mutatedExchange);
        } catch (Exception e) {

            System.err.println("Ошибка при обработке JWT токена: " + e.getMessage());
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Метод для обработки ошибок аутентификации, возвращает ответ с заданным кодом состояния.
     *
     * @param exchange Контекст запроса и ответа.
     * @param httpStatus Статус ошибки.
     * @return {@link Mono<Void>} Завершающий Mono.
     */
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        logger.warn("Возвращаем ответ с кодом: {}", httpStatus);
        return response.setComplete();
    }

    /**
     * Проверяет, присутствует ли заголовок "Authorization" в запросе.
     *
     * @param request Запрос.
     * @return true, если заголовок отсутствует, false в противном случае.
     */
    private boolean authMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }
}
