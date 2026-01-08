package dev.jsmitty.bux.system.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Admin login request payload.
 */
public record LoginRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Password is required") String password) {}
