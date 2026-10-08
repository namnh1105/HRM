package com.hainam.worksphere.shared.audit.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuditLogDetailDto {
    private Long id;            // Keep Long auto-increment for details
    private UUID auditLogId;    // UUID foreign key reference to audit_logs
    private String fieldName;
    private String oldValue;
    private String newValue;
}
