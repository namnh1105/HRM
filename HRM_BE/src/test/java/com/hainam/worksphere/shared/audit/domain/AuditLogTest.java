package com.hainam.worksphere.shared.audit.domain;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.shared.domain.EntityType;
import com.hainam.worksphere.shared.web.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("AuditLog Domain Tests")
class AuditLogTest extends BaseUnitTest {

    private UUID auditLogId;
    private Instant timestamp;
    private String entityId;
    private String userId;

    @BeforeEach
    void setUp() {
        auditLogId = UUID.randomUUID();
        timestamp = Instant.now();
        entityId = UUID.randomUUID().toString();
        userId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("Should create audit log with builder pattern")
    void shouldCreateAuditLogWithBuilderPattern() {
        // When
        AuditLog auditLog = AuditLog.builder()
                .id(auditLogId)
                .actionType(ActionType.CREATE)
                .actionCode("CREATE_USER")
                .entityType(EntityType.USER)
                .entityId(entityId)
                .userId(userId)
                .username("test@example.com")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .requestId(UUID.randomUUID().toString())
                .requestMethod(HttpMethod.POST)
                .requestUrl("/api/users")
                .timestamp(timestamp)
                .build();

        // Then
        assertAll(
                () -> assertThat(auditLog.getId()).isEqualTo(auditLogId),
                () -> assertThat(auditLog.getActionType()).isEqualTo(ActionType.CREATE),
                () -> assertThat(auditLog.getActionCode()).isEqualTo("CREATE_USER"),
                () -> assertThat(auditLog.getEntityType()).isEqualTo(EntityType.USER),
                () -> assertThat(auditLog.getEntityId()).isEqualTo(entityId),
                () -> assertThat(auditLog.getUserId()).isEqualTo(userId),
                () -> assertThat(auditLog.getUsername()).isEqualTo("test@example.com"),
                () -> assertThat(auditLog.getIpAddress()).isEqualTo("192.168.1.1"),
                () -> assertThat(auditLog.getUserAgent()).isEqualTo("Mozilla/5.0"),
                () -> assertThat(auditLog.getRequestMethod()).isEqualTo(HttpMethod.POST),
                () -> assertThat(auditLog.getRequestUrl()).isEqualTo("/api/users"),
                () -> assertThat(auditLog.getTimestamp()).isEqualTo(timestamp)
        );
    }

    @Test
    @DisplayName("Should create audit log with all constructor")
    void shouldCreateAuditLogWithAllConstructor() {
        // Given
        List<AuditLogDetail> details = new ArrayList<>();
        String requestId = UUID.randomUUID().toString();

        // When
        AuditLog auditLog = new AuditLog(
                auditLogId, ActionType.UPDATE, "UPDATE_PROFILE", EntityType.USER,
                entityId, details, userId, "john.doe@example.com", "10.0.0.1",
                "Chrome/98.0", requestId, HttpMethod.PUT, "/api/users/profile",
                timestamp, AuditStatus.SUCCESS, null
        );

        // Then
        assertAll(
                () -> assertThat(auditLog.getId()).isEqualTo(auditLogId),
                () -> assertThat(auditLog.getActionType()).isEqualTo(ActionType.UPDATE),
                () -> assertThat(auditLog.getActionCode()).isEqualTo("UPDATE_PROFILE"),
                () -> assertThat(auditLog.getEntityType()).isEqualTo(EntityType.USER),
                () -> assertThat(auditLog.getEntityId()).isEqualTo(entityId),
                () -> assertThat(auditLog.getDetails()).isEqualTo(details),
                () -> assertThat(auditLog.getUserId()).isEqualTo(userId),
                () -> assertThat(auditLog.getUsername()).isEqualTo("john.doe@example.com"),
                () -> assertThat(auditLog.getIpAddress()).isEqualTo("10.0.0.1"),
                () -> assertThat(auditLog.getUserAgent()).isEqualTo("Chrome/98.0"),
                () -> assertThat(auditLog.getRequestId()).isEqualTo(requestId),
                () -> assertThat(auditLog.getRequestMethod()).isEqualTo(HttpMethod.PUT),
                () -> assertThat(auditLog.getRequestUrl()).isEqualTo("/api/users/profile"),
                () -> assertThat(auditLog.getTimestamp()).isEqualTo(timestamp),
                () -> assertThat(auditLog.getStatus()).isEqualTo(AuditStatus.SUCCESS),
                () -> assertThat(auditLog.getErrorMessage()).isNull()
        );
    }

    @Test
    @DisplayName("Should create audit log with no args constructor")
    void shouldCreateAuditLogWithNoArgsConstructor() {
        // When
        AuditLog auditLog = new AuditLog();

        // Then
        assertThat(auditLog).isNotNull();
    }

    @Test
    @DisplayName("Should handle different action types")
    void shouldHandleDifferentActionTypes() {
        // When
        AuditLog createLog = AuditLog.builder().actionType(ActionType.CREATE).build();
        AuditLog readLog = AuditLog.builder().actionType(ActionType.READ).build();
        AuditLog updateLog = AuditLog.builder().actionType(ActionType.UPDATE).build();
        AuditLog deleteLog = AuditLog.builder().actionType(ActionType.DELETE).build();

        // Then
        assertAll(
                () -> assertThat(createLog.getActionType()).isEqualTo(ActionType.CREATE),
                () -> assertThat(readLog.getActionType()).isEqualTo(ActionType.READ),
                () -> assertThat(updateLog.getActionType()).isEqualTo(ActionType.UPDATE),
                () -> assertThat(deleteLog.getActionType()).isEqualTo(ActionType.DELETE)
        );
    }

    @Test
    @DisplayName("Should handle different entity types")
    void shouldHandleDifferentEntityTypes() {
        // When
        AuditLog userLog = AuditLog.builder().entityType(EntityType.USER).build();
        AuditLog employeeLog = AuditLog.builder().entityType(EntityType.EMPLOYEE).build();
        AuditLog roleLog = AuditLog.builder().entityType(EntityType.ROLE).build();

        // Then
        assertAll(
                () -> assertThat(userLog.getEntityType()).isEqualTo(EntityType.USER),
                () -> assertThat(employeeLog.getEntityType()).isEqualTo(EntityType.EMPLOYEE),
                () -> assertThat(roleLog.getEntityType()).isEqualTo(EntityType.ROLE)
        );
    }

    @Test
    @DisplayName("Should handle different HTTP methods")
    void shouldHandleDifferentHttpMethods() {
        // When
        AuditLog getLog = AuditLog.builder().requestMethod(HttpMethod.GET).build();
        AuditLog postLog = AuditLog.builder().requestMethod(HttpMethod.POST).build();
        AuditLog putLog = AuditLog.builder().requestMethod(HttpMethod.PUT).build();
        AuditLog deleteLog = AuditLog.builder().requestMethod(HttpMethod.DELETE).build();

        // Then
        assertAll(
                () -> assertThat(getLog.getRequestMethod()).isEqualTo(HttpMethod.GET),
                () -> assertThat(postLog.getRequestMethod()).isEqualTo(HttpMethod.POST),
                () -> assertThat(putLog.getRequestMethod()).isEqualTo(HttpMethod.PUT),
                () -> assertThat(deleteLog.getRequestMethod()).isEqualTo(HttpMethod.DELETE)
        );
    }

    @Test
    @DisplayName("Should handle audit log details relationship")
    void shouldHandleAuditLogDetailsRelationship() {
        // Given
        AuditLogDetail detail1 = AuditLogDetail.builder()
                .id(1L)
                .fieldName("email")
                .oldValue("old@example.com")
                .newValue("new@example.com")
                .build();

        AuditLogDetail detail2 = AuditLogDetail.builder()
                .id(2L)
                .fieldName("name")
                .oldValue("Old Name")
                .newValue("New Name")
                .build();

        List<AuditLogDetail> details = List.of(detail1, detail2);

        // When
        AuditLog auditLog = AuditLog.builder()
                .id(auditLogId)
                .actionType(ActionType.UPDATE)
                .details(details)
                .build();

        // Then
        assertAll(
                () -> assertThat(auditLog.getDetails()).hasSize(2),
                () -> assertThat(auditLog.getDetails()).contains(detail1, detail2)
        );
    }

    @Test
    @DisplayName("Should handle request context fields")
    void shouldHandleRequestContextFields() {
        // Given
        String requestId = "req-" + UUID.randomUUID().toString();
        String ipAddress = "203.0.113.1";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        String requestUrl = "https://api.example.com/users/123";

        // When
        AuditLog auditLog = AuditLog.builder()
                .requestId(requestId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .requestUrl(requestUrl)
                .requestMethod(HttpMethod.PATCH)
                .build();

        // Then
        assertAll(
                () -> assertThat(auditLog.getRequestId()).isEqualTo(requestId),
                () -> assertThat(auditLog.getIpAddress()).isEqualTo(ipAddress),
                () -> assertThat(auditLog.getUserAgent()).isEqualTo(userAgent),
                () -> assertThat(auditLog.getRequestUrl()).isEqualTo(requestUrl),
                () -> assertThat(auditLog.getRequestMethod()).isEqualTo(HttpMethod.PATCH)
        );
    }

    @Test
    @DisplayName("Should handle actor information")
    void shouldHandleActorInformation() {
        // Given
        String userId = "user-" + UUID.randomUUID().toString();
        String username = "admin@company.com";

        // When
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username)
                .build();

        // Then
        assertAll(
                () -> assertThat(auditLog.getUserId()).isEqualTo(userId),
                () -> assertThat(auditLog.getUsername()).isEqualTo(username)
        );
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        // When
        AuditLog auditLog = AuditLog.builder()
                .id(auditLogId)
                .actionType(ActionType.CREATE)
                .entityType(EntityType.USER)
                .entityId(null)
                .userId(null)
                .username(null)
                .build();

        // Then
        assertAll(
                () -> assertThat(auditLog.getId()).isEqualTo(auditLogId),
                () -> assertThat(auditLog.getEntityId()).isNull(),
                () -> assertThat(auditLog.getUserId()).isNull(),
                () -> assertThat(auditLog.getUsername()).isNull()
        );
    }

    @Test
    @DisplayName("Should handle different audit statuses")
    void shouldHandleDifferentAuditStatuses() {
        // When
        AuditLog successLog = AuditLog.builder().status(AuditStatus.SUCCESS).build();
        AuditLog failedLog = AuditLog.builder().status(AuditStatus.FAILED).errorMessage("Error occurred").build();
        AuditLog partialLog = AuditLog.builder().status(AuditStatus.PARTIAL_SUCCESS).build();

        // Then
        assertAll(
                () -> assertThat(successLog.getStatus()).isEqualTo(AuditStatus.SUCCESS),
                () -> assertThat(failedLog.getStatus()).isEqualTo(AuditStatus.FAILED),
                () -> assertThat(failedLog.getErrorMessage()).isEqualTo("Error occurred"),
                () -> assertThat(partialLog.getStatus()).isEqualTo(AuditStatus.PARTIAL_SUCCESS)
        );
    }

    @Test
    @DisplayName("Should handle error message with status")
    void shouldHandleErrorMessageWithStatus() {
        // Given
        String errorMessage = "Database connection failed";

        // When
        AuditLog errorLog = AuditLog.builder()
                .actionType(ActionType.CREATE)
                .status(AuditStatus.FAILED)
                .errorMessage(errorMessage)
                .build();

        // Then
        assertAll(
                () -> assertThat(errorLog.getStatus()).isEqualTo(AuditStatus.FAILED),
                () -> assertThat(errorLog.getErrorMessage()).isEqualTo(errorMessage)
        );
    }

    @Test
    @DisplayName("Should handle timestamp correctly")
    void shouldHandleTimestampCorrectly() {
        // Given
        Instant specificTime = Instant.parse("2024-01-15T10:30:45Z");

        // When
        AuditLog auditLog = AuditLog.builder()
                .timestamp(specificTime)
                .build();

        // Then
        assertThat(auditLog.getTimestamp()).isEqualTo(specificTime);
    }
}
