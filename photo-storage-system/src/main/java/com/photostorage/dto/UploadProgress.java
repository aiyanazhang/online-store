package com.photostorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 上传进度信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadProgress {

    /** 进度ID */
    private String progressId;

    /** 文件名 */
    private String fileName;

    /** 文件总大小（字节） */
    private Long totalBytes;

    /** 已上传字节数 */
    private Long uploadedBytes;

    /** 上传进度百分比 */
    private Integer percentage;

    /** 上传速度（字节/秒） */
    private Long speed;

    /** 预计剩余时间（秒） */
    private Long estimatedTimeRemaining;

    /** 上传状态 */
    private UploadStatus status;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 最后更新时间 */
    private LocalDateTime lastUpdateTime;

    /** 错误信息 */
    private String errorMessage;

    /**
     * 上传状态枚举
     */
    public enum UploadStatus {
        PENDING,        // 等待中
        UPLOADING,      // 上传中
        PROCESSING,     // 处理中
        COMPLETED,      // 完成
        FAILED,         // 失败
        CANCELLED       // 已取消
    }

    /**
     * 计算上传进度百分比
     */
    public void calculatePercentage() {
        if (totalBytes != null && totalBytes > 0 && uploadedBytes != null) {
            this.percentage = (int) Math.round((double) uploadedBytes / totalBytes * 100);
        } else {
            this.percentage = 0;
        }
    }

    /**
     * 计算上传速度
     * @param bytesUploaded 本次上传字节数
     * @param elapsedTimeMs 经过时间（毫秒）
     */
    public void calculateSpeed(long bytesUploaded, long elapsedTimeMs) {
        if (elapsedTimeMs > 0) {
            this.speed = (bytesUploaded * 1000) / elapsedTimeMs;
            // 计算预计剩余时间
            if (this.speed > 0 && this.totalBytes != null) {
                long remainingBytes = this.totalBytes - this.uploadedBytes;
                this.estimatedTimeRemaining = remainingBytes / this.speed;
            }
        }
    }
}
