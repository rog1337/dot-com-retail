package com.dotcom.retail.common.exception.product

open class ProductException : RuntimeException {
    constructor() : super(DEFAULT_MSG)
    constructor(message: String) : super(message)

    companion object {
        private const val DEFAULT_MSG = "Product error"
    }
}

class ProductNotFoundException : ProductException {
    constructor() : super(DEFAULT_MSG)
    constructor(product: Any) : super("$DEFAULT_MSG $product")

    companion object {
        private const val DEFAULT_MSG = "Product not found"
    }
}