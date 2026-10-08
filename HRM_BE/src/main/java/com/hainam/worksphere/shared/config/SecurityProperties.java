package com.hainam.worksphere.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Arrays;

@Configuration
@ConfigurationProperties(prefix = "app.security")
@Data
public class SecurityProperties {

    private Rbac rbac = new Rbac();
    private String publicEndpoints = "";

    @Data
    public static class Rbac {
        private boolean enabled = true;
    }

    /**
     * Get public endpoints as a list, splitting by comma
     */
    public List<String> getPublicEndpointsList() {
        if (publicEndpoints == null || publicEndpoints.trim().isEmpty()) {
            return Arrays.asList(
                "/api/v1/auth/register",
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/api/v1/auth/oauth2/**",
                "/api/public/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/actuator/health",
                "/error"
            );
        }
        return Arrays.asList(publicEndpoints.split(","));
    }
}
