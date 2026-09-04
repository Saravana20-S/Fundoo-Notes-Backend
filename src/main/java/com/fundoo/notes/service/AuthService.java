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
import com.fundoo.notes.redis.TokenCacheService;
import com.fundoo.notes.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final TokenCacheService tokenCacheService;

    @Transactional
    public UserResponse register(RegisterRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmail(email)) {

            log.warn(
                    "Registration failed. Email already exists: {}",
                    email
            );

            throw new DuplicateEmailException(
                    "Email already registered"
            );
        }

        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(email)
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .mobile(request.getMobile())
                .build();

        User savedUser =
                userRepository.save(user);

        log.info(
                "User registered successfully. userId={}",
                savedUser.getId()
        );

        return mapToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidPasswordException(
                                "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            log.warn(
                    "Login failed for email: {}",
                    email
            );

            throw new InvalidPasswordException(
                    "Invalid email or password"
            );
        }

        String token =
                jwtService.generateToken(user.getEmail());

        tokenCacheService.saveToken(
                token,
                user.getEmail(),
                jwtService.getExpiration()
        );

        log.info(
                "User login successful. userId={}",
                user.getId()
        );

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }


    public void logout(String token) {

        if (token == null || token.isBlank()) {
            throw new InvalidTokenException(
                    "Token is required for logout"
            );
        }

        if (!tokenCacheService.isTokenActive(token)) {
            throw new InvalidTokenException(
                    "Token is already expired or revoked"
            );
        }

        tokenCacheService.removeToken(token);

        log.info("User logged out successfully");
    }

    private UserResponse mapToUserResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .build();
    }
}