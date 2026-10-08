package com.hainam.worksphere.leave.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.employee.dto.response.EmployeeResponse;
import com.hainam.worksphere.employee.service.EmployeeService;
import com.hainam.worksphere.leave.domain.LeaveRequestStatus;
import com.hainam.worksphere.leave.dto.request.ApproveLeaveRequestDto;
import com.hainam.worksphere.leave.dto.request.CreateLeaveRequestDto;
import com.hainam.worksphere.leave.dto.response.LeaveRequestResponse;
import com.hainam.worksphere.leave.service.LeaveRequestService;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.shared.dto.PaginatedApiResponse;
import com.hainam.worksphere.shared.dto.ResourceStatsResponse;
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
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
@Tag(name = "Leave Request Management")
@SecurityRequirement(name = "Bearer Authentication")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final EmployeeService employeeService;

    @PostMapping
    @Operation(summary = "Create a new leave request")
    @RequirePermission(PermissionType.CREATE_LEAVE_REQUEST)
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> createLeaveRequest(
            @Valid @RequestBody CreateLeaveRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userPrincipal.getId());
        LeaveRequestResponse response = leaveRequestService.createLeaveRequest(employee.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Leave request created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all leave requests with optional status filter")
    @RequirePermission(PermissionType.VIEW_LEAVE_REQUEST)
    public ResponseEntity<PaginatedApiResponse<LeaveRequestResponse>> getAllLeaveRequests(
            @RequestParam(required = false) LeaveRequestStatus status,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<LeaveRequestResponse> response = leaveRequestService.getAllLeaveRequests(status, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my leave requests")
    @RequirePermission(PermissionType.VIEW_LEAVE_REQUEST)
    public ResponseEntity<PaginatedApiResponse<LeaveRequestResponse>> getMyLeaveRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userPrincipal.getId());
        Page<LeaveRequestResponse> response = leaveRequestService.getMyLeaveRequests(employee.getId(), pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending leave requests (admin)")
    @RequirePermission(PermissionType.APPROVE_LEAVE_REQUEST)
    public ResponseEntity<PaginatedApiResponse<LeaveRequestResponse>> getPendingLeaveRequests(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<LeaveRequestResponse> response = leaveRequestService.getPendingLeaveRequests(pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get leave request by ID")
    @RequirePermission(PermissionType.VIEW_LEAVE_REQUEST)
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> getLeaveRequestById(
            @PathVariable UUID id
    ) {
        LeaveRequestResponse response = leaveRequestService.getLeaveRequestById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve or reject a leave request")
    @RequirePermission(PermissionType.APPROVE_LEAVE_REQUEST)
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> approveLeaveRequest(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveLeaveRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        EmployeeResponse approver = employeeService.getEmployeeByUserId(userPrincipal.getId());
        LeaveRequestResponse response = leaveRequestService.approveLeaveRequest(id, approver.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Leave request processed successfully", response));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel own leave request")
    @RequirePermission(PermissionType.CREATE_LEAVE_REQUEST)
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> cancelLeaveRequest(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userPrincipal.getId());
        LeaveRequestResponse response = leaveRequestService.cancelLeaveRequest(id, employee.getId());
        return ResponseEntity.ok(ApiResponse.success("Leave request cancelled successfully", response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get leave request statistics")
    @RequirePermission(PermissionType.VIEW_LEAVE_REQUEST)
    public ResponseEntity<ApiResponse<ResourceStatsResponse>> getLeaveRequestStats() {
        ResourceStatsResponse stats = leaveRequestService.getLeaveRequestStats();
        return ResponseEntity.ok(ApiResponse.success("Leave request statistics retrieved successfully", stats));
    }
}
