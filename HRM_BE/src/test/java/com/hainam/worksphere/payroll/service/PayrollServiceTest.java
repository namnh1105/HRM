package com.hainam.worksphere.payroll.service;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.contract.repository.ContractRepository;
import com.hainam.worksphere.payroll.domain.Payroll;
import com.hainam.worksphere.payroll.domain.PayrollStatus;
import com.hainam.worksphere.payroll.dto.request.CreatePayrollRequest;
import com.hainam.worksphere.payroll.dto.request.UpdatePayrollRequest;
import com.hainam.worksphere.payroll.dto.response.PayrollResponse;
import com.hainam.worksphere.payroll.mapper.PayrollMapper;
import com.hainam.worksphere.payroll.repository.PayrollRepository;
import com.hainam.worksphere.shared.exception.PayrollNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("PayrollService Tests")
class PayrollServiceTest extends BaseUnitTest {

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private PayrollMapper payrollMapper;

    @InjectMocks
    private PayrollService payrollService;

    private Employee testEmployee;
    private Payroll testPayroll;
    private PayrollResponse testPayrollResponse;
    private UUID createdBy;

    @BeforeEach
    void setUp() {
        testEmployee = TestFixtures.createTestEmployee();
        testPayroll = TestFixtures.createTestPayroll();
        testPayroll.setId(UUID.randomUUID());
        testPayroll.setEmployee(testEmployee);
        createdBy = UUID.randomUUID();

        testPayrollResponse = PayrollResponse.builder()
                .id(testPayroll.getId())
                .employeeId(testEmployee.getId())
                .employeeName(testEmployee.getFullName())
                .employeeCode(testEmployee.getEmployeeCode())
                .month(testPayroll.getMonth())
                .year(testPayroll.getYear())
                .baseSalary(testPayroll.getBaseSalary())
                .salaryCoefficient(testPayroll.getSalaryCoefficient())
                .workingDays(testPayroll.getWorkingDays())
                .actualWorkingDays(testPayroll.getActualWorkingDays())
                .status(testPayroll.getStatus())
                .build();
    }

    @Test
    @DisplayName("Should get payroll by ID successfully")
    void shouldGetPayrollByIdSuccessfully() {
        // Given
        UUID payrollId = testPayroll.getId();
        when(payrollRepository.findActiveById(payrollId)).thenReturn(Optional.of(testPayroll));
        when(payrollMapper.toPayrollResponse(testPayroll)).thenReturn(testPayrollResponse);

        // When
        PayrollResponse result = payrollService.getPayrollById(payrollId);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isEqualTo(payrollId),
            () -> assertThat(result.getEmployeeId()).isEqualTo(testEmployee.getId()),
            () -> verify(payrollRepository).findActiveById(payrollId),
            () -> verify(payrollMapper).toPayrollResponse(testPayroll)
        );
    }

    @Test
    @DisplayName("Should throw PayrollNotFoundException when payroll not found")
    void shouldThrowPayrollNotFoundExceptionWhenNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(payrollRepository.findActiveById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> payrollService.getPayrollById(nonExistentId))
                .isInstanceOf(PayrollNotFoundException.class);

        verify(payrollRepository).findActiveById(nonExistentId);
        verifyNoInteractions(payrollMapper);
    }

    @Test
    @DisplayName("Should create payroll successfully")
    void shouldCreatePayrollSuccessfully() {
        // Given
        CreatePayrollRequest request = CreatePayrollRequest.builder()
                .employeeId(testEmployee.getId())
                .month(1)
                .year(2025)
                .baseSalary(10000000.0)
                .salaryCoefficient(1.0)
                .workingDays(20)
                .actualWorkingDays(20)
                .overtimeHours(0.0)
                .overtimePay(0.0)
                .allowance(500000.0)
                .bonus(1000000.0)
                .socialInsurance(800000.0)
                .healthInsurance(150000.0)
                .unemploymentInsurance(100000.0)
                .personalIncomeTax(500000.0)
                .note("Test payroll")
                .build();

        when(employeeRepository.findActiveById(testEmployee.getId())).thenReturn(Optional.of(testEmployee));
        when(payrollRepository.findActiveByEmployeeIdAndMonthAndYear(testEmployee.getId(), 1, 2025))
                .thenReturn(Optional.empty());
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> {
            Payroll saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(payrollMapper.toPayrollResponse(any(Payroll.class))).thenReturn(testPayrollResponse);

        // When
        PayrollResponse result = payrollService.createPayroll(request, createdBy);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> verify(employeeRepository).findActiveById(testEmployee.getId()),
            () -> verify(payrollRepository).findActiveByEmployeeIdAndMonthAndYear(testEmployee.getId(), 1, 2025)
        );

        // Verify auto-calculated fields via the saved payroll
        // proratedSalary = (10000000 * 1.0 / 20) * 20 = 10000000
        // totalIncome = 10000000 + 0 + 500000 + 1000000 = 11500000
        // totalDeductions = 800000 + 150000 + 100000 + 500000 = 1550000
        // netSalary = 11500000 - 1550000 = 9950000
        verify(payrollRepository).save(argThat(payroll ->
                payroll.getTotalIncome() == 11500000.0
                && payroll.getTotalDeductions() == 1550000.0
                && payroll.getNetSalary() == 9950000.0
                && payroll.getStatus() == PayrollStatus.DRAFT
        ));
    }

    @Test
    @DisplayName("Should throw ValidationException when duplicate payroll exists")
    void shouldThrowValidationExceptionWhenDuplicatePayrollExists() {
        // Given
        CreatePayrollRequest request = CreatePayrollRequest.builder()
                .employeeId(testEmployee.getId())
                .month(1)
                .year(2025)
                .baseSalary(15000000.0)
                .workingDays(22)
                .actualWorkingDays(22)
                .build();

        when(employeeRepository.findActiveById(testEmployee.getId())).thenReturn(Optional.of(testEmployee));
        when(payrollRepository.findActiveByEmployeeIdAndMonthAndYear(testEmployee.getId(), 1, 2025))
                .thenReturn(Optional.of(testPayroll));

        // When & Then
        assertThatThrownBy(() -> payrollService.createPayroll(request, createdBy))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Payroll already exists");

        verify(payrollRepository, never()).save(any(Payroll.class));
    }

    @Test
    @DisplayName("Should get payrolls by employee ID successfully")
    void shouldGetPayrollsByEmployeeIdSuccessfully() {
        // Given
        UUID employeeId = testEmployee.getId();
        List<Payroll> payrolls = Arrays.asList(testPayroll);
        when(payrollRepository.findActiveByEmployeeId(employeeId)).thenReturn(payrolls);
        when(payrollMapper.toPayrollResponse(any(Payroll.class))).thenReturn(testPayrollResponse);

        // When
        List<PayrollResponse> result = payrollService.getByEmployeeId(employeeId);

        // Then
        assertAll(
            () -> assertThat(result).hasSize(1),
            () -> verify(payrollRepository).findActiveByEmployeeId(employeeId),
            () -> verify(payrollMapper).toPayrollResponse(testPayroll)
        );
    }

    @Test
    @DisplayName("Should get payrolls by month and year successfully")
    void shouldGetPayrollsByMonthAndYearSuccessfully() {
        // Given
        Integer month = 1;
        Integer year = 2025;
        List<Payroll> payrolls = Arrays.asList(testPayroll);
        when(payrollRepository.findActiveByMonthAndYear(month, year)).thenReturn(payrolls);
        when(payrollMapper.toPayrollResponse(any(Payroll.class))).thenReturn(testPayrollResponse);

        // When
        List<PayrollResponse> result = payrollService.getByMonthAndYear(month, year);

        // Then
        assertAll(
            () -> assertThat(result).hasSize(1),
            () -> verify(payrollRepository).findActiveByMonthAndYear(month, year),
            () -> verify(payrollMapper).toPayrollResponse(testPayroll)
        );
    }

    @Test
    @DisplayName("Should update payroll successfully")
    void shouldUpdatePayrollSuccessfully() {
        // Given
        UUID payrollId = testPayroll.getId();
        UUID updatedBy = UUID.randomUUID();
        UpdatePayrollRequest request = UpdatePayrollRequest.builder()
                .baseSalary(18000000.0)
                .bonus(2000000.0)
                .note("Updated payroll")
                .build();

        when(payrollRepository.findActiveById(payrollId)).thenReturn(Optional.of(testPayroll));
        when(payrollRepository.save(any(Payroll.class))).thenReturn(testPayroll);
        when(payrollMapper.toPayrollResponse(testPayroll)).thenReturn(testPayrollResponse);

        // When
        PayrollResponse result = payrollService.updatePayroll(payrollId, request, updatedBy);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> verify(payrollRepository).findActiveById(payrollId),
            () -> verify(payrollRepository).save(any(Payroll.class)),
            () -> verify(payrollMapper).toPayrollResponse(testPayroll)
        );
    }

    @Test
    @DisplayName("Should confirm payroll successfully")
    void shouldConfirmPayrollSuccessfully() {
        // Given
        UUID payrollId = testPayroll.getId();
        UUID updatedBy = UUID.randomUUID();
        testPayroll.setStatus(PayrollStatus.DRAFT);

        PayrollResponse confirmedResponse = PayrollResponse.builder()
                .id(payrollId)
                .status(PayrollStatus.CONFIRMED)
                .build();

        when(payrollRepository.findActiveById(payrollId)).thenReturn(Optional.of(testPayroll));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(payrollMapper.toPayrollResponse(any(Payroll.class))).thenReturn(confirmedResponse);

        // When
        PayrollResponse result = payrollService.confirmPayroll(payrollId, updatedBy);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getStatus()).isEqualTo(PayrollStatus.CONFIRMED),
            () -> verify(payrollRepository).findActiveById(payrollId),
            () -> verify(payrollRepository).save(any(Payroll.class))
        );
    }

    @Test
    @DisplayName("Should throw ValidationException when confirming non-DRAFT payroll")
    void shouldThrowValidationExceptionWhenConfirmingNonDraftPayroll() {
        // Given
        UUID payrollId = testPayroll.getId();
        UUID updatedBy = UUID.randomUUID();
        testPayroll.setStatus(PayrollStatus.CONFIRMED);

        when(payrollRepository.findActiveById(payrollId)).thenReturn(Optional.of(testPayroll));

        // When & Then
        assertThatThrownBy(() -> payrollService.confirmPayroll(payrollId, updatedBy))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Only DRAFT payrolls can be confirmed");

        verify(payrollRepository).findActiveById(payrollId);
        verify(payrollRepository, never()).save(any(Payroll.class));
    }

    @Test
    @DisplayName("Should soft delete payroll successfully")
    void shouldSoftDeletePayrollSuccessfully() {
        // Given
        UUID payrollId = testPayroll.getId();
        UUID deletedBy = UUID.randomUUID();

        when(payrollRepository.findActiveById(payrollId)).thenReturn(Optional.of(testPayroll));
        when(payrollRepository.save(any(Payroll.class))).thenReturn(testPayroll);

        // When
        payrollService.deletePayroll(payrollId, deletedBy);

        // Then
        verify(payrollRepository).findActiveById(payrollId);
        verify(payrollRepository).save(argThat(payroll ->
                payroll.getIsDeleted()
                && payroll.getDeletedAt() != null
                && payroll.getDeletedBy().equals(deletedBy)
        ));
    }
}
