package com.abarigena.authenthicationservice.services;

import com.abarigena.authenthicationservice.client.UserServiceClient;
import com.abarigena.dto.AuthRequest;
import com.abarigena.dto.AuthResponse;
import com.abarigena.dto.LoginRequest;
import com.abarigena.dto.UserDto;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserServiceClient userServiceClient, JwtUtil jwtUtil) {
        this.userServiceClient = userServiceClient;
        this.jwtUtil = jwtUtil;
    }

    public void register(AuthRequest request) {
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
        request.setPassword(hashedPassword);

        // Регистрация пользователя без выдачи токена
        userServiceClient.registerUser(request);
    }

    public AuthResponse login(LoginRequest request) {
        // Получаем информацию о пользователе
        UserDto user = userServiceClient.findByEmail(request.getEmail());

        // Проверяем пароль
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Используем роли пользователя для создания токена
        String roles = String.join(",", user.getRoles());

        String accessToken = jwtUtil.generate(user.getId(), roles, "ACCESS");
        String refreshToken = jwtUtil.generate(user.getId(), roles, "REFRESH");

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshToken(String refreshToken) {
        // Проверяем, что токен не истёк
        if (jwtUtil.isExpired(refreshToken)) {
            throw new RuntimeException("Refresh token expired");
        }

        // Получаем данные из токена
        String userId = jwtUtil.getClaims(refreshToken).getSubject();
        String role = jwtUtil.getClaims(refreshToken).get("role", String.class);

        // Генерируем новые токены
        String newAccessToken = jwtUtil.generate(userId, role, "ACCESS");
        String newRefreshToken = jwtUtil.generate(userId, role, "REFRESH");

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

}
