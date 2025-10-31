package com.dotcom.retail.domain.catalogue.product

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
class Tyre(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "product_id")
    var product: Product,

    var width: Int,
    var height: Int,
    var diameter: String,

    var season: String? = null,
    var studded: Boolean? = null,

    var loadIndex: Int? = null,
    var speedIndex: String? = null,

    var fuelEfficiency: String? = null,
    var noiseLevel: Int? = null,
    var rimProtection: Boolean? = null,
    var runFlat: Boolean? = null,
)