package com.hainam.worksphere.department.repository;

import com.hainam.worksphere.department.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    @Query("SELECT d FROM Department d WHERE d.isDeleted = false")
    List<Department> findAllActive();

    @Query("SELECT d FROM Department d WHERE d.id = :id AND d.isDeleted = false")
    Optional<Department> findActiveById(@Param("id") UUID id);

    @Query("SELECT d FROM Department d WHERE d.code = :code AND d.isDeleted = false")
    Optional<Department> findActiveByCode(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Department d WHERE d.code = :code AND d.isDeleted = false")
    boolean existsActiveByCode(@Param("code") String code);

    @Query("SELECT d FROM Department d WHERE d.parentDepartment.id = :parentId AND d.isDeleted = false")
    List<Department> findActiveByParentDepartmentId(@Param("parentId") UUID parentId);

    @Query("SELECT d FROM Department d WHERE d.manager.id = :managerId AND d.isDeleted = false")
    List<Department> findActiveByManagerId(@Param("managerId") UUID managerId);

    @Query("SELECT d FROM Department d WHERE d.isActive = true AND d.isDeleted = false")
    List<Department> findAllActiveAndEnabled();

    @Query("SELECT d FROM Department d WHERE d.store.id = :storeId AND d.isDeleted = false")
    List<Department> findActiveByStoreId(@Param("storeId") UUID storeId);

    @Query("SELECT d FROM Department d WHERE d.isDeleted = false AND (" +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.email) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    List<Department> searchActive(@Param("keyword") String keyword);
}
