package com.photostorage.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * XSS防护工具类
 * 提供输入过滤和转义功能
 */
@Slf4j
public class XssUtils {

    // 危险的HTML标签模式
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>[\\s\\S]*?</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile("\\s*on\\w+\\s*=\\s*\"[^\"]*\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_PROTOCOL = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_PROTOCOL = Pattern.compile("data:", Pattern.CASE_INSENSITIVE);
    private static final Pattern VBSCRIPT_PROTOCOL = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXPRESSION = Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE);

    // HTML特殊字符转义映射
    private static final String[][] HTML_ESCAPES = {
            {"&", "&amp;"},
            {"<", "&lt;"},
           {">", "&gt;"},
            {"\"", "&quot;"},
            {"'", "&#x27;"},
            {"/", "&#x2F;"}
    };

    /**
     * 清理HTML内容，移除危险标签和属性
     * @param input 原始输入
     * @return 清理后的内容
     */
    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String cleaned = input;

        // 移除script标签
        cleaned = SCRIPT_PATTERN.matcher(cleaned).replaceAll("");

        // 移除事件处理器
        cleaned = EVENT_HANDLER_PATTERN.matcher(cleaned).replaceAll("");

        // 移除危险协议
        cleaned = JAVASCRIPT_PROTOCOL.matcher(cleaned).replaceAll("");
        cleaned = DATA_PROTOCOL.matcher(cleaned).replaceAll("");
        cleaned = VBSCRIPT_PROTOCOL.matcher(cleaned).replaceAll("");

        // 移除CSS表达式
        cleaned = EXPRESSION.matcher(cleaned).replaceAll("");

        return cleaned;
    }

    /**
     * HTML转义
     * @param input 原始输入
     * @return 转义后的内容
     */
    public static String escapeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            boolean escaped = false;
            for (String[] escape : HTML_ESCAPES) {
                if (String.valueOf(c).equals(escape[0])) {
                    result.append(escape[1]);
                    escaped = true;
                    break;
                }
            }
            if (!escaped) {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 严格的HTML转义（推荐用于普通文本输出）
     * @param input 原始输入
     * @return 转义后的内容
     */
    public static String strictEscape(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 先进行清理
        String cleaned = sanitize(input);
        // 再进行转义
        return escapeHtml(cleaned);
    }

    /**
     * 检查是否包含XSS攻击向量
     * @param input 输入内容
     * @return 是否包含可疑内容
     */
    public static boolean containsXss(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        String lowerInput = input.toLowerCase();

        // 检查常见的XSS模式
        if (lowerInput.contains("<script")) return true;
        if (lowerInput.contains("javascript:")) return true;
        if (lowerInput.contains("onerror=")) return true;
        if (lowerInput.contains("onload=")) return true;
        if (lowerInput.contains("onclick=")) return true;
        if (lowerInput.contains("eval(")) return true;
        if (lowerInput.contains("expression(")) return true;
        if (lowerInput.contains("data:text/html")) return true;

        return false;
    }

    /**
     * 清理文件名中的XSS攻击向量
     * @param filename 文件名
     * @return 清理后的文件名
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unnamed";
        }

        // 先进行XSS清理
        String cleaned = sanitize(filename);

        // 移除特殊字符
        cleaned = cleaned.replaceAll("[<>'\"&]", "_");

        // 限制长度
        if (cleaned.length() > 200) {
            cleaned = cleaned.substring(0, 200);
        }

        return cleaned.isEmpty() ? "unnamed" : cleaned;
    }

    /**
     * 清理URL参数
     * @param param URL参数值
     * @return 清理后的参数
     */
    public static String sanitizeUrlParam(String param) {
        if (param == null || param.isEmpty()) {
            return param;
        }

        // 移除换行符
        String cleaned = param.replaceAll("[\\r\\n]", "");

        // 进行XSS清理
        cleaned = sanitize(cleaned);

        return cleaned;
    }
}
