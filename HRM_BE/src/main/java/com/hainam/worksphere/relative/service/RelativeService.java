package com.hainam.worksphere.relative.service;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.relative.domain.Relative;
import com.hainam.worksphere.relative.dto.request.CreateRelativeRequest;
import com.hainam.worksphere.relative.dto.response.RelativeResponse;
import com.hainam.worksphere.relative.mapper.RelativeMapper;
import com.hainam.worksphere.relative.repository.RelativeRepository;
import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import com.hainam.worksphere.shared.exception.RelativeNotFoundException;
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
public class RelativeService {

    private final RelativeRepository relativeRepository;
    private final EmployeeRepository employeeRepository;
    private final RelativeMapper relativeMapper;

    @Transactional
    @CacheEvict(value = CacheConfig.RELATIVE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "RELATIVE")
    public RelativeResponse createRelative(CreateRelativeRequest request, UUID createdBy) {
        Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                .orElseThrow(() -> EmployeeNotFoundException.byId(request.getEmployeeId().toString()));

        Relative relative = Relative.builder()
                .employee(employee)
                .fullName(request.getFullName())
                .relationship(request.getRelationship())
                .dateOfBirth(request.getDateOfBirth())
                .phone(request.getPhone())
                .idCardNumber(request.getIdCardNumber())
                .occupation(request.getOccupation())
                .address(request.getAddress())
                .isEmergencyContact(request.getIsEmergencyContact() != null ? request.getIsEmergencyContact() : false)
                .isDependent(request.getIsDependent() != null ? request.getIsDependent() : false)
                .createdBy(createdBy)
                .build();

        Relative saved = relativeRepository.save(relative);
        AuditContext.registerCreated(saved);

        return relativeMapper.toRelativeResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.RELATIVE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "RELATIVE")
    public RelativeResponse updateRelative(UUID id, CreateRelativeRequest request, UUID updatedBy) {
        Relative relative = relativeRepository.findActiveById(id)
                .orElseThrow(() -> RelativeNotFoundException.byId(id.toString()));

        AuditContext.snapshot(relative);

        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                    .orElseThrow(() -> EmployeeNotFoundException.byId(request.getEmployeeId().toString()));
            relative.setEmployee(employee);
        }
        if (request.getFullName() != null) {
            relative.setFullName(request.getFullName());
        }
        if (request.getRelationship() != null) {
            relative.setRelationship(request.getRelationship());
        }
        if (request.getDateOfBirth() != null) {
            relative.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getPhone() != null) {
            relative.setPhone(request.getPhone());
        }
        if (request.getIdCardNumber() != null) {
            relative.setIdCardNumber(request.getIdCardNumber());
        }
        if (request.getOccupation() != null) {
            relative.setOccupation(request.getOccupation());
        }
        if (request.getAddress() != null) {
            relative.setAddress(request.getAddress());
        }
        if (request.getIsEmergencyContact() != null) {
            relative.setIsEmergencyContact(request.getIsEmergencyContact());
        }
        if (request.getIsDependent() != null) {
            relative.setIsDependent(request.getIsDependent());
        }
        relative.setUpdatedBy(updatedBy);

        Relative saved = relativeRepository.save(relative);
        AuditContext.registerUpdated(saved);

        return relativeMapper.toRelativeResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.RELATIVE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.DELETE, entity = "RELATIVE")
    public void deleteRelative(UUID id, UUID deletedBy) {
        Relative relative = relativeRepository.findActiveById(id)
                .orElseThrow(() -> RelativeNotFoundException.byId(id.toString()));

        AuditContext.registerDeleted(relative);

        relative.setIsDeleted(true);
        relative.setDeletedAt(Instant.now());
        relative.setDeletedBy(deletedBy);
        relativeRepository.save(relative);
    }

    @Cacheable(value = CacheConfig.RELATIVE_CACHE, key = "'all'")
    public List<RelativeResponse> getAllRelatives() {
        return relativeRepository.findAllActive()
                .stream()
                .map(relativeMapper::toRelativeResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.RELATIVE_CACHE, key = "#id.toString()")
    public RelativeResponse getRelativeById(UUID id) {
        Relative relative = relativeRepository.findActiveById(id)
                .orElseThrow(() -> RelativeNotFoundException.byId(id.toString()));
        return relativeMapper.toRelativeResponse(relative);
    }

    @Cacheable(value = CacheConfig.RELATIVE_CACHE, key = "'employee:' + #employeeId.toString()")
    public List<RelativeResponse> getByEmployeeId(UUID employeeId) {
        return relativeRepository.findActiveByEmployeeId(employeeId)
                .stream()
                .map(relativeMapper::toRelativeResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.RELATIVE_CACHE, key = "'emergency:' + #employeeId.toString()")
    public List<RelativeResponse> getEmergencyContacts(UUID employeeId) {
        return relativeRepository.findActiveEmergencyContactsByEmployeeId(employeeId)
                .stream()
                .map(relativeMapper::toRelativeResponse)
                .collect(Collectors.toList());
    }
}
