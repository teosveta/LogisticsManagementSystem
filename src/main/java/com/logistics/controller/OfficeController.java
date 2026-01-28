package com.logistics.controller;

import com.logistics.dto.office.OfficeRequest;
import com.logistics.dto.office.OfficeResponse;
import com.logistics.service.OfficeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offices")
@Tag(name = "Offices", description = "Office management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OfficeController {

    private static final Logger logger = LoggerFactory.getLogger(OfficeController.class);

    private final OfficeService officeService;

    public OfficeController(OfficeService officeService) {
        this.officeService = officeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Create office", description = "Creates a new office (Employee only)")
    public ResponseEntity<OfficeResponse> createOffice(@Valid @RequestBody OfficeRequest request) {
        logger.info("Creating office: {}", request.getName());
        OfficeResponse response = officeService.createOffice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get office by ID", description = "Retrieves an office by its ID (Employee only)")
    public ResponseEntity<OfficeResponse> getOfficeById(@PathVariable Long id) {
        logger.debug("Fetching office with ID: {}", id);
        OfficeResponse response = officeService.getOfficeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get all offices", description = "Retrieves all offices (Employee only)")
    public ResponseEntity<List<OfficeResponse>> getAllOffices() {
        logger.debug("Fetching all offices");
        List<OfficeResponse> offices = officeService.getAllOffices();
        return ResponseEntity.ok(offices);
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get offices by company", description = "Retrieves all offices for a company (Employee only)")
    public ResponseEntity<List<OfficeResponse>> getOfficesByCompanyId(@PathVariable Long companyId) {
        logger.debug("Fetching offices for company ID: {}", companyId);
        List<OfficeResponse> offices = officeService.getOfficesByCompanyId(companyId);
        return ResponseEntity.ok(offices);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Update office", description = "Updates an existing office (Employee only)")
    public ResponseEntity<OfficeResponse> updateOffice(
            @PathVariable Long id,
            @Valid @RequestBody OfficeRequest request) {
        logger.info("Updating office with ID: {}", id);
        OfficeResponse response = officeService.updateOffice(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Delete office", description = "Deletes an office by ID (Employee only)")
    public ResponseEntity<Void> deleteOffice(@PathVariable Long id) {
        logger.info("Deleting office with ID: {}", id);
        officeService.deleteOffice(id);
        return ResponseEntity.noContent().build();
    }
}
