package com.hainam.worksphere.insurance.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.insurance.dto.request.CreateInsuranceRegistrationRequest;
import com.hainam.worksphere.insurance.dto.request.CreateInsuranceRequest;
import com.hainam.worksphere.insurance.dto.response.InsuranceRegistrationResponse;
import com.hainam.worksphere.insurance.dto.response.InsuranceResponse;
import com.hainam.worksphere.insurance.service.InsuranceService;
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
@RequestMapping("/api/v1/insurances")
@RequiredArgsConstructor
@Tag(name = "Insurance Management")
@SecurityRequirement(name = "Bearer Authentication")
public class InsuranceController {

    private final InsuranceService insuranceService;

    // ==================== Insurance Endpoints ====================

    @PostMapping
    @Operation(summary = "Create a new insurance")
    @RequirePermission(PermissionType.CREATE_INSURANCE)
    public ResponseEntity<ApiResponse<InsuranceResponse>> createInsurance(
            @Valid @RequestBody CreateInsuranceRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        InsuranceResponse response = insuranceService.createInsurance(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Insurance created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all insurances")
    @RequirePermission(PermissionType.VIEW_INSURANCE)
    public ResponseEntity<ApiResponse<List<InsuranceResponse>>> getAllInsurances() {
        List<InsuranceResponse> response = insuranceService.getAllInsurances();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get insurance by ID")
    @RequirePermission(PermissionType.VIEW_INSURANCE)
    public ResponseEntity<ApiResponse<InsuranceResponse>> getInsuranceById(
            @PathVariable UUID id
    ) {
        InsuranceResponse response = insuranceService.getInsuranceById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an insurance")
    @RequirePermission(PermissionType.UPDATE_INSURANCE)
    public ResponseEntity<ApiResponse<InsuranceResponse>> updateInsurance(
            @PathVariable UUID id,
            @Valid @RequestBody CreateInsuranceRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        InsuranceResponse response = insuranceService.updateInsurance(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Insurance updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an insurance (soft delete)")
    @RequirePermission(PermissionType.DELETE_INSURANCE)
    public ResponseEntity<ApiResponse<Void>> deleteInsurance(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        insuranceService.deleteInsurance(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Insurance deleted successfully", null));
    }

    // ==================== Insurance Registration Endpoints ====================

    @PostMapping("/registrations")
    @Operation(summary = "Create a new insurance registration")
    @RequirePermission(PermissionType.CREATE_INSURANCE)
    public ResponseEntity<ApiResponse<InsuranceRegistrationResponse>> createRegistration(
            @Valid @RequestBody CreateInsuranceRegistrationRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        InsuranceRegistrationResponse response = insuranceService.createRegistration(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Insurance registration created successfully", response));
    }

    @GetMapping("/registrations")
    @Operation(summary = "Get all insurance registrations")
    @RequirePermission(PermissionType.VIEW_INSURANCE)
    public ResponseEntity<ApiResponse<List<InsuranceRegistrationResponse>>> getAllRegistrations() {
        List<InsuranceRegistrationResponse> response = insuranceService.getAllRegistrations();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/registrations/{id}")
    @Operation(summary = "Get insurance registration by ID")
    @RequirePermission(PermissionType.VIEW_INSURANCE)
    public ResponseEntity<ApiResponse<InsuranceRegistrationResponse>> getRegistrationById(
            @PathVariable UUID id
    ) {
        InsuranceRegistrationResponse response = insuranceService.getRegistrationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/registrations/employee/{employeeId}")
    @Operation(summary = "Get insurance registrations by employee ID")
    @RequirePermission(PermissionType.VIEW_INSURANCE)
    public ResponseEntity<ApiResponse<List<InsuranceRegistrationResponse>>> getRegistrationsByEmployeeId(
            @PathVariable UUID employeeId
    ) {
        List<InsuranceRegistrationResponse> response = insuranceService.getRegistrationsByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/registrations/insurance/{insuranceId}")
    @Operation(summary = "Get insurance registrations by insurance ID")
    @RequirePermission(PermissionType.VIEW_INSURANCE)
    public ResponseEntity<ApiResponse<List<InsuranceRegistrationResponse>>> getRegistrationsByInsuranceId(
            @PathVariable UUID insuranceId
    ) {
        List<InsuranceRegistrationResponse> response = insuranceService.getRegistrationsByInsuranceId(insuranceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/registrations/{id}")
    @Operation(summary = "Update an insurance registration")
    @RequirePermission(PermissionType.UPDATE_INSURANCE)
    public ResponseEntity<ApiResponse<InsuranceRegistrationResponse>> updateRegistration(
            @PathVariable UUID id,
            @Valid @RequestBody CreateInsuranceRegistrationRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        InsuranceRegistrationResponse response = insuranceService.updateRegistration(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Insurance registration updated successfully", response));
    }

    @DeleteMapping("/registrations/{id}")
    @Operation(summary = "Delete an insurance registration (soft delete)")
    @RequirePermission(PermissionType.DELETE_INSURANCE)
    public ResponseEntity<ApiResponse<Void>> deleteRegistration(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        insuranceService.deleteRegistration(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Insurance registration deleted successfully", null));
    }
}
