package com.hainam.worksphere.authorization.service;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.domain.RolePermission;
import com.hainam.worksphere.authorization.repository.RolePermissionRepository;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.exception.DuplicateResourceException;
import com.hainam.worksphere.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleService roleService;
    private final PermissionService permissionService;

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.ROLE_CACHE, key = "#roleId.toString()"),
        @CacheEvict(value = CacheConfig.ROLE_PERMISSIONS_CACHE, key = "#roleId"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, allEntries = true)
    })
    public RolePermission assignPermissionToRole(UUID roleId, UUID permissionId) {
        Role role = roleService.getRoleById(roleId);
        Permission permission = permissionService.getPermissionById(permissionId);

        Optional<RolePermission> existing = rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId);
        if (existing.isPresent()) {
            if (existing.get().getIsActive()) {
                throw new DuplicateResourceException("Permission is already assigned to role");
            } else {
                existing.get().setIsActive(true);
                return rolePermissionRepository.save(existing.get());
            }
        }

        RolePermission rolePermission = RolePermission.builder()
                .role(role)
                .permission(permission)
                .isActive(true)
                .build();

        return rolePermissionRepository.save(rolePermission);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.ROLE_CACHE, key = "#roleId.toString()"),
        @CacheEvict(value = CacheConfig.ROLE_PERMISSIONS_CACHE, key = "#roleId"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, allEntries = true)
    })
    public void removePermissionFromRole(UUID roleId, UUID permissionId) {
        RolePermission rolePermission = rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission assignment not found"));

        rolePermission.setIsActive(false);
        rolePermissionRepository.save(rolePermission);
    }

    public Optional<RolePermission> getRolePermission(UUID roleId, UUID permissionId) {
        return rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId);
    }

    public List<RolePermission> getRolePermissionsByRoleId(UUID roleId) {
        return rolePermissionRepository.findByRoleId(roleId);
    }

    public List<RolePermission> getActiveRolePermissionsByRoleId(UUID roleId) {
        return rolePermissionRepository.findByRoleIdAndIsActiveTrue(roleId);
    }

    public List<RolePermission> getRolePermissionsByPermissionId(UUID permissionId) {
        return rolePermissionRepository.findByPermissionId(permissionId);
    }

    public List<RolePermission> getActiveRolePermissionsByPermissionId(UUID permissionId) {
        return rolePermissionRepository.findByPermissionIdAndIsActiveTrue(permissionId);
    }

    public boolean roleHasPermission(UUID roleId, UUID permissionId) {
        return rolePermissionRepository.existsByRoleIdAndPermissionIdAndIsActiveTrue(roleId, permissionId);
    }

    public boolean roleHasPermission(String roleCode, String permissionCode) {
        return rolePermissionRepository.existsByRoleCodeAndPermissionCodeAndIsActiveTrue(roleCode, permissionCode);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.ROLE_CACHE, key = "#roleId.toString()"),
        @CacheEvict(value = CacheConfig.ROLE_PERMISSIONS_CACHE, key = "#roleId"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, allEntries = true)
    })
    public void deactivateAllPermissionsForRole(UUID roleId) {
        rolePermissionRepository.deactivateByRoleId(roleId);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.ROLE_PERMISSIONS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, allEntries = true)
    })
    public void deactivateAllRolesForPermission(UUID permissionId) {
        log.info("Deactivating all roles for permission {}", permissionId);
        rolePermissionRepository.deactivateByPermissionId(permissionId);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.ROLE_CACHE, key = "#roleId.toString()"),
        @CacheEvict(value = CacheConfig.ROLE_PERMISSIONS_CACHE, key = "#roleId"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, allEntries = true)
    })
    public void activateRolePermission(UUID roleId, UUID permissionId) {
        rolePermissionRepository.activate(roleId, permissionId);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.ROLE_CACHE, key = "#roleId.toString()"),
        @CacheEvict(value = CacheConfig.ROLE_PERMISSIONS_CACHE, key = "#roleId"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, allEntries = true)
    })
    public void deactivateRolePermission(UUID roleId, UUID permissionId) {
        rolePermissionRepository.deactivate(roleId, permissionId);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.ROLE_CACHE, key = "#roleId.toString()"),
        @CacheEvict(value = CacheConfig.ROLE_PERMISSIONS_CACHE, key = "#roleId"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, allEntries = true)
    })
    public void syncPermissionsToRole(UUID roleId, List<UUID> permissionIds) {
        log.info("Syncing {} permissions for role {}", permissionIds.size(), roleId);
        
        // 1. Deactivate all existing permissions for this role
        rolePermissionRepository.deactivateByRoleId(roleId);
        
        // 2. Assign/Reactivate requested permissions
        for (UUID permissionId : permissionIds) {
            Optional<RolePermission> existing = rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId);
            if (existing.isPresent()) {
                RolePermission rp = existing.get();
                rp.setIsActive(true);
                rolePermissionRepository.save(rp);
            } else {
                Role role = roleService.getRoleById(roleId);
                Permission permission = permissionService.getPermissionById(permissionId);
                RolePermission rolePermission = RolePermission.builder()
                        .role(role)
                        .permission(permission)
                        .isActive(true)
                        .build();
                rolePermissionRepository.save(rolePermission);
            }
        }
    }

    @Transactional
    public void assignPermissionsToRole(UUID roleId, List<UUID> permissionIds) {
        for (UUID permissionId : permissionIds) {
            try {
                assignPermissionToRole(roleId, permissionId);
            } catch (IllegalArgumentException e) {
                log.warn("Failed to assign permission {} to role {}: {}", permissionId, roleId, e.getMessage());
            }
        }
    }

    @Transactional
    public void removePermissionsFromRole(UUID roleId, List<UUID> permissionIds) {
        for (UUID permissionId : permissionIds) {
            removePermissionFromRole(roleId, permissionId);
        }
    }
}
