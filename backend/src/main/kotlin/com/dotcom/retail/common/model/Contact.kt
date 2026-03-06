package com.dotcom.retail.common.model

import jakarta.persistence.Embeddable

@Embeddable
data class Contact(
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var address: AddressFields = AddressFields()
)
