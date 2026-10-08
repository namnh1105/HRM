package com.hainam.worksphere.shared.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Action name to be logged
     */
    String action();

    /**
     * Entity type being audited
     */
    String entityType() default "";

    /**
     * Description of the action
     */
    String description() default "";

    /**
     * Whether to log request parameters
     */
    boolean logParameters() default false;

    /**
     * Whether to log response data
     */
    boolean logResponse() default false;

    /**
     * Whether to log only on success
     */
    boolean logOnlyOnSuccess() default false;
}
