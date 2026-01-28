package com.logistics.repository;

import com.logistics.model.entity.User;
import com.logistics.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity database operations.
 *
 * Spring Data JPA automatically generates the implementation based on method names.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     * Used primarily for authentication.
     *
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     * Used for registration validation and password recovery.
     *
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a username already exists.
     * Used during registration to prevent duplicates.
     *
     * @param username the username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Checks if an email already exists.
     * Used during registration to prevent duplicates.
     *
     * @param email the email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Finds all users with a specific role.
     *
     * @param role the role to filter by
     * @return list of users with the specified role
     */
    List<User> findByRole(Role role);
}
