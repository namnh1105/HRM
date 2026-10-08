package com.hainam.worksphere.authorization.controller;

import com.hainam.worksphere.authorization.domain.UserRole;
import com.hainam.worksphere.authorization.dto.request.AssignRolesRequest;
import com.hainam.worksphere.authorization.dto.response.UserRoleAssignmentResponse;
import com.hainam.worksphere.authorization.mapper.UserRoleMapper;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.authorization.service.UserRoleService;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class UserRoleController {

    private final UserRoleService userRoleService;
    private final UserRoleMapper userRoleMapper;

    @GetMapping("/users/{userId}/roles")
    @RequirePermission(PermissionType.READ_USER_ROLE)
    public ResponseEntity<ApiResponse<List<UserRoleAssignmentResponse>>> getUserRoles(@PathVariable UUID userId) {
        List<UserRole> userRoles = userRoleService.getActiveUserRolesByUserId(userId);
        List<UserRoleAssignmentResponse> response = userRoles.stream()
                .map(userRoleMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("User roles retrieved successfully", response));
    }

    @PostMapping("/users/{userId}/roles/{roleId}")
    @RequirePermission(PermissionType.MANAGE_USER_ROLE)
    public ResponseEntity<ApiResponse<Void>> assignRoleToUser(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {
        userRoleService.assignRoleToUser(userId, roleId);

        return ResponseEntity.ok(ApiResponse.success("Role assigned to user successfully", null));
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @RequirePermission(PermissionType.MANAGE_USER_ROLE)
    public ResponseEntity<ApiResponse<Void>> removeRoleFromUser(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {
        userRoleService.removeRoleFromUser(userId, roleId);

        return ResponseEntity.ok(ApiResponse.success("Role removed from user successfully", null));
    }

    @PutMapping("/users/{userId}/roles")
    @RequirePermission(PermissionType.MANAGE_USER_ROLE)
    public ResponseEntity<ApiResponse<Void>> replaceUserRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRolesRequest request) {
        userRoleService.replaceUserRoles(userId, request.getRoleIds());

        return ResponseEntity.ok(ApiResponse.success("User roles replaced successfully", null));
    }

    @PostMapping("/users/{userId}/roles")
    @RequirePermission(PermissionType.MANAGE_USER_ROLE)
    public ResponseEntity<ApiResponse<Void>> assignRolesToUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRolesRequest request) {
        userRoleService.assignRolesToUser(userId, request.getRoleIds());

        return ResponseEntity.ok(ApiResponse.success("Roles assigned to user successfully", null));
    }

    @DeleteMapping("/users/{userId}/roles")
    @RequirePermission(PermissionType.MANAGE_USER_ROLE)
    public ResponseEntity<ApiResponse<Void>> removeRolesFromUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRolesRequest request) {
        userRoleService.removeRolesFromUser(userId, request.getRoleIds());

        return ResponseEntity.ok(ApiResponse.success("Roles removed from user successfully", null));
    }

    @GetMapping("/roles/{roleId}/users")
    @RequirePermission(PermissionType.READ_USER_ROLE)
    public ResponseEntity<ApiResponse<List<UserRoleAssignmentResponse>>> getUsersByRole(@PathVariable UUID roleId) {
        List<UserRole> userRoles = userRoleService.getActiveUserRolesByRoleId(roleId);
        List<UserRoleAssignmentResponse> response = userRoles.stream()
                .map(userRoleMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Role users retrieved successfully", response));
    }

    @GetMapping("/users/{userId}/roles/{roleId}")
    @RequirePermission(PermissionType.READ_USER_ROLE)
    public ResponseEntity<ApiResponse<Boolean>> checkUserHasRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {
        boolean hasRole = userRoleService.userHasRole(userId, roleId);

        return ResponseEntity.ok(ApiResponse.success("User role check completed", hasRole));
    }
}
