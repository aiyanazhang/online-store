package com.photostorage.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 配置Caffeine缓存管理器
 */
@Configuration
public class CacheConfig {

    public static final String FILE_METADATA_CACHE = "fileMetadataCache";
    public static final String THUMBNAIL_CACHE = "thumbnailCache";
    public static final String STORAGE_STATS_CACHE = "storageStatsCache";

    /**
     * 主缓存管理器
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .recordStats());
        cacheManager.setCacheNames(java.util.Arrays.asList(
                FILE_METADATA_CACHE,
                THUMBNAIL_CACHE,
                STORAGE_STATS_CACHE
        ));
        return cacheManager;
    }

    /**
     * 文件元数据缓存配置
     */
    @Bean
    public Caffeine<Object, Object> fileMetadataCaffeine() {
        return Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats();
    }
}
