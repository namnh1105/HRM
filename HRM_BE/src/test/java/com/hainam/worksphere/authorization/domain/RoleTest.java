package com.hainam.worksphere.authorization.domain;

import com.hainam.worksphere.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Role Domain Tests")
class RoleTest extends BaseUnitTest {

    private UUID roleId;
    private Instant createdAt;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        createdAt = Instant.now();
    }

    @Test
    @DisplayName("Should create role with builder pattern")
    void shouldCreateRoleWithBuilderPattern() {
        // When
        Role role = Role.builder()
                .id(roleId)
                .code("ADMIN")
                .displayName("Administrator")
                .description("System administrator role")
                .isSystem(false)
                .isActive(true)
                .createdAt(createdAt)
                .build();

        // Then
        assertAll(
                () -> assertThat(role.getId()).isEqualTo(roleId),
                () -> assertThat(role.getCode()).isEqualTo("ADMIN"),
                () -> assertThat(role.getDisplayName()).isEqualTo("Administrator"),
                () -> assertThat(role.getDescription()).isEqualTo("System administrator role"),
                () -> assertThat(role.getIsSystem()).isFalse(),
                () -> assertThat(role.getIsActive()).isTrue(),
                () -> assertThat(role.getCreatedAt()).isEqualTo(createdAt)
        );
    }

    @Test
    @DisplayName("Should create role with default values")
    void shouldCreateRoleWithDefaultValues() {
        // When
        Role role = Role.builder()
                .code("USER")
                .displayName("User")
                .build();

        // Then
        assertAll(
                () -> assertThat(role.getCode()).isEqualTo("USER"),
                () -> assertThat(role.getDisplayName()).isEqualTo("User"),
                () -> assertThat(role.getIsSystem()).isFalse(), // Default value
                () -> assertThat(role.getIsActive()).isTrue()   // Default value
        );
    }

    @Test
    @DisplayName("Should create role with no args constructor")
    void shouldCreateRoleWithNoArgsConstructor() {
        // When
        Role role = new Role();

        // Then
        assertThat(role).isNotNull();
    }

    @Test
    @DisplayName("Should handle system role")
    void shouldHandleSystemRole() {
        // When
        Role systemRole = Role.builder()
                .code("SYSTEM_ADMIN")
                .displayName("System Administrator")
                .isSystem(true)
                .isActive(true)
                .build();

        // Then
        assertAll(
                () -> assertThat(systemRole.getIsSystem()).isTrue(),
                () -> assertThat(systemRole.getIsActive()).isTrue()
        );
    }

    @Test
    @DisplayName("Should handle inactive role")
    void shouldHandleInactiveRole() {
        // When
        Role inactiveRole = Role.builder()
                .code("TEMP_USER")
                .displayName("Temporary User")
                .isActive(false)
                .build();

        // Then
        assertThat(inactiveRole.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle role permissions relationship")
    void shouldHandleRolePermissionsRelationship() {
        // Given
        Set<RolePermission> permissions = new HashSet<>();

        // When
        Role role = Role.builder()
                .code("EDITOR")
                .displayName("Editor")
                .rolePermissions(permissions)
                .build();

        // Then
        assertThat(role.getRolePermissions()).isEqualTo(permissions);
    }

    @Test
    @DisplayName("Should handle empty role permissions")
    void shouldHandleEmptyRolePermissions() {
        // When
        Role role = Role.builder()
                .code("VIEWER")
                .displayName("Viewer")
                .build();

        // Then
        assertThat(role.getRolePermissions()).isNotNull();
    }

    @Test
    @DisplayName("Should handle role updates")
    void shouldHandleRoleUpdates() {
        // Given
        Role role = Role.builder()
                .code("USER")
                .displayName("User")
                .description("Basic user role")
                .isActive(true)
                .build();

        Instant updateTime = Instant.now();

        // When
        role.setDisplayName("Standard User");
        role.setDescription("Standard user with basic permissions");
        role.setUpdatedAt(updateTime);

        // Then
        assertAll(
                () -> assertThat(role.getDisplayName()).isEqualTo("Standard User"),
                () -> assertThat(role.getDescription()).isEqualTo("Standard user with basic permissions"),
                () -> assertThat(role.getUpdatedAt()).isEqualTo(updateTime)
        );
    }

    @Test
    @DisplayName("Should handle role deactivation")
    void shouldHandleRoleDeactivation() {
        // Given
        Role role = Role.builder()
                .code("TEMP_ROLE")
                .displayName("Temporary Role")
                .isActive(true)
                .build();

        // When
        role.setIsActive(false);

        // Then
        assertThat(role.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle long text values")
    void shouldHandleLongTextValues() {
        // Given
        String longDescription = "This is a very long description ".repeat(10);

        // When
        Role role = Role.builder()
                .code("LONG_DESC_ROLE")
                .displayName("Role with Long Description")
                .description(longDescription)
                .build();

        // Then
        assertThat(role.getDescription()).isEqualTo(longDescription);
    }

    @Test
    @DisplayName("Should handle null description")
    void shouldHandleNullDescription() {
        // When
        Role role = Role.builder()
                .code("NO_DESC_ROLE")
                .displayName("Role without Description")
                .description(null)
                .build();

        // Then
        assertThat(role.getDescription()).isNull();
    }

    @Test
    @DisplayName("Should handle timestamp fields")
    void shouldHandleTimestampFields() {
        // Given
        Instant createdAt = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant updatedAt = Instant.now();

        // When
        Role role = Role.builder()
                .code("TIME_ROLE")
                .displayName("Timed Role")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // Then
        assertAll(
                () -> assertThat(role.getCreatedAt()).isEqualTo(createdAt),
                () -> assertThat(role.getUpdatedAt()).isEqualTo(updatedAt)
        );
    }

    @Test
    @DisplayName("Should handle role code uniqueness")
    void shouldHandleRoleCodeUniqueness() {
        // When
        Role role1 = Role.builder()
                .code("UNIQUE_ROLE")
                .displayName("First Role")
                .build();

        Role role2 = Role.builder()
                .code("UNIQUE_ROLE")
                .displayName("Second Role")
                .build();

        // Then
        assertAll(
                () -> assertThat(role1.getCode()).isEqualTo("UNIQUE_ROLE"),
                () -> assertThat(role2.getCode()).isEqualTo("UNIQUE_ROLE"),
                () -> assertThat(role1.getDisplayName()).isNotEqualTo(role2.getDisplayName())
        );
    }

    @Test
    @DisplayName("Should handle various role codes")
    void shouldHandleVariousRoleCodes() {
        // When
        Role adminRole = Role.builder()
                .code("ADMIN")
                .displayName("Administrator")
                .build();

        Role userRole = Role.builder()
                .code("USER")
                .displayName("Regular User")
                .build();

        Role managerRole = Role.builder()
                .code("MANAGER")
                .displayName("Manager")
                .build();

        // Then
        assertAll(
                () -> assertThat(adminRole.getCode()).isEqualTo("ADMIN"),
                () -> assertThat(userRole.getCode()).isEqualTo("USER"),
                () -> assertThat(managerRole.getCode()).isEqualTo("MANAGER")
        );
    }
}
