package com.photostorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装类
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 当前页码 */
    private Integer pageNum;

    /** 每页大小 */
    private Integer pageSize;

    /** 总记录数 */
    private Long total;

    /** 总页数 */
    private Integer totalPages;

    /** 数据列表 */
    private List<T> list;

    /** 是否有下一页 */
    private Boolean hasNext;

    /** 是否有上一页 */
    private Boolean hasPrevious;

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(Integer pageNum, Integer pageSize, Long total, List<T> list) {
        int totalPages = (int) Math.ceil((double) total / pageSize);
        return PageResult.<T>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(total)
                .totalPages(totalPages)
                .list(list)
                .hasNext(pageNum < totalPages)
                .hasPrevious(pageNum > 1)
                .build();
    }
}
