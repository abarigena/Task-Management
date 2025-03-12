package com.abarigena.taskservice.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(
        prePostEnabled = true,   // Активация @PreAuthorize, @PostAuthorize
        securedEnabled = true,   // Активация @Secured
        jsr250Enabled = true     // Активация @RolesAllowed
)
public class MethodSecurityConfig {

}