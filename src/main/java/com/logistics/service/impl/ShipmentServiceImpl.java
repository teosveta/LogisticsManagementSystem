package com.logistics.service.impl;

import com.logistics.dto.shipment.ShipmentRequest;
import com.logistics.dto.shipment.ShipmentResponse;
import com.logistics.dto.shipment.ShipmentStatusUpdateRequest;
import com.logistics.exception.InvalidDataException;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.model.entity.Customer;
import com.logistics.model.entity.Employee;
import com.logistics.model.entity.Office;
import com.logistics.model.entity.Shipment;
import com.logistics.model.enums.ShipmentStatus;
import com.logistics.repository.CustomerRepository;
import com.logistics.repository.EmployeeRepository;
import com.logistics.repository.OfficeRepository;
import com.logistics.repository.ShipmentRepository;
import com.logistics.service.PricingService;
import com.logistics.service.ShipmentService;
import com.logistics.util.EntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ShipmentService.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This service handles shipment operations ONLY.
 *   Price calculation is DELEGATED to PricingService - not done here.
 *   This keeps this class focused on shipment lifecycle management.
 * - Open/Closed (OCP): New shipment types or validation rules can be added
 *   without modifying the core registration logic.
 * - Liskov Substitution (LSP): This implementation can replace any ShipmentService.
 * - Dependency Inversion (DIP): Depends on PricingService INTERFACE, not implementation.
 *   This means we can swap pricing strategies without changing this class.
 *
 * Key Business Rules:
 * 1. Either deliveryAddress OR deliveryOfficeId must be set (not both, not neither)
 * 2. Price is calculated automatically using PricingService
 * 3. Status transitions: REGISTERED -> IN_TRANSIT -> DELIVERED (or CANCELLED)
 * 4. Only DELIVERED shipments count as revenue
 */
@Service
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentServiceImpl.class);

    private final ShipmentRepository shipmentRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final OfficeRepository officeRepository;

    /**
     * PricingService injected via constructor.
     * This is Dependency Inversion (DIP) in action:
     * - We depend on the PricingService INTERFACE
     * - We don't know or care about the implementation
     * - This allows swapping pricing strategies easily
     */
    private final PricingService pricingService;

    public ShipmentServiceImpl(ShipmentRepository shipmentRepository,
                               CustomerRepository customerRepository,
                               EmployeeRepository employeeRepository,
                               OfficeRepository officeRepository,
                               PricingService pricingService) {
        this.shipmentRepository = shipmentRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.officeRepository = officeRepository;
        this.pricingService = pricingService;
    }

    @Override
    public ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername) {
        logger.info("Registering shipment by employee: {}", employeeUsername);

        // Validate delivery destination - must have exactly one
        validateDeliveryDestination(request);

        // Validate weight is positive and within limits
        validateWeight(request.getWeight());

        // Get the employee who is registering the shipment
        Employee employee = employeeRepository.findByUsername(employeeUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "username", employeeUsername));

        // Get sender and recipient customers
        Customer sender = customerRepository.findById(request.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer (sender)", "id", request.getSenderId()));

        Customer recipient = customerRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer (recipient)", "id", request.getRecipientId()));

        // Create shipment entity
        Shipment shipment = new Shipment();
        shipment.setSender(sender);
        shipment.setRecipient(recipient);
        shipment.setRegisteredBy(employee);
        shipment.setWeight(request.getWeight());
        shipment.setStatus(ShipmentStatus.REGISTERED);

        // Set delivery destination (either office or address)
        boolean isOfficeDelivery = false;
        if (request.isOfficeDelivery()) {
            Office deliveryOffice = officeRepository.findById(request.getDeliveryOfficeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Office", "id", request.getDeliveryOfficeId()));
            shipment.setDeliveryOffice(deliveryOffice);
            isOfficeDelivery = true;
        } else {
            shipment.setDeliveryAddress(request.getDeliveryAddress());
        }

        // Calculate price using PricingService (Dependency Inversion in action!)
        // The price formula is: Base + (Weight × PricePerKg) + DeliveryFee
        BigDecimal price = pricingService.calculatePrice(request.getWeight(), isOfficeDelivery);
        shipment.setPrice(price);

        logger.debug("Calculated price: {} for weight: {}, isOfficeDelivery: {}",
                price, request.getWeight(), isOfficeDelivery);

        // Save and return
        Shipment savedShipment = shipmentRepository.save(shipment);
        logger.info("Shipment registered with ID: {}, price: {}", savedShipment.getId(), price);

        return EntityMapper.toShipmentResponse(savedShipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(Long id) {
        logger.debug("Fetching shipment with ID: {}", id);

        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", id));

        return EntityMapper.toShipmentResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAllShipments() {
        logger.debug("Fetching all shipments");

        return shipmentRepository.findAll().stream()
                .map(EntityMapper::toShipmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipmentsByCustomerId(Long customerId) {
        logger.debug("Fetching shipments for customer ID: {}", customerId);

        // Validate customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        // Return shipments where customer is sender OR recipient
        return shipmentRepository.findBySenderIdOrRecipientId(customerId, customerId).stream()
                .map(EntityMapper::toShipmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ShipmentResponse updateShipmentStatus(Long id, ShipmentStatusUpdateRequest request) {
        logger.info("Updating status of shipment ID: {} to: {}", id, request.getStatus());

        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", id));

        // Validate status transition
        validateStatusTransition(shipment.getStatus(), request.getStatus());

        // Update status
        shipment.setStatus(request.getStatus());

        // If status is DELIVERED, set delivery timestamp
        if (ShipmentStatus.DELIVERED.equals(request.getStatus())) {
            shipment.setDeliveredAt(LocalDateTime.now());
            logger.info("Shipment {} marked as DELIVERED at {}", id, shipment.getDeliveredAt());
        }

        Shipment updatedShipment = shipmentRepository.save(shipment);
        return EntityMapper.toShipmentResponse(updatedShipment);
    }

    @Override
    public ShipmentResponse updateShipment(Long id, ShipmentRequest request, String employeeUsername) {
        logger.info("Updating shipment ID: {}", id);

        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", id));

        // Cannot update cancelled or delivered shipments
        if (shipment.isCancelled() || shipment.isDelivered()) {
            throw new InvalidDataException("Cannot update a " + shipment.getStatus() + " shipment");
        }

        // Validate delivery destination
        validateDeliveryDestination(request);

        // Validate weight is positive and within limits
        validateWeight(request.getWeight());

        // Update sender and recipient if changed
        if (!shipment.getSender().getId().equals(request.getSenderId())) {
            Customer sender = customerRepository.findById(request.getSenderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer (sender)", "id", request.getSenderId()));
            shipment.setSender(sender);
        }

        if (!shipment.getRecipient().getId().equals(request.getRecipientId())) {
            Customer recipient = customerRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer (recipient)", "id", request.getRecipientId()));
            shipment.setRecipient(recipient);
        }

        // Update delivery destination
        boolean isOfficeDelivery = false;
        if (request.isOfficeDelivery()) {
            Office deliveryOffice = officeRepository.findById(request.getDeliveryOfficeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Office", "id", request.getDeliveryOfficeId()));
            shipment.setDeliveryOffice(deliveryOffice);
            shipment.setDeliveryAddress(null);
            isOfficeDelivery = true;
        } else {
            shipment.setDeliveryAddress(request.getDeliveryAddress());
            shipment.setDeliveryOffice(null);
        }

        // Update weight and recalculate price
        shipment.setWeight(request.getWeight());
        BigDecimal newPrice = pricingService.calculatePrice(request.getWeight(), isOfficeDelivery);
        shipment.setPrice(newPrice);

        Shipment updatedShipment = shipmentRepository.save(shipment);
        logger.info("Shipment updated with ID: {}", updatedShipment.getId());

        return EntityMapper.toShipmentResponse(updatedShipment);
    }

    @Override
    public void deleteShipment(Long id) {
        logger.info("Deleting shipment with ID: {}", id);

        if (!shipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shipment", "id", id);
        }

        shipmentRepository.deleteById(id);
        logger.info("Shipment deleted with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status) {
        logger.debug("Fetching shipments with status: {}", status);

        return shipmentRepository.findByStatus(status).stream()
                .map(EntityMapper::toShipmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validates that exactly one delivery destination is specified.
     * Either deliveryAddress OR deliveryOfficeId, but not both and not neither.
     *
     * @param request the shipment request to validate
     * @throws InvalidDataException if validation fails
     */
    private void validateDeliveryDestination(ShipmentRequest request) {
        boolean hasAddress = request.isAddressDelivery();
        boolean hasOffice = request.isOfficeDelivery();

        if (!hasAddress && !hasOffice) {
            throw new InvalidDataException("Either deliveryAddress or deliveryOfficeId must be provided");
        }

        if (hasAddress && hasOffice) {
            throw new InvalidDataException("Cannot specify both deliveryAddress and deliveryOfficeId");
        }
    }

    /**
     * Validates that the weight is positive and within acceptable limits.
     * Weight must be between 0.01 and 10000 kg.
     *
     * @param weight the weight to validate
     * @throws InvalidDataException if weight is invalid
     */
    private void validateWeight(BigDecimal weight) {
        if (weight == null) {
            throw new InvalidDataException("Weight is required");
        }

        if (weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDataException("Weight must be greater than 0");
        }

        BigDecimal maxWeight = new BigDecimal("10000.00");
        if (weight.compareTo(maxWeight) > 0) {
            throw new InvalidDataException("Weight cannot exceed 10000 kg");
        }
    }

    /**
     * Validates that the status transition is valid.
     * Valid transitions:
     * - REGISTERED -> IN_TRANSIT, CANCELLED
     * - IN_TRANSIT -> DELIVERED, CANCELLED
     * - DELIVERED, CANCELLED -> (no transitions allowed)
     *
     * @param currentStatus the current status
     * @param newStatus     the desired new status
     * @throws InvalidDataException if transition is not allowed
     */
    private void validateStatusTransition(ShipmentStatus currentStatus, ShipmentStatus newStatus) {
        // Same status is not a change
        if (currentStatus == newStatus) {
            return;
        }

        // Cannot change from terminal states
        if (currentStatus == ShipmentStatus.DELIVERED || currentStatus == ShipmentStatus.CANCELLED) {
            throw new InvalidDataException(
                    String.format("Cannot change status from %s to %s", currentStatus, newStatus));
        }

        // Valid transitions from REGISTERED
        if (currentStatus == ShipmentStatus.REGISTERED) {
            if (newStatus != ShipmentStatus.IN_TRANSIT && newStatus != ShipmentStatus.CANCELLED) {
                throw new InvalidDataException(
                        String.format("Invalid status transition from %s to %s. " +
                                "From REGISTERED, can only transition to IN_TRANSIT or CANCELLED",
                                currentStatus, newStatus));
            }
        }

        // Valid transitions from IN_TRANSIT
        if (currentStatus == ShipmentStatus.IN_TRANSIT) {
            if (newStatus != ShipmentStatus.DELIVERED && newStatus != ShipmentStatus.CANCELLED) {
                throw new InvalidDataException(
                        String.format("Invalid status transition from %s to %s. " +
                                "From IN_TRANSIT, can only transition to DELIVERED or CANCELLED",
                                currentStatus, newStatus));
            }
        }
    }
}
