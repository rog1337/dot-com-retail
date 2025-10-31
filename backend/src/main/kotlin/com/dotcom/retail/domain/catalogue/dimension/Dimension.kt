package com.dotcom.retail.domain.catalogue.dimension

import jakarta.persistence.Embeddable

@Embeddable
data class Dimension(

    val widthMm: Double? = null,
    val heightMm: Double? = null,
    val lengthMm: Double? = null,

    val widthInch: Double? = null,
    val heightInch: Double? = null,
    val lengthInch: Double? = null,
) {
    companion object {
        fun fromMm(width: Double, height: Double, length: Double): Dimension {
            return Dimension(
                widthMm = width,
                heightMm = height,
                lengthMm = length,
                widthInch = width / 25.4,
                heightInch = height / 25.4,
                lengthInch = length?.div(25.4)
            )
        }
    }
}
