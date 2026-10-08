package com.hainam.worksphere.department.domain;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.employee.domain.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Department Domain Tests")
class DepartmentTest extends BaseUnitTest {

    @Test
    @DisplayName("Should create department with builder pattern")
    void shouldCreateDepartmentWithBuilderPattern() {
        // Given
        UUID id = UUID.randomUUID();
        String name = "Engineering";
        String code = "ENG";
        String description = "Engineering Department";
        String phone = "0901234567";
        String email = "engineering@example.com";
        Instant now = Instant.now();

        // When
        Department department = Department.builder()
                .id(id)
                .name(name)
                .code(code)
                .description(description)
                .phone(phone)
                .email(email)
                .isActive(true)
                .isDeleted(false)
                .createdAt(now)
                .build();

        // Then
        assertAll(
                () -> assertThat(department.getId()).isEqualTo(id),
                () -> assertThat(department.getName()).isEqualTo(name),
                () -> assertThat(department.getCode()).isEqualTo(code),
                () -> assertThat(department.getDescription()).isEqualTo(description),
                () -> assertThat(department.getPhone()).isEqualTo(phone),
                () -> assertThat(department.getEmail()).isEqualTo(email),
                () -> assertThat(department.getIsActive()).isTrue(),
                () -> assertThat(department.getIsDeleted()).isFalse(),
                () -> assertThat(department.getCreatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("Should create department with default values")
    void shouldCreateDepartmentWithDefaultValues() {
        // When
        Department department = Department.builder()
                .name("HR")
                .code("HR")
                .build();

        // Then
        assertAll(
                () -> assertThat(department.getName()).isEqualTo("HR"),
                () -> assertThat(department.getCode()).isEqualTo("HR"),
                () -> assertThat(department.getIsActive()).isTrue(),   // Default value
                () -> assertThat(department.getIsDeleted()).isFalse()  // Default value
        );
    }

    @Test
    @DisplayName("Should create department with no args constructor")
    void shouldCreateDepartmentWithNoArgsConstructor() {
        // When
        Department department = new Department();

        // Then
        assertThat(department).isNotNull();
    }

    @Test
    @DisplayName("Should handle soft deletion fields")
    void shouldHandleSoftDeletionFields() {
        // Given
        Instant deletionTime = Instant.now();
        UUID deletedBy = UUID.randomUUID();

        // When
        Department department = Department.builder()
                .name("Deleted Dept")
                .code("DEL")
                .isDeleted(true)
                .deletedAt(deletionTime)
                .deletedBy(deletedBy)
                .build();

        // Then
        assertAll(
                () -> assertThat(department.getIsDeleted()).isTrue(),
                () -> assertThat(department.getDeletedAt()).isEqualTo(deletionTime),
                () -> assertThat(department.getDeletedBy()).isEqualTo(deletedBy)
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
        Department department = Department.builder()
                .name("Audit Dept")
                .code("AUD")
                .createdAt(createdTime)
                .updatedAt(updatedTime)
                .createdBy(createdBy)
                .updatedBy(updatedBy)
                .build();

        // Then
        assertAll(
                () -> assertThat(department.getCreatedAt()).isEqualTo(createdTime),
                () -> assertThat(department.getUpdatedAt()).isEqualTo(updatedTime),
                () -> assertThat(department.getCreatedBy()).isEqualTo(createdBy),
                () -> assertThat(department.getUpdatedBy()).isEqualTo(updatedBy)
        );
    }

    @Test
    @DisplayName("Should handle department updates")
    void shouldHandleDepartmentUpdates() {
        // Given
        Department department = Department.builder()
                .name("Original Name")
                .code("ORIG")
                .description("Original description")
                .email("original@example.com")
                .build();

        // When
        department.setName("Updated Name");
        department.setDescription("Updated description");
        department.setEmail("updated@example.com");

        // Then
        assertAll(
                () -> assertThat(department.getName()).isEqualTo("Updated Name"),
                () -> assertThat(department.getDescription()).isEqualTo("Updated description"),
                () -> assertThat(department.getEmail()).isEqualTo("updated@example.com")
        );
    }

    @Test
    @DisplayName("Should handle department deactivation")
    void shouldHandleDepartmentDeactivation() {
        // Given
        Department department = Department.builder()
                .name("Active Dept")
                .code("ACT")
                .isActive(true)
                .build();

        // When
        department.setIsActive(false);

        // Then
        assertThat(department.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle parent department relationship")
    void shouldHandleParentDepartmentRelationship() {
        // Given
        Department parentDepartment = Department.builder()
                .id(UUID.randomUUID())
                .name("Parent Department")
                .code("PARENT")
                .isActive(true)
                .build();

        Department childDepartment = Department.builder()
                .name("Child Department")
                .code("CHILD")
                .isActive(true)
                .build();

        // When
        childDepartment.setParentDepartment(parentDepartment);

        // Then
        assertAll(
                () -> assertThat(childDepartment.getParentDepartment()).isNotNull(),
                () -> assertThat(childDepartment.getParentDepartment().getName()).isEqualTo("Parent Department"),
                () -> assertThat(childDepartment.getParentDepartment().getCode()).isEqualTo("PARENT")
        );
    }

    @Test
    @DisplayName("Should handle manager relationship")
    void shouldHandleManagerRelationship() {
        // Given
        Employee manager = TestFixtures.createTestEmployee();
        Department department = Department.builder()
                .name("Managed Dept")
                .code("MGD")
                .isActive(true)
                .build();

        // When
        department.setManager(manager);

        // Then
        assertAll(
                () -> assertThat(department.getManager()).isNotNull(),
                () -> assertThat(department.getManager().getFullName()).isEqualTo("Nguyen Van A"),
                () -> assertThat(department.getManager().getEmployeeCode()).isEqualTo("EMP001")
        );
    }
}
