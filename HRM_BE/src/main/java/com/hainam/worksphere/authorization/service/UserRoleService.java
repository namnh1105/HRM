package com.hainam.worksphere.authorization.service;

import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.domain.UserRole;
import com.hainam.worksphere.authorization.repository.UserRoleRepository;
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
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final RoleService roleService;

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId.toString()")
    })
    public UserRole assignRoleToUser(UUID userId, UUID roleId) {
        Role role = roleService.getRoleById(roleId);

        Optional<UserRole> existing = userRoleRepository.findByUserIdAndRoleId(userId, roleId);
        if (existing.isPresent()) {
            if (existing.get().getIsActive()) {
                throw new DuplicateResourceException("Role is already assigned to user");
            } else {
                existing.get().setIsActive(true);
                return userRoleRepository.save(existing.get());
            }
        }

        UserRole userRole = UserRole.builder()
                .userId(userId)
                .role(role)
                .isActive(true)
                .build();

        return userRoleRepository.save(userRole);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId.toString()")
    })
    public void removeRoleFromUser(UUID userId, UUID roleId) {
        UserRole userRole = userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role assignment not found"));

        userRole.setIsActive(false);
        userRoleRepository.save(userRole);
    }

    public Optional<UserRole> getUserRole(UUID userId, UUID roleId) {
        return userRoleRepository.findByUserIdAndRoleId(userId, roleId);
    }

    public List<UserRole> getUserRolesByUserId(UUID userId) {
        return userRoleRepository.findByUserId(userId);
    }

    public List<UserRole> getActiveUserRolesByUserId(UUID userId) {
        return userRoleRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public List<UserRole> getUserRolesByRoleId(UUID roleId) {
        return userRoleRepository.findByRoleId(roleId);
    }

    public List<UserRole> getActiveUserRolesByRoleId(UUID roleId) {
        return userRoleRepository.findByRoleIdAndIsActiveTrue(roleId);
    }

    public boolean userHasRole(UUID userId, UUID roleId) {
        return userRoleRepository.existsByUserIdAndRoleIdAndIsActiveTrue(userId, roleId);
    }

    public boolean userHasRole(UUID userId, String roleCode) {
        return userRoleRepository.existsByUserIdAndRoleCodeAndIsActiveTrue(userId, roleCode);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId.toString()")
    })
    public void deactivateAllRolesForUser(UUID userId) {
        userRoleRepository.deactivateByUserId(userId);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, allEntries = true)
    public void deactivateAllUsersForRole(UUID roleId) {
        userRoleRepository.deactivateByRoleId(roleId);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId.toString()")
    })
    public void activateUserRole(UUID userId, UUID roleId) {
        userRoleRepository.activate(userId, roleId);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId.toString()")
    })
    public void deactivateUserRole(UUID userId, UUID roleId) {
        userRoleRepository.deactivate(userId, roleId);
    }

    public long countActiveUsersByRoleId(UUID roleId) {
        return userRoleRepository.countActiveUsersByRoleId(roleId);
    }

    public long countActiveRolesByUserId(UUID userId) {
        return userRoleRepository.countActiveRolesByUserId(userId);
    }

    @Transactional
    public void assignRolesToUser(UUID userId, List<UUID> roleIds) {
        for (UUID roleId : roleIds) {
            assignRoleToUser(userId, roleId);
        }
    }

    @Transactional
    public void removeRolesFromUser(UUID userId, List<UUID> roleIds) {
        log.info("Removing {} roles from user {}", roleIds.size(), userId);

        for (UUID roleId : roleIds) {
            removeRoleFromUser(userId, roleId);
        }
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId.toString()")
    })
    public void replaceUserRoles(UUID userId, List<UUID> newRoleIds) {
        deactivateAllRolesForUser(userId);

        assignRolesToUser(userId, newRoleIds);
    }
}
