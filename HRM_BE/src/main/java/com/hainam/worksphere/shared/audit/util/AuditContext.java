package com.hainam.worksphere.shared.audit.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thread-local context for AOP-based audit logging.
 *
 * <p>Services register entity state into this context, and the {@code AuditActionAspect}
 * picks it up after method execution to perform audit logging.</p>
 *
 * <h3>Usage in service methods:</h3>
 *
 * <b>CREATE:</b>
 * <pre>
 * {@literal @}AuditAction(type = ActionType.CREATE, entity = "EMPLOYEE")
 * public EmployeeResponse createEmployee(CreateEmployeeRequest req, UUID createdBy) {
 *     Employee saved = repository.save(employee);
 *     AuditContext.registerCreated(saved);           // <-- register created entity
 *     return mapper.toResponse(saved);
 * }
 * </pre>
 *
 * <b>UPDATE:</b>
 * <pre>
 * {@literal @}AuditAction(type = ActionType.UPDATE, entity = "EMPLOYEE")
 * public EmployeeResponse updateEmployee(UUID id, UpdateReq req, UUID updatedBy) {
 *     Employee employee = repo.findById(id).orElseThrow(...);
 *     AuditContext.snapshot(employee);               // <-- snapshot BEFORE modify
 *     employee.setName(req.getName());
 *     Employee saved = repo.save(employee);
 *     AuditContext.registerUpdated(saved);            // <-- register updated entity
 *     return mapper.toResponse(saved);
 * }
 * </pre>
 *
 * <b>DELETE:</b>
 * <pre>
 * {@literal @}AuditAction(type = ActionType.DELETE, entity = "EMPLOYEE")
 * public void softDeleteEmployee(UUID id, UUID deletedBy) {
 *     Employee employee = repo.findById(id).orElseThrow(...);
 *     AuditContext.registerDeleted(employee);         // <-- register BEFORE delete
 *     employee.setIsDeleted(true);
 *     repo.save(employee);
 * }
 * </pre>
 */
@Slf4j
public final class AuditContext {

    private static final ThreadLocal<Map<String, Object>> SNAPSHOT = new ThreadLocal<>();
    private static final ThreadLocal<Object> CREATED_ENTITY = new ThreadLocal<>();
    private static final ThreadLocal<Object> UPDATED_ENTITY = new ThreadLocal<>();
    private static final ThreadLocal<Object> DELETED_ENTITY = new ThreadLocal<>();

    private AuditContext() {}

    /**
     * Take a snapshot of the entity's current state (before modification).
     * Must be called inside the service method BEFORE making changes.
     */
    public static void snapshot(Object entity) {
        if (entity == null) {
            log.debug("AuditContext.snapshot called with null entity, ignoring");
            return;
        }

        Map<String, Object> map = new LinkedHashMap<>();
        Class<?> clazz = entity.getClass();
        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(entity));
            }
        } catch (Exception e) {
            log.warn("Failed to create audit snapshot for {}: {}", clazz.getSimpleName(), e.getMessage());
        }
        SNAPSHOT.set(map);
    }

    /**
     * Register a newly created entity for CREATE audit.
     */
    public static void registerCreated(Object entity) {
        CREATED_ENTITY.set(entity);
    }

    /**
     * Register the updated entity (after save) for UPDATE audit.
     */
    public static void registerUpdated(Object entity) {
        UPDATED_ENTITY.set(entity);
    }

    /**
     * Register the entity that is about to be deleted for DELETE audit.
     * Must be called BEFORE performing soft-delete.
     */
    public static void registerDeleted(Object entity) {
        DELETED_ENTITY.set(entity);
    }

    // === Public getters for AuditActionAspect ===

    public static Map<String, Object> getSnapshot() {
        return SNAPSHOT.get();
    }

    public static Object getCreatedEntity() {
        return CREATED_ENTITY.get();
    }

    public static Object getUpdatedEntity() {
        return UPDATED_ENTITY.get();
    }

    public static Object getDeletedEntity() {
        return DELETED_ENTITY.get();
    }

    /**
     * Clear all thread-local state (called by AuditActionAspect in finally block).
     */
    public static void clear() {
        SNAPSHOT.remove();
        CREATED_ENTITY.remove();
        UPDATED_ENTITY.remove();
        DELETED_ENTITY.remove();
    }
}

