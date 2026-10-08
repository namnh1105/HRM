package com.hainam.worksphere.shared.audit.aspect;

import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.annotation.AuditableEntity;
import com.hainam.worksphere.shared.audit.dto.AuditLogDetailDto;
import com.hainam.worksphere.shared.audit.service.AuditService;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import com.hainam.worksphere.shared.audit.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * AOP Aspect that automatically handles audit logging for methods annotated with @AuditAction.
 *
 * <p>The aspect reads entity state from {@link AuditContext} ThreadLocal, which must be
 * populated by the service method:</p>
 * <ul>
 *   <li><b>CREATE</b>: Service calls {@code AuditContext.registerCreated(savedEntity)}</li>
 *   <li><b>UPDATE</b>: Service calls {@code AuditContext.snapshot(entity)} before modify,
 *       then {@code AuditContext.registerUpdated(savedEntity)} after save</li>
 *   <li><b>DELETE</b>: Service calls {@code AuditContext.registerDeleted(entity)} before soft-delete</li>
 * </ul>
 *
 * <p>This keeps audit as a cross-cutting concern while the service only needs 1 line of
 * context registration instead of 3-5 lines of manual audit calls.</p>
 */
@Slf4j
@Aspect
@Component
@Order(10)
@RequiredArgsConstructor
public class AuditActionAspect {

    private final AuditService auditService;

    @Around("@annotation(auditAction)")
    public Object handleAudit(ProceedingJoinPoint joinPoint, AuditAction auditAction) throws Throwable {
        String requestId = RequestContextUtil.getRequestId();

        try {
            Object result = joinPoint.proceed();

            try {
                switch (auditAction.type()) {
                    case CREATE -> auditCreate(auditAction, requestId);
                    case UPDATE -> auditUpdate(auditAction, requestId);
                    case DELETE -> auditDelete(auditAction, joinPoint, requestId);
                    default -> log.debug("No audit handler for action type: {}", auditAction.type());
                }
            } catch (Exception e) {
                log.error("Audit logging failed for {}.{}: {}",
                        joinPoint.getTarget().getClass().getSimpleName(),
                        joinPoint.getSignature().getName(),
                        e.getMessage(), e);
            }

            return result;

        } finally {
            AuditContext.clear();
        }
    }

    // ==================== CREATE ====================

    private void auditCreate(AuditAction auditAction, String requestId) {
        Object entity = AuditContext.getCreatedEntity();
        if (entity == null) {
            log.debug("No created entity registered for CREATE audit on {}", auditAction.entity());
            return;
        }

        List<AuditLogDetailDto> details = buildFieldDetails(entity, FieldMode.NEW_ONLY);
        if (details.isEmpty()) return;

        String entityId = extractEntityId(entity);
        String actionCode = resolveActionCode(auditAction);

        auditService.createAuditLogWithDetails(actionCode, auditAction.entity(), entityId, details, requestId);
        log.debug("Audit CREATE: {} id={}", auditAction.entity(), entityId);
    }

    // ==================== UPDATE ====================

    private void auditUpdate(AuditAction auditAction, String requestId) {
        Map<String, Object> beforeSnapshot = AuditContext.getSnapshot();
        Object afterEntity = AuditContext.getUpdatedEntity();

        if (beforeSnapshot == null || beforeSnapshot.isEmpty()) {
            log.warn("No snapshot for UPDATE on {}. Call AuditContext.snapshot(entity) before modifying.",
                    auditAction.entity());
            return;
        }
        if (afterEntity == null) {
            log.warn("No updated entity for UPDATE on {}. Call AuditContext.registerUpdated(entity) after saving.",
                    auditAction.entity());
            return;
        }

        Class<?> clazz = afterEntity.getClass();
        AuditableEntity auditable = clazz.getAnnotation(AuditableEntity.class);
        if (auditable == null) return;

        Set<String> ignoreFields = Set.of(auditable.ignoreFields());
        List<AuditLogDetailDto> details = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            String fieldName = field.getName();
            if (ignoreFields.contains(fieldName)) continue;
            field.setAccessible(true);
            try {
                Object oldValue = beforeSnapshot.get(fieldName);
                Object newValue = field.get(afterEntity);

                if (!Objects.equals(oldValue, newValue)) {
                    details.add(AuditLogDetailDto.builder()
                            .fieldName(toSnakeCase(fieldName))
                            .oldValue(serializeValue(oldValue))
                            .newValue(serializeValue(newValue))
                            .build());
                }
            } catch (IllegalAccessException e) {
                log.warn("Cannot access field {}: {}", fieldName, e.getMessage());
            }
        }

        if (!details.isEmpty()) {
            String entityId = extractEntityId(afterEntity);
            String actionCode = resolveActionCode(auditAction);
            auditService.createAuditLogWithDetails(actionCode, auditAction.entity(), entityId, details, requestId);
            log.debug("Audit UPDATE: {} id={}, {} field(s) changed",
                    auditAction.entity(), entityId, details.size());
        }
    }

    // ==================== DELETE ====================

    private void auditDelete(AuditAction auditAction, ProceedingJoinPoint joinPoint, String requestId) {
        Object entity = AuditContext.getDeletedEntity();
        String actionCode = resolveActionCode(auditAction);

        if (entity == null) {
            // Fallback: log delete with just the entity ID from args
            String entityId = extractEntityIdFromArgs(joinPoint.getArgs());
            if (entityId != null) {
                auditService.createAuditLogWithDetails(
                        actionCode, auditAction.entity(), entityId, List.of(), requestId);
                log.debug("Audit DELETE: {} id={} (no field details)", auditAction.entity(), entityId);
            }
            return;
        }

        List<AuditLogDetailDto> details = buildFieldDetails(entity, FieldMode.OLD_ONLY);
        String entityId = extractEntityId(entity);

        auditService.createAuditLogWithDetails(actionCode, auditAction.entity(), entityId,
                details.isEmpty() ? List.of() : details, requestId);
        log.debug("Audit DELETE: {} id={}", auditAction.entity(), entityId);
    }

    // ==================== Helpers ====================

    private enum FieldMode { NEW_ONLY, OLD_ONLY }

    private List<AuditLogDetailDto> buildFieldDetails(Object entity, FieldMode mode) {
        Class<?> clazz = entity.getClass();
        AuditableEntity auditable = clazz.getAnnotation(AuditableEntity.class);
        if (auditable == null) return List.of();

        Set<String> ignoreFields = Set.of(auditable.ignoreFields());
        List<AuditLogDetailDto> details = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (ignoreFields.contains(field.getName())) continue;
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value != null) {
                    details.add(AuditLogDetailDto.builder()
                            .fieldName(toSnakeCase(field.getName()))
                            .oldValue(mode == FieldMode.OLD_ONLY ? serializeValue(value) : null)
                            .newValue(mode == FieldMode.NEW_ONLY ? serializeValue(value) : null)
                            .build());
                }
            } catch (IllegalAccessException e) {
                log.warn("Cannot access field {}: {}", field.getName(), e.getMessage());
            }
        }
        return details;
    }

    private String resolveActionCode(AuditAction auditAction) {
        if (!auditAction.actionCode().isEmpty()) return auditAction.actionCode();
        return auditAction.type().name() + "_" + auditAction.entity();
    }

    private String extractEntityId(Object entity) {
        if (entity == null) return null;
        try {
            Method getId = entity.getClass().getMethod("getId");
            Object id = getId.invoke(entity);
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractEntityIdFromArgs(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof UUID) return arg.toString();
        }
        return null;
    }

    private String toSnakeCase(String fieldName) {
        return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private String serializeValue(Object value) {
        if (value == null) return null;
        if (value instanceof String || value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Enum<?> e) return e.name();
        if (value instanceof Collection<?> col) return "Collection[" + col.size() + "]";
        if (value.getClass().isArray()) return "Array[" + ((Object[]) value).length + "]";
        try {
            Method getId = value.getClass().getMethod("getId");
            Object id = getId.invoke(value);
            if (id != null) return value.getClass().getSimpleName() + ":" + id;
        } catch (Exception ignored) {}
        return value.getClass().getSimpleName() + ":" + value;
    }
}
