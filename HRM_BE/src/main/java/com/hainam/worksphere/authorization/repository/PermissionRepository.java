package com.hainam.worksphere.authorization.repository;

import com.hainam.worksphere.authorization.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    boolean existsByCode(String code);

    Optional<Permission> findByCode(String code);

    List<Permission> findByIsActiveTrueAndIsDeletedFalse();

    List<Permission> findByIsSystemTrueAndIsDeletedFalse();

    List<Permission> findByResourceAndIsDeletedFalse(String resource);

    List<Permission> findByActionAndIsDeletedFalse(String action);

    List<Permission> findByResourceAndActionAndIsDeletedFalse(String resource, String action);

    @Query("SELECT p FROM Permission p JOIN p.rolePermissions rp WHERE rp.role.id = :roleId AND rp.isActive = true AND p.isActive = true AND p.isDeleted = false")
    List<Permission> findByRoleId(@Param("roleId") UUID roleId);

    @Query("SELECT p FROM Permission p JOIN p.rolePermissions rp WHERE rp.role.code = :roleCode AND rp.isActive = true AND p.isActive = true AND p.isDeleted = false")
    List<Permission> findByRoleCode(@Param("roleCode") String roleCode);

    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN p.rolePermissions rp " +
           "JOIN rp.role r " +
           "JOIN UserRole ur ON ur.role.id = r.id " +
           "WHERE ur.userId = :userId AND ur.isActive = true AND rp.isActive = true AND p.isActive = true AND p.isDeleted = false")
    List<Permission> findByUserId(@Param("userId") UUID userId);

    List<Permission> findByCodeInAndIsDeletedFalse(Set<String> codes);

    @Query("SELECT p FROM Permission p WHERE " +
           "(LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.displayName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "p.isActive = true AND p.isDeleted = false")
    List<Permission> searchByCodeOrDisplayName(@Param("search") String search);

    List<Permission> findByIsDeletedTrue();
    List<Permission> findByIsDeletedFalse();
    org.springframework.data.domain.Page<Permission> findByIsDeletedFalse(org.springframework.data.domain.Pageable pageable);

    long countByIsDeletedFalse();
    long countByIsActiveTrueAndIsDeletedFalse();
    long countByIsActiveFalseAndIsDeletedFalse();
    long countByIsDeletedTrue();
}
