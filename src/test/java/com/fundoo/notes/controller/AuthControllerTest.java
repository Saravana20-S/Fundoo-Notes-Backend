package com.fundoo.notes.controller;

import com.fundoo.notes.dto.auth.LoginRequest;
import com.fundoo.notes.dto.auth.LoginResponse;
import com.fundoo.notes.dto.auth.RegisterRequest;
import com.fundoo.notes.dto.user.UserResponse;
import com.fundoo.notes.exception.InvalidTokenException;
import com.fundoo.notes.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        authController = new AuthController(authService);
    }

    @Test
    void shouldRegisterUser() {

        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("Password@123")
                .mobile("9876543210")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .mobile("9876543210")
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        var result = authController.register(request);

        assertNotNull(result);
        assertEquals(201, result.getStatusCode().value());

        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        assertEquals(
                "john@example.com",
                result.getBody().getEmail()
        );
    }

    @Test
    void shouldLoginUser() {

        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("Password@123")
                .build();

        LoginResponse response = LoginResponse.builder()
                .token("jwt-token")
                .tokenType("Bearer")
                .userId(1L)
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        var result = authController.login(request);

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());

        assertNotNull(result.getBody());
        assertEquals(
                "jwt-token",
                result.getBody().getToken()
        );

        assertEquals(
                "Bearer",
                result.getBody().getTokenType()
        );

        assertEquals(
                "john@example.com",
                result.getBody().getEmail()
        );
    }


// phase 6
    @Test
    void shouldLogoutSuccessfully() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer jwt-token");

        doNothing()
                .when(authService)
                .logout("jwt-token");

        ResponseEntity<Void> response =
                authController.logout(request);

        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        verify(authService)
                .logout("jwt-token");
    }


    @Test
    void shouldRejectLogoutWithoutToken() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        assertThrows(
                InvalidTokenException.class,
                () -> authController.logout(request)
        );

        verifyNoInteractions(authService);
    }


    @Test
    void shouldRejectInvalidAuthorizationHeader() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getHeader("Authorization"))
                .thenReturn("Basic abc123");

        assertThrows(
                InvalidTokenException.class,
                () -> authController.logout(request)
        );

        verifyNoInteractions(authService);
    }
}