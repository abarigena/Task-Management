package com.abarigena.authenthicationservice;

import com.abarigena.authenthicationservice.services.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {
    @Mock
    private Key key;

    @Mock
    private Claims claims;

    @Mock
    private JwtParserBuilder jwtParserBuilder;

    @Mock
    private JwtParser jwtParser;

    @Mock
    private JwtBuilder jwtBuilder;

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwicm9sZSI6IlVTRVIiLCJpZCI6InVzZXIxMjMiLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6MTUxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_ROLE = "USER";
    private static final String EXPIRATION = "3600";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", "testsecretkeythatisusedforthejwttokengeneration");
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    @Test
    void testGetClaims_Success() {
        try (MockedStatic<Jwts> jwtsMock = mockStatic(Jwts.class)) {
            jwtsMock.when(Jwts::parserBuilder).thenReturn(jwtParserBuilder);
            when(jwtParserBuilder.setSigningKey(key)).thenReturn(jwtParserBuilder);
            when(jwtParserBuilder.build()).thenReturn(jwtParser);
            when(jwtParser.parseClaimsJws(anyString())).thenReturn(mock(io.jsonwebtoken.Jws.class));
            when(jwtParser.parseClaimsJws(anyString()).getBody()).thenReturn(claims);

            Claims result = jwtUtil.getClaims(TEST_TOKEN);

            assertNotNull(result);
            assertEquals(claims, result);
        }
    }

    @Test
    void testGetClaims_Exception() {
        try (MockedStatic<Jwts> jwtsMock = mockStatic(Jwts.class)) {
            jwtsMock.when(Jwts::parserBuilder).thenReturn(jwtParserBuilder);
            when(jwtParserBuilder.setSigningKey(key)).thenReturn(jwtParserBuilder);
            when(jwtParserBuilder.build()).thenReturn(jwtParser);
            when(jwtParser.parseClaimsJws(anyString())).thenThrow(new RuntimeException("Invalid token"));

            Exception exception = assertThrows(RuntimeException.class, () -> jwtUtil.getClaims(TEST_TOKEN));
            assertEquals("Invalid token", exception.getMessage());
        }
    }

    @Test
    void testGetExpirationDate() {
        Date expirationDate = new Date();

        try (MockedStatic<Jwts> jwtsMock = mockStatic(Jwts.class)) {
            jwtsMock.when(Jwts::parserBuilder).thenReturn(jwtParserBuilder);
            when(jwtParserBuilder.setSigningKey(key)).thenReturn(jwtParserBuilder);
            when(jwtParserBuilder.build()).thenReturn(jwtParser);
            when(jwtParser.parseClaimsJws(anyString())).thenReturn(mock(io.jsonwebtoken.Jws.class));
            when(jwtParser.parseClaimsJws(anyString()).getBody()).thenReturn(claims);
            when(claims.getExpiration()).thenReturn(expirationDate);

            Date result = jwtUtil.getExpirationDate(TEST_TOKEN);

            assertEquals(expirationDate, result);
        }
    }
}
