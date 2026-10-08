package com.hainam.worksphere.payroll.mapper;

import com.hainam.worksphere.payroll.domain.Payroll;
import com.hainam.worksphere.payroll.dto.response.PayrollResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PayrollMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "employeeCode", source = "employee.employeeCode")
    @Mapping(target = "totalDeductions", source = ".", qualifiedByName = "calculateTotalDeductions")
    @Mapping(target = "totalIncome", source = ".", qualifiedByName = "calculateTotalIncome")
    @Mapping(target = "netSalary", source = ".", qualifiedByName = "calculateNetSalary")
    PayrollResponse toPayrollResponse(Payroll payroll);

    @Named("calculateTotalDeductions")
    default Double calculateTotalDeductions(Payroll payroll) {
        double socialInsurance = payroll.getSocialInsurance() != null ? payroll.getSocialInsurance() : 0.0;
        double healthInsurance = payroll.getHealthInsurance() != null ? payroll.getHealthInsurance() : 0.0;
        double unemploymentInsurance = payroll.getUnemploymentInsurance() != null ? payroll.getUnemploymentInsurance() : 0.0;
        double personalIncomeTax = payroll.getPersonalIncomeTax() != null ? payroll.getPersonalIncomeTax() : 0.0;
        double latePenalty = payroll.getLatePenalty() != null ? payroll.getLatePenalty() : 0.0;
        return socialInsurance + healthInsurance + unemploymentInsurance + personalIncomeTax + latePenalty;
    }

    @Named("calculateTotalIncome")
    default Double calculateTotalIncome(Payroll payroll) {
        double baseSalary = payroll.getBaseSalary() != null ? payroll.getBaseSalary() : 0.0;
        double salaryCoefficient = payroll.getSalaryCoefficient() != null ? payroll.getSalaryCoefficient() : 1.0;
        double overtimePay = payroll.getOvertimePay() != null ? payroll.getOvertimePay() : 0.0;
        double allowance = payroll.getAllowance() != null ? payroll.getAllowance() : 0.0;
        double bonus = payroll.getBonus() != null ? payroll.getBonus() : 0.0;

        int workingDays = payroll.getWorkingDays() != null && payroll.getWorkingDays() > 0 ? payroll.getWorkingDays() : 1;
        int actualWorkingDays = payroll.getActualWorkingDays() != null ? payroll.getActualWorkingDays() : workingDays;

        double proratedSalary = (baseSalary * salaryCoefficient / workingDays) * actualWorkingDays;
        return proratedSalary + overtimePay + allowance + bonus;
    }

    @Named("calculateNetSalary")
    default Double calculateNetSalary(Payroll payroll) {
        double totalIncome = calculateTotalIncome(payroll);
        double totalDeductions = calculateTotalDeductions(payroll);
        return totalIncome - totalDeductions;
    }
}
