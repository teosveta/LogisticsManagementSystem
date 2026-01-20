package com.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Logistics Management System application.
 *
 * This Spring Boot application demonstrates SOLID principles throughout its architecture:
 * - Single Responsibility: Each component has one clear purpose
 * - Open/Closed: Services use interfaces for extensibility
 * - Liskov Substitution: All implementations are interchangeable via interfaces
 * - Interface Segregation: Small, focused interfaces for each domain
 * - Dependency Inversion: High-level modules depend on abstractions
 *
 * @author Logistics Management System Team
 * @version 1.0.0
 */
@SpringBootApplication
public class LogisticsApplication {

    /**
     * Application entry point.
     * Bootstraps the Spring Boot application with auto-configuration.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(LogisticsApplication.class, args);
    }
}
