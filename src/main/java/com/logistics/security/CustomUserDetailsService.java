package com.logistics.security;

import com.logistics.model.entity.User;
import com.logistics.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation for Spring Security.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Only loads user details for authentication.
 *   Doesn't handle user management - that's UserRepository/AuthService's job.
 * - Interface Segregation (ISP): Implements only UserDetailsService.
 * - Liskov Substitution (LSP): Can be used anywhere UserDetailsService is expected.
 * - Dependency Inversion (DIP): Depends on UserRepository abstraction.
 *
 * This service is used by Spring Security during authentication to load user details.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user details by username for Spring Security authentication.
     *
     * @param username the username to look up
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user details for: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        // Convert our User entity to Spring Security's UserDetails
        // The role is prefixed with "ROLE_" as per Spring Security convention
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
