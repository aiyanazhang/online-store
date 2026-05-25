package com.photostorage.security.validator;

import com.photostorage.config.StorageProperties;
import com.photostorage.exception.FileValidationException;
import com.photostorage.exception.SecurityException;
import com.photostorage.utils.FileUtils;
import com.photostorage.utils.XssUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * 文件安全验证器
 * 负责验证上传文件的安全性
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileSecurityValidator {

    private final StorageProperties storageProperties;
    private static final Tika TIKA = new Tika();

    // 危险的文件扩展名黑名单
    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList(
            "exe", "dll", "bat", "cmd", "sh", "php", "jsp", "asp", "aspx",
            "jar", "war", "ear", "class", "py", "rb", "pl", "cgi"
    );

    // 危险的MIME类型
    private static final List<String> DANGEROUS_MIME_TYPES = Arrays.asList(
            "application/x-msdownload",
            "application/x-executable",
            "application/x-dosexec",
            "application/x-java-archive",
            "text/x-php",
            "application/x-httpd-php"
    );

    // 图片文件魔数（文件头标识）
    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] GIF_MAGIC = new byte[]{0x47, 0x49, 0x46, 0x38};
    private static final byte[] WEBP_MAGIC = new byte[]{0x52, 0x49, 0x46, 0x46};
    private static final byte[] BMP_MAGIC = new byte[]{0x42, 0x4D};

    /**
     * 全面验证文件安全性
     * @param file 上传的文件
     * @throws FileValidationException 验证失败
     * @throws SecurityException 安全威胁
     */
    public void validate(MultipartFile file) {
        // 1. 基本检查
        validateBasic(file);

        // 2. 文件名安全检查
        validateFileName(file.getOriginalFilename());

        // 3. 文件类型检查
        validateFileType(file);

        // 4. 文件大小检查
        validateFileSize(file);

        // 5. 文件魔数检查
        validateFileMagic(file);

        // 6. 内容安全检查
        validateContent(file);

        log.debug("文件安全验证通过: {}", file.getOriginalFilename());
    }

    /**
     * 基本检查
     */
    private void validateBasic(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw FileValidationException.emptyFile();
        }
    }

    /**
     * 文件名安全检查
     */
    private void validateFileName(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw FileValidationException.invalidFileName("文件名为空");
        }

        // XSS检查
        if (XssUtils.containsXss(filename)) {
            throw SecurityException.maliciousContent();
        }

        // 路径遍历检查
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw FileValidationException.invalidFileName("包含路径分隔符");
        }

        // 空字节检查
        if (filename.contains("\0")) {
            throw FileValidationException.invalidFileName("包含空字节");
        }

        // 扩展名黑名单检查
        String extension = FileUtils.getFileExtension(filename);
        if (DANGEROUS_EXTENSIONS.contains(extension.toLowerCase())) {
            throw FileValidationException.unsupportedType("危险的文件类型: " + extension);
        }
    }

    /**
     * 文件类型验证
     */
    private void validateFileType(MultipartFile file) {
        try {
            String detectedType;
            try (InputStream is = file.getInputStream()) {
                detectedType = TIKA.detect(is);
            }

            // 检查是否为危险的MIME类型
            if (DANGEROUS_MIME_TYPES.stream().anyMatch(detectedType::contains)) {
                log.warn("检测到危险的MIME类型: {}", detectedType);
                throw SecurityException.maliciousContent();
            }

            // 检查是否在允许的类型列表中
            List<String> allowedTypes = storageProperties.getAllowedTypes();
            if (allowedTypes == null || allowedTypes.isEmpty()) {
                return;
            }

            boolean allowed = allowedTypes.stream()
                    .anyMatch(type -> type.equalsIgnoreCase(detectedType));

            if (!allowed) {
                log.warn("不支持的文件类型: detected={}, allowed={}", detectedType, allowedTypes);
                throw FileValidationException.unsupportedType(detectedType);
            }

            // 验证扩展名与MIME类型匹配
            String extension = FileUtils.getFileExtension(file.getOriginalFilename());
            if (!isExtensionMatchMimeType(extension, detectedType)) {
                log.warn("文件扩展名与类型不匹配: extension={}, mime={}", extension, detectedType);
                throw FileValidationException.unsupportedType("文件扩展名与内容不匹配");
            }

        } catch (IOException e) {
            log.error("文件类型检测失败", e);
            throw new FileValidationException("文件类型检测失败");
        }
    }

    /**
     * 文件大小验证
     */
    private void validateFileSize(MultipartFile file) {
        long maxSize = storageProperties.getMaxFileSizeInBytes();
        if (file.getSize() > maxSize) {
            throw FileValidationException.fileTooLarge(maxSize);
        }

        // 检查空文件
        if (file.getSize() == 0) {
            throw FileValidationException.emptyFile();
        }
    }

    /**
     * 文件魔数验证（防止伪装扩展名）
     */
    private void validateFileMagic(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);

            if (read < 2) {
                throw FileValidationException.invalidFileName("无法读取文件头");
            }

            String extension = FileUtils.getFileExtension(file.getOriginalFilename()).toLowerCase();

            boolean valid = switch (extension) {
                case "jpg", "jpeg" -> startsWith(header, JPEG_MAGIC);
                case "png" -> startsWith(header, PNG_MAGIC);
                case "gif" -> startsWith(header, GIF_MAGIC);
                case "webp" -> startsWith(header, WEBP_MAGIC);
                case "bmp" -> startsWith(header, BMP_MAGIC);
                default -> false;
            };

            if (!valid) {
                log.warn("文件魔数不匹配: extension={}, header={}",
                        extension, bytesToHex(header));
                throw FileValidationException.unsupportedType("文件内容格式不正确");
            }

        } catch (IOException e) {
            log.error("文件魔数验证失败", e);
            throw new FileValidationException("文件验证失败");
        }
    }

    /**
     * 文件内容安全检查
     */
    private void validateContent(MultipartFile file) {
        // 检查文件名中是否包含可执行代码特征
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String lowerName = filename.toLowerCase();

            // 检查双重扩展名攻击
            if (lowerName.matches(".*\\.(jpg|jpeg|png|gif|webp|bmp)\\.(exe|php|jsp|asp).*")) {
                log.warn("检测到双重扩展名攻击: {}", filename);
                throw SecurityException.maliciousContent();
            }
        }
    }

    /**
     * 检查扩展名是否与MIME类型匹配
     */
    private boolean isExtensionMatchMimeType(String extension, String mimeType) {
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
     * 检查字节数组是否以指定魔数开头
     */
    private boolean startsWith(byte[] data, byte[] magic) {
        if (data.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (data[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
