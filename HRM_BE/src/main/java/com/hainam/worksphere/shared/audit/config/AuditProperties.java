package com.hainam.worksphere.shared.audit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.audit")
public class AuditProperties {

    private boolean enabled = true;

    private boolean logParameters = false;

    private boolean logResponse = false;

    private boolean logOnlySuccess = false;

    private int maxValueLength = 10000;

    private boolean asyncLogging = true;

    private int retentionDays = 365;

    private boolean compressOldLogs = true;
}
