package com.hainam.worksphere.shared.audit.repository;

import com.hainam.worksphere.shared.audit.domain.AuditLog;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.domain.EntityType;
import com.hainam.worksphere.shared.audit.domain.AuditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Find audit logs by user ID
     */
    Page<AuditLog> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    /**
     * Find audit logs by entity type and ID
     */
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(EntityType entityType, String entityId, Pageable pageable);

    /**
     * Find audit logs by action type
     */
    Page<AuditLog> findByActionTypeOrderByTimestampDesc(ActionType actionType, Pageable pageable);

    /**
     * Find audit logs by action code
     */
    Page<AuditLog> findByActionCodeOrderByTimestampDesc(String actionCode, Pageable pageable);

    /**
     * Find audit logs within date range
     */
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(Instant startDate, Instant endDate, Pageable pageable);

    /**
     * Find audit logs by multiple criteria
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:actionType IS NULL OR a.actionType = :actionType) AND " +
           "(:actionCode IS NULL OR a.actionCode = :actionCode) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByCriteria(@Param("userId") String userId,
                                  @Param("actionType") ActionType actionType,
                                  @Param("actionCode") String actionCode,
                                  @Param("entityType") EntityType entityType,
                                  @Param("startDate") Instant startDate,
                                  @Param("endDate") Instant endDate,
                                  Pageable pageable);

    /**
     * Find audit logs by multiple criteria including field-level details
     */
    @Query("SELECT DISTINCT a FROM AuditLog a LEFT JOIN a.details d WHERE " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:actionType IS NULL OR a.actionType = :actionType) AND " +
           "(:actionCode IS NULL OR a.actionCode = :actionCode) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) AND " +
           "(:fieldName IS NULL OR d.fieldName = :fieldName) AND " +
           "(:oldValue IS NULL OR d.oldValue LIKE %:oldValue%) AND " +
           "(:newValue IS NULL OR d.newValue LIKE %:newValue%) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByAllCriteria(@Param("userId") String userId,
                                     @Param("actionType") ActionType actionType,
                                     @Param("actionCode") String actionCode,
                                     @Param("entityType") EntityType entityType,
                                     @Param("startDate") Instant startDate,
                                     @Param("endDate") Instant endDate,
                                     @Param("fieldName") String fieldName,
                                     @Param("oldValue") String oldValue,
                                     @Param("newValue") String newValue,
                                     Pageable pageable);

    /**
     * Find failed audit logs
     */
    Page<AuditLog> findByStatusOrderByTimestampDesc(AuditStatus status, Pageable pageable);

    /**
     * Count audit logs by user within date range
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.timestamp >= :startDate")
    Long countByUserIdAndTimestampAfter(@Param("userId") String userId, @Param("startDate") Instant startDate);

    /**
     * Get audit statistics by action type
     */
    @Query("SELECT a.actionType, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :startDate GROUP BY a.actionType")
    List<Object[]> getAuditStatisticsByActionType(@Param("startDate") Instant startDate);

    /**
     * Get audit statistics by action code
     */
    @Query("SELECT a.actionCode, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :startDate GROUP BY a.actionCode")
    List<Object[]> getAuditStatisticsByActionCode(@Param("startDate") Instant startDate);

    /**
     * Find old audit logs for cleanup
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp < :cutoffDate ORDER BY a.timestamp ASC")
    List<AuditLog> findTop1000ByTimestampBeforeOrderByTimestampAsc(@Param("cutoffDate") Instant cutoffDate, Pageable pageable);
}
