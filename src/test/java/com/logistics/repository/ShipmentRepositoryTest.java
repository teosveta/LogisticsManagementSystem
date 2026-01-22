package com.logistics.repository;

import com.logistics.model.entity.*;
import com.logistics.model.enums.EmployeeType;
import com.logistics.model.enums.Role;
import com.logistics.model.enums.ShipmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for ShipmentRepository.
 * Tests custom queries using @DataJpaTest with H2.
 */
@DataJpaTest
@ActiveProfiles("test")
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ShipmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ShipmentRepository shipmentRepository;

    private User senderUser;
    private User recipientUser;
    private User employeeUser;
    private Customer sender;
    private Customer recipient;
    private Company company;
    private Employee employee;
    private Shipment shipment1;
    private Shipment shipment2;

    @BeforeEach
    void setUp() {
        // Create users
        senderUser = new User();
        senderUser.setUsername("sender");
        senderUser.setEmail("sender@test.com");
        senderUser.setPassword("password");
        senderUser.setRole(Role.CUSTOMER);
        entityManager.persist(senderUser);

        recipientUser = new User();
        recipientUser.setUsername("recipient");
        recipientUser.setEmail("recipient@test.com");
        recipientUser.setPassword("password");
        recipientUser.setRole(Role.CUSTOMER);
        entityManager.persist(recipientUser);

        employeeUser = new User();
        employeeUser.setUsername("employee");
        employeeUser.setEmail("employee@test.com");
        employeeUser.setPassword("password");
        employeeUser.setRole(Role.EMPLOYEE);
        entityManager.persist(employeeUser);

        // Create company
        company = new Company();
        company.setName("Test Company");
        company.setRegistrationNumber("REG123");
        company.setAddress("123 Company St");
        entityManager.persist(company);

        // Create customers
        sender = new Customer(senderUser);
        sender.setPhone("1111111111");
        sender.setAddress("111 Sender St");
        entityManager.persist(sender);

        recipient = new Customer(recipientUser);
        recipient.setPhone("2222222222");
        recipient.setAddress("222 Recipient St");
        entityManager.persist(recipient);

        // Create employee
        employee = new Employee();
        employee.setUser(employeeUser);
        employee.setCompany(company);
        employee.setEmployeeType(EmployeeType.OFFICE_STAFF);
        employee.setHireDate(LocalDate.now());
        employee.setSalary(new BigDecimal("50000.00"));
        entityManager.persist(employee);

        // Create shipments
        shipment1 = new Shipment();
        shipment1.setSender(sender);
        shipment1.setRecipient(recipient);
        shipment1.setRegisteredBy(employee);
        shipment1.setWeight(new BigDecimal("5.00"));
        shipment1.setPrice(new BigDecimal("25.00"));
        shipment1.setDeliveryAddress("123 Delivery St");
        shipment1.setStatus(ShipmentStatus.REGISTERED);
        entityManager.persist(shipment1);

        shipment2 = new Shipment();
        shipment2.setSender(sender);
        shipment2.setRecipient(recipient);
        shipment2.setRegisteredBy(employee);
        shipment2.setWeight(new BigDecimal("10.00"));
        shipment2.setPrice(new BigDecimal("45.00"));
        shipment2.setDeliveryAddress("456 Delivery St");
        shipment2.setStatus(ShipmentStatus.DELIVERED);
        shipment2.setDeliveredAt(LocalDateTime.now().minusDays(1));
        entityManager.persist(shipment2);

        entityManager.flush();
    }

    @Nested
    @DisplayName("findByRegisteredById Tests")
    class FindByRegisteredByIdTests {

        @Test
        @DisplayName("Should find shipments by employee ID")
        void findByRegisteredById_ReturnsShipments() {
            // Act
            List<Shipment> shipments = shipmentRepository.findByRegisteredById(employee.getId());

            // Assert
            assertEquals(2, shipments.size());
        }

        @Test
        @DisplayName("Should return empty list when no shipments found")
        void findByRegisteredById_NoShipments_ReturnsEmptyList() {
            // Act
            List<Shipment> shipments = shipmentRepository.findByRegisteredById(999L);

            // Assert
            assertTrue(shipments.isEmpty());
        }
    }

    @Nested
    @DisplayName("findBySenderId Tests")
    class FindBySenderIdTests {

        @Test
        @DisplayName("Should find shipments by sender ID")
        void findBySenderId_ReturnsShipments() {
            // Act
            List<Shipment> shipments = shipmentRepository.findBySenderId(sender.getId());

            // Assert
            assertEquals(2, shipments.size());
        }
    }

    @Nested
    @DisplayName("findByRecipientId Tests")
    class FindByRecipientIdTests {

        @Test
        @DisplayName("Should find shipments by recipient ID")
        void findByRecipientId_ReturnsShipments() {
            // Act
            List<Shipment> shipments = shipmentRepository.findByRecipientId(recipient.getId());

            // Assert
            assertEquals(2, shipments.size());
        }
    }

    @Nested
    @DisplayName("findBySenderIdOrRecipientId Tests")
    class FindBySenderIdOrRecipientIdTests {

        @Test
        @DisplayName("Should find shipments where customer is sender or recipient")
        void findBySenderIdOrRecipientId_ReturnsShipments() {
            // Act
            List<Shipment> shipments = shipmentRepository.findBySenderIdOrRecipientId(
                    sender.getId(), sender.getId());

            // Assert
            assertEquals(2, shipments.size());
        }
    }

    @Nested
    @DisplayName("findByStatus Tests")
    class FindByStatusTests {

        @Test
        @DisplayName("Should find shipments by status")
        void findByStatus_ReturnsShipments() {
            // Act
            List<Shipment> registeredShipments = shipmentRepository.findByStatus(ShipmentStatus.REGISTERED);
            List<Shipment> deliveredShipments = shipmentRepository.findByStatus(ShipmentStatus.DELIVERED);

            // Assert
            assertEquals(1, registeredShipments.size());
            assertEquals(1, deliveredShipments.size());
        }
    }

    @Nested
    @DisplayName("findAllPendingShipments Tests")
    class FindAllPendingShipmentsTests {

        @Test
        @DisplayName("Should find all pending (non-delivered) shipments")
        void findAllPendingShipments_ReturnsPendingShipments() {
            // Act
            List<Shipment> pendingShipments = shipmentRepository.findAllPendingShipments();

            // Assert
            assertEquals(1, pendingShipments.size());
            assertEquals(ShipmentStatus.REGISTERED, pendingShipments.get(0).getStatus());
        }
    }

    @Nested
    @DisplayName("countByStatus Tests")
    class CountByStatusTests {

        @Test
        @DisplayName("Should count shipments by status")
        void countByStatus_ReturnsCorrectCount() {
            // Act
            long registeredCount = shipmentRepository.countByStatus(ShipmentStatus.REGISTERED);
            long deliveredCount = shipmentRepository.countByStatus(ShipmentStatus.DELIVERED);
            long cancelledCount = shipmentRepository.countByStatus(ShipmentStatus.CANCELLED);

            // Assert
            assertEquals(1, registeredCount);
            assertEquals(1, deliveredCount);
            assertEquals(0, cancelledCount);
        }
    }

    @Nested
    @DisplayName("calculateTotalRevenue Tests")
    class CalculateTotalRevenueTests {

        @Test
        @DisplayName("Should calculate total revenue")
        void calculateTotalRevenue_ReturnsTotal() {
            // Act
            BigDecimal totalRevenue = shipmentRepository.calculateTotalRevenue();

            // Assert
            // 25.00 + 45.00 = 70.00
            assertEquals(0, new BigDecimal("70.00").compareTo(totalRevenue));
        }
    }

    @Nested
    @DisplayName("countInTransitShipments Tests")
    class CountInTransitShipmentsTests {

        @Test
        @DisplayName("Should count in-transit shipments")
        void countInTransitShipments_ReturnsCount() {
            // Act
            long count = shipmentRepository.countInTransitShipments();

            // Assert
            assertEquals(1, count); // Only REGISTERED shipment
        }
    }

    @Nested
    @DisplayName("calculateRevenueBetweenDates Tests")
    class CalculateRevenueBetweenDatesTests {

        @Test
        @DisplayName("Should calculate revenue between dates")
        void calculateRevenueBetweenDates_ReturnsRevenue() {
            // Act
            LocalDateTime startDate = LocalDateTime.now().minusDays(7);
            LocalDateTime endDate = LocalDateTime.now();
            BigDecimal revenue = shipmentRepository.calculateRevenueBetweenDates(startDate, endDate);

            // Assert
            assertEquals(0, new BigDecimal("45.00").compareTo(revenue)); // Only delivered shipment
        }

        @Test
        @DisplayName("Should return null when no delivered shipments in range")
        void calculateRevenueBetweenDates_NoShipments_ReturnsNull() {
            // Act
            LocalDateTime startDate = LocalDateTime.now().minusDays(100);
            LocalDateTime endDate = LocalDateTime.now().minusDays(90);
            BigDecimal revenue = shipmentRepository.calculateRevenueBetweenDates(startDate, endDate);

            // Assert
            assertNull(revenue);
        }
    }

    @Nested
    @DisplayName("countBySenderId Tests")
    class CountBySenderIdTests {

        @Test
        @DisplayName("Should count shipments by sender ID")
        void countBySenderId_ReturnsCount() {
            // Act
            long count = shipmentRepository.countBySenderId(sender.getId());

            // Assert
            assertEquals(2, count);
        }
    }

    @Nested
    @DisplayName("calculateTotalSpentBySenderId Tests")
    class CalculateTotalSpentBySenderIdTests {

        @Test
        @DisplayName("Should calculate total spent by sender")
        void calculateTotalSpentBySenderId_ReturnsTotal() {
            // Act
            BigDecimal totalSpent = shipmentRepository.calculateTotalSpentBySenderId(sender.getId());

            // Assert
            assertEquals(0, new BigDecimal("70.00").compareTo(totalSpent));
        }
    }
}
