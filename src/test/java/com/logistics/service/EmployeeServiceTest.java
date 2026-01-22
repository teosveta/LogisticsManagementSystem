package com.logistics.service;

import com.logistics.dto.employee.EmployeeRequest;
import com.logistics.dto.employee.EmployeeResponse;
import com.logistics.exception.DuplicateResourceException;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.model.entity.Company;
import com.logistics.model.entity.Employee;
import com.logistics.model.entity.Office;
import com.logistics.model.entity.User;
import com.logistics.model.enums.EmployeeType;
import com.logistics.model.enums.Role;
import com.logistics.repository.CompanyRepository;
import com.logistics.repository.EmployeeRepository;
import com.logistics.repository.OfficeRepository;
import com.logistics.repository.UserRepository;
import com.logistics.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeService.
 * Tests employee CRUD operations in isolation.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private OfficeRepository officeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private User user;
    private Company company;
    private Office office;
    private Employee employee;
    private EmployeeRequest employeeRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("employee");
        user.setEmail("employee@test.com");
        user.setRole(Role.EMPLOYEE);

        company = new Company();
        company.setId(1L);
        company.setName("Test Company");
        company.setRegistrationNumber("REG123");
        company.setAddress("123 Company St");

        office = new Office();
        office.setId(1L);
        office.setCompany(company);
        office.setName("Main Office");
        office.setAddress("100 Office St");
        office.setCity("Test City");
        office.setCountry("Test Country");

        employee = new Employee();
        employee.setId(1L);
        employee.setUser(user);
        employee.setCompany(company);
        employee.setEmployeeType(EmployeeType.OFFICE_STAFF);
        employee.setOffice(office);
        employee.setHireDate(LocalDate.of(2023, 1, 15));
        employee.setSalary(new BigDecimal("50000.00"));

        employeeRequest = new EmployeeRequest();
        employeeRequest.setUserId(1L);
        employeeRequest.setCompanyId(1L);
        employeeRequest.setEmployeeType(EmployeeType.OFFICE_STAFF);
        employeeRequest.setOfficeId(1L);
        employeeRequest.setHireDate(LocalDate.of(2023, 1, 15));
        employeeRequest.setSalary(new BigDecimal("50000.00"));
    }

    @Nested
    @DisplayName("createEmployee Tests")
    class CreateEmployeeTests {

        @Test
        @DisplayName("Should create employee with company and office successfully")
        void createEmployee_WithCompanyAndOffice_Success() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(employeeRepository.existsByUserId(1L)).thenReturn(false);
            when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
            when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
                Employee e = invocation.getArgument(0);
                e.setId(1L);
                return e;
            });

            // Act
            EmployeeResponse response = employeeService.createEmployee(employeeRequest);

            // Assert
            assertNotNull(response);
            assertEquals(EmployeeType.OFFICE_STAFF, response.getEmployeeType());
            assertEquals(new BigDecimal("50000.00"), response.getSalary());
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should create employee without company (self-registered)")
        void createEmployee_WithoutCompany_Success() {
            // Arrange
            employeeRequest.setCompanyId(null);
            employeeRequest.setOfficeId(null);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(employeeRepository.existsByUserId(1L)).thenReturn(false);
            when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
                Employee e = invocation.getArgument(0);
                e.setId(1L);
                return e;
            });

            // Act
            EmployeeResponse response = employeeService.createEmployee(employeeRequest);

            // Assert
            assertNotNull(response);
            verify(companyRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void createEmployee_UserNotFound_ThrowsException() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> employeeService.createEmployee(employeeRequest));
        }

        @Test
        @DisplayName("Should throw exception for duplicate employee")
        void createEmployee_DuplicateEmployee_ThrowsException() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(employeeRepository.existsByUserId(1L)).thenReturn(true);

            // Act & Assert
            assertThrows(DuplicateResourceException.class,
                    () -> employeeService.createEmployee(employeeRequest));
        }

        @Test
        @DisplayName("Should throw exception when company not found")
        void createEmployee_CompanyNotFound_ThrowsException() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(employeeRepository.existsByUserId(1L)).thenReturn(false);
            when(companyRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> employeeService.createEmployee(employeeRequest));
        }
    }

    @Nested
    @DisplayName("getEmployeeById Tests")
    class GetEmployeeByIdTests {

        @Test
        @DisplayName("Should return employee when found")
        void getEmployeeById_Exists_ReturnsEmployee() {
            // Arrange
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

            // Act
            EmployeeResponse response = employeeService.getEmployeeById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getId());
        }

        @Test
        @DisplayName("Should throw exception when employee not found")
        void getEmployeeById_NotFound_ThrowsException() {
            // Arrange
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> employeeService.getEmployeeById(999L));
        }
    }

    @Nested
    @DisplayName("getEmployeeByUsername Tests")
    class GetEmployeeByUsernameTests {

        @Test
        @DisplayName("Should return employee when found by username")
        void getEmployeeByUsername_Exists_ReturnsEmployee() {
            // Arrange
            when(employeeRepository.findByUsername("employee")).thenReturn(Optional.of(employee));

            // Act
            EmployeeResponse response = employeeService.getEmployeeByUsername("employee");

            // Assert
            assertNotNull(response);
            assertEquals("employee", response.getUsername());
        }
    }

    @Nested
    @DisplayName("getAllEmployees Tests")
    class GetAllEmployeesTests {

        @Test
        @DisplayName("Should return all employees")
        void getAllEmployees_ReturnsAllEmployees() {
            // Arrange
            User user2 = new User();
            user2.setId(2L);
            user2.setUsername("employee2");
            user2.setRole(Role.EMPLOYEE);

            Employee employee2 = new Employee();
            employee2.setId(2L);
            employee2.setUser(user2);
            employee2.setEmployeeType(EmployeeType.COURIER);
            employee2.setHireDate(LocalDate.now());
            employee2.setSalary(new BigDecimal("40000.00"));

            when(employeeRepository.findAll()).thenReturn(Arrays.asList(employee, employee2));

            // Act
            List<EmployeeResponse> responses = employeeService.getAllEmployees();

            // Assert
            assertEquals(2, responses.size());
        }
    }

    @Nested
    @DisplayName("updateEmployee Tests")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("Should update employee successfully")
        void updateEmployee_ValidData_Success() {
            // Arrange
            EmployeeRequest updateRequest = new EmployeeRequest();
            updateRequest.setUserId(1L);
            updateRequest.setCompanyId(1L);
            updateRequest.setEmployeeType(EmployeeType.COURIER);
            updateRequest.setHireDate(LocalDate.of(2023, 6, 1));
            updateRequest.setSalary(new BigDecimal("55000.00"));

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

            // Act
            EmployeeResponse response = employeeService.updateEmployee(1L, updateRequest);

            // Assert
            assertNotNull(response);
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should throw exception when employee not found")
        void updateEmployee_NotFound_ThrowsException() {
            // Arrange
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> employeeService.updateEmployee(999L, employeeRequest));
        }
    }

    @Nested
    @DisplayName("deleteEmployee Tests")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("Should delete employee successfully")
        void deleteEmployee_Exists_Success() {
            // Arrange
            when(employeeRepository.existsById(1L)).thenReturn(true);
            doNothing().when(employeeRepository).deleteById(1L);

            // Act
            employeeService.deleteEmployee(1L);

            // Assert
            verify(employeeRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when employee not found")
        void deleteEmployee_NotFound_ThrowsException() {
            // Arrange
            when(employeeRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> employeeService.deleteEmployee(999L));
        }
    }
}
