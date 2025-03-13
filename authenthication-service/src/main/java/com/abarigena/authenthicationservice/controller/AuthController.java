package com.abarigena.authenthicationservice.controller;

import com.abarigena.authenthicationservice.services.AuthService;
import com.abarigena.dto.AuthRequest;
import com.abarigena.dto.AuthResponse;
import com.abarigena.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Аутентификация", description = "API для регистрации, входа и обновления токенов")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/register")
    @Operation(
            summary = "Регистрация пользователя",
            description = "Регистрирует нового пользователя на основе переданных данных",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
            }
    )
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping(value = "/login")
    @Operation(
            summary = "Вход в систему",
            description = "Аутентифицирует пользователя и возвращает токены доступа и обновления",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный вход",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
            }
    )
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(value = "/refresh")
    @Operation(
            summary = "Обновление токена",
            description = "Обновляет токен доступа на основе токена обновления",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Токен успешно обновлен",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Недействительный токен обновления")
            }
    )
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String token) {
        String refreshToken = token.replace("Bearer ", "");
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}
