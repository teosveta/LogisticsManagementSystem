package com.logistics.security;

import com.logistics.model.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token Provider for generating and validating JWT tokens.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This class ONLY handles JWT token operations.
 *   It doesn't handle authentication logic - that's AuthService's job.
 * - Open/Closed (OCP): Token configuration comes from external properties,
 *   so behavior can be changed without modifying code.
 * - Dependency Inversion (DIP): Configuration values are injected.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey secretKey;
    private final long expirationMs;

    /**
     * Constructor that initializes JWT configuration from application.properties.
     *
     * @param jwtSecret     the secret key for signing tokens
     * @param jwtExpiration token expiration time in milliseconds
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration}") long jwtExpiration) {

        // Create a secure key from the secret string
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = jwtExpiration;

        logger.info("JwtTokenProvider initialized with expiration: {} ms", jwtExpiration);
    }

    /**
     * Generates a JWT token for a user.
     *
     * @param username the username to include in the token
     * @param role     the user's role to include in the token
     * @return the generated JWT token string
     */
    public String generateToken(String username, Role role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        String token = Jwts.builder()
                .subject(username)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();

        logger.debug("Generated token for user: {}, expires at: {}", username, expiryDate);

        return token;
    }

    /**
     * Extracts the username from a JWT token.
     *
     * @param token the JWT token
     * @return the username stored in the token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Extracts the role from a JWT token.
     *
     * @param token the JWT token
     * @return the role stored in the token
     */
    public Role getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String roleName = claims.get("role", String.class);
        return Role.valueOf(roleName);
    }

    /**
     * Validates a JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
        }
        return false;
    }
}
