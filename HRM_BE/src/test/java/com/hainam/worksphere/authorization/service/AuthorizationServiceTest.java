package com.hainam.worksphere.authorization.service;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@DisplayName("AuthorizationService Tests")
class AuthorizationServiceTest extends BaseUnitTest {

    @Mock
    private RoleService roleService;

    @Mock
    private PermissionService permissionService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private RolePermissionService rolePermissionService;

    @InjectMocks
    private AuthorizationService authorizationService;

    private UUID testUserId;
    private Role testRole;
    private Permission testPermission;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testRole = TestFixtures.createTestRole();
        testPermission = TestFixtures.createTestPermission();
    }

    @Test
    @DisplayName("Should check if user has permission successfully")
    void shouldCheckIfUserHasPermissionSuccessfully() {
        // Given
        String permissionCode = testPermission.getCode();
        when(permissionService.getPermissionsByUserId(testUserId)).thenReturn(Arrays.asList(testPermission));

        // When
        boolean hasPermission = authorizationService.hasPermission(testUserId, permissionCode);

        // Then
        assertAll(
            () -> assertThat(hasPermission).isTrue(),
            () -> verify(permissionService).getPermissionsByUserId(testUserId)
        );
    }

    @Test
    @DisplayName("Should return false when user does not have permission")
    void shouldReturnFalseWhenUserDoesNotHavePermission() {
        // Given
        String nonExistentPermission = "NON_EXISTENT_PERMISSION";
        when(permissionService.getPermissionsByUserId(testUserId)).thenReturn(Arrays.asList(testPermission));

        // When
        boolean hasPermission = authorizationService.hasPermission(testUserId, nonExistentPermission);

        // Then
        assertAll(
            () -> assertThat(hasPermission).isFalse(),
            () -> verify(permissionService).getPermissionsByUserId(testUserId)
        );
    }

    @Test
    @DisplayName("Should check if user has any of required permissions")
    void shouldCheckIfUserHasAnyOfRequiredPermissions() {
        // Given
        String[] requiredPermissions = {"READ_USER", "WRITE_USER", testPermission.getCode()};
        when(permissionService.getPermissionsByUserId(testUserId)).thenReturn(Arrays.asList(testPermission));

        // When
        boolean hasAnyPermission = authorizationService.hasAnyPermission(testUserId, requiredPermissions);

        // Then
        assertAll(
            () -> assertThat(hasAnyPermission).isTrue(),
            () -> verify(permissionService).getPermissionsByUserId(testUserId)
        );
    }

    @Test
    @DisplayName("Should return false when user has none of required permissions")
    void shouldReturnFalseWhenUserHasNoneOfRequiredPermissions() {
        // Given
        String[] requiredPermissions = {"ADMIN_PERMISSION", "SUPER_ADMIN_PERMISSION"};
        when(permissionService.getPermissionsByUserId(testUserId)).thenReturn(Arrays.asList(testPermission));

        // When
        boolean hasAnyPermission = authorizationService.hasAnyPermission(testUserId, requiredPermissions);

        // Then
        assertAll(
            () -> assertThat(hasAnyPermission).isFalse(),
            () -> verify(permissionService).getPermissionsByUserId(testUserId)
        );
    }

    @Test
    @DisplayName("Should check if user has all required permissions")
    void shouldCheckIfUserHasAllRequiredPermissions() {
        // Given
        Permission permission1 = Permission.builder()
                .code("READ_USER")
                .displayName("Read User")
                .resource("USER")
                .action("READ")
                .build();
        Permission permission2 = Permission.builder()
                .code("WRITE_USER")
                .displayName("Write User")
                .resource("USER")
                .action("WRITE")
                .build();

        String[] requiredPermissions = {"READ_USER", "WRITE_USER"};
        when(permissionService.getPermissionsByUserId(testUserId))
                .thenReturn(Arrays.asList(permission1, permission2));

        // When
        boolean hasAllPermissions = authorizationService.hasAllPermissions(testUserId, requiredPermissions);

        // Then
        assertAll(
            () -> assertThat(hasAllPermissions).isTrue(),
            () -> verify(permissionService).getPermissionsByUserId(testUserId)
        );
    }

    @Test
    @DisplayName("Should return false when user does not have all required permissions")
    void shouldReturnFalseWhenUserDoesNotHaveAllRequiredPermissions() {
        // Given
        String[] requiredPermissions = {"READ_USER", "ADMIN_PERMISSION"};
        when(permissionService.getPermissionsByUserId(testUserId)).thenReturn(Arrays.asList(testPermission));

        // When
        boolean hasAllPermissions = authorizationService.hasAllPermissions(testUserId, requiredPermissions);

        // Then
        assertAll(
            () -> assertThat(hasAllPermissions).isFalse(),
            () -> verify(permissionService).getPermissionsByUserId(testUserId)
        );
    }

    @Test
    @DisplayName("Should check if user has role successfully")
    void shouldCheckIfUserHasRoleSuccessfully() {
        // Given
        String roleCode = testRole.getCode();
        when(userRoleService.userHasRole(testUserId, roleCode)).thenReturn(true);

        // When
        boolean hasRole = authorizationService.hasRole(testUserId, roleCode);

        // Then
        assertAll(
            () -> assertThat(hasRole).isTrue(),
            () -> verify(userRoleService).userHasRole(testUserId, roleCode)
        );
    }

    @Test
    @DisplayName("Should return false when user does not have role")
    void shouldReturnFalseWhenUserDoesNotHaveRole() {
        // Given
        String nonExistentRole = "NON_EXISTENT_ROLE";
        when(userRoleService.userHasRole(testUserId, nonExistentRole)).thenReturn(false);

        // When
        boolean hasRole = authorizationService.hasRole(testUserId, nonExistentRole);

        // Then
        assertAll(
            () -> assertThat(hasRole).isFalse(),
            () -> verify(userRoleService).userHasRole(testUserId, nonExistentRole)
        );
    }

    @Test
    @DisplayName("Should check if user has any of required roles")
    void shouldCheckIfUserHasAnyOfRequiredRoles() {
        // Given
        String[] requiredRoles = {"ADMIN", "SUPER_ADMIN", testRole.getCode()};
        when(userRoleService.userHasRole(testUserId, "ADMIN")).thenReturn(false);
        when(userRoleService.userHasRole(testUserId, "SUPER_ADMIN")).thenReturn(false);
        when(userRoleService.userHasRole(testUserId, testRole.getCode())).thenReturn(true);

        // When
        boolean hasAnyRole = authorizationService.hasAnyRole(testUserId, requiredRoles);

        // Then
        assertAll(
            () -> assertThat(hasAnyRole).isTrue(),
            () -> verify(userRoleService).userHasRole(testUserId, testRole.getCode())
        );
    }

    @Test
    @DisplayName("Should return false when user has none of required roles")
    void shouldReturnFalseWhenUserHasNoneOfRequiredRoles() {
        // Given
        String[] requiredRoles = {"ADMIN", "SUPER_ADMIN"};
        when(userRoleService.userHasRole(testUserId, "ADMIN")).thenReturn(false);
        when(userRoleService.userHasRole(testUserId, "SUPER_ADMIN")).thenReturn(false);

        // When
        boolean hasAnyRole = authorizationService.hasAnyRole(testUserId, requiredRoles);

        // Then
        assertAll(
            () -> assertThat(hasAnyRole).isFalse(),
            () -> verify(userRoleService).userHasRole(testUserId, "ADMIN"),
            () -> verify(userRoleService).userHasRole(testUserId, "SUPER_ADMIN")
        );
    }

    @Test
    @DisplayName("Should get user permissions successfully")
    void shouldGetUserPermissionsSuccessfully() {
        // Given
        List<Permission> permissions = Arrays.asList(testPermission);
        when(permissionService.getPermissionsByUserId(testUserId)).thenReturn(permissions);

        // When
        List<Permission> userPermissions = authorizationService.getUserPermissions(testUserId);

        // Then
        assertAll(
            () -> assertThat(userPermissions).hasSize(1),
            () -> assertThat(userPermissions).contains(testPermission),
            () -> verify(permissionService).getPermissionsByUserId(testUserId)
        );
    }

    @Test
    @DisplayName("Should get user roles successfully")
    void shouldGetUserRolesSuccessfully() {
        // Given
        List<Role> roles = Arrays.asList(testRole);
        when(roleService.getRolesByUserId(testUserId)).thenReturn(roles);

        // When
        List<Role> userRoles = authorizationService.getUserRoles(testUserId);

        // Then
        assertAll(
            () -> assertThat(userRoles).hasSize(1),
            () -> assertThat(userRoles).contains(testRole),
            () -> verify(roleService).getRolesByUserId(testUserId)
        );
    }

    @Test
    @DisplayName("Should return empty list when user has no permissions")
    void shouldReturnEmptyListWhenUserHasNoPermissions() {
        // Given
        when(permissionService.getPermissionsByUserId(testUserId)).thenReturn(Collections.emptyList());

        // When
        List<Permission> userPermissions = authorizationService.getUserPermissions(testUserId);

        // Then
        assertAll(
            () -> assertThat(userPermissions).isEmpty(),
            () -> verify(permissionService).getPermissionsByUserId(testUserId)
        );
    }

    @Test
    @DisplayName("Should return empty list when user has no roles")
    void shouldReturnEmptyListWhenUserHasNoRoles() {
        // Given
        when(roleService.getRolesByUserId(testUserId)).thenReturn(Collections.emptyList());

        // When
        List<Role> userRoles = authorizationService.getUserRoles(testUserId);

        // Then
        assertAll(
            () -> assertThat(userRoles).isEmpty(),
            () -> verify(roleService).getRolesByUserId(testUserId)
        );
    }

    @Test
    @DisplayName("Should check if user is admin")
    void shouldCheckIfUserIsAdmin() {
        // Given
        when(userRoleService.userHasRole(testUserId, "ADMIN")).thenReturn(true);

        // When
        boolean isAdmin = authorizationService.isAdmin(testUserId);

        // Then
        assertAll(
            () -> assertThat(isAdmin).isTrue(),
            () -> verify(userRoleService).userHasRole(testUserId, "ADMIN")
        );
    }

    @Test
    @DisplayName("Should return false when user is not admin")
    void shouldReturnFalseWhenUserIsNotAdmin() {
        // Given
        when(userRoleService.userHasRole(testUserId, "ADMIN")).thenReturn(false);
        when(userRoleService.userHasRole(testUserId, "SUPER_ADMIN")).thenReturn(false);

        // When
        boolean isAdmin = authorizationService.isAdmin(testUserId);

        // Then
        assertAll(
            () -> assertThat(isAdmin).isFalse(),
            () -> verify(userRoleService).userHasRole(testUserId, "ADMIN"),
            () -> verify(userRoleService).userHasRole(testUserId, "SUPER_ADMIN")
        );
    }

    @Test
    @DisplayName("Should check if user is super admin")
    void shouldCheckIfUserIsSuperAdmin() {
        // Given
        when(userRoleService.userHasRole(testUserId, "SUPER_ADMIN")).thenReturn(true);

        // When
        boolean isSuperAdmin = authorizationService.isSuperAdmin(testUserId);

        // Then
        assertAll(
            () -> assertThat(isSuperAdmin).isTrue(),
            () -> verify(userRoleService).userHasRole(testUserId, "SUPER_ADMIN")
        );
    }

    @Test
    @DisplayName("Should return false when user is not super admin")
    void shouldReturnFalseWhenUserIsNotSuperAdmin() {
        // Given
        when(userRoleService.userHasRole(testUserId, "SUPER_ADMIN")).thenReturn(false);

        // When
        boolean isSuperAdmin = authorizationService.isSuperAdmin(testUserId);

        // Then
        assertAll(
            () -> assertThat(isSuperAdmin).isFalse(),
            () -> verify(userRoleService).userHasRole(testUserId, "SUPER_ADMIN")
        );
    }

    @Test
    @DisplayName("Should handle null user ID gracefully")
    void shouldHandleNullUserIdGracefully() {
        // Given
        UUID userId = null;
        String permissionCode = "READ_USER";
        when(permissionService.getPermissionsByUserId(userId)).thenReturn(Collections.emptyList());

        // When
        boolean hasPermission = authorizationService.hasPermission(userId, permissionCode);

        // Then
        assertAll(
            () -> assertThat(hasPermission).isFalse(),
            () -> verify(permissionService).getPermissionsByUserId(userId)
        );
    }

    @Test
    @DisplayName("Should handle empty permission arrays")
    void shouldHandleEmptyPermissionArrays() {
        // Given
        String[] emptyPermissions = {};
        when(permissionService.getPermissionsByUserId(testUserId)).thenReturn(Arrays.asList(testPermission));

        // When
        boolean hasAnyPermission = authorizationService.hasAnyPermission(testUserId, emptyPermissions);
        boolean hasAllPermissions = authorizationService.hasAllPermissions(testUserId, emptyPermissions);

        // Then
        assertAll(
            () -> assertThat(hasAnyPermission).isFalse(),
            () -> assertThat(hasAllPermissions).isTrue(),
            () -> verify(permissionService, times(2)).getPermissionsByUserId(testUserId)
        );
    }
}
