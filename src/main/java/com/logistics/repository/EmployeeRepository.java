package com.logistics.repository;

import com.logistics.model.entity.Employee;
import com.logistics.model.enums.EmployeeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity database operations.
 *
 * SOLID Principles Applied:
 * - Interface Segregation (ISP): Contains only employee-specific query methods.
 * - Dependency Inversion (DIP): Services depend on this abstraction.
 *
 * Spring Data JPA provides the implementation automatically.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Finds an employee by their associated user ID.
     *
     * @param userId the user's ID
     * @return Optional containing the employee if found
     */
    Optional<Employee> findByUserId(Long userId);

    /**
     * Finds an employee by their associated username.
     *
     * @param username the user's username
     * @return Optional containing the employee if found
     */
    @Query("SELECT e FROM Employee e WHERE e.user.username = :username")
    Optional<Employee> findByUsername(@Param("username") String username);

    /**
     * Finds all employees of a specific company.
     *
     * @param companyId the company's ID
     * @return list of employees for the company
     */
    List<Employee> findByCompanyId(Long companyId);

    /**
     * Finds all employees of a specific type (COURIER or OFFICE_STAFF).
     *
     * @param employeeType the employee type to filter by
     * @return list of employees of the specified type
     */
    List<Employee> findByEmployeeType(EmployeeType employeeType);

    /**
     * Finds all employees assigned to a specific office.
     *
     * @param officeId the office's ID
     * @return list of employees at the office
     */
    List<Employee> findByOfficeId(Long officeId);

    /**
     * Checks if an employee exists for a specific user.
     *
     * @param userId the user's ID
     * @return true if an employee record exists for this user
     */
    boolean existsByUserId(Long userId);

    /**
     * Finds all couriers (employees with COURIER type).
     *
     * @return list of all couriers
     */
    default List<Employee> findAllCouriers() {
        return findByEmployeeType(EmployeeType.COURIER);
    }

    /**
     * Finds all office staff (employees with OFFICE_STAFF type).
     *
     * @return list of all office staff
     */
    default List<Employee> findAllOfficeStaff() {
        return findByEmployeeType(EmployeeType.OFFICE_STAFF);
    }
}
