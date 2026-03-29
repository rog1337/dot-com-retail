package com.dotcom.retail.domain.admin.review.dto

import com.dotcom.retail.domain.admin.user.dto.AdminUserDto

data class AdminReviewDto(
    val id: Long,
    val rating: Int,
    val body: String?,
    val votes: Int,
    val author: AdminUserDto,
)

data class DeleteReviewRequest(
    val cause: String,
)