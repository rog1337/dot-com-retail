package com.dotcom.retail.common.exception.catalogue

open class CategoryException : RuntimeException {
    constructor() : super(DEFAULT_MSG)
    constructor(message: String) : super(message)

    companion object {
        private const val DEFAULT_MSG = "Category error"
    }
}

class CategoryNotFoundException : CategoryException {
    constructor() : super(DEFAULT_MSG)
    constructor(category: Any) : super("$DEFAULT_MSG: $category")

    companion object {
        private const val DEFAULT_MSG = "Category not found"
    }
}