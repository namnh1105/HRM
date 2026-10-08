package com.hainam.worksphere.leave.service;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.leave.domain.LeaveRequest;
import com.hainam.worksphere.leave.domain.LeaveRequestStatus;
import com.hainam.worksphere.leave.domain.LeaveType;
import com.hainam.worksphere.leave.dto.request.ApproveLeaveRequestDto;
import com.hainam.worksphere.leave.dto.request.CreateLeaveRequestDto;
import com.hainam.worksphere.leave.dto.response.LeaveRequestResponse;
import com.hainam.worksphere.leave.mapper.LeaveRequestMapper;
import com.hainam.worksphere.leave.repository.LeaveRequestRepository;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import com.hainam.worksphere.shared.exception.LeaveRequestNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("LeaveRequestService Tests")
class LeaveRequestServiceTest extends BaseUnitTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveRequestMapper leaveRequestMapper;

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    private Employee testEmployee;
    private Employee testApprover;
    private LeaveRequest testLeaveRequest;
    private LeaveRequestResponse testLeaveRequestResponse;
    private UUID employeeId;
    private UUID approverId;
    private UUID leaveRequestId;

    @BeforeEach
    void setUp() {
        testEmployee = TestFixtures.createTestEmployee();
        employeeId = testEmployee.getId();

        testApprover = TestFixtures.createTestEmployee("approver@example.com");
        approverId = testApprover.getId();

        testLeaveRequest = TestFixtures.createTestLeaveRequest();
        testLeaveRequest.setId(UUID.randomUUID());
        testLeaveRequest.setEmployee(testEmployee);
        leaveRequestId = testLeaveRequest.getId();

        testLeaveRequestResponse = LeaveRequestResponse.builder()
                .id(leaveRequestId)
                .employeeId(employeeId)
                .employeeName(testEmployee.getFullName())
                .leaveType(LeaveType.ANNUAL_LEAVE.name())
                .startDate(testLeaveRequest.getStartDate())
                .endDate(testLeaveRequest.getEndDate())
                .totalDays(testLeaveRequest.getTotalDays())
                .reason(testLeaveRequest.getReason())
                .status(LeaveRequestStatus.PENDING.name())
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should create leave request successfully")
    void shouldCreateLeaveRequestSuccessfully() {
        // Given
        CreateLeaveRequestDto request = CreateLeaveRequestDto.builder()
                .leaveType(LeaveType.ANNUAL_LEAVE)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .reason("Personal vacation")
                .build();

        when(employeeRepository.findActiveById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        when(leaveRequestMapper.toLeaveRequestResponse(testLeaveRequest)).thenReturn(testLeaveRequestResponse);

        // When
        LeaveRequestResponse result = leaveRequestService.createLeaveRequest(employeeId, request);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getEmployeeId()).isEqualTo(employeeId),
            () -> assertThat(result.getLeaveType()).isEqualTo(LeaveType.ANNUAL_LEAVE.name()),
            () -> assertThat(result.getStatus()).isEqualTo(LeaveRequestStatus.PENDING.name()),
            () -> verify(employeeRepository).findActiveById(employeeId),
            () -> verify(leaveRequestRepository).save(any(LeaveRequest.class)),
            () -> verify(leaveRequestMapper).toLeaveRequestResponse(testLeaveRequest)
        );
    }

    @Test
    @DisplayName("Should throw ValidationException when end date is before start date")
    void shouldThrowWhenEndDateBeforeStartDate() {
        // Given
        CreateLeaveRequestDto request = CreateLeaveRequestDto.builder()
                .leaveType(LeaveType.ANNUAL_LEAVE)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(2))
                .reason("Invalid dates")
                .build();

        when(employeeRepository.findActiveById(employeeId)).thenReturn(Optional.of(testEmployee));

        // When & Then
        assertThatThrownBy(() -> leaveRequestService.createLeaveRequest(employeeId, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date must not be before start date");

        verify(employeeRepository).findActiveById(employeeId);
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
        verifyNoInteractions(leaveRequestMapper);
    }

    @Test
    @DisplayName("Should approve leave request successfully")
    void shouldApproveLeaveRequestSuccessfully() {
        // Given
        ApproveLeaveRequestDto request = ApproveLeaveRequestDto.builder()
                .approved(true)
                .comment("Approved by manager")
                .build();

        LeaveRequestResponse approvedResponse = LeaveRequestResponse.builder()
                .id(leaveRequestId)
                .employeeId(employeeId)
                .status(LeaveRequestStatus.APPROVED.name())
                .approverId(approverId)
                .approvedAt(Instant.now())
                .approverComment("Approved by manager")
                .build();

        when(leaveRequestRepository.findActiveById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));
        when(employeeRepository.findActiveById(approverId)).thenReturn(Optional.of(testApprover));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        when(leaveRequestMapper.toLeaveRequestResponse(testLeaveRequest)).thenReturn(approvedResponse);

        // When
        LeaveRequestResponse result = leaveRequestService.approveLeaveRequest(leaveRequestId, approverId, request);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getStatus()).isEqualTo(LeaveRequestStatus.APPROVED.name()),
            () -> assertThat(result.getApproverId()).isEqualTo(approverId),
            () -> assertThat(result.getApprovedAt()).isNotNull(),
            () -> assertThat(testLeaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.APPROVED),
            () -> assertThat(testLeaveRequest.getApprover()).isEqualTo(testApprover),
            () -> assertThat(testLeaveRequest.getApprovedAt()).isNotNull(),
            () -> verify(leaveRequestRepository).findActiveById(leaveRequestId),
            () -> verify(employeeRepository).findActiveById(approverId),
            () -> verify(leaveRequestRepository).save(any(LeaveRequest.class))
        );
    }

    @Test
    @DisplayName("Should reject leave request successfully")
    void shouldRejectLeaveRequestSuccessfully() {
        // Given
        ApproveLeaveRequestDto request = ApproveLeaveRequestDto.builder()
                .approved(false)
                .comment("Insufficient leave balance")
                .build();

        LeaveRequestResponse rejectedResponse = LeaveRequestResponse.builder()
                .id(leaveRequestId)
                .employeeId(employeeId)
                .status(LeaveRequestStatus.REJECTED.name())
                .approverId(approverId)
                .approverComment("Insufficient leave balance")
                .build();

        when(leaveRequestRepository.findActiveById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));
        when(employeeRepository.findActiveById(approverId)).thenReturn(Optional.of(testApprover));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        when(leaveRequestMapper.toLeaveRequestResponse(testLeaveRequest)).thenReturn(rejectedResponse);

        // When
        LeaveRequestResponse result = leaveRequestService.approveLeaveRequest(leaveRequestId, approverId, request);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getStatus()).isEqualTo(LeaveRequestStatus.REJECTED.name()),
            () -> assertThat(testLeaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.REJECTED),
            () -> verify(leaveRequestRepository).findActiveById(leaveRequestId),
            () -> verify(employeeRepository).findActiveById(approverId),
            () -> verify(leaveRequestRepository).save(any(LeaveRequest.class))
        );
    }

    @Test
    @DisplayName("Should throw ValidationException when approving non-pending request")
    void shouldThrowWhenApprovingNonPendingRequest() {
        // Given
        testLeaveRequest.setStatus(LeaveRequestStatus.APPROVED);

        ApproveLeaveRequestDto request = ApproveLeaveRequestDto.builder()
                .approved(true)
                .comment("Trying to approve again")
                .build();

        when(leaveRequestRepository.findActiveById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));

        // When & Then
        assertThatThrownBy(() -> leaveRequestService.approveLeaveRequest(leaveRequestId, approverId, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Only PENDING leave requests can be approved or rejected");

        verify(leaveRequestRepository).findActiveById(leaveRequestId);
        verify(employeeRepository, never()).findActiveById(any(UUID.class));
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Should cancel leave request successfully")
    void shouldCancelLeaveRequestSuccessfully() {
        // Given
        LeaveRequestResponse cancelledResponse = LeaveRequestResponse.builder()
                .id(leaveRequestId)
                .employeeId(employeeId)
                .status(LeaveRequestStatus.CANCELLED.name())
                .build();

        when(leaveRequestRepository.findActiveById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);
        when(leaveRequestMapper.toLeaveRequestResponse(testLeaveRequest)).thenReturn(cancelledResponse);

        // When
        LeaveRequestResponse result = leaveRequestService.cancelLeaveRequest(leaveRequestId, employeeId);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getStatus()).isEqualTo(LeaveRequestStatus.CANCELLED.name()),
            () -> assertThat(testLeaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.CANCELLED),
            () -> verify(leaveRequestRepository).findActiveById(leaveRequestId),
            () -> verify(leaveRequestRepository).save(any(LeaveRequest.class)),
            () -> verify(leaveRequestMapper).toLeaveRequestResponse(testLeaveRequest)
        );
    }

    @Test
    @DisplayName("Should throw ValidationException when cancelling non-pending request")
    void shouldThrowWhenCancellingNonPendingRequest() {
        // Given
        testLeaveRequest.setStatus(LeaveRequestStatus.APPROVED);

        when(leaveRequestRepository.findActiveById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));

        // When & Then
        assertThatThrownBy(() -> leaveRequestService.cancelLeaveRequest(leaveRequestId, employeeId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Only PENDING leave requests can be cancelled");

        verify(leaveRequestRepository).findActiveById(leaveRequestId);
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
        verifyNoInteractions(leaveRequestMapper);
    }

    @Test
    @DisplayName("Should get my leave requests successfully")
    void shouldGetMyLeaveRequestsSuccessfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        LeaveRequest leaveRequest1 = TestFixtures.createTestLeaveRequest();
        leaveRequest1.setEmployee(testEmployee);
        LeaveRequest leaveRequest2 = TestFixtures.createTestLeaveRequest();
        leaveRequest2.setEmployee(testEmployee);

        List<LeaveRequest> leaveRequests = Arrays.asList(leaveRequest1, leaveRequest2);
        Page<LeaveRequest> leaveRequestPage = new PageImpl<>(leaveRequests, pageable, leaveRequests.size());

        when(leaveRequestRepository.findActiveByEmployeeId(eq(employeeId), eq(pageable))).thenReturn(leaveRequestPage);
        when(leaveRequestMapper.toLeaveRequestResponse(any(LeaveRequest.class))).thenReturn(testLeaveRequestResponse);

        // When
        Page<LeaveRequestResponse> result = leaveRequestService.getMyLeaveRequests(employeeId, pageable);

        // Then
        assertAll(
            () -> assertThat(result.getContent()).hasSize(2),
            () -> verify(leaveRequestRepository).findActiveByEmployeeId(eq(employeeId), eq(pageable)),
            () -> verify(leaveRequestMapper, times(2)).toLeaveRequestResponse(any(LeaveRequest.class))
        );
    }

    @Test
    @DisplayName("Should get pending leave requests successfully")
    void shouldGetPendingLeaveRequestsSuccessfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        LeaveRequest pendingRequest1 = TestFixtures.createTestLeaveRequest();
        LeaveRequest pendingRequest2 = TestFixtures.createTestLeaveRequest();

        List<LeaveRequest> pendingRequests = Arrays.asList(pendingRequest1, pendingRequest2);
        Page<LeaveRequest> pendingRequestPage = new PageImpl<>(pendingRequests, pageable, pendingRequests.size());

        when(leaveRequestRepository.findActiveByStatus(eq(LeaveRequestStatus.PENDING), eq(pageable))).thenReturn(pendingRequestPage);
        when(leaveRequestMapper.toLeaveRequestResponse(any(LeaveRequest.class))).thenReturn(testLeaveRequestResponse);

        // When
        Page<LeaveRequestResponse> result = leaveRequestService.getPendingLeaveRequests(pageable);

        // Then
        assertAll(
            () -> assertThat(result.getContent()).hasSize(2),
            () -> verify(leaveRequestRepository).findActiveByStatus(eq(LeaveRequestStatus.PENDING), eq(pageable)),
            () -> verify(leaveRequestMapper, times(2)).toLeaveRequestResponse(any(LeaveRequest.class))
        );
    }
}
