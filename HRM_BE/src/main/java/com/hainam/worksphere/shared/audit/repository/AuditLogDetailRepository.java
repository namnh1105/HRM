package com.hainam.worksphere.shared.audit.repository;

import com.hainam.worksphere.shared.audit.domain.AuditLogDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogDetailRepository extends JpaRepository<AuditLogDetail, Long> {

    /**
     * Find audit log details by audit log ID
     */
    List<AuditLogDetail> findByAuditLogIdOrderByIdAsc(UUID auditLogId);

    /**
     * Find audit log details by multiple audit log IDs
     */
    List<AuditLogDetail> findByAuditLogIdInOrderByAuditLogIdAscIdAsc(List<UUID> auditLogIds);

    /**
     * Find audit log details by field name
     */
    Page<AuditLogDetail> findByFieldNameOrderByIdDesc(String fieldName, Pageable pageable);

    /**
     * Find audit log details by field name and audit log ID
     */
    List<AuditLogDetail> findByAuditLogIdAndFieldName(UUID auditLogId, String fieldName);

    /**
     * Find audit log details with specific value changes
     */
    @Query("SELECT d FROM AuditLogDetail d WHERE " +
           "(:fieldName IS NULL OR d.fieldName = :fieldName) AND " +
           "(:oldValue IS NULL OR d.oldValue LIKE %:oldValue%) AND " +
           "(:newValue IS NULL OR d.newValue LIKE %:newValue%) " +
           "ORDER BY d.id DESC")
    Page<AuditLogDetail> findByValueCriteria(@Param("fieldName") String fieldName,
                                            @Param("oldValue") String oldValue,
                                            @Param("newValue") String newValue,
                                            Pageable pageable);

    /**
     * Count details by audit log ID
     */
    Long countByAuditLogId(UUID auditLogId);

    /**
     * Delete all details by audit log ID (for cleanup)
     */
    void deleteByAuditLogId(UUID auditLogId);

    /**
     * Delete details by audit log IDs (batch cleanup)
     */
    void deleteByAuditLogIdIn(List<UUID> auditLogIds);
}
