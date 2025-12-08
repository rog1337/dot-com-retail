package com.dotcom.retail.common.exception.catalogue

open class BrandException : RuntimeException {
    constructor() : super(DEFAULT_MSG)
    constructor(message: String) : super(message)

    companion object {
        private const val DEFAULT_MSG = "Brand error"
    }
}

class BrandNotFoundException : BrandException {
    constructor() : super(DEFAULT_MSG)
    constructor(brand: Any) : super("$DEFAULT_MSG: $brand")

    companion object {
        private const val DEFAULT_MSG = "Brand not found"
    }
}