package com.photostorage.config;

import com.photostorage.security.interceptor.AntiHotlinkInterceptor;
import com.photostorage.security.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置CORS、拦截器、资源处理器等
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;
    private final SecurityProperties securityProperties;
    private final RateLimitInterceptor rateLimitInterceptor;

    /**
     * 配置跨域访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 防盗链拦截器
        if (securityProperties.isAntiHotlinkEnabled()) {
            registry.addInterceptor(new AntiHotlinkInterceptor(securityProperties))
                    .addPathPatterns("/files/**", "/photos/**")
                    .excludePathPatterns("/api/files/download/**");
        }

        // 限流拦截器
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/photos/**", "/files/**");
    }

    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传文件访问路径映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + storageProperties.getUploadDir() + "/");

        // 缩略图访问路径映射
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:" + storageProperties.getThumbnailDir() + "/");
    }
}
