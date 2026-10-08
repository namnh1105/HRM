package com.hainam.worksphere.workshift.service;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.shared.exception.WorkShiftNotFoundException;
import com.hainam.worksphere.workshift.domain.EmployeeWorkShift;
import com.hainam.worksphere.workshift.domain.WorkShift;
import com.hainam.worksphere.workshift.dto.request.AssignWorkShiftRequest;
import com.hainam.worksphere.workshift.dto.request.UpdateAssignWorkShiftRequest;
import com.hainam.worksphere.workshift.dto.response.EmployeeWorkShiftResponse;
import com.hainam.worksphere.workshift.mapper.EmployeeWorkShiftMapper;
import com.hainam.worksphere.workshift.repository.EmployeeWorkShiftRepository;
import com.hainam.worksphere.workshift.repository.WorkShiftRepository;
import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeWorkShiftService {

    private final EmployeeWorkShiftRepository employeeWorkShiftRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeWorkShiftMapper employeeWorkShiftMapper;

    @Transactional(readOnly = true)
    public List<EmployeeWorkShiftResponse> getByEmployeeId(UUID employeeId) {
        employeeRepository.findActiveById(employeeId)
                .orElseThrow(() -> EmployeeNotFoundException.byId(employeeId.toString()));

        return employeeWorkShiftRepository.findActiveByEmployeeId(employeeId)
                .stream()
                .map(employeeWorkShiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeWorkShiftResponse> getByEmployeeIdAndDate(UUID employeeId, LocalDate date) {
        employeeRepository.findActiveById(employeeId)
                .orElseThrow(() -> EmployeeNotFoundException.byId(employeeId.toString()));

        return employeeWorkShiftRepository.findActiveByEmployeeIdAndDate(employeeId, date)
                .stream()
                .map(employeeWorkShiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeWorkShiftResponse> getByWorkShiftId(UUID workShiftId) {
        workShiftRepository.findActiveById(workShiftId)
                .orElseThrow(() -> WorkShiftNotFoundException.byId(workShiftId.toString()));

        return employeeWorkShiftRepository.findActiveByWorkShiftId(workShiftId)
                .stream()
                .map(employeeWorkShiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeWorkShiftResponse getById(UUID id) {
        EmployeeWorkShift ews = employeeWorkShiftRepository.findActiveById(id)
                .orElseThrow(() -> new ValidationException("Employee work shift assignment not found with id: " + id));
        return employeeWorkShiftMapper.toResponse(ews);
    }

    @Transactional
    @AuditAction(type = ActionType.CREATE, entity = "EMPLOYEE_WORK_SHIFT")
    public EmployeeWorkShiftResponse assignWorkShift(AssignWorkShiftRequest request, UUID createdBy) {
        Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                .orElseThrow(() -> EmployeeNotFoundException.byId(request.getEmployeeId().toString()));

        WorkShift workShift = workShiftRepository.findActiveById(request.getWorkShiftId())
                .orElseThrow(() -> WorkShiftNotFoundException.byId(request.getWorkShiftId().toString()));

        // Check for duplicate assignment: same employee + same shift + same date
        boolean exists = employeeWorkShiftRepository.existsActiveAssignment(
                request.getEmployeeId(), request.getWorkShiftId(),
                request.getDate()
        );
        if (exists) {
            throw new ValidationException(
                    "Nhân viên đã được gán ca làm việc này vào ngày " + request.getDate()
            );
        }

        EmployeeWorkShift ews = employeeWorkShiftMapper.toEntity(request);
        ews.setEmployee(employee);
        ews.setWorkShift(workShift);
        ews.setCreatedBy(createdBy);

        EmployeeWorkShift saved = employeeWorkShiftRepository.save(ews);
        AuditContext.registerCreated(saved);
        return employeeWorkShiftMapper.toResponse(saved);
    }

    @Transactional
    @AuditAction(type = ActionType.UPDATE, entity = "EMPLOYEE_WORK_SHIFT")
    public EmployeeWorkShiftResponse updateAssignment(UUID id, UpdateAssignWorkShiftRequest request, UUID updatedBy) {
        EmployeeWorkShift ews = employeeWorkShiftRepository.findActiveById(id)
                .orElseThrow(() -> new ValidationException("Employee work shift assignment not found with id: " + id));

        AuditContext.snapshot(ews);

        if (request.getDate() != null) {
            ews.setDate(request.getDate());
        }

        ews.setUpdatedBy(updatedBy);
        EmployeeWorkShift saved = employeeWorkShiftRepository.save(ews);
        AuditContext.registerUpdated(saved);
        return employeeWorkShiftMapper.toResponse(saved);
    }

    @Transactional
    @AuditAction(type = ActionType.DELETE, entity = "EMPLOYEE_WORK_SHIFT")
    public void softDelete(UUID id, UUID deletedBy) {
        EmployeeWorkShift ews = employeeWorkShiftRepository.findActiveById(id)
                .orElseThrow(() -> new ValidationException("Employee work shift assignment not found with id: " + id));

        AuditContext.registerDeleted(ews);

        ews.setIsDeleted(true);
        ews.setDeletedAt(Instant.now());
        ews.setDeletedBy(deletedBy);
        employeeWorkShiftRepository.save(ews);
    }

    @Transactional(readOnly = true)
    public List<EmployeeWorkShiftResponse> getByUserIdAndDate(UUID userId, LocalDate date) {
        Employee employee = employeeRepository.findActiveByUserId(userId)
                .orElseThrow(() -> EmployeeNotFoundException.byId("user:" + userId));

        return employeeWorkShiftRepository.findActiveByEmployeeIdAndDate(employee.getId(), date)
                .stream()
                .map(employeeWorkShiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeWorkShiftResponse> getAllByUserId(UUID userId) {
        Employee employee = employeeRepository.findActiveByUserId(userId)
                .orElseThrow(() -> EmployeeNotFoundException.byId("user:" + userId));

        return employeeWorkShiftRepository.findActiveByEmployeeId(employee.getId())
                .stream()
                .map(employeeWorkShiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeWorkShiftResponse> getByStoreId(UUID storeId) {
        return employeeWorkShiftRepository.findActiveByStoreId(storeId)
                .stream()
                .map(employeeWorkShiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeWorkShiftResponse> getAllActive() {
        return employeeWorkShiftRepository.findAllActive()
                .stream()
                .map(employeeWorkShiftMapper::toResponse)
                .collect(Collectors.toList());
    }
}
