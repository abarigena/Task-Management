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

public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(GatewayAuthenticationFilter.class);

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

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