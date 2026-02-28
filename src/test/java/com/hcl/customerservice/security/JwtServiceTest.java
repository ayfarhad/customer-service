package com.hcl.customerservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        jwtService.secret = "test-secret-key-test-secret-key-test";
        jwtService.expiration = 3600000;
        jwtService.init();
    }

    @Test
    void generateAndValidateToken() {
        UserDetails user = User.withUsername("bob").password("pwd").roles("USER").build();
        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertEquals("bob", jwtService.extractUsername(token));
        assertTrue(jwtService.validateToken(token, user));
    }

    @Test
    void expiredToken() {
        jwtService.expiration = -1000; // already expired
        jwtService.init();
        UserDetails user = User.withUsername("bob").password("pwd").roles("USER").build();
        String token = jwtService.generateToken(user);
        assertFalse(jwtService.validateToken(token, user));
    }
}
