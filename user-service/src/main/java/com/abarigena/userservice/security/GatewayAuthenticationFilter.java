package com.abarigena.userservice.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Фильтр для аутентификации запросов в шлюзе.
 * Фильтрует запросы и устанавливает аутентификацию в контексте безопасности на основе заголовков.
 * <p>
 * Этот фильтр проверяет заголовки запросов для межсервисной аутентификации и аутентификации пользователя.
 * Если заголовки присутствуют и корректны, то устанавливается соответствующий объект аутентификации в контексте безопасности.
 * </p>
 */
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(GatewayAuthenticationFilter.class);

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    /**
     * Метод, который определяет, следует ли пропускать фильтрацию для текущего запроса.
     * <p>
     * Запросы к Swagger UI и документации API пропускаются.
     * </p>
     *
     * @param request HTTP-запрос
     * @return true, если фильтрация не требуется, иначе false
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api-docs") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.contains("swagger-ui.html") ||
                path.contains("swagger-resources") ||
                path.startsWith("/webjars");
    }

    /**
     * Основной метод фильтрации запроса, который устанавливает аутентификацию для запроса
     * на основе заголовков.
     * <p>
     * Проверяет наличие заголовков аутентификации и создает объект аутентификации,
     * который затем устанавливается в контексте безопасности.
     * </p>
     *
     * @param request HTTP-запрос
     * @param response HTTP-ответ
     * @param filterChain Цепочка фильтров для дальнейшей обработки запроса
     * @throws ServletException исключение, если происходит ошибка в фильтрации
     * @throws IOException исключение, если происходит ошибка ввода/вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug("Обработка запроса к: {}", request.getRequestURI());

        // Проверяем заголовок для межсервисной коммуникации
        String serviceAuth = request.getHeader("X-Service-Auth");
        if (serviceAuth != null && serviceAuth.equals("internal-service-key")) {
            logger.info("Обнаружена межсервисная аутентификация");
            // Создаем аутентификацию для сервиса с ролью SERVICE
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            "service-account",
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_SERVICE"))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Установлена аутентификация для сервисного аккаунта");
            filterChain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader(USER_ID_HEADER);
        String userRoles = request.getHeader(USER_ROLE_HEADER);

        if (userId != null && userRoles != null && !userId.isEmpty() && !userRoles.isEmpty()) {
            logger.info("Обработка запроса от пользователя ID: {}", userId);

            Collection<SimpleGrantedAuthority> authorities = Collections.emptyList();

            if (userRoles.contains(",")) {
                authorities = Arrays.stream(userRoles.split(","))
                        .map(role -> new SimpleGrantedAuthority(role.trim()))
                        .collect(Collectors.toList());
                logger.debug("Пользователь имеет несколько ролей: {}", userRoles);
            } else {
                authorities = Collections.singletonList(new SimpleGrantedAuthority(userRoles.trim()));
                logger.debug("Пользователь имеет роль: {}", userRoles);
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Аутентификация успешно установлена в контекст безопасности");
        } else {
            System.out.println("Отсутствуют заголовки: X-User-Id=" + userId + ", X-User-Role=" + userRoles);
        }

        filterChain.doFilter(request, response);
    }
}