package com.logistics.service;

import com.logistics.dto.company.CompanyRequest;
import com.logistics.dto.company.CompanyResponse;
import com.logistics.exception.DuplicateResourceException;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.model.entity.Company;
import com.logistics.repository.CompanyRepository;
import com.logistics.service.impl.CompanyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CompanyService.
 * Tests company CRUD operations in isolation.
 */
@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private Company company;
    private CompanyRequest companyRequest;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(1L);
        company.setName("Test Logistics");
        company.setRegistrationNumber("REG123");
        company.setAddress("123 Test St");
        company.setPhone("1234567890");
        company.setEmail("test@company.com");

        companyRequest = new CompanyRequest();
        companyRequest.setName("Test Logistics");
        companyRequest.setRegistrationNumber("REG123");
        companyRequest.setAddress("123 Test St");
        companyRequest.setPhone("1234567890");
        companyRequest.setEmail("test@company.com");
    }

    @Nested
    @DisplayName("createCompany Tests")
    class CreateCompanyTests {

        @Test
        @DisplayName("Should create company successfully")
        void createCompany_ValidData_Success() {
            // Arrange
            when(companyRepository.existsByRegistrationNumber(anyString())).thenReturn(false);
            when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
                Company c = invocation.getArgument(0);
                c.setId(1L);
                return c;
            });

            // Act
            CompanyResponse response = companyService.createCompany(companyRequest);

            // Assert
            assertNotNull(response);
            assertEquals("Test Logistics", response.getName());
            assertEquals("REG123", response.getRegistrationNumber());
            verify(companyRepository).save(any(Company.class));
        }

        @Test
        @DisplayName("Should throw exception for duplicate registration number")
        void createCompany_DuplicateRegistrationNumber_ThrowsException() {
            // Arrange
            when(companyRepository.existsByRegistrationNumber("REG123")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                    () -> companyService.createCompany(companyRequest));

            assertTrue(exception.getMessage().contains("registrationNumber"));
            verify(companyRepository, never()).save(any(Company.class));
        }
    }

    @Nested
    @DisplayName("getCompanyById Tests")
    class GetCompanyByIdTests {

        @Test
        @DisplayName("Should return company when found")
        void getCompanyById_Exists_ReturnsCompany() {
            // Arrange
            when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

            // Act
            CompanyResponse response = companyService.getCompanyById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("Test Logistics", response.getName());
        }

        @Test
        @DisplayName("Should throw exception when company not found")
        void getCompanyById_NotFound_ThrowsException() {
            // Arrange
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> companyService.getCompanyById(999L));

            assertTrue(exception.getMessage().contains("Company"));
        }
    }

    @Nested
    @DisplayName("getAllCompanies Tests")
    class GetAllCompaniesTests {

        @Test
        @DisplayName("Should return all companies")
        void getAllCompanies_ReturnsAllCompanies() {
            // Arrange
            Company company2 = new Company();
            company2.setId(2L);
            company2.setName("Second Logistics");
            company2.setRegistrationNumber("REG456");
            company2.setAddress("456 Second St");

            when(companyRepository.findAll()).thenReturn(Arrays.asList(company, company2));

            // Act
            List<CompanyResponse> responses = companyService.getAllCompanies();

            // Assert
            assertEquals(2, responses.size());
        }

        @Test
        @DisplayName("Should return empty list when no companies exist")
        void getAllCompanies_NoCompanies_ReturnsEmptyList() {
            // Arrange
            when(companyRepository.findAll()).thenReturn(Arrays.asList());

            // Act
            List<CompanyResponse> responses = companyService.getAllCompanies();

            // Assert
            assertTrue(responses.isEmpty());
        }
    }

    @Nested
    @DisplayName("updateCompany Tests")
    class UpdateCompanyTests {

        @Test
        @DisplayName("Should update company successfully")
        void updateCompany_ValidData_Success() {
            // Arrange
            CompanyRequest updateRequest = new CompanyRequest();
            updateRequest.setName("Updated Logistics");
            updateRequest.setRegistrationNumber("REG123"); // Same registration number
            updateRequest.setAddress("999 Updated St");
            updateRequest.setPhone("9999999999");
            updateRequest.setEmail("updated@company.com");

            when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
            when(companyRepository.save(any(Company.class))).thenReturn(company);

            // Act
            CompanyResponse response = companyService.updateCompany(1L, updateRequest);

            // Assert
            assertNotNull(response);
            verify(companyRepository).save(any(Company.class));
        }

        @Test
        @DisplayName("Should throw exception when updating to duplicate registration number")
        void updateCompany_DuplicateRegistrationNumber_ThrowsException() {
            // Arrange
            CompanyRequest updateRequest = new CompanyRequest();
            updateRequest.setName("Updated Logistics");
            updateRequest.setRegistrationNumber("REG999"); // Different registration number
            updateRequest.setAddress("999 Updated St");

            when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
            when(companyRepository.existsByRegistrationNumber("REG999")).thenReturn(true);

            // Act & Assert
            assertThrows(DuplicateResourceException.class,
                    () -> companyService.updateCompany(1L, updateRequest));
        }

        @Test
        @DisplayName("Should throw exception when company not found")
        void updateCompany_NotFound_ThrowsException() {
            // Arrange
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> companyService.updateCompany(999L, companyRequest));
        }
    }

    @Nested
    @DisplayName("deleteCompany Tests")
    class DeleteCompanyTests {

        @Test
        @DisplayName("Should delete company successfully")
        void deleteCompany_Exists_Success() {
            // Arrange
            when(companyRepository.existsById(1L)).thenReturn(true);
            doNothing().when(companyRepository).deleteById(1L);

            // Act
            companyService.deleteCompany(1L);

            // Assert
            verify(companyRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when company not found")
        void deleteCompany_NotFound_ThrowsException() {
            // Arrange
            when(companyRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> companyService.deleteCompany(999L));
        }
    }
}
