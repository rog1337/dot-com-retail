package com.dotcom.retail.domain.admin.review.dto

import com.dotcom.retail.domain.catalogue.review.dto.ReviewAuthorDto

data class AdminReviewDto(
    val id: Long,
    val rating: Int,
    val body: String?,
    val votes: Int,
    val author: ReviewAuthorDto,
)

data class DeleteReviewRequest(
    val cause: String,
)