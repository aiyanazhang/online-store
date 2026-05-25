package com.photostorage.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 存储配置初始化类
 * 应用启动时自动创建必要的目录结构
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StorageConfig {

    private final StorageProperties storageProperties;

    /**
     * 初始化存储目录
     * 创建上传目录和缩略图目录
     */
    @PostConstruct
    public void init() {
        try {
            // 创建上传目录
            Path uploadPath = Paths.get(storageProperties.getUploadDir());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("创建上传目录: {}", uploadPath.toAbsolutePath());
            }

            // 创建缩略图目录
            Path thumbnailPath = Paths.get(storageProperties.getThumbnailDir());
            if (!Files.exists(thumbnailPath)) {
                Files.createDirectories(thumbnailPath);
                log.info("创建缩略图目录: {}", thumbnailPath.toAbsolutePath());
            }

            log.info("存储配置初始化完成");
        } catch (IOException e) {
            log.error("创建存储目录失败", e);
            throw new RuntimeException("无法初始化存储目录", e);
        }
    }
}
