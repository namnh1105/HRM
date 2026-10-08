package com.hainam.worksphere.shared.audit.annotation;

import com.hainam.worksphere.shared.audit.domain.ActionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AOP-based audit annotation for service methods.
 *
 * <p>Usage examples:</p>
 * <pre>
 * // CREATE - aspect takes return value, extracts entity via getId()
 * {@literal @}AuditAction(type = ActionType.CREATE, entity = "EMPLOYEE")
 * public EmployeeResponse createEmployee(CreateEmployeeRequest req, UUID createdBy) { ... }
 *
 * // UPDATE - service registers snapshot via AuditContext.snapshot(entity) BEFORE modifying,
 * //          aspect diffs snapshot vs return value
 * {@literal @}AuditAction(type = ActionType.UPDATE, entity = "EMPLOYEE")
 * public EmployeeResponse updateEmployee(UUID id, UpdateReq req, UUID updatedBy) { ... }
 *
 * // DELETE - aspect uses first UUID param as entityId
 * {@literal @}AuditAction(type = ActionType.DELETE, entity = "EMPLOYEE")
 * public void softDeleteEmployee(UUID employeeId, UUID deletedBy) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditAction {

    /**
     * The CRUD action type (CREATE, UPDATE, DELETE)
     */
    ActionType type();

    /**
     * The entity type name (e.g., "EMPLOYEE", "DEPARTMENT", "CONTRACT")
     */
    String entity();

    /**
     * Custom action code override. If empty, auto-generated as "{TYPE}_{ENTITY}"
     * e.g., "CREATE_EMPLOYEE", "UPDATE_DEPARTMENT"
     */
    String actionCode() default "";
}

