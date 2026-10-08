package com.hainam.worksphere.contract.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.contract.dto.request.CreateContractRequest;
import com.hainam.worksphere.contract.dto.request.UpdateContractRequest;
import com.hainam.worksphere.contract.dto.response.ContractResponse;
import com.hainam.worksphere.contract.service.ContractService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@Tag(name = "Contract Management")
@SecurityRequirement(name = "Bearer Authentication")
public class ContractController {

    private final ContractService contractService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new contract")
    @RequirePermission(PermissionType.CREATE_CONTRACT)
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(
            @Valid @ModelAttribute CreateContractRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ContractResponse response = contractService.createContract(request, file, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Contract created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all contracts")
    @RequirePermission(PermissionType.VIEW_CONTRACT)
    public ResponseEntity<PaginatedApiResponse<ContractResponse>> getAllContracts(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ContractResponse> response = contractService.getAllContracts(pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contract by ID")
    @RequirePermission(PermissionType.VIEW_CONTRACT)
    public ResponseEntity<ApiResponse<ContractResponse>> getContractById(
            @PathVariable UUID id
    ) {
        ContractResponse response = contractService.getContractById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get contracts by employee ID")
    @RequirePermission(PermissionType.VIEW_CONTRACT)
    public ResponseEntity<PaginatedApiResponse<ContractResponse>> getByEmployeeId(
            @PathVariable UUID employeeId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ContractResponse> response = contractService.getByEmployeeId(employeeId, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active contracts")
    @RequirePermission(PermissionType.VIEW_CONTRACT)
    public ResponseEntity<PaginatedApiResponse<ContractResponse>> getActiveContracts(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ContractResponse> response = contractService.getActiveContracts(pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update a contract")
    @RequirePermission(PermissionType.UPDATE_CONTRACT)
    public ResponseEntity<ApiResponse<ContractResponse>> updateContract(
            @PathVariable UUID id,
            @Valid @ModelAttribute UpdateContractRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ContractResponse response = contractService.updateContract(id, request, file, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Contract updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a contract (soft delete)")
    @RequirePermission(PermissionType.DELETE_CONTRACT)
    public ResponseEntity<ApiResponse<Void>> deleteContract(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        contractService.deleteContract(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Contract deleted successfully", null));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get contract statistics")
    @RequirePermission(PermissionType.VIEW_CONTRACT)
    public ResponseEntity<ApiResponse<ResourceStatsResponse>> getContractStats() {
        ResourceStatsResponse stats = contractService.getContractStats();
        return ResponseEntity.ok(ApiResponse.success("Contract statistics retrieved successfully", stats));
    }
}
