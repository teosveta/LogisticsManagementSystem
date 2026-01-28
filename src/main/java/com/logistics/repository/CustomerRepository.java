package com.logistics.repository;

import com.logistics.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Customer entity database operations.
 *
 * Spring Data JPA provides the implementation automatically.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Finds a customer by their associated user ID.
     *
     * @param userId the user's ID
     * @return Optional containing the customer if found
     */
    Optional<Customer> findByUserId(Long userId);

    /**
     * Finds a customer by their associated username.
     *
     * @param username the user's username
     * @return Optional containing the customer if found
     */
    @Query("SELECT c FROM Customer c WHERE c.user.username = :username")
    Optional<Customer> findByUsername(@Param("username") String username);

    /**
     * Finds a customer by their associated email.
     *
     * @param email the user's email
     * @return Optional containing the customer if found
     */
    @Query("SELECT c FROM Customer c WHERE c.user.email = :email")
    Optional<Customer> findByEmail(@Param("email") String email);

    /**
     * Checks if a customer exists for a specific user.
     *
     * @param userId the user's ID
     * @return true if a customer record exists for this user
     */
    boolean existsByUserId(Long userId);
}
