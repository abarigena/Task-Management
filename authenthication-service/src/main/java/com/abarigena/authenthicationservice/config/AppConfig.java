package com.abarigena.authenthicationservice.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Key;

@Configuration
public class AppConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}