package com.fundoo.notes.service;

import com.fundoo.notes.dto.auth.LoginRequest;
import com.fundoo.notes.dto.auth.LoginResponse;
import com.fundoo.notes.dto.auth.RegisterRequest;
import com.fundoo.notes.dto.user.UserResponse;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.DuplicateEmailException;
import com.fundoo.notes.exception.InvalidPasswordException;
import com.fundoo.notes.exception.InvalidTokenException;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.security.JwtService;
import com.fundoo.notes.redis.TokenCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenCacheService tokenCacheService;

    @InjectMocks
    private AuthService authService;


    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("Password@123")
                .mobile("9876543210")
                .build();

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Password@123"))
                .thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .mobile("9876543210")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());

        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder).encode("Password@123");
        verify(userRepository).save(any(User.class));
    }


    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("Password@123")
                .mobile("9876543210")
                .build();

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> authService.register(request)
        );

        verify(userRepository).existsByEmail("john@example.com");

        verify(userRepository, never())
                .save(any(User.class));
    }


    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("Password@123")
                .build();

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .mobile("9876543210")
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password@123",
                "encodedPassword"
        )).thenReturn(true);

        when(jwtService.generateToken("john@example.com"))
                .thenReturn("jwt-token");

        when(jwtService.getExpiration())
                .thenReturn(3600000L);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(1L, response.getUserId());

        verify(userRepository).findByEmail("john@example.com");

        verify(passwordEncoder)
                .matches("Password@123", "encodedPassword");

        verify(jwtService)
                .generateToken("john@example.com");

        verify(tokenCacheService)
                .saveToken("jwt-token", "john@example.com", 3600000L);
    }


    @Test
    void shouldThrowExceptionForWrongPassword() {

        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("WrongPassword")
                .build();

        User user = User.builder()
                .id(1L)
                .email("john@example.com")
                .password("encodedPassword")
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "encodedPassword"
        )).thenReturn(false);

        assertThrows(
                InvalidPasswordException.class,
                () -> authService.login(request)
        );

        verify(jwtService, never())
                .generateToken(any());

        verify(tokenCacheService, never())
                .saveToken(any(), any(), anyLong());
    }


//phase 6
    @Test
    void shouldLogoutSuccessfully() {

        String token = "jwt-token";

        when(tokenCacheService.isTokenActive(token))
                .thenReturn(true);

        authService.logout(token);

        verify(tokenCacheService)
                .isTokenActive(token);

        verify(tokenCacheService)
                .removeToken(token);
    }


    @Test
    void shouldRejectAlreadyRevokedToken() {

        String token = "jwt-token";

        when(tokenCacheService.isTokenActive(token))
                .thenReturn(false);

        assertThrows(
                InvalidTokenException.class,
                () -> authService.logout(token)
        );

        verify(tokenCacheService)
                .isTokenActive(token);

        verify(tokenCacheService, never())
                .removeToken(token);
    }


    @Test
    void shouldRejectNullToken() {

        assertThrows(
                InvalidTokenException.class,
                () -> authService.logout(null)
        );

        verifyNoInteractions(tokenCacheService);
    }


    @Test
    void shouldRejectBlankToken() {

        assertThrows(
                InvalidTokenException.class,
                () -> authService.logout("   ")
        );

        verifyNoInteractions(tokenCacheService);
    }
}