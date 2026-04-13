package com.jobportal.apigateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret = "thisisaverylongsecretkeyforjwttestingpurposes12345";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", secret);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = generateToken("test@user.com", "ROLE_USER", 1L, 1000 * 60);
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        String token = generateToken("test@user.com", "ROLE_USER", 1L, -1000);
        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    void extractEmail_ReturnsSubject() {
        String token = generateToken("test@user.com", "ROLE_USER", 1L, 1000 * 60);
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("test@user.com");
    }

    @Test
    void extractRole_ReturnsRoleClaim() {
        String token = generateToken("test@user.com", "ROLE_ADMIN", 1L, 1000 * 60);
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void extractUserId_ReturnsUserIdClaim() {
        String token = generateToken("test@user.com", "ROLE_USER", 999L, 1000 * 60);
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(999L);
    }

    private String generateToken(String email, String role, Long userId, long expiryMs) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of("role", role, "userId", userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
