package com.abarigena.taskservice.client;

import com.abarigena.taskservice.config.FeignServiceAuthConfig;
import com.abarigena.taskservice.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Клиент для взаимодействия с сервисом пользователей.
 * Предназначен для получения информации о пользователе по его ID.
 */
@FeignClient(name = "user-service", configuration = FeignServiceAuthConfig.class)
public interface UserServiceClient {

    /**
     * Получает информацию о пользователе по его ID.
     *
     * @param userId ID пользователя
     * @return объект с информацией о пользователе
     */
    @GetMapping("/users/info/{userId}")
    UserInfoDto getUserInfo(@PathVariable("userId") String userId);
}