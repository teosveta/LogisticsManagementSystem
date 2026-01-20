package com.logistics.service;

import com.logistics.dto.auth.AuthResponse;
import com.logistics.dto.auth.LoginRequest;
import com.logistics.dto.auth.RegisterRequest;

/**
 * Service interface for authentication operations.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Only handles authentication (login/register).
 *   User management is in a separate service if needed.
 * - Interface Segregation (ISP): Small, focused interface.
 * - Dependency Inversion (DIP): Controllers depend on this interface.
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
