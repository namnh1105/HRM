package com.hainam.worksphere.user.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.shared.audit.annotation.Auditable;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.shared.dto.PaginatedApiResponse;
import com.hainam.worksphere.user.dto.request.ChangePasswordRequest;
import com.hainam.worksphere.user.dto.request.UpdateProfileRequest;
import com.hainam.worksphere.user.dto.response.UserResponse;
import com.hainam.worksphere.shared.dto.ResourceStatsResponse;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users (optional include deleted)")
    @RequirePermission(PermissionType.VIEW_USER)
    public ResponseEntity<PaginatedApiResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<UserResponse> response = userService.getAllUsers(includeDeleted, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all users (optional include deleted)")
    @RequirePermission(PermissionType.VIEW_USER)
    public ResponseEntity<PaginatedApiResponse<UserResponse>> getAllUsersLegacy(
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<UserResponse> response = userService.getAllUsers(includeDeleted, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get user account statistics")
    @RequirePermission(PermissionType.VIEW_USER)
    public ResponseEntity<ApiResponse<ResourceStatsResponse>> getUserStats() {
        ResourceStatsResponse response = userService.getUserStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by keyword")
    @RequirePermission(PermissionType.VIEW_USER)
    public ResponseEntity<PaginatedApiResponse<UserResponse>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<UserResponse> response = userService.searchUsers(keyword, includeDeleted, pageable);
        return ResponseEntity.ok(PaginatedApiResponse.success(response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    @RequirePermission(PermissionType.VIEW_PROFILE)
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        UserResponse response = userService.getCurrentUser(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    @RequirePermission(PermissionType.VIEW_USER)
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable UUID userId
    ) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    @RequirePermission(PermissionType.UPDATE_PROFILE)
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse response = userService.updateProfile(userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change password")
    @RequirePermission(PermissionType.UPDATE_PROFILE)
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @DeleteMapping("/deactivate")
    @Operation(summary = "Deactivate account")
    @RequirePermission(PermissionType.UPDATE_PROFILE)
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        userService.deactivateAccount(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Account deactivated successfully", null));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Soft delete user by ID")
    @RequirePermission(PermissionType.DELETE_USER)
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        userService.softDeleteUser(userId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @PostMapping("/{userId}/activate")
    @Operation(summary = "Activate user by ID")
    @RequirePermission(PermissionType.UPDATE_USER)
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        UserResponse response = userService.activateUser(userId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("User activated successfully", response));
    }

    @PostMapping("/{userId}/deactivate")
    @Operation(summary = "Deactivate user by ID")
    @RequirePermission(PermissionType.UPDATE_USER)
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        UserResponse response = userService.deactivateUser(userId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", response));
    }

    @PostMapping("/{userId}/restore")
    @Operation(summary = "Restore soft deleted user")
    @RequirePermission(PermissionType.RESTORE_USER)
    public ResponseEntity<ApiResponse<UserResponse>> restoreUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        UserResponse response = userService.restoreUser(userId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("User restored successfully", response));
    }

    @DeleteMapping("/{userId}/permanent")
    @Operation(summary = "Permanently delete user")
    @RequirePermission(PermissionType.PERMANENT_DELETE_USER)
    public ResponseEntity<ApiResponse<Void>> permanentDeleteUser(
            @PathVariable UUID userId
    ) {
        userService.permanentDeleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User permanently deleted", null));
    }
}
