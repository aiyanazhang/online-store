package com.photostorage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 照片文件实体类
 * 存储文件元数据信息
 */
@Entity
@Table(name = "photo_files", indexes = {
        @Index(name = "idx_filename", columnList = "fileName"),
        @Index(name = "idx_upload_time", columnList = "uploadTime"),
        @Index(name = "idx_file_hash", columnList = "fileHash")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 原始文件名 */
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    /** 存储文件名（唯一） */
    @Column(name = "file_name", nullable = false, unique = true, length = 255)
    private String fileName;

    /** 文件存储路径 */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /** 缩略图路径 */
    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;

    /** 文件大小（字节） */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /** 文件MIME类型 */
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    /** 文件扩展名 */
    @Column(name = "file_extension", length = 20)
    private String fileExtension;

    /** 文件MD5哈希值（用于去重） */
    @Column(name = "file_hash", length = 64)
    private String fileHash;

    /** 图片宽度 */
    @Column(name = "image_width")
    private Integer imageWidth;

    /** 图片高度 */
    @Column(name = "image_height")
    private Integer imageHeight;

    /** 文件描述 */
    @Column(name = "description", length = 500)
    private String description;

    /** 上传者IP */
    @Column(name = "upload_ip", length = 50)
    private String uploadIp;

    /** 下载次数 */
    @Column(name = "download_count")
    @Builder.Default
    private Long downloadCount = 0L;

    /** 是否压缩 */
    @Column(name = "is_compressed")
    @Builder.Default
    private Boolean isCompressed = false;

    /** 压缩后文件大小 */
    @Column(name = "compressed_size")
    private Long compressedSize;

    /** 上传时间 */
    @CreationTimestamp
    @Column(name = "upload_time", nullable = false, updatable = false)
    private LocalDateTime uploadTime;

    /** 更新时间 */
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /** 最后访问时间 */
    @Column(name = "last_access_time")
    private LocalDateTime lastAccessTime;

    /** 是否已删除（逻辑删除） */
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * 增加下载计数
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    /**
     * 更新最后访问时间
     */
    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }
}
