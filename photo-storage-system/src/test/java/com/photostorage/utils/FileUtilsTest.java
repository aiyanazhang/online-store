package com.photostorage.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件工具类单元测试
 */
class FileUtilsTest {

    @Test
    void generateUniqueFileName_ShouldGenerateUniqueName() {
        // Given
        String originalFilename = "test.jpg";

        // When
        String result1 = FileUtils.generateUniqueFileName(originalFilename);
        String result2 = FileUtils.generateUniqueFileName(originalFilename);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1, result2);
        assertTrue(result1.endsWith(".jpg"));
        assertTrue(result2.endsWith(".jpg"));
    }

    @Test
    void getFileExtension_ShouldReturnExtension() {
        assertEquals("jpg", FileUtils.getFileExtension("test.jpg"));
        assertEquals("jpeg", FileUtils.getFileExtension("test.jpeg"));
        assertEquals("png", FileUtils.getFileExtension("test.png"));
        assertEquals("", FileUtils.getFileExtension("test"));
        assertEquals("", FileUtils.getFileExtension(null));
    }

    @Test
    void getFileNameWithoutExtension_ShouldReturnName() {
        assertEquals("test", FileUtils.getFileNameWithoutExtension("test.jpg"));
        assertEquals("my.file", FileUtils.getFileNameWithoutExtension("my.file.png"));
        assertEquals("test", FileUtils.getFileNameWithoutExtension("test"));
    }

    @Test
    void sanitizeFileName_ShouldRemoveIllegalChars() {
        assertEquals("test_file.jpg", FileUtils.sanitizeFileName("test/file.jpg"));
        assertEquals("test_file.jpg", FileUtils.sanitizeFileName("test\\\\file.jpg"));
        assertEquals("test_file.jpg", FileUtils.sanitizeFileName("test:file.jpg"));
        assertEquals("test_file.jpg", FileUtils.sanitizeFileName("test*file.jpg"));
    }

    @Test
    void formatFileSize_ShouldFormatCorrectly() {
        assertEquals("100 B", FileUtils.formatFileSize(100));
        assertEquals("1.00 KB", FileUtils.formatFileSize(1024));
        assertEquals("1.00 MB", FileUtils.formatFileSize(1024 * 1024));
        assertEquals("1.00 GB", FileUtils.formatFileSize(1024L * 1024 * 1024));
    }

    @Test
    void validateFileName_ShouldThrowOnPathTraversal() {
        assertThrows(Exception.class, () -> {
            FileUtils.validateFileName("../etc/passwd");
        });

        assertThrows(Exception.class, () -> {
            FileUtils.validateFileName("test\\file.txt");
        });

        assertThrows(Exception.class, () -> {
            FileUtils.validateFileName("test\0file.txt");
        });
    }

    @Test
    void generateThumbnailFileName_ShouldAddThumbSuffix() {
        assertEquals("test_thumb.jpg", FileUtils.generateThumbnailFileName("test.jpg"));
        assertEquals("my.file_thumb.jpg", FileUtils.generateThumbnailFileName("my.file.png"));
    }
}
