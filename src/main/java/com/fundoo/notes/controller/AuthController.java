package com.fundoo.notes.controller;

import com.fundoo.notes.dto.auth.LoginRequest;
import com.fundoo.notes.dto.auth.LoginResponse;
import com.fundoo.notes.dto.auth.RegisterRequest;
import com.fundoo.notes.dto.user.UserResponse;
import com.fundoo.notes.exception.InvalidTokenException;
import com.fundoo.notes.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "User registration and login APIs"
)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user"
    )
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse response =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login user"
    )
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }


    //phase 6
    @PostMapping("/logout")
    @Operation(summary = "Logout authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logout(
            HttpServletRequest request) {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            throw new InvalidTokenException(
                    "Bearer token is required"
            );
        }

        String token =
                authorizationHeader.substring(7);

        authService.logout(token);

        return ResponseEntity.noContent().build();
    }
}