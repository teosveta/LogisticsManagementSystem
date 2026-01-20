package com.logistics.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter that intercepts requests and validates JWT tokens.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This filter ONLY handles JWT token extraction
 *   and setting up the security context. Token validation is in JwtTokenProvider.
 * - Dependency Inversion (DIP): Depends on JwtTokenProvider and UserDetailsService
 *   interfaces/abstractions.
 *
 * This filter runs once per request and:
 * 1. Extracts JWT from the Authorization header
 * 2. Validates the token
 * 3. Loads user details and sets up Spring Security context
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Filters each request to check for valid JWT token.
     * If valid, sets up the Spring Security context with the authenticated user.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract JWT from request header
            String jwt = extractJwtFromRequest(request);

            // If JWT is present and valid, authenticate the user
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                // Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Create authentication token
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Authenticated user: {}", username);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from the Authorization header.
     * Expected format: "Bearer <token>"
     *
     * @param request the HTTP request
     * @return the JWT token string, or null if not present
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
