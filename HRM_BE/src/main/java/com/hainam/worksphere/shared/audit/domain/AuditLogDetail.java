package com.hainam.worksphere.shared.audit.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
    name = "audit_log_details",
    indexes = {
        @Index(name = "idx_audit_detail_log_id", columnList = "auditLogId"),
        @Index(name = "idx_audit_detail_field", columnList = "fieldName"),
        @Index(name = "idx_audit_detail_log_field", columnList = "auditLogId, fieldName")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "audit_log_id", columnDefinition = "uuid")
    private UUID auditLogId;

    @Column(length = 100)
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_log_id", insertable = false, updatable = false)
    private AuditLog auditLog;
}
