package com.hainam.worksphere.workshift.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.shared.dto.PaginatedApiResponse;
import com.hainam.worksphere.shared.dto.ResourceStatsResponse;
import com.hainam.worksphere.workshift.dto.request.CreateWorkShiftRequest;
import com.hainam.worksphere.workshift.dto.request.UpdateWorkShiftRequest;
import com.hainam.worksphere.workshift.dto.response.WorkShiftResponse;
import com.hainam.worksphere.workshift.service.WorkShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/work-shifts")
@RequiredArgsConstructor
@Tag(name = "Work Shift Management")
@SecurityRequirement(name = "Bearer Authentication")
public class WorkShiftController {

    private final WorkShiftService workShiftService;

    @GetMapping
    @Operation(summary = "Get all active work shifts")
    @RequirePermission(PermissionType.VIEW_WORK_SHIFT)
    public ResponseEntity<PaginatedApiResponse<WorkShiftResponse>> getAllWorkShifts(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<WorkShiftResponse> response = workShiftService.getAllActiveWorkShifts(pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/enabled")
    @Operation(summary = "Get all active and enabled work shifts")
    @RequirePermission(PermissionType.VIEW_WORK_SHIFT)
    public ResponseEntity<PaginatedApiResponse<WorkShiftResponse>> getAllEnabledWorkShifts(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<WorkShiftResponse> response = workShiftService.getAllActiveAndEnabledWorkShifts(pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get work shifts by store")
    @RequirePermission(PermissionType.VIEW_WORK_SHIFT)
    public ResponseEntity<PaginatedApiResponse<WorkShiftResponse>> getWorkShiftsByStore(
            @PathVariable UUID storeId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<WorkShiftResponse> response = workShiftService.getWorkShiftsByStore(storeId, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/{workShiftId}")
    @Operation(summary = "Get work shift by ID")
    @RequirePermission(PermissionType.VIEW_WORK_SHIFT)
    public ResponseEntity<ApiResponse<WorkShiftResponse>> getWorkShiftById(
            @PathVariable UUID workShiftId
    ) {
        WorkShiftResponse response = workShiftService.getWorkShiftById(workShiftId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create a new work shift")
    @RequirePermission(PermissionType.CREATE_WORK_SHIFT)
    public ResponseEntity<ApiResponse<WorkShiftResponse>> createWorkShift(
            @Valid @RequestBody CreateWorkShiftRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        WorkShiftResponse response = workShiftService.createWorkShift(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Work shift created successfully", response));
    }

    @PutMapping("/{workShiftId}")
    @Operation(summary = "Update work shift")
    @RequirePermission(PermissionType.UPDATE_WORK_SHIFT)
    public ResponseEntity<ApiResponse<WorkShiftResponse>> updateWorkShift(
            @PathVariable UUID workShiftId,
            @Valid @RequestBody UpdateWorkShiftRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        WorkShiftResponse response = workShiftService.updateWorkShift(workShiftId, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Work shift updated successfully", response));
    }

    @DeleteMapping("/{workShiftId}")
    @Operation(summary = "Soft delete work shift")
    @RequirePermission(PermissionType.DELETE_WORK_SHIFT)
    public ResponseEntity<ApiResponse<Void>> deleteWorkShift(
            @PathVariable UUID workShiftId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        workShiftService.softDeleteWorkShift(workShiftId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Work shift deleted successfully", null));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get work shift statistics")
    @RequirePermission(PermissionType.VIEW_WORK_SHIFT)
    public ResponseEntity<ApiResponse<ResourceStatsResponse>> getWorkShiftStats() {
        ResourceStatsResponse stats = workShiftService.getWorkShiftStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
