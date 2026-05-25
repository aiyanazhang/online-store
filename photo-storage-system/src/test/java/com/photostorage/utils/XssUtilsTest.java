package com.photostorage.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XSS防护工具类单元测试
 */
class XssUtilsTest {

    @Test
    void sanitize_ShouldRemoveScriptTags() {
        String input = "<script>alert('xss')</script>Hello";
        String result = XssUtils.sanitize(input);
        assertFalse(result.contains("<script>"));
        assertTrue(result.contains("Hello"));
    }

    @Test
    void sanitize_ShouldRemoveEventHandlers() {
        String input = "<img src='x' onerror='alert(1)'>";
        String result = XssUtils.sanitize(input);
        assertFalse(result.contains("onerror"));
    }

    @Test
    void sanitize_ShouldRemoveJavaScriptProtocol() {
        String input = "<a href='javascript:alert(1)'>Click</a>";
        String result = XssUtils.sanitize(input);
        assertFalse(result.toLowerCase().contains("javascript:"));
    }

    @Test
    void escapeHtml_ShouldEscapeSpecialChars() {
        String input = "<div>Test & Demo</div>";
        String result = XssUtils.escapeHtml(input);
        assertFalse(result.contains("<"));
        assertFalse(result.contains(">"));
        assertTrue(result.contains("&lt;"));
        assertTrue(result.contains("&gt;"));
    }

    @Test
    void strictEscape_ShouldSanitizeAndEscape() {
        String input = "<script>alert('xss')</script><b>Safe</b>";
        String result = XssUtils.strictEscape(input);
        assertFalse(result.contains("<script>"));
        assertTrue(result.contains("&lt;"));
    }

    @Test
    void containsXss_ShouldDetectXssPatterns() {
        assertTrue(XssUtils.containsXss("<script>alert(1)</script>"));
        assertTrue(XssUtils.containsXss("javascript:alert(1)"));
        assertTrue(XssUtils.containsXss("onerror=alert(1)"));
        assertTrue(XssUtils.containsXss("eval(something)"));
        assertFalse(XssUtils.containsXss("Hello World"));
        assertFalse(XssUtils.containsXss("This is a normal text"));
    }

    @Test
    void sanitizeFilename_ShouldRemoveXss() {
        String input = "<script>alert(1)</script>file.jpg";
        String result = XssUtils.sanitizeFilename(input);
        assertFalse(result.contains("<script>"));
        assertTrue(result.endsWith(".jpg"));
    }

    @Test
    void sanitizeUrlParam_ShouldRemoveNewlines() {
        String input = "param\r\nvalue";
        String result = XssUtils.sanitizeUrlParam(input);
        assertFalse(result.contains("\r"));
        assertFalse(result.contains("\n"));
    }

    @Test
    void sanitize_ShouldHandleNull() {
        assertNull(XssUtils.sanitize(null));
    }

    @Test
    void escapeHtml_ShouldHandleNull() {
        assertNull(XssUtils.escapeHtml(null));
    }
}
