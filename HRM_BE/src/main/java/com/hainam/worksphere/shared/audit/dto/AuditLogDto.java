package com.hainam.worksphere.shared.audit.dto;

import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.domain.EntityType;
import com.hainam.worksphere.shared.audit.domain.AuditStatus;
import com.hainam.worksphere.shared.web.HttpMethod;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AuditLogDto {
    private UUID id;

    // New structured fields
    private ActionType actionType;
    private String actionCode;
    private EntityType entityType;

    private String entityId;
    private List<AuditLogDetailDto> details;
    private String userId;
    private String username;
    private String ipAddress;
    private String userAgent;
    private String requestId;
    private HttpMethod requestMethod;
    private String requestUrl;
    private Instant timestamp;
    private AuditStatus status;
    private String errorMessage;

    // Backward compatibility - these will be populated from details for single field changes
    private String fieldName;
    private String oldValue;
    private String newValue;
}
