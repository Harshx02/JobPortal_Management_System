package com.jobportal.authservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "testSecret12345678901234567890123456789012";
    private final long expiration = 3600000;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", expiration);
    }

    @Test
    void testGenerateAndValidateToken() {
        String email = "test@example.com";
        Long userId = 1L;
        String role = "USER";

        String token = jwtUtil.generateToken(email, userId, role);
        assertNotNull(token);

        assertTrue(jwtUtil.validateToken(token, email));
        assertEquals(email, jwtUtil.extractEmail(token));
    }

    @Test
    void testValidateToken_InvalidEmail() {
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email, 1L, "USER");

        assertFalse(jwtUtil.validateToken(token, "wrong@example.com"));
    }

    @Test
    void testValidateToken_InvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here", "test@example.com"));
    }
}
