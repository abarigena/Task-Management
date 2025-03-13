package com.abarigena.userservice.controllers;

import com.abarigena.dto.UserDto;
import com.abarigena.dto.UserInfoDto;
import com.abarigena.userservice.security.SecurityUtils;
import com.abarigena.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/users")
@Tag(name = "Пользователи", description = "API для управления пользователями")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Создание нового пользователя", description = "Создает нового пользователя на основе переданных данных")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно создан")
    public ResponseEntity<UserDto> save(@RequestBody UserDto userDto) {
        logger.info("Запрос на создание нового пользователя: {}", userDto.getEmail());
        UserDto savedUser = userService.save(userDto);
        logger.info("Пользователь успешно создан: {}", savedUser.getEmail());
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/{email}")
    @Operation(summary = "Поиск пользователя по email", description = "Возвращает информацию о пользователе по его email")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    public ResponseEntity<UserDto> findByEmail(@PathVariable String email) {
        logger.info("Запрос на поиск пользователя по email: {}", email);
        UserDto user = userService.findByEmail(email);
        logger.info("Пользователь найден: {}", email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{email}/assign-admin")
    @Operation(summary = "Назначение роли администратора", description = "Назначает роль администратора пользователю с указанным email")
    @ApiResponse(responseCode = "200", description = "Роль администратора успешно назначена")
    public ResponseEntity<String> assignAdminRole(@PathVariable String email) {
        logger.info("Запрос на назначение роли администратора пользователю: {}", email);
        userService.assignAdminRole(email);
        logger.info("Роль администратора успешно назначена пользователю: {}", email);
        return ResponseEntity.ok("Роль администратора назначена пользователю с email: " + email);
    }

    @GetMapping("/secured")
    @Operation(summary = "Защищенный эндпоинт", description = "Пример защищенного эндпоинта")
    @ApiResponse(responseCode = "200", description = "Успешный доступ к защищенному эндпоинту")
    public ResponseEntity<String> securedEndpoint() {
        logger.info("Доступ к защищенному эндпоинту");
        return ResponseEntity.ok("Привет с защищенного эндпоинта!");
    }

    @GetMapping("/profile")
    @Operation(summary = "Профиль пользователя", description = "Возвращает профиль текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Профиль пользователя успешно получен")
    public ResponseEntity<String> userProfile() {
        // Получение ID пользователя из контекста безопасности
        String userId = SecurityUtils.getCurrentUserId();
        logger.info("Запрос профиля пользователя с ID: {}", userId);
        UserDto userDto = userService.getCurrentUser(Long.valueOf(userId));
        return ResponseEntity.ok("Профиль пользователя с ID: " + userDto.toString());
    }

    @GetMapping("/info/{userId}")
    @Operation(summary = "Информация о пользователе", description = "Возвращает информацию о пользователе по его ID")
    @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно получена")
    public ResponseEntity<UserInfoDto> getUserInfo(@PathVariable String userId) {
        logger.info("Получение информации о пользователе с ID: {}", userId);
        UserInfoDto userDto = userService.findById(userId);
        return ResponseEntity.ok(userDto);
    }
}
