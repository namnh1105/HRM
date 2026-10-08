package com.hainam.worksphere.workshift.repository;

import com.hainam.worksphere.workshift.domain.EmployeeWorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeWorkShiftRepository extends JpaRepository<EmployeeWorkShift, UUID> {

    @Query("SELECT ews FROM EmployeeWorkShift ews " +
           "JOIN FETCH ews.workShift ws JOIN FETCH ews.employee emp " +
           "WHERE ews.isDeleted = false")
    List<EmployeeWorkShift> findAllActive();

    @Query("SELECT ews FROM EmployeeWorkShift ews WHERE ews.id = :id AND ews.isDeleted = false")
    Optional<EmployeeWorkShift> findActiveById(@Param("id") UUID id);

    @Query("SELECT ews FROM EmployeeWorkShift ews JOIN FETCH ews.workShift ws JOIN FETCH ews.employee emp " +
           "WHERE ews.employee.id = :employeeId AND ews.isDeleted = false " +
           "ORDER BY ews.date DESC")
    List<EmployeeWorkShift> findActiveByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT ews FROM EmployeeWorkShift ews JOIN FETCH ews.workShift ws JOIN FETCH ews.employee emp " +
           "WHERE ews.workShift.id = :workShiftId AND ews.isDeleted = false")
    List<EmployeeWorkShift> findActiveByWorkShiftId(@Param("workShiftId") UUID workShiftId);

    @Query("SELECT ews FROM EmployeeWorkShift ews JOIN FETCH ews.workShift ws JOIN FETCH ews.employee emp " +
           "WHERE ews.employee.id = :employeeId " +
           "AND ews.date = :date " +
           "AND ews.isDeleted = false")
    List<EmployeeWorkShift> findActiveByEmployeeIdAndDate(
            @Param("employeeId") UUID employeeId,
            @Param("date") LocalDate date
    );

    @Query("SELECT COUNT(ews) FROM EmployeeWorkShift ews WHERE ews.employee.id = :employeeId " +
          "AND ews.date BETWEEN :startDate AND :endDate " +
          "AND ews.isDeleted = false")
    long countActiveByEmployeeIdAndDateBetween(
           @Param("employeeId") UUID employeeId,
           @Param("startDate") LocalDate startDate,
           @Param("endDate") LocalDate endDate
    );

    @Query("SELECT CASE WHEN COUNT(ews) > 0 THEN true ELSE false END FROM EmployeeWorkShift ews " +
           "WHERE ews.employee.id = :employeeId AND ews.workShift.id = :workShiftId " +
           "AND ews.date = :date " +
           "AND ews.isDeleted = false")
    boolean existsActiveAssignment(
            @Param("employeeId") UUID employeeId,
            @Param("workShiftId") UUID workShiftId,
            @Param("date") LocalDate date
    );

    @Query("SELECT ews FROM EmployeeWorkShift ews JOIN FETCH ews.workShift ws JOIN FETCH ews.employee emp " +
           "WHERE ews.date = :date AND ews.isDeleted = false")
    List<EmployeeWorkShift> findActiveByDate(@Param("date") LocalDate date);

    @Query("SELECT ews FROM EmployeeWorkShift ews " +
           "JOIN FETCH ews.workShift ws JOIN FETCH ews.employee emp " +
           "WHERE ews.employee.store.id = :storeId AND ews.isDeleted = false")
    List<EmployeeWorkShift> findActiveByStoreId(@Param("storeId") UUID storeId);
}
