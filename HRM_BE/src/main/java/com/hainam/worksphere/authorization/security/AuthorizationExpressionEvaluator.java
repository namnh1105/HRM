package com.hainam.worksphere.authorization.security;

import com.hainam.worksphere.authorization.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

/**
 * Custom PermissionEvaluator for handling authorization in @PreAuthorize annotations.
 * Integrates with our custom authorization system.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationExpressionEvaluator implements PermissionEvaluator {

    private final AuthorizationService authorizationService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        try {
            UUID userId = getUserIdFromAuthentication(authentication);
            String permissionName = permission.toString();

            log.debug("Evaluating permission: {} for user: {}", permissionName, userId);

            return authorizationService.hasPermission(userId, permissionName);
        } catch (Exception e) {
            log.error("Error evaluating permission", e);
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        try {
            UUID userId = getUserIdFromAuthentication(authentication);
            String permissionName = permission.toString();

            log.debug("Evaluating permission: {} for user: {} on target: {} ({})",
                     permissionName, userId, targetId, targetType);

            if (targetType != null && permissionName.contains(":")) {
                String[] parts = permissionName.split(":");
                if (parts.length == 2) {
                    String resource = parts[0];
                    String action = parts[1];
                    return authorizationService.canAccess(userId, resource, action);
                }
            }

            return authorizationService.hasPermission(userId, permissionName);
        } catch (Exception e) {
            log.error("Error evaluating permission with target", e);
            return false;
        }
    }

    public boolean hasRole(Authentication authentication, String roleName) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        try {
            UUID userId = getUserIdFromAuthentication(authentication);
            log.debug("Evaluating role: {} for user: {}", roleName, userId);

            return authorizationService.hasRole(userId, roleName);
        } catch (Exception e) {
            log.error("Error evaluating role", e);
            return false;
        }
    }

    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(Authentication authentication, String... roleNames) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        try {
            UUID userId = getUserIdFromAuthentication(authentication);
            log.debug("Evaluating any roles: {} for user: {}", String.join(", ", roleNames), userId);

            return authorizationService.hasAnyRole(userId, roleNames);
        } catch (Exception e) {
            log.error("Error evaluating any roles", e);
            return false;
        }
    }

    public boolean canAccess(Authentication authentication, String resource, String action) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        try {
            UUID userId = getUserIdFromAuthentication(authentication);
            log.debug("Evaluating resource access: {}:{} for user: {}", resource, action, userId);

            return authorizationService.canAccess(userId, resource, action);
        } catch (Exception e) {
            log.error("Error evaluating resource access", e);
            return false;
        }
    }

    public boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        try {
            UUID userId = getUserIdFromAuthentication(authentication);
            return authorizationService.isAdmin(userId);
        } catch (Exception e) {
            log.error("Error checking admin status", e);
            return false;
        }
    }

    public boolean isSuperAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        try {
            UUID userId = getUserIdFromAuthentication(authentication);
            return authorizationService.isSuperAdmin(userId);
        } catch (Exception e) {
            log.error("Error checking super admin status", e);
            return false;
        }
    }

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }

        if (principal instanceof String) {
            try {
                return UUID.fromString(principal.toString());
            } catch (IllegalArgumentException e) {
                log.error("Invalid user ID format in authentication principal: {}", principal);
                throw new SecurityException("Invalid user ID in authentication");
            }
        }

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            try {
                return UUID.fromString(username);
            } catch (IllegalArgumentException e) {
                log.error("Invalid user ID format in UserDetails username: {}", username);
                throw new SecurityException("Invalid user ID in UserDetails");
            }
        }

        log.error("Unsupported principal type: {}", principal.getClass());
        throw new SecurityException("Unable to extract user ID from authentication");
    }

    public interface UserPrincipal {
        UUID getId();
    }
}
