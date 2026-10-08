package com.hainam.worksphere.employee.service;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.department.repository.DepartmentRepository;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.domain.EmploymentStatus;
import com.hainam.worksphere.employee.dto.request.CreateEmployeeRequest;
import com.hainam.worksphere.employee.dto.request.UpdateEmployeeRequest;
import com.hainam.worksphere.employee.dto.response.EmployeeResponse;
import com.hainam.worksphere.employee.mapper.EmployeeMapper;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.user.repository.UserRepository;
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

@DisplayName("EmployeeService Tests")
class EmployeeServiceTest extends BaseUnitTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeResponse testEmployeeResponse;

    @BeforeEach
    void setUp() {
        testEmployee = TestFixtures.createTestEmployee();
        testEmployeeResponse = EmployeeResponse.builder()
                .id(testEmployee.getId())
                .employeeCode(testEmployee.getEmployeeCode())
                .firstName(testEmployee.getFirstName())
                .lastName(testEmployee.getLastName())
                .fullName(testEmployee.getFullName())
                .email(testEmployee.getEmail())
                .phone(testEmployee.getPhone())
                .position(testEmployee.getPosition())
                .employmentStatus(testEmployee.getEmploymentStatus().name())
                .createdAt(testEmployee.getCreatedAt())
                .build();
    }

    @Test
    @DisplayName("Should get employee by ID successfully")
    void shouldGetEmployeeByIdSuccessfully() {
        // Given
        UUID employeeId = testEmployee.getId();
        when(employeeRepository.findActiveById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toEmployeeResponse(testEmployee)).thenReturn(testEmployeeResponse);

        // When
        EmployeeResponse result = employeeService.getEmployeeById(employeeId);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isEqualTo(employeeId),
            () -> assertThat(result.getEmployeeCode()).isEqualTo(testEmployee.getEmployeeCode()),
            () -> assertThat(result.getEmail()).isEqualTo(testEmployee.getEmail()),
            () -> verify(employeeRepository).findActiveById(employeeId),
            () -> verify(employeeMapper).toEmployeeResponse(testEmployee)
        );
    }

    @Test
    @DisplayName("Should throw EmployeeNotFoundException when employee not found")
    void shouldThrowEmployeeNotFoundExceptionWhenNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(employeeRepository.findActiveById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> employeeService.getEmployeeById(nonExistentId))
                .isInstanceOf(EmployeeNotFoundException.class);

        verify(employeeRepository).findActiveById(nonExistentId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("Should get all active employees successfully")
    void shouldGetAllActiveEmployeesSuccessfully() {
        // Given
        Employee anotherEmployee = TestFixtures.createTestEmployee("another@example.com");
        List<Employee> employees = Arrays.asList(testEmployee, anotherEmployee);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());

        when(employeeRepository.findAllActive(pageable)).thenReturn(employeePage);
        when(employeeMapper.toEmployeeResponse(any(Employee.class))).thenReturn(testEmployeeResponse);

        // When
        Page<EmployeeResponse> result = employeeService.getAllActiveEmployees(pageable);

        // Then
        assertAll(
            () -> assertThat(result.getContent()).hasSize(2),
            () -> verify(employeeRepository).findAllActive(pageable),
            () -> verify(employeeMapper, times(2)).toEmployeeResponse(any(Employee.class))
        );
    }

    @Test
    @DisplayName("Should create employee successfully")
    void shouldCreateEmployeeSuccessfully() {
        // Given
        UUID createdBy = UUID.randomUUID();
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .employeeCode("EMP002")
                .firstName("Tran")
                .lastName("Van B")
                .email("tran.vanb@example.com")
                .phone("0909876543")
                .dateOfBirth(LocalDate.of(1992, 5, 20))
                .gender("MALE")
                .position("Backend Developer")
                .joinDate(LocalDate.of(2024, 1, 1))
                .build();

        Employee savedEmployee = TestFixtures.createTestEmployee();

        when(employeeRepository.existsActiveByEmployeeCode(request.getEmployeeCode())).thenReturn(false);
        when(employeeRepository.existsActiveByEmail(request.getEmail())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);
        when(employeeMapper.toEmployeeResponse(savedEmployee)).thenReturn(testEmployeeResponse);

        // When
        EmployeeResponse result = employeeService.createEmployee(request, createdBy);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> verify(employeeRepository).existsActiveByEmployeeCode(request.getEmployeeCode()),
            () -> verify(employeeRepository).existsActiveByEmail(request.getEmail()),
            () -> verify(employeeRepository).save(any(Employee.class)),
            () -> verify(employeeMapper).toEmployeeResponse(savedEmployee)
        );
    }

    @Test
    @DisplayName("Should throw ValidationException when duplicate employee code")
    void shouldThrowValidationExceptionWhenDuplicateEmployeeCode() {
        // Given
        UUID createdBy = UUID.randomUUID();
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .employeeCode("EMP001")
                .firstName("Tran")
                .lastName("Van B")
                .email("tran.vanb@example.com")
                .build();

        when(employeeRepository.existsActiveByEmployeeCode(request.getEmployeeCode())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> employeeService.createEmployee(request, createdBy))
                .isInstanceOf(ValidationException.class);

        verify(employeeRepository).existsActiveByEmployeeCode(request.getEmployeeCode());
        verify(employeeRepository, never()).save(any(Employee.class));
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("Should throw ValidationException when duplicate email")
    void shouldThrowValidationExceptionWhenDuplicateEmail() {
        // Given
        UUID createdBy = UUID.randomUUID();
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .employeeCode("EMP003")
                .firstName("Tran")
                .lastName("Van C")
                .email("existing@example.com")
                .build();

        when(employeeRepository.existsActiveByEmployeeCode(request.getEmployeeCode())).thenReturn(false);
        when(employeeRepository.existsActiveByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> employeeService.createEmployee(request, createdBy))
                .isInstanceOf(ValidationException.class);

        verify(employeeRepository).existsActiveByEmployeeCode(request.getEmployeeCode());
        verify(employeeRepository).existsActiveByEmail(request.getEmail());
        verify(employeeRepository, never()).save(any(Employee.class));
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("Should update employee successfully")
    void shouldUpdateEmployeeSuccessfully() {
        // Given
        UUID employeeId = testEmployee.getId();
        UUID updatedBy = UUID.randomUUID();
        UpdateEmployeeRequest request = UpdateEmployeeRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .position("Senior Engineer")
                .build();

        Employee updatedEmployee = TestFixtures.createTestEmployee();
        updatedEmployee.setFirstName("Updated");
        updatedEmployee.setLastName("Name");
        updatedEmployee.setFullName("Name Updated");
        updatedEmployee.setPosition("Senior Engineer");

        EmployeeResponse updatedResponse = EmployeeResponse.builder()
                .id(employeeId)
                .firstName("Updated")
                .lastName("Name")
                .fullName("Name Updated")
                .position("Senior Engineer")
                .employmentStatus(EmploymentStatus.ACTIVE.name())
                .build();

        when(employeeRepository.findActiveById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);
        when(employeeMapper.toEmployeeResponse(any(Employee.class))).thenReturn(updatedResponse);

        // When
        EmployeeResponse result = employeeService.updateEmployee(employeeId, request, updatedBy);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getFirstName()).isEqualTo("Updated"),
            () -> assertThat(result.getPosition()).isEqualTo("Senior Engineer"),
            () -> verify(employeeRepository).findActiveById(employeeId),
            () -> verify(employeeRepository).save(any(Employee.class))
        );
    }

    @Test
    @DisplayName("Should soft delete employee successfully")
    void shouldSoftDeleteEmployeeSuccessfully() {
        // Given
        UUID employeeId = testEmployee.getId();
        UUID deletedBy = UUID.randomUUID();

        when(employeeRepository.findActiveById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // When
        employeeService.softDeleteEmployee(employeeId, deletedBy);

        // Then
        assertAll(
            () -> assertThat(testEmployee.getIsDeleted()).isTrue(),
            () -> assertThat(testEmployee.getDeletedAt()).isNotNull(),
            () -> assertThat(testEmployee.getDeletedBy()).isEqualTo(deletedBy),
            () -> assertThat(testEmployee.getEmploymentStatus()).isEqualTo(EmploymentStatus.TERMINATED),
            () -> verify(employeeRepository).findActiveById(employeeId),
            () -> verify(employeeRepository).save(any(Employee.class))
        );
    }
}
