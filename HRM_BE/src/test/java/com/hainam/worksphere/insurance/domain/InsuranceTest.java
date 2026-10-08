package com.hainam.worksphere.insurance.domain;

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

@DisplayName("Insurance Domain Tests")
class InsuranceTest extends BaseUnitTest {

    @Test
    @DisplayName("Should create insurance with builder pattern")
    void shouldCreateInsuranceWithBuilderPattern() {
        // Given
        UUID id = UUID.randomUUID();
        String code = "INS001";
        String name = "Social Insurance";
        String provider = "Vietnam Social Insurance";
        Double employeeRate = 8.0;
        Double employerRate = 17.5;
        String description = "Mandatory social insurance";
        Instant now = Instant.now();

        // When
        Insurance insurance = Insurance.builder()
                .id(id)
                .code(code)
                .name(name)
                .insuranceType(InsuranceType.SOCIAL)
                .provider(provider)
                .employeeRate(employeeRate)
                .employerRate(employerRate)
                .description(description)
                .isActive(true)
                .isDeleted(false)
                .createdAt(now)
                .build();

        // Then
        assertAll(
                () -> assertThat(insurance.getId()).isEqualTo(id),
                () -> assertThat(insurance.getCode()).isEqualTo(code),
                () -> assertThat(insurance.getName()).isEqualTo(name),
                () -> assertThat(insurance.getInsuranceType()).isEqualTo(InsuranceType.SOCIAL),
                () -> assertThat(insurance.getProvider()).isEqualTo(provider),
                () -> assertThat(insurance.getEmployeeRate()).isEqualTo(employeeRate),
                () -> assertThat(insurance.getEmployerRate()).isEqualTo(employerRate),
                () -> assertThat(insurance.getDescription()).isEqualTo(description),
                () -> assertThat(insurance.getIsActive()).isTrue(),
                () -> assertThat(insurance.getIsDeleted()).isFalse(),
                () -> assertThat(insurance.getCreatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("Should create insurance with default values")
    void shouldCreateInsuranceWithDefaultValues() {
        // When
        Insurance insurance = Insurance.builder()
                .code("INS_DEF")
                .name("Default Insurance")
                .build();

        // Then
        assertAll(
                () -> assertThat(insurance.getIsActive()).isTrue(),    // Default value
                () -> assertThat(insurance.getIsDeleted()).isFalse()   // Default value
        );
    }

    @Test
    @DisplayName("Should handle insurance type enum")
    void shouldHandleInsuranceTypeEnum() {
        // Given
        Insurance insurance = Insurance.builder()
                .code("INS_TYPE")
                .name("Type Test Insurance")
                .build();

        // When & Then - All insurance types
        insurance.setInsuranceType(InsuranceType.SOCIAL);
        assertThat(insurance.getInsuranceType()).isEqualTo(InsuranceType.SOCIAL);

        insurance.setInsuranceType(InsuranceType.HEALTH);
        assertThat(insurance.getInsuranceType()).isEqualTo(InsuranceType.HEALTH);

        insurance.setInsuranceType(InsuranceType.UNEMPLOYMENT);
        assertThat(insurance.getInsuranceType()).isEqualTo(InsuranceType.UNEMPLOYMENT);

        insurance.setInsuranceType(InsuranceType.ACCIDENT);
        assertThat(insurance.getInsuranceType()).isEqualTo(InsuranceType.ACCIDENT);
    }

    @Test
    @DisplayName("Should create insurance registration with builder pattern")
    void shouldCreateInsuranceRegistrationWithBuilderPattern() {
        // Given
        UUID id = UUID.randomUUID();
        Employee employee = TestFixtures.createTestEmployee();
        Insurance insurance = Insurance.builder()
                .id(UUID.randomUUID())
                .code("INS001")
                .name("Social Insurance")
                .insuranceType(InsuranceType.SOCIAL)
                .isActive(true)
                .build();
        String registrationNumber = "REG001";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        Instant now = Instant.now();

        // When
        InsuranceRegistration registration = InsuranceRegistration.builder()
                .id(id)
                .employee(employee)
                .insurance(insurance)
                .registrationNumber(registrationNumber)
                .startDate(startDate)
                .endDate(endDate)
                .status(InsuranceRegistrationStatus.ACTIVE)
                .isDeleted(false)
                .createdAt(now)
                .build();

        // Then
        assertAll(
                () -> assertThat(registration.getId()).isEqualTo(id),
                () -> assertThat(registration.getEmployee()).isEqualTo(employee),
                () -> assertThat(registration.getInsurance()).isEqualTo(insurance),
                () -> assertThat(registration.getRegistrationNumber()).isEqualTo(registrationNumber),
                () -> assertThat(registration.getStartDate()).isEqualTo(startDate),
                () -> assertThat(registration.getEndDate()).isEqualTo(endDate),
                () -> assertThat(registration.getStatus()).isEqualTo(InsuranceRegistrationStatus.ACTIVE),
                () -> assertThat(registration.getIsDeleted()).isFalse(),
                () -> assertThat(registration.getCreatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("Should handle insurance registration status enum")
    void shouldHandleInsuranceRegistrationStatusEnum() {
        // Given
        InsuranceRegistration registration = InsuranceRegistration.builder()
                .employee(TestFixtures.createTestEmployee())
                .insurance(Insurance.builder().code("INS001").name("Test").build())
                .build();

        // When & Then - All statuses
        registration.setStatus(InsuranceRegistrationStatus.ACTIVE);
        assertThat(registration.getStatus()).isEqualTo(InsuranceRegistrationStatus.ACTIVE);

        registration.setStatus(InsuranceRegistrationStatus.EXPIRED);
        assertThat(registration.getStatus()).isEqualTo(InsuranceRegistrationStatus.EXPIRED);

        registration.setStatus(InsuranceRegistrationStatus.CANCELLED);
        assertThat(registration.getStatus()).isEqualTo(InsuranceRegistrationStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should handle rate fields")
    void shouldHandleRateFields() {
        // Given
        Double employeeRate = 8.0;
        Double employerRate = 17.5;

        // When
        Insurance insurance = Insurance.builder()
                .code("INS_RATE")
                .name("Rate Test Insurance")
                .insuranceType(InsuranceType.SOCIAL)
                .employeeRate(employeeRate)
                .employerRate(employerRate)
                .build();

        // Then
        assertAll(
                () -> assertThat(insurance.getEmployeeRate()).isEqualTo(employeeRate),
                () -> assertThat(insurance.getEmployerRate()).isEqualTo(employerRate)
        );
    }
}
