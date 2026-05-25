package com.photostorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 照片存储系统主入口
 * 功能特性：
 * - 支持单文件/多文件上传
 * - 支持断点续传下载
 * - 图片压缩与缩略图生成
 * - 文件类型安全检查
 * - 防盗链与XSS防护
 * - 存储容量监控与自动清理
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class PhotoStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhotoStorageApplication.class, args);
    }
}
