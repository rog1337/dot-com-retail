package com.dotcom.retail.domain.user

interface UserService {

    fun getByEmail(email: String): User

    fun findByEmail(email: String): User?

    fun create(request: CreateUserParams): User

    fun save(user: User): User
}
