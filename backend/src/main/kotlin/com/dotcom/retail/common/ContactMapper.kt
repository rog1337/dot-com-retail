package com.dotcom.retail.common

import com.dotcom.retail.common.model.AddressFields
import com.dotcom.retail.common.model.Contact
import com.dotcom.retail.common.service.EncryptionService
import org.springframework.stereotype.Component

@Component
class ContactMapper(
    private val encryptionService: EncryptionService
) {

    fun decryptContact(c: Contact): Contact {
        return Contact(
            name = encryptionService.decrypt(c.name),
            email = encryptionService.decrypt(c.email),
            phone = encryptionService.decrypt(c.phone),
            address = decryptAddress(c.address),
        )
    }

    fun decryptAddress(a: AddressFields): AddressFields {
        return AddressFields(
            streetLine1 = encryptionService.decrypt(a.streetLine1),
            streetLine2 = a.streetLine2?.let { encryptionService.decrypt(it) },
            city = encryptionService.decrypt(a.city),
            stateOrProvince = a.stateOrProvince?.let { encryptionService.decrypt(it) },
            postalCode = a.postalCode?.let { encryptionService.decrypt(it) },
            country = encryptionService.decrypt(a.country),
        )
    }
}