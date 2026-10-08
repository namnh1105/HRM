package com.hainam.worksphere.leave.domain;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.employee.domain.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("LeaveRequest Domain Tests")
class LeaveRequestTest extends BaseUnitTest {

    @Test
    @DisplayName("Should create leave request with builder pattern")
    void shouldCreateLeaveRequestWithBuilderPattern() {
        // Given
        UUID id = UUID.randomUUID();
        Employee employee = TestFixtures.createTestEmployee();
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        Double totalDays = 3.0;
        String reason = "Personal vacation";
        Instant now = Instant.now();

        // When
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .id(id)
                .employee(employee)
                .leaveType(LeaveType.ANNUAL_LEAVE)
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(totalDays)
                .reason(reason)
                .status(LeaveRequestStatus.PENDING)
                .isDeleted(false)
                .createdAt(now)
                .build();

        // Then
        assertAll(
                () -> assertThat(leaveRequest.getId()).isEqualTo(id),
                () -> assertThat(leaveRequest.getEmployee()).isEqualTo(employee),
                () -> assertThat(leaveRequest.getLeaveType()).isEqualTo(LeaveType.ANNUAL_LEAVE),
                () -> assertThat(leaveRequest.getStartDate()).isEqualTo(startDate),
                () -> assertThat(leaveRequest.getEndDate()).isEqualTo(endDate),
                () -> assertThat(leaveRequest.getTotalDays()).isEqualTo(totalDays),
                () -> assertThat(leaveRequest.getReason()).isEqualTo(reason),
                () -> assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.PENDING),
                () -> assertThat(leaveRequest.getIsDeleted()).isFalse(),
                () -> assertThat(leaveRequest.getCreatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("Should create leave request with default values")
    void shouldCreateLeaveRequestWithDefaultValues() {
        // When
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(TestFixtures.createTestEmployee())
                .leaveType(LeaveType.ANNUAL_LEAVE)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .build();

        // Then
        assertAll(
                () -> assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.PENDING), // Default value
                () -> assertThat(leaveRequest.getIsDeleted()).isFalse()                            // Default value
        );
    }

    @Test
    @DisplayName("Should create leave request with no args constructor")
    void shouldCreateLeaveRequestWithNoArgsConstructor() {
        // When
        LeaveRequest leaveRequest = new LeaveRequest();

        // Then
        assertThat(leaveRequest).isNotNull();
    }

    @Test
    @DisplayName("Should handle leave type enum")
    void shouldHandleLeaveTypeEnum() {
        // Given
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(TestFixtures.createTestEmployee())
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .build();

        // When & Then - All 9 leave types
        leaveRequest.setLeaveType(LeaveType.ANNUAL_LEAVE);
        assertThat(leaveRequest.getLeaveType()).isEqualTo(LeaveType.ANNUAL_LEAVE);

        leaveRequest.setLeaveType(LeaveType.SICK_LEAVE);
        assertThat(leaveRequest.getLeaveType()).isEqualTo(LeaveType.SICK_LEAVE);

        leaveRequest.setLeaveType(LeaveType.PERSONAL_LEAVE);
        assertThat(leaveRequest.getLeaveType()).isEqualTo(LeaveType.PERSONAL_LEAVE);

        leaveRequest.setLeaveType(LeaveType.MATERNITY_LEAVE);
        assertThat(leaveRequest.getLeaveType()).isEqualTo(LeaveType.MATERNITY_LEAVE);

        leaveRequest.setLeaveType(LeaveType.PATERNITY_LEAVE);
        assertThat(leaveRequest.getLeaveType()).isEqualTo(LeaveType.PATERNITY_LEAVE);

        leaveRequest.setLeaveType(LeaveType.WEDDING_LEAVE);
        assertThat(leaveRequest.getLeaveType()).isEqualTo(LeaveType.WEDDING_LEAVE);

        leaveRequest.setLeaveType(LeaveType.BEREAVEMENT_LEAVE);
        assertThat(leaveRequest.getLeaveType()).isEqualTo(LeaveType.BEREAVEMENT_LEAVE);

        leaveRequest.setLeaveType(LeaveType.UNPAID_LEAVE);
        assertThat(leaveRequest.getLeaveType()).isEqualTo(LeaveType.UNPAID_LEAVE);

        leaveRequest.setLeaveType(LeaveType.OTHER);
        assertThat(leaveRequest.getLeaveType()).isEqualTo(LeaveType.OTHER);
    }

    @Test
    @DisplayName("Should handle leave request status enum")
    void shouldHandleLeaveRequestStatusEnum() {
        // Given
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(TestFixtures.createTestEmployee())
                .leaveType(LeaveType.ANNUAL_LEAVE)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .build();

        // When & Then - All statuses
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);
        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.PENDING);

        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.APPROVED);

        leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.REJECTED);

        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);
        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should handle approval")
    void shouldHandleApproval() {
        // Given
        Employee approver = TestFixtures.createTestEmployee();
        Instant approvedAt = Instant.now();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(TestFixtures.createTestEmployee())
                .leaveType(LeaveType.ANNUAL_LEAVE)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .totalDays(3.0)
                .reason("Vacation")
                .status(LeaveRequestStatus.PENDING)
                .build();

        // When
        leaveRequest.setApprover(approver);
        leaveRequest.setApprovedAt(approvedAt);
        leaveRequest.setApproverComment("Approved. Enjoy your vacation.");
        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);

        // Then
        assertAll(
                () -> assertThat(leaveRequest.getApprover()).isNotNull(),
                () -> assertThat(leaveRequest.getApprovedAt()).isEqualTo(approvedAt),
                () -> assertThat(leaveRequest.getApproverComment()).isEqualTo("Approved. Enjoy your vacation."),
                () -> assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.APPROVED)
        );
    }

    @Test
    @DisplayName("Should handle rejection")
    void shouldHandleRejection() {
        // Given
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(TestFixtures.createTestEmployee())
                .leaveType(LeaveType.ANNUAL_LEAVE)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .status(LeaveRequestStatus.PENDING)
                .build();

        // When
        leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
        leaveRequest.setApproverComment("Insufficient leave balance.");

        // Then
        assertAll(
                () -> assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.REJECTED),
                () -> assertThat(leaveRequest.getApproverComment()).isEqualTo("Insufficient leave balance.")
        );
    }

    @Test
    @DisplayName("Should handle cancellation")
    void shouldHandleCancellation() {
        // Given
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(TestFixtures.createTestEmployee())
                .leaveType(LeaveType.ANNUAL_LEAVE)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .status(LeaveRequestStatus.PENDING)
                .build();

        // When
        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);

        // Then
        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequestStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should handle soft deletion fields")
    void shouldHandleSoftDeletionFields() {
        // Given
        Instant deletionTime = Instant.now();
        UUID deletedBy = UUID.randomUUID();

        // When
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(TestFixtures.createTestEmployee())
                .leaveType(LeaveType.ANNUAL_LEAVE)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .isDeleted(true)
                .deletedAt(deletionTime)
                .deletedBy(deletedBy)
                .build();

        // Then
        assertAll(
                () -> assertThat(leaveRequest.getIsDeleted()).isTrue(),
                () -> assertThat(leaveRequest.getDeletedAt()).isEqualTo(deletionTime),
                () -> assertThat(leaveRequest.getDeletedBy()).isEqualTo(deletedBy)
        );
    }

    @Test
    @DisplayName("Should calculate total days")
    void shouldCalculateTotalDays() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 3, 10);
        LocalDate endDate = LocalDate.of(2025, 3, 14);
        Double totalDays = 5.0;

        // When
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(TestFixtures.createTestEmployee())
                .leaveType(LeaveType.ANNUAL_LEAVE)
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(totalDays)
                .build();

        // Then
        assertAll(
                () -> assertThat(leaveRequest.getStartDate()).isEqualTo(startDate),
                () -> assertThat(leaveRequest.getEndDate()).isEqualTo(endDate),
                () -> assertThat(leaveRequest.getTotalDays()).isEqualTo(5.0)
        );
    }
}
