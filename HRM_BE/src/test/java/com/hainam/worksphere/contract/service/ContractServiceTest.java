package com.hainam.worksphere.contract.service;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.contract.domain.Contract;
import com.hainam.worksphere.contract.domain.ContractStatus;
import com.hainam.worksphere.contract.domain.ContractType;
import com.hainam.worksphere.contract.dto.request.CreateContractRequest;
import com.hainam.worksphere.contract.dto.request.UpdateContractRequest;
import com.hainam.worksphere.contract.dto.response.ContractResponse;
import com.hainam.worksphere.contract.mapper.ContractMapper;
import com.hainam.worksphere.contract.repository.ContractRepository;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.exception.ContractNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.shared.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ContractService Tests")
class ContractServiceTest extends BaseUnitTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ContractMapper contractMapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private ContractService contractService;

    private Employee testEmployee;
    private Contract testContract;
    private ContractResponse testContractResponse;
    private UUID createdBy;

    @BeforeEach
    void setUp() {
        testEmployee = TestFixtures.createTestEmployee();
        testContract = TestFixtures.createTestContract();
        testContract.setId(UUID.randomUUID());
        testContract.setEmployee(testEmployee);
        createdBy = UUID.randomUUID();

        testContractResponse = ContractResponse.builder()
                .id(testContract.getId())
                .employeeId(testEmployee.getId())
                .employeeName(testEmployee.getFullName())
                .employeeCode(testEmployee.getEmployeeCode())
                .contractType(testContract.getContractType().name())
                .startDate(testContract.getStartDate())
                .baseSalary(testContract.getBaseSalary())
                .salaryCoefficient(testContract.getSalaryCoefficient())
                .status(testContract.getStatus().name())
                .build();
    }

    @Test
    @DisplayName("Should get contract by ID successfully")
    void shouldGetContractByIdSuccessfully() {
        // Given
        UUID contractId = testContract.getId();
        when(contractRepository.findActiveById(contractId)).thenReturn(Optional.of(testContract));
        when(contractMapper.toContractResponse(testContract)).thenReturn(testContractResponse);

        // When
        ContractResponse result = contractService.getContractById(contractId);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isEqualTo(contractId),
            () -> verify(contractRepository).findActiveById(contractId),
            () -> verify(contractMapper).toContractResponse(testContract)
        );
    }

    @Test
    @DisplayName("Should throw ContractNotFoundException when contract not found")
    void shouldThrowContractNotFoundExceptionWhenNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(contractRepository.findActiveById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contractService.getContractById(nonExistentId))
                .isInstanceOf(ContractNotFoundException.class);

        verify(contractRepository).findActiveById(nonExistentId);
        verifyNoInteractions(contractMapper);
    }

    @Test
    @DisplayName("Should create contract successfully")
    void shouldCreateContractSuccessfully() {
        // Given
        CreateContractRequest request = CreateContractRequest.builder()
                .employeeId(testEmployee.getId())
                .contractType(ContractType.INDEFINITE_TERM)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .signingDate(LocalDate.of(2024, 12, 25))
                .baseSalary(18000000.0)
                .salaryCoefficient(1.2)
                .note("New contract")
                .attachmentUrl("https://example.com/contract.pdf")
                .build();
        when(employeeRepository.findActiveById(testEmployee.getId())).thenReturn(Optional.of(testEmployee));
        when(cloudinaryService.upload(null, "contracts")).thenReturn(null);
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
            Contract saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(contractMapper.toContractResponse(any(Contract.class))).thenReturn(testContractResponse);

        // When
        ContractResponse result = contractService.createContract(request, null, createdBy);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> verify(employeeRepository).findActiveById(testEmployee.getId()),
            () -> verify(contractRepository).save(any(Contract.class)),
            () -> verify(contractMapper).toContractResponse(any(Contract.class))
        );
    }

    @Test
    @DisplayName("Should throw ValidationException when end date is before start date")
    void shouldThrowValidationExceptionWhenEndDateBeforeStartDate() {
        // Given
        CreateContractRequest request = CreateContractRequest.builder()
                .employeeId(testEmployee.getId())
                .contractType(ContractType.INDEFINITE_TERM)
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 1, 1))
                .baseSalary(15000000.0)
                .build();
        when(employeeRepository.findActiveById(testEmployee.getId())).thenReturn(Optional.of(testEmployee));

        // When & Then
        assertThatThrownBy(() -> contractService.createContract(request, null, createdBy))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date must not be before start date");

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Should get contracts by employee ID successfully")
    void shouldGetContractsByEmployeeIdSuccessfully() {
        // Given
        UUID employeeId = testEmployee.getId();
        List<Contract> contracts = Arrays.asList(testContract);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contract> contractPage = new PageImpl<>(contracts, pageable, contracts.size());

        when(contractRepository.findActiveByEmployeeId(employeeId, pageable)).thenReturn(contractPage);
        when(contractMapper.toContractResponse(any(Contract.class))).thenReturn(testContractResponse);

        // When
        Page<ContractResponse> result = contractService.getByEmployeeId(employeeId, pageable);

        // Then
        assertAll(
            () -> assertThat(result.getContent()).hasSize(1),
            () -> verify(contractRepository).findActiveByEmployeeId(employeeId, pageable),
            () -> verify(contractMapper).toContractResponse(testContract)
        );
    }

    @Test
    @DisplayName("Should update contract successfully")
    void shouldUpdateContractSuccessfully() {
        // Given
        UUID contractId = testContract.getId();
        UUID updatedBy = UUID.randomUUID();
        UpdateContractRequest request = UpdateContractRequest.builder()
                .baseSalary(20000000.0)
                .salaryCoefficient(1.5)
                .note("Updated contract")
                .build();

        when(contractRepository.findActiveById(contractId)).thenReturn(Optional.of(testContract));
        when(contractRepository.save(any(Contract.class))).thenReturn(testContract);
        when(contractMapper.toContractResponse(testContract)).thenReturn(testContractResponse);

        // When
        ContractResponse result = contractService.updateContract(contractId, request, null, updatedBy);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> verify(contractRepository).findActiveById(contractId),
            () -> verify(contractRepository).save(any(Contract.class)),
            () -> verify(contractMapper).toContractResponse(testContract)
        );
    }

    @Test
    @DisplayName("Should soft delete contract successfully")
    void shouldSoftDeleteContractSuccessfully() {
        // Given
        UUID contractId = testContract.getId();
        UUID deletedBy = UUID.randomUUID();

        when(contractRepository.findActiveById(contractId)).thenReturn(Optional.of(testContract));
        when(contractRepository.save(any(Contract.class))).thenReturn(testContract);

        // When
        contractService.deleteContract(contractId, deletedBy);

        // Then
        verify(contractRepository).findActiveById(contractId);
        verify(contractRepository).save(argThat(contract ->
                contract.getIsDeleted()
                && contract.getDeletedAt() != null
                && contract.getDeletedBy().equals(deletedBy)
        ));
    }

    @Test
    @DisplayName("Should throw ContractNotFoundException when updating non-existent contract")
    void shouldThrowContractNotFoundExceptionWhenUpdatingNonExistentContract() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        UpdateContractRequest request = UpdateContractRequest.builder()
                .baseSalary(20000000.0)
                .build();

        when(contractRepository.findActiveById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contractService.updateContract(nonExistentId, request, null, updatedBy))
                .isInstanceOf(ContractNotFoundException.class);

        verify(contractRepository).findActiveById(nonExistentId);
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Should throw ContractNotFoundException when deleting non-existent contract")
    void shouldThrowContractNotFoundExceptionWhenDeletingNonExistentContract() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();

        when(contractRepository.findActiveById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contractService.deleteContract(nonExistentId, deletedBy))
                .isInstanceOf(ContractNotFoundException.class);

        verify(contractRepository).findActiveById(nonExistentId);
        verify(contractRepository, never()).save(any(Contract.class));
    }
}
