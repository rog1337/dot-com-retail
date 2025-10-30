package com.dotcom.retail.catalogue.product

class ProductNotFoundException : RuntimeException {
    companion object {
        private const val DEFAULT_MSG = "Product not found"
    }
    constructor() : super(DEFAULT_MSG)
    constructor(product: Any) : super("$DEFAULT_MSG $product")
}