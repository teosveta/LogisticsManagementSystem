package com.logistics.service.impl;

import com.logistics.dto.office.OfficeRequest;
import com.logistics.dto.office.OfficeResponse;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.model.entity.Company;
import com.logistics.model.entity.Office;
import com.logistics.repository.CompanyRepository;
import com.logistics.repository.OfficeRepository;
import com.logistics.service.OfficeService;
import com.logistics.util.EntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of OfficeService.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Only handles office business logic.
 * - Open/Closed (OCP): Can add new office types or rules via extension.
 * - Dependency Inversion (DIP): Depends on repository interfaces.
 */
@Service
@Transactional
public class OfficeServiceImpl implements OfficeService {

    private static final Logger logger = LoggerFactory.getLogger(OfficeServiceImpl.class);

    private final OfficeRepository officeRepository;
    private final CompanyRepository companyRepository;

    public OfficeServiceImpl(OfficeRepository officeRepository, CompanyRepository companyRepository) {
        this.officeRepository = officeRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public OfficeResponse createOffice(OfficeRequest request) {
        logger.info("Creating office: {} for company ID: {}", request.getName(), request.getCompanyId());

        // Validate company exists
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));

        // Create office entity
        Office office = new Office();
        office.setCompany(company);
        office.setName(request.getName());
        office.setAddress(request.getAddress());
        office.setCity(request.getCity());
        office.setCountry(request.getCountry());
        office.setPhone(request.getPhone());

        Office savedOffice = officeRepository.save(office);
        logger.info("Office created with ID: {}", savedOffice.getId());

        return EntityMapper.toOfficeResponse(savedOffice);
    }

    @Override
    @Transactional(readOnly = true)
    public OfficeResponse getOfficeById(Long id) {
        logger.debug("Fetching office with ID: {}", id);

        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", "id", id));

        return EntityMapper.toOfficeResponse(office);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficeResponse> getAllOffices() {
        logger.debug("Fetching all offices");

        return officeRepository.findAll().stream()
                .map(EntityMapper::toOfficeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficeResponse> getOfficesByCompanyId(Long companyId) {
        logger.debug("Fetching offices for company ID: {}", companyId);

        // Validate company exists
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company", "id", companyId);
        }

        return officeRepository.findByCompanyId(companyId).stream()
                .map(EntityMapper::toOfficeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OfficeResponse updateOffice(Long id, OfficeRequest request) {
        logger.info("Updating office with ID: {}", id);

        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", "id", id));

        // If company is being changed, validate new company exists
        if (!office.getCompany().getId().equals(request.getCompanyId())) {
            Company newCompany = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));
            office.setCompany(newCompany);
        }

        office.setName(request.getName());
        office.setAddress(request.getAddress());
        office.setCity(request.getCity());
        office.setCountry(request.getCountry());
        office.setPhone(request.getPhone());

        Office updatedOffice = officeRepository.save(office);
        logger.info("Office updated with ID: {}", updatedOffice.getId());

        return EntityMapper.toOfficeResponse(updatedOffice);
    }

    @Override
    public void deleteOffice(Long id) {
        logger.info("Deleting office with ID: {}", id);

        if (!officeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Office", "id", id);
        }

        officeRepository.deleteById(id);
        logger.info("Office deleted with ID: {}", id);
    }
}
