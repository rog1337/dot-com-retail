package com.dotcom.retail.common.util.pagination

data class PageDto(
    val page: Int,
    val size: Int,
    val elements: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isLast: Boolean,
    val isFirst: Boolean,
)