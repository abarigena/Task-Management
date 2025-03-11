package com.abarigena.userservice.controllers;

import com.abarigena.dto.UserDto;
import com.abarigena.userservice.security.SecurityUtils;
import com.abarigena.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> save(@RequestBody UserDto userDto) {
        logger.info("Запрос на создание нового пользователя: {}", userDto.getEmail());
        UserDto savedUser = userService.save(userDto);
        logger.info("Пользователь успешно создан: {}", savedUser.getEmail());
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserDto> findByEmail(@PathVariable String email) {
        logger.info("Запрос на поиск пользователя по email: {}", email);
        UserDto user = userService.findByEmail(email);
        logger.info("Пользователь найден: {}", email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{email}/assign-admin")
    public ResponseEntity<String> assignAdminRole(@PathVariable String email) {
        logger.info("Запрос на назначение роли администратора пользователю: {}", email);
        userService.assignAdminRole(email);
        logger.info("Роль администратора успешно назначена пользователю: {}", email);
        return ResponseEntity.ok("Роль администратора назначена пользователю с email: " + email);
    }

    @GetMapping("/secured")
    public ResponseEntity<String> securedEndpoint() {
        logger.info("Доступ к защищенному эндпоинту");
        return ResponseEntity.ok("Привет с защищенного эндпоинта!");
    }

    @GetMapping("/profile")
    public ResponseEntity<String> userProfile() {
        // Получение ID пользователя из контекста безопасности
        String userId = SecurityUtils.getCurrentUserId();
        logger.info("Запрос профиля пользователя с ID: {}", userId);
        return ResponseEntity.ok("Профиль пользователя с ID: " + userId);
    }
}
