package com.hainam.worksphere.store.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.store.dto.request.CreateStoreRequest;
import com.hainam.worksphere.store.dto.request.UpdateStoreRequest;
import com.hainam.worksphere.store.dto.response.StoreResponse;
import com.hainam.worksphere.store.service.StoreService;
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
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Tag(name = "Store Management")
@SecurityRequirement(name = "Bearer Authentication")
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    @Operation(summary = "Get all active stores")
    @RequirePermission(PermissionType.VIEW_STORE)
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getAllStores() {
        List<StoreResponse> response = storeService.getAllActiveStores();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{storeId}")
    @Operation(summary = "Get store by ID")
    @RequirePermission(PermissionType.VIEW_STORE)
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(
            @PathVariable UUID storeId
    ) {
        StoreResponse response = storeService.getStoreById(storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create a new store")
    @RequirePermission(PermissionType.CREATE_STORE)
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(
            @Valid @RequestBody CreateStoreRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        StoreResponse response = storeService.createStore(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Store created successfully", response));
    }

    @PutMapping("/{storeId}")
    @Operation(summary = "Update store")
    @RequirePermission(PermissionType.UPDATE_STORE)
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody UpdateStoreRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        StoreResponse response = storeService.updateStore(storeId, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Store updated successfully", response));
    }

    @DeleteMapping("/{storeId}")
    @Operation(summary = "Soft delete store")
    @RequirePermission(PermissionType.DELETE_STORE)
    public ResponseEntity<ApiResponse<Void>> deleteStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        storeService.softDeleteStore(storeId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Store deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search stores by keyword")
    @RequirePermission(PermissionType.VIEW_STORE)
    public ResponseEntity<ApiResponse<List<StoreResponse>>> searchStores(
            @RequestParam String keyword
    ) {
        List<StoreResponse> response = storeService.searchStores(keyword);
        return ResponseEntity.ok(ApiResponse.success("Store search completed", response));
    }
}
