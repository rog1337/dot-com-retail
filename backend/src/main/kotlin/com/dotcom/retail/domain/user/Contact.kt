package com.dotcom.retail.domain.user

import com.dotcom.retail.common.model.AddressFields
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Contact {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long = 0

    var name: String = ""
    var email: String = ""
    var phone: String = ""

    @Embedded
    var address: AddressFields = AddressFields()
}