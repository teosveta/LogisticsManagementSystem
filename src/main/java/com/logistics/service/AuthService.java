package com.logistics.service;

import com.logistics.dto.auth.AuthResponse;
import com.logistics.dto.auth.LoginRequest;
import com.logistics.dto.auth.RegisterRequest;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Registers a new user.
     *
     * @param request the registration data
     * @return auth response with JWT token
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request the login credentials
     * @return auth response with JWT token
     */
    AuthResponse login(LoginRequest request);
}
