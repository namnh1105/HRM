package com.hainam.worksphere.insurance.service;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.insurance.domain.Insurance;
import com.hainam.worksphere.insurance.domain.InsuranceRegistration;
import com.hainam.worksphere.insurance.dto.request.CreateInsuranceRegistrationRequest;
import com.hainam.worksphere.insurance.dto.request.CreateInsuranceRequest;
import com.hainam.worksphere.insurance.dto.response.InsuranceRegistrationResponse;
import com.hainam.worksphere.insurance.dto.response.InsuranceResponse;
import com.hainam.worksphere.insurance.mapper.InsuranceMapper;
import com.hainam.worksphere.insurance.repository.InsuranceRegistrationRepository;
import com.hainam.worksphere.insurance.repository.InsuranceRepository;
import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import com.hainam.worksphere.shared.exception.InsuranceNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsuranceService {

    private final InsuranceRepository insuranceRepository;
    private final InsuranceRegistrationRepository registrationRepository;
    private final EmployeeRepository employeeRepository;
    private final InsuranceMapper insuranceMapper;

    // ==================== Insurance CRUD ====================

    @Transactional
    @CacheEvict(value = CacheConfig.INSURANCE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "INSURANCE")
    public InsuranceResponse createInsurance(CreateInsuranceRequest request, UUID createdBy) {
        if (insuranceRepository.existsActiveByCode(request.getCode())) {
            throw new ValidationException("Insurance code already exists: " + request.getCode());
        }

        Insurance insurance = Insurance.builder()
                .code(request.getCode())
                .name(request.getName())
                .insuranceType(request.getInsuranceType())
                .provider(request.getProvider())
                .employeeRate(request.getEmployeeRate())
                .employerRate(request.getEmployerRate())
                .description(request.getDescription())
                .createdBy(createdBy)
                .build();

        Insurance saved = insuranceRepository.save(insurance);
        AuditContext.registerCreated(saved);
        return insuranceMapper.toInsuranceResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.INSURANCE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "INSURANCE")
    public InsuranceResponse updateInsurance(UUID id, CreateInsuranceRequest request, UUID updatedBy) {
        Insurance insurance = insuranceRepository.findActiveById(id)
                .orElseThrow(() -> InsuranceNotFoundException.byId(id.toString()));

        AuditContext.snapshot(insurance);

        insurance.setName(request.getName());
        insurance.setInsuranceType(request.getInsuranceType());
        insurance.setProvider(request.getProvider());
        insurance.setEmployeeRate(request.getEmployeeRate());
        insurance.setEmployerRate(request.getEmployerRate());
        insurance.setDescription(request.getDescription());
        insurance.setUpdatedBy(updatedBy);

        Insurance saved = insuranceRepository.save(insurance);
        AuditContext.registerUpdated(saved);
        return insuranceMapper.toInsuranceResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.INSURANCE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.DELETE, entity = "INSURANCE")
    public void deleteInsurance(UUID id, UUID deletedBy) {
        Insurance insurance = insuranceRepository.findActiveById(id)
                .orElseThrow(() -> InsuranceNotFoundException.byId(id.toString()));

        AuditContext.registerDeleted(insurance);

        insurance.setIsDeleted(true);
        insurance.setDeletedAt(Instant.now());
        insurance.setDeletedBy(deletedBy);
        insuranceRepository.save(insurance);
    }

    @Cacheable(value = CacheConfig.INSURANCE_CACHE, key = "'all'")
    public List<InsuranceResponse> getAllInsurances() {
        return insuranceRepository.findAllActive()
                .stream()
                .map(insuranceMapper::toInsuranceResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.INSURANCE_CACHE, key = "#id.toString()")
    public InsuranceResponse getInsuranceById(UUID id) {
        Insurance insurance = insuranceRepository.findActiveById(id)
                .orElseThrow(() -> InsuranceNotFoundException.byId(id.toString()));
        return insuranceMapper.toInsuranceResponse(insurance);
    }

    // ==================== InsuranceRegistration CRUD ====================

    @Transactional
    @CacheEvict(value = CacheConfig.INSURANCE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "INSURANCE_REGISTRATION")
    public InsuranceRegistrationResponse createRegistration(CreateInsuranceRegistrationRequest request, UUID createdBy) {
        Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                .orElseThrow(() -> EmployeeNotFoundException.byId(request.getEmployeeId().toString()));

        Insurance insurance = insuranceRepository.findActiveById(request.getInsuranceId())
                .orElseThrow(() -> InsuranceNotFoundException.byId(request.getInsuranceId().toString()));

        if (request.getEndDate() != null && request.getStartDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new ValidationException("End date must not be before start date");
        }

        InsuranceRegistration registration = InsuranceRegistration.builder()
                .employee(employee)
                .insurance(insurance)
                .registrationNumber(request.getRegistrationNumber())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .note(request.getNote())
                .createdBy(createdBy)
                .build();

        InsuranceRegistration saved = registrationRepository.save(registration);
        AuditContext.registerCreated(saved);
        return insuranceMapper.toInsuranceRegistrationResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.INSURANCE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "INSURANCE_REGISTRATION")
    public InsuranceRegistrationResponse updateRegistration(UUID id, CreateInsuranceRegistrationRequest request, UUID updatedBy) {
        InsuranceRegistration registration = registrationRepository.findActiveById(id)
                .orElseThrow(() -> InsuranceNotFoundException.byId(id.toString()));

        AuditContext.snapshot(registration);

        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                    .orElseThrow(() -> EmployeeNotFoundException.byId(request.getEmployeeId().toString()));
            registration.setEmployee(employee);
        }
        if (request.getInsuranceId() != null) {
            Insurance insurance = insuranceRepository.findActiveById(request.getInsuranceId())
                    .orElseThrow(() -> InsuranceNotFoundException.byId(request.getInsuranceId().toString()));
            registration.setInsurance(insurance);
        }
        if (request.getRegistrationNumber() != null) {
            registration.setRegistrationNumber(request.getRegistrationNumber());
        }
        if (request.getStartDate() != null) {
            registration.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            registration.setEndDate(request.getEndDate());
        }
        if (request.getNote() != null) {
            registration.setNote(request.getNote());
        }
        registration.setUpdatedBy(updatedBy);

        InsuranceRegistration saved = registrationRepository.save(registration);
        AuditContext.registerUpdated(saved);
        return insuranceMapper.toInsuranceRegistrationResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.INSURANCE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.DELETE, entity = "INSURANCE_REGISTRATION")
    public void deleteRegistration(UUID id, UUID deletedBy) {
        InsuranceRegistration registration = registrationRepository.findActiveById(id)
                .orElseThrow(() -> InsuranceNotFoundException.byId(id.toString()));

        AuditContext.registerDeleted(registration);

        registration.setIsDeleted(true);
        registration.setDeletedAt(Instant.now());
        registration.setDeletedBy(deletedBy);
        registrationRepository.save(registration);
    }

    @Cacheable(value = CacheConfig.INSURANCE_CACHE, key = "'registrations:all'")
    public List<InsuranceRegistrationResponse> getAllRegistrations() {
        return registrationRepository.findAllActive()
                .stream()
                .map(insuranceMapper::toInsuranceRegistrationResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.INSURANCE_CACHE, key = "'registration:' + #id.toString()")
    public InsuranceRegistrationResponse getRegistrationById(UUID id) {
        InsuranceRegistration registration = registrationRepository.findActiveById(id)
                .orElseThrow(() -> InsuranceNotFoundException.byId(id.toString()));
        return insuranceMapper.toInsuranceRegistrationResponse(registration);
    }

    @Cacheable(value = CacheConfig.INSURANCE_CACHE, key = "'registrations:employee:' + #employeeId.toString()")
    public List<InsuranceRegistrationResponse> getRegistrationsByEmployeeId(UUID employeeId) {
        return registrationRepository.findActiveByEmployeeId(employeeId)
                .stream()
                .map(insuranceMapper::toInsuranceRegistrationResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.INSURANCE_CACHE, key = "'registrations:insurance:' + #insuranceId.toString()")
    public List<InsuranceRegistrationResponse> getRegistrationsByInsuranceId(UUID insuranceId) {
        return registrationRepository.findActiveByInsuranceId(insuranceId)
                .stream()
                .map(insuranceMapper::toInsuranceRegistrationResponse)
                .collect(Collectors.toList());
    }
}
