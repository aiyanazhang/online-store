package com.photostorage.service;

import com.photostorage.config.StorageProperties;
import com.photostorage.dto.*;
import com.photostorage.entity.PhotoFile;
import com.photostorage.exception.FileNotFoundException;
import com.photostorage.exception.FileValidationException;
import com.photostorage.exception.SecurityException;
import com.photostorage.exception.StorageException;
import com.photostorage.repository.PhotoFileRepository;
import com.photostorage.security.validator.FileSecurityValidator;
import com.photostorage.utils.FileUtils;
import com.photostorage.utils.XssUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 存储服务
 * 处理文件上传、下载、管理等核心业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageProperties storageProperties;
    private final PhotoFileRepository photoFileRepository;
    private final FileSecurityValidator fileSecurityValidator;

    // 上传进度跟踪
    private final ConcurrentHashMap<String, UploadProgress> uploadProgressMap = new ConcurrentHashMap<>();

    /**
     * 单文件上传
     */
    @Transactional
    @CacheEvict(value = "storageStatsCache", allEntries = true)
    public UploadResponse uploadFile(MultipartFile file, String description,
                                     Boolean generateThumbnail, Boolean compressImage,
                                     Float compressionQuality, HttpServletRequest request) {

        // 生成进度ID
        String progressId = UUID.randomUUID().toString();

        try {
            // 初始化进度
            initProgress(progressId, file.getOriginalFilename(), file.getSize());

            // 安全验证
            updateProgress(progressId, UploadProgress.UploadStatus.UPLOADING, 0);
            fileSecurityValidator.validate(file);

            // 检查存储空间
            checkStorageSpace(file.getSize());

            // 检查重复文件
            String fileHash = FileUtils.calculateMd5(file);
            Optional<PhotoFile> existingFile = photoFileRepository.findByFileHash(fileHash);
            if (existingFile.isPresent()) {
                log.info("检测到重复文件: {}", file.getOriginalFilename());
                updateProgress(progressId, UploadProgress.UploadStatus.COMPLETED, 100);
                return convertToUploadResponse(existingFile.get());
            }

            // 保存文件
            updateProgress(progressId, UploadProgress.UploadStatus.PROCESSING, 50);
            PhotoFile photoFile = saveFile(file, description, generateThumbnail,
                    compressImage, compressionQuality, request, fileHash);

            updateProgress(progressId, UploadProgress.UploadStatus.COMPLETED, 100);

            return convertToUploadResponse(photoFile);

        } catch (Exception e) {
            updateProgress(progressId, UploadProgress.UploadStatus.FAILED, 0, e.getMessage());
            throw e;
        }
    }

    /**
     * 多文件上传
     */
    @Transactional
    @CacheEvict(value = "storageStatsCache", allEntries = true)
    public UploadResponse.BatchUploadResponse uploadMultipleFiles(
            List<MultipartFile> files, String description,
            Boolean generateThumbnail, Boolean compressImage,
            Float compressionQuality, HttpServletRequest request) {

        List<UploadResponse> successFiles = new ArrayList<>();
        List<UploadResponse.UploadError> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                UploadResponse response = uploadFile(file, description, generateThumbnail,
                        compressImage, compressionQuality, request);
                successFiles.add(response);
            } catch (Exception e) {
                failedFiles.add(UploadResponse.UploadError.builder()
                        .originalName(file.getOriginalFilename())
                        .errorMessage(e.getMessage())
                        .errorCode(e instanceof StorageException ?
                                ((StorageException) e).getErrorCode() : 500)
                        .build());
                log.error("文件上传失败: {}", file.getOriginalFilename(), e);
            }
        }

        return UploadResponse.BatchUploadResponse.builder()
                .successFiles(successFiles)
                .failedFiles(failedFiles)
                .totalCount(files.size())
                .successCount(successFiles.size())
                .failedCount(failedFiles.size())
                .build();
    }

    /**
     * 保存文件到存储系统
     */
    private PhotoFile saveFile(MultipartFile file, String description,
                               Boolean generateThumbnail, Boolean compressImage,
                               Float compressionQuality, HttpServletRequest request,
                               String fileHash) throws IOException {

        // 生成唯一文件名
        String originalFilename = XssUtils.sanitizeFilename(file.getOriginalFilename());
        String fileName = FileUtils.generateUniqueFileName(originalFilename);
        String extension = FileUtils.getFileExtension(originalFilename);

        // 按日期组织目录
        String dateDir = LocalDate.now().toString();
        Path targetDir = Paths.get(storageProperties.getUploadDir(), dateDir);
        Files.createDirectories(targetDir);

        Path targetPath = targetDir.resolve(fileName);

        // 获取图片尺寸
        int[] dimensions = FileUtils.getImageDimensions(file);

        // 保存文件
        long fileSize = file.getSize();
        if (Boolean.TRUE.equals(compressImage) && compressionQuality != null) {
            // 压缩保存
            compressAndSaveImage(file, targetPath, compressionQuality);
            fileSize = Files.size(targetPath);
        } else {
            // 直接保存
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 生成缩略图
        String thumbnailPath = null;
        if (Boolean.TRUE.equals(generateThumbnail) && storageProperties.isGenerateThumbnail()) {
            thumbnailPath = generateThumbnail(targetPath, fileName);
        }

        // 保存到数据库
        PhotoFile photoFile = PhotoFile.builder()
                .originalName(originalFilename)
                .fileName(fileName)
                .filePath(targetPath.toString())
                .thumbnailPath(thumbnailPath)
                .fileSize(fileSize)
                .contentType(file.getContentType())
                .fileExtension(extension)
                .fileHash(fileHash)
                .imageWidth(dimensions != null ? dimensions[0] : null)
                .imageHeight(dimensions != null ? dimensions[1] : null)
                .description(XssUtils.strictEscape(description))
                .uploadIp(getClientIp(request))
                .isCompressed(Boolean.TRUE.equals(compressImage))
                .compressedSize(Boolean.TRUE.equals(compressImage) ? fileSize : null)
                .build();

        return photoFileRepository.save(photoFile);
    }

    /**
     * 压缩并保存图片
     */
    private void compressAndSaveImage(MultipartFile file, Path targetPath, float quality) throws IOException {
        try (InputStream is = file.getInputStream();
             OutputStream os = Files.newOutputStream(targetPath)) {
            Thumbnails.of(is)
                    .scale(1.0)
                    .outputQuality(quality)
                    .toOutputStream(os);
        }
    }

    /**
     * 生成缩略图
     */
    private String generateThumbnail(Path sourcePath, String fileName) {
        try {
            String thumbnailDir = storageProperties.getThumbnailDir();
            Files.createDirectories(Paths.get(thumbnailDir));

            String thumbnailName = FileUtils.generateThumbnailFileName(fileName);
            Path thumbnailPath = Paths.get(thumbnailDir, thumbnailName);

            Thumbnails.of(sourcePath.toFile())
                    .size(storageProperties.getThumbnailWidth(), storageProperties.getThumbnailHeight())
                    .keepAspectRatio(true)
                    .toFile(thumbnailPath.toFile());

            return thumbnailPath.toString();
        } catch (Exception e) {
            log.warn("生成缩略图失败: {}", fileName, e);
            return null;
        }
    }

    /**
     * 文件下载（支持断点续传）
     */
    @Transactional
    public void downloadFile(Long fileId, HttpServletRequest request,
                             HttpServletResponse response) throws IOException {

        PhotoFile photoFile = photoFileRepository.findByIdAndIsDeletedFalse(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        Path filePath = Paths.get(photoFile.getFilePath());
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException(photoFile.getFileName(), true);
        }

        // 更新下载计数和访问时间
        photoFileRepository.incrementDownloadCount(fileId);

        // 获取文件信息
        long fileSize = Files.size(filePath);
        String fileName = photoFile.getOriginalName();
        String contentType = photoFile.getContentType();

        // 处理断点续传
        handleRangeRequest(request, response, filePath, fileSize, fileName, contentType);
    }

    /**
     * 处理断点续传请求
     */
    private void handleRangeRequest(HttpServletRequest request, HttpServletResponse response,
                                    Path filePath, long fileSize, String fileName,
                                    String contentType) throws IOException {

        // 设置响应头
        response.setContentType(contentType != null ? contentType : "application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1") + "\"");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Cache-Control", "private, max-age=86400");

        // 解析Range头
        String rangeHeader = request.getHeader("Range");
        long start = 0;
        long end = fileSize - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            try {
                start = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                }
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            } catch (NumberFormatException e) {
                log.warn("无效的Range头: {}", rangeHeader);
            }
        }

        long contentLength = end - start + 1;
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);

        // 写入响应
        try (InputStream is = Files.newInputStream(filePath);
             OutputStream os = response.getOutputStream()) {

            is.skip(start);
            byte[] buffer = new byte[8192];
            long remaining = contentLength;
            int read;

            while (remaining > 0 && (read = is.read(buffer, 0,
                    (int) Math.min(buffer.length, remaining))) != -1) {
                os.write(buffer, 0, read);
                remaining -= read;
            }

            os.flush();
        }
    }

    /**
     * 在线预览文件
     */
    @Transactional(readOnly = true)
    public Resource previewFile(Long fileId, HttpServletRequest request,
                                HttpServletResponse response) throws IOException {

        PhotoFile photoFile = photoFileRepository.findByIdAndIsDeletedFalse(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        Path filePath = Paths.get(photoFile.getFilePath());
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException(photoFile.getFileName(), true);
        }

        // 更新访问时间
        photoFileRepository.updateLastAccessTime(fileId);

        // 设置预览响应头
        response.setContentType(photoFile.getContentType());
        response.setHeader("Content-Disposition", "inline; filename=\"" + photoFile.getOriginalName() + "\"");
        response.setHeader("Cache-Control", "public, max-age=86400");

        return new InputStreamResource(Files.newInputStream(filePath));
    }

    /**
     * 获取文件信息
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "fileMetadataCache", key = "#fileId")
    public FileInfoResponse getFileInfo(Long fileId) {
        PhotoFile photoFile = photoFileRepository.findByIdAndIsDeletedFalse(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        return convertToFileInfoResponse(photoFile);
    }

    /**
     * 分页查询文件列表
     */
    @Transactional(readOnly = true)
    public PageResult<FileInfoResponse> listFiles(int pageNum, int pageSize, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "uploadTime"));

        Page<PhotoFile> page;
        if (StringUtils.hasText(keyword)) {
            page = photoFileRepository.findByOriginalNameContaining(keyword, pageable);
        } else {
            page = photoFileRepository.findAllByIsDeletedFalse(pageable);
        }

        List<FileInfoResponse> list = page.getContent().stream()
                .map(this::convertToFileInfoResponse)
                .collect(Collectors.toList());

        return PageResult.of(pageNum, pageSize, page.getTotalElements(), list);
    }

    /**
     * 删除文件
     */
    @Transactional
    @CacheEvict(value = {"fileMetadataCache", "storageStatsCache"}, allEntries = true)
    public void deleteFile(Long fileId) {
        PhotoFile photoFile = photoFileRepository.findByIdAndIsDeletedFalse(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        // 软删除
        photoFileRepository.softDeleteById(fileId);

        // 异步删除物理文件
        try {
            Path filePath = Paths.get(photoFile.getFilePath());
            Files.deleteIfExists(filePath);

            if (photoFile.getThumbnailPath() != null) {
                Path thumbnailPath = Paths.get(photoFile.getThumbnailPath());
                Files.deleteIfExists(thumbnailPath);
            }
        } catch (IOException e) {
            log.error("删除物理文件失败: {}", photoFile.getFilePath(), e);
        }
    }

    /**
     * 获取存储统计信息
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "storageStatsCache")
    public StorageStats getStorageStats() {
        LocalDateTime today = LocalDate.now().atStartOfDay();

        Long totalFiles = (long) photoFileRepository.count();
        Long usedStorage = photoFileRepository.sumUsedStorage();
        Long todayUploadCount = photoFileRepository.countTodayUploads(today);
        Long todayUploadSize = photoFileRepository.sumTodayUploadSize(today);
        Long totalDownloads = photoFileRepository.sumDownloadCount();

        StorageStats stats = StorageStats.builder()
                .totalFiles(totalFiles)
                .usedStorage(usedStorage)
                .formattedUsedStorage(StorageStats.formatFileSize(usedStorage))
                .totalStorage(storageProperties.getMaxStorageSizeInBytes())
                .formattedTotalStorage(StorageStats.formatFileSize(storageProperties.getMaxStorageSizeInBytes()))
                .todayUploadCount(todayUploadCount)
                .todayUploadSize(todayUploadSize)
                .totalDownloads(totalDownloads)
                .statsTime(LocalDateTime.now())
                .build();

        stats.calculateUsagePercentage();
        stats.setAvailableStorage(stats.getTotalStorage() - stats.getUsedStorage());
        stats.setFormattedAvailableStorage(StorageStats.formatFileSize(stats.getAvailableStorage()));

        return stats;
    }

    /**
     * 获取上传进度
     */
    public UploadProgress getUploadProgress(String progressId) {
        return uploadProgressMap.get(progressId);
    }

    /**
     * 初始化上传进度
     */
    private void initProgress(String progressId, String fileName, long totalBytes) {
        UploadProgress progress = UploadProgress.builder()
                .progressId(progressId)
                .fileName(fileName)
                .totalBytes(totalBytes)
                .uploadedBytes(0L)
                .percentage(0)
                .status(UploadProgress.UploadStatus.PENDING)
                .startTime(LocalDateTime.now())
                .lastUpdateTime(LocalDateTime.now())
                .build();
        uploadProgressMap.put(progressId, progress);
    }

    /**
     * 更新上传进度
     */
    private void updateProgress(String progressId, UploadProgress.UploadStatus status, int percentage) {
        updateProgress(progressId, status, percentage, null);
    }

    /**
     * 更新上传进度
     */
    private void updateProgress(String progressId, UploadProgress.UploadStatus status,
                                int percentage, String errorMessage) {
        UploadProgress progress = uploadProgressMap.get(progressId);
        if (progress != null) {
            progress.setStatus(status);
            progress.setPercentage(percentage);
            progress.setLastUpdateTime(LocalDateTime.now());
            if (errorMessage != null) {
                progress.setErrorMessage(errorMessage);
            }
        }
    }

    /**
     * 检查存储空间
     */
    private void checkStorageSpace(long fileSize) {
        Long usedStorage = photoFileRepository.sumUsedStorage();
        long maxStorage = storageProperties.getMaxStorageSizeInBytes();

        if (usedStorage + fileSize > maxStorage) {
            throw FileValidationException.storageFull();
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 转换为上传响应
     */
    private UploadResponse convertToUploadResponse(PhotoFile photoFile) {
        return UploadResponse.builder()
                .id(photoFile.getId())
                .originalName(photoFile.getOriginalName())
                .fileName(photoFile.getFileName())
                .fileUrl("/files/download/" + photoFile.getId())
                .thumbnailUrl(photoFile.getThumbnailPath() != null ?
                        "/files/thumbnail/" + photoFile.getId() : null)
                .fileSize(photoFile.getFileSize())
                .contentType(photoFile.getContentType())
                .imageWidth(photoFile.getImageWidth())
                .imageHeight(photoFile.getImageHeight())
                .uploadTime(photoFile.getUploadTime())
                .description(photoFile.getDescription())
                .build();
    }

    /**
     * 转换为文件信息响应
     */
    private FileInfoResponse convertToFileInfoResponse(PhotoFile photoFile) {
        return FileInfoResponse.builder()
                .id(photoFile.getId())
                .originalName(photoFile.getOriginalName())
                .fileName(photoFile.getFileName())
                .fileUrl("/files/download/" + photoFile.getId())
                .thumbnailUrl(photoFile.getThumbnailPath() != null ?
                        "/files/thumbnail/" + photoFile.getId() : null)
                .fileSize(photoFile.getFileSize())
                .formattedFileSize(FileInfoResponse.formatFileSize(photoFile.getFileSize()))
                .contentType(photoFile.getContentType())
                .fileExtension(photoFile.getFileExtension())
                .imageWidth(photoFile.getImageWidth())
                .imageHeight(photoFile.getImageHeight())
                .description(photoFile.getDescription())
                .downloadCount(photoFile.getDownloadCount())
                .isCompressed(photoFile.getIsCompressed())
                .compressedSize(photoFile.getCompressedSize())
                .uploadTime(photoFile.getUploadTime())
                .lastAccessTime(photoFile.getLastAccessTime())
                .build();
    }
}
