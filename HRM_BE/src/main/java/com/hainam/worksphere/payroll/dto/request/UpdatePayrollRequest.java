package com.hainam.worksphere.payroll.dto.request;

import com.hainam.worksphere.payroll.domain.PayrollStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePayrollRequest {

    private Double baseSalary;

    private Double salaryCoefficient;

    private Integer workingDays;

    private Integer actualWorkingDays;

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

    private PayrollStatus status;

    private LocalDate paymentDate;

    private String note;
}
