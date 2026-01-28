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
 * Returns JSON 401 response when unauthenticated users access protected resources.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

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
