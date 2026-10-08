package com.hainam.worksphere.workshift.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.workshift.dto.request.AssignWorkShiftRequest;
import com.hainam.worksphere.workshift.dto.request.UpdateAssignWorkShiftRequest;
import com.hainam.worksphere.workshift.dto.response.EmployeeWorkShiftResponse;
import com.hainam.worksphere.workshift.service.EmployeeWorkShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employee-work-shifts")
@RequiredArgsConstructor
@Tag(name = "Employee Work Shift Assignment")
@SecurityRequirement(name = "Bearer Authentication")
public class EmployeeWorkShiftController {

    private final EmployeeWorkShiftService employeeWorkShiftService;

    @GetMapping
    @Operation(summary = "Get all work shift assignments")
    @RequirePermission(PermissionType.VIEW_WORK_SHIFT)
    public ResponseEntity<ApiResponse<List<EmployeeWorkShiftResponse>>> getAllActive() {
        List<EmployeeWorkShiftResponse> response = employeeWorkShiftService.getAllActive();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get all work shift assignments for an employee")
    @RequirePermission(PermissionType.VIEW_WORK_SHIFT)
    public ResponseEntity<ApiResponse<List<EmployeeWorkShiftResponse>>> getByEmployeeId(
            @PathVariable UUID employeeId
    ) {
        List<EmployeeWorkShiftResponse> response = employeeWorkShiftService.getByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}/date")
    @Operation(summary = "Get work shift assignments for an employee on a specific date")
    @RequirePermission(PermissionType.VIEW_WORK_SHIFT)
    public ResponseEntity<ApiResponse<List<EmployeeWorkShiftResponse>>> getByEmployeeIdAndDate(
            @PathVariable UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<EmployeeWorkShiftResponse> response = employeeWorkShiftService.getByEmployeeIdAndDate(employeeId, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/work-shift/{workShiftId}")
    @Operation(summary = "Get all employees assigned to a work shift")
    @RequirePermission(PermissionType.VIEW_WORK_SHIFT)
    public ResponseEntity<ApiResponse<List<EmployeeWorkShiftResponse>>> getByWorkShiftId(
            @PathVariable UUID workShiftId
    ) {
        List<EmployeeWorkShiftResponse> response = employeeWorkShiftService.getByWorkShiftId(workShiftId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get work shift assignment by ID")
    @RequirePermission(PermissionType.VIEW_WORK_SHIFT)
    public ResponseEntity<ApiResponse<EmployeeWorkShiftResponse>> getById(
            @PathVariable UUID id
    ) {
        EmployeeWorkShiftResponse response = employeeWorkShiftService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Assign a work shift to an employee")
    @RequirePermission(PermissionType.CREATE_WORK_SHIFT)
    public ResponseEntity<ApiResponse<EmployeeWorkShiftResponse>> assignWorkShift(
            @Valid @RequestBody AssignWorkShiftRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        EmployeeWorkShiftResponse response = employeeWorkShiftService.assignWorkShift(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Work shift assigned successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a work shift assignment")
    @RequirePermission(PermissionType.UPDATE_WORK_SHIFT)
    public ResponseEntity<ApiResponse<EmployeeWorkShiftResponse>> updateAssignment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAssignWorkShiftRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        EmployeeWorkShiftResponse response = employeeWorkShiftService.updateAssignment(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Work shift assignment updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a work shift assignment")
    @RequirePermission(PermissionType.DELETE_WORK_SHIFT)
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        employeeWorkShiftService.softDelete(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Work shift assignment removed successfully", null));
    }

    @GetMapping("/my-shifts")
    @Operation(summary = "Get current user's work shift assignments for today")
    public ResponseEntity<ApiResponse<List<EmployeeWorkShiftResponse>>> getMyShiftsToday(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<EmployeeWorkShiftResponse> response = employeeWorkShiftService.getByUserIdAndDate(
                userPrincipal.getId(), LocalDate.now()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get all work shift assignments for current user")
    public ResponseEntity<ApiResponse<List<EmployeeWorkShiftResponse>>> getMyAllShifts(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<EmployeeWorkShiftResponse> response = employeeWorkShiftService.getAllByUserId(
                userPrincipal.getId()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get all work shift assignments in a store")
    @RequirePermission(PermissionType.VIEW_WORK_SHIFT)
    public ResponseEntity<ApiResponse<List<EmployeeWorkShiftResponse>>> getByStoreId(
            @PathVariable UUID storeId
    ) {
        List<EmployeeWorkShiftResponse> response = employeeWorkShiftService.getByStoreId(storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
