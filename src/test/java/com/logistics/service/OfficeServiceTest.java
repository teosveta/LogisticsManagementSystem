package com.logistics.service;

import com.logistics.dto.office.OfficeRequest;
import com.logistics.dto.office.OfficeResponse;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.model.entity.Company;
import com.logistics.model.entity.Office;
import com.logistics.repository.CompanyRepository;
import com.logistics.repository.OfficeRepository;
import com.logistics.service.impl.OfficeServiceImpl;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for OfficeService.
 * Tests office CRUD operations in isolation.
 */
@ExtendWith(MockitoExtension.class)
class OfficeServiceTest {

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private OfficeServiceImpl officeService;

    private Company company;
    private Office office;
    private OfficeRequest officeRequest;

    @BeforeEach
    void setUp() {
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
        office.setPhone("1234567890");

        officeRequest = new OfficeRequest();
        officeRequest.setCompanyId(1L);
        officeRequest.setName("Main Office");
        officeRequest.setAddress("100 Office St");
        officeRequest.setCity("Test City");
        officeRequest.setCountry("Test Country");
        officeRequest.setPhone("1234567890");
    }

    @Nested
    @DisplayName("createOffice Tests")
    class CreateOfficeTests {

        @Test
        @DisplayName("Should create office successfully")
        void createOffice_ValidData_Success() {
            // Arrange
            when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
            when(officeRepository.save(any(Office.class))).thenAnswer(invocation -> {
                Office o = invocation.getArgument(0);
                o.setId(1L);
                return o;
            });

            // Act
            OfficeResponse response = officeService.createOffice(officeRequest);

            // Assert
            assertNotNull(response);
            assertEquals("Main Office", response.getName());
            assertEquals("Test City", response.getCity());
            verify(officeRepository).save(any(Office.class));
        }

        @Test
        @DisplayName("Should throw exception when company not found")
        void createOffice_CompanyNotFound_ThrowsException() {
            // Arrange
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());
            officeRequest.setCompanyId(999L);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> officeService.createOffice(officeRequest));
        }
    }

    @Nested
    @DisplayName("getOfficeById Tests")
    class GetOfficeByIdTests {

        @Test
        @DisplayName("Should return office when found")
        void getOfficeById_Exists_ReturnsOffice() {
            // Arrange
            when(officeRepository.findById(1L)).thenReturn(Optional.of(office));

            // Act
            OfficeResponse response = officeService.getOfficeById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("Main Office", response.getName());
        }

        @Test
        @DisplayName("Should throw exception when office not found")
        void getOfficeById_NotFound_ThrowsException() {
            // Arrange
            when(officeRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> officeService.getOfficeById(999L));
        }
    }

    @Nested
    @DisplayName("getAllOffices Tests")
    class GetAllOfficesTests {

        @Test
        @DisplayName("Should return all offices")
        void getAllOffices_ReturnsAllOffices() {
            // Arrange
            Office office2 = new Office();
            office2.setId(2L);
            office2.setCompany(company);
            office2.setName("Branch Office");
            office2.setAddress("200 Branch St");
            office2.setCity("Branch City");
            office2.setCountry("Test Country");

            when(officeRepository.findAll()).thenReturn(Arrays.asList(office, office2));

            // Act
            List<OfficeResponse> responses = officeService.getAllOffices();

            // Assert
            assertEquals(2, responses.size());
        }

        @Test
        @DisplayName("Should return empty list when no offices exist")
        void getAllOffices_NoOffices_ReturnsEmptyList() {
            // Arrange
            when(officeRepository.findAll()).thenReturn(Arrays.asList());

            // Act
            List<OfficeResponse> responses = officeService.getAllOffices();

            // Assert
            assertTrue(responses.isEmpty());
        }
    }

    @Nested
    @DisplayName("getOfficesByCompanyId Tests")
    class GetOfficesByCompanyIdTests {

        @Test
        @DisplayName("Should return offices for company")
        void getOfficesByCompanyId_ReturnsOffices() {
            // Arrange
            when(companyRepository.existsById(1L)).thenReturn(true);
            when(officeRepository.findByCompanyId(1L)).thenReturn(Arrays.asList(office));

            // Act
            List<OfficeResponse> responses = officeService.getOfficesByCompanyId(1L);

            // Assert
            assertEquals(1, responses.size());
            assertEquals("Main Office", responses.get(0).getName());
        }

        @Test
        @DisplayName("Should throw exception when company not found")
        void getOfficesByCompanyId_CompanyNotFound_ThrowsException() {
            // Arrange
            when(companyRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> officeService.getOfficesByCompanyId(999L));
        }
    }

    @Nested
    @DisplayName("updateOffice Tests")
    class UpdateOfficeTests {

        @Test
        @DisplayName("Should update office successfully")
        void updateOffice_ValidData_Success() {
            // Arrange
            OfficeRequest updateRequest = new OfficeRequest();
            updateRequest.setCompanyId(1L);
            updateRequest.setName("Updated Office");
            updateRequest.setAddress("999 Updated St");
            updateRequest.setCity("Updated City");
            updateRequest.setCountry("Updated Country");
            updateRequest.setPhone("9999999999");

            when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
            when(officeRepository.save(any(Office.class))).thenReturn(office);

            // Act
            OfficeResponse response = officeService.updateOffice(1L, updateRequest);

            // Assert
            assertNotNull(response);
            verify(officeRepository).save(any(Office.class));
        }

        @Test
        @DisplayName("Should update office with new company")
        void updateOffice_ChangeCompany_Success() {
            // Arrange
            Company newCompany = new Company();
            newCompany.setId(2L);
            newCompany.setName("New Company");
            newCompany.setRegistrationNumber("REG456");
            newCompany.setAddress("456 New St");

            OfficeRequest updateRequest = new OfficeRequest();
            updateRequest.setCompanyId(2L);
            updateRequest.setName("Updated Office");
            updateRequest.setAddress("999 Updated St");
            updateRequest.setCity("Updated City");
            updateRequest.setCountry("Updated Country");

            when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
            when(companyRepository.findById(2L)).thenReturn(Optional.of(newCompany));
            when(officeRepository.save(any(Office.class))).thenReturn(office);

            // Act
            OfficeResponse response = officeService.updateOffice(1L, updateRequest);

            // Assert
            assertNotNull(response);
            verify(companyRepository).findById(2L);
        }

        @Test
        @DisplayName("Should throw exception when office not found")
        void updateOffice_NotFound_ThrowsException() {
            // Arrange
            when(officeRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> officeService.updateOffice(999L, officeRequest));
        }
    }

    @Nested
    @DisplayName("deleteOffice Tests")
    class DeleteOfficeTests {

        @Test
        @DisplayName("Should delete office successfully")
        void deleteOffice_Exists_Success() {
            // Arrange
            when(officeRepository.existsById(1L)).thenReturn(true);
            doNothing().when(officeRepository).deleteById(1L);

            // Act
            officeService.deleteOffice(1L);

            // Assert
            verify(officeRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when office not found")
        void deleteOffice_NotFound_ThrowsException() {
            // Arrange
            when(officeRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> officeService.deleteOffice(999L));
        }
    }
}
