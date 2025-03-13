package com.abarigena.authenthicationservice.client;

import com.abarigena.authenthicationservice.config.ServiceAuthConfig;
import com.abarigena.dto.AuthRequest;
import com.abarigena.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Клиент для взаимодействия с сервисом пользователей.
 * Используется для регистрации пользователей и поиска по электронной почте.
 */
@FeignClient(name = "user-service", configuration = ServiceAuthConfig.class)
public interface UserServiceClient {

    /**
     * Регистрирует нового пользователя.
     *
     * @param request запрос с данными для регистрации пользователя.
     * @return объект {@link UserDto} с данными зарегистрированного пользователя.
     */
    @PostMapping("/users")
    UserDto registerUser(AuthRequest request);

    /**
     * Ищет пользователя по его электронной почте.
     *
     * @param email электронная почта пользователя.
     * @return объект {@link UserDto} с данными пользователя.
     */
    @GetMapping("/users/{email}")
    UserDto findByEmail(@PathVariable("email") String email);
}