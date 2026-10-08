package com.hainam.worksphere.degree.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.degree.dto.request.CreateDegreeRequest;
import com.hainam.worksphere.degree.dto.response.DegreeResponse;
import com.hainam.worksphere.degree.service.DegreeService;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.shared.dto.PaginatedApiResponse;
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
@RequestMapping("/api/v1/degrees")
@RequiredArgsConstructor
@Tag(name = "Degree Management")
@SecurityRequirement(name = "Bearer Authentication")
public class DegreeController {

    private final DegreeService degreeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new degree")
    @RequirePermission(PermissionType.CREATE_DEGREE)
    public ResponseEntity<ApiResponse<DegreeResponse>> createDegree(
            @Valid @ModelAttribute CreateDegreeRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        DegreeResponse response = degreeService.createDegree(request, file, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Degree created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all degrees")
    @RequirePermission(PermissionType.VIEW_DEGREE)
    public ResponseEntity<PaginatedApiResponse<DegreeResponse>> getAllDegrees(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<DegreeResponse> response = degreeService.getAllDegrees(pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get degree by ID")
    @RequirePermission(PermissionType.VIEW_DEGREE)
    public ResponseEntity<ApiResponse<DegreeResponse>> getDegreeById(
            @PathVariable UUID id
    ) {
        DegreeResponse response = degreeService.getDegreeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get degrees by employee ID")
    @RequirePermission(PermissionType.VIEW_DEGREE)
    public ResponseEntity<PaginatedApiResponse<DegreeResponse>> getByEmployeeId(
            @PathVariable UUID employeeId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<DegreeResponse> response = degreeService.getByEmployeeId(employeeId, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update a degree")
    @RequirePermission(PermissionType.UPDATE_DEGREE)
    public ResponseEntity<ApiResponse<DegreeResponse>> updateDegree(
            @PathVariable UUID id,
            @Valid @ModelAttribute CreateDegreeRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        DegreeResponse response = degreeService.updateDegree(id, request, file, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Degree updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a degree (soft delete)")
    @RequirePermission(PermissionType.DELETE_DEGREE)
    public ResponseEntity<ApiResponse<Void>> deleteDegree(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        degreeService.deleteDegree(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Degree deleted successfully", null));
    }
}
