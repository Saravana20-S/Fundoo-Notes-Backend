package com.fundoo.notes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(
        name = "Users",
        description = "User APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @GetMapping("/me")
    @Operation(
            summary = "Get authenticated user"
    )
    public Map<String, String> getCurrentUser(
            Authentication authentication) {

        return Map.of(
                "message",
                "Authentication successful",
                "email",
                authentication.getName()
        );
    }
}