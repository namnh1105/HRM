package com.hainam.worksphere.insurance.repository;

import com.hainam.worksphere.insurance.domain.Insurance;
import com.hainam.worksphere.insurance.domain.InsuranceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, UUID> {

    @Query("SELECT i FROM Insurance i WHERE i.isDeleted = false")
    List<Insurance> findAllActive();

    @Query("SELECT i FROM Insurance i WHERE i.id = :id AND i.isDeleted = false")
    Optional<Insurance> findActiveById(@Param("id") UUID id);

    @Query("SELECT i FROM Insurance i WHERE i.code = :code AND i.isDeleted = false")
    Optional<Insurance> findActiveByCode(@Param("code") String code);

    @Query("SELECT i FROM Insurance i WHERE i.insuranceType = :insuranceType AND i.isDeleted = false")
    List<Insurance> findActiveByInsuranceType(@Param("insuranceType") InsuranceType insuranceType);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Insurance i WHERE i.code = :code AND i.isDeleted = false")
    boolean existsActiveByCode(@Param("code") String code);
}
