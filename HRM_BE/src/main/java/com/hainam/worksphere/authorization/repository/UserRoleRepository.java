package com.hainam.worksphere.authorization.repository;

import com.hainam.worksphere.authorization.domain.UserRole;
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
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    Optional<UserRole> findByUserIdAndRoleId(UUID userId, UUID roleId);

    List<UserRole> findByUserId(UUID userId);

    List<UserRole> findByRoleId(UUID roleId);

    List<UserRole> findByUserIdAndIsActiveTrue(UUID userId);

    List<UserRole> findByRoleIdAndIsActiveTrue(UUID roleId);

    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.userId = :userId AND ur.role.id = :roleId AND ur.isActive = true")
    boolean existsByUserIdAndRoleIdAndIsActiveTrue(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.userId = :userId AND ur.role.code = :roleCode AND ur.isActive = true")
    boolean existsByUserIdAndRoleCodeAndIsActiveTrue(@Param("userId") UUID userId, @Param("roleCode") String roleCode);

    @Modifying
    @Transactional
    @Query("UPDATE UserRole ur SET ur.isActive = false WHERE ur.userId = :userId")
    void deactivateByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserRole ur SET ur.isActive = false WHERE ur.role.id = :roleId")
    void deactivateByRoleId(@Param("roleId") UUID roleId);

    @Modifying
    @Transactional
    @Query("UPDATE UserRole ur SET ur.isActive = true WHERE ur.userId = :userId AND ur.role.id = :roleId")
    void activate(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    @Modifying
    @Transactional
    @Query("UPDATE UserRole ur SET ur.isActive = false WHERE ur.userId = :userId AND ur.role.id = :roleId")
    void deactivate(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.role.id = :roleId AND ur.isActive = true")
    long countActiveUsersByRoleId(@Param("roleId") UUID roleId);

    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.userId = :userId AND ur.isActive = true")
    long countActiveRolesByUserId(@Param("userId") UUID userId);
}
