package com.abarigena.userservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    /**
     * Получает ID текущего пользователя из контекста безопасности
     * @return ID пользователя или null, если пользователь не аутентифицирован
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getPrincipal().toString();
        }

        return null;
    }

    /**
     * Проверяет, имеет ли текущий пользователь указанную роль
     * @param role роль для проверки
     * @return true, если пользователь имеет роль, иначе false
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(role));
        }

        return false;
    }
}