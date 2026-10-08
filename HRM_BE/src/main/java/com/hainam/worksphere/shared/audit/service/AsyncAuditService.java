package com.hainam.worksphere.shared.audit.service;

import com.hainam.worksphere.shared.audit.config.AuditProperties;
import com.hainam.worksphere.shared.audit.domain.AuditLog;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.domain.AuditStatus;
import com.hainam.worksphere.shared.domain.EntityType;
import com.hainam.worksphere.shared.web.HttpMethod;
import com.hainam.worksphere.shared.audit.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.audit.async-logging", havingValue = "true", matchIfMissing = true)
public class AsyncAuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final AuditProperties auditProperties;

    @Async("auditTaskExecutor")
    public void createAuditLogAsync(String action, String entityType, String entityId,
                                   Object oldValue, Object newValue,
                                   String status, String errorMessage,
                                   String userId, String username, String ipAddress,
                                   String userAgent, String requestMethod, String requestUrl) {

        if (!auditProperties.isEnabled()) {
            return;
        }

        try {
            AuditLog auditLog = AuditLog.builder()
                    .actionType(parseActionType(action))
                    .actionCode(action)
                    .entityType(parseEntityType(entityType))
                    .entityId(entityId)
                    .status(parseAuditStatus(status != null ? status : "SUCCESS"))
                    .errorMessage(errorMessage)
                    .userId(userId)
                    .username(username)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .requestMethod(parseHttpMethod(requestMethod))
                    .requestUrl(requestUrl)
                    .timestamp(Instant.now())
                    .build();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Failed to create async audit log for action: {}, entityType: {}, entityId: {}",
                     action, entityType, entityId, e);
        }
    }

    @Async("auditTaskExecutor")
    public void createSimpleAuditLogAsync(String action) {
        if (!auditProperties.isEnabled()) {
            return;
        }

        try {
            // Get current context data
            String userId = null;
            String username = null;
            String ipAddress = null;
            String userAgent = null;
            String requestMethod = null;
            String requestUrl = null;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                userId = authentication.getName();
                if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                    username = userDetails.getUsername();
                }
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = getClientIpAddress(request);
                userAgent = request.getHeader("User-Agent");
                requestMethod = request.getMethod();
                requestUrl = request.getRequestURL().toString();
            }

            createAuditLogAsync(action, null, null, null, null,
                               "SUCCESS", null, userId, username, ipAddress,
                               userAgent, requestMethod, requestUrl);

        } catch (Exception e) {
            log.error("Failed to create simple async audit log for action: {}", action, e);
        }
    }

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
     * Parse ActionType from legacy action string
     */
    private ActionType parseActionType(String action) {
        if (action == null) {
            return ActionType.CREATE;
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

        return ActionType.CREATE;
    }

    /**
     * Parse EntityType from legacy entity type string
     */
    private EntityType parseEntityType(String entityType) {
        if (entityType == null) {
            return EntityType.USER;
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
            return AuditStatus.SUCCESS;
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
            return HttpMethod.GET;
        }

        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown HTTP method: {}, defaulting to GET", method);
            return HttpMethod.GET;
        }
    }
}
