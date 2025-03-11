package com.abarigena.userservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Разрешаем сервисной роли доступ к API регистрации и поиска пользователей
                        .requestMatchers("/users").hasAnyRole("SERVICE", "ADMIN")
                        .requestMatchers("/users/{email}").hasAnyRole("SERVICE", "ADMIN")
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/users/profile").authenticated()
                        // Защищенные эндпоинты
                        .requestMatchers("/users/secured/**").hasRole("ADMIN")
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable())

                .addFilterBefore(new GatewayAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public GatewayAuthenticationFilter gatewayAuthenticationFilter() {
        return new GatewayAuthenticationFilter();
    }
}
