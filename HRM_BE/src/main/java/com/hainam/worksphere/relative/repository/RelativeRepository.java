package com.hainam.worksphere.relative.repository;

import com.hainam.worksphere.relative.domain.Relative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RelativeRepository extends JpaRepository<Relative, UUID> {

    @Query("SELECT r FROM Relative r WHERE r.isDeleted = false")
    List<Relative> findAllActive();

    @Query("SELECT r FROM Relative r WHERE r.id = :id AND r.isDeleted = false")
    Optional<Relative> findActiveById(@Param("id") UUID id);

    @Query("SELECT r FROM Relative r WHERE r.employee.id = :employeeId AND r.isDeleted = false")
    List<Relative> findActiveByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT r FROM Relative r WHERE r.employee.id = :employeeId AND r.isEmergencyContact = true AND r.isDeleted = false")
    List<Relative> findActiveEmergencyContactsByEmployeeId(@Param("employeeId") UUID employeeId);
}
