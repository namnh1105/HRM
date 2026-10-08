package com.hainam.worksphere.authorization.security;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * AOP aspect that intercepts methods annotated with @RequirePermission
 * and performs the permission check using the AuthorizationService.
 * Only active when RBAC is enabled (app.security.rbac.enabled=true).
 */
@Aspect
@Component
@Order(0)
@ConditionalOnProperty(value = "app.security.rbac.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class RequirePermissionAspect {

    private final AuthorizationService authorizationService;

    @Before("@annotation(com.hainam.worksphere.authorization.security.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        if (annotation == null) {
            return;
        }

        String permissionKey = annotation.value();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        UUID userId = getUserIdFromAuthentication(authentication);
        log.debug("Checking permission '{}' for user '{}'", permissionKey, userId);

        boolean hasPermission = authorizationService.hasPermission(userId, permissionKey);
        if (!hasPermission) {
            log.warn("User '{}' does not have permission '{}'", userId, permissionKey);
            throw new AccessDeniedException("Insufficient permissions: " + permissionKey);
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

        throw new SecurityException("Cannot extract user ID from authentication principal: " + principal.getClass());
    }
}

