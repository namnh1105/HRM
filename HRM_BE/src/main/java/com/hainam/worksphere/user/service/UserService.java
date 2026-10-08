package com.hainam.worksphere.user.service;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.shared.audit.annotation.AuditAction;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.util.AuditContext;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.exception.UserNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.dto.request.ChangePasswordRequest;
import com.hainam.worksphere.user.dto.request.UpdateProfileRequest;
import com.hainam.worksphere.user.dto.response.UserResponse;
import com.hainam.worksphere.shared.dto.ResourceStatsResponse;
import com.hainam.worksphere.user.mapper.UserMapper;
import com.hainam.worksphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserResponse getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findActiveById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));
        Employee employee = employeeRepository.findByUserId(user.getId()).orElse(null);
        
        return mapToUserResponseWithAuthorities(user, employee, userPrincipal);
    }

    @Cacheable(value = CacheConfig.USER_CACHE, key = "#userId.toString()")
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.toString()));
        Employee employee = employeeRepository.findByUserId(user.getId()).orElse(null);
        return userMapper.toUserResponse(user, employee);
    }

    public Page<UserResponse> getAllActiveUsers(Pageable pageable) {
        return userRepository.findAllActive(pageable)
                .map(user -> {
                    Employee employee = employeeRepository.findByUserId(user.getId()).orElse(null);
                    return userMapper.toUserResponse(user, employee);
                });
    }

    public Page<UserResponse> getAllUsers(boolean includeDeleted, Pageable pageable) {
        Page<User> users = includeDeleted ? userRepository.findAll(pageable) : userRepository.findAllActive(pageable);
        return users.map(user -> {
                    Employee employee = employeeRepository.findByUserId(user.getId()).orElse(null);
                    return userMapper.toUserResponse(user, employee);
                });
    }

    public Page<UserResponse> searchUsers(String keyword, boolean includeDeleted, Pageable pageable) {
        return userRepository.searchByKeyword(keyword, includeDeleted, pageable)
                .map(user -> {
                    Employee employee = employeeRepository.findByUserId(user.getId()).orElse(null);
                    return userMapper.toUserResponse(user, employee);
                });
    }

    public ResourceStatsResponse getUserStats() {
        return ResourceStatsResponse.builder()
                .total(userRepository.countTotal())
                .active(userRepository.countActive())
                .inactive(userRepository.countInactive())
                .deleted(userRepository.countDeleted())
                .build();
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_BY_EMAIL_CACHE, allEntries = true)
    })
    public UserResponse activateUser(UUID userId, UUID updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.toString()));
        user.setIsEnabled(true);
        user.setUpdatedBy(updatedBy);
        User savedUser = userRepository.save(user);
        Employee employee = employeeRepository.findByUserId(savedUser.getId()).orElse(null);
        return userMapper.toUserResponse(savedUser, employee);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_BY_EMAIL_CACHE, allEntries = true)
    })
    public UserResponse deactivateUser(UUID userId, UUID updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.toString()));
        user.setIsEnabled(false);
        user.setUpdatedBy(updatedBy);
        User savedUser = userRepository.save(user);
        Employee employee = employeeRepository.findByUserId(savedUser.getId()).orElse(null);
        return userMapper.toUserResponse(savedUser, employee);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userPrincipal.id.toString()"),
        @CacheEvict(value = CacheConfig.USER_BY_EMAIL_CACHE, allEntries = true)
    })
    @AuditAction(type = ActionType.UPDATE, entity = "USER", actionCode = "UPDATE_PROFILE")
    public UserResponse updateProfile(UserPrincipal userPrincipal, UpdateProfileRequest request) {
        User user = userRepository.findActiveById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));

        Employee employee = employeeRepository.findByUserId(user.getId())
                .orElseGet(() -> Employee.builder()
                        .user(user)
                        .employeeCode(null)
                .firstName("")
                .lastName("")
                .fullName("")
                        .email(user.getEmail())
                        .createdBy(userPrincipal.getId())
                        .build());

        AuditContext.snapshot(employee);

        if (request.getGivenName() != null) {
            employee.setFirstName(request.getGivenName());
        }
        if (request.getFamilyName() != null) {
            employee.setLastName(request.getFamilyName());
        }
        if (request.getGivenName() != null || request.getFamilyName() != null) {
            employee.setFullName(buildFullName(employee.getLastName(), employee.getFirstName()));
        }
        if (request.getAvatarUrl() != null) {
            employee.setAvatarUrl(request.getAvatarUrl());
        }
        employee.setUpdatedBy(userPrincipal.getId());
        Employee savedEmployee = employeeRepository.save(employee);

        AuditContext.registerUpdated(savedEmployee);

        return mapToUserResponseWithAuthorities(user, savedEmployee, userPrincipal);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userPrincipal.id.toString()")
    public void changePassword(UserPrincipal userPrincipal, ChangePasswordRequest request) {
        User user = userRepository.findActiveById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw ValidationException.passwordMismatch();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedBy(userPrincipal.getId());
        userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userPrincipal.id.toString()")
    public void deactivateAccount(UserPrincipal userPrincipal) {
        User user = userRepository.findActiveById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));

        user.setIsEnabled(false);
        user.setUpdatedBy(userPrincipal.getId());
        userRepository.save(user);
    }

    // Soft Delete Methods
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId.toString()")
    })
    @AuditAction(type = ActionType.DELETE, entity = "USER")
    public void softDeleteUser(UUID userId, UUID deletedBy) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.toString()));

        AuditContext.registerDeleted(user);

        user.setIsDeleted(true);
        user.setDeletedAt(Instant.now());
        user.setDeletedBy(deletedBy);
        user.setUpdatedBy(deletedBy);

        userRepository.save(user);
    }

    @Transactional
    public void softDeleteCurrentUser(UserPrincipal userPrincipal) {
        softDeleteUser(userPrincipal.getId(), userPrincipal.getId());
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId.toString()")
    })
    public UserResponse restoreUser(UUID userId, UUID restoredBy) {
        User user = userRepository.findDeletedById(userId)
                .orElseThrow(() -> new ValidationException("User not found or not deleted"));

        user.setIsDeleted(false);
        user.setDeletedAt(null);
        user.setDeletedBy(null);
        user.setUpdatedBy(restoredBy);

        User restoredUser = userRepository.save(user);
        Employee employee = employeeRepository.findByUserId(restoredUser.getId()).orElse(null);
        return userMapper.toUserResponse(restoredUser, employee);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userId.toString()")
    public void permanentDeleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.toString()));

        userRepository.delete(user);
    }

    private UserResponse mapToUserResponseWithAuthorities(User user, Employee employee, UserPrincipal userPrincipal) {
        UserResponse response = userMapper.toUserResponse(user, employee);
        if (userPrincipal != null) {
            if (userPrincipal.getRoles() != null) {
                response.setRoles(userPrincipal.getRoles().stream()
                        .map(com.hainam.worksphere.authorization.domain.Role::getCode)
                        .collect(Collectors.toList()));
            }
            if (userPrincipal.getPermissions() != null) {
                response.setPermissions(userPrincipal.getPermissions().stream()
                        .map(com.hainam.worksphere.authorization.domain.Permission::getCode)
                        .collect(Collectors.toList()));
            }
        }
        return response;
    }

    private String buildFullName(String familyName, String givenName) {
        String safeFamilyName = familyName != null ? familyName.trim() : "";
        String safeGivenName = givenName != null ? givenName.trim() : "";
        return (safeFamilyName + " " + safeGivenName).trim();
    }
}
