package com.hainam.worksphere.authorization.service;

import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.repository.RoleRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional
    @CacheEvict(value = {CacheConfig.ROLE_CACHE, CacheConfig.ROLE_BY_CODE_CACHE, CacheConfig.ACTIVE_ROLES_CACHE, CacheConfig.SYSTEM_ROLES_CACHE}, allEntries = true)
    public Role createRole(Role role) {
        if (roleRepository.existsByCode(role.getCode())) {
            throw new DuplicateResourceException("Role with code '" + role.getCode() + "' already exists");
        }

        return roleRepository.save(role);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.ROLE_CACHE, key = "#roleId.toString()"),
        @CacheEvict(value = CacheConfig.ROLE_BY_CODE_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, allEntries = true)
    })
    public Role updateRole(UUID roleId, Role roleUpdate) {
        Role existingRole = getRoleById(roleId);
        if (!existingRole.getCode().equals(roleUpdate.getCode()) &&
            roleRepository.existsByCode(roleUpdate.getCode())) {
            throw new DuplicateResourceException("Role with code '" + roleUpdate.getCode() + "' already exists");
        }

        existingRole.setCode(roleUpdate.getCode());
        existingRole.setDisplayName(roleUpdate.getDisplayName());
        existingRole.setDescription(roleUpdate.getDescription());
        existingRole.setIsActive(roleUpdate.getIsActive());

        return roleRepository.save(existingRole);
    }

    @Cacheable(value = CacheConfig.ROLE_CACHE, key = "#roleId.toString()")
    public Role getRoleById(UUID roleId) {
        return roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
    }

    @Cacheable(value = CacheConfig.ROLE_BY_CODE_CACHE, key = "#code")
    public Optional<Role> getRoleByCode(String code) {
        return roleRepository.findByCode(code);
    }

    public Page<Role> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    public Page<Role> getAllActiveRoles(Pageable pageable) {
        return roleRepository.findAllActive(pageable);
    }

    public List<Role> getAllSystemRoles() {
        return roleRepository.findByIsSystemTrueAndIsDeletedFalse();
    }

    @Cacheable(value = CacheConfig.USER_ROLES_CACHE, key = "#userId.toString()")
    public List<Role> getRolesByUserId(UUID userId) {
        return roleRepository.findByUserId(userId);
    }

    public List<Role> getRolesByPermissionCode(String permissionCode) {
        return roleRepository.findByPermissionCode(permissionCode);
    }

    public List<Role> getRolesByCodes(Set<String> codes) {
        return roleRepository.findByCodeInAndIsDeletedFalse(codes);
    }

    public Page<Role> searchRoles(String search, Pageable pageable) {
        return roleRepository.searchRoles(search, pageable);
    }


    public boolean userHasRole(UUID userId, UUID roleId) {
        return roleRepository.userHasRole(userId, roleId);
    }

    public boolean userHasRole(UUID userId, String roleCode) {
        return roleRepository.userHasRoleByCode(userId, roleCode);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.ROLE_CACHE, key = "#roleId.toString()"),
        @CacheEvict(value = CacheConfig.ROLE_BY_CODE_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, allEntries = true)
    })
    public void deleteRole(UUID roleId) {
        Role role = getRoleById(roleId);

        role.setIsDeleted(true);
        role.setDeletedAt(Instant.now());
        role.setIsActive(false);
        roleRepository.save(role);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.ROLE_CACHE, key = "#roleId.toString()"),
        @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, allEntries = true)
    })
    public void restoreRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        role.setIsDeleted(false);
        role.setDeletedAt(null);
        role.setIsActive(true);
        roleRepository.save(role);
    }

    public Page<Role> getAllRoles(Pageable pageable, boolean includeDeleted) {
        if (includeDeleted) return roleRepository.findAll(pageable);
        return roleRepository.findByIsDeletedFalse(pageable);
    }

    public List<Role> getAllDeletedRoles() {
        return roleRepository.findByIsDeletedTrue();
    }

    public boolean existsByCode(String code) {
        return roleRepository.existsByCode(code);
    }

    public ResourceStatsResponse getRoleStats() {
        return ResourceStatsResponse.builder()
                .total(roleRepository.count())
                .active(roleRepository.countByIsActiveTrueAndIsDeletedFalse())
                .inactive(roleRepository.countByIsActiveFalseAndIsDeletedFalse())
                .deleted(roleRepository.countByIsDeletedTrue())
                .build();
    }
}
