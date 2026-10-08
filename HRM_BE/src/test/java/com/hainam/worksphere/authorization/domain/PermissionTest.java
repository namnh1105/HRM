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

@DisplayName("Permission Domain Tests")
class PermissionTest extends BaseUnitTest {

    private UUID permissionId;
    private Instant createdAt;

    @BeforeEach
    void setUp() {
        permissionId = UUID.randomUUID();
        createdAt = Instant.now();
    }

    @Test
    @DisplayName("Should create permission with builder pattern")
    void shouldCreatePermissionWithBuilderPattern() {
        // When
        Permission permission = Permission.builder()
                .id(permissionId)
                .code("USER_READ")
                .displayName("Read Users")
                .description("Permission to read user data")
                .resource("USER")
                .action("READ")
                .isSystem(false)
                .isActive(true)
                .createdAt(createdAt)
                .build();

        // Then
        assertAll(
                () -> assertThat(permission.getId()).isEqualTo(permissionId),
                () -> assertThat(permission.getCode()).isEqualTo("USER_READ"),
                () -> assertThat(permission.getDisplayName()).isEqualTo("Read Users"),
                () -> assertThat(permission.getDescription()).isEqualTo("Permission to read user data"),
                () -> assertThat(permission.getResource()).isEqualTo("USER"),
                () -> assertThat(permission.getAction()).isEqualTo("READ"),
                () -> assertThat(permission.getIsSystem()).isFalse(),
                () -> assertThat(permission.getIsActive()).isTrue(),
                () -> assertThat(permission.getCreatedAt()).isEqualTo(createdAt)
        );
    }

    @Test
    @DisplayName("Should create permission with default values")
    void shouldCreatePermissionWithDefaultValues() {
        // When
        Permission permission = Permission.builder()
                .code("DEFAULT_PERMISSION")
                .displayName("Default Permission")
                .resource("RESOURCE")
                .action("ACTION")
                .build();

        // Then
        assertAll(
                () -> assertThat(permission.getCode()).isEqualTo("DEFAULT_PERMISSION"),
                () -> assertThat(permission.getDisplayName()).isEqualTo("Default Permission"),
                () -> assertThat(permission.getResource()).isEqualTo("RESOURCE"),
                () -> assertThat(permission.getAction()).isEqualTo("ACTION"),
                () -> assertThat(permission.getIsSystem()).isFalse(), // Default value
                () -> assertThat(permission.getIsActive()).isTrue()   // Default value
        );
    }

    @Test
    @DisplayName("Should create permission with no args constructor")
    void shouldCreatePermissionWithNoArgsConstructor() {
        // When
        Permission permission = new Permission();

        // Then
        assertThat(permission).isNotNull();
    }

    @Test
    @DisplayName("Should handle system permission")
    void shouldHandleSystemPermission() {
        // When
        Permission systemPermission = Permission.builder()
                .code("SYSTEM_ADMIN")
                .displayName("System Administration")
                .resource("SYSTEM")
                .action("ADMIN")
                .isSystem(true)
                .isActive(true)
                .build();

        // Then
        assertAll(
                () -> assertThat(systemPermission.getIsSystem()).isTrue(),
                () -> assertThat(systemPermission.getIsActive()).isTrue()
        );
    }

    @Test
    @DisplayName("Should handle inactive permission")
    void shouldHandleInactivePermission() {
        // When
        Permission inactivePermission = Permission.builder()
                .code("DEPRECATED_PERMISSION")
                .displayName("Deprecated Permission")
                .resource("OLD_RESOURCE")
                .action("OLD_ACTION")
                .isActive(false)
                .build();

        // Then
        assertThat(inactivePermission.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle different resource types")
    void shouldHandleDifferentResourceTypes() {
        // When
        Permission userPermission = Permission.builder()
                .code("USER_MANAGE")
                .resource("USER")
                .action("MANAGE")
                .build();

        Permission rolePermission = Permission.builder()
                .code("ROLE_ASSIGN")
                .resource("ROLE")
                .action("ASSIGN")
                .build();

        Permission systemPermission = Permission.builder()
                .code("SYSTEM_CONFIG")
                .resource("SYSTEM")
                .action("CONFIGURE")
                .build();

        // Then
        assertAll(
                () -> assertThat(userPermission.getResource()).isEqualTo("USER"),
                () -> assertThat(rolePermission.getResource()).isEqualTo("ROLE"),
                () -> assertThat(systemPermission.getResource()).isEqualTo("SYSTEM")
        );
    }

    @Test
    @DisplayName("Should handle different action types")
    void shouldHandleDifferentActionTypes() {
        // When
        Permission readPermission = Permission.builder()
                .code("READ_PERMISSION")
                .resource("DATA")
                .action("READ")
                .build();

        Permission writePermission = Permission.builder()
                .code("WRITE_PERMISSION")
                .resource("DATA")
                .action("WRITE")
                .build();

        Permission deletePermission = Permission.builder()
                .code("DELETE_PERMISSION")
                .resource("DATA")
                .action("DELETE")
                .build();

        // Then
        assertAll(
                () -> assertThat(readPermission.getAction()).isEqualTo("READ"),
                () -> assertThat(writePermission.getAction()).isEqualTo("WRITE"),
                () -> assertThat(deletePermission.getAction()).isEqualTo("DELETE")
        );
    }

    @Test
    @DisplayName("Should handle role permissions relationship")
    void shouldHandleRolePermissionsRelationship() {
        // Given
        Set<RolePermission> rolePermissions = new HashSet<>();

        // When
        Permission permission = Permission.builder()
                .code("MULTI_ROLE_PERMISSION")
                .displayName("Multi Role Permission")
                .resource("RESOURCE")
                .action("ACTION")
                .rolePermissions(rolePermissions)
                .build();

        // Then
        assertThat(permission.getRolePermissions()).isEqualTo(rolePermissions);
    }

    @Test
    @DisplayName("Should handle empty role permissions")
    void shouldHandleEmptyRolePermissions() {
        // When
        Permission permission = Permission.builder()
                .code("NO_ROLE_PERMISSION")
                .displayName("Permission without Roles")
                .resource("RESOURCE")
                .action("ACTION")
                .build();

        // Then
        assertThat(permission.getRolePermissions()).isNotNull();
    }

    @Test
    @DisplayName("Should handle permission updates")
    void shouldHandlePermissionUpdates() {
        // Given
        Permission permission = Permission.builder()
                .code("UPDATABLE_PERMISSION")
                .displayName("Original Name")
                .description("Original description")
                .resource("RESOURCE")
                .action("ACTION")
                .isActive(true)
                .build();

        Instant updateTime = Instant.now();

        // When
        permission.setDisplayName("Updated Name");
        permission.setDescription("Updated description");
        permission.setUpdatedAt(updateTime);

        // Then
        assertAll(
                () -> assertThat(permission.getDisplayName()).isEqualTo("Updated Name"),
                () -> assertThat(permission.getDescription()).isEqualTo("Updated description"),
                () -> assertThat(permission.getUpdatedAt()).isEqualTo(updateTime)
        );
    }

    @Test
    @DisplayName("Should handle permission deactivation")
    void shouldHandlePermissionDeactivation() {
        // Given
        Permission permission = Permission.builder()
                .code("DEACTIVATABLE_PERMISSION")
                .displayName("Deactivatable Permission")
                .resource("RESOURCE")
                .action("ACTION")
                .isActive(true)
                .build();

        // When
        permission.setIsActive(false);

        // Then
        assertThat(permission.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle null description")
    void shouldHandleNullDescription() {
        // When
        Permission permission = Permission.builder()
                .code("NO_DESC_PERMISSION")
                .displayName("Permission without Description")
                .resource("RESOURCE")
                .action("ACTION")
                .description(null)
                .build();

        // Then
        assertThat(permission.getDescription()).isNull();
    }

    @Test
    @DisplayName("Should handle timestamp fields")
    void shouldHandleTimestampFields() {
        // Given
        Instant createdAt = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant updatedAt = Instant.now();

        // When
        Permission permission = Permission.builder()
                .code("TIME_PERMISSION")
                .displayName("Timed Permission")
                .resource("RESOURCE")
                .action("ACTION")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // Then
        assertAll(
                () -> assertThat(permission.getCreatedAt()).isEqualTo(createdAt),
                () -> assertThat(permission.getUpdatedAt()).isEqualTo(updatedAt)
        );
    }

    @Test
    @DisplayName("Should handle complex permission codes")
    void shouldHandleComplexPermissionCodes() {
        // When
        Permission complexPermission = Permission.builder()
                .code("USER_PROFILE_UPDATE_ADMIN")
                .displayName("Admin User Profile Update")
                .description("Administrative permission to update user profiles")
                .resource("USER_PROFILE")
                .action("UPDATE_ADMIN")
                .build();

        // Then
        assertAll(
                () -> assertThat(complexPermission.getCode()).isEqualTo("USER_PROFILE_UPDATE_ADMIN"),
                () -> assertThat(complexPermission.getResource()).isEqualTo("USER_PROFILE"),
                () -> assertThat(complexPermission.getAction()).isEqualTo("UPDATE_ADMIN")
        );
    }

    @Test
    @DisplayName("Should handle various permission combinations")
    void shouldHandleVariousPermissionCombinations() {
        // When
        Permission readUser = Permission.builder()
                .code("READ_USER")
                .resource("USER")
                .action("READ")
                .build();

        Permission writeRole = Permission.builder()
                .code("WRITE_ROLE")
                .resource("ROLE")
                .action("WRITE")
                .build();

        Permission deletePermission = Permission.builder()
                .code("DELETE_PERMISSION")
                .resource("PERMISSION")
                .action("DELETE")
                .build();

        // Then
        assertAll(
                () -> assertThat(readUser.getCode()).isEqualTo("READ_USER"),
                () -> assertThat(writeRole.getCode()).isEqualTo("WRITE_ROLE"),
                () -> assertThat(deletePermission.getCode()).isEqualTo("DELETE_PERMISSION")
        );
    }
}
