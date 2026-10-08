package com.hainam.worksphere.payroll.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.employee.dto.response.EmployeeResponse;
import com.hainam.worksphere.employee.service.EmployeeService;
import com.hainam.worksphere.payroll.dto.request.AutoGeneratePayrollRequest;
import com.hainam.worksphere.payroll.dto.request.CreatePayrollRequest;
import com.hainam.worksphere.payroll.dto.request.UpdatePayrollRequest;
import com.hainam.worksphere.payroll.dto.response.PayrollResponse;
import com.hainam.worksphere.payroll.service.PayrollService;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payrolls")
@RequiredArgsConstructor
@Tag(name = "Payroll Management")
@SecurityRequirement(name = "Bearer Authentication")
public class PayrollController {

    private final PayrollService payrollService;
    private final EmployeeService employeeService;

    @GetMapping
    @Operation(summary = "Get all payrolls")
    @RequirePermission(PermissionType.VIEW_PAYROLL)
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getAllPayrolls() {
        List<PayrollResponse> response = payrollService.getAllPayrolls();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payroll by ID")
    @RequirePermission(PermissionType.VIEW_PAYROLL)
    public ResponseEntity<ApiResponse<PayrollResponse>> getPayrollById(@PathVariable UUID id) {
        PayrollResponse response = payrollService.getPayrollById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current employee's payrolls")
    @RequirePermission(PermissionType.VIEW_PAYROLL)
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getMyPayrolls(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userPrincipal.getId());
        List<PayrollResponse> response = payrollService.getByEmployeeId(employee.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get payrolls by employee ID")
    @RequirePermission(PermissionType.VIEW_PAYROLL)
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getPayrollsByEmployeeId(
            @PathVariable UUID employeeId
    ) {
        List<PayrollResponse> response = payrollService.getByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/month/{month}/year/{year}")
    @Operation(summary = "Get payrolls by month and year")
    @RequirePermission(PermissionType.VIEW_PAYROLL)
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getPayrollsByMonthAndYear(
            @PathVariable Integer month,
            @PathVariable Integer year
    ) {
        List<PayrollResponse> response = payrollService.getByMonthAndYear(month, year);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create a new payroll")
    @RequirePermission(PermissionType.CREATE_PAYROLL)
    public ResponseEntity<ApiResponse<PayrollResponse>> createPayroll(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CreatePayrollRequest request
    ) {
        PayrollResponse response = payrollService.createPayroll(request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Payroll created successfully", response));
    }

    @PostMapping("/auto-generate")
    @Operation(summary = "Auto-generate payrolls from attendance")
    @RequirePermission(PermissionType.CREATE_PAYROLL)
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> autoGeneratePayrolls(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody AutoGeneratePayrollRequest request
    ) {
        List<PayrollResponse> response = payrollService.autoGeneratePayrolls(request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Payrolls generated successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a payroll")
    @RequirePermission(PermissionType.UPDATE_PAYROLL)
    public ResponseEntity<ApiResponse<PayrollResponse>> updatePayroll(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UpdatePayrollRequest request
    ) {
        PayrollResponse response = payrollService.updatePayroll(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Payroll updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a payroll (soft delete)")
    @RequirePermission(PermissionType.DELETE_PAYROLL)
    public ResponseEntity<ApiResponse<Void>> deletePayroll(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        payrollService.deletePayroll(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Payroll deleted successfully", null));
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirm a payroll")
    @RequirePermission(PermissionType.UPDATE_PAYROLL)
    public ResponseEntity<ApiResponse<PayrollResponse>> confirmPayroll(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        PayrollResponse response = payrollService.confirmPayroll(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Payroll confirmed successfully", response));
    }

}
