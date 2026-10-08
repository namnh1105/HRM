package com.hainam.worksphere.leave.repository;

import com.hainam.worksphere.leave.domain.LeaveRequest;
import com.hainam.worksphere.leave.domain.LeaveRequestStatus;
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
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.isDeleted = false")
    Page<LeaveRequest> findAllActive(Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.id = :id AND lr.isDeleted = false")
    Optional<LeaveRequest> findActiveById(@Param("id") UUID id);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.isDeleted = false")
    Page<LeaveRequest> findActiveByEmployeeId(@Param("employeeId") UUID employeeId, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.status = :status AND lr.isDeleted = false")
    List<LeaveRequest> findActiveByEmployeeIdAndStatus(@Param("employeeId") UUID employeeId, @Param("status") LeaveRequestStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = :status AND lr.isDeleted = false")
    Page<LeaveRequest> findActiveByStatus(@Param("status") LeaveRequestStatus status, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.startDate BETWEEN :start AND :end AND lr.isDeleted = false")
    List<LeaveRequest> findActiveByEmployeeIdAndStartDateBetween(@Param("employeeId") UUID employeeId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    long countByIsDeletedFalse();
    long countByStatusAndIsDeletedFalse(LeaveRequestStatus status);
    long countByIsDeletedTrue();
}
