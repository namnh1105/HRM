package com.hainam.worksphere.employee.domain;

import com.hainam.worksphere.department.domain.Department;
import com.hainam.worksphere.shared.audit.annotation.AuditableEntity;
import com.hainam.worksphere.shared.persistence.encryption.EncryptedLocalDateConverter;
import com.hainam.worksphere.shared.persistence.encryption.EncryptedStringConverter;
import com.hainam.worksphere.store.domain.Store;
import com.hainam.worksphere.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees")
@AuditableEntity(ignoreFields = {
    "id", "updatedAt", "updatedBy", "createdAt", "createdBy",
    "isDeleted", "deletedAt", "deletedBy"
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_code", unique = true, length = 20)
    private String employeeCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "id_card_number", length = 512)
    private String idCardNumber;

    @Convert(converter = EncryptedLocalDateConverter.class)
    @Column(name = "id_card_issued_date")
    private LocalDate idCardIssuedDate;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "id_card_issued_place", length = 512)
    private String idCardIssuedPlace;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "permanent_address", columnDefinition = "TEXT")
    private String permanentAddress;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "current_address", columnDefinition = "TEXT")
    private String currentAddress;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(name = "leave_date")
    private LocalDate leaveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", length = 20)
    @Builder.Default
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "bank_account_number", length = 512)
    private String bankAccountNumber;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "bank_name", length = 512)
    private String bankName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "tax_code", length = 512)
    private String taxCode;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "social_insurance_number", length = 512)
    private String socialInsuranceNumber;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "health_insurance_number", length = 512)
    private String healthInsuranceNumber;

    @Column(name = "embedding", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String embedding;

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
