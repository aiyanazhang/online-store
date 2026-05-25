package com.photostorage.security.interceptor;

import com.photostorage.config.SecurityProperties;
import com.photostorage.exception.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求限流拦截器
 * 基于IP地址进行请求频率限制
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final SecurityProperties securityProperties;

    // IP请求记录: IP -> [请求次数, 窗口开始时间]
    private final Map<String, RateLimitEntry> requestCounts = new ConcurrentHashMap<>();

    // 时间窗口（毫秒）
    private static final long TIME_WINDOW = 60 * 1000; // 1分钟

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取客户端IP
        String clientIp = getClientIp(request);

        // 检查是否超过限制
        if (isRateLimited(clientIp)) {
            log.warn("请求频率超限: IP={}, URI={}", clientIp, request.getRequestURI());
            throw SecurityException.rateLimitExceeded();
        }

        return true;
    }

    /**
     * 检查IP是否被限流
     */
    private boolean isRateLimited(String clientIp) {
        long now = Instant.now().toEpochMilli();
        int maxRequests = securityProperties.getRateLimit();

        RateLimitEntry entry = requestCounts.compute(clientIp, (ip, existing) -> {
            if (existing == null || now - existing.windowStart > TIME_WINDOW) {
                // 新窗口
                return new RateLimitEntry(1, now);
            } else {
                // 当前窗口，增加计数
                existing.count++;
                return existing;
            }
        });

        return entry.count > maxRequests;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 限流记录条目
     */
    private static class RateLimitEntry {
        int count;
        long windowStart;

        RateLimitEntry(int count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}
