package com.hainam.worksphere.shared.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditableEntity {
    String[] ignoreFields() default {
        "id",
        "updatedAt",
        "updatedBy",
        "createdAt",
        "createdBy",
        "version",
        "isDeleted",
        "deletedAt",
        "deletedBy"
    };
}
