package com.photostorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件上传响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadResponse {

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

    /** 文件MIME类型 */
    private String contentType;

    /** 图片宽度 */
    private Integer imageWidth;

    /** 图片高度 */
    private Integer imageHeight;

    /** 上传时间 */
    private LocalDateTime uploadTime;

    /** 文件描述 */
    private String description;

    /**
     * 批量上传响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchUploadResponse {

        /** 成功上传的文件列表 */
        private List<UploadResponse> successFiles;

        /** 上传失败的文件列表 */
        private List<UploadError> failedFiles;

        /** 总文件数 */
        private Integer totalCount;

        /** 成功数 */
        private Integer successCount;

        /** 失败数 */
        private Integer failedCount;
    }

    /**
     * 上传错误信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadError {

        /** 原始文件名 */
        private String originalName;

        /** 错误信息 */
        private String errorMessage;

        /** 错误码 */
        private Integer errorCode;
    }
}
