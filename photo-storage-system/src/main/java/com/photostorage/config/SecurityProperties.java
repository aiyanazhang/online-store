package com.photostorage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 安全配置属性类
 * 包含防盗链、XSS防护等安全相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /** 是否启用防盗链检查 */
    private boolean antiHotlinkEnabled = true;

    /** 允许的Referer域名列表 */
    private List<String> allowedReferers = List.of("localhost", "127.0.0.1");

    /** API访问密钥 */
    private String apiKey = "default-secret-key-change-in-production";

    /** 请求频率限制 (每分钟最大请求数) */
    private int rateLimit = 100;

    /** XSS防护白名单标签 */
    private List<String> xssWhiteListTags = List.of("b", "i", "em", "strong", "u");
}
