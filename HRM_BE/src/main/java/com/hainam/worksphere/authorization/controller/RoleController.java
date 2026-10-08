package com.hainam.worksphere.authorization.controller;

import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.dto.request.AssignPermissionsToRoleRequest;
import com.hainam.worksphere.authorization.dto.request.CreateRoleRequest;
import com.hainam.worksphere.authorization.dto.request.UpdateRoleRequest;
import com.hainam.worksphere.authorization.dto.response.RoleResponse;
import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.dto.response.PermissionResponse;
import com.hainam.worksphere.authorization.mapper.PermissionMapper;
import com.hainam.worksphere.authorization.mapper.RoleMapper;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.authorization.service.PermissionService;
import com.hainam.worksphere.authorization.service.RolePermissionService;
import com.hainam.worksphere.authorization.service.RoleService;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.shared.dto.PaginatedApiResponse;
import com.hainam.worksphere.shared.dto.ResourceStatsResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;
    private final RolePermissionService rolePermissionService;
    private final PermissionService permissionService;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    @PostMapping
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("Creating role: {}", request.getCode());

        Role role = roleMapper.toEntity(request);
        Role createdRole = roleService.createRole(role);
        RoleResponse response = roleMapper.toResponse(createdRole);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", response));
    }

    @GetMapping("/{roleId}")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable UUID roleId) {
        log.info("Fetching role with ID: {}", roleId);

        Role role = roleService.getRoleById(roleId);
        RoleResponse response = roleMapper.toResponse(role);

        return ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", response));
    }

    @GetMapping
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<PaginatedApiResponse<RoleResponse>> getAllRoles(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        log.info("Fetching all roles, includeDeleted: {}", includeDeleted);

        Page<Role> roles = roleService.getAllRoles(pageable, includeDeleted);
        Page<RoleResponse> response = roles.map(roleMapper::toSimpleResponse);

        return ResponseEntity.ok(PaginatedApiResponse.success("Roles retrieved successfully", response));
    }

    @GetMapping("/active")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<PaginatedApiResponse<RoleResponse>> getActiveRoles(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching active roles");
        Page<Role> roles = roleService.getAllActiveRoles(pageable);
        Page<RoleResponse> response = roles.map(roleMapper::toSimpleResponse);
        return ResponseEntity.ok(PaginatedApiResponse.success("Active roles retrieved successfully", response));
    }

    @PutMapping("/{roleId}")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request) {
        log.info("Updating role with ID: {}", roleId);

        Role existingRole = roleService.getRoleById(roleId);
        roleMapper.updateEntity(existingRole, request);
        Role savedRole = roleService.updateRole(roleId, existingRole);
        RoleResponse response = roleMapper.toResponse(savedRole);

        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", response));
    }

    @DeleteMapping("/{roleId}")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID roleId) {
        log.info("Deleting role with ID: {}", roleId);

        roleService.deleteRole(roleId);

        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }

    @PostMapping("/{roleId}/activate")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<Void>> activateRole(@PathVariable UUID roleId) {
        log.info("Activating role with ID: {}", roleId);

        // In RoleService, I'll need to re-add these if I removed them accidentally
        Role role = roleService.getRoleById(roleId);
        role.setIsActive(true);
        roleService.updateRole(roleId, role);

        return ResponseEntity.ok(ApiResponse.success("Role activated successfully", null));
    }

    @PostMapping("/{roleId}/deactivate")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<Void>> deactivateRole(@PathVariable UUID roleId) {
        log.info("Deactivating role with ID: {}", roleId);

        Role role = roleService.getRoleById(roleId);
        role.setIsActive(false);
        roleService.updateRole(roleId, role);

        return ResponseEntity.ok(ApiResponse.success("Role deactivated successfully", null));
    }

    @PostMapping("/{roleId}/restore")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<Void>> restoreRole(@PathVariable UUID roleId) {
        log.info("Restoring role with ID: {}", roleId);

        roleService.restoreRole(roleId);

        return ResponseEntity.ok(ApiResponse.success("Role restored successfully", null));
    }

    @PostMapping("/{roleId}/permissions")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<Void>> assignPermissionsToRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignPermissionsToRoleRequest request) {
        log.info("Assigning {} permissions to role {}", request.getPermissionIds().size(), roleId);

        rolePermissionService.assignPermissionsToRole(roleId, request.getPermissionIds());

        return ResponseEntity.ok(ApiResponse.success("Permissions assigned to role successfully", null));
    }

    @GetMapping("/{roleId}/permissions")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getRolePermissions(@PathVariable UUID roleId) {
        log.info("Fetching permissions for role ID: {}", roleId);
        List<Permission> permissions = permissionService.getPermissionsByRoleId(roleId);
        log.info("Found {} permissions for role ID: {}", permissions.size(), roleId);
        List<PermissionResponse> response = permissions.stream()
                .map(permissionMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Role permissions retrieved successfully", response));
    }

    @PutMapping("/{roleId}/permissions")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<Void>> syncPermissionsToRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignPermissionsToRoleRequest request) {
        log.info("Syncing {} permissions for role {}", request.getPermissionIds().size(), roleId);

        rolePermissionService.syncPermissionsToRole(roleId, request.getPermissionIds());

        return ResponseEntity.ok(ApiResponse.success("Permissions synced for role successfully", null));
    }

    @DeleteMapping("/{roleId}/permissions")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<Void>> removePermissionsFromRole(
            @PathVariable UUID roleId,
            @RequestBody List<UUID> permissionIds) {
        log.info("Removing {} permissions from role {}", permissionIds.size(), roleId);

        rolePermissionService.removePermissionsFromRole(roleId, permissionIds);

        return ResponseEntity.ok(ApiResponse.success("Permissions removed from role successfully", null));
    }

    @GetMapping("/search")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<PaginatedApiResponse<RoleResponse>> searchRoles(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Searching roles with query: {}", query);
        Page<Role> roles = roleService.searchRoles(query, pageable);
        Page<RoleResponse> response = roles.map(roleMapper::toSimpleResponse);
        return ResponseEntity.ok(PaginatedApiResponse.success("Roles search completed", response));
    }

    @GetMapping("/stats")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<ResourceStatsResponse>> getRoleStats() {
        log.info("Fetching role statistics");
        ResourceStatsResponse stats = roleService.getRoleStats();
        return ResponseEntity.ok(ApiResponse.success("Role statistics retrieved successfully", stats));
    }
}
