package com.logistics.repository;

import com.logistics.model.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Company entity database operations.
 *
 * SOLID Principles Applied:
 * - Interface Segregation (ISP): Contains only company-specific query methods.
 * - Dependency Inversion (DIP): Services depend on this abstraction.
 *
 * Spring Data JPA provides the implementation automatically.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Finds a company by its registration number.
     * Registration numbers are unique identifiers.
     *
     * @param registrationNumber the official registration number
     * @return Optional containing the company if found
     */
    Optional<Company> findByRegistrationNumber(String registrationNumber);

    /**
     * Finds a company by its name.
     *
     * @param name the company name
     * @return Optional containing the company if found
     */
    Optional<Company> findByName(String name);

    /**
     * Checks if a registration number already exists.
     *
     * @param registrationNumber the registration number to check
     * @return true if registration number exists
     */
    boolean existsByRegistrationNumber(String registrationNumber);
}
