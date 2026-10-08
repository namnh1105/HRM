package com.hainam.worksphere.payroll.domain;

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

@DisplayName("Payroll Domain Tests")
class PayrollTest extends BaseUnitTest {

    @Test
    @DisplayName("Should create payroll with builder pattern")
    void shouldCreatePayrollWithBuilderPattern() {
        // Given
        UUID id = UUID.randomUUID();
        Employee employee = TestFixtures.createTestEmployee();
        Integer month = 1;
        Integer year = 2025;
        Double baseSalary = 15000000.0;
        Double salaryCoefficient = 1.0;
        Integer workingDays = 22;
        Integer actualWorkingDays = 22;
        Instant now = Instant.now();

        // When
        Payroll payroll = Payroll.builder()
                .id(id)
                .employee(employee)
                .month(month)
                .year(year)
                .baseSalary(baseSalary)
                .salaryCoefficient(salaryCoefficient)
                .workingDays(workingDays)
                .actualWorkingDays(actualWorkingDays)
                .status(PayrollStatus.DRAFT)
                .isDeleted(false)
                .createdAt(now)
                .build();

        // Then
        assertAll(
                () -> assertThat(payroll.getId()).isEqualTo(id),
                () -> assertThat(payroll.getEmployee()).isEqualTo(employee),
                () -> assertThat(payroll.getMonth()).isEqualTo(month),
                () -> assertThat(payroll.getYear()).isEqualTo(year),
                () -> assertThat(payroll.getBaseSalary()).isEqualTo(baseSalary),
                () -> assertThat(payroll.getSalaryCoefficient()).isEqualTo(salaryCoefficient),
                () -> assertThat(payroll.getWorkingDays()).isEqualTo(workingDays),
                () -> assertThat(payroll.getActualWorkingDays()).isEqualTo(actualWorkingDays),
                () -> assertThat(payroll.getStatus()).isEqualTo(PayrollStatus.DRAFT),
                () -> assertThat(payroll.getIsDeleted()).isFalse(),
                () -> assertThat(payroll.getCreatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("Should create payroll with default values")
    void shouldCreatePayrollWithDefaultValues() {
        // When
        Payroll payroll = Payroll.builder()
                .employee(TestFixtures.createTestEmployee())
                .month(6)
                .year(2025)
                .build();

        // Then
        assertAll(
                () -> assertThat(payroll.getOvertimeHours()).isEqualTo(0.0),
                () -> assertThat(payroll.getOvertimePay()).isEqualTo(0.0),
                () -> assertThat(payroll.getAllowance()).isEqualTo(0.0),
                () -> assertThat(payroll.getBonus()).isEqualTo(0.0),
                () -> assertThat(payroll.getSocialInsurance()).isEqualTo(0.0),
                () -> assertThat(payroll.getHealthInsurance()).isEqualTo(0.0),
                () -> assertThat(payroll.getUnemploymentInsurance()).isEqualTo(0.0),
                () -> assertThat(payroll.getPersonalIncomeTax()).isEqualTo(0.0),
                () -> assertThat(payroll.getTotalDeductions()).isEqualTo(0.0),
                () -> assertThat(payroll.getTotalIncome()).isEqualTo(0.0),
                () -> assertThat(payroll.getNetSalary()).isEqualTo(0.0),
                () -> assertThat(payroll.getStatus()).isEqualTo(PayrollStatus.DRAFT),    // Default value
                () -> assertThat(payroll.getIsDeleted()).isFalse()                        // Default value
        );
    }

    @Test
    @DisplayName("Should create payroll with no args constructor")
    void shouldCreatePayrollWithNoArgsConstructor() {
        // When
        Payroll payroll = new Payroll();

        // Then
        assertThat(payroll).isNotNull();
    }

    @Test
    @DisplayName("Should handle payroll status enum")
    void shouldHandlePayrollStatusEnum() {
        // Given
        Payroll payroll = Payroll.builder()
                .employee(TestFixtures.createTestEmployee())
                .month(1)
                .year(2025)
                .build();

        // When & Then - All statuses
        payroll.setStatus(PayrollStatus.DRAFT);
        assertThat(payroll.getStatus()).isEqualTo(PayrollStatus.DRAFT);

        payroll.setStatus(PayrollStatus.CONFIRMED);
        assertThat(payroll.getStatus()).isEqualTo(PayrollStatus.CONFIRMED);

        payroll.setStatus(PayrollStatus.PAID);
        assertThat(payroll.getStatus()).isEqualTo(PayrollStatus.PAID);

        payroll.setStatus(PayrollStatus.CANCELLED);
        assertThat(payroll.getStatus()).isEqualTo(PayrollStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should handle employee relationship")
    void shouldHandleEmployeeRelationship() {
        // Given
        Employee employee = TestFixtures.createTestEmployee();

        // When
        Payroll payroll = Payroll.builder()
                .employee(employee)
                .month(1)
                .year(2025)
                .build();

        // Then
        assertAll(
                () -> assertThat(payroll.getEmployee()).isNotNull(),
                () -> assertThat(payroll.getEmployee().getFullName()).isEqualTo("Nguyen Van A"),
                () -> assertThat(payroll.getEmployee().getEmployeeCode()).isEqualTo("EMP001")
        );
    }

    @Test
    @DisplayName("Should handle salary calculation fields")
    void shouldHandleSalaryCalculationFields() {
        // Given
        Payroll payroll = Payroll.builder()
                .employee(TestFixtures.createTestEmployee())
                .month(1)
                .year(2025)
                .baseSalary(15000000.0)
                .salaryCoefficient(1.0)
                .workingDays(22)
                .actualWorkingDays(20)
                .build();

        // When
        payroll.setOvertimeHours(10.0);
        payroll.setOvertimePay(500000.0);
        payroll.setAllowance(1000000.0);
        payroll.setBonus(2000000.0);
        payroll.setSocialInsurance(1200000.0);
        payroll.setHealthInsurance(225000.0);
        payroll.setUnemploymentInsurance(150000.0);
        payroll.setPersonalIncomeTax(750000.0);
        payroll.setTotalDeductions(2325000.0);
        payroll.setTotalIncome(18500000.0);
        payroll.setNetSalary(16175000.0);

        // Then
        assertAll(
                () -> assertThat(payroll.getOvertimeHours()).isEqualTo(10.0),
                () -> assertThat(payroll.getOvertimePay()).isEqualTo(500000.0),
                () -> assertThat(payroll.getAllowance()).isEqualTo(1000000.0),
                () -> assertThat(payroll.getBonus()).isEqualTo(2000000.0),
                () -> assertThat(payroll.getSocialInsurance()).isEqualTo(1200000.0),
                () -> assertThat(payroll.getHealthInsurance()).isEqualTo(225000.0),
                () -> assertThat(payroll.getUnemploymentInsurance()).isEqualTo(150000.0),
                () -> assertThat(payroll.getPersonalIncomeTax()).isEqualTo(750000.0),
                () -> assertThat(payroll.getTotalDeductions()).isEqualTo(2325000.0),
                () -> assertThat(payroll.getTotalIncome()).isEqualTo(18500000.0),
                () -> assertThat(payroll.getNetSalary()).isEqualTo(16175000.0)
        );
    }

    @Test
    @DisplayName("Should handle soft deletion fields")
    void shouldHandleSoftDeletionFields() {
        // Given
        Instant deletionTime = Instant.now();
        UUID deletedBy = UUID.randomUUID();

        // When
        Payroll payroll = Payroll.builder()
                .employee(TestFixtures.createTestEmployee())
                .month(1)
                .year(2025)
                .isDeleted(true)
                .deletedAt(deletionTime)
                .deletedBy(deletedBy)
                .build();

        // Then
        assertAll(
                () -> assertThat(payroll.getIsDeleted()).isTrue(),
                () -> assertThat(payroll.getDeletedAt()).isEqualTo(deletionTime),
                () -> assertThat(payroll.getDeletedBy()).isEqualTo(deletedBy)
        );
    }

    @Test
    @DisplayName("Should handle month year fields")
    void shouldHandleMonthYearFields() {
        // Given & When - January
        Payroll janPayroll = Payroll.builder()
                .employee(TestFixtures.createTestEmployee())
                .month(1)
                .year(2025)
                .build();

        // Given & When - December
        Payroll decPayroll = Payroll.builder()
                .employee(TestFixtures.createTestEmployee())
                .month(12)
                .year(2025)
                .build();

        // Then
        assertAll(
                () -> assertThat(janPayroll.getMonth()).isEqualTo(1),
                () -> assertThat(janPayroll.getYear()).isEqualTo(2025),
                () -> assertThat(decPayroll.getMonth()).isEqualTo(12),
                () -> assertThat(decPayroll.getYear()).isEqualTo(2025)
        );
    }
}
