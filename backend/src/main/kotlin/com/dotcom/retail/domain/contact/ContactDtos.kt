package com.dotcom.retail.domain.contact

data class ContactRequest(
    val name: String,
    val email: String,
    val message: String
)