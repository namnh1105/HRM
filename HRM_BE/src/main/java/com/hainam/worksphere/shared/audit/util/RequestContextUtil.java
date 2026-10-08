package com.hainam.worksphere.shared.audit.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RequestContextUtil {

    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    /**
     * Generate and set request ID for current thread
     */
    public static String generateRequestId() {
        String requestId = "REQ-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
        REQUEST_ID.set(requestId);
        return requestId;
    }

    /**
     * Get current request ID for thread
     */
    public static String getRequestId() {
        String requestId = REQUEST_ID.get();
        if (requestId == null) {
            requestId = generateRequestId();
        }
        return requestId;
    }

    /**
     * Clear request ID for current thread
     */
    public static void clearRequestId() {
        REQUEST_ID.remove();
    }
}
