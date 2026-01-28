package com.logistics.service.impl;

import com.logistics.dto.employee.EmployeeRequest;
import com.logistics.dto.employee.EmployeeResponse;
import com.logistics.exception.DuplicateResourceException;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.model.entity.Company;
import com.logistics.model.entity.Employee;
import com.logistics.model.entity.Office;
import com.logistics.model.entity.User;
import com.logistics.repository.CompanyRepository;
import com.logistics.repository.EmployeeRepository;
import com.logistics.repository.OfficeRepository;
import com.logistics.repository.UserRepository;
import com.logistics.service.EmployeeService;
import com.logistics.util.EntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final OfficeRepository officeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               UserRepository userRepository,
                               CompanyRepository companyRepository,
                               OfficeRepository officeRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.officeRepository = officeRepository;
    }

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        logger.info("Creating employee for user ID: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        if (employeeRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateResourceException("Employee", "userId", request.getUserId());
        }

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setEmployeeType(request.getEmployeeType());
        employee.setHireDate(request.getHireDate());
        employee.setSalary(request.getSalary());

        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));
            employee.setCompany(company);
        }

        if (request.getOfficeId() != null) {
            Office office = officeRepository.findById(request.getOfficeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Office", "id", request.getOfficeId()));
            employee.setOffice(office);
        }

        Employee savedEmployee = employeeRepository.save(employee);
        logger.info("Employee created with ID: {}", savedEmployee.getId());

        return EntityMapper.toEmployeeResponse(savedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        logger.debug("Fetching employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        return EntityMapper.toEmployeeResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByUsername(String username) {
        logger.debug("Fetching employee with username: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "username", username));

        return EntityMapper.toEmployeeResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        logger.debug("Fetching all employees");

        return employeeRepository.findAll().stream()
                .map(EntityMapper::toEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        logger.info("Updating employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (request.getCompanyId() != null) {
            Long currentCompanyId = employee.getCompany() != null ? employee.getCompany().getId() : null;
            if (!request.getCompanyId().equals(currentCompanyId)) {
                Company company = companyRepository.findById(request.getCompanyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));
                employee.setCompany(company);
            }
        } else {
            employee.setCompany(null);
        }

        employee.setEmployeeType(request.getEmployeeType());
        employee.setHireDate(request.getHireDate());
        employee.setSalary(request.getSalary());

        if (request.getOfficeId() != null) {
            Office office = officeRepository.findById(request.getOfficeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Office", "id", request.getOfficeId()));
            employee.setOffice(office);
        } else {
            employee.setOffice(null);
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        logger.info("Employee updated with ID: {}", updatedEmployee.getId());

        return EntityMapper.toEmployeeResponse(updatedEmployee);
    }

    @Override
    public void deleteEmployee(Long id) {
        logger.info("Deleting employee with ID: {}", id);

        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", "id", id);
        }

        employeeRepository.deleteById(id);
        logger.info("Employee deleted with ID: {}", id);
    }
}
