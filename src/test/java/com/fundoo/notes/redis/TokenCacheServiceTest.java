package com.fundoo.notes.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private TokenCacheService tokenCacheService;

    @BeforeEach
    void setUp() {
        tokenCacheService =
                new TokenCacheService(redisTemplate);
    }

    @Test
    void shouldSaveToken() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        String token = "jwt-token";
        String email = "john@example.com";
        long expiration = 3600000L;

        tokenCacheService.saveToken(
                token,
                email,
                expiration
        );

        verify(valueOperations).set(
                "auth:token:" + token,
                email,
                Duration.ofMillis(expiration)
        );
    }

    @Test
    void shouldReturnTrueWhenTokenIsActive() {

        when(redisTemplate.hasKey(
                "auth:token:jwt-token"
        )).thenReturn(true);

        boolean result =
                tokenCacheService.isTokenActive(
                        "jwt-token"
                );

        assertTrue(result);

        verify(redisTemplate).hasKey(
                "auth:token:jwt-token"
        );
    }

    @Test
    void shouldReturnFalseWhenTokenIsNotActive() {

        when(redisTemplate.hasKey(
                "auth:token:jwt-token"
        )).thenReturn(false);

        boolean result =
                tokenCacheService.isTokenActive(
                        "jwt-token"
                );

        assertFalse(result);

        verify(redisTemplate).hasKey(
                "auth:token:jwt-token"
        );
    }

    @Test
    void shouldGetEmail() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get(
                "auth:token:jwt-token"
        )).thenReturn("john@example.com");

        String email =
                tokenCacheService.getEmail(
                        "jwt-token"
                );

        assertEquals(
                "john@example.com",
                email
        );

        verify(valueOperations).get(
                "auth:token:jwt-token"
        );
    }

    @Test
    void shouldRemoveToken() {

        tokenCacheService.removeToken(
                "jwt-token"
        );

        verify(redisTemplate).delete(
                "auth:token:jwt-token"
        );
    }

    @Test
    void shouldReturnTokenTtl() {

        when(redisTemplate.getExpire(
                "auth:token:jwt-token",
                TimeUnit.SECONDS
        )).thenReturn(3500L);

        long ttl =
                tokenCacheService.getTokenTtl(
                        "jwt-token"
                );

        assertEquals(3500L, ttl);

        verify(redisTemplate).getExpire(
                "auth:token:jwt-token",
                TimeUnit.SECONDS
        );
    }
}