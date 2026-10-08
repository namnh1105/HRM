package com.hainam.worksphere.authorization.service;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.repository.PermissionRepository;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.dto.ResourceStatsResponse;
import com.hainam.worksphere.shared.exception.BusinessRuleViolationException;
import com.hainam.worksphere.shared.exception.DuplicateResourceException;
import com.hainam.worksphere.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional
    @CacheEvict(value = {CacheConfig.PERMISSION_CACHE, CacheConfig.PERMISSION_BY_CODE_CACHE, CacheConfig.ACTIVE_PERMISSIONS_CACHE, CacheConfig.SYSTEM_PERMISSIONS_CACHE}, allEntries = true)
    public Permission createPermission(Permission permission) {
        if (permissionRepository.existsByCode(permission.getCode())) {
            throw new DuplicateResourceException("Permission with code '" + permission.getCode() + "' already exists");
        }

        return permissionRepository.save(permission);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.PERMISSION_CACHE, key = "#permissionId.toString()"),
        @CacheEvict(value = CacheConfig.PERMISSION_BY_CODE_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.ROLE_PERMISSIONS_CACHE, allEntries = true)
    })
    public Permission updatePermission(UUID permissionId, Permission permissionUpdate) {
        Permission existingPermission = getPermissionById(permissionId);

        if (!existingPermission.getCode().equals(permissionUpdate.getCode()) &&
            permissionRepository.existsByCode(permissionUpdate.getCode())) {
            throw new DuplicateResourceException("Permission with code '" + permissionUpdate.getCode() + "' already exists");
        }

        existingPermission.setCode(permissionUpdate.getCode());
        existingPermission.setDisplayName(permissionUpdate.getDisplayName());
        existingPermission.setDescription(permissionUpdate.getDescription());
        existingPermission.setResource(permissionUpdate.getResource());
        existingPermission.setAction(permissionUpdate.getAction());
        existingPermission.setIsActive(permissionUpdate.getIsActive());

        return permissionRepository.save(existingPermission);
    }

    @Cacheable(value = CacheConfig.PERMISSION_CACHE, key = "#permissionId.toString()")
    public Permission getPermissionById(UUID permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));
    }

    @Cacheable(value = CacheConfig.PERMISSION_BY_CODE_CACHE, key = "#code")
    public Optional<Permission> getPermissionByCode(String code) {
        return permissionRepository.findByCode(code);
    }

    public Page<Permission> getAllPermissions(Pageable pageable, boolean includeDeleted) {
        if (includeDeleted) return permissionRepository.findAll(pageable);
        return permissionRepository.findByIsDeletedFalse(pageable);
    }

    public List<Permission> getAllActivePermissions() {
        return permissionRepository.findByIsActiveTrueAndIsDeletedFalse();
    }

    public List<Permission> getAllSystemPermissions() {
        return permissionRepository.findByIsSystemTrueAndIsDeletedFalse();
    }

    public List<Permission> getPermissionsByResource(String resource) {
        return permissionRepository.findByResourceAndIsDeletedFalse(resource);
    }

    public List<Permission> getPermissionsByAction(String action) {
        return permissionRepository.findByActionAndIsDeletedFalse(action);
    }

    public List<Permission> getPermissionsByResourceAndAction(String resource, String action) {
        return permissionRepository.findByResourceAndActionAndIsDeletedFalse(resource, action);
    }

    @Cacheable(value = CacheConfig.ROLE_PERMISSIONS_CACHE, key = "#roleId.toString()")
    public List<Permission> getPermissionsByRoleId(UUID roleId) {
        return permissionRepository.findByRoleId(roleId);
    }

    public List<Permission> getPermissionsByRoleCode(String roleCode) {
        return permissionRepository.findByRoleCode(roleCode);
    }

    @Cacheable(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId.toString()")
    public List<Permission> getPermissionsByUserId(UUID userId) {
        return permissionRepository.findByUserId(userId);
    }

    public List<Permission> getPermissionsByCodes(Set<String> codes) {
        return permissionRepository.findByCodeInAndIsDeletedFalse(codes);
    }

    public List<Permission> searchPermissions(String search) {
        return permissionRepository.searchByCodeOrDisplayName(search);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.PERMISSION_CACHE, key = "#permissionId.toString()"),
        @CacheEvict(value = CacheConfig.PERMISSION_BY_CODE_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.ROLE_PERMISSIONS_CACHE, allEntries = true)
    })
    public void deletePermission(UUID permissionId) {
        Permission permission = getPermissionById(permissionId);
        
        permission.setIsDeleted(true);
        permission.setIsActive(false);
        permission.setDeletedAt(java.time.Instant.now());
        permissionRepository.save(permission);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.PERMISSION_CACHE, key = "#permissionId.toString()"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, allEntries = true)
    })
    public void restorePermission(UUID permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));
        permission.setIsDeleted(false);
        permission.setDeletedAt(null);
        permission.setIsActive(true);
        permissionRepository.save(permission);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.PERMISSION_CACHE, key = "#permissionId.toString()"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, allEntries = true)
    })
    public void activatePermission(UUID permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));
        permission.setIsActive(true);
        permissionRepository.save(permission);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.PERMISSION_CACHE, key = "#permissionId.toString()"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, allEntries = true)
    })
    public void deactivatePermission(UUID permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));
        permission.setIsActive(false);
        permissionRepository.save(permission);
    }

    public boolean existsByCode(String code) {
        return permissionRepository.existsByCode(code);
    }

    public ResourceStatsResponse getPermissionStats() {
        return ResourceStatsResponse.builder()
                .total(permissionRepository.count())
                .active(permissionRepository.countByIsActiveTrueAndIsDeletedFalse())
                .inactive(permissionRepository.countByIsActiveFalseAndIsDeletedFalse())
                .deleted(permissionRepository.countByIsDeletedTrue())
                .build();
    }
}
