package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.dto.LoginRequest;
import dev.jsmitty.bux.system.dto.LoginResponse;
import dev.jsmitty.bux.system.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                    .body(java.util.Map.of("error", "Invalid credentials"));
        }
    }
}
