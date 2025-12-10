package com.dotcom.retail.common.exception

class BrandNotFoundException(id: Any) : ResourceNotFoundException("Brand", id)
class CategoryNotFoundException(id: Any) : ResourceNotFoundException("Category", id)
class ProductNotFoundException(id: Any) : ResourceNotFoundException("Product", id)