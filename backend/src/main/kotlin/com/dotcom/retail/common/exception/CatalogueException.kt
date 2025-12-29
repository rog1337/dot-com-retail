package com.dotcom.retail.common.exception

class BrandNotFoundException(id: Any? = null) : ResourceNotFoundException("Brand", id)
class BrandAlreadyExistsException(id: Any? = null) : AlreadyExistsException("Brand", id)
class CategoryNotFoundException(id: Any? = null) : ResourceNotFoundException("Category", id)
class ProductNotFoundException(id: Any? = null) : ResourceNotFoundException("Product", id)
class ImageNotFoundException(id: Any? = null) : ResourceNotFoundException("Image", id)