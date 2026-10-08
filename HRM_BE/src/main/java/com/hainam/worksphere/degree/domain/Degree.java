package com.hainam.worksphere.degree.domain;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.shared.audit.annotation.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "degrees")
@AuditableEntity(ignoreFields = {
    "id", "updatedAt", "updatedBy", "createdAt", "createdBy",
    "isDeleted", "deletedAt", "deletedBy"
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Degree {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "degree_name", nullable = false, length = 100)
    private String degreeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "degree_level", length = 20)
    private DegreeLevel degreeLevel;

    @Column(name = "major", length = 100)
    private String major;

    @Column(name = "institution", length = 200)
    private String institution;

    @Column(name = "graduation_date")
    private LocalDate graduationDate;

    @Column(name = "gpa")
    private Double gpa;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;
}
