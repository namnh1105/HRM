package com.hainam.worksphere.payroll.service;

import com.hainam.worksphere.contract.domain.Contract;
import com.hainam.worksphere.contract.domain.ContractStatus;
import com.hainam.worksphere.contract.repository.ContractRepository;
import com.hainam.worksphere.attendance.repository.AttendanceRepository;
import com.hainam.worksphere.leave.repository.LeaveRequestRepository;
import com.hainam.worksphere.leave.domain.LeaveRequestStatus;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.payroll.domain.Payroll;
import com.hainam.worksphere.payroll.domain.PayrollStatus;
import com.hainam.worksphere.payroll.dto.request.AutoGeneratePayrollRequest;
import com.hainam.worksphere.payroll.dto.request.CreatePayrollRequest;
import com.hainam.worksphere.payroll.dto.request.UpdatePayrollRequest;
import com.hainam.worksphere.payroll.dto.response.PayrollResponse;
import com.hainam.worksphere.payroll.mapper.PayrollMapper;
import com.hainam.worksphere.payroll.repository.PayrollRepository;
import com.hainam.worksphere.notification.domain.NotificationType;
import com.hainam.worksphere.notification.service.NotificationService;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import com.hainam.worksphere.shared.exception.PayrollNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import com.hainam.worksphere.workshift.repository.EmployeeWorkShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final PayrollMapper payrollMapper;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeWorkShiftRepository employeeWorkShiftRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationService notificationService;

    @Cacheable(value = CacheConfig.PAYROLL_CACHE, key = "'all'")
    public List<PayrollResponse> getAllPayrolls() {
        return payrollRepository.findAllActive()
                .stream()
                .map(payrollMapper::toPayrollResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.PAYROLL_CACHE, key = "'id:' + #id")
    public PayrollResponse getPayrollById(UUID id) {
        Payroll payroll = payrollRepository.findActiveById(id)
                .orElseThrow(() -> PayrollNotFoundException.byId(id.toString()));
        return payrollMapper.toPayrollResponse(payroll);
    }

    @Cacheable(value = CacheConfig.PAYROLL_CACHE, key = "'employee:' + #employeeId")
    public List<PayrollResponse> getByEmployeeId(UUID employeeId) {
        return payrollRepository.findActiveByEmployeeId(employeeId)
                .stream()
                .map(payrollMapper::toPayrollResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.PAYROLL_CACHE, key = "'month:' + #month + ':year:' + #year")
    public List<PayrollResponse> getByMonthAndYear(Integer month, Integer year) {
        return payrollRepository.findActiveByMonthAndYear(month, year)
                .stream()
                .map(payrollMapper::toPayrollResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = CacheConfig.PAYROLL_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "PAYROLL")
    public PayrollResponse createPayroll(CreatePayrollRequest request, UUID createdBy) {
        Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                .orElseThrow(() -> EmployeeNotFoundException.byId(request.getEmployeeId().toString()));

        payrollRepository.findActiveByEmployeeIdAndMonthAndYear(
                request.getEmployeeId(), request.getMonth(), request.getYear()).ifPresent(existing -> {
                    throw new ValidationException("Payroll already exists for this employee in " + request.getMonth()
                            + "/" + request.getYear());
                });

        // Resolve baseSalary: use provided value, or look up from active contract
        Double baseSalary = request.getBaseSalary();
        if (baseSalary == null) {
            baseSalary = contractRepository
                    .findActiveByEmployeeIdAndStatus(request.getEmployeeId(), ContractStatus.ACTIVE)
                    .stream()
                    .findFirst()
                    .map(Contract::getBaseSalary)
                    .orElseThrow(() -> new ValidationException(
                            "No active contract found for employee. Please provide base_salary or create an employee contract record."));
        }

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .month(request.getMonth())
                .year(request.getYear())
                .baseSalary(baseSalary)
                .salaryCoefficient(request.getSalaryCoefficient() != null ? request.getSalaryCoefficient() : 1.0)
                .workingDays(request.getWorkingDays())
                .actualWorkingDays(request.getActualWorkingDays())
                .overtimeHours(request.getOvertimeHours() != null ? request.getOvertimeHours() : 0.0)
                .overtimePay(request.getOvertimePay() != null ? request.getOvertimePay() : 0.0)
                .allowance(request.getAllowance() != null ? request.getAllowance() : 0.0)
                .bonus(request.getBonus() != null ? request.getBonus() : 0.0)
                .socialInsurance(request.getSocialInsurance() != null ? request.getSocialInsurance() : 0.0)
                .healthInsurance(request.getHealthInsurance() != null ? request.getHealthInsurance() : 0.0)
                .unemploymentInsurance(
                        request.getUnemploymentInsurance() != null ? request.getUnemploymentInsurance() : 0.0)
                .personalIncomeTax(request.getPersonalIncomeTax() != null ? request.getPersonalIncomeTax() : 0.0)
                .latePenalty(request.getLatePenalty() != null ? request.getLatePenalty() : 0.0)
                .lateCount(request.getLateCount() != null ? request.getLateCount() : 0)
                .note(request.getNote())
                .status(PayrollStatus.DRAFT)
                .createdBy(createdBy)
                .build();

        calculatePayroll(payroll);

        Payroll saved = payrollRepository.save(payroll);
        AuditContext.registerCreated(saved);

        if (saved.getEmployee().getUser() != null) {
            notificationService.sendNotification(
                    saved.getEmployee().getUser().getId(),
                    NotificationType.SALARY,
                    "New Salary Slip",
                    String.format("Your salary slip for %d/%d has been generated.", saved.getMonth(), saved.getYear()));
        }

        return payrollMapper.toPayrollResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.PAYROLL_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "PAYROLL")
    public PayrollResponse updatePayroll(UUID id, UpdatePayrollRequest request, UUID updatedBy) {
        Payroll payroll = payrollRepository.findActiveById(id)
                .orElseThrow(() -> PayrollNotFoundException.byId(id.toString()));

        AuditContext.snapshot(payroll);

        if (request.getBaseSalary() != null) {
            payroll.setBaseSalary(request.getBaseSalary());
        }
        if (request.getSalaryCoefficient() != null) {
            payroll.setSalaryCoefficient(request.getSalaryCoefficient());
        }
        if (request.getWorkingDays() != null) {
            payroll.setWorkingDays(request.getWorkingDays());
        }
        if (request.getActualWorkingDays() != null) {
            payroll.setActualWorkingDays(request.getActualWorkingDays());
        }
        if (request.getOvertimeHours() != null) {
            payroll.setOvertimeHours(request.getOvertimeHours());
        }
        if (request.getOvertimePay() != null) {
            payroll.setOvertimePay(request.getOvertimePay());
        }
        if (request.getAllowance() != null) {
            payroll.setAllowance(request.getAllowance());
        }
        if (request.getBonus() != null) {
            payroll.setBonus(request.getBonus());
        }
        if (request.getSocialInsurance() != null) {
            payroll.setSocialInsurance(request.getSocialInsurance());
        }
        if (request.getHealthInsurance() != null) {
            payroll.setHealthInsurance(request.getHealthInsurance());
        }
        if (request.getUnemploymentInsurance() != null) {
            payroll.setUnemploymentInsurance(request.getUnemploymentInsurance());
        }
        if (request.getPersonalIncomeTax() != null) {
            payroll.setPersonalIncomeTax(request.getPersonalIncomeTax());
        }
        if (request.getLatePenalty() != null) {
            payroll.setLatePenalty(request.getLatePenalty());
        }
        if (request.getLateCount() != null) {
            payroll.setLateCount(request.getLateCount());
        }
        if (request.getStatus() != null) {
            payroll.setStatus(request.getStatus());
        }
        if (request.getPaymentDate() != null) {
            payroll.setPaymentDate(request.getPaymentDate());
        }
        if (request.getNote() != null) {
            payroll.setNote(request.getNote());
        }

        payroll.setUpdatedBy(updatedBy);
        calculatePayroll(payroll);

        Payroll saved = payrollRepository.save(payroll);
        AuditContext.registerUpdated(saved);

        if (saved.getEmployee().getUser() != null) {
            notificationService.sendNotification(
                    saved.getEmployee().getUser().getId(),
                    NotificationType.SALARY,
                    "Salary Slip Updated",
                    String.format("Your salary slip for %d/%d has been updated.", saved.getMonth(), saved.getYear()));
        }

        return payrollMapper.toPayrollResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.PAYROLL_CACHE, allEntries = true)
    @AuditAction(type = ActionType.CREATE, entity = "PAYROLL", actionCode = "AUTO_GENERATE_PAYROLLS")
    public List<PayrollResponse> autoGeneratePayrolls(AutoGeneratePayrollRequest request, UUID createdBy) {
        Integer month = request.getMonth();
        Integer year = request.getYear();
        if (month == null || year == null) {
            throw new ValidationException("Month and year are required for payroll generation");
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        double latePenaltyPerShift = request.getLatePenaltyPerShift() != null ? request.getLatePenaltyPerShift() : 0.0;
        double latePenaltyPerHour = request.getLatePenaltyPerHour() != null ? request.getLatePenaltyPerHour() : 0.0;
        double requestAllowance = request.getAllowance() != null ? request.getAllowance() : 0.0;
        boolean overwriteExisting = request.getOverwriteExisting() != null && request.getOverwriteExisting();

        List<Employee> employees = employeeRepository.findAllActiveList();
        List<PayrollResponse> results = new ArrayList<>();
        int skippedNoContract = 0;
        int skippedNoAttendance = 0;

        for (Employee employee : employees) {
            Payroll existing = payrollRepository.findActiveByEmployeeIdAndMonthAndYear(employee.getId(), month, year)
                    .orElse(null);

            if (existing != null && !overwriteExisting) {
                results.add(payrollMapper.toPayrollResponse(existing));
                continue;
            }

            if (existing != null && existing.getStatus() != PayrollStatus.DRAFT) {
                results.add(payrollMapper.toPayrollResponse(existing));
                continue;
            }

            Contract contract = contractRepository
                    .findActiveByEmployeeIdAndStatus(employee.getId(), ContractStatus.ACTIVE)
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (contract == null) {
                skippedNoContract++;
                continue;
            }

            long scheduledShifts = employeeWorkShiftRepository.countActiveByEmployeeIdAndDateBetween(
                    employee.getId(), startDate, endDate);
            long attendedShifts = attendanceRepository.countPresentByEmployeeIdAndWorkDateBetween(
                    employee.getId(), startDate, endDate);
            long lateCount = attendanceRepository.countLateByEmployeeIdAndWorkDateBetween(
                    employee.getId(), startDate, endDate);
            double workingHours = attendanceRepository.sumWorkingHoursByEmployeeIdAndWorkDateBetween(
                    employee.getId(), startDate, endDate);

            if (scheduledShifts == 0 && attendedShifts == 0) {
                skippedNoAttendance++;
                continue;
            }

            double contractBaseSalary = contract.getBaseSalary() != null ? contract.getBaseSalary() : 0.0;
            double contractAllowance = contract.getAllowance() != null ? contract.getAllowance() : 0.0;
            double totalAllowance = requestAllowance + contractAllowance;

            double baseSalaryForMonth;
            double calculatedLatePenalty;

            if (contract.getContractType() == com.hainam.worksphere.contract.domain.ContractType.MONTHLY) {
                long lateMinutes = attendanceRepository.sumLateMinutesByEmployeeIdAndWorkDateBetween(
                        employee.getId(), startDate, endDate);

                long approvedLeavesCount = leaveRequestRepository.findActiveByEmployeeIdAndStartDateBetween(
                        employee.getId(), startDate, endDate)
                        .stream()
                        .filter(lr -> lr.getStatus() == LeaveRequestStatus.APPROVED)
                        .mapToLong(lr -> java.time.temporal.ChronoUnit.DAYS.between(lr.getStartDate(), lr.getEndDate())
                                + 1)
                        .sum();

                long presentDays = attendedShifts + approvedLeavesCount;
                if (scheduledShifts > 0) {
                    baseSalaryForMonth = contractBaseSalary * presentDays / scheduledShifts;
                } else {
                    baseSalaryForMonth = contractBaseSalary;
                }
                calculatedLatePenalty = (lateMinutes / 60.0) * latePenaltyPerHour;
            } else {
                baseSalaryForMonth = contractBaseSalary * (scheduledShifts > 0 ? scheduledShifts : attendedShifts);
                calculatedLatePenalty = latePenaltyPerShift * lateCount;
            }

            Payroll payroll = existing != null ? existing
                    : Payroll.builder()
                            .employee(employee)
                            .month(month)
                            .year(year)
                            .status(PayrollStatus.DRAFT)
                            .createdBy(createdBy)
                            .build();

            payroll.setBaseSalary(baseSalaryForMonth);
            payroll.setSalaryCoefficient(
                    contract.getSalaryCoefficient() != null ? contract.getSalaryCoefficient() : 1.0);
            payroll.setWorkingDays((int) (scheduledShifts > 0 ? scheduledShifts : 1));
            payroll.setActualWorkingDays((int) attendedShifts);
            payroll.setWorkingHours(workingHours);
            payroll.setOvertimeHours(0.0);
            payroll.setOvertimePay(0.0);
            payroll.setAllowance(totalAllowance);
            payroll.setBonus(0.0);
            payroll.setLatePenalty(calculatedLatePenalty);
            payroll.setLateCount((int) lateCount);
            payroll.setUpdatedBy(createdBy);

            calculatePayroll(payroll);

            Payroll saved = payrollRepository.save(payroll);
            if (existing == null) {
                AuditContext.registerCreated(saved);
            } else {
                AuditContext.registerUpdated(saved);
            }

            results.add(payrollMapper.toPayrollResponse(saved));
        }

        if (results.isEmpty()) {
            throw new ValidationException(
                    "Không tạo được bảng lương: không có nhân viên đủ điều kiện (thiếu hợp đồng hoặc không có chấm công). "
                            +
                            "Thiếu hợp đồng: " + skippedNoContract + ", không có chấm công: " + skippedNoAttendance);
        }

        return results;
    }

    @Transactional
    @CacheEvict(value = CacheConfig.PAYROLL_CACHE, allEntries = true)
    @AuditAction(type = ActionType.DELETE, entity = "PAYROLL")
    public void deletePayroll(UUID id, UUID deletedBy) {
        Payroll payroll = payrollRepository.findActiveById(id)
                .orElseThrow(() -> PayrollNotFoundException.byId(id.toString()));

        AuditContext.registerDeleted(payroll);

        payroll.setIsDeleted(true);
        payroll.setDeletedAt(Instant.now());
        payroll.setDeletedBy(deletedBy);
        payrollRepository.save(payroll);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.PAYROLL_CACHE, allEntries = true)
    @AuditAction(type = ActionType.UPDATE, entity = "PAYROLL", actionCode = "CONFIRM_PAYROLL")
    public PayrollResponse confirmPayroll(UUID id, UUID updatedBy) {
        Payroll payroll = payrollRepository.findActiveById(id)
                .orElseThrow(() -> PayrollNotFoundException.byId(id.toString()));

        AuditContext.snapshot(payroll);

        if (payroll.getStatus() != PayrollStatus.DRAFT) {
            throw new ValidationException("Only DRAFT payrolls can be confirmed");
        }

        payroll.setStatus(PayrollStatus.CONFIRMED);
        payroll.setUpdatedBy(updatedBy);

        Payroll saved = payrollRepository.save(payroll);
        AuditContext.registerUpdated(saved);
        return payrollMapper.toPayrollResponse(saved);
    }

    /**
     * Auto-calculate totalDeductions, totalIncome, and netSalary on the entity.
     */
    private void calculatePayroll(Payroll payroll) {
        double baseSalary = payroll.getBaseSalary() != null ? payroll.getBaseSalary() : 0.0;
        double salaryCoefficient = payroll.getSalaryCoefficient() != null ? payroll.getSalaryCoefficient() : 1.0;
        double overtimePay = payroll.getOvertimePay() != null ? payroll.getOvertimePay() : 0.0;
        double allowance = payroll.getAllowance() != null ? payroll.getAllowance() : 0.0;
        double bonus = payroll.getBonus() != null ? payroll.getBonus() : 0.0;

        int workingDays = payroll.getWorkingDays() != null && payroll.getWorkingDays() > 0 ? payroll.getWorkingDays()
                : 1;
        int actualWorkingDays = payroll.getActualWorkingDays() != null ? payroll.getActualWorkingDays() : workingDays;

        double proratedSalary = (baseSalary * salaryCoefficient / workingDays) * actualWorkingDays;
        double totalIncome = proratedSalary + overtimePay + allowance + bonus;

        double socialInsurance = payroll.getSocialInsurance() != null ? payroll.getSocialInsurance() : 0.0;
        double healthInsurance = payroll.getHealthInsurance() != null ? payroll.getHealthInsurance() : 0.0;
        double unemploymentInsurance = payroll.getUnemploymentInsurance() != null ? payroll.getUnemploymentInsurance()
                : 0.0;
        double personalIncomeTax = payroll.getPersonalIncomeTax() != null ? payroll.getPersonalIncomeTax() : 0.0;
        double latePenalty = payroll.getLatePenalty() != null ? payroll.getLatePenalty() : 0.0;
        double totalDeductions = socialInsurance + healthInsurance + unemploymentInsurance + personalIncomeTax
                + latePenalty;

        double netSalary = totalIncome - totalDeductions;

        payroll.setTotalIncome(totalIncome);
        payroll.setTotalDeductions(totalDeductions);
        payroll.setNetSalary(netSalary);
    }
}
