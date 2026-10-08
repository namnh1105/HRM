package com.hainam.worksphere.contract.service;

import com.hainam.worksphere.contract.domain.Contract;
import com.hainam.worksphere.contract.domain.ContractStatus;
import com.hainam.worksphere.contract.dto.request.CreateContractRequest;
import com.hainam.worksphere.contract.dto.request.UpdateContractRequest;
import com.hainam.worksphere.contract.dto.response.ContractResponse;
import com.hainam.worksphere.contract.mapper.ContractMapper;
import com.hainam.worksphere.contract.repository.ContractRepository;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.dto.ResourceStatsResponse;
import com.hainam.worksphere.shared.exception.ContractNotFoundException;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.shared.service.CloudinaryService;
import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractMapper contractMapper;
    private final CloudinaryService cloudinaryService;

    @Transactional
    @CacheEvict(value = CacheConfig.CONTRACT_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "CONTRACT")
    public ContractResponse createContract(CreateContractRequest request, MultipartFile file, UUID createdBy) {
        Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                .orElseThrow(() -> EmployeeNotFoundException.byId(request.getEmployeeId().toString()));

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new ValidationException("End date must not be before start date");
        }

        String attachmentUrl = cloudinaryService.upload(file, "contracts");

        Contract contract = Contract.builder()
                .employee(employee)
                .contractType(request.getContractType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .signingDate(request.getSigningDate())
                .baseSalary(request.getBaseSalary())
                .salaryCoefficient(request.getSalaryCoefficient() != null ? request.getSalaryCoefficient() : 1.0)
                .allowance(request.getAllowance() != null ? request.getAllowance() : 0.0)
                .note(request.getNote())
            .attachmentUrl(attachmentUrl)
                .createdBy(createdBy)
                .build();

        Contract saved = contractRepository.save(contract);
        AuditContext.registerCreated(saved);
        return contractMapper.toContractResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CONTRACT_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "CONTRACT")
    public ContractResponse updateContract(UUID id, UpdateContractRequest request, MultipartFile file, UUID updatedBy) {
        Contract contract = contractRepository.findActiveById(id)
                .orElseThrow(() -> ContractNotFoundException.byId(id.toString()));

        AuditContext.snapshot(contract);

        if (request.getEndDate() != null) {
            if (request.getEndDate().isBefore(contract.getStartDate())) {
                throw new ValidationException("End date must not be before start date");
            }
            contract.setEndDate(request.getEndDate());
        }
        if (request.getBaseSalary() != null) {
            contract.setBaseSalary(request.getBaseSalary());
        }
        if (request.getSalaryCoefficient() != null) {
            contract.setSalaryCoefficient(request.getSalaryCoefficient());
        }
        if (request.getAllowance() != null) {
            contract.setAllowance(request.getAllowance());
        }
        if (request.getStatus() != null) {
            contract.setStatus(request.getStatus());
        }
        if (request.getNote() != null) {
            contract.setNote(request.getNote());
        }
        if (request.getAttachmentUrl() != null) {
            contract.setAttachmentUrl(request.getAttachmentUrl());
        }
        if (file != null && !file.isEmpty()) {
            String attachmentUrl = cloudinaryService.upload(file, "contracts");
            contract.setAttachmentUrl(attachmentUrl);
        }
        contract.setUpdatedBy(updatedBy);

        Contract saved = contractRepository.save(contract);
        AuditContext.registerUpdated(saved);
        return contractMapper.toContractResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CONTRACT_CACHE, allEntries = true)
    @AuditAction(type = ActionType.DELETE, entity = "CONTRACT")
    public void deleteContract(UUID id, UUID deletedBy) {
        Contract contract = contractRepository.findActiveById(id)
                .orElseThrow(() -> ContractNotFoundException.byId(id.toString()));

        AuditContext.registerDeleted(contract);

        contract.setIsDeleted(true);
        contract.setDeletedAt(Instant.now());
        contract.setDeletedBy(deletedBy);
        contractRepository.save(contract);
    }

    @Cacheable(value = CacheConfig.CONTRACT_CACHE, key = "'all'")
    public Page<ContractResponse> getAllContracts(Pageable pageable) {
        return contractRepository.findAllActive(pageable)
                .map(contractMapper::toContractResponse);
    }

    @Cacheable(value = CacheConfig.CONTRACT_CACHE, key = "#id.toString()")
    public ContractResponse getContractById(UUID id) {
        Contract contract = contractRepository.findActiveById(id)
                .orElseThrow(() -> ContractNotFoundException.byId(id.toString()));
        return contractMapper.toContractResponse(contract);
    }

    @Cacheable(value = CacheConfig.CONTRACT_CACHE, key = "'employee:' + #employeeId + ':' + #pageable.pageNumber")
    public Page<ContractResponse> getByEmployeeId(UUID employeeId, Pageable pageable) {
        return contractRepository.findActiveByEmployeeId(employeeId, pageable)
                .map(contractMapper::toContractResponse);
    }

    @Cacheable(value = CacheConfig.CONTRACT_CACHE, key = "'active'")
    public Page<ContractResponse> getActiveContracts(Pageable pageable) {
        return contractRepository.findActiveByStatus(ContractStatus.ACTIVE, pageable)
                .map(contractMapper::toContractResponse);
    }

    public ResourceStatsResponse getContractStats() {
        return ResourceStatsResponse.builder()
                .total(contractRepository.count())
                .active(contractRepository.countByStatusAndIsDeletedFalse(ContractStatus.ACTIVE))
                .inactive(contractRepository.countByStatusAndIsDeletedFalse(ContractStatus.EXPIRED) +
                        contractRepository.countByStatusAndIsDeletedFalse(ContractStatus.TERMINATED))
                .deleted(contractRepository.countByIsDeletedTrue())
                .build();
    }
}
