package com.hainam.worksphere.relative.domain;

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

@DisplayName("Relative Domain Tests")
class RelativeTest extends BaseUnitTest {

    @Test
    @DisplayName("Should create relative with builder pattern")
    void shouldCreateRelativeWithBuilderPattern() {
        // Given
        UUID id = UUID.randomUUID();
        Employee employee = TestFixtures.createTestEmployee();
        String fullName = "Nguyen Thi B";
        LocalDate dateOfBirth = LocalDate.of(1965, 5, 20);
        String phone = "0901234568";
        String idCardNumber = "012345678901";
        String occupation = "Teacher";
        String address = "123 Nguyen Hue, District 1, HCMC";
        Instant now = Instant.now();

        // When
        Relative relative = Relative.builder()
                .id(id)
                .employee(employee)
                .fullName(fullName)
                .relationship(RelationshipType.MOTHER)
                .dateOfBirth(dateOfBirth)
                .phone(phone)
                .idCardNumber(idCardNumber)
                .occupation(occupation)
                .address(address)
                .isEmergencyContact(false)
                .isDependent(false)
                .isDeleted(false)
                .createdAt(now)
                .build();

        // Then
        assertAll(
                () -> assertThat(relative.getId()).isEqualTo(id),
                () -> assertThat(relative.getEmployee()).isEqualTo(employee),
                () -> assertThat(relative.getFullName()).isEqualTo(fullName),
                () -> assertThat(relative.getRelationship()).isEqualTo(RelationshipType.MOTHER),
                () -> assertThat(relative.getDateOfBirth()).isEqualTo(dateOfBirth),
                () -> assertThat(relative.getPhone()).isEqualTo(phone),
                () -> assertThat(relative.getIdCardNumber()).isEqualTo(idCardNumber),
                () -> assertThat(relative.getOccupation()).isEqualTo(occupation),
                () -> assertThat(relative.getAddress()).isEqualTo(address),
                () -> assertThat(relative.getIsEmergencyContact()).isFalse(),
                () -> assertThat(relative.getIsDependent()).isFalse(),
                () -> assertThat(relative.getIsDeleted()).isFalse(),
                () -> assertThat(relative.getCreatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("Should create relative with default values")
    void shouldCreateRelativeWithDefaultValues() {
        // When
        Relative relative = Relative.builder()
                .employee(TestFixtures.createTestEmployee())
                .fullName("Default Relative")
                .relationship(RelationshipType.SPOUSE)
                .build();

        // Then
        assertAll(
                () -> assertThat(relative.getIsEmergencyContact()).isFalse(), // Default value
                () -> assertThat(relative.getIsDependent()).isFalse(),        // Default value
                () -> assertThat(relative.getIsDeleted()).isFalse()           // Default value
        );
    }

    @Test
    @DisplayName("Should create relative with no args constructor")
    void shouldCreateRelativeWithNoArgsConstructor() {
        // When
        Relative relative = new Relative();

        // Then
        assertThat(relative).isNotNull();
    }

    @Test
    @DisplayName("Should handle relationship type enum")
    void shouldHandleRelationshipTypeEnum() {
        // Given
        Relative relative = Relative.builder()
                .employee(TestFixtures.createTestEmployee())
                .fullName("Enum Test")
                .build();

        // When & Then - All 6 relationship types
        relative.setRelationship(RelationshipType.FATHER);
        assertThat(relative.getRelationship()).isEqualTo(RelationshipType.FATHER);

        relative.setRelationship(RelationshipType.MOTHER);
        assertThat(relative.getRelationship()).isEqualTo(RelationshipType.MOTHER);

        relative.setRelationship(RelationshipType.SPOUSE);
        assertThat(relative.getRelationship()).isEqualTo(RelationshipType.SPOUSE);

        relative.setRelationship(RelationshipType.CHILD);
        assertThat(relative.getRelationship()).isEqualTo(RelationshipType.CHILD);

        relative.setRelationship(RelationshipType.SIBLING);
        assertThat(relative.getRelationship()).isEqualTo(RelationshipType.SIBLING);

        relative.setRelationship(RelationshipType.OTHER);
        assertThat(relative.getRelationship()).isEqualTo(RelationshipType.OTHER);
    }

    @Test
    @DisplayName("Should handle emergency contact")
    void shouldHandleEmergencyContact() {
        // Given
        Relative relative = Relative.builder()
                .employee(TestFixtures.createTestEmployee())
                .fullName("Emergency Contact")
                .relationship(RelationshipType.SPOUSE)
                .phone("0901234567")
                .isEmergencyContact(false)
                .build();

        // When
        relative.setIsEmergencyContact(true);

        // Then
        assertThat(relative.getIsEmergencyContact()).isTrue();
    }

    @Test
    @DisplayName("Should handle dependent")
    void shouldHandleDependent() {
        // Given
        Relative relative = Relative.builder()
                .employee(TestFixtures.createTestEmployee())
                .fullName("Dependent Child")
                .relationship(RelationshipType.CHILD)
                .dateOfBirth(LocalDate.of(2015, 6, 15))
                .isDependent(false)
                .build();

        // When
        relative.setIsDependent(true);

        // Then
        assertThat(relative.getIsDependent()).isTrue();
    }

    @Test
    @DisplayName("Should handle soft deletion fields")
    void shouldHandleSoftDeletionFields() {
        // Given
        Instant deletionTime = Instant.now();
        UUID deletedBy = UUID.randomUUID();

        // When
        Relative relative = Relative.builder()
                .employee(TestFixtures.createTestEmployee())
                .fullName("Deleted Relative")
                .relationship(RelationshipType.OTHER)
                .isDeleted(true)
                .deletedAt(deletionTime)
                .deletedBy(deletedBy)
                .build();

        // Then
        assertAll(
                () -> assertThat(relative.getIsDeleted()).isTrue(),
                () -> assertThat(relative.getDeletedAt()).isEqualTo(deletionTime),
                () -> assertThat(relative.getDeletedBy()).isEqualTo(deletedBy)
        );
    }
}
