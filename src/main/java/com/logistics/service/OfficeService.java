package com.logistics.service;

import com.logistics.dto.office.OfficeRequest;
import com.logistics.dto.office.OfficeResponse;

import java.util.List;

/**
 * Service interface for Office operations.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Only handles office-related business logic.
 * - Interface Segregation (ISP): Contains only methods relevant to offices.
 * - Dependency Inversion (DIP): Controllers depend on this interface.
 */
public interface OfficeService {

    /**
     * Creates a new office.
     *
     * @param request the office data
     * @return the created office response
     */
    OfficeResponse createOffice(OfficeRequest request);

    /**
     * Retrieves an office by ID.
     *
     * @param id the office ID
     * @return the office response
     */
    OfficeResponse getOfficeById(Long id);

    /**
     * Retrieves all offices.
     *
     * @return list of all offices
     */
    List<OfficeResponse> getAllOffices();

    /**
     * Retrieves all offices for a specific company.
     *
     * @param companyId the company ID
     * @return list of offices for the company
     */
    List<OfficeResponse> getOfficesByCompanyId(Long companyId);

    /**
     * Updates an existing office.
     *
     * @param id      the office ID
     * @param request the updated office data
     * @return the updated office response
     */
    OfficeResponse updateOffice(Long id, OfficeRequest request);

    /**
     * Deletes an office by ID.
     *
     * @param id the office ID
     */
    void deleteOffice(Long id);
}
