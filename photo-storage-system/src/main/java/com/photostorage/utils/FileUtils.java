package com.photostorage.utils;

import com.photostorage.config.StorageProperties;
import com.photostorage.exception.FileValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * 文件工具类
 * 提供文件操作相关的通用方法
 */
@Slf4j
public class FileUtils {

    private static final Tika TIKA = new Tika();

    /**
     * 生成唯一文件名
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    public static String generateUniqueFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + (extension.isEmpty() ? "" : "." + extension);
    }

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名（小写，不含点）
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 获取文件名（不含扩展名）
     * @param filename 文件名
     * @return 文件名（不含扩展名）
     */
    public static String getFileNameWithoutExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return filename;
        }
        return filename.substring(0, filename.lastIndexOf("."));
    }

    /**
     * 清理文件名中的非法字符
     * @param filename 原始文件名
     * @return 清理后的文件名
     */
    public static String sanitizeFileName(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unnamed";
        }
        // 移除路径分隔符和控制字符
        String sanitized = filename.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .trim();
        // 限制长度
        if (sanitized.length() > 200) {
            String ext = getFileExtension(sanitized);
            sanitized = sanitized.substring(0, 200 - ext.length() - 1) + "." + ext;
        }
        return sanitized.isEmpty() ? "unnamed" : sanitized;
    }

    /**
     * 检测文件MIME类型
     * @param inputStream 文件输入流
     * @return MIME类型
     */
    public static String detectMimeType(InputStream inputStream) throws IOException {
        return TIKA.detect(inputStream);
    }

    /**
     * 检测文件MIME类型
     * @param file 文件
     * @return MIME类型
     */
    public static String detectMimeType(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return detectMimeType(is);
        } catch (IOException e) {
            log.warn("检测文件类型失败: {}", file.getOriginalFilename(), e);
            return file.getContentType();
        }
    }

    /**
     * 验证文件类型
     * @param file 上传的文件
     * @param allowedTypes 允许的类型列表
     * @return 是否有效
     */
    public static boolean validateFileType(MultipartFile file, java.util.List<String> allowedTypes) {
        try {
            String detectedType = detectMimeType(file);
            if (detectedType == null) {
                return false;
            }
            // 检查检测到的类型是否在允许列表中
            boolean mimeMatch = allowedTypes.stream()
                    .anyMatch(type -> type.equalsIgnoreCase(detectedType));
            if (!mimeMatch) {
                log.warn("文件类型不匹配: detected={}, allowed={}", detectedType, allowedTypes);
                return false;
            }

            // 额外验证扩展名
            String extension = getFileExtension(file.getOriginalFilename());
            return validateExtensionByMimeType(extension, detectedType);
        } catch (Exception e) {
            log.error("验证文件类型失败", e);
            return false;
        }
    }

    /**
     * 根据MIME类型验证扩展名
     */
    private static boolean validateExtensionByMimeType(String extension, String mimeType) {
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg" -> extension.equals("jpg") || extension.equals("jpeg");
            case "image/png" -> extension.equals("png");
            case "image/gif" -> extension.equals("gif");
            case "image/webp" -> extension.equals("webp");
            case "image/bmp" -> extension.equals("bmp");
            default -> false;
        };
    }

    /**
     * 计算文件MD5哈希值
     * @param file 文件
     * @return MD5哈希值
     */
    public static String calculateMd5(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return calculateMd5(is);
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("计算文件MD5失败", e);
            return null;
        }
    }

    /**
     * 计算输入流的MD5哈希值
     */
    public static String calculateMd5(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            md.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(md.digest());
    }

    /**
     * 计算文件的MD5哈希值
     */
    public static String calculateFileMd5(Path filePath) {
        try (InputStream is = Files.newInputStream(filePath)) {
            return calculateMd5(is);
        } catch (Exception e) {
            log.error("计算文件MD5失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 格式化文件大小
     * @param size 文件大小（字节）
     * @return 格式化后的字符串
     */
    public static String formatFileSize(long size) {
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

    /**
     * 验证文件名安全性
     * @param filename 文件名
     * @throws FileValidationException 验证失败时抛出
     */
    public static void validateFileName(String filename) throws FileValidationException {
        if (filename == null || filename.isEmpty()) {
            throw FileValidationException.invalidFileName("文件名为空");
        }
        // 检查路径遍历攻击
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw FileValidationException.invalidFileName("包含非法字符");
        }
        // 检查空字节攻击
        if (filename.contains("\0")) {
            throw FileValidationException.invalidFileName("包含空字节");
        }
    }

    /**
     * 获取图片尺寸
     * @param file 图片文件
     * @return int数组 [width, height]，如果无法读取则返回null
     */
    public static int[] getImageDimensions(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(is);
            if (image != null) {
                return new int[]{image.getWidth(), image.getHeight()};
            }
        } catch (IOException e) {
            log.warn("读取图片尺寸失败: {}", file.getOriginalFilename(), e);
        }
        return null;
    }

    /**
     * 获取图片尺寸
     * @param filePath 图片文件路径
     * @return int数组 [width, height]，如果无法读取则返回null
     */
    public static int[] getImageDimensions(Path filePath) {
        try (InputStream is = Files.newInputStream(filePath)) {
            java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(is);
            if (image != null) {
                return new int[]{image.getWidth(), image.getHeight()};
            }
        } catch (IOException e) {
            log.warn("读取图片尺寸失败: {}", filePath, e);
        }
        return null;
    }

    /**
     * 创建缩略图文件名
     * @param originalFileName 原始文件名
     * @return 缩略图文件名
     */
    public static String generateThumbnailFileName(String originalFileName) {
        String nameWithoutExt = getFileNameWithoutExtension(originalFileName);
        return nameWithoutExt + "_thumb.jpg";
    }
}
