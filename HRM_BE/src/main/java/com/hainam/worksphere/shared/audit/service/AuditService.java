package com.hainam.worksphere.shared.audit.service;

import com.hainam.worksphere.shared.audit.domain.AuditLog;
import com.hainam.worksphere.shared.audit.domain.AuditLogDetail;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.domain.AuditStatus;
import com.hainam.worksphere.shared.domain.EntityType;
import com.hainam.worksphere.shared.web.HttpMethod;
import com.hainam.worksphere.shared.audit.dto.AuditLogDto;
import com.hainam.worksphere.shared.audit.dto.AuditLogDetailDto;
import com.hainam.worksphere.shared.audit.dto.AuditLogSearchRequest;
import com.hainam.worksphere.shared.audit.dto.AuditStatisticDto;
import com.hainam.worksphere.shared.audit.repository.AuditLogRepository;
import com.hainam.worksphere.shared.audit.repository.AuditLogDetailRepository;
import com.hainam.worksphere.shared.audit.config.AuditProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogDetailRepository auditLogDetailRepository;
    private final ObjectMapper objectMapper;
    private final AuditProperties auditProperties;

    /**
     * Create audit log (new version with enums)
     */
    @Transactional
    public void createAuditLog(ActionType actionType, String actionCode, EntityType entityType, String entityId,
                              Object oldValue, Object newValue) {
        createAuditLog(actionType, actionCode, entityType, entityId, oldValue, newValue, AuditStatus.SUCCESS, null);
    }

    /**
     * Create audit log with status (new version with enums)
     */
    @Transactional
    public void createAuditLog(ActionType actionType, String actionCode, EntityType entityType, String entityId,
                              Object oldValue, Object newValue,
                              AuditStatus status, String errorMessage) {
        createAuditLog(actionType, actionCode, entityType, entityId, null, oldValue, newValue,
                      status, errorMessage, null);
    }

    /**
     * Create comprehensive audit log with all fields (new version with enums)
     */
    @Transactional
    public void createAuditLog(ActionType actionType, String actionCode, EntityType entityType, String entityId, String fieldName,
                              Object oldValue, Object newValue,
                              AuditStatus status, String errorMessage, String requestId) {
        if (!auditProperties.isEnabled()) {
            return;
        }

        try {
            // Create header record
            AuditLog auditLog = AuditLog.builder()
                    .actionType(actionType)
                    .actionCode(actionCode)
                    .entityType(entityType)
                    .entityId(entityId)
                    .status(status)
                    .errorMessage(errorMessage)
                    .requestId(requestId)
                    .build();

            enrichWithContextData(auditLog);
            AuditLog savedAuditLog = auditLogRepository.save(auditLog);

            // Create detail record if field-level change
            if (fieldName != null) {
                AuditLogDetail detail = AuditLogDetail.builder()
                        .auditLogId(savedAuditLog.getId())
                        .fieldName(fieldName)
                        .oldValue(truncateValue(serializeObject(oldValue)))
                        .newValue(truncateValue(serializeObject(newValue)))
                        .build();

                auditLogDetailRepository.save(detail);
            }
        } catch (Exception e) {
            log.error("Failed to create audit log for actionType: {}, actionCode: {}, entityType: {}, entityId: {}",
                     actionType, actionCode, entityType, entityId, e);
        }
    }

    /**
     * Create audit log (backward compatibility)
     */
    @Transactional
    public void createAuditLog(String action, String entityType, String entityId,
                              Object oldValue, Object newValue) {
        createAuditLog(action, entityType, entityId, oldValue, newValue, "SUCCESS", null);
    }

    /**
     * Create audit log with status (backward compatibility)
     */
    @Transactional
    public void createAuditLog(String action, String entityType, String entityId,
                              Object oldValue, Object newValue,
                              String status, String errorMessage) {
        createAuditLog(action, entityType, entityId, null, oldValue, newValue,
                      status, errorMessage, null);
    }

    /**
     * Create comprehensive audit log with all fields (backward compatibility)
     */
    @Transactional
    public void createAuditLog(String action, String entityType, String entityId, String fieldName,
                              Object oldValue, Object newValue,
                              String status, String errorMessage, String requestId) {
        if (!auditProperties.isEnabled()) {
            return;
        }

        // Parse the legacy action to determine actionType and actionCode
        ActionType actionType = parseActionType(action);
        String actionCode = action; // The full action becomes the actionCode
        EntityType entityTypeEnum = parseEntityType(entityType);
        AuditStatus statusEnum = parseAuditStatus(status);

        // Delegate to new method
        createAuditLog(actionType, actionCode, entityTypeEnum, entityId, fieldName,
                      oldValue, newValue, statusEnum, errorMessage, requestId);
    }

    /**
     * Create field-level audit log
     */
    @Transactional
    public void auditField(String action, String entityType, String entityId,
                          String fieldName, Object oldValue, Object newValue, String requestId) {
        createAuditLog(action, entityType, entityId, fieldName, oldValue, newValue,
                      "SUCCESS", null, requestId);
    }

    /**
     * Create simple audit log for action only
     */
    @Transactional
    public void audit(String action) {
        createAuditLog(action, null, null, null, null);
    }

    /**
     * Create audit log for entity operation
     */
    @Transactional
    public void auditEntity(String action, String entityType, String entityId,
                           Object oldValue, Object newValue) {
        createAuditLog(action, entityType, entityId, oldValue, newValue);
    }

    /**
     * Create audit log with multiple field changes (new version with enums)
     */
    @Transactional
    public void createAuditLogWithDetails(ActionType actionType, String actionCode, EntityType entityType, String entityId,
                                         List<AuditLogDetailDto> fieldChanges, String requestId) {
        if (!auditProperties.isEnabled()) {
            return;
        }

        try {
            // Create header record
            AuditLog auditLog = AuditLog.builder()
                    .actionType(actionType)
                    .actionCode(actionCode)
                    .entityType(entityType)
                    .entityId(entityId)
                    .status(AuditStatus.SUCCESS)
                    .requestId(requestId)
                    .build();

            enrichWithContextData(auditLog);
            AuditLog savedAuditLog = auditLogRepository.save(auditLog);

            // Create detail records for each field change
            if (fieldChanges != null && !fieldChanges.isEmpty()) {
                List<AuditLogDetail> details = fieldChanges.stream()
                        .map(dto -> AuditLogDetail.builder()
                                .auditLogId(savedAuditLog.getId())
                                .fieldName(dto.getFieldName())
                                .oldValue(truncateValue(dto.getOldValue()))
                                .newValue(truncateValue(dto.getNewValue()))
                                .build())
                        .collect(Collectors.toList());

                auditLogDetailRepository.saveAll(details);
            }
        } catch (Exception e) {
            log.error("Failed to create audit log with details for actionType: {}, actionCode: {}, entityType: {}, entityId: {}",
                     actionType, actionCode, entityType, entityId, e);
        }
    }

    /**
     * Create audit log with multiple field changes (backward compatibility)
     */
    @Transactional
    public void createAuditLogWithDetails(String action, String entityType, String entityId,
                                         List<AuditLogDetailDto> fieldChanges, String requestId) {
        ActionType actionType = parseActionType(action);
        String actionCode = action;
        EntityType entityTypeEnum = parseEntityType(entityType);

        createAuditLogWithDetails(actionType, actionCode, entityTypeEnum, entityId, fieldChanges, requestId);
    }

    /**
     * Search audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> searchAuditLogs(AuditLogSearchRequest request, Pageable pageable) {
        Page<AuditLog> auditLogs;

        // Check if field-level search is needed
        boolean hasFieldSearch = request.getFieldName() != null ||
                                request.getOldValue() != null ||
                                request.getNewValue() != null;

        if (hasFieldSearch) {
            auditLogs = auditLogRepository.findByAllCriteria(
                    request.getUserId(),
                    request.getActionType(),
                    request.getActionCode(),
                    request.getEntityType(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getFieldName(),
                    request.getOldValue(),
                    request.getNewValue(),
                    pageable
            );
        } else {
            auditLogs = auditLogRepository.findByCriteria(
                    request.getUserId(),
                    request.getActionType(),
                    request.getActionCode(),
                    request.getEntityType(),
                    request.getStartDate(),
                    request.getEndDate(),
                    pageable
            );
        }

        return auditLogs.map(this::convertToDto);
    }

    /**
     * Get audit logs by user
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogsByUser(String userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable)
                .map(this::convertToDto);
    }

    /**
     * Get audit logs by entity (new version with enum)
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogsByEntity(EntityType entityType, String entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable)
                .map(this::convertToDto);
    }

    /**
     * Get audit logs by entity (backward compatibility)
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogsByEntity(String entityType, String entityId, Pageable pageable) {
        EntityType entityTypeEnum = parseEntityType(entityType);
        return getAuditLogsByEntity(entityTypeEnum, entityId, pageable);
    }

    /**
     * Get audit statistics by action type
     */
    @Transactional(readOnly = true)
    public List<AuditStatisticDto> getAuditStatisticsByActionType(Instant startDate) {
        List<Object[]> statistics = auditLogRepository.getAuditStatisticsByActionType(startDate);
        return statistics.stream()
                .map(row -> AuditStatisticDto.builder()
                        .actionCode(row[0].toString()) // ActionType enum
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get audit statistics by action code
     */
    @Transactional(readOnly = true)
    public List<AuditStatisticDto> getAuditStatisticsByActionCode(Instant startDate) {
        List<Object[]> statistics = auditLogRepository.getAuditStatisticsByActionCode(startDate);
        return statistics.stream()
                .map(row -> AuditStatisticDto.builder()
                        .actionCode((String) row[0]) // ActionCode string
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get audit statistics (backward compatibility - uses action code)
     */
    @Transactional(readOnly = true)
    public List<AuditStatisticDto> getAuditStatistics(Instant startDate) {
        return getAuditStatisticsByActionCode(startDate);
    }

    /**
     * Get failed audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getFailedAuditLogs(Pageable pageable) {
        return auditLogRepository.findByStatusOrderByTimestampDesc(AuditStatus.FAILED, pageable)
                .map(this::convertToDto);
    }

    /**
     * Enrich audit log with context data
     */
    private void enrichWithContextData(AuditLog auditLog) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            auditLog.setUserId(authentication.getName());
            if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                auditLog.setUsername(userDetails.getUsername());
            }
        }

        // Get HTTP request data
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setRequestMethod(parseHttpMethod(request.getMethod()));
            auditLog.setRequestUrl(request.getRequestURL().toString());

            // Generate request ID if not already set
            if (auditLog.getRequestId() == null) {
                auditLog.setRequestId(generateRequestId());
            }
        }

        // Set timestamp
        auditLog.setTimestamp(Instant.now());
    }

    /**
     * Generate a unique request ID
     */
    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() + "-" +
               Thread.currentThread().getId();
    }

    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Serialize object to string without JSON quotes for simple values
     */
    private String serializeObject(Object obj) {
        if (obj == null) {
            return null;
        }

        // Handle simple types without JSON quotes
        if (obj instanceof String ||
            obj instanceof Number ||
            obj instanceof Boolean ||
            obj instanceof Character) {
            return obj.toString();
        }

        // Handle enums
        if (obj instanceof Enum<?>) {
            return ((Enum<?>) obj).name();
        }

        // For complex objects, use JSON serialization
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object: {}", obj.getClass().getSimpleName(), e);
            return obj.toString();
        }
    }

    /**
     * Truncate value to maximum allowed length
     */
    private String truncateValue(String value) {
        if (value == null) {
            return null;
        }

        int maxLength = auditProperties.getMaxValueLength();
        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }

    /**
     * Convert AuditLog to DTO
     */
    private AuditLogDto convertToDto(AuditLog auditLog) {
        // Load details if not already loaded
        List<AuditLogDetail> details = auditLog.getDetails();
        if (details == null) {
            details = auditLogDetailRepository.findByAuditLogIdOrderByIdAsc(auditLog.getId());
        }

        // Convert details to DTOs
        List<AuditLogDetailDto> detailDtos = details.stream()
                .map(detail -> AuditLogDetailDto.builder()
                        .id(detail.getId())
                        .auditLogId(detail.getAuditLogId())
                        .fieldName(detail.getFieldName())
                        .oldValue(detail.getOldValue())
                        .newValue(detail.getNewValue())
                        .build())
                .collect(Collectors.toList());

        // For backward compatibility, populate field-level properties from first detail
        String fieldName = null;
        String oldValue = null;
        String newValue = null;
        if (!detailDtos.isEmpty()) {
            AuditLogDetailDto firstDetail = detailDtos.get(0);
            fieldName = firstDetail.getFieldName();
            oldValue = firstDetail.getOldValue();
            newValue = firstDetail.getNewValue();
        }

        return AuditLogDto.builder()
                .id(auditLog.getId())
                .actionType(auditLog.getActionType())
                .actionCode(auditLog.getActionCode())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .details(detailDtos)
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .requestId(auditLog.getRequestId())
                .requestMethod(auditLog.getRequestMethod())
                .requestUrl(auditLog.getRequestUrl())
                .timestamp(auditLog.getTimestamp())
                .status(auditLog.getStatus())
                .errorMessage(auditLog.getErrorMessage())
                // Backward compatibility fields
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
    }

    /**
     * Parse ActionType from legacy action string
     */
    private ActionType parseActionType(String action) {
        if (action == null) {
            return ActionType.CREATE; // default
        }

        String upperAction = action.toUpperCase();
        if (upperAction.contains("CREATE") || upperAction.contains("REGISTER") || upperAction.contains("ADD")) {
            return ActionType.CREATE;
        } else if (upperAction.contains("UPDATE") || upperAction.contains("EDIT") || upperAction.contains("MODIFY") || upperAction.contains("ASSIGN")) {
            return ActionType.UPDATE;
        } else if (upperAction.contains("DELETE") || upperAction.contains("REMOVE")) {
            return ActionType.DELETE;
        } else if (upperAction.contains("READ") || upperAction.contains("VIEW") || upperAction.contains("GET") || upperAction.contains("LOGIN")) {
            return ActionType.READ;
        }

        return ActionType.CREATE; // default fallback
    }

    /**
     * Parse EntityType from legacy entity type string
     */
    private EntityType parseEntityType(String entityType) {
        if (entityType == null) {
            return EntityType.USER; // default
        }

        try {
            return EntityType.valueOf(entityType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown entity type: {}, defaulting to USER", entityType);
            return EntityType.USER;
        }
    }

    /**
     * Parse AuditStatus from legacy status string
     */
    private AuditStatus parseAuditStatus(String status) {
        if (status == null) {
            return AuditStatus.SUCCESS; // default
        }

        try {
            return AuditStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown audit status: {}, defaulting to SUCCESS", status);
            return AuditStatus.SUCCESS;
        }
    }

    /**
     * Parse HttpMethod from method string
     */
    private HttpMethod parseHttpMethod(String method) {
        if (method == null) {
            return HttpMethod.GET; // default
        }

        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown HTTP method: {}, defaulting to GET", method);
            return HttpMethod.GET;
        }
    }
}
