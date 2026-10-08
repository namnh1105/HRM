package com.hainam.worksphere.attendance.repository;

import com.hainam.worksphere.attendance.domain.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    @Query("SELECT a FROM Attendance a WHERE a.isDeleted = false")
    List<Attendance> findAllActive();

    @Query("SELECT a FROM Attendance a WHERE a.id = :id AND a.isDeleted = false")
    Optional<Attendance> findActiveById(@Param("id") UUID id);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.isDeleted = false")
    List<Attendance> findActiveByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate = :workDate AND a.isDeleted = false")
    Optional<Attendance> findActiveByEmployeeIdAndWorkDate(@Param("employeeId") UUID employeeId, @Param("workDate") LocalDate workDate);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate BETWEEN :startDate AND :endDate AND a.isDeleted = false")
    Page<Attendance> findActiveByEmployeeIdAndWorkDateBetween(@Param("employeeId") UUID employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate = :workDate AND a.isDeleted = false")
    boolean existsActiveByEmployeeIdAndWorkDate(@Param("employeeId") UUID employeeId, @Param("workDate") LocalDate workDate);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate = :workDate AND a.workShift.id = :workShiftId AND a.isDeleted = false")
    Optional<Attendance> findActiveByEmployeeIdAndWorkDateAndWorkShift(
            @Param("employeeId") UUID employeeId,
            @Param("workDate") LocalDate workDate,
            @Param("workShiftId") UUID workShiftId
    );

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate = :workDate " +
           "AND a.checkInTime IS NOT NULL AND a.checkOutTime IS NULL AND a.isDeleted = false")
    List<Attendance> findOpenAttendances(
            @Param("employeeId") UUID employeeId,
            @Param("workDate") LocalDate workDate
    );

    @Query("SELECT a FROM Attendance a WHERE a.store.id = :storeId AND a.isDeleted = false")
    Page<Attendance> findActiveByStoreId(@Param("storeId") UUID storeId, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.store.id = :storeId AND a.workDate BETWEEN :startDate AND :endDate AND a.isDeleted = false")
    Page<Attendance> findActiveByStoreIdAndWorkDateBetween(@Param("storeId") UUID storeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.store.id = :storeId AND a.workDate = :workDate AND a.isDeleted = false")
    Page<Attendance> findActiveByStoreIdAndWorkDate(@Param("storeId") UUID storeId, @Param("workDate") LocalDate workDate, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate BETWEEN :startDate AND :endDate " +
           "AND a.checkInTime IS NOT NULL AND a.isDeleted = false")
    long countPresentByEmployeeIdAndWorkDateBetween(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate BETWEEN :startDate AND :endDate " +
           "AND a.lateMinutes > 0 AND a.isDeleted = false")
    long countLateByEmployeeIdAndWorkDateBetween(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(a.lateMinutes), 0) FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate BETWEEN :startDate AND :endDate " +
           "AND a.isDeleted = false")
    long sumLateMinutesByEmployeeIdAndWorkDateBetween(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(a.workingHours), 0.0) FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate BETWEEN :startDate AND :endDate " +
           "AND a.isDeleted = false")
    double sumWorkingHoursByEmployeeIdAndWorkDateBetween(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    long countByIsDeletedFalse();
    long countByWorkDateAndIsDeletedFalse(LocalDate workDate);
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.workDate = :workDate AND a.status = 'LATE' AND a.isDeleted = false")
    long countLateByDate(@Param("workDate") LocalDate workDate);
    long countByIsDeletedTrue();
}
