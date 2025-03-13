package com.abarigena.authenthicationservice;

import com.abarigena.authenthicationservice.client.UserServiceClient;
import com.abarigena.authenthicationservice.services.AuthService;
import com.abarigena.authenthicationservice.services.JwtUtil;
import com.abarigena.dto.AuthRequest;
import com.abarigena.dto.AuthResponse;
import com.abarigena.dto.LoginRequest;
import com.abarigena.dto.UserDto;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Claims claims;

    @InjectMocks
    private AuthService authService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_HASHED_PASSWORD = "$2a$10$abcdefghijklmnopqrstuv";
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_ROLE = "USER";
    private static final String TEST_ACCESS_TOKEN = "access.token.123";
    private static final String TEST_REFRESH_TOKEN = "refresh.token.456";

    @Test
    void testRegister_Success() {
        AuthRequest request = new AuthRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        UserDto mockUserDto = new UserDto();
        mockUserDto.setId(TEST_USER_ID);
        mockUserDto.setEmail(TEST_EMAIL);

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.gensalt()).thenReturn("salt");
            bcryptMock.when(() -> BCrypt.hashpw(anyString(), anyString())).thenReturn(TEST_HASHED_PASSWORD);

            when(userServiceClient.registerUser(any(AuthRequest.class))).thenReturn(mockUserDto);

            authService.register(request);

            verify(userServiceClient, times(1)).registerUser(request);
            assertEquals(TEST_HASHED_PASSWORD, request.getPassword());
        }
    }

    @Test
    void testRegister_ThrowsException() {
        AuthRequest request = new AuthRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.gensalt()).thenReturn("salt");
            bcryptMock.when(() -> BCrypt.hashpw(anyString(), anyString())).thenReturn(TEST_HASHED_PASSWORD);

            // Stub для userServiceClient, который выбрасывает исключение
            when(userServiceClient.registerUser(any(AuthRequest.class))).thenThrow(new RuntimeException("User already exists"));

            Exception exception = assertThrows(RuntimeException.class, () -> authService.register(request));
            assertEquals("User already exists", exception.getMessage());
        }
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        UserDto userDto = new UserDto();
        userDto.setId(TEST_USER_ID);
        userDto.setEmail(TEST_EMAIL);
        userDto.setPassword(TEST_HASHED_PASSWORD);

        Set<String> roles = new HashSet<>();
        roles.add(TEST_ROLE);
        userDto.setRoles(roles);

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.checkpw(TEST_PASSWORD, TEST_HASHED_PASSWORD)).thenReturn(true);

            when(userServiceClient.findByEmail(TEST_EMAIL)).thenReturn(userDto);
            when(jwtUtil.generate(TEST_USER_ID, TEST_ROLE, "ACCESS")).thenReturn(TEST_ACCESS_TOKEN);
            when(jwtUtil.generate(TEST_USER_ID, TEST_ROLE, "REFRESH")).thenReturn(TEST_REFRESH_TOKEN);

            AuthResponse response = authService.login(request);

            assertNotNull(response);
            assertEquals(TEST_ACCESS_TOKEN, response.getAccessToken());
            assertEquals(TEST_REFRESH_TOKEN, response.getRefreshToken());
            verify(userServiceClient).findByEmail(TEST_EMAIL);
            verify(jwtUtil).generate(TEST_USER_ID, TEST_ROLE, "ACCESS");
            verify(jwtUtil).generate(TEST_USER_ID, TEST_ROLE, "REFRESH");
        }
    }

    @Test
    void testLogin_InvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        UserDto userDto = new UserDto();
        userDto.setId(TEST_USER_ID);
        userDto.setEmail(TEST_EMAIL);
        userDto.setPassword(TEST_HASHED_PASSWORD);

        Set<String> roles = new HashSet<>();
        roles.add(TEST_ROLE);
        userDto.setRoles(roles);

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.checkpw(TEST_PASSWORD, TEST_HASHED_PASSWORD)).thenReturn(false);

            when(userServiceClient.findByEmail(TEST_EMAIL)).thenReturn(userDto);

            Exception exception = assertThrows(RuntimeException.class, () -> authService.login(request));
            assertEquals("Invalid email or password", exception.getMessage());
        }
    }

    @Test
    void testLogin_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        when(userServiceClient.findByEmail(TEST_EMAIL)).thenThrow(new RuntimeException("User not found"));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Ошибка при авторизации: User not found", exception.getMessage());
    }

    @Test
    void testRefreshToken_Success() {
        // Arrange
        String refreshToken = TEST_REFRESH_TOKEN;

        when(jwtUtil.isExpired(refreshToken)).thenReturn(false);
        when(jwtUtil.getClaims(refreshToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_USER_ID);
        when(claims.get("role", String.class)).thenReturn(TEST_ROLE);
        when(jwtUtil.generate(TEST_USER_ID, TEST_ROLE, "ACCESS")).thenReturn("new.access.token");
        when(jwtUtil.generate(TEST_USER_ID, TEST_ROLE, "REFRESH")).thenReturn("new.refresh.token");

        // Act
        AuthResponse response = authService.refreshToken(refreshToken);

        // Assert
        assertNotNull(response);
        assertEquals("new.access.token", response.getAccessToken());
        assertEquals("new.refresh.token", response.getRefreshToken());

        verify(jwtUtil).isExpired(refreshToken);
        // Меняем на `times(2)`, так как метод вызывается дважды
        verify(jwtUtil, times(2)).getClaims(refreshToken);
        verify(jwtUtil).generate(TEST_USER_ID, TEST_ROLE, "ACCESS");
        verify(jwtUtil).generate(TEST_USER_ID, TEST_ROLE, "REFRESH");
    }

    @Test
    void testRefreshToken_Expired() {
        String refreshToken = TEST_REFRESH_TOKEN;

        when(jwtUtil.isExpired(refreshToken)).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> authService.refreshToken(refreshToken));
        assertEquals("Refresh token expired", exception.getMessage());

        verify(jwtUtil).isExpired(refreshToken);
        verify(jwtUtil, never()).getClaims(refreshToken);
    }
}
