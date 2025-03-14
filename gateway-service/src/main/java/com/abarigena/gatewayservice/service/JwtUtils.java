package com.abarigena.gatewayservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Утилитный класс для работы с JWT токенами.
 * Обрабатывает парсинг токенов и проверку их срока годности.
 */
@Service
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    public void initKey() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Извлекает данные из JWT токена.
     *
     * @param token JWT токен.
     * @return {@link Claims} Содержимое токена.
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    /**
     * Проверяет, не истек ли срок действия токена.
     *
     * @param token JWT токен.
     * @return true, если токен истек, иначе false.
     */
    public boolean isExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}