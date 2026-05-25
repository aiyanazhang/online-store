package com.photostorage.exception;

import com.photostorage.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 全局异常处理器
 * 统一处理系统中抛出的各类异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理存储相关异常
     */
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiResponse<Void>> handleStorageException(
            StorageException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        log.error("[{}] 存储异常: {} - 路径: {}", requestId, ex.getMessage(), request.getRequestURI(), ex);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(ex.getErrorCode()).body(response);
    }

    /**
     * 处理文件未找到异常
     */
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileNotFoundException(
            FileNotFoundException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        log.warn("[{}] 文件未找到: {} - 路径: {}", requestId, ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(404)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理文件验证异常
     */
    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileValidationException(
            FileValidationException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        log.warn("[{}] 文件验证失败: {} - 路径: {}", requestId, ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(400)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理安全异常
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleSecurityException(
            SecurityException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        log.warn("[{}] 安全异常: {} - 路径: {} - IP: {}",
                requestId, ex.getMessage(), request.getRequestURI(), request.getRemoteAddr());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(403)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        log.warn("[{}] 文件大小超限: {} - 路径: {}", requestId, ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(400)
                .message("文件大小超过限制，单个文件最大支持10MB")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理文件上传异常
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipartException(
            MultipartException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        log.error("[{}] 文件上传异常: {} - 路径: {}", requestId, ex.getMessage(), request.getRequestURI(), ex);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(400)
                .message("文件上传失败: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数验证失败");

        log.warn("[{}] 参数验证失败: {} - 路径: {}", requestId, message, request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(400)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .findFirst()
                .orElse("参数验证失败");

        log.warn("[{}] 约束违反: {} - 路径: {}", requestId, message, request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(400)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        log.warn("[{}] 缺少请求参数: {} - 路径: {}", requestId, ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(400)
                .message("缺少必要参数: " + ex.getParameterName())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        log.error("[{}] 系统异常: {} - 路径: {}", requestId, ex.getMessage(), request.getRequestURI(), ex);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(500)
                .message("系统内部错误，请联系管理员")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
