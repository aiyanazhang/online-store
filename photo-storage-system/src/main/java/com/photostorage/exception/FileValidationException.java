package com.photostorage.exception;

/**
 * 文件验证异常
 */
public class FileValidationException extends StorageException {

    private static final Integer ERROR_CODE = 400;

    public FileValidationException(String message) {
        super(message, ERROR_CODE);
    }

    public FileValidationException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }

    /**
     * 不支持的文件类型
     */
    public static FileValidationException unsupportedType(String contentType) {
        return new FileValidationException("不支持的文件类型: " + contentType);
    }

    /**
     * 文件过大
     */
    public static FileValidationException fileTooLarge(long maxSize) {
        return new FileValidationException("文件大小超过限制，最大允许: " + formatSize(maxSize));
    }

    /**
     * 空文件
     */
    public static FileValidationException emptyFile() {
        return new FileValidationException("上传的文件不能为空");
    }

    /**
     * 文件名不合法
     */
    public static FileValidationException invalidFileName(String reason) {
        return new FileValidationException("文件名不合法: " + reason);
    }

    /**
     * 存储空间不足
     */
    public static FileValidationException storageFull() {
        return new FileValidationException("存储空间不足");
    }

    private static String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
