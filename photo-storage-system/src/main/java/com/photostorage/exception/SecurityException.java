package com.photostorage.exception;

/**
 * 安全相关异常
 */
public class SecurityException extends StorageException {

    public SecurityException(String message) {
        super(message, 403);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, 403, cause);
    }

    /**
     * 防盗链异常
     */
    public static SecurityException hotlinkDenied() {
        return new SecurityException("非法访问：防盗链保护");
    }

    /**
     * 未授权访问
     */
    public static SecurityException unauthorized() {
        return new SecurityException("未授权访问");
    }

    /**
     * API密钥无效
     */
    public static SecurityException invalidApiKey() {
        return new SecurityException("API密钥无效");
    }

    /**
     * 请求过于频繁
     */
    public static SecurityException rateLimitExceeded() {
        return new SecurityException("请求过于频繁，请稍后再试");
    }

    /**
     * 文件包含恶意内容
     */
    public static SecurityException maliciousContent() {
        return new SecurityException("文件包含恶意内容，上传被拒绝");
    }
}
