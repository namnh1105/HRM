package com.hainam.worksphere.payroll.domain;

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
@Table(name = "payrolls", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "month", "year"})
})
@AuditableEntity(ignoreFields = {
        "id", "updatedAt", "updatedBy", "createdAt", "createdBy",
        "isDeleted", "deletedAt", "deletedBy"
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "base_salary")
    private Double baseSalary;

    @Column(name = "salary_coefficient")
    @Builder.Default
    private Double salaryCoefficient = 1.0;

    @Column(name = "working_days")
    private Integer workingDays;

    @Column(name = "actual_working_days")
    private Integer actualWorkingDays;

    @Column(name = "working_hours")
    @Builder.Default
    private Double workingHours = 0.0;

    @Column(name = "overtime_hours")
    @Builder.Default
    private Double overtimeHours = 0.0;

    @Column(name = "overtime_pay")
    @Builder.Default
    private Double overtimePay = 0.0;

    @Column(name = "allowance")
    @Builder.Default
    private Double allowance = 0.0;

    @Column(name = "bonus")
    @Builder.Default
    private Double bonus = 0.0;

    @Column(name = "social_insurance")
    @Builder.Default
    private Double socialInsurance = 0.0;

    @Column(name = "health_insurance")
    @Builder.Default
    private Double healthInsurance = 0.0;

    @Column(name = "unemployment_insurance")
    @Builder.Default
    private Double unemploymentInsurance = 0.0;

    @Column(name = "personal_income_tax")
    @Builder.Default
    private Double personalIncomeTax = 0.0;

        @Column(name = "late_penalty")
        @Builder.Default
        private Double latePenalty = 0.0;

        @Column(name = "late_count")
        @Builder.Default
        private Integer lateCount = 0;

    @Column(name = "total_deductions")
    @Builder.Default
    private Double totalDeductions = 0.0;

    @Column(name = "total_income")
    @Builder.Default
    private Double totalIncome = 0.0;

    @Column(name = "net_salary")
    @Builder.Default
    private Double netSalary = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

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
