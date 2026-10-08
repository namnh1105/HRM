package com.hainam.worksphere.employee.repository;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.domain.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    @Query("SELECT e FROM Employee e WHERE e.isDeleted = false")
    Page<Employee> findAllActive(Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.isDeleted = false")
    List<Employee> findAllActiveList();

    @Query("SELECT e FROM Employee e WHERE e.id = :id AND e.isDeleted = false")
    Optional<Employee> findActiveById(@Param("id") UUID id);

    @Query("SELECT e FROM Employee e WHERE e.employeeCode = :code AND e.isDeleted = false")
    Optional<Employee> findActiveByEmployeeCode(@Param("code") String code);

    @Query("SELECT e FROM Employee e WHERE e.email = :email AND e.isDeleted = false")
    Optional<Employee> findActiveByEmail(@Param("email") String email);

    @Query("SELECT e FROM Employee e WHERE e.user.id = :userId AND e.isDeleted = false")
    Optional<Employee> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT e FROM Employee e WHERE e.user.id = :userId")
    Optional<Employee> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Employee e WHERE e.employeeCode = :code AND e.isDeleted = false")
    boolean existsActiveByEmployeeCode(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Employee e WHERE e.email = :email AND e.isDeleted = false")
    boolean existsActiveByEmail(@Param("email") String email);

    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId AND e.isDeleted = false")
    Page<Employee> findActiveByDepartmentId(@Param("departmentId") UUID departmentId, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.employmentStatus = :status AND e.isDeleted = false")
    Page<Employee> findActiveByEmploymentStatus(@Param("status") EmploymentStatus status, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.store.id = :storeId AND e.isDeleted = false")
    Page<Employee> findActiveByStoreId(@Param("storeId") UUID storeId, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.isDeleted = false AND (" +
            "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.position) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Employee> searchActive(@Param("keyword") String keyword, Pageable pageable);

    long countByIsDeletedFalse();
    long countByEmploymentStatusAndIsDeletedFalse(EmploymentStatus status);
    long countByIsDeletedTrue();
}
