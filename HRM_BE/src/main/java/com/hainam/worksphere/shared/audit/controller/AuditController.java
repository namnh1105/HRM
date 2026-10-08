package com.hainam.worksphere.shared.audit.controller;

import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.shared.audit.dto.AuditLogDto;
import com.hainam.worksphere.shared.audit.dto.AuditLogSearchRequest;
import com.hainam.worksphere.shared.audit.dto.AuditStatisticDto;
import com.hainam.worksphere.shared.audit.service.AuditService;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.shared.dto.PaginatedApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit logging management")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/logs")
    @RequirePermission(PermissionType.VIEW_AUDIT_LOGS)
    @Operation(summary = "Search audit logs", description = "Search audit logs with various criteria")
    public ResponseEntity<PaginatedApiResponse<AuditLogDto>> searchAuditLogs(
            @Parameter(description = "Search criteria") AuditLogSearchRequest request,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {

        Page<AuditLogDto> auditLogs = auditService.searchAuditLogs(request, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success("Audit logs retrieved successfully", auditLogs));
    }

    @GetMapping("/logs/user/{userId}")
    @RequirePermission(PermissionType.VIEW_AUDIT_LOGS)
    @Operation(summary = "Get audit logs by user", description = "Get audit logs for a specific user")
    public ResponseEntity<PaginatedApiResponse<AuditLogDto>> getAuditLogsByUser(
            @PathVariable String userId,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {

        Page<AuditLogDto> auditLogs = auditService.getAuditLogsByUser(userId, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success("User audit logs retrieved successfully", auditLogs));
    }

    @GetMapping("/logs/entity/{entityType}/{entityId}")
    @RequirePermission(PermissionType.VIEW_AUDIT_LOGS)
    @Operation(summary = "Get audit logs by entity", description = "Get audit logs for a specific entity")
    public ResponseEntity<PaginatedApiResponse<AuditLogDto>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {

        Page<AuditLogDto> auditLogs = auditService.getAuditLogsByEntity(entityType, entityId, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success("Entity audit logs retrieved successfully", auditLogs));
    }

    @GetMapping("/logs/failed")
    @RequirePermission(PermissionType.VIEW_AUDIT_LOGS)
    @Operation(summary = "Get failed audit logs", description = "Get audit logs with failed status")
    public ResponseEntity<PaginatedApiResponse<AuditLogDto>> getFailedAuditLogs(
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {

        Page<AuditLogDto> auditLogs = auditService.getFailedAuditLogs(pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success("Failed audit logs retrieved successfully", auditLogs));
    }

    @GetMapping("/statistics")
    @RequirePermission(PermissionType.VIEW_AUDIT_LOGS)
    @Operation(summary = "Get audit statistics", description = "Get audit statistics for a specific period")
    public ResponseEntity<ApiResponse<List<AuditStatisticDto>>> getAuditStatistics(
            @RequestParam(defaultValue = "7") int days) {

        Instant startDate = Instant.now().minus(java.time.Duration.ofDays(days));
        List<AuditStatisticDto> statistics = auditService.getAuditStatistics(startDate);
        return ResponseEntity.ok(ApiResponse.success("Audit statistics retrieved successfully", statistics));
    }
}
