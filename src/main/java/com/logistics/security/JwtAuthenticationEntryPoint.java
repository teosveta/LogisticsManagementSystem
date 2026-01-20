package com.logistics.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Entry point for handling authentication failures.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Only handles authentication failure responses.
 *
 * This class is triggered when an unauthenticated user tries to access a protected resource.
 * It returns a proper JSON error response instead of a redirect or HTML error page.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Called when authentication is required but not provided.
     * Returns a 401 Unauthorized JSON response.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        logger.warn("Unauthorized access attempt to: {}", request.getRequestURI());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication required. Please provide a valid JWT token.",
                request.getRequestURI()
        );

        objectMapper.findAndRegisterModules(); // For Java 8 date/time support
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
