package com.hainam.worksphere.payroll.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePayrollRequest {

    private UUID employeeId;

    private Integer month;

    private Integer year;

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

    private String note;
}
