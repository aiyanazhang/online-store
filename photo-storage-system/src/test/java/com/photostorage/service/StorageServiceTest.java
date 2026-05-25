package com.photostorage.service;

import com.photostorage.config.StorageProperties;
import com.photostorage.dto.*;
import com.photostorage.entity.PhotoFile;
import com.photostorage.exception.FileNotFoundException;
import com.photostorage.exception.FileValidationException;
import com.photostorage.repository.PhotoFileRepository;
import com.photostorage.security.validator.FileSecurityValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 存储服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private StorageProperties storageProperties;

    @Mock
    private PhotoFileRepository photoFileRepository;

    @Mock
    private FileSecurityValidator fileSecurityValidator;

    @InjectMocks
    private StorageService storageService;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        when(storageProperties.getUploadDir()).thenReturn("./test-uploads");
        when(storageProperties.getThumbnailDir()).thenReturn("./test-uploads/thumbnails");
        when(storageProperties.getMaxStorageSizeInBytes()).thenReturn(10L * 1024 * 1024 * 1024);
        when(storageProperties.getMaxFileSizeInBytes()).thenReturn(10L * 1024 * 1024);
    }

    @Test
    void getFileInfo_Success() {
        // Given
        Long fileId = 1L;
        PhotoFile photoFile = PhotoFile.builder()
                .id(fileId)
                .originalName("test.jpg")
                .fileName("uuid123.jpg")
                .filePath("./uploads/test.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();

        when(photoFileRepository.findByIdAndIsDeletedFalse(fileId))
                .thenReturn(Optional.of(photoFile));

        // When
        FileInfoResponse result = storageService.getFileInfo(fileId);

        // Then
        assertNotNull(result);
        assertEquals(fileId, result.getId());
        assertEquals("test.jpg", result.getOriginalName());
        assertEquals("image/jpeg", result.getContentType());
    }

    @Test
    void getFileInfo_NotFound() {
        // Given
        Long fileId = 999L;
        when(photoFileRepository.findByIdAndIsDeletedFalse(fileId))
                .thenReturn(Optional.empty());

        // Then
        assertThrows(FileNotFoundException.class, () -> {
            storageService.getFileInfo(fileId);
        });
    }

    @Test
    void getStorageStats_Success() {
        // Given
        when(photoFileRepository.count()).thenReturn(10L);
        when(photoFileRepository.sumUsedStorage()).thenReturn(1024 * 1024L);
        when(photoFileRepository.countTodayUploads(any())).thenReturn(2L);
        when(photoFileRepository.sumTodayUploadSize(any())).thenReturn(512L);
        when(photoFileRepository.sumDownloadCount()).thenReturn(100L);

        // When
        StorageStats stats = storageService.getStorageStats();

        // Then
        assertNotNull(stats);
        assertEquals(10L, stats.getTotalFiles());
        assertEquals(1024 * 1024L, stats.getUsedStorage());
        assertEquals(2L, stats.getTodayUploadCount());
        assertEquals(100L, stats.getTotalDownloads());
    }

    @Test
    void deleteFile_Success() {
        // Given
        Long fileId = 1L;
        PhotoFile photoFile = PhotoFile.builder()
                .id(fileId)
                .originalName("test.jpg")
                .fileName("uuid123.jpg")
                .filePath("./test-uploads/test.jpg")
                .build();

        when(photoFileRepository.findByIdAndIsDeletedFalse(fileId))
                .thenReturn(Optional.of(photoFile));

        // When
        storageService.deleteFile(fileId);

        // Then
        verify(photoFileRepository).softDeleteById(fileId);
    }
}
