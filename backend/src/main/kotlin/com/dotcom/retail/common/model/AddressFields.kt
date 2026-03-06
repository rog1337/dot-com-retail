package com.dotcom.retail.common.model

import jakarta.persistence.Embeddable

@Embeddable
data class AddressFields(
    var streetLine1: String = "",
    var streetLine2: String? = null,

    var city: String = "",
    var stateOrProvince: String? = null,
    var postalCode: String? = null,

    var country: String = "",
)