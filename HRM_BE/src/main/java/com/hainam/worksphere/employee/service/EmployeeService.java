package com.hainam.worksphere.employee.service;

import com.hainam.worksphere.department.domain.Department;
import com.hainam.worksphere.department.repository.DepartmentRepository;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.domain.EmploymentStatus;
import com.hainam.worksphere.employee.domain.Gender;
import com.hainam.worksphere.employee.dto.request.CreateEmployeeRequest;
import com.hainam.worksphere.employee.dto.request.UpdateEmployeeRequest;
import com.hainam.worksphere.employee.dto.response.EmployeeResponse;
import com.hainam.worksphere.employee.mapper.EmployeeMapper;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.dto.ResourceStatsResponse;
import com.hainam.worksphere.shared.exception.DepartmentNotFoundException;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import com.hainam.worksphere.shared.exception.StoreNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.store.domain.Store;
import com.hainam.worksphere.store.repository.StoreRepository;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final EmployeeMapper employeeMapper;

    @Cacheable(value = CacheConfig.EMPLOYEE_CACHE, key = "#employeeId.toString()")
    public EmployeeResponse getEmployeeById(UUID employeeId) {
        Employee employee = employeeRepository.findActiveById(employeeId)
                .orElseThrow(() -> EmployeeNotFoundException.byId(employeeId.toString()));
        return employeeMapper.toEmployeeResponse(employee);
    }

    public EmployeeResponse getEmployeeByUserId(UUID userId) {
        Employee employee = employeeRepository.findActiveByUserId(userId)
                .orElseThrow(() -> EmployeeNotFoundException.byUserId(userId.toString()));
        return employeeMapper.toEmployeeResponse(employee);
    }

    public Page<EmployeeResponse> getAllActiveEmployees(Pageable pageable) {
        return employeeRepository.findAllActive(pageable)
                .map(employeeMapper::toEmployeeResponse);
    }

    public Page<EmployeeResponse> getEmployeesByDepartment(UUID departmentId, Pageable pageable) {
        return employeeRepository.findActiveByDepartmentId(departmentId, pageable)
                .map(employeeMapper::toEmployeeResponse);
    }

    public Page<EmployeeResponse> getEmployeesByStore(UUID storeId, Pageable pageable) {
        return employeeRepository.findActiveByStoreId(storeId, pageable)
                .map(employeeMapper::toEmployeeResponse);
    }

    public Page<EmployeeResponse> searchEmployees(String keyword, Pageable pageable) {
        return employeeRepository.searchActive(keyword, pageable)
                .map(employeeMapper::toEmployeeResponse);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.EMPLOYEE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "EMPLOYEE")
    public EmployeeResponse createEmployee(CreateEmployeeRequest request, UUID createdBy) {
        if (employeeRepository.existsActiveByEmployeeCode(request.getEmployeeCode())) {
            throw ValidationException.duplicateField("employee_code", request.getEmployeeCode());
        }
        if (employeeRepository.existsActiveByEmail(request.getEmail())) {
            throw ValidationException.duplicateField("email", request.getEmail());
        }

        Employee employee = Employee.builder()
                .employeeCode(request.getEmployeeCode())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .fullName(request.getLastName() + " " + request.getFirstName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender() != null ? Gender.valueOf(request.getGender()) : null)
                .idCardNumber(request.getIdCardNumber())
                .idCardIssuedDate(request.getIdCardIssuedDate())
                .idCardIssuedPlace(request.getIdCardIssuedPlace())
                .permanentAddress(request.getPermanentAddress())
                .currentAddress(request.getCurrentAddress())
                .position(request.getPosition())
                .joinDate(request.getJoinDate())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankName(request.getBankName())
                .taxCode(request.getTaxCode())
                .socialInsuranceNumber(request.getSocialInsuranceNumber())
                .healthInsuranceNumber(request.getHealthInsuranceNumber())
                .createdBy(createdBy)
                .build();

        if (request.getUserId() != null) {
            User user = userRepository.findActiveById(request.getUserId())
                    .orElseThrow(() -> new com.hainam.worksphere.shared.exception.UserNotFoundException("User not found with id: " + request.getUserId()));
            employee.setUser(user);
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findActiveById(request.getDepartmentId())
                    .orElseThrow(() -> DepartmentNotFoundException.byId(request.getDepartmentId().toString()));
            employee.setDepartment(department);
        }

        if (request.getStoreId() != null) {
            Store store = storeRepository.findActiveById(request.getStoreId())
                    .orElseThrow(() -> StoreNotFoundException.byId(request.getStoreId().toString()));
            employee.setStore(store);
        }

        Employee saved = employeeRepository.save(employee);
        AuditContext.registerCreated(saved);

        return employeeMapper.toEmployeeResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.EMPLOYEE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "EMPLOYEE")
    public EmployeeResponse updateEmployee(UUID employeeId, UpdateEmployeeRequest request, UUID updatedBy) {
        Employee employee = employeeRepository.findActiveById(employeeId)
                .orElseThrow(() -> EmployeeNotFoundException.byId(employeeId.toString()));

        AuditContext.snapshot(employee);

        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getFirstName() != null || request.getLastName() != null) {
            employee.setFullName(employee.getLastName() + " " + employee.getFirstName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsActiveByEmail(request.getEmail())) {
                throw ValidationException.duplicateField("email", request.getEmail());
            }
            employee.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) employee.setPhone(request.getPhone());
        if (request.getDateOfBirth() != null) employee.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) employee.setGender(Gender.valueOf(request.getGender()));
        if (request.getIdCardNumber() != null) employee.setIdCardNumber(request.getIdCardNumber());
        if (request.getIdCardIssuedDate() != null) employee.setIdCardIssuedDate(request.getIdCardIssuedDate());
        if (request.getIdCardIssuedPlace() != null) employee.setIdCardIssuedPlace(request.getIdCardIssuedPlace());
        if (request.getPermanentAddress() != null) employee.setPermanentAddress(request.getPermanentAddress());
        if (request.getCurrentAddress() != null) employee.setCurrentAddress(request.getCurrentAddress());
        if (request.getPosition() != null) employee.setPosition(request.getPosition());
        if (request.getEmploymentStatus() != null) employee.setEmploymentStatus(EmploymentStatus.valueOf(request.getEmploymentStatus()));
        if (request.getBankAccountNumber() != null) employee.setBankAccountNumber(request.getBankAccountNumber());
        if (request.getBankName() != null) employee.setBankName(request.getBankName());
        if (request.getTaxCode() != null) employee.setTaxCode(request.getTaxCode());
        if (request.getSocialInsuranceNumber() != null) employee.setSocialInsuranceNumber(request.getSocialInsuranceNumber());
        if (request.getHealthInsuranceNumber() != null) employee.setHealthInsuranceNumber(request.getHealthInsuranceNumber());
        if (request.getLeaveDate() != null) employee.setLeaveDate(request.getLeaveDate());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findActiveById(request.getDepartmentId())
                    .orElseThrow(() -> DepartmentNotFoundException.byId(request.getDepartmentId().toString()));
            employee.setDepartment(department);
        }

        if (request.getStoreId() != null) {
            Store store = storeRepository.findActiveById(request.getStoreId())
                    .orElseThrow(() -> StoreNotFoundException.byId(request.getStoreId().toString()));
            employee.setStore(store);
        }

        employee.setUpdatedBy(updatedBy);
        Employee saved = employeeRepository.save(employee);
        AuditContext.registerUpdated(saved);

        return employeeMapper.toEmployeeResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.EMPLOYEE_CACHE, allEntries = true)
    @AuditAction(type = ActionType.DELETE, entity = "EMPLOYEE")
    public void softDeleteEmployee(UUID employeeId, UUID deletedBy) {
        Employee employee = employeeRepository.findActiveById(employeeId)
                .orElseThrow(() -> EmployeeNotFoundException.byId(employeeId.toString()));

        AuditContext.registerDeleted(employee);

        employee.setIsDeleted(true);
        employee.setDeletedAt(Instant.now());
        employee.setDeletedBy(deletedBy);
        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        employeeRepository.save(employee);
    }

    public ResourceStatsResponse getEmployeeStats() {
        long terminated = employeeRepository.countByEmploymentStatusAndIsDeletedFalse(EmploymentStatus.TERMINATED);
        long resigned = employeeRepository.countByEmploymentStatusAndIsDeletedFalse(EmploymentStatus.RESIGNED);
        long softDeleted = employeeRepository.countByIsDeletedTrue();
        return ResourceStatsResponse.builder()
                .total(employeeRepository.count())
                .active(employeeRepository.countByEmploymentStatusAndIsDeletedFalse(EmploymentStatus.ACTIVE))
                .inactive(employeeRepository.countByEmploymentStatusAndIsDeletedFalse(EmploymentStatus.ON_LEAVE) +
                        employeeRepository.countByEmploymentStatusAndIsDeletedFalse(EmploymentStatus.PROBATION))
                .deleted(terminated + resigned + softDeleted)
                .build();
    }
}
