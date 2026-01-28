package com.logistics.dto.auth;

import com.logistics.model.enums.Role;

/**
 * DTO for authentication responses (login/register).
 *
 * Contains JWT token and basic user information after successful authentication.
 */
public class AuthResponse {

    /**
     * JWT token for subsequent authenticated requests.
     * Include in Authorization header as "Bearer {token}".
     */
    private String token;

    /**
     * Type of token (always "Bearer" for this application).
     */
    private String tokenType = "Bearer";

    /**
     * User's ID for client reference.
     */
    private Long userId;

    /**
     * User's username for display.
     */
    private String username;

    /**
     * User's email for display.
     */
    private String email;

    /**
     * User's role for client-side access control.
     */
    private Role role;

    // Default constructor
    public AuthResponse() {
    }

    /**
     * Constructs an AuthResponse with all fields.
     *
     * @param token    the JWT token
     * @param userId   the user's ID
     * @param username the user's username
     * @param email    the user's email
     * @param role     the user's role
     */
    public AuthResponse(String token, Long userId, String username, String email, Role role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
