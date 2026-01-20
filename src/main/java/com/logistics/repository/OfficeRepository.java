package com.logistics.repository;

import com.logistics.model.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Office entity database operations.
 *
 * SOLID Principles Applied:
 * - Interface Segregation (ISP): Contains only office-specific query methods.
 * - Dependency Inversion (DIP): Services depend on this abstraction.
 *
 * Spring Data JPA provides the implementation automatically.
 */
@Repository
public interface OfficeRepository extends JpaRepository<Office, Long> {

    /**
     * Finds all offices belonging to a specific company.
     *
     * @param companyId the company's ID
     * @return list of offices for the company
     */
    List<Office> findByCompanyId(Long companyId);

    /**
     * Finds all offices in a specific city.
     *
     * @param city the city name
     * @return list of offices in the city
     */
    List<Office> findByCity(String city);

    /**
     * Finds all offices in a specific country.
     *
     * @param country the country name
     * @return list of offices in the country
     */
    List<Office> findByCountry(String country);

    /**
     * Finds offices by name (partial match, case-insensitive).
     *
     * @param name the name pattern to search
     * @return list of matching offices
     */
    List<Office> findByNameContainingIgnoreCase(String name);
}
