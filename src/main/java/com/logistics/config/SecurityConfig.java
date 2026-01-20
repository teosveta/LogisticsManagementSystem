package com.logistics.config;

import com.logistics.security.JwtAuthenticationEntryPoint;
import com.logistics.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for JWT-based authentication.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This class configures security ONLY.
 *   It doesn't implement security logic - that's in filters and services.
 * - Open/Closed (OCP): Security rules are configurable through this class
 *   without modifying the security framework itself.
 * - Dependency Inversion (DIP): Depends on abstractions (UserDetailsService,
 *   PasswordEncoder) not concrete implementations.
 *
 * Access Control:
 * - /api/auth/** - Public (login, register)
 * - /swagger-ui/**, /api-docs/** - Public (API documentation)
 * - All other endpoints - Require authentication
 *
 * Role-based access is enforced at the controller level using @PreAuthorize.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configures the security filter chain.
     *
     * Key configurations:
     * 1. CSRF disabled (using stateless JWT)
     * 2. Session management set to stateless
     * 3. Public endpoints for auth and Swagger
     * 4. All other requests require authentication
     * 5. JWT filter added before username/password filter
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF - we're using stateless JWT authentication
                .csrf(csrf -> csrf.disable())

                // Set session management to stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Set unauthorized handler
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Static files - public access (no authentication required)
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/*.html").permitAll()
                        .requestMatchers("/css/**").permitAll()
                        .requestMatchers("/js/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()

                        // Auth endpoints - public access
                        .requestMatchers("/api/auth/**").permitAll()

                        // Swagger/OpenAPI documentation
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()

                        // Health check endpoint (useful for deployment)
                        .requestMatchers("/actuator/health").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Add authentication provider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before the standard authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Password encoder using BCrypt.
     * BCrypt is a secure hashing algorithm that includes salt automatically.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider that uses our UserDetailsService and PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager bean required for programmatic authentication (login).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
