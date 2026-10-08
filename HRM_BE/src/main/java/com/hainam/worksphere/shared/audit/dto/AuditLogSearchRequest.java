package com.hainam.worksphere.shared.audit.dto;

import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.domain.EntityType;
import com.hainam.worksphere.shared.audit.domain.AuditStatus;
import com.hainam.worksphere.shared.web.HttpMethod;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Data
public class AuditLogSearchRequest {
    private String userId;
    private String username;

    private ActionType actionType;
    private String actionCode;
    private EntityType entityType;
    private AuditStatus status;
    private HttpMethod requestMethod;

    private String entityId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant endDate;

    private String ipAddress;

    private String fieldName;
    private String oldValue;
    private String newValue;
}
