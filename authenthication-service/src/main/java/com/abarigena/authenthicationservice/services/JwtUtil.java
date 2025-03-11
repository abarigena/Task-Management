package com.abarigena.authenthicationservice.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private String expiration;

    private Key key;

    @Autowired
    public JwtUtil(Key key) {
        this.key = key;
    }

    public Claims getClaims(String token) {
        logger.debug("Получение данных из JWT токена");
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();
        } catch (Exception e) {
            logger.error("Ошибка при извлечении данных из токена: {}", e.getMessage());
            throw e;
        }
    }

    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }

    public boolean isExpired(String token) {
        try {
            boolean expired = getExpirationDate(token).before(new Date());
            logger.debug("Проверка срока действия токена: {}", expired ? "истёк" : "действителен");
            return expired;
        } catch (Exception e) {
            logger.warn("Ошибка при проверке срока действия токена: {}", e.getMessage());
            return true;
        }
    }

    public String generate(String userId, String role, String tokenType) {
        logger.info("Генерация {} токена для пользователя: {}", tokenType, userId);

        Map<String, String> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("role", role);

        long expMillis = "ACCESS".equalsIgnoreCase(tokenType)
                ? Long.parseLong(expiration) * 1000
                : Long.parseLong(expiration) * 1000 * 5;

        final Date now = new Date();
        final Date exp = new Date(now.getTime() + expMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();
    }
}
