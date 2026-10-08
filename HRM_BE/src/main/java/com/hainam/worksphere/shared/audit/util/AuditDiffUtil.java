package com.hainam.worksphere.shared.audit.util;

import com.hainam.worksphere.shared.audit.annotation.AuditableEntity;
import com.hainam.worksphere.shared.audit.dto.AuditLogDetailDto;
import com.hainam.worksphere.shared.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditDiffUtil {

    private final AuditService auditService;

    /**
     * Create a snapshot (field value map) of an entity for later comparison.
     * Call this BEFORE modifying the entity, then pass the snapshot to auditUpdate().
     */
    public Map<String, Object> snapshot(Object entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (entity == null) return map;

        Class<?> clazz = entity.getClass();
        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(entity));
            }
        } catch (Exception e) {
            log.warn("Failed to snapshot entity: {}", clazz.getSimpleName(), e);
        }
        return map;
    }

    /**
     * Audit entity update by comparing a pre-change snapshot with the current entity state.
     *
     * @param action the action code (e.g., "UPDATE_EMPLOYEE")
     * @param entityType the entity type (e.g., "EMPLOYEE")
     * @param entityId the entity ID
     * @param beforeSnapshot the snapshot taken before modifications (from snapshot() method)
     * @param after the entity after modifications
     * @param requestId the request ID
     */
    public void auditUpdate(
            String action,
            String entityType,
            String entityId,
            Map<String, Object> beforeSnapshot,
            Object after,
            String requestId
    ) {
        if (beforeSnapshot == null || after == null) {
            log.debug("Skipping audit update - snapshot or entity is null");
            return;
        }

        Class<?> clazz = after.getClass();
        AuditableEntity auditable = clazz.getAnnotation(AuditableEntity.class);

        if (auditable == null) {
            log.debug("Class {} is not annotated with @AuditableEntity, skipping audit update",
                     clazz.getSimpleName());
            return;
        }

        Set<String> ignoreFields = Set.of(auditable.ignoreFields());
        List<AuditLogDetailDto> fieldChanges = new ArrayList<>();

        try {
            for (Field field : clazz.getDeclaredFields()) {
                String fieldName = field.getName();
                if (ignoreFields.contains(fieldName)) continue;

                field.setAccessible(true);
                Object oldValue = beforeSnapshot.get(fieldName);
                Object newValue = field.get(after);

                if (!Objects.equals(oldValue, newValue)) {
                    fieldChanges.add(AuditLogDetailDto.builder()
                            .fieldName(convertFieldName(fieldName))
                            .oldValue(String.valueOf(serializeValue(oldValue)))
                            .newValue(String.valueOf(serializeValue(newValue)))
                            .build());
                }
            }

            if (!fieldChanges.isEmpty()) {
                auditService.createAuditLogWithDetails(action, entityType, entityId, fieldChanges, requestId);
            }
        } catch (Exception e) {
            log.error("Failed to audit update for entityType: {}, entityId: {}", entityType, entityId, e);
        }
    }

    /**
     * Audit all field changes using class-level @AuditableEntity annotation.
     * This method audits all fields by default and excludes only the specified ones.
     *
     * @param action the action being performed (e.g., "UPDATE_PROFILE")
     * @param entityType the type of entity being audited (e.g., "USER")
     * @param entityId the ID of the entity
     * @param before the entity state before changes
     * @param after the entity state after changes
     * @param requestId the request ID for tracking
     */
    public void auditAllChanges(
            String action,
            String entityType,
            String entityId,
            Object before,
            Object after,
            String requestId
    ) {
        if (before == null || after == null) {
            log.debug("Skipping audit - before or after object is null");
            return;
        }

        if (!before.getClass().equals(after.getClass())) {
            log.warn("Cannot compare objects of different types: {} vs {}",
                    before.getClass().getSimpleName(), after.getClass().getSimpleName());
            return;
        }

        Class<?> clazz = before.getClass();
        AuditableEntity auditable = clazz.getAnnotation(AuditableEntity.class);

        if (auditable == null) {
            log.debug("Class {} is not annotated with @AuditableEntity, skipping audit",
                     clazz.getSimpleName());
            return;
        }

        Set<String> ignoreFields = Set.of(auditable.ignoreFields());
        log.debug("Auditing entity {} with ignored fields: {}", clazz.getSimpleName(), ignoreFields);

        List<AuditLogDetailDto> fieldChanges = new ArrayList<>();

        try {
            for (Field field : clazz.getDeclaredFields()) {
                String fieldName = field.getName();

                if (ignoreFields.contains(fieldName)) {
                    log.trace("Ignoring field: {}", fieldName);
                    continue;
                }

                field.setAccessible(true);

                try {
                    Object oldValue = field.get(before);
                    Object newValue = field.get(after);

                    if (!Objects.equals(oldValue, newValue)) {
                        log.debug("Field {} changed from {} to {}", fieldName, oldValue, newValue);

                        fieldChanges.add(AuditLogDetailDto.builder()
                                .fieldName(convertFieldName(fieldName))
                                .oldValue(String.valueOf(serializeValue(oldValue)))
                                .newValue(String.valueOf(serializeValue(newValue)))
                                .build());
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Cannot access field {}: {}", fieldName, e.getMessage());
                }
            }

            // Create single audit record with all field changes
            if (!fieldChanges.isEmpty()) {
                auditService.createAuditLogWithDetails(
                    action,
                    entityType,
                    entityId,
                    fieldChanges,
                    requestId
                );
            }
        } catch (Exception e) {
            log.error("Failed to audit all changes for entityType: {}, entityId: {}",
                     entityType, entityId, e);
        }
    }


    /**
     * Audit entity creation - logs all non-ignored fields of a newly created entity.
     *
     * @param action the action being performed (e.g., "CREATE_EMPLOYEE")
     * @param entityType the type of entity being audited (e.g., "EMPLOYEE")
     * @param entityId the ID of the entity
     * @param newEntity the newly created entity
     * @param requestId the request ID for tracking
     */
    public void auditCreate(
            String action,
            String entityType,
            String entityId,
            Object newEntity,
            String requestId
    ) {
        if (newEntity == null) {
            log.debug("Skipping audit create - entity is null");
            return;
        }

        Class<?> clazz = newEntity.getClass();
        AuditableEntity auditable = clazz.getAnnotation(AuditableEntity.class);

        if (auditable == null) {
            log.debug("Class {} is not annotated with @AuditableEntity, skipping audit create",
                     clazz.getSimpleName());
            return;
        }

        Set<String> ignoreFields = Set.of(auditable.ignoreFields());
        log.debug("Auditing create for entity {} with ignored fields: {}", clazz.getSimpleName(), ignoreFields);

        List<AuditLogDetailDto> fieldChanges = new ArrayList<>();

        try {
            for (Field field : clazz.getDeclaredFields()) {
                String fieldName = field.getName();

                if (ignoreFields.contains(fieldName)) {
                    continue;
                }

                field.setAccessible(true);

                try {
                    Object newValue = field.get(newEntity);

                    if (newValue != null) {
                        fieldChanges.add(AuditLogDetailDto.builder()
                                .fieldName(convertFieldName(fieldName))
                                .oldValue(null)
                                .newValue(String.valueOf(serializeValue(newValue)))
                                .build());
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Cannot access field {}: {}", fieldName, e.getMessage());
                }
            }

            if (!fieldChanges.isEmpty()) {
                auditService.createAuditLogWithDetails(
                    action,
                    entityType,
                    entityId,
                    fieldChanges,
                    requestId
                );
            }
        } catch (Exception e) {
            log.error("Failed to audit create for entityType: {}, entityId: {}",
                     entityType, entityId, e);
        }
    }

    /**
     * Audit entity deletion - logs the action of deleting an entity.
     *
     * @param action the action being performed (e.g., "DELETE_EMPLOYEE")
     * @param entityType the type of entity being audited (e.g., "EMPLOYEE")
     * @param entityId the ID of the entity
     * @param entity the entity being deleted (to log its state)
     * @param requestId the request ID for tracking
     */
    public void auditDelete(
            String action,
            String entityType,
            String entityId,
            Object entity,
            String requestId
    ) {
        if (entity == null) {
            log.debug("Skipping audit delete - entity is null");
            return;
        }

        Class<?> clazz = entity.getClass();
        AuditableEntity auditable = clazz.getAnnotation(AuditableEntity.class);

        if (auditable == null) {
            log.debug("Class {} is not annotated with @AuditableEntity, skipping audit delete",
                     clazz.getSimpleName());
            return;
        }

        Set<String> ignoreFields = Set.of(auditable.ignoreFields());
        List<AuditLogDetailDto> fieldChanges = new ArrayList<>();

        try {
            for (Field field : clazz.getDeclaredFields()) {
                String fieldName = field.getName();

                if (ignoreFields.contains(fieldName)) {
                    continue;
                }

                field.setAccessible(true);

                try {
                    Object oldValue = field.get(entity);

                    if (oldValue != null) {
                        fieldChanges.add(AuditLogDetailDto.builder()
                                .fieldName(convertFieldName(fieldName))
                                .oldValue(String.valueOf(serializeValue(oldValue)))
                                .newValue(null)
                                .build());
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Cannot access field {}: {}", fieldName, e.getMessage());
                }
            }

            if (!fieldChanges.isEmpty()) {
                auditService.createAuditLogWithDetails(
                    action,
                    entityType,
                    entityId,
                    fieldChanges,
                    requestId
                );
            }
        } catch (Exception e) {
            log.error("Failed to audit delete for entityType: {}, entityId: {}",
                     entityType, entityId, e);
        }
    }

    /**
     * Convert camelCase field name to snake_case for database consistency
     */
    private String convertFieldName(String fieldName) {
        // Convert camelCase to snake_case
        return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * Serialize value to string for audit logging
     */
    private Object serializeValue(Object value) {
        if (value == null) {
            return null;
        }

        // Handle primitive types and strings directly
        if (isPrimitiveOrWrapper(value.getClass()) || value instanceof String) {
            return value;
        }

        // Handle enums
        if (value instanceof Enum<?>) {
            return ((Enum<?>) value).name();
        }

        // Handle collections - just return size for audit
        if (value instanceof Collection<?> collection) {
            return "Collection[" + collection.size() + "]";
        }

        // Handle arrays
        if (value.getClass().isArray()) {
            return "Array[" + ((Object[]) value).length + "]";
        }

        // For complex objects, return class name + toString
        return value.getClass().getSimpleName() + ":" + value.toString();
    }

    /**
     * Check if class is primitive or wrapper type
     */
    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() ||
               type == Boolean.class ||
               type == Byte.class ||
               type == Character.class ||
               type == Double.class ||
               type == Float.class ||
               type == Integer.class ||
               type == Long.class ||
               type == Short.class;
    }
}
