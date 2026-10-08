package com.hainam.worksphere.department.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.department.dto.request.CreateDepartmentRequest;
import com.hainam.worksphere.department.dto.request.UpdateDepartmentRequest;
import com.hainam.worksphere.department.dto.response.DepartmentResponse;
import com.hainam.worksphere.department.service.DepartmentService;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management")
@SecurityRequirement(name = "Bearer Authentication")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @Operation(summary = "Get all active departments")
    @RequirePermission(PermissionType.VIEW_DEPARTMENT)
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        List<DepartmentResponse> response = departmentService.getAllActiveDepartments();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{departmentId}")
    @Operation(summary = "Get department by ID")
    @RequirePermission(PermissionType.VIEW_DEPARTMENT)
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(
            @PathVariable UUID departmentId
    ) {
        DepartmentResponse response = departmentService.getDepartmentById(departmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{departmentId}/sub-departments")
    @Operation(summary = "Get sub-departments")
    @RequirePermission(PermissionType.VIEW_DEPARTMENT)
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getSubDepartments(
            @PathVariable UUID departmentId
    ) {
        List<DepartmentResponse> response = departmentService.getSubDepartments(departmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get departments by store")
    @RequirePermission(PermissionType.VIEW_DEPARTMENT)
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getDepartmentsByStore(
            @PathVariable UUID storeId
    ) {
        List<DepartmentResponse> response = departmentService.getDepartmentsByStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create a new department")
    @RequirePermission(PermissionType.CREATE_DEPARTMENT)
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        DepartmentResponse response = departmentService.createDepartment(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Department created successfully", response));
    }

    @PutMapping("/{departmentId}")
    @Operation(summary = "Update department")
    @RequirePermission(PermissionType.UPDATE_DEPARTMENT)
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable UUID departmentId,
            @Valid @RequestBody UpdateDepartmentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        DepartmentResponse response = departmentService.updateDepartment(departmentId, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", response));
    }

    @DeleteMapping("/{departmentId}")
    @Operation(summary = "Soft delete department")
    @RequirePermission(PermissionType.DELETE_DEPARTMENT)
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        departmentService.softDeleteDepartment(departmentId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search departments by keyword")
    @RequirePermission(PermissionType.VIEW_DEPARTMENT)
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> searchDepartments(
            @RequestParam String keyword
    ) {
        List<DepartmentResponse> response = departmentService.searchDepartments(keyword);
        return ResponseEntity.ok(ApiResponse.success("Department search completed", response));
    }
}
