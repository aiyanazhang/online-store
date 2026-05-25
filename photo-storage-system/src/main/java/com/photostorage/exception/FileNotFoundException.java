package com.photostorage.exception;

/**
 * 文件不存在异常
 */
public class FileNotFoundException extends StorageException {

    private static final Integer ERROR_CODE = 404;

    public FileNotFoundException(String message) {
        super(message, ERROR_CODE);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }

    public FileNotFoundException(Long fileId) {
        super("文件不存在: ID=" + fileId, ERROR_CODE);
    }

    public FileNotFoundException(String fileName, boolean isFileName) {
        super("文件不存在: " + fileName, ERROR_CODE);
    }
}
