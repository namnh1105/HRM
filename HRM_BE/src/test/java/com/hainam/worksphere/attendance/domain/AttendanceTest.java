package com.hainam.worksphere.attendance.domain;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.workshift.domain.WorkShift;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Attendance Domain Tests")
class AttendanceTest extends BaseUnitTest {

    @Test
    @DisplayName("Should create attendance with builder pattern")
    void shouldCreateAttendanceWithBuilderPattern() {
        // Given
        UUID id = UUID.randomUUID();
        Employee employee = TestFixtures.createTestEmployee();
        LocalDate workDate = LocalDate.now();
        LocalTime checkInTime = LocalTime.of(8, 0);
        LocalTime checkOutTime = LocalTime.of(17, 0);
        Double workingHours = 8.0;
        Instant now = Instant.now();

        // When
        Attendance attendance = Attendance.builder()
                .id(id)
                .employee(employee)
                .workDate(workDate)
                .checkInTime(checkInTime)
                .checkOutTime(checkOutTime)
                .status(AttendanceStatus.PRESENT)
                .workingHours(workingHours)
                .isDeleted(false)
                .createdAt(now)
                .build();

        // Then
        assertAll(
                () -> assertThat(attendance.getId()).isEqualTo(id),
                () -> assertThat(attendance.getEmployee()).isEqualTo(employee),
                () -> assertThat(attendance.getWorkDate()).isEqualTo(workDate),
                () -> assertThat(attendance.getCheckInTime()).isEqualTo(checkInTime),
                () -> assertThat(attendance.getCheckOutTime()).isEqualTo(checkOutTime),
                () -> assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENT),
                () -> assertThat(attendance.getWorkingHours()).isEqualTo(workingHours),
                () -> assertThat(attendance.getIsDeleted()).isFalse(),
                () -> assertThat(attendance.getCreatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("Should create attendance with default values")
    void shouldCreateAttendanceWithDefaultValues() {
        // When
        Attendance attendance = Attendance.builder()
                .employee(TestFixtures.createTestEmployee())
                .workDate(LocalDate.now())
                .build();

        // Then
        assertAll(
                () -> assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENT),   // Default value
                () -> assertThat(attendance.getOvertimeHours()).isEqualTo(0.0),                 // Default value
                () -> assertThat(attendance.getLateMinutes()).isEqualTo(0),                      // Default value
                () -> assertThat(attendance.getEarlyLeaveMinutes()).isEqualTo(0),                // Default value
                () -> assertThat(attendance.getIsDeleted()).isFalse()                            // Default value
        );
    }

    @Test
    @DisplayName("Should create attendance with no args constructor")
    void shouldCreateAttendanceWithNoArgsConstructor() {
        // When
        Attendance attendance = new Attendance();

        // Then
        assertThat(attendance).isNotNull();
    }

    @Test
    @DisplayName("Should handle check in and check out")
    void shouldHandleCheckInCheckOut() {
        // Given
        Attendance attendance = Attendance.builder()
                .employee(TestFixtures.createTestEmployee())
                .workDate(LocalDate.now())
                .build();

        LocalTime checkInTime = LocalTime.of(8, 0);
        LocalTime checkOutTime = LocalTime.of(17, 0);

        // When
        attendance.setCheckInTime(checkInTime);
        attendance.setCheckOutTime(checkOutTime);
        attendance.setWorkingHours(8.0);

        // Then
        assertAll(
                () -> assertThat(attendance.getCheckInTime()).isEqualTo(checkInTime),
                () -> assertThat(attendance.getCheckOutTime()).isEqualTo(checkOutTime),
                () -> assertThat(attendance.getWorkingHours()).isEqualTo(8.0)
        );
    }

    @Test
    @DisplayName("Should handle attendance status enum")
    void shouldHandleAttendanceStatusEnum() {
        // Given
        Attendance attendance = Attendance.builder()
                .employee(TestFixtures.createTestEmployee())
                .workDate(LocalDate.now())
                .build();

        // When & Then - All statuses
        attendance.setStatus(AttendanceStatus.PRESENT);
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENT);

        attendance.setStatus(AttendanceStatus.ABSENT);
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.ABSENT);

        attendance.setStatus(AttendanceStatus.LATE);
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.LATE);

        attendance.setStatus(AttendanceStatus.EARLY_LEAVE);
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.EARLY_LEAVE);

        attendance.setStatus(AttendanceStatus.HALF_DAY);
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.HALF_DAY);

        attendance.setStatus(AttendanceStatus.ON_LEAVE);
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.ON_LEAVE);

        attendance.setStatus(AttendanceStatus.HOLIDAY);
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.HOLIDAY);

        attendance.setStatus(AttendanceStatus.WEEKEND);
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.WEEKEND);
    }

    @Test
    @DisplayName("Should handle employee relationship")
    void shouldHandleEmployeeRelationship() {
        // Given
        Employee employee = TestFixtures.createTestEmployee();

        // When
        Attendance attendance = Attendance.builder()
                .employee(employee)
                .workDate(LocalDate.now())
                .build();

        // Then
        assertAll(
                () -> assertThat(attendance.getEmployee()).isNotNull(),
                () -> assertThat(attendance.getEmployee().getFullName()).isEqualTo("Nguyen Van A"),
                () -> assertThat(attendance.getEmployee().getEmployeeCode()).isEqualTo("EMP001")
        );
    }

    @Test
    @DisplayName("Should handle work shift relationship")
    void shouldHandleWorkShiftRelationship() {
        // Given
        WorkShift workShift = TestFixtures.createTestWorkShift();
        Attendance attendance = Attendance.builder()
                .employee(TestFixtures.createTestEmployee())
                .workDate(LocalDate.now())
                .build();

        // When
        attendance.setWorkShift(workShift);

        // Then
        assertAll(
                () -> assertThat(attendance.getWorkShift()).isNotNull(),
                () -> assertThat(attendance.getWorkShift().getName()).isEqualTo("Morning Shift"),
                () -> assertThat(attendance.getWorkShift().getCode()).isEqualTo("MORNING"),
                () -> assertThat(attendance.getWorkShift().getTotalHours()).isEqualTo(8.0)
        );
    }

    @Test
    @DisplayName("Should handle IP and GPS fields")
    void shouldHandleIpAndGpsFields() {
        // Given
        String checkInIp = "192.168.1.100";
        String checkOutIp = "192.168.1.101";
        Double checkInLatitude = 10.762622;
        Double checkInLongitude = 106.660172;
        Double checkOutLatitude = 10.762700;
        Double checkOutLongitude = 106.660200;

        // When
        Attendance attendance = Attendance.builder()
                .employee(TestFixtures.createTestEmployee())
                .workDate(LocalDate.now())
                .checkInIp(checkInIp)
                .checkOutIp(checkOutIp)
                .checkInLatitude(checkInLatitude)
                .checkInLongitude(checkInLongitude)
                .checkOutLatitude(checkOutLatitude)
                .checkOutLongitude(checkOutLongitude)
                .build();

        // Then
        assertAll(
                () -> assertThat(attendance.getCheckInIp()).isEqualTo(checkInIp),
                () -> assertThat(attendance.getCheckOutIp()).isEqualTo(checkOutIp),
                () -> assertThat(attendance.getCheckInLatitude()).isEqualTo(checkInLatitude),
                () -> assertThat(attendance.getCheckInLongitude()).isEqualTo(checkInLongitude),
                () -> assertThat(attendance.getCheckOutLatitude()).isEqualTo(checkOutLatitude),
                () -> assertThat(attendance.getCheckOutLongitude()).isEqualTo(checkOutLongitude)
        );
    }

    @Test
    @DisplayName("Should handle soft deletion fields")
    void shouldHandleSoftDeletionFields() {
        // Given
        Instant deletionTime = Instant.now();
        UUID deletedBy = UUID.randomUUID();

        // When
        Attendance attendance = Attendance.builder()
                .employee(TestFixtures.createTestEmployee())
                .workDate(LocalDate.now())
                .isDeleted(true)
                .deletedAt(deletionTime)
                .deletedBy(deletedBy)
                .build();

        // Then
        assertAll(
                () -> assertThat(attendance.getIsDeleted()).isTrue(),
                () -> assertThat(attendance.getDeletedAt()).isEqualTo(deletionTime),
                () -> assertThat(attendance.getDeletedBy()).isEqualTo(deletedBy)
        );
    }

    @Test
    @DisplayName("Should handle overtime and late minutes")
    void shouldHandleOvertimeAndLateMinutes() {
        // Given
        Attendance attendance = Attendance.builder()
                .employee(TestFixtures.createTestEmployee())
                .workDate(LocalDate.now())
                .build();

        // When
        attendance.setOvertimeHours(2.5);
        attendance.setLateMinutes(15);
        attendance.setEarlyLeaveMinutes(10);

        // Then
        assertAll(
                () -> assertThat(attendance.getOvertimeHours()).isEqualTo(2.5),
                () -> assertThat(attendance.getLateMinutes()).isEqualTo(15),
                () -> assertThat(attendance.getEarlyLeaveMinutes()).isEqualTo(10)
        );
    }
}
