package com.logistics.service;

import com.logistics.dto.company.CompanyRequest;
import com.logistics.dto.company.CompanyResponse;

import java.util.List;

/**
 * Service interface for Company operations.
 */
public interface CompanyService {

    /**
     * Creates a new company.
     *
     * @param request the company data
     * @return the created company response
     */
    CompanyResponse createCompany(CompanyRequest request);

    /**
     * Retrieves a company by ID.
     *
     * @param id the company ID
     * @return the company response
     * @throws com.logistics.exception.ResourceNotFoundException if not found
     */
    CompanyResponse getCompanyById(Long id);

    /**
     * Retrieves all companies.
     *
     * @return list of all companies
     */
    List<CompanyResponse> getAllCompanies();

    /**
     * Updates an existing company.
     *
     * @param id      the company ID
     * @param request the updated company data
     * @return the updated company response
     */
    CompanyResponse updateCompany(Long id, CompanyRequest request);

    /**
     * Deletes a company by ID.
     *
     * @param id the company ID
     */
    void deleteCompany(Long id);
}
