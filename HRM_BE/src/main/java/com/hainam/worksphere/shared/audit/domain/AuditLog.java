package com.hainam.worksphere.shared.audit.domain;

import com.hainam.worksphere.shared.domain.EntityType;
import com.hainam.worksphere.shared.web.HttpMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_entity", columnList = "entityType, entityId"),
        @Index(name = "idx_audit_user", columnList = "userId"),
        @Index(name = "idx_audit_action_type", columnList = "actionType"),
        @Index(name = "idx_audit_action_code", columnList = "actionCode"),
        @Index(name = "idx_audit_request", columnList = "requestId"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /* ========= Business context ========= */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionType actionType;
    // CREATE, READ, UPDATE, DELETE

    @Column(nullable = false, length = 100)
    private String actionCode;
    // UPDATE_PROFILE, ASSIGN_ROLE, LOGIN, LOGOUT...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EntityType entityType;
    // USER, EMPLOYEE, ROLE...

    @Column(length = 100)
    private String entityId;

    /* ========= Audit Details ========= */

    @OneToMany(mappedBy = "auditLog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuditLogDetail> details;

    /* ========= Actor ========= */

    @Column(length = 50)
    private String userId;

    @Column(length = 100)
    private String username;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    /* ========= Request context ========= */

    @Column(length = 50)
    private String requestId;
    // dùng để group nhiều audit trong 1 request

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private HttpMethod requestMethod;

    @Column(length = 500)
    private String requestUrl;

    /* ========= Status ========= */

    @Column(nullable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AuditStatus status;
    // SUCCESS, FAILED, PARTIAL_SUCCESS, CANCELLED

    @Column(length = 1000)
    private String errorMessage;


    /* ========= Lifecycle ========= */

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (status == null) {
            status = AuditStatus.SUCCESS;
        }
    }
}
