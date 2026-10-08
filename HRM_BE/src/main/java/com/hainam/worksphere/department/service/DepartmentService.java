package com.hainam.worksphere.department.service;

import com.hainam.worksphere.department.domain.Department;
import com.hainam.worksphere.department.dto.request.CreateDepartmentRequest;
import com.hainam.worksphere.department.dto.request.UpdateDepartmentRequest;
import com.hainam.worksphere.department.dto.response.DepartmentResponse;
import com.hainam.worksphere.department.mapper.DepartmentMapper;
import com.hainam.worksphere.department.repository.DepartmentRepository;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.exception.DepartmentNotFoundException;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import com.hainam.worksphere.shared.exception.StoreNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.store.domain.Store;
import com.hainam.worksphere.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final DepartmentMapper departmentMapper;

    @Cacheable(value = CacheConfig.DEPARTMENT_CACHE, key = "#departmentId.toString()")
    public DepartmentResponse getDepartmentById(UUID departmentId) {
        Department department = departmentRepository.findActiveById(departmentId)
                .orElseThrow(() -> DepartmentNotFoundException.byId(departmentId.toString()));
        return departmentMapper.toDepartmentResponse(department);
    }

    public List<DepartmentResponse> getAllActiveDepartments() {
        return departmentRepository.findAllActive()
                .stream()
                .map(departmentMapper::toDepartmentResponse)
                .collect(Collectors.toList());
    }

    public List<DepartmentResponse> getSubDepartments(UUID parentId) {
        return departmentRepository.findActiveByParentDepartmentId(parentId)
                .stream()
                .map(departmentMapper::toDepartmentResponse)
                .collect(Collectors.toList());
    }

    public List<DepartmentResponse> getDepartmentsByStore(UUID storeId) {
        return departmentRepository.findActiveByStoreId(storeId)
                .stream()
                .map(departmentMapper::toDepartmentResponse)
                .collect(Collectors.toList());
    }

    public List<DepartmentResponse> searchDepartments(String keyword) {
        return departmentRepository.searchActive(keyword)
                .stream()
                .map(departmentMapper::toDepartmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = CacheConfig.DEPARTMENT_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "DEPARTMENT")
    public DepartmentResponse createDepartment(CreateDepartmentRequest request, UUID createdBy) {
        if (departmentRepository.existsActiveByCode(request.getCode())) {
            throw ValidationException.duplicateField("code", request.getCode());
        }

        Department department = departmentMapper.toEntity(request);
        department.setCreatedBy(createdBy);

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findActiveById(request.getManagerId())
                    .orElseThrow(() -> EmployeeNotFoundException.byId(request.getManagerId().toString()));
            department.setManager(manager);
        }

        if (request.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findActiveById(request.getParentDepartmentId())
                    .orElseThrow(() -> DepartmentNotFoundException.byId(request.getParentDepartmentId().toString()));
            department.setParentDepartment(parent);
        }

        if (request.getStoreId() != null) {
            Store store = storeRepository.findActiveById(request.getStoreId())
                    .orElseThrow(() -> StoreNotFoundException.byId(request.getStoreId().toString()));
            department.setStore(store);
        }

        Department saved = departmentRepository.save(department);
        AuditContext.registerCreated(saved);

        return departmentMapper.toDepartmentResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.DEPARTMENT_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "DEPARTMENT")
    public DepartmentResponse updateDepartment(UUID departmentId, UpdateDepartmentRequest request, UUID updatedBy) {
        Department department = departmentRepository.findActiveById(departmentId)
                .orElseThrow(() -> DepartmentNotFoundException.byId(departmentId.toString()));

        AuditContext.snapshot(department);

        if (request.getName() != null) {
            department.setName(request.getName());
        }
        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }
        if (request.getPhone() != null) {
            department.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            department.setEmail(request.getEmail());
        }
        if (request.getIsActive() != null) {
            department.setIsActive(request.getIsActive());
        }
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findActiveById(request.getManagerId())
                    .orElseThrow(() -> EmployeeNotFoundException.byId(request.getManagerId().toString()));
            department.setManager(manager);
        }
        if (request.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findActiveById(request.getParentDepartmentId())
                    .orElseThrow(() -> DepartmentNotFoundException.byId(request.getParentDepartmentId().toString()));
            department.setParentDepartment(parent);
        }

        if (request.getStoreId() != null) {
            Store store = storeRepository.findActiveById(request.getStoreId())
                    .orElseThrow(() -> StoreNotFoundException.byId(request.getStoreId().toString()));
            department.setStore(store);
        }

        department.setUpdatedBy(updatedBy);
        Department saved = departmentRepository.save(department);
        AuditContext.registerUpdated(saved);

        return departmentMapper.toDepartmentResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.DEPARTMENT_CACHE, allEntries = true)
    @AuditAction(type = ActionType.DELETE, entity = "DEPARTMENT")
    public void softDeleteDepartment(UUID departmentId, UUID deletedBy) {
        Department department = departmentRepository.findActiveById(departmentId)
                .orElseThrow(() -> DepartmentNotFoundException.byId(departmentId.toString()));

        AuditContext.registerDeleted(department);

        department.setIsDeleted(true);
        department.setDeletedAt(Instant.now());
        department.setDeletedBy(deletedBy);
        departmentRepository.save(department);
    }
}
