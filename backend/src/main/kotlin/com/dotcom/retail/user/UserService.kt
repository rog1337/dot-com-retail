package com.dotcom.retail.user

import com.dotcom.retail.auth.RegisterRequest

interface UserService {

    fun getByEmail(email: String): User?

    fun findByEmail(email: String): User?

    fun create(request: RegisterRequest): User

    fun save(user: User): User
}
