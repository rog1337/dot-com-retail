package com.dotcom.retail.common.util.pagination

data class PagedResponse<T>(
    val content: List<T>,
    val page: PageDto,
)