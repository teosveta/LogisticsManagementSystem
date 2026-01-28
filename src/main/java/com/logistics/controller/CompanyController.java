package com.logistics.controller;

import com.logistics.dto.company.CompanyRequest;
import com.logistics.dto.company.CompanyResponse;
import com.logistics.service.CompanyService;
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
@RequestMapping("/api/companies")
@Tag(name = "Companies", description = "Company management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Create company", description = "Creates a new company (Employee only)")
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request) {
        logger.info("Creating company: {}", request.getName());
        CompanyResponse response = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get company by ID", description = "Retrieves a company by its ID (Employee only)")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        logger.debug("Fetching company with ID: {}", id);
        CompanyResponse response = companyService.getCompanyById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get all companies", description = "Retrieves all companies (Employee only)")
    public ResponseEntity<List<CompanyResponse>> getAllCompanies() {
        logger.debug("Fetching all companies");
        List<CompanyResponse> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(companies);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Update company", description = "Updates an existing company (Employee only)")
    public ResponseEntity<CompanyResponse> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request) {
        logger.info("Updating company with ID: {}", id);
        CompanyResponse response = companyService.updateCompany(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Delete company", description = "Deletes a company by ID (Employee only)")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        logger.info("Deleting company with ID: {}", id);
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
