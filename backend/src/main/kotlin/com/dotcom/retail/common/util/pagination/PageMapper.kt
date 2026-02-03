package com.dotcom.retail.common.util.pagination

import org.springframework.data.domain.Page

object PageMapper {
    fun <T>toPagedResponse(page: Page<T>): PagedResponse<T> {
        return PagedResponse(
            content = page.content,
            page = toPageDto(page)
        )
    }

    fun <T>toPageDto(page: Page<T>): PageDto {
        return PageDto(
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            isLast = page.isLast
        )
    }
}