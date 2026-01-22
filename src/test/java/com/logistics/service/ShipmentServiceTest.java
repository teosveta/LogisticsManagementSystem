package com.logistics.service;

import com.logistics.dto.shipment.ShipmentRequest;
import com.logistics.dto.shipment.ShipmentResponse;
import com.logistics.dto.shipment.ShipmentStatusUpdateRequest;
import com.logistics.exception.InvalidDataException;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.model.entity.*;
import com.logistics.model.enums.EmployeeType;
import com.logistics.model.enums.Role;
import com.logistics.model.enums.ShipmentStatus;
import com.logistics.repository.CustomerRepository;
import com.logistics.repository.EmployeeRepository;
import com.logistics.repository.OfficeRepository;
import com.logistics.repository.ShipmentRepository;
import com.logistics.service.impl.ShipmentServiceImpl;
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
 * Unit tests for ShipmentService.
 * Tests shipment business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    private User senderUser;
    private User recipientUser;
    private User employeeUser;
    private Customer sender;
    private Customer recipient;
    private Employee employee;
    private Company company;
    private Office office;
    private Shipment shipment;

    @BeforeEach
    void setUp() {
        // Setup test users
        senderUser = new User();
        senderUser.setId(1L);
        senderUser.setUsername("sender");
        senderUser.setEmail("sender@test.com");
        senderUser.setRole(Role.CUSTOMER);

        recipientUser = new User();
        recipientUser.setId(2L);
        recipientUser.setUsername("recipient");
        recipientUser.setEmail("recipient@test.com");
        recipientUser.setRole(Role.CUSTOMER);

        employeeUser = new User();
        employeeUser.setId(3L);
        employeeUser.setUsername("employee");
        employeeUser.setEmail("employee@test.com");
        employeeUser.setRole(Role.EMPLOYEE);

        // Setup test customers
        sender = new Customer(senderUser);
        sender.setId(1L);
        sender.setPhone("1234567890");
        sender.setAddress("123 Sender St");

        recipient = new Customer(recipientUser);
        recipient.setId(2L);
        recipient.setPhone("0987654321");
        recipient.setAddress("456 Recipient Ave");

        // Setup test company and office
        company = new Company();
        company.setId(1L);
        company.setName("Test Logistics");
        company.setRegistrationNumber("TEST123");
        company.setAddress("789 Company Blvd");

        office = new Office();
        office.setId(1L);
        office.setCompany(company);
        office.setName("Main Office");
        office.setAddress("100 Office St");
        office.setCity("Test City");
        office.setCountry("Test Country");

        // Setup test employee
        employee = new Employee();
        employee.setId(1L);
        employee.setUser(employeeUser);
        employee.setCompany(company);
        employee.setEmployeeType(EmployeeType.OFFICE_STAFF);
        employee.setHireDate(LocalDate.now());
        employee.setSalary(new BigDecimal("50000.00"));

        // Setup test shipment
        shipment = new Shipment();
        shipment.setId(1L);
        shipment.setSender(sender);
        shipment.setRecipient(recipient);
        shipment.setRegisteredBy(employee);
        shipment.setWeight(new BigDecimal("5.00"));
        shipment.setPrice(new BigDecimal("25.00"));
        shipment.setDeliveryAddress("789 Delivery Rd");
        shipment.setStatus(ShipmentStatus.REGISTERED);
    }

    @Nested
    @DisplayName("registerShipment Tests")
    class RegisterShipmentTests {

        @Test
        @DisplayName("Should register shipment with address delivery successfully")
        void registerShipment_AddressDelivery_Success() {
            // Arrange
            ShipmentRequest request = new ShipmentRequest();
            request.setSenderId(1L);
            request.setRecipientId(2L);
            request.setDeliveryAddress("789 Delivery Rd");
            request.setWeight(new BigDecimal("5.00"));

            when(employeeRepository.findByUsername("employee")).thenReturn(Optional.of(employee));
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sender));
            when(customerRepository.findById(2L)).thenReturn(Optional.of(recipient));
            when(pricingService.calculatePrice(any(), eq(false))).thenReturn(new BigDecimal("25.00"));
            when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
                Shipment s = invocation.getArgument(0);
                s.setId(1L);
                return s;
            });

            // Act
            ShipmentResponse response = shipmentService.registerShipment(request, "employee");

            // Assert
            assertNotNull(response);
            assertEquals(new BigDecimal("25.00"), response.getPrice());
            assertEquals("789 Delivery Rd", response.getDeliveryAddress());
            assertEquals(ShipmentStatus.REGISTERED, response.getStatus());
            verify(shipmentRepository).save(any(Shipment.class));
        }

        @Test
        @DisplayName("Should register shipment with office delivery successfully")
        void registerShipment_OfficeDelivery_Success() {
            // Arrange
            ShipmentRequest request = new ShipmentRequest();
            request.setSenderId(1L);
            request.setRecipientId(2L);
            request.setDeliveryOfficeId(1L);
            request.setWeight(new BigDecimal("5.00"));

            when(employeeRepository.findByUsername("employee")).thenReturn(Optional.of(employee));
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sender));
            when(customerRepository.findById(2L)).thenReturn(Optional.of(recipient));
            when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
            when(pricingService.calculatePrice(any(), eq(true))).thenReturn(new BigDecimal("15.00"));
            when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
                Shipment s = invocation.getArgument(0);
                s.setId(1L);
                return s;
            });

            // Act
            ShipmentResponse response = shipmentService.registerShipment(request, "employee");

            // Assert
            assertNotNull(response);
            assertEquals(new BigDecimal("15.00"), response.getPrice());
            assertNotNull(response.getDeliveryOfficeName());
            verify(pricingService).calculatePrice(any(), eq(true));
        }

        @Test
        @DisplayName("Should throw exception when both address and office are provided")
        void registerShipment_BothAddressAndOffice_ThrowsException() {
            // Arrange
            ShipmentRequest request = new ShipmentRequest();
            request.setSenderId(1L);
            request.setRecipientId(2L);
            request.setDeliveryAddress("789 Delivery Rd");
            request.setDeliveryOfficeId(1L);
            request.setWeight(new BigDecimal("5.00"));

            // Act & Assert
            InvalidDataException exception = assertThrows(InvalidDataException.class,
                    () -> shipmentService.registerShipment(request, "employee"));

            assertTrue(exception.getMessage().contains("Cannot specify both"));
        }

        @Test
        @DisplayName("Should throw exception when neither address nor office is provided")
        void registerShipment_NoDeliveryDestination_ThrowsException() {
            // Arrange
            ShipmentRequest request = new ShipmentRequest();
            request.setSenderId(1L);
            request.setRecipientId(2L);
            request.setWeight(new BigDecimal("5.00"));

            // Act & Assert
            InvalidDataException exception = assertThrows(InvalidDataException.class,
                    () -> shipmentService.registerShipment(request, "employee"));

            assertTrue(exception.getMessage().contains("Either deliveryAddress or deliveryOfficeId must be provided"));
        }

        @Test
        @DisplayName("Should throw exception when employee not found")
        void registerShipment_EmployeeNotFound_ThrowsException() {
            // Arrange
            ShipmentRequest request = new ShipmentRequest();
            request.setSenderId(1L);
            request.setRecipientId(2L);
            request.setDeliveryAddress("789 Delivery Rd");
            request.setWeight(new BigDecimal("5.00"));

            when(employeeRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> shipmentService.registerShipment(request, "unknown"));
        }

        @Test
        @DisplayName("Should throw exception when sender not found")
        void registerShipment_SenderNotFound_ThrowsException() {
            // Arrange
            ShipmentRequest request = new ShipmentRequest();
            request.setSenderId(999L);
            request.setRecipientId(2L);
            request.setDeliveryAddress("789 Delivery Rd");
            request.setWeight(new BigDecimal("5.00"));

            when(employeeRepository.findByUsername("employee")).thenReturn(Optional.of(employee));
            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> shipmentService.registerShipment(request, "employee"));

            assertTrue(exception.getMessage().contains("sender"));
        }
    }

    @Nested
    @DisplayName("getShipmentById Tests")
    class GetShipmentByIdTests {

        @Test
        @DisplayName("Should return shipment when found")
        void getShipmentById_Exists_ReturnsShipment() {
            // Arrange
            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

            // Act
            ShipmentResponse response = shipmentService.getShipmentById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals(ShipmentStatus.REGISTERED, response.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when shipment not found")
        void getShipmentById_NotFound_ThrowsException() {
            // Arrange
            when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> shipmentService.getShipmentById(999L));
        }
    }

    @Nested
    @DisplayName("getAllShipments Tests")
    class GetAllShipmentsTests {

        @Test
        @DisplayName("Should return all shipments")
        void getAllShipments_ReturnsAllShipments() {
            // Arrange
            Shipment shipment2 = new Shipment();
            shipment2.setId(2L);
            shipment2.setSender(sender);
            shipment2.setRecipient(recipient);
            shipment2.setRegisteredBy(employee);
            shipment2.setWeight(new BigDecimal("3.00"));
            shipment2.setPrice(new BigDecimal("16.00"));
            shipment2.setDeliveryAddress("Another Address");
            shipment2.setStatus(ShipmentStatus.IN_TRANSIT);

            when(shipmentRepository.findAll()).thenReturn(Arrays.asList(shipment, shipment2));

            // Act
            List<ShipmentResponse> responses = shipmentService.getAllShipments();

            // Assert
            assertEquals(2, responses.size());
        }

        @Test
        @DisplayName("Should return empty list when no shipments exist")
        void getAllShipments_NoShipments_ReturnsEmptyList() {
            // Arrange
            when(shipmentRepository.findAll()).thenReturn(Arrays.asList());

            // Act
            List<ShipmentResponse> responses = shipmentService.getAllShipments();

            // Assert
            assertTrue(responses.isEmpty());
        }
    }

    @Nested
    @DisplayName("updateShipmentStatus Tests")
    class UpdateShipmentStatusTests {

        @Test
        @DisplayName("Should update status from REGISTERED to IN_TRANSIT")
        void updateShipmentStatus_RegisteredToInTransit_Success() {
            // Arrange
            ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
            request.setStatus(ShipmentStatus.IN_TRANSIT);

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

            // Act
            ShipmentResponse response = shipmentService.updateShipmentStatus(1L, request);

            // Assert
            assertEquals(ShipmentStatus.IN_TRANSIT, response.getStatus());
        }

        @Test
        @DisplayName("Should update status from IN_TRANSIT to DELIVERED")
        void updateShipmentStatus_InTransitToDelivered_Success() {
            // Arrange
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);
            ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
            request.setStatus(ShipmentStatus.DELIVERED);

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

            // Act
            ShipmentResponse response = shipmentService.updateShipmentStatus(1L, request);

            // Assert
            assertEquals(ShipmentStatus.DELIVERED, response.getStatus());
            assertNotNull(shipment.getDeliveredAt());
        }

        @Test
        @DisplayName("Should throw exception for invalid status transition from DELIVERED")
        void updateShipmentStatus_FromDelivered_ThrowsException() {
            // Arrange
            shipment.setStatus(ShipmentStatus.DELIVERED);
            ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
            request.setStatus(ShipmentStatus.IN_TRANSIT);

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

            // Act & Assert
            InvalidDataException exception = assertThrows(InvalidDataException.class,
                    () -> shipmentService.updateShipmentStatus(1L, request));

            assertTrue(exception.getMessage().contains("Cannot change status from DELIVERED"));
        }

        @Test
        @DisplayName("Should throw exception for invalid status transition from CANCELLED")
        void updateShipmentStatus_FromCancelled_ThrowsException() {
            // Arrange
            shipment.setStatus(ShipmentStatus.CANCELLED);
            ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
            request.setStatus(ShipmentStatus.IN_TRANSIT);

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

            // Act & Assert
            assertThrows(InvalidDataException.class,
                    () -> shipmentService.updateShipmentStatus(1L, request));
        }

        @Test
        @DisplayName("Should throw exception for invalid transition REGISTERED to DELIVERED")
        void updateShipmentStatus_RegisteredToDelivered_ThrowsException() {
            // Arrange
            ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
            request.setStatus(ShipmentStatus.DELIVERED);

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

            // Act & Assert
            InvalidDataException exception = assertThrows(InvalidDataException.class,
                    () -> shipmentService.updateShipmentStatus(1L, request));

            assertTrue(exception.getMessage().contains("Invalid status transition"));
        }
    }

    @Nested
    @DisplayName("updateShipment Tests")
    class UpdateShipmentTests {

        @Test
        @DisplayName("Should update shipment successfully")
        void updateShipment_ValidData_Success() {
            // Arrange
            ShipmentRequest request = new ShipmentRequest();
            request.setSenderId(1L);
            request.setRecipientId(2L);
            request.setDeliveryAddress("New Address");
            request.setWeight(new BigDecimal("7.00"));

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
            when(pricingService.calculatePrice(any(), eq(false))).thenReturn(new BigDecimal("29.00"));
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

            // Act
            ShipmentResponse response = shipmentService.updateShipment(1L, request, "employee");

            // Assert
            assertNotNull(response);
            verify(shipmentRepository).save(any(Shipment.class));
        }

        @Test
        @DisplayName("Should throw exception when updating delivered shipment")
        void updateShipment_DeliveredShipment_ThrowsException() {
            // Arrange
            shipment.setStatus(ShipmentStatus.DELIVERED);
            ShipmentRequest request = new ShipmentRequest();
            request.setSenderId(1L);
            request.setRecipientId(2L);
            request.setDeliveryAddress("New Address");
            request.setWeight(new BigDecimal("7.00"));

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

            // Act & Assert
            InvalidDataException exception = assertThrows(InvalidDataException.class,
                    () -> shipmentService.updateShipment(1L, request, "employee"));

            assertTrue(exception.getMessage().contains("Cannot update"));
        }
    }

    @Nested
    @DisplayName("deleteShipment Tests")
    class DeleteShipmentTests {

        @Test
        @DisplayName("Should delete shipment successfully")
        void deleteShipment_Exists_Success() {
            // Arrange
            when(shipmentRepository.existsById(1L)).thenReturn(true);
            doNothing().when(shipmentRepository).deleteById(1L);

            // Act
            shipmentService.deleteShipment(1L);

            // Assert
            verify(shipmentRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when shipment not found")
        void deleteShipment_NotFound_ThrowsException() {
            // Arrange
            when(shipmentRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> shipmentService.deleteShipment(999L));
        }
    }

    @Nested
    @DisplayName("getShipmentsByStatus Tests")
    class GetShipmentsByStatusTests {

        @Test
        @DisplayName("Should return shipments filtered by status")
        void getShipmentsByStatus_ReturnsFilteredShipments() {
            // Arrange
            when(shipmentRepository.findByStatus(ShipmentStatus.REGISTERED))
                    .thenReturn(Arrays.asList(shipment));

            // Act
            List<ShipmentResponse> responses = shipmentService.getShipmentsByStatus(ShipmentStatus.REGISTERED);

            // Assert
            assertEquals(1, responses.size());
            assertEquals(ShipmentStatus.REGISTERED, responses.get(0).getStatus());
        }
    }
}
