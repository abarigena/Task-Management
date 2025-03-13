package com.abarigena.authenthicationservice.services;

import com.abarigena.authenthicationservice.client.UserServiceClient;
import com.abarigena.dto.AuthRequest;
import com.abarigena.dto.AuthResponse;
import com.abarigena.dto.LoginRequest;
import com.abarigena.dto.UserDto;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Сервис для обработки аутентификации пользователя, включая регистрацию, вход и обновление токенов.
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserServiceClient userServiceClient, JwtUtil jwtUtil) {
        this.userServiceClient = userServiceClient;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param request данные для регистрации пользователя.
     */
    public void register(AuthRequest request) {
        logger.info("Начало процесса регистрации пользователя с email: {}", request.getEmail());

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
        request.setPassword(hashedPassword);

        try {
            userServiceClient.registerUser(request);
            logger.info("Пользователь успешно зарегистрирован: {}", request.getEmail());
        } catch (Exception e) {
            logger.error("Ошибка при регистрации пользователя {}: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * Выполняет вход в систему и генерирует токены доступа и обновления.
     *
     * @param request данные для аутентификации пользователя.
     * @return объект {@link AuthResponse} с токенами доступа и обновления.
     */
    public AuthResponse login(LoginRequest request) {
        logger.info("Попытка входа пользователя: {}", request.getEmail());

        UserDto user;
        try {
            user = userServiceClient.findByEmail(request.getEmail());
            logger.debug("Пользователь найден в базе данных: {}", request.getEmail());
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Ошибка при авторизации: " + e.getMessage());
        }

        // Проверяем пароль
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            logger.warn("Неверный пароль для пользователя: {}", request.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        String roles = String.join(",", user.getRoles());

        logger.debug("Генерация токенов для пользователя: {} с ролями: {}", user.getId(), roles);
        String accessToken = jwtUtil.generate(user.getId(), roles, "ACCESS");
        String refreshToken = jwtUtil.generate(user.getId(), roles, "REFRESH");

        logger.info("Пользователь успешно вошел в систему: {}", request.getEmail());
        return new AuthResponse(accessToken, refreshToken);
    }

    /**
     * Обновляет токен доступа на основе токена обновления.
     *
     * @param refreshToken токен обновления.
     * @return объект {@link AuthResponse} с новыми токенами доступа и обновления.
     */
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("Запрос на обновление токена");

        if (jwtUtil.isExpired(refreshToken)) {
            throw new RuntimeException("Refresh token expired");
        }

        String userId = jwtUtil.getClaims(refreshToken).getSubject();
        String role = jwtUtil.getClaims(refreshToken).get("role", String.class);

        logger.debug("Обновление токена для пользователя: {} с ролью: {}", userId, role);

        String newAccessToken = jwtUtil.generate(userId, role, "ACCESS");
        String newRefreshToken = jwtUtil.generate(userId, role, "REFRESH");

        logger.info("Токены успешно обновлены для пользователя: {}", userId);
        return new AuthResponse(newAccessToken, newRefreshToken);
    }

}
