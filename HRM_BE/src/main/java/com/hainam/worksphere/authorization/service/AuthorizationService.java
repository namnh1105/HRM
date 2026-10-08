package com.hainam.worksphere.authorization.service;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final RoleService roleService;
    private final PermissionService permissionService;
    private final UserRoleService userRoleService;
    private final RolePermissionService rolePermissionService;

    public boolean hasPermission(UUID userId, String permissionCode) {
        List<Permission> userPermissions = permissionService.getPermissionsByUserId(userId);
        return userPermissions.stream()
                .anyMatch(permission -> permission.getCode().equals(permissionCode));
    }

    public boolean hasAnyPermission(UUID userId, String... permissionCodes) {
        List<Permission> userPermissions = permissionService.getPermissionsByUserId(userId);
        Set<String> userPermissionCodes = userPermissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        for (String permissionCode : permissionCodes) {
            if (userPermissionCodes.contains(permissionCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllPermissions(UUID userId, String... permissionCodes) {
        List<Permission> userPermissions = permissionService.getPermissionsByUserId(userId);
        Set<String> userPermissionCodes = userPermissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        for (String permissionCode : permissionCodes) {
            if (!userPermissionCodes.contains(permissionCode)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasRole(UUID userId, String roleCode) {
        return userRoleService.userHasRole(userId, roleCode);
    }

    public boolean hasAnyRole(UUID userId, String... roleCodes) {
        for (String roleCode : roleCodes) {
            if (userRoleService.userHasRole(userId, roleCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllRoles(UUID userId, String... roleCodes) {
        for (String roleCode : roleCodes) {
            if (!userRoleService.userHasRole(userId, roleCode)) {
                return false;
            }
        }
        return true;
    }

    public List<Permission> getUserPermissions(UUID userId) {
        return permissionService.getPermissionsByUserId(userId);
    }

    public List<Role> getUserRoles(UUID userId) {
        return roleService.getRolesByUserId(userId);
    }

    public Set<String> getUserPermissionCodes(UUID userId) {
        return permissionService.getPermissionsByUserId(userId).stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    public Set<String> getUserRoleCodes(UUID userId) {
        return roleService.getRolesByUserId(userId).stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
    }

    public boolean canAccess(UUID userId, String resource, String action) {
        List<Permission> userPermissions = permissionService.getPermissionsByUserId(userId);
        return userPermissions.stream()
                .anyMatch(permission ->
                    permission.getResource().equals(resource) &&
                    permission.getAction().equals(action));
    }

    public boolean canAccessResource(UUID userId, String resource) {
        List<Permission> userPermissions = permissionService.getPermissionsByUserId(userId);
        return userPermissions.stream()
                .anyMatch(permission -> permission.getResource().equals(resource));
    }

    public boolean isAdmin(UUID userId) {
        return hasRole(userId, "ADMIN") || hasRole(userId, "SUPER_ADMIN");
    }


    public boolean isSuperAdmin(UUID userId) {
        return hasRole(userId, "SUPER_ADMIN");
    }

    public boolean canManageUsers(UUID userId) {
        return hasAnyPermission(userId, "user:create", "user:update", "user:delete") ||
               hasRole(userId, "USER_MANAGER");
    }

    public boolean canManageRoles(UUID userId) {
        return hasAnyPermission(userId, "role:create", "role:update", "role:delete") ||
               isAdmin(userId);
    }

    public boolean canManagePermissions(UUID userId) {
        return hasAnyPermission(userId, "permission:create", "permission:update", "permission:delete") ||
               isSuperAdmin(userId);
    }

    public void validateAccess(UUID userId, String requiredPermission) {
        if (!hasPermission(userId, requiredPermission)) {
            throw new SecurityException("Access denied: User does not have required permission: " + requiredPermission);
        }
    }

    public void validateRole(UUID userId, String requiredRole) {
        if (!hasRole(userId, requiredRole)) {
            throw new SecurityException("Access denied: User does not have required role: " + requiredRole);
        }
    }

    public void validateResourceAccess(UUID userId, String resource, String action) {
        if (!canAccess(userId, resource, action)) {
            throw new SecurityException("Access denied: User cannot perform " + action + " on " + resource);
        }
    }
}
