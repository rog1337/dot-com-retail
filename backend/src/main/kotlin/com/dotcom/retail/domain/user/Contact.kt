package com.dotcom.retail.domain.user

import com.dotcom.retail.common.model.AddressFields
import com.dotcom.retail.common.model.AuditingEntity
import com.dotcom.retail.common.model.Contact
import jakarta.persistence.*

@Entity
@Table(name = "contacts")
class Contact(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long = 0,

    @Embedded
    var contact: Contact
) :  AuditingEntity()