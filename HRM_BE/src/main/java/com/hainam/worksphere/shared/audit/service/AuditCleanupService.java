package com.hainam.worksphere.shared.audit.service;

import com.hainam.worksphere.shared.audit.config.AuditProperties;
import com.hainam.worksphere.shared.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.audit.enabled", havingValue = "true", matchIfMissing = true)
public class AuditCleanupService {

    private final AuditLogRepository auditLogRepository;
    private final AuditProperties auditProperties;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldAuditLogs() {
        if (!auditProperties.isEnabled()) {
            return;
        }

        try {
            Instant cutoffDate = Instant.now().minus(java.time.Duration.ofDays(auditProperties.getRetentionDays()));

            long totalCount = auditLogRepository.count();

            int batchSize = 1000;
            long deletedCount = 0;

            while (true) {
                var oldLogs = auditLogRepository.findTop1000ByTimestampBeforeOrderByTimestampAsc(
                    cutoffDate, PageRequest.of(0, batchSize));

                if (oldLogs.isEmpty()) {
                    break;
                }

                auditLogRepository.deleteAll(oldLogs);
                deletedCount += oldLogs.size();

                log.debug("Deleted {} audit log records, total deleted: {}", oldLogs.size(), deletedCount);

                if (oldLogs.size() < batchSize) {
                    break;
                }
            }

            long remainingCount = auditLogRepository.count();

            log.info("Audit log cleanup completed. Deleted: {}, Remaining: {}, Total before: {}",
                    deletedCount, remainingCount, totalCount);

        } catch (Exception e) {
            log.error("Error during audit log cleanup", e);
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void logAuditStatistics() {
        if (!auditProperties.isEnabled()) {
            return;
        }

        try {
            long totalCount = auditLogRepository.count();
            Instant last24Hours = Instant.now().minus(java.time.Duration.ofDays(1));
            Long recentCount = auditLogRepository.countByUserIdAndTimestampAfter(null, last24Hours);

            if (recentCount == null) {
                recentCount = 0L;
            }
        } catch (Exception e) {
            log.error("Error getting audit log statistics", e);
        }
    }
}
