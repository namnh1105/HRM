package com.hainam.worksphere.shared.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for extracting the client's real IP address from HttpServletRequest.
 * Handles common reverse proxy headers (X-Forwarded-For, X-Real-IP, etc.).
 */
public final class IpAddressUtil {

    private IpAddressUtil() {
        // utility class
    }

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_CLIENT_IP",
    };

    /**
     * Extract the client IP address from the request, checking proxy headers first.
     */
    public static String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For may contain multiple IPs: "client, proxy1, proxy2"
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
