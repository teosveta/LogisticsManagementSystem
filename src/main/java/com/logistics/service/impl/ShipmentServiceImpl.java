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
 * Business rules:
 * - Shipment must have exactly one delivery destination (address or office, not both)
 * - Price is calculated automatically based on weight and delivery type
 * - Status flow: REGISTERED -> IN_TRANSIT -> DELIVERED (can be CANCELLED at any non-terminal state)
 */
@Service
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentServiceImpl.class);

    private final ShipmentRepository shipmentRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final OfficeRepository officeRepository;
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

        validateDeliveryDestination(request);
        validateWeight(request.getWeight());

        Employee employee = employeeRepository.findByUsername(employeeUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "username", employeeUsername));

        Customer sender = customerRepository.findById(request.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer (sender)", "id", request.getSenderId()));

        Customer recipient = customerRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer (recipient)", "id", request.getRecipientId()));

        Shipment shipment = new Shipment();
        shipment.setSender(sender);
        shipment.setRecipient(recipient);
        shipment.setRegisteredBy(employee);
        shipment.setWeight(request.getWeight());
        shipment.setStatus(ShipmentStatus.REGISTERED);

        if (request.getOriginOfficeId() != null) {
            Office originOffice = officeRepository.findById(request.getOriginOfficeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Office (origin)", "id", request.getOriginOfficeId()));
            shipment.setOriginOffice(originOffice);
        } else if (employee.getOffice() != null) {
            shipment.setOriginOffice(employee.getOffice());
        }

        boolean isOfficeDelivery = false;
        if (request.isOfficeDelivery()) {
            Office deliveryOffice = officeRepository.findById(request.getDeliveryOfficeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Office", "id", request.getDeliveryOfficeId()));
            shipment.setDeliveryOffice(deliveryOffice);
            isOfficeDelivery = true;
        } else {
            shipment.setDeliveryAddress(request.getDeliveryAddress());
        }

        BigDecimal price = pricingService.calculatePrice(request.getWeight(), isOfficeDelivery);
        shipment.setPrice(price);

        logger.debug("Calculated price: {} for weight: {}, isOfficeDelivery: {}",
                price, request.getWeight(), isOfficeDelivery);

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

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        return shipmentRepository.findBySenderIdOrRecipientId(customerId, customerId).stream()
                .map(EntityMapper::toShipmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ShipmentResponse updateShipmentStatus(Long id, ShipmentStatusUpdateRequest request) {
        logger.info("Updating status of shipment ID: {} to: {}", id, request.getStatus());

        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", id));

        validateStatusTransition(shipment.getStatus(), request.getStatus());
        shipment.setStatus(request.getStatus());

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

        if (shipment.isCancelled() || shipment.isDelivered()) {
            throw new InvalidDataException("Cannot update a " + shipment.getStatus() + " shipment");
        }

        validateDeliveryDestination(request);
        validateWeight(request.getWeight());

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
     * Enforces the shipment status state machine:
     * REGISTERED -> IN_TRANSIT or CANCELLED
     * IN_TRANSIT -> DELIVERED or CANCELLED
     * DELIVERED and CANCELLED are terminal states
     */
    private void validateStatusTransition(ShipmentStatus currentStatus, ShipmentStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        if (currentStatus == ShipmentStatus.DELIVERED || currentStatus == ShipmentStatus.CANCELLED) {
            throw new InvalidDataException(
                    String.format("Cannot change status from %s to %s", currentStatus, newStatus));
        }

        if (currentStatus == ShipmentStatus.REGISTERED) {
            if (newStatus != ShipmentStatus.IN_TRANSIT && newStatus != ShipmentStatus.CANCELLED) {
                throw new InvalidDataException(
                        String.format("Invalid status transition from %s to %s. " +
                                "From REGISTERED, can only transition to IN_TRANSIT or CANCELLED",
                                currentStatus, newStatus));
            }
        }

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
