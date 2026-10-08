package com.hainam.worksphere.insurance.repository;

import com.hainam.worksphere.insurance.domain.InsuranceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InsuranceRegistrationRepository extends JpaRepository<InsuranceRegistration, UUID> {

    @Query("SELECT r FROM InsuranceRegistration r WHERE r.isDeleted = false")
    List<InsuranceRegistration> findAllActive();

    @Query("SELECT r FROM InsuranceRegistration r WHERE r.id = :id AND r.isDeleted = false")
    Optional<InsuranceRegistration> findActiveById(@Param("id") UUID id);

    @Query("SELECT r FROM InsuranceRegistration r WHERE r.employee.id = :employeeId AND r.isDeleted = false")
    List<InsuranceRegistration> findActiveByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT r FROM InsuranceRegistration r WHERE r.insurance.id = :insuranceId AND r.isDeleted = false")
    List<InsuranceRegistration> findActiveByInsuranceId(@Param("insuranceId") UUID insuranceId);

    @Query("SELECT r FROM InsuranceRegistration r WHERE r.employee.id = :employeeId AND r.insurance.id = :insuranceId AND r.isDeleted = false")
    List<InsuranceRegistration> findActiveByEmployeeIdAndInsuranceId(@Param("employeeId") UUID employeeId, @Param("insuranceId") UUID insuranceId);
}
