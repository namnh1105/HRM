package com.hainam.worksphere.payroll.dto.response;

import com.hainam.worksphere.payroll.domain.PayrollStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResponse {

    private UUID id;

    private UUID employeeId;

    private String employeeName;

    private String employeeCode;

    private Integer month;

    private Integer year;

    private Double baseSalary;

    private Double salaryCoefficient;

    private Integer workingDays;

    private Integer actualWorkingDays;

    private Double workingHours;

    private Double overtimeHours;

    private Double overtimePay;

    private Double allowance;

    private Double bonus;

    private Double socialInsurance;

    private Double healthInsurance;

    private Double unemploymentInsurance;

    private Double personalIncomeTax;

    private Double latePenalty;

    private Integer lateCount;

    private Double totalDeductions;

    private Double totalIncome;

    private Double netSalary;

    private PayrollStatus status;

    private String note;

    private LocalDate paymentDate;

    private Instant createdAt;

    private Instant updatedAt;

    private UUID createdBy;

    private UUID updatedBy;
}
