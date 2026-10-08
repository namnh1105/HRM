package com.hainam.worksphere.shared.audit.aspect;

import com.hainam.worksphere.shared.audit.annotation.Auditable;
import com.hainam.worksphere.shared.audit.service.AuditService;
import com.hainam.worksphere.shared.audit.util.RequestContextUtil;
import com.hainam.worksphere.auth.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();

        // Generate and set request ID for the thread
        String requestId = RequestContextUtil.generateRequestId();

        Object result = null;
        Exception exception = null;

        try {
            // Log method entry if needed
            if (auditable.logParameters()) {
                logMethodEntry(joinPoint, auditable, requestId);
            }

            // Execute the method
            result = joinPoint.proceed();

            // Log successful execution - action level only
            logMethodSuccess(joinPoint, auditable, result, startTime, requestId);

            return result;

        } catch (Exception e) {
            exception = e;

            // Log method failure
            logMethodFailure(joinPoint, auditable, e, startTime, requestId);

            throw e;
        } finally {
            // Clear request ID from thread local
            RequestContextUtil.clearRequestId();
        }
    }

    private void logMethodEntry(JoinPoint joinPoint, Auditable auditable, String requestId) {
        try {
            String description = auditable.description();
            if (description.isEmpty()) {
                description = "Method " + joinPoint.getSignature().getName() + " started";
            }

            String entityId = extractEntityId(joinPoint.getArgs());

            auditService.createAuditLog(
                auditable.action() + "_STARTED",
                auditable.entityType().isEmpty() ? null : auditable.entityType(),
                entityId,
                null, // fieldName
                null, // oldValue
                null, // newValue
                "SUCCESS",
                null, // errorMessage
                requestId
            );

        } catch (Exception e) {
            log.error("Failed to log method entry for {}", joinPoint.getSignature().getName(), e);
        }
    }

    private void logMethodSuccess(JoinPoint joinPoint, Auditable auditable, Object result, long startTime, String requestId) {
        try {
            long executionTime = System.currentTimeMillis() - startTime;
            String description = buildSuccessDescription(auditable, joinPoint, executionTime);

            String entityId = extractEntityId(joinPoint.getArgs());

            // Create action-level audit log only
            auditService.createAuditLog(
                auditable.action(),
                auditable.entityType().isEmpty() ? null : auditable.entityType(),
                entityId,
                null, // fieldName
                null, // oldValue
                auditable.logResponse() ? result : null, // newValue (response if needed)
                "SUCCESS",
                null, // errorMessage
                requestId
            );

        } catch (Exception e) {
            log.error("Failed to log method success for {}", joinPoint.getSignature().getName(), e);
        }
    }

    private void logMethodFailure(JoinPoint joinPoint, Auditable auditable, Exception exception, long startTime, String requestId) {
        try {
            long executionTime = System.currentTimeMillis() - startTime;
            String description = buildFailureDescription(auditable, joinPoint, exception, executionTime);

            String entityId = extractEntityId(joinPoint.getArgs());

            auditService.createAuditLog(
                auditable.action(),
                auditable.entityType().isEmpty() ? null : auditable.entityType(),
                entityId,
                null, // fieldName
                null, // oldValue
                null, // newValue
                "FAILED",
                exception.getMessage(),
                requestId
            );

        } catch (Exception e) {
            log.error("Failed to log method failure for {}", joinPoint.getSignature().getName(), e);
        }
    }

    private String buildSuccessDescription(Auditable auditable, JoinPoint joinPoint, long executionTime) {
        if (!auditable.description().isEmpty()) {
            return auditable.description() + " (executed in " + executionTime + "ms)";
        }

        return "Method " + joinPoint.getSignature().getName() + " executed successfully in " + executionTime + "ms";
    }

    private String buildFailureDescription(Auditable auditable, JoinPoint joinPoint, Exception exception, long executionTime) {
        if (!auditable.description().isEmpty()) {
            return auditable.description() + " failed after " + executionTime + "ms: " + exception.getMessage();
        }

        return "Method " + joinPoint.getSignature().getName() + " failed after " + executionTime + "ms: " + exception.getMessage();
    }

    private String extractEntityId(Object[] args) {
        // First try to extract from UserPrincipal
        for (Object arg : args) {
            if (arg instanceof UserPrincipal) {
                return ((UserPrincipal) arg).getId().toString();
            }
        }

        // Try to find UUID parameter (user ID)
        for (Object arg : args) {
            if (arg instanceof UUID) {
                return arg.toString();
            }
        }

        // Try to find ID parameter (Long, Integer, String)
        for (Object arg : args) {
            if (arg instanceof Long || arg instanceof Integer ||
                (arg instanceof String && ((String) arg).matches("\\d+"))) {
                return arg.toString();
            }
        }

        return null;
    }

    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "exception")
    public void auditException(JoinPoint joinPoint, Auditable auditable, Exception exception) {
        // This is handled in the @Around advice, but kept for completeness
        log.debug("Exception occurred in auditable method: {}", joinPoint.getSignature().getName());
    }
}
