package com.logistics.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 *
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 * Access API docs at: http://localhost:8080/api-docs
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Logistics Management System API",
                version = "1.0.0",
                description = """
                        REST API for Logistics Company Management System.

                        ## Features
                        - User authentication with JWT tokens
                        - Company and office management
                        - Employee and customer management
                        - Shipment registration and tracking
                        - Comprehensive reporting

                        ## Authentication
                        Use the /api/auth/login endpoint to get a JWT token.
                        Include the token in the Authorization header: `Bearer <token>`

                        ## Roles
                        - **EMPLOYEE**: Full access to all features
                        - **CUSTOMER**: Limited access to their own shipments

                        ## SOLID Principles
                        This API is built following SOLID principles:
                        - Single Responsibility: Each component has one purpose
                        - Open/Closed: Extensible without modification
                        - Liskov Substitution: Interfaces are properly abstracted
                        - Interface Segregation: Small, focused interfaces
                        - Dependency Inversion: Depend on abstractions
                        """,
                contact = @Contact(
                        name = "Logistics System Support",
                        email = "support@logistics.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Development Server")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT authentication. Use the /api/auth/login endpoint to get a token."
)
public class OpenApiConfig {
    // Configuration is done via annotations
}
