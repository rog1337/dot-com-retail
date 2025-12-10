package com.dotcom.retail.common.exception

class UserNotFoundException(id: Any) : ResourceNotFoundException("User", id)
class EmailNotFoundException(id: Any) : ResourceNotFoundException("Email", id)
class EmailAlreadyRegisteredException(id: Any) : AlreadyExistsException("Email", id)