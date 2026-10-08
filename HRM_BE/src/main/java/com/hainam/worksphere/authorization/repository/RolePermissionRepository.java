package com.hainam.worksphere.authorization.repository;

import com.hainam.worksphere.authorization.domain.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    Optional<RolePermission> findByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    List<RolePermission> findByRoleId(UUID roleId);

    List<RolePermission> findByPermissionId(UUID permissionId);

    List<RolePermission> findByRoleIdAndIsActiveTrue(UUID roleId);

    List<RolePermission> findByPermissionIdAndIsActiveTrue(UUID permissionId);

    @Query("SELECT COUNT(rp) > 0 FROM RolePermission rp WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId AND rp.isActive = true")
    boolean existsByRoleIdAndPermissionIdAndIsActiveTrue(@Param("roleId") UUID roleId, @Param("permissionId") UUID permissionId);

    @Query("SELECT COUNT(rp) > 0 FROM RolePermission rp WHERE rp.role.code = :roleCode AND rp.permission.code = :permissionCode AND rp.isActive = true")
    boolean existsByRoleCodeAndPermissionCodeAndIsActiveTrue(@Param("roleCode") String roleCode, @Param("permissionCode") String permissionCode);

    @Modifying
    @Transactional
    @Query("UPDATE RolePermission rp SET rp.isActive = false WHERE rp.role.id = :roleId")
    void deactivateByRoleId(@Param("roleId") UUID roleId);

    @Modifying
    @Transactional
    @Query("UPDATE RolePermission rp SET rp.isActive = false WHERE rp.permission.id = :permissionId")
    void deactivateByPermissionId(@Param("permissionId") UUID permissionId);

    @Modifying
    @Transactional
    @Query("UPDATE RolePermission rp SET rp.isActive = true WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId")
    void activate(@Param("roleId") UUID roleId, @Param("permissionId") UUID permissionId);

    @Modifying
    @Transactional
    @Query("UPDATE RolePermission rp SET rp.isActive = false WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId")
    void deactivate(@Param("roleId") UUID roleId, @Param("permissionId") UUID permissionId);
}
