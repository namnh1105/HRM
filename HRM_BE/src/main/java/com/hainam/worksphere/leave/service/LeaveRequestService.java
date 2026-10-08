package com.hainam.worksphere.leave.service;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.leave.domain.LeaveRequest;
import com.hainam.worksphere.leave.domain.LeaveRequestStatus;
import com.hainam.worksphere.leave.dto.request.ApproveLeaveRequestDto;
import com.hainam.worksphere.leave.dto.request.CreateLeaveRequestDto;
import com.hainam.worksphere.leave.dto.response.LeaveRequestResponse;
import com.hainam.worksphere.leave.mapper.LeaveRequestMapper;
import com.hainam.worksphere.leave.repository.LeaveRequestRepository;
import com.hainam.worksphere.notification.domain.NotificationType;
import com.hainam.worksphere.notification.service.NotificationService;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.dto.ResourceStatsResponse;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import com.hainam.worksphere.shared.exception.LeaveRequestNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestMapper leaveRequestMapper;
    private final NotificationService notificationService;

    @Transactional
    @CacheEvict(value = CacheConfig.LEAVE_REQUEST_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "LEAVE_REQUEST")
    public LeaveRequestResponse createLeaveRequest(UUID employeeId, CreateLeaveRequestDto request) {
        Employee employee = employeeRepository.findActiveById(employeeId)
                .orElseThrow(() -> EmployeeNotFoundException.byId(employeeId.toString()));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ValidationException("End date must not be before start date");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Start date must not be in the past");
        }

        double totalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(totalDays)
                .reason(request.getReason())
                .attachmentUrl(request.getAttachmentUrl())
                .createdBy(employeeId)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        AuditContext.registerCreated(saved);
        return leaveRequestMapper.toLeaveRequestResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.LEAVE_REQUEST_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "LEAVE_REQUEST", actionCode = "APPROVE_LEAVE_REQUEST")
    public LeaveRequestResponse approveLeaveRequest(UUID leaveRequestId, UUID approverId, ApproveLeaveRequestDto request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findActiveById(leaveRequestId)
                .orElseThrow(() -> LeaveRequestNotFoundException.byId(leaveRequestId.toString()));

        AuditContext.snapshot(leaveRequest);

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new ValidationException("Only PENDING leave requests can be approved or rejected");
        }

        Employee approver = employeeRepository.findActiveById(approverId)
                .orElseThrow(() -> EmployeeNotFoundException.byId(approverId.toString()));

        leaveRequest.setStatus(request.getApproved() ? LeaveRequestStatus.APPROVED : LeaveRequestStatus.REJECTED);
        leaveRequest.setApprover(approver);
        leaveRequest.setApprovedAt(Instant.now());
        leaveRequest.setApproverComment(request.getComment());
        leaveRequest.setUpdatedBy(approverId);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        AuditContext.registerUpdated(saved);
        
        // Send notification
        if (saved.getEmployee().getUser() != null) {
            NotificationType notifType = request.getApproved() ? NotificationType.LEAVE_APPROVED : NotificationType.LEAVE_REJECTED;
            String title = request.getApproved() ? "Leave Request Approved" : "Leave Request Rejected";
            String approverName = approver.getFullName();
            
            notificationService.sendNotification(
                saved.getEmployee().getUser().getId(),
                notifType,
                title,
                String.format("Your leave request for %s to %s has been %s by %s.", 
                    saved.getStartDate(), saved.getEndDate(), request.getApproved() ? "approved" : "rejected", approverName)
            );
        }

        return leaveRequestMapper.toLeaveRequestResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.LEAVE_REQUEST_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "LEAVE_REQUEST", actionCode = "CANCEL_LEAVE_REQUEST")
    public LeaveRequestResponse cancelLeaveRequest(UUID leaveRequestId, UUID employeeId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findActiveById(leaveRequestId)
                .orElseThrow(() -> LeaveRequestNotFoundException.byId(leaveRequestId.toString()));

        AuditContext.snapshot(leaveRequest);

        if (!leaveRequest.getEmployee().getId().equals(employeeId)) {
            throw new ValidationException("You can only cancel your own leave requests");
        }

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new ValidationException("Only PENDING leave requests can be cancelled");
        }

        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);
        leaveRequest.setUpdatedBy(employeeId);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        AuditContext.registerUpdated(saved);
        return leaveRequestMapper.toLeaveRequestResponse(saved);
    }

    @Cacheable(value = CacheConfig.LEAVE_REQUEST_CACHE, key = "'employee:' + #employeeId")
    public Page<LeaveRequestResponse> getMyLeaveRequests(UUID employeeId, Pageable pageable) {
        return leaveRequestRepository.findActiveByEmployeeId(employeeId, pageable)
                .map(leaveRequestMapper::toLeaveRequestResponse);
    }

    @Cacheable(value = CacheConfig.LEAVE_REQUEST_CACHE, key = "'pending'")
    public Page<LeaveRequestResponse> getPendingLeaveRequests(Pageable pageable) {
        return leaveRequestRepository.findActiveByStatus(LeaveRequestStatus.PENDING, pageable)
                .map(leaveRequestMapper::toLeaveRequestResponse);
    }

    @Cacheable(value = CacheConfig.LEAVE_REQUEST_CACHE, key = "#id.toString()")
    public LeaveRequestResponse getLeaveRequestById(UUID id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findActiveById(id)
                .orElseThrow(() -> LeaveRequestNotFoundException.byId(id.toString()));
        return leaveRequestMapper.toLeaveRequestResponse(leaveRequest);
    }

    public Page<LeaveRequestResponse> getAllLeaveRequests(LeaveRequestStatus status, Pageable pageable) {
        if (status != null) {
            return leaveRequestRepository.findActiveByStatus(status, pageable)
                    .map(leaveRequestMapper::toLeaveRequestResponse);
        }
        return leaveRequestRepository.findAllActive(pageable)
                .map(leaveRequestMapper::toLeaveRequestResponse);
    }

    public ResourceStatsResponse getLeaveRequestStats() {
        return ResourceStatsResponse.builder()
                .total(leaveRequestRepository.count())
                .active(leaveRequestRepository.countByStatusAndIsDeletedFalse(LeaveRequestStatus.PENDING))
                .inactive(leaveRequestRepository.countByStatusAndIsDeletedFalse(LeaveRequestStatus.APPROVED) +
                        leaveRequestRepository.countByStatusAndIsDeletedFalse(LeaveRequestStatus.REJECTED) +
                        leaveRequestRepository.countByStatusAndIsDeletedFalse(LeaveRequestStatus.CANCELLED))
                .deleted(leaveRequestRepository.countByIsDeletedTrue())
                .build();
    }
}
