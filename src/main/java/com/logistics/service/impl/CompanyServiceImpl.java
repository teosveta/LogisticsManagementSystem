package com.logistics.service.impl;

import com.logistics.dto.company.CompanyRequest;
import com.logistics.dto.company.CompanyResponse;
import com.logistics.exception.DuplicateResourceException;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.model.entity.Company;
import com.logistics.repository.CompanyRepository;
import com.logistics.service.CompanyService;
import com.logistics.util.EntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyServiceImpl.class);

    private final CompanyRepository companyRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public CompanyResponse createCompany(CompanyRequest request) {
        logger.info("Creating company with registration number: {}", request.getRegistrationNumber());

        if (companyRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Company", "registrationNumber",
                    request.getRegistrationNumber());
        }

        Company company = new Company();
        company.setName(request.getName());
        company.setRegistrationNumber(request.getRegistrationNumber());
        company.setAddress(request.getAddress());
        company.setPhone(request.getPhone());
        company.setEmail(request.getEmail());

        Company savedCompany = companyRepository.save(company);
        logger.info("Company created with ID: {}", savedCompany.getId());

        return EntityMapper.toCompanyResponse(savedCompany);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long id) {
        logger.debug("Fetching company with ID: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));

        return EntityMapper.toCompanyResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies() {
        logger.debug("Fetching all companies");

        return companyRepository.findAll().stream()
                .map(EntityMapper::toCompanyResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        logger.info("Updating company with ID: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));

        if (!company.getRegistrationNumber().equals(request.getRegistrationNumber()) &&
                companyRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Company", "registrationNumber",
                    request.getRegistrationNumber());
        }

        company.setName(request.getName());
        company.setRegistrationNumber(request.getRegistrationNumber());
        company.setAddress(request.getAddress());
        company.setPhone(request.getPhone());
        company.setEmail(request.getEmail());

        Company updatedCompany = companyRepository.save(company);
        logger.info("Company updated with ID: {}", updatedCompany.getId());

        return EntityMapper.toCompanyResponse(updatedCompany);
    }

    @Override
    public void deleteCompany(Long id) {
        logger.info("Deleting company with ID: {}", id);

        if (!companyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Company", "id", id);
        }

        companyRepository.deleteById(id);
        logger.info("Company deleted with ID: {}", id);
    }
}
