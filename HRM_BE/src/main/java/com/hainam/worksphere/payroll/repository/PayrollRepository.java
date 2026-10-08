package com.hainam.worksphere.payroll.repository;

import com.hainam.worksphere.payroll.domain.Payroll;
import com.hainam.worksphere.payroll.domain.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, UUID> {

    @Query("SELECT p FROM Payroll p WHERE p.isDeleted = false")
    List<Payroll> findAllActive();

    @Query("SELECT p FROM Payroll p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Payroll> findActiveById(@Param("id") UUID id);

    @Query("SELECT p FROM Payroll p WHERE p.employee.id = :employeeId AND p.isDeleted = false ORDER BY p.year DESC, p.month DESC")
    List<Payroll> findActiveByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT p FROM Payroll p WHERE p.employee.id = :employeeId AND p.month = :month AND p.year = :year AND p.isDeleted = false")
    Optional<Payroll> findActiveByEmployeeIdAndMonthAndYear(@Param("employeeId") UUID employeeId, @Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT p FROM Payroll p WHERE p.month = :month AND p.year = :year AND p.isDeleted = false ORDER BY p.employee.fullName ASC")
    List<Payroll> findActiveByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT p FROM Payroll p WHERE p.status = :status AND p.isDeleted = false ORDER BY p.year DESC, p.month DESC")
    List<Payroll> findActiveByStatus(@Param("status") PayrollStatus status);
}
