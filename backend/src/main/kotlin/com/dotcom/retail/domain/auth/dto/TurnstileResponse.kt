package com.dotcom.retail.domain.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TurnstileResponse(
    val success: Boolean,
    @JsonProperty("error-codes")
    val errorCodes: List<String>?,
    val hostname: String?,
    @JsonProperty("challenge_ts")
    val challengeTs: String?
)
