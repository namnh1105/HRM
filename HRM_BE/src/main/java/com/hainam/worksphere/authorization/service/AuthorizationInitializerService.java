package com.hainam.worksphere.authorization.service;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.domain.RolePermission;
import com.hainam.worksphere.authorization.repository.PermissionRepository;
import com.hainam.worksphere.authorization.repository.RoleRepository;
import com.hainam.worksphere.authorization.repository.RolePermissionRepository;
import com.hainam.worksphere.shared.constant.PermissionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class AuthorizationInitializerService implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        createDefaultPermissions();
        createDefaultRoles();
        assignPermissionsToRoles();
    }

    private void createDefaultPermissions() {
        log.info("Creating default permissions...");

        List<PermissionType.PermissionDef> permissions = PermissionType.all();

        for (PermissionType.PermissionDef permDef : permissions) {
            if (!permissionRepository.existsByCode(permDef.getCode())) {
                Permission permission = Permission.builder()
                        .code(permDef.getCode())
                        .displayName(permDef.getDisplayName())
                        .description(permDef.getDescription())
                        .resource(permDef.getResource())
                        .action(permDef.getAction())
                        .isSystem(true)
                        .isActive(true)
                        .build();

                permissionRepository.save(permission);
                log.debug("Created permission: {}", permDef.getCode());
            }
        }

        log.info("Default permissions created: {} total definitions", permissions.size());
    }

    private void createDefaultRoles() {
        log.info("Creating default roles...");

        List<RoleData> roles = Arrays.asList(
            new RoleData("SUPER_ADMIN", "Super Administrator", "Full system access with all permissions"),
            new RoleData("ADMIN", "Administrator", "Administrative access with user and role management"),
            new RoleData("USER", "User", "Basic user with profile management permissions")
        );

        for (RoleData roleData : roles) {
            if (!roleRepository.existsByCode(roleData.code)) {
                Role role = Role.builder()
                        .code(roleData.code)
                        .displayName(roleData.displayName)
                        .description(roleData.description)
                        .isSystem(true)
                        .isActive(true)
                        .build();

                roleRepository.save(role);
                log.debug("Created role: {}", roleData.code);
            }
        }
    }

    private void assignPermissionsToRoles() {
        log.info("Assigning permissions to roles...");

        // SUPER_ADMIN gets ALL permissions
        Role superAdmin = roleRepository.findByCode("SUPER_ADMIN").orElse(null);
        if (superAdmin != null) {
            List<Permission> allPermissions = permissionRepository.findAll();
            for (Permission permission : allPermissions) {
                if (!rolePermissionRepository.existsByRoleIdAndPermissionIdAndIsActiveTrue(superAdmin.getId(), permission.getId())) {
                    RolePermission rolePermission = RolePermission.builder()
                            .role(superAdmin)
                            .permission(permission)
                            .isActive(true)
                            .build();
                    rolePermissionRepository.save(rolePermission);
                    log.debug("Assigned permission {} to SUPER_ADMIN", permission.getCode());
                }
            }
        }

        // ADMIN gets management permissions
        Role admin = roleRepository.findByCode("ADMIN").orElse(null);
        if (admin != null) {
            String[] adminPermissions = {
                PermissionType.MANAGE_USER, PermissionType.VIEW_USER, PermissionType.CREATE_USER,
                PermissionType.UPDATE_USER, PermissionType.DELETE_USER,
                PermissionType.MANAGE_ROLES, PermissionType.ASSIGN_ROLES, PermissionType.REVOKE_ROLES,
                PermissionType.VIEW_DEPARTMENT, PermissionType.CREATE_DEPARTMENT,
                PermissionType.UPDATE_DEPARTMENT, PermissionType.DELETE_DEPARTMENT,
                PermissionType.VIEW_EMPLOYEE, PermissionType.CREATE_EMPLOYEE,
                PermissionType.UPDATE_EMPLOYEE, PermissionType.DELETE_EMPLOYEE,
                PermissionType.VIEW_ATTENDANCE, PermissionType.VIEW_WORK_SHIFT,
                PermissionType.VIEW_LEAVE_REQUEST, PermissionType.APPROVE_LEAVE_REQUEST,
                PermissionType.VIEW_CONTRACT, PermissionType.VIEW_PAYROLL,
                PermissionType.VIEW_INSURANCE, PermissionType.VIEW_AUDIT_LOGS,
                PermissionType.VIEW_PROFILE, PermissionType.UPDATE_PROFILE
            };
            for (String permissionCode : adminPermissions) {
                Permission permission = permissionRepository.findByCode(permissionCode).orElse(null);
                if (permission != null && !rolePermissionRepository.existsByRoleIdAndPermissionIdAndIsActiveTrue(admin.getId(), permission.getId())) {
                    RolePermission rolePermission = RolePermission.builder()
                            .role(admin)
                            .permission(permission)
                            .isActive(true)
                            .build();
                    rolePermissionRepository.save(rolePermission);
                    log.debug("Assigned permission {} to ADMIN", permission.getCode());
                }
            }
        }

        // USER gets basic profile and view permissions
        Role user = roleRepository.findByCode("USER").orElse(null);
        if (user != null) {
            String[] userPermissions = {
                PermissionType.VIEW_PROFILE, PermissionType.UPDATE_PROFILE,
                PermissionType.VIEW_ATTENDANCE, PermissionType.CREATE_ATTENDANCE,
                PermissionType.VIEW_WORK_SHIFT,
                PermissionType.VIEW_LEAVE_REQUEST, PermissionType.CREATE_LEAVE_REQUEST,
                PermissionType.READ_USER_ROLE
            };
            for (String permissionCode : userPermissions) {
                Permission permission = permissionRepository.findByCode(permissionCode).orElse(null);
                if (permission != null && !rolePermissionRepository.existsByRoleIdAndPermissionIdAndIsActiveTrue(user.getId(), permission.getId())) {
                    RolePermission rolePermission = RolePermission.builder()
                            .role(user)
                            .permission(permission)
                            .isActive(true)
                            .build();
                    rolePermissionRepository.save(rolePermission);
                    log.debug("Assigned permission {} to USER", permission.getCode());
                }
            }
        }
    }

    private static class RoleData {
        final String code;
        final String displayName;
        final String description;

        RoleData(String code, String displayName, String description) {
            this.code = code;
            this.displayName = displayName;
            this.description = description;
        }
    }
}
