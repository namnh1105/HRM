package com.hainam.worksphere.shared.audit.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditStatisticDto {
    private String actionCode; // Changed from 'action' to 'actionCode' for clarity
    private Long count;
    private String period;
}
