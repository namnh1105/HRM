package com.hainam.worksphere.shared.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin controller for managing rate limits
 */
@RestController
@RequestMapping("/api/v1/admin/rate-limit")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class RateLimitController {

    private final RateLimitService rateLimitService;

    /**
     * Reset rate limit for a specific key (user or IP)
     */
    @DeleteMapping("/reset/{key}")
    public ResponseEntity<Map<String, String>> resetRateLimit(@PathVariable String key) {
        rateLimitService.resetRateLimit(key);
        log.info("Rate limit reset for key: {}", key);
        return ResponseEntity.ok(Map.of(
            "message", "Rate limit reset successfully",
            "key", key
        ));
    }

    /**
     * Unban a specific key
     */
    @DeleteMapping("/unban/{key}")
    public ResponseEntity<Map<String, String>> unbanKey(@PathVariable String key) {
        rateLimitService.unbanKey(key);
        log.info("Key unbanned: {}", key);
        return ResponseEntity.ok(Map.of(
            "message", "Key unbanned successfully",
            "key", key
        ));
    }

    /**
     * Get remaining ban time for a key
     */
    @GetMapping("/ban-status/{key}")
    public ResponseEntity<Map<String, Object>> getBanStatus(@PathVariable String key) {
        long remainingBanTime = rateLimitService.getRemainingBanTime(key);
        boolean isBanned = remainingBanTime > 0;

        return ResponseEntity.ok(Map.of(
            "key", key,
            "isBanned", isBanned,
            "remainingBanTimeSeconds", remainingBanTime
        ));
    }

    /**
     * Get available tokens for a key and rate limit type
     */
    @GetMapping("/tokens/{key}")
    public ResponseEntity<Map<String, Object>> getAvailableTokens(
            @PathVariable String key,
            @RequestParam(defaultValue = "AUTHENTICATED") RateLimitType type) {
        long availableTokens = rateLimitService.getAvailableTokens(key, type);

        return ResponseEntity.ok(Map.of(
            "key", key,
            "type", type.name(),
            "availableTokens", availableTokens
        ));
    }

    /**
     * Clear all rate limits (use with caution)
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, String>> clearAllRateLimits() {
        rateLimitService.clearAllRateLimits();
        log.warn("All rate limits cleared by admin");
        return ResponseEntity.ok(Map.of(
            "message", "All rate limits cleared successfully"
        ));
    }
}

