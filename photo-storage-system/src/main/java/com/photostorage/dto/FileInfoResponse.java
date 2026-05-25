package com.photostorage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件信息响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileInfoResponse {

    /** 文件ID */
    private Long id;

    /** 原始文件名 */
    private String originalName;

    /** 存储文件名 */
    private String fileName;

    /** 文件访问URL */
    private String fileUrl;

    /** 缩略图URL */
    private String thumbnailUrl;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 格式化后的文件大小 */
    private String formattedFileSize;

    /** 文件MIME类型 */
    private String contentType;

    /** 文件扩展名 */
    private String fileExtension;

    /** 图片宽度 */
    private Integer imageWidth;

    /** 图片高度 */
    private Integer imageHeight;

    /** 文件描述 */
    private String description;

    /** 下载次数 */
    private Long downloadCount;

    /** 是否压缩 */
    private Boolean isCompressed;

    /** 压缩后文件大小 */
    private Long compressedSize;

    /** 上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    /** 最后访问时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessTime;

    /**
     * 格式化文件大小
     * @param size 文件大小（字节）
     * @return 格式化后的字符串
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
