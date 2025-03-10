package com.abarigena.authenthicationservice.controller;

import com.abarigena.authenthicationservice.services.AuthService;
import com.abarigena.dto.AuthRequest;
import com.abarigena.dto.AuthResponse;
import com.abarigena.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping(value = "/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String token) {
        // Убираем префикс "Bearer " из токена
        String refreshToken = token.replace("Bearer ", "");
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}
