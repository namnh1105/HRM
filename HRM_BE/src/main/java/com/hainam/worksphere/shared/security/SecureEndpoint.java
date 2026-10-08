package com.hainam.worksphere.shared.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ConditionalOnProperty(value = "app.security.rbac.enabled", havingValue = "true", matchIfMissing = true)
public @interface SecureEndpoint {

    String value();
}
