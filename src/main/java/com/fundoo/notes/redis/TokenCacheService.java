package com.fundoo.notes.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenCacheService {

    private static final String TOKEN_PREFIX = "auth:token:";

    private final StringRedisTemplate redisTemplate;

    public void saveToken(
            String token,
            String email,
            long expirationMillis) {

        String key = TOKEN_PREFIX + token;

        redisTemplate.opsForValue().set(
                key,
                email,
                Duration.ofMillis(expirationMillis)
        );
    }

    public boolean isTokenActive(String token) {

        String key = TOKEN_PREFIX + token;

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(key)
        );
    }

    public String getEmail(String token) {

        String key = TOKEN_PREFIX + token;

        return redisTemplate.opsForValue().get(key);
    }

    public void removeToken(String token) {

        String key = TOKEN_PREFIX + token;

        redisTemplate.delete(key);
    }

    public long getTokenTtl(String token) {

        String key = TOKEN_PREFIX + token;

        Long ttl = redisTemplate.getExpire(
                key,
                java.util.concurrent.TimeUnit.SECONDS
        );

        return ttl != null ? ttl : -1;
    }
}