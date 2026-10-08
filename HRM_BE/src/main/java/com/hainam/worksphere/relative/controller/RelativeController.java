package com.hainam.worksphere.relative.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.relative.dto.request.CreateRelativeRequest;
import com.hainam.worksphere.relative.dto.response.RelativeResponse;
import com.hainam.worksphere.relative.service.RelativeService;
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
@RequestMapping("/api/v1/relatives")
@RequiredArgsConstructor
@Tag(name = "Relative Management")
@SecurityRequirement(name = "Bearer Authentication")
public class RelativeController {

    private final RelativeService relativeService;

    @PostMapping
    @Operation(summary = "Create a new relative")
    @RequirePermission(PermissionType.CREATE_RELATIVE)
    public ResponseEntity<ApiResponse<RelativeResponse>> createRelative(
            @Valid @RequestBody CreateRelativeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        RelativeResponse response = relativeService.createRelative(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Relative created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all relatives")
    @RequirePermission(PermissionType.VIEW_RELATIVE)
    public ResponseEntity<ApiResponse<List<RelativeResponse>>> getAllRelatives() {
        List<RelativeResponse> response = relativeService.getAllRelatives();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get relative by ID")
    @RequirePermission(PermissionType.VIEW_RELATIVE)
    public ResponseEntity<ApiResponse<RelativeResponse>> getRelativeById(
            @PathVariable UUID id
    ) {
        RelativeResponse response = relativeService.getRelativeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get relatives by employee ID")
    @RequirePermission(PermissionType.VIEW_RELATIVE)
    public ResponseEntity<ApiResponse<List<RelativeResponse>>> getByEmployeeId(
            @PathVariable UUID employeeId
    ) {
        List<RelativeResponse> response = relativeService.getByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}/emergency-contacts")
    @Operation(summary = "Get emergency contacts by employee ID")
    @RequirePermission(PermissionType.VIEW_RELATIVE)
    public ResponseEntity<ApiResponse<List<RelativeResponse>>> getEmergencyContacts(
            @PathVariable UUID employeeId
    ) {
        List<RelativeResponse> response = relativeService.getEmergencyContacts(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a relative")
    @RequirePermission(PermissionType.UPDATE_RELATIVE)
    public ResponseEntity<ApiResponse<RelativeResponse>> updateRelative(
            @PathVariable UUID id,
            @Valid @RequestBody CreateRelativeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        RelativeResponse response = relativeService.updateRelative(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Relative updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a relative (soft delete)")
    @RequirePermission(PermissionType.DELETE_RELATIVE)
    public ResponseEntity<ApiResponse<Void>> deleteRelative(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        relativeService.deleteRelative(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Relative deleted successfully", null));
    }
}
