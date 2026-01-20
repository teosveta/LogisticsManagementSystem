package com.logistics.controller;

import com.logistics.dto.auth.AuthResponse;
import com.logistics.dto.auth.LoginRequest;
import com.logistics.dto.auth.RegisterRequest;
import com.logistics.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This controller ONLY handles authentication endpoints.
 *   Business logic is delegated to AuthService.
 * - Dependency Inversion (DIP): Depends on AuthService interface, not implementation.
 *
 * Endpoints:
 * - POST /api/auth/register - Register new user
 * - POST /api/auth/login - Login and get JWT token
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and login")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user.
     *
     * @param request the registration data
     * @return JWT token and user info
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns a JWT token")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration request for username: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request the login credentials
     * @return JWT token and user info
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user and returns a JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request for username: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
