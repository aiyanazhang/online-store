package com.photostorage.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadRequest {

    /** 单文件上传 */
    @NotNull(message = "文件不能为空")
    private MultipartFile file;

    /** 多文件上传 */
    @Size(max = 10, message = "单次最多上传10个文件")
    private List<MultipartFile> files;

    /** 文件描述 */
    @Size(max = 500, message = "描述长度不能超过500字符")
    private String description;

    /** 是否生成缩略图 */
    private Boolean generateThumbnail = true;

    /** 是否压缩图片 */
    private Boolean compressImage = false;

    /** 压缩质量 (0.0 - 1.0) */
    private Float compressionQuality = 0.8f;

    /**
     * 多文件上传请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultiUploadRequest {

        @NotNull(message = "文件列表不能为空")
        @Size(min = 1, max = 10, message = "文件数量必须在1-10之间")
        private List<MultipartFile> files;

        @Size(max = 500, message = "描述长度不能超过500字符")
        private String description;

        private Boolean generateThumbnail = true;

        private Boolean compressImage = false;

        private Float compressionQuality = 0.8f;
    }
}
