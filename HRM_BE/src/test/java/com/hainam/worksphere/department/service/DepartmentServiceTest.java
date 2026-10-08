package com.hainam.worksphere.department.service;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.department.domain.Department;
import com.hainam.worksphere.department.dto.request.CreateDepartmentRequest;
import com.hainam.worksphere.department.dto.request.UpdateDepartmentRequest;
import com.hainam.worksphere.department.dto.response.DepartmentResponse;
import com.hainam.worksphere.department.mapper.DepartmentMapper;
import com.hainam.worksphere.department.repository.DepartmentRepository;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.exception.DepartmentNotFoundException;
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
import static org.mockito.Mockito.*;

@DisplayName("DepartmentService Tests")
class DepartmentServiceTest extends BaseUnitTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentService departmentService;

    private Department testDepartment;
    private DepartmentResponse testDepartmentResponse;

    @BeforeEach
    void setUp() {
        testDepartment = TestFixtures.createTestDepartment();
        testDepartmentResponse = DepartmentResponse.builder()
                .id(testDepartment.getId())
                .name(testDepartment.getName())
                .code(testDepartment.getCode())
                .description(testDepartment.getDescription())
                .isActive(testDepartment.getIsActive())
                .createdAt(testDepartment.getCreatedAt())
                .build();
    }

    @Test
    @DisplayName("Should get department by ID successfully")
    void shouldGetDepartmentByIdSuccessfully() {
        // Given
        UUID departmentId = testDepartment.getId();
        when(departmentRepository.findActiveById(departmentId)).thenReturn(Optional.of(testDepartment));
        when(departmentMapper.toDepartmentResponse(testDepartment)).thenReturn(testDepartmentResponse);

        // When
        DepartmentResponse result = departmentService.getDepartmentById(departmentId);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isEqualTo(departmentId),
            () -> assertThat(result.getName()).isEqualTo(testDepartment.getName()),
            () -> assertThat(result.getCode()).isEqualTo(testDepartment.getCode()),
            () -> verify(departmentRepository).findActiveById(departmentId),
            () -> verify(departmentMapper).toDepartmentResponse(testDepartment)
        );
    }

    @Test
    @DisplayName("Should throw DepartmentNotFoundException when department not found")
    void shouldThrowDepartmentNotFoundExceptionWhenNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(departmentRepository.findActiveById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> departmentService.getDepartmentById(nonExistentId))
                .isInstanceOf(DepartmentNotFoundException.class);

        verify(departmentRepository).findActiveById(nonExistentId);
        verifyNoInteractions(departmentMapper);
    }

    @Test
    @DisplayName("Should get all active departments successfully")
    void shouldGetAllActiveDepartmentsSuccessfully() {
        // Given
        Department anotherDepartment = TestFixtures.createTestDepartment();
        List<Department> departments = Arrays.asList(testDepartment, anotherDepartment);

        when(departmentRepository.findAllActive()).thenReturn(departments);
        when(departmentMapper.toDepartmentResponse(any(Department.class))).thenReturn(testDepartmentResponse);

        // When
        List<DepartmentResponse> result = departmentService.getAllActiveDepartments();

        // Then
        assertAll(
            () -> assertThat(result).hasSize(2),
            () -> verify(departmentRepository).findAllActive(),
            () -> verify(departmentMapper, times(2)).toDepartmentResponse(any(Department.class))
        );
    }

    @Test
    @DisplayName("Should create department successfully")
    void shouldCreateDepartmentSuccessfully() {
        // Given
        UUID createdBy = UUID.randomUUID();
        CreateDepartmentRequest request = CreateDepartmentRequest.builder()
                .name("Engineering")
                .code("ENG")
                .description("Engineering Department")
                .build();

        Department newDepartment = TestFixtures.createTestDepartment();

        when(departmentRepository.existsActiveByCode(request.getCode())).thenReturn(false);
        when(departmentMapper.toEntity(request)).thenReturn(newDepartment);
        when(departmentRepository.save(any(Department.class))).thenReturn(newDepartment);
        when(departmentMapper.toDepartmentResponse(newDepartment)).thenReturn(testDepartmentResponse);

        // When
        DepartmentResponse result = departmentService.createDepartment(request, createdBy);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getName()).isEqualTo(testDepartmentResponse.getName()),
            () -> verify(departmentRepository).existsActiveByCode(request.getCode()),
            () -> verify(departmentMapper).toEntity(request),
            () -> verify(departmentRepository).save(any(Department.class)),
            () -> verify(departmentMapper).toDepartmentResponse(newDepartment)
        );
    }

    @Test
    @DisplayName("Should throw ValidationException when duplicate department code")
    void shouldThrowValidationExceptionWhenDuplicateCode() {
        // Given
        UUID createdBy = UUID.randomUUID();
        CreateDepartmentRequest request = CreateDepartmentRequest.builder()
                .name("Engineering")
                .code("ENG")
                .description("Engineering Department")
                .build();

        when(departmentRepository.existsActiveByCode(request.getCode())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> departmentService.createDepartment(request, createdBy))
                .isInstanceOf(ValidationException.class);

        verify(departmentRepository).existsActiveByCode(request.getCode());
        verify(departmentRepository, never()).save(any(Department.class));
        verifyNoInteractions(departmentMapper);
    }

    @Test
    @DisplayName("Should update department successfully")
    void shouldUpdateDepartmentSuccessfully() {
        // Given
        UUID departmentId = testDepartment.getId();
        UUID updatedBy = UUID.randomUUID();
        UpdateDepartmentRequest request = UpdateDepartmentRequest.builder()
                .name("Updated Engineering")
                .description("Updated Description")
                .build();

        Department updatedDepartment = TestFixtures.createTestDepartment();
        updatedDepartment.setName("Updated Engineering");

        DepartmentResponse updatedResponse = DepartmentResponse.builder()
                .id(departmentId)
                .name("Updated Engineering")
                .code(testDepartment.getCode())
                .description("Updated Description")
                .isActive(true)
                .build();

        when(departmentRepository.findActiveById(departmentId)).thenReturn(Optional.of(testDepartment));
        when(departmentRepository.save(any(Department.class))).thenReturn(updatedDepartment);
        when(departmentMapper.toDepartmentResponse(any(Department.class))).thenReturn(updatedResponse);

        // When
        DepartmentResponse result = departmentService.updateDepartment(departmentId, request, updatedBy);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getName()).isEqualTo("Updated Engineering"),
            () -> verify(departmentRepository).findActiveById(departmentId),
            () -> verify(departmentRepository).save(any(Department.class))
        );
    }

    @Test
    @DisplayName("Should soft delete department successfully")
    void shouldSoftDeleteDepartmentSuccessfully() {
        // Given
        UUID departmentId = testDepartment.getId();
        UUID deletedBy = UUID.randomUUID();

        when(departmentRepository.findActiveById(departmentId)).thenReturn(Optional.of(testDepartment));
        when(departmentRepository.save(any(Department.class))).thenReturn(testDepartment);

        // When
        departmentService.softDeleteDepartment(departmentId, deletedBy);

        // Then
        assertAll(
            () -> assertThat(testDepartment.getIsDeleted()).isTrue(),
            () -> assertThat(testDepartment.getDeletedAt()).isNotNull(),
            () -> assertThat(testDepartment.getDeletedBy()).isEqualTo(deletedBy),
            () -> verify(departmentRepository).findActiveById(departmentId),
            () -> verify(departmentRepository).save(any(Department.class))
        );
    }
}
