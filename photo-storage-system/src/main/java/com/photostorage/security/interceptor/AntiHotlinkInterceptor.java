package com.photostorage.security.interceptor;

import com.photostorage.config.SecurityProperties;
import com.photostorage.exception.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * 防盗链拦截器
 * 检查请求来源，防止外部网站直接引用资源
 */
@Slf4j
@RequiredArgsConstructor
public class AntiHotlinkInterceptor implements HandlerInterceptor {

    private final SecurityProperties securityProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行OPTIONS请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 获取Referer
        String referer = request.getHeader("Referer");
        String host = request.getHeader("Host");

        // 允许直接访问（无Referer）
        if (referer == null || referer.isEmpty()) {
            return true;
        }

        // 检查Referer是否在白名单中
        List<String> allowedReferers = securityProperties.getAllowedReferers();
        if (allowedReferers == null || allowedReferers.isEmpty()) {
            return true;
        }

        // 提取Referer的host
        String refererHost = extractHost(referer);

        // 检查是否在白名单中
        boolean allowed = allowedReferers.stream()
                .anyMatch(allowed -> {
                    // 精确匹配
                    if (refererHost.equalsIgnoreCase(allowed)) {
                        return true;
                    }
                    // 子域名匹配
                    if (refererHost.endsWith("." + allowed)) {
                        return true;
                    }
                    return false;
                });

        if (!allowed) {
            log.warn("防盗链拦截: referer={}, host={}, uri={}", referer, host, request.getRequestURI());
            throw SecurityException.hotlinkDenied();
        }

        return true;
    }

    /**
     * 从URL中提取host
     */
    private String extractHost(String url) {
        try {
            // 移除协议部分
            String withoutProtocol = url.replaceAll("^https?://", "");
            // 提取host（到第一个/或:为止）
            int slashIndex = withoutProtocol.indexOf('/');
            int colonIndex = withoutProtocol.indexOf(':');

            int endIndex;
            if (slashIndex == -1 && colonIndex == -1) {
                endIndex = withoutProtocol.length();
            } else if (slashIndex == -1) {
                endIndex = colonIndex;
            } else if (colonIndex == -1) {
                endIndex = slashIndex;
            } else {
                endIndex = Math.min(slashIndex, colonIndex);
            }

            return withoutProtocol.substring(0, endIndex).toLowerCase();
        } catch (Exception e) {
            log.warn("解析Referer失败: {}", url);
            return "";
        }
    }
}
