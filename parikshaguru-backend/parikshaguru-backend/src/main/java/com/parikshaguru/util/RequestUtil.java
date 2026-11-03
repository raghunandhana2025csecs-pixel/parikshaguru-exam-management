package com.parikshaguru.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class RequestUtil {
    private RequestUtil() {}
    public static String clientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a) {
            HttpServletRequest r = a.getRequest();
            String xf = r.getHeader("X-Forwarded-For");
            return xf != null && !xf.isBlank() ? xf.split(",")[0].trim() : r.getRemoteAddr();
        }
        return null;
    }
}
