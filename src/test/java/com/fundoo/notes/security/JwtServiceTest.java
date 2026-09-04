package com.fundoo.notes.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        String secret =
                "FundooNotesSecretKeyForJwtAuthentication2026FundooNotes";

        long expiration = 3600000L;

        jwtService = new JwtService(secret, expiration);
    }

    @Test
    void shouldGenerateValidToken() {

        String token = jwtService.generateToken(
                "john@example.com"
        );

        assertNotNull(token);

        assertEquals(
                "john@example.com",
                jwtService.extractUsername(token)
        );

        assertTrue(
                jwtService.isTokenValid(token)
        );
    }

    @Test
    void shouldReturnCorrectExpiration() {

        assertEquals(
                3600000L,
                jwtService.getExpiration()
        );
    }
}