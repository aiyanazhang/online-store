package com.photostorage.repository;

import com.photostorage.entity.PhotoFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 照片文件数据访问层
 */
@Repository
public interface PhotoFileRepository extends JpaRepository<PhotoFile, Long> {

    /**
     * 根据文件名查询
     */
    Optional<PhotoFile> findByFileName(String fileName);

    /**
     * 根据文件哈希查询（用于去重）
     */
    Optional<PhotoFile> findByFileHash(String fileHash);

    /**
     * 根据ID和删除状态查询
     */
    Optional<PhotoFile> findByIdAndIsDeletedFalse(Long id);

    /**
     * 查询所有未删除的文件（分页）
     */
    Page<PhotoFile> findAllByIsDeletedFalse(Pageable pageable);

    /**
     * 根据原始文件名模糊查询
     */
    @Query("SELECT p FROM PhotoFile p WHERE p.isDeleted = false AND p.originalName LIKE %:keyword%")
    Page<PhotoFile> findByOriginalNameContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询今日上传的文件数量
     */
    @Query("SELECT COUNT(p) FROM PhotoFile p WHERE p.isDeleted = false AND p.uploadTime >= :startOfDay")
    Long countTodayUploads(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 查询今日上传的总大小
     */
    @Query("SELECT COALESCE(SUM(p.fileSize), 0) FROM PhotoFile p WHERE p.isDeleted = false AND p.uploadTime >= :startOfDay")
    Long sumTodayUploadSize(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 查询总下载次数
     */
    @Query("SELECT COALESCE(SUM(p.downloadCount), 0) FROM PhotoFile p WHERE p.isDeleted = false")
    Long sumDownloadCount();

    /**
     * 查询已用存储空间
     */
    @Query("SELECT COALESCE(SUM(p.fileSize), 0) FROM PhotoFile p WHERE p.isDeleted = false")
    Long sumUsedStorage();

    /**
     * 查询指定时间之前的文件
     */
    @Query("SELECT p FROM PhotoFile p WHERE p.isDeleted = false AND p.lastAccessTime < :beforeDate")
    List<PhotoFile> findByLastAccessTimeBefore(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 查询指定时间之前的文件（从未访问过）
     */
    @Query("SELECT p FROM PhotoFile p WHERE p.isDeleted = false AND p.lastAccessTime IS NULL AND p.uploadTime < :beforeDate")
    List<PhotoFile> findNeverAccessedBefore(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 软删除文件
     */
    @Modifying
    @Query("UPDATE PhotoFile p SET p.isDeleted = true WHERE p.id = :id")
    void softDeleteById(@Param("id") Long id);

    /**
     * 增加下载次数
     */
    @Modifying
    @Query("UPDATE PhotoFile p SET p.downloadCount = p.downloadCount + 1, p.lastAccessTime = CURRENT_TIMESTAMP WHERE p.id = :id")
    void incrementDownloadCount(@Param("id") Long id);

    /**
     * 更新最后访问时间
     */
    @Modifying
    @Query("UPDATE PhotoFile p SET p.lastAccessTime = CURRENT_TIMESTAMP WHERE p.id = :id")
    void updateLastAccessTime(@Param("id") Long id);

    /**
     * 检查文件名是否存在
     */
    boolean existsByFileName(String fileName);
}
