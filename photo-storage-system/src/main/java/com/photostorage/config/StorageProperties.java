package com.photostorage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件存储配置属性类
 * 从application.yml中读取storage前缀的配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /** 上传文件存储目录 */
    private String uploadDir = "./uploads";

    /** 缩略图存储目录 */
    private String thumbnailDir = "./uploads/thumbnails";

    /** 允许上传的文件MIME类型列表 */
    private List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp");

    /** 允许的文件扩展名列表 */
    private List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    /** 单个文件最大大小 (MB) */
    private long maxFileSize = 10;

    /** 缩略图宽度 (像素) */
    private int thumbnailWidth = 300;

    /** 缩略图高度 (像素) */
    private int thumbnailHeight = 300;

    /** 是否自动生成缩略图 */
    private boolean generateThumbnail = true;

    /** 文件清理间隔 (天) */
    private int cleanupIntervalDays = 30;

    /** 最大存储容量 (GB) */
    private long maxStorageSize = 10;

    /**
     * 获取最大文件大小（字节）
     * @return 最大文件大小（字节）
     */
    public long getMaxFileSizeInBytes() {
        return maxFileSize * 1024 * 1024;
    }

    /**
     * 获取最大存储容量（字节）
     * @return 最大存储容量（字节）
     */
    public long getMaxStorageSizeInBytes() {
        return maxStorageSize * 1024 * 1024 * 1024;
    }
}
