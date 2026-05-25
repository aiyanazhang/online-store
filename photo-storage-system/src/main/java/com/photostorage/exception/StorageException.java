package com.photostorage.exception;

/**
 * 存储相关异常基类
 */
public class StorageException extends RuntimeException {

    private final Integer errorCode;

    public StorageException(String message) {
        super(message);
        this.errorCode = 500;
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 500;
    }

    public StorageException(String message, Integer errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public StorageException(String message, Integer errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
