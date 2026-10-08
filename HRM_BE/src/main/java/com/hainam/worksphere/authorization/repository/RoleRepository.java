package com.hainam.worksphere.authorization.repository;

import com.hainam.worksphere.authorization.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    boolean existsByCode(String code);

    Optional<Role> findByCode(String code);

    List<Role> findByIsActiveTrueAndIsDeletedFalse();

    List<Role> findByIsSystemTrueAndIsDeletedFalse();

    @Query("SELECT r FROM Role r JOIN UserRole ur ON ur.role.id = r.id WHERE ur.userId = :userId AND ur.isActive = true AND r.isActive = true AND r.isDeleted = false")
    List<Role> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT r FROM Role r JOIN r.rolePermissions rp JOIN rp.permission p WHERE p.code = :permissionCode AND rp.isActive = true AND r.isActive = true AND r.isDeleted = false")
    List<Role> findByPermissionCode(@Param("permissionCode") String permissionCode);

    @Query("SELECT r FROM Role r JOIN r.rolePermissions rp WHERE rp.permission.id = :permissionId AND rp.isActive = true AND r.isActive = true AND r.isDeleted = false")
    List<Role> findByPermissionId(@Param("permissionId") UUID permissionId);

    List<Role> findByCodeInAndIsDeletedFalse(Set<String> codes);

    @Query("SELECT r FROM Role r WHERE r.isActive = true AND r.isDeleted = false")
    Page<Role> findAllActive(Pageable pageable);

    @Query("SELECT r FROM Role r WHERE (LOWER(r.displayName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.code) LIKE LOWER(CONCAT('%', :query, '%'))) AND r.isDeleted = false")
    Page<Role> searchRoles(@Param("query") String query, Pageable pageable);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.rolePermissions rp LEFT JOIN FETCH rp.permission WHERE r.id = :id AND r.isDeleted = false")
    Optional<Role> findByIdWithPermissions(@Param("id") UUID id);

    List<Role> findByIsDeletedTrue();
    List<Role> findByIsDeletedFalse();
    Page<Role> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.userId = :userId AND ur.role.id = :roleId AND ur.isActive = true")
    boolean userHasRole(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.userId = :userId AND ur.role.code = :roleCode AND ur.isActive = true")
    boolean userHasRoleByCode(@Param("userId") UUID userId, @Param("roleCode") String roleCode);

    long countByIsDeletedFalse();
    long countByIsActiveTrueAndIsDeletedFalse();
    long countByIsActiveFalseAndIsDeletedFalse();
    long countByIsDeletedTrue();
}
