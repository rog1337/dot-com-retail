package com.dotcom.retail.domain.catalogue.review.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.util.UUID

data class ReviewDto(
    val id: Long,
    val rating: Int,
    val body: String?,
    val votes: Int,
    val author: ReviewAuthorDto,
)

data class ReviewAuthorDto(
    val id: UUID,
    val displayName: String,
)

data class AddReviewRequest(
    @field:Min(1) @field:Max(5)
    val rating: Int,
    @field:Size(max = 512)
    val body: String? = null,
)