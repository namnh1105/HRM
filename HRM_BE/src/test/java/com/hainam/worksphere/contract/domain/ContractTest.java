package com.hainam.worksphere.contract.domain;

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

@DisplayName("Contract Domain Tests")
class ContractTest extends BaseUnitTest {

    @Test
    @DisplayName("Should create contract with builder pattern")
    void shouldCreateContractWithBuilderPattern() {
        // Given
        UUID id = UUID.randomUUID();
        Employee employee = TestFixtures.createTestEmployee();
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        LocalDate signingDate = LocalDate.of(2022, 12, 15);
        Double baseSalary = 15000000.0;
        Double salaryCoefficient = 1.2;
        Instant now = Instant.now();

        // When
        Contract contract = Contract.builder()
                .id(id)
                .employee(employee)
                .contractType(ContractType.INDEFINITE_TERM)
                .startDate(startDate)
                .endDate(endDate)
                .signingDate(signingDate)
                .baseSalary(baseSalary)
                .salaryCoefficient(salaryCoefficient)
                .status(ContractStatus.ACTIVE)
                .isDeleted(false)
                .createdAt(now)
                .build();

        // Then
        assertAll(
                () -> assertThat(contract.getId()).isEqualTo(id),
                () -> assertThat(contract.getEmployee()).isEqualTo(employee),
                () -> assertThat(contract.getContractType()).isEqualTo(ContractType.INDEFINITE_TERM),
                () -> assertThat(contract.getStartDate()).isEqualTo(startDate),
                () -> assertThat(contract.getEndDate()).isEqualTo(endDate),
                () -> assertThat(contract.getSigningDate()).isEqualTo(signingDate),
                () -> assertThat(contract.getBaseSalary()).isEqualTo(baseSalary),
                () -> assertThat(contract.getSalaryCoefficient()).isEqualTo(salaryCoefficient),
                () -> assertThat(contract.getStatus()).isEqualTo(ContractStatus.ACTIVE),
                () -> assertThat(contract.getIsDeleted()).isFalse(),
                () -> assertThat(contract.getCreatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("Should create contract with default values")
    void shouldCreateContractWithDefaultValues() {
        // When
        Contract contract = Contract.builder()
                .employee(TestFixtures.createTestEmployee())
                .contractType(ContractType.INDEFINITE_TERM)
                .startDate(LocalDate.now())
                .baseSalary(15000000.0)
                .build();

        // Then
        assertAll(
                () -> assertThat(contract.getStatus()).isEqualTo(ContractStatus.ACTIVE),   // Default value
                () -> assertThat(contract.getSalaryCoefficient()).isEqualTo(1.0),           // Default value
                () -> assertThat(contract.getIsDeleted()).isFalse()                         // Default value
        );
    }

    @Test
    @DisplayName("Should create contract with no args constructor")
    void shouldCreateContractWithNoArgsConstructor() {
        // When
        Contract contract = new Contract();

        // Then
        assertThat(contract).isNotNull();
    }

    @Test
    @DisplayName("Should handle contract type enum")
    void shouldHandleContractTypeEnum() {
        // Given
        Contract contract = Contract.builder()
                .employee(TestFixtures.createTestEmployee())
                .startDate(LocalDate.now())
                .baseSalary(15000000.0)
                .build();

        // When & Then - All contract types
        contract.setContractType(ContractType.PROBATION);
        assertThat(contract.getContractType()).isEqualTo(ContractType.PROBATION);

        contract.setContractType(ContractType.DEFINITE_TERM);
        assertThat(contract.getContractType()).isEqualTo(ContractType.DEFINITE_TERM);

        contract.setContractType(ContractType.INDEFINITE_TERM);
        assertThat(contract.getContractType()).isEqualTo(ContractType.INDEFINITE_TERM);

        contract.setContractType(ContractType.SEASONAL);
        assertThat(contract.getContractType()).isEqualTo(ContractType.SEASONAL);

        contract.setContractType(ContractType.PART_TIME);
        assertThat(contract.getContractType()).isEqualTo(ContractType.PART_TIME);
    }

    @Test
    @DisplayName("Should handle contract status enum")
    void shouldHandleContractStatusEnum() {
        // Given
        Contract contract = Contract.builder()
                .employee(TestFixtures.createTestEmployee())
                .contractType(ContractType.INDEFINITE_TERM)
                .startDate(LocalDate.now())
                .baseSalary(15000000.0)
                .build();

        // When & Then - All statuses
        contract.setStatus(ContractStatus.ACTIVE);
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.ACTIVE);

        contract.setStatus(ContractStatus.EXPIRED);
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.EXPIRED);

        contract.setStatus(ContractStatus.TERMINATED);
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.TERMINATED);

        contract.setStatus(ContractStatus.RENEWED);
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.RENEWED);
    }

    @Test
    @DisplayName("Should handle employee relationship")
    void shouldHandleEmployeeRelationship() {
        // Given
        Employee employee = TestFixtures.createTestEmployee();

        // When
        Contract contract = Contract.builder()
                .employee(employee)
                .contractType(ContractType.INDEFINITE_TERM)
                .startDate(LocalDate.now())
                .baseSalary(15000000.0)
                .build();

        // Then
        assertAll(
                () -> assertThat(contract.getEmployee()).isNotNull(),
                () -> assertThat(contract.getEmployee().getFullName()).isEqualTo("Nguyen Van A"),
                () -> assertThat(contract.getEmployee().getEmployeeCode()).isEqualTo("EMP001")
        );
    }

    @Test
    @DisplayName("Should handle salary fields")
    void shouldHandleSalaryFields() {
        // Given
        Double baseSalary = 20000000.0;
        Double salaryCoefficient = 1.5;

        // When
        Contract contract = Contract.builder()
                .employee(TestFixtures.createTestEmployee())
                .contractType(ContractType.INDEFINITE_TERM)
                .startDate(LocalDate.now())
                .baseSalary(baseSalary)
                .salaryCoefficient(salaryCoefficient)
                .build();

        // Then
        assertAll(
                () -> assertThat(contract.getBaseSalary()).isEqualTo(baseSalary),
                () -> assertThat(contract.getSalaryCoefficient()).isEqualTo(salaryCoefficient)
        );
    }

    @Test
    @DisplayName("Should handle soft deletion fields")
    void shouldHandleSoftDeletionFields() {
        // Given
        Instant deletionTime = Instant.now();
        UUID deletedBy = UUID.randomUUID();

        // When
        Contract contract = Contract.builder()
                .employee(TestFixtures.createTestEmployee())
                .contractType(ContractType.INDEFINITE_TERM)
                .startDate(LocalDate.now())
                .baseSalary(15000000.0)
                .isDeleted(true)
                .deletedAt(deletionTime)
                .deletedBy(deletedBy)
                .build();

        // Then
        assertAll(
                () -> assertThat(contract.getIsDeleted()).isTrue(),
                () -> assertThat(contract.getDeletedAt()).isEqualTo(deletionTime),
                () -> assertThat(contract.getDeletedBy()).isEqualTo(deletedBy)
        );
    }

    @Test
    @DisplayName("Should handle date fields")
    void shouldHandleDateFields() {
        // Given
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        LocalDate signingDate = LocalDate.of(2022, 12, 15);

        // When
        Contract contract = Contract.builder()
                .employee(TestFixtures.createTestEmployee())
                .contractType(ContractType.DEFINITE_TERM)
                .startDate(startDate)
                .endDate(endDate)
                .signingDate(signingDate)
                .baseSalary(15000000.0)
                .build();

        // Then
        assertAll(
                () -> assertThat(contract.getStartDate()).isEqualTo(startDate),
                () -> assertThat(contract.getEndDate()).isEqualTo(endDate),
                () -> assertThat(contract.getSigningDate()).isEqualTo(signingDate)
        );
    }
}
