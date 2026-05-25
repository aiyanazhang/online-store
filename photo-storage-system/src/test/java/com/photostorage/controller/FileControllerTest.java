package com.photostorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photostorage.dto.*;
import com.photostorage.service.CleanupService;
import com.photostorage.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 文件控制器单元测试
 */
@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StorageService storageService;

    @MockBean
    private CleanupService cleanupService;

    @Test
    void getFileInfo_ShouldReturnFileInfo() throws Exception {
        // Given
        Long fileId = 1L;
        FileInfoResponse fileInfo = FileInfoResponse.builder()
                .id(fileId)
                .originalName("test.jpg")
                .fileName("uuid123.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .uploadTime(LocalDateTime.now())
                .build();

        when(storageService.getFileInfo(fileId)).thenReturn(fileInfo);

        // When & Then
        mockMvc.perform(get("/api/files/info/{fileId}", fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.originalName").value("test.jpg"))
                .andExpect(jsonPath("$.data.contentType").value("image/jpeg"));
    }

    @Test
    void listFiles_ShouldReturnPageResult() throws Exception {
        // Given
        PageResult<FileInfoResponse> pageResult = PageResult.<FileInfoResponse>builder()
                .pageNum(1)
                .pageSize(10)
                .total(1L)
                .totalPages(1)
                .list(List.of(FileInfoResponse.builder()
                        .id(1L)
                        .originalName("test.jpg")
                        .build()))
                .build();

        when(storageService.listFiles(anyInt(), anyInt(), any())).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/files/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void getStorageStats_ShouldReturnStats() throws Exception {
        // Given
        StorageStats stats = StorageStats.builder()
                .totalFiles(10L)
                .usedStorage(1024 * 1024L)
                .totalStorage(10L * 1024 * 1024 * 1024)
                .usagePercentage(0.01)
                .build();

        when(storageService.getStorageStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/files/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalFiles").value(10));
    }

    @Test
    void deleteFile_ShouldReturnSuccess() throws Exception {
        // Given
        Long fileId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/files/delete/{fileId}", fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    @Test
    void manualCleanup_ShouldReturnDeletedCount() throws Exception {
        // Given
        when(cleanupService.manualCleanup()).thenReturn(5);

        // When & Then
        mockMvc.perform(post("/api/files/admin/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    void getUploadProgress_ShouldReturnProgress() throws Exception {
        // Given
        String progressId = "test-progress-id";
        UploadProgress progress = UploadProgress.builder()
                .progressId(progressId)
                .fileName("test.jpg")
                .percentage(50)
                .status(UploadProgress.UploadStatus.UPLOADING)
                .build();

        when(storageService.getUploadProgress(progressId)).thenReturn(progress);

        // When & Then
        mockMvc.perform(get("/api/files/progress/{progressId}", progressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.percentage").value(50));
    }
}
