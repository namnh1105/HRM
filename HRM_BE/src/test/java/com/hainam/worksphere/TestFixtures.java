package com.hainam.worksphere;

import com.hainam.worksphere.auth.domain.RefreshToken;
import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.department.domain.Department;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.domain.Gender;
import com.hainam.worksphere.employee.domain.EmploymentStatus;
import com.hainam.worksphere.attendance.domain.Attendance;
import com.hainam.worksphere.attendance.domain.AttendanceStatus;
import com.hainam.worksphere.leave.domain.LeaveRequest;
import com.hainam.worksphere.leave.domain.LeaveType;
import com.hainam.worksphere.leave.domain.LeaveRequestStatus;
import com.hainam.worksphere.contract.domain.Contract;
import com.hainam.worksphere.contract.domain.ContractType;
import com.hainam.worksphere.contract.domain.ContractStatus;
import com.hainam.worksphere.payroll.domain.Payroll;
import com.hainam.worksphere.payroll.domain.PayrollStatus;
import com.hainam.worksphere.workshift.domain.WorkShift;

import java.time.LocalDate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Test fixture factory for creating test data objects
 */
public class TestFixtures {

    // User fixtures
    public static User createTestUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("john.doe@example.com")
                .password("encodedPassword")
                .isEnabled(true)
                .isDeleted(false)
                .createdAt(Instant.now())
                .build();
    }

    public static User createTestUser(String email) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password("encodedPassword")
                .isEnabled(true)
                .isDeleted(false)
                .createdAt(Instant.now())
                .build();
    }

    // Role fixtures
    public static Role createTestRole() {
        return Role.builder()
                .id(UUID.randomUUID())
                .code("TEST_ROLE")
                .displayName("Test Role")
                .description("A test role")
                .isSystem(false)
                .isActive(true)
                .createdAt(Instant.now())
                .build();
    }

    // Permission fixtures
    public static Permission createTestPermission() {
        return Permission.builder()
                .id(UUID.randomUUID())
                .code("TEST_PERMISSION")
                .displayName("Test Permission")
                .description("A test permission")
                .resource("USER")
                .action("READ")
                .isSystem(false)
                .isActive(true)
                .createdAt(Instant.now())
                .build();
    }

    // RefreshToken fixtures
    public static RefreshToken createTestRefreshToken() {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("test-refresh-token")
                .user(createTestUser())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .isRevoked(false)
                .createdAt(Instant.now())
                .build();
    }

    // Department fixtures
    public static Department createTestDepartment() {
        return Department.builder()
                .id(UUID.randomUUID())
                .name("Engineering")
                .code("ENG")
                .description("Engineering Department")
                .isActive(true)
                .isDeleted(false)
                .createdAt(Instant.now())
                .build();
    }

    // Employee fixtures
    public static Employee createTestEmployee() {
        return Employee.builder()
                .id(UUID.randomUUID())
                .employeeCode("EMP001")
                .firstName("Nguyen")
                .lastName("Van A")
                .fullName("Nguyen Van A")
                .email("nguyen.vana@example.com")
                .phone("0901234567")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .gender(Gender.MALE)
                .position("Software Engineer")
                .joinDate(LocalDate.of(2023, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .isDeleted(false)
                .createdAt(Instant.now())
                .build();
    }

    public static Employee createTestEmployee(String email) {
        return Employee.builder()
                .id(UUID.randomUUID())
                .employeeCode("EMP" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .firstName("Nguyen")
                .lastName("Van A")
                .fullName("Nguyen Van A")
                .email(email)
                .phone("0901234567")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .gender(Gender.MALE)
                .position("Software Engineer")
                .joinDate(LocalDate.of(2023, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .isDeleted(false)
                .createdAt(Instant.now())
                .build();
    }

    // WorkShift fixtures
    public static WorkShift createTestWorkShift() {
        return WorkShift.builder()
                .name("Morning Shift")
                .code("MORNING")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .breakDuration(1.0)
                .totalHours(8.0)
                .isActive(true)
                .isNightShift(false)
                .isDeleted(false)
                .build();
    }

    // Attendance fixtures
    public static Attendance createTestAttendance() {
        return Attendance.builder()
                .employee(createTestEmployee())
                .workDate(LocalDate.now())
                .checkInTime(LocalTime.of(8, 0))
                .status(AttendanceStatus.PRESENT)
                .workingHours(8.0)
                .isDeleted(false)
                .build();
    }

    // LeaveRequest fixtures
    public static LeaveRequest createTestLeaveRequest() {
        return LeaveRequest.builder()
                .employee(createTestEmployee())
                .leaveType(LeaveType.ANNUAL_LEAVE)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .totalDays(3.0)
                .reason("Personal vacation")
                .status(LeaveRequestStatus.PENDING)
                .isDeleted(false)
                .build();
    }

    // Contract fixtures
    public static Contract createTestContract() {
        return Contract.builder()
                .employee(createTestEmployee())
                .contractType(ContractType.INDEFINITE_TERM)
                .startDate(LocalDate.of(2023, 1, 1))
                .baseSalary(15000000.0)
                .salaryCoefficient(1.0)
                .status(ContractStatus.ACTIVE)
                .isDeleted(false)
                .build();
    }

    // Payroll fixtures
    public static Payroll createTestPayroll() {
        return Payroll.builder()
                .employee(createTestEmployee())
                .month(1)
                .year(2025)
                .baseSalary(15000000.0)
                .salaryCoefficient(1.0)
                .workingDays(22)
                .actualWorkingDays(22)
                .status(PayrollStatus.DRAFT)
                .isDeleted(false)
                .build();
    }
}
