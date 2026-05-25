package com.photostorage.controller;

import com.photostorage.dto.ApiResponse;
import com.photostorage.dto.FileInfoResponse;
import com.photostorage.dto.PageResult;
import com.photostorage.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 照片访问控制器
 * 提供简洁的照片访问接口，主要用于前端展示
 */
@Slf4j
@RestController
@RequestMapping("/photos")
@RequiredArgsConstructor
@Validated
@Tag(name = "照片访问", description = "照片查看和访问接口")
public class PhotoController {

    private final StorageService storageService;

    /**
     * 获取照片（简化版预览）
     */
    @GetMapping("/{fileId}")
    @Operation(summary = "查看照片",
            description = "直接查看照片，适合在img标签中使用",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "404", description = "照片不存在")
            })
    public ResponseEntity<Resource> getPhoto(
            @Parameter(description = "照片ID", required = true)
            @PathVariable Long fileId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        return ResponseEntity.ok(storageService.previewFile(fileId, request, response));
    }

    /**
     * 获取缩略图
     */
    @GetMapping("/{fileId}/thumbnail")
    @Operation(summary = "查看缩略图",
            description = "获取照片的缩略图版本",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "404", description = "缩略图不存在")
            })
    public ResponseEntity<Resource> getThumbnail(
            @Parameter(description = "照片ID", required = true)
            @PathVariable Long fileId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        // 暂时返回原图，实际项目中可以返回缩略图
        return ResponseEntity.ok(storageService.previewFile(fileId, request, response));
    }

    /**
     * 照片列表（简化版）
     */
    @GetMapping("/gallery")
    @Operation(summary = "照片画廊",
            description = "获取照片列表，适合画廊展示",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = PageResult.class)))
            })
    public ResponseEntity<ApiResponse<PageResult<FileInfoResponse>>> getGallery(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer pageSize) {

        PageResult<FileInfoResponse> result = storageService.listFiles(pageNum, pageSize, null);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 搜索照片
     */
    @GetMapping("/search")
    @Operation(summary = "搜索照片",
            description = "根据关键词搜索照片",
            responses = {
                    @ApiResponse(responseCode = "200", description = "搜索成功",
                            content = @Content(schema = @Schema(implementation = PageResult.class)))
            })
    public ResponseEntity<ApiResponse<PageResult<FileInfoResponse>>> searchPhotos(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer pageSize) {

        PageResult<FileInfoResponse> result = storageService.listFiles(pageNum, pageSize, keyword);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
