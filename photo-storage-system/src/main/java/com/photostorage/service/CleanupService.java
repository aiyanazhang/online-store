package com.photostorage.service;

import com.photostorage.config.StorageProperties;
import com.photostorage.entity.PhotoFile;
import com.photostorage.repository.PhotoFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 清理服务
 * 定期清理过期文件和孤立文件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private final StorageProperties storageProperties;
    private final PhotoFileRepository photoFileRepository;

    /**
     * 清理过期文件（每天凌晨2点执行）
     * 清理超过配置天数的未访问文件
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredFiles() {
        log.info("开始清理过期文件...");

        LocalDateTime cutoffDate = LocalDateTime.now()
                .minusDays(storageProperties.getCleanupIntervalDays());

        // 查找过期文件
        List<PhotoFile> expiredFiles = photoFileRepository.findByLastAccessTimeBefore(cutoffDate);
        List<PhotoFile> neverAccessedFiles = photoFileRepository.findNeverAccessedBefore(cutoffDate);

        expiredFiles.addAll(neverAccessedFiles);

        int deletedCount = 0;
        for (PhotoFile file : expiredFiles) {
            try {
                deletePhysicalFile(file);
                photoFileRepository.softDeleteById(file.getId());
                deletedCount++;
                log.debug("删除过期文件: {}", file.getFileName());
            } catch (Exception e) {
                log.error("删除过期文件失败: {}", file.getFileName(), e);
            }
        }

        log.info("过期文件清理完成，共删除 {} 个文件", deletedCount);
    }

    /**
     * 清理孤立文件（每周日凌晨3点执行）
     * 清理数据库中不存在但物理存在的文件
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void cleanupOrphanedFiles() {
        log.info("开始清理孤立文件...");

        Path uploadDir = Paths.get(storageProperties.getUploadDir());
        if (!Files.exists(uploadDir)) {
            return;
        }

        try {
            // 获取数据库中所有有效文件路径
            List<String> validPaths = photoFileRepository.findAll().stream()
                    .map(PhotoFile::getFilePath)
                    .toList();

            // 遍历上传目录，删除孤立文件
            int deletedCount = Files.walk(uploadDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> !validPaths.contains(path.toString()))
                    .mapToInt(path -> {
                        try {
                            Files.delete(path);
                            log.debug("删除孤立文件: {}", path);
                            return 1;
                        } catch (IOException e) {
                            log.error("删除孤立文件失败: {}", path, e);
                            return 0;
                        }
                    })
                    .sum();

            log.info("孤立文件清理完成，共删除 {} 个文件", deletedCount);

        } catch (IOException e) {
            log.error("清理孤立文件时发生错误", e);
        }
    }

    /**
     * 清理空目录（每月1号凌晨4点执行）
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void cleanupEmptyDirectories() {
        log.info("开始清理空目录...");

        Path uploadDir = Paths.get(storageProperties.getUploadDir());
        if (!Files.exists(uploadDir)) {
            return;
        }

        try {
            int deletedCount = Files.walk(uploadDir)
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(uploadDir))
                    .filter(path -> {
                        try {
                            return Files.list(path).findAny().isEmpty();
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .mapToInt(path -> {
                        try {
                            Files.delete(path);
                            log.debug("删除空目录: {}", path);
                            return 1;
                        } catch (IOException e) {
                            log.error("删除空目录失败: {}", path, e);
                            return 0;
                        }
                    })
                    .sum();

            log.info("空目录清理完成，共删除 {} 个目录", deletedCount);

        } catch (IOException e) {
            log.error("清理空目录时发生错误", e);
        }
    }

    /**
     * 删除物理文件
     */
    private void deletePhysicalFile(PhotoFile photoFile) throws IOException {
        // 删除主文件
        Path filePath = Paths.get(photoFile.getFilePath());
        Files.deleteIfExists(filePath);

        // 删除缩略图
        if (photoFile.getThumbnailPath() != null) {
            Path thumbnailPath = Paths.get(photoFile.getThumbnailPath());
            Files.deleteIfExists(thumbnailPath);
        }
    }

    /**
     * 手动触发清理（用于管理接口）
     * @return 清理的文件数量
     */
    @Transactional
    public int manualCleanup() {
        LocalDateTime cutoffDate = LocalDateTime.now()
                .minusDays(storageProperties.getCleanupIntervalDays());

        List<PhotoFile> expiredFiles = photoFileRepository.findByLastAccessTimeBefore(cutoffDate);
        List<PhotoFile> neverAccessedFiles = photoFileRepository.findNeverAccessedBefore(cutoffDate);

        expiredFiles.addAll(neverAccessedFiles);

        int deletedCount = 0;
        for (PhotoFile file : expiredFiles) {
            try {
                deletePhysicalFile(file);
                photoFileRepository.softDeleteById(file.getId());
                deletedCount++;
            } catch (Exception e) {
                log.error("手动清理文件失败: {}", file.getFileName(), e);
            }
        }

        return deletedCount;
    }
}
