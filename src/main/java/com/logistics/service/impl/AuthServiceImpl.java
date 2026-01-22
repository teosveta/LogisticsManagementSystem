package com.logistics.service.impl;

import com.logistics.dto.auth.AuthResponse;
import com.logistics.dto.auth.LoginRequest;
import com.logistics.dto.auth.RegisterRequest;
import com.logistics.exception.DuplicateResourceException;
import com.logistics.model.entity.Customer;
import com.logistics.model.entity.Employee;
import com.logistics.model.entity.User;
import com.logistics.model.enums.EmployeeType;
import com.logistics.model.enums.Role;
import com.logistics.repository.CustomerRepository;
import com.logistics.repository.EmployeeRepository;
import com.logistics.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.logistics.security.JwtTokenProvider;
import com.logistics.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Only handles authentication logic.
 *   Password encoding is delegated to PasswordEncoder.
 *   Token generation is delegated to JwtTokenProvider.
 * - Dependency Inversion (DIP): Depends on interfaces (PasswordEncoder,
 *   AuthenticationManager) not concrete implementations.
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           EmployeeRepository employeeRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user: {}", request.getUsername());

        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create user with encrypted password
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);
        logger.info("User created with ID: {}", savedUser.getId());

        // Create role-specific records automatically
        if (Role.CUSTOMER.equals(request.getRole())) {
            // Create Customer record for CUSTOMER role
            Customer customer = new Customer(savedUser);
            customerRepository.save(customer);
            logger.info("Customer record created for user: {}", savedUser.getUsername());
        } else if (Role.EMPLOYEE.equals(request.getRole())) {
            // Create Employee record for EMPLOYEE role
            Employee employee = new Employee();
            employee.setUser(savedUser);
            employee.setEmployeeType(EmployeeType.OFFICE_STAFF);
            employee.setHireDate(LocalDate.now());
            employee.setSalary(BigDecimal.ZERO);
            // company and office are null - can be assigned later by admin
            employeeRepository.save(employee);
            logger.info("Employee record created for user: {}", savedUser.getUsername());
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(savedUser.getUsername(), savedUser.getRole());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        logger.info("User login attempt: {}", request.getUsername());

        // Authenticate using Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // If authentication successful, get user and generate token
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole());

        logger.info("User logged in successfully: {}", user.getUsername());

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
