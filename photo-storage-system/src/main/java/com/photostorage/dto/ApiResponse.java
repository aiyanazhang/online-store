package com.photostorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一API响应封装类
 * @param <T> 响应数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** 响应状态码 */
    private Integer code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 时间戳 */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /** 请求路径 */
    private String path;

    /** 请求ID（用于追踪） */
    private String requestId;

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 成功响应（带消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 错误响应（带数据）
     */
    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 参数错误
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(400, message);
    }

    /**
     * 未授权
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(401, message);
    }

    /**
     * 禁止访问
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return error(403, message);
    }

    /**
     * 资源不存在
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return error(404, message);
    }

    /**
     * 服务器错误
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return error(500, message);
    }
}
