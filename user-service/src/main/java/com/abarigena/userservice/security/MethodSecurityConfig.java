package com.abarigena.userservice.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Конфигурация безопасности, включающая методовую безопасность.
 * <p>
 * Включает активацию аннотаций для метода безопасности, таких как @PreAuthorize, @Secured и @RolesAllowed.
 * </p>
 */
@Configuration
@EnableMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true
)
public class MethodSecurityConfig {

}