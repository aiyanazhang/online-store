package com.photostorage.controller;

import com.photostorage.dto.*;
import com.photostorage.service.CleanupService;
import com.photostorage.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件管理控制器
 * 提供文件上传、下载、预览、管理等RESTful API
 */
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Validated
@Tag(name = "文件管理", description = "照片上传、下载、预览和管理接口")
public class FileController {

    private final StorageService storageService;
    private final CleanupService cleanupService;

    /**
     * 单文件上传
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "单文件上传",
            description = "上传单个图片文件，支持JPG、PNG、GIF、WEBP、BMP格式",
            responses = {
                    @ApiResponse(responseCode = "200", description = "上传成功",
                            content = @Content(schema = @Schema(implementation = UploadResponse.class))),
                    @ApiResponse(responseCode = "400", description = "文件验证失败"),
                    @ApiResponse(responseCode = "413", description = "文件过大")
            })
    public ResponseEntity<ApiResponse<UploadResponse>> uploadFile(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件描述")
            @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "是否生成缩略图")
            @RequestParam(value = "generateThumbnail", defaultValue = "true") Boolean generateThumbnail,
            @Parameter(description = "是否压缩图片")
            @RequestParam(value = "compressImage", defaultValue = "false") Boolean compressImage,
            @Parameter(description = "压缩质量(0.0-1.0)")
            @RequestParam(value = "compressionQuality", defaultValue = "0.8") Float compressionQuality,
            HttpServletRequest request) {

        log.info("接收到单文件上传请求: {}", file.getOriginalFilename());

        UploadResponse response = storageService.uploadFile(
                file, description, generateThumbnail, compressImage, compressionQuality, request);

        return ResponseEntity.ok(ApiResponse.success("上传成功", response));
    }

    /**
     * 多文件上传
     */
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "多文件上传",
            description = "批量上传多个图片文件，单次最多10个文件",
            responses = {
                    @ApiResponse(responseCode = "200", description = "上传完成",
                            content = @Content(schema = @Schema(implementation = UploadResponse.BatchUploadResponse.class)))
            })
    public ResponseEntity<ApiResponse<UploadResponse.BatchUploadResponse>> uploadMultipleFiles(
            @Parameter(description = "上传的文件列表", required = true)
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "文件描述")
            @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "是否生成缩略图")
            @RequestParam(value = "generateThumbnail", defaultValue = "true") Boolean generateThumbnail,
            @Parameter(description = "是否压缩图片")
            @RequestParam(value = "compressImage", defaultValue = "false") Boolean compressImage,
            @Parameter(description = "压缩质量(0.0-1.0)")
            @RequestParam(value = "compressionQuality", defaultValue = "0.8") Float compressionQuality,
            HttpServletRequest request) {

        log.info("接收到批量上传请求，文件数量: {}", files.size());

        UploadResponse.BatchUploadResponse response = storageService.uploadMultipleFiles(
                files, description, generateThumbnail, compressImage, compressionQuality, request);

        String message = String.format("上传完成，成功 %d 个，失败 %d 个",
                response.getSuccessCount(), response.getFailedCount());

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * 文件下载（支持断点续传）
     */
    @GetMapping("/download/{fileId}")
    @Operation(summary = "文件下载",
            description = "下载指定文件，支持断点续传",
            responses = {
                    @ApiResponse(responseCode = "200", description = "下载成功"),
                    @ApiResponse(responseCode = "206", description = "部分内容（断点续传）"),
                    @ApiResponse(responseCode = "404", description = "文件不存在")
            })
    public void downloadFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long fileId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        log.debug("文件下载请求: fileId={}", fileId);
        storageService.downloadFile(fileId, request, response);
    }

    /**
     * 文件预览
     */
    @GetMapping("/preview/{fileId}")
    @Operation(summary = "文件预览",
            description = "在线预览图片文件",
            responses = {
                    @ApiResponse(responseCode = "200", description = "预览成功"),
                    @ApiResponse(responseCode = "404", description = "文件不存在")
            })
    public ResponseEntity<Resource> previewFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long fileId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        log.debug("文件预览请求: fileId={}", fileId);
        Resource resource = storageService.previewFile(fileId, request, response);
        return ResponseEntity.ok(resource);
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{fileId}")
    @Operation(summary = "获取文件信息",
            description = "获取指定文件的详细信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = FileInfoResponse.class)))
            })
    public ResponseEntity<ApiResponse<FileInfoResponse>> getFileInfo(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long fileId) {

        FileInfoResponse fileInfo = storageService.getFileInfo(fileId);
        return ResponseEntity.ok(ApiResponse.success(fileInfo));
    }

    /**
     * 分页查询文件列表
     */
    @GetMapping("/list")
    @Operation(summary = "文件列表",
            description = "分页查询文件列表",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = PageResult.class)))
            })
    public ResponseEntity<ApiResponse<PageResult<FileInfoResponse>>> listFiles(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize,
            @Parameter(description = "搜索关键词")
            @RequestParam(required = false) String keyword) {

        PageResult<FileInfoResponse> result = storageService.listFiles(pageNum, pageSize, keyword);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete/{fileId}")
    @Operation(summary = "删除文件",
            description = "删除指定文件（逻辑删除）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功"),
                    @ApiResponse(responseCode = "404", description = "文件不存在")
            })
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long fileId) {

        storageService.deleteFile(fileId);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    /**
     * 获取存储统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "存储统计",
            description = "获取存储空间使用统计信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = StorageStats.class)))
            })
    public ResponseEntity<ApiResponse<StorageStats>> getStorageStats() {
        StorageStats stats = storageService.getStorageStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 获取上传进度
     */
    @GetMapping("/progress/{progressId}")
    @Operation(summary = "上传进度",
            description = "获取文件上传进度",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = UploadProgress.class)))
            })
    public ResponseEntity<ApiResponse<UploadProgress>> getUploadProgress(
            @Parameter(description = "进度ID", required = true)
            @PathVariable String progressId) {

        UploadProgress progress = storageService.getUploadProgress(progressId);
        if (progress == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "进度信息不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    /**
     * 手动触发清理
     */
    @PostMapping("/admin/cleanup")
    @Operation(summary = "手动清理",
            description = "手动触发过期文件清理（管理接口）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "清理完成")
            })
    public ResponseEntity<ApiResponse<Integer>> manualCleanup() {
        int deletedCount = cleanupService.manualCleanup();
        String message = String.format("清理完成，共删除 %d 个文件", deletedCount);
        return ResponseEntity.ok(ApiResponse.success(message, deletedCount));
    }
}
