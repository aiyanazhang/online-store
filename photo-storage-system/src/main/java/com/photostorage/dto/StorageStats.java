package com.photostorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 存储统计信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageStats {

    /** 总文件数 */
    private Long totalFiles;

    /** 已用存储空间（字节） */
    private Long usedStorage;

    /** 格式化后的已用存储空间 */
    private String formattedUsedStorage;

    /** 总存储容量（字节） */
    private Long totalStorage;

    /** 格式化后的总存储容量 */
    private String formattedTotalStorage;

    /** 可用存储空间（字节） */
    private Long availableStorage;

    /** 格式化后的可用存储空间 */
    private String formattedAvailableStorage;

    /** 存储使用率（百分比） */
    private Double usagePercentage;

    /** 今日上传文件数 */
    private Long todayUploadCount;

    /** 今日上传大小（字节） */
    private Long todayUploadSize;

    /** 总下载次数 */
    private Long totalDownloads;

    /** 统计时间 */
    private LocalDateTime statsTime;

    /**
     * 计算存储使用率
     */
    public void calculateUsagePercentage() {
        if (totalStorage != null && totalStorage > 0 && usedStorage != null) {
            this.usagePercentage = Math.round((double) usedStorage / totalStorage * 10000) / 100.0;
        } else {
            this.usagePercentage = 0.0;
        }
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(Long size) {
        if (size == null || size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
