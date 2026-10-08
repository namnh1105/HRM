package com.hainam.worksphere.employee.domain;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.department.domain.Department;
import com.hainam.worksphere.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Employee Domain Tests")
class EmployeeTest extends BaseUnitTest {

    @Test
    @DisplayName("Should create employee with builder pattern")
    void shouldCreateEmployeeWithBuilderPattern() {
        // Given
        UUID id = UUID.randomUUID();
        String employeeCode = "EMP001";
        String firstName = "Nguyen";
        String lastName = "Van A";
        String fullName = "Nguyen Van A";
        String email = "nguyen.vana@example.com";
        String phone = "0901234567";
        LocalDate dateOfBirth = LocalDate.of(1990, 1, 15);
        String position = "Software Engineer";
        LocalDate joinDate = LocalDate.of(2023, 1, 1);
        Instant now = Instant.now();

        // When
        Employee employee = Employee.builder()
                .id(id)
                .employeeCode(employeeCode)
                .firstName(firstName)
                .lastName(lastName)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .dateOfBirth(dateOfBirth)
                .gender(Gender.MALE)
                .position(position)
                .joinDate(joinDate)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .isDeleted(false)
                .createdAt(now)
                .build();

        // Then
        assertAll(
                () -> assertThat(employee.getId()).isEqualTo(id),
                () -> assertThat(employee.getEmployeeCode()).isEqualTo(employeeCode),
                () -> assertThat(employee.getFirstName()).isEqualTo(firstName),
                () -> assertThat(employee.getLastName()).isEqualTo(lastName),
                () -> assertThat(employee.getFullName()).isEqualTo(fullName),
                () -> assertThat(employee.getEmail()).isEqualTo(email),
                () -> assertThat(employee.getPhone()).isEqualTo(phone),
                () -> assertThat(employee.getDateOfBirth()).isEqualTo(dateOfBirth),
                () -> assertThat(employee.getGender()).isEqualTo(Gender.MALE),
                () -> assertThat(employee.getPosition()).isEqualTo(position),
                () -> assertThat(employee.getJoinDate()).isEqualTo(joinDate),
                () -> assertThat(employee.getEmploymentStatus()).isEqualTo(EmploymentStatus.ACTIVE),
                () -> assertThat(employee.getIsDeleted()).isFalse(),
                () -> assertThat(employee.getCreatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("Should create employee with default values")
    void shouldCreateEmployeeWithDefaultValues() {
        // When
        Employee employee = Employee.builder()
                .employeeCode("EMP002")
                .firstName("Test")
                .lastName("User")
                .fullName("Test User")
                .email("test@example.com")
                .build();

        // Then
        assertAll(
                () -> assertThat(employee.getEmploymentStatus()).isEqualTo(EmploymentStatus.ACTIVE), // Default value
                () -> assertThat(employee.getIsDeleted()).isFalse()  // Default value
        );
    }

    @Test
    @DisplayName("Should create employee with no args constructor")
    void shouldCreateEmployeeWithNoArgsConstructor() {
        // When
        Employee employee = new Employee();

        // Then
        assertThat(employee).isNotNull();
    }

    @Test
    @DisplayName("Should handle soft deletion fields")
    void shouldHandleSoftDeletionFields() {
        // Given
        Instant deletionTime = Instant.now();
        UUID deletedBy = UUID.randomUUID();

        // When
        Employee employee = Employee.builder()
                .employeeCode("EMP_DEL")
                .firstName("Deleted")
                .lastName("Employee")
                .fullName("Deleted Employee")
                .email("deleted@example.com")
                .isDeleted(true)
                .deletedAt(deletionTime)
                .deletedBy(deletedBy)
                .build();

        // Then
        assertAll(
                () -> assertThat(employee.getIsDeleted()).isTrue(),
                () -> assertThat(employee.getDeletedAt()).isEqualTo(deletionTime),
                () -> assertThat(employee.getDeletedBy()).isEqualTo(deletedBy)
        );
    }

    @Test
    @DisplayName("Should handle audit timestamps")
    void shouldHandleAuditTimestamps() {
        // Given
        Instant createdTime = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant updatedTime = Instant.now();
        UUID createdBy = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();

        // When
        Employee employee = Employee.builder()
                .employeeCode("EMP_AUD")
                .firstName("Audit")
                .lastName("Employee")
                .fullName("Audit Employee")
                .email("audit@example.com")
                .createdAt(createdTime)
                .updatedAt(updatedTime)
                .createdBy(createdBy)
                .updatedBy(updatedBy)
                .build();

        // Then
        assertAll(
                () -> assertThat(employee.getCreatedAt()).isEqualTo(createdTime),
                () -> assertThat(employee.getUpdatedAt()).isEqualTo(updatedTime),
                () -> assertThat(employee.getCreatedBy()).isEqualTo(createdBy),
                () -> assertThat(employee.getUpdatedBy()).isEqualTo(updatedBy)
        );
    }

    @Test
    @DisplayName("Should handle employee status changes")
    void shouldHandleEmployeeStatusChanges() {
        // Given
        Employee employee = Employee.builder()
                .employeeCode("EMP_STATUS")
                .firstName("Status")
                .lastName("Test")
                .fullName("Status Test")
                .email("status@example.com")
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();

        // When & Then - RESIGNED
        employee.setEmploymentStatus(EmploymentStatus.RESIGNED);
        assertThat(employee.getEmploymentStatus()).isEqualTo(EmploymentStatus.RESIGNED);

        // When & Then - TERMINATED
        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        assertThat(employee.getEmploymentStatus()).isEqualTo(EmploymentStatus.TERMINATED);

        // When & Then - ON_LEAVE
        employee.setEmploymentStatus(EmploymentStatus.ON_LEAVE);
        assertThat(employee.getEmploymentStatus()).isEqualTo(EmploymentStatus.ON_LEAVE);

        // When & Then - PROBATION
        employee.setEmploymentStatus(EmploymentStatus.PROBATION);
        assertThat(employee.getEmploymentStatus()).isEqualTo(EmploymentStatus.PROBATION);
    }

    @Test
    @DisplayName("Should handle gender enum")
    void shouldHandleGenderEnum() {
        // Given
        Employee maleEmployee = Employee.builder()
                .employeeCode("EMP_M")
                .firstName("Male")
                .lastName("Employee")
                .fullName("Male Employee")
                .email("male@example.com")
                .gender(Gender.MALE)
                .build();

        Employee femaleEmployee = Employee.builder()
                .employeeCode("EMP_F")
                .firstName("Female")
                .lastName("Employee")
                .fullName("Female Employee")
                .email("female@example.com")
                .gender(Gender.FEMALE)
                .build();

        Employee otherEmployee = Employee.builder()
                .employeeCode("EMP_O")
                .firstName("Other")
                .lastName("Employee")
                .fullName("Other Employee")
                .email("other@example.com")
                .gender(Gender.OTHER)
                .build();

        // Then
        assertAll(
                () -> assertThat(maleEmployee.getGender()).isEqualTo(Gender.MALE),
                () -> assertThat(femaleEmployee.getGender()).isEqualTo(Gender.FEMALE),
                () -> assertThat(otherEmployee.getGender()).isEqualTo(Gender.OTHER)
        );
    }

    @Test
    @DisplayName("Should handle department relationship")
    void shouldHandleDepartmentRelationship() {
        // Given
        Department department = TestFixtures.createTestDepartment();
        Employee employee = Employee.builder()
                .employeeCode("EMP_DEPT")
                .firstName("Dept")
                .lastName("Employee")
                .fullName("Dept Employee")
                .email("dept@example.com")
                .build();

        // When
        employee.setDepartment(department);

        // Then
        assertAll(
                () -> assertThat(employee.getDepartment()).isNotNull(),
                () -> assertThat(employee.getDepartment().getName()).isEqualTo("Engineering"),
                () -> assertThat(employee.getDepartment().getCode()).isEqualTo("ENG")
        );
    }

    @Test
    @DisplayName("Should handle user relationship")
    void shouldHandleUserRelationship() {
        // Given
        User user = TestFixtures.createTestUser();
        Employee employee = Employee.builder()
                .employeeCode("EMP_USR")
                .firstName("User")
                .lastName("Employee")
                .fullName("User Employee")
                .email("user.emp@example.com")
                .build();

        // When
        employee.setUser(user);

        // Then
        assertAll(
                () -> assertThat(employee.getUser()).isNotNull(),
                () -> assertThat(employee.getUser().getEmail()).isEqualTo("john.doe@example.com")
        );
    }

    @Test
    @DisplayName("Should handle financial fields")
    void shouldHandleFinancialFields() {
        // Given
        String bankAccountNumber = "1234567890";
        String bankName = "Vietcombank";
        String taxCode = "TAX123456";

        // When
        Employee employee = Employee.builder()
                .employeeCode("EMP_FIN")
                .firstName("Finance")
                .lastName("Employee")
                .fullName("Finance Employee")
                .email("finance@example.com")
                .bankAccountNumber(bankAccountNumber)
                .bankName(bankName)
                .taxCode(taxCode)
                .build();

        // Then
        assertAll(
                () -> assertThat(employee.getBankAccountNumber()).isEqualTo(bankAccountNumber),
                () -> assertThat(employee.getBankName()).isEqualTo(bankName),
                () -> assertThat(employee.getTaxCode()).isEqualTo(taxCode)
        );
    }

    @Test
    @DisplayName("Should handle full name generation")
    void shouldHandleFullNameGeneration() {
        // Given
        Employee employee = Employee.builder()
                .employeeCode("EMP_NAME")
                .firstName("Nguyen")
                .lastName("Van B")
                .fullName("Nguyen Van B")
                .email("name@example.com")
                .build();

        // When
        employee.setFirstName("Tran");
        employee.setLastName("Thi C");
        employee.setFullName("Tran Thi C");

        // Then
        assertAll(
                () -> assertThat(employee.getFirstName()).isEqualTo("Tran"),
                () -> assertThat(employee.getLastName()).isEqualTo("Thi C"),
                () -> assertThat(employee.getFullName()).isEqualTo("Tran Thi C")
        );
    }
}
